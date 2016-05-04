package arces.unibo.gateway.mapping;

public class Mapping {
	ResourceAction resourceAction;
	String requestPattern;
	String responsePattern;
	String mappingURI;
	
	public Mapping(ResourceAction resourceAction,String requestPattern,String responsePattern){
		this.resourceAction = resourceAction;
		this.responsePattern = responsePattern;
		this.requestPattern = requestPattern;
	}
		
	public String getRequestPattern(){return requestPattern;}
	public String getResponsePattern(){return responsePattern;}
	public String getResourceURI(){return resourceAction.getResourceURI();}
	public String getActionURI(){return resourceAction.getActionURI();}
	public String getActionValue() {return resourceAction.getValue();}
	public String getURI(){return mappingURI;}
	
	@Override
	public boolean equals(Object obj){
		if (!obj.getClass().equals(this.getClass())) return false;
		Mapping target = (Mapping) obj;
		return (this.resourceAction.equals(target.resourceAction));
	}
	
	@Override
	public String toString() {
		return "Resource mapping: "+ resourceAction.toString()+ ": Request-Pattern<"+ requestPattern + "> Response-Pattern: <"+ responsePattern +">"; 
	}
}
