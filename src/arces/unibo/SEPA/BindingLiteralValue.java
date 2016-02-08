package arces.unibo.SEPA;

public class BindingLiteralValue extends BindingValue {

	public BindingLiteralValue(String value) {
		this.value = value;
		this.type = BINDING_TYPE.LITERAL;
	}
}
