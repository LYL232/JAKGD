package pers.lyl232.jakgd.repository.node;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pers.lyl232.jakgd.entity.node.User;

@Repository
public interface UserRepository extends Neo4jRepository<User, Long> {

    @Query("create (n:__user{username:{username}, password:{password}})")
    void createUserNode(@Param("username") String username, @Param("password") String password);

    @Query("match(n:__user) where n.username = {username} return n")
    User findByUsername(@Param("username") String username);
}
