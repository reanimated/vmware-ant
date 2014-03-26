package com.reanimation.ant.vmware;

import org.apache.tools.ant.BuildException;

import com.vmware.vim25.mo.VirtualMachine;

/**
 * Destroy a virtual machine.
 */
public class DestroyVM extends VMTask {

	@Override
	public void execute () {
		if (!connect())
			return;
		
		try {
			VirtualMachine vm = findVM();
			stopVM(vm);
			log("Deleting " + toString(vm) + " ...");
			com.vmware.vim25.mo.Task task = vm.destroy_Task();
			String status = task.waitForTask();
			if (status != com.vmware.vim25.mo.Task.SUCCESS)
				throw new BuildException(task.getTaskInfo().getError().getLocalizedMessage());
		} catch (Exception ex) {
			fail(ex);
		}
	}

}
