package arces.unibo.gateway.mapping.manager;

import java.util.ArrayList;

import arces.unibo.gateway.mapping.MPMapping;

public interface MPMappingEventListener {
	public void addedMPMappings(ArrayList<MPMapping> mappings);
	public void removedMPMappings(ArrayList<MPMapping> mappings);
}
