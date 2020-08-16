package pers.lyl232.jakgd.service;

import com.alibaba.fastjson.JSONObject;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.transaction.Transaction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pers.lyl232.jakgd.entity.result.NodeData;
import pers.lyl232.jakgd.entity.result.RelationshipData;
import pers.lyl232.jakgd.entity.result.RelationshipWithOneNodeData;
import pers.lyl232.jakgd.repository.SearchSessionRepository;
import pers.lyl232.jakgd.repository.node.NodeRepository;

import java.util.*;

@Service
public class SearchService {
    final private NodeRepository nodeRepository;

    final private SearchSessionRepository searchSessionRepository;

    public SearchService(NodeRepository nodeRepository,
                         SearchSessionRepository searchSessionRepository) {
        this.searchSessionRepository = searchSessionRepository;
        this.nodeRepository = nodeRepository;
    }

    /**
     * 搜索空字符串的结果
     *
     * @param limit 限制返回个数
     * @return JSONObject {
     * nodes: [{id, labels: [...], properties: {key:value...}}],
     * relationships: [{id, type, startNode(id),
     * endNode(id), properties: {key:value}}]
     * }
     * 保证nodes中有relationships中涉及到的所有node
     */
    @Transactional(readOnly = true)
    public JSONObject getDefault(int limit) {
        List<NodeData> nodes = nodeRepository.getDefaultSearchNodes(limit);
        List<Long> nodeIdList = new LinkedList<>();
        Set<Long> nodeIdSet = new HashSet<>();
        for (NodeData node : nodes) {
            nodeIdList.add(node.id);
            nodeIdSet.add(node.id);
        }
        List<RelationshipWithOneNodeData> relWithOtherNodes =
                nodeRepository.getNodesDirectNeighbor(nodeIdList, limit);
        List<RelationshipData> relationships = new LinkedList<>();
        for (RelationshipWithOneNodeData each : relWithOtherNodes) {
            if (!nodeIdSet.contains(each.nodeId)) {
                nodeIdSet.add(each.nodeId);
                nodes.add(new NodeData(each.nodeId, each.nodeLabels, each.nodeProperties));
            }
            relationships.add(new RelationshipData(each.id, each.type, each.properties,
                    each.otherNodeId, each.nodeId));
        }
        return graphResultWrapper(nodes, relationships);
    }

    /**
     * 在节点的label, 关系的类型, 和属性中搜索指定关键字
     *
     * @param key             关键字
     * @param queryProperties 查询的语句
     * @param limit           限制返回个数
     * @return {
     * nodes: [{id, labels: [...], properties: {key:value...}}],
     * relationships: [{id, type, startNode(id),
     * endNode(id), properties: {key:value}}]
     * }
     * 保证nodes中有relationships中涉及到的所有node
     * TODO: 并行化查询？
     */
    public JSONObject getSearchResult(
            String key, Set<String> queryProperties, int limit) {
        if (key.isEmpty()) {
            return getDefault(limit);
        }
        Session session = searchSessionRepository.getSession();
        Transaction transaction = session.beginTransaction(Transaction.Type.READ_ONLY);
        List<NodeData> nodes = new LinkedList<>(), nodeData;
        List<RelationshipData> relationships = new LinkedList<>(), relationshipData;
        Set<Long> nodeIdSet = new HashSet<>(), relationshipsIdSet = new HashSet<>(),
                nodesIdToQuery = new HashSet<>();
        try {
            // 如果key是纯数字, 则优先查询id
            int id = Integer.parseInt(key);
            nodeData = searchSessionRepository.getNodesById(session,
                    Collections.singletonList((long) id));
            for (NodeData node : nodeData) {
                nodeIdSet.add(node.id);
                nodes.add(node);
            }
        } catch (NumberFormatException ignored) {
        }
        nodeData = searchSessionRepository.searchInNodeProperties(
                session, key, queryProperties, limit);
        if (appendUntilLimit(nodeData, nodeIdSet, nodes, limit) >= limit) {
            transaction.commit();
            return graphResultWrapper(nodes, relationships);
        }
        nodeData = searchSessionRepository.searchInNodeLabel(session, key, limit);
        if (appendUntilLimit(nodeData, nodeIdSet, nodes, limit) >= limit) {
            transaction.commit();
            return graphResultWrapper(nodes, relationships);
        }
        relationshipData = searchSessionRepository.searchInRelationshipProperties(
                session, key, queryProperties, limit
        );
        if (appendUntilLimit(relationshipData, relationshipsIdSet,
                relationships, nodeIdSet, nodesIdToQuery, limit) >= limit) {
            nodes.addAll(searchSessionRepository.getNodesById(
                    session, new LinkedList<>(nodesIdToQuery)));
            transaction.commit();
            return graphResultWrapper(nodes, relationships);
        }
        relationshipData = searchSessionRepository.searchInRelationshipType(
                session, key, limit);
        appendUntilLimit(relationshipData, relationshipsIdSet,
                relationships, nodeIdSet, nodesIdToQuery, limit);
        nodes.addAll(searchSessionRepository.getNodesById(
                session, new LinkedList<>(nodesIdToQuery)));
        transaction.commit();
        return graphResultWrapper(nodes, relationships);
    }

