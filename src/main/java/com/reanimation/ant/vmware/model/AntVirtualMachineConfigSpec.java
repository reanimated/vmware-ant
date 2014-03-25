package com.reanimation.ant.vmware.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.Task;

import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VirtualMachineConfigSpec;

public class AntVirtualMachineConfigSpec {
	
	public VirtualMachineConfigSpec spec = new VirtualMachineConfigSpec();
	public List<AntCustomValue> customValues = new ArrayList<AntCustomValue>();
	
	private AntVmConfigSpec vappSpec = null;
	
	public AntVirtualMachineConfigSpec () {
	}
	
	public void processExisting (Task task, VirtualMachineConfigInfo existing) {
		if (vappSpec != null)
			vappSpec.processExisting(task, existing.getVAppConfig());
	}
	
	public void setVAppConfigRemoved (boolean remove) {
		spec.setVAppConfigRemoved(remove);
	}
	
	public void setAnnotation (String s) {
		spec.setAnnotation(s);
	}
	
	public AntCustomValue createCustomvalue () {
		
		AntCustomValue value = new AntCustomValue();
		customValues.add(value);
		return value;
	}
	
	
	public AntVmConfigSpec createVappconfig () {
		vappSpec = new AntVmConfigSpec();
		setVAppConfigRemoved(false);
		spec.setVAppConfig(vappSpec.spec);
		return vappSpec;
	}

}
