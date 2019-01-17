package it.mgt.uti.jpajsonsearch;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

public class JpaJsonSearchSort {

    enum Order {

        ASC("ASC", "ASC"),
        DESC("DESC", "DESC");

        final String placeholder;
        final String jpql;

        Order(String placeholder, String jpql) {
            this.placeholder = placeholder;
            this.jpql = jpql;
        }

        static Order parse(String placeholder) {
            switch (placeholder) {
                case "ASC":
                    return ASC;
                case "DESC":
                    return DESC;
                default:
                    throw new JpaJsonSearchException("Unknown " + placeholder + " placeholder");
            }
        }
    };

    private JpaJsonSearch<?> search;
    JpaJsonSearchParameter parameter;
    private Order order;

    public JpaJsonSearchSort(JpaJsonSearch<?> search) {
        this.search = search;
    }

    JpaJsonSearchJpqlAndParams buildJpql() {
        if (parameter == null)
            return new JpaJsonSearchJpqlAndParams();

        return new JpaJsonSearchJpqlAndParams(parameter.path)
                .append(JpaJsonSearch.SPACE)
                .append(order.jpql);
    }

    JpaJsonSearchSort parse(JsonNode jsonNode) {
        if (jsonNode.size() != 1)
            throw new JpaJsonSearchException("Expected condition filter element to have a single child");

        Map.Entry<String, JsonNode> child = jsonNode.fields().next();

        parameter = search.parametersMap.get(child.getKey());
        order = Order.parse(child.getValue().textValue());

        return this;
    }

    public static JpaJsonSearchSort buildParse(JpaJsonSearch<?> search, JsonNode jsonNode) {
        return new JpaJsonSearchSort(search).parse(jsonNode);
    }
}
