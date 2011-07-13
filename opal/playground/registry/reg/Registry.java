import org.hibernate.Session;
import org.hibernate.Query;
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.HibernateException;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
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
    public static String registry_path = 
	"/var/www/html/registry/registry.xml";

    private static SessionFactory sessionFactory = null;
    private static String confFile = "hibernate.cfg.xml";

    private static SessionFactory buildSessionFactory() {
        try {
            Configuration conf = new Configuration();
            conf = conf.configure(confFile);
            SessionFactory sessionFactoryTemp = conf.buildSessionFactory();
            return sessionFactoryTemp;
        } catch (HibernateException ex) {
            System.out.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static void setConfFile(String conf){
        confFile = conf;
    }

    public static SessionFactory getSessionFactory() {
        if ( sessionFactory == null ) {
            sessionFactory = buildSessionFactory();
        }
        return sessionFactory;
    }


    public static String [] siv = 
        {"numCpuTotal", "numCpuFree", "numJobsRunning", "numJobsQueued"};

    public static void main(String[] args) {
	String hostfile = "hosts.txt";
	String [] hosts = null; 

	while (true) {
	    hosts = getHostList(hostfile);

	    for (int i = 0; i < hosts.length; i++)
		updateHostInfo(hosts[i]);

	    makeRegistryXML(registry_path);

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
		
		if (line.indexOf("opal") != -1 && line.indexOf("#") == -1)
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

    public static int [] getSystemInfo(String url) {
	StringWriter sw = new StringWriter();
	String sws, tmp;
	int pos;
	int [] si = new int[siv.length];

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

	sws = sw.toString();

	for (int i = 0; i < siv.length; i++) {
	    pos = sws.indexOf("<" + siv[i]);
	    tmp = sws.substring(pos);
	    pos = tmp.indexOf(">");
	    tmp = tmp.substring(pos+1);
	    pos = tmp.indexOf("<");
	    tmp = tmp.substring(0, pos);
	    si[i] = Integer.parseInt(tmp);
	}

	return si;
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
			    services.add(url + "/" + sn);
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

    public static void updateHostInfo(String host) {	
	int numCpuTotal = 0;
	int numCpuFree = 0; 
	int numJobsRunning = 0;
	int numJobsQueued = 0;
	
	Session session = null;
	Transaction tx = null;
	
	Set services = getServices(host);
	Iterator sit = services.iterator();
	
	if (sit.hasNext()) {
	    String sn = (String)sit.next();
	    int [] si = getSystemInfo(sn);
	    numCpuTotal = si[0];
	    numCpuFree = si[1];
	    numJobsRunning = si[2];
	    numJobsQueued = si[3];
	}

	try { 
	    session = getSessionFactory().openSession();
	    session.beginTransaction();

	    List dl =
		session.createQuery("select url from OpalService where host="+"\'"+host+"\'").list();
	    Iterator dit = dl.iterator();
	    sit = services.iterator();

	    List dul = new ArrayList();
	    Iterator dit2 = dl.iterator();

	    while (dit2.hasNext())
		dul.add(((String)dit2.next()).toUpperCase());

	    while (dit.hasNext()) {
		String du = (String)dit.next();

		if (!services.contains(du))
		    session.createQuery("delete from OpalService where url=\'"+du+"\'");
	    }	

	    while (sit.hasNext()) {
		String s = (String)sit.next();

		if (dul.contains(s.toUpperCase()) == false) {
		    OpalService h = new OpalService();
		    String name = s.substring(s.lastIndexOf('/')+1, s.length());
		    
		    h.setName(name);
		    h.setUrl(s);
		    h.setHost(host);
		    h.setNumCpuTotal(numCpuTotal);
		    h.setNumCpuFree(numCpuFree);
		    h.setNumJobsRunning(numJobsRunning);
		    h.setNumJobsQueued(numJobsQueued);

		    session.save(h);
		}
		else {
		    String snv = "numCpuTotal=\'"+numCpuTotal+"\',numCpuFree=\'"+numCpuFree+
			"\',numJobsRunning=\'"+numJobsRunning+"\',numJobsQueued=\'"+numJobsQueued+"\'";
		    session.createQuery("update OpalService set "+snv+" where url="+"\'"+s+"\'");
		}
	    }
	    
	    session.getTransaction().commit();
	} catch (HibernateException hne) {
	    throw new ExceptionInInitializerError(hne);
	} catch(Exception e){
	    e.printStackTrace();
	} finally {
	    if(session != null){
		session.close();
	    }

	}
    }

    public static void makeRegistryXML (String filename) {
	String name_db, url_db, hostname_db;
	int numCpuTotal_db, numCpuFree_db, numJobsRunning_db, numJobsQueued_db;
	Element host, hostname, url, name, numCpuTotal, numCpuFree, numJobsRunning, numJobsQueued;
	Session session = null;

        try{
 	    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 
	    Document doc = docBuilder.newDocument();
	    Element rootElement = doc.createElement("opalservices");
	    doc.appendChild(rootElement);

	    session = getSessionFactory().openSession();

            List dl = session.createQuery("from OpalService").list();
	    Iterator it = dl.iterator();
	    
	    while (it.hasNext()) {
		OpalService h = (OpalService)it.next();
		url_db = h.getUrl();
		name_db = h.getName();
		hostname_db = h.getHost();
		numCpuTotal_db = h.getNumCpuTotal();
		numCpuFree_db = h.getNumCpuFree();
		numJobsRunning_db = h.getNumJobsRunning();
		numJobsQueued_db = h.getNumJobsQueued();

		host = doc.createElement("opalservice");
		rootElement.appendChild(host);

		url = doc.createElement("url");
		url.appendChild(doc.createTextNode(url_db));
		host.appendChild(url);
 
		name = doc.createElement("name");
		name.appendChild(doc.createTextNode(name_db));
		host.appendChild(name);

		hostname = doc.createElement("host");
		hostname.appendChild(doc.createTextNode(hostname_db));
		host.appendChild(hostname);

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