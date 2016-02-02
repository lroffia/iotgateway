package arces.unibo.iot.dispatching;

import java.util.UUID;

import arces.unibo.iot.SEPA.BindingLiteralValue;
import arces.unibo.iot.SEPA.BindingURIValue;
import arces.unibo.iot.SEPA.Bindings;
import arces.unibo.iot.mapping.IoTContextAction;

public class MPRequestDispatcher extends RequestDispatcher {
	private static String IoT_PENDING_REQUEST = 
			" INSERT { "
					+ "?request rdf:type iot:IoT-Request . "
					+ "?request iot:hasValue ?value . "
					+ "?request iot:hasAction ?action . "
					+ "?request iot:hasContext ?context"
					+ " }";
	
	public MPRequestDispatcher() {
		super(IoT_PENDING_REQUEST);
	}

	public void dispatch(IoTContextAction contextAction){		
		Bindings bindings = new Bindings();
		bindings.addBinding("?request", new BindingURIValue("iot:IoT-Request_"+UUID.randomUUID().toString()));
		bindings.addBinding("?context", new BindingURIValue(contextAction.getContextURI()));
		bindings.addBinding("?action", new BindingURIValue(contextAction.getActionURI()));
		bindings.addBinding("?value", new BindingLiteralValue(contextAction.getValue()));
		
		update(bindings);
	}
}
