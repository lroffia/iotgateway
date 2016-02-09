package arces.unibo.gateway.adapters.network;

import java.util.UUID;

import arces.unibo.SEPA.Aggregator;
import arces.unibo.SEPA.BindingLiteralValue;
import arces.unibo.SEPA.BindingURIValue;
import arces.unibo.SEPA.Bindings;
import arces.unibo.SEPA.BindingsResults;

public abstract class MNAdapter {
	
	protected abstract String networkURI();
	protected abstract void mnRequest(String request);
	protected abstract boolean doStart();
	protected abstract void doStop();
	
	private MNRequestResponseDispatcher dispatcher;
	
	class MNRequestResponseDispatcher extends Aggregator {
		private static final String MN_RESPONSE = 
				" INSERT DATA { "
				+ "?response rdf:type iot:MN-Response . "
				+ "?response iot:hasNetwork ?network . "
				+ "?response iot:hasMNResponseString ?value"
				+ " }";
		
		private static final String MN_REQUEST =
				" SELECT ?value WHERE { "
				+ "?request rdf:type iot:MN-Request . "
				+ "?request iot:hasNetwork ?network . "
				+ "?request iot:hasMNRequestString ?value"
				+ " }";
		
		public MNRequestResponseDispatcher(){
			super(MN_REQUEST,MN_RESPONSE);
		}
			
		@Override
		public boolean subscribe(){
			//SPARQL SUBSCRIBE
			Bindings bindings = new Bindings();
			bindings.addBinding("?network", new BindingURIValue(networkURI()));
			return subscribe(bindings);
		}
		
		@Override
		public void notify(BindingsResults notify) {
			for (Bindings bindings : notify.getAddedBindings()){
				mnRequest(bindings.getBindingValue("?value").getValue());
			}
		}
	}
	
	protected final boolean mnResponse(String value) {
		String response = "iot:MN-Response_"+UUID.randomUUID().toString();
		
		Bindings bindings = new Bindings();
		bindings.addBinding("?value", new BindingLiteralValue(value));
		bindings.addBinding("?response", new BindingURIValue(response));
		bindings.addBinding("?network", new BindingURIValue(networkURI()));
		
		//SPARQL UPDATE
		return dispatcher.update(bindings);
	}
	
	public boolean start(){
		dispatcher = new MNRequestResponseDispatcher();
		if (!dispatcher.join()) return false;
		if (!dispatcher.subscribe()) return false;
		return doStart();
	}
	
	public void stop(){
		dispatcher.unsubscribe();
		doStop();
	}
}
