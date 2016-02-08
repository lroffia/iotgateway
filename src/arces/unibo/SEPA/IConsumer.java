package arces.unibo.SEPA;

public interface IConsumer extends IClient{	
	public BindingsResults subscribe(Bindings forcedBindings);
	public boolean unsubscribe();
	public void notify(BindingsResults notify);
}
