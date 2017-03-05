package sfanttasks.sfmetadatacleanup

import sfanttasks.helpers.ConfigHelper
import sfanttasks.xml.EscapingXmlNodePrinter

import groovy.io.FileType
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import org.apache.tools.ant.BuildException
import org.apache.tools.ant.Task

class SFMetadataCleanupTask extends Task {
	def srcFolder
	def configFile
	
	private config
	def pool = Executors.newFixedThreadPool(15)
	def futures = []

	void execute() {
		try {
			config = ConfigHelper.getCleanupConfig(configFile)
			def filesMatch = '\\.' + (config.keySet() as String[]).join('|') + '\$'
			Paths.get(srcFolder).eachFileRecurse(FileType.FILES) { Path file ->
				if (file =~ /$filesMatch/) {
					futures << pool.submit(new Thread(new Runnable() {
						void run() {
							def extension = file.toString()[file.toString().lastIndexOf('.')+1..-1]
							def fileXml = new XmlParser(false, false, false).parse(file.toFile())

							def removedNodes = 0
							config[extension].each { field, matchConfig ->
								fileXml[field].findAll { node ->
									def orMatch = false
									for (def matcher : matchConfig) {
										def andMatch = true
										for (def matchEntry : matcher.entrySet()) {
                                            // here matchEntry.value is an object "operation" : "string to compare with"
											andMatch = andMatch && doMatchOperations(node[matchEntry.key].text(), matchEntry.value) //node[matchEntry.key].text() =~ /${matchEntry.value}/)
											if (!andMatch) {
												break
											}
										}
										orMatch = orMatch || andMatch

										if (orMatch) {
											break
										}
									}

									orMatch
								}.each { node ->
									removedNodes++
									node.replaceNode {}
								}
							}

							if (removedNodes > 0) {
								println "$file had $removedNodes cleaned up"
								writeXmlToFile(fileXml, file as Path)
							} else {
								println "There was no node cleaned up in $file"
							}
						}
					}))
				}
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

    private Boolean doMatchOperations(nodeValue, matchOperations) {
        Boolean andMatch = true
        for (def matchOperation : matchOperations.entrySet()) {
            def operation = matchOperation.key
            def value = matchOperation.value
            if (MatchOperations.metaClass.respondsTo(MatchOperations.class, operation)) {
                andMatch = andMatch && MatchOperations."$operation"(value, nodeValue)
            } else {
                println "WARNING!! Match operation $operation cannot be applied, so it will be ignored. This could result in unexpected items being removed."
            }
            if (!andMatch) {
                break
            }
        }

        andMatch
    }

	private writeXmlToFile(xml, Path file) {
		def sw = new StringWriter()
		def printer = new EscapingXmlNodePrinter(new PrintWriter(sw), '    ')
		printer.with {
		  preserveWhitespace = true
		  expandEmptyElements = true
		}
		printer.print(xml)

		file.toFile().withWriter('UTF-8') { it.write "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n$sw" }
	}
}