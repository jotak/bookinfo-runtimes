#!/bin/bash

oc new-project bookinfo-runtimes
oc adm policy add-scc-to-user privileged -z default

pushd productpage
    mvn clean package
    mvn fabric8:build -Popenshift
popd

pushd reviews
    mvn clean package dependency:copy-dependencies
    docker build -t jotak/bookinfo-runtimes-reviews:latest .
popd

pushd ratings
    npm install
    docker build -t jotak/bookinfo-runtimes-ratings-v1:latest --build-arg service_version=v1 .
    docker build -t jotak/bookinfo-runtimes-ratings-v2:latest --build-arg service_version=v2 .
popd

pushd details
    mvn clean install dockerfile:build
popd
