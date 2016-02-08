package arces.unibo.SEPA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import arces.unibo.KPI.SSAP_sparql_response;

public class BindingsResults {
	private ArrayList<Bindings> addedBindings = null;
	private ArrayList<Bindings> removedBindings = null;
	
	public BindingsResults(SSAP_sparql_response addedBindings,SSAP_sparql_response removedBindings,HashMap<String,String> URI2PrefixMap){
		this.addedBindings = getBindings(addedBindings,URI2PrefixMap);
		this.removedBindings = getBindings(removedBindings,URI2PrefixMap);
	}
	
	public ArrayList<Bindings> getAddedBindings() {
		return addedBindings;
	}
	
	public ArrayList<Bindings> getRemovedBindings() {
		return removedBindings;
	}
	
	private ArrayList<Bindings> getBindings(SSAP_sparql_response sparl,HashMap<String,String> URI2PrefixMap) {
		ArrayList<Bindings> ret = new ArrayList<Bindings>();
		
		if (sparl == null) return ret;
		
		for(Vector<String[]> result : sparl.getResults()) {
			Bindings bindings = new Bindings();
			for(String[] variable : result) {
				String name = SSAP_sparql_response.getCellName(variable);
				String value = SSAP_sparql_response.getCellValue(variable);
				boolean uri = SSAP_sparql_response.getCellCategory(variable).equals("uri");
				
				if (uri)
					bindings.addBinding("?"+name,new BindingURIValue(value,URI2PrefixMap));
				else
					bindings.addBinding("?"+name,new BindingLiteralValue(value));
			}
			ret.add(bindings);
		}
		return ret;
	}
	
	@Override
	public String toString(){
		String ret = "********************************\n";
		
		ret += "ADDED BINDINGS\n-------------\n";
		if (addedBindings != null)
			for(Bindings bindings : addedBindings) ret += bindings.toString() + "\n";

		ret += "REMOVED BINDINGS\n-------------\n";
		if (removedBindings != null)
			for(Bindings bindings : removedBindings) ret += bindings.toString() + "\n";
		
		ret += "********************************\n";
		return ret;
	}
}
