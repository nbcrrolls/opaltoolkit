/**
 * Axis Handler for NBCR services that authorizes clients on the basis of 
 * accepted CAs
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
import org.globus.gsi.gssapi.GSSConstants;

import org.ietf.jgss.Oid;

import java.io.IOException;

import java.util.Enumeration;

public class CAAuthHandler extends BasicHandler {

    // location of ca-map file
    String caMapLoc = null;

    // get an instance of the log4j logger
    private static Logger logger = 
	Logger.getLogger(CAAuthHandler.class.getName());

    public void init() {
	super.init();
	caMapLoc = (String)getOption("ca-map");
	if (caMapLoc == null)
	    logger.error("Property ca-map not set");
	else 
	    logger.info("Location of ca-map: " + caMapLoc);
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
	String issuerDN = null;
	try {
	    if (gssContext != null) {
		Object certs = ((GlobusGSSContextImpl)gssContext).
		    inquireByOid(GSSConstants.X509_CERT_CHAIN);
		if (certs != null) {
		    X509Certificate[] chain = (X509Certificate[]) certs;
		    logger.debug("Certificate chain - ");
		    for (int i = 0; i < chain.length; i++) {
			logger.debug(chain[i].getSubjectDN());
		    }
		    issuerDN = chain[chain.length-1].getSubjectDN().toString();
		    logger.info("Client's CA DN: " + issuerDN);
		}
	    } else {
		// if this is not set, gsi https is not being used
		logger.info("exiting");
		return;
	    }
	} catch (Exception e) {
	    // log, and return
	    logger.error(e);
	    throw new AxisFault("Error while reading certificate chain: " + 
				e.getMessage());
	}

	GridMap caMap = new GridMap();
	try {
	    caMap.load(caMapLoc);
	} catch (IOException ioe) {
	    logger.fatal("Can't load ca-map", ioe);
	    throw new AxisFault("Can't load ca-map", ioe);
	}

	if (issuerDN == null) {
	    logger.error("Can't find DN for the client's CA");
	    throw new AxisFault("Can't find DN for the client's CA");
	}

	if (caMap.getUserID(issuerDN) == null) {
	    logger.info("DN for the client's CA not on the ca-map");
	    throw new AxisFault("CA: " + issuerDN + 
				" does not have an entry on the ca-map");
	}
        logger.info("exiting");
    }
}
