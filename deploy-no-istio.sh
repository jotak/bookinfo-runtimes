#!/bin/bash

oc new-project bookinfo-runtimes
oc project bookinfo-runtimes
oc adm policy add-scc-to-user privileged -z default

pushd productpage
    if [[ "$1" = "--s2i" ]]; then
        oc apply -f DeploymentConfig.yml
    else
        oc apply -f Deployment.yml
    fi
    oc apply -f Service.yml
    oc expose svc/productpage
popd

pushd reviews
    oc apply -f Deployment.yml
    oc apply -f Service.yml
popd

pushd ratings
    oc apply -f Deployment-v1.yml
    oc apply -f Deployment-v2.yml
    oc apply -f Service.yml
popd

pushd details
    oc apply -f Deployment.yml
    oc apply -f Service.yml
popd
