package arces.unibo.gateway.adapters.network;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import arces.unibo.SEPA.client.pattern.ApplicationProfile;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.HashMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;

public class MQTTAdapter extends MNAdapter implements MqttCallback {
	private MqttClient mqttClient;
	
	private static final Logger logger = LogManager.getLogger("MQTTAdapter");
	
	private boolean created = false;
	
	public static HashMap<String,String> debugHash = new HashMap<String,String>();
	private HashMap<String,String> topicResponseCache = new HashMap<String,String>();

	private String serverURI = "ssl://giove.mars:8883";
	private String[] topicsFilter = {"arces/servers/#"};
	private String caCrtFile = "/usr/local/mosquitto-certs/ca.crt";
	private String crtFile = "/usr/local/mosquitto-certs/mml.crt";
	private String keyFile = "/usr/local/mosquitto-certs/mml.key" ;
	private boolean sslEnabled = false;
	private String clientID = "SEPAMQTTClient";
	
	public static class SslUtil {

	    public static SSLSocketFactory getSocketFactory(final String caCrtFile, final String crtFile, final String keyFile,
	                                                    final String password) {
	        try {

	            /**
	             * Add BouncyCastle as a Security Provider
	             */
	            Security.addProvider(new BouncyCastleProvider());

	            JcaX509CertificateConverter certificateConverter = new JcaX509CertificateConverter().setProvider("BC");

	            /**
	             * Load Certificate Authority (CA) certificate
	             */
	            PEMParser reader = new PEMParser(new FileReader(caCrtFile));
	            X509CertificateHolder caCertHolder = (X509CertificateHolder) reader.readObject();
	            reader.close();

	            X509Certificate caCert = certificateConverter.getCertificate(caCertHolder);

	            /**
	             * Load client certificate
	             */
	            reader = new PEMParser(new FileReader(crtFile));
	            X509CertificateHolder certHolder = (X509CertificateHolder) reader.readObject();
	            reader.close();

	            X509Certificate cert = certificateConverter.getCertificate(certHolder);

	            /**
	             * Load client private key
	             */
	            reader = new PEMParser(new FileReader(keyFile));
	            Object keyObject = reader.readObject();
	            reader.close();

	            PEMDecryptorProvider provider = new JcePEMDecryptorProviderBuilder().build(password.toCharArray());
	            JcaPEMKeyConverter keyConverter = new JcaPEMKeyConverter().setProvider("BC");

	            KeyPair key;

	            if (keyObject instanceof PEMEncryptedKeyPair) {
	                key = keyConverter.getKeyPair(((PEMEncryptedKeyPair) keyObject).decryptKeyPair(provider));
	            } else {
	                key = keyConverter.getKeyPair((PEMKeyPair) keyObject);
	            }

	            /**
	             * CA certificate is used to authenticate server
	             */
	            KeyStore caKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
	            caKeyStore.load(null, null);
	            caKeyStore.setCertificateEntry("ca-certificate", caCert);

	            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
	                    TrustManagerFactory.getDefaultAlgorithm());
	            trustManagerFactory.init(caKeyStore);

	            /**
	             * Client key and certificates are sent to server so it can authenticate the client
	             */
	            KeyStore clientKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
	            clientKeyStore.load(null, null);
	            clientKeyStore.setCertificateEntry("certificate", cert);
	            clientKeyStore.setKeyEntry("private-key", key.getPrivate(), password.toCharArray(),
	                    new Certificate[]{cert});

	            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
	                    KeyManagerFactory.getDefaultAlgorithm());
	            keyManagerFactory.init(clientKeyStore, password.toCharArray());

	            /**
	             * Create SSL socket factory
	             */
	            SSLContext context = SSLContext.getInstance("TLSv1.2");
	            context.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

	            /**
	             * Return the newly created socket factory object
	             */
	            return context.getSocketFactory();

	        } catch (Exception e) {
	        	logger.error(e.getMessage());
	        }

