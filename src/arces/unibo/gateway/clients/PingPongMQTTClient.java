package arces.unibo.gateway.clients;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

public class PingPongMQTTClient {
	private static MqttClient client;
	private static String serverURI = "tcp://iot.eclipse.org:1883";
	private static String clientID = "PingPongMQTTClient";
	
	private static final Logger logger = LogManager.getLogger("PingPongMQTTClient");
	
	public static void main(String[] args){
		String tmpDir = System.getProperty("java.io.tmpdir");
    	MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);
    	
		try 
		{
			client = new MqttClient(serverURI,clientID,dataStore);
		} 
		catch (MqttException e) {
			logger.error( e.getMessage());
			return;
		}
		
		try 
		{
			client.connect();
		} 
		catch (MqttException e) {
			logger.fatal(e.getMessage());
			return;
		}
		
		try 
		{
			client.publish("PING", new MqttMessage(new byte[]{'P','I','N','G'}));
			client.publish("PONG", new MqttMessage(new byte[]{'P','O','N','G'}));
		} 
		catch (MqttException e) {
			logger.fatal( e.getMessage());
			return;
		}
		
		try 
		{
			client.disconnect();
		} 
		catch (MqttException e) {
			logger.fatal(e.getMessage());
			return;
		}
	}
}
