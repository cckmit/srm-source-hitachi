package org.srm.source.share.app.service;

import org.srm.source.share.api.dto.QuotaTaxDTO;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * 日本税率处理
 *
 * @author liujuunjie
 */
public interface HitachiQuotaTaxService {

    /**
     * 处理税率 - 判断
     *
     * @param tenantId    租户
     * @param cnfArgs     配置参数
     * @param handleData  需要处理数据
     * @param preHandler  数据预处理为目标对象QuotaTaxDTO (id必填且唯一)
     * @param postHandler 数据处理后回写数据
     * @param <T>         需要处理数据的数据类型
     * @return 是否执行日本税率
     */
    <T> boolean handleQuotaTaxCalculation(Long tenantId,
                                          Map<String, String> cnfArgs,
                                          List<T> handleData,
                                          BiConsumer<QuotaTaxDTO, T> preHandler,
                                          BiConsumer<QuotaTaxDTO, T> postHandler);

    /**
     * 处理税率 - 直接处理
     *
     * @param tenantId    租户
     * @param handleData  需要处理数据
     * @param preHandler  数据预处理为目标对象QuotaTaxDTO (id必填且唯一)
     * @param postHandler 数据处理后回写数据
     * @param <T>         需要处理数据的数据类型
     * @return 是否执行日本税率
     */
    <T> void handleQuotaTaxCalculation(Long tenantId,
                                       List<T> handleData,
                                       BiConsumer<QuotaTaxDTO, T> preHandler,
                                       BiConsumer<QuotaTaxDTO, T> postHandler);

    /**
     * 获取日本税率标识
     *
     * @param tenantId
     * @param companyId
     * @return
     */
    Integer getJapanTaxFlag(Long tenantId, Long companyId);
}
