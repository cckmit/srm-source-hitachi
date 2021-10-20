package org.srm.source.rfx.app.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hzero.boot.customize.service.CustomizeClient;
import org.hzero.boot.file.FileClient;
import org.hzero.boot.message.util.DateUtils;
import org.hzero.boot.platform.lov.adapter.LovAdapter;
import org.hzero.core.base.AopProxy;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.util.EncoderUtils;
import org.hzero.mybatis.domian.Condition;
import org.hzero.mybatis.util.Sqls;
import org.hzero.starter.keyencrypt.core.IEncryptionService;
import org.hzero.mybatis.domian.Condition;
import org.hzero.mybatis.util.Sqls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StreamUtils;
import org.srm.boot.platform.reltable.RelTableHelper;
import org.srm.source.bid.infra.constant.BidConstants;
import org.srm.source.rfx.api.dto.*;
import org.srm.source.rfx.app.service.HitachiRfxHeaderPrintService;
import org.srm.source.rfx.domain.entity.RfxHeader;
import org.srm.source.rfx.domain.entity.RfxLineItem;
import org.srm.source.rfx.domain.entity.RfxLineSupplier;
import org.srm.source.rfx.domain.repository.*;
import org.srm.source.rfx.infra.mapper.HitachiRfxHeaderPrintMapper;
import org.srm.source.share.api.dto.Node;
import org.srm.source.share.domain.entity.User;
import org.srm.source.share.infra.constant.HitachiConstants;
import org.srm.source.share.infra.constant.ShareConstants;
import org.srm.source.share.infra.utils.PdfUtil;

/**
 * @author guotao.yu@hand-china.com 2021/3/22 下午8:39
 */
@Service
public class HitachiRfxHeaderPrintServiceImpl implements HitachiRfxHeaderPrintService, AopProxy<HitachiRfxHeaderPrintService> {
    @Autowired
    private HitachiRfxHeaderPrintRepository hitachiRfxHeaderPrintRepository;
    @Autowired
    private HitachiRfxHeaderPrintMapper hitachiRfxHeaderPrintMapper;
    @Autowired
    private RfxHeaderRepository rfxHeaderRepository;
    @Autowired
    private RfxLineSupplierRepository rfxLineSupplierRepository;
    @Autowired
    private PdfUtil pdfUtil;
    @Autowired
    private FileClient fileClient;
    @Autowired
    private CommonQueryRepository commonQueryRepository;
    @Autowired
    CustomizeClient customizeClient;
    @Autowired
    LovAdapter lovAdapter;
    @Autowired
    private RfxLineItemRepository rfxLineItemRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(HitachiRfxHeaderPrintServiceImpl.class);
    @Autowired
    private IEncryptionService iEncryptionService;

    @Override
    public Page<HitachiRfxPrintDTO> rfxPrintListQuery(HitachiRfxPrintQueryDTO hitachiRfxPrintQueryDTO, PageRequest pageRequest){
        Page<HitachiRfxPrintDTO> hitachiRfxPrintDTOS = hitachiRfxHeaderPrintRepository.rfxPrintListQuery(hitachiRfxPrintQueryDTO, pageRequest);
        //查询创建人部门
        hitachiRfxPrintDTOS.forEach(hitachiRfxPrintDTO -> {
            List<HitachiUnit> hitachiUnits = hitachiRfxHeaderPrintMapper.selectUserOwnDepartment(hitachiRfxPrintQueryDTO.getTenantId(), hitachiRfxPrintDTO.getCreatedBy());
            String createdByUnitName = hitachiUnits.stream().map(HitachiUnit::getUnitName).collect(Collectors.joining(","));
            hitachiRfxPrintDTO.setCreatedByUnitName(createdByUnitName);
        });
        return hitachiRfxPrintDTOS;
    }

