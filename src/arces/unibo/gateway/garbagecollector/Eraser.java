package arces.unibo.gateway.garbagecollector;

import arces.unibo.SEPA.client.pattern.ApplicationProfile;
import arces.unibo.SEPA.client.pattern.Producer;

public 	class Eraser extends Producer{
	public Eraser(ApplicationProfile appProfile){
		super(appProfile,"DELETE_ALL");
	}
	
	public boolean update() {
		return super.update(null);
		}
}
