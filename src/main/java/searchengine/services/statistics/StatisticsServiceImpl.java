package searchengine.services.statistics;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.SiteEntity;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final SiteRepository siteRepository;

    @Override
    public StatisticsResponse getStatistics() {
        TotalStatistics total = getTotal();
        List<DetailedStatisticsItem> list = getStatisticsData();
        StatisticsData statistics = new StatisticsData(total, list);
        return new StatisticsResponse(true, statistics);
    }

    private TotalStatistics getTotal() {
        long sites = siteRepository.count();
        long pages = pageRepository.count();
        long lemmas = lemmaRepository.count();
        return new TotalStatistics((int) sites, (int) pages, (int) lemmas, true);
    }

    private DetailedStatisticsItem getDetailed(SiteEntity site) {
        String url = site.getUrl();
        String name = site.getName();
        String status = site.getStatus().toString();
        long statusTime = site.getStatusTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        String error = (site.getLastError() != null ? site.getLastError() : "Ошибок нет");
        int pages = pageRepository.countBySiteEntity(site);
        int lemmas = lemmaRepository.countBySiteEntity(site);
        return new DetailedStatisticsItem(url, name, status, statusTime, error, pages, lemmas);
    }

    private List<DetailedStatisticsItem> getStatisticsData() {
        List<SiteEntity> sites = siteRepository.findAll();
        List<DetailedStatisticsItem> result = new ArrayList<>();
        sites.forEach(s -> result.add(getDetailed(s)));
        return result;
    }
}
