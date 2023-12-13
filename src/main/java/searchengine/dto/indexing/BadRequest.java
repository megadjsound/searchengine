package searchengine.dto.indexing;

import lombok.Value;

@Value
//all fields are made private and final by default, and setters are not generated.
public class BadRequest {
    boolean result;
    String error;
}
