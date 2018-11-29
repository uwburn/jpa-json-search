package it.mgt.uti.jpajsonsearch;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class JpaJsonSearchBuilder {

    @PersistenceContext
    private EntityManager em;

    public JpaJsonSearchBuilder() {
    }

    public <T> JpaJsonSearch<T> build(Class<T> type) {
        return new JpaJsonSearch<T>(em, type);
    }

}
