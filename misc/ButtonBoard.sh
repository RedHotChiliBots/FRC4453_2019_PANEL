#! /bin/bash
export JAVA_HOME=/usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt
JVMARGS="-Dlog4j2.configurationFile=/home/pi/ButtonBoard/log4j2.xml"
sudo $JAVA_HOME/bin/java $JVMARGS -jar /home/pi/ButtonBoard/ButtonBoard.jar
