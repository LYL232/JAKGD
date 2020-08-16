package pers.lyl232.jakgd.repository.node;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pers.lyl232.jakgd.entity.node.Permission;

import java.util.Set;

@Repository
public interface PermissionRepository extends Neo4jRepository<Permission, Long> {
    @Query("match (n:__permission) where n.name = $name return n")
    Permission get(@Param("name") String name);

    @Query("match (user:__user) where user.username = $username with user " +
            "match (n)-[:__is*]->(m:__role) with distinct m " +
            "match (m)-[:__has]->(p:__permission) " +
            "return p")
    Set<Permission> getUserPermissions(@Param("username") String username);

    @Query("match (m:__role)-[:__has]->(p:__permission) where p.name = $permission " +
            "with m match (n:__user)-[:__is*]->(m) " +
            "where n.username = $username " +
            "return count(n) > 0")
    Boolean userHasPermission(@Param("username") String username,
                              @Param("permission") String permission);
}
