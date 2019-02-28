FROM fabric8/java-centos-openjdk8-jdk:1.5
ENV AB_PROMETHEUS_OFF=true JAVA_APP_DIR=/deployments
EXPOSE 8080
COPY target/*.jar /deployments/
RUN ls -al /deployments/
