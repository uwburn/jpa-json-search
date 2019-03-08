package it.mgt.uti.jpajsonsearch;

import com.fasterxml.jackson.databind.JsonNode;

public abstract class JpaJsonSearchFilter<T> {

    protected JpaJsonSearch<T> search;
    private JpaJsonSearchFilterLogical container;

    public JpaJsonSearchFilter(JpaJsonSearch<T> search, JpaJsonSearchFilterLogical container) {
        this.search = search;
        this.container = container;
    }

    abstract JpaJsonSearchJpqlAndParams buildJpql();

    abstract JpaJsonSearchFilter parse(JsonNode jsonNode);

    abstract void remove();

    public JpaJsonSearch<T> search() {
        return search;
    }

    public JpaJsonSearchFilterLogical logicalContainer() {
        return container;
    }

}
