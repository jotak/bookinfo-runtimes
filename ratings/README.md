## Node.js with metrics on Kubernetes

This is a slightly modified version of this tutorial: https://developers.redhat.com/blog/2018/12/21/monitoring-node-js-applications-on-openshift-with-prometheus/

### Build & deploy

Using `oc`

```bash
docker build -t jotak/nodejs-prom-example:latest .
oc new-project nodejs-prom
oc apply -f <(istioctl kube-inject -f Deployment.yml)
oc apply -f Service.yml
```
