package com.reanimation.ant.vmware.model;

import com.vmware.vim25.VAppOvfSectionInfo;
import com.vmware.vim25.VAppOvfSectionSpec;

public class AntVAppOvfSectionSpec extends AntArraySpec<VAppOvfSectionSpec> {
	
	public AntVAppOvfSectionSpec () {
		super(new VAppOvfSectionSpec());		
		spec.info = new VAppOvfSectionInfo();
	}
	
	public void addText (String contents) {
		setContents(contents);
	}
	
	public void setEnvelopeLevel  (boolean x) {
		spec.info.setAtEnvelopeLevel(x);
	}
	
	public void setNamespace (String namespace) {
		spec.info.setNamespace(namespace);
	}
	
	public void setType (String type) {
		spec.info.setType(type);
	}
	
	public void setContents (String contents) {
		spec.info.setContents(contents);
	}
}
