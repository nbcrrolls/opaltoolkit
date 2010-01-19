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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletInputStream;

/**
 * Implements ServletInputStream to store data from a ByteArrayOutputStream.
 */
public class OpalSOAPInputStream extends ServletInputStream {

	/** The input data. */
	private InputStream in;

	/**
	 * Instantiates a new custom soap input stream.
	 * 
	 * @param baos the output stream
	 */
	public OpalSOAPInputStream(ByteArrayOutputStream baos) {
		super();
		in = new ByteArrayInputStream(baos.toByteArray());
	}

	/* (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException {
		return in.read();
	}

	/* (non-Javadoc)
	 * @see java.io.InputStream#close()
	 */
	@Override
	public void close() throws IOException {
		in.close();
	}

}
