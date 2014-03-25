package com.reanimation.ant.vmware;

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;

import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.VirtualMachine;
import com.vmware.vim25.mo.util.MorUtil;

/**
 * A task that needs a virtual machine.
 * The virtual machine is identified by its MOR value, name, or IP address
 */
public abstract class VMTask extends VSphereTask {
	
	protected static final long DEFAULT_TIMEOUT =  2 * 60 * 1000;

	private String ip;
	private String mor;
	private String name;
	
	
	/**
	 * Set the IP address of the virtual machine
	 * @param ip IP address in dotted form
	 */
	public void setIp (String ip) {
		this.ip = ip;
	}
	
	/**
	 * Set the MOR value of the virtual machine
	 * @param id Virtual machine MOR value
	 */
	public void setMor (String id) {
		this.mor = id;
	}
	
	/**
	 * Set the name of the virtual machine
	 * @param name Virtual machine name
	 */
	public void setName (String name) {
		this.name = name;
	}
	
	protected String getVMDescription () {
		StringBuilder s = new StringBuilder();
		if (mor != null)
			s.append("MOR=").append(mor).append(' ');
		if (name != null)
			s.append("Name=").append(name).append(' ');
		if (ip != null)
			s.append("IP=").append(ip).append(' ');
		return s.toString();
	}
	
	protected String toString (VirtualMachine vm) {
		return "VM " + vm.getName();
	}
	
	protected String waitForIpAddress (VirtualMachine vm, long timeout) throws Exception {
		trace("Waiting for " + toString(vm) + " primary IP address ...");
		String ip = null;
		long start = System.currentTimeMillis();
		while ((ip == null || ip.isEmpty()) && (System.currentTimeMillis() - start < timeout)) {
			ip = vm.getSummary().getGuest().getIpAddress();
			if (ip == null || ip.isEmpty()) {
				Thread.sleep(2500);
			}
		}
		
		if (ip == null || ip.isEmpty()) {
			throw new BuildException("Timed out waiting for VM primary IP address");
		}
		
		return ip;
	}
	
	protected void stopVM (VirtualMachine vm) throws BuildException {
		try {
			if (vm.getRuntime().getPowerState() != VirtualMachinePowerState.poweredOff) {
				log("Stopping " + toString(vm) + " ...");
				com.vmware.vim25.mo.Task task = vm.powerOffVM_Task();
				String status = task.waitForTask();
				if (status != com.vmware.vim25.mo.Task.SUCCESS)
					throw new BuildException(task.getTaskInfo().getError().getLocalizedMessage());
			}
		} catch (BuildException buildError) {
			throw buildError;
		} catch (Exception ex) {
			throw new BuildException(ex);
		}
	}
	
	
	protected VirtualMachine findVM () throws BuildException {
		
		if ((ip == null || ip.isEmpty()) && 
			(mor == null || mor.isEmpty()) && 
			(name == null || name.isEmpty())) {
			
			throw new BuildException("Must specify either the VM IP address, MOR ID, or name");
		}

		VirtualMachine vm = null;
		if (mor != null && !mor.isEmpty())
			vm = getVMFromId(mor);
		else if (name != null && !name.isEmpty())
			vm = getVMFromName(name);
		else
			vm = findVM(ip);
		
		if (vm == null)
			throw new BuildException("The VM with " + getVMDescription() + "was not found");
		
		return vm;
	}
	
	private VirtualMachine findVM (String ip) throws BuildException {
		try {
			ManagedEntity[] vms = getRooNavigator().searchManagedEntities("VirtualMachine");
			List<VirtualMachine> foundVms = new ArrayList<VirtualMachine>();
			for (ManagedEntity e : vms) {
				VirtualMachine vm = (VirtualMachine) MorUtil.createExactManagedEntity(getService().getServerConnection(), e.getMOR());
				if (ip.equals(vm.getGuest().getIpAddress())) {
					foundVms.add(vm);
				}
			}
			
			if (foundVms.isEmpty()) {
				return null;
			} else if (foundVms.size() == 1) {
				return foundVms.get(0);
			} else {
				VirtualMachine selectedVm = null;
				for (VirtualMachine vm : foundVms) {
					VirtualMachinePowerState powerState = vm.getRuntime().getPowerState();
					trace("FOUND " + vm.getMOR().getVal() + " " + vm.getName() + " " + powerState);
					if (powerState  == VirtualMachinePowerState.poweredOn) {
						if (selectedVm != null) {
							throw new BuildException("Multiple running VMs match the server IP address!");
						}
						selectedVm = vm;
					}
				}
				return selectedVm;
			}
		} catch (BuildException buildError) {
			throw buildError;
		} catch (Exception ex) {
			throw new BuildException(ex);
		}
	}
}
