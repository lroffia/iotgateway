package arces.unibo.gateway.dispatching;

import arces.unibo.SEPA.application.SEPALogger;

import java.util.UUID;

import arces.unibo.SEPA.application.ApplicationProfile;
import arces.unibo.SEPA.application.SEPALogger.VERBOSITY;
import arces.unibo.SEPA.commons.SPARQL.Bindings;
import arces.unibo.SEPA.commons.SPARQL.RDFTermLiteral;
import arces.unibo.SEPA.commons.SPARQL.RDFTermURI;
import arces.unibo.gateway.mapping.ResourceAction;

public class MNDispatcher implements MNMappingNotFoundListener {
	private static String tag = "MN DISPATCHER";
	
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
			SEPALogger.log(VERBOSITY.FATAL, tag,"Mapper subscription FAILED");
			return false;
		}
		
		SEPALogger.log(VERBOSITY.DEBUG, tag,"Mapper subscription\t"+subID);
		
		subID = mnRequestDispatcher.subscribe();
		
		if (subID == null) {
			SEPALogger.log(VERBOSITY.FATAL, tag,"Request dispatcher subscription FAILED");
			return false;
		}
		
		SEPALogger.log(VERBOSITY.DEBUG, tag,"Request dispatcher subscription\t"+subID);
		
		subID = mnResponseDispatcher.subscribe();
		
		if (subID == null) {
			SEPALogger.log(VERBOSITY.FATAL, tag,"Response dispatcher subscription FAILED");
			return false;
		}
		
		SEPALogger.log(VERBOSITY.DEBUG, tag,"Response dispatcher subscription\t"+subID);
		
		SEPALogger.log(VERBOSITY.INFO, tag,"Started");
		
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

		SEPALogger.log(VERBOSITY.WARNING, tag, ">> Resource-Response "+response.toString());
		
		mnResponseDispatcher.update(bindings);
		
	}
}
