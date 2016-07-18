package arces.unibo.gateway.adapters.network;

import java.util.ArrayList;
import java.util.UUID;

import arces.unibo.SEPA.Aggregator;
import arces.unibo.SEPA.BindingLiteralValue;
import arces.unibo.SEPA.BindingURIValue;
import arces.unibo.SEPA.Bindings;
import arces.unibo.SEPA.BindingsResults;
import arces.unibo.SEPA.SPARQLApplicationProfile;
import arces.unibo.tools.Logging;
import arces.unibo.tools.Logging.VERBOSITY;

public abstract class MNAdapter {
	private MNRequestResponseDispatcher dispatcher;
	
	//Abstract methods
	public abstract String networkURI();
	protected abstract void mnRequest(String request);
	protected abstract boolean doStart();
	protected abstract void doStop();
	public abstract String adapterName();
	
	public MNAdapter() {
		dispatcher = new MNRequestResponseDispatcher();	
	}
	
	public MNAdapter(String SIB_IP,int SIB_PORT,String SIB_NAME){
		dispatcher = new MNRequestResponseDispatcher(SIB_IP, SIB_PORT,SIB_NAME);
	}
	
	class MNRequestResponseDispatcher extends Aggregator {
		
		public MNRequestResponseDispatcher(){
			super(SPARQLApplicationProfile.subscribe("MN_REQUEST"),SPARQLApplicationProfile.insert("MN_RESPONSE"));
		}
		
		public MNRequestResponseDispatcher(String SIB_IP,int SIB_PORT,String SIB_NAME){
			super(SPARQLApplicationProfile.subscribe("MN_REQUEST"),SPARQLApplicationProfile.insert("MN_RESPONSE"),SIB_IP, SIB_PORT,SIB_NAME);
		}
		
		public String  subscribe(){
			//SPARQL SUBSCRIBE
			Bindings bindings = new Bindings();
			bindings.addBinding("?network", new BindingURIValue(networkURI()));
			
			return subscribe(bindings);
		}
		
		@Override
		public void notify(BindingsResults notify) {

		}

		@Override
		public void notifyAdded(ArrayList<Bindings> bindings) {
			for (Bindings binding : bindings){
				mnRequest(binding.getBindingValue("?value").getValue());
			}
		}

		@Override
		public void notifyRemoved(ArrayList<Bindings> bindings) {
			
		}

		@Override
		public void notifyFirst(ArrayList<Bindings> bindingsResults) {
			notifyAdded(bindingsResults);
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
		if (!dispatcher.join()) {
			Logging.log(VERBOSITY.FATAL,adapterName(),"Join FAILED");
			return false;
		}
		
		String subID = dispatcher.subscribe();
		
		if (subID == null) {
			Logging.log(VERBOSITY.FATAL,adapterName(),"Subscription FAILED");
			return false;
		}
		
		Logging.log(VERBOSITY.DEBUG,adapterName(),"Subscription "+subID);
		
		return doStart();
	}
	
	public boolean stop(){
		doStop();
		return dispatcher.unsubscribe();
	}
}
