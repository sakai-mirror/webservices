/**
 * Copyright (c) 2008 Katherine Edwards (katherine.edwards2@mail.mcgill.ca)
 * 
 * Licensed under the Apache 2.0 license
 *
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/apache2.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
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
	
	private static String toEpr = "http://127.0.0.1:8080/sakai-axis2/services/MyService/sayhi";
	
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
