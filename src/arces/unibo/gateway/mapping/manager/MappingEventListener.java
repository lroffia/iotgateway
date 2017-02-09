package arces.unibo.gateway.mapping.manager;

import java.util.ArrayList;

import arces.unibo.gateway.mapping.MNMapping;
import arces.unibo.gateway.mapping.MPMapping;

public interface MappingEventListener {
	public void addedMNMappings(ArrayList<MNMapping> mappings);
	public void removedMNMappings(ArrayList<MNMapping> mappings);
	public void addedMPMappings(ArrayList<MPMapping> mappings);
	public void removedMPMappings(ArrayList<MPMapping> mappings);
}

