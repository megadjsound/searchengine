package searchengine.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import searchengine.config.JsoupConnection;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

@Slf4j
@Component
@RequiredArgsConstructor
public class UrlUtil {

    private final JsoupConnection jsoupConnection;

    public Document getConnection(String url) throws IOException {
        try {
            return Jsoup.connect(url)
                    .userAgent(jsoupConnection.getUserAgent())
                    .referrer(jsoupConnection.getReferrer())
                    .get();
        } catch (HttpStatusException he) {
            throw new IOException(he.getMessage());
        } catch (Exception e) {
            log.error(LogUtil.LOG_EXCEPTION);
            return null;
        }
    }

    public String getPathToPage(String pageUrl) {
        try {
            URL url = new URL(pageUrl);
            return url.getPath();
        } catch (MalformedURLException e) {
            log.error(LogUtil.LOG_MALFORMED_EXCEPTION + pageUrl);
            return "";
        }
    }

    public String getHostFromPage(String page) {
        try {
            URL url = new URL(page);
            return url.getHost();
        } catch (MalformedURLException e) {
            log.error(LogUtil.LOG_MALFORMED_EXCEPTION + page);
            return "";
        }
    }

    public String getToPath(String url) {
        return url.endsWith("/") ? getPathToPage(url) : getPathToPage(url) + "/";
    }

    public String getTitleFromHtml(String content) {
        Document doc = Jsoup.parse(content);
        return doc.title();
    }
}
