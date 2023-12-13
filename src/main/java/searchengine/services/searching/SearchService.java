package searchengine.services.searching;

import org.springframework.http.ResponseEntity;
import searchengine.dto.searching.SearchData;

import java.util.List;

public interface SearchService {
    ResponseEntity<Object> search(String query, String url, int offset, int limit);
    List<SearchData> pageSearch(String query, String url, int offset, int limit);
}
