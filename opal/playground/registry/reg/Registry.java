import org.hibernate.Session;
import org.hibernate.Query;
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.HibernateException;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Stack;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;

import edu.sdsc.nbcr.opal.*;
import edu.sdsc.nbcr.opal.gui.common.*;

import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.client.Call;
import org.apache.axis.client.AxisClient;
import org.apache.axis.SimpleTargetedChain;
import org.apache.axis.configuration.SimpleProvider;
import org.apache.axis.description.TypeDesc;
import org.apache.axis.encoding.SerializationContext;
import org.apache.axis.MessageContext;


public class Registry {
    public static void main(String[] args) {
	String hostfile = "hosts.txt";
	String [] hosts = null; 

	while (true) {
	    hosts = getHostList(hostfile);
	    refreshHosts(hosts);
	    makeRegistryXML("/var/www/html/registry/registry.xml");
	    System.out.println("Going to sleep for 30 seconds");

	    try {
		Thread.sleep(30000);
	    }
	    catch (Exception e) {
		e.printStackTrace();
	    }
	}
    }

    public static String [] getHostList(String hostfile) {
	Set <String> hostset = new HashSet<String>();
	File file = new File(hostfile);
	FileInputStream fis = null;
	BufferedInputStream bis = null;
	DataInputStream dis = null;
	String line;

	try {
	    fis = new FileInputStream(file);
	    bis = new BufferedInputStream(fis);
	    dis = new DataInputStream(bis);

	    while (dis.available() != 0) {
		line = dis.readLine();
		
		if (line.indexOf("opal") != -1)
		    hostset.add(line);
	    }

	    fis.close();
	    bis.close();
	    dis.close();
	    
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}

	String [] hosts = new String[hostset.size()];
	Iterator it = hostset.iterator();
	int i = 0;

	while (it.hasNext()) {
	    hosts[i] = (String)it.next();
	    i++;
	}	
	
	return hosts;
    }


    public static String getInfo(String sws, String query) {
	int pos;
	String tmp;
	
	if (query != "numCpuTotal" && query != "numCpuFree" && query != "numJobsRunning" && query != "numJobsQueued") {
	    System.out.println("ERROR: Invalid query in getInfo()");
	    return "error";
	}	    

	pos = sws.indexOf("<" + query);
	tmp = sws.substring(pos);
	pos = tmp.indexOf(">");
	tmp = tmp.substring(pos+1);
	pos = tmp.indexOf("<");
	tmp = tmp.substring(0, pos);

	return tmp;
    }

    public static String getSystemInfo(String url) {
	StringWriter sw = new StringWriter();

	try {
	    AppServiceLocator asl = new AppServiceLocator();
	    AppServicePortType appServicePort = asl.getAppServicePort(new java.net.URL(url));
	    SystemInfoType sit = appServicePort.getSystemInfo(new SystemInfoInputType());

	    
            TypeDesc typeDesc = sit.getTypeDesc();
            MessageContext mc = new MessageContext(new AxisClient());
            SerializationContext sc = new SerializationContext(sw, mc);
            sc.setDoMultiRefs(false);
            sc.setPretty(true);
            sc.serialize(typeDesc.getXmlType(),
                         null,
                         sit,
                         typeDesc.getXmlType(),
                         new Boolean(true),
                         new Boolean(true));
            sw.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}

	return sw.toString();
    }

    public static Set<String> getServices (String url) {
	Set <String> services = new HashSet<String>();

	try {
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

		while ((s = dis.readLine()) != null) {
		    if (s.contains("?wsdl")) {
			fpos = s.indexOf("\"") + 1;
			lpos = s.indexOf("?");
			s = s.substring(fpos, lpos);
                                                
			if (s.startsWith("http"))
			    s = s.substring(7);
                                                
			lslash = s.lastIndexOf('/');
			sn = s.substring(lslash + 1);                                   
                                                                                                        
			if (!sn.equals("Version") && !sn.equals("AdminService")) 
			    services.add(url + "/" + sn + " ");
		    }
		}
                            
		dis.close();
	    } catch (IOException e) {
		e.printStackTrace();
	    }                                   
                        
	} catch (MalformedURLException e1) {
	    e1.printStackTrace();
	}

