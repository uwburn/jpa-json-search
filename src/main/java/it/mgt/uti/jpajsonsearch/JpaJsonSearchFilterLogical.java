package it.mgt.uti.jpajsonsearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JpaJsonSearchFilterLogical extends JpaJsonSearchFilter {

    private final static Logger logger = LoggerFactory.getLogger(JpaJsonSearchFilter.class);

    enum Conjunction {

        AND(" AND "),
        OR(" OR ");

        final String jpql;

        Conjunction(String jpql) {
            this.jpql = jpql;
        }

        static Conjunction parse(String placeholder) {
            switch (placeholder) {
                case "$and":
                    return AND;
                case "$or":
                    return OR;
                default:
                    throw new JpaJsonSearchException("Unknown " + placeholder + " placeholder");
            }
        }
    };

    private Conjunction conjunction;
    private JpaJsonSearch<?> search;
    private List<JpaJsonSearchFilter> filters = new ArrayList<>();

    JpaJsonSearchFilterLogical(JpaJsonSearch<?> search, Conjunction conjunction) {
        this.search = search;
        this.conjunction = conjunction;
    }

    @Override
    JpaJsonSearchJpqlAndParams buildJpql() {
        logger.trace("Building JPQL");

        JpaJsonSearchJpqlAndParams jpqlAndParams = new JpaJsonSearchJpqlAndParams();

        if (filters.size() == 0)
            return jpqlAndParams;

        jpqlAndParams.append(JpaJsonSearch.OPEN_PARENTHESIS);

        boolean first = true;
        for (JpaJsonSearchFilter filter : filters) {
            if (first)
                first = false;
            else
                jpqlAndParams.append(conjunction.jpql);

            jpqlAndParams.append(filter.buildJpql());
        }

        jpqlAndParams.append(JpaJsonSearch.CLOSE_PARENTHESIS);

        return jpqlAndParams;
    }

    @Override
    JpaJsonSearchFilter parse(JsonNode jsonNode) {
        logger.trace("Parsing logical filter");

        if (jsonNode == null)
            return this;

        if (!jsonNode.isArray())
            throw new JpaJsonSearchException("Expected logical filter to be an array node");

        ArrayNode arrayNode = (ArrayNode) jsonNode;
        for (Iterator<JsonNode> it = arrayNode.elements(); it.hasNext(); ) {
            JsonNode elementNode = it.next();

            if (!elementNode.isObject())
                throw new JpaJsonSearchException("Expected logical filter element to be an object node");

            ObjectNode elementObjectNode = (ObjectNode) elementNode;

            if (elementObjectNode.size() != 1)
                throw new JpaJsonSearchException("Expected logical filter element to have a single child");

            Map.Entry<String, JsonNode> child = elementNode.fields().next();

            switch (child.getKey()) {
                case "$and":
                    and().parse(child.getValue());
                    break;
                case "$or":
                    or().parse(child.getValue());
                    break;
                default:
                    filters.add(new JpaJsonSearchFilterCondition(search, child.getKey()).parse(child.getValue()));
                    break;
            }
        }

        return this;
    }

    int size() {
        return filters.size();
    }

    public List<JpaJsonSearchFilter> getFilters() {
        return new ArrayList<>(filters);
    }

    public List<JpaJsonSearchFilterCondition> getConditionFilters() {
        return filters.stream()
                .filter(f -> f instanceof JpaJsonSearchFilterCondition)
                .map(f -> (JpaJsonSearchFilterCondition)f)
                .collect(Collectors.toList());
    }

    public JpaJsonSearchFilterLogical remove(JpaJsonSearchFilter filter) {
        filters.remove(filter);
        filter.remove();

        return this;
    }

    @Override
    void remove() {
        filters.forEach(JpaJsonSearchFilter::remove);
    }

    public JpaJsonSearchFilterLogical clear() {
        filters.clear();
        return this;
    }

    public JpaJsonSearchFilterLogical and() {
        JpaJsonSearchFilterLogical filter = new JpaJsonSearchFilterLogical(this.search, Conjunction.AND);
        filters.add(filter);
        return filter;
    }

    public JpaJsonSearchFilterLogical or() {
        JpaJsonSearchFilterLogical filter = new JpaJsonSearchFilterLogical(this.search, Conjunction.AND);
        filters.add(filter);
        return filter;
    }

    private JpaJsonSearchFilterLogical addFilterCondition(String name, JpaJsonSearchFilterCondition.Operator operator, Object value) {
        filters.add(new JpaJsonSearchFilterCondition(search, name, operator, value));
        return this;
    }

    private JpaJsonSearchFilterLogical addFilterCondition(String name, JpaJsonSearchFilterCondition.Operator operator) {
        filters.add(new JpaJsonSearchFilterCondition(search, name, operator));
        return this;
    }

    public JpaJsonSearchFilterLogical equal(String name, Object value) {
        return addFilterCondition(name, JpaJsonSearchFilterCondition.Operator.EQ, value);
    }

    public JpaJsonSearchFilterLogical notEqual(String name, Object value) {
        return addFilterCondition(name, JpaJsonSearchFilterCondition.Operator.NEQ, value);
    }

    public JpaJsonSearchFilterLogical greaterThan(String name, Object value) {
        return addFilterCondition(name, JpaJsonSearchFilterCondition.Operator.GT, value);
    }

    public JpaJsonSearchFilterLogical addFilterGreaterThanEqual(String name, Object value) {
        return addFilterCondition(name, JpaJsonSearchFilterCondition.Operator.GTE, value);
    }

    public JpaJsonSearchFilterLogical lessThan(String name, Object value) {
        return addFilterCondition(name, JpaJsonSearchFilterCondition.Operator.LT, value);
    }

    public JpaJsonSearchFilterLogical lessThanEqual(String name, Object value) {
        return addFilterCondition(name, JpaJsonSearchFilterCondition.Operator.LTE, value);
    }

    public JpaJsonSearchFilterLogical between(String name, List<?> value) {
        return addFilterCondition(name, JpaJsonSearchFilterCondition.Operator.BETWEEN, value);
    }

    public JpaJsonSearchFilterLogical notBetween(String name, List<?> value) {
        return addFilterCondition(name, JpaJsonSearchFilterCondition.Operator.NOT_BETWEEN, value);
    }

    public JpaJsonSearchFilterLogical like(String name, Object value) {
        return addFilterCondition(name, JpaJsonSearchFilterCondition.Operator.LIKE, value);
    }

    public JpaJsonSearchFilterLogical notLike(String name, Object value) {
        return addFilterCondition(name, JpaJsonSearchFilterCondition.Operator.NOT_LIKE, value);
    }

    public JpaJsonSearchFilterLogical likeWildcard(String name, String value) {
        return addFilterCondition(name, JpaJsonSearchFilterCondition.Operator.LIKE_WILDCARD, value);
    }

    public JpaJsonSearchFilterLogical notLikeWildcard(String name, String value) {
        return addFilterCondition(name, JpaJsonSearchFilterCondition.Operator.NOT_LIKE_WILDCARD, value);
    }

    public JpaJsonSearchFilterLogical isNull(String name) {
        return addFilterCondition(name, JpaJsonSearchFilterCondition.Operator.NULL);
    }

    public JpaJsonSearchFilterLogical isNotNull(String name) {
        return addFilterCondition(name, JpaJsonSearchFilterCondition.Operator.NOT_NULL);
    }
}
