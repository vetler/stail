package stail

class OutputStream extends java.io.OutputStream {
  var enabled = true
  
  def write(b: Int) {
    if (enabled)
      System.out.write(b)
  }

  def disable() {
    enabled = false
  }

  def enable() {
    enabled = true
  }

  def disabled() = {
    !enabled
  }

}
