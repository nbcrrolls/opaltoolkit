package edu.sdsc.nbcr.opal;

import java.net.URL;

import javax.xml.rpc.Stub;

import org.apache.axis.client.Call;
import org.apache.axis.client.AxisClient;
import org.apache.axis.SimpleTargetedChain;
import org.apache.axis.configuration.SimpleProvider;
import org.apache.axis.description.TypeDesc;
import org.apache.axis.encoding.SerializationContext;
import org.apache.axis.MessageContext;

import org.globus.gram.GramJob;
import org.globus.axis.util.Util;
import org.globus.axis.gsi.GSIConstants;
import org.globus.axis.transport.GSIHTTPSender;
import org.globus.axis.transport.HTTPSSender;
import org.globus.axis.gsi.GSIConstants;
import org.globus.gsi.gssapi.auth.IdentityAuthorization;

import org.ietf.jgss.GSSCredential;
import org.gridforum.jgss.ExtendedGSSManager;
import org.gridforum.jgss.ExtendedGSSCredential;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.StringWriter;

/**
 * Generic client for any Opal based Web service
 * 
 * @author  Sriram Krishnan
 */

public class GenericServiceClient {
    
    /**
     * Default constructor
     */
    public GenericServiceClient() {
    }

    /**
     * Run the generic Opal Web services client
     * 
     * <p>For usage information, type <i>java edu.sdsc.nbcr.opal.GenericServiceClient</i>
     */
    public static void main(String args[])
	throws Exception {

	String serviceURL = null;
	String operation = null;

	Options options = new Options();
	options.addOption(OptionBuilder.withArgName("url")
			  .isRequired()
			  .withDescription("service url")
			  .hasArg()
			  .create("l"));
	options.addOption(OptionBuilder.withArgName("operation")
			  .isRequired()
			  .withDescription("remote operation to invoke: [getAppMetadata|" +
					   "launchJob|queryStatus|getOutputs|destroy]")
			  .hasArg()
			  .create("r"));
	options.addOption(OptionBuilder.withArgName("num_procs")
			  .withDescription("number of processors for parallel job")
			  .hasArg()
			  .create("n"));
	options.addOption(OptionBuilder.withArgName("job_id")
			  .withDescription("job id for a run")
			  .hasArg()
			  .create("j"));
	options.addOption(OptionBuilder.withArgName("args")
			  .withDescription("command line arguments")
			  .hasArg()
			  .create("a"));
	options.addOption(OptionBuilder.withArgName("file1" + 
						    File.pathSeparatorChar + 
						    "file2" + 
						    File.pathSeparatorChar + 
						    "..")
			  .withDescription("input files")
			  .withValueSeparator(File.pathSeparatorChar)
			  .hasArgs(Option.UNLIMITED_VALUES)
			  .create("f"));
	options.addOption(OptionBuilder.withArgName("serverDN")
			  .withDescription("server DN expected - if gsi is being used")
			  .hasArg()
			  .create("dn"));
	options.addOption(OptionBuilder.withArgName("noMutualAuth")
			  .withDescription("disable mutual authentication on client side")
			  .create("m"));

	System.out.println("\nReading command line arguments");
	CommandLineParser parser = new GnuParser();
	CommandLine line = null;
	try {
	    line = parser.parse(options, args);
	} catch (Exception e) {
	    System.err.println(e.toString());
	    HelpFormatter formatter = new HelpFormatter();
	    formatter.printHelp("java edu.sdsc.nbcr.opal.GenericServiceClient", options);
	    System.exit(1);
	}

	serviceURL = line.getOptionValue("l");
	System.out.println("Service URL: " + serviceURL);

	operation = line.getOptionValue("r");
	System.out.println("Invoking operation: " + operation);

	String serverDN = "/C=US/O=nbcr/OU=sdsc/CN=apbs_service";
	if (line.getOptionValue("dn") != null) {
	    serverDN = line.getOptionValue("dn");
	    System.out.println("Server DN: " + serverDN);
	}
	System.out.print("\n");

	// connect to the App Web service
	AppServiceLocator asl = new AppServiceLocator();

	// register a protocol handler for https, if need be
	int index = serviceURL.indexOf(":");
	boolean httpsInUse = false;
	if (index > 0) {
	    String proto = serviceURL.substring(0, index);
	    if (proto.equals("https")) {
		httpsInUse = true;
	    }
	}
	boolean gsiInUse = httpsInUse;
	if (line.hasOption("m")) {
	    // if mutual auth is disabled, don't use GSI
	    gsiInUse = false;
	}

	if (gsiInUse) {
	    SimpleProvider provider = new SimpleProvider();	
	    SimpleTargetedChain c = new SimpleTargetedChain(new HTTPSSender());
	    provider.deployTransport("https", c);
	    asl.setEngine(new AxisClient(provider));
	    Util.registerTransport();
	    System.out.println("HTTPS protocol handler registered\n");
	}

	AppServicePortType appServicePort =
	    asl.getAppServicePort(new URL(serviceURL));

	// read credentials for the client
	GSSCredential proxy = null;
	if (gsiInUse) {
	    String proxyPath = System.getProperty("X509_USER_PROXY");
	    if (proxyPath == null) {
		System.err.println("Required property X509_USER_PROXY not set");
		System.exit(1);
	    }
	    File f = new File(proxyPath);
	    byte [] data = new byte[(int) f.length()];
	    FileInputStream in = new FileInputStream(f);
	    in.read(data);
	    in.close();
	    
	    ExtendedGSSManager manager = 
		(ExtendedGSSManager) ExtendedGSSManager.getInstance();
	    proxy = manager.createCredential(data,
					     ExtendedGSSCredential.IMPEXP_OPAQUE,
					     GSSCredential.DEFAULT_LIFETIME,
					     null, // use default mechanism - GSI
					     GSSCredential.INITIATE_AND_ACCEPT);
	}

	// set the GSI specific properties
	IdentityAuthorization auth = 
	    new IdentityAuthorization(serverDN);
	if (gsiInUse) {
	    ((Stub) appServicePort)._setProperty(GSIConstants.GSI_AUTHORIZATION, 
						 auth);
	    ((Stub) appServicePort)._setProperty(GSIConstants.GSI_CREDENTIALS,
						 proxy);
	}

	if (operation.equals("getAppMetadata")) {
	    System.out.println("Getting application metadata - ");
	    AppMetadataType amt = appServicePort.getAppMetadata(new AppMetadataInputType());

	    TypeDesc typeDesc = amt.getTypeDesc();
	    StringWriter sw = new StringWriter();
	    MessageContext mc = new MessageContext(new AxisClient());
	    SerializationContext sc = new SerializationContext(sw, mc);
	    sc.setDoMultiRefs(false);
	    sc.setPretty(true);
	    sc.serialize(typeDesc.getXmlType(), 
			 null, 
			 amt,
			 typeDesc.getXmlType(), 
			 new Boolean(true), 
			 new Boolean(true));
	    sw.close();
	    
	    System.out.println(sw.toString());

	} else if (operation.equals("launchJob")) {
	    JobInputType in = new JobInputType();

	    String cmdArgs = line.getOptionValue("a");
	    if (cmdArgs != null) {
		System.out.println("Command line arguments: " + cmdArgs);
		in.setArgList(cmdArgs);
	    }

	    String numProcs = line.getOptionValue("n");
	    if (numProcs != null) {
		System.out.println("Number of processors: " + numProcs);
		in.setNumProcs(new Integer(numProcs));
	    }

	    String[] inputFiles = line.getOptionValues("f");
	    if (inputFiles != null) {
		InputFileType[] infile = new InputFileType[inputFiles.length];
		for (int i = 0; i < inputFiles.length; i++) {
		    File f = new File(inputFiles[i]);
		    byte[] data = new byte[(int) f.length()];
		    FileInputStream fIn = new FileInputStream(f);
		    fIn.read(data);
		    fIn.close();
		    infile[i] = new InputFileType();
		    infile[i].setName(f.getName());
		    infile[i].setContents(data);
		}
		in.setInputFile(infile);
	    }

	    // set up a non-blocking call
	    System.out.println("Making non-blocking invocation on Opal service -");
	    JobSubOutputType subOut = appServicePort.launchJob(in);
	    System.out.println("Received jobID: " + subOut.getJobID());
	    
	    StatusOutputType status = subOut.getStatus();
	    System.out.println("Current Status:\n" +
			       "\tCode: " + status.getCode() + "\n" +
			       "\tMessage: " + status.getMessage() + "\n" +
			       "\tOutput Base URL: " + status.getBaseURL());

	} else if (operation.equals("queryStatus")) {
	    String jobID = line.getOptionValue("j");
	    if (jobID == null) {
		System.err.println("Required option -j not found for queryStatus operation");
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java edu.sdsc.nbcr.opal.GenericServiceClient", options);
		System.exit(1);
	    }

	    System.out.println("Retrieving job status");
	    StatusOutputType status = appServicePort.queryStatus(jobID);
	    System.out.println("Status for job: " + 
			       jobID + "\n" +
			       "\tCode: " + status.getCode() + "\n" +
			       "\tMessage: " + status.getMessage() + "\n" +
			       "\tOutput Base URL: " + status.getBaseURL());

	} else if (operation.equals("getOutputs")) {
	    String jobID = line.getOptionValue("j");
	    if (jobID == null) {
		System.err.println("Required option -j not found for queryStatus operation");
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java edu.sdsc.nbcr.opal.GenericServiceClient", options);
		System.exit(1);
	    }

	    System.out.println("Retrieving job output -");
	    JobOutputType out = appServicePort.getOutputs(jobID);
	    System.out.println("Standard output: " +
			       out.getStdOut().toString());
	    System.out.println("Standard error: " +
			       out.getStdErr().toString());
	    OutputFileType[] outfile = out.getOutputFile();
	    if (outfile != null) {
		for (int i = 0; i < outfile.length; i++) {
		    System.out.println(outfile[i].getName() + ": " +
				       outfile[i].getUrl());
		}
	    }
	} else if (operation.equals("destroy")) {
	    String jobID = line.getOptionValue("j");
	    System.out.println("Job id: " + jobID);
	    if (jobID == null) {
		System.err.println("Required option -j not found for queryStatus operation");
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java edu.sdsc.nbcr.opal.GenericServiceClient", options);
		System.exit(1);
	    }

	    System.out.println("Destroying job");
	    StatusOutputType status = appServicePort.destroy(jobID);
	    System.out.println("Final status for job: " + 
			       jobID + "\n" +
			       "\tCode: " + status.getCode() + "\n" +
			       "\tMessage: " + status.getMessage() + "\n" +
			       "\tOutput Base URL: " + status.getBaseURL());
	} else {
	    System.err.println("Operation " + operation + " not supported");
	    System.exit(1);
	}
    }
}    



