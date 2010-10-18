

import java.lang.management.ManagementFactory
import javax.management.MBeanOperationInfo
import javax.management.ObjectName

server = ManagementFactory.getPlatformMBeanServer();
home_url = "jmx.groovy"
inspect_mbean = "inspect_mbean"
invoke_mbean = "invoke_mbean"
invoke_op = "invoke_op"

html.html {
  head {
    title("Jmx Console")
    script(type: "text/javascript", src: "shCore.js", " ")
    script(type: "text/javascript", src: "shBrushGroovy.js", " ")
    script(type: "text/javascript", src: "shBrushXml.js", " ")
    link(href: "shCore.css", rel: "stylesheet", type: "text/css")
    link(href: "shThemeDefault.css", rel: "stylesheet", type: "text/css")
  }
  body {
    script(type: "text/javascript", "SyntaxHighlighter.all()")
    try {
      if (params[inspect_mbean]) {
        inspectMbean()
      } else if (params[invoke_mbean]) {
        invokeMbean()
      } else {
        showMbeans()
      }
    } catch (Throwable t) {
      def strw = new StringWriter()
      t.printStackTrace(new PrintWriter(strw))
      pre(strw)
      throw t;
    }
  }
}

def showMbeans() {
  def domains = server.getDomains()
  domains.each {domain ->
    html.h1(domain)
    html.ul {
      def names = server.queryNames(new ObjectName(domain + ":*"), null).sort()
      names.each {name ->
        li {
          inspectLink(name, name)
        }
      }
    }
  }
}

def inspectMbean() {
  def name = params[inspect_mbean]
  homeLink()
  def mbean = new GroovyMBean(server, name)
  def beanInfo = mbean.info()
  def beanClass = beanInfo.getClassName()
  html.h2("Bean [$name], Class[$beanClass]")

  if (beanInfo.getOperations().size() > 0) {
    html.h3("Operations")
  }
  beanInfo.getOperations().sort{it.name}.each {op ->
    operationForm(mbean, op, name)
  }

  attributeList(mbean)
}

def invokeMbean() {
  def mbeanName = params[invoke_mbean]
  def opName = params[invoke_op]
  def mbean = new GroovyMBean(server, mbeanName)
  def beanInfo = mbean.info()

  html.table(cellspacing: 4, cellpadding: 4) {
    tr {
      td { homeLink() };
      td { inspectLink(mbeanName, "Back to MBean") };
      td { reinvokeMBean(mbeanName, "Re-invoke MBean") };
      def q = new StringBuilder()
      def prefix = "?"
      params.each {k, v -> q << prefix + k + '=' + v; prefix = "&"}
      td { a(href: q, "Re-invoke GET")};
    }
  }

  html.form(action: home_url, method: "post", name: "reinvoker") {
    params.each {k, v -> input(type: "hidden", name: k, value: v) }
  }

  MBeanOperationInfo info = beanInfo.getOperations().find {op -> op.getName().equals(opName)}
  def values = info.getSignature().collect {sig ->
    JmxValuesConvertor.createParameterValue(sig.getType(), params[sig.getName()])
  }
  def ret = mbean."$opName"(* values)
  html.h3("Bean [$mbeanName] Op [$opName] Returned: ")

  out.print "<pre>"
  out.print(ret != null ? ret : "Nothing")
  out.print "</pre>"
}

private def operationForm(mbean, MBeanOperationInfo op, name) {
  html.table(cellspacing: 2, cellpadding: 2) {
    tr {
      td { b(op.name) }; td("//"); td(op.description)
    }
  }

  html.form(action: home_url, method: "post") {
    input(type: "hidden", name: invoke_mbean, value: name)
    input(type: "hidden", name: invoke_op, value: op.name)
    if (op.signature.size() > 0) {
      table(cellspacing: 2, cellpadding: 2, border: 1) {
        tr {
          th("Name"); th("Type"); th("Value"); th("Description")
        }
        op.signature.each {param ->
          tr {
            td(param.getName())
            td(param.getType())
            td {

              if (param.getType().equals(Boolean.class.getName()) || param.getType().equals("boolean")) {
                input(type: "radio", name: param.getName(), value: "True", checked: "True", "True")
                input(type: "radio", name: param.getName(), value: "False", "False")
              } else {
                input(type: "text", name: param.getName())
              }
            }
            td(param.getDescription())
          }
        }
      }
    }
    input(type: "submit", value: "Invoke")
  }
}

private def attributeList(GroovyMBean mBean) {
  if (mBean.info().getAttributes().size() == 0) return;
  html.h3("Attributes")
  html.table(border: 1) {
    tr {th("Name"); th("Value"); th("Description")}
    mBean.info().getAttributes().each {attr ->
      if (attr.isReadable()) {
        def name = attr.getName()
        tr { td(name); td(mBean."$name"); td(attr.getDescription()) }
      }
    }
  }
}

private def homeLink() {
  html.a(href: home_url, "Home")
}

private def inspectLink(name, message) {
  html.a(href: "?$inspect_mbean=$name", message)
}

private def reinvokeMBean(name, message) {
  html.a(href: "javascript:document.reinvoker.submit()", message)
}
