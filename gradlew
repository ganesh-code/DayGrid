#!/bin/sh
#
# Copyright © 2015-2021 the original authors.
#
# Gradle wrapper script for Unix-like systems.

##############################################################################
# Configuration
##############################################################################

APP_NAME="DayGrid"
APP_BASE_NAME=$(basename "$0")

SCRIPT_LOCATION="$(cd "$(dirname "$0")" && pwd)"
GRADLE_OPTS="${GRADLE_OPTS:-}"

# Attempt to find JAVA_HOME
if [ -n "$JAVA_HOME" ] ; then
    JAVA_CMD="$JAVA_HOME/bin/java"
    if [ ! -x "$JAVA_CMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME"
    fi
else
    JAVA_CMD="java"
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command found."
fi

# Setup distribution download
GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"
GRADLE_WRAPPER_JAR="$SCRIPT_LOCATION/gradle/wrapper/gradle-wrapper.jar"
GRADLE_WRAPPER_PROPS="$SCRIPT_LOCATION/gradle/wrapper/gradle-wrapper.properties"

exec "$JAVA_CMD" \
    $GRADLE_OPTS \
    "-Dorg.gradle.appname=$APP_BASE_NAME" \
    -classpath "$GRADLE_WRAPPER_JAR" \
    org.gradle.wrapper.GradleWrapperMain \
    "$@"
