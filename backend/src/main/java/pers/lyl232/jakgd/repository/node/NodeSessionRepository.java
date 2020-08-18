package pers.lyl232.jakgd.repository.node;

import org.neo4j.ogm.model.QueryStatistics;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import pers.lyl232.jakgd.entity.result.NodeData;
import pers.lyl232.jakgd.exception.QueryExecuteException;
import pers.lyl232.jakgd.repository.BaseSessionRepository;

import java.util.*;

@Repository
public class NodeSessionRepository extends BaseSessionRepository {
    final static Logger logger = LoggerFactory.getLogger(NodeSessionRepository.class);


    public NodeSessionRepository(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    /**
     * 创建节点
     *
     * @param labels             指定labels
     * @param properties         指定属性
     * @param skipInternalLabels 是否跳过内部labels
     * @return 创建成功节点的id, 不成功则null
     */
    public Long create(Set<String> labels, Map<String, String> properties,
                       boolean skipInternalLabels) throws QueryExecuteException {
        return create(getSession(), labels, properties, skipInternalLabels);
    }

    /**
     * 创建节点
     *
     * @param session            querying session
     * @param labels             指定labels
     * @param properties         指定属性
     * @param skipInternalLabels 是否跳过内部labels
     * @return 创建成功节点的id, 不成功则null
     */
    public Long create(Session session, Set<String> labels, Map<String, String> properties,
                       boolean skipInternalLabels) throws QueryExecuteException {
        if (skipInternalLabels) {
            labels.removeIf(label -> label.startsWith("__"));
        }
        String labelStatement = getLabelStatement(labels),
                propertiesStatement = "";
        if (!properties.isEmpty()) {
            List<String> propertiesDefinitionList = new LinkedList<>();
            for (String key : properties.keySet()) {
                propertiesDefinitionList.add(String.format("%s: $%s", key, key));
            }
            propertiesStatement = String.format("{%s}", String.join(",", propertiesDefinitionList));
        }
        String query = String.format("create(n%s%s) return id(n) as id",
                labelStatement, propertiesStatement);
        logger.info("query in create: " + query);
        Result result = session.query(query, properties);
        for (Map<String, Object> res : result) {
            return (Long) res.get("id");
        }
        throw new QueryExecuteException(query);
    }

    /**
     * 覆盖设置节点的属性: 删除多余属性
     *
     * @param session    执行query的session
     * @param node       节点
     * @param properties 属性map
     * @throws QueryExecuteException 语句执行异常
     */
    public void coverNodeProperties(
            Session session, NodeData node, Map<String, String> properties)
            throws QueryExecuteException {
        Set<String> removeProperties = new HashSet<>(node.properties.keySet());
        removeProperties.removeAll(properties.keySet());

        // 保留字段
        removeProperties.removeAll(notRemoveProperties);
        // 不改变字段
        for (String property : notChangeProperties) {
            properties.remove(property);
        }

        if (removeProperties.isEmpty() && properties.isEmpty()) {
            return;
        }

        StringBuilder query = new StringBuilder(String.format(
                "match (n) where id(n) = %d ", node.id));

        if (!properties.isEmpty()) {
            List<String> propertiesSetCode = new LinkedList<>();
            query.append("set ");
            for (String key : properties.keySet()) {
                propertiesSetCode.add(String.format("n.%s = $%s", key, key));
            }
            query.append(String.join(",", propertiesSetCode)).append(" ");
        }

        if (!removeProperties.isEmpty()) {
            List<String> propertiesRemoveCode = new LinkedList<>();
            query.append("remove ");
            for (String key : removeProperties) {
                propertiesRemoveCode.add(String.format("n.%s", key));
            }
            query.append(String.join(",", propertiesRemoveCode));
        }

        logger.info("query in coverNodeProperties: " + query);
        if (session.query(query.toString(), properties).queryStatistics().getPropertiesSet() < 1) {
            throw new QueryExecuteException(query.toString());
        }
    }

    /**
     * 覆盖节点的labels, 删除多余labels
     *
     * @param session            querying session
     * @param node               node
     * @param labels             想要覆盖的节点labels
     * @param originalLabels     原先节点的labels
     * @param skipInternalLabels 是否跳过内部labels (以__开头)
     * @throws QueryExecuteException 语句执行异常
     */
    public void coverNodeLabels(
            Session session, NodeData node, Set<String> labels,
            Set<String> originalLabels, boolean skipInternalLabels)
            throws QueryExecuteException {

        Set<String> removeLabels = new HashSet<>(originalLabels),
                setLabels = new HashSet<>(labels);
        removeLabels.removeAll(labels);
        setLabels.removeAll(originalLabels);

        if (skipInternalLabels) {
            setLabels.removeIf(label -> label.startsWith("__"));
            removeLabels.removeIf(label -> label.startsWith("__"));
        }

        if (setLabels.isEmpty() && removeLabels.isEmpty()) {
            return;
        }

        StringBuilder query = new StringBuilder(
                String.format("match (n) where id(n) = %d ", node.id));

        if (!setLabels.isEmpty()) {
            query.append("set n").append(getLabelStatement(setLabels));
        }
        if (!removeLabels.isEmpty()) {
            query.append(" remove n").append(getLabelStatement(removeLabels));
        }
        logger.info("query in coverNodeLabels: " + query);
        QueryStatistics statistics = session.query(query.toString(), Collections.emptyMap())
                .queryStatistics();
        if (statistics.getLabelsAdded() + statistics.getLabelsRemoved() < 1) {
            throw new QueryExecuteException(query.toString());
        }
    }

    /**
     * 根据节点集合获取cql中label的表示字符串, {"a", "b", "c"} => ":a:b:c"
     *
     * @param labels 节点标签集合
     * @return cql label表示
     */
    static private String getLabelStatement(
            Set<String> labels) {
        if (labels.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (String label : labels) {
            builder.append(":").append(label);
        }
        return builder.toString();
    }

    final public static Set<String> notRemoveProperties = new HashSet<>();

    final public static Set<String> notChangeProperties = new HashSet<>();

    static {
        notRemoveProperties.add("author");
        notRemoveProperties.add("updated");
        notRemoveProperties.add("created");
        notChangeProperties.add("created");
        notChangeProperties.add("author");
    }

}
