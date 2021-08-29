package pers.lyl232.jakgd.repository;

import org.neo4j.ogm.model.Result;
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
     * @param idList  指定的id列表
     * @return 节点数据
     */
    public List<NodeData> getNodesById(List<Long> idList) {
        if (idList.size() < 1) {
            return new LinkedList<>();
        }
        String query = "match (n) where id(n) in $idList " +
                "and not '__internal' in labels(n) return id(n) as id, " +
                "labels(n) as labels, properties(n) as properties";
        return getResultNodeData(getSession().query(query, Collections.singletonMap("idList", idList)));
    }

    /**
     * 在节点的属性中搜索关键字
     *
     * @param key             关键字
     * @param queryProperties 搜索的属性
     * @param skip            跳过的个数
     * @param limit           限制返回个数
     * @return 指定属性中含有关键字的节点数据
     */
    public List<NodeData> searchInNodeProperties(
            String key, Set<String> queryProperties, long skip, long limit) {
        if (queryProperties.size() < 1) {
            return new LinkedList<>();
        }
        StringBuilder query = new StringBuilder("match (n) where not '__internal' in labels(n) and (");
        List<String> propertiesMatchCode = new LinkedList<>();
        for (String property : queryProperties) {
            propertiesMatchCode.add(String.format("n.%s =~ '(?i).*%s.*'", property, key));
        }
        query.append(String.join(" or ", propertiesMatchCode)).append(
                String.format(
                        ") with distinct n return id(n) as id, labels(n) as labels, " +
                                "properties(n) as properties order by id(n) skip %d limit %d",
                        skip, limit));
        logger.info("query in searchInNodeProperties: " + query.toString());

        return getResultNodeData(getSession().query(query.toString(), Collections.emptyMap()));
    }

    /**
     * 在节点的属性中搜索关键字，返回结果的个数
     *
     * @param key             关键字
     * @param queryProperties 搜索的属性
     * @return 指定属性中含有关键字的节点的个数
     */
    public long searchCountInNodeProperties(String key, Set<String> queryProperties) {
        if (queryProperties.size() < 1) {
            return 0;
        }
        StringBuilder query = new StringBuilder("match (n) where not '__internal' in labels(n) and (");
        List<String> propertiesMatchCode = new LinkedList<>();
        for (String property : queryProperties) {
            propertiesMatchCode.add(String.format("n.%s =~ '(?i).*%s.*'", property, key));
        }
        query.append(String.join(" or ", propertiesMatchCode))
                .append(") with distinct n return count(n) as c");
        logger.info("query in searchCountInNodeProperties: " + query.toString());

        return getCountResult(getSession().query(query.toString(), Collections.emptyMap()), "c");
    }

    /**
     * 在节点的label中搜索关键字
     *
     * @param key   关键字
     * @param skip  跳过节点个数
     * @param limit 限制返回个数
     * @return 标签中含有关键字的节点数据
     */
    public List<NodeData> searchInNodeLabel(String key, long skip, long limit) {
        Set<String> labels = searchAllMatchedLabels(key);
        if (labels.size() < 1) {
            return new LinkedList<>();
        }
        StringBuilder query = new StringBuilder("match (n) where not '__internal' in labels(n) and (");
        List<String> labelMatchedCode = new LinkedList<>();
        for (String label : labels) {
            labelMatchedCode.add(String.format("n:%s", label));
        }
        query.append(String.join(" or ", labelMatchedCode))
                .append(String.format(") with distinct n" +
                        " return id(n) as id, labels(n) as labels, properties(n) as properties" +
                        " order by id(n)" +
                        " skip %d limit %d", skip, limit
                ));
        logger.info("query in searchInNodeLabel: " + query.toString());
        return getResultNodeData(getSession().query(query.toString(), Collections.emptyMap()));
    }

    /**
     * 在节点的label中搜索关键字，返回符合要求的节点个数
     *
     * @param key 关键字
     * @return 标签中含有关键字的节点数据
     */
    public long searchCountInNodeLabel(String key) {
        Set<String> labels = searchAllMatchedLabels(key);
        if (labels.size() < 1) {
            return 0;
        }
        StringBuilder query = new StringBuilder("match (n) where not '__internal' in labels(n) and (");
        List<String> labelMatchedCode = new LinkedList<>();
        for (String label : labels) {
            labelMatchedCode.add(String.format("n:%s", label));
        }
        query.append(String.join(" or ", labelMatchedCode))
                .append(") with distinct n return count(n) as count");
        logger.info("query in searchCountInNodeLabel: " + query.toString());
        return getCountResult(getSession().query(query.toString(), Collections.emptyMap()), "count");
    }

    /**
     * 在关系类型中搜索关键字
     *
     * @param key     关键字
     * @param limit   限制返回个数
     * @return 关系类型中含有关键字的数据
     */
    public List<RelationshipData> searchInRelationshipType(String key, long skip, long limit) {
        String query = String.format("match ()-[r]->() where not type(r) contains '__' " +
                "and type(r) =~ '(?i).*%s.*' with distinct r return id(r) as id, " +
                "id(startNode(r)) as startNode, id(endNode(r)) as endNode, " +
                "type(r) as type, properties(r) as properties order by id(r) skip %d limit %d",
                key, skip, limit);
        logger.info("query in searchInRelationshipType: " + query);
        return getResultRelationshipData(getSession().query(query, Collections.emptyMap()));
    }

    /**
     * 在关系类型中搜索关键字, 返回符合的关系个数
     *
     * @param key     关键字
     * @return 关系类型中含有关键字的数据个数
     */
    public long searchCountInRelationshipType(String key) {
        String query = String.format("match ()-[r]->() where not type(r) contains '__' " +
                "and type(r) =~ '(?i).*%s.*' with distinct r return count(r) as c", key);
        logger.info("query in searchCountInRelationshipType: " + query);
        return getCountResult(getSession().query(query, Collections.emptyMap()), "c");
    }

    /**
     * 搜索所有包含关键字的label
     *
     * @param key     关键字
     * @return labels集合
     */
    public Set<String> searchAllMatchedLabels(String key) {
        String query;
        // 如果搜索关键字是某个可以展示的内部标签的前端名称, 则去搜索这个label的节点
        if (nameToInternalLabel.containsKey(key)) {
            query = String.format("match (n) unwind labels(n) as label with " +
                    "distinct label where (not label contains '__' and label =~'(?i).*%s.*') " +
                    "or label = '%s' " +
                    "return label", key, nameToInternalLabel.get(key));
        } else {
            query = String.format("match (n) unwind labels(n) as label with " +
                    "distinct label where not label contains '__' and label =~'(?i).*%s.*' " +
                    "return label", key);
        }
        logger.info("query in searchAllMatchedLabels: " + query);
        Set<String> labels = new HashSet<>();
        Result result = getSession().query(query, Collections.emptyMap());
        for (Map<String, Object> record : result) {
            labels.add((String) record.get("label"));
        }
        return labels;
    }

    /**
     * 在关系的属性中搜索关键字
     *
     * @param key             关键字
     * @param queryProperties 搜索的属性
     * @param skip           跳过的记录个数
     * @param limit           限制返回的个数
     * @return 指定属性中含有关键字的关系数据
     */
    public List<RelationshipData> searchInRelationshipProperties(
            String key, Set<String> queryProperties, long skip, long limit) {
        if (queryProperties.size() < 1) {
            return new LinkedList<>();
        }
        StringBuilder query = new StringBuilder("match ()-[r]->() where ");
        List<String> propertiesMatchCode = new LinkedList<>();
        for (String property : queryProperties) {
            propertiesMatchCode.add(String.format("r.%s =~ '(?i).*%s.*'", property, key));
        }
        query.append(String.join(" or ", propertiesMatchCode))
                .append(String.format(" with distinct r " +
                        " return id(r) as id, type(r) as type, properties(r) as properties," +
                        " id(startNode(r)) as startNode, id(endNode(r)) as endNode order by id(r)" +
                        " skip %d limit %d", skip, limit));
        logger.info("query in searchInRelationshipProperties: " + query.toString());
        return getResultRelationshipData(getSession().query(query.toString(), Collections.emptyMap()));
    }

    /**
     * 在关系的属性中搜索关键字，返回符合要求的关系个数
     *
     * @param key             关键字
     * @param queryProperties 搜索的属性
     * @return 指定属性中含有关键字的关系数据
     */
    public long searchCountInRelationshipProperties(String key, Set<String> queryProperties) {
        if (queryProperties.size() < 1) {
            return 0;
        }
        StringBuilder query = new StringBuilder("match ()-[r]->() where ");
        List<String> propertiesMatchCode = new LinkedList<>();
        for (String property : queryProperties) {
            propertiesMatchCode.add(String.format("r.%s =~ '(?i).*%s.*'", property, key));
        }
        query.append(String.join(" or ", propertiesMatchCode))
                .append(" with distinct r return count(r) as c");
        logger.info("query in searchCountInRelationshipProperties: " + query.toString());
        return getCountResult(getSession().query(query.toString(), Collections.emptyMap()), "c");
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

    private long getCountResult(Result result, String key) {
        Long res = null;
        for (Map<String, Object> record : result) {
            assert res == null : "duplicate record: " + record.get(key);
            res = (Long) record.get(key);
        }
        assert res != null : "no record";
        return res;
    }

    // 如果搜索关键字是某个可以展示的内部标签的前端名称, 则去搜索这个label的节点
    final static private Map<String, String> nameToInternalLabel = new HashMap<>();

    static {
        nameToInternalLabel.put("知识", "__knowledge");
    }
}
