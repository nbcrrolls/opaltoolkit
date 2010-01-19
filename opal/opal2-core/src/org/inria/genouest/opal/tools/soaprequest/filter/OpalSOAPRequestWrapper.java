/**
 * OpalSOAPRequestFilter package
 * 
 * 
 * Licence: BSD
 * 
 * Genouest Platform (http://www.genouest.org)
 * Author: Anthony Bretaudeau <anthony.bretaudeau@irisa.fr>
 * Creation: May 26th, 2009
 */

package org.inria.genouest.opal.tools.soaprequest.filter;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Implements HttpServletRequestWrapper to actually do the conversion.
 */
public class OpalSOAPRequestWrapper extends HttpServletRequestWrapper {
	
	/** The modified stream. */
	private OpalSOAPInputStream modifiedStream;
	
	/** The Constant logger. */
	private static final Logger logger = Logger.getLogger(OpalSOAPRequestWrapper.class);
	
	/** The context. */
	private ServletContext context;
	
	/** The servlet path. */
	private String servletPath;
	
	/** The wsdd name space. */
	static String wsddNameSpace = "http://xml.apache.org/axis/wsdd/";

	/**
	 * Instantiates a new opal soap request wrapper.
	 * 
	 * @param request the request
	 * @param servletContext the servlet context
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public OpalSOAPRequestWrapper(HttpServletRequest request, ServletContext servletContext) throws IOException {
		super(request);
		context = servletContext;
		servletPath = request.getRequestURL().toString();

		// Retrieve SOAP request
		InputStream is = request.getInputStream();
		int ch;
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		while ((ch = is.read()) != -1)
		{
			buffer.write((byte)ch);
		}

		// Modify SOAP request if needed
		logger.debug("Entering OpalSOAPRequestFilter");
		ByteArrayOutputStream baos = ModifySOAPRequest(buffer);
		modifiedStream = new OpalSOAPInputStream(baos);
	}

	/**
	 * Modify soap request.
	 * 
	 * @param buffer the buffer
	 * 
	 * @return the byte array output stream
	 */
	private ByteArrayOutputStream ModifySOAPRequest(ByteArrayOutputStream buffer) {
		if (buffer.size() == 0)
			return buffer;
		
		byte result[];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			String encoding = getCharacterEncoding();
			if (encoding == null)
				encoding = "UTF-8";
			String originalSOAPStr = new String(buffer.toByteArray(), encoding);

			// Do the XML stuff
			InputStream xsltStream = getClass().getResourceAsStream("/xslt/typedSOAP2OpalSOAP.xsl");
			if (xsltStream == null) {
				logger.error("The XSLT file could not be found.");
				throw new Exception("The XSLT file could not be found.");
			}

			String resultSOAPStr = originalSOAPStr; // In case we received bad request
			
			// If using SOAP attachment, original request is not an XML string but a MIME content.
			if (getContentType().startsWith("multipart/")) {
				// Get boundary = the line separating each part of the mime message
				BufferedReader br = new BufferedReader(new StringReader(originalSOAPStr));
				String boundary = br.readLine();
				if (boundary.length() == 0) // sometimes there's an empty line at the beginning
					boundary = br.readLine();
				
				String[] mimeParts = originalSOAPStr.split(boundary);
				
				for (int i = 0; i < mimeParts.length; i++) {
					String unknownPart = mimeParts[i];
					if (unknownPart.indexOf("Content-Type: text/xml") >= 0) {
						// This part is XML, perhaps the SOAP request, try to apply stylesheet
						String xmlPart = unknownPart.substring(unknownPart.indexOf("<?xml"));
						String resultXmlPart = applyXSLTTransformation(xsltStream, xmlPart);
						// And apply changes to full message
						// Add a \r\n to ensure next part is correctly recognized (\n is not enough)
						resultSOAPStr = resultSOAPStr.replace(xmlPart, resultXmlPart+"\r\n");
					}
				}
			}
			else {
				// Normal XML message. Just apply XSLT stylesheet.
				resultSOAPStr = applyXSLTTransformation(xsltStream, originalSOAPStr);
			}

			logger.debug("Received this SOAP request in filter:");
			logger.debug(originalSOAPStr);
			logger.debug("Modified SOAP request returned to axis:");
			logger.debug(resultSOAPStr);
			
			result = resultSOAPStr.getBytes(encoding);
			baos.write(result,0,result.length);
		}catch(Exception e) {
			logger.error("Exception Message", e);
		}
		return baos;
	}

	/**
	 * Apply xslt transformation.
	 * 
	 * @param xsltFile the xslt file
	 * @param originalSOAP the original soap
	 * 
	 * @return the string
	 * 
	 * @throws TransformerFactoryConfigurationError the transformer factory configuration error
	 * @throws Exception the exception
	 */
	private String applyXSLTTransformation(InputStream xsltFile, String originalSOAP)
	throws TransformerFactoryConfigurationError, Exception {
		String finalSOAP = "";

		TransformerFactory transfoFact = TransformerFactory.newInstance();
		StreamSource stylesource = new StreamSource(xsltFile);
		Transformer transformer = transfoFact.newTransformer(stylesource);

		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

		// Création de la source DOM
		DocumentBuilderFactory docBFact = DocumentBuilderFactory.newInstance();
		docBFact.setNamespaceAware(true);
		DocumentBuilder docBuilder = docBFact.newDocumentBuilder();

		Source source;
		try {
			Reader reader = new CharArrayReader(originalSOAP.toCharArray());
			Document document = docBuilder.parse(new org.xml.sax.InputSource(reader));
			source = new DOMSource(document);
		}
		catch (SAXException e) {
			// There was a problem reading the original request string. Probably a malformed xml like the ones sent by biocatalogue for monitoring
			// There no point in trying to transform. Just give back the string as is. Axis will send a fault SOAP message to the client.
			return originalSOAP;
		}

		// Création du fichier de sortie
		StringWriter sw = new StringWriter();
		StreamResult result = new StreamResult( sw ); 
		
		String serviceName = "";
		String[] servletPathSplit = servletPath.split("/");
		if (servletPathSplit.length > 0)
			serviceName = servletPathSplit[servletPathSplit.length-1];
		
		if (("").equals(serviceName)) {
			logger.error("Unknown service name.");
			throw new Exception("Unknown service name.");
		}
		
		String serviceConfigFilePath = getServiceConfigFilePath(serviceName);
		if (serviceConfigFilePath != null) {
			transformer.setParameter("configPath", serviceConfigFilePath);
			transformer.transform(source, result);
			
			sw.close();
			finalSOAP = sw.toString();
		}
		else {
			// No config file found. We cannot do anything, just pass the request as is.
			finalSOAP = originalSOAP;
		}

		return finalSOAP;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequestWrapper#getContentLength()
	 */
	@Override
	public int getContentLength() {
		return -1;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequestWrapper#getInputStream()
	 */
	@Override
	public ServletInputStream getInputStream() throws IOException {
		return modifiedStream;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequestWrapper#getReader()
	 */
	@Override
	public BufferedReader getReader() throws IOException {
		final String enc = getCharacterEncoding();
		final InputStream istream = getInputStream();
		final Reader r = new InputStreamReader(istream, enc);

		return new BufferedReader(r);
	}

	/**
	 * Gets the service config file path.
	 * 
	 * @param serviceName the service name
	 * 
	 * @return the service config file path
	 */
	private String getServiceConfigFilePath(String serviceName) {
        String configFileName = null;
        try {
	        String path = context.getRealPath("/")+"WEB-INF"+File.separator+"server-config.wsdd";

			// Création de la source DOM
			DocumentBuilderFactory docBFact = DocumentBuilderFactory.newInstance();
			docBFact.setNamespaceAware(true);
			DocumentBuilder docBuilder = docBFact.newDocumentBuilder();

			Reader reader = new FileReader(path);
			Document document = docBuilder.parse(new org.xml.sax.InputSource(reader));
	        
	        // XPath to find service config file path
			XPathFactory xpathFact = XPathFactory.newInstance();
			XPath xpath = xpathFact.newXPath();
			
			// Namespace context for xpath
			NamespaceContext namespace = new NamespaceContext(){
				public String getNamespaceURI(String prefix){
					if(prefix.equals("dd")){
						return wsddNameSpace;
					}else{
						return "";
					}
				}
				public String getPrefix(String namespaceURI){
					if(namespaceURI.equals(wsddNameSpace)){
						return "dd";
					}else{
						return "";
					}
				}
				public Iterator<String> getPrefixes(String namespaceURI){
					List<String> list = new ArrayList<String>();

					if (namespaceURI.equals(wsddNameSpace)) {
						list.add("dd");
					}
					
					return list.iterator();
				} 
			};
			xpath.setNamespaceContext(namespace);
			
			// evaluate xpath
			XPathExpression exp = xpath.compile("/dd:deployment/dd:service[@name='"+serviceName+"']/dd:parameter[@name='appConfig']/@value");
			configFileName = (String) exp.evaluate(document, XPathConstants.STRING);
			
	        if (configFileName == null) {
	            logger.error("Required parameter appConfig not found in WSDD");
	            return null;
	        }
	        
	    }
        catch(XPathExpressionException xpee){
			xpee.printStackTrace();
		} catch (Exception e) {
        	logger.warn("We could not get the service list from the Axis Engine...");
        }
        return configFileName;
    }
}
