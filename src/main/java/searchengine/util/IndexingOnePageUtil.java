package searchengine.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.*;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Slf4j
@Component
public class IndexingOnePageUtil {

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final SitesList sitesList;
    private final UrlUtil urlUtil;
    private final LemmaFinderUtil lemmaFinder;
    private final DbUtil dbUtil;

    public void start(String page) {
        String path = page.endsWith("/") ? urlUtil.getPathToPage(page) : urlUtil.getPathToPage(page) + "/";
        String hostSite = urlUtil.getHostFromPage(page);
        SiteEntity siteEntity;

        if (siteRepository.findByUrlLike("%" + hostSite + "%") == null) {
            String name = getNameSite(page);
            String url = getUrlSite(page);
            siteEntity = new SiteEntity();
            siteEntity.setUrl(url);
            siteEntity.setName(name);
            dbUtil.saveSite(siteEntity, StatusType.INDEXING, null);
            log.info(LogUtil.LOG_ADD_SITE);
        } else {
            siteEntity = siteRepository.findByUrlLike("%" + hostSite + "%");
        }
        PageEntity pageEntityOld = pageRepository.findPageBySiteIdAndPath(siteEntity.getId(), path);
        if (pageEntityOld != null) {
            dbUtil.deletePage(pageEntityOld);
        }

        PageEntity pageEntity = new PageEntity();
        Document document = null;
        try {
            document = urlUtil.getConnection(page);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (document == null) {
            pageEntity.setSiteEntity(siteEntity);
            pageEntity.setPath(path);
            pageEntity.setCode(504);
            pageEntity.setContent("GATEAWAY TIMEOUT");
        } else {
            Connection.Response response = document.connection().response();
            int code = response.statusCode();
            String htmlContent = document.outerHtml();

            pageEntity.setSiteEntity(siteEntity);
            pageEntity.setPath(path);
            pageEntity.setCode(code);
            pageEntity.setContent(htmlContent);
        }

        pageRepository.save(pageEntity);
        addLemmas(pageEntity, siteEntity);

        log.info(LogUtil.LOG_ADD_LEMMAS_AND_INDEX + page);
    }

    private void addLemmas(PageEntity pageEntity, SiteEntity siteEntity) {
        String content = pageEntity.getContent();
        String clearContent = lemmaFinder.removeHtmlTags(content);
        HashMap<String, Integer> lemmasMap = lemmaFinder.collectLemmas(clearContent);
        Set<String> lemmasSet = new HashSet<>(lemmasMap.keySet());

        for (String lemma : lemmasSet) {
            float rank = lemmasMap.get(lemma);
            if (lemmaRepository.findLemmaByLemmaAndSite(lemma, siteEntity) != null) {
                LemmaEntity lemmaEntity = lemmaRepository.findLemmaByLemmaAndSite(lemma, siteEntity);
                lemmaEntity.setFrequency(lemmaEntity.getFrequency() + 1);
                lemmaRepository.save(lemmaEntity);
                addIndex(pageEntity, lemmaEntity, rank);
                dbUtil.saveSite(siteEntity, StatusType.INDEXED, null);
            } else {
                LemmaEntity lemmaEntity = new LemmaEntity();
                lemmaEntity.setSiteEntity(siteEntity);
                lemmaEntity.setLemma(lemma);
                lemmaEntity.setFrequency(1);
                lemmaRepository.save(lemmaEntity);
                addIndex(pageEntity, lemmaEntity, rank);
                dbUtil.saveSite(siteEntity, StatusType.INDEXED, null);
            }
        }
    }

    private void addIndex(PageEntity pageEntity, LemmaEntity lemmaEntity, float rank) {
        IndexEntity indexEntity = new IndexEntity();
        indexEntity.setPageEntity(pageEntity);
        indexEntity.setLemmaEntity(lemmaEntity);
        indexEntity.setRank(rank);
        indexRepository.save(indexEntity);
    }

    private String getNameSite(String page) {
        for (Site site : sitesList.getSites()) {
            String pageHost = urlUtil.getHostFromPage(page);
            if (site.getUrl().contains(pageHost)) {
                return site.getName();
            }
        }
        return "";
    }

    private String getUrlSite(String page) {
        for (Site site : sitesList.getSites()) {
            String pageHost = urlUtil.getHostFromPage(page);
            if (site.getUrl().contains(pageHost)) {
                return site.getUrl();
            }
        }
        return "";
    }
}
