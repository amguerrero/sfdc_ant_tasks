# Salesforce.com Helper ANT Tasks
Salesforce.com Helper ANT Tasks project is a lib where I'm compiling some ant tasks I think are useful to keep a Salesforce.com metadata git clean while keeping the deployment as hassle free as possible.
These are the tasks you can find in the project:
  - **Salesforce.com Delta Deployment Task**: allows you to create a directory ready to deploy just the files that are different between a git tag or commit and the HEAD of the current branch.
  - **Salesforce.com Metadata Cleanup Task**: allows you to remove some nodes from the metadata in a specific directory. The nodes to be removed can be defined by configuration files.
  - **Salesforce.com Negative Permissions Adder Task**: given you have a Salesforce.com metadata git repository in which you only store positive permissions in the profiles and permission sets and given a git commit or tag, this task adds the missing permissions to the profiles and permission sets that are going to be actually deployed as negative permissions. Thus the permissions won't be ignored when deployed.

## Quick Usage Guide
Ok, enough is enough, you just want to use this ANT tasks, so let's go for the prerequisites, before doing anything be aware that these ant tasks need for **Delta Deployment Task** and **Negative Permissions Adder Task**:
  - To have git installed in the system and available in the PATH of the user executing the ant targets.
  - To be used from the root of a git project

Being happy with these prerequisites we need to add these tasks so we can use them in our ANT build.xml file.
First we need to copy the jar file [lib/SalesforceAntTasks-with-dependencies.jar](https://github.com/amguerrero/sfdc_ant_tasks/blob/master/lib/SalesforceAntTasks-with-dependencies.jar) somewhere so we can reference it from build.xml file like the dir *libs/* for instance:
```xml
<taskdef name="deltaDeployment"
    classpath="libs/SalesforceAntTasks-with-dependencies.jar"
    classname="sfanttasks.sfdeltadeployment.SFDeltaDeploymentTask" />
```
```xml
<taskdef name="addNegativePermisions"
    classpath="libs/SalesforceAntTasks-with-dependencies.jar"
    classname="sfanttasks.sfpermissionadjustments.SFNegativePermissionAdderTask" />
```
```xml
<taskdef name="metadataCleanup"
    classpath="libs/SalesforceAntTasks-with-dependencies.jar"
    classname="sfanttasks.sfmetadatacleanup.SFMetadataCleanupTask" />
```
Once we have the task defined in the build.xml, we can begin using them like this:
```xml
<target name="deploy">
    <deltaDeployment deltaFolder="delta"
        previousDeployment="v.1.0.1" />
    <addNegativePermisions srcFolder="delta"
        previousDeployment="v.1.0.1" />
    <metadataCleanup srcFolder="delta" />
    <sf:deploy deployRoot="delta/src" ... />
</target>
```
In this example, the target **deploy**:
  - Creates *delta/* directory and copies there only the files that changed between the commit tagged as v.1.0.1 and the HEAD of the current branch. It keeps salesforce package structure inside *delta/* directory. And generates the **delta/src/package.xml** file.
  - Adds the permissions that were removed between v.1.0.1 and the current branch HEAD to the profiles and permission sets in *delta/src/* as negative permissions.
  - Cleans up all the metadata in *delta/src/*. In this case uses the configuration by default (in short removes all the mentions to 3rd party packages -managed or not- from objects, profiles and permission sets, and the list views on the objects)
  - Finally, deploys the package we have automatically created in *delta/src/*

## Configuration files
In *config/* directory you can find an example of the config files for the Salesforce.com Helper ANT Tasks. These configuration files are actually the configuration by default if the files are not provided.