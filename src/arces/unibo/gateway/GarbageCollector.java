package arces.unibo.gateway;

import arces.unibo.SEPA.application.Aggregator;
import arces.unibo.SEPA.application.Consumer;
import arces.unibo.SEPA.application.Logger;
import arces.unibo.SEPA.application.Producer;
import arces.unibo.SEPA.commons.ARBindingsResults;
import arces.unibo.SEPA.commons.Bindings;
import arces.unibo.SEPA.commons.BindingsResults;
import arces.unibo.SEPA.application.ApplicationProfile;
import arces.unibo.SEPA.application.Logger.VERBOSITY;

class GarbageCollector {
	static String tag = "GARBAGE COLLECTOR";

	public ApplicationProfile appProfile;
	
	private ResourcePendingRequestRemover resourcePendingRequest;
	private MPResponseRemover mpResponse;
	private ResourceResponseRemover resourceResponse;
	private ResourceRequestRemover resourceRequest;
	private MNResponseRemover mnResponse;
	private MNRequestRemover mnRequest;
	private Eraser eraser;
	private TriplesMonitor triplesMonitor;
	private MPRequestMonitor mpRequestMonitor;
	
	private boolean monitor = false;
	
	private GarbageCollectorListener listener;
	
	public GarbageCollector(ApplicationProfile appProfile) {
		this.appProfile = appProfile;
	}
	
	public void setListener(GarbageCollectorListener listener) {this.listener = listener;}
		
	public interface GarbageCollectorListener {
		public void newMPRequest(String protocol, String value);
		
		public void removedResourcePendingRequest(String resource,String action, String value);
		public void removedResourceRequest(String resource,String action, String value);
		public void removedResourceResponse(String resource,String action, String value);
		public void removedMPResponse(String protocol, String value);
		public void removedMNResponse(String network, String value);
		public void removedMNRequest(String network, String value);
		public void removedMPRequest(String protocol, String value);
		
		public void totalTriples(long triples);
	}
	
	public class MPRequestMonitor extends Consumer {
		public MPRequestMonitor() {
			super(appProfile,"MP_REQUEST");
		}

		public String subscribe() {return subscribe(null);}

		@Override
		public void notify(ARBindingsResults notify, String spuid, Integer sequence) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void notifyAdded(BindingsResults bindingsResults, String spuid, Integer sequence) {
			if(!monitor || listener == null) return;
			
			for (Bindings garbage : bindingsResults.getBindings())
				listener.newMPRequest(
						garbage.getBindingValue("protocol"),
						garbage.getBindingValue("value"));	
			
		}

		@Override
		public void notifyRemoved(BindingsResults bindingsResults, String spuid, Integer sequence) {
			if(!monitor || listener == null) return;
			
			for (Bindings garbage : bindingsResults.getBindings())
				listener.removedMPRequest(
						garbage.getBindingValue("protocol"),
						garbage.getBindingValue("value"));		
			
		}

		@Override
		public void onSubscribe(BindingsResults bindingsResults, String spuid) {
			// TODO Auto-generated method stub
			
		}
	}
	
	public class TriplesMonitor extends Consumer {
		private  long triplesNumber;
		
		public TriplesMonitor() {
			super(appProfile,"ALL");
			triplesNumber = 0;
		}

		public String subscribe() {return subscribe(null);}

		@Override
		public void notify(ARBindingsResults notify, String spuid, Integer sequence) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void notifyAdded(BindingsResults bindingsResults, String spuid, Integer sequence) {
			triplesNumber += bindingsResults.size();
			if (listener != null) listener.totalTriples(triplesNumber);
			Logger.log(VERBOSITY.DEBUG, tag, "Total triples: "+triplesNumber);
			
		}

		@Override
		public void notifyRemoved(BindingsResults bindingsResults, String spuid, Integer sequence) {
			triplesNumber -= bindingsResults.size();
			if (listener != null) listener.totalTriples(triplesNumber);
			Logger.log(VERBOSITY.DEBUG, tag, "Total triples: "+triplesNumber);
		}

