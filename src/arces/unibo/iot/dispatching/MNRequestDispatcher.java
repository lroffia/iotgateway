package arces.unibo.iot.dispatching;

import java.util.UUID;

import arces.unibo.iot.SEPA.BindingLiteralValue;
import arces.unibo.iot.SEPA.BindingURIValue;
import arces.unibo.iot.SEPA.Bindings;
import arces.unibo.iot.mapping.MNRequest;

public class MNRequestDispatcher extends RequestDispatcher {
	private static String MN_PENDING_REQUEST = 
			" INSERT { "
					+ "?request rdf:type iot:MN-Request . "
					+ "?request iot:hasNetwork ?network . "
					+ "?request iot:hasMNRequestString ?value"
					+ " }";
	
	public MNRequestDispatcher() {
		super(MN_PENDING_REQUEST);
	}

	public void dispatch(MNRequest request){
		Bindings bindings = new Bindings();
		bindings.addBinding("?request", new BindingURIValue("iot:MN-Request_"+UUID.randomUUID().toString()));
		bindings.addBinding("?network", new BindingURIValue(request.getNetwork()));
		bindings.addBinding("?value", new BindingLiteralValue(request.getRequestString()));
		
		update(bindings);
	}
	
}
