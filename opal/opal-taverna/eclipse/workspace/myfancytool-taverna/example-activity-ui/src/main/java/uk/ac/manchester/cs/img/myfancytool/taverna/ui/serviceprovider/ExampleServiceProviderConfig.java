package uk.ac.manchester.cs.img.myfancytool.taverna.ui.serviceprovider;

import java.net.URI;

import net.sf.taverna.t2.lang.beans.PropertyAnnotated;
import net.sf.taverna.t2.lang.beans.PropertyAnnotation;

//public class ExampleServiceProviderConfig {
public class ExampleServiceProviderConfig extends PropertyAnnotated {
    //private URI uri;
    private URI uri = URI.create("http://ws.nbcr.net/opal2/services");


	public URI getUri() {
		return uri;
	}

	@PropertyAnnotation(displayName="Opal Web Service URL")
	public void setUri(URI uri) {
		this.uri = uri;
	}
}
