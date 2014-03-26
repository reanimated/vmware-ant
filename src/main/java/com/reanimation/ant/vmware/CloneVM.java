package com.reanimation.ant.vmware;

import org.apache.tools.ant.BuildException;

import com.reanimation.ant.vmware.model.AntCustomValue;
import com.reanimation.ant.vmware.model.AntVirtualMachineConfigSpec;
import com.vmware.vim25.VirtualMachineCloneSpec;
import com.vmware.vim25.VirtualMachineRelocateSpec;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.VirtualMachine;

public class CloneVM extends VMTask {
	
	private String targetFolderName;
	private String targetName;
	private long timeout = DEFAULT_TIMEOUT;
	private boolean powerOn = true;
	private boolean template = false;
	private AntVirtualMachineConfigSpec configuration = null;
	
	private String targetIpProperty = null;
	private String targetVmIdProperty = null;

	/**
	 * Set the timeout in milliseconds to wait for the
	 * primary IP address to be assigned. The task will
	 * fail if it takes longer than this value to get an
	 * assigned primary IP address.
	 * @param timeout Timeout in milliseconds
	 */
	public void setTimeout (long timeout) {this.timeout = timeout;}
	
	/**
	 * Set the name of the Ant property that will receive the
	 * acquired IP address of the cloned virtual machine.
	 * @param propertyName An Ant property name
	 */
	public void setTargetIpProperty (String propertyName) {
		this.targetIpProperty = propertyName;
	}
	
	/**
	 * Set the name of the Ant property that will receive
	 * the MOR value of the cloned virtual machine
	 * @param propertyName An Ant property name
	 */
	public void setTargetVmIdProperty (String propertyName) {
		this.targetVmIdProperty = propertyName;
	}
	

	/**
	 * Set the configuration of the cloned virtual machine.
	 * @return A new configuration
	 */
	public AntVirtualMachineConfigSpec createConfiguration () {
		configuration = new AntVirtualMachineConfigSpec();
		return configuration;
	}

	/**
	 * Set the folder path for the cloned virtual machine.
	 * If not specified, the cloned virtual machine will be
	 * created in the same folder as the source VM.
	 * @param path An absolute vCenter folder path 
	 */
	public void setTargetFolder (String path) {
		this.targetFolderName = path;
	}
	
	/**
	 * Set the name for the cloned virtual machine.
	 * @param virtualMachineName A name for the new virtual machine
	 */
	public void setTargetName (String virtualMachineName) {
		this.targetName = virtualMachineName;
	}
	
	/**
	 * Set whether to power on the new virtual machine.
	 * @param on If true the cloned virtual machine will be
	 *   powered on.
	 */
	public void setPowerOn (boolean on) {
		this.powerOn = on;
	}

	/**
	 * Set whether to clone the source virtual machine to a vmware template.
	 * @param template If true the cloned virtual machine will be a template
	 */
	public void setTemplate (boolean template) {
		this.template = template;
	}

	@Override
	public void execute() throws BuildException {
		
		if (!connect())
			return;
		
		try {
			VirtualMachine sourceVM = findVM();
			if (targetName == null)
				throw new BuildException("target name MUST be specified");
			
			Folder targetFolder = null;
			if (targetFolderName == null) {
				targetFolder = (Folder) sourceVM.getParent();
			} else {
				targetFolder = findFolder(targetFolderName);
			}
			
			if (targetFolder == null)
				throw new BuildException("Target folder not found!");
			
			if (powerOn == false && targetIpProperty != null)
				throw new BuildException("Must not set both powerOn=false and ipProperty");
			
			VirtualMachineCloneSpec clone = new VirtualMachineCloneSpec();
			clone.setPowerOn(powerOn);
			clone.setTemplate(template);
			
			VirtualMachineRelocateSpec relocate = new VirtualMachineRelocateSpec();
			clone.setLocation(relocate);
			
			AntVirtualMachineConfigSpec config = null;
			if (configuration != null) {
				config = configuration;
				config.processExisting(this, null);
			} else {
				config = new AntVirtualMachineConfigSpec();
			}
			clone.setConfig(config.spec);

			com.vmware.vim25.mo.Task task = sourceVM.cloneVM_Task(targetFolder, targetName, clone);
			String status = task.waitForTask();
			if (status != com.vmware.vim25.mo.Task.SUCCESS)
				throw new BuildException(task.getTaskInfo().getError().getLocalizedMessage());

			VirtualMachine targetVM = getVMFromName(targetName);
			if (targetVM == null)
				throw new BuildException("Error finding cloned VM!");
			
			if (targetVmIdProperty != null) {
				getProject().setProperty(targetVmIdProperty, targetVM.getMOR().getVal());
			}

			if (targetIpProperty != null) {
				String targetIp = waitForIpAddress(targetVM, timeout);
				getProject().setProperty(targetIpProperty, targetIp);
				log("Cloned VM started with IP address " + targetIp);
			}
			
			if (configuration != null) {
				for (AntCustomValue customValue : configuration.customValues) {
					trace("Setting " + customValue.name + " = " + customValue.value);
					targetVM.setCustomValue(customValue.name, customValue.value);
				}
			}
			
		} catch (Exception ex) {
			fail(ex);
		}
	}
	
	
	
	
	
	

}
