MAIN_CLASS=com.paduvi.App
MAIN_JAR=HealthCheckSystem.jar.original
lib_project=dependency
JAVA=$JAVA_HOME/bin/java
JAVA_HEAP_MAX=-Xmx2g
CLASS_PATH=$JAVA_HOME/lib/*:$lib_project/*:$MAIN_JAR
until java $JAVA_HEAP_MAX -classpath $CLASS_PATH $MAIN_CLASS $MAIN_JAR $1; do
	echo "Application crashed with exit code $?. Respawning... ">&2
	sleep 5
done