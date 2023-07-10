package repository;

import exception.RepositoryException;
import model.Move;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.query.Query;
import java.util.*;

public class MoveDBRepository implements IMoveRepository {

    private static final Logger logger= LogManager.getLogger(MoveDBRepository.class.getName());
    private Session session;


    public MoveDBRepository() {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder().configure().build();
        SessionFactory factory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
        session = factory.openSession();
    }

    @Override
    public Move add(Move entity) {
        logger.traceEntry();
        Transaction transaction = session.beginTransaction();
        Long id = (Long) session.save(entity);
        entity.setId(id);
        transaction.commit();
        logger.traceExit();
        return entity;
    }

    @Override
    public void delete(Move entity) {
        logger.traceEntry();
        Transaction transaction = session.beginTransaction();
        session.delete(entity);
        transaction.commit();
        logger.traceExit();
    }

    @Override
    public void update(Move entity) {
        logger.traceEntry();
        Transaction transaction = session.beginTransaction();
        session.update(entity);
        transaction.commit();
        logger.traceExit();
    }

    @Override
    public Move findById(Long id) throws RepositoryException {
        logger.traceEntry();
        Transaction transaction = session.beginTransaction();
        Move entity = session.get(Move.class, id);
        transaction.commit();
        logger.traceExit();
        return entity;
    }

    @Override
    public Iterable<Move> getAll() {
        logger.traceEntry();
        Query query = session.createQuery("from Move");
        List<Move> entities = query.list();
        logger.traceExit();
        return entities;
    }


}