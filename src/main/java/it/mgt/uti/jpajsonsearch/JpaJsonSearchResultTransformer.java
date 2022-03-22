package it.mgt.uti.jpajsonsearch;

import java.util.List;

public interface JpaJsonSearchResultTransformer<T> {

    <V> List<V> transform(List<T> result);

}
