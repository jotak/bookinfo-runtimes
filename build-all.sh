#!/bin/bash

pushd productpage
    mvn clean package fabric8:build
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
    mvn clean package dockerfile:build
popd
