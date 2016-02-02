package arces.unibo.iot.dispatching;

import java.util.Observable;

import arces.unibo.iot.SEPA.BindingsResults;
import arces.unibo.iot.SEPA.Consumer;

public abstract class ResponseDispatcher extends Observable {
	ResponseConsumer mResponseConsumer;
	
	public abstract void responseNotification(BindingsResults notify);
	
	class ResponseConsumer extends Consumer {
	
		public ResponseConsumer(String subscribeQuery) {
			super(subscribeQuery);
		}

		@Override
		public void notify(BindingsResults notify) {
			responseNotification(notify);
		}
		
	}
	
	public ResponseDispatcher(String subscribeQuery) {
		mResponseConsumer = new ResponseConsumer(subscribeQuery);
	}
	
	public BindingsResults start(){
		new Thread(mResponseConsumer).start();
		return mResponseConsumer.subscribe(null);
	}
}
