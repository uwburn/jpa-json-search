package it.mgt.uti.jpajsonsearch;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class JpaJsonSearchFactory {

    @PersistenceContext
    private EntityManager em;

    public JpaJsonSearchFactory() {
    }

    public <T> JpaJsonSearch<T> build(Class<T> type) {
        return new JpaJsonSearch<T>(em, type);
    }

}
