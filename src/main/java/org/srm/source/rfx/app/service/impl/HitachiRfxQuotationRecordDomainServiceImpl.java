package org.srm.source.rfx.app.service.impl;


import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.redis.RedisHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.srm.source.rfx.domain.entity.*;
import org.srm.source.rfx.domain.repository.RfxHeaderRepository;
import org.srm.source.rfx.domain.repository.RfxQuotationHeaderRepository;
import org.srm.source.rfx.domain.repository.RfxQuotationLineRepository;
import org.srm.source.rfx.domain.repository.RfxQuotationRecordRepository;
import org.srm.source.rfx.domain.service.impl.IRfxQuotationRecordDomainServiceImpl;
import org.srm.source.rfx.infra.constant.SourceConstants;
import org.srm.source.rfx.infra.mapper.RfxQuotationRecordMapper;
import org.srm.source.share.domain.entity.RoundHeader;
import org.srm.source.share.domain.repository.RoundHeaderRepository;
import org.srm.source.share.domain.repository.SourceTemplateRepository;
import org.srm.source.share.infra.constant.HitachiConstants;
import org.srm.source.share.infra.constant.ShareConstants;
import org.srm.source.share.infra.mapper.RoundRankLineMapper;


import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;


/**
 * @author mingke.yan@hand-china.com
 * @version 1.0
 * @date 2019-02-24
 */
