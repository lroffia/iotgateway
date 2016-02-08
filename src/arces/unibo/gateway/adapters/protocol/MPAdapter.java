package arces.unibo.gateway.adapters.protocol;

import java.util.Iterator;
import java.util.UUID;

import arces.unibo.SEPA.Aggregator;
import arces.unibo.SEPA.BindingLiteralValue;
import arces.unibo.SEPA.BindingURIValue;
import arces.unibo.SEPA.Bindings;
import arces.unibo.SEPA.BindingsResults;

public abstract class MPAdapter extends Aggregator implements IProtocolAdapter {

	//MP-ADAPTER
		private static String MP_REQUEST = 
				" INSERT DATA { "
				+ "?request rdf:type iot:MP-Request . "
				+ "?request iot:hasProtocol ?protocol . "
				+ "?request iot:hasMPRequestString ?value"
				+ " }";
		
		private static String MP_RESPONSE =
				" SELECT ?request ?value WHERE { "
						+ "?response rdf:type iot:MP-Response . "
						+ "?request iot:hasMPResponse ?response . "	
						+ "?response iot:hasMPResponseString ?value . "
						+ "?request iot:hasProtocol ?protocol"
						+ " }";
	
	public boolean start() {
		if(!super.start()) return false;
		
		BindingsResults ret = subscribe();
		notify(ret);
		
		return true;
	}
	
	public BindingsResults subscribe() {
		Bindings bindings = new Bindings();
		bindings.addBinding("?protocol", new BindingURIValue(protocolURI()));
		return super.subscribe(bindings);	
	}
	
	public MPAdapter(){
		super(MP_RESPONSE,MP_REQUEST);
	}
	
	protected final String mpRequest(String value) {
		Bindings bindings = new Bindings();
		String request = "iot:MP-Request_"+UUID.randomUUID().toString();
		bindings.addBinding("?request", new BindingURIValue(request));
		bindings.addBinding("?value", new BindingLiteralValue(value));
		bindings.addBinding("?protocol", new BindingURIValue(protocolURI()));
		
		//SPARQL UPDATE
		if(!update(bindings)) return null;
		
		return request;
	}
	
	public abstract void mpResponse(String request,String value);
	
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
