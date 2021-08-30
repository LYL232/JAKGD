package pers.lyl232.jakgd.repository.node;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pers.lyl232.jakgd.entity.node.Document;

import java.util.List;

@Repository
public interface DocumentRepository extends Neo4jRepository<Document, Long> {
    @Query("match(n:__document) where id(n) = $id return n")
    Document get(@Param("id") Long id);

    @Query("match (n)<-[r:__part_of]-(m:__document)" +
            "where id(n) = $id " +
            "return m as document order by r.index")
    List<Document> getNodeDocuments(@Param("id") Long id);

    @Query("match (n) where id(n) = $nodeId " +
            "create (m:__document:__internal{type: $type, name: $name, content: $content, " +
            "created: $created, updated: $updated, author: $author})" +
            "-[r:__part_of{index: $index}]->(n) return id(m)")
    Long createPartDocument(
            @Param("nodeId") Long nodeId, @Param("type") String type,
            @Param("name") String name, @Param("content") String content,
            @Param("created") String created, @Param("updated") String updated,
            @Param("author") String author, @Param("index") Integer index);

    @Query("match (n:__document)-[r:__part_of]->(m) " +
            "where id(n) = $dId and r.index > 0 and id(m) = $nodeId " +
            "with r as nr " +
            "match (n:__document)-[r:__part_of]->(m) " +
            "where id(n) <> $dId and id(m) = $nodeId and r.index = nr.index - 1 " +
            "set r.index = r.index + 1, nr.index = nr.index - 1")
    void switchDocumentUpward(@Param("nodeId") Long nodeId, @Param("dId") Long dId);

    @Query("match (n:__document)-[r:__part_of]->(m) " +
            "where id(m) = $nodeId " +
            "with count(r) as tot " +
            "match (n:__document)-[r:__part_of]->(m) " +
            "where id(n) = $dId and r.index < tot - 1 and id(m) = $nodeId " +
            "with r as nr " +
            "match (n:__document)-[r:__part_of]->(m) " +
            "where id(n) <> $dId and r.index = nr.index + 1 and id(m) = $nodeId " +
            "set r.index = r.index - 1, nr.index = nr.index + 1")
    void switchDocumentDownward(@Param("nodeId") Long nodeId, @Param("dId") Long dId);

    @Query("match (:__document)-[r:__part_of]->(n) where id(n) = $id return count(r)")
    Integer getPartDocumentsCount(@Param("id") Long id);

    @Query("match(n:__document)-[r:__part_of]->(m) " +
            "where id(m) = $kId and r.index > $index " +
            "set r.index = r.index - 1")
    void correctPartOfIndex(@Param("kId") Long kId, @Param("index") Integer index);

    @Query("match (n)<-[r:__part_of]-(m:__document) where id(n) = $nodeId and " +
            "id(m) = $dId return r.index")
    Integer getPartOfIndex(@Param("nodeId") Long nodeId, @Param("dId") Long dId);

    @Query("match (n:__document)-[r:__part_of]-(m) where id(n) = $dId " +
            "return id(m)")
    Long getPartOfNodeId(@Param("dId") Long dId);

    @Query("match (n:__document) where n.author = $author and n.content contains($key) return count(n)")
    Long getUserDocumentContain(@Param("author") String author, @Param("key")String key);

}
