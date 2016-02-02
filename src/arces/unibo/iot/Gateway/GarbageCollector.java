package arces.unibo.iot.Gateway;

import java.util.ArrayList;
import java.util.Iterator;

import arces.unibo.iot.SEPA.Aggregator;
import arces.unibo.iot.SEPA.Bindings;
import arces.unibo.iot.SEPA.BindingsResults;

public class GarbageCollector extends Aggregator {
	private static String GARBAGE_DELETE = 
			" DELETE { "
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
		ArrayList<Bindings> bindings = notify.getAddedBindings();
		if (bindings == null) return;
		Iterator<Bindings> bindingsIt = bindings.iterator();
		while(bindingsIt.hasNext()) {
			Bindings garbage = bindingsIt.next();
			System.out.println("Garbage collector: "+garbage.toString());
			update(garbage);
		}
		
	}
	
	public BindingsResults start(){
		System.out.println("*********************");
		System.out.println("* Garbage Collector *");
		System.out.println("*********************");
		
		return subscribe(null);
	}
}
