package org.srm.source.rfx.app.service.impl;

import io.choerodon.core.exception.CommonException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hzero.boot.customize.util.CustomizeHelper;
import org.hzero.core.base.AopProxy;
import org.hzero.core.base.BaseConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.srm.boot.adaptor.client.AdaptorTaskHelper;
import org.srm.boot.adaptor.client.exception.TaskNotExistException;
import org.srm.boot.adaptor.client.result.TaskResultBox;
import org.srm.common.TenantInfoHelper;
import org.srm.common.client.entity.Tenant;
import org.srm.source.rfx.api.dto.HitachiInvOrganizationDTO;
import org.srm.source.rfx.api.dto.InvOrganizationDTO;
import org.srm.source.rfx.app.service.RfxHeaderService;
import org.srm.source.rfx.app.service.RfxLineItemService;
import org.srm.source.rfx.domain.entity.RfxHeader;
import org.srm.source.rfx.domain.entity.RfxLineItem;
import org.srm.source.rfx.domain.entity.RfxQuotationLine;
import org.srm.source.rfx.domain.repository.CommonQueryRepository;
import org.srm.source.rfx.domain.repository.RfxHeaderRepository;
import org.srm.source.rfx.domain.repository.RfxLineItemRepository;
import org.srm.source.rfx.domain.repository.RfxQuotationLineRepository;
import org.srm.source.rfx.domain.service.IRfxLineItemDomainService;
import org.srm.source.rfx.infra.constant.SourceConstants;
import org.srm.source.rfx.infra.mapper.HitachiCommonQueryMapper;
import org.srm.source.share.domain.entity.Item;
import org.srm.source.share.infra.constant.HitachiConstants;
import org.srm.source.share.infra.constant.ShareConstants;
import org.srm.source.share.infra.feign.SmdmRemoteService;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 询价单物料行应用服务默认实现
 *
 * @author le.zhao@going-link.com 2021-3-26 12:49:40
 */
@Service
@org.srm.web.annotation.Tenant(HitachiConstants.TENANT_NUM)
public class HitachiRfxLineItemServiceImpl extends RfxLineItemServiceImpl implements RfxLineItemService, AopProxy<RfxLineItemService> {

    @Autowired
    private RfxLineItemRepository rfxLineItemRepository;
    @Autowired
    private RfxHeaderRepository rfxHeaderRepository;
    @Autowired
    private IRfxLineItemDomainService rfxLineItemDomainService;
    @Autowired
    private SmdmRemoteService smdmRemoteService;
    @Autowired
    private RfxHeaderService rfxHeaderService;
    @Autowired
    private CommonQueryRepository commonQueryRepository;
    @Autowired
    private RfxQuotationLineRepository rfxQuotationLineRepository;
    @Autowired
    private HitachiCommonQueryMapper hitachiCommonQueryMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(RfxHeaderServiceImpl.class);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<RfxLineItem> createItemList(String approveType, Long organizationId, List<RfxLineItem> rfxLineItemList) {
        if (CollectionUtils.isEmpty(rfxLineItemList)) {
            return null;
        }

        //获取是否一次性采购，一次性采购的不生成物料
        for(RfxLineItem rfxLineItem : rfxLineItemList){
            RfxQuotationLine rfxQuotationLineParam = new RfxQuotationLine();
            rfxQuotationLineParam.setRfxLineItemId(rfxLineItem.getRfxLineItemId());
            List<RfxQuotationLine> rfxQuotationLineList = rfxQuotationLineRepository.select(rfxQuotationLineParam).stream().filter(e -> (StringUtils.isNotBlank(e.getAttributeVarchar10()))).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(rfxQuotationLineList)){
                //价格适用范围
                rfxLineItem.setAttributeVarchar10(rfxQuotationLineList.get(0).getAttributeVarchar10());
                //是否一次性采购
                if(BaseConstants.Flag.YES.equals(rfxQuotationLineList.get(0).getAttributeTinyint10())){
                    rfxLineItemList.remove(rfxLineItem);
                }
            }else{
                throw new CommonException(HitachiConstants.ErrorCode.ERROR_PRICE_SCOPE_AND_ONE_TIME_PURCHASE_EMPTY);
            }
        };

