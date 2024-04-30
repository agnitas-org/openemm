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
. $HOME/scripts/config.sh
#
exec $HOME/bin/service.sh -C '#
[once(_stdstart)]
#
[crontab]
cmd-start = $HOME/bin/crontab.sh $id
[distribute(_std)]
active = mailer
[smsd(_std)]
active = mailer, merger
[slrtscn(_std)]
active = mailer, merger
[statd(_std)]
active = mailer, mailloop
[bav-relay(_std)]
active = mailer, !mailloop
[direct-path(_std)]
active = mailer
plugin = {
def active (name):
	import	os
	
	if service_active ("console") or service_active ("rdir"):
		path = os.path.join ("$HOME", "DIRECT")
		return os.path.isdir (path)
	return False
}
[bav-update(_std)]
active = mailloop
[bavd(_std)]
active = mailloop
[bav(_std)]
active = mailloop
[npickup(_std)]
active = merger
[frdir(_std)]
active = merger
[enqueue(_std)]
active = merger
[mailout(_std)]
active = merger
[fetch(_std)]
active = merger
[trigger(_std)]
active = merger
[generate(_std)]
active = merger
[amsd(_std)]
syscfg = ams
active = merger
plugin = {
def active (name):
	import	os
	from	agn3.definitions import base
	
	if name == "amsd":
		return os.access (os.path.join (base, "bin", f"{name}.sh"), os.X_OK)
	return True
}
[update(_std)]
active = merger
[pegid(_std)]
active = pegi
' -i emm "$@"
