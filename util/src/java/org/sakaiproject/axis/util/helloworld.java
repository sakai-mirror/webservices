package org.sakaiproject.axis.util;
import org.apache.axiom.om.*;
import javax.xml.stream.*;
import org.apache.axis2.AxisFault;

public class helloworld {
	
	public OMElement sayHello(OMElement el) throws XMLStreamException {
		el.build();
		el.detach();
			
		OMElement child = el.getFirstElement();
		String person = child.getText();
		
		String rootName = el.getLocalName();
        System.out.println("Reading "+rootName+" element");
		
		OMFactory factory = OMAbstractFactory.getOMFactory();
		OMNamespace namespace = factory.createOMNamespace("www.example.com", "person");
		
		OMElement method = factory.createOMElement("sayHello", namespace);
		OMElement value = factory.createOMElement("greeting", namespace);
		
		value.addChild( factory.createOMText("Hello, " + person ) );
		
		method.addChild( value );
		
		return method;
	}

}