    private JSONObject graphResultWrapper(
            List<NodeData> nodeData, List<RelationshipData> relationshipData) {
        JSONObject res = new JSONObject();
        res.put("nodes", nodeData);
        res.put("relationships", relationshipData);
        return res;
    }

    /**
     * 追加节点数据直至集合达到限制或者新数据追加完毕
     *
     * @param newData   新数据
     * @param nodeIdSet 节点id集合
     * @param nodes     节点数据集合
     * @param limit     限制
     * @return 节点集合的大小
     */
    private int appendUntilLimit(
            List<NodeData> newData, Set<Long> nodeIdSet,
            List<NodeData> nodes, int limit) {
        for (NodeData node : newData) {
            if (!nodeIdSet.contains(node.id)) {
                nodes.add(node);
                nodeIdSet.add(node.id);
            }
            if (nodeIdSet.size() >= limit) {
                return nodeIdSet.size();
            }
        }
        return nodeIdSet.size();
    }

    /**
     * 追加关系数据直至集合达到限制或者新数据追加完毕
     *
     * @param newData            新数据
     * @param relationshipsIdSet 等待查询节点id集合
     * @param relationships      关系数据列表
     * @param nodeIdSet          已有数据节点id集合
     * @param nodesIdToQuery     未有数据节点id集合
     * @param limit              限制
     * @return nodeIdSet 与 relationshipsIdSet 大小的最大值
     */
    private int appendUntilLimit(
            List<RelationshipData> newData,
            Set<Long> relationshipsIdSet,
            List<RelationshipData> relationships,
            Set<Long> nodeIdSet,
            Set<Long> nodesIdToQuery, int limit) {
        for (RelationshipData relationship : newData) {
            if (!relationshipsIdSet.contains(relationship.id)) {
                relationships.add(relationship);
                relationshipsIdSet.add(relationship.id);
                if (!nodeIdSet.contains(relationship.startNode)) {
                    nodesIdToQuery.add(relationship.startNode);
                    nodeIdSet.add(relationship.startNode);
                }
                if (!nodeIdSet.contains(relationship.endNode)) {
                    nodesIdToQuery.add(relationship.endNode);
                    nodeIdSet.add(relationship.endNode);
                }
            }
            if (nodeIdSet.size() >= limit || relationshipsIdSet.size() >= limit) {
                return Math.max(nodeIdSet.size(), relationshipsIdSet.size());
            }
        }
        return Math.max(nodeIdSet.size(), relationshipsIdSet.size());
    }
}
