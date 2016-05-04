package arces.unibo.gateway.mapping;

public class ResourceAction {
	String actionURI = "";
	String value ="";
	String resourceURI = "";
	
	public ResourceAction(String resourceURI, String actionURI,String value) {
		this.actionURI = actionURI;
		this.resourceURI = resourceURI;
		this.value = value;
	}

	public String getActionURI(){return actionURI;}
	public String getResourceURI(){return resourceURI;}
	public String getValue(){return value;}
	
	@Override
	public boolean equals(Object obj){
		if(!obj.getClass().equals(this.getClass())) return false;
		ResourceAction target = (ResourceAction) obj;
		return (this.actionURI.equals(target.getActionURI()) && this.resourceURI.equals(target.getResourceURI()));
	}
	
	@Override
	public String toString(){
		return "ResourceAction<" + resourceURI + "," + actionURI +","+value+">";
	}
}
