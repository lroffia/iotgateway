package arces.unibo.iot.dispatching;

import java.util.Observer;

import arces.unibo.iot.SEPA.Aggregator;
import arces.unibo.iot.SEPA.BindingsResults;
import arces.unibo.iot.mapping.Map;
import arces.unibo.iot.mapping.Mapper;

public abstract class Dispatcher extends Aggregator implements Observer {
	
	protected RequestDispatcher requestDispatcher;
	protected ResponseDispatcher responseDispatcher;
	protected Mapper mapper;
	protected Map map;

	public Dispatcher(String subscribeQuery,String updateQuery){
		super(subscribeQuery,updateQuery);
	}
		
	public BindingsResults start(){
		new Thread(requestDispatcher).start();
		new Thread(mapper).start();
		
		BindingsResults ret = responseDispatcher.start();
		responseDispatcher.addObserver(this);
		
		return ret;
	}
}
