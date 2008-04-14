package edu.sdsc.nbcr.opal.state;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import org.apache.log4j.Logger;

/**
 *
 * Utility class for hibernate functions
 *
 * @author Sriram Krishnan
 */

public class HibernateUtil {

    // get an instance of the log4j Logger
    private static Logger logger = 
	Logger.getLogger(HibernateUtil.class.getName());
    
    private static final SessionFactory sessionFactory;

    static {
        try {
            // Create the SessionFactory from hibernate.cfg.xml
            sessionFactory = 
		new Configuration().configure("hibernate-opal.cfg.xml").buildSessionFactory();
        } catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            logger.error("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Return a hibernate session factory loaded from the configuration
     */
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}