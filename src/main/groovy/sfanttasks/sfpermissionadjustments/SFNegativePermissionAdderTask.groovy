package sfanttasks.sfpermissionadjustments

import sfanttasks.helpers.ConfigHelper
import sfanttasks.helpers.GitHelper

import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import org.apache.tools.ant.BuildException
import org.apache.tools.ant.Task


class SFNegativePermissionAdderTask extends Task {
	def srcFolder
	def previousDeployment
	def negativesConfigFile

	def srcPath
	def negativeConfig

	def pool = Executors.newFixedThreadPool(15)
	def futures = []

	void execute() {
		srcPath = Paths.get(srcFolder)
		negativeConfig = ConfigHelper.getNegativeConfig(negativesConfigFile)
		
		def profilesPath = Paths.get("$srcFolder/src/profiles")
		def permissionSetsPath = Paths.get("$srcFolder/src/permissionsets")

		try {
			if (profilesPath.toFile().exists()) {
				println "Adding negatives from $previousDeployment to profiles in $profilesPath"
				profilesPath.eachFileMatch(~/^.+\.profile$/, addNegativesFuture(pool, futures))
			}
			if (permissionSetsPath.toFile().exists()) {
				println "Adding negatives from $previousDeployment to permissionsets in $permissionSetsPath"
				permissionSetsPath.eachFileMatch(~/^.+\.permissionset$/, addNegativesFuture(pool, futures))
			}
		} catch (Exception e) {
			throw new BuildException(e)
		}

		pool.shutdown()

		def errors = []
		futures.each { future ->
		        try {
		                future.get()
		        } catch (ExecutionException ee) {
		                println "Exception!! $ee"
		                errors << errors.size() + " -- ${ee.getMessage()}"
		        }
		}
		if (!errors.isEmpty())
		        throw new RuntimeException('\n' + errors.join('\n'))
	}

	private Closure addNegativesFuture(def pool, def futures) {
		{ Path file ->
			futures << pool.submit(new Thread(new Runnable() {
				void run() {
					def parser = new XmlParser(false, false, false)
					def gitFile = file.subpath(srcPath.getNameCount(), file.getNameCount())
					def previousVersionContent = GitHelper.getPreviousVersionOf(gitFile, previousDeployment)

					def previousXml = parser.parseText(previousVersionContent.text)
					def previousUniqueIds = generateNodeUniqueIds(previousXml)

					def currentXml = parser.parse(file.toFile())
					currentXml."*".findAll { node ->
						def nodeName = (node.name() instanceof groovy.xml.QName) ? node.name().getLocalPart() : node.name()
						negativeConfig."$nodeName"
					}.each { node ->
						def nodeName = (node.name() instanceof groovy.xml.QName) ? node.name().getLocalPart() : node.name()
						def nodeConfig = negativeConfig[nodeName]
						previousUniqueIds.remove("${nodeName}#" + node[nodeConfig.id].text())
					}

					if (!previousUniqueIds.isEmpty()) {
						println "Adding ${previousUniqueIds.size()} to $file"

						previousUniqueIds.each { uniqueId ->
							def (nodeName, id) = uniqueId.split("#")
							def nodeConfig = negativeConfig[nodeName]
							def nodeTemplate = nodeConfig.negativeNodeTemplate
							def negativeNode = parser.parseText(nodeTemplate)
							negativeNode[nodeConfig.id][0].value()[0] = id

							currentXml.append negativeNode
						}

						currentXml.children().sort(true) {
							(it.name() instanceof groovy.xml.QName) ? it.name().getLocalPart() : it.name()
						}
						writeXmlToFile(currentXml, file)
					}
				}
			}))
		}
	}

	private def generateNodeUniqueIds(def root) {
		def uniqueIds = []

		root."*".findAll { node ->
			def nodeName = (node.name() instanceof groovy.xml.QName) ? node.name().getLocalPart() : node.name()
			negativeConfig."$nodeName"
		}.each { node ->
			def nodeName = (node.name() instanceof groovy.xml.QName) ? node.name().getLocalPart() : node.name()
			def nodeConfig = negativeConfig[nodeName]
			uniqueIds << "${nodeName}#" + node[nodeConfig.id].text()
		}

		uniqueIds
	}

	private def writeXmlToFile(def xml, Path file) {
		def sw = new StringWriter()
		def printer = new XmlNodePrinter(new PrintWriter(sw), '    ')
		printer.with {
		  preserveWhitespace = true
		  expandEmptyElements = true
		}
		printer.print(xml)

		file.toFile().withWriter('UTF-8') { it.write "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n$sw" }
	}
}