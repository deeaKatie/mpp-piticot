package repository;
import exception.RepositoryException;
import model.PlayerMoves;
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

public class PlayerMovesDBRepository implements IPlayerMovesRepository {

    private static final Logger logger= LogManager.getLogger(PlayerMovesDBRepository.class.getName());
    private Session session;

    public PlayerMovesDBRepository() {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder().configure().build();
        SessionFactory factory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
        session = factory.openSession();
    }

    @Override
    public PlayerMoves add(PlayerMoves entity) {
        logger.traceEntry();
        Transaction transaction = session.beginTransaction();
        Long id = (Long) session.save(entity);
        entity.setId(id);
        transaction.commit();
        logger.traceExit();
        return entity;
    }

    @Override
    public void delete(PlayerMoves entity) {
        logger.traceEntry();
        Transaction transaction = session.beginTransaction();
        session.delete(entity);
        transaction.commit();
        logger.traceExit();
    }

    @Override
    public void update(PlayerMoves entity) {
        logger.traceEntry();
        Transaction transaction = session.beginTransaction();
        session.update(entity);
        transaction.commit();
        logger.traceExit();
    }

    @Override
    public PlayerMoves findById(Long id) throws RepositoryException {
        logger.traceEntry();
        Transaction transaction = session.beginTransaction();
        PlayerMoves entity = session.get(PlayerMoves.class, id);
        transaction.commit();
        logger.traceExit();
        return entity;
    }

    @Override
    public Iterable<PlayerMoves> getAll() {
        logger.traceEntry();
        Query query = session.createQuery("from PlayerMoves");
        List<PlayerMoves> entities = query.list();
        logger.traceExit();
        return entities;
    }
    
}
