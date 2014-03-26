vmware-ant
==========

Ant tasks for vmware vSphere

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
    
        <target name="main">
            <vsphere 
                host = "${vcenter.host}"
                user = "${vcenter.user}"
                password = "${vcenter.password}">
            
                <findvm 
                    ip="192.168.1.4"
                    nameProperty="vm.name"
                    pathProperty="vm.path"
                    morProperty="vm.id"/>
                
                <echo message = "Found VM Name=${vm.name} MOR=${vm.id} Path=${vm.path}"/>
                
                <stopvm mor="${vm.id}"/>
                
                <clonevm
                    mor = "${vm.id}"
                    targetName = "my-cloned-vm"
                    targetVmIdProperty = "clone.id"
                    powerOn = "false">
                </clonevm>
                
                <startvm
                    mor="${vm.id}"
                    ipProperty="vm.ip"/>
                
                <echo message="Started VM with IP ${vm.ip}"/>
                
                <destroyvm mor="${clone.id}"/>
            </vsphere>
        </target>
    </project>      
    
    
## How To Use  

## Available Tasks

findvm
startvm
stopvm
destroyvm
clonevm
