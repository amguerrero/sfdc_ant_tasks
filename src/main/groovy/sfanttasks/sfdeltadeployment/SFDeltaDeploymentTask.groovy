package sfanttasks.sfdeltadeployment

import sfanttasks.helpers.ConfigHelper
import sfanttasks.helpers.GitHelper
import sfanttasks.packagecreator.PackageCreator

import java.nio.file.StandardCopyOption
import java.nio.file.Files
import java.nio.file.Paths
import org.apache.tools.ant.BuildException
import org.apache.tools.ant.Task

class SFDeltaDeploymentTask extends Task {
	def deltaFolder
	def previousDeployment
	def configFile

	void execute() {
		println "Preparing Delta Deployment file in $deltaFolder folder"

		// Retrieve files that changed from $previousDeployment 
		def deltaPath = Paths.get(deltaFolder)
		try {
			GitHelper.getFilesModifiedSince(previousDeployment)
				.inputStream.eachLine { line ->
					// Each line is a file, relative to the root of the git project
					def filePath = Paths.get(line)
					def deltaFilePath = deltaPath.resolve(filePath)

					if (!Files.exists(deltaFilePath.parent)) {
						Files.createDirectories(deltaFilePath.parent)
					}

					Files.copy(filePath, deltaPath.resolve(filePath), StandardCopyOption.REPLACE_EXISTING)
					//println "Copying $line to $deltaFolder"
			}
			// Build package.xml in $deltaFolder/src using configFile if exists
			new PackageCreator(configFile, deltaPath.resolve("src"))
					.create()
		} catch (Exception e) {
			throw new BuildException(e)
		}
	}
}