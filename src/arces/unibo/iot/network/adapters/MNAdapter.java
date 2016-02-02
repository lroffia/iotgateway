package arces.unibo.iot.network.adapters;

import java.util.ArrayList;
import java.util.UUID;

import arces.unibo.iot.SEPA.Aggregator;
import arces.unibo.iot.SEPA.BindingLiteralValue;
import arces.unibo.iot.SEPA.BindingURIValue;
import arces.unibo.iot.SEPA.Bindings;
import arces.unibo.iot.SEPA.BindingsResults;

public abstract class MNAdapter extends Aggregator implements INetworkAdapter {
	//static MNAdapter adapter;
		
	private static String MN_RESPONSE = 
			" INSERT { "
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
		if (!joined) return false;
		return Subscribe_to_MN_Request();
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
	
	private boolean Subscribe_to_MN_Request(){
		//SPARQL SUBSCRIBE
		Bindings bindings = new Bindings();
		bindings.addBinding("?network", new BindingURIValue(networkURI()));

		//SPARQL SUBSCRIBE
		BindingsResults ret = subscribe(bindings);
		if (ret == null) return false;
		
		System.out.println(ret.toString());
		
		return true;
	}
	
	public abstract void mnRequest(String request);
	
	@Override
	public void notify(BindingsResults notify) {
		ArrayList<Bindings> addedBindings = notify.getAddedBindings();
		if (addedBindings.size() != 1) return;
		String request = addedBindings.get(0).getBindingValue("?value").getValue();
		
		mnRequest(request);
	}
}
