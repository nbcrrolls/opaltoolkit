// GamaInfoServlet.java
//
// Servlet that implements the opal dashboard
//
// 11/28/07   - created, Luca Clementi
//


package edu.sdsc.nbcr.opal.dashboard.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.RequestDispatcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.sdsc.nbcr.opal.dashboard.persistence.DBManager;
import edu.sdsc.nbcr.opal.dashboard.util.DateHelper;

import java.io.IOException;
import java.io.InputStream;
import java.lang.Character;

/** 
 *
 * This class implements all the business logic behind the Opal dashboard.
 * 
 * @author clem
 *
 */
public class OpalInfoServlet extends HttpServlet {

    protected static Log log = LogFactory.getLog(OpalInfoServlet.class.getName());
    private static final String SUMMARY_JSP = "/dashboard-jsp/summary.jsp";
    private static final String STATISTICS_JSP = "/dashboard-jsp/statistics.jsp";
    private static final String SYSINFO_JSP = "/dashboard-jsp/sysinfo.jsp";
    private static final String DOC_JSP = "/dashboard-jsp/documentation.jsp";
    private static final String CONTACTUS_JSP = "/dashboard-jsp/contactus.jsp";
    
    //private static final String ERROR_JSP = "/dashboard-jsp/error.jsp";
    //this is used only when the DB is not available
    private static final String ERROR_JSP = "/dashboard-jsp/statistics_noDB.jsp";

    private String opalVersion = null;
    private String opalDataLifetime = null;
    private String opalUptimeCommand = "uptime";
    private String opalBuildDateCommand = "uname -a";
    private String opalWebsite = null;
    private String opalDocumentation = null;
    private Boolean drmaa = Boolean.FALSE; 
    //TODO improve handling of unconnected DB
    private Boolean dbUsed = Boolean.FALSE;
    private Boolean globus = Boolean.FALSE;
    private String globusGatekeeper = null;
    private DBManager dbManager = null;
    

    public final void init(ServletConfig config) throws ServletException {
        super.init(config);
        log.info("Loading OpalInfoServlet (init method).");
        try {
            opalVersion = config.getServletContext().getInitParameter("OPAL_VERSION");
            opalBuildDateCommand = config.getServletContext().getInitParameter("OPAL_BUILDDATE_COMMAND");
            opalUptimeCommand = config.getServletContext().getInitParameter("OPAL_UPTIME_COMMAND");
            opalWebsite = config.getServletContext().getInitParameter("OPAL_WEB_SITE");
            opalDocumentation = config.getServletContext().getInitParameter("OPAL_DOC");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("An error occurred while initializing the OpalInfoServlet, impossible to load web.xml: " + e.getMessage());
            dbManager = new DBManager();
            return;
        }
        
        
        
        //-------     initializing the DB connection    -----
        java.util.Properties props = new java.util.Properties();
        String propsFileName = "opal.properties";
        String databaseUrl = null;
        String dbUserName = null;
        String dbPassword = null;
        boolean initialized = false;
        try {
            // load runtime properties from properties file
            props.load(PloterServlet.class.getClassLoader().getResourceAsStream(propsFileName));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Impossible to load opal.properties: " + e.getMessage());
            dbManager = new DBManager();
            //pointless to go on!!
            return;
        }
        
        
        //getting some more informations
        if ( props.getProperty("drmaa.use") != null ) {
            if ( props.getProperty("drmaa.use").equals("true") ) 
                drmaa = Boolean.TRUE;
        }
        if ( props.getProperty("globus.use") != null ) {
            if ( props.getProperty("globus.use").equals("true") ) 
                globus = Boolean.TRUE;
        }
        if ( globus.booleanValue() == true ) {
            globusGatekeeper = props.getProperty("globus.gatekeeper");
        }
        if (props.getProperty("opal.datalifetime") != null) {
            opalDataLifetime = props.getProperty("opal.datalifetime");
        } else opalDataLifetime = null;
        
        
        if (props.getProperty("database.use") != null) {
            if ( props.getProperty("database.use").equals("true") ) {
                dbUsed = Boolean.TRUE;
                initialized = true;
            } else {
                //no DB connection, useless to go on 
                dbManager = new DBManager();
                initialized = false; 
                return ;
            }
        }
        if (props.getProperty("database.url") != null) {
            databaseUrl = props.getProperty("database.url");
        }
        if (props.getProperty("database.user") != null) {
            dbUserName = props.getProperty("database.user");
        }
        if (props.getProperty("database.passwd") != null) {
            dbPassword = props.getProperty("database.passwd");
        }
        if ( initialized ) {
            // connect to database
            System.out.println("Initializing database connection... to " + databaseUrl);
            dbManager = new DBManager( databaseUrl, "org.postgresql.Driver", dbUserName, dbPassword);
            if ( dbManager.init() ) {
                initialized = true;
                config.getServletContext().setAttribute("dbManager", dbManager);
            }
            else initialized = false;
        }//if
        
    }

