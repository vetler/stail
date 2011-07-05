import java.io.File
import scala.io.Source
import com.jcraft.jsch._

object SSHLog {
  var forwardingPort = 10000 // Next available port for port forwarding

  def usage() {
    println("""
Usage: sshlog <FILE>
""")

    exit(1)
  }

  def main(args: Array[String]) {
    if (args.length == 0) usage

    var configurations = List[Configuration]()
    val configurationFile = new File(args(0))
    Source.fromFile(configurationFile)("UTF-8").getLines() foreach { untrimmedLine =>
      val line = untrimmedLine.trim
      if (line matches "\\[.*\\]") {
        val id = line substring (line.indexOf("[") + 1, line.indexOf("]"))
        configurations = configurations ::: List(new Configuration(id))
      }

      if (line matches "file\\s*=\\s*.*") {
        val filename = line.substring(line.indexOf("=") + 1).trim
        configurations.last.addFile(filename)
      }

      if (line matches "server\\s*=\\s*.*") {
        val server = line.substring(line.indexOf("=") + 1).trim
        configurations.last.setServer(server)
      }

      if (line matches "user\\s*=\\s*.*") {
        val user = line.substring(line.indexOf("=") + 1).trim
        configurations.last.user = user
      }

      if (line matches "password\\s*=\\s*.*") {
        val password = line.substring(line.indexOf("=") + 1).trim
        configurations.last.password = password
      }

      if (line matches "forward-through\\s*=\\s*.*") {
        val forwardThroughServer = line.substring(line.indexOf("=") + 1).trim
        configurations.last.setForwarding(forwardThroughServer)
      }

    }

    if (configurations.size == 0) {
      println("No configurations found in " + args(0))
      exit(1)
    } else {
      for (config <- configurations if config.id == args(1)) {
        config.forwardThroughServer match {
          case Some(forwardedThroughServer) => {
            println("Tailing log files on " + config.server + ", forwarded through " + forwardedThroughServer)
            val jsch = new JSch()
            val forwardingSession = createSession(jsch, forwardedThroughServer, config.user, config.password, 22)
            forwardingSession.connect()
            forwardingSession.setPortForwardingL(forwardingPort, config.server, 22)

            val session = createSession(jsch, "localhost", config.user, config.password, forwardingPort)
            session.connect()

            val channel = session.openChannel("exec").asInstanceOf[ChannelExec]
            channel.setCommand("tail -f " + config.files.mkString("", " ", ""))
            channel.setInputStream(null)
            channel.setErrStream(System.err)
            channel.setOutputStream(System.out)
            channel.connect()
          }
          case None => {
            println("Tailing log files on " + config.server)
            val jsch = new JSch()
            val session = createSession(jsch, config.server, config.user, config.password, 22)
            session.connect

            val channel = session.openChannel("exec").asInstanceOf[ChannelExec]
            channel.setCommand("tail -f " + config.files.mkString("", " ", ""))
            channel.setInputStream(null)
            channel.setErrStream(System.err)
            channel.setOutputStream(System.out)
            channel.connect()
          }
        }
      }
    }
  }

  def createSession(jsch: JSch, server: String, user: String, password: String, port: Int): Session = {
    val session = jsch.getSession(user, server, port)
    setConfiguration(session)
    session.setUserInfo(new SimpleUserInfo(password))
    session
  }

  def setConfiguration(session: Session) {
    val properties = new java.util.Properties
    properties.put("StrictHostKeyChecking", "no") // Turn off strict host key checking
    session.setConfig(properties)
  }

}

class SimpleUserInfo(password: String) extends UserInfo {
  override def getPassword(): String = password
  override def promptYesNo(str: String) = true
  override def showMessage(message: String) { println(message) }
  override def getPassphrase(): String = null
  override def promptPassphrase(str: String) = true
  override def promptPassword(str: String) = true
}

class Configuration(id: String) {
  var files = List[String]()
  var server = ""
  var user = "root"
  var password = ""
  var forwardThroughServer: Option[String] = None

  def id(): String = id

  def setForwarding(server: String) {
    forwardThroughServer = Some(server)
  }

  def setServer(server: String) {
    this.server = server
  }

  def addFile(filename: String) {
    files = files ::: List(filename)
  }

  override def toString(): String = {
    "[" + id + "]" + files.mkString("", ",", "")
  }
}