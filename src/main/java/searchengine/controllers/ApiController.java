package searchengine.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.config.ParamQuery;
import searchengine.dto.indexing.IndexResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.indexing.IndexingService;
import searchengine.services.searching.SearchService;
import searchengine.services.statistics.StatisticsService;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    private final SearchService searchService;

    public ApiController(StatisticsService statisticsService, IndexingService indexingService, SearchService searchService, ParamQuery paramQuery) {
        this.statisticsService = statisticsService;
        this.indexingService = indexingService;
        this.searchService = searchService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<IndexResponse> startIndexing() {
        return getIndexingResponse(indexingService.startIndexing());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexResponse> stopIndexing() {
        return getIndexingResponse(indexingService.stopIndexing());
    }

    @PostMapping("/indexPage")
    public ResponseEntity<IndexResponse> indexPage (@RequestParam(name = "url") String url) {
            return getIndexingResponse(indexingService.indexPage(url));
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam(name = "query", required = false, defaultValue = "")
                                         String query,
                                         @RequestParam(name = "site", required = false, defaultValue = "")
                                         String site,
                                         @RequestParam(name = "offset", required = false, defaultValue = "0")
                                         int offset,
                                         @RequestParam(name = "limit", required = false, defaultValue = "20")
                                         int limit) {

        return searchService.search(query, site, offset, limit);
    }

    private ResponseEntity<IndexResponse> getIndexingResponse(IndexResponse indexResponse) {
        if (!indexResponse.isResult()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(indexResponse);
        }
        return ResponseEntity.ok(indexResponse);
    }
}
