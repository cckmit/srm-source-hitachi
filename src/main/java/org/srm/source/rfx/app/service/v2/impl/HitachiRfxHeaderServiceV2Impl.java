package org.srm.source.rfx.app.service.v2.impl;

import io.choerodon.core.oauth.DetailsHelper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hzero.boot.customize.util.CustomizeHelper;
import org.hzero.boot.platform.code.builder.CodeRuleBuilder;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.redis.RedisHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.srm.source.bid.api.dto.BiddingWorkDTO;
import org.srm.source.bid.domain.entity.SourceNotice;
import org.srm.source.bid.domain.service.ISourceNoticeDomainService;
import org.srm.source.rfx.api.dto.CheckPriceHeaderDTO;
import org.srm.source.rfx.api.dto.CheckSaveContextDTO;
import org.srm.source.rfx.api.dto.HeaderQueryDTO;
import org.srm.source.rfx.api.dto.RfxCheckItemDTO;
import org.srm.source.rfx.app.service.HitachiRfxQuotationLineService;
import org.srm.source.rfx.app.service.RfxLineItemService;
import org.srm.source.rfx.app.service.RfxMemberService;
import org.srm.source.rfx.app.service.SourceMatterConfService;
import org.srm.source.rfx.domain.entity.*;
import org.srm.source.rfx.domain.repository.RfxHeaderRepository;
import org.srm.source.rfx.domain.repository.RfxMemberRepository;
import org.srm.source.rfx.domain.service.IRfxActionDomainService;
import org.srm.source.rfx.domain.service.IRfxHeaderDomainService;
import org.srm.source.rfx.domain.service.v2.RfxHeaderDomainService;
import org.srm.source.rfx.domain.strategy.CheckSaveStrategyContext;
import org.srm.source.rfx.domain.vo.RfxFullHeader;
import org.srm.source.rfx.infra.constant.SourceConstants;
import org.srm.source.rfx.infra.util.RfxEventUtil;
import org.srm.source.share.app.service.SourceTemplateService;
import org.srm.source.share.domain.entity.EvaluateIndic;
import org.srm.source.share.domain.entity.PrequalHeader;
import org.srm.source.share.domain.entity.PrequalMember;
import org.srm.source.share.domain.entity.SourceTemplate;
import org.srm.source.share.domain.repository.PrequalMemberRepository;
import org.srm.source.share.domain.repository.SourceTemplateRepository;
import org.srm.source.share.domain.service.IEvaluateDomainService;
import org.srm.source.share.infra.constant.HitachiConstants;
import org.srm.source.share.infra.constant.ShareConstants;
import org.srm.web.annotation.Tenant;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 询价单头表应用服务默认实现 v2
 *
 * @author le.zhao@hand-china.com 2021-4-9 16:00:44
 */
@Service("hitachRfxHeaderServiceImpl.v2")
@Tenant(HitachiConstants.TENANT_NUM)
public class HitachiRfxHeaderServiceV2Impl extends RfxHeaderServiceV2Impl {
    @Autowired
    private SourceTemplateService sourceTemplateService;
    @Autowired
    private org.srm.source.rfx.app.service.RfxHeaderService rfxHeaderServiceV1;
    @Autowired
    private RfxHeaderRepository rfxHeaderRepository;
    @Autowired
    private CheckSaveStrategyContext checkSaveStrategyContext;
    @Autowired
    private SourceTemplateRepository sourceTemplateRepository;
    @Autowired
    private HitachiRfxQuotationLineService hitachiRfxQuotationLineService;

