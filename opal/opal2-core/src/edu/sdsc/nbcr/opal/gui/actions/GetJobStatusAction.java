package edu.sdsc.nbcr.opal.gui.actions;


import java.net.URL;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.MappingDispatchAction;
import org.apache.struts.upload.FormFile;

import edu.sdsc.nbcr.opal.AppServiceLocator;
import edu.sdsc.nbcr.opal.AppServicePortType;
import edu.sdsc.nbcr.opal.InputFileType;
import edu.sdsc.nbcr.opal.JobInputType;
import edu.sdsc.nbcr.opal.JobSubOutputType;
import edu.sdsc.nbcr.opal.StatusOutputType;
import edu.sdsc.nbcr.opal.gui.common.AppMetadata;
import edu.sdsc.nbcr.opal.gui.common.ArgFlag;
import edu.sdsc.nbcr.opal.gui.common.ArgParam;
import edu.sdsc.nbcr.opal.gui.common.Constants;
import edu.sdsc.nbcr.opal.gui.common.OPALService;

/**
 * This action is used to get the job status, it requires a jobId parameters in the request.
 * Given the ID of a job it returns a page displaying its status.
 * 
 * @author clem
 *
 */
public class GetJobStatusAction extends MappingDispatchAction{
    
    protected Log log = LogFactory.getLog(Constants.PACKAGE);
    
    
    // See superclass for Javadoc
    public ActionForward execute(ActionMapping mapping, ActionForm form, 
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        
    	log.info("Action: GetJobStatusAction");
        String jobId = (String) request.getParameter("jobId");
        AppMetadata app = (AppMetadata) request.getSession().getAttribute("appMetadata");
        if ( (jobId == null) || (app == null) ) {
        	ArrayList errors = new ArrayList();
        	if ( jobId == null ) log.error("Error jobId can not be retrived.");
        	if ( app == null ) log.error("Error AppMetadata can not be retrived.");
            //something went wrong return an error
            errors = new ArrayList();
            errors.add("I could not find the jobId or the appMetadata");
            errors.add("Please return to the welcome page...");
            request.setAttribute(Constants.ERROR_MESSAGES, errors);
            return mapping.findForward("Error");
        }//if
        AppServiceLocator asl = new AppServiceLocator();
        AppServicePortType appServicePort = asl.getAppServicePort(new URL( app.getURL() ));
        StatusOutputType status = appServicePort.queryStatus(jobId);
        request.setAttribute("status", status);

        return  mapping.findForward("JobStatus");
    }//exectue
    
    
    
}//class

