#!/bin/bash

oc new-project bookinfo-runtimes
oc adm policy add-scc-to-user privileged -z default

pushd productpage
    oc apply -f <(istioctl kube-inject -f application.yml)
    oc expose svc/productpage
popd

pushd reviews
    oc apply -f <(istioctl kube-inject -f Deployment.yml)
    oc apply -f Service.yml
popd

pushd ratings
    oc apply -f <(istioctl kube-inject -f Deployment-v1.yml)
    oc apply -f <(istioctl kube-inject -f Deployment-v2.yml)
    oc apply -f Service.yml
popd

pushd details
    oc apply -f <(istioctl kube-inject -f Deployment.yml)
    oc apply -f Service.yml
popd
