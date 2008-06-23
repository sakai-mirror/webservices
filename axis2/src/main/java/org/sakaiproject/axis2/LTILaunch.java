package org.sakaiproject.axis2;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import java.io.UnsupportedEncodingException; 
import java.security.NoSuchAlgorithmException; 
import java.security.MessageDigest;

import javax.xml.namespace.*;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.exception.*;
import org.sakaiproject.id.cover.IdManager;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;



public class LTILaunch {
	public OMFactory factory = OMAbstractFactory.getOMFactory();
	private boolean CHECK_TIME = false;
	private boolean CHECK_DIGEST = false;
	private boolean CHECK_SITE = false;
	private boolean CHECK_LOGIN = false;
	private final String secret = "secret";
	
	public OMElement testlaunch(OMElement request) {
		request.build();
		request.detach();
		
		OMElement launchResponse = factory.createOMElement(new QName("launchResponse"));
		OMElement status = factory.createOMElement(new QName("status"));
		
		
		OMElement toolID_el = request.getFirstChildWithName(new QName("toolID"));
		String toolID = toolID_el.getText();
		//launchResponse.addChild(toolID_el); 
		
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
		
		CHECK_DIGEST = digest.equals(sha1);
		CHECK_TIME = TimeCalculator.within2Days(created);
		
		String sessionId = null;
		try {
			sessionId = loginCreateSession(request.getFirstChildWithName(new QName("user_id")),
					request.getFirstChildWithName(new QName("user_firstname")),
					request.getFirstChildWithName(new QName("user_lastname")), 
					request.getFirstChildWithName(new QName("user_email")));
			CHECK_LOGIN = true;
		}
		catch(Exception e) {
			CHECK_LOGIN = false;
		}
		
		ToolConfiguration siteTool = SiteService.findTool(toolID);
		CHECK_SITE = siteTool != null;
		String siteId = null;
		try {
			siteId = siteTool.getSiteId();
			CHECK_SITE = true;
			
		}
		catch(Exception e) {
			CHECK_SITE = false;
		}
	
		boolean success = CHECK_DIGEST && CHECK_TIME && CHECK_SITE && CHECK_LOGIN;
		
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
			
			//this is just here for now, not sure where to put this data
			OMElement test_launchURL = factory.createOMElement(new QName("TOOLURL"));
			test_launchURL.setText("http://localhost:8080/portal/tool/" + toolID + "?sakai.session=" + sessionId);
			launchResponse.addChild(test_launchURL);
		}
		else {
			status.setText("fail");
			OMElement code = factory.createOMElement(new QName("code"));
			OMElement description = factory.createOMElement(new QName("description"));
			/* set error codes */
			if (!CHECK_DIGEST) {
				code.setText("BadPasswordDigest");
				description.setText("The password digest was invalid");
			}
			else if (!CHECK_TIME) {
				code.setText("BadRequestTime");
				description.setText("The request created time was invalid");
			}
			else if (!CHECK_LOGIN) {
				code.setText("LoginFail");
				description.setText("Could not log user in");
			}
			else if (!CHECK_SITE) {
				code.setText("BadSiteRequest");
				description.setText("Could not find the requested site.");
			}
			
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
	
	
	/**
	 * 
	 * @param eid_el
	 * @param fname_el
	 * @param lname_el
	 * @param email_el
	 * @return Session Id of created session.
	 * @throws AxisFault
	 */
	private String loginCreateSession(OMElement eid_el, OMElement fname_el, OMElement lname_el, OMElement email_el) 
	throws AxisFault {
		eid_el.build();
		eid_el.detach();
		fname_el.build();
		fname_el.detach();
		lname_el.build();
		lname_el.detach();
		email_el.build();
		email_el.detach();
		
		String eid = eid_el.getText();
		String fname = fname_el.getText();
		String lname = lname_el.getText();
		String email = email_el.getText();
		
		MessageContext messageContext = MessageContext.getCurrentMessageContext();
		String ipAddress = (String)messageContext.getProperty("REMOTE_ADDRESS");

		User user = null;
		try {
			user = UserDirectoryService.getUserByEid(eid);
		}
		catch(Exception e) {
			user = null;
		}
		
		if (user == null && fname != null && lname != null && email != null) {
			try {
				String hiddenPW = IdManager.createUuid();
				UserDirectoryService.addUser(null,eid,fname,lname,email,hiddenPW,"registered", null);
				user = UserDirectoryService.getUserByEid(eid);
			}
			catch(Exception e) {
				throw new AxisFault("Unable to create user.");
			}
			
		}
		
		
		Session s = SessionManager.startSession();
		SessionManager.setCurrentSession(s);
		
		if (s == null) {
			throw new AxisFault("Unable to create session.");
		}
		else {
			UsageSessionService.login(user.getId(), eid, ipAddress, null, UsageSessionService.EVENT_LOGIN_WS);
			return s.getId();
		}
		
	}

}
