// Good, old java imports
import java.io.File
import java.io.Console

// Look ma, Scala!
import scala.io.Source
import scala.tools.nsc.interpreter.ILoop.break
import scala.tools.nsc.interpreter.NamedParam

// Java library for SSH
import com.jcraft.jsch._

object STail {
  def usage(): String = {
    println("""
Usage: stail <OPTIONS> username@host:/path/to/file

Options:
    -via HOSTSPEC     Tail file on remote host by using SSH forwarding through the specified host.
                      Local port to be used for port forwarding can also be specified.

                      Example: user@example.com:1001:/path/to/file
""")
    sys.exit(1)
  }

  // A couple of predefined values
  val defaultForwardingPort = "10001"
  val defaultTimeout = 10000
  val jsch = new JSch()

  def main(args: Array[String]) {
    // Argument list length should be 1 or 3
    if (isInvalidArgsList(args)) usage()

    // Hostspec is the target server configuration
    var hostSpec = args last
    var fwHostSpec = forwardingHostSpec(args)
    val (username, origHost, origPort, path) = readHostSpec(hostSpec)

    // Set up forwarding if applicable
    val (host, port) = fwHostSpec match {
      case Some(x) =>
	forwardingSetup(origHost, x)
      case None =>
	(origHost, origPort match {
	  case Some(x) => Some(x)
	  case None => Some("22")
	})
    }

    // Create the session for tailing, connecting either directly to
    // the server, or to localhost if we're using forwarding
    val password = readPassword(username, host).toString
    val session = createSession(jsch, host, username, password, port.get.toInt)

    println("Connected to "+ origHost + (if (origHost == host) "" else " (via local port "+ port.get +")"))

    val channel = session.openChannel("exec").asInstanceOf[ChannelExec]
    channel.setCommand("tail -f "+ path.get)
    channel.setInputStream(null)
    channel.setErrStream(System.err)
    channel.setOutputStream(System.out)
    channel.connect(defaultTimeout)
  }

  def forwardingHostSpec(args: Array[String]) = {
    // 3 arguments means the user wrote "-via HOSTSPEC", unless he's
    // done something wrong
    args.length match {
      case 3 => Some(args(1))
      case _ => None
    }
  }

  def isInvalidArgsList(args: Array[String]) = {
    args.length match {
      case 1 | 3 => false
      case _     => true
    }
  }

  def forwardingSetup(remoteHost: String, hostSpec: String) = {
    val (username, host, port, path) = readHostSpec(hostSpec)
    val forwardingPort = port match {
      case Some(x) => x
      case None => defaultForwardingPort
    }

    val forwardingSession = createSession(jsch, host, username, readPassword(username, host).toString, 22)
    forwardingSession.setPortForwardingL(forwardingPort.toInt, remoteHost, 22)
    println("Forwarding through "+ host + " on local port "+ forwardingPort)
    ("localhost", Some(forwardingPort))
  }

  def readPassword(username: String, host: String) = {
    print("Password for "+ username +"@"+ host +": ")
    new jline.ConsoleReader().readLine('*')
  }
  
  val RHostSpec = """(.*)@([^:]+)(:\d+)?(:.+)?""".r
  def readHostSpec(hostSpec: String): (String, String, Option[String], Option[String]) = {
    val RHostSpec(username, host, port, path) = hostSpec
    (username, host, if (port == null) None else Some(port tail), if (path == null) None else Some(path tail))
  }

  def createSession(jsch: JSch, server: String, user: String, password: String, port: Int): Session = {
    val session = jsch.getSession(user, server, port)

    val properties = new java.util.Properties
    properties.put("StrictHostKeyChecking", "no") // Turn off strict host key checking

    session.setConfig(properties)
    session.setUserInfo(new SimpleUserInfo(password))
    session.connect(defaultTimeout)
    session
  }

  // For JSch
  class SimpleUserInfo(password: String) extends UserInfo {
    override def getPassword(): String = password
    override def promptYesNo(str: String) = true
    override def showMessage(message: String) { println(message) }
    override def getPassphrase(): String = null
    override def promptPassphrase(str: String) = true
    override def promptPassword(str: String) = true
  }
}

