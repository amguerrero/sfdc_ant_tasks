package sfanttasks.sfnodesubstitution

import groovy.io.FileType
import org.apache.tools.ant.BuildException
import org.apache.tools.ant.Task
import sfanttasks.sfnodesubstitution.nodes.Always
import sfanttasks.xml.EscapingXmlNodePrinter

import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors

class SFNodeSubstitutionTask extends Task {
    Set<Always> alwaysTags = new LinkedHashSet<>()
    def srcFolder
    def config = [:]

    def pool = Executors.newFixedThreadPool(15)
    def futures = []

    void execute() {
        buildConfiguration()

        try {
            def filesMatch = '.+\\.(' + (config.keySet() as String[]).join('|') + ')\$'
            Paths.get(srcFolder).eachFileRecurse(FileType.FILES) { Path file ->
                if (!(file =~ /$filesMatch/)) {
                    return
                }

                futures << pool.submit {
                    def extension = file.toString()[file.toString().lastIndexOf('.')+1..-1]
                    def fileXml = new XmlParser(false, false, false).parse(file.toFile())

                    // First process 'always' substitutions
                    config[extension]['always'].each { field, value ->
                        fileXml.children().'**'.findAll { node ->
                            if (node instanceof String) {
                                return
                            }

                            def fullName = [ node.name() ]
                            def parent = node.parent()
                            while (parent.parent()) {
                                fullName.add(0, parent.name())
                                parent = parent.parent()
                            }

                            fullName.join('.') == field
                        }.each { node ->
                            node.value()[0] = value
                        }
                    }

                    writeXmlToFile(fileXml, file)
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

    private buildConfiguration() {
        alwaysTags.forEach { always ->
            def confFileType = config[always.fileType]
            if (!confFileType) {
                confFileType = [:]
                confFileType.always = [:]
                config[always.fileType] = confFileType
            }

            always.nodes.forEach { node ->
                confFileType['always'][node.name] = always.value
            }
        }
    }

    Always createAlways() {
        Always always = new Always()
        alwaysTags.add(always)

        always
    }
}