package org.srm.source.bid.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.core.oauth.DetailsHelper;
import org.apache.commons.lang.StringUtils;
import org.hzero.boot.platform.lov.adapter.LovAdapter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.srm.source.bid.app.service.impl.SourceNoticeServiceImpl;
import org.srm.source.bid.domain.entity.PubSourceNotice;
import org.srm.source.bid.domain.entity.SourceNotice;
import org.srm.source.share.infra.constant.HitachiConstants;
import org.srm.web.annotation.Tenant;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@Tenant(HitachiConstants.TENANT_NUM)
public class HitachiSourceNoticeService extends SourceNoticeServiceImpl {

    @Autowired
    private LovAdapter lovAdapter;

    @Override
    public Page<PubSourceNotice> processLovTranslate(String organizationId, String lang, Page<SourceNotice> list) {
        Long organizationIdLong = !"NO_TENANT".equals(organizationId) && !"undefined".equals(organizationId) ? Long.parseLong(organizationId) : null;
        List<PubSourceNotice> pubSourceNoticeList = new ArrayList();

        PubSourceNotice pubSourceNotice;
        for(Iterator var6 = list.iterator(); var6.hasNext(); pubSourceNoticeList.add(pubSourceNotice)) {
            SourceNotice sourceNotice = (SourceNotice)var6.next();
            pubSourceNotice = new PubSourceNotice();
            BeanUtils.copyProperties(sourceNotice, pubSourceNotice);
            if (StringUtils.isBlank(lang)) {
                if (null != DetailsHelper.getUserDetails()) {
                    pubSourceNotice.setSourceCategoryMeaning(this.lovAdapter.queryLovMeaning("SSRC.SOURCE_CATEGORY", null == organizationIdLong ? 0L : organizationIdLong, sourceNotice.getSourceCategory(), DetailsHelper.getUserDetails().getLanguage()));
                } else {
                    pubSourceNotice.setSourceCategoryMeaning(this.lovAdapter.queryLovMeaning("SSRC.SOURCE_CATEGORY", null == organizationIdLong ? 0L : organizationIdLong, sourceNotice.getSourceCategory()));
                }
            } else {
                pubSourceNotice.setSourceCategoryMeaning(this.lovAdapter.queryLovMeaning("SSRC.SOURCE_CATEGORY", null == organizationIdLong ? 0L : organizationIdLong, sourceNotice.getSourceCategory(), lang));
            }
        }

        Page<PubSourceNotice> pubSourceNoticePage = new Page();
        pubSourceNoticePage.setContent(pubSourceNoticeList);
        pubSourceNoticePage.setTotalElements(list.getTotalElements());
        pubSourceNoticePage.setTotalPages(list.getTotalPages());
        pubSourceNoticePage.setNumberOfElements(list.getNumberOfElements());
        pubSourceNoticePage.setSize(list.getSize());
        pubSourceNoticePage.setNumber(list.getNumber());
        return pubSourceNoticePage;
    }
}
