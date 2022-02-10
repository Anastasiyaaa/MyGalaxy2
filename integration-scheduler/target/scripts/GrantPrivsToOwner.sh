#!/bin/bash

set -e

if [ $# -eq 1 ]; then

    username=$(whoami)
    dirname=$1

    sudo chown -R "$username" ./"$dirname"
    sudo chmod 770 ./"$dirname" -R
fi