    @Override
    public void postRfxPrintPackDownload(HttpServletRequest request, HttpServletResponse response, Long tenantId, HitachiSelectedPrintDTO hitachiSelectedPrintDTO) {
        if(null == hitachiSelectedPrintDTO){
            return;
        }
        //获取勾选行
        List<HitachiRfxPrintDTO> hitachiRfxPrintDTOList = hitachiSelectedPrintDTO.getHitachiRfxPrintDTOList();
        if(CollectionUtils.isEmpty(hitachiRfxPrintDTOList)){
            return;
        }
        Date now = new Date();
        List<HitachiFileInfoDTO> fileInfoList = new ArrayList<>();
        hitachiRfxPrintDTOList.forEach(hitachiRfxPrintDTO -> {
            HitachiRfxConsigneeDTO consigneeDTO = new HitachiRfxConsigneeDTO();
            Long rfxHeaderId = hitachiRfxPrintDTO.getRfxHeaderId();
            //获取已打印过的数据且注文書の送付先的值不为空，则按原来的值打印，否则按最新选择的值进行打印
            if(BaseConstants.Flag.YES.equals(null == hitachiRfxPrintDTO.getAttributeTinyint1() ? BaseConstants.Flag.NO : hitachiRfxPrintDTO.getAttributeTinyint1()) && StringUtils.isNotBlank(hitachiRfxPrintDTO.getDeliveryAddress())){
                consigneeDTO.setDeliveryAddress(hitachiRfxPrintDTO.getDeliveryAddress());
                consigneeDTO.setPosition(hitachiRfxPrintDTO.getPosition());
                consigneeDTO.setApplicant(hitachiRfxPrintDTO.getApplicant());
            }else{
                consigneeDTO = hitachiSelectedPrintDTO.getHitachiRfxConsigneeDTO();
            }
            RfxHeader rfxHeader = rfxHeaderRepository.selectByPrimaryKey(rfxHeaderId);
            customizeClient.translateResult(rfxHeader, "SSRC.INQUIRY_HALL.NEW_EDIT.INFO_V2");
            //获取打印行
            List<HitachiRfxLineItemPrintDTO> hitachiRfxLineItemPrintDTOS = hitachiRfxHeaderPrintMapper.rfxLineItemPrintQuery(tenantId, rfxHeaderId);
            //获取行数
            int lineCount = hitachiRfxLineItemPrintDTOS.size();
            //每页打印四行，获取需要打印的页数
            int pageCount = lineCount / 4;
            //取余数
            int remainder = lineCount % 4;
            if(remainder > 0 ){
                pageCount = pageCount + 1;
            }
            Node totalRoot = Node.getDefault();
            //分页打印处理
            for(int i=0; i< pageCount; i++){
                List<HitachiRfxLineItemPrintDTO> pageList =new ArrayList<>();
                int endSize = 0;
                int startSize = 4*i;
                if(i == pageCount-1 && remainder !=0){
                    endSize = 4*(i) + remainder;
                }else{
                    endSize = 4*(i+1);
                }
                for(int j = startSize; j< endSize; j++){
                    pageList.add(hitachiRfxLineItemPrintDTOS.get(j));
                }
                int currentPage = i+1;
                this.processPageRfxPrint(tenantId, fileInfoList, rfxHeader, pageList, totalRoot, now,consigneeDTO);
            }
            String pdfNamePrompt = null;
            String pdfUrl = null;
            List<HitachiUnit> createdByUnits = hitachiRfxHeaderPrintMapper.selectUserOwnDepartment(tenantId, rfxHeader.getCreatedBy());
            String createdByUnit = "";
            if(CollectionUtils.isNotEmpty(createdByUnits)){
                createdByUnit = createdByUnits.get(0).getUnitName() + "_";
            }
            //订单类别AttributeVarchar1,相关法规attributeVarchar14
            if("2".equals(rfxHeader.getAttributeVarchar14())) {
                pdfNamePrompt = "注文内示書（下請法）_" + createdByUnit + DateUtils.format(now,"yyyyMMdd") + ".pdf";
                pdfUrl = pdfUtil.originalReport(tenantId, pdfNamePrompt, "PDF", "SSRC.SOURCE_HEADER_SUBCONTRACT_PRINT", totalRoot);
            }else{
                pdfNamePrompt = "見積依頼書_" + createdByUnit + DateUtils.format(now,"yyyyMMdd") + ".pdf";
                pdfUrl = pdfUtil.originalReport(tenantId, pdfNamePrompt, "PDF", "SSRC.SOURCE_HEADER_COMMON_PRINT", totalRoot);
            }
            HitachiFileInfoDTO hitachiFileInfoDTO = new HitachiFileInfoDTO();
            hitachiFileInfoDTO.setFileName(pdfNamePrompt);
            hitachiFileInfoDTO.setDownloadUrl(pdfUrl);
            hitachiFileInfoDTO.setType(rfxHeader.getAttributeVarchar14());
            fileInfoList.add(hitachiFileInfoDTO);
        });
        //根据附件名称进行分组
        Map<String, List<HitachiFileInfoDTO>> fileInfoGroupByName = fileInfoList.stream().collect(Collectors.groupingBy(HitachiFileInfoDTO::getFileName));
        for (Map.Entry<String, List<HitachiFileInfoDTO>> rfxProcessAttachmentEntry : fileInfoGroupByName.entrySet()){
            //获取当前附件名称存在的文件个数
            int size = rfxProcessAttachmentEntry.getValue().size();
            //如果附件数大于1，则需要重命名
            if(size >1){
                //最后一个点的位置
                int lastPointIndex = rfxProcessAttachmentEntry.getValue().get(0).getFileName().lastIndexOf(".");
                String firstFileName = rfxProcessAttachmentEntry.getValue().get(0).getFileName().substring(0, lastPointIndex);
                String LastFileName = rfxProcessAttachmentEntry.getValue().get(0).getFileName().substring(lastPointIndex);
                for(int i=0; i<rfxProcessAttachmentEntry.getValue().size() ;i++){
                    if(i==0){
                        continue;
                    }
                    String fileNameChange=firstFileName + "(" + i + ")" + LastFileName;
                    rfxProcessAttachmentEntry.getValue().get(i).setFileName(fileNameChange);
                }
            }
        }
        //处理文件进行打包压缩
        response.setHeader("Content-Disposition", String.format("attachment;filename=%s", EncoderUtils.encodeFilename("注文内示書兼見積依頼書.zip")));
        response.setContentType("application/octet-stream; charset=" + StandardCharsets.UTF_8.displayName());
        response.setCharacterEncoding(StandardCharsets.UTF_8.displayName());
        try (ServletOutputStream out = response.getOutputStream(); ZipOutputStream zos = new ZipOutputStream(out)) {
            for (HitachiFileInfoDTO fileInfoDTO : fileInfoList) {
                AsyncResult<InputStream> asyncInputStream = new AsyncResult<>(fileClient.downloadFile(tenantId, ShareConstants.BucketName.PRIVATE_BUCKET, fileInfoDTO.getDownloadUrl()));
                if("2".equals(fileInfoDTO.getType())) {
                    zos.putNextEntry(new ZipEntry("注文内示書（下請法）" + "/" + fileInfoDTO.getFileName()));
                }else{
                    zos.putNextEntry(new ZipEntry("見積依頼書" + "/" + fileInfoDTO.getFileName()));
                }
                Optional.ofNullable(asyncInputStream.get(700, TimeUnit.MILLISECONDS)).ifPresent(in -> {
                    try {
                        StreamUtils.copy(in, zos);
                    } catch (IOException ex) {
                        LOGGER.error("Hitachi rfx stream copy error,{}",ex.getMessage());
                    }
                });
            }
        } catch (Exception ex) {
            throw new CommonException(BidConstants.ErrorCode.ERROR_PACKAGE_DOWNLOAD_ERROR);
        }
    }

