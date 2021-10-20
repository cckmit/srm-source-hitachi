package org.srm.source.share.infra.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.srm.source.share.api.dto.QuotaTaxDTO;
import org.srm.source.share.api.dto.QuotaTaxReturnDTO;
import org.srm.source.share.config.SourceFeignClientsConfig;
import org.srm.source.share.infra.feign.fallback.SmdmRemoteFallbackImpl;

import java.util.List;

/**
 * 接口服务feign接口
 *
 * @author liujunjie
 */
@FeignClient(value = "srm-mdm", configuration = SourceFeignClientsConfig.class, fallbackFactory = SmdmRemoteFallbackImpl.class, path = "v1")

public interface HitachiSmdmRemoteService {

    /**
     * mdm 计算定额税
     * @param organizationId
     * @param quotaTaxDTO
     * @return
     */
    @PostMapping("/{organizationId}/tax-services/quota-tax")
    ResponseEntity<QuotaTaxReturnDTO> getQuotaTax(@PathVariable("organizationId") Long organizationId, @RequestBody List<QuotaTaxDTO> quotaTaxDTO);

}