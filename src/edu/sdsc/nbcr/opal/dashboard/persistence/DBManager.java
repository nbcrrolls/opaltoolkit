

package edu.sdsc.nbcr.opal.dashboard.persistence;


import org.hibernate.classic.Session;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
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
    private Session session = null;


    /**

     */
    //private Connection conn = null;
    //private String databaseUrl = null;
    //private String driver = null;
    //private String dbUserName = null;
    //private String dbPassword = null;
    private String error = null;
    
    /**
     * 
     * @return the database URL
     */
    public String getDatabaseUrl() {
        String driver = null;
        try {
            driver = session.connection().getMetaData().getURL();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return driver;
    }

    /**
     * 
     * @return the string representing the driver
     */
    public String getDriver() {
        String driver = null;
        try {
            driver = session.connection().getMetaData().getDriverName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return driver;
    }

    /**
     * @return username used to connect to the DB
     * not used anymore
    public String getDbUserName() {
        return dbUserName;
    }
     */

    /**
     * See the top of the page for the usage of the various input parameters 
     * 
     */
   /* public DBManager( String databaseUrl, String driver, String dbUserName, String dbPassword) {
        super();

        this.databaseUrl = databaseUrl;
        this.driver = driver;
        this.dbUserName = dbUserName;
        this.dbPassword = dbPassword;
    }  */
    
    /**
     * default constructor
     */
    public DBManager(){
        session = null;
    }
    
    /**
     * This function has to be called to initialized the connection to the DB
     * @return true if everything went OK
     */
    public boolean init() {
        session = edu.sdsc.nbcr.opal.state.HibernateUtil.getSessionFactory().openSession();
	return true ;
    }
    
    /**
     * Return true if the connection with the DB is valid
     * @return true if the connection to the DB is valid
     */
    public boolean isConnected(){
        if ( session != null ) return true;
        else return false;
    }

    /**
     * Close the connection with the db
     * @throws java.sql.SQLException
    public void close() throws java.sql.SQLException{
        if (isConnected()) {
            conn.close();
        }
        
    }
     */


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
        List serviceList = session.createQuery(
            "select serviceName from JobInfo group by serviceName ").list();
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

        //creating the query
        int numberOfDays = DateHelper.getOffsetDays(endDate, startDate);
        //SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        //String startDateString = formatter.format(startDate);
        //String endDateString = formatter.format(endDate);
        
        //I need to add 23:59:59 hours to the endDate to make the query working
        Calendar cal = Calendar.getInstance();
        cal.setTime(endDate);
        cal.add(Calendar.HOUR , 23);
        cal.add(Calendar.MINUTE , 59);
        cal.add(Calendar.SECOND , 59);
        endDate = cal.getTime();

        //query will hold the value of the query that will be run against the
        //DB, depending on the type value query chages
        String query = null;
        if ( type.equals("hits") ) {
            query = "select str(year(startTime))||' '||str(month(startTime))||' '||str(day(startTime))  , count(*)  " +            
                " from JobInfo where serviceName= :service" +
                " and startTime >= :startDate " +
                " and startTime <= :endDate " +
                " group by  str(year(startTime))||' '||str(month(startTime))||' '||str(day(startTime)) " +
                " order by  str(year(startTime))||' '||str(month(startTime))||' '||str(day(startTime)) desc"; 
        }else if (type.equals("exectime") ) {
            query = "select str(year(startTime))||' '||str(month(startTime))||' '||str(day(startTime)), " +
                //"sum( (lastUpdate - startTime) ) / count(*) as average " +
                "avg( (second(last_update) - second(start_time))  + (minute(last_update) - minute(start_time)) * 60 + " +
                "(hour(last_update) - hour(start_time))*60*60) " + 
                " from JobInfo " +
                " where serviceName= :service " +
                " and startTime >= :startDate " +
                " and startTime <= :endDate " +
                " and code=8 " +
                " group by str(year(startTime))||' '||str(month(startTime))||' '||str(day(startTime)) " +
                " order by str(year(startTime))||' '||str(month(startTime))||' '||str(day(startTime)) desc" ;
        } else if (type.equals("error") ){
            query  = "select str(year(startTime))||' '||str(month(startTime))||' '||str(day(startTime)), count(*) " +
                "from JobInfo " +
            	"where serviceName= :service " +
            	"and startTime >= :startDate " +
            	"and startTime <= :endDate " +
            	"and code=4 " +
            	"group by str(year(startTime))||' '||str(month(startTime))||' '||str(day(startTime)) " +
            	"order by str(year(startTime))||' '||str(month(startTime))||' '||str(day(startTime)) desc";   
        }
        
        
        //going to execute the query
        if ( ! isConnected() ) return null;
        try {
             Query queryStat = session.createQuery(query)
                .setString("service", service)
                .setTimestamp("startDate", startDate)
                .setTimestamp("endDate", endDate);
            List result = queryStat.list();
            Iterator itera = result.iterator();
            log.info("Going to get the " + type + " for the service: " + service + 
                    "\nRunning the following query: " + queryStat.getQueryString());
            
            double [] values = new double[numberOfDays+1];
            int counter = numberOfDays;
            Date previousDate =  endDate;//we are gonna start from the current date (today)
            //now we have put the data from the result of the query into the return array
            while( itera.hasNext() ) {
                Object [] entry = (Object []) itera.next();
                Date date = DateHelper.parseDateWithSpaces((String) entry[0]);
                //log.debug("For the date " + date + " we have n entries: " + ((Integer)entry[1]) );
                
                while ( ! DateHelper.compareDates(previousDate, date) ){
                    //since some day can have no hits we have to put zero in the array for those days
                    values[counter] = 0;
                    log.trace("Inserting a zero for date: " + previousDate + " on position: " + counter);
                    counter--;
                    previousDate = DateHelper.subtractDay(previousDate);
                    if ( counter == -1 ) {
                        break;
                    }
                }//if
                if ( counter == -1 ) {
                    break;
                }
                //we don't have a gap!
                if (type.equals("hits") ) {
                    values[counter] = ((Integer)entry[1]).doubleValue(); 
                }else if ( type.equals("exectime") ) {
                    //PGInterval pginterval =  (PGInterval)rs.getObject("average");
                    //double interval =  pginterval.getSeconds() + pginterval.getMinutes() * 60 + pginterval.getHours() * 60 * 60 + 
                    //pginterval.getDays() * 24 * 60 * 60 + pginterval.getMonths() * 30 * 24 * 60 * 60;
                    values[counter] = ((Float)entry[1]).doubleValue();
                }else if ( type.equals("error") ) {
                    values[counter] = (double) ((Integer)entry[1]).doubleValue(); 
                }
                log.trace("Inserting the value " + values[counter] + " for date: " + date + " on position: " + counter);
                //decrease the counter
                counter--;
                //and decrease the date
                previousDate = DateHelper.subtractDay(previousDate);
                
            }//while
            //everything went fine, let's log and return 
            String str = new String();
            for ( counter = 0; counter < values.length; counter++)
                str += values[counter] + ", ";
            log.info("The query on " + type + " with service " + service + " is returning values: " + str); 
            return values;
        }catch (Exception e ) {
            log.error("Error while querying for the " + type + " with service " + service + " : " + e.getMessage(), e);
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
        
        Integer ret = (Integer) session.createQuery(query).uniqueResult();
        log.debug("getRunningJobs for service: " + service + " returning:" + ret); 
        return ret.intValue();
    }

}
