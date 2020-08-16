package pers.lyl232.jakgd.service.node;

import com.alibaba.fastjson.JSONObject;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.transaction.Transaction;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pers.lyl232.jakgd.entity.result.NodeData;
import pers.lyl232.jakgd.entity.result.RelationshipData;
import pers.lyl232.jakgd.exception.ExceptionWithBriefJSONResponse;
import pers.lyl232.jakgd.exception.ObjectNotFoundException;
import pers.lyl232.jakgd.exception.QueryExecuteException;
import pers.lyl232.jakgd.exception.UnknownException;
import pers.lyl232.jakgd.repository.node.NodeRepository;
import pers.lyl232.jakgd.repository.node.NodeSessionRepository;
import pers.lyl232.jakgd.service.BaseService;
import pers.lyl232.jakgd.util.BriefJSONResponse;

import java.util.*;

@Service
public class NodeService extends BaseService {
    final private NodeRepository nodeRepository;

    final private NodeSessionRepository nodeSessionRepository;


    NodeService(NodeRepository nodeRepository,
                NodeSessionRepository nodeSessionRepository) {
        this.nodeRepository = nodeRepository;
        this.nodeSessionRepository = nodeSessionRepository;
    }

    /**
     * 检查指定id的节点是否存在
     *
     * @param id node id
     * @return 是否存在
     */
    public boolean notExists(Long id) {
        return !nodeRepository.exists(id);
    }

    /**
     * 判断是否一系列节点不是全部存在的
     *
     * @param idList 节点id列表
     * @return 是否不是全部存在的
     */
    public boolean notAllExist(Long... idList) {
        if (idList.length < 1) {
            return true;
        }
        return nodeRepository.existCount(idList) < idList.length;
    }

    /**
     * 获取指定id的节点数据
     *
     * @param id node id
     * @return 节点数据 {
     * id, labels, properties
     * }
     * @throws ObjectNotFoundException 找不到异常
     */
    public NodeData getNotNull(Long id) throws ObjectNotFoundException {
        NodeData nodeData = nodeRepository.getNodeById(id);
        if (nodeData == null) {
            throw new ObjectNotFoundException("Node id:" + id);
        }
        return nodeData;
    }

    /**
     * 获得某节点的邻域
     *
     * @param id    node id
     * @param limit 限制
     * @return {
     * nodes: [{
     * id, labels, properties...
     * }]
     * relationships: [{
     * id, type, startNode, endNode, properties...
     * }]
     * }
     * @throws ObjectNotFoundException 找不到节点
     */
    @Transactional(readOnly = true)
    public JSONObject getNeighbourhood(Long id, Integer limit, boolean direct)
            throws ObjectNotFoundException {
        if (notExists(id)) {
            throw new ObjectNotFoundException("Node id: " + id);
        }
        List<RelationshipData> relationships =
                direct ? nodeRepository.getDirectNeighborRelationships(id, limit) :
                        nodeRepository.getNeighborRelationships(id, limit);
        Set<Long> nodeIdSet = new HashSet<>();
        for (RelationshipData relationship : relationships) {
            nodeIdSet.add(relationship.startNode);
            nodeIdSet.add(relationship.endNode);
        }
        List<NodeData> nodes = nodeRepository.getNodesById(new ArrayList<>(nodeIdSet));
        JSONObject res = new JSONObject();
        res.put("nodes", nodes);
        res.put("relationships", relationships);
        return res;
    }

    /**
     * 根据labels和properties创建节点
     *
     * @param username   创建用户名
     * @param labels     节点标签
     * @param properties 节点属性
     * @return node id
     */
    public Long create(
            String username, Set<String> labels, Map<String, String> properties)
            throws QueryExecuteException {
        return create(username, labels, properties, true);
    }

    /**
     * 根据labels和properties创建节点
     *
     * @param username           创建用户名
     * @param labels             节点标签
     * @param properties         节点属性
     * @param skipInternalLabels 是否跳过内部label
     * @return node id
     */
    public Long create(
            String username, Set<String> labels, Map<String, String> properties,
            boolean skipInternalLabels
    ) throws QueryExecuteException {
        String now = getFormatNowDateStringBySecond();
        properties.put("created", now);
        properties.put("updated", now);
        properties.put("author", username);
        return nodeSessionRepository.create(labels, properties, skipInternalLabels);
    }

    /**
     * 设置节点的属性并覆盖节点的labels, 跳过内部label的修改
     *
     * @param id            node id
     * @param setProperties set properties
     * @param setLabels     将要设置的标签集合
     * @throws ExceptionWithBriefJSONResponse 找不到节点, 查询异常
     */
    public void update(
            Long id, Map<String, String> setProperties, Set<String> setLabels)
            throws ExceptionWithBriefJSONResponse {
        update(id, setProperties, setLabels, true);
    }

    /**
     * 设置节点的属性并覆盖节点的labels
     *
     * @param id                 node id
     * @param setProperties      set properties
     * @param setLabels          将要设置的标签集合
     * @param skipInternalLabels 是否跳过内部label的修改
     * @throws ExceptionWithBriefJSONResponse 找不到节点, 查询异常
     */
    public void update(Long id, Map<String, String> setProperties, Set<String> setLabels,
                       boolean skipInternalLabels) throws ExceptionWithBriefJSONResponse {
        NodeData node = getNotNull(id);
        Session session = nodeSessionRepository.getSession();
        Transaction transaction = session.beginTransaction();

        setProperties.put("updated", getFormatNowDateStringBySecond());

        try {
            nodeSessionRepository.coverNodeProperties(session, node, setProperties);
            nodeSessionRepository.coverNodeLabels(session, node, setLabels,
                    node.labels, skipInternalLabels);
            transaction.commit();
        } catch (QueryExecuteException exception) {
            transaction.rollback();
            throw exception;
        } catch (Exception exception) {
            transaction.rollback();
            exception.printStackTrace();
            throw new UnknownException(exception.getMessage());
        }
    }

    /**
     * 删除指定id的节点
     *
     * @param node 节点信息体
     */
    public void delete(NodeData node) throws ExceptionWithBriefJSONResponse {
        try {
            nodeRepository.delete(node.id);
        } catch (DataIntegrityViolationException exception) {
            String msg = exception.getMessage();
            if (msg != null && msg.contains("still has relationships")) {
                throw new ExceptionWithBriefJSONResponse(
                        BriefJSONResponse.Code.DELETE_NODE_WITH_RELATIONSHIP);
            }
        }

    }
}
