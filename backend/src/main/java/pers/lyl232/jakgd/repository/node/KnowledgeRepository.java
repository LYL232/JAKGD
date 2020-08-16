package pers.lyl232.jakgd.repository.node;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pers.lyl232.jakgd.entity.node.Knowledge;

@Repository
public interface KnowledgeRepository extends Neo4jRepository<Knowledge, Long> {
    @Query("match (n:__knowledge) where id(n) = $id return n")
    Knowledge get(@Param("id") Long id);
}