        if (Objects.equals(rfxLineItemList.get(0).getCreateItemFlag(), ShareConstants.Digital.THREE)) {
            rfxLineItemList.forEach(rfxLineItem -> {
                if (rfxLineItem.getItemCode() == null){

                    //获取租户信息
                    Tenant tenant = TenantInfoHelper.selectByTenantId(organizationId);
                    //根据配置器获取数据
                    try {
                        TaskResultBox taskResultBox = AdaptorTaskHelper.executeAdaptorTask(HitachiConstants.AdaptorTask.SSRC_ITEM_CODE_CREATE, tenant.getTenantNum(), rfxLineItem);
                        String itemCreateCode = taskResultBox.get(0, String.class);
                        rfxLineItem.setItemCode(itemCreateCode);
                    }catch (TaskNotExistException e){
                        LOGGER.info("============SSRC_ITEM_CODE_CREATE-TaskNotExistException=============={}",tenant.getTenantNum(),e.getMessage(),e.getStackTrace());
                    }
                    // throw new CommonException(ShareConstants.ErrorCode.ITEM_CODE_NOT_NULL);
                }
                rfxLineItem.setCreatedItemCategoryId(rfxLineItem.getItemCategoryId());
                rfxLineItem.setCreatedItemCategoryName(rfxLineItem.getItemCategoryName());
            });
        }
        //如果是补充物料但是没有选择lov时,过滤
        if (Objects.equals(rfxLineItemList.get(0).getCreateItemFlag(), BaseConstants.Digital.TWO)) {
            rfxLineItemList = rfxLineItemList.stream().filter(rfxLineItem -> rfxLineItem.getItemCode() != null).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(rfxLineItemList)){
                return null;
            }
        }

        //校验物料code是否重复
        rfxLineItemDomainService.validCodeRepeat(rfxLineItemList);
        List<Item> itemList = rfxLineItemList.stream().map(rfxLineItem -> this.itemConvertor(rfxLineItem, approveType)).collect(Collectors.toList());
        //这里面存在两种情况:1.新增物料,2.补充物料
        //如果是补充物料的话只需要审批通过维护关系,通过前台lov选择已经存在的物料,有itemId
        //如果是自审批/审批通过和补充物料的时候,需要创建新的品类和物料的关系
        itemList = smdmRemoteService.createItemFromSrmSource(organizationId, itemList).getBody();
        if (CollectionUtils.isEmpty(itemList)) {
            //只是将参数返回
            throw new CommonException(SourceConstants.ErrorCode.FAILED_TO_CREATE_ITEM_ERROR);
        }
        //回写创建的itemId
        Map<Long, Item> itemMap = itemList.stream().collect(Collectors.toMap(Item::getRfxLineItemId, item->item));
        List<Long> rfxLineItemIdList = rfxLineItemList.stream().map(RfxLineItem::getRfxLineItemId).collect(Collectors.toList());
        rfxLineItemList = rfxLineItemRepository.selectByIds(rfxLineItemIdList.stream().map(Object::toString).collect(Collectors.joining(",")));
        rfxLineItemList.forEach(rfxLineItem -> rfxLineItem.insertItemCodeBack(itemMap.get(rfxLineItem.getRfxLineItemId())));
        List<RfxLineItem> finalRfxLineItemList = rfxLineItemList;
        CustomizeHelper.ignore(() -> rfxLineItemRepository.batchUpdateOptional(finalRfxLineItemList,
                RfxLineItem.FIELD_CREATED_ITEM_ID,
                RfxLineItem.FIELD_CREATED_ITEM_CATEGORY_ID,
                RfxLineItem.FIELD_CREATED_ITEM_CATEGORY_NAME));
        return rfxLineItemList;
    }
    //todo 重写itemConvertor
    public Item itemConvertor(RfxLineItem rfxLineItem,String approveType) {
        Item item = new Item();
        item.setTenantId(rfxLineItem.getTenantId());
        //补充物料并且审批中不用调用创建物料接口,可直接将物料id赋值
        item.setItemCategoryName(rfxLineItem.getItemCategoryName());
        item.setItemId(rfxLineItem.getItemId());
        item.setItemCode(rfxLineItem.getItemCode());
        item.setItemName(rfxLineItem.getItemName());
        item.setImportFlag(BaseConstants.Digital.ZERO);
        item.setPrimaryUomId(rfxLineItem.getUomId());
        item.setRfxLineItemId(rfxLineItem.getRfxLineItemId());
        item.setSourceCategoryId(rfxLineItem.getItemCategoryId());
        // 品类主键
        item.setAttributeBigint1(rfxLineItem.getItemCategoryId());
        // 购买类型
        item.setAttributeVarchar1(rfxLineItem.getAttributeVarchar10());
        item.setSourceInvOrganizationId(rfxLineItem.getInvOrganizationId());
        item.setSourceOuId(rfxLineItem.getOuId());
        if (null != rfxLineItem.getCreateItemFlag() && (rfxLineItem.getCreateItemFlag().equals(BaseConstants.Digital.TWO)||rfxLineItem.getCreateItemFlag().equals(ShareConstants.Digital.THREE))){
            item.setCreateSsrcItemFlag(rfxLineItem.getCreateItemFlag());
        }
        if (Objects.equals(ShareConstants.SourceTemplate.ReleaseApproveType.SELF, approveType)) {
            if (rfxLineItem.getItemCategoryId() != null) {
                item.createItemCategoryAssign(rfxLineItem.getTenantId(), null, rfxLineItem.getItemCategoryId());
            }
            RfxHeader rfxHeader = rfxHeaderRepository.selectByPrimaryKey(rfxLineItem.getRfxHeaderId());
            HitachiInvOrganizationDTO queryParam = new HitachiInvOrganizationDTO();
            queryParam.setTenantId(rfxHeader.getTenantId());
            //库存组织处理
            //ALL,COMPANY,DEPARTMENT,BUSSINESS_UNIT,APPOINT
            //1.	全GRへ表示（物料将在全集团使用，此时，插入租户下全部的公司和公司下的全部业务实体的库存组织）
            if("ALL".equals(rfxLineItem.getAttributeVarchar1())){

                //2. 自会社のみ表示（物料仅在询价单所属公司使用，此时插入询价单所选公司下的全部业务实体的全部库存组织
            }else if("COMPANY".equals(rfxLineItem.getAttributeVarchar1())){
                queryParam.setCompanyId(rfxHeader.getCompanyId());
                //3. 本部内のみ表示（物料仅在本部表示，（本部为部门编码第一位相同的为同一本部），此时插入所有业务实体编码第一位与询价单业务实体第一位一样的业务实体下的全部库存组织）
            }else if("DEPARTMENT".equals(rfxLineItem.getAttributeVarchar1())){
                queryParam.setOuId(rfxHeader.getUnitId());
                queryParam.setOuCodeFlag(BaseConstants.Flag.YES);
                //4. 営業所内のみ表示（物料仅在询价单的业务实体内表示，此时插入询价单业务实体下的全部库存组织）
            }else if("BUSSINESS_UNIT".equals(rfxLineItem.getAttributeVarchar1())){
                queryParam.setOuId(rfxHeader.getUnitId());
                //5. 指定範囲内のみ可能：一つの会社内の範囲、指定したZ、Z614→森さんから頂けるなどで決める（手工指定适用的业务实体，此时插入所属部门字段所选的业务实体下的全部库存组织）
            }else if("APPOINT".equals(rfxLineItem.getAttributeVarchar1())){
                if(rfxLineItem.getAttributeBigint1() != null){
                    queryParam.setOrganizationId(rfxLineItem.getAttributeBigint1());
                    // item.createItemOrgRel(rfxLineItem.getTenantId(), null, rfxLineItem.getAttributeBigint1());
                }
            }
            List<InvOrganizationDTO> invOrganizationDTOS = hitachiCommonQueryMapper.selectInvOrganizations(queryParam);
            invOrganizationDTOS.forEach(invOrganizationDTO -> {
                item.createItemOrgRel(rfxLineItem.getTenantId(), null, invOrganizationDTO.getOrganizationId());
            });

        } else {
            item.setEnabledFlag(BaseConstants.Flag.NO);
            item.setFrozenFlag(BaseConstants.Flag.YES);
        }
        return item;
    }

}
