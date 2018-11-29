package it.mgt.uti.jpajsonsearch;

class JpaJsonSearchParameter {

    String path;
    Class<?> type;

    JpaJsonSearchParameter(String path, Class<?> type) {
        this.path = path;
        this.type = type;
    }
}
