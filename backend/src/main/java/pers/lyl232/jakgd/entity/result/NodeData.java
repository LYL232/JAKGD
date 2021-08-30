package pers.lyl232.jakgd.entity.result;

import org.springframework.data.neo4j.annotation.QueryResult;

import java.util.Map;
import java.util.Set;

@QueryResult
public class NodeData {
    public Long id;
    public Set<String> labels;
    public Map<String, Object> properties;

    public NodeData() {
    }

    public NodeData(Long id, Set<String> labels, Map<String, Object> properties) {
        this.id = id;
        this.labels = labels;
        this.properties = properties;
    }
}
