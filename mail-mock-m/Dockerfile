FROM amazoncorretto:17-alpine
WORKDIR /home/app
COPY docker/main/layers/libs /home/app/libs
COPY docker/main/layers/classes /home/app/classes
COPY docker/main/layers/resources /home/app/resources
COPY docker/main/layers/r /home/app/r
COPY docker/main/layers/application.jar /home/app/application.jar
EXPOSE 8080 25 465 110 995 143 993
ENTRYPOINT ["java", "-jar", "/home/app/application.jar"]
