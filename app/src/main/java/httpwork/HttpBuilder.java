package httpwork;

/**
 * Created by chenglin on 2018-1-8.
 */

public class HttpBuilder {
    private boolean cache = false;

    /**
     * 是否用缓存
     */
    public boolean isCache() {
        return cache;
    }

    /**
     * 是否用缓存
     */
    public void setCache(boolean cache) {
        this.cache = cache;
    }

}
