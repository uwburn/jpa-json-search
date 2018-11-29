package it.mgt.uti.jpajsonsearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.*;

public class JpaJsonSearch<T> {

    private final static Logger logger = LoggerFactory.getLogger(JpaJsonSearch.class);

    final static String SPACE = " ";
    final static String COMMA = ", ";
    final static String COLON = ":";
    final static String SELECT = "SELECT ";
    final static String COUNT = "COUNT";
    final static String DISTINCT = "DISTINCT ";
    final static String FROM = " FROM ";
    final static String WHERE = " WHERE ";
    final static String ORDER_BY = " ORDER BY ";
    final static String OPEN_PARENTHESIS = "(";
    final static String CLOSE_PARENTHESIS = ")";
    final static String PERCENT = "%";
    final static String AND = " AND ";

    EntityManager em;

    private String alias;
    private String fromJpql;
    private boolean distinct;
    private Class<T> type;
    private int page = 0;
    private int pageSize = 10;

    Map<String, JpaJsonSearchParameter> parametersMap = new HashMap<>();
    private JpaJsonSearchFilterLogical rootFilter = new JpaJsonSearchFilterLogical(this, JpaJsonSearchFilterLogical.Conjunction.AND);
    private List<JpaJsonSearchSort> sorts = new ArrayList<>();
    private Set<String> parameterNames = new HashSet<>();

    public JpaJsonSearch(EntityManager em, Class<T> type) {
        this.em = em;
        this.type = type;
    }

    String buildParameterName(String name) {
        String parameterName;
        do {
            parameterName = name + "_" + Math.round(Math.random() * 100);
        } while (parameterNames.contains(parameterName));
        parameterNames.add(parameterName);

        return parameterName;
    }

    public String getAlias() {
        return alias;
    }

    public JpaJsonSearch<T> alias(String alias) {
        this.alias = alias;
        return this;
    }

    public String getFrom() {
        return this.fromJpql;
    }

    public JpaJsonSearch<T> from(String jpql) {
        this.fromJpql = jpql;
        return this;
    }

    public JpaJsonSearch<T> addParameter(String name, String path, Class<?> type) {
        this.parametersMap.put(name, new JpaJsonSearchParameter(path, type));
        return this;
    }

    public JpaJsonSearchFilterLogical filter() {
        return rootFilter;
    }

    public List<JpaJsonSearchSort> getSorts() {
        return sorts;
    }

    public JpaJsonSearch<T> addSort(JpaJsonSearchSort sort) {
        sorts.add(sort);
        return this;
    }

    public JpaJsonSearch<T> clearSorts() {
        sorts.clear();
        return this;
    }

    public JpaJsonSearch<T> distinct(boolean value) {
        distinct = value;
        return this;
    }

    public JpaJsonSearch<T> page(int page) {
        this.page = page;
        return this;
    }

    public JpaJsonSearch<T> pageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public JpaJsonSearch<T> parse(JsonNode jsonNode) {
        logger.trace("Parsing search");

        if (!jsonNode.isObject())
            throw new JpaJsonSearchException("Expected root node to be an object node");


        ObjectNode objectNode = (ObjectNode) jsonNode;
        rootFilter.parse(objectNode.get("filter"));

        JsonNode subNode = objectNode.get("pageSize");
        if (subNode != null) {
            if (!subNode.isIntegralNumber())
                throw new JpaJsonSearchException("Expected pageSize node to be an integral number");

            pageSize = subNode.asInt();
        }

        subNode = objectNode.get("page");
        if (subNode != null) {
            if (!subNode.isIntegralNumber())
                throw new JpaJsonSearchException("Expected page node to be an integral number");

            page = subNode.asInt();
        }

        parseSorts(objectNode.get("sort"));

        return this;
    }

    private void parseSorts(JsonNode jsonNode) {
        logger.trace("Parsing sorts");

        if (jsonNode == null)
            return;

        if (!jsonNode.isArray())
            throw new JpaJsonSearchException("Expected root node to be an array node");

        ArrayNode arrayNode = (ArrayNode) jsonNode;

        for (Iterator<JsonNode> it = arrayNode.elements(); it.hasNext(); ) {
            JsonNode e = it.next();
            sorts.add(JpaJsonSearchSort.buildParse(this, e));
        }
    }

    private JpaJsonSearchJpqlAndParams buildJpql(boolean count) {
        logger.trace("Executing JPQL");

        JpaJsonSearchJpqlAndParams jpqlAndParams = new JpaJsonSearchJpqlAndParams(SELECT);

        if (count) {
            jpqlAndParams.append(COUNT)
                    .append(OPEN_PARENTHESIS);

            if (distinct)
                jpqlAndParams.append(DISTINCT);

            jpqlAndParams.append(alias.trim())
                    .append(CLOSE_PARENTHESIS);
        }
        else {
            if (distinct)
                jpqlAndParams.append(DISTINCT);

            jpqlAndParams.append(alias.trim());
        }

        jpqlAndParams.append(FROM)
                .append(fromJpql.trim());

        if (rootFilter.size() > 0)
            jpqlAndParams.append(WHERE)
                .append(rootFilter.buildJpql());

        if (sorts.size() > 0)
            jpqlAndParams.append(ORDER_BY);

        boolean first = true;
        for (JpaJsonSearchSort orderBy : sorts) {
            if (first)
                first = false;
            else
                jpqlAndParams.append(COMMA);

            jpqlAndParams.append(orderBy.buildJpql());
        }

        return jpqlAndParams;
    }

    private <V> TypedQuery<V> buildQuery(Class<V> type, boolean count) {
        logger.trace("Building query");

        JpaJsonSearchJpqlAndParams jpqlAndParams = buildJpql(count);

        logger.debug("Resulting JPQL: " + jpqlAndParams.jpql);

        TypedQuery<V> query = em.createQuery(jpqlAndParams.jpql.toString(), type);
        for (Map.Entry<String, Object> e : jpqlAndParams.params.entrySet())
            query.setParameter(e.getKey(), e.getValue());

        if (!count) {
            query.setMaxResults(pageSize);
            query.setFirstResult(page * pageSize);
        }

        return query;
    }

    public List<T> find() {
        logger.trace("Executing find");

        try {
            return buildQuery(type, false).getResultList();
        }
        catch (JpaJsonSearchException e) {
            throw e;
        }
        catch (Exception e) {
            throw new JpaJsonSearchException(e);
        }
    }

    public T findSingle() {
        logger.trace("Executing find single");

        try {
            return buildQuery(type, false).getSingleResult();
        }
        catch (JpaJsonSearchException e) {
            throw e;
        }
        catch (Exception e) {
            throw new JpaJsonSearchException(e);
        }
    }

    public Number count() {
        logger.trace("Executing count");

        try {
            return buildQuery(Number.class, true).getSingleResult();
        }
        catch (JpaJsonSearchException e) {
            throw e;
        }
        catch (Exception e) {
            throw new JpaJsonSearchException(e);
        }
    }

    public JpaJsonSearchResult<T> result() {
        logger.trace("Executing result");

        try {
            return new JpaJsonSearchResult<>(find(), count(), page, pageSize);
        }
        catch (JpaJsonSearchException e) {
            throw e;
        }
        catch (Exception e) {
            throw new JpaJsonSearchException(e);
        }
    }
}
