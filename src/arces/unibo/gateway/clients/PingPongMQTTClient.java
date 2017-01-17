package arces.unibo.gateway.clients;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import arces.unibo.SEPA.application.Logger;
import arces.unibo.SEPA.application.Logger.VERBOSITY;

public class PingPongMQTTClient {
	private static MqttClient client;
	private static String serverURI = "tcp://iot.eclipse.org:1883";
	private static String clientID = "MQTTPingPongClient";
	
	public static void main(String[] args){
		String tmpDir = System.getProperty("java.io.tmpdir");
    	MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);
    	
		try 
		{
			client = new MqttClient(serverURI,clientID,dataStore);
		} 
		catch (MqttException e) {
			Logger.log(VERBOSITY.FATAL, "MQTT CLIENT", e.getMessage());
			return;
		}
		
		try 
		{
			client.connect();
		} 
		catch (MqttException e) {
			Logger.log(VERBOSITY.FATAL, "MQTT CLIENT", e.getMessage());
			return;
		}
		
		try 
		{
			client.publish("PING", new MqttMessage(new byte[]{'P','I','N','G'}));
			client.publish("PONG", new MqttMessage(new byte[]{'P','O','N','G'}));
		} 
		catch (MqttException e) {
			Logger.log(VERBOSITY.FATAL, "MQTT CLIENT", e.getMessage());
			return;
		}
		
		try 
		{
			client.disconnect();
		} 
		catch (MqttException e) {
			Logger.log(VERBOSITY.FATAL, "MQTT CLIENT", e.getMessage());
			return;
		}
	}
}
