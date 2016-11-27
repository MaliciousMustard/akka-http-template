#!/usr/bin/env bash

. $(dirname $0)/functions

push_ecr_image
deploy_cluster
