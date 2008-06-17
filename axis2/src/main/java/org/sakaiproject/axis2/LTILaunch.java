package org.sakaiproject.axis2;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMAbstractFactory;
import java.io.UnsupportedEncodingException; 
import java.security.NoSuchAlgorithmException; 
import java.security.MessageDigest;

import javax.xml.namespace.*;

public class LTILaunch {
	public OMFactory factory = OMAbstractFactory.getOMFactory();
	
	public OMElement testlaunch(OMElement request) {
		request.build();
		request.detach();
		
		OMElement launchResponse = factory.createOMElement(new QName("launchResponse"));
		
		OMElement siteID_el = request.getFirstChildWithName(new QName("toolID"));
		String siteID = siteID_el.getText();
		
		OMElement toolID_el = request.getFirstChildWithName(new QName("toolID"));
		String toolID = toolID_el.getText();
		
		String secret = "secret";
		
		OMElement nonce_el = request.getFirstChildWithName(new QName("sec_nonce"));
		String nonce = nonce_el.getText();
		
		OMElement created_el = request.getFirstChildWithName(new QName("sec_created"));
		String created = created_el.getText();
		
		OMElement digest_el = request.getFirstChildWithName(new QName("sec_digest"));
		String digest = digest_el.getText();
		
		String presha1 = nonce + created + secret;
		String sha1 = null;
		
		try {
			MessageDigest md;
			md = MessageDigest.getInstance("SHA-1");
			byte[] sha1hash = new byte[40];
			md.update(presha1.getBytes("utf-8"), 0, presha1.length());
			sha1hash = md.digest();
			sha1 = new sun.misc.BASE64Encoder().encode(sha1hash);
		}
		catch(NoSuchAlgorithmException e) {}
		catch(UnsupportedEncodingException e) {}
		
		boolean success = digest.equals(sha1);
		
		OMElement status = factory.createOMElement(new QName("status"));
		
		
		if (success) {
			status.setText("success");
			launchResponse.addChild(status);
			
			OMElement launch_targets_el = request.getFirstChildWithName(new QName("launch_targets"));
			
			boolean dowidget;
			
			if (launch_targets_el != null) {
				String launch_targets = launch_targets_el.getText();
				dowidget = launch_targets.startsWith("widget");
			}
			else {
				dowidget = false;
			}
			
			OMElement type = factory.createOMElement(new QName("type"));
			
			if (dowidget) {
				type.setText("widget");
				OMElement widget = getWidget(request.getFirstChildWithName(new QName("launch_width")), 
						request.getFirstChildWithName(new QName("launch_height")));
				launchResponse.addChild(type);
				launchResponse.addChild(widget);
			}
			else {
				type.setText("iFrame");
				OMElement launchUrl = factory.createOMElement(new QName("launchUrl"));
				launchUrl.setText("http://www.youtube.com/v/f90ysF9BenI");
				launchResponse.addChild(type);
				launchResponse.addChild(launchUrl);
			}
			
		}
		else {
			status.setText("fail");
			OMElement code = factory.createOMElement(new QName("code"));
			code.setText("BadPasswordDigest");
			OMElement description = factory.createOMElement(new QName("description"));
			description.setText("The password digest was invalid");
			
			launchResponse.addChild(status);
			launchResponse.addChild(code);
			launchResponse.addChild(description);
		}
	
				
		return launchResponse;
		
	}
	
	
	private OMElement getWidget(OMElement width_el, OMElement height_el) {
		width_el.build();
		width_el.detach();
		height_el.build();
		height_el.detach();
		
		OMElement widget = factory.createOMElement(new QName("widget"));
		String width, height;
		
		if (width_el != null && width_el.getText().length() > 0) 
			width = width_el.getText();
		else
			width = "425";
		
		if (height_el != null && height_el.getText().length() > 0) 
			height = height_el.getText();
		else
			height = "344";
		
		widget.setText("<object width=\"" + width +"\" height=\""+ height + "\"><param name=\"movie\" value=\"http://www.youtube.com/v/f90ysF9BenI&hl=en\"></param><embed src=\"http://www.youtube.com/v/f90ysF9BenI&hl=en\" type=\"application/x-shockwave-flash\" width=\"" + width + "\" height=\"" + height + "\"></embed></object>");
		
		return widget;
	}

}
