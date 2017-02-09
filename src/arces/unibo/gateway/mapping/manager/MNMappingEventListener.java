package arces.unibo.gateway.mapping.manager;

import java.util.ArrayList;

import arces.unibo.gateway.mapping.MNMapping;

public interface MNMappingEventListener {
	public void addedMNMappings(ArrayList<MNMapping> mappings);
	public void removedMNMappings(ArrayList<MNMapping> mappings);
}
