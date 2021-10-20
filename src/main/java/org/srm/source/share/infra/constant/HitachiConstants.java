package org.srm.source.share.infra.constant;

/**
 * description
 *
 * @author guotao.yu@going-link.com  2021/03/22 19:00
 */
public class HitachiConstants {
    public static final String TENANT_NUM = "SRM-HITACHI";

    /**
     * 配置中心编码
     */
    public static class ConfigCenterCode {
        public static final String JAPAN_TAX_CONFIG = "SITE.SMDM.JAPAN_TAX_CONFIG";
    }

    /**
     * 配置表编码
     */
    public static class ConfigTableCode {
        public static final String SCUX_HITACHI_RECEIVER_INFOR = "scux_hitachi_receiver_infor";

        /**
         * 日立物流商城地址是否主地址配置表
         */
        public static final String SCUX_HITACHI_SMALL_ADDRESS_MAIN = "small_address_main";
    }

    /**
     * 错误代码
     */
    public static class ErrorCode {
        /**
         * 区分商务技术时商务权重与技术权重和为一百
         */
        public static final String ERROR_PRINT_DATA_CAN_NOT_EMPTY = "error.print_data_can_not_empty";
        /**
         * 价格适用范围、是否一次性采购字段未维护
         */
        public static final String ERROR_PRICE_SCOPE_AND_ONE_TIME_PURCHASE_EMPTY = "error.price_scope_and_one_time_purchase_empty";
        /**
         * 您不能选择供应商 (ERP) 有多个的组合
         * サプライヤー名（ERP）と所課の組合せは、複数選択できません
         */
        public static final String ERROR_SSRC_QUOTATION_SUPPLIER_MORE = "error.ssrc_quotation_supplier_more";

    }

    /**
     * 适配器
     */
    public static class AdaptorTask {
        private AdaptorTask() {
        }

        /**
         * 获取物料编码
         */
        public static final String SSRC_ITEM_CODE_CREATE = "SSRC_ITEM_CODE_CREATE";
    }
}
