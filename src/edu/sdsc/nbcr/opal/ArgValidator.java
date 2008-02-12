package edu.sdsc.nbcr.opal;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.regex.Pattern;

import java.io.File;
import java.net.URL;

import org.apache.log4j.Logger;

import org.apache.axis.types.Id;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;

import edu.sdsc.nbcr.common.TypeDeserializer;

/**
 * Implementation of a class that validates command-line arguments
 * based on the argument schema definition, within the WSDL
 * 
 * @author  Sriram Krishnan
 */

public class ArgValidator {

    private static Logger logger = 
	Logger.getLogger(ArgValidator.class.getName());

    // cache the description of arguments
    private ArgumentsType argDesc;

    /**
     * Default constructor
     *
     * @param argDesc_ argument description, parsed from XML desc
     */
    public ArgValidator(ArgumentsType argDesc_) {
	logger.info("called");

	argDesc = argDesc_;
    }


    /**
     * Validates the command line arguments
     *
     * @param workingDir the working direction for execution
     * @param args the command line arguments
     * @return true if validation was successful, false otherwise
     * @throws FaultType if there was an error during argument validation
     */
    public boolean validateArgList(String workingDir, 
				   String args)
	throws FaultType {
	logger.info("called");

	// retrieve the parameter types
	FlagsType[] flags = null;
	if (argDesc.getFlags() != null)
	    flags = argDesc.getFlags().getFlag();
	ParamsType[] taggedParams = null;
	if (argDesc.getTaggedParams() != null)
	    taggedParams = argDesc.getTaggedParams().getParam();
	ParamsType[] untaggedParams = null;
	if (argDesc.getUntaggedParams() != null)
	    untaggedParams = argDesc.getUntaggedParams().getParam();

	// separate the tags and their values
	String separator = null;
	if (argDesc.getTaggedParams() != null)
	    separator = argDesc.getTaggedParams().getSeparator();
	logger.debug("Separator used is: " + separator);
	if (separator != null) {
	    args = args.replaceAll(separator, " ");
	}
	logger.debug("Separated arguments: " + args);

	// split the arguments
	args = args.trim();
	String[] argList = args.split("[\\s]+");

	// now validate the individual arguments
	int index = 0;
	int untaggedIndex = 0;
	boolean untagged = false;
	HashSet present = new HashSet();

	while (index < argList.length) {
	    boolean match = false;
	    String current = argList[index];

	    logger.debug("args[" + index + "]: " + current);

	    // check if this is a valid flag
	    if ((flags != null) && (!untagged)) {
		for (int i = 0; i < flags.length; i++) {
		    String expr = flags[i].getTag();
		    logger.debug("Comparing with flag: " + expr);
		    match = Pattern.matches(expr, current);
		    if (match) {
			logger.debug("Flag " + current + " matches pattern: " + expr);
			Id id = flags[i].getId();
			present.add(id);
			break;
		    }
		}
		// if this was a valid flag, get the next parameter
		if (match) {
		    index++;
		    continue;
		}
	    }

	    // check if this is a valid tagged parameter
	    if ((taggedParams != null) && (!untagged)) {
		for (int i = 0; i < taggedParams.length; i++) {
		    String expr = taggedParams[i].getTag();
		    Id id = taggedParams[i].getId();

		    logger.debug("Comparing with tag: " + expr);
		    if (expr == null) {
			throw new FaultType("Tag missing for parameter with id: " + 
					    id + " in descriptor");
		    }

		    match = Pattern.matches(expr, current);
		    if (match) {
			logger.debug("Tag " + current + " matches pattern: " + expr);

			// check the validity of the parameter value
			index++;
			if (index == argList.length) {
			    String msg = "Too few arguments, can't find value for tag: " +
				current;
			    logger.error(msg);
			    throw new FaultType(msg);
			}

			current = argList[index];
			logger.debug("Received value: " + current + " for tag: " + expr);
			
			// check with enumerated values, if they exist
			String[] values = taggedParams[i].getValue();
			if (values != null) {
			    boolean found = false;
			    for (int j = 0; j < values.length; j++) {
				logger.debug("Comparing with value: " + values[j]);
				if (current.equals(values[j])) {
				    logger.debug("Value matches acceptable value: " +
						 values[j]);
				    found = true;
				    break;
				}
			    }
			    if (!found) {
				String msg = "Value: " + current + " not found on the list " +
				    "of acceptable values for id: " + id;
				logger.error(msg);
				throw new FaultType(msg);
			    }
			}

			// check type validity
			ParamType type = taggedParams[i].getParamType();
			if (type.getValue().equals(ParamType._INT)) {
			    try {
				Integer.parseInt(current);
				logger.debug("Value: " + current + " is of valid type INT");
			    } catch (Exception e) {
				logger.error(e);
				throw new FaultType("Parameter " + expr + " expects integer value");
			    }
			} else if (type.getValue().equals(ParamType._BOOL)) {
			    try {
				Boolean.valueOf(current);
				logger.debug("Value: " + current + " is of valid type BOOL");
			    } catch (Exception e) {
				logger.error(e);
				throw new FaultType("Parameter " + expr + " expects boolean value");
			    }
			} else if (type.getValue().equals(ParamType._FLOAT)) {
			    try {
				Float.parseFloat(current);
				logger.debug("Value: " + current + " is of valid type FLOAT");
			    } catch (Exception e) {
				logger.error(e);
				throw new FaultType("Parameter " + expr + " expects float value");
			    }
			} else if (type.getValue().equals(ParamType._STRING)) {
			    // this is always true
			} else if (type.getValue().equals(ParamType._FILE)) {
			    IOType ioType = taggedParams[i].getIoType();
			    if (ioType == null)
				ioType = new IOType("INPUT");
			    if (ioType.getValue().equals(IOType._INPUT) ||
				ioType.getValue().equals(IOType._INOUT)) {
				String filePath = workingDir + File.separator + current;
				File test = new File(filePath);
				if (test.exists()) {
				    logger.debug("Value: " + filePath + " is a valid FILE");
				} else {
				    logger.error("File parameter: " + filePath + " for tag " +
						 expr + " doesn't exist");
				    throw new FaultType("File parameter: " + filePath + " for tag " +
							expr + " doesn't exist");
				}
			    } else {
				logger.debug("Not checking existence of OUTPUT file: " + current);
			    }
			} else { // type.getValue().equals(ParamType._URL)
			    try {
				new URL(current);
				logger.debug("Value: " + current + " is a valid URL");
			    } catch (Exception e) {
				logger.error(e);
				throw new FaultType("Parameter " + expr + " expects a valid URL");
			    }
			}
			present.add(id);
			break;
		    }
		}

		// if this was a valid parameter, get the next parameter
		if (match) {
		    index++;
		    continue;
		}
	    }

	    // check if this is an untagged parameter in correct order
	    // since order matters, check only with the current untagged param
	    if (untaggedParams != null) {
		if (untaggedIndex < untaggedParams.length) {
		    Id id = untaggedParams[untaggedIndex].getId();
		    logger.debug("Checking if " + current + " is a valid untagged parameter" +
				 " for id: " + id);

		    // check with enumerated values, if they exist
		    String[] values = untaggedParams[untaggedIndex].getValue();
		    if (values != null) {
			boolean found = false;
			for (int j = 0; j < values.length; j++) {
			    logger.debug("Comparing with value: " + values[j]);
			    if (current.equals(values[j])) {
				logger.debug("Value matches acceptable value: " +
					     values[j] + " for id: " + id);
				found = true;
				break;
			    }
			}
			if (!found) {
			    String msg = "Value: " + current + " not found on the list " +
				"of acceptable values for id: " + id;
			    logger.error(msg);
			    throw new FaultType(msg);
			}
		    }

		    // check type validity
		    ParamType type = untaggedParams[untaggedIndex].getParamType();
		    if (type.getValue().equals(ParamType._INT)) {
			try {
			    Integer.parseInt(current);
			    logger.debug("Value: " + current + " is of valid type INT");
			} catch (Exception e) {
			    logger.error(e);
			    throw new FaultType("Parameter " + id + " expects integer value");
			}
		    } else if (type.getValue().equals(ParamType._BOOL)) {
			try {
			    Boolean.valueOf(current);
			    logger.debug("Value: " + current + " is of valid type BOOL");
			} catch (Exception e) {
			    logger.error(e);
			    throw new FaultType("Parameter " + id + " expects boolean value");
			}
		    } else if (type.getValue().equals(ParamType._FLOAT)) {
			try {
			    Float.parseFloat(current);
			    logger.debug("Value: " + current + " is of valid type FLOAT");
			} catch (Exception e) {
			    logger.error(e);
			    throw new FaultType("Parameter " + id + " expects float value");
			}
		    } else if (type.getValue().equals(ParamType._STRING)) {
			// this is always true
		    } else if (type.getValue().equals(ParamType._FILE)) {
			IOType ioType = untaggedParams[untaggedIndex].getIoType();
			if (ioType == null)
			    ioType = new IOType("INPUT");
			if (ioType.getValue().equals(IOType._INPUT) ||
			    ioType.getValue().equals(IOType._INOUT)) {
			    String filePath = workingDir + File.separator + current;
			    File test = new File(filePath);
			    if (test.exists()) {
				logger.debug("Value: " + filePath + " is a valid FILE");
			    } else {
				logger.error("File parameter: " + filePath + " for id: " +
					     id + " doesn't exist");
				throw new FaultType("File parameter: " + filePath + "for id: " +
						    id + " doesn't exist");
			    }
			} else {
			    logger.debug("Not checking existence of OUTPUT file: " + current);
			}
		    } else { // type.getValue().equals(ParamType._URL)
			try {
			    new URL(current);
			    logger.debug("Value: " + current + " is a valid URL");
			} catch (Exception e) {
			    logger.error(e);
			    throw new FaultType("Parameter " + id + " expects a valid URL");
			}
		    }
		    
		    // if this is the right type and/or matches list of possible values,
		    // there is a match for an untagged parameter
		    logger.debug(current + " is a valid untagged parameter for id: " + id);
		    present.add(id);
		    match = true;

		    // set up for next iteration
		    untaggedIndex++;
		    untagged = true;
		    index++;
		    continue;
		} else {
		    String msg = "Ran out of untagged paramters to check against";
		    logger.error(msg);
		    throw new FaultType(msg);
		}
	    }

	    // make sure that there is a match
	    if (!match) {
		logger.error("No match found for argument: " + current);
		throw new FaultType("No match found for argument: " + current);
	    }
	}

	// check if all required arguments are present
	if (taggedParams != null) {
	    for (int i = 0; i < taggedParams.length; i++) {
		Id id = taggedParams[i].getId();
		boolean required = false;		
		if (taggedParams[i].getRequired() != null) {
		    required = taggedParams[i].getRequired().booleanValue();
		}
		if ((required) && (!present.contains(id))) {
		    logger.error("Required parameter " + id + " not found");
		    throw new FaultType("Required parameter " + id + " not found");
		}
	    }
	}
	if (untaggedParams != null) {
	    for (int i = 0; i < untaggedParams.length; i++) {
		Id id = untaggedParams[i].getId();
		boolean required = false;
		if (untaggedParams[i].getRequired() != null) {
		    required = untaggedParams[i].getRequired().booleanValue();
		}
		if ((required) && (!present.contains(id))) {
		    logger.error("Required parameter " + id + " not found");
		    throw new FaultType("Required parameter " + id + " not found");
		}
	    }
	}

	return true;
    }

