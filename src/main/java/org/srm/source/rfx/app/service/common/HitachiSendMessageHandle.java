package org.srm.source.rfx.app.service.common;

import com.alibaba.fastjson.JSON;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hzero.boot.message.MessageClient;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.helper.LanguageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.srm.boot.platform.configcenter.CnfHelper;
import org.srm.boot.platform.message.MessageHelper;
import org.srm.boot.platform.message.entity.SpfmMessageSender;
import org.srm.boot.platform.message.feign.SpfmMessageRemoteService;
import org.srm.source.bid.domain.repository.BidHeaderRepository;
import org.srm.source.bid.infra.constant.BidConstants;
import org.srm.source.rfx.api.dto.CompanyDTO;
import org.srm.source.rfx.api.dto.RfxMonitorSupplierDTO;
import org.srm.source.rfx.api.dto.RfxQuotationHeaderDTO;
import org.srm.source.rfx.domain.entity.RfxHeader;
import org.srm.source.rfx.domain.entity.RfxLineSupplier;
import org.srm.source.rfx.domain.entity.RfxQuotationHeader;
import org.srm.source.rfx.domain.entity.RfxQuotationLine;
import org.srm.source.rfx.domain.repository.*;
import org.srm.source.rfx.infra.constant.Constants;
import org.srm.source.rfx.infra.constant.HitachiSourceConstants;
import org.srm.source.rfx.infra.constant.SourceConstants;
import org.srm.source.share.domain.entity.MessageMark;
import org.srm.source.share.domain.repository.EvaluateExpertRepository;
import org.srm.source.share.domain.repository.MessageMarkRepository;
import org.srm.source.share.domain.repository.PrequalHeaderRepository;
import org.srm.source.share.domain.repository.PrequalLineRepository;
import org.srm.source.share.infra.constant.ShareConstants;
import org.srm.source.share.infra.utils.PromptUtil;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class HitachiSendMessageHandle{

    @Autowired
    private RfxHeaderRepository rfxHeaderRepository;
    @Autowired
    private RfxLineSupplierRepository rfxLineSupplierRepository;
    @Autowired
    private CommonQueryRepository commonQueryRepository;
    @Autowired
    private RfxQuotationHeaderRepository rfxQuotationHeaderRepository;
    @Autowired
    private MessageHelper messageHelper;
    @Autowired
    private MessageMarkRepository messageMarkRepository;
    @Autowired
    @Lazy
    private PromptUtil promptUtil;

    private SimpleDateFormat format = new SimpleDateFormat(BaseConstants.Pattern.DATETIME);
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 询价单发布消息发送
     */
    @Async
    public void sendMessageForRelease(RfxHeader rfxHeader, CustomUserDetails userDetails) {
        try {
            DetailsHelper.setCustomUserDetails(userDetails);
            if (rfxHeader == null) {
                return;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("rfHeader:{}", rfxHeader.toJSONString());
            }
            Map<String, String> map = new ConcurrentHashMap<>();
            map.put(Constants.MessageConstants.ORGANIZATION_ID, String.valueOf(rfxHeader.getTenantId()));
            map.put(Constants.MessageConstants.COMPANY_ID, String.valueOf(rfxHeader.getCompanyId()));
            map.put(Constants.MessageConstants.COMPANY_NAME, rfxHeader.getCompanyName());
            map.put(Constants.MessageConstants.RFX_NUMBER, rfxHeader.getRfxNum());
            map.put(Constants.MessageConstants.RFX_TITLE, rfxHeader.getRfxTitle());
            if(!StringUtils.isEmpty(rfxHeader.getPurName())){
                map.put(Constants.MessageConstants.PURCHASE_NAME, rfxHeader.getPurName());
            }
            if(!StringUtils.isEmpty(rfxHeader.getPurEmail())){
                map.put(Constants.MessageConstants.PURCHASE_EMAIL, rfxHeader.getPurEmail());
            }
            if(!StringUtils.isEmpty(rfxHeader.getPurPhone())){
                map.put(Constants.MessageConstants.PURCHASE_PHONE, rfxHeader.getPurPhone());
            }
            map.put(Constants.MessageConstants.QUOTATION_END_DATE, Objects.nonNull(rfxHeader.getQuotationEndDate()) ? format.format(rfxHeader.getQuotationEndDate()) : "");


            if (Objects.nonNull(rfxHeader.getQuotationStartDate())) {
                map.put(Constants.MessageConstants.RFX_START_TIME, format.format(rfxHeader.getQuotationStartDate()));
            }
            if (Objects.nonNull(rfxHeader.getEstimatedStartTime())) {
                map.put(Constants.MessageConstants.ESTIMATED_START_TIME, format.format(rfxHeader.getEstimatedStartTime()));
            }
            map.put(Constants.MessageConstants.CURRENT_TIME, format.format(new Date()));
            map.put(Constants.MessageConstants.SOURCE_HEADER_ID, String.valueOf(rfxHeader.getRfxHeaderId()));
            map.put(Constants.MessageConstants.SOURCE_TYPE, ShareConstants.SourceCategory.RFX);
            //map.put(Constants.MessageConstants.SOURCE_ADDRESS, "<a target='_blank' href='"+ ShareConstants.LINK_ADDRESS_PREFIX +rfxHeader.getServerName()+"/app/ssrc/inquiry-hall/rfx-detail/"+rfxHeader.getRfxHeaderId()+"'>"+getLinkString(userDetails)+"</a>");
            if (SourceConstants.RfxType.INVITE.equals(rfxHeader.getSourceMethod())) {
                RfxLineSupplier temp = new RfxLineSupplier();
                temp.setRfxHeaderId(rfxHeader.getRfxHeaderId());
                List<RfxLineSupplier> rfxLineSupplierList = rfxLineSupplierRepository.select(temp);
                if (CollectionUtils.isNotEmpty(rfxLineSupplierList)) {
                    String supplierQuotationTitleName = promptUtil.selectPrompt(HitachiSourceConstants.PromptConstants.SUPPLIER_QUOTATION_PROMPT_KEY, HitachiSourceConstants.PromptConstants.SUPPLIER_QUOTATION_PROMPT_CODE, LanguageHelper.language(), "供应商报价");
                    for (RfxLineSupplier rfxLineSupplier : rfxLineSupplierList) {
                        //feature-srm-57766  查询业务规则定义配置，判断此供应商是否需要发送邮件
                        if(!checkSendMessageSupplier(rfxHeader,rfxLineSupplier)) { continue; }
                        String serverName = commonQueryRepository.selectServerName(rfxLineSupplier.getSupplierTenantId());
                        map.put(Constants.MessageConstants.SOURCE_ADDRESS, "<a target='_blank' href='" + ShareConstants.LINK_ADDRESS_PREFIX + serverName + "/app/ssrc/inquiry-hall/rfx-detail/" + rfxHeader.getRfxHeaderId() + "'>" + getLinkString(userDetails) + "</a>");
                        String URL = BidConstants.MessageConstants.BID_PARTICIPATE_LINK + "/ssrc/supplier-quotation\'," + "path:\'/ssrc/supplier-quotation/detail/" + rfxHeader.getRfxHeaderId() + "/" + rfxLineSupplier.getSupplierCompanyId() + "/operation\',title:\'"+supplierQuotationTitleName+"\'})\">" + rfxHeader.getRfxNum() + "</a>";
                        map.put(Constants.MessageConstants.RFX_PARTICIPATE_URL, URL);
                        map.put(Constants.MessageConstants.SUPPLIER_TENANT_ID, String.valueOf(rfxLineSupplier.getSupplierTenantId()));
                        map.put(Constants.MessageConstants.SUPPLIER_COMPANY_ID, String.valueOf(rfxLineSupplier.getSupplierCompanyId()));
                        messageHelper.sendMessage(new SpfmMessageSender(rfxHeader.getTenantId(), Objects.isNull(rfxHeader.getEstimatedStartTime()) ? Constants.MessageCodeConstants.RFX_RELEASE : Constants.MessageCodeConstants.RFX_RELEASE_FASTBIDDING, map));
                        if (Objects.equals(BaseConstants.Digital.ONE, rfxHeader.getStartFlag())) {
                            messageHelper.sendMessage(new SpfmMessageSender(rfxHeader.getTenantId(), Constants.MessageCodeConstants.QUOTATION_START, map));
                        }
                    };
                }
            } else {
                List<CompanyDTO> companyDTOList = commonQueryRepository.listPartnerCompany(rfxHeader.getTenantId(), rfxHeader.getCompanyId());
                if (CollectionUtils.isNotEmpty(companyDTOList)) {
                    String supplierQuotationTitleName = promptUtil.selectPrompt(HitachiSourceConstants.PromptConstants.SUPPLIER_QUOTATION_PROMPT_KEY, HitachiSourceConstants.PromptConstants.SUPPLIER_QUOTATION_PROMPT_CODE, LanguageHelper.language(), "供应商报价");
                    for (CompanyDTO companyDTO : companyDTOList) {
                        RfxLineSupplier rfxLineSupplier = new RfxLineSupplier();
                        rfxLineSupplier.setTenantId(rfxHeader.getTenantId());
                        rfxLineSupplier.setSupplierCompanyId(companyDTO.getCompanyId());
                        rfxLineSupplier.setSupplierTenantId(companyDTO.getPartnerTenantId());
                        if(!checkSendMessageSupplier(rfxHeader,rfxLineSupplier)) { continue; }
                        String serverName = commonQueryRepository.selectServerName(companyDTO.getPartnerTenantId());
                        map.put(Constants.MessageConstants.SOURCE_ADDRESS, "<a target='_blank' href='" + ShareConstants.LINK_ADDRESS_PREFIX + serverName + "/app/ssrc/inquiry-hall/rfx-detail/" + rfxHeader.getRfxHeaderId() + "'>" + getLinkString(userDetails) + "</a>");
                        String URL = BidConstants.MessageConstants.BID_PARTICIPATE_LINK + "/ssrc/supplier-quotation\'," + "path:\'/ssrc/supplier-quotation/detail/" + rfxHeader.getRfxHeaderId() + "/" + companyDTO.getCompanyId() + "/operation\',title:\'"+supplierQuotationTitleName+"\'})\">" + rfxHeader.getRfxNum() + "</a>";
                        map.put(Constants.MessageConstants.RFX_PARTICIPATE_URL, URL);
                        map.put(Constants.MessageConstants.SUPPLIER_TENANT_ID, String.valueOf(companyDTO.getPartnerTenantId()));
                        map.put(Constants.MessageConstants.SUPPLIER_COMPANY_ID, String.valueOf(companyDTO.getCompanyId()));
                        messageHelper.sendMessage(new SpfmMessageSender(rfxHeader.getTenantId(), Objects.isNull(rfxHeader.getEstimatedStartTime()) ? Constants.MessageCodeConstants.RFX_RELEASE : Constants.MessageCodeConstants.RFX_RELEASE_FASTBIDDING, map));
                        logger.info("SSRC.RFX.QUOTATION_START===== {}=={}", rfxHeader.getStartFlag(), companyDTO.getCompanyName());
                        if (Objects.equals(BaseConstants.Digital.ONE, rfxHeader.getStartFlag())) {
                            messageHelper.sendMessage(new SpfmMessageSender(rfxHeader.getTenantId(), Constants.MessageCodeConstants.QUOTATION_START, map));
                        }
                    };
                }
            }
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    public static String getLinkString(CustomUserDetails userDetails) {
        String linkString;
        if (ShareConstants.Language.ZH.equals(userDetails.getLanguage())) {
            linkString = ShareConstants.LINK_ADDRESS_CN;
        } else {
            linkString = ShareConstants.LINK_ADDRESS_EN;
        }
        return linkString;
    }

    /**
     * 新增供应商消息发送
     */
    public void sendMessageForSupplier(RfxLineSupplier rfxLineSupplier) {
        if (null == rfxLineSupplier) {
            return;
        }
        RfxHeader rfxHeader = rfxHeaderRepository.selectByPrimaryKey(rfxLineSupplier.getRfxHeaderId());
        if (null == rfxHeader) {
            return;
        }
        Map<String, String> map = new ConcurrentHashMap<>();
        map.put(Constants.MessageConstants.RFX_TITLE, rfxHeader.getRfxTitle());
        map.put(Constants.MessageConstants.SOURCE_TYPE, ShareConstants.SourceCategory.RFX);
        map.put(Constants.MessageConstants.SOURCE_HEADER_ID, String.valueOf(rfxHeader.getRfxHeaderId()));
        map.put(Constants.MessageConstants.RFX_START_TIME, format.format(rfxHeader.getQuotationStartDate()));
        map.put(Constants.MessageConstants.COMPANY_NAME, rfxHeader.getCompanyName());
        map.put(Constants.MessageConstants.RFX_NUMBER, rfxHeader.getRfxNum());
        map.put(Constants.MessageConstants.ORGANIZATION_ID, String.valueOf(rfxHeader.getTenantId()));
        map.put(Constants.MessageConstants.COMPANY_ID, String.valueOf(rfxHeader.getCompanyId()));
        map.put(Constants.MessageConstants.SUPPLIER_TENANT_ID, String.valueOf(rfxLineSupplier.getSupplierTenantId()));
        map.put(Constants.MessageConstants.SUPPLIER_COMPANY_ID, String.valueOf(rfxLineSupplier.getSupplierCompanyId()));
        String supplierQuotationTitleName = promptUtil.selectPrompt(HitachiSourceConstants.PromptConstants.SUPPLIER_QUOTATION_PROMPT_KEY, HitachiSourceConstants.PromptConstants.SUPPLIER_QUOTATION_PROMPT_CODE, LanguageHelper.language(), "供应商报价");
        String URL = BidConstants.MessageConstants.BID_PARTICIPATE_LINK + "/ssrc/supplier-quotation\'," + "path:\'/ssrc/supplier-quotation/detail/" + rfxHeader.getRfxHeaderId() + "/" + rfxLineSupplier.getSupplierCompanyId() + "/operation\',title:\'"+supplierQuotationTitleName+"\'})\">" + rfxHeader.getRfxNum() + "</a>";
        map.put(Constants.MessageConstants.RFX_PARTICIPATE_URL, URL);
        messageHelper.sendMessage(new SpfmMessageSender(rfxHeader.getTenantId(), Constants.MessageCodeConstants.RFX_RELEASE, map));
    }

    /**
     * RFX询价方还价通知
     */
    public void sendMessageForBargain(List<RfxQuotationLine> rfxQuotationLineList) {
        RfxQuotationHeader temp = rfxQuotationHeaderRepository.selectByPrimaryKey(rfxQuotationLineList.get(0).getQuotationHeaderId());
        RfxHeader rfxHeader = rfxHeaderRepository.selectByPrimaryKey(temp.getRfxHeaderId());
        Map<String, String> map = new ConcurrentHashMap<>();
        map.put(Constants.MessageConstants.COMPANY_NAME, rfxHeader.getCompanyName());
        map.put(Constants.MessageConstants.RFX_NUMBER, rfxHeader.getRfxNum());
        map.put(Constants.MessageConstants.RFX_TITLE, rfxHeader.getRfxTitle());
        map.put(Constants.MessageConstants.ORGANIZATION_ID, String.valueOf(rfxHeader.getTenantId()));
        map.put(Constants.MessageConstants.COMPANY_ID, String.valueOf(rfxHeader.getCompanyId()));
        map.put(Constants.MessageConstants.CURRENT_TIME, format.format(new Date()));
        map.put(Constants.MessageConstants.SOURCE_HEADER_ID, String.valueOf(rfxHeader.getRfxHeaderId()));
        map.put(Constants.MessageConstants.SOURCE_TYPE, ShareConstants.SourceCategory.RFX);
        String serverName = commonQueryRepository.selectServerName(rfxHeader.getTenantId());
        map.put(Constants.MessageConstants.SOURCE_ADDRESS, "<a target='_blank' href='" + ShareConstants.LINK_ADDRESS_PREFIX + serverName + "/app/ssrc/inquiry-hall/rfx-detail/" + rfxHeader.getRfxHeaderId() + "'>" + getLinkString(DetailsHelper.getUserDetails()) + "</a>");
        List<Long> idList = new ArrayList<>();
        rfxQuotationLineList.forEach(rfxQuotationLine -> {
            if (rfxQuotationLine != null) {
                idList.add(rfxQuotationLine.getQuotationHeaderId());
            }
        });
        String ids = idList.stream().map(Object::toString).collect(Collectors.joining(","));
        List<RfxQuotationHeader> rfxQuotationHeaderList = rfxQuotationHeaderRepository.selectByIds(ids);
        String supplierQuotationTitleName = promptUtil.selectPrompt(HitachiSourceConstants.PromptConstants.SUPPLIER_QUOTATION_PROMPT_KEY, HitachiSourceConstants.PromptConstants.SUPPLIER_QUOTATION_PROMPT_CODE, LanguageHelper.language(), "供应商报价");
        rfxQuotationHeaderList.forEach(rfxQuotationHeader -> {
            if (null != rfxQuotationHeader) {
                map.put(Constants.MessageConstants.SUPPLIER_TENANT_ID, String.valueOf(rfxQuotationHeader.getSupplierTenantId()));
                map.put(Constants.MessageConstants.SUPPLIER_COMPANY_ID, String.valueOf(rfxQuotationHeader.getSupplierCompanyId()));
                String rfxQuotationAddress = BidConstants.MessageConstants.BID_PARTICIPATE_LINK + "/ssrc/supplier-quotation\'," + "path:\'/ssrc/supplier-quotation/inquiry-price/" + rfxQuotationHeader.getQuotationHeaderId() + "\',title:\'"+supplierQuotationTitleName+"\'})\">" + rfxHeader.getRfxNum() + "</a>";
                map.put(Constants.MessageConstants.RFX_QUOTATION_ADDRESS, rfxQuotationAddress);
                messageHelper.sendMessage(new SpfmMessageSender(rfxHeader.getTenantId(), Constants.MessageCodeConstants.BARGAIN, map));
            }
        });
    }


    public void sendMessageToSuppler(RfxQuotationHeaderDTO rfxQuotationHeaderDTO) {
        Map<String, String> paramMap = new ConcurrentHashMap<>();
        getInitMap(paramMap, rfxQuotationHeaderDTO.getRfxNum(), rfxQuotationHeaderDTO.getRfxTitle(), rfxQuotationHeaderDTO.getTenantId(), rfxQuotationHeaderDTO.getRfxHeaderId());
        paramMap.put(Constants.MessageConstants.FEEDBACK_END_TIME, format.format(rfxQuotationHeaderDTO.getQuotationEndDate()));
        Date date = new Date();
        BigDecimal subtract = BigDecimal.valueOf(rfxQuotationHeaderDTO.getQuotationEndDate().getTime()).subtract(BigDecimal.valueOf(date.getTime()));
        BigDecimal timeRemaining = subtract.divide(BigDecimal.valueOf(3600000), 1, BigDecimal.ROUND_HALF_UP);
        paramMap.put(Constants.MessageConstants.TIME_REMAINING, String.valueOf(timeRemaining));
        String supplierQuotationTitleName = promptUtil.selectPrompt(HitachiSourceConstants.PromptConstants.SUPPLIER_QUOTATION_PROMPT_KEY, HitachiSourceConstants.PromptConstants.SUPPLIER_QUOTATION_PROMPT_CODE, LanguageHelper.language(), "供应商报价");
        String URL = BidConstants.MessageConstants.BID_PARTICIPATE_LINK + "/ssrc/supplier-quotation\'," + "path:\'/ssrc/supplier-quotation/detail/" + rfxQuotationHeaderDTO.getRfxHeaderId() + "/" + rfxQuotationHeaderDTO.getSupplierCompanyId() + "/operation\',title:\'"+supplierQuotationTitleName+"\'})\">" + rfxQuotationHeaderDTO.getRfxNum() + "</a>";
        paramMap.put(Constants.MessageConstants.RFX_PARTICIPATE_URL, URL);
        paramMap.put(Constants.MessageConstants.SOURCE_ADDRESS, commonQueryRepository.selectServerName(rfxQuotationHeaderDTO.getTenantId()) + "/app/ssrc/supplier-quotation/list");
        paramMap.put(Constants.MessageConstants.COMPANY_ID, rfxQuotationHeaderDTO.getCompanyId().toString());
        paramMap.put(Constants.MessageConstants.SUPPLIER_TENANT_ID, String.valueOf(rfxQuotationHeaderDTO.getSupplierTenantId()));
        paramMap.put(Constants.MessageConstants.SUPPLIER_COMPANY_ID, String.valueOf(rfxQuotationHeaderDTO.getSupplierCompanyId()));
        paramMap.put(Constants.MessageConstants.SOURCE_HEADER_ID, String.valueOf(rfxQuotationHeaderDTO.getRfxHeaderId()));
        paramMap.put(Constants.MessageConstants.SOURCE_TYPE, ShareConstants.SourceCategory.RFX);
        logger.info("=====================send message paras = {}========================", JSON.toJSONString(paramMap));
        //不需要传userDetail，lang会message服务默认赋值的
        messageHelper.sendMessage(new SpfmMessageSender(rfxQuotationHeaderDTO.getTenantId(), Constants.MessageCodeConstants.RFX_QUOTATION_END_SUPPLIER, paramMap));
        //更新发送标识
        createMessageMark(rfxQuotationHeaderDTO.getSendMethodFlag(),
                rfxQuotationHeaderDTO.getTenantId(),
                rfxQuotationHeaderDTO.getRfxHeaderId(),
                ShareConstants.MessageMarkCode.SSRC_RFX_QUO_END_SUPPLIER_HOUR,
                ShareConstants.MessageMarkCode.SSRC_RFX_QUO_END_SUPPLIER_DAY);
    }

    /**
     * 手动发送邮件给供应商，提醒报价时间快结束
     * @param rfxHeader
     * @param rfxMonitorSupplierDTO
     */
    @Async
    public void sendSupplierQuotationNotice(RfxHeader rfxHeader, RfxMonitorSupplierDTO rfxMonitorSupplierDTO, CustomUserDetails userDetails ){
        try {
            DetailsHelper.setCustomUserDetails(userDetails);
            Map<String, String> paramMap = new ConcurrentHashMap<>();
            getInitMap(paramMap, rfxHeader.getRfxNum(), rfxHeader.getRfxTitle(), rfxHeader.getTenantId(), rfxHeader.getRfxHeaderId());
            paramMap.put(Constants.MessageConstants.FEEDBACK_END_TIME, format.format(rfxHeader.getQuotationEndDate()));
            Date date = new Date();
            BigDecimal subtract = BigDecimal.valueOf(rfxHeader.getQuotationEndDate().getTime()).subtract(BigDecimal.valueOf(date.getTime()));
            BigDecimal timeRemaining = subtract.divide(BigDecimal.valueOf(3600000), 1, BigDecimal.ROUND_HALF_UP);
            paramMap.put(Constants.MessageConstants.TIME_REMAINING, String.valueOf(timeRemaining));
            String supplierQuotationTitleName = promptUtil.selectPrompt(HitachiSourceConstants.PromptConstants.SUPPLIER_QUOTATION_PROMPT_KEY, HitachiSourceConstants.PromptConstants.SUPPLIER_QUOTATION_PROMPT_CODE, LanguageHelper.language(), "供应商报价");
            String URL = BidConstants.MessageConstants.BID_PARTICIPATE_LINK + "/ssrc/supplier-quotation\'," + "path:\'/ssrc/supplier-quotation/detail/" + rfxHeader.getRfxHeaderId() + "/" + rfxMonitorSupplierDTO.getSupplierCompanyId() + "/operation\',title:\'"+supplierQuotationTitleName+"\'})\">" + rfxHeader.getRfxNum() + "</a>";
            paramMap.put(Constants.MessageConstants.RFX_PARTICIPATE_URL, URL);
            paramMap.put(Constants.MessageConstants.SOURCE_ADDRESS, commonQueryRepository.selectServerName(rfxHeader.getTenantId()) + "/app/ssrc/supplier-quotation/list");
            paramMap.put(Constants.MessageConstants.COMPANY_ID, rfxHeader.getCompanyId().toString());
            paramMap.put(Constants.MessageConstants.SUPPLIER_TENANT_ID, String.valueOf(rfxMonitorSupplierDTO.getSupplierTenantId()));
            paramMap.put(Constants.MessageConstants.SUPPLIER_COMPANY_ID, String.valueOf(rfxMonitorSupplierDTO.getSupplierCompanyId()));
            paramMap.put(Constants.MessageConstants.SOURCE_HEADER_ID, String.valueOf(rfxHeader.getRfxHeaderId()));
            paramMap.put(Constants.MessageConstants.SOURCE_TYPE, ShareConstants.SourceCategory.RFX);
            logger.info("=====================send message paras = {}========================", JSON.toJSONString(paramMap));
            messageHelper.sendMessage(getSpfmMessageSender(rfxHeader.getTenantId(), Constants.MessageCodeConstants.RFX_QUOTATION_NEAR_END, paramMap, DetailsHelper.getUserDetails()));
            //更新发送标识
            MessageMark messageMark = new MessageMark(rfxHeader.getTenantId(),
                    rfxHeader.getRfxHeaderId(),
                    ShareConstants.SourceCategory.RFX,
                    Constants.MessageCodeConstants.RFX_QUOTATION_NEAR_END,
                    BaseConstants.Flag.YES);
            messageMarkRepository.insertSelective(messageMark);
        } finally {
            SecurityContextHolder.clearContext();
        }

    }

    private void getInitMap(Map<String, String> paramMap, String rfxNum, String rfxTitle, Long organizationId, Long rfxHeaderId) {
        paramMap.put(Constants.MessageConstants.RFX_NUMBER, rfxNum);
        paramMap.put(Constants.MessageConstants.RFX_TITLE, rfxTitle);
        paramMap.put(Constants.MessageConstants.ORGANIZATION_ID, String.valueOf(organizationId));
        paramMap.put(Constants.MessageConstants.RFX_HEADER_ID, String.valueOf(rfxHeaderId));
        paramMap.put(Constants.MessageConstants.SOURCE_HEADER_ID, String.valueOf(rfxHeaderId));
    }

    public Boolean checkSendMessageSupplier(RfxHeader rfxHeader, RfxLineSupplier supplier) {
        this.logger.info("判断此供应商是否需要发送信息");
        Map<String, String> parameter = new HashMap();
        parameter.put("companyId", rfxHeader.getCompanyId() + "");
        parameter.put("sourceCategory", rfxHeader.getSourceCategory());
        parameter.put("sourceMethod", rfxHeader.getSourceMethod());
        Long stageId = this.commonQueryRepository.selectSupplierLiftCycleStageId(rfxHeader.getTenantId(), rfxHeader.getCompanyId(), supplier.getSupplierTenantId(), supplier.getSupplierCompanyId());
        parameter.put("supplierLifeCycle", stageId + "");
        List<Long> categories = this.commonQueryRepository.selectSupplierCategories(rfxHeader.getTenantId(), supplier.getSupplierTenantId(), supplier.getSupplierCompanyId());
        if (CollectionUtils.isEmpty(categories)) {
            String rfxSendSupplierQuoteResult = (String) CnfHelper.select(rfxHeader.getTenantId(), "SITE.SSRC_RFX_SUPPLIER_QUOTE_JUDGE", String.class).invokeWithParameter(parameter);
            return !StringUtils.equals(rfxSendSupplierQuoteResult, "N") ? Boolean.TRUE : Boolean.FALSE;
        } else {
            Iterator var6 = categories.iterator();

            String rfxSendSupplierQuoteResult;
            do {
                if (!var6.hasNext()) {
                    return Boolean.FALSE;
                }

                Long category = (Long)var6.next();
                parameter.put("supplierCategory", category + "");
                rfxSendSupplierQuoteResult = (String)CnfHelper.select(rfxHeader.getTenantId(), "SITE.SSRC_RFX_SUPPLIER_QUOTE_JUDGE", String.class).invokeWithParameter(parameter);
            } while(StringUtils.equals(rfxSendSupplierQuoteResult, "N"));

            return Boolean.TRUE;
        }
    }

    public void createMessageMark(Integer sendMethodFlag, Long tenantId, Long rfxHeaderId, String messageMarkCode, String messageCode) {
        MessageMark messageMark;
        if (BaseConstants.Flag.YES.equals(sendMethodFlag)) {
            messageMark = new MessageMark(tenantId, rfxHeaderId, "RFX", messageCode, BaseConstants.Flag.YES);
            this.messageMarkRepository.insertSelective(messageMark);
        } else {
            messageMark = new MessageMark(tenantId, rfxHeaderId, "RFX", messageMarkCode, BaseConstants.Flag.YES);
            this.messageMarkRepository.insertSelective(messageMark);
        }

    }

    public SpfmMessageSender getSpfmMessageSender(Long tenantId, String messageCode, Map<String, String> args, CustomUserDetails customUserDetails) {
        SpfmMessageSender spfmMessageSender = new SpfmMessageSender(tenantId, messageCode, args);
        spfmMessageSender.setLang(customUserDetails.getLanguage());
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("spfmMessageSender:{}", spfmMessageSender.toString());
        }

        return spfmMessageSender;
    }
}
