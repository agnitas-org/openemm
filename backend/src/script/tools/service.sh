#!/bin/sh
#
. $HOME/scripts/config.sh
#
py3select $HOME/scripts/service3.py $HOME/scripts/service.py
exec $command "$@"
