package it.mgt.uti.jpajsonsearch;

import java.util.List;

public class JpaJsonSearchResult<T> {

    private final List<T> values;
    private final Number count;
    private final int page;
    private final int pageSize;

    public JpaJsonSearchResult(List<T> values, Number count, int page, int pageSize) {
        this.values = values;
        this.count = count;
        this.page = page;
        this.pageSize = pageSize;
    }

    public List<T> getValues() {
        return values;
    }

    public Number getCount() {
        return count;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getPages() {
        return (int) Math.round(Math.ceil(count.doubleValue() / pageSize));
    }

    public <V> List<V> transform(JpaJsonSearchResultTransformer<T> transformer) {
        return transformer.transform(values);
    }
}
