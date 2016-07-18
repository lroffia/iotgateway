package arces.unibo.gateway;

import java.util.ArrayList;

import arces.unibo.SEPA.Aggregator;
import arces.unibo.SEPA.Bindings;
import arces.unibo.SEPA.BindingsResults;
import arces.unibo.SEPA.Consumer;
import arces.unibo.SEPA.Producer;
import arces.unibo.SEPA.SPARQLApplicationProfile;
import arces.unibo.tools.Logging;
import arces.unibo.tools.Logging.VERBOSITY;

class GarbageCollector {
	static String tag = "GARBAGE COLLECTOR";
	
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
			super(SPARQLApplicationProfile.subscribe("MP_REQUEST"));
		}

		public String subscribe() {return subscribe(null);}
		
		@Override
		public void notify(BindingsResults notify) {}

		@Override
		public void notifyAdded(ArrayList<Bindings> bindingsResults) {
			if(!monitor || listener == null) return;
			
			for (Bindings garbage : bindingsResults)
				listener.newMPRequest(
						garbage.getBindingValue("?protocol").getValue(),
						garbage.getBindingValue("?value").getValue());	
		}

		@Override
		public void notifyRemoved(ArrayList<Bindings> bindingsResults) {
			if(!monitor || listener == null) return;
			
			for (Bindings garbage : bindingsResults)
				listener.removedMPRequest(
						garbage.getBindingValue("?protocol").getValue(),
						garbage.getBindingValue("?value").getValue());		
		}

		@Override
		public void notifyFirst(ArrayList<Bindings> bindingsResults) {

		}
	}
	
	public class TriplesMonitor extends Consumer {
		private  long triplesNumber;
		
		public TriplesMonitor() {
			super(SPARQLApplicationProfile.subscribe("ALL"));
			triplesNumber = 0;
		}

		public String subscribe() {return subscribe(null);}
		
		@Override
		public void notify(BindingsResults notify) {}

		@Override
		public void notifyAdded(ArrayList<Bindings> bindingsResults) {
			triplesNumber += bindingsResults.size();
			if (listener != null) listener.totalTriples(triplesNumber);
			Logging.log(VERBOSITY.INFO, tag, "Total triples: "+triplesNumber);
		}

		@Override
		public void notifyRemoved(ArrayList<Bindings> bindingsResults) {
			triplesNumber -= bindingsResults.size();
			if (listener != null) listener.totalTriples(triplesNumber);
			Logging.log(VERBOSITY.INFO, tag, "Total triples: "+triplesNumber);
		}

		@Override
		public void notifyFirst(ArrayList<Bindings> bindingsResults) {
			triplesNumber = bindingsResults.size();
			if (listener != null) listener.totalTriples(triplesNumber);
			Logging.log(VERBOSITY.INFO, tag, "Initial triples: "+triplesNumber);
		}
	}
	
	class ResourcePendingRequestRemover extends Aggregator {
		public ResourcePendingRequestRemover() {
			super(SPARQLApplicationProfile.subscribe("RESOURCE_PENDING_REQUEST"), SPARQLApplicationProfile.delete("REQUEST"));
		}
	
		@Override
		public void notify(BindingsResults notify) {}
		
		public String subscribe() {return super.subscribe(null);}

		@Override
		public void notifyAdded(ArrayList<Bindings> bindingsResults) {
			if (monitor) return;
			
			Logging.log(VERBOSITY.DEBUG, tag, "DELETE RESOURCE PENDING REQUEST "+bindingsResults.toString());
			for (Bindings garbage : bindingsResults) update(garbage);
		}

		@Override
		public void notifyRemoved(ArrayList<Bindings> bindingsResults) {
			if (!monitor || listener == null)  return;
			
			for (Bindings garbage : bindingsResults) 
				listener.removedResourcePendingRequest(
						garbage.getBindingValue("?resource").getValue(), 
						garbage.getBindingValue("?action").getValue(), 
						garbage.getBindingValue("?value").getValue());	
		}

		@Override
		public void notifyFirst(ArrayList<Bindings> bindingsResults) {
			notifyAdded(bindingsResults);
		}
	}
	
	class Eraser extends Producer{
		public Eraser(){
			super(SPARQLApplicationProfile.delete("ALL"));
		}
		
		public boolean update() {
			return super.update(null);
			}
	}
	
	class MPResponseRemover extends Aggregator {
		public MPResponseRemover() {
			super(SPARQLApplicationProfile.subscribe("MP_RESPONSE"),SPARQLApplicationProfile.delete("REQUEST_RESPONSE"));
		}
	
		@Override
		public void notify(BindingsResults notify) {}
		
		public String subscribe() {return super.subscribe(null);}

		@Override
		public void notifyAdded(ArrayList<Bindings> bindingsResults) {
			if (monitor) return;
			
			Logging.log(VERBOSITY.DEBUG, tag, "DELETE MP RESPONSE "+bindingsResults.toString());
			
			for (Bindings garbage : bindingsResults) update(garbage);
		}

		@Override
		public void notifyRemoved(ArrayList<Bindings> bindingsResults) {
			if (!monitor || listener == null) return;
			
			for (Bindings garbage : bindingsResults)	
				listener.removedMPResponse(
						garbage.getBindingValue("?protocol").getValue(), 
						garbage.getBindingValue("?value").getValue());	
		}

		@Override
		public void notifyFirst(ArrayList<Bindings> bindingsResults) {notifyAdded(bindingsResults);}	
	}
	
	class ResourceResponseRemover extends Aggregator {		
		public ResourceResponseRemover() {
			super(SPARQLApplicationProfile.subscribe("RESOURCE_RESPONSE"), SPARQLApplicationProfile.delete("RESPONSE"));
		}
	
		@Override
		public void notify(BindingsResults notify) {}
		
		public String subscribe() {
			return super.subscribe(null);
		}

		@Override
		public void notifyAdded(ArrayList<Bindings> bindingsResults) {
			if (monitor) return;
			
			Logging.log(VERBOSITY.DEBUG, tag, "DELETE RESOURCE RESPONSE "+bindingsResults.toString());
			
			for (Bindings garbage : bindingsResults) update(garbage);			
		}

		@Override
		public void notifyRemoved(ArrayList<Bindings> bindingsResults) {
			if (!monitor || listener == null) return;
			
			for (Bindings garbage : bindingsResults)
				listener.removedResourceResponse(
						garbage.getBindingValue("?resource").getValue(), 
						garbage.getBindingValue("?action").getValue(), 
						garbage.getBindingValue("?value").getValue());
		}

		@Override
		public void notifyFirst(ArrayList<Bindings> bindingsResults) {
			notifyAdded(bindingsResults);			
		}	
	}
	
	class ResourceRequestRemover extends Aggregator {		
		public ResourceRequestRemover() {
			super(SPARQLApplicationProfile.subscribe("RESOURCE_REQUEST"), SPARQLApplicationProfile.delete("REQUEST"));
		}
	
		@Override
		public void notify(BindingsResults notify) {}
		
		public String subscribe() {
			return super.subscribe(null);
		}

		@Override
		public void notifyAdded(ArrayList<Bindings> bindingsResults) {
			if (monitor) return;
			
			Logging.log(VERBOSITY.DEBUG, tag, "DELETE RESOURCE REQUEST "+bindingsResults.toString());
			for (Bindings garbage : bindingsResults) update(garbage);
		}

		@Override
		public void notifyRemoved(ArrayList<Bindings> bindingsResults) {
			if(!monitor || listener == null) return;
			
			for (Bindings garbage : bindingsResults)
				listener.removedResourceRequest(
						garbage.getBindingValue("?resource").getValue(), 
						garbage.getBindingValue("?action").getValue(), 
						garbage.getBindingValue("?value").getValue());	
		}

		@Override
		public void notifyFirst(ArrayList<Bindings> bindingsResults) {
			notifyAdded(bindingsResults);			
		}		
	}
	
	class MNResponseRemover extends Aggregator {		
		public MNResponseRemover() {
			super(SPARQLApplicationProfile.subscribe("MN_RESPONSE"), SPARQLApplicationProfile.delete("RESPONSE"));
		}
	
		@Override
		public void notify(BindingsResults notify) {}
		
		public String subscribe() {
			return super.subscribe(null);
		}

		@Override
		public void notifyAdded(ArrayList<Bindings> bindingsResults) {
			if (monitor) return;
				
			Logging.log(VERBOSITY.DEBUG, tag, "DELETE MN RESPONSE "+bindingsResults.toString());
			for (Bindings garbage : bindingsResults) update(garbage);
		}

		@Override
		public void notifyRemoved(ArrayList<Bindings> bindingsResults) {
			if (!monitor || listener == null) return;
			
			for (Bindings garbage : bindingsResults)
				listener.removedMNResponse(
						garbage.getBindingValue("?network").getValue(), 
						garbage.getBindingValue("?value").getValue());
		}

		@Override
		public void notifyFirst(ArrayList<Bindings> bindingsResults) {
			notifyAdded(bindingsResults);			
		}		
	}
	
	class MNRequestRemover extends Aggregator {		
		public MNRequestRemover() {
			super(SPARQLApplicationProfile.subscribe("MN_REQUEST"), SPARQLApplicationProfile.delete("REQUEST"));
		}
	
		@Override
		public void notify(BindingsResults notify) {}
		
		public String subscribe() {
			return super.subscribe(null);
		}

		@Override
		public void notifyAdded(ArrayList<Bindings> bindingsResults) {
			if (monitor) return;
			
			Logging.log(VERBOSITY.DEBUG, tag, "DELETE MN REQUEST "+bindingsResults.toString());
			for (Bindings garbage : bindingsResults) update(garbage);
		}

		@Override
		public void notifyRemoved(ArrayList<Bindings> bindingsResults) {
			if (!monitor || listener == null) return;
				
			for (Bindings garbage : bindingsResults)
				listener.removedMNRequest(
						garbage.getBindingValue("?network").getValue(), 
						garbage.getBindingValue("?value").getValue());	
		}

		@Override
		public void notifyFirst(ArrayList<Bindings> bindingsResults) {
			notifyAdded(bindingsResults);			
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
			Logging.log(VERBOSITY.FATAL, tag, "Resource pending request subscription FAILED");
			return false;
		}
		Logging.log(VERBOSITY.DEBUG, tag,"Resource pending request subscription \t"+subID);
		
		subID = mpResponse.subscribe();
		if (subID == null) {
			Logging.log(VERBOSITY.FATAL, tag,"MP Response subscription FAILED");
			return false;
		}
		Logging.log(VERBOSITY.DEBUG, tag,"MP Response subscription \t"+subID);
		
		subID = resourceResponse.subscribe();
		if (subID == null) {
			Logging.log(VERBOSITY.FATAL, tag,"Resource response subscription FAILED");
			return false;
		}
		Logging.log(VERBOSITY.DEBUG, tag,"Resource response subscription\t"+subID);
		
		subID = resourceRequest.subscribe();
		if (subID == null) {
			Logging.log(VERBOSITY.FATAL, tag,"Resource request subscription FAILED");
			return false;
		}
		Logging.log(VERBOSITY.DEBUG, tag,"Resource request subscription\t"+subID);
		
		subID = mnResponse.subscribe();
		if (subID == null) {
			Logging.log(VERBOSITY.FATAL, tag,"MN Response subscription FAILED");
			return false;
		}
		Logging.log(VERBOSITY.DEBUG, tag,"MN Response subscription \t"+subID);
		
		subID = mnRequest.subscribe();
		if (subID == null) {
			Logging.log(VERBOSITY.FATAL, tag,"MN Request subscription FAILED");
			return false;
		}
		Logging.log(VERBOSITY.DEBUG, tag,"MN Response subscription\t"+subID);
		
		subID = triplesMonitor.subscribe();
		if (subID == null) {
			Logging.log(VERBOSITY.FATAL, tag,"Triples monitor subscription FAILED");
			return false;
		}
		Logging.log(VERBOSITY.DEBUG, tag,"Triples monitor subscription\t"+subID);
		
		subID = mpRequestMonitor.subscribe();
		if (subID == null) {
			Logging.log(VERBOSITY.FATAL, tag,"MP-Request monitor subscription FAILED");
			return false;
		}
		Logging.log(VERBOSITY.DEBUG, tag,"MP-Request monitor subscription\t"+subID);
		
		Logging.log(VERBOSITY.INFO, tag,"Started");
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