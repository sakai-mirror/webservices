package org.sakaiproject.axis2;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import java.io.UnsupportedEncodingException; 
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException; 
import java.security.MessageDigest;
import java.util.Properties;
import java.util.Set;

import javax.xml.namespace.*;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.component.cover.ServerConfigurationService;
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
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.cover.ToolManager;


/**
 * Launch Service. A webservice adhering to IMS LTI spec. (see http://simplelti.appspot.com/static/simple_lti_v05.pdf)
 * for learning tools interoperability. Given a request with the required parameters it will return a response (xml)
 * containing launchURL & other info.
 * Point requests at http://yourserver:port/sakai-axis2/LTILaunch/testlaunch?toolID=foo
 * @author katieedwards
 *
 */
public class LTILaunch {
	public OMFactory factory = OMAbstractFactory.getOMFactory();
	private boolean CHECK_TIME = false;
	private boolean CHECK_DIGEST = false;
	private boolean CHECK_ORG_DIGEST = false;
	private boolean CHECK_SITE = false;
	private boolean CHECK_LOGIN = false;
	private boolean CHECK_JOINED = false;
	private final static String SECRET = "sakai.ims.lti.secret";
	private String secret;
	
	public OMElement launch(OMElement request) throws AxisFault {
		request.build();
		request.detach();
		
		OMElement launchResponse = factory.createOMElement(new QName("launchResponse"));
		OMElement status = factory.createOMElement(new QName("status"));
		
		/* this parameter can (but doesn't have to) be built into the URL*/
		OMElement toolID_el = request.getFirstChildWithName(new QName("toolID"));
		String toolID = toolID_el.getText();
		
		OMElement nonce_el = request.getFirstChildWithName(new QName("sec_nonce"));
		String nonce = nonce_el.getText();
		
		OMElement created_el = request.getFirstChildWithName(new QName("sec_created"));
		String created = created_el.getText();
		
		OMElement digest_el = request.getFirstChildWithName(new QName("sec_digest"));
		String digest = digest_el.getText();
		
		OMElement org_digest_el = request.getFirstChildWithName(new QName("sec_org_digest"));
		String org_digest = org_digest_el.getText();
		
		/* get the secret from the tool's placement properties. 
		 * If the tool has none, secret will be null and we'll get a badPasswordDigest error */
		try {
			secret = findSecret(toolID);
		}catch (Exception e) {
			throw new AxisFault("Error validating secret");
		}
		
		
		MessageContext messageContext = MessageContext.getCurrentMessageContext();
		String ipAddress = (String)messageContext.getProperty("REMOTE_ADDRESS");
		String hostname = null;
		try {
			hostname = InetAddress.getByName(ipAddress).getHostName();
		}catch (UnknownHostException e) {
			hostname = null;
		}
//		
//		//check organizational secret
		if (org_digest != null) {
			//get host name for organizational password lookup
			
		String org_secret = ServerConfigurationService.getString("simplelti.secret." + hostname); /*Just for now*/

			
			String presha1 = nonce + created + org_secret;
			
			CHECK_ORG_DIGEST = securityCheck(presha1, org_digest);
		}
		else {
			//first make sure there isn't a secret saved for that host name!
			if (ServerConfigurationService.getString("simplelti.secret." + hostname) == null) {
				CHECK_ORG_DIGEST = true;
			}
			else CHECK_ORG_DIGEST = false;
		}

		//check regular secret
		String presha1 = nonce + created + secret;
		CHECK_DIGEST = securityCheck(presha1, digest);
		CHECK_TIME = TimeCalculator.within2Days(created);
		
		String sessionId = null;
		OMElement eid_el = request.getFirstChildWithName(new QName("user_id"));
		OMElement fname_el = request.getFirstChildWithName(new QName("user_firstname"));
		OMElement lname_el = request.getFirstChildWithName(new QName("user_lastname"));
		OMElement email_el = request.getFirstChildWithName(new QName("user_email"));
		//attempt user login if security check passed
		if (CHECK_DIGEST && CHECK_ORG_DIGEST && CHECK_TIME) {
			try {
				sessionId = loginCreateSession(eid_el, fname_el,lname_el, email_el);
				CHECK_LOGIN = true;
			} catch (Exception e) {
				CHECK_LOGIN = false;
			}
		}
		
		//this will only work if login worked anyway...
		//try to find a site by it's unique id
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
		

		String userrole = request.getFirstChildWithName(new QName("user_role")).getText();

		try {
			Site thesite = SiteService.getSite(siteId);
			Set<Role> roles = thesite.getRoles();

			User theuser = UserDirectoryService.getUserByEid(eid_el.getText());
			if (userrole.equalsIgnoreCase("administrator") || userrole.equalsIgnoreCase("maintain")) {
				//requested role is administrator, join as maintain
				thesite.addMember(theuser.getId(), "maintain", true, true);
				CHECK_JOINED = true;
			}
			else {
				for (Role r : roles) {
					//scan available roles and join with requested role if possible
					if (r.getId().equalsIgnoreCase(userrole)) {
						try {
							thesite.addMember(theuser.getId(), userrole, true, true);
							CHECK_JOINED = true;
						}catch(Exception e) {
							CHECK_JOINED = false;
						}
						
					}
				}
			}
			//last ditch effort to join the site
			if (!CHECK_JOINED) 
				try {
					SiteService.join(siteId);
					CHECK_JOINED = true;
				} catch (Exception e) {
					CHECK_JOINED = false;
				}
		}catch(Exception e) {
			throw new AxisFault("Could not join site " + siteId);
		}

		//we want to make sure everything is ok before proceeding.
		boolean success = CHECK_DIGEST  && CHECK_ORG_DIGEST && CHECK_TIME && CHECK_SITE && CHECK_LOGIN && CHECK_JOINED;
		
		if (success) {
			//build the success response
			status.setText("success");
			launchResponse.addChild(status);
			
			OMElement launch_targets_el = request.getFirstChildWithName(new QName("launch_targets"));
			
			boolean dowidget;
			String launch_targets = launch_targets_el.getText();
			
			if (launch_targets_el != null) {				
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
				if (launch_targets.startsWith("post"))
					type.setText("post");
				else
					type.setText("iFrame");		//default
				OMElement launchUrl = factory.createOMElement(new QName("launchUrl"));
				launchUrl.setText("http://localhost:8080/portal/tool/" + toolID + "?sakai.session=" + sessionId);
				launchResponse.addChild(type);
				launchResponse.addChild(launchUrl);
			}
			
		}
		else {
			//build fail response
			status.setText("fail");
			OMElement code = factory.createOMElement(new QName("code"));
			OMElement description = factory.createOMElement(new QName("description"));
			/* set error codes */
			if (!CHECK_DIGEST) {
				code.setText("BadPasswordDigest");
				description.setText("The password digest was invalid");
			}
			else if (!CHECK_ORG_DIGEST) {
				code.setText("BadOrgPasswordDigest");
				description.setText("Organizational authentication failed"/* + ServerConfigurationService.getString("simplelti.secret." + hostname) + "hostname: " + hostname*/);
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
			else if (!CHECK_JOINED) {
				code.setText("SiteJoinFail");
				description.setText("Could not join the requested site.");
			}
			
			launchResponse.addChild(status);
			launchResponse.addChild(code);
			launchResponse.addChild(description);
		}
	
				
		return launchResponse;
		
	}
	
	/**
	 * Build the widget part of the XML response.
	 * This needs to be changed, that youtube link should be changed for a more appropriate URL
	 * but you get the idea
	 * @param width_el
	 * @param height_el
	 * @return
	 */
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
	 * Check the validity of the digest against the string composed of a nonce, date and secret.
	 * @param presha nonce + created + secret
	 * @param digest the digest that should match
	 * @return true if there's a match, false otherwise
	 */
	private boolean securityCheck(String presha1, String digest) {
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
		
		return digest.equals(sha1);
	}
	
	
	/**
	 * Login the user based on the given parameters. In the case that the user does not exist, create the user.
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
	
	/**
	 * this method is basically the same as findSecret from org.sakaiproject.sakaiimsltihelper.tool.impl.ImsLtiHandler, 
	 * which is part of the IMS LTI Preferences tool inside sakai that sets the secrets and settings for tools. 
	 * It just grabs the secret from the tool's placement properties file.
	 * 
	 * @param toolId The unique ID of the tool whose secret we're looking for.
	 * @return
	 */
	private String findSecret(String toolId) {
		Site site = null;
		ToolConfiguration siteTool = SiteService.findTool(toolId);
		String siteId = null;
		try {
			siteId = siteTool.getSiteId();
			site = SiteService.getSite(siteId);
		}
		catch(Exception e) {
		}
      

		try {
			ToolConfiguration tc = site.getTool(toolId);
			Properties toolconfig = tc.getPlacementConfig();
			return toolconfig.getProperty(SECRET);
		}
		catch(Exception e) {
			e.printStackTrace();
			return e.getStackTrace()[0].toString();
		}
	}
	

}
