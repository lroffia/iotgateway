package arces.unibo.gateway.adapters.protocol;

import java.util.Iterator;
import java.util.UUID;

import arces.unibo.SEPA.Aggregator;
import arces.unibo.SEPA.BindingLiteralValue;
import arces.unibo.SEPA.BindingURIValue;
import arces.unibo.SEPA.Bindings;
import arces.unibo.SEPA.BindingsResults;

public abstract class MPAdapter {

	public abstract String protocolURI();
	public abstract void mpResponse(String request,String value);
	public abstract boolean doStart();
	public abstract void doStop();
	
	private MPRequestResponseDispatcher dispatcher;
	
	class MPRequestResponseDispatcher extends Aggregator {
		//MP-ADAPTER
			private static final String MP_REQUEST = 
					" INSERT DATA { "
					+ "?request rdf:type iot:MP-Request . "
					+ "?request iot:hasProtocol ?protocol . "
					+ "?request iot:hasMPRequestString ?value"
					+ " }";
			
			private static final String MP_RESPONSE =
					" SELECT ?request ?value WHERE { "
							+ "?response rdf:type iot:MP-Response . "
							+ "?request iot:hasMPResponse ?response . "	
							+ "?response iot:hasMPResponseString ?value . "
							+ "?request iot:hasProtocol ?protocol"
							+ " }";
		
		
		public boolean subscribe() {
			Bindings bindings = new Bindings();
			bindings.addBinding("?protocol", new BindingURIValue(protocolURI()));
			return super.subscribe(bindings);	
		}
		
		public MPRequestResponseDispatcher(){
			super(MP_RESPONSE,MP_REQUEST);
		}
		
		@Override
		public void notify(BindingsResults notify) {
			if (notify.getAddedBindings().isEmpty()) return;
			
			Iterator<Bindings> bindings = notify.getAddedBindings().iterator();
			
			while(bindings.hasNext()) {
				Bindings binding = bindings.next();
	
				mpResponse(binding.getBindingValue("?request").getValue(),binding.getBindingValue("?value").getValue());
			}
		}
	}
	
	public boolean start(){
		dispatcher = new MPRequestResponseDispatcher();
		if (!dispatcher.join()) return false;
		if (!dispatcher.subscribe()) return false;
		return doStart();
	}
	
	public void stop(){
		dispatcher.unsubscribe();
		doStop();
	}
	
	protected final String mpRequest(String value) {
		Bindings bindings = new Bindings();
		String request = "iot:MP-Request_"+UUID.randomUUID().toString();
		bindings.addBinding("?request", new BindingURIValue(request));
		bindings.addBinding("?value", new BindingLiteralValue(value));
		bindings.addBinding("?protocol", new BindingURIValue(protocolURI()));
		
		//SPARQL UPDATE
		if(!dispatcher.update(bindings)) return null;
		
		return request;
	}
}