    private void processPageRfxPrint(Long tenantId, List<HitachiFileInfoDTO> fileInfoList, RfxHeader rfxHeader, List<HitachiRfxLineItemPrintDTO> hitachiRfxLineItemPrintDTOS, Node totalRoot, Date now,HitachiRfxConsigneeDTO consigneeDTO) {
        String isType="";//用来区分是否是第一次打印
        DateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日");
        if(!BaseConstants.Flag.YES.equals(rfxHeader.getAttributeTinyint1()) && null == rfxHeader.getAttributeDatetime1()){
            isType="1";
            rfxHeader.setAttributeTinyint1(BaseConstants.Flag.YES);
            rfxHeader.setAttributeDatetime1(now);
            rfxHeaderRepository.updateOptional(rfxHeader,"attributeTinyint1","attributeDatetime1");
        }
        List<RfxLineSupplier> rfxLineSupplierList = rfxLineSupplierRepository.select(new RfxLineSupplier(rfxHeader.getRfxHeaderId()));
        if(CollectionUtils.isEmpty(rfxLineSupplierList)){
            return;
        }
        if(!ObjectUtils.isEmpty(consigneeDTO)){
            rfxLineSupplierList.forEach(rfxLineSupplier -> {
                rfxLineSupplier.setAttributeVarchar1(consigneeDTO.getDeliveryAddress());
                rfxLineSupplier.setAttributeVarchar2(consigneeDTO.getPosition());
                rfxLineSupplier.setAttributeVarchar3(consigneeDTO.getApplicant());
            });
            rfxLineSupplierRepository.batchUpdateOptional(rfxLineSupplierList, "attributeVarchar1", "attributeVarchar2", "attributeVarchar3");
        }
        //因为日立物流只会邀请一个供应商，所以这里直接获取第一个供应商 多个报错
        if (rfxLineSupplierList.size() > 1) {
            throw new CommonException(HitachiConstants.ErrorCode.ERROR_SSRC_QUOTATION_SUPPLIER_MORE);
        }
        RfxLineSupplier rfxLineSupplier = rfxLineSupplierList.get(0);
        Node root = totalRoot.addNodeGroup("header");
        //-----------------头信息处理--------------------
        //供应商名称
        root.addNode("supplierCompanyName" ,rfxLineSupplier.getSupplierCompanyName());
        if (!ObjectUtils.isEmpty(consigneeDTO)) {
            //初始化打印数据
            consigneeDTO.initPrintData();
            //供应商联系人名称
            root.addNode("supplierContactName", consigneeDTO.getApplicant());
            //供应商联系人职位
            root.addNode("supplierContactPosition", consigneeDTO.getPosition());
            //供应商联系人职位
            root.addNode("supplierContactPositionAndName", consigneeDTO.getPosition() + BaseConstants.Symbol.SPACE + consigneeDTO.getApplicant());
            //供应商联系人部门
            root.addNode("supplierContactUnitName", consigneeDTO.getDeliveryAddress());
        } else {
            //供应商联系人职位和名称
            root.addNode("supplierContactPositionAndName", BaseConstants.Symbol.SPACE);
            //供应商联系人部门
            root.addNode("supplierContactUnitName", BaseConstants.Symbol.SPACE);
        }
        //第一次打印日期
        root.addNode("attributeDatetime1" ,simpleDateFormat.format(null == rfxHeader.getAttributeDatetime1() ? now : rfxHeader.getAttributeDatetime1()));
        //区分首次打印和再次打印
        if ("1".equals(isType)) {
            root.addNode("INDEX", "注文内示書兼見積依頼書");
        } else {
            root.addNode("INDEX", "注文内示書兼見積依頼書（再印刷）");
        }
        //公司名称
        root.addNode("companyName" ,rfxHeader.getCompanyName());
        //登录人部门
        String loginUnitName = null;
        List<HitachiUnit> hitachiUnits = hitachiRfxHeaderPrintMapper.selectUserOwnDepartment(tenantId, DetailsHelper.getUserDetails().getUserId());
        if(CollectionUtils.isNotEmpty(hitachiUnits)){
            //获取登录人部门,因为日立物流一个员工只会有一个部门，所以直接获取第一行
            HitachiUnit hitachiUnit = hitachiUnits.get(0);
            List<String> unitNameList  = new ArrayList<>();
            //获取上下级所有部门
            this.getUnitName(hitachiUnit, unitNameList);
            //将list进行倒序
            Collections.reverse(unitNameList);
            loginUnitName = unitNameList.stream().collect(Collectors.joining("\n"));
        }
        root.addNode("loginUnitName" ,loginUnitName);
        //库存组织telephone
        //String mobile = hitachiRfxHeaderPrintMapper.invOrganizationTelQuery(tenantId, rfxHeader.getRfxHeaderId());
        String mobile = "";
        List<RfxLineItem> rfxLineItemList = rfxLineItemRepository.selectByCondition(Condition.builder(RfxLineItem.class)
                .andWhere(Sqls.custom()
                        .andEqualTo(RfxLineItem.FIELD_TENANT_ID, tenantId)
                        .andEqualTo(RfxLineItem.FIELD_RFX_HEADER_ID, rfxHeader.getRfxHeaderId())
                        .andEqualTo(RfxLineItem.FIELD_CURRENT_ROUND_NUMBER, rfxHeader.getRoundNumber())
                        .andIsNotNull(RfxLineItem.FIELD_OU_ID)
                ).build());
        //已经让业务跟客户确认完只要去业务实体第一行数据去查询电话号码
        if(CollectionUtils.isNotEmpty(rfxLineItemList)){
            mobile = hitachiRfxHeaderPrintMapper.selectOuTelPhone(tenantId, rfxLineItemList.get(0).getOuId());
        }
        root.addNode("telephone" ,mobile);
        //币种
        root.addNode("currencyName" ,hitachiRfxHeaderPrintMapper.selectValidCurrency(tenantId, rfxHeader.getCurrencyCode()));
        //-------------下請法---------------
        if("2".equals(rfxHeader.getAttributeVarchar14())) {
            //支払方法
            root.addNode("attributeVarchar15", rfxHeader.get_innerMap().get("attributeVarchar15Meaning"));
            //隐私发布理由（下）详细
            root.addNode("attributeVarchar21", rfxHeader.getAttributeVarchar21());
            //检验完成日期（下）
            root.addNode("attributeVarchar39", null == rfxHeader.getAttributeVarchar39() ? "" :
                    DateUtils.format(DateUtils.parseDate(rfxHeader.getAttributeVarchar39(), BaseConstants.Pattern.DATETIME), BaseConstants.Pattern.SYS_DATE));
            //预定日期（下）
            root.addNode("attributeVarchar38", null == rfxHeader.getAttributeVarchar38() ? "" :
                    DateUtils.format(DateUtils.parseDate(rfxHeader.getAttributeVarchar38(), BaseConstants.Pattern.DATETIME), BaseConstants.Pattern.SYS_DATE));
        }
        hitachiRfxLineItemPrintDTOS.forEach(hitachiRfxLineItemPrintDTO -> {
            String releaseApprovedBy = null;
            User userInfo = commonQueryRepository.getUserInfoById(rfxHeader.getAttributeBigint1());
            if(null != userInfo){
                releaseApprovedBy = userInfo.getRealName();
            }
            Node table = root.addNodeGroup("rfxItemPrintLines");
            table.addNode("rfxNum",hitachiRfxLineItemPrintDTO.getRfxNum());
            table.addNode("itemName",hitachiRfxLineItemPrintDTO.getItemName());
            table.addNode("model",hitachiRfxLineItemPrintDTO.getModel());
            table.addNode("rfxQuantity",hitachiRfxLineItemPrintDTO.getRfxQuantity());
            table.addNode("uomName",hitachiRfxLineItemPrintDTO.getUomName());
            table.addNode("demandDate",null == hitachiRfxLineItemPrintDTO.getDemandDate() ? "" :DateUtils.format(hitachiRfxLineItemPrintDTO.getDemandDate(),"yyyy/MM/dd"));
            table.addNode("releaseApprovedBy",releaseApprovedBy);
            table.addNode("currentDate", DateUtils.format(now,"yyyy/MM/dd"));
            table.addNode("invTelephone",hitachiRfxLineItemPrintDTO.getInvTelephone());
            table.addNode("receiveAddress",hitachiRfxLineItemPrintDTO.getReceiveAddress());
            table.addNode("receiveContactName",hitachiRfxLineItemPrintDTO.getReceiveContactName());
            table.addNode("loginRealName",DetailsHelper.getUserDetails().getRealName());
        });
    }

