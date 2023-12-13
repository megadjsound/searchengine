package searchengine.dto.indexing;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
public class IndexResponse {

    private boolean result;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String error;

    public IndexResponse() {
    }

    public IndexResponse(boolean result) {
        this.result = result;
    }

    public IndexResponse(boolean result, String error) {
        this.result = result;
        this.error = error;
    }
}
