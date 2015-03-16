package net.beaconcontroller.dependablecontroller.data;

import java.util.List;

public class SwitchList {
	private List<SwitchDependency> switches;

	public List<SwitchDependency> getSwitches() {
		return switches;
	}

	public void setSwitches(List<SwitchDependency> switches) {
		this.switches = switches;
	}
}
