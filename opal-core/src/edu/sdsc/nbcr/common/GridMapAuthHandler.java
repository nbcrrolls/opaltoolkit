/**
 * Axis Handler for NBCR services that uses a GridMap for authorization of clients
 *
 * @author Sriram Krishnan [mailto:sriram@sdsc.edu]
 */
package edu.sdsc.nbcr.common;

import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.security.cert.X509Certificate;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.transport.http.HTTPConstants;

import org.globus.axis.gsi.GSIConstants;
import org.globus.security.gridmap.GridMap;
import org.globus.gsi.gssapi.GlobusGSSContextImpl;

import java.io.IOException;

import java.util.Enumeration;

public class GridMapAuthHandler extends BasicHandler {

    // location of gridmap file
    String gridmapLoc = null;

    // get an instance of the log4j logger
    private static Logger logger = 
	Logger.getLogger(GridMapAuthHandler.class.getName());

    public void init() {
	super.init();
	gridmapLoc = (String)getOption("gridmap");
	if (gridmapLoc == null)
	    gridmapLoc = "/etc/grid-security/grid-mapfile"; // default value
	logger.info("Location of gridmap: " + gridmapLoc);
    }

    public void invoke(MessageContext msgContext) 
	throws AxisFault {
        logger.info("entering");

	// get the HttpServletRequest object
        Object tmp = 
	    msgContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);

        if((tmp == null) || !(tmp instanceof HttpServletRequest)) {
	    logger.info("exiting");
            return;
        }

        HttpServletRequest req = (HttpServletRequest) tmp;

	// get the user's DN
        Object userDN = req.getAttribute(GSIConstants.GSI_USER_DN);

        if(userDN == null) {
	    // if this is not set, gsi https is not being used
	    logger.info("exiting");
	    return;
        }
	logger.info("Client's DN: " + userDN);

	// get the GSSContext
	Object gssContext = req.getAttribute("org.globus.gsi.context");
	try {
	    if (gssContext != null) {
		logger.debug("Source name: " + ((GlobusGSSContextImpl)gssContext).getSrcName());
		logger.debug("Target name: " + ((GlobusGSSContextImpl)gssContext).getTargName());
	    }
	} catch (Exception e) {
	    // log, and ignore
	    logger.error(e);
	}

	GridMap gridmap = new GridMap();
	try {
	    gridmap.load(gridmapLoc);
	} catch (IOException ioe) {
	    logger.fatal("Can't load gridmap", ioe);
	    throw new AxisFault("Can't load gridmap", ioe);
	}

	if (gridmap.getUserID((String) userDN) == null) {
	    logger.info("User not on the gridmap");
	    throw new AxisFault("User: " + userDN + 
				" does not have an entry on the gridmap");
	}
        logger.info("exiting");
    }
}
