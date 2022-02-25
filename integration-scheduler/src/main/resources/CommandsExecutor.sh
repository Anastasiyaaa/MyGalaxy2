#!/bin/bash

set -e

if [ $# -gt 1 ]; then
    sudo -u "$@"
else
    echo " this script needs 2 or more arguments you have given $# "
    echo ""
    exit 1
fi

