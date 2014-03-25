package com.reanimation.ant.vmware.model;

import java.lang.reflect.Method;

import org.apache.tools.ant.Task;

import com.vmware.vim25.ArrayUpdateOperation;
import com.vmware.vim25.ArrayUpdateSpec;

public class AntArraySpec<SpecType extends ArrayUpdateSpec> {
	
	protected SpecType spec;
	
	private Integer key = null;
	private ArrayUpdateOperation operation = null;
	
	public AntArraySpec (SpecType spec) {
		this.spec = spec;
	}
	
	public SpecType getSpec () {
		return spec;
	}
	
	
	public <InfoType> void processExisting (Task task, InfoType[] existing) {
		if (operation == null) {
			
			if (key != null) {
				InfoType found = null;
				if (existing != null) {
					for (InfoType info : existing) {
						try {
							Method getKey= info.getClass().getMethod("getKey");
							Object infoKey = getKey.invoke(info);
							if (key.equals(infoKey)) {
								found = info;
							}
						} catch (Exception ex) {
							throw new RuntimeException("Error getting key from info object", ex);
						}
					}
				}
				operation = found == null ? ArrayUpdateOperation.add : ArrayUpdateOperation.edit;
			} else {
				operation = ArrayUpdateOperation.add;
			}
			task.log("Auto set " + operation + " array operation for " + spec.getClass().getSimpleName() + " key " + key);
			spec.setOperation(operation);
		}
	}
	
	public void setKey (int key) {
		this.key = key;
		try {
			Method getInfo = spec.getClass().getMethod("getInfo");
			Object info = getInfo.invoke(spec);
			
			Method setKey = null;
			for (Method m : info.getClass().getMethods()) {
				if (m.getName().equals("setKey")) {
					setKey = m;
				}
			}
			setKey.invoke(info, key);
		} catch (Exception ex) {
			throw new RuntimeException("Error setting key on info object: " + ex.getMessage(), ex);
		}
	}
	
	public void setArrayOperation (String operation) {
		this.operation = ArrayUpdateOperation.valueOf(operation);
		spec.setOperation(this.operation);
	}
	
	public void setRemoveKey (int key) {
		spec.setRemoveKey(key);
	}
	
	
}
