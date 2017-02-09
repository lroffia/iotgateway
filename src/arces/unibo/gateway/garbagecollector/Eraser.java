package arces.unibo.gateway.garbagecollector;

import arces.unibo.SEPA.application.ApplicationProfile;
import arces.unibo.SEPA.application.Producer;

public 	class Eraser extends Producer{
	public Eraser(ApplicationProfile appProfile){
		super(appProfile,"DELETE_ALL");
	}
	
	public boolean update() {
		return super.update(null);
		}
}
