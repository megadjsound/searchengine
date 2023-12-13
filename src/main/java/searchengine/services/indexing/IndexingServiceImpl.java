package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexResponse;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.util.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private static final int CORE_COUNT = Runtime.getRuntime().availableProcessors();

    private ExecutorService executorService;

    private final SitesList sitesList;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final GetDtoLemmasUtil getDtoLemmas;
    private final GetDtoIndexUtil getDtoIndex;
    private final UrlUtil urlUtil;
    private final IndexingOnePageUtil indexingOnePage;
    private final DbUtil dbUtil;

    @Override
    public IndexResponse startIndexing() {
        IndexResponse response = new IndexResponse();
        if (isIndexing()) {
            return new IndexResponse(false, LogUtil.ERROR_INDEXING_ALREADY_STARTED);
        }
        StorageUtil.setIsIndexing(true);
        executorService = Executors.newFixedThreadPool(CORE_COUNT);
        for (Site site : sitesList.getSites()) {
            executorService.submit(new IndexingAllPagesUtil
                    (siteRepository, pageRepository,
                            lemmaRepository, indexRepository,
                            getDtoLemmas, getDtoIndex,
                            site, urlUtil, dbUtil)
            );
        }
        response.setResult(true);
        executorService.shutdown();
        return response;
    }

    @Override
    public IndexResponse stopIndexing() {
        if (!dbUtil.isIndexing()) {
            return new IndexResponse(false, LogUtil.ERROR_INDEXING_NOT_STARTED);
        }
        StorageUtil.setIsIndexing(false);
        log.info(LogUtil.LOG_STOP_INDEXING);
        executorService.shutdownNow();
        return new IndexResponse(true);
    }


    @Override
    public IndexResponse indexPage(String page) {
        if (isIndexing()) {
            return new IndexResponse(false, LogUtil.ERROR_INDEXING_ALREADY_STARTED);
        }
        if (isPageAvailable(page) && !page.isEmpty()) {
            log.info(LogUtil.LOG_START_INDEXING_PAGE + page);
            indexingOnePage.start(page);
            log.info(LogUtil.LOG_DONE_INDEXING_PAGE + page);
            return new IndexResponse(true);
        } else {
            log.info(LogUtil.LOG_NOT_AVAILABLE_PAGE + page);
            return new IndexResponse(false, LogUtil.LOG_NOT_AVAILABLE_PAGE);
        }
    }

    private boolean isIndexing() {
        return siteRepository.existsByStatus(StatusType.INDEXING) && StorageUtil.getIsIndexing();
    }


    private boolean isPageAvailable(String page) {
        for (Site site : sitesList.getSites()) {
            String pageHost = urlUtil.getHostFromPage(page);
            if (site.getUrl().contains(pageHost)) {
                return true;
            }
        }
        return false;
    }

    @Scheduled(fixedRateString = "${timing.updateData}")
    private void updateSiteStatusTime() {
        List<SiteEntity> sites = siteRepository.findByStatus(StatusType.INDEXING);
        for (SiteEntity siteEntity : sites) {
            siteEntity.setStatusTime(LocalDateTime.now());
            siteRepository.save(siteEntity);
        }
    }

}
