package arces.unibo.iot.dispatching;

import java.util.Iterator;

import arces.unibo.iot.SEPA.Bindings;
import arces.unibo.iot.SEPA.BindingsResults;
import arces.unibo.iot.mapping.IoTContextAction;

public class MPResponseDispatcher extends ResponseDispatcher {
	private static String IoT_RESPONSE =
			" SELECT ?action ?context ?value WHERE { "
					+ "?response rdf:type iot:IoT-Response . "
					+ "?response iot:hasAction ?action . "
					+ "?response iot:hasContext ?context . "
					+ "?response iot:hasValue ?value"
					+ " }";	
	
	public MPResponseDispatcher() {
		super(IoT_RESPONSE);
	}

	@Override
	public void responseNotification(BindingsResults notify) {
		Iterator<Bindings> results = notify.getAddedBindings().iterator();
		while(results.hasNext()){
			Bindings bindings = results.next();

			IoTContextAction response = new IoTContextAction(bindings.getBindingValue("?context").getValue(), 
					bindings.getBindingValue("?action").getValue(),  
					bindings.getBindingValue("?value").getValue());
			
			setChanged();
			notifyObservers(response);
		}
	}
}
