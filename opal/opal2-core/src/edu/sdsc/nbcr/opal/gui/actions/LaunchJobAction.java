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
import edu.sdsc.nbcr.opal.FaultType;
import edu.sdsc.nbcr.opal.gui.common.AppMetadata;
import edu.sdsc.nbcr.opal.gui.common.ArgFlag;
import edu.sdsc.nbcr.opal.gui.common.ArgParam;
import edu.sdsc.nbcr.opal.gui.common.Constants;
import edu.sdsc.nbcr.opal.gui.common.OPALService;

/**
 * This action is invoked to launch a job.
 * It expects the appMetadata in the ActionForm with the values 
 * inserted by the user in the submission form.
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
        if ( app.isAddFile() ) {
            //maybe the user has updated a file, let's check
            FormFile [] formFiles = app.getFiles();
            if ( (formFiles[formFiles.length - 1] != null) 
                    && (formFiles[formFiles.length - 1].getFileName().length() > 0 ) ) {
                //the user has actually uploaded something!
                log.info("Adding one more input file to the simple submission form");
                //let's add an element to the files array so the user upload a new file there is a place holder (the last element)
                
                FormFile [] newFormFiles = new FormFile[formFiles.length + 1];
                for (int i = 0; i < formFiles.length; i++ ){
                    newFormFiles[i] = formFiles[i];
                }
                app.setFiles(newFormFiles);
            }
            return mapping.findForward("DisplaySimpleForm");
        }else if ( app.isArgMetadataEnable()) {
            ArgFlag [] flags = app.getArgFlags();
            if (flags != null ) { 
                for (int i = 0; i < flags.length; i++ ) {
                    debug += "for flags " + flags[i].getId() + " the user has entered: " + flags[i].isSelected() + "\n";
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
            if ( (app.getFiles() != null) && (app.getFiles().length > 0) ) { 
                debug += "we have " + app.getFiles().length + " file(s)";
                debug += "the uploaded file are: "; 
                for (int i = 0; i < app.getFiles().length; i++ )
                debug += "" + app.getFiles()[i].getFileName() + " ";
            }
        }
        log.info("the following parameters has been posted:\n" + debug);
        //let's build the command line
        String cmd = makeCmdLine(app);
        if (cmd == null){
            //that's bad!
            log.error("The command line is null!");
            errors.add("We could not built the command line from your input parameters");
            request.setAttribute(Constants.ERROR_MESSAGES, errors);
            return mapping.findForward("Error");
        }
        log.info("the submitted command line is: " + cmd);
        //now we could validate the cmd line
        //let's invoke the remote opal service
        JobInputType in = new JobInputType();
        in.setArgList(cmd);


        int numCpu = -1;
        if ( (app.getNumCpu() != null) && (app.getNumCpu().length() >= 1) ) {
            //let's get the number of CPUs
            try { 
                numCpu = Integer.parseInt( app.getNumCpu() ); 
                if ( numCpu <= 0 ) throw new NumberFormatException();
            }
            catch (NumberFormatException e) {
                log.info("the user has entered wrong number of cpu");
                errors.add("the number of cpu is worng.");
                errors.add("Number of cpu should be a positive integer number and not a \"" + app.getNumCpu() + "\"");
                request.setAttribute(Constants.ERROR_MESSAGES, errors);
                return mapping.findForward("Error");
            }
        }
        //here numCpu is either -1 or a positive integer
        if ( numCpu != -1 ) {
            in.setNumProcs(numCpu);
        }

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
        JobSubOutputType subOut = null;
        try {
            AppServiceLocator asl = new AppServiceLocator();
            AppServicePortType appServicePort = asl.getAppServicePort(new URL(app.getURL()));

	    // TODO: fix this to use attachments, if need be
	    subOut = appServicePort.launchJob(in, null);
            if ( subOut == null ) {
                throw new Exception("launchJob returned null");
            }
        }catch (FaultType e){
            log.error("A remote error occurred while submitting the job.");
            log.error("The remote error message is: " + e.getMessage1(), e);

            errors.add("A remote error occured while submitting the job to the remote server");
            errors.add("the remote error message is: " + e.getMessage1());
            request.setAttribute(Constants.ERROR_MESSAGES, errors);
            return mapping.findForward("Error");
        }catch (Exception e){
            log.error("An error occurred while submitting the job.");
            log.error("The error message is: " + e.getMessage(), e);

            errors.add("An error occured while submitting the job to the remote server");
            errors.add("the error message is: " + e.getMessage());
            errors.add("Please go back to the List of Application page and try resubmitting the job");
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
        return new ActionRedirect(mapping.findForward("JobStatus").getPath() + "?jobId=" +  subOut.getJobID() + "&serviceID=" + app.getServiceID());
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
                //let's do the flag
                ArgFlag [] flags = app.getArgFlags();
                for ( int i = 0; i < flags.length; i++){
                    //if ( (flags[i].getSelected() != null) && (flags[i].getSelected().equals("on")) )
                	if ( flags[i].isSelected() )
                        str += " " + flags[i].getTag();
                }//for
            }
            if (app.getArgParams() != null ){
                //let's do the tag and untagged
                ArgParam [] params = app.getArgParams();
                String taggedParams = "";
                String separator = app.getSeparator();
                if ( separator == null ) { 
                    separator =  " ";
                }
                String [] untaggedParams = new String[app.getNumUnttagedParams()];
                for ( int i = 0; i < untaggedParams.length; i++ )
                    untaggedParams[i] = "";
                log.info("We have " + app.getNumUnttagedParams() + " untaggged parameters.");
                for( int i = 0; i < params.length; i++ ) {
                	log.info("Analizing param: " + params[i].getId());
                    if (params[i].getTag() != null) {
                        //tagged params
                        if ( params[i].isFileUploaded() ) {
                            //we have a file!
                            taggedParams += " " + params[i].getTag() + separator + params[i].getFile().getFileName();
                        }else if ( (params[i].getSelectedValue() != null) && ( params[i].getSelectedValue().length() > 0) )
                            taggedParams += " " + params[i].getTag() + separator + params[i].getSelectedValue();
                    } else {
                        //untagged parameters
                        if (params[i].isFileUploaded() ) {
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
            } else if ( (app.getFiles() != null) && ( app.getFiles()[0] != null) && (app.getFiles()[0].getFileName().length() > 0 ) ){
                //simple form, we have at least one input file
                log.info("We have at least a file in the simple form");
                FormFile [] filesForm =  app.getFiles();
                ArrayList filesArrayReturn = new ArrayList();
                
                //get the number of files  --- not nice! make a function!
                for (int i = 0; i < filesForm.length; i++) {
                    if ((filesForm[i] != null) && (filesForm[i].getFileName().length() > 0)){
                        //let's an input file
                        InputFileType file = new InputFileType();
                        file.setName( app.getFiles()[i].getFileName() );
                        file.setContents( app.getFiles()[i].getFileData() );
                        filesArrayReturn.add( file );
                    }//if
                }//for
                
                files = (InputFileType[]) filesArrayReturn.toArray(new InputFileType[filesArrayReturn.size()]);
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

