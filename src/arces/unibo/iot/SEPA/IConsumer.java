package arces.unibo.iot.SEPA;

import arces.unibo.iot.KPI.iKPIC_subscribeHandler2;

public interface IConsumer extends IClient, iKPIC_subscribeHandler2 {	
	public BindingsResults subscribe(Bindings forcedBindings);
	public boolean unsubscribe();
	public void notify(BindingsResults notify);
}
