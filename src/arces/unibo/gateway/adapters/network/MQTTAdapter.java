package arces.unibo.gateway.adapters.network;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import arces.unibo.SEPA.Logger;
import arces.unibo.SEPA.Logger.VERBOSITY;

public class MQTTAdapter extends MNAdapter implements MqttCallback {
	private MqttClient mqttClient;
	private String serverURI = "tcp://mml.arces.unibo.it:10996"; // tcp://iot.eclipse.org:1883";
	private String clientID = "MQTTAdapter";
	private String[] topicsFilter = {"toffano/#"};
	
	private HashMap<String,String> topicResponseCache = new HashMap<String,String>();
	
	@Override
	public String networkURI() {
		return "iot:MQTT";
	}

	@Override
	protected void mnRequest(String request) {
		if (topicResponseCache.containsKey(request)) mnResponse(topicResponseCache.get(request));
	}

	@Override
	protected boolean doStart() {
		
		try 
		{
			mqttClient = new MqttClient(serverURI,clientID);
		} 
		catch (MqttException e) {
			Logger.log(VERBOSITY.FATAL,adapterName(),"Failed to create MQTT client "+e.getMessage());
			return false;
		}
		
		try 
		{
			mqttClient.connect();
		} 
		catch (MqttException e) {
			Logger.log(VERBOSITY.FATAL,adapterName(),"Failed to connect "+e.getMessage());
			return false;
		}
		
		mqttClient.setCallback(this);
		
		try 
		{
			mqttClient.subscribe(topicsFilter);
		} 
		catch (MqttException e) {
			Logger.log(VERBOSITY.FATAL,adapterName(),"Failed to subscribe "+e.getMessage());
			return false;
		}
		
		String topics = "";
		for (int i=0; i < topicsFilter.length;i++) topics += "\""+ topicsFilter[i] + "\" ";
		
		Logger.log(VERBOSITY.INFO,adapterName(),"MQTT client "+clientID+" subscribed to "+serverURI+" Topic filter "+topics);
		
		return true;
	}

	@Override
	protected void doStop() {
		try 
		{
			if (topicsFilter != null) mqttClient.unsubscribe(topicsFilter);
		} 
		catch (MqttException e1) {
			Logger.log(VERBOSITY.ERROR,adapterName(),"Failed to unsubscribe "+e1.getMessage());
		}
		
		try 
		{
			mqttClient.disconnect();
		} 
		catch (MqttException e) {
			Logger.log(VERBOSITY.ERROR,adapterName(),"Failed to disconnect "+e.getMessage());
		}
	}

	@Override
	public void connectionLost(Throwable arg0) {
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		String value = new String(message.getPayload()) + " @ " + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
		topicResponseCache.put(topic, topic+"&"+value);
	}

	@Override
	public String adapterName() {
		return "MQTT ADAPTER    ";
	}
}
