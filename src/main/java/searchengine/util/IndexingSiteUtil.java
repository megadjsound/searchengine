package searchengine.util;

import searchengine.config.JsoupConnection;
import searchengine.model.SiteEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

public class IndexingSiteUtil extends RecursiveTask {
    private final SiteEntity siteEntity;
    private final StorageUtil storage;
    private final JsoupConnection jsoupConnection;
    private final List<String> hrefList;
    private final List<IndexingSiteUtil> taskList = new ArrayList<>();

    public IndexingSiteUtil(SiteEntity siteEntity, StorageUtil storage, JsoupConnection jsoupConnection, List<String> hrefList) {
        this.siteEntity = siteEntity;
        this.storage = storage;
        this.jsoupConnection = jsoupConnection;
        this.hrefList = new ArrayList<>();
    }

    @Override
    protected Object compute() {
        return null;
    }
}
