package com.reanimation.ant.vmware.model;

import com.vmware.vim25.VAppProductInfo;
import com.vmware.vim25.VAppProductSpec;

public class AntVAppProductSpec extends AntArraySpec<VAppProductSpec> {
	
	public AntVAppProductSpec () {
		super(new VAppProductSpec());
		spec.info = new VAppProductInfo();
	}
	
	public void setAppUrl (String s) {
		spec.info.setAppUrl(s);
	}
	
	public void setFullVersion (String s) {
		spec.info.setFullVersion(s);
	}
	
	public void setName (String s) {
		spec.info.setName(s);
	}
	
	public void setProductUrl (String s) {
		spec.info.setProductUrl(s);
	}
	
	public void setVendor (String s) {
		spec.info.setVendor(s);
	}
	
	public void setVendorUrl (String s) {
		spec.info.setVendorUrl(s);
	}
	
	public void setVersion (String s) {
		spec.info.setVersion(s);
	}
}
