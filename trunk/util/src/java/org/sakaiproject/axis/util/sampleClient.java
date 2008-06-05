package org.sakaiproject.axis.util;

import org.apache.axis2.*;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.*;
import org.apache.axis2.client.ServiceClient;
import org.apache.axiom.om.*;
import javax.xml.stream.*;

public class sampleClient {
	
	private static String toEpr = "http://localhost:8080/hello-0.1/services/helloService/sayHello";
	
	public static void main(String[] args) throws AxisFault {
		
		Options options = new Options();
		options.setTo( new EndpointReference(toEpr));
		options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
		options.setUseSeparateListener(false);
		options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
		options.setProperty(Constants.Configuration.HTTP_METHOD,
				Constants.Configuration.HTTP_METHOD_GET);
		
		ServiceClient sc = new ServiceClient();
		sc.setOptions(options);
		
		OMElement pl = getPayload("Katie");
		OMElement result = sc.sendReceive(pl);
		System.out.println(result.getFirstElement().getText());
		
//		try {
//	            XMLStreamWriter writer = XMLOutputFactory.newInstance()
//	                   .createXMLStreamWriter(System.out);
//	            result.serialize(writer);
//	            writer.flush();
//	        } catch (XMLStreamException e) {
//	            e.printStackTrace();
//	        } catch (FactoryConfigurationError e) {
//	            e.printStackTrace();
//	        }
	}
	
	private static OMElement getPayload(String person) {
		OMFactory factory = OMAbstractFactory.getOMFactory();
		OMNamespace ns = factory.createOMNamespace("www.EXAMPLE.com", "PERSON");
		
		OMElement method = factory.createOMElement("sayHello", ns);
		OMElement value = factory.createOMElement("person", ns);
		
		value.addChild(factory.createOMText(value, person));
		
		method.addChild(value);
		return method;
	}

}
