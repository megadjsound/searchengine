package searchengine.util;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class DbUtil {
    final private SiteRepository siteRepository;
    final private PageRepository pageRepository;
    final private LemmaRepository lemmaRepository;
    final private IndexRepository indexRepository;

    public void deletePage(PageEntity page) {
        log.info(LogUtil.LOG_DELETE_PAGE + page.getSiteEntity().getUrl() + page.getPath());
        indexRepository.deleteByPage(page.getId());
        lemmaRepository.deleteByPage(page.getId());
        pageRepository.deleteById(page.getId());
    }

    public void saveSite(SiteEntity site, StatusType statusType, String lastError) {
        site.setStatus(statusType);
        site.setStatusTime(LocalDateTime.now());
        if (lastError != null) {
            site.setLastError(lastError);
        }
        siteRepository.save(site);
    }

    public boolean isIndexing() {
        return siteRepository.existsByStatus(StatusType.INDEXING);
    }

    public void deleteSite(Site site) {
        log.info(LogUtil.LOG_DELETE_DATA + site.getName());
        SiteEntity siteEntity = siteRepository.findByUrl(site.getUrl());
        siteEntity.setStatus(StatusType.INDEXING);
        siteRepository.save(siteEntity);
        siteRepository.delete(siteEntity);
        log.info(LogUtil.LOG_DELETE_DATA_DONE + site.getName());
    }

    public void savePage(PageEntity page) {
        pageRepository.savePage(page.getSiteEntity().getId(), page.getPath(), page.getCode(), page.getContent());
    }
}
