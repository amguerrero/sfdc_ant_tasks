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
	def lastDeployment =  null
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
			if (!lastDeployment) {
				println "Last deployment commit/branch was not provided, setting it to HEAD"
				lastDeployment = "HEAD"
			} else {
				println "Last deployment commit/branch set to $lastDeployment"
			}
			GitHelper.withGitBaseDir(gitBaseDir).getFilesModifiedBetweenCommits(previousDeployment, lastDeployment)
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

						// If -meta.xml file has been updated, include the file it describes
						def pathWithoutMetaEnding = line.replace('-meta.xml', '')
						def gitNonMetaFilePath = Paths.get("${gitBaseDir}/${pathWithoutMetaEnding}")
						def deltaNonMetaFilePath = deltaPath.resolve(Paths.get(pathWithoutMetaEnding))

						if (line.endsWith('-meta.xml') && !Files.exists(deltaNonMetaFilePath)) {
							Files.copy(gitNonMetaFilePath, deltaNonMetaFilePath, StandardCopyOption.REPLACE_EXISTING)							
						}

						// Check if there is a -meta.xml file, and copy it to delta dir even it didn't change
						def gitMetaFilePath = Paths.get("${gitBaseDir}/${line}-meta.xml")
						if (Files.exists(gitMetaFilePath)) {
							def deltaMetaFilePath = deltaPath.resolve(Paths.get(line + "-meta.xml"))
							println "Found Meta XML file ${gitMetaFilePath}: copying it to ${deltaMetaFilePath}"
							Files.copy(gitMetaFilePath, deltaMetaFilePath, StandardCopyOption.REPLACE_EXISTING)
						}

						// Include metadata files for e.g. document folders
						def gitParentMetaFilePath = Paths.get("${gitFilePath.parent}-meta.xml")
						def deltaParentMetaFilePath = Paths.get("${deltaFilePath.parent}-meta.xml")

						if (Files.exists(gitParentMetaFilePath) && !Files.exists(deltaParentMetaFilePath)) {
							Files.copy(gitParentMetaFilePath, deltaParentMetaFilePath, StandardCopyOption.REPLACE_EXISTING)
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