@Service
@org.srm.web.annotation.Tenant(HitachiConstants.TENANT_NUM)
public class HitachiRfxQuotationRecordDomainServiceImpl extends IRfxQuotationRecordDomainServiceImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(HitachiRfxQuotationRecordDomainServiceImpl.class);

    @Autowired
    private RoundHeaderRepository roundHeaderRepository;
    @Autowired
    private RfxQuotationRecordMapper rfxQuotationRecordMapper;
    @Autowired
    private RoundRankLineMapper roundRankLineMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateQuotationRecords(RfxHeader rfxHeader, List<RfxQuotationLine> rfxQuotationLines) {
        RoundHeader roundHeaderDb = roundHeaderRepository.selectOne(new RoundHeader(rfxHeader.getTenantId(), rfxHeader.getRfxHeaderId(), ShareConstants.SourceCategory.RFX));
        Boolean autoRoundFlag = StringUtils.isNotBlank(rfxHeader.getRoundQuotationRule()) && rfxHeader.getRoundQuotationRule().contains(ShareConstants.RoundQuotationRule.AUTO);
        Boolean roundFlag = (Objects.nonNull(roundHeaderDb) && (Objects.equals(roundHeaderDb.getRoundHeaderStatus(), SourceConstants.RoundHeaderStatus.ROUND_SCORING)
                || Objects.equals(roundHeaderDb.getRoundHeaderStatus(), SourceConstants.RoundHeaderStatus.ROUND_CHECKING)))
                || autoRoundFlag;
        List<Long> quotationIds = rfxQuotationLines.stream().map(RfxQuotationLine::getQuotationLineId).collect(Collectors.toList());
        Map<Long, Long> quotationIdCountMap = new HashMap<>(quotationIds.size());
        List<Long> recordIds = rfxQuotationRecordMapper.selectLatestQuotationRecordId(quotationIds);
        if (CollectionUtils.isNotEmpty(recordIds)) {
            List<RfxQuotationRecord> rfxQuotationRecords = rfxQuotationRecordMapper.selectLatestQuotationCount(recordIds);
            for (RfxQuotationRecord rfxQuotationRecord : rfxQuotationRecords) {
                quotationIdCountMap.put(rfxQuotationRecord.getQuotationLineId(),rfxQuotationRecord.getQuotationCount());
            }
        }
        List<RfxQuotationRecord> rfxQuotationRecordList = new ArrayList<>();
        String quotationNode = this.getQuotationNode(rfxHeader, roundHeaderDb);
        LongAdder longAdder = new LongAdder(); longAdder.add(0L);
        for (RfxQuotationLine rfxQuotationLineParam : rfxQuotationLines) {
            longAdder.increment();
            RfxQuotationRecord rfxQuotationRecord = initRecord(rfxHeader, roundHeaderDb, autoRoundFlag, roundFlag, rfxQuotationLineParam);
            rfxQuotationRecord.setQuotationNode(quotationNode);
            rfxQuotationRecord.setRfxHeaderId(rfxHeader.getRfxHeaderId());
            Long quotationCount = quotationIdCountMap.get(rfxQuotationLineParam.getQuotationLineId());
            rfxQuotationRecord.setQuotationCount(quotationCount == null? 1L : quotationCount + 1);
            //新增的时候 还价日期是没有值的
            rfxQuotationRecord.setBargainDate(null);
            rfxQuotationRecordList.add(rfxQuotationRecord);
            // 插入报价记录
            if (longAdder.longValue() % 1000 == 0) {
                rfxQuotationRecordMapper.insertList(rfxQuotationRecordList);
                for (RfxQuotationRecord quotationRecord : rfxQuotationRecordList) {
                    roundRankLineMapper.batchUpdateRankLineRecordId(quotationRecord);
                }
                rfxQuotationRecordList = new ArrayList<>();
            }
        }

        if (CollectionUtils.isNotEmpty(rfxQuotationRecordList)) {
            rfxQuotationRecordMapper.insertList(rfxQuotationRecordList);
            if (!ShareConstants.RoundQuotationRule.NONE.equals(rfxHeader.getRoundQuotationRule())) {
                for (RfxQuotationRecord quotationRecord : rfxQuotationRecordList) {
                    roundRankLineMapper.batchUpdateRankLineRecordId(quotationRecord);
                }
            }
        }
    }


    private RfxQuotationRecord initRecord(RfxHeader rfxHeader, RoundHeader roundHeaderDb, Boolean autoRoundFlag, Boolean roundFlag, RfxQuotationLine rfxQuotationLineParam) {
        if (BaseConstants.Flag.YES.equals(rfxQuotationLineParam.getAbandonedFlag())) {
            // 放弃，设置报价记录，
            RfxQuotationRecord rfxQuotationRecord = new RfxQuotationRecord();
            rfxQuotationRecord.setQuotationLineId(rfxQuotationLineParam.getQuotationLineId());
            rfxQuotationRecord.setTenantId(rfxQuotationLineParam.getTenantId());
            rfxQuotationRecord.setQuotationPrice(BigDecimal.ZERO);
            rfxQuotationRecord.setQuotationCount(rfxQuotationLineParam.getQuotationCount());
            rfxQuotationRecord.setQuotedBy(rfxHeader.getLastUpdatedBy());
            // rfxQuotationRecord.setQuotedDate(new Date());
            // srm-hitachi-94 bug 修复  竞价记录的报价时间取供应商报价行信息的报价时间
            LOGGER.info("srm-hitachi-94:{}"+rfxQuotationLineParam.getQuotedDate());
            rfxQuotationRecord.setQuotedDate(rfxQuotationLineParam.getQuotedDate());
            rfxQuotationRecord.setValidNetPrice(BigDecimal.ZERO);
            rfxQuotationRecord.setTaxIncludedFlag(rfxQuotationLineParam.getTaxIncludedFlag());
            rfxQuotationRecord.setTaxId(rfxQuotationLineParam.getTaxId());
            rfxQuotationRecord.setTaxRate(rfxQuotationLineParam.getTaxRate());
            rfxQuotationRecord.setBargainFlag(BaseConstants.Flag.NO);
            if (Objects.nonNull(roundHeaderDb)) {
                rfxQuotationRecord.setQuotationRoundNumber(roundHeaderDb.getQuotationRoundNumber());
            }
            if (rfxHeader.getCurrentQuotationRound() != null) {
                rfxQuotationRecord.setQuotationRoundNumber(rfxHeader.getCurrentQuotationRound());
            }
            return rfxQuotationRecord;
        } else {
            RfxQuotationRecord rfxQuotationRecord = new RfxQuotationRecord().initValidQuoteInformation(rfxQuotationLineParam, null);
            if (Objects.nonNull(roundHeaderDb)) {
                rfxQuotationRecord.setQuotationRoundNumber(roundHeaderDb.getQuotationRoundNumber());
            }
            rfxQuotationLineParam.setRoundFlag(autoRoundFlag || roundFlag ?BaseConstants.Flag.YES : BaseConstants.Flag.NO);
            if (rfxHeader.getCurrentQuotationRound() != null) {
                rfxQuotationRecord.setQuotationRoundNumber(rfxHeader.getCurrentQuotationRound());
            }
            if (roundFlag) {
                rfxQuotationRecord.setSourceType(ShareConstants.QuotationRecordSourceType.ROUND);
            }
            return rfxQuotationRecord;
        }
    }

}