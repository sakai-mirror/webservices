package org.sakaiproject.axis2;

import org.apache.axiom.om.OMElement;

public class EchoService {
	
	public OMElement echo(OMElement element) {

        element.build();

        element.detach();

        return element;

    }

	
}
