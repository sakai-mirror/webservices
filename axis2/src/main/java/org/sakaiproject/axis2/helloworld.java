package org.sakaiproject.axis2;
import org.apache.axiom.om.*;
import javax.xml.stream.*;
import org.apache.axis2.AxisFault;
import javax.xml.namespace.*;

public class helloworld {
	
	public OMElement sayHello(OMElement el) throws XMLStreamException {
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
