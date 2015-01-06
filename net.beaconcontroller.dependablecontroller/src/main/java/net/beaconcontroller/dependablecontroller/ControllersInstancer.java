package net.beaconcontroller.dependablecontroller;

import java.util.ArrayList;
import java.util.List;

public class ControllersInstancer {
	private static final int DEFAULT_NUMBER_OF_CONTROLLERS = 3;
	private static List<VirtualController> controllers = new ArrayList<VirtualController>();

	public static List<VirtualController> getVirtualControllers() {
		return controllers;
	}
	
	public static boolean askRole(VirtualController askingController, ControllerRole askingRole) {
		// TODO regra de atribuição de papéis
		return true;
	}
	
	
	public static List<VirtualController> createVirtualControllers() {
		if(controllers.isEmpty()) {
			VirtualController auxController;
			for (int i = 0; i < DEFAULT_NUMBER_OF_CONTROLLERS; i++) {
				auxController = new VirtualController((long) i,
						getControllerRole(i));
				controllers.add(auxController);
			}
		}
		return controllers;
	}

	private static ControllerRole getControllerRole(int idx) {
		// if((idx % 2)== 0) {
		if (idx == 0) {
			return ControllerRole.ROLE_MASTER;
		} else {
			return ControllerRole.ROLE_SLAVE;
		}
	}
}
