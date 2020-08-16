package pers.lyl232.jakgd.entity.relationship;

import org.neo4j.ogm.annotation.*;
import pers.lyl232.jakgd.entity.node.AbstractNode;

@RelationshipEntity(type = "__refer")
public class ReferenceRelationship {

    @Id
    @GeneratedValue
    private Long id;

    @Property
    private String description;

    @StartNode
    private AbstractNode startNode;

    @EndNode
    private AbstractNode endNode;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AbstractNode getStartNode() {
        return startNode;
    }

    public void setStartNode(AbstractNode startNode) {
        this.startNode = startNode;
    }

    public AbstractNode getEndNode() {
        return endNode;
    }

    public void setEndNode(AbstractNode endNode) {
        this.endNode = endNode;
    }
}
