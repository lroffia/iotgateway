package arces.unibo.gateway.adapters.network;

import java.util.UUID;

import arces.unibo.SEPA.Aggregator;
import arces.unibo.SEPA.BindingLiteralValue;
import arces.unibo.SEPA.BindingURIValue;
import arces.unibo.SEPA.Bindings;
import arces.unibo.SEPA.BindingsResults;

public abstract class MNAdapter extends Aggregator implements INetworkAdapter {
		
	private static String MN_RESPONSE = 
			" INSERT DATA { "
			+ "?response rdf:type iot:MN-Response . "
			+ "?response iot:hasNetwork ?network . "
			+ "?response iot:hasMNResponseString ?value"
			+ " }";
	
	private static String MN_REQUEST =
			" SELECT ?value WHERE { "
			+ "?request rdf:type iot:MN-Request . "
			+ "?request iot:hasNetwork ?network . "
			+ "?request iot:hasMNRequestString ?value"
			+ " }";
	
	public boolean start() {
		if(!super.start()) return false;
		
		BindingsResults ret = subscribe();
		notify(ret);
		
		return true;
	}
	
	public MNAdapter(){
		super(MN_REQUEST,MN_RESPONSE);
	}
	
	public boolean mnResponse(String value) {
		String response = "iot:MN-Response_"+UUID.randomUUID().toString();
		
		Bindings bindings = new Bindings();
		bindings.addBinding("?value", new BindingLiteralValue(value));
		bindings.addBinding("?response", new BindingURIValue(response));
		bindings.addBinding("?network", new BindingURIValue(networkURI()));
		
		//SPARQL UPDATE
		return update(bindings);
	}
	
	public BindingsResults subscribe(){
		//SPARQL SUBSCRIBE
		Bindings bindings = new Bindings();
		bindings.addBinding("?network", new BindingURIValue(networkURI()));
		return subscribe(bindings);
	}
	
	public abstract void mnRequest(String request);
	
	@Override
	public void notify(BindingsResults notify) {
		for (Bindings bindings : notify.getAddedBindings()){
			mnRequest(bindings.getBindingValue("?value").getValue());
		}
	}
}
