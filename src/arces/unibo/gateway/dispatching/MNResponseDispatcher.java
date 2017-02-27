package arces.unibo.gateway.dispatching;

import java.util.UUID;

import arces.unibo.SEPA.application.Aggregator;
import arces.unibo.SEPA.application.ApplicationProfile;
import arces.unibo.SEPA.application.SEPALogger;
import arces.unibo.SEPA.application.SEPALogger.VERBOSITY;
import arces.unibo.SEPA.commons.SPARQL.ARBindingsResults;
import arces.unibo.SEPA.commons.SPARQL.Bindings;
import arces.unibo.SEPA.commons.SPARQL.BindingsResults;
import arces.unibo.SEPA.commons.SPARQL.RDFTermLiteral;
import arces.unibo.SEPA.commons.SPARQL.RDFTermURI;
import arces.unibo.gateway.mapping.MNResponse;
import arces.unibo.gateway.mapping.ResourceAction;

public class MNResponseDispatcher extends Aggregator {		
	private static final String tag = "MN RESPONSE DISPATCHER";
	private MNMap mnMap;
	
	public MNResponseDispatcher(ApplicationProfile appProfile,MNMap mnMap) {
		super(appProfile,"MN_RESPONSE","INSERT_RESOURCE_RESPONSE");
		this.mnMap = mnMap;}

	public String subscribe() {return super.subscribe(null);}

	@Override
	public void notify(ARBindingsResults notify, String spuid, Integer sequence) {
		// TODO Auto-generated method stub
	}

	@Override
	public void notifyAdded(BindingsResults bindingsResults, String spuid, Integer sequence) {
		for (Bindings bindings : bindingsResults.getBindings()){

			MNResponse response = new MNResponse(bindings.getBindingValue("network"), bindings.getBindingValue("value"));
			
			SEPALogger.log(VERBOSITY.INFO, tag,"<< "+response.toString());
			
			//Mapping MN Response to Resource Response
			ResourceAction resourceAction = mnMap.mnResponse2ResourceAction(response);
			
			if (resourceAction == null) {
				resourceAction = new ResourceAction("iot:NULL","iot:NULL","MN-MAPPING NOT FOUND FOR "+response.toString());
				SEPALogger.log(VERBOSITY.WARNING, tag,">> Resource-Response "+resourceAction.toString());
			}
			else SEPALogger.log(VERBOSITY.INFO, tag,">> Resource-Response "+resourceAction.toString());
			
			bindings = new Bindings();
			bindings.addBinding("response", new RDFTermURI("iot:Resource-Response_"+UUID.randomUUID().toString()));						
			bindings.addBinding("resource", new RDFTermURI(resourceAction.getResourceURI()));
			bindings.addBinding("action", new RDFTermURI(resourceAction.getActionURI()));
			bindings.addBinding("value", new RDFTermLiteral(resourceAction.getValue()));
			
			update(bindings);
		}
		
	}

	@Override
	public void notifyRemoved(BindingsResults bindingsResults, String spuid, Integer sequence) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSubscribe(BindingsResults bindingsResults, String spuid) {
		notifyAdded(bindingsResults,spuid,0);	
		
	}

	@Override
	public void brokenSubscription() {
		// TODO Auto-generated method stub
		
	}
}

