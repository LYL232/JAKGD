package pers.lyl232.jakgd.repository.relationship;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pers.lyl232.jakgd.entity.relationship.ReferenceRelationship;

@Repository
public interface ReferRelationshipRepository extends
        Neo4jRepository<ReferenceRelationship, Long> {
    @Query("match (n) where id(n) = $startNode with n " +
            "match (m) where id(m) = $endNode " +
            "create (n)-[:__refer{created: $now, updated: $now}]->(m)")
    void create(@Param("startNode") Long startNode, @Param("endNode") Long endNode,
                @Param("now") String now);

    @Query("match (n)-[:__refer*0..]->(m) where id(n) = $id " +
            "and id(m) = $queryId return count(m)")
    Integer queryIfHasReferred(@Param("id") Long id, @Param("queryId") Long queryId);

    /**
     * 即将添加某个引用关系时, 检查引用者所有直接引用的节点, 如果将要引用的节点对这些节点具有可达性，
     * 那么需要删除这些节点
     *
     * @param id      referrer id
     * @param referId reference id
     */
    @Query("match (n:__knowledge)-[r:__refer]->(o:__knowledge) " +
            "where id(n) = $id with n, r, o " +
            "match (m:__knowledge)-[:__refer*1..]->(o:__knowledge) " +
            "where id(m) = $referId " +
            "delete r")
    void deleteDuplicateReferRelationships(@Param("id") Long id, @Param("referId") Long referId);
}
