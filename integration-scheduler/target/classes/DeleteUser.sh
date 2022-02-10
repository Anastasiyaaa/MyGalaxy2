#!/bin/bash

SUCCESS=0

if [ $# -eq 2 ]; then
    dirname=$1
    username=$2

    grep -q "$username" /etc/passwd
    if [ $? -eq $SUCCESS ]
    then
        set -e

        if [ -d ./"$dirname" ]; then
            sudo rm -rf ./"$dirname"
        fi
        sudo userdel "$username"
        exit 0
    else
        echo "User $username does not exist."
        exit 0
    fi

else
    echo " this script needs 2 arguments you have given $# "
    echo " you have to call the script $0 , dirname and username"
    exit 1
fi