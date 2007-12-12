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
 * this action is invoked to launch a job
 * it expect the appMetadata in the ActionForm with the values 
 * inserted by the user
 * 
 * @author clem
 *
 */
public class LaunchJobAction extends MappingDispatchAction{
    
    protected Log log = LogFactory.getLog(Constants.PACKAGE);
    
    
    // See superclass for Javadoc
    public ActionForward execute(ActionMapping mapping, ActionForm form, 
            HttpServletRequest request, HttpServletResponse response) throws Exception {
    	
        log.info("Action: LaunchJob");
        AppMetadata app = (AppMetadata) form;
        ArrayList errors = new ArrayList();
        if (app == null){
            log.error("Error the appMetadata is not present.");
            errors.add("We could not find the input values please go back to the welcome page");
            errors.add("appMetadata is not available");
            request.setAttribute(Constants.ERROR_MESSAGES, errors);
            return mapping.findForward("Error");
        }
        //let's print some debug informations
        String debug = "";
        if ( app.isArgMetadataEnable()) {
            ArgFlag [] flags = app.getArgFlags();
            if (flags != null ) { 
                for (int i = 0; i < flags.length; i++ ) {
                    debug += "for flags " + flags[i].getId() + " the user has entered: " + flags[i].getSelected() + "\n";
                }
            }else { debug += "no falgs found"; }
            ArgParam [] params = app.getArgParams();
            if ( params != null) {
                for (int i = 0; i < params.length; i++ ) {
                    debug += "for flags " + params[i].getId() + " the user has entered: " + params[i].getSelectedValue() + "\n";
                }
            } else { debug += "no parameters found\n"; }
        } else {
            debug += "the command line is: " + app.getCmdLine() + "\n";
            if ( app.getFile() != null) 
                debug += "the uploaded file is: " + app.getFile().getFileName() + "\n";
        }
        log.info("the following parameters has been posted:\n" + debug);
        //let's build the command line
        String cmd = makeCmdLine(app);
        if (cmd == null){
            //that's bad!
            log.error("The command line is null!");
            errors.add("We could not built the command line from your input parameters");
            errors.add("Please go back to the welcome page");
            request.setAttribute(Constants.ERROR_MESSAGES, errors);
            return mapping.findForward("Error");
        }
        log.info("the submitted command line is: " + cmd);
        //now we validate the cmd line
        //TODO add validation
        
        //let's invoke the remote opal service
        JobInputType in = new JobInputType();
        in.setArgList(cmd);
        // preparing the input files
        InputFileType [] files = getFiles(app);
        if ( files != null ) {            
            log.info(files.length + " files have been submitted");
            String names = "";
            for (int i = 0; i < files.length; i++ ) names += files[i].getName() + " ";
            log.info("their names are: " + names);
            in.setInputFile(files);
        } else{
        	//TODO improve this, it could be that the input file has been lost in the way
        	log.info("No file has been submitted.");
        }
        //finally invoke opal service!
        AppServiceLocator asl = new AppServiceLocator();
        AppServicePortType appServicePort = asl.getAppServicePort(new URL(app.getURL()));

        JobSubOutputType subOut = appServicePort.launchJob(in);
        if ( subOut == null ) {
        	log.error("An error occurred while submitting the job.");
        	log.error("The JobSubOutputType is null!!");
            errors.add("An error occured while submitting the job to the remote server");
            errors.add("Please go back to the welcome page");
            request.setAttribute(Constants.ERROR_MESSAGES, errors);
            return mapping.findForward("Error");
        }
        app.setJobId( subOut.getJobID() );
        //Let's do some logging
        log.info("Job submitted received jobID: " + subOut.getJobID());
        StatusOutputType status = subOut.getStatus();
        log.info("Current Status:\n" +
                           "\tCode: " + status.getCode() + "\n" +
                           "\tMessage: " + status.getMessage() + "\n" +
                           "\tOutput Base URL: " + status.getBaseURL());
        log.info("redirecting to the status page...");

        // everything went allright redirect to the status page
        // put the jobId in the URL coz we are redirecting and not forwarding 
        return new ActionRedirect(mapping.findForward("JobStatus").getPath() + "?jobId=" +  subOut.getJobID());
    }//exectue
    
    
    /**
     * Given an appMetadata we build the command line
     * 
     * This method can also be static...
     * 
     * @param app
     * @return a string representing the command line
     */
    private  String makeCmdLine(AppMetadata app){
        String str = "";
        if ( app.isArgMetadataEnable() ) {
            //build we have the configuration paramters
            if (app.getArgFlags() != null ) {
                ArgFlag [] flags = app.getArgFlags();
                for ( int i = 0; i < flags.length; i++){
                    //if ( (flags[i].getSelected() != null) && (flags[i].getSelected().equals("on")) )
                	if ( flags[i].getSelected() )
                        str += " " + flags[i].getTag();
                }//for
            }
            if (app.getArgParams() != null ){
                ArgParam [] params = app.getArgParams();
                String taggedParams = "";
                String [] untaggedParams = new String[app.getNumUnttagedParams()];
                for ( int i = 0; i < untaggedParams.length; i++ )
                    untaggedParams[i] = "";
                log.info("We have " + app.getNumUnttagedParams() + " untaggged parameters.");
                for( int i = 0; i < params.length; i++ ) {
                	log.info("Analizing param: " + params[i].getId());
                    if (params[i].getTag() != null) {
                        //tagged params
                        if ( params[i].getFile() != null ) {
                            //we have a file!
                            taggedParams += " " + params[i].getTag() + " " + params[i].getFile().getFileName();
                        }else if ( (params[i].getSelectedValue() != null) && ( params[i].getSelectedValue().length() > 0) )
                            taggedParams += " " + params[i].getTag() + " "  + params[i].getSelectedValue();
                    } else {
                        //untagged parameters
                        if (params[i].getFile() != null) {
                            //we have a file
                            untaggedParams[params[i].getPosition()] = " " + params[i].getFile().getFileName();
                        } else if ( (params[i].getSelectedValue() != null) && (params[i].getSelectedValue().length() > 0 ) ) {
                            //untagged params this is a bit unreadable!!
                            untaggedParams[params[i].getPosition()] = " " + params[i].getSelectedValue();
                            log.info("Adding the " + i + " untagged paramters with: " + untaggedParams[params[i].getPosition()]);
                        }//if
                    }//else
                }//for
                if (taggedParams.length() > 0)
                    str += taggedParams;
                for (int i = 0; i < app.getNumUnttagedParams(); i ++) 
                    str += untaggedParams[i];
            }
        } else {
            //no configuration parameters, that's easy
            str = app.getCmdLine();
        }
        return str;
    }//makeCmdLine
    
    
    /**
     * Given an appMetadata we return an array of InputFileType with all the files 
     * submitted by the user
     * 
     * @param app
     * @return the files submitted by the user
     */
    private InputFileType [] getFiles(AppMetadata app){
        InputFileType [] files = null;
        try {
            if ( app.getNumArgFileSubmitted() > 0 ) {
                //we have some files in the argParam array...
                int numFile = app.getNumArgFileSubmitted();
                log.info("We have " + numFile + " input files, in the ArgParam array");
                files = new InputFileType[numFile];
                for ( int i = 0; i < numFile; i++ ){
                    ArgParam param = app.getArgFileSubmitted(i);
                    files[i] = new InputFileType();
                    if (param.getFile() != null) {
                        files[i].setName(param.getFile().getFileName());
                        files[i].setContents(param.getFile().getFileData());
                        log.info("Setting up one input file which is called: " + param.getFile().getFileName());
                    } else {
                        log.error("This is very nasty... Contact developers!!\n The arg: " + param + "lost the file...");
                        return null;
                    }
                }//for
            } else if ( (app.getFile() != null) && (app.getFile().getFileName().length() > 0 ) ){
                //simple form, we have some generic input files
                log.info("We have a file in from the simple form");
                //TODO add support for multiple files
                files = new InputFileType[1];
                files[0] = new InputFileType();
                files[0].setName( app.getFile().getFileName() );
                files[0].setContents( app.getFile().getFileData() );
            }
        } catch (Exception e){
            log.error("There was an error reading uploaded files!");
            log.error("The exception is: " + e.getMessage(), e.getCause());
            e.printStackTrace();
            return null;
        }
        return files;
    }//getfile
    
}//class