		@Override
		public void onSubscribe(BindingsResults bindingsResults, String spuid) {
			triplesNumber = bindingsResults.size();
			if (listener != null) listener.totalTriples(triplesNumber);
			Logger.log(VERBOSITY.DEBUG, tag, "Initial triples: "+triplesNumber);
		}
	}
	
	class ResourcePendingRequestRemover extends Aggregator {
		public ResourcePendingRequestRemover() {
			super(appProfile,"RESOURCE_PENDING_REQUEST", "DELETE_RESOURCE_PENDING_REQUEST");
		}

		@Override
		public void notify(ARBindingsResults notify, String spuid, Integer sequence) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void notifyAdded(BindingsResults bindingsResults, String spuid, Integer sequence) {
			if (monitor) return;
			
			Logger.log(VERBOSITY.DEBUG, tag, "DELETE RESOURCE PENDING REQUEST "+bindingsResults.toString());
			for (Bindings garbage : bindingsResults.getBindings()) update(garbage);
			
		}

		@Override
		public void notifyRemoved(BindingsResults bindingsResults, String spuid, Integer sequence) {
			if (!monitor || listener == null)  return;
			
			for (Bindings garbage : bindingsResults.getBindings()) 
				listener.removedResourcePendingRequest(
						garbage.getBindingValue("resource"), 
						garbage.getBindingValue("action"), 
						garbage.getBindingValue("value"));	
			
		}

		@Override
		public void onSubscribe(BindingsResults bindingsResults, String spuid) {
			// TODO Auto-generated method stub
			
		}

		public String subscribe() {
			return super.subscribe(null);
		}
	}
	
	class Eraser extends Producer{
		public Eraser(){
			super(appProfile,"DELETE_ALL");
		}
		
		public boolean update() {
			return super.update(null);
			}
	}
	
	class MPResponseRemover extends Aggregator {
		public MPResponseRemover() {
			super(appProfile,"MP_RESPONSE","DELETE_REQUEST_RESPONSE");
		}
			
		public String subscribe() {return super.subscribe(null);}

		@Override
		public void notifyAdded(BindingsResults bindingsResults, String spuid, Integer sequence)  {
			if (monitor) return;
			
			Logger.log(VERBOSITY.DEBUG, tag, "DELETE MP RESPONSE "+bindingsResults.toString());
			
			for (Bindings garbage : bindingsResults.getBindings()) update(garbage);
		}

		@Override
		public void notifyRemoved(BindingsResults bindingsResults, String spuid, Integer sequence) {
			if (!monitor || listener == null) return;
			
			for (Bindings garbage : bindingsResults.getBindings())	
				listener.removedMPResponse(
						garbage.getBindingValue("protocol"), 
						garbage.getBindingValue("value"));	
		}

		@Override
		public void notify(ARBindingsResults notify, String spuid, Integer sequence) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSubscribe(BindingsResults bindingsResults, String spuid) {
			// TODO Auto-generated method stub
			
		}

	}
	
	class ResourceResponseRemover extends Aggregator {		
		public ResourceResponseRemover() {
			super(appProfile,"RESOURCE_RESPONSE", "DELETE_RESOURCE_RESPONSE");
		}

		@Override
		public void notify(ARBindingsResults notify, String spuid, Integer sequence) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void notifyAdded(BindingsResults bindingsResults, String spuid, Integer sequence) {
			if (monitor) return;
			
			Logger.log(VERBOSITY.INFO, tag, "DELETE RESOURCE RESPONSE "+bindingsResults.toString());
			
			for (Bindings garbage : bindingsResults.getBindings()) update(garbage);	
			
		}

		@Override
		public void notifyRemoved(BindingsResults bindingsResults, String spuid, Integer sequence) {
			if (!monitor || listener == null) return;
			
			for (Bindings garbage : bindingsResults.getBindings())
				listener.removedResourceResponse(
						garbage.getBindingValue("resource"), 
						garbage.getBindingValue("action"), 
						garbage.getBindingValue("value"));
			
		}

