#!/bin/sh

export LANG=en_US.UTF-8
./jdk/bin/java -Xms512m -Xmx2G -Dlogback.configurationFile=./logback.xml -cp ".:lib/*" -jar msdata-converter-@version@.jar "$@"