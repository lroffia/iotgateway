package arces.unibo.SEPA;

public interface IConsumer extends IClient{	
	public boolean subscribe(Bindings forcedBindings);
	public boolean unsubscribe();
	public void notify(BindingsResults notify);
	public BindingsResults getQueryResults();
}
