package org.sakaiproject.axis2;
import org.apache.axiom.om.*;
import javax.xml.stream.*;
import org.apache.axis2.AxisFault;
import javax.xml.namespace.*;

public class helloagain {
	
	public OMElement test(OMElement el) throws XMLStreamException {
		el.build();
		el.detach();
			
		
		OMFactory factory = OMAbstractFactory.getOMFactory();
		
		OMElement response = factory.createOMElement(new QName("launchResponse"));
		
		OMElement status = factory.createOMElement(new QName("status"));
		status.setText("success");
		
		OMElement type = factory.createOMElement(new QName("type"));
		type.setText("iframe");
		
		OMElement url = factory.createOMElement(new QName("launchUrl"));
		url.setText("http://www.youtube.com/v/f90ysF9BenI&hl=en");
		
		response.addChild(status);
		response.addChild(type);
		response.addChild(url);
		
		return response;
	}

}
