FROM gcr.io/distroless/java17-debian12:nonroot
ENV TZ="Europe/Oslo"
EXPOSE 8080
COPY build/libs/hm-grunndata-media-all.jar ./app.jar
CMD ["-jar", "/app.jar"]