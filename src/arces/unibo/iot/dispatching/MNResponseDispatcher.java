package arces.unibo.iot.dispatching;

import java.util.Iterator;

import arces.unibo.iot.SEPA.Bindings;
import arces.unibo.iot.SEPA.BindingsResults;
import arces.unibo.iot.mapping.MNResponse;

public class MNResponseDispatcher extends ResponseDispatcher {
	
	private static String MN_RESPONSE =
			" SELECT ?network ?value WHERE { "
			+ "?response rdf:type iot:MN-Response . "
			+ "?response iot:hasNetwork ?network . "
			+ "?response iot:hasMNResponseString ?value"
			+ " }";
	
	public MNResponseDispatcher() {
		super(MN_RESPONSE);
	}

	@Override
	public void responseNotification(BindingsResults notify) {
		Iterator<Bindings> results = notify.getAddedBindings().iterator();
		while(results.hasNext()){
			Bindings bindings = results.next();

			MNResponse response = new MNResponse(bindings.getBindingValue("?network").getValue(), bindings.getBindingValue("?value").getValue());
			
			setChanged();
			notifyObservers(response);
		}
	}
}
