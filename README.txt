WRITING A NEW WEBSERVICE:

1) Write the service class. Public methods will be exposed as operations in the service.
	Place the source inside src/main/java
	
2) Create a directory for your services inside src/webapp/WEB-INF/services and create a directory inside
	that called META-INF
	
3) Write services.xml and place it inside META-INF. It should look something like this : 

<?xml version="1.0" encoding="UTF-8"?>

<service name="Name_of_service">

	<description>
		bla bla bla
	</description>
	
	<!-- State the service class name  -->
	<parameter name="ServiceClass" locked="false">
		org.sakaiproject.axis2.myService
	</parameter>
	
	<messageReceivers>
        <messageReceiver mep="http://www.w3.org/2004/08/wsdl/in-only"
                         class="org.apache.axis2.rpc.receivers.RPCInOnlyMessageReceiver"/>
        <messageReceiver mep="http://www.w3.org/2004/08/wsdl/in-out"
                         class="org.apache.axis2.rpc.receivers.RPCMessageReceiver"/>
    </messageReceivers>
    
</service>

4) From inside axis2 directory type sudo mvn compile war:war, and place the resulting war file
	inside $CATALINA_HOME/webapps. Restart Tomcat.
	/* should be able to do this automatically when building Sakai */

5) You can see the wsdl for your service at http://localhost:8080/sakai-axis2-0.1/myService?wsdl,
	or test the service at http://localhost:8080/sakai-axis2-0.1/myService/myOperation?myParameter=foo
	
	+-src/
	  |
	  +-main/
	  | |
	  | +-java/
	  |   |
	  |   myService.java
      |
      +-webapp/
        |
        +-WEB-INF/
          |
          +-services/
          	|
          	+-myService/
          	  |
          	  +-META-INF/
          	    |
          	    services.xml