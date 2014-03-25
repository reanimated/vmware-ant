package com.reanimation.ant.vmware;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;
import org.apache.tools.ant.UnknownElement;
import org.apache.tools.ant.property.LocalProperties;

import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;
import com.vmware.vim25.mo.util.MorUtil;

/**
 * A task that requires a connection to vSphere (either ESX server or vCenter).
 */
public class VSphereTask extends Task implements TaskContainer {
	
	private String host;
	private String user;
	private String password;
	private boolean ignoreCert = true;
	private ServiceInstance service;
	private Folder rootFolder;
	private InventoryNavigator rootNavigator;
	private boolean verbose = true;
	private List<Task> tasks = null;
	
	/**
	 * Set the required vSphere host.
	 * This can be set as a vSphere URL such as http://192.168.1.1/sdk
	 * or as a plain host name/IP address such as 192.168.1.1
	 * @param host The vSphere host
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * Set the required vSphere user name.
	 * @param user The vSphere user name
	 */
	public void setUser (String user) {
		this.user = user;
	}
	
	/**
	 * Set the required vSphere password.
	 * @param password The vSphere password
	 */
	public void setPassword (String password) {
		this.password = password;
	}

	/**
	 * Set whether to ignore self-signed vSphere certs.
	 * In most cases you will set this to true (the default)
	 * @param ignoreCert Set to true to ignore unverified SSL cert.
	 */
	public void setIgnorecert (boolean ignoreCert) {
		this.ignoreCert = ignoreCert;
	}
	
	/**
	 * Set whether to include extra log information.
	 * @param verbose Set to true to output additional vmware information
	 */
	public void setVerbose (boolean verbose) {
		this.verbose = verbose;
	}

	
	public boolean isVerbose () {
		return verbose;
	}
	
	public ServiceInstance getService () {
		return service;
	}
	
	public Folder getRootFolder () {
		return rootFolder;
	}
	
	public InventoryNavigator getRooNavigator () {
		return rootNavigator;
	}
	
	public void addTask (Task task) {
		if (tasks == null) {
			tasks = new ArrayList<Task>();
		}
		tasks.add(task);
	}
	

	protected void connect () throws BuildException {
		if (service != null)
			return;
		
		if (host == null)
			throw new BuildException("vSphere host is required");
		if (password == null)
			throw new BuildException("vSphere password is required");
		if (user == null)
			throw new BuildException("vSphere user is required");

		try {
			service = new ServiceInstance(getVSphereURL(host), user, password, ignoreCert);
			rootFolder = service.getRootFolder();
			rootNavigator = new InventoryNavigator(rootFolder);
		} catch (Exception ex) {
			throw new BuildException(ex);
		}
	}
	
	@Override
	public void execute () throws BuildException {

		if (tasks != null && !tasks.isEmpty()) {
			connect();
			LocalProperties localProperties = LocalProperties.get(getProject());
			localProperties.enterScope();
			try {
				for (Task task : tasks) {
					if (task instanceof VSphereTask) {
						((VSphereTask) task).setConnection(service, rootFolder, rootNavigator);
					} else if (task instanceof UnknownElement) {
						UnknownElement ue = (UnknownElement) task;
						ue.maybeConfigure();
						if (ue.getRealThing() instanceof VSphereTask) {
							((VSphereTask) ue.getRealThing()).setConnection(service, rootFolder,
									rootNavigator);
						}
					}
					task.perform();
				}
			} finally {
				localProperties.exitScope();
			}
		}
	}
	
	
	private void setConnection(ServiceInstance service, Folder rootFolder,
			InventoryNavigator rootNavigator) {
		this.service = service;
		this.rootFolder = rootFolder;
		this.rootNavigator = rootNavigator;
	}

	public VirtualMachine getVMFromId (String morId) {
		ManagedObjectReference mor = new ManagedObjectReference();
		mor.setType("VirtualMachine");
		mor.setVal(morId);
		VirtualMachine vm = new VirtualMachine(service.getServerConnection(), mor);
		return vm;
	}
	
	public VirtualMachine getVMFromName (String name) throws BuildException {
		try {
			ManagedEntity[] vms = rootNavigator.searchManagedEntities("VirtualMachine");
			List<VirtualMachine> foundVms = new ArrayList<VirtualMachine>();
			for (ManagedEntity e : vms) {
				if (name.equals(e.getName())) {
					foundVms.add((VirtualMachine) MorUtil.createExactManagedEntity(service.getServerConnection(), e.getMOR()));
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
							throw new BuildException("Multiple running VMs match the VM name!");
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
	
	public Folder findFolder (String name) throws Exception {
		Folder found = null;
		for (ManagedEntity e : rootNavigator.searchManagedEntities("Folder")) {
			if (name.equals(e.getName())) {
				if (found != null) {
					throw new BuildException("Duplicate folders found!");
				}
				found = (Folder) MorUtil.createExactManagedEntity(service.getServerConnection(), e.getMOR());
			}
		}
		return found;
	}
	
	protected void trace (String message) {
		if (isVerbose())
			log(message);
	}
	
	private static URL getVSphereURL (String spec) throws MalformedURLException {
		if (spec == null || spec.isEmpty()) {
			throw new MalformedURLException();
		}
		if (spec.startsWith("http")) {
			return new URL(spec);
		}
		return new URL("https", spec, "/sdk");
	}
}
