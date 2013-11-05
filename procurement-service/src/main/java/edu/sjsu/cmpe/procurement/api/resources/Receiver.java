package edu.sjsu.cmpe.procurement.api.resources;

import java.io.BufferedReader; 
import java.io.IOException; 
import java.io.InputStreamReader; 
import java.net.HttpURLConnection; 
import java.net.MalformedURLException; 
import java.net.URL; 
import java.util.StringTokenizer; 
import java.io.FileNotFoundException; 
import java.io.FileReader; 
import java.io.IOException; 
import java.util.Iterator;

import javax.jms.Connection; 
import javax.jms.DeliveryMode; 
import javax.jms.Destination; 
import javax.jms.JMSException; 
import javax.jms.MessageProducer; 
import javax.jms.Session; 
import javax.jms.TextMessage;

import org.fusesource.stomp.jms.StompJmsConnectionFactory; 
import org.fusesource.stomp.jms.StompJmsDestination; 
import org.json.simple.JSONArray; 
import org.json.simple.JSONObject; 
import org.json.simple.parser.JSONParser; 
import org.json.simple.parser.ParseException;

public class Receiver {

/** * @param args * @throws ParseException * @throws JMSException */ 
/*public static void main(String[] args) throws ParseException, JMSException 
{ // TODO Auto-generated method stub
*/

public static void main(String args[]) 
{

System.out.println("----------Reciever Thread---------------");
JSONParser parser = new JSONParser(); 

try { URL url = new URL(" http://54.215.210.214:9000/orders/31103"); 
HttpURLConnection conn = (HttpURLConnection) url.openConnection(); 
conn.setRequestMethod("GET"); 
conn.setRequestProperty("Accept", "application/json"); 
if (conn.getResponseCode() != 200) 
{ 
throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode()); 
} 

BufferedReader br = new BufferedReader(new InputStreamReader( (conn.getInputStream())));

String output; 
System.out.println("Output from Server .... \n"); 
while ((output = br.readLine()) != null) 
{ 
System.out.println(output); 
Object obj = parser.parse(output); 
JSONObject jsonObject = (JSONObject) obj; 
JSONArray responseMsg = (JSONArray) jsonObject.get("shipped_books"); 
String []msgFormat = new String[responseMsg.size()]; 
for(int i=0;i<responseMsg.size();i++) 
{ 
JSONObject books = (JSONObject) responseMsg.get(i); 
msgFormat[i]= books.get("isbn").toString() +":"+"\""+books.get("title").toString()+"\""+":"+"\""+books.get("category").toString()+"\""+":"+"\""+books.get("coverimage").toString()+"\""; System.out.println(msgFormat[i]);
}

//Publishing messages to topic 
/* 
String user = env("APOLLO_USER", "admin"); 
String password = env("APOLLO_PASSWORD", "password"); 
String host = env("APOLLO_HOST", "54.215.210.214"); 
int port = Integer.parseInt(env("APOLLO_PORT", "61613")); 
String destination = "/topic/33387.book"; 
*/ 
String user = env("APOLLO_USER", "admin"); 
String password = env("APOLLO_PASSWORD", "password"); 
String host = env("APOLLO_HOST", "54.215.210.214"); 
int port = Integer.parseInt(env("APOLLO_PORT", "61613")); 
String destination = "/topic/31103.book";

StompJmsConnectionFactory factory = new StompJmsConnectionFactory(); 
factory.setBrokerURI("tcp://" + host + ":" + port);

Connection connection = factory.createConnection(user, password); 
connection.start(); 
Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE); 
for(int i=0;i<msgFormat.length;i++) 
{ 
JSONObject books = (JSONObject) responseMsg.get(i); 
Destination dest = new StompJmsDestination(destination+"."+books.get("category").toString()); 
MessageProducer producer = session.createProducer(dest); 
producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

String data = msgFormat[i]; 
TextMessage msg = session.createTextMessage(data); 
msg.setLongProperty("id", System.currentTimeMillis()); 
producer.send(msg); 
if(i==msgFormat.length-1) 
{ 
producer.send(session.createTextMessage("SHUTDOWN")); 
} 
} 
} 
conn.disconnect();
} 
catch (MalformedURLException e) 
{ 
e.printStackTrace(); 
} 
catch (IOException e) 
{ 
e.printStackTrace();
} catch (JMSException e) {
// TODO Auto-generated catch block
e.printStackTrace();
} catch (ParseException e) {
// TODO Auto-generated catch block
e.printStackTrace();
}
}
 

private static String env(String key, String defaultValue) 
{ 
String rc = System.getenv(key); 
if( rc== null ) 
{ 
return defaultValue; 
} 
return rc; 
}

}