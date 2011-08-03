/**
 * Created by IntelliJ IDEA.
 * User: vetler
 * Date: 8/2/11
 * Time: 9:06 PM
 * To change this template use File | Settings | File Templates.
 */

import javax.servlet.http.{HttpServlet,
HttpServletRequest => HSReq, HttpServletResponse => HSResp}
import org.fusesource.scalate._

class SSHLogServlet extends HttpServlet {

  override def doGet(req: HSReq, resp: HSResp) {
    val engine = new TemplateEngine
    val output = engine.layout("index.scaml")
    resp.getWriter.print(output)
  }

}