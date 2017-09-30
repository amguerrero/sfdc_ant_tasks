package sfanttasks.sfdeltadeployment

import sfanttasks.helpers.GitHelper
import sfanttasks.packagecreator.PackageCreator

import java.nio.file.StandardCopyOption
import java.nio.file.Files
import java.nio.file.Paths
import org.apache.tools.ant.BuildException
import org.apache.tools.ant.Task

class SFDeltaDeploymentTask extends Task {
	def deltaFolder = "delta"
	def destructiveFolder = "destructiveChanges"
	def previousDeployment
	def packageVersion
	def configFile
	def gitBaseDir = "."

	def deltaCreated = false
	def destructiveCreated = false

	void execute() {
		println "Preparing Delta Deployment file in $deltaFolder folder"

		// Retrieve files that changed from $previousDeployment 
		def deltaPath = Paths.get(deltaFolder)
		def destructivePath = Paths.get(destructiveFolder)
		try {
			GitHelper.withGitBaseDir(gitBaseDir).getFilesModifiedSince(previousDeployment)
				.inputStream.eachLine { line ->
					// Each line is a file, relative to the root of the git project
					def filePath = Paths.get(line)
					def gitFilePath = Paths.get("${gitBaseDir}/${line}")

					if (Files.exists(gitFilePath)) {
						deltaCreated = true
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
					} else {
						// The file was either removed
						destructiveCreated = true
						def destructiveFilePath = destructivePath.resolve(filePath)

						if (!Files.exists(destructiveFilePath.parent)) {
							Files.createDirectories(destructiveFilePath.parent)
						}

						destructiveFilePath.toFile().createNewFile()
					}
			}

			if (deltaCreated) {
				// Build package.xml in $deltaFolder/src using configFile if exists
				new PackageCreator(configFile, packageVersion, deltaPath.resolve("src"))
						.create("package.xml")
			}

			if (destructiveCreated) {
				def destructiveSrcPath = destructivePath.resolve("src")
				def packageCreator = new PackageCreator(configFile, packageVersion, destructiveSrcPath, true)

				packageCreator.createEmpty("package.xml")

				packageCreator.create("destructiveChanges.xml")

				println "Cleaning up $destructivePath"
				Files.copy(destructiveSrcPath.resolve("package.xml"), 
					destructivePath.resolve("package.xml"), StandardCopyOption.REPLACE_EXISTING)
				Files.copy(destructiveSrcPath.resolve("destructiveChanges.xml"),
					destructivePath.resolve("destructiveChanges.xml"), StandardCopyOption.REPLACE_EXISTING)

				destructiveSrcPath.toFile().deleteDir()
			}

			getProject().setProperty("DD_DELTA_CREATED", Boolean.toString(deltaCreated))
			getProject().setProperty("DD_DESTRUCTIVE_CREATED", Boolean.toString(destructiveCreated))
		} catch (Exception e) {
			throw new BuildException(e)
		}
	}
}