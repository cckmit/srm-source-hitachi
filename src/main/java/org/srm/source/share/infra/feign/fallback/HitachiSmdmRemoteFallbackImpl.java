package org.srm.source.share.infra.feign.fallback;

import cfca.com.itextpdf.text.log.Logger;
import cfca.com.itextpdf.text.log.LoggerFactory;
import feign.hystrix.FallbackFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.srm.source.share.api.dto.QuotaTaxDTO;
import org.srm.source.share.api.dto.QuotaTaxReturnDTO;
import org.srm.source.share.infra.feign.HitachiSmdmRemoteService;
import org.srm.source.share.infra.feign.SmdmRemoteService;

import java.util.List;

/**
 * 接口服务feign接口-失败调用
 *
 * @author liujunjie
 */
@Component
public class HitachiSmdmRemoteFallbackImpl implements FallbackFactory<HitachiSmdmRemoteService> {
    private static final Logger logger = LoggerFactory.getLogger(SmdmRemoteFallbackImpl.class);

    @Override
    public HitachiSmdmRemoteService create(Throwable cause) {
        return new HitachiSmdmRemoteService() {
            @Override
            public ResponseEntity<QuotaTaxReturnDTO> getQuotaTax(Long organizationId, List<QuotaTaxDTO> quotaTaxDTO) {
                logger.error("Failed to get quota tax data");
                return null;
            }
        };
    }
}
