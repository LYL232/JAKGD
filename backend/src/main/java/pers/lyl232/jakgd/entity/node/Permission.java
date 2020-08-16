package pers.lyl232.jakgd.entity.node;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

@NodeEntity(label = "__permission")
public class Permission {

    public Permission() {
    }

    public Permission(String name) {
        this.name = name;
    }

    final static public Permission CREATE_NODE = new Permission("创建节点");
    final static public Permission VIEW_PUBLIC_NODE = new Permission("查询公开节点");
    final static public Permission DELETE_OWN_NODE = new Permission("删除自己所作节点");
    final static public Permission MODIFY_OWN_NODE = new Permission("修改自己所作节点");
    final static public Permission MODIFY_ALL_NODE = new Permission("修改所有节点");
    final static public Permission DELETE_ALL_NODE = new Permission("删除所有节点");

    @Id
    @GeneratedValue
    private Long id;

    @Property
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Permission) {
            return name.equals(((Permission) obj).getName());
        }
        return false;
    }
}
