#!/bin/sh
#	-*- sh -*-
#
exec $HOME/bin/service.sh -C '#
[once(_stdstart)]
#
[mlprepare]
cmd-start = $HOME/bin/mlcontrol.sh setup
cmd-stop = $HOME/bin/mlcontrol.sh teardown
[mailout(_std)]
[update(_std)]
[trigger(_std)]
[generate(_std)]
[mta(_std)]
[pickdist(_std)]
[slrtscn(_std)]
[direct-path(_std)]
[bav-update(_std)]
[bavd(_std)]
[bav(_std)]
[mlcontrol(_std)]
' -i openemm "$@"
