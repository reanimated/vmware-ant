package com.reanimation.ant.vmware;

import org.apache.tools.ant.BuildException;


/**
 * Power down a virtual machine.
 */
public class StopVM extends VMTask {
	@Override
	public void execute () throws BuildException {
		if (!connect())
			return;
		try {
			stopVM(findVM());
		} catch (Exception ex) {
			fail(ex);
		}
	}
}
