package org.sakaiproject.axis2;

import org.apache.axis2.*;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.*;
import org.apache.axis2.client.ServiceClient;
import org.apache.axiom.om.*;
import javax.xml.stream.*;

/**
 * A sample client you can run to test out the "MyService" webservice included in this package.
 * @author katieedwards
 *
 */
public class sampleClient {
	
	private static String toEpr = "http://localhost:8080/sakai-axis2/services/MyService/sayhi";
	
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
		

	}
	
	private static OMElement getPayload(String name) {
		OMFactory factory = OMAbstractFactory.getOMFactory();
		OMNamespace ns = factory.createOMNamespace("www.EXAMPLE.com", "PERSON");
		
		OMElement method = factory.createOMElement("sayhi", ns);
		OMElement value = factory.createOMElement("name", ns);
		
		value.addChild(factory.createOMText(value, name));
		
		method.addChild(value);
		return method;
	}

}
