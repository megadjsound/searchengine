package searchengine.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import searchengine.config.Site;
import searchengine.dto.indexing.DtoIndex;
import searchengine.dto.indexing.DtoLemma;
import searchengine.dto.indexing.DtoPage;
import searchengine.model.*;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

@Slf4j
@RequiredArgsConstructor
public class IndexingAllPagesUtil implements Runnable {

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final GetDtoLemmasUtil getDtoLemmas;
    private final GetDtoIndexUtil getDtoIndex;
    private final Site site;
    private final UrlUtil urlUtil;
    private final DbUtil dbUtil;

    @Override
    public void run() {
        if (siteRepository.findByUrl(site.getUrl()) != null) {
            dbUtil.deleteSite(site);
        }
        try {
            addSite();

            addPages();

            addLemmas();

            addIndex();

            String siteUrl = site.getUrl();
            SiteEntity siteEntity = siteRepository.findByUrl(siteUrl);

            PageEntity currentPageEntity =
                    pageRepository.findPageBySiteIdAndPath(siteEntity.getId(), urlUtil.getToPath(siteUrl));
            StatusType statusType;
            String lastError = null;
            if ((currentPageEntity != null) && (currentPageEntity.getCode() != 200)) {
                statusType = StatusType.FAILED;
                lastError = currentPageEntity.getContent();
            } else {
                statusType = StatusType.INDEXED;
            }
            dbUtil.saveSite(siteEntity, statusType, lastError);
            log.info(LogUtil.LOG_INDEXING_COMPLETED + siteUrl);
        } catch (InterruptedException e) {
            log.info(LogUtil.LOG_INDEXING_BREAK);
            List<SiteEntity> sites = siteRepository.findByStatus(StatusType.INDEXING);
            sites.stream().forEach(s -> {
                dbUtil.saveSite(s, StatusType.FAILED, LogUtil.LOG_INDEXING_BREAK);
            });
        }
    }


    private void addSite() throws InterruptedException {
        if (!Thread.interrupted()) {
            SiteEntity siteEntity = new SiteEntity();
            siteEntity.setUrl(site.getUrl());
            siteEntity.setName(site.getName());
            dbUtil.saveSite(siteEntity, StatusType.INDEXING, null);
            log.info(LogUtil.LOG_ADD_SITE + site.getName());
        } else {
            throw new InterruptedException();
        }
    }

    private void addPages() throws InterruptedException {
        if (!Thread.interrupted()) {

            String siteUrl = site.getUrl();
            log.info(LogUtil.LOG_ADD_PAGES + site.getName());

            ForkJoinPool pool = new ForkJoinPool();
            CopyOnWriteArrayList<String> linksPool = new CopyOnWriteArrayList<>();
            CopyOnWriteArrayList<DtoPage> pages = new CopyOnWriteArrayList<>();

            SiteEntity siteEntity = siteRepository.findByUrl(siteUrl);

            RecursiveTask<CopyOnWriteArrayList<DtoPage>> task =
                    new GetDtoPagesUtil(linksPool, pages, urlUtil, siteUrl, siteUrl);

            CopyOnWriteArrayList<DtoPage> dtoPages = pool.invoke(task);

            if (!StorageUtil.getIsIndexing()) {
                pool.shutdownNow();
                throw new InterruptedException();
            }

            for (DtoPage dtoPage : dtoPages) {
                String pageUrl = dtoPage.getPath();
                String path = pageUrl.endsWith("/") ? urlUtil.getPathToPage(pageUrl)
                        : urlUtil.getPathToPage(pageUrl) + "/";
                PageEntity pageEntity = new PageEntity();
                pageEntity.setSiteEntity(siteEntity);
                pageEntity.setPath(path);
                pageEntity.setCode(dtoPage.getCode());
                pageEntity.setContent(dtoPage.getContent());
                dbUtil.savePage(pageEntity);
                log.info(LogUtil.LOG_ADD_PAGES_DONE + site.getName());
            }

        } else {
            throw new InterruptedException();
        }
    }

    private void addLemmas() throws InterruptedException {
        if (!Thread.interrupted()) {
            log.info(LogUtil.LOG_ADD_LEMMAS + site.getName());
            SiteEntity siteEntity = siteRepository.findByUrl(site.getUrl());
            List<DtoLemma> lemmaList = getDtoLemmas.getLemmas(siteEntity);

            List<LemmaEntity> lemmaEntityList = new CopyOnWriteArrayList<>();
            for (DtoLemma lemma : lemmaList) {
                LemmaEntity lemmaEntity = new LemmaEntity();
                lemmaEntity.setSiteEntity(siteEntity);
                lemmaEntity.setLemma(lemma.getLemma());
                lemmaEntity.setFrequency(lemma.getFrequency());
                lemmaEntityList.add(lemmaEntity);
            }
            lemmaRepository.saveAll(lemmaEntityList);
            log.info(LogUtil.LOG_ADD_LEMMAS_DONE + site.getName());
        } else {
            throw new InterruptedException();
        }
    }

    private void addIndex() throws InterruptedException {
        if (!Thread.interrupted()) {
            log.info(LogUtil.LOG_ADD_INDEX + site.getName());
            SiteEntity siteEntity = siteRepository.findByUrl(site.getUrl());
            List<DtoIndex> dtoIndexList = getDtoIndex.getDtoIndexList(siteEntity);
            List<IndexEntity> indexEntityList = new CopyOnWriteArrayList<>();
            for (DtoIndex dtoIndex : dtoIndexList) {
                PageEntity pageEntity = pageRepository.getById(dtoIndex.getPageId());
                LemmaEntity lemmaEntity = lemmaRepository.getById(dtoIndex.getLemmaId());
                IndexEntity indexEntity = new IndexEntity();
                indexEntity.setPageEntity(pageEntity);
                indexEntity.setLemmaEntity(lemmaEntity);
                indexEntity.setRank(dtoIndex.getRank());
                indexEntityList.add(indexEntity);
            }
            indexRepository.saveAll(indexEntityList);
            log.info(LogUtil.LOG_ADD_INDEX_DONE + site.getName());
        } else {
            throw new InterruptedException();
        }
    }
}
