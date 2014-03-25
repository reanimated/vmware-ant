package com.reanimation.ant.vmware.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.tools.ant.Task;

import com.vmware.vim25.VAppOvfSectionSpec;
import com.vmware.vim25.VAppProductSpec;
import com.vmware.vim25.VmConfigInfo;
import com.vmware.vim25.VmConfigSpec;

public class AntVmConfigSpec {
	
	public VmConfigSpec spec = new VmConfigSpec();
	
	private List<AntVAppOvfSectionSpec> ovfSections = new ArrayList<AntVAppOvfSectionSpec>();
	private List<AntVAppProductSpec> products = new ArrayList<AntVAppProductSpec>();
	
	public AntVmConfigSpec () {
	}
	
	public void processExisting (Task task, VmConfigInfo existing) {
		for (AntVAppOvfSectionSpec ovfSection : ovfSections)
			ovfSection.processExisting(task, existing == null ? null : existing.getOvfSection());
		
		for (AntVAppProductSpec product : products)
			product.processExisting(task, existing == null ? null : existing.getProduct());
	}
	
	public AntVAppOvfSectionSpec createOvfsection () {
		AntVAppOvfSectionSpec section = new AntVAppOvfSectionSpec();
		ovfSections.add(section);
		VAppOvfSectionSpec[] current = spec.getOvfSection();
		if (current == null) {
			spec.setOvfSection(new VAppOvfSectionSpec[] {section.getSpec()});
		} else {
			VAppOvfSectionSpec[] list = new VAppOvfSectionSpec[current.length + 1];
			System.arraycopy(current, 0, list, 0, current.length);
			list[current.length] = section.getSpec();
			spec.setOvfSection(list);
		}
		return section;
	}
	
	public AntVAppProductSpec createProduct () {
		AntVAppProductSpec p = new AntVAppProductSpec();
		products.add(p);
		VAppProductSpec[] current = spec.getProduct();
		if (current != null) {
			List<VAppProductSpec> list = Arrays.asList(current);
			list.add(p.getSpec());
			spec.setProduct(list.toArray(new VAppProductSpec[0]));
		} else {
			spec.setProduct(new VAppProductSpec[] {p.getSpec()});
		}
		
		return p;
	}
	
	public void setEula (String s) {
		spec.setEula(new String[] {s});
	}
}
