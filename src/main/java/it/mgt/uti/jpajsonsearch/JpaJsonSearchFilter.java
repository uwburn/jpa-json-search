package it.mgt.uti.jpajsonsearch;

import com.fasterxml.jackson.databind.JsonNode;

public abstract class JpaJsonSearchFilter {

    abstract JpaJsonSearchJpqlAndParams buildJpql();

    abstract JpaJsonSearchFilter parse(JsonNode jsonNode);

    abstract void remove();

}