	return services;
    }

    public static String setToStr(Set <String> s) {
	Iterator it = s.iterator();
	String str = "";

	while (it.hasNext()) {
	    str += it.next() + " ";
	}

	return str;
    }

    public static void refreshHosts(String [] hosts) {
	Session session = null;
	Transaction tx = null;
	Iterator it, it2;

	Map <String, Set<String>> hsm = new HashMap<String, Set<String>>();
	Set <String> dh = new HashSet<String>();
	Set s;
	int numCpuTotal;
	int numCpuFree;
	int numJobsRunning;
	int numJobsQueued;

	for (int i = 0; i < hosts.length; i++) {
	    Set services = getServices(hosts[i]);
	    hsm.put(hosts[i], services);
	}

	s = hsm.entrySet();
	it = s.iterator();

	while (it.hasNext()) {
	    Map.Entry kv = (Map.Entry)it.next();
	    String k = (String) kv.getKey();
	    Set<String> v = (Set<String>) kv.getValue();
	}

	try{ 
	    SessionFactory sessionFactory = new 
		Configuration().configure().buildSessionFactory();
	    session = sessionFactory.openSession();
	    tx = session.beginTransaction();

	    List dl = session.createQuery("from Host").list();

	    it = dl.iterator();
	    
	    while (it.hasNext()) {
		Host h = (Host)it.next();
		String hn = h.getName();
		String hs;

		dh.add(hn);

		if (hsm.containsKey(hn)) {
		    hs = hsm.get(hn).iterator().next();
		    
		    String si = getSystemInfo(hs);
		    numCpuTotal = Integer.parseInt(getInfo(si, "numCpuTotal"));
		    numCpuFree = Integer.parseInt(getInfo(si, "numCpuFree"));
		    numJobsRunning = Integer.parseInt(getInfo(si, "numJobsRunning"));
		    numJobsQueued = Integer.parseInt(getInfo(si, "numJobsQueued"));
		    
		    h.setNumCpuTotal(numCpuTotal);
		    h.setNumCpuFree(numCpuFree);		
		    h.setNumJobsRunning(numJobsRunning);
		    h.setNumJobsQueued(numJobsQueued);
		    h.setServices(setToStr(hsm.get(hn)));

		    session.update(h);
		}
		else 
		    session.delete(h);
	    }

	    s = hsm.entrySet();
	    it = s.iterator();
	    
	    while (it.hasNext()) {
		Map.Entry kv = (Map.Entry)it.next();
		String k = (String) kv.getKey();
		Set<String> v = (Set<String>) kv.getValue();
		String hs = v.iterator().next();
		Host h = new Host();

		if (!(dh.contains(k))) {
		    String si = getSystemInfo(hs);
		    numCpuTotal = Integer.parseInt(getInfo(si, "numCpuTotal"));
		    numCpuFree = Integer.parseInt(getInfo(si, "numCpuFree"));
		    numJobsRunning = Integer.parseInt(getInfo(si, "numJobsRunning"));
		    numJobsQueued = Integer.parseInt(getInfo(si, "numJobsQueued"));
		    
		    h.setName(k);
		    h.setNumCpuTotal(numCpuTotal);
		    h.setNumCpuFree(numCpuFree);		
		    h.setNumJobsRunning(numJobsRunning);
		    h.setNumJobsQueued(numJobsQueued);
		    h.setServices(setToStr(hsm.get(k)));

		    session.save(h);
		}
	    }

	    tx.commit();
	    session.flush(); 
	} catch (HibernateException hne) {
	    throw new ExceptionInInitializerError(hne);
	}catch(Exception e){
	    e.printStackTrace();
	} finally {
	    if(session != null){
		session.flush();
		session.close();
	    }

	}
    }

    public static void makeRegistryXML (String filename) {
	String name_db, services_db;
	int numCpuTotal_db, numCpuFree_db, numJobsRunning_db, numJobsQueued_db;
	Element host, name, numCpuTotal, numCpuFree, numJobsRunning, numJobsQueued, services;
	Session session = null;

        try{
 	    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 
	    Document doc = docBuilder.newDocument();
	    Element rootElement = doc.createElement("hosts");
	    doc.appendChild(rootElement);

            SessionFactory sessionFactory = new
                Configuration().configure().buildSessionFactory();
            session = sessionFactory.openSession();

            List dl = session.createQuery("from Host").list();
	    
	    Iterator it = dl.iterator();
	    
	    while (it.hasNext()) {
		Host h = (Host)it.next();
		name_db = h.getName();
		numCpuTotal_db = h.getNumCpuTotal();
		numCpuFree_db = h.getNumCpuFree();
		numJobsRunning_db = h.getNumJobsRunning();
		numJobsQueued_db = h.getNumJobsQueued();
		services_db = h.getServices();

		host = doc.createElement("host");
		rootElement.appendChild(host);

		name = doc.createElement("name");
		name.appendChild(doc.createTextNode(name_db));
		host.appendChild(name);
 
		numCpuTotal = doc.createElement("numCpuTotal");
		numCpuTotal.appendChild(doc.createTextNode(String.valueOf(numCpuTotal_db)));
		host.appendChild(numCpuTotal);

		numCpuFree = doc.createElement("numCpuFree");
		numCpuFree.appendChild(doc.createTextNode(String.valueOf(numCpuFree_db)));
		host.appendChild(numCpuFree);

		numJobsRunning = doc.createElement("numJobsRunning");
		numJobsRunning.appendChild(doc.createTextNode(String.valueOf(numJobsRunning_db)));
		host.appendChild(numJobsRunning);

		numJobsQueued = doc.createElement("numJobsQueued");
		numJobsQueued.appendChild(doc.createTextNode(String.valueOf(numJobsQueued_db)));
		host.appendChild(numJobsQueued);

	        services = doc.createElement("services");
		services.appendChild(doc.createTextNode(services_db));
		host.appendChild(services);
	    }

	    TransformerFactory transformerFactory = TransformerFactory.newInstance();
	    Transformer transformer = transformerFactory.newTransformer();
	    DOMSource source = new DOMSource(doc);
	    StreamResult result =  new StreamResult(new File(filename));
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
	    transformer.transform(source, result);
        } catch (HibernateException hne) {
            throw new ExceptionInInitializerError(hne);
	} catch(ParserConfigurationException pce) {
	    pce.printStackTrace();
	} catch(TransformerException tfe){
	    tfe.printStackTrace();
	} catch(Exception e){
	    e.printStackTrace();
	} finally {
	    if(session != null)
		session.close();
	}
    } 
}