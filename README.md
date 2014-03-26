vmware-ant
==========

Ant tasks for vmware vSphere:

    <project name="test" basedir=".">
        <taskdef resource="com/reanimation/ant/antlib.xml">
            <classpath>
                <pathelement location="vmware-ant-tasks.jar"/>
                <pathelement location="vijava-55b20130927.jar"/>
                <pathelement location="dom4j-1.6.1.jar"/>
            </classpath>
        </taskdef>  
    
        <property name="vcenter.host" value="192.168.1.2"/>
        <property name="vcenter.user" value="user-name"/>
        <property name="vcenter.password" value="password"/>
        <property name="vm.ip" value="192.168.1.3"/>
    
        <target name="main">
            <vsphere 
                host = "${vcenter.host}"
                user = "${vcenter.user}"
                password = "${vcenter.password}">
            
                <!-- Find the existing source VM using its IP address -->
                <findvm 
                    ip = "${vm.ip}"
                    nameProperty = "vm.name"
                    pathProperty = "vm.path"
                    vmidProperty = "vm.id"/>
                <echo message = "Found VM Name=${vm.name} MOR=${vm.id} Path=${vm.path}"/>
    
                <!-- Stop the VM -->
                <stopvm vmid = "${vm.id}"/>
                
                <!-- Clone the VM and wait for it to get an IP address -->
                <clonevm
                    vmid = "${vm.id}"
                    targetName = "${clone.name}"
                    targetVmIdProperty = "clone.id"
                    targetIpProperty = "clone.ip"
                    powerOn = "true">
                </clonevm>
                <echo message = "Cloned VM with IP ${clone.ip}"/>
    
                <!-- Delete the cloned VM -->
                <destroyvm vmid = "${clone.id}"/>
                
                <!-- Restart the source VM -->
                <startvm
                    vmid = "${vm.id}"
                    ipProperty = "vm.ip"/>
                <echo message = "Started VM with IP ${vm.ip}"/>
            </vsphere>
        </target>
    </project>      
    
    
## About

The tasks use the [VI Java API](http://vijava.sourceforge.net/) to communicate with an ESX or vCenter server and
do simple things that might be useful as part of an Ant build script.
  
There are three jars required: vmware-ant-tasks contains the tasks and uses vijava which uses on dom4j. So a taskdef
that looks like this should work:
  
    <taskdef resource="com/reanimation/ant/antlib.xml">
        <classpath>
            <pathelement location="vmware-ant-tasks.jar"/>
            <pathelement location="vijava-55b20130927.jar"/>
            <pathelement location="dom4j-1.6.1.jar"/>
        </classpath>
    </taskdef>
        
You could also place these three jars in your installed Ant's lib directory.
  
You can use the tasks in one of two ways:
  
  1. Use the [vSphere](../wiki/vSphere) task to connect to vSphere and place the other tasks as nested  
     children within this task. This is what is shown in the example at the top of this page. You can  
     also intermix any other Ant tasks within a vSphere task. 
  2. Do not use the vSphere task and instead use the remaining tasks "standalone." In this case you  
     just need to supply the vSphere connection parameters to each task, for example:

        <findvm 
            host = "${vcenter.host}"
            user = "${vcenter.user}"
            password = "${vcenter.password}"
            ip = "${vm.ip}"
            nameProperty = "vm.name"
            pathProperty = "vm.path"
            vmidProperty = "vm.id"/>
  
  

## Available Tasks

| Task       | Description
|:-----------|:-----------                                   
|[vSphere](../wiki/vSphere)     | A container task that makes a connection to a vSphere server and shares it with nested tasks
|[FindVM](../wiki/FindVM)      | Located an existing virtual machine
|[StartVM](../wiki/StartVM)     | Start a virtual machine
|[StopVM](../wiki/StopVM)      | Stop a virtual machine
|[DestroyVM](../wiki/DestroyVM)   | Delete a virtual machine
|[CloneVM](../wiki/CloneVM)     | Clone a virtual machine
|[ConfigureVM](../wiki/ConfigureVM) |  Reconfigure a virtual machine
