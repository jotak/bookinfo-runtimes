#!/bin/bash

oc new-project bookinfo-runtimes
oc adm policy add-scc-to-user privileged -z default

pushd productpage
    mvn clean package
    mvn fabric8:build -Popenshift
    oc apply -f <(istioctl kube-inject -f application.yml)
    oc expose svc/productpage
popd

pushd reviews
    mvn clean package dependency:copy-dependencies
    docker build -t jotak/bookinfo-runtimes-reviews:latest .
    oc apply -f <(istioctl kube-inject -f Deployment.yml)
    oc apply -f Service.yml
popd

pushd ratings
    npm install
    docker build -t jotak/bookinfo-runtimes-ratings-v1:latest --build-arg service_version=v1 .
    docker build -t jotak/bookinfo-runtimes-ratings-v2:latest --build-arg service_version=v2 .
    oc apply -f <(istioctl kube-inject -f Deployment-v1.yml)
    oc apply -f <(istioctl kube-inject -f Deployment-v2.yml)
    oc apply -f Service.yml
popd

pushd details
    mvn clean install dockerfile:build
    oc apply -f <(istioctl kube-inject -f Deployment.yml)
    oc apply -f Service.yml
popd
