package pers.lyl232.jakgd.repository.node;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pers.lyl232.jakgd.entity.node.AbstractNode;
import pers.lyl232.jakgd.entity.result.NodeData;
import pers.lyl232.jakgd.entity.result.RelationshipData;
import pers.lyl232.jakgd.entity.result.RelationshipWithOneNodeData;

import java.util.List;

@Repository
public interface NodeRepository extends Neo4jRepository<AbstractNode, Long> {
    @Query("match (n) where id(n) in $idList return id(n) as id, labels(n) as labels," +
            "properties(n) as properties")
    List<NodeData> getNodesById(@Param("idList") List<Long> idList);

    @Query("match (n) where id(n) = $id with n " +
            "match p=(n)-[*0..]-(m) where not '__internal' in labels(m) " +
            "unwind relationships(p) as r " +
            "with distinct r " +
            "return id(r) as id, type(r) as type, " +
            "properties(r) as properties, " +
            "id(startNode(r)) as startNode, " +
            "id(endNode(r)) as endNode limit $limit")
    List<RelationshipData> getNeighborRelationships(
            @Param("id") Long id, @Param("limit") Long limit);

    @Query("match (n) where id(n) = $id with n " +
            "match p=(n)-[*0..1]-(m) " +
            "where not '__internal' in labels(m) unwind relationships(p) as r " +
            "with distinct r " +
            "return id(r) as id, type(r) as type, " +
            "properties(r) as properties, " +
            "id(startNode(r)) as startNode, " +
            "id(endNode(r)) as endNode limit $limit")
    List<RelationshipData> getDirectNeighborRelationships(
            @Param("id") Long id, @Param("limit") Long limit);

    @Query("match (n)-[r]->() where id(n) = $id return " +
            "id(r) as id, type(r) as type, properties(r) as properties, " +
            "startNode(r) as startNode, endNode(r) as endNode")
    List<RelationshipData> getStartNodeRelationships(@Param("id") Long id);

    @Query("match (n)<-[r]-() where id(n) = $id return " +
            "id(r) as id, type(r) as type, properties(r) as properties, " +
            "startNode(r) as startNode, endNode(r) as endNode")
    List<RelationshipData> getEndNodeRelationships(@Param("id") Long id);

    @Query("match (n) where not '__internal' in labels(n)" +
            "return id(n) as id, " +
            "labels(n) as labels, " +
            "properties(n) as properties " +
            "limit $limit")
    List<NodeData> getDefaultSearchNodes(@Param("limit") Long limit);

    @Query("match(n) where id(n) = $id return count(n) > 0")
    Boolean exists(@Param("id") Long id);

    @Query("match (n)-[r]->(m) where id(n) in $idList with distinct n, r, m " +
            "return id(n) as otherNodeId, " +
            "id(r) as id, type(r) as type, properties(r) as properties, " +
            "id(m) as nodeId, labels(m) as nodeLabels, " +
            "properties(m) as nodeProperties limit $limit")
    List<RelationshipWithOneNodeData> getNodesDirectNeighbor(
            @Param("idList") List<Long> idList, @Param("limit") Long limit);

    @Query("match (n) where id(n) in $idList return count(n)")
    Long existCount(@Param("idList") Long... idList);

    @Query("match (n) where id(n) = $id return id(n) as id, " +
            "labels(n) as labels, properties(n) as properties")
    NodeData getNodeById(@Param("id") Long id);

    @Query("match (n) where id(n) in $idList return n.author")
    List<String> getNodesAuthor(@Param("idList") Long... idList);

    @Query("match (n) where id(n) = $id return n.author")
    String getNodeAuthor(@Param("id") Long id);

    @Query("match (n) where id(n) = $id delete n")
    void delete(@Param("id") Long id);

}
