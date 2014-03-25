package com.reanimation.ant.vmware;

import org.apache.tools.ant.BuildException;

import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.VirtualMachine;

/**
 * Find a VirtualMachine in vCenter.
 * The VirtualMachine can be located from its IP address, its name, or the full folder path in vCenter. 
 */
public class FindVM extends VMTask  {
	
	private String morProperty;
	private String pathProperty;
	private String nameProperty;
	private String ipProperty;
	
	/**
	 * Set the name of the Ant property that will receive the MOR value 
	 * of the located virtual machine.
	 * @param propertyName Ant property name
	 */
	public void setMorProperty (String propertyName) {
		this.morProperty = propertyName;
	}
	
	/**
	 * Set the name of the Ant property that will receive the full
	 * folder path of the located virtual machine.
	 * @param propertyName Ant property name
	 */
	public void setPathProperty (String propertyName) {
		this.pathProperty = propertyName;
	}

	/**
	 * Set the name of the Ant property that will receive the
	 * name of the located virtual machine.
	 * @param propertyName Ant property name
	 */
	public void setNameProperty (String propertyName) {
		this.nameProperty = propertyName;
	}

	/**
	 * Set the name of the Ant property that will receive the
	 * primary IP address of the located virtual machine.
	 * @param propertyName Ant property name
	 */
	public void setIpProperty (String propertyName) {
		this.ipProperty = propertyName;
	}

	@Override
	public void execute() throws BuildException {
		connect();
		VirtualMachine vm = findVM();
		
		if (morProperty != null) {
			getProject().setProperty(morProperty, vm.getMOR().getVal());
		}
			
		if (pathProperty != null) {
			StringBuilder path = new StringBuilder();
			path.append(vm.getName());
			ManagedEntity parent = vm.getParent();
			boolean done = false;
			while (parent != null && !done) {
				path.insert(0, parent.getName() + "/" );
				if (parent.getMOR().getType().equals("Datacenter"))
					done = true;
				parent = parent.getParent();
			}
			getProject().setProperty(pathProperty, path.toString());
		}
			
		if (nameProperty != null) {
			getProject().setProperty(nameProperty, vm.getName());
		}
			
		if (ipProperty != null) {
			getProject().setProperty(ipProperty, vm.getSummary().getGuest().getIpAddress());
		}
	}
}
