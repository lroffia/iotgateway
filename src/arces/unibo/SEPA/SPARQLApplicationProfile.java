package arces.unibo.SEPA;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import arces.unibo.tools.Logging;
import arces.unibo.tools.Logging.VERBOSITY;

public class SPARQLApplicationProfile {	
	private static final String tag ="SPARQL PARSER";
	
	private static HashMap<String,String> updateMap = new HashMap<>();
	private static HashMap<String,String> subscribeMap = new HashMap<>();
	private static HashMap<String,String> insertMap = new HashMap<>();
	private static HashMap<String,String> deleteMap = new HashMap<>();
	
	private static HashMap<String,Bindings> updateBindingsMap = new HashMap<>();
	private static HashMap<String,Bindings> subscribeBindingsMap = new HashMap<>();
	private static HashMap<String,Bindings> insertBindingsMap = new HashMap<>();
	private static HashMap<String,Bindings> deleteBindingsMap = new HashMap<>();
	
	private static HashMap<String,String> namespaceMap = new HashMap<>();
	
	static Document doc = null;
	
	public static Set<String> getSubscribeIds() {return subscribeMap.keySet();}
	public static Set<String> getUpdateIds() {return updateMap.keySet();}
	public static Set<String> getInsertIds() {return insertMap.keySet();}
	public static Set<String> getDeleteIds() {return deleteMap.keySet();}
	public static Set<String> getPrefixes() {return namespaceMap.keySet();}
	
	public static String getNamespaceURI(String prefix) {
		String ret = namespaceMap.get(prefix);
		if (ret == null) Logging.log(VERBOSITY.ERROR, tag, "Prefix " + prefix + " NOT FOUND");
		return ret;
	}
	public static Bindings subscribeBindings(String id) {
		return subscribeBindingsMap.get(id);
	}
	
	public static Bindings updateBindings(String id) {
		return updateBindingsMap.get(id);
	}
	
	public static Bindings insertBindings(String id) {
		return insertBindingsMap.get(id);
	}
	
	public static Bindings deleteBindings(String id) {
		return deleteBindingsMap.get(id);
	}
	
	public static String subscribe(String id) {
		if (!subscribeMap.containsKey(id)) {
			Logging.log(VERBOSITY.ERROR, tag, "SUBSCRIBE ID <" + id + "> NOT FOUND");
			return null;
		}
		return subscribeMap.get(id);
	}
	
	public static String update(String id) {
		if (!updateMap.containsKey(id)) {
			Logging.log(VERBOSITY.ERROR, tag, "UPDATE ID <" + id + "> NOT FOUND");
			return null;
		}
		return updateMap.get(id);
	}
	
	public static String insert(String id) {
		if (!insertMap.containsKey(id)) {
			Logging.log(VERBOSITY.ERROR, tag, "INSERT ID <" + id + "> NOT FOUND");
			return null;
		}
		return insertMap.get(id);
	}
	
	public static String delete(String id) {
		if (!deleteMap.containsKey(id)) {
			Logging.log(VERBOSITY.ERROR, tag, "DELETE ID <" + id + "> NOT FOUND");
			return null;
		}
		return deleteMap.get(id);
	}
	
	public static boolean load(String fileName){
		SAXBuilder builder = new SAXBuilder();
		File inputFile = new File(fileName);
		
		try {
			doc = builder.build(inputFile);
		} catch (JDOMException | IOException e) {
			Logging.log(VERBOSITY.FATAL, tag, e.getMessage());
			return false;
		}
		
		Element root = doc.getRootElement();
		
		if (root == null) return false;
		
		List<Element> nodes = root.getChildren();
		
		for (Element node : nodes) {
			HashMap<String,String> sparqlMap = null;
			HashMap<String,Bindings> bindingsMap = null;
			
			switch(node.getName()){
				case "subscribes":
					sparqlMap = subscribeMap;
					bindingsMap = subscribeBindingsMap;
					break;
				case "updates":
					sparqlMap = updateMap;
					bindingsMap = updateBindingsMap;
					break;
				case "inserts":
					sparqlMap = insertMap;
					bindingsMap = insertBindingsMap;
					break;
				case "deletes":
					sparqlMap = deleteMap;
					bindingsMap = deleteBindingsMap;
					break;
				case "namespaces":
					sparqlMap = namespaceMap;
					break;
			}
			
			List<Element> elements = node.getChildren();
			
			if (elements == null) continue;
			
			for(Element element : elements) {
				sparqlMap.put(element.getAttributeValue("ID"), element.getText());
				
				if (node.getName().equals("namespaces")) continue;
				
				Element forcedBindings = element.getChild("forcedBindings");
				
				if (forcedBindings == null) continue;
				
				List<Element> bindingElements = forcedBindings.getChildren();
				Bindings bindings = new Bindings();
				for (Element bindingElement : bindingElements) {
					if (bindingElement.getAttributeValue("type").equals("URI"))
						bindings.addBinding(bindingElement.getAttributeValue("name"), new BindingURIValue(bindingElement.getAttributeValue("value")));
					else
						bindings.addBinding(bindingElement.getAttributeValue("name"), new BindingLiteralValue(bindingElement.getAttributeValue("value")));
				}
				
				bindingsMap.put(element.getAttributeValue("ID"), bindings);
			}
		}
		
		return true;
	}
}
