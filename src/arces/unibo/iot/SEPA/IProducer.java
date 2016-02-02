package arces.unibo.iot.SEPA;

public interface IProducer extends IClient {
	 public abstract boolean update(Bindings forcedBindings);
}
