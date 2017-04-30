package arces.unibo.gateway.dispatching;

import java.util.UUID;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import arces.unibo.SEPA.client.pattern.ApplicationProfile;

import arces.unibo.SEPA.commons.SPARQL.Bindings;
import arces.unibo.SEPA.commons.SPARQL.RDFTermLiteral;
import arces.unibo.SEPA.commons.SPARQL.RDFTermURI;
import arces.unibo.gateway.mapping.ResourceAction;

public class MNDispatcher implements MNMappingNotFoundListener {
	private static final Logger logger = LogManager.getLogger("MNDispatcher");
	
	private MNRequestDispatcher mnRequestDispatcher;
	private MNResponseDispatcher mnResponseDispatcher;
	private MNMapper mnMapper;
	private MNMap mnMap;
		
	public MNDispatcher(ApplicationProfile appProfile) {
		mnMap = new MNMap();
		mnRequestDispatcher = new MNRequestDispatcher(appProfile,mnMap,this);
		mnResponseDispatcher = new MNResponseDispatcher(appProfile,mnMap);
		mnMapper = new MNMapper(appProfile,mnMap);
	}
	
	public boolean start() {
		if(!mnMapper.join()) return false;
		if(!mnRequestDispatcher.join()) return false;
		if(!mnResponseDispatcher.join()) return false;
		
		String subID = mnMapper.subscribe();
		
		if (subID == null) {
			logger.fatal("Mapper subscription FAILED");
			return false;
		}
		
		logger.debug("Mapper subscription\t"+subID);
		
		subID = mnRequestDispatcher.subscribe();
		
		if (subID == null) {
			logger.fatal("Request dispatcher subscription FAILED");
			return false;
		}
		
		logger.debug("Request dispatcher subscription\t"+subID);
		
		subID = mnResponseDispatcher.subscribe();
		
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
	public void mappingNotFound(ResourceAction request) {
		ResourceAction response = new ResourceAction(request.getResourceURI(), request.getActionURI(), "MN-MAPPING NOT FOUND FOR "+ request.toString());
		
		Bindings bindings = new Bindings();
		
		bindings.addBinding("response", new RDFTermURI("iot:Resource-Response_"+UUID.randomUUID().toString()));
		bindings.addBinding("resource", new RDFTermURI(response.getResourceURI()));
		bindings.addBinding("action", new RDFTermURI(response.getActionURI()));
		bindings.addBinding("value", new RDFTermLiteral(response.getValue()));

		logger.warn( ">> Resource-Response "+response.toString());
		
		mnResponseDispatcher.update(bindings);
		
	}
}
