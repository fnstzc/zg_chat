FROM openjdk:8u181-jre-slim

EXPOSE 9000

ENV J_OPTS ""

COPY Dockercmd.sh /bin/
RUN ["chmod", "+x", "/bin/Dockercmd.sh"]

COPY target/zgchat-1.0.0-SNAPSHOT.jar /bin/zgchat/

CMD [ "/bin/bash", "-c", "/bin/Dockercmd.sh" ]
