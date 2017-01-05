package sfanttasks.sfdeltadeployment

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
	def gitBaseDir = "."

	void execute() {
		println "Preparing Delta Deployment file in $deltaFolder folder"

		// Retrieve files that changed from $previousDeployment 
		def deltaPath = Paths.get(deltaFolder)
		try {
			GitHelper.withGitBaseDir(gitBaseDir).getFilesModifiedSince(previousDeployment)
				.inputStream.eachLine { line ->
					// Each line is a file, relative to the root of the git project
					def filePath = Paths.get(line)
					def gitFilePath = Paths.get("${gitBaseDir}/${line}")
					def deltaFilePath = deltaPath.resolve(filePath)

					if (!Files.exists(deltaFilePath.parent)) {
						Files.createDirectories(deltaFilePath.parent)
					}

					Files.copy(gitFilePath, deltaFilePath, StandardCopyOption.REPLACE_EXISTING)

					// Check if there is a -meta.xml file, and copy it to delta dir even it didn't change
					def gitMetaFilePath = Paths.get("${gitBaseDir}/${line}-meta.xml")
					if (Files.exists(gitMetaFilePath)) {
						def deltaMetaFilePath = deltaPath.resolve(Paths.get(line + "-meta.xml"))
						println "Found Meta XML file ${gitMetaFilePath}: copying it to ${deltaMetaFilePath}"
						Files.copy(gitMetaFilePath, deltaMetaFilePath, StandardCopyOption.REPLACE_EXISTING)
					}
			}
			// Build package.xml in $deltaFolder/src using configFile if exists
			new PackageCreator(configFile, deltaPath.resolve("src"))
					.create()
		} catch (Exception e) {
			throw new BuildException(e)
		}
	}
}