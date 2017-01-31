package sfanttasks.xml

import groovy.xml.XmlUtil

class EscapingXmlNodePrinter extends XmlNodePrinter {
   EscapingXmlNodePrinter(PrintWriter out) {
      super(out)
   }
   EscapingXmlNodePrinter(PrintWriter out, String indent) {
      super(out, indent)
   }

   void printSimpleItem(Object value) {
      out.print(XmlUtil.escapeXml(value))
  }
}