#!/bin/bash

set -e

if [ $# -eq 2 ]; then
    dirname=$1
    username=$2

    sudo chown -R "$username" ./"$dirname"
    sudo chmod 770 ./"$dirname" -R
    sudo chmod 440 ./"$dirname/ihub_parameters.json"

else
    echo  " this script needs 2 arguments you have given $# "
    echo  " you have to call the script $0 , dirname and username"
    exit 1
fi