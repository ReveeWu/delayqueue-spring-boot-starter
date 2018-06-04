#!/usr/bin/env bash
#上传生产包
echo "uploading biz/target/acrm-delayqueue-$(date +%F).jar";
curl -# -F "file1=@main/target/acrm-delayqueue-$(date +%F).jar" "http://192.168.5.110:8080" > /dev/null
echo "http://deploy.91gfb.com/acrm-delayqueue-$(date +%F).jar"