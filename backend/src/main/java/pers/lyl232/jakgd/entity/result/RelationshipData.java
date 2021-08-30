package pers.lyl232.jakgd.entity.result;

import org.springframework.data.neo4j.annotation.QueryResult;

import java.util.Map;

@QueryResult
public class RelationshipData {
    public Long id;
    public String type;
    public Map<String, Object> properties;
    public Long startNode;
    public Long endNode;

    @SuppressWarnings("unused")
    public RelationshipData() {
    }

    public RelationshipData(
            Long id, String type, Map<String, Object> properties,
            Long startNode, Long endNode) {
        this.id = id;
        this.type = type;
        this.properties = properties;
        this.startNode = startNode;
        this.endNode = endNode;
    }
}
