package sfanttasks.xml

class EscapingXmlNodePrinter extends XmlNodePrinter {
   EscapingXmlNodePrinter(PrintWriter out) {
      super(out)
   }
   EscapingXmlNodePrinter(PrintWriter out, String indent) {
      super(out, indent)
   }

   void printSimpleItem(Object value) {
      value = value.replaceAll(~/\'/, '&apos;')
                   .replaceAll(~/\"/, '&quot;')
      out.print(value)
   }
}