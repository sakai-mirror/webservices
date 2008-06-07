helloService : Should be able to deploy on it's own now.
-Switch to directory axis2
-type (sudo) mvn compile war:war
- move ./target/sakai-axis2-0.1.war into $CATALINA_HOME/webapps
- restart tomcat

Run sampleClient.java as a simple test to see everything's working OK.