    private static final Logger LOGGER = LoggerFactory.getLogger(HitachiRfxHeaderServiceV2Impl.class);
    @Autowired
    private RedisHelper redisHelper;
    @Autowired
    private RfxEventUtil rfxEventUtil;
    @Autowired
    private RfxMemberRepository rfxMemberRepository;
    @Autowired
    private PrequalMemberRepository prequalMemberRepository;
    @Autowired
    private RfxMemberService rfxMemberService;
    @Autowired
    private ISourceNoticeDomainService sourceNoticeDomainService;
    @Autowired
    private IRfxHeaderDomainService rfxHeaderDomainService;
    @Autowired
    private RfxLineItemService rfxLineItemService;
    @Autowired
    private IRfxActionDomainService rfxActionDomainService;
    @Autowired
    private IEvaluateDomainService evaluateDomainService;
    @Autowired
    private RfxHeaderDomainService rfxHeaderDomainServiceV2;
    @Autowired
    private CodeRuleBuilder codeRuleBuilder;
    @Autowired
    private SourceMatterConfService sourceMatterConfService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkPriceSave(Long organizationId, CheckPriceHeaderDTO checkPriceHeaderDTO) {
        if (CollectionUtils.isEmpty(checkPriceHeaderDTO.getCheckPriceDTOLineList())) {
            return;
        }
        RfxHeader dbRfxHeader = rfxHeaderRepository.selectByPrimaryKey(checkPriceHeaderDTO.getRfxHeaderId());
        Assert.notNull(checkPriceHeaderDTO.getRfxHeaderId(), ShareConstants.ErrorCode.ERROR_DATA_NOT_EXISTS);
        BeanUtils.copyProperties(checkPriceHeaderDTO, dbRfxHeader, RfxHeader.FIELD_OBJECT_VERSION_NUMBER);

        if ("STANDARD".equals(checkPriceHeaderDTO.getProjectName())) {
            //标准里面添加了参数，调用这个方法，根据参数处理一些逻辑，但是二开里面没有调用，所以在标准里面传projectName来区分是标准还是二开
            rfxHeaderServiceV1.lineDateHandle(checkPriceHeaderDTO);
        }

        //组装核价保存上下文用于后续处理
        CheckSaveContextDTO checkSaveContextDTO = new CheckSaveContextDTO();
        checkSaveContextDTO.setTenantId(organizationId);
        checkSaveContextDTO.setStrategyType(checkPriceHeaderDTO.getCheckPriceDTOLineList().get(0).getType());
        checkSaveContextDTO.setCheckPriceHeaderDTO(checkPriceHeaderDTO);
        checkSaveContextDTO.setSourceTemplate(sourceTemplateService.selectById(dbRfxHeader.getTemplateId()));

        //根据不同页签走不同保存策略
        checkSaveStrategyContext.processCheckSaveStrategy(checkSaveContextDTO);

        //保存 模板编码 不含税总金额
        HeaderQueryDTO headerQueryDTO = new HeaderQueryDTO(checkPriceHeaderDTO.getRfxHeaderId(), organizationId);
        List<RfxCheckItemDTO> rfxCheckItemDTOS = hitachiRfxQuotationLineService.quotationDetail(headerQueryDTO);
        BigDecimal attributeDecimal10 = rfxCheckItemDTOS.stream().map(RfxCheckItemDTO::getNetAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
        dbRfxHeader.setAttributeDecimal10(attributeDecimal10);
        SourceTemplate sourceTemplate = sourceTemplateRepository.selectByPrimaryKey(dbRfxHeader.getTemplateId());
        dbRfxHeader.setAttributeVarchar10(sourceTemplate.getTemplateNum());
        CustomizeHelper.ignore(() ->
        rfxHeaderRepository.updateOptional(dbRfxHeader,
                RfxHeader.FIELD_TOTAL_COST,
                RfxHeader.FIELD_CHECK_ATTACHMENT_UUID,
                RfxHeader.FIELD_COST_REMARK,
                RfxHeader.FIELD_CHECK_REMARK,
                RfxHeader.FIELD_PRICE_EFFECTIVE_DATE,
                RfxHeader.FIELD_PRICE_EXPIRY_DATE,
                "attributeDecimal10",
                "attributeVarchar10"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RfxFullHeader saveOrUpdateFullHeader(RfxFullHeader rfxFullHeader) {
        // 获取询价头基础信息
        RfxHeader rfxHeader = rfxFullHeader.getRfxHeader();
        //当寻源类别为竞价 报价范围默认值为部分报价
        rfxHeader.validationCategoryType();
        //采购企业id为空设置虚拟值
        if(rfxHeader.getCompanyId() == null){
            rfxHeader.setCompanyId(-1L);
        }
        //设置币种默认值CNY
        if(StringUtils.isBlank(rfxHeader.getCurrencyCode())) {
            rfxHeader.setCurrencyCode(SourceConstants.RfxConstants.CURRENCY_CODE_DEFAULT);
        }
        //校验模版是否选择
        Assert.notNull(rfxHeader.getTemplateId(),ShareConstants.ErrorCode.ERROR_SOURCE_TEMPLATE_NOT_SELECTED);
        //获取寻源模版信息
        SourceTemplate sourceTemplate = sourceTemplateService.selectByPrimaryKey(rfxHeader.getTemplateId());
        Assert.notNull(sourceTemplate, ShareConstants.ErrorCode.ERROR_SOURCE_TEMPLATE_ID_NOT_FOUND);
        redisHelper.strSet("ssrc:rfx:source:template:" + rfxHeader.getTemplateId(), redisHelper.toJson(sourceTemplate), 3, TimeUnit.MINUTES);
        //资格预审
        PrequalHeader prequalHeader = rfxFullHeader.getPrequalHeader();
        //评分要素
        List<EvaluateIndic> evaluateIndics = rfxFullHeader.getEvaluateIndics();
        List<EvaluateIndic> initialReviewIndicList = rfxFullHeader.getInitialReviewIndicList();
        //物料行
        List<RfxLineItem> rfxLineItemList = rfxFullHeader.getRfxLineItemList();
        //供应商行
        List<RfxLineSupplier> rfxLineSupplierList = rfxFullHeader.getRfxLineSupplierList();
        //寻源小组
        List<RfxMember> rfxMemberList = rfxFullHeader.getRfxMemberList();
        //预审小组
        List<PrequalMember> prequalMemberList = rfxFullHeader.getPrequalMemberList();
        //新建标识
        Integer newFlag = 0;
        //如果是新建
        if (rfxHeader.getRfxHeaderId() == null) {
            newFlag = 1;
            //创建询价单头
            rfxHeader = this.self().createHeader(rfxHeader);
            Long rfxHeaderId = rfxHeader.getRfxHeaderId();
            //初始化评分要素
            evaluateIndics.forEach(evaluateIndic -> {
                //设置关联的来源头id和来源类型
                evaluateIndic.setSourceHeaderId(rfxHeaderId);
                evaluateIndic.setSourceFrom(ShareConstants.SourceTemplate.CategoryType.RFX);
            });
            initialReviewIndicList.forEach(initialReviewIndic -> {
                //设置关联的来源头id和来源类型
                initialReviewIndic.setSourceHeaderId(rfxHeaderId);
                initialReviewIndic.setSourceFrom(ShareConstants.SourceTemplate.CategoryType.RFX);
            });
            //默认显示寻源模板中的寻源事项须知
            rfxHeader.setMatterDetail(sourceTemplate.getMatterDetail());
        } else {
            // 校验切换模板
            rfxHeaderServiceV1.changeTemplate(rfxFullHeader);
            //删除数据库的寻源小组数据，在下面重新新增数据进去
            rfxMemberRepository.delete(new RfxMember(rfxHeader.getTenantId(), rfxHeader.getRfxHeaderId()));
            rfxFullHeader.getRfxMemberList().forEach(rfxMember -> rfxMember.setRfxMemberId(null));

            //删除数据库的预审小组数据，在下面重新新增数据进去
            prequalMemberRepository.delete(new PrequalMember(rfxHeader.getRfxHeaderId(), rfxHeader.getTenantId(), ShareConstants.QuotationColumn.SourceFrom.RFX));
            Optional.ofNullable(rfxFullHeader.getPrequalMemberList()).ifPresent(list -> list.forEach(e -> e.setPrequalMemberId(null)));
        }
        //初始化寻源小组招标员是否启用开标密码字段
        rfxMemberList.stream().filter(e -> SourceConstants.RfxRole.OPENED_BY.equals(e.getRfxRole())).forEach(rfxMember -> rfxMember.setPasswordFlag(rfxFullHeader.getRfxHeader().getPasswordFlag() == null ? BaseConstants.Flag.NO : rfxFullHeader.getRfxHeader().getPasswordFlag()));
        //新增和修改寻源小组
        rfxMemberService.save(rfxHeader.getTenantId(), rfxHeader.getRfxHeaderId(), rfxMemberList);
        //由于询价工作台的值。存在于头部，则将头部的采购联系人赋值给公干上面
        SourceNotice sourceNotice = rfxFullHeader.getSourceNotice();
        if(!Objects.isNull(sourceNotice)){
            sourceNotice.setPurName(rfxHeader.getPurName());
            sourceNotice.setPurEmail(rfxHeader.getPurEmail());
            sourceNotice.setPurPhone(rfxHeader.getPurPhone());
            sourceNotice.setInternationalTelCode(rfxHeader.getInternationalTelCode());
        }

        //处理寻源公告
        sourceNoticeDomainService.processRfxNotice(rfxHeader, rfxFullHeader.getSourceNotice());
        if (!Objects.equals(1,rfxHeader.getMultiSectionFlag())){
            //按原逻辑单个单子处理资格预审
            processPrequalSingle(rfxFullHeader, rfxHeader, sourceTemplate, prequalHeader, prequalMemberList);
        }
        if (Objects.equals(1,rfxHeader.getMultiSectionFlag())){
            if (StringUtils.isNotBlank(rfxHeader.getMergeType())){
                //多标段的时候，合并方式为分组时，处理资格预审分组信息
                processPrequalGroup(rfxFullHeader, rfxHeader, sourceTemplate, prequalHeader, prequalMemberList);
            }
        }
        //新增或修改物料行
        rfxHeaderDomainService.processRfxLineItem(rfxHeader, rfxLineItemList);
        // 新增或修改供应商行
        rfxHeaderDomainService.processRfxLineSupplier(rfxHeader, rfxLineSupplierList);
        //检查供货能力清单的供应商是否删除
        rfxLineItemService.checkRfxSuppliers(rfxHeader.getTenantId(),rfxHeader.getRfxHeaderId());
        //专家信息信息初始化
        BiddingWorkDTO evaluateExperts = this.getBiddingWorkDTO(rfxFullHeader, rfxHeader);
        //新建或更新专家信息
        evaluateDomainService.createOrUpdateEvaluateExpert(rfxFullHeader.getRfxHeader().getTenantId(), evaluateExperts, ShareConstants.Expert.EvaluateStatus.SUBMITTED);
        //新建或更新评分要素
        evaluateDomainService.createOrUpdateEvaluateEvaluateIndic(rfxFullHeader.getRfxHeader().getTenantId(), evaluateIndics, ShareConstants.Expert.EvaluateStatus.SUBMITTED);
        //更新初审评审评分要素
        evaluateDomainService.createOrUpdateInitialReviewIndic(rfxFullHeader.getRfxHeader().getTenantId(),rfxFullHeader.getInitialReviewIndicList(),ShareConstants.Expert.EvaluateStatus.SUBMITTED);

        //初始化预算金额及修改标识
        rfxHeaderServiceV1.initBudget(rfxHeader);

        //计算预估金额
        rfxHeaderServiceV1.initTotalEstimatedAmount(rfxHeader);

        // 处理自动多轮报价时间
        rfxHeaderDomainServiceV2.processRoundQuotationDate(sourceTemplate, rfxHeader, rfxFullHeader.getRoundHeaderDates());
        // 处理多标段维护
        rfxHeaderDomainServiceV2.processMultiSections(rfxFullHeader.getProjectLineSections());

        //新建单据写入操作记录和触发新建事件
        if (BaseConstants.Flag.YES.equals(newFlag)) {
            // 写入操作记录
            this.rfxActionDomainService.insertAction(rfxHeader, SourceConstants.RfxActionOperation.CREATE, null);
            //---询价单创建事件--
            //1.待发布+1
            rfxEventUtil.eventSend(RfxEventUtil.EventCode.SSRC_RFX_CREATE, RfxEventUtil.Action.CREATE, rfxHeader);
        }else{

            this.rfxHeaderRepository.updateRfxHeader(rfxHeader);
        }
        //清除模版缓存
        redisHelper.delKey("ssrc:rfx:source:template:" + rfxHeader.getTemplateId());

        return rfxFullHeader;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RfxHeader createHeader(RfxHeader rfxHeader) {
        //-----------必输字段为空添加虚拟值--------------
        //寻源类别
        if(StringUtils.isBlank(rfxHeader.getSourceCategory())){
            rfxHeader.setSourceCategory("virtual-value");
        }
        //寻源方式
        if(StringUtils.isBlank(rfxHeader.getSourceMethod())){
            rfxHeader.setSourceMethod("virtual-value");
        }
        //设置新建状态和轮次
        rfxHeader.setRfxStatus(SourceConstants.RfxStatus.NEW);
        rfxHeader.setRoundNumber(1L);
        // 创建询价单头
        SourceTemplate sourceTemplate = sourceTemplateService.selectByPrimaryKey(rfxHeader.getTemplateId());
        Assert.notNull(sourceTemplate, ShareConstants.ErrorCode.ERROR_SOURCE_TEMPLATE_ID_NOT_FOUND);
        rfxHeader.setRfxNum(codeRuleBuilder.generateCode(DetailsHelper.getUserDetails().getTenantId(), SourceConstants.CodeRule.RFX_NUM, "GLOBAL", "GLOBAL", null));
        rfxHeader.setCheckedBy(DetailsHelper.getUserDetails().getUserId());
        rfxHeader.setLackQuotedSendFlag(BaseConstants.Flag.NO);
        if (rfxHeader.getBidBond() == null) {
            rfxHeader.setBidBond(BigDecimal.ZERO);
        }
        // 获取配置中心事项说明
            SourceMatterConf sourceMatterConf = sourceMatterConfService.getMatterRequireAndDetail(rfxHeader.getTenantId(), rfxHeader.getCompanyId());
        if (sourceMatterConf != null) {
            rfxHeader.setMatterRequireFlag(sourceMatterConf.getRfxRequireFlag());
        }
        rfxHeader.setQuotationRounds(rfxHeader.getQuotationRounds() == null? sourceTemplate.getQuotationRounds() : rfxHeader.getQuotationRounds());
        this.rfxHeaderRepository.insertSelective(rfxHeader);
        return rfxHeader;
    }

}
