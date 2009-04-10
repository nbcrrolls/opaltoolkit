

package edu.sdsc.nbcr.opal.dashboard.persistence;


import org.hibernate.classic.Session;
import org.hibernate.SQLQuery;
import org.hibernate.Query;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.hibernate.impl.SessionFactoryImpl;
import java.util.List;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.gram.GramJob;
import org.postgresql.util.PGInterval;

import edu.sdsc.nbcr.opal.dashboard.util.DateHelper;

/**
 * This class is used to manage the connection with the persistency layer.
 * 
 * The following attributes are part of this class:
 * <ul>
 * <li>databaseUrl is the url used to connect to the DB
 * <li>dirver is the name of the java driver to be used to connect to the db
 * <li>dbUserName the user name to use to connect to the db
 * <li>dpPassword the password to use for connecting to the db
 * </ul>
 * 
 * @author clem
 *
 */
public class DBManager {
    

    protected static Log log = LogFactory.getLog(DBManager.class.getName());

    //new hiberante stuff
    //private Session session = null; this should not be here!!
    private String error = null;
    private String driver = null;
    private String dialect = null;
    private SessionFactory sessionFactory = null;
    
    /**
     * 
     * @return the database URL
     */
//    public String getDatabaseUrl() {
//        String driver = null;
//        try {
//            driver = session.connection().getMetaData().getURL();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return driver;
//    }

    
    /**
     * default constructor
     */
    public DBManager(){
        sessionFactory = edu.sdsc.nbcr.opal.state.HibernateUtil.getSessionFactory();
        driver = null;
        try {
            //maybe not the best way to find the driver name, but I couldn't
            //find a better one
            Session session = sessionFactory.openSession();
            driver = session.connection().getMetaData().getDriverName();
            session.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //getting the dialect
        SessionFactoryImpl factoryimpl = (SessionFactoryImpl) sessionFactory;
        dialect = factoryimpl.getDialect().getClass().getName();
    }

    /**
     * 
     * @return the string representing the driver
     */
    public String getDriver() {
        return driver;
    }


    /**
     * 
     * @return the string representing the driver
     */
    public String getDialect() {
        return dialect;
    }


    /**
     * This function has to be called to initialized the connection to the DB
     * @return true if everything went OK
     */
    public boolean init() {
        return true ;
    }
    
    /**
     * Return true if the connection with the DB is valid
     * @return true if the connection to the DB is valid
     */
    public boolean isConnected(){
        return true;
    }


    /**
     * Returns the list of services available on the Opal server.
     * 
     * The service has to be called at least once in order to be retrieved 
     * by the client.
     * 
     * @return an Array of strings containing the list of services
     * 
     */
    public String [] getServicesList(){
        Session session = sessionFactory.openSession();
        List serviceList = session.createQuery(
            "select serviceName from JobInfo group by serviceName ").list();
        session.close();
        Iterator itera = serviceList.iterator();
        return (String []) serviceList.toArray(new String[serviceList.size()]);
    }
    

    /**
     * Legacy function it is here only for backward compatibility, it uses the getResultsTimeseries
     * 
     * @see #getResultsTimeseries(Date, Date, String, String)
     * 
     */
    public double [] getHits(Date startDate, Date endDate, String service){
        
        return getResultsTimeseries(startDate, endDate, service, "hits");
    }
    
    /**
     * Legacy function it is here only for backward compatibility, it uses the getResultsTimeseries
     * 
     * @see #getResultsTimeseries(Date, Date, String, String)
     * 
     */
    public double [] getError(Date startDate, Date endDate, String service){
        return getResultsTimeseries(startDate, endDate, service, "error");
    }
    
    /**
     * Legacy function it is here only for backward compatibility, it uses the getResultsTimeseries
     * 
     * @see #getResultsTimeseries(Date, Date, String, String)
     * 
     */
    public double [] getExectime(Date startDate, Date endDate, String service){
        return getResultsTimeseries(startDate, endDate, service, "exectime");
    }//getExectime
    

    
    /**
     * This is a generic functions which make a query and return an array of double containing a value 
     * for every day of the query. The value of the returned array depends on the type parameters. 
     * The return array will have the size equal to numberOfDay(endDate - startDate)
     * 
     * @param startDate the beginning of the time series
     * @param endDate the end of the time series
     * @param service the service you want to get the data from
     * @param type this can be:
     * <ul>
     * <li>hits: the number daily of hits received for the service</li>
     * <li>exectime: the daily average execution time</li>
     * <li>error: the daily number of failed job</li>
     * </ul>  
     * 
     * @return an array of values 
     */
    public double [] getResultsTimeseries(Date startDate, Date endDate, String service, String type){

        Session session = sessionFactory.openSession();
        //creating the query
        int numberOfDays = DateHelper.getOffsetDays(endDate, startDate);
        java.sql.Date  endDateSQL = new java.sql.Date(endDate.getTime());
        java.sql.Date startDateSQL = new java.sql.Date(startDate.getTime());

        //query will hold the value of the query that will be run against the
        //DB, depending on the type value query chages
        Query queryStat = null; 
        String query = null;
        if ( type.equals("hits") ) {
            query = "select jobInfo.startTimeDate, count(*)  " +            
                " from JobInfo jobInfo where jobInfo.serviceName = :service" +
                " and jobInfo.startTimeDate >= :startDate " +
                " and jobInfo.startTimeDate <= :endDate " +
                " and jobInfo.code=8 " +
                " group by jobInfo.startTimeDate " +
                " order by jobInfo.startTimeDate desc"; 
            queryStat = session.createQuery(query);
        }else if (type.equals("exectime") ) {
            query = getQueryExectime();
            if ( query != null ) {
                SQLQuery sqlQuery = session.createSQLQuery(query);
                sqlQuery.addScalar("date", Hibernate.DATE);
                sqlQuery.addScalar("average", Hibernate.DOUBLE);
                queryStat = sqlQuery;
            }else {
                //the database in use is not supported
                queryStat = session.getNamedQuery("exectime"); 
            }

        } else if (type.equals("error") ){
            query  = "select jobInfo.startTimeDate, count(*) " +
                "from JobInfo jobInfo " +
            	"where jobInfo.serviceName = :service " +
            	"and jobInfo.startTimeDate >= :startDate " +
            	"and jobInfo.startTimeDate <= :endDate " +
            	"and jobInfo.code=4 " +
            	"group by jobInfo.startTimeDate " +
            	"order by jobInfo.startTimeDate desc ";   
            queryStat = session.createQuery(query);
        }
        
        
        //going to execute the query
        try {
            queryStat.setString("service", service)
                .setDate("startDate", startDateSQL)
                .setDate("endDate", endDateSQL);
            List result = queryStat.list();
            Iterator itera = result.iterator();
            log.info("Going to get the " + type + " for the service: " + service + 
                    "\nRunning the following query: " + queryStat.getQueryString());
            //while (itera.hasNext()){
            //    Object [] entry = (Object []) itera.next();
            //    log.info("the rsults are: " + (String) entry[0] + ",  " + (Integer)entry[1] );
            //}
            //itera = result.iterator(); 
            double [] values = new double[numberOfDays+1];
            int counter = numberOfDays;
            //we are gonna start from the end date going backward
            //currentDate represent the date pointed by values[counter]
            //Date currentDate =  DateHelper.subtractDay(endDate);
            Date currentDate = endDate;
            //now we have put the data from the result of the query into the return array
            while( itera.hasNext() ) {
                Object [] entry = (Object []) itera.next();
                java.sql.Date date = (java.sql.Date) entry[0];
                //log.debug("For the date " + date + " we have n entries: " + ((Integer)entry[1]) );
                
                while ( ! DateHelper.compareDates(currentDate, date) ){
                    //since some day can have no hits we have to put zero in the array for those days
                    if ( counter == -1 ) {
                        break;
                    }
                    values[counter] = 0;
                    log.trace("Inserting a zero for date: " + currentDate + " on position: " + counter);
                    counter--;
                    currentDate = DateHelper.subtractDay(currentDate);
                }//if
                if ( counter == -1 ) {
                    break;
                }
                //we don't have a gap!
                if (type.equals("hits") ) {
                    values[counter] = ((Long)entry[1]).doubleValue(); 
                }else if ( type.equals("exectime") ) {
                    values[counter] = ((Double)entry[1]).doubleValue();
                }else if ( type.equals("error") ) {
                    values[counter] = (double) ((Long)entry[1]).doubleValue(); 
                }
                log.trace("Inserting the value " + values[counter] + " for date: " + date + " on position: " + counter);
                //decrease the counter
                counter--;
                //and decrease the date
                currentDate = DateHelper.subtractDay(currentDate);
                
            }//while
            //everything went fine, let's log and return 
            String str = new String();
            for ( counter = 0; counter < values.length; counter++)
                str += values[counter] + ", ";
            log.info("The query on " + type + " with service " + service + " is returning values: " + str); 
            session.close();
            return values;
        }catch (Exception e ) {
            log.error("Error while querying for the " + type + " with service " + service + " : " + e.getMessage(), e);
            if ( session.isConnected() ) {
                session.close();
            }
            return null;
        }
        //return null;
    }
    
    
    /**
     * this function returns the number of running jobs for the specified service
     * 
     * @param service the service name
     * @return the number of running job of the service 
     */
    public int getRunningJobs(String service){
        //a job is running if its status is 
        //STATUS_PENDING STATUS_ACTIVE STATUS_STAGE_IN STATUS_STAGE_OUT
        //1 2 64 128
        /*
        int number = -1;
        String query = " select count(job_id) from job_status where " +
        		"(code=" + GramJob.STATUS_PENDING + " or code=" + GramJob.STATUS_ACTIVE + " or " +
        		"code=" + GramJob.STATUS_STAGE_IN + " or code=" + GramJob.STATUS_STAGE_OUT + ") and " +
        		"service_name='" + service + "' ;";
        if ( ! isConnected() ) return -1;
        try {
            Statement sql = conn.createStatement();
            ResultSet rs = sql.executeQuery(query);
            
            if ( rs.next() ){ //something wrong happen
                number = rs.getInt("count");
            }
        }catch ( Exception e) {
            log.error("Nasty error happen while query the Data Base: " + e.getMessage(), e);
            return -1;
        }*/
        
        String query = " select count(jobID) from JobInfo where " +
            "(code=" + GramJob.STATUS_PENDING + " or code=" + GramJob.STATUS_ACTIVE + " or " +
            "code=" + GramJob.STATUS_STAGE_IN + " or code=" + GramJob.STATUS_STAGE_OUT + ") and " +
            "serviceName='" + service + "' ";

        Session session = sessionFactory.openSession();
        Long ret = (Long) session.createQuery(query).uniqueResult();
        session.close();
        log.debug("getRunningJobs for service: " + service + " returning:" + ret); 
        return ret.intValue();
    }

    /**
     * this function return the proper query based on the Datebase in use
     * it looks at the current dialect
     *
     * it returns null if the current dialect is not supported 
     *
     */
    private String getQueryExectime(){
        String query = null;
        String queryTail = " from job_info jobInfo " +
                " where jobInfo.service_name = :service " +
                " and jobInfo.start_time_date >= :startDate " +
                " and jobInfo.start_time_date <= :endDate " +
                " and jobInfo.code=8 " +
                " group by jobInfo.start_time_date " + 
                " order by jobInfo.start_time_date desc" ;
        //getting the dialect
        String dialect = getDialect();
        log.info("The dialect in use is: " + dialect);
        if (dialect.equals("org.hibernate.dialect.HSQLDialect")){
            //this is HSQL
            query = "select jobInfo.start_time_date as date, " +
                " avg( datediff('ss', jobInfo.start_time_date, jobInfo.last_update_date) + " +
                " datediff('ss', jobInfo.start_time_time, jobInfo.last_update_time) ) as average " + queryTail; 
        }else if (dialect.equals("org.hibernate.dialect.PostgreSQLDialect")){
            //this is postgress
            query = "select jobInfo.start_time_date as date, " +
                //number of day 
                " avg( ( last_update_date - start_time_date ) * 86400 + " +
                //plus number of seconds (epoch returns seconds!)
                " extract(epoch from ( last_update_time - start_time_time))) as average " + queryTail; 
        }
        return query;
    }

}