    /**
     * 递归方法获取部门
     * @param hitachiUnit
     * @return
     */
    public void getUnitName(HitachiUnit hitachiUnit, List<String> unitNameList){
        if(null == hitachiUnit.getParentUnitId()){
            unitNameList.add(hitachiUnit.getUnitName());
        }else{
            unitNameList.add(hitachiUnit.getUnitName());
            HitachiUnit parentHitachiUnit = hitachiRfxHeaderPrintMapper.selectUnit(hitachiUnit.getTenantId(), hitachiUnit.getParentUnitId());
            if(null != parentHitachiUnit){
                this.getUnitName(parentHitachiUnit, unitNameList);
            }
        }
    }

    @Override
    public List<HitachiRfxConsigneeDTO> hitachiRfxPrintValidate(Long tenantId, List<HitachiRfxPrintDTO> hitachiRfxPrintDTOList){
        //获取未打印过的数据或者注文書の送付先的值为空
        List<HitachiRfxPrintDTO> list = hitachiRfxPrintDTOList.stream().filter(e -> BaseConstants.Flag.NO.equals(null == e.getAttributeTinyint1() ? BaseConstants.Flag.NO : e.getAttributeTinyint1()) || StringUtils.isBlank(e.getDeliveryAddress())).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(list)){
            return null;
        }
        //根据供应商 + 所课 的维度进行分组
        Map<String, List<HitachiRfxPrintDTO>> groupList = list.stream().collect(Collectors.groupingBy(e -> e.getSapSupplierCode() + "-" + e.getDivisionCode()));
        if(groupList.size() > 1){
            throw new CommonException("サプライヤー名（ERP）と所課の組合せは、複数選択できません。");
        }
        HitachiRfxConsigneeDTO consigneeDTO = new HitachiRfxConsigneeDTO();
        consigneeDTO.setSupplier(list.get(0).getSapSupplierCode());
        consigneeDTO.setSubDepartment(list.get(0).getDivisionCode());
        LOGGER.info("---------查询送达方RelTableHelper.selectByCondition-------参数：{}",consigneeDTO.toString());
        List<HitachiRfxConsigneeDTO> consigneeDTOS = RelTableHelper.selectByCondition(tenantId, HitachiConstants.ConfigTableCode.SCUX_HITACHI_RECEIVER_INFOR, consigneeDTO);
        LOGGER.info("---------查询送达方RelTableHelper.selectByCondition-------结果：{}", JSONObject.toJSONString(consigneeDTOS));
        if(CollectionUtils.isEmpty(consigneeDTOS)){
            return null;
        }
        return consigneeDTOS;
    }
}
