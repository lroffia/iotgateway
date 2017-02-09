package arces.unibo.gateway.dispatching;

import java.util.ArrayList;
import java.util.HashMap;

import arces.unibo.gateway.mapping.ResourceAction;
import arces.unibo.SEPA.application.Logger;
import arces.unibo.SEPA.application.ApplicationProfile;
import arces.unibo.SEPA.application.Logger.VERBOSITY;
import arces.unibo.SEPA.commons.Bindings;
import arces.unibo.SEPA.commons.RDFTermLiteral;
import arces.unibo.SEPA.commons.RDFTermURI;
import arces.unibo.gateway.mapping.MPRequest;
import arces.unibo.gateway.mapping.MPResponse;

public class MPDispatcher implements MPMappingNotFoundListener {
	private static String tag = "MP DISPATCHER";
	
	private MPRequestDispatcher mpRequestDispatcher;
	private MPResponseDispatcher mpResponseDispatcher;
	private MPMapper mpMapper;
	private MPMap mpMap;
	private HashMap<ResourceAction,ArrayList<MPRequest>> requestMap = new HashMap<ResourceAction,ArrayList<MPRequest>>();
			
	public MPDispatcher(ApplicationProfile appProfile) {
		mpMap = new MPMap();
		mpRequestDispatcher = new MPRequestDispatcher(appProfile,mpMap,requestMap,this);
		mpResponseDispatcher = new MPResponseDispatcher(appProfile,mpMap,requestMap);
		mpMapper = new MPMapper(appProfile,mpMap);
	}
	
	public boolean start() {		
		if(!mpMapper.join()) return false;
		if(!mpRequestDispatcher.join()) return false;
		if(!mpResponseDispatcher.join()) return false;
		
		String subID = mpMapper.subscribe();
		
		if (subID == null) {
			Logger.log(VERBOSITY.FATAL,tag,"Mapper subscription FAILED");
			return false;
		}
		
		Logger.log(VERBOSITY.DEBUG,tag,"Mapper subscription\t"+subID);
		
		subID = mpRequestDispatcher.subscribe();
		
		if (subID == null) {
			Logger.log(VERBOSITY.FATAL,tag,"Request dispatcher subscription FAILED");
			return false;
		}
		
		Logger.log(VERBOSITY.DEBUG,tag,"Request dispatcher subscription\t"+subID);
		
		subID = mpResponseDispatcher.subscribe();
		
		if (subID == null) {
			Logger.log(VERBOSITY.FATAL,tag,"Response dispatcher subscription FAILED");
			return false;
		}
		
		Logger.log(VERBOSITY.DEBUG,tag,"Response dispatcher subscription\t"+subID);
		
		Logger.log(VERBOSITY.INFO,tag,"Started");
		
		return true;
	}
	
	public boolean stop(){				
		return true;
	}
	
		@Override
	public void mappingNotFound(MPRequest request) {
		MPResponse response = new MPResponse(request.getProtocol(),"MP-MAPPING NOT FOUND FOR "+request.toString());
		
		Bindings bindings = new Bindings();
		bindings.addBinding("request", new RDFTermURI(request.getURI()));
		bindings.addBinding("response", new RDFTermURI(response.getURI()));
		bindings.addBinding("value", new RDFTermLiteral(response.getResponseString()));
		bindings.addBinding("protocol", new RDFTermURI(response.getProtocol()));
		
		Logger.log(VERBOSITY.WARNING,tag,">> " + response.toString());
		
		mpResponseDispatcher.update(bindings);
		
	}
}
