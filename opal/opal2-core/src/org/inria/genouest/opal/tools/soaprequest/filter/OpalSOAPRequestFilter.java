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

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * A servlet filter that do the SOAP request conversion if needed.
 */
public class OpalSOAPRequestFilter implements Filter {
	
	/** The filter config. */
	private FilterConfig filterConfig = null;

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig filterConfig) 
	throws ServletException {
		this.filterConfig = filterConfig;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
		this.filterConfig = null;
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;

		String path = req.getRequestURL().toString();

		// only do the translation if it is not a built-in Axis service
		if (!path.endsWith("AxisServlet")) {
		    OpalSOAPRequestWrapper reqWrapper = new OpalSOAPRequestWrapper(req, filterConfig.getServletContext()); // Modify the SOAP request if needed

		    // Pass to other filters/servlet
		    chain.doFilter(reqWrapper, response);
		} else {
		    chain.doFilter(request, response);
		}
	}

	/**
	 * Fetch the entire contents of a text file, and return it in a String.
	 * This style of implementation does not throw Exceptions to the caller.
	 * 
	 * @param aFile is a file which already exists and can be read.
	 * 
	 * @return the contents
	 */
	static public String getContents(File aFile) {
		//...checks on aFile are elided
		StringBuilder contents = new StringBuilder();

		try {
			//use buffering, reading one line at a time
			//FileReader always assumes default encoding is OK!
			BufferedReader input =  new BufferedReader(new FileReader(aFile));
			try {
				String line = null; //not declared within while loop
				/*
				 * readLine is a bit quirky :
				 * it returns the content of a line MINUS the newline.
				 * it returns null only for the END of the stream.
				 * it returns an empty String if two newlines appear in a row.
				 */
				while (( line = input.readLine()) != null){
					contents.append(line);
					contents.append(System.getProperty("line.separator"));
				}
			}
			finally {
				input.close();
			}
		}
		catch (IOException ex){
			ex.printStackTrace();
		}

		return contents.toString();
	}

}
