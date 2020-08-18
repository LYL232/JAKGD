package pers.lyl232.jakgd.repository;

import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import pers.lyl232.jakgd.entity.result.NodeData;
import pers.lyl232.jakgd.entity.result.RelationshipData;

import java.util.*;

@Repository
public class SearchSessionRepository extends BaseSessionRepository {
    final static Logger logger = LoggerFactory.getLogger(SearchSessionRepository.class);

    public SearchSessionRepository(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    /**
     * 获取一系列指定id的节点
     *
     * @param session querying session
     * @param idList  指定的id列表
     * @return 节点数据
     */
    public List<NodeData> getNodesById(Session session, List<Long> idList) {
        if (idList.size() < 1) {
            return new LinkedList<>();
        }
        String query = "match (n) where id(n) in $idList " +
                "and not '__internal' in labels(n) return id(n) as id, " +
                "labels(n) as labels, properties(n) as properties";
        return getResultNodeData(session.query(query,
                Collections.singletonMap("idList", idList)));
    }

    /**
     * 在节点的属性中搜索关键字
     *
     * @param session         querying session
     * @param key             关键字
     * @param queryProperties 搜索的属性
     * @param limit           限制返回个数
     * @return 指定属性中含有关键字的节点数据
     */
    public List<NodeData> searchInNodeProperties(
            Session session, String key, Set<String> queryProperties, int limit) {
        if (queryProperties.size() < 1) {
            return new LinkedList<>();
        }
        StringBuilder query = new StringBuilder("match (n) where not '__internal' in labels(n) and (");
        List<String> propertiesMatchCode = new LinkedList<>();
        for (String property : queryProperties) {
            propertiesMatchCode.add(String.format("n.%s =~ '.*%s.*'", property,
                    key));
        }
        query.append(String.join(" or ", propertiesMatchCode))
                .append(") with distinct n " +
                "return id(n) as id, labels(n) as labels, " +
                "properties(n) as properties limit $limit");
        logger.info("query in searchInNodeProperties: " + query.toString());
        return getResultNodeData(session.query(query.toString(),
                Collections.singletonMap("limit", limit)));
    }

    /**
     * 在节点的label中搜索关键字
     *
     * @param session querying session
     * @param key     关键字
     * @param limit   限制返回个数
     * @return 标签中含有关键字的节点数据
     */
    public List<NodeData> searchInNodeLabel(Session session, String key, int limit) {
        Set<String> labels = searchAllMatchedLabels(session, key);
        if (labels.size() < 1) {
            return new LinkedList<>();
        }
        StringBuilder query = new StringBuilder("match (n) where not '__internal' in labels(n) and (");
        List<String> labelMatchedCode = new LinkedList<>();
        for (String label : labels) {
            labelMatchedCode.add(String.format("n:%s", label));
        }
        query.append(String.join(" or ", labelMatchedCode))
                .append(") with distinct n " +
                        "return id(n) as id, labels(n) as labels, properties(n) as properties " +
                        "limit ")
                .append(limit);
        logger.info("query in searchInNodeLabel: " + query.toString());
        return getResultNodeData(getSession().query(query.toString(),
                Collections.emptyMap()));
    }

    /**
     * 在关系类型中搜索关键字
     *
     * @param session querying session
     * @param key     关键字
     * @param limit   限制返回个数
     * @return 关系类型中含有关键字的节点数据
     */
    public List<RelationshipData> searchInRelationshipType(
            Session session, String key, int limit) {
        String query = String.format("match ()-[r]->() where not type(r) contains '__' " +
                "and type(r) =~ '.*%s.*' with distinct r return id(r) as id, " +
                "id(startNode(r)) as startNode, id(endNode(r)) as endNode, " +
                "type(r) as type, properties(r) as properties limit %d", key, limit);
        logger.info("query in searchInRelationshipType: " + query);
        return getResultRelationshipData(session.query(query, Collections.emptyMap()));
    }

    /**
     * 搜索所有包含关键字的label
     *
     * @param session querying session
     * @param key     关键字
     * @return labels集合
     */
    public Set<String> searchAllMatchedLabels(Session session, String key) {
        String query;
        // 如果搜索关键字是某个可以展示的内部标签的前端名称, 则去搜索这个label的节点
        if (nameToInternalLabel.containsKey(key)) {
            query = String.format("match (n) unwind labels(n) as label with " +
                    "distinct label where (not label contains '__' and label =~'.*%s.*') " +
                    "or label = '%s' " +
                    "return label", key, nameToInternalLabel.get(key));
        } else {
            query = String.format("match (n) unwind labels(n) as label with " +
                    "distinct label where not label contains '__' and label =~'.*%s.*' " +
                    "return label", key);
        }
        logger.info("query in searchAllMatchedLabels: " + query);
        Set<String> labels = new HashSet<>();
        Result result = session.query(query, Collections.emptyMap());
        for (Map<String, Object> record : result) {
            labels.add((String) record.get("label"));
        }
        return labels;
    }

    /**
     * 在关系的属性中搜索关键字
     *
     * @param session         querying session
     * @param key             关键字
     * @param queryProperties 搜索的属性
     * @param limit           限制返回的个数
     * @return 指定属性中含有关键字的关系数据
     */
    public List<RelationshipData> searchInRelationshipProperties(
            Session session, String key, Set<String> queryProperties, int limit) {
        if (queryProperties.size() < 1) {
            return new LinkedList<>();
        }
        StringBuilder query = new StringBuilder("match ()-[r]->() where ");
        List<String> propertiesMatchCode = new LinkedList<>();
        for (String property : queryProperties) {
            propertiesMatchCode.add(String.format("r.%s =~ '.*%s.*'", property,
                    key));
        }
        query.append(String.join(" or ", propertiesMatchCode))
                .append(" with distinct r " +
                        "return id(r) as id, type(r) as type, properties(r) as properties, " +
                        "id(startNode(r)) as startNode, id(endNode(r)) as endNode limit ")
                .append(limit);
        logger.info("query in searchInRelationshipProperties: " + query.toString());
        return getResultRelationshipData(session.query(query.toString(),
                Collections.emptyMap()));
    }

    /**
     * 提取result中的NodeData数据
     *
     * @param result query Result object
     * @return NodeData数据
     */
    private List<NodeData> getResultNodeData(Result result) {
        List<NodeData> res = new LinkedList<>();
        for (Map<String, Object> record : result) {
            Set<String> labels = new HashSet<>();
            Collections.addAll(labels, (String[]) record.get("labels"));
            res.add(new NodeData((Long) record.get("id"), labels,
                    getResultProperties(record)));
        }
        return res;
    }

    /**
     * 提取result中的关系数据
     *
     * @param result query Result object
     * @return RelationshipData数据
     */
    private List<RelationshipData> getResultRelationshipData(Result result) {
        List<RelationshipData> res = new LinkedList<>();
        for (Map<String, Object> record : result) {
            res.add(new RelationshipData((Long) record.get("id"),
                    (String) record.get("type"),
                    getResultProperties(record),
                    (Long) record.get("startNode"),
                    (Long) record.get("endNode")));
        }
        return res;
    }

    /**
     * 提取Result中的properties对象
     *
     * @param record query Result object row
     * @return properties map
     */
    private Map<String, String> getResultProperties(Map<String, Object> record) {
        Map<String, String> properties = new HashMap<>();
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) record.get("properties")).entrySet()) {
            properties.put((String) entry.getKey(), (String) entry.getValue());
        }
        return properties;
    }

    // 如果搜索关键字是某个可以展示的内部标签的前端名称, 则去搜索这个label的节点
    final static private Map<String, String> nameToInternalLabel = new HashMap<>();

    static {
        nameToInternalLabel.put("知识", "__knowledge");
    }
}
