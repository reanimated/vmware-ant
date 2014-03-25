package com.reanimation.ant.vmware;

import org.apache.tools.ant.BuildException;

import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.mo.VirtualMachine;

/**
 * Power on a virtual machine.
 * By setting the ipProperty the task will also wait for the virtual
 * machine to acquire the primary IP address.
 */
public class StartVM extends VMTask {
	
	private long timeout = DEFAULT_TIMEOUT;
	private String ipProperty = null;
	

	/**
	 * Set the name of the Ant property that will receive the 
	 * primary IP address of the virtual machine after power on.
	 * By setting the IP property, the task will wait for the 
	 * virtual machine to be assigned a primary IP address after
	 * power on.
	 * @param s An Ant property name
	 */
	public void setIpProperty (String s) {this.ipProperty = s;}
	
	/**
	 * Set the timeout in milliseconds to wait for the
	 * primary IP address to be assigned. The task will
	 * fail if it takes longer than this value to get an
	 * assigned primary IP address.
	 * @param timeout Timeout in milliseconds
	 */
	public void setTimeout (long timeout) {this.timeout = timeout;}
	
	@Override
	public void execute () throws BuildException {
		connect();
		VirtualMachine vm = findVM();
		try {
			if (vm.getRuntime().getPowerState() != VirtualMachinePowerState.poweredOn) {
				log("Starting " + toString(vm) + " ...");
				com.vmware.vim25.mo.Task task = vm.powerOnVM_Task(null);
				String status = task.waitForTask();
				if (status != com.vmware.vim25.mo.Task.SUCCESS)
					throw new BuildException(task.getTaskInfo().getError().getLocalizedMessage());
			} else {
				log(toString(vm) + " already powered on");
			}
			
			if (ipProperty != null) {
				String ip = waitForIpAddress(vm, timeout);
				getProject().setProperty(ipProperty, ip);
			}
		} catch (BuildException buildError) {
			throw buildError;
		} catch (Exception ex) {
			throw new BuildException(ex);
		}
	}
}
