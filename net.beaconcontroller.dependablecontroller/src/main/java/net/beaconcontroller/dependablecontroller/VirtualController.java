package net.beaconcontroller.dependablecontroller;

import java.util.ArrayList;
import java.util.List;

import net.beaconcontroller.core.IOFSwitch;

public class VirtualController {
	private Long id;
	private ControllerRole role;
	private ControllerStatus status;
	private List<IOFSwitch> switches;
	
//	private Long masterId;
	private List<VirtualController> brothers;
	
	public VirtualController(Long id, ControllerRole role) {
		this.setId(id);
		if(role != null) {
			this.role = role;
		} else {
			role = ControllerRole.ROLE_EQUAL;
		}
		status = ControllerStatus.ONLINE; 
		List<IOFSwitch> switches = new ArrayList<IOFSwitch>();
	}
	
	public boolean turnOff() {
		if(status.equals(ControllerStatus.ONLINE)) {
			status = ControllerStatus.OFFLINE;
			return true;
		} else {
			return false;
		}
	}
	
	public boolean turnOn() {
		if(status.equals(ControllerStatus.OFFLINE)) {
			status = ControllerStatus.ONLINE;
			return true;
		} else {
			return false;
		}
	}
	
	public boolean changeRole(ControllerRole role) {
		if(this.role.equals(role)) {
			return false;
		} else {
			return true;
		}
	}
	
//	public boolean requestRole(ControllerRole role) {
//		// TODO
//		if(brothers!=null && brothers.size()>0) {
//			return false;
//		}
//		return true;
//		// TODO nem sei que fiz aqui
//	}
//	
//	private void askRole() {
//		
//	}
	
	public boolean addBrother(VirtualController brother) {
		if(brothers.contains(brother)) {
			return false;
		}
		return true;
	}

	public List<IOFSwitch> getSwitches() {
		return switches;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
