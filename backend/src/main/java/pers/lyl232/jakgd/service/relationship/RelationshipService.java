package pers.lyl232.jakgd.service.relationship;

import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.transaction.Transaction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pers.lyl232.jakgd.entity.result.RelationshipData;
import pers.lyl232.jakgd.exception.ExceptionWithBriefJSONResponse;
import pers.lyl232.jakgd.exception.ObjectNotFoundException;
import pers.lyl232.jakgd.exception.QueryExecuteException;
import pers.lyl232.jakgd.repository.node.NodeRepository;
import pers.lyl232.jakgd.repository.relationship.RelationshipRepository;
import pers.lyl232.jakgd.repository.relationship.RelationshipSessionRepository;
import pers.lyl232.jakgd.service.BaseService;

import java.util.List;
import java.util.Map;

@Service
public class RelationshipService extends BaseService {

    final private RelationshipSessionRepository relationshipSessionRepository;

    final private RelationshipRepository relationshipRepository;

    final private NodeRepository nodeRepository;

    public RelationshipService(
            RelationshipSessionRepository relationshipSessionRepository,
            NodeRepository nodeRepository,
            RelationshipRepository relationshipRepository) {
        this.relationshipSessionRepository = relationshipSessionRepository;
        this.relationshipRepository = relationshipRepository;
        this.nodeRepository = nodeRepository;
    }


    /**
     * 获取指定id关系数据
     *
     * @param id 关系id
     * @return 关系数据
     * @throws ObjectNotFoundException 找不到关系
     */
    public RelationshipData getNotNull(Long id) throws ObjectNotFoundException {
        RelationshipData res = relationshipRepository.get(id);
        if (res == null) {
            throw new ObjectNotFoundException("Relationship id: " + id);
        }
        return res;
    }


    /**
     * 删除指定关系
     *
     * @param id 关系id
     */
    public void delete(Long id) {
        relationshipRepository.delete(id);
    }

    /**
     * 创建指定类型关系
     *
     * @param startNode  starNode id
     * @param endNode    endNode id
     * @param type       关系类型
     * @param properties 属性Map
     */
    public boolean create(Long startNode, Long endNode, String type,
                          Map<String, Object> properties) {
        String now = getFormatNowDateStringBySecond();
        properties.put("updated", now);
        properties.put("created", now);
        return relationshipSessionRepository.create(startNode, endNode, type, properties);
    }

    /**
     * 判断两个节点间是否有指定类型的关系
     *
     * @param startNode 起始节点
     * @param endNode   终止节点
     * @param type      关系类型
     * @return 是否有指定类型的关系
     * @throws QueryExecuteException 查询执行错误
     */
    public boolean nodesHasRelationshipByType(Long startNode, Long endNode, String type)
            throws QueryExecuteException {
        return relationshipSessionRepository.queryNodesRelationshipCountByType(
                startNode, endNode, type) > 0;
    }

    /**
     * 设置关系的属性
     *
     * @param id         关系的id
     * @param properties 关系的属性
     */
    public void setProperties(Long id, Map<String, String> properties)
            throws ExceptionWithBriefJSONResponse {
        properties.remove("created");
        properties.put("updated", getFormatNowDateStringBySecond());
        relationshipSessionRepository.setProperties(id, properties);
    }

    /**
     * 获取两个节点间的所有关系
     *
     * @param n node 1
     * @param m node 2
     * @return 节点间关系列表
     * @throws ObjectNotFoundException 找不到节点
     */
    @Transactional(readOnly = true)
    public List<RelationshipData> getRelationshipBetweenNodes(Long n, Long m) throws
            ObjectNotFoundException {
        if (nodeRepository.existCount(n, m) < 2) {
            throw new ObjectNotFoundException("Node id: " + n + ", " + m);
        }
        List<RelationshipData> res = relationshipRepository.
                getDirectionalRelationshipsByEndNodes(n, m);
        res.addAll(relationshipRepository.
                getDirectionalRelationshipsByEndNodes(m, n));
        return res;
    }


    /**
     * 删除两个节点间指定类型的关系
     *
     * @param startNode 起始节点
     * @param endNode   终止节点
     * @param relType   关系类型
     */
    public void delete(Long startNode, Long endNode, String relType)
            throws ExceptionWithBriefJSONResponse {
        Session session = relationshipSessionRepository.getSession();
        Transaction transaction = session.beginTransaction();
        List<Long> idList = relationshipSessionRepository.
                getRelationshipIdBetweenNodesByType(startNode, endNode, relType);
        if (idList.size() == 0) {
            throw new ObjectNotFoundException(String.format("Relationship (%d)-[:%s]->(%d)",
                    startNode, relType, endNode));
        }
        if (idList.size() > relationshipSessionRepository.deleteByIdList(idList)) {
            transaction.rollback();
            throw new QueryExecuteException("delete not completely");
        } else {
            transaction.commit();
        }

    }
}