    /**
     * Checks to see if all the implicit parameters are present
     *
     * @param workingDir the working direction for execution
     * @return true if all implicit params are present, false otherwise
     * @throws FaultType if there in an error during implicit param validation
     */
    public boolean validateImplicitParams(String workingDir)
	throws FaultType {
	logger.info("called");

	File dir = new File(workingDir);
	if (!dir.exists()) {
	    logger.error("Working directory " + workingDir +
			 " does not exist");
	    throw new FaultType("Working directory " + workingDir +
				" does not exist");
	}

	if (!dir.isDirectory()) {
	    logger.error("Specified working directory " + workingDir + 
			 " is not a valid directory");
	    throw new FaultType("Specified working directory " + workingDir + 
				" is not a valid directory");
	}

	ImplicitParamsType[] params = null;
	if (argDesc.getImplicitParams() != null)
	    params = argDesc.getImplicitParams().getParam();
	if (params == null) {
	    logger.debug("No implicit parameters to verify against");
	    return true;
	}

	// hash table to store implicit parameters that are present
	Hashtable present = new Hashtable();

	// match all existing file to the implicit parameters
	File[] files = dir.listFiles();
	for (int i = 0; i < files.length; i++) {
	    String fileName = files[i].getName();
	    logger.debug("Trying to find a match for file: " + fileName);
	    boolean match = false;

	    for (int j = 0; j < params.length; j++) {

		// check if file extension is specified
		String ext = params[j].getExtension();
		if (ext != null) {
		    if (fileName.indexOf(ext) != -1) {
			// found a file with this extension
			if (present.containsKey(params[j].getId())) {
			    // found one earlier already
			    Integer val = (Integer) present.get(params[j].getId());
			    present.remove(params[j].getId());
			    present.put(params[j].getId(), new Integer(val.intValue()+1));
			} else {
			    // first time
			    present.put(params[j].getId(), new Integer(1));
			}

			// found a match
			logger.debug("File " + fileName + " matches implicit parameter: " +
				     params[j].getId());
			match = true;
			break;
		    }
		}

		// check if file name is specified
		String regex = params[j].getName();
		if (regex != null) {
		    if (fileName.matches(regex)) {
			// found a file that matches the name
			if (present.containsKey(params[j].getId())) {
			    // found one earlier already
			    Integer val = (Integer) present.get(params[j].getId());
			    present.remove(params[j].getId());
			    present.put(params[j].getId(), new Integer(val.intValue()+1));
			} else {
			    // first time
			    present.put(params[j].getId(), new Integer(1));
			}

			// found a match
			logger.debug("File " + fileName + " matches implicit parameter: " +
				     params[j].getId());
			match = true;
			break;
		    }
		}
	    }

	    if (!match) {
		logger.debug("Unable to find a match for file: " + fileName);
	    }
	}
	
	// make sure that required implicit parameters are present
	for (int i = 0; i < params.length; i++) {
	    if (params[i].getRequired().booleanValue()) {
		if (present.containsKey(params[i].getId())) {
		    logger.debug("Implicit parameter " + params[i].getId() + " present");
		} else {
		    logger.error("Required implicit parameter " + params[i].getId() + 
				 " missing from working directory");
		    throw new FaultType("Required implicit parameter " + params[i].getId() + 
					" missing from working directory");
		}
	    }

	    if (params[i].getMin() != null) {
		int min = params[i].getMin().intValue();
		int num = 0;
		if (present.containsKey(params[i].getId())) {
		    num = ((Integer) present.get(params[i].getId())).intValue();
		} 

		if (num < min) {
		    logger.error("Number of implicit parameters " + params[i].getId() + 
				 " less than minimum (" + min + ")");
		    throw new FaultType("Number of implicit parameters " + params[i].getId() + 
					" less than minimum (" + min + ")");
		} else {
		    logger.debug("Number of implicit parameters " + params[i].getId() +
				 " greater than required minimum");
		}
	    }

	    if (params[i].getMax() != null) {
		int max = params[i].getMax().intValue();
		int num = 0;
		if (present.containsKey(params[i].getId())) {
		    num = ((Integer) present.get(params[i].getId())).intValue();
		} 

		if (num > max) {
		    logger.error("Number of implicit parameters " + params[i].getId() + 
				 " greater than maximum (" + max + ")");
		    throw new FaultType("Number of implicit parameters " + params[i].getId() + 
					" greater than maximum (" + max + ")");
		} else {
		    logger.debug("Number of implicit parameters " + params[i].getId() +
				 " less than required maximum");
		} 
	    }
	}

	return true;
    }

