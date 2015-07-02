FROM ubuntu:utopic

MAINTAINER kmbulebu@gmail.com

RUN apt-get update
RUN apt-get install openjdk-8-jdk -y

ADD http://apache.mirrors.lucidnetworks.net/maven/maven-3/3.3.3/binaries/apache-maven-3.3.3-bin.tar.gz /tmp/

RUN tar xf /tmp/apache-maven-3.3.3-bin.tar.gz -C /opt/

ADD pom.xml /usr/src/microservice-demo/
ADD src /usr/src/microservice-demo/src

WORKDIR /usr/src/microservice-demo

RUN /opt/apache-maven-3.3.3/bin/mvn verify

CMD /opt/apache-maven-3.3.3/bin/mvn spring-boot:run
