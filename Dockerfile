FROM navikt/java:17
USER root
USER apprunner
COPY build/libs/hm-grunndata-media-all.jar ./app.jar