    /**
     * Main method to run validation of command-line arguments, given the 
     * argument schema
     *
     * <p>Run <i>java edu.sdsc.nbcr.opal.ArgValidator</i> for usage information
     */
    public static void main(String args[])
	throws Exception {

	String configFile = null;
	String workingDir = null;
	String cmdArgs = null;

	Options options = new Options();
	options.addOption(OptionBuilder.withArgName("config")
			  .isRequired()
			  .withDescription("application configuration XML")
			  .hasArg()
			  .create("d"));
	options.addOption(OptionBuilder.withArgName("dir")
			  .isRequired()
			  .withDescription("working directory for execution")
			  .hasArg()
			  .create("w"));
	options.addOption(OptionBuilder.withArgName("args")
			  .isRequired()
			  .withDescription("command line arguments")
			  .hasArg()
			  .create("a"));

	//	options.addOption("d", "desc", true, "argument descriptor XML" );
	//	options.addOption("w", "dir", true, "working director for execution" );
	//	options.addOption("a", "args", true, "command line arguments");

	System.out.println("Reading command line arguments");
	CommandLineParser parser = new GnuParser();
	CommandLine line = null;
	try {
	    line = parser.parse(options, args);
	} catch (Exception e) {
	    System.err.println(e.toString());
	    HelpFormatter formatter = new HelpFormatter();
	    formatter.printHelp("java edu.sdsc.nbcr.opal.ArgValidator", options);
	    System.exit(1);
	}

	configFile = line.getOptionValue("d");
	workingDir = line.getOptionValue("w");
	cmdArgs = line.getOptionValue("a");

	System.out.println("Parsing argument description");
	AppConfigType appConfig =
	    (AppConfigType) TypeDeserializer.getValue(configFile,
						      new AppConfigType());
	ArgumentsType argsDesc_ = appConfig.getMetadata().getTypes();
	if (argsDesc_ == null) {
	    System.err.println("Opal configuration file: " + configFile +
			       " does not include argument description");
	    System.exit(1);
	}

	ArgValidator av = new ArgValidator(argsDesc_);

	System.out.println("Validating arguments");
	boolean success = av.validateArgList(workingDir, cmdArgs);
	if (success)
	    System.out.println("Argument validation successful");
	else
	    System.err.println("Argument validation unsuccessful");

	System.out.println("Validating presence of implicit parameters");
	success = av.validateImplicitParams(workingDir);
	if (success)
	    System.out.println("Validation of implicit parameters successful");
	else
	    System.err.println("Validation of implicit parameters unsuccessful");
    }
}
