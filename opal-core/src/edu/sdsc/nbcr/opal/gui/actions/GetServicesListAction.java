package edu.sdsc.nbcr.opal.gui.actions;

import java.net.URL;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.axis.message.SOAPBodyElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.MappingDispatchAction;

import edu.sdsc.nbcr.opal.AppServiceLocator;
import edu.sdsc.nbcr.opal.AppServicePortType;
import edu.sdsc.nbcr.opal.StatusOutputType;
import edu.sdsc.nbcr.opal.gui.common.AppMetadata;
import edu.sdsc.nbcr.opal.gui.common.Constants;
import edu.sdsc.nbcr.opal.gui.common.GetServiceListHelper;
import edu.sdsc.nbcr.opal.gui.common.OPALService;

/**
 * This class fetches the list of available opal services from an AXIS server and then 
 * forwards to a view that displays them.
 * 
 * @author clem
 */
public class GetServicesListAction extends MappingDispatchAction{
    
    protected Log log = LogFactory.getLog(Constants.PACKAGE);
    
    
    // See superclass for Javadoc
    public ActionForward execute(ActionMapping mapping, ActionForm form, 
            HttpServletRequest request, HttpServletResponse response) throws Exception {
    	log.info("Action: GetSerivcesListAction");
    	//let's fetch the list of services
    	String url = getServlet().getServletContext().getInitParameter("opalUrl");
    	if ( url == null ) {
    		log.warn("the opalUrl was not found in the WEB-INF/web.xml file.\nUsing the default...");
    		url = Constants.OPALDEFAULT_URL;
    	}
    	GetServiceListHelper helper = new GetServiceListHelper();
    	helper.setBaseURL(url);
    	//TODO check for exceptions, like list == null
    	SOAPBodyElement list = helper.getServiceList();
    	if ( list == null ) return returnServiceError(mapping, request, "Impossible to get the service list from the server");
    	OPALService [] servicesList = helper.parseServiceList(list.toString());
    	if ( servicesList == null ) return returnServiceError(mapping, request, "Impossible to parse the service list from the server");
    	if ( ! helper.setServiceName(servicesList) ) {
            return returnServiceError(mapping, request, "An error occurred when trying to the services names");
    	}
    	request.setAttribute("servicesList", servicesList);
    	log.info("The service list is: " + OPALService.arrayToString(servicesList));
        log.info("Action: GetSerivcesListAction forwarding to DisplayServiceList");
        return  mapping.findForward("DisplayServicesList");
    }//exectue
    
    
    
    public ActionForward returnServiceError(ActionMapping mapping, HttpServletRequest request, String errorDesc){
        log.error(errorDesc);
        ArrayList errors = new ArrayList();
        errors.add(errorDesc);
        errors.add("Try to refresh the page...");
        request.setAttribute(Constants.ERROR_MESSAGES, errors);
        return mapping.findForward("Error");
    }
    
    
}
