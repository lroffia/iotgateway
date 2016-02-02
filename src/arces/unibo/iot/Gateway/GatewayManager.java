package arces.unibo.iot.Gateway;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import arces.unibo.iot.SEPA.BindingLiteralValue;
import arces.unibo.iot.SEPA.BindingURIValue;
import arces.unibo.iot.SEPA.Bindings;
import arces.unibo.iot.SEPA.BindingsResults;
import arces.unibo.iot.SEPA.Consumer;
import arces.unibo.iot.SEPA.Producer;

import arces.unibo.iot.network.adapters.MNAdapter;
import arces.unibo.iot.protocol.adapters.MPAdapter;

public class GatewayManager {
	
	static MappingManager mappingManager;
	static MNDispatcher mnDispatcher;
	static MPDispatcher mpDispatcher;
	static ArrayList<MNAdapter> networks;
	static ArrayList<MPAdapter> protocols;
	static GarbageCollector gc;
	
	public static class GatewayConsumer extends Consumer {
		private final static String MP_REQUEST =
				" SELECT ?request ?protocol ?value WHERE { "
				+ "?request rdf:type iot:MP-Request . "
				+ "?request iot:hasProtocol ?protocol . "
				+ "?request iot:hasMPRequestString ?value" 
				+ " }";
		
		public GatewayConsumer() {
			super(MP_REQUEST);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void notify(BindingsResults notify) {
			// TODO Auto-generated method stub
			System.out.println(notify.toString());
		}
		
	}
	
	public static class GatewayProducer extends Producer {
		private final static String MP_REQUEST = 
				" INSERT DATA { "
				+ "?request rdf:type iot:MP-Request . "
				+ "?request iot:hasProtocol ?protocol . "
				+ "?request iot:hasMPRequestString ?value"
				+ " }";
		public GatewayProducer() {
			super(MP_REQUEST);
			// TODO Auto-generated constructor stub
		}
		
	}
	
	public static void main(String[] args) throws IOException {
		GatewayProducer producer = new GatewayProducer();
		GatewayConsumer consumer = new GatewayConsumer();
			
		Bindings bindings = new Bindings();

		new Thread(producer).start();
		new Thread(consumer).start();
		BindingsResults ret = consumer.subscribe(null);
		if (ret == null) return;
		System.out.println(ret.toString());
		
		while(true){
			bindings.addBinding("?request", new BindingURIValue("iot:MP-Request_"+UUID.randomUUID().toString()));
			bindings.addBinding("?value", new BindingLiteralValue(UUID.randomUUID().toString()));
			bindings.addBinding("?protocol", new BindingURIValue("iot:"+UUID.randomUUID().toString()));		
			producer.update(bindings);
		}
		
/*		
		//Mapping manager
		mappingManager = new MappingManager();
		if(!mappingManager.start()) return;
		
		//Dispatchers
		//mnDispatcher = new MNDispatcher();
		//if(!mnDispatcher.start()) return;
		mpDispatcher = new MPDispatcher();
		BindingsResults mpRequests = mpDispatcher.start();
		mpDispatcher.notify(mpRequests);
		
		//Networks
		networks = new ArrayList<MNAdapter>();
		//TODO: add all supported networks here
		//networks.add(new PingPongAdapter());
		
				
		//Protocols
		protocols= new ArrayList<MPAdapter>();
		//TODO: add all supported protocols here
		protocols.add(new HTTPAdapter());

		//Starting adapters
		for (MNAdapter adapter : networks) adapter.start();
		for (MPAdapter adapter : protocols) adapter.start();

		
		//Garbage collector
		//gc = new GarbageCollector();
		//if (!gc.start()) return;
		

		
		//Simulator
		PingPongHTTPClient client = new PingPongHTTPClient();
		String ret = "PING";
		while(true) {
			ret = client.doRequest(ret);
			if (ret.equals("ERROR") || ret.equals("TIMEOUT")) ret = "PING";
		}
		*/
	}
}
