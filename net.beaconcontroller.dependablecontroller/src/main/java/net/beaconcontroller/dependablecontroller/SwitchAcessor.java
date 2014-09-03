package net.beaconcontroller.dependablecontroller;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class SwitchAcessor {
	String accessDomainName;
	String ownDomainName;
	
	public SwitchAcessor(String accessDomainName,String ownDomainName) {
		this.accessDomainName = accessDomainName;
		this.ownDomainName = ownDomainName;
//		levando em consideração que o swich sera uma rede mininet!
		//in mac oxs
		String command = "ping -c 3 " + accessDomainName;
 
		//in windows
		//String command = "ping -n 3 " + domainName;
	}
	
	public void shutDownSwitch(){
		
	}
	public void instatiateNewSwitch() {
		String command = "sudo mn --topo single,3 --mac --switch ovsk --controller remote --ip 192.168.0.13";
		executeCommand(command);
	}
 
	private String executeCommand(String command) {
 
		StringBuffer output = new StringBuffer();
 
		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = 
                            new BufferedReader(new InputStreamReader(p.getInputStream()));
 
                        String line = "";			
			while ((line = reader.readLine())!= null) {
				output.append(line + "\n");
			}
 
		} catch (Exception e) {
			e.printStackTrace();
		}
 
		return output.toString();
 
	}

}
