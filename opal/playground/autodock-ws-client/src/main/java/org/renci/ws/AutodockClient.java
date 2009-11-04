package org.renci.ws;

import static org.apache.axis2.Constants.TRANSPORT_HTTPS;
import static org.apache.axis2.Constants.VALUE_TRUE;
import static org.apache.axis2.Constants.Configuration.ENABLE_MTOM;
import static org.apache.axis2.transport.http.HTTPConstants.CONNECTION_TIMEOUT;
import static org.apache.axis2.transport.http.HTTPConstants.SO_TIMEOUT;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.util.Base64;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.io.FileUtils;

/**
 * 
 * @author jdr0887
 * @author sriram (minor updates)
 */
public class AutodockClient implements Runnable {

    private static final EndpointReference ENDPOINT = new EndpointReference(
            "https://irrawaddy.renci.org:8443/synch/services/AutodockBasicService");

    // private static final EndpointReference ENDPOINT = new
    // EndpointReference("https://irrawaddy.renci.org:8443/asynch/services/AutodockBasicService");

    // private static final EndpointReference ENDPOINT = new
    // EndpointReference("https://irrawaddy.renci.org:8443/asynch/services/AutodockAdvancedService");

    private String username;
    private String password;
    private String dpfFile;
    private String mapZipFile;
    private String outputDir;

    public AutodockClient(String username,
			  String password,
			  String dpfFile,
			  String mapZipFile,
			  String outputDir) {
        super();

	// initialize variables
	this.username = username;
	this.password = password;
	this.dpfFile = dpfFile;
	this.mapZipFile = mapZipFile;
	this.outputDir = outputDir;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {

        OMFactory fac = org.apache.axiom.om.OMAbstractFactory.getOMFactory();
        OMNamespace ns = fac.createOMNamespace("http://basic.autodock.ws.sp.renci.org", "ns");
        OMElement applicationService = fac.createOMElement("runAutodockBasic", ns);

        OMElement requestElement = fac.createOMElement("request", null);

        OMElement usernameElement = fac.createOMElement("username", null);
        usernameElement.addChild(fac.createOMText(usernameElement, username));
        requestElement.addChild(usernameElement);

        OMElement passwordElement = fac.createOMElement("password", null);
        passwordElement.addChild(fac.createOMText(passwordElement, Base64.encode(password.getBytes())));
        requestElement.addChild(passwordElement);

        File parameterFile = new File(dpfFile);
        OMElement parameterFileElement = fac.createOMElement("parameterFile", null);
        parameterFileElement.addAttribute(fac.createOMAttribute("filename", null, parameterFile.getName()));
        try {
            parameterFileElement.addChild(fac.createOMText(parameterFileElement, FileUtils
                    .readFileToString(parameterFile)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        requestElement.addChild(parameterFileElement);
					  
	OMElement mapZipFileElement = fac.createOMElement("mapZipFile", null);
	DataHandler dataHandler = new DataHandler(new FileDataSource(new File(mapZipFile)));
	mapZipFileElement.addAttribute(fac.createOMAttribute("filename", null, dataHandler.getName()));
        OMText textData = fac.createOMText(dataHandler, true);
        mapZipFileElement.addChild(textData);
        requestElement.addChild(mapZipFileElement);

        // @InputParam(name = "keepResidueNumbers", flag = "-k")
        // @InputParam(name = "ignoreHeader", flag = "-i")
        // @InputParam(name = "parsePDBQTFile", flag = "-t")
        // @InputParam(name = "incrementDebugLevel", flag = "-d")
        // InputType.CHECKBOX, order = 4)

        OMElement parametersElement = fac.createOMElement("parameters", null);
        parametersElement.addChild(fac.createOMText(parametersElement, "keepResidueNumbers=true,ignoreHeader=true"));
        requestElement.addChild(parametersElement);

        applicationService.addChild(requestElement);

        Options options = new Options();
        options.setTo(ENDPOINT);
        options.setTransportInProtocol(TRANSPORT_HTTPS);
        options.setProperty(SO_TIMEOUT, 1800000);
        options.setProperty(CONNECTION_TIMEOUT, 1800000);
        options.setProperty(ENABLE_MTOM, VALUE_TRUE);

        try {
            ServiceClient sender = new ServiceClient();
            sender.setOptions(options);
            OMElement resultsElement = sender.sendReceive(applicationService);

	    OMElement returnElement = resultsElement.getFirstElement();
	    
	    OMElement responseElement = returnElement.getFirstElement();
	  
	    Iterator<OMElement> childElements = responseElement.getChildElements();

	    while (childElements.hasNext()) {

		OMElement e = childElements.next();
		File f = new File(outputDir, e.getLocalName());
		try {
		    FileUtils.writeStringToFile(f, e.getText());
		} catch (IOException e1) {
		    e1.printStackTrace();
		}
	    }

	    System.out.println("Remote execution complete");
        } catch (AxisFault e1) {
            e1.printStackTrace();
        }

    }

    public static void main(String[] args) {

	if (args.length != 5) {
	    System.out.println("java org.renci.ws.AutodockClient " + 
			       "<username> <password> <dpf_path> " + 
			       "<map_zipfiles_path> <output_dir>");
	    System.exit(1);
	}
        Thread t = new Thread(new AutodockClient(args[0],
						 args[1],
						 args[2],
						 args[3],
						 args[4]));
        t.start();
    }
}
