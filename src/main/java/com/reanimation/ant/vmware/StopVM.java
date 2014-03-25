package com.reanimation.ant.vmware;

import org.apache.tools.ant.BuildException;


/**
 * Power down a virtual machine.
 */
public class StopVM extends VMTask {
	@Override
	public void execute () throws BuildException {
		connect();
		stopVM(findVM());
	}
}
