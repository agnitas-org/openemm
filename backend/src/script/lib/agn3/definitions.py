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
import	sys, os
from	datetime import datetime
from	.exceptions import error
from	.systemconfig import Systemconfig
#
__all__ = [
	'epoch', 'syscfg', 'licence',
	'fqdn', 'host', 'unique',
	'base', 'user', 'home',
	'program', 'dbid_default',
	'version', 'ams'
]
#
epoch = datetime.fromtimestamp (0)
syscfg = Systemconfig ()
licence = syscfg.iget ('licence', -1)
if licence == -1:
	raise error ('no licence id found')
#
fqdn = syscfg._fqdn
host = syscfg._host
#
unique: str
if (_unique := syscfg.get ('unique')) is None:
	import	hashlib
	from	string import ascii_lowercase as letters

	digest = hashlib.new ('md5')
	digest.update (fqdn.encode ('UTF-8'))
	unique_nr = sum (digest.digest ())
	unique = letters[(unique_nr >> 6) % len (letters)] + letters[unique_nr % len (letters)]
else:
	unique = _unique
#
base = syscfg._home
user = syscfg._user
home = syscfg._home
#
if len (sys.argv) > 0 and sys.argv[0]:
	(_basename, _extension) = os.path.splitext (os.path.basename (sys.argv[0]))
	program = _basename if _extension.lower ().startswith ('.py') else _basename + _extension
else:
	program = 'unset'
#
dbid_default = syscfg.get ('dbid', 'emm')
#
version = os.environ.get ('VERSION', 'unknown')
ams = False
