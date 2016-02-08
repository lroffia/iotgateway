package arces.unibo.gateway;

import java.util.UUID;

import arces.unibo.SEPA.BindingLiteralValue;
import arces.unibo.SEPA.BindingURIValue;
import arces.unibo.SEPA.Bindings;
import arces.unibo.SEPA.Producer;

public class MNMappingManager {

	private MappingCreator creator;
	private MappingRemover remover;
	
	private class MappingCreator extends Producer {

		private static final String INSERT_MAPPING =
				" INSERT DATA { "
						+ "?mapping rdf:type iot:MN-Mapping . "
						+ "?mapping iot:hasNetwork ?network . "
						+ "?mapping iot:hasContext ?context . "
						+ "?mapping iot:hasAction ?action . "
						+ "?mapping iot:hasValue ?value . "
						+ "?mapping iot:hasMNRequestPattern ?requestPattern . "
						+ "?mapping iot:hasMNResponsePattern ?responsePattern"
						+ " }";
		
		public MappingCreator() {
			super(INSERT_MAPPING);
			// TODO Auto-generated constructor stub
		}
		
		public boolean addMapping(String protocol,String requestPattern,String responsePattern,String context,String action,String value){
			Bindings bindings = new Bindings();
			String mapping = "iot:MN-Mapping_"+UUID.randomUUID().toString();
			bindings.addBinding("?mapping", new BindingURIValue(mapping));
			bindings.addBinding("?network", new BindingURIValue(protocol));
			bindings.addBinding("?context", new BindingURIValue(context));
			bindings.addBinding("?action", new BindingURIValue(action));
			bindings.addBinding("?value", new BindingLiteralValue(value));
			bindings.addBinding("?requestPattern", new BindingLiteralValue(requestPattern));
			bindings.addBinding("?responsePattern", new BindingLiteralValue(responsePattern));
			
			return update(bindings);
		}
	}
	
	private class MappingRemover extends Producer {
		private static final String DELETE_MAPPING =
				" DELETE { "
						+ "?mapping rdf:type iot:MN-Mapping . "
						+ "?mapping iot:hasNetwork ?network . "
						+ "?mapping iot:hasContext ?context . "
						+ "?mapping iot:hasAction ?action . "
						+ "?mapping iot:hasValue ?value . "
						+ "?mapping iot:hasMNRequestPattern ?requestPattern . "
						+ "?mapping iot:hasMNResponsePattern ?responsePattern"
				+ " } WHERE { "
						+ "?mapping rdf:type iot:MN-Mapping . "
						+ "?mapping iot:hasNetwork ?network . "
						+ "?mapping iot:hasContext ?context . "
						+ "?mapping iot:hasAction ?action . "
						+ "?mapping iot:hasValue ?value . "
						+ "?mapping iot:hasMNRequestPattern ?requestPattern . "
						+ "?mapping iot:hasMNResponsePattern ?responsePattern"
				+ " }";
		
		public MappingRemover() {
			super(DELETE_MAPPING);
			// TODO Auto-generated constructor stub
		}
		
		public boolean removeMapping(String protocol,String requestPattern,String responsePattern,String context,String action,String value){
			Bindings bindings = new Bindings();
			bindings.addBinding("?network", new BindingURIValue(protocol));
			bindings.addBinding("?context", new BindingURIValue(context));
			bindings.addBinding("?action", new BindingURIValue(action));
			bindings.addBinding("?value", new BindingLiteralValue(value));
			bindings.addBinding("?requestPattern", new BindingLiteralValue(requestPattern));
			bindings.addBinding("?responsePattern", new BindingLiteralValue(responsePattern));
			
			return update(bindings);
		}
		
		public boolean removeAllMapping(){
			return update(null);
		}
		
	}

	public MNMappingManager() {
		creator = new MappingCreator();
		remover = new MappingRemover();
	}
	
	public boolean removeAllMapping(){
		return remover.removeAllMapping();
	}
	
	public boolean removeMapping(String protocol,String requestPattern,String responsePattern,String context,String action,String value){
		return remover.removeMapping(protocol, requestPattern, responsePattern, context, action, value);
	}
	
	public boolean addMapping(String protocol,String requestPattern,String responsePattern,String context,String action,String value){
		return creator.addMapping(protocol, requestPattern, responsePattern, context, action, value);
	}
	
	public boolean start(){
		if(!creator.start()) return false;
		if(!remover.start()) return false;
		
		return true;
	}

}