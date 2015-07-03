#
# Based on Oracle Java 8 Dockerfile
# https://github.com/dockerfile/java
# https://github.com/dockerfile/java/tree/master/oracle-java8
#

# Image name: ubuntu_trusty_java8

# Pull base image, Ubuntu Vivid Vervet
FROM ubuntu:14.04
MAINTAINER Alejandro Revilla "apr@jpos.org"
# RUN groupadd -r postgres && useradd -r -g postgres postgres

RUN \
   echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" > /etc/apt/sources.list.d/webupd8team-java.list && \
   echo "deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" >> /etc/apt/sources.list.d/webupd8team-java.list && \
   apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys EEA14886 && \
   apt-get update && \
   echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
   apt-get install -y oracle-java8-installer && \
   apt-get dist-upgrade -y && \
   rm -rf /var/lib/apt/lists/* && \
   rm -rf /var/cache/oracle-jdk8-installer && \
   apt-get clean

# Define working directory.
# WORKDIR /data

# Define commonly used JAVA_HOME variable
ENV JAVA_HOME /usr/lib/jvm/java-8-oracle
ENV LANG C.UTF-8

# USER jpos
# EXPOSE 8583
# Define default command.
CMD ["bash"]

