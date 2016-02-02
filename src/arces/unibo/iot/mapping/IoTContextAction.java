package arces.unibo.iot.mapping;

public class IoTContextAction {
	String actionURI = "";
	String value ="";
	String contextURI = "";
	
	public IoTContextAction(String contextURI, String actionURI,String value) {
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
		IoTContextAction target = (IoTContextAction) obj;
		return (this.actionURI.equals(target.getActionURI()) && this.contextURI.equals(target.getContextURI()));
	}
	
	@Override
	public String toString(){
		return "IoT-ContextAction<" + contextURI + "," + actionURI +","+value+">";
	}
}