    /**
     * 
     * @param req the <code>HttpServletRequest</code>
     * @param res the <code>HttpServletResponse</code>
     * @throws java.io.IOException if an I/O error occurs
     * @throws javax.servlet.ServletException if a servlet error occurs
     */
    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        processRequest(req, res);
    }

    /**
     * @see #doGet
     */
    public final void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        doGet(req, res);
    }

    /**
     * Both doGet and goPort call this function that actually does the processing
     * 
     * @throws IOException
     * @throws ServletException
     */
    public void processRequest(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        String command = req.getParameter("command");
        RequestDispatcher dispatcher;
        

        if ("statistics".equals(command)) {
            //let's check if the DB connection is OK
            if ( (dbManager == null ) || ( !dbManager.isConnected()) ) {
                String errorMsg = "The connection to the Data Base is not present.<br/> " +
                        "Either you are not using a DB or there are some problem in your configuration file. <br/>" +
                        "Please have a look at the opal WEB_INF/web.xml and WEB-INF/classes/opal.properties. <br/>";
                log.error("We had an error: " + errorMsg);
                req.setAttribute("error", errorMsg);
                dispatcher = getServletContext().getRequestDispatcher(ERROR_JSP);
                try { dispatcher.forward(req, res); }
                catch (Exception e ) {
                   log.error("Impossible to forward to the error page...Don't know what else I can do....", e);
                }
                return;
            }
            //Begin and end date
            String startDateStr = req.getParameter("startDate");
            String endDateStr = req.getParameter("endDate");
            if ( (DateHelper.parseDate(startDateStr) == null) ||
                (DateHelper.parseDate(endDateStr) == null) ) {
                //we do not have start and and date
                endDateStr = DateHelper.formatDate( DateHelper.getEndDate() );
                startDateStr = DateHelper.formatDate( DateHelper.getStartDate() );
            }
            req.setAttribute("startDate", startDateStr);
            req.setAttribute("endDate", endDateStr);
            
            //list of services to display
            String [] servicesName = dbManager.getServicesList();
            String [] servicesNameSelected = req.getParameterValues("servicesName"); 
            if ( servicesNameSelected == null){
                log.info("there was no service name selected in the request parameters, selecting all of them");
                servicesNameSelected = dbManager.getServicesList();
            }
            req.setAttribute("servicesNameSelected", servicesNameSelected);
            req.setAttribute("servicesName", servicesName);
            dispatcher = getServletContext().getRequestDispatcher(STATISTICS_JSP);
            dispatcher.forward(req, res);
        } else if ("sysinfo".equals(command)) {
            //this doesn't exist anymore... Now there is the opal GUI
            dispatcher = getServletContext().getRequestDispatcher(SYSINFO_JSP);
            dispatcher.forward(req, res);
        } else if ("doc".equals(command)) {
            //this doesn't exist anymore... Now there is the opal GUI
            res.sendRedirect(opalDocumentation);
        } else if ("contactus".equals(command)) {
            req.setAttribute("opalWebsite", opalWebsite);
            dispatcher = getServletContext().getRequestDispatcher(CONTACTUS_JSP);
            dispatcher.forward(req, res);
        } else {
            // need to gather a bunch of information regarding the opal
            // installation
            req.setAttribute("systemIPAddress", req.getLocalAddr());
            req.setAttribute("systemUptime", getUptime());
            req.setAttribute("systemBuildDate", getBuildDate());
            req.setAttribute("opalVersion", opalVersion);
            req.setAttribute("opalWebsite", opalWebsite);
            req.setAttribute("opalDocumentation", opalDocumentation);
            req.setAttribute("dbURL", dbManager.getDatabaseUrl());
            req.setAttribute("dbUsername", dbManager.getDbUserName());
            req.setAttribute("dbDriver", dbManager.getDriver());
            req.setAttribute("drmaa", drmaa);
            req.setAttribute("globus", globus);
            req.setAttribute("globusGatekeeper", globusGatekeeper);
            req.setAttribute("opalDataLifetime", opalDataLifetime);
            
            dispatcher = getServletContext().getRequestDispatcher(SUMMARY_JSP);
            dispatcher.forward(req, res);
        }
    }

    /**
     * exec the command on the local system
     * @param command the command to be executed 
     * @param error the log string to be printed in case of error
     * @return the output of the command
     */
    public String exec(String command, String error) {
        String r = new String();
        try {
            Process child = Runtime.getRuntime().exec(command);
            child.waitFor();
            if (child.exitValue() != 0) {
                // error
                r = new String(error);
            } else {
                InputStream in = child.getInputStream();
                int c;
                while ((c = in.read()) != -1) {
                    r = r.concat(Character.toString((char) c));
                }
                in.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            r = new String(error);
        }
        log.info("Exec: command = " + command + " result = " + r);
        return r;
    }
	
    public String getUptime() {
        return exec(opalUptimeCommand, "error, unable to determine uptime");
    }

    public String getBuildDate() {
        return exec (opalBuildDateCommand, "error, unable to determine build date");
    }
	
    public void destroy() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

}
