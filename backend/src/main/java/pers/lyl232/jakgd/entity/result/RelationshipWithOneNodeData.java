package pers.lyl232.jakgd.entity.result;

import org.springframework.data.neo4j.annotation.QueryResult;

import java.util.Map;
import java.util.Set;

@QueryResult
public class RelationshipWithOneNodeData {
    public Long id;
    public String type;
    public Map<String, String> properties;
    public Long otherNodeId;
    public Long nodeId;
    public Set<String> nodeLabels;
    public Map<String, String> nodeProperties;
}
