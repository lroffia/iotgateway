package arces.unibo.gateway.mapping;

public class ContextAction {
	String actionURI = "";
	String value ="";
	String contextURI = "";
	
	public ContextAction(String contextURI, String actionURI,String value) {
		this.actionURI = actionURI;
		this.contextURI = contextURI;
		this.value = value;
	}

	public String getActionURI(){return actionURI;}
	public String getContextURI(){return contextURI;}
	public String getValue(){return value;}
	
	@Override
	public boolean equals(Object obj){
		if(!obj.getClass().equals(this.getClass())) return false;
		ContextAction target = (ContextAction) obj;
		return (this.actionURI.equals(target.getActionURI()) && this.contextURI.equals(target.getContextURI()));
	}
	
	@Override
	public String toString(){
		return "IoT-ContextAction<" + contextURI + "," + actionURI +","+value+">";
	}
}
