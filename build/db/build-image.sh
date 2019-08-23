#!/bin/bash
cp -r ../../wecube-core/src/main/resources/database  database
docker build -t wecube-db:dev .
rm -rf database