		@Override
		public void onSubscribe(BindingsResults bindingsResults, String spuid) {
			notifyAdded(bindingsResults,spuid,0);	
		}	
		
		public String subscribe() {
			return super.subscribe(null);
		}
	}
	
	class ResourceRequestRemover extends Aggregator {		
		public ResourceRequestRemover() {
			super(appProfile,"RESOURCE_REQUEST", "DELETE_RESOURCE_REQUEST");
		}
		
		public String subscribe() {
			return super.subscribe(null);
		}

		@Override
		public void notify(ARBindingsResults notify, String spuid, Integer sequence) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void notifyAdded(BindingsResults bindingsResults, String spuid, Integer sequence) {
			if (monitor) return;
			
			Logger.log(VERBOSITY.DEBUG, tag, "DELETE RESOURCE REQUEST "+bindingsResults.toString());
			for (Bindings garbage : bindingsResults.getBindings()) update(garbage);
			
		}

		@Override
		public void notifyRemoved(BindingsResults bindingsResults, String spuid, Integer sequence) {
			if(!monitor || listener == null) return;
			
			for (Bindings garbage : bindingsResults.getBindings())
				listener.removedResourceRequest(
						garbage.getBindingValue("resource"), 
						garbage.getBindingValue("action"), 
						garbage.getBindingValue("value"));	
			
		}

		@Override
		public void onSubscribe(BindingsResults bindingsResults, String spuid) {
			// TODO Auto-generated method stub
			
		}		
	}
	
	class MNResponseRemover extends Aggregator {		
		public MNResponseRemover() {
			super(appProfile,"MN_RESPONSE", "DELETE_MN_RESPONSE");
		}
		
		public String subscribe() {
			return super.subscribe(null);
		}

		@Override
		public void notify(ARBindingsResults notify, String spuid, Integer sequence) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void notifyAdded(BindingsResults bindingsResults, String spuid, Integer sequence) {
			if (monitor) return;
			
			Logger.log(VERBOSITY.DEBUG, tag, "DELETE MN RESPONSE "+bindingsResults.toString());
			for (Bindings garbage : bindingsResults.getBindings()) update(garbage);	
		}

		@Override
		public void notifyRemoved(BindingsResults bindingsResults, String spuid, Integer sequence) {
			if (!monitor || listener == null) return;
			
			for (Bindings garbage : bindingsResults.getBindings())
				listener.removedMNResponse(
						garbage.getBindingValue("network"), 
						garbage.getBindingValue("value"));
		}

		@Override
		public void onSubscribe(BindingsResults bindingsResults, String spuid) {
			notifyAdded(bindingsResults,spuid,0);
		}		
	}
	
	class MNRequestRemover extends Aggregator {		
		public MNRequestRemover() {
			super(appProfile,"MN_REQUEST", "DELETE_MN_REQUEST");
		}
	
		@Override
		public void notify(ARBindingsResults notify, String spuid, Integer sequence)  {}
		
		public String subscribe() {
			return super.subscribe(null);
		}

		@Override
		public void notifyAdded(BindingsResults bindingsResults, String spuid, Integer sequence) {
			if (monitor) return;
			
			Logger.log(VERBOSITY.DEBUG, tag, "DELETE MN REQUEST "+bindingsResults.toString());
			for (Bindings garbage : bindingsResults.getBindings()) update(garbage);
		}

		@Override
		public void notifyRemoved(BindingsResults bindingsResults, String spuid, Integer sequence) {
			if (!monitor || listener == null) return;
				
			for (Bindings garbage : bindingsResults.getBindings())
				listener.removedMNRequest(
						garbage.getBindingValue("network"), 
						garbage.getBindingValue("value"));	
		}

		@Override
		public void onSubscribe(BindingsResults bindingsResults, String spuid) {
			notifyAdded(bindingsResults,spuid,0);			
		}		
	}
	
