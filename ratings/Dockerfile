FROM nodeshift/centos7-s2i-nodejs
COPY . /app/

ARG service_version
ENV SERVICE_VERSION ${service_version:-v1}

EXPOSE 9080
CMD ["node", "/app/ratings.js", "9080"]
