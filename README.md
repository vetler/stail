STAIL
=====


Overiew
-------

Stail is a tool for tailing files over SSH. It can connect to a server
directly, or by first going through another server using SSH port
forwarding.

At the moment, it's not such a big improvement over just doing it
yourself, but the idea is that it will be expanded in the future with
pattern matching and filtering. Other suggestions are welcome, please
file <a href="https://bitbucket.org/vetler/stail/issues/new">an
issue</a>.


Usage
-----

    Usage: stail <OPTIONS> username@host:/path/to/file

    Options:
        -via HOSTSPEC     Tail file on remote host by using SSH forwarding through the specified host.
                          Local port to be used for port forwarding can also be specified.

                          Example: user@example.com:1001:/path/to/file