	public boolean start(boolean erase,boolean monitor) {	
		this.monitor = monitor;
		
		eraser = new Eraser();
		if (!eraser.join()) return false;
		if (erase) if (!eraser.update()) return false;
		
		resourcePendingRequest = new ResourcePendingRequestRemover();
		mpResponse = new MPResponseRemover();
		resourceResponse = new ResourceResponseRemover();
		resourceRequest = new ResourceRequestRemover();
		mnRequest = new MNRequestRemover();
		triplesMonitor = new TriplesMonitor();
		mnResponse = new MNResponseRemover();
		mpRequestMonitor = new MPRequestMonitor();
		
		if (!resourcePendingRequest.join()) return false;
		if (!mpResponse.join()) return false;
		if (!resourceResponse.join()) return false;
		if (!resourceRequest.join()) return false;
		if (!mnResponse.join()) return false;
		if (!mnRequest.join()) return false;
		if (!triplesMonitor.join()) return false;
		if (!mpRequestMonitor.join()) return false;
		
		String subID = resourcePendingRequest.subscribe();
		if (subID == null) {
			Logger.log(VERBOSITY.FATAL, tag, "Resource pending request subscription FAILED");
			return false;
		}
		Logger.log(VERBOSITY.DEBUG, tag,"Resource pending request subscription \t"+subID);
		
		subID = mpResponse.subscribe();
		if (subID == null) {
			Logger.log(VERBOSITY.FATAL, tag,"MP Response subscription FAILED");
			return false;
		}
		Logger.log(VERBOSITY.DEBUG, tag,"MP Response subscription \t"+subID);
		
		subID = resourceResponse.subscribe();
		if (subID == null) {
			Logger.log(VERBOSITY.FATAL, tag,"Resource response subscription FAILED");
			return false;
		}
		Logger.log(VERBOSITY.DEBUG, tag,"Resource response subscription\t"+subID);
		
		subID = resourceRequest.subscribe();
		if (subID == null) {
			Logger.log(VERBOSITY.FATAL, tag,"Resource request subscription FAILED");
			return false;
		}
		Logger.log(VERBOSITY.DEBUG, tag,"Resource request subscription\t"+subID);
		
		subID = mnResponse.subscribe();
		if (subID == null) {
			Logger.log(VERBOSITY.FATAL, tag,"MN Response subscription FAILED");
			return false;
		}
		Logger.log(VERBOSITY.DEBUG, tag,"MN Response subscription \t"+subID);
		
		subID = mnRequest.subscribe();
		if (subID == null) {
			Logger.log(VERBOSITY.FATAL, tag,"MN Request subscription FAILED");
			return false;
		}
		Logger.log(VERBOSITY.DEBUG, tag,"MN Response subscription\t"+subID);
		
		subID = triplesMonitor.subscribe();
		if (subID == null) {
			Logger.log(VERBOSITY.FATAL, tag,"Triples monitor subscription FAILED");
			return false;
		}
		Logger.log(VERBOSITY.DEBUG, tag,"Triples monitor subscription\t"+subID);
		
		subID = mpRequestMonitor.subscribe();
		if (subID == null) {
			Logger.log(VERBOSITY.FATAL, tag,"MP-Request monitor subscription FAILED");
			return false;
		}
		Logger.log(VERBOSITY.DEBUG, tag,"MP-Request monitor subscription\t"+subID);
		
		Logger.log(VERBOSITY.INFO, tag,"Started");
		return true;
	}
	
	public boolean stop(){
		if (resourcePendingRequest == null) return false;
		boolean ret = resourcePendingRequest.unsubscribe();	
		ret = ret && mpResponse.unsubscribe();
		ret = ret && resourceResponse.unsubscribe();
		ret = ret && resourceRequest.unsubscribe();
		ret = ret && mnResponse.unsubscribe();
		ret = ret && mnRequest.unsubscribe();
		ret = ret && triplesMonitor.unsubscribe();
		ret = ret && mpRequestMonitor.unsubscribe();
		return ret;
	}
}