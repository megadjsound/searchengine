package searchengine.services.indexing;

import searchengine.dto.indexing.IndexResponse;

public interface IndexingService {
    IndexResponse startIndexing();

    IndexResponse stopIndexing();
    IndexResponse indexPage(String page);
}
