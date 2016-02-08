package arces.unibo.SEPA;

public abstract class BindingValue {
	public enum BINDING_TYPE{URI,LITERAL};
	
	String value;
	BINDING_TYPE type;
	
	public boolean isURI() {
		return (type == BINDING_TYPE.URI);
	}
	
	public boolean isLiteral() {
		return (type == BINDING_TYPE.LITERAL);
	}
	
	public String getValue() {return value;}
}
