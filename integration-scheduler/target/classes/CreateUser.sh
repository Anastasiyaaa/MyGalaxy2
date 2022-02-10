#!/bin/bash

SUCCESS=0

if [ $# -eq 2 ]; then
    dirname=$1
    username=$2

    # Check if user already exists.
    grep -q "$username" /etc/passwd
    if [ $? -eq $SUCCESS ]
    then
        if ! [ -d ./"$dirname" ]; then
            set -e
            mkdir ./"$dirname"
        fi
        echo "User $username does already exist."
        exit 0
    fi

    set -e

    sudo useradd -s /bin/bash "$username"

    mkdir ./"$dirname"

    echo "the account is setup"

    exit 0
else
    echo " this script needs 2 arguments you have given $# "
    echo " you have to call the script $0 , dirname and username"
    exit 1
fi