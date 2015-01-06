package net.beaconcontroller.dependablecontroller;

import net.neoremind.sshxcute.core.ConnBean;
import net.neoremind.sshxcute.core.Result;
import net.neoremind.sshxcute.core.SSHExec;
import net.neoremind.sshxcute.exception.TaskExecFailException;
import net.neoremind.sshxcute.task.CustomTask;
import net.neoremind.sshxcute.task.impl.ExecCommand;

public class MininetAccess {
	private String address;
	private String username;
	private String password;
	
//	Script
//	#!/bin/bash
//	screen -S mini$1 -p 0 -X stuff "sudo mn --topo single,3 --mac --switch ovsk --controller remote --ip $2 $(printf \\r)"

	public MininetAccess(String address, String username, String password) {
		this.address = address;
		this.username = username;
		this.password = password;
	}

	public void executeCommand(String command) {
		SSHExec ssh = null;
		try {
			// Initialize a ConnBean object, parameter list is ip, username,
			// password
			ConnBean cb = new ConnBean(address, username, password);
			ssh = SSHExec.getInstance(cb);
			CustomTask ct1 = new ExecCommand(command);
			// Connect to server
			ssh.connect();
			Result res = ssh.exec(ct1);
			// Check result and print out messages.
			if (res.isSuccess) {
				System.out.println("Return code: " + res.rc);
				System.out.println("sysout: " + res.sysout);
			} else {
				System.out.println("Return code: " + res.rc);
				System.out.println("error message: " + res.error_msg);
			}
		} catch (TaskExecFailException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} finally {
			ssh.disconnect();
		}
	}
}
