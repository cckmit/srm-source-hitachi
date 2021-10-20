package org.srm.source.share.infra.repository.impl;

import org.hzero.mybatis.base.BaseRepository;
import org.hzero.mybatis.base.impl.BaseRepositoryImpl;
import org.springframework.stereotype.Component;
import org.srm.source.share.domain.entity.PriceAppScope;
import org.srm.source.share.domain.repository.HitachiPriceAppScopeRepository;

@Component
public class HitachiPriceAppScopeRepositoryImpl extends BaseRepositoryImpl<PriceAppScope> implements HitachiPriceAppScopeRepository {
}
