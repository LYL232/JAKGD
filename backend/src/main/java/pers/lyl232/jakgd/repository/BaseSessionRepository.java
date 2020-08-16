package pers.lyl232.jakgd.repository;

import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.stereotype.Repository;

@Repository
public class BaseSessionRepository {
    final protected SessionFactory sessionFactory;

    public BaseSessionRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Session getSession() {
        return sessionFactory.openSession();
    }
}
