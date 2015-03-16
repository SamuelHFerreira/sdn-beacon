package net.beaconcontroller.dependablecontroller.data;

import java.util.HashMap;
import java.util.Map;

public class RouteMap {
	protected Map<String, Map<Long,Short>> macTablesReplica = new HashMap<String, Map<Long,Short>>();

	public Map<String, Map<Long, Short>> getMacTablesReplica() {
		return macTablesReplica;
	}

	public void setMacTablesReplica(Map<String, Map<Long, Short>> macTablesReplica) {
		this.macTablesReplica = macTablesReplica;
	}
}
