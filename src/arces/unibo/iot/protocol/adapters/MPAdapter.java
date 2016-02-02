package arces.unibo.iot.protocol.adapters;

import java.util.Iterator;
import java.util.UUID;

import arces.unibo.iot.SEPA.Aggregator;
import arces.unibo.iot.SEPA.BindingLiteralValue;
import arces.unibo.iot.SEPA.BindingURIValue;
import arces.unibo.iot.SEPA.Bindings;
import arces.unibo.iot.SEPA.BindingsResults;

public abstract class MPAdapter extends Aggregator implements IProtocolAdapter {

	//MP-ADAPTER
		private static String MP_REQUEST = 
				" INSERT { "
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
		if(!joined) return false;
		
		Bindings bindings = new Bindings();
		bindings.addBinding("?protocol", new BindingURIValue(protocolURI()));
		
		//SPARQL SUBSCRIBE
		BindingsResults ret = subscribe(bindings);
		if (ret == null) return false;
		
		System.out.println(ret.toString());
		
		return true;
	}
	
	public MPAdapter(){
		super(MP_RESPONSE,MP_REQUEST);
	}
	
	protected String mpRequest(String value) {
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
