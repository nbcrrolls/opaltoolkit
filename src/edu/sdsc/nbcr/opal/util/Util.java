package edu.sdsc.nbcr.opal.util;

import org.apache.axis.MessageContext;
import javax.servlet.http.HttpServletRequest;
import org.apache.axis.transport.http.HTTPConstants;

/**
 *
 * Utility class used by various other classes
 *
 * @author Sriram Krishnan
 */

public class Util {

    /**
     * Get IP address of remote Web service client
     */
    public static String getRemoteIP() {
	// get the current MessageContext
	MessageContext msgContext = MessageContext.getCurrentContext();

	// get the HttpServletRequest object
        Object tmp = 
	    msgContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);

        if((tmp == null) || !(tmp instanceof HttpServletRequest)) {
            return "Unknown";
        }

        HttpServletRequest req = (HttpServletRequest) tmp;
	return req.getRemoteAddr();
    }
}