#!/usr/bin/env bash
#开发环境部署脚本
#cd web/target
scp main/target/acrm-delayqueue-$(date +%F).jar root@172.16.1.27:/dashu/application/bak
ssh  root@172.16.1.27<< EOF
    cd /dashu/application;
    rm -rf acrm-delayqueue*.jar;
    cp bak/acrm-delayqueue-$(date +%F).jar acrm-delayqueue-$(date +%F).jar
    bash restart-delayqueue.sh;
    exit;
EOF
