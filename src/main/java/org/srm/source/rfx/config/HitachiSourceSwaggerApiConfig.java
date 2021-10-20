package org.srm.source.rfx.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.service.Tag;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * swagger config
 * @author Allen  2021/03/22
 */
@Configuration
public class HitachiSourceSwaggerApiConfig {

    /**
     * 询价单打印-日立物流
     */
    public static final String HITACHI_SOURCE_RFX_PRINT = "Hitachi Rfx Header";

    @Autowired
    public HitachiSourceSwaggerApiConfig(Docket docket) {
        docket.tags(
                new Tag(HITACHI_SOURCE_RFX_PRINT, "询价单打印-日立物流")
        );
    }
}
