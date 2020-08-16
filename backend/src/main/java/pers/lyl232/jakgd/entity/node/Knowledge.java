package pers.lyl232.jakgd.entity.node;

import org.neo4j.ogm.annotation.*;

import java.util.Set;

@NodeEntity(label = "__knowledge")
public class Knowledge {
    @Id
    @GeneratedValue
    private Long id;

    @Property
    private String author;

    @Property
    private String name;

    @Labels
    private Set<String> labels;

    @Property
    private String summary;

    @Property
    private String created;

    @Property
    private String updated;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<String> getLabels() {
        return labels;
    }

    public void setLabels(Set<String> labels) {
        this.labels = labels;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

}
