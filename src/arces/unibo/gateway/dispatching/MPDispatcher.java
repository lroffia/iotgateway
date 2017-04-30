package arces.unibo.gateway.dispatching;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import arces.unibo.gateway.mapping.ResourceAction;
import arces.unibo.SEPA.client.pattern.ApplicationProfile;

import arces.unibo.SEPA.commons.SPARQL.Bindings;
import arces.unibo.SEPA.commons.SPARQL.RDFTermLiteral;
import arces.unibo.SEPA.commons.SPARQL.RDFTermURI;
import arces.unibo.gateway.mapping.MPRequest;
import arces.unibo.gateway.mapping.MPResponse;

public class MPDispatcher implements MPMappingNotFoundListener {
	private static final Logger logger = LogManager.getLogger("MPDispatcher");
	
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
			logger.fatal("Mapper subscription FAILED");
			return false;
		}
		
		logger.debug("Mapper subscription\t"+subID);
		
		subID = mpRequestDispatcher.subscribe();
		
		if (subID == null) {
			logger.fatal("Request dispatcher subscription FAILED");
			return false;
		}
		
		logger.debug("Request dispatcher subscription\t"+subID);
		
		subID = mpResponseDispatcher.subscribe();
		
		if (subID == null) {
			logger.fatal("Response dispatcher subscription FAILED");
			return false;
		}
		
		logger.debug("Response dispatcher subscription\t"+subID);
		
		logger.info("Started");
		
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
		
		logger.warn(">> " + response.toString());
		
		mpResponseDispatcher.update(bindings);
		
	}
}
