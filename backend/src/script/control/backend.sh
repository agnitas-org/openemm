#!/bin/sh
####################################################################################################################################################################################################################################################################
#                                                                                                                                                                                                                                                                  #
#                                                                                                                                                                                                                                                                  #
#        Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)                                                                                                                                                                                                   #
#                                                                                                                                                                                                                                                                  #
#        This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.    #
#        This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.           #
#        You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.                                                                                                            #
#                                                                                                                                                                                                                                                                  #
####################################################################################################################################################################################################################################################################
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
