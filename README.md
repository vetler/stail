STAIL
=====


Overiew
-------

Stail is a tool for tailing files over SSH. It can connect to a server
directly, or by first going through another server using SSH port
forwarding, and it will then call the `tail` command and echo the
output.

At the moment, it's not such a big improvement over just doing it
yourself if you have SSH installed, but the idea is that it will be
expanded in the future with pattern matching and filtering. Other
suggestions are welcome, please file [an
issue](https://bitbucket.org/vetler/stail/issues/new).

Usage
-----

    Usage: stail <OPTIONS> username@host:/path/to/file

    Options:
        -via HOSTSPEC     Tail file on remote host by using SSH forwarding through the specified host.
                          Local port to be used for port forwarding can also be specified.

                          Example: user@example.com:1001

To use **stail** like this, you first have to build it and set up a
wrapper script. A sample wrapper script can be found [in the
source](https://bitbucket.org/vetler/stail/src/c77f3a5b7cec/src/main/shell/stail). To
use this script, first [download
it](https://bitbucket.org/vetler/stail/raw/c77f3a5b7cec/src/main/shell/stail),
download the [pre-built jar file](https://bitbucket.org/vetler/stail/downloads/stail-assembly-0.1.jar) (or build it yourself) and set the
environment variable `$STAIL_JAR` to point to it.

On Windows you can set the environment variable `%STAIL_JAR`, and use
the [batch
file](https://bitbucket.org/vetler/stail/raw/214b50f2a08c/src/main/shell/stail.bat).

If you are building **stail** yourself, you can use `sbt assembly` to
build a self-contained jar file.

### Examples

#### Connecting directly to a server

    stail user@example.com:/var/log/apache2/access.log

#### Connecting via another server

    stail -via anotheruser@external.example.com:9999 user@internal.example.com:/var/log/apache2/access.log

#### Examples without the stail script

    java -jar stail-assembly-0.1.jar user@example.com:/var/log/apache2/access.log
    
    java -jar stail-assembly-0.1.jar -via anotheruser@external.example.com:9999 user@internal.example.com:/var/log/apache2/access.log


### More advanced usage

If you are impatient and can't wait for new features, feel free to
fork and implement more advanced features by extending
`outputstream.scala`.


Troubleshooting
---------------

**Q:** HALP! I get this error message:

    Can't load IA 32-bit .dll on a AMD 64-bit platform
        at java.lang.ClassLoader$NativeLibrary.load(Native Method)
        at java.lang.ClassLoader.loadLibrary0(Unknown Source)
        at java.lang.ClassLoader.loadLibrary(Unknown Source)
        at java.lang.Runtime.load0(Unknown Source)
        at java.lang.System.load(Unknown Source)
        at jline.WindowsTerminal.loadLibrary(WindowsTerminal.java:215)
        at jline.WindowsTerminal.initializeTerminal(WindowsTerminal.java:144)
        at jline.Terminal.setupTerminal(Terminal.java:69)
        at jline.Terminal.getTerminal(Terminal.java:26)
        at jline.ConsoleReader.<init>(ConsoleReader.java:151)
        at jline.ConsoleReader.<init>(ConsoleReader.java:146)
        at jline.ConsoleReader.<init>(ConsoleReader.java:135)
        at STail$.readPassword(stail.scala:92)
        at STail$.main(stail.scala:49)
        at STail.main(stail.scala)

**A:** Try running **stail** with `-Djline.terminal=jline.UnsupportedTerminal`, i.e.:

    java -Djline.terminal=jline.UnsupportedTerminal -jar stail-assembly-0.1.jar user@example.com:/var/log/apache2/access.log

You might not see the password prompt, but you should still be able to
use the basic functionality while we try to find a more permanent
solution.

