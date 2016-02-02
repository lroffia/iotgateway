package arces.unibo.iot.dispatching;

import arces.unibo.iot.SEPA.Producer;

public abstract class RequestDispatcher extends Producer {

	public RequestDispatcher(String updateQuery) {
		super(updateQuery);
	}
}
