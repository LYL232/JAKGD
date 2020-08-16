package pers.lyl232.jakgd.entity.relationship;

import org.neo4j.ogm.annotation.*;
import pers.lyl232.jakgd.entity.node.AbstractNode;

@RelationshipEntity
public class AbstractRelationship {
    @Id
    @GeneratedValue
    private Long id;

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
