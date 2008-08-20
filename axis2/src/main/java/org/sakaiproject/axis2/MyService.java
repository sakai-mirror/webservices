package org.sakaiproject.axis2;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;

public class MyService {
	
	public OMElement sayhi(OMElement el) throws XMLStreamException {
		el.build();
		el.detach();
			
		OMElement child = el.getFirstChildWithName(new QName("name"));
		String person = child.getText();
		
		OMFactory factory = OMAbstractFactory.getOMFactory();
		
		OMElement reply = factory.createOMElement(new QName("reply"));
		reply.setText("Hello, " + person);
		
		return reply;
	}

}
