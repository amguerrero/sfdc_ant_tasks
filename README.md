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
        destructiveFolder="destructiveChanges"
        gitBaseDir="git_repository/sfdc_project"
        previousDeployment="v.1.0.1" />
    <addNegativePermisions srcFolder="delta"
        gitBaseDir="git_repository/sfdc_project"
        previousDeployment="v.1.0.1" />
    <metadataCleanup srcFolder="delta" />
    <sf:deploy deployRoot="delta/src" ... />
    <sf:deploy deployRoot="destructiveChanges" ... />
</target>
```
In this example, the target **deploy**:
  - Creates *delta/* directory and copies there only the files that changed between the commit tagged as v.1.0.1 and the HEAD of the current branch. It keeps salesforce package structure inside *delta/* directory. And generates the **delta/src/package.xml** file. The **gitBaseDir** attribute of deltaDeployment points to the root of the local git clone of the Salesforce project.
  - Creates *destructiveChanges/* directory and inside it creates an empty *package.xml** and generates a **destructiveChanges.xml**. **deltaFolder** and **destructiveFolder** attributes are optional, if they are not give, by default they values are "delta" and "destructiveChanges" respectively.
  - Adds the following properties to the ant project:
    + **DD_DELTA_CREATED**: true if the delta directory was created, thus a normal deployment is required
    + **DD_DESTRUCTIVE_CREATED**: true if the destructiveChanges directory was created, thus a destructive changes deployment is required
  - Adds the permissions that were removed between v.1.0.1 and the current branch HEAD to the profiles and permission sets in *delta/src/* as negative permissions. The **gitBaseDir** attribute of deltaDeployment points to the root of the local git clone of the Salesforce project.
  - Cleans up all the metadata in *delta/src/*. In this case uses the configuration by default (in short removes all the mentions to 3rd party packages -managed or not- from objects, profiles and permission sets, and the list views on the objects)
  - Finally, deploys the package we have automatically created in *delta/src/*

## Configuration files
In *config/* directory you can find an example of the config files for the Salesforce.com Helper ANT Tasks. These configuration files are actually the configuration by default if the files are not provided.

### Update for *v.0.1*
From the version tagged **v.0.1** there is a change in how to configure the Metadata Cleanup, it will use matching operations instead of regular expressions, i.e.:
```
{
  "object": {
    "fields": [
      { "fullName": { "isManagedPackage" : "true" } }
    ],
    "validationRules": [
      { "fullName": { "startsWith" : "REMOVE_ME" } }
    ],
    "webLinks": [
      { "fullName": { "endsWith" : "Delete__c" } }
    ],
    "listViews": [
      { "fullName": { "always" : "" } }
    ]
  },
  "profile": {
    "applicationVisibilities": [
      { "application": { "matches" : ".+(XXX|999).+" } },
      {
        "default": { "equalTo" : "false" },
        "visible": { "equalTo" : "false" }
      } ],
    "objectPermissions": [
      { "object": { "doesNotContain" : "My_Object" } }
    ]
  }
}
```
In the example above we're configuring the metadata cleanup task to:
   - Delete all the fields in the objects which fullName is a Managed Package added field.
   - All the validation rules with a name starting with "REMOVE_ME" from the objects
   - All the web links ending with "Delete__c" from the objects
   - All the list views from the objects
   - All the application visibilites with the name matching ".+(XXX|999).+" regular expression from the profiles
   - All the application visiblities where default and visible are both "false" from the profiles
   - All the object permissions whose names don't contain "My_Object" in them from the profiles.

There are now the following matching operations:
   - **matches** / **doesNotMatch**: accepts a regular expression as value, and checks if the value of the field matches or doesn't match respectively.
   - **startsWith** / **doesNotStartWith**: accepts a string as value and checks if the field text starts (or doesn't start) with the value passed.
   - **endsWith** / **doesNotEndWith**: accepts a string as value and checks if the field text ends (or doesn't end) with that value.
   - **contains** / **doesNotContain**: accepts a string as value and checks if the field text contains (or doesn't contain) the value configured.
   - **equalTo** / **notEqualTo**: accepts a string as value and checks if the field text is (or isn't) exactly equal to the value configured.
   - **isManagedPackage**: accepts a string with the following values: **"true"** or **"false"** and checks if the field value belongs (in case the configuration is "true") or doesn't belong (in case the configured value is "false") to a managed package.
   - **always**: needs a value, but ignores it, so it can perfectly be an empty string. It is that way to be compatible with the other operations. it will remove all the fields with this configuration.

## Building the jar files
In order to build the jar files after modifying the project's classes there are a couple of options:
With gradle:
```
# Gradle
$ gradle fatJar
```
It will create the following jar file:
```
build/libs/sfdc_ant_tasks-all.jar
```
With maven:
```
# Maven
$ mvn package
```
It will create the following jar files:
```
target/SalesforceAntTasks-1.0-SNAPSHOT-jar-with-dependencies.jar
target/SalesforceAntTasks-1.0-SNAPSHOT.jar
```
