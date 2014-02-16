package net.beaconcontroller.dependablecontroller;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

import org.openflow.protocol.OFType;

import net.beaconcontroller.core.IBeaconProvider;
import net.beaconcontroller.core.IOFInitializerListener;
import net.beaconcontroller.core.IOFMessageListener;
import net.beaconcontroller.core.IOFSwitch;
import net.beaconcontroller.core.IOFSwitchListener;

public class DController implements IBeaconProvider {

	@Override
	public void addOFInitializerListener(IOFInitializerListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeOFInitializerListener(IOFInitializerListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initializerComplete(IOFSwitch sw,
			IOFInitializerListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addOFMessageListener(OFType type, IOFMessageListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeOFMessageListener(OFType type, IOFMessageListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<Long, IOFSwitch> getSwitches() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addOFSwitchListener(IOFSwitchListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeOFSwitchListener(IOFSwitchListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<OFType, List<IOFMessageListener>> getListeners() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IOFInitializerListener> getInitializers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InetAddress getListeningIPAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getListeningPort() {
		// TODO Auto-generated method stub
		return 0;
	}

}
