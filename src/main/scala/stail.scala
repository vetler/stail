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
  val defaultTimeout = 10000

  def usage() {
    println("""
Usage: stail <OPTIONS> username@host:/path/to/file

Options:
    -via HOSTSPEC     Tail file on remote host by using SSH forwarding through the specified host.
                      Local port to be used for port forwarding can also be specified.

                      Example: user@example.com:1001:/path/to/file
""")

    exit(1)
  }

  def main(args: Array[String]) {
    args.length match {
      case 1 => Nil
      case 3 => setUpForwarding(args)
      case _ => usage
    }

    val (username, host, port, path) = args.length match {
      case 1 => readHostSpec(args(0))
      case 3 => readHostSpec(args(2))
      case _ => dieWithError("Unrecognized number of arguments: "+ args.length)
    }

//         config.forwardThroughServer match {
//           case Some(forwardedThroughServer) => {
//             println("Tailing log files on " + config.server + ", forwarded through " + forwardedThroughServer)
//             val jsch = new JSch()
//             val forwardingSession = createSession(jsch, forwardedThroughServer, config.user, config.password, 22)
//             forwardingSession.connect(defaultTimeout)
//             forwardingSession.setPortForwardingL(forwardingPort, config.server, 22)

//             val session = createSession(jsch, "localhost", config.user, config.password, forwardingPort)
//             session.connect(defaultTimeout)

//             val channel = session.openChannel("exec").asInstanceOf[ChannelExec]
//             channel.setCommand("tail -f " + config.files.mkString("", " ", ""))
//             channel.setInputStream(null)
//             channel.setErrStream(System.err)
//             channel.setOutputStream(System.out)
//             channel.connect(defaultTimeout)
//           }

    val password = readPassword().toString

    println("Tailing log files on " + host)
    val jsch = new JSch()
    val session = createSession(jsch, host, username, password, 22)
    session.connect(defaultTimeout)
    
    val channel = session.openChannel("exec").asInstanceOf[ChannelExec]
    channel.setCommand("tail -f "+ path)
    channel.setInputStream(null)
    channel.setErrStream(System.err)
    channel.setOutputStream(System.out)
    channel.connect(defaultTimeout)
  }

  def readPassword() = {
    print("Password: ")
    new jline.ConsoleReader().readLine('*')
  }
  
  def dieWithError(error: String): (String, String, Option[String], String) = {
    println(error)
    exit(1)
  }

  object Username {
    def unapply(f: String) = {
      f.substring(0, f.indexOf('@'))
    }
  }

  object Host {
    def unapply(f: String) = {
      //break(List(NamedParam("f", f)))
      f.substring(f.indexOf('@') + 1, f.indexOf(':'))
    }
  }

  object Port {
    def unapply(f: String): Option[String] = {
      var idx = f.indexOf(':')
      var nextIdx = f.indexOf(':', idx+1)
      if (nextIdx == -1 || idx == nextIdx)
	None
      else
	Some(f.substring(idx+1, nextIdx))
    }
  }

  object Path {
    def unapply(f: String) = {
      f.substring(f.lastIndexOf(':') + 1)
    }
  }

  def readHostSpec(hostSpec: String): (String, String, Option[String], String) = {
    // username@host(:port):/path/to/file
    val username = Username.unapply(hostSpec)
    val host = Host.unapply(hostSpec)
    val port = Port.unapply(hostSpec)
    val path = Path.unapply(hostSpec)

    (username, host, port, path)
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

  def setUpForwarding(args: Array[String]) {}
}

class SimpleUserInfo(password: String) extends UserInfo {
  override def getPassword(): String = password
  override def promptYesNo(str: String) = true
  override def showMessage(message: String) { println(message) }
  override def getPassphrase(): String = null
  override def promptPassphrase(str: String) = true
  override def promptPassword(str: String) = true
}

