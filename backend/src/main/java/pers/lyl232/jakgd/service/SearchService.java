package pers.lyl232.jakgd.service;

import com.alibaba.fastjson.JSONObject;
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
    public JSONObject getDefault(long limit) {
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
     * 在节点的属性中搜索包含指定关键字的节点，返回其数据
     *
     * @param key             关键字
     * @param queryProperties 查询的属性
     * @param skip            跳过的记录个数
     * @param limit           限制返回个数
     * @return {
     * nodes: [{id, labels: [...], properties: {key:value...}}],
     * }
     */
    @Transactional(readOnly = true)
    public JSONObject getSearchInNodePropertyResult(
            String key, Set<String> queryProperties, long skip, long limit) {
        return nodesResultWrapper(
                searchSessionRepository.searchInNodeProperties(
                        key, queryProperties, skip, limit));
    }

    /**
     * 在节点的属性中搜索包含指定关键字的节点，返回个数
     *
     * @param key             关键字
     * @param queryProperties 查询的属性
     * @return 搜索结果个数
     */
    public long getSearchInNodePropertyCount(String key, Set<String> queryProperties) {
        return searchSessionRepository.searchCountInNodeProperties(key, queryProperties);
    }

    /**
     * 在节点的label中搜索包含指定关键字的节点，返回其数据
     *
     * @param key   关键字
     * @param skip  跳过的记录个数
     * @param limit 限制返回个数
     * @return {
     * nodes: [{id, labels: [...], properties: {key:value...}}],
     * }
     */
    public JSONObject getSearchInNodeLabelResult(String key, long skip, long limit) {
        return nodesResultWrapper(searchSessionRepository.searchInNodeLabel(key, skip, limit));
    }

    /**
     * 在节点的label中搜索包含指定关键字的节点，返回其个数
     *
     * @param key 关键字
     * @return 个数
     */
    public long getSearchInNodeLabelCount(String key) {
        return searchSessionRepository.searchCountInNodeLabel(key);
    }

    /**
     * 在关系的属性中搜索包含指定关键字的关系，返回其数据，和其关联的所有节点的数据
     *
     * @param key             关键字
     * @param queryProperties 查询的属性
     * @param skip            跳过的记录个数
     * @param limit           限制返回个数
     * @return {
     * nodes: [{id, labels: [...], properties: {key:value...}}],
     * relationships: [{id, type, startNode(id), endNode(id), properties: {key:value}}]
     * }
     */
    public JSONObject getSearchInRelationshipPropertyResult(
            String key, Set<String> queryProperties, long skip, long limit) {
        List<RelationshipData> relationshipData = searchSessionRepository.searchInRelationshipProperties(
                key, queryProperties, skip, limit);
        return graphResultWrapper(getNodeByRelationships(relationshipData), relationshipData);
    }

    /**
     * 在关系的属性中搜索包含指定关键字的关系，返回其个数
     *
     * @param key             关键字
     * @param queryProperties 查询的属性
     * @return {
     * relationships: [{id, type, startNode(id), endNode(id), properties: {key:value}}]
     * }
     */
    public long getSearchInRelationshipPropertyCount(String key, Set<String> queryProperties) {
        return searchSessionRepository.searchCountInRelationshipProperties(key, queryProperties);
    }

    /**
     * 在关系的type中搜索包含指定关键字的关系，返回其数据，和其关联的所有节点的数据
     *
     * @param key   关键字
     * @param skip  跳过的记录个数
     * @param limit 限制返回个数
     * @return {
     * nodes: [{id, labels: [...], properties: {key:value...}}],
     * relationships: [{id, type, startNode(id), endNode(id), properties: {key:value}}]
     * }
     */
    public JSONObject getSearchInRelationshipTypeResult(String key, long skip, long limit) {
        List<RelationshipData> relationshipData = searchSessionRepository.searchInRelationshipType(key, skip, limit);
        return graphResultWrapper(getNodeByRelationships(relationshipData), relationshipData);
    }

    /**
     * 在关系的type中搜索包含指定关键字的关系，返回其个数
     *
     * @param key 关键字
     * @return 符合条件的记录个数
     */
    public long getSearchInRelationshipTypeCount(String key) {
        return searchSessionRepository.searchCountInRelationshipType(key);
    }

    private JSONObject graphResultWrapper(
            List<NodeData> nodeData, List<RelationshipData> relationshipData) {
        JSONObject res = new JSONObject();
        res.put("nodes", nodeData);
        res.put("relationships", relationshipData);
        return res;
    }

    private JSONObject nodesResultWrapper(List<NodeData> nodeData) {
        JSONObject res = new JSONObject();
        res.put("nodes", nodeData);
        return res;
    }

    private List<NodeData> getNodeByRelationships(List<RelationshipData> relationships) {
        Set<Long> nodeIdSet = new HashSet<>();
        for (RelationshipData each : relationships) {
            nodeIdSet.add(each.startNode);
            nodeIdSet.add(each.endNode);
        }
        return searchSessionRepository.getNodesById(new ArrayList<>(nodeIdSet));
    }
}
