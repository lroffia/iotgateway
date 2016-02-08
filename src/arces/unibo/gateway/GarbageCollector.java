package arces.unibo.gateway;

import arces.unibo.SEPA.Aggregator;
import arces.unibo.SEPA.Bindings;
import arces.unibo.SEPA.BindingsResults;

public class GarbageCollector extends Aggregator {
	private static String GARBAGE_DELETE = 
			" DELETE DATA { "
			+ "?mprequest rdf:type iot:MP-Request . "
			+ "?iotrequest rdf:type iot:IoT-Request . "
			+ "?mnrequest rdf:type iot:MN-Request . "
			+ "?mnresponse rdf:type iot:MN-Response . "
			+ "?iotresponse rdf:type iot:IoT-Response . "
			+ "?mpresponse rdf:type iot:MP-Response . "
			
			+ "?mprequest iot:hasProtocol ?protocol . "
			+ "?mprequest iot:hasMPRequestString ?mpRequestValue . "
			+ "?mprequest iot:hasMPResponse ?mpresponse . "
			
			+ "?iotrequest iot:hasValue ?iotRequestValue . "
			+ "?iotrequest iot:hasAction ?action . "
			+ "?iotrequest iot:hasContext ?context . "
						
			+ "?mnrequest iot:hasNetwork ?network . "
			+ "?mnrequest iot:hasMNRequestString ?mnRequestValue . "
		
			+ "?mnresponse iot:hasNetwork ?network . "
			+ "?mnresponse iot:hasMNResponseString ?mnResponseValue . "
			
			+ "?iotresponse iot:hasAction ?action . "
			+ "?iotresponse iot:hasContext ?context . "
			+ "?iotresponse iot:hasValue ?iotResponseValue . "
			
			+ "?mpresponse iot:hasMPResponseString ?mpResponseValue"

			+ " }";
	
	private static String GARBAGE_SUBSCRIBE = 
			" SELECT  ?mprequest ?iotrequest ?mnrequest ?mnresponse ?iotresponse "
			+ "?mpresponse ?protocol ?mpRequestValue ?iotRequestValue  "
			+ " ?action ?context ?network ?mnRequestValue ?mnResponseValue ?iotResponseValue ?mpResponseValue WHERE { "
			+ "?mprequest rdf:type iot:MP-Request . "
			+ "?iotrequest rdf:type iot:IoT-Request . "
			+ "?mnrequest rdf:type iot:MN-Request . "
			+ "?mnresponse rdf:type iot:MN-Response . "
			+ "?iotresponse rdf:type iot:IoT-Response . "
			+ "?mpresponse rdf:type iot:MP-Response . "
			
			+ "?mprequest iot:hasProtocol ?protocol . "
			+ "?mprequest iot:hasMPRequestString ?mpRequestValue . "
			+ "?mprequest iot:hasMPResponse ?mpresponse . "
			
			+ "?iotrequest iot:hasValue ?iotRequestValue . "
			+ "?iotrequest iot:hasAction ?action . "
			+ "?iotrequest iot:hasContext ?context . "
						
			+ "?mnrequest iot:hasNetwork ?network . "
			+ "?mnrequest iot:hasMNRequestString ?mnRequestValue . "
		
			+ "?mnresponse iot:hasNetwork ?network . "
			+ "?mnresponse iot:hasMNResponseString ?mnResponseValue . "
			
			+ "?iotresponse iot:hasAction ?action . "
			+ "?iotresponse iot:hasContext ?context . "
			+ "?iotresponse iot:hasValue ?iotResponseValue . "
			
			+ "?mpresponse iot:hasMPResponseString ?mpResponseValue"

			+ " }";
	
	public GarbageCollector() {
		super(GARBAGE_SUBSCRIBE, GARBAGE_DELETE);
	}

	@Override
	public void notify(BindingsResults notify) {
		for (Bindings garbage : notify.getAddedBindings()){
			System.out.println("Garbage collector: "+garbage.toString());
			update(garbage);
		}
	}
	
	public boolean start(){
		System.out.println("*********************");
		System.out.println("* Garbage Collector *");
		System.out.println("*********************");
		
		if(!super.start()) return false;
		
		BindingsResults ret = subscribe(null);
		notify(ret);
		
		return true;
	}
}
