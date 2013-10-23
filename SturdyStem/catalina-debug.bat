SET CATALINA_OPTS=-server -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8288,suspend=n,server=y
call catalina start
jdb -connect com.sun.jdi.SocketAttach:port=8288,hostname=127.0.0.1 -sourcepath ./src