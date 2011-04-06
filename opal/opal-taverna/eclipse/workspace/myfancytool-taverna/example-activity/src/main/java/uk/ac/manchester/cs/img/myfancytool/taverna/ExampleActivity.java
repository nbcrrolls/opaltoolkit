package uk.ac.manchester.cs.img.myfancytool.taverna;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
//import java.net.MalformedURLException;
import java.net.*;
import java.rmi.RemoteException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
import java.util.*;

import javax.xml.rpc.ServiceException;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
import net.sf.taverna.t2.workflowmodel.utils.AnnotationTools;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.annotation.Annotated;
import net.sf.taverna.t2.annotation.annotationbeans.FreeTextDescription;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;

import org.apache.axis.types.URI;
import org.apache.log4j.Logger;

import edu.sdsc.nbcr.opal.*;
import edu.sdsc.nbcr.opal.gui.common.GetServiceListHelper;
import edu.sdsc.nbcr.opal.gui.common.OPALService;

public class ExampleActivity extends
		AbstractAsynchronousActivity<ExampleActivityConfigurationBean>
		implements AsynchronousActivity<ExampleActivityConfigurationBean> {
	
	private static final String outport = "out_url_list";
	
    private static final Logger logger = Logger
                    .getLogger(ExampleActivity.class);
	
	private ExampleActivityConfigurationBean configBean;
	
	private Map<String, String> inputTypeMap = new HashMap<String, String>();
	
	private String url;
	
	private Set<String> flag_set = new HashSet<String> ();
	private Set<String> untagged_set = new HashSet<String> ();
	private Set<String> tagged_set = new HashSet<String> ();
	private Set<String> file_set = new HashSet<String> ();
	
	private Map<String, String> input_arg_map = new HashMap<String, String> ();
	private Map<String, Object> defaultValues = new HashMap();
	
	private String test_var;

	private boolean basic_ws = false;
	private String basic_ws_cmdLine;
	private String basic_ws_numProcs;
	private String basic_ws_inFiles;
	
	@Override
	public void configure(ExampleActivityConfigurationBean configBean)
			throws ActivityConfigurationException {		
		// Any pre-config sanity checks
		if (configBean.getExampleString().equals("invalidExample")) {
			throw new ActivityConfigurationException(
					"Example string can't be 'invalidExample'");
		}
		// Store for getConfiguration(), but you could also make
		// getConfiguration() return a new bean from other sources
		this.configBean = configBean;
		

		// OPTIONAL: 
		// Do any server-side lookups and configuration, like resolving WSDLs

		// myClient = new MyClient(configBean.getExampleUri());
		// this.service = myClient.getService(configBean.getExampleString());
				
		test_var = configBean.getExampleString();
		int spos = test_var.indexOf("_from");
		String test_var_snd = test_var.substring(0, spos);
		String test_var_fst = test_var.substring(spos + 6);
		test_var  = "http://" + test_var_fst.replace('_', '/') + '/' + test_var_snd;
		
		// REQUIRED: (Re)create input/output ports depending on configuration
		configurePorts();
	}

	protected void configurePorts() {
		// In case we are being reconfigured - remove existing ports first
		// to avoid duplicates
		removeInputs();
		removeOutputs();
		
		/*
		Annotated annotated;
		AnnotationTools annotationTools = new AnnotationTools();
	
		annotated = null;
		
		try {
			annotationTools.setAnnotationString(annotated, FreeTextDescription.class, "test value").doEdit();
		} catch (EditException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		*/

		// FIXME: Replace with your input and output port definitions

		AppServiceLocator asl = new AppServiceLocator();
		AppServicePortType appServicePort;
		AppMetadataType amt;
		//String opal_url = "http://ws.nbcr.net/opal2/services/Pdb2pqrOpalService";
		//url = opal_url;
		//opal_url = test_var;
		//url = test_var;
		
		String opal_url = test_var;
		url = test_var;
		
		boolean has_tagged = true;
		boolean has_untagged = true;
		boolean has_flags = true;
				
		try {
			appServicePort = asl.getAppServicePort(new java.net.URL(opal_url));
			amt = appServicePort.getAppMetadata(new AppMetadataInputType());
			
			//inputTypeMap.put("mykey", "myval");
			
			ParamsType [] paramType = null;
			ArrayList args = new ArrayList();
			
			try { 
				amt.getTypes().getTaggedParams();
			} catch (NullPointerException npe) {
				has_tagged = false;
			}
			
			try { 
				amt.getTypes().getUntaggedParams();
			} catch (NullPointerException npe) {
				has_untagged = false;
			}
			
			try {
				amt.getTypes().getFlags();
			} catch (NullPointerException npe) {
				has_flags = false;
			}
			
			if (has_tagged == false && has_untagged == false && has_flags == false) {
				addInput("commandLine", 0, false, null, String.class);
				addInput("inFiles", 0, false, null, String.class);
				addInput("numProcs", 0, false, null, String.class);
				
				basic_ws = true;
			} 
			else {
				ParamsArrayType taggedArrayType = amt.getTypes().getTaggedParams();	
				ParamsArrayType untaggedArrayType = amt.getTypes().getUntaggedParams();	
				//FlagsArrayType flagArrayType = amt.getTypes().getFlags();

				if ( (taggedArrayType != null) && (taggedArrayType.getParam() != null ) ) {
					paramType = taggedArrayType.getParam();
				
					String separator = taggedArrayType.getSeparator();
					
					if (separator == null) 
						separator = " ";
				
					for (int i = 0; i < paramType.length; i++) {
						String pn = paramType[i].getId().toString();
						String tag = paramType[i].getTag().toString();
						String arg = tag + separator;
						//addInput(pn, 0, false, null, String.class);
						tagged_set.add(pn);
						input_arg_map.put(pn, arg);
					
						if (paramType[i].getParamType() != null) {
							IOType iotype = paramType[i].getIoType();
							if (paramType[i].getParamType().getValue() == ParamType._FILE && iotype.getValue().equals(IOType._INPUT)) {							
								file_set.add(pn);
								addInput(pn, 0, false, null, File.class);
							}
							else
								addInput(pn, 0, false, null, String.class);
						}
					}	
				}
			
				if ( (untaggedArrayType != null) && (untaggedArrayType.getParam() != null ) ) {
					paramType = untaggedArrayType.getParam();
				
					for (int i = 0; i < paramType.length; i++) {
						String pn = paramType[i].getId().toString();
						//addInput(pn, 0, false, null, String.class);
						untagged_set.add(pn);
						input_arg_map.put(pn, "");
				
						if (paramType[i].getParamType() != null) {
							IOType iotype = paramType[i].getIoType();
							if (paramType[i].getParamType().getValue() == ParamType._FILE && iotype.getValue().equals(IOType._INPUT)) {
								file_set.add(pn);
								addInput(pn, 0, false, null, File.class);
							}
							else
								addInput(pn, 0, false, null, String.class);
						}
					}
				}
			
				if ((amt.getTypes().getFlags() != null) && (amt.getTypes().getFlags().getFlag() != null)) {
					FlagsType [] flagsType = amt.getTypes().getFlags().getFlag();
				
					for (int i = 0; i < flagsType.length; i++) {
						String pn = flagsType[i].getId().toString();
						String tag = flagsType[i].getTag().toString();
						//pn = flagsType[i].getTextDesc();
						//addInput(pn, 0, false, null, Boolean.class);
				
						addInput(pn, 0, false, null, Boolean.class);
						//inputTypeMap.put(pn, opal_url);
						flag_set.add(pn);
						input_arg_map.put(pn, tag);
						
						//annotationTools.setAnnotationString(annotated, FreeTextDescription.class, "test description").doEdit();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Hard coded input port, expecting a single String
		//addInput(IN_FIRST_INPUT, 0, true, null, String.class);

		// Optional ports depending on configuration
		//if (configBean.getExampleString().equals("specialCase")) {
			// depth 1, ie. list of binary byte[] arrays
			//addInput(IN_EXTRA_DATA, 1, true, null, byte[].class);
			//addOutput(OUT_REPORT, 0);
		//}
		
		// Single value output port (depth 0)
		//addOutput(OUT_SIMPLE_OUTPUT, 0);
		// Output port with list of values (depth 1)
		//addOutput(OUT_MORE_OUTPUTS, 1);
		addOutput(outport, 0);
		
		
		AnnotationTools annotationTools = new AnnotationTools();

		for (ActivityInputPort aip: this.getInputPorts()) {
			annotationTools.setAnnotationString(aip, FreeTextDescription.class, "some descriptive text that you get from somewhere");
		}
			
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void executeAsynch(final Map<String, T2Reference> inputs,
			final AsynchronousActivityCallback callback) {
		// Don't execute service directly now, request to be run ask to be run
		// from thread pool and return asynchronously
		callback.requestRun(new Runnable() {
			
			@SuppressWarnings("deprecation")
			public void run() {
				String numProcs = null;
				String inFiles = null;
				Set<String> infile_list = new HashSet<String> ();
				Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
				
				InvocationContext context = callback
						.getContext();
				ReferenceService referenceService = context
						.getReferenceService();
			
				/*
	            for (Map.Entry<String, T2Reference> entry : inputs
	            		.entrySet()) {
			System.out.println("Resolving " + entry.getKey() + " to "
                		   + inputTypeMap.get(entry.getKey()));
	            }
				*/
      
				Set set = input_arg_map.entrySet();
				Iterator i = set.iterator();
								
				Map<String, String> ri_t = new HashMap<String, String> ();
				Map<String, String> ri_ut = new HashMap<String, String> ();
				Map<String, String> ri_f = new HashMap<String, String> ();
				
				Map<String, String> ri_files = new HashMap<String, String> ();
				
				while (i.hasNext()) {
					Map.Entry kv = (Map.Entry)i.next();
					String k = (String) kv.getKey();
					String v = (String) kv.getValue();
					
					try {
						String iv = (String) referenceService.renderIdentifier(inputs.get(k), String.class, context);
												
						//File f = new File(iv);
						 
						  //if(f.exists()){
						
						if (file_set.contains(k))
							ri_files.put(k, iv);
						
						if (untagged_set.contains(k))
							ri_ut.put(k, iv);
						else if (tagged_set.contains(k))
							ri_t.put(k, iv);
						else if (flag_set.contains(k))
							ri_f.put(k, iv);
					} catch (Exception e) {
						//e.printStackTrace();
					}
				}
				
				String cmdline = "";
				
				Set ri_t_set = ri_t.entrySet();
				Set ri_ut_set = ri_ut.entrySet();
				Set ri_f_set = ri_f.entrySet();
				
				Iterator ri_t_i = ri_t_set.iterator();
				Iterator ri_ut_i = ri_ut_set.iterator();
				Iterator ri_f_i = ri_f_set.iterator();
				
				Map.Entry ri_kv;
				
				String ri_k, ri_v, arg;
				
				while (ri_f_i.hasNext()) {
					ri_kv = (Map.Entry)ri_f_i.next();
					ri_k = (String) ri_kv.getKey();
					ri_v = (String) ri_kv.getValue();
					
					arg = input_arg_map.get(ri_k);
					cmdline += arg + " ";
				}	
								
				while (ri_t_i.hasNext()) {
					ri_kv = (Map.Entry)ri_t_i.next();
					ri_k = (String) ri_kv.getKey();
					ri_v = (String) ri_kv.getValue();
					
					arg = input_arg_map.get(ri_k);		
					
					if (file_set.contains(ri_k) && ri_v.indexOf('/') > -1) {
						int slash = ri_v.lastIndexOf('/') + 1;
						ri_files.put(ri_k, ri_v);
						ri_v = ri_v.substring(slash);
					}
					
					cmdline += arg + ri_v + " ";
				}
				
				while (ri_ut_i.hasNext()) {
					ri_kv = (Map.Entry)ri_ut_i.next();
					ri_k = (String) ri_kv.getKey();
					ri_v = (String) ri_kv.getValue();
					
					arg = input_arg_map.get(ri_k);
					
					if (file_set.contains(ri_k) && ri_v.indexOf('/') > -1) {
						int slash = ri_v.lastIndexOf('/') + 1;
						ri_files.put(ri_k, ri_v);
						ri_v = ri_v.substring(slash);
					}
					
					cmdline += arg + ri_v + " ";
				}
				
				if (basic_ws == true) {
					cmdline =  (String) referenceService.renderIdentifier(inputs.get("commandLine"), String.class, context);
					//numProcs =  (String) referenceService.renderIdentifier(inputs.get("numProcs"), String.class, context);
					
					if (inputs.get("numProcs") != null) {
						numProcs =  (String) referenceService.renderIdentifier(inputs.get("numProcs"), String.class, context);
					}

					inFiles = (String) referenceService.renderIdentifier(inputs.get("inFiles"), String.class, context);

					if (inFiles != null) {
						ArrayList<String> tokens = new ArrayList<String> ();
						Scanner tokenize = new Scanner(inFiles);
						
						int var = 0;
					
						while (tokenize.hasNext()) {
							ri_files.put(String.valueOf(var), tokenize.next());
							var++;
						}
					}
				}
							
				// Resolve inputs 				
				//String firstInput = (String) referenceService.renderIdentifier(inputs.get(IN_FIRST_INPUT), 
				//		String.class, context);
				
				// Support our configuration-dependendent input
				boolean optionalPorts = configBean.getExampleString().equals("specialCase"); 
				
				//List<byte[]> special = null;
				// We'll also allow IN_EXTRA_DATA to be optionally not provided
				//if (optionalPorts && inputs.containsKey(IN_EXTRA_DATA)) {
				//	// Resolve as a list of byte[]
				//	special = (List<byte[]>) referenceService.renderIdentifier(
				//			inputs.get(IN_EXTRA_DATA), byte[].class, context);
				//}

				//String opal_url = "http://ws.nbcr.net/opal2/services/Pdb2pqrOpalService";
				AppServiceLocator asl = new AppServiceLocator();	
				String jobID;
				String jobURL;
				StatusOutputType status;
				int code;
				
				try {
					Set ri_file_set = ri_files.entrySet();
					Iterator ri_file_i = ri_file_set.iterator();
					Map.Entry ri_file_kv;
					String ri_file_k, ri_file_v;
					Vector inputFileVector = new Vector();
					
					while (ri_file_i.hasNext()) {
						ri_file_kv = (Map.Entry)ri_file_i.next();
						ri_file_k = (String) ri_file_kv.getKey();
						ri_file_v = (String) ri_file_kv.getValue();
						
						DataHandler dh = new DataHandler(new FileDataSource(ri_file_v));
						InputFileType infile = new InputFileType();
						File f = new File(ri_file_v);
						infile.setName(f.getName());
						infile.setAttachment(dh);
						inputFileVector.add(infile);
					}
											
					int last_slash = url.lastIndexOf('/') + 1;
					String service_name = url.substring(last_slash);
					
					AppServicePortType appServicePort = asl.getAppServicePort(new java.net.URL(url));
					JobInputType in = new JobInputType();
					
					if (cmdline != null) {
					    System.out.println("\n*****[OPAL WS INFO] " + service_name + " is going to run with arguments:\n     " + cmdline);
					    in.setArgList(cmdline);
					}
					
					if (numProcs != null) {
					    System.out.println("\n*****[OPAL WS INFO] " + service_name + " is going to run with " + numProcs + " processors\n");
					    in.setNumProcs(new Integer(numProcs));
					}
					
		            int arraySize = inputFileVector.size();
		            if (arraySize > 0) {
		                InputFileType[] infileArray = new InputFileType[arraySize];
		                for (int k = 0; k < arraySize; k++) {
		                    infileArray[k] = (InputFileType) inputFileVector.get(k);
		                }
		                in.setInputFile(infileArray);
		            }

					JobSubOutputType subOut = appServicePort.launchJob(in);
					jobID = subOut.getJobID();
					status = appServicePort.queryStatus(jobID);
					code = status.getCode();
					
		            System.out.println("*****[OPAL WS INFO] Job output URL: " + status.getBaseURL());
					
					while (code != 4 && code != 8) {
						status = appServicePort.queryStatus(jobID);
						code = status.getCode();
						Thread.sleep(10000);						
					}
					
					if (code == 8) {
						JobOutputType out = appServicePort.getOutputs(jobID);
						OutputFileType [] outfile = out.getOutputFile();
						String outurls = "";

						System.out.println("*****[OPAL WS INFO] Job completed successfully.");
						
						String stdout = status.getBaseURL() + "/stdout.txt";
						String stderr = status.getBaseURL() + "/stderr.txt";

						outurls = stdout + ", " + stdout + ", ";
						
						if (outfile != null) {
							for (int j = 0; j < outfile.length; j++) {
							    // System.out.println(outfile[j].getName() + ": " + outfile[j].getUrl());
							    outurls += outfile[j].getUrl().toString() + ", ";
							}
						}

						outurls = outurls.substring(0, outurls.length() - 2);

						//String test = (outfile[0].getUrl()).toString();	
						String test = (outfile[0].getUrl()).toString();	
						
						T2Reference testvalue = referenceService.register(outurls, 0, true, context);
						//T2Reference testvalue = referenceService.register(test, 0, true, context);
						outputs.put(outport, testvalue);
						
						/*
						Set <String> outlist = new HashSet <String>();
						outlist.add("A");
						outlist.add("B");
						outlist.add("C");
						T2Reference tv = referenceService.register(outlist, 3, true, context);
						outputs.put(outport, tv);
						*/
					}
					else {
						System.out.println("*****[OPAL WS INFO] ERROR: Job failed.");
					}
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ServiceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FaultType e) {
					// TODO Auto-generated catch block
					e.printStackTrace(); 
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 

				
				
				// TODO: Do the actual service invocation
//				try {
//					results = this.service.invoke(firstInput, special)
//				} catch (ServiceException ex) {
//					callback.fail("Could not invoke Example service " + configBean.getExampleUri(),
//							ex);
//					// Make sure we don't call callback.receiveResult later 
//					return;
//				}

				// Register outputs
				/*
				Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
				String simpleValue = "simple222222";
				T2Reference simpleRef = referenceService.register(simpleValue, 0, true, context);
				*/
				//outputs.put(OUT_SIMPLE_OUTPUT, simpleRef);

				// For list outputs, only need to register the top level list
				/*
				List<String> moreValues = new ArrayList<String>();
				moreValues.add("Value 1");
				moreValues.add("Value 2");
				T2Reference moreRef = referenceService.register(moreValues, 1, true, context);
				outputs.put(OUT_MORE_OUTPUTS, moreRef);

				if (optionalPorts) {
					// Populate our optional output port					
					// NOTE: Need to return output values for all defined output ports
					String report = "Everything OK";
					outputs.put(OUT_REPORT, referenceService.register(report,
							0, true, context));
				}
				*/
				// return map of output data, with empty index array as this is
				// the only and final result (this index parameter is used if
				// pipelining output)
				callback.receiveResult(outputs, new int[0]);
			}
		});
	}

	@Override
	public ExampleActivityConfigurationBean getConfiguration() {
		return this.configBean;
	}

}
