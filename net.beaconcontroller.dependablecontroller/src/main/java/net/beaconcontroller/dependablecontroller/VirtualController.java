package net.beaconcontroller.dependablecontroller;

import java.util.List;

public class VirtualController {
	private Long id;
	private ControllerRole role;
	private List<VirtualController> brothers;
	
	public VirtualController(Long id, ControllerRole role) {
		this.id  = id;
		if(role != null) {
			this.role = role;
		} else {
			role = ControllerRole.ROLE_EQUAL;
		}
		
	}
	
	public boolean changeRole(ControllerRole role) {
		if(this.role.equals(role)) {
			return false;
		} else {
			return true;
		}
	}
	
	public boolean requestRole(ControllerRole role) {
		// TODO
		if(brothers!=null && brothers.size()>0) {
			return false;
		}
		return false;
	}
	
	private void askRole() {
		
	}
	
	public boolean addBrother(VirtualController brother) {
		if(brothers.contains(brother)) {
			return false;
		}
		return true;
	}
}
