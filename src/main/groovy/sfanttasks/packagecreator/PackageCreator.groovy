package sfanttasks.packagecreator

import sfanttasks.helpers.ConfigHelper
import sfanttasks.xml.EscapingXmlNodePrinter

import java.nio.file.Path

class PackageCreator {
	private final config
	private final parser = new XmlParser(false, false, false)

	private final Path srcDir
	private final Node packageRoot

	private final boolean avoidAsterisks

	PackageCreator(String configFile, String packageVersion, Path srcDir) {
		this.config = ConfigHelper.getPackageConfig(configFile)
		if (packageVersion) {
			this.config.version = packageVersion
		}
		this.srcDir = srcDir
		this.avoidAsterisks = false
		this.packageRoot = parser.parseText("""
<Package xmlns=\"http://soap.sforce.com/2006/04/metadata\">
    <version>${this.config.version}</version>
</Package>
""")
	}

	PackageCreator(String configFile, String packageVersion, Path srcDir, boolean avoidAsterisks) {
		this(configFile, packageVersion, srcDir)
		this.avoidAsterisks = avoidAsterisks
	}

	void createEmpty(def packageFileName) {
		writePackageXml(packageFileName, packageRoot)
	}

	void create(def packageFileName) {
		println "Finding directories and files to create package.xml in $srcDir"
		srcDir.toFile().eachDir {dir ->
			if (config.dirs."${dir.name}") {
				def nodeConfig = config.dirs."${dir.name}"

				println " * Found ${dir.name}: adding ${nodeConfig.xmlTag}"
				def types = typesNode()
				if (!avoidAsterisks && nodeConfig.acceptsAsterisk) {
					types.append membersNode("*")
				} else {
					if (nodeConfig.extension) {
						addMembers(types, dir, nodeConfig.extension)
					} else {
						addMembersExcludePattern(types, dir, nodeConfig.excludeExtension)
					}
				}
				types.append nameNode(nodeConfig.xmlTag)
				packageRoot.append types
			} else {
				println " * '${dir.name}' directory is not configured... It will be ignored"
			}
		}

		writePackageXml(packageFileName, packageRoot)
	}

	private def addMembersExcludePattern(def node, def dir, def excludeExtension) {
		dir.eachDir() { subDir ->
			node.append membersNode(subDir.name)

			subDir.eachFile() { file ->
				if (!file.name.endsWith(excludeExtension)) {
					node.append membersNode(subDir.name + "/" + file.name)
				}
			}	
		}
	}

	private def addMembers(def node, def dir, def extension) {
		dir.eachDir() { subDir ->
			node.append membersNode(subDir.name)

			subDir.eachFileMatch(~/.*${extension}/) { file ->
				node.append membersNode(subDir.name + "/" + (file.name - "$extension"))
			}	
		}
		dir.eachFileMatch(~/.*${extension}/) { file ->
			node.append membersNode(file.name - "$extension")
		}
	}

	private def typesNode() {
		parser.parseText('<types></types>')
	}

	private def membersNode(def member) {
		def node = parser.parseText("<members></members>")
		node.value = member

		node
	}

	private def nameNode(def name) {
		def node = parser.parseText("<name></name>")
		node.value = name

		node
	}

	private def writePackageXml(def packageFileName, def xml) {
		def packageXmlPath = srcDir.resolve(packageFileName)
		println "Writting $packageFileName to $packageXmlPath"
		def sw = new StringWriter()
		def printer = new EscapingXmlNodePrinter(new PrintWriter(sw), '    ')
		printer.with {
		  preserveWhitespace = true
		  expandEmptyElements = true
		}
		printer.print(xml)

		packageXmlPath.toFile().withWriter('UTF-8') { it.write "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n$sw" }
	}
}