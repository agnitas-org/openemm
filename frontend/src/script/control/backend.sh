#!/bin/sh
#	-*- sh -*-
#
exec $HOME/bin/service.sh -C '#
[once(_stdstart)]
#
[mailout(_std)]
[update(_std)]
[trigger(_std)]
[generate(_std)]
[pickdist(_std)]
[slrtscn(_std)]
[direct-path(_std)]
[bav-update(_std)]
[bavd(_std)]
[bav(_std)]
' -i openemm "$@"
