package searchengine.util;

import java.util.concurrent.atomic.AtomicBoolean;


public class StorageUtil {
    private static AtomicBoolean isIndexing = new AtomicBoolean(false);

    public static void setIsIndexing(boolean value) {
        isIndexing.set(value);
    }

    public static boolean getIsIndexing() {
        return isIndexing.get();
    }
}
