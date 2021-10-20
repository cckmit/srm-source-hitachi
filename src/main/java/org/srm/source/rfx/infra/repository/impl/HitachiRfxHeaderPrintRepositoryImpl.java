package org.srm.source.rfx.infra.repository.impl;

import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.srm.source.rfx.api.dto.HitachiRfxPrintDTO;
import org.srm.source.rfx.api.dto.HitachiRfxPrintQueryDTO;
import org.srm.source.rfx.domain.repository.HitachiRfxHeaderPrintRepository;
import org.srm.source.rfx.infra.mapper.HitachiRfxHeaderPrintMapper;

/**
 * @author guotao.yu@hand-china.com 2021/3/22 下午8:59
 */
@Component
public class HitachiRfxHeaderPrintRepositoryImpl implements HitachiRfxHeaderPrintRepository {
    @Autowired
    private HitachiRfxHeaderPrintMapper hitachiRfxHeaderPrintMapper;

    @Override
    public Page<HitachiRfxPrintDTO> rfxPrintListQuery(HitachiRfxPrintQueryDTO hitachiRfxPrintQueryDTO, PageRequest pageRequest){
        return PageHelper.doPage(pageRequest, ()->hitachiRfxHeaderPrintMapper.rfxPrintListQuery(hitachiRfxPrintQueryDTO));
    }
}
