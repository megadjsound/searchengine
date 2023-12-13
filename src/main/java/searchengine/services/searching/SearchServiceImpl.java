package searchengine.services.searching;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import searchengine.config.ParamQuery;
import searchengine.dto.indexing.BadRequest;
import searchengine.dto.searching.SearchData;
import searchengine.dto.searching.SearchResponse;
import searchengine.model.*;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.util.LogUtil;
import searchengine.util.LemmaFinderUtil;
import searchengine.util.UrlUtil;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final LemmaFinderUtil lemmaFinderUtil;
    private final UrlUtil urlUtil;
    private final ParamQuery paramQuery;

    private int allPagesCount = 0;

    @Override
    public ResponseEntity<Object> search(String query, String url, int offset, int limit) {

        if (query.trim().length() == 0) {
            return new ResponseEntity<>(new BadRequest(false, LogUtil.ERROR_EMPTY_QUERY),
                    HttpStatus.BAD_REQUEST);
        } else {

            if (!url.isEmpty()) {
                if (!siteRepository.existsByUrlAndStatus(url, StatusType.INDEXED)) {
                    return new ResponseEntity<>(new BadRequest(false, LogUtil.LOG_NOT_AVAILABLE_PAGE),
                            HttpStatus.BAD_REQUEST);
                }
            } else {
                if (siteRepository.findAll()
                        .stream()
                        .map(s -> s.getStatus() != StatusType.INDEXED)
                        .reduce(Boolean.FALSE, Boolean::logicalOr)
                ) {
                    return new ResponseEntity<>(new BadRequest(false, LogUtil.LOG_NOT_SITE_INDEXING),
                            HttpStatus.BAD_REQUEST);
                }
            }
        }
        List<SearchData> searchData;
        searchData = pageSearch(query, url, offset, limit);
        log.info(LogUtil.LOG_FINISH_SITES_SEARCH);

        if (searchData == null) {
            return new ResponseEntity<>(new BadRequest(false, LogUtil.ERROR_NOT_FOUND),
                    HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(new SearchResponse(true, allPagesCount, searchData), HttpStatus.OK);
    }

    @Override
    public List<SearchData> pageSearch(String query, String url, int offset, int limit) {

        List<String> lemmasFromQuery = getQueryIntoLemma(query);

        if (lemmasFromQuery.size() > 0) {
            List<SiteEntity> sites;
            if (url.isEmpty()) {
                sites = siteRepository.findAll();
            } else {
                sites = Stream.of(siteRepository.findByUrl(url)).collect(Collectors.toList());
            }

            List<LemmaEntity> lemmasFromSite = getLemmasFromSite(lemmasFromQuery, sites);

            if (lemmasFromSite.size() == 0) {
                return new ArrayList<>();
            }

            int pages = pageRepository.getPageCountBySiteEntity(sites);

            //Исключать из полученного списка леммы, которые встречаются на слишком большом количестве страниц
            List<LemmaEntity> lemmasFromSiteFilter =
                    lemmasFromSite.stream()
                            .filter(l -> ((float) l.getFrequency() / pages * 100) < paramQuery.getPercentLemma())
                            .collect(Collectors.toList());
            return getSearchDataList(lemmasFromSiteFilter, lemmasFromQuery, offset, limit);
        } else return new ArrayList<>();
    }

    private List<SearchData> getSearchDataList(List<LemmaEntity> lemmasFromSite,
                                               List<String> lemmasFromQuery,
                                               int offset, int limit) {

        if (lemmasFromSite.size() >= lemmasFromQuery.size()) {
            List<PageEntity> sortedPageList = pageRepository.findByLemmas(lemmasFromSite);
            List<IndexEntity> sortedIndexList = indexRepository.findByLemmasAndPages(lemmasFromSite, sortedPageList);
            LinkedHashMap<PageEntity, Float> sortedPagesByAbsRelevance =
                    getSortPagesWithAbsRelevance(sortedPageList, sortedIndexList);
            allPagesCount = sortedPagesByAbsRelevance.size();
            System.out.println(allPagesCount);

            List<SearchData> searchDataList = getSearchData(sortedPagesByAbsRelevance, lemmasFromQuery, offset, limit);
            return searchDataList;
        } else return new ArrayList<>();
    }

    private List<SearchData> getSearchData(LinkedHashMap<PageEntity, Float> sortedPages,
                                           List<String> lemmasFromQuery, int offset, int limit) {
        List<SearchData> searchData = new ArrayList<>();

        for (PageEntity pageEntity : sortedPages.keySet()) {
            if (offset-- > 0) continue;

            String uri = pageEntity.getPath();
            String content = pageEntity.getContent();
            String title = urlUtil.getTitleFromHtml(content);
            SiteEntity siteEntity = pageEntity.getSiteEntity();
            String site = siteEntity.getUrl();
            String siteName = siteEntity.getName();
            Float absRelevance = sortedPages.get(pageEntity);
            String clearContent = lemmaFinderUtil.removeHtmlTags(content);
            String snippet = getSnippet(clearContent, lemmasFromQuery);

            searchData.add(new SearchData(site, siteName, uri, title, snippet, absRelevance));

            if (--limit == 0) break;
        }
        return searchData;
    }

    private String getSnippet(String clearContent, List<String> lemmasFromQuery) {
        List<Integer> lemmaIndex = new ArrayList<>();
        StringBuilder result = new StringBuilder();
        for (String lemma : lemmasFromQuery) {
            lemmaIndex.addAll(lemmaFinderUtil.findLemmaIndexInText(clearContent, lemma));
        }
        Collections.sort(lemmaIndex);
        List<String> wordsList = extractAndHighlightWordsByLemmaIndex(clearContent, lemmaIndex);
        for (int i = 0; i < wordsList.size(); i++) {
            result.append(wordsList.get(i)).append("... ");
            if (i > 3) {
                break;
            }
        }
        return result.toString();
    }

    private List<String> extractAndHighlightWordsByLemmaIndex(String clearContent, List<Integer> lemmaIndex) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < lemmaIndex.size(); i++) {
            int start = lemmaIndex.get(i);
            int end = clearContent.indexOf(" ", start);
            int step = i + 1;
            while (step < lemmaIndex.size() && lemmaIndex.get(step) - end > 0 && lemmaIndex.get(step) - end < 5) {
                end = clearContent.indexOf(" ", lemmaIndex.get(step));
                step += 1;
            }
            i = step - 1;
            String text = getWordsFromIndexWithHighlighting(start, end, clearContent);
            result.add(text);
        }
        result.sort(Comparator.comparingInt(String::length).reversed());
        return result;
    }

    private String getWordsFromIndexWithHighlighting(int start, int end, String clearContent) {
        String word = clearContent.substring(start, end);
        int prevPoint;
        int lastPoint;
        if (clearContent.lastIndexOf(" ", start) != -1) {
            prevPoint = clearContent.lastIndexOf(" ", start);
        } else prevPoint = start;
        if (clearContent.indexOf(" ", end + 30) != -1) {
            lastPoint = clearContent.indexOf(" ", end + 30);
        } else lastPoint = clearContent.indexOf(" ", end);
        String text = clearContent.substring(prevPoint, lastPoint);
        try {
            text = text.replaceAll(Pattern.quote(word), "<b>" + word + "</b>");
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return text;
    }

    private LinkedHashMap<PageEntity, Float> getSortPagesWithAbsRelevance(List<PageEntity> pages,
                                                                          List<IndexEntity> indexes) {
        HashMap<PageEntity, Float> pageWithRelevance = new HashMap<>();
        for (PageEntity page : pages) {
            float relevant = 0;
            for (IndexEntity index : indexes) {
                if (index.getPageEntity().equals(page)) {
                    relevant += index.getRank();
                }
            }
            pageWithRelevance.put(page, relevant);
        }
        HashMap<PageEntity, Float> pagesWithAbsRelevance = new HashMap<>();
        //макс.абс. релевантность среди всех страниц
        float maxRel = Collections.max(pageWithRelevance.values());
        for (PageEntity page : pageWithRelevance.keySet()) {
            float absRelevant = pageWithRelevance.get(page) / maxRel;
            pagesWithAbsRelevance.put(page, absRelevant);
        }
        //Сортировать страницы по убыванию релевантности
        return pagesWithAbsRelevance
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    private List<LemmaEntity> getLemmasFromSite(List<String> lemmas, List<SiteEntity> siteEntity) {
        ArrayList<LemmaEntity> lemmaList = (ArrayList<LemmaEntity>) lemmaRepository.findLemmasBySite(lemmas, siteEntity);
        //Сортировать леммы по частоте встречаемости
        lemmaList.sort(Comparator.comparingInt(LemmaEntity::getFrequency));
        return lemmaList;
    }

    private List<String> getQueryIntoLemma(String query) {
        String[] words = lemmaFinderUtil.arrayContainsRussianWords(query);
        List<String> lemmaList = new ArrayList<>();
        for (String word : words) {
            List<String> lemma = lemmaFinderUtil.getLemma(word);
            lemmaList.addAll(lemma);
        }
        return lemmaList.stream().distinct().toList();
    }
}
