package net.beaconcontroller.dependablecontroller.data;

public class SwitchDependency {
	private String address;
	private String port;
	
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	
	@Override
	public String toString() {
		return address+":" +port;
	}
	
}
