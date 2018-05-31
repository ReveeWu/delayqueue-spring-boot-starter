#!/usr/bin/env bash
#开发环境部署脚本
#cd web/target
scp main/target/delayqueue-$(date +%F).jar root@172.16.1.27:/dashu/application/bak
ssh  root@172.16.1.27<< EOF
    cd /dashu/application;
    rm -rf delayqueue*.jar;
    cp bak/delayqueue-$(date +%F).jar delayqueue-$(date +%F).jar
    bash restart-delayqueue.sh;
    exit;
EOF
