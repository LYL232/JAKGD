package pers.lyl232.jakgd.repository.relationship;

import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import pers.lyl232.jakgd.exception.QueryExecuteException;
import pers.lyl232.jakgd.repository.BaseSessionRepository;

import java.util.*;

@Repository
public class RelationshipSessionRepository extends BaseSessionRepository {
    final static private Logger logger = LoggerFactory.getLogger(RelationshipSessionRepository.class);

    public RelationshipSessionRepository(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    /**
     * 创建指定类型关系
     *
     * @param startNode  starNode id
     * @param endNode    endNode id
     * @param type       关系类型
     * @param properties 属性Map
     * @return 是否创建成功
     */
    public boolean create(Long startNode, Long endNode, String type,
                          Map<String, Object> properties) {
        return create(getSession(), startNode, endNode, type, properties);
    }

    /**
     * 查找两个节点间指定类型的关系id
     *
     * @param startNode 起始节点
     * @param endNode   终止节点
     * @param relType   关系类型
     * @return 关系id列表
     */
    public List<Long> getRelationshipIdBetweenNodesByType(
            Long startNode, Long endNode, String relType) {
        return getRelationshipIdBetweenNodesByType(getSession(), startNode, endNode, relType);
    }

    /**
     * 查找两个节点间指定类型的关系id
     *
     * @param session   querying session
     * @param startNode 起始节点
     * @param endNode   终止节点
     * @param relType   关系类型
     * @return 关系id列表
     */
    public List<Long> getRelationshipIdBetweenNodesByType(
            Session session, Long startNode, Long endNode, String relType) {
        String query = String.format("match (n) where id(n) = $startNode with n " +
                        "match (n)-[r:`%s`]->(m) where id(m) = $endNode return id(r) as id",
                relType);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("startNode", startNode);
        parameters.put("endNode", endNode);
        Result result = session.query(query, parameters);
        List<Long> idList = new LinkedList<>();
        for (Map<String, Object> res : result) {
            idList.add((Long) res.get("id"));
        }
        return idList;
    }

    /**
     * 根据列表的id删除指定关系
     *
     * @param idList 指定的id列表
     * @return 成功删除的个数
     */
    public Integer deleteByIdList(List<Long> idList) {
        return deleteByIdList(getSession(), idList);
    }

    /**
     * 根据列表的id删除指定关系
     *
     * @param session querying session
     * @param idList  指定的id列表
     * @return 成功删除的个数
     */
    public Integer deleteByIdList(Session session, List<Long> idList) {
        return session.query("match ()-[r]-() where id(r) in $idList " +
                "delete r", Collections.singletonMap("idList", (Object) idList))
                .queryStatistics().getRelationshipsDeleted();
    }

    /**
     * 查询两个节点间指定类型的关系的数量
     *
     * @param startNode 起始节点
     * @param endNode   终止节点
     * @param type      关系类型
     * @return 关系的数量
     * @throws QueryExecuteException 语句执行异常
     */
    public Long queryNodesRelationshipCountByType(
            Long startNode, Long endNode, String type) throws QueryExecuteException {
        return queryNodesRelationshipCountByType(getSession(), startNode, endNode, type);
    }

    /**
     * 查询两个节点间指定类型的关系的数量
     *
     * @param session   querying session
     * @param startNode 起始节点
     * @param endNode   终止节点
     * @param type      关系类型
     * @return 关系的数量
     * @throws QueryExecuteException 语句执行异常
     */
    public Long queryNodesRelationshipCountByType(
            Session session, Long startNode, Long endNode, String type)
            throws QueryExecuteException {
        Map<String, Object> properties = new HashMap<>();
        properties.put("startNode", startNode);
        properties.put("endNode", endNode);
        String query = String.format("match (n) where id(n) = $startNode with n " +
                        "match (n)-[r:%s]->(m) where id(m) = $endNode return count(r) as count",
                type);
        Result result = session.query(query, properties);
        for (Map<String, Object> res : result) {
            Object r = res.get("count");
            if (r == null) {
                throw new QueryExecuteException(query);
            }
            return (Long) r;
        }
        return null;
    }

    /**
     * 创建指定类型关系
     *
     * @param session    querying session
     * @param startNode  starNode id
     * @param endNode    endNode id
     * @param type       关系类型
     * @param properties 属性Map
     * @return 是否创建成功
     */
    public boolean create(Session session, Long startNode, Long endNode,
                          String type, Map<String, Object> properties) {
        String propertiesString = "";
        if (!properties.isEmpty()) {
            List<String> propertiesList = new LinkedList<>();
            for (String key : properties.keySet()) {
                propertiesList.add(String.format("%s:$%s", key, key));
            }
            propertiesString = String.format("{%s}", String.join(",", propertiesList));
        }
        String query = String.format("match(n) where id(n) = %d with n " +
                        "match (m) where id(m) = %d create " +
                        "(n)-[r:%s%s]->(m) return id(r) as res",
                startNode, endNode, type, propertiesString);
        logger.info("query in create: " + query);
        return session.query(query, properties).queryStatistics()
                .getRelationshipsCreated() > 0;
    }

    /**
     * 设置关系的属性
     *
     * @param id         关系的id
     * @param properties 关系的属性
     * @throws QueryExecuteException 语句执行异常
     */
    public void setProperties(Long id, Map<String, String> properties)
            throws QueryExecuteException {
        setProperties(getSession(), id, properties);
    }

    /**
     * 设置关系的属性
     *
     * @param session    querying session
     * @param id         关系的id
     * @param properties 关系的属性
     * @throws QueryExecuteException 语句执行异常
     */
    public void setProperties(Session session, Long id, Map<String, String> properties)
            throws QueryExecuteException {
        if (properties.isEmpty()) {
            return;
        }
        List<String> propertiesList = new LinkedList<>();
        for (String key : properties.keySet()) {
            propertiesList.add(String.format("r.%s=$%s", key, key));
        }
        String query = String.format("match ()-[r]-() where id(r) = %d set %s",
                id, String.join(",", propertiesList));
        logger.info("query in setProperties: " + query);
        if (session.query(query, properties).queryStatistics()
                .getPropertiesSet() < 1) {
            throw new QueryExecuteException(query);
        }
    }

    /**
     * 删除指定关系
     *
     * @param id 关系id
     * @return 是否删除成功
     */
    public boolean delete(Long id) {
        return delete(getSession(), id);
    }

    /**
     * 删除指定关系
     *
     * @param session querying session
     * @param id      关系id
     * @return 是否删除成功
     */
    public boolean delete(Session session, Long id) {
        String query = String.format("match ()-[r]-() where id(r) = %d delete r", id);
        logger.info("query in delete: " + query);
        return session.query(query, Collections.emptyMap()).queryStatistics()
                .getRelationshipsDeleted() > 0;
    }
}
