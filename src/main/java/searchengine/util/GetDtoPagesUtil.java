package searchengine.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.dto.indexing.DtoPage;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RecursiveTask;

@Slf4j
@RequiredArgsConstructor
public class GetDtoPagesUtil extends RecursiveTask<CopyOnWriteArrayList<DtoPage>> {

    private final CopyOnWriteArrayList<String> linksPool;
    private final CopyOnWriteArrayList<DtoPage> dtoPages;
    private final UrlUtil urlUtil;
    private final String siteUrl;
    private final String root;

    @Override
    protected CopyOnWriteArrayList<DtoPage> compute() {

        linksPool.add(siteUrl);
        try {
            if ((!StorageUtil.getIsIndexing()) && (!Thread.currentThread().isInterrupted())) {
                throw new InterruptedException();
            }
            Thread.sleep(150);
            Document document = urlUtil.getConnection(siteUrl);
            Connection.Response response = document.connection().response();
            int code = response.statusCode();
            String htmlContent = document.html();
            DtoPage dtoPage = new DtoPage(siteUrl, code, htmlContent);
            dtoPages.add(dtoPage);
            Set<GetDtoPagesUtil> tasks = new HashSet<>();
            Elements elements = document.select("a[href]");
            for (Element element : elements) {
                String link = element.absUrl("href");
                if (ValidationUtil.isCorrectLink(link)
                        && link.startsWith(root)
                        && !linksPool.contains(link)) {
                    linksPool.add(link);
                    GetDtoPagesUtil task = new GetDtoPagesUtil(linksPool, dtoPages, urlUtil, link, root);
                    task.fork();//Ответляем задачу
                    tasks.add(task);
                }
            }

            for (GetDtoPagesUtil task : tasks) {
                task.join();
            }

        } catch (InterruptedException e) {

        } catch (IOException e) {
            System.out.println(e.getMessage() + " " + siteUrl);
            log.info(e.getMessage() + " " + siteUrl);
        } catch (Exception ex) {
            DtoPage dtoPage = new DtoPage(siteUrl, 500, "INTERNAL SERVER ERROR");
            dtoPages.add(dtoPage);
            System.out.println(ex.getMessage() + " " + siteUrl);
            log.info(ex.getMessage() + " " + siteUrl);
        }
        return dtoPages;
    }
}
