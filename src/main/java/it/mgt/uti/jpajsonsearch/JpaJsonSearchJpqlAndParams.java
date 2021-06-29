package it.mgt.uti.jpajsonsearch;

import java.util.HashMap;
import java.util.Map;

public class JpaJsonSearchJpqlAndParams {

    StringBuilder jpql;
    Map<String, Object> params;

    public JpaJsonSearchJpqlAndParams() {
        this.jpql = new StringBuilder();
        this.params = new HashMap<>();
    }

    public JpaJsonSearchJpqlAndParams(CharSequence jpql) {
        this.jpql = new StringBuilder(jpql);
        this.params = new HashMap<>();
    }

    public JpaJsonSearchJpqlAndParams(StringBuilder jpql) {
        this.jpql = jpql;
        this.params = new HashMap<>();
    }

    public JpaJsonSearchJpqlAndParams(StringBuilder jpql, String name, Object param) {
        this.jpql = jpql;
        this.params = new HashMap<>();
        this.params.put(name, param);
    }

    public JpaJsonSearchJpqlAndParams(StringBuilder jpql, Map<String, Object> params) {
        this.jpql = jpql;
        this.params = params;
    }

    JpaJsonSearchJpqlAndParams append(CharSequence jpql) {
        this.jpql.append(jpql);
        return this;
    }

    JpaJsonSearchJpqlAndParams append(Map<String, Object> params) {
        this.params.putAll(params);
        return this;
    }

    JpaJsonSearchJpqlAndParams append(JpaJsonSearchJpqlAndParams jpqlAndParams) {
        jpql.append(jpqlAndParams.jpql);
        params.putAll(jpqlAndParams.params);
        return this;
    }

    boolean isEmpty() {
        return this.jpql.length() == 0 && this.params.size() == 0;
    }

}
