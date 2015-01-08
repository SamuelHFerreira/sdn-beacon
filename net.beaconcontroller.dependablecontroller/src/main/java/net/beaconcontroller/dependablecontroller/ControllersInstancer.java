package net.beaconcontroller.dependablecontroller;

import java.util.ArrayList;
import java.util.List;

public class ControllersInstancer {
	private static final int DEFAULT_NUMBER_OF_CONTROLLERS = 3;
	private static List<VirtualController> controllers = new ArrayList<VirtualController>();

	public static List<VirtualController> getVirtualControllers() {
		return controllers;
	}

	public static boolean askRole(VirtualController askingController,
			ControllerRole askingRole) {
		// TODO regra de atribuição de papéis
		return true;
	}

	public static List<VirtualController> createVirtualControllers() {
		if (controllers.isEmpty()) {
			VirtualController auxController;
			for (int i = 0; i < DEFAULT_NUMBER_OF_CONTROLLERS; i++) {
				auxController = new VirtualController((long) i,
						getControllerRole(i));
				controllers.add(auxController);
			}
		}
		return controllers;
	}

	public static VirtualController getMasterController() {
		for (VirtualController controller : controllers) {
			if (controller.getRole().equals(ControllerRole.ROLE_MASTER)
					&& controller.getStatus().equals(ControllerStatus.ONLINE)) {
				return controller;
			}
		}
//		if(!controllers.isEmpty() && chooseNewMaster()) {
//			return getMasterController();
//		}
		return null;
	}

	public static VirtualController chooseNewMaster() {
		for (VirtualController controller : controllers) {
			if (controller.getStatus().equals(ControllerStatus.ONLINE)) {
				controller.changeRole(ControllerRole.ROLE_MASTER);
				return controller;
			}
		}
		return null;
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
