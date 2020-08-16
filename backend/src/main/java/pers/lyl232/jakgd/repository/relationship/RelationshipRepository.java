package pers.lyl232.jakgd.repository.relationship;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pers.lyl232.jakgd.entity.relationship.AbstractRelationship;
import pers.lyl232.jakgd.entity.result.RelationshipData;

import java.util.List;

@Repository
public interface RelationshipRepository extends Neo4jRepository<AbstractRelationship, Long> {
    @Query("match (n) where id(n) = $nId with n " +
            "match (n)-[r]->(m) where id(m) = $mId return " +
            "id(r) as id, type(r) as type, properties(r) as properties, " +
            "startNode(r) as startNode, endNode(r) as endNode")
    List<RelationshipData> getDirectionalRelationshipsByEndNodes(
            @Param("nId") Long nId, @Param("mId") Long mId
    );

    @Query("match ()-[r]-() where id(r) = $id delete r")
    void delete(@Param("id") Long id);

    @Query("match ()-[r]->() where id(r) = $id return id(r) as id, " +
            "type(r) as type, id(startNode(r)) as startNode, id(endNode(r)) as endNode, " +
            "properties(r) as properties")
    RelationshipData get(@Param("id") Long id);
}
