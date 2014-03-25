package com.reanimation.ant.vmware;

import org.apache.tools.ant.BuildException;

import com.reanimation.ant.vmware.model.AntCustomValue;
import com.reanimation.ant.vmware.model.AntVirtualMachineConfigSpec;
import com.vmware.vim25.VAppOvfSectionInfo;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VmConfigInfo;
import com.vmware.vim25.mo.VirtualMachine;

public class ReconfigureVM extends VMTask {
	
	private AntVirtualMachineConfigSpec configuration = null;
	
	
	public AntVirtualMachineConfigSpec createConfiguration () {
		configuration = new AntVirtualMachineConfigSpec();
		return configuration;
	}
	

	@Override
	public void execute () throws BuildException {
		connect();
		VirtualMachine vm = findVM();
		
		try {
			if (configuration == null) {
				StringBuilder b = new StringBuilder();
				b.append("Configuration for ").append(toString(vm));
				VirtualMachineConfigInfo config = vm.getConfig();
				VmConfigInfo vapp = config.getVAppConfig();
				
				String[] eula = vapp.getEula();
				if (eula != null) {
					for (int i = 0; i < eula.length; ++i) {
						b.append("EULA #").append(i).append('\n').append(eula[i]);
					}
				}
				
				VAppOvfSectionInfo[] ovfSections = vapp.getOvfSection();
				if (ovfSections != null) {
					for (VAppOvfSectionInfo section : ovfSections) {
						b.append("\nVAppOvfSectioinInfo:").
						append("\n\tenvelopeLevel: ").append(section.getAtEnvelopeLevel()).
						append("\n\tkey: ").append(section.getKey()).
						append("\n\tcontents: ").append(section.getContents()).
						append("\n\tnamespace: ").append(section.getNamespace()).
						append("\n\ttype: ").append(section.getType());
					}
				}

				System.out.println(b);
				
				
			} else {
				log("Configuring " + toString(vm) + " ...");
				configuration.processExisting(this, vm.getConfig());
				com.vmware.vim25.mo.Task task = vm.reconfigVM_Task(configuration.spec);
				String status = task.waitForTask();
				if (status != com.vmware.vim25.mo.Task.SUCCESS)
					throw new BuildException(task.getTaskInfo().getError().getLocalizedMessage());
				
				for (AntCustomValue customValue : configuration.customValues) {
					System.out.println("Setting " + customValue.name + " = " + customValue.value);
					vm.setCustomValue(customValue.name, customValue.value);
				}
			}

		} catch (BuildException buildError) {
			throw buildError;
		} catch (Exception ex) {
			throw new BuildException(ex);
		}
	}
}
