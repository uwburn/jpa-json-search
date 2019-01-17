package it.mgt.uti.jpajsonsearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import it.mgt.util.jpa.JpaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Entity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class JpaJsonSearchFilterCondition extends JpaJsonSearchFilter {

    private final static Logger logger = LoggerFactory.getLogger(JpaJsonSearchFilterCondition.class);

    enum Operator {

        EQ(" = "),
        NEQ(" <> "),
        GT(" > "),
        GTE(" >= "),
        LT(" < "),
        LTE(" <= "),
        BETWEEN(" BETWEEN "),
        NOT_BETWEEN(" NOT_BETWEEN "),
        IN(" IN "),
        NOT_IN(" NOT IN "),
        LIKE(" LIKE "),
        NOT_LIKE(" NOT LIKE "),
        LIKE_WILDCARD(" LIKE "),
        NOT_LIKE_WILDCARD(" NOT LIKE "),
        NULL("IS NULL"),
        NOT_NULL("IS NOT NULL");

        final String jpql;

        Operator(String jpql) {
            this.jpql = jpql;
        }

        static public Operator parse(String placeholder) {
            switch (placeholder) {
                case "$eq":
                    return EQ;
                case "$neq":
                    return NEQ;
                case "$gt":
                    return GT;
                case "$gte":
                    return GTE;
                case "$lt":
                    return LT;
                case "$lte":
                    return LTE;
                case "$bt":
                    return BETWEEN;
                case "$nbt":
                    return NOT_BETWEEN;
                case "$in":
                    return IN;
                case "$nin":
                    return NOT_IN;
                case "$lk":
                    return LIKE;
                case "$nlk":
                    return NOT_LIKE;
                case "$lkw":
                    return LIKE_WILDCARD;
                case "nlkw":
                    return NOT_LIKE_WILDCARD;
                case "$null":
                    return NULL;
                case "$nnull":
                    return NOT_NULL;
                default:
                    throw new JpaJsonSearchException("Unknown " + placeholder + " placeholder");
            }
        }
    }

    private JpaJsonSearch<?> search;
    private String name;
    private Operator operator;
    private JpaJsonSearchParameter parameter;
    private Object value;

    JpaJsonSearchFilterCondition(JpaJsonSearch<?> search, String name) {
        this.search = search;
        this.name = name;
        this.parameter = search.parametersMap.get(name);
    }

    JpaJsonSearchFilterCondition(JpaJsonSearch<?> search, String name, Operator operator) {
        this(search, name);

        this.operator = operator;
    }

    JpaJsonSearchFilterCondition(JpaJsonSearch<?> search, String name, Operator operator, Object value) {
        this(search, name, operator);

        this.value = value;
    }

    @Override
    JpaJsonSearchJpqlAndParams buildJpql() {
        logger.trace("Building JPQL");

        if (parameter == null)
            return new JpaJsonSearchJpqlAndParams();

        StringBuilder jpql = new StringBuilder();

        switch (operator) {
            case NULL:
            case NOT_NULL:
                jpql.append(parameter.path)
                        .append(operator.jpql);
                return new JpaJsonSearchJpqlAndParams(jpql);
            case BETWEEN:
            case NOT_BETWEEN:
                if (!(value instanceof List))
                    throw new JpaJsonSearchException("Expected value for between operator to be a list");

                List<?> list = (List<?>) value;
                if (list.size() != 2)
                    throw new JpaJsonSearchException("Expected value for between operator to be a list with 2 elements");

                String firstParamName = search.buildParameterName(name);
                String secondParamName = search.buildParameterName(name);

                Map<String, Object> params = new HashMap<>();
                params.put(firstParamName, list.get(0));
                params.put(secondParamName, list.get(1));

                jpql.append(parameter.path)
                        .append(operator.jpql)
                        .append(JpaJsonSearch.COLON)
                        .append(firstParamName)
                        .append(JpaJsonSearch.AND)
                        .append(JpaJsonSearch.COLON)
                        .append(secondParamName);
                return new JpaJsonSearchJpqlAndParams(jpql, params);
            default:
                String paramName = search.buildParameterName(name);

                jpql.append(parameter.path)
                        .append(operator.jpql)
                        .append(JpaJsonSearch.COLON)
                        .append(paramName);
                return new JpaJsonSearchJpqlAndParams(jpql, paramName, value);
        }
    }

    private <T> Object parseParam(String value, Class<T> type) {
        logger.trace("Parsing parameter");

        if (JpaUtils.getAnnotation(type, Entity.class) != null) {
            Class<?> idType = JpaUtils.getIdClass(type);
            Object id = JpaUtils.parseParam(value, idType);
            T entity = search.em.find(type, id);
            if (entity == null)
                throw new JpaJsonSearchException("Reference error");

            return entity;
        }
        else {
            return JpaUtils.parseParam(value, type);
        }
    }

    @Override
    JpaJsonSearchFilter parse(JsonNode jsonNode) {
        logger.trace("Parsing condition filter");

        if (jsonNode.size() != 1)
            throw new JpaJsonSearchException("Expected condition filter element to have a single child");

        Map.Entry<String, JsonNode> child = jsonNode.fields().next();

        operator = Operator.parse(child.getKey());
        if (child.getValue().isArray()) {
            ArrayNode arrayNode = (ArrayNode) child.getValue();
            value = StreamSupport.stream(arrayNode.spliterator(), false)
                    .map(jn ->  parseParam(jn.asText(), parameter.type))
                    .collect(Collectors.toList());
        }
        else {
            value = parseParam(child.getValue().asText(), parameter.type);
        }

        if ((operator == Operator.LIKE_WILDCARD || operator == Operator.NOT_LIKE_WILDCARD) && value instanceof String)
            value = JpaJsonSearch.PERCENT + value + JpaJsonSearch.PERCENT;

        return this;
    }

    @Override
    void remove() {

    }

    public String getName() {
        return name;
    }

    public Operator getOperator() {
        return operator;
    }

    public Object getValue() {
        return value;
    }
}