	        return null;
	    }
	}
	
	public MQTTAdapter(ApplicationProfile appProfile, String url,int port,String[] topics) {
		super(appProfile);
		this.sslEnabled = false;
		serverURI = "tcp://"+url+":"+String.format("%d", port);
		topicsFilter = topics;
	}
	
	public MQTTAdapter(ApplicationProfile appProfile, String url,int port,String[] topics,String caCrtFile,String crtFile,String keyFile) {
		super(appProfile);
		this.sslEnabled = true;
		serverURI = "ssl://"+url+":"+String.format("%d", port);
		topicsFilter = topics; 
		this.caCrtFile = caCrtFile;
		this.crtFile = crtFile;
		this.keyFile = keyFile;
	}
	

	@Override
	public void connectionLost(Throwable arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void messageArrived(String topic, MqttMessage value) throws Exception {
		logger.debug(topic+ " "+value.toString());
		
		mnResponse(topic+"&"+value.toString());
		
		topicResponseCache.put(topic, topic+"&"+value.toString());
		
		if (debugHash.containsKey(topic)) {
			if (!debugHash.get(topic).equals(value.toString())) {
				logger.debug(topic+ " "+debugHash.get(topic)+"-->"+value.toString());	
			}
		}
		
		debugHash.put(topic, value.toString());
	}

	//Secure: -url giove.mars -port 8883 -topics arces/servers/# -cacrt /usr/local/mosquitto-certs/ca.crt -crt /usr/local/mosquitto-certs/mml.crt -key /usr/local/mosquitto-certs/mml.key
	//Not secure: -url giove.mars -port 1883 -topics #
	public static void main(String[] args) throws IOException {		
		String path = "GatewayProfile.sap";
		ApplicationProfile appProfile = new ApplicationProfile();
		
		if(!appProfile.load(path)) {
			logger.fatal("Failed to load: "+ path);
			return;
		}
		else logger.info("Loaded application profile "+ path);
		
		MQTTAdapter adapter;
		String url = null;
		String port = null;
		String[] topics = null;
		String caCrtFile = null;
		String crtFile = null;
		String keyFile = null ;
		
		for (int i=0; i < args.length; i++) {
			switch(args[i]) {
				case "-url":
					url = args[++i];
					break;
				case "-port":
					port = args[++i];
					break;
				case "-topics":
					topics = args[++i].split(",");
					break;
				case "-cacrt":
					caCrtFile = args[++i];
					break;
				case "-crt":
					crtFile = args[++i];
					break;
				case "-key":
					keyFile = args[++i];
					break;
			}
		}
		
		if (caCrtFile != null && crtFile != null && keyFile != null){
			adapter = new MQTTAdapter(appProfile,url,Integer.parseInt(port),topics,caCrtFile,crtFile,keyFile);
			logger.debug("caCrtFile="+caCrtFile);
			logger.debug("crtFile="+crtFile);
			logger.debug("keyFile="+keyFile);
		}
		else
			adapter = new MQTTAdapter(appProfile,url,Integer.parseInt(port),topics);
		
		logger.debug("url="+url);
		logger.debug("port="+port);
		logger.debug("topics="+topics);
		
		if(adapter.start()) {
			logger.info("Press any key to exit...");
			System.in.read();
			if(adapter.stop()) logger.info("Stopped");
		}
		else {
			logger.fatal("NOT running");
			logger.info("Press any key to exit...");
			System.in.read();
		}	
	}

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
			logger.fatal("Failed to create MQTT client "+e.getMessage());
			return created;
		}
		
		try 
		{	
			MqttConnectOptions options = new MqttConnectOptions();
			if (sslEnabled) {
				SSLSocketFactory ssl = SslUtil.getSocketFactory(caCrtFile, crtFile, keyFile, "");
				if (ssl == null) {
					logger.fatal("SSL security option creation failed");
				}
				else options.setSocketFactory(ssl);												
			}
			mqttClient.connect(options);
		} 
		catch (MqttException e) {
			logger.fatal("Failed to connect "+e.getMessage());
			return created;
		}
		
		mqttClient.setCallback(this);
		
		try 
		{
			mqttClient.subscribe(topicsFilter);
		} 
		catch (MqttException e) {
			logger.fatal("Failed to subscribe "+e.getMessage());
			return created;
		}
		
		String topics = "";
		for (int i=0; i < topicsFilter.length;i++) topics += "\""+ topicsFilter[i] + "\" ";
		
		logger.info("MQTT client "+clientID+" subscribed to "+serverURI+" Topic filter "+topics);
	
		created = true;
		
		return created;
	}

	@Override
	protected void doStop() {
		try 
		{
			if (topicsFilter != null) mqttClient.unsubscribe(topicsFilter);
		} 
		catch (MqttException e1) {
			logger.error("Failed to unsubscribe "+e1.getMessage());
		}
		
		try 
		{
			mqttClient.disconnect();
		} 
		catch (MqttException e) {
			logger.error("Failed to disconnect "+e.getMessage());
		}
		
	}

	@Override
	public String adapterName() {
		return "MQTT ADAPTER    ";
	}
}
/*
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import arces.unibo.SEPA.application.SEPALogger;
import arces.unibo.SEPA.application.ApplicationProfile;
import arces.unibo.SEPA.application.SEPALogger.VERBOSITY;


public class MQTTAdapter extends MNAdapter implements MqttCallback {
	
	public MQTTAdapter(ApplicationProfile appProfile) {
		super(appProfile);
	}

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
			SEPALogger.log(VERBOSITY.FATAL,adapterName(),"Failed to create MQTT client "+e.getMessage());
			return false;
		}
		
		try 
		{
			mqttClient.connect();
		} 
		catch (MqttException e) {
			SEPALogger.log(VERBOSITY.FATAL,adapterName(),"Failed to connect "+e.getMessage());
			return false;
		}
		
		mqttClient.setCallback(this);
		
		try 
		{
			mqttClient.subscribe(topicsFilter);
		} 
		catch (MqttException e) {
			SEPALogger.log(VERBOSITY.FATAL,adapterName(),"Failed to subscribe "+e.getMessage());
			return false;
		}
		
		String topics = "";
		for (int i=0; i < topicsFilter.length;i++) topics += "\""+ topicsFilter[i] + "\" ";
		
		SEPALogger.log(VERBOSITY.INFO,adapterName(),"MQTT client "+clientID+" subscribed to "+serverURI+" Topic filter "+topics);
		
		return true;
	}

	@Override
	protected void doStop() {
		try 
		{
			if (topicsFilter != null) mqttClient.unsubscribe(topicsFilter);
		} 
		catch (MqttException e1) {
			SEPALogger.log(VERBOSITY.ERROR,adapterName(),"Failed to unsubscribe "+e1.getMessage());
		}
		
		try 
		{
			mqttClient.disconnect();
		} 
		catch (MqttException e) {
			SEPALogger.log(VERBOSITY.ERROR,adapterName(),"Failed to disconnect "+e.getMessage());
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
*/