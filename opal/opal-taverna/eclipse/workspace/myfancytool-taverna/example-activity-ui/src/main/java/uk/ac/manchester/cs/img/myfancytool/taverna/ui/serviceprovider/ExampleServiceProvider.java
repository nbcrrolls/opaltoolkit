package uk.ac.manchester.cs.img.myfancytool.taverna.ui.serviceprovider;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;
import javax.xml.rpc.ServiceException;

import net.sf.taverna.t2.servicedescriptions.AbstractConfigurableServiceProvider;
import net.sf.taverna.t2.servicedescriptions.ConfigurableServiceProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;

import edu.sdsc.nbcr.opal.*;
import edu.sdsc.nbcr.opal.gui.common.GetServiceListHelper;
import edu.sdsc.nbcr.opal.gui.common.OPALService;

import org.apache.axis.message.SOAPBodyElement;


public class ExampleServiceProvider extends
	AbstractConfigurableServiceProvider<ExampleServiceProviderConfig> implements
	ConfigurableServiceProvider<ExampleServiceProviderConfig> {

//public class ExampleServiceProvider implements ServiceDescriptionProvider {
		
	private static final URI providerId = URI
		.create("http://example.com/2010/service-provider/example-activity-ui");
	
	
	/**
	 * Do the actual search for services. Return using the callBack parameter.
	 */
	@SuppressWarnings("unchecked")
	public void findServiceDescriptionsAsync(
			FindServiceDescriptionsCallBack callBack) {
		// Use callback.status() for long-running searches
		// callBack.status("Resolving example services");
		String url;
		boolean url_valid = true;
		
        URI url_uri = getConfiguration().getUri();
        url = url_uri.toString();
        
		// url = "http://ws.nbcr.net/opal2/services";
	        
		URLConnection urlConn_test;
		
		try {
			urlConn_test = (new URL(url)).openConnection();
		} catch (MalformedURLException e2) {
			// TODO Auto-generated catch block
			url_valid = false;
			e2.printStackTrace();
			System.out.println("ERROR: Bad Opal service URL entered:" + url);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			url_valid = false;
			e2.printStackTrace();
			System.out.println("ERROR: Bad Opal service URL entered:" + url);
		}
        
        //if (url_uri != null && url_uri.toString().contains("/opal2/services") && url_valid == true) {
		if (url_uri != null && url_valid == true) {
			System.out.println("URL entered: " + url_uri);
        	url = url_uri.toString();
        
		
		List<ServiceDescription> results = new ArrayList<ServiceDescription>();
	
		
//		System.out.println("\n\n\n\n\n\nUUUUU=\n\n\n\n\n" + url);
		
		/*
        GetServiceListHelper servicelist = new GetServiceListHelper();
        servicelist.setBasePrivateURL(url);
        servicelist.setBasePublicURL(url);

        SOAPBodyElement list = servicelist.getServiceList();

        OPALService [] serviceList = servicelist.parseServiceList(list.toString()); 
        */
		
        /*
        for (int i = 0; i < serviceList.length; i++){
            //System.out.println("the service " + i + " is: " + serviceList[i]);
			ExampleServiceDesc service = new ExampleServiceDesc();
			// Populate the service description bean
			service.setExampleString(serviceList[i].getURL());
			service.setExampleUri(URI.create(url));

			// Optional: set description
			service.setDescription("Service example number " + i);
			results.add(service);
        }
        */

		// FIXME: Implement the actual service search/lookup instead
		// of dummy for-loop
        
		try {
			//url = "http://ws.nbcr.net/opal2/services";
			URL ws_url = new URL(url);
			URLConnection urlConn;
			DataInputStream dis;
			
		    try {
				urlConn = ws_url.openConnection();
				urlConn.setDoInput(true); 
			    urlConn.setUseCaches(false);
			    dis = new DataInputStream(urlConn.getInputStream()); 
			    String s;
			    int fpos = 0;
			    int lpos;
			    int lslash;
			    String sn;
			    String hi;
			    
			    while ((s = dis.readLine()) != null) {
					if (s.contains("?wsdl")) {
			    		fpos = s.indexOf("\"") + 1;
						lpos = s.indexOf("?");
						s = s.substring(fpos, lpos);
						
						if (s.startsWith("http"))
							s = s.substring(7);
						
						lslash = s.lastIndexOf('/');
						sn = s.substring(lslash + 1);					
						hi = s.substring(0, lslash);
						hi = hi.replace('/', '_');
						//hi = hi.replace('.', '_');
						//sn = sn.replace('.', '_');
													
						if (!sn.equals("Version") && !sn.equals("AdminService")) {
							ExampleServiceDesc service = new ExampleServiceDesc();
							// Populate the service description bean
							s = sn + "_from_" + hi; 
							service.setExampleString(s);
							service.setExampleUri(URI.create(url));

							// Optional: set description
							//service.setDescription("Service example number ");
							//service.setDescription(s);
							results.add(service);
						}
					}
			    }
			    
			    dis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 				    
			
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
        
		/*
		for (int i = 1; i <= 5; i++) { 
			ExampleServiceDesc service = new ExampleServiceDesc();
			// Populate the service description bean
			service.setExampleString("PDB2PQR " + i);
			service.setExampleUri(URI.create(url));

			// Optional: set description
			service.setDescription("Service example number " + i);
			results.add(service);
		}
		*/
		

		// partialResults() can also be called several times from inside
		// for-loop if the full search takes a long time
		callBack.partialResults(results);

		// No more results will be coming
		callBack.finished();
        }
	}

	/**
	 * Icon for service provider
	 */
	public Icon getIcon() {
		return ExampleServiceIcon.getIcon();
	}

	/**
	 * Name of service provider, appears in right click for 'Remove service
	 * provider'
	 */
	public String getName() {
		return "Opal Web Service URL";
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	public String getId() {
		return providerId.toASCIIString();
	}

	@Override
	protected List<? extends Object> getIdentifyingData() {
		// TODO Auto-generated method stub
		return Arrays.asList(getConfiguration().getUri());
		//return null;
	}

	public ExampleServiceProvider() {
	    super(new ExampleServiceProviderConfig());
	}
}
