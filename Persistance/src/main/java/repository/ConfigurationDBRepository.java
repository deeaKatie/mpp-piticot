package repository;
import exception.RepositoryException;
import model.Configuration;
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

public class ConfigurationDBRepository implements IConfigurationRepository {
    private static final Logger logger= LogManager.getLogger(ConfigurationDBRepository.class.getName());
    private Session session;


    public ConfigurationDBRepository() {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder().configure().build();
        SessionFactory factory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
        session = factory.openSession();
    }

    @Override
    public Configuration add(Configuration entity) {
        logger.traceEntry();
        Transaction transaction = session.beginTransaction();
        Long id = (Long) session.save(entity);
        entity.setId(id);
        transaction.commit();
        logger.traceExit();
        return entity;
    }

    @Override
    public void delete(Configuration entity) {
        logger.traceEntry();
        Transaction transaction = session.beginTransaction();
        session.delete(entity);
        transaction.commit();
        logger.traceExit();
    }

    @Override
    public void update(Configuration entity) {
        logger.traceEntry();
        Transaction transaction = session.beginTransaction();
        session.update(entity);
        transaction.commit();
        logger.traceExit();
    }

    @Override
    public Configuration findById(Long id) throws RepositoryException {
        logger.traceEntry();
        Transaction transaction = session.beginTransaction();
        Configuration entity = session.get(Configuration.class, id);
        transaction.commit();
        logger.traceExit();
        return entity;
    }

    @Override
    public Iterable<Configuration> getAll() {
        logger.traceEntry();
        Query query = session.createQuery("from Configuration");
        List<Configuration> entities = query.list();
        logger.traceExit();
        return entities;
    }
}
