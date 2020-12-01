####################################################################################################################################################################################################################################################################
#                                                                                                                                                                                                                                                                  #
#                                                                                                                                                                                                                                                                  #
#        Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)                                                                                                                                                                                                   #
#                                                                                                                                                                                                                                                                  #
#        This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.    #
#        This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.           #
#        You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.                                                                                                            #
#                                                                                                                                                                                                                                                                  #
####################################################################################################################################################################################################################################################################
#
import	sys, os, platform, pwd
from	.exceptions import error
from	.systemconfig import Systemconfig
#
__all__ = ['syscfg', 'licence', 'system', 'fqdn', 'host', 'base', 'user', 'home', 'program', 'version']
#
syscfg = Systemconfig ()
licence = syscfg.get_int ('licence', -1)
if licence == -1:
	raise error ('no licence id found')
#
system = platform.system ().lower ()
fqdn = platform.node ().lower ()
host = fqdn.split ('.', 1)[0]
#
base = os.environ.get ('HOME', '.')
try:
	pw = pwd.getpwuid (os.getuid ())
	user = pw.pw_name
	home = pw.pw_dir
except KeyError:
	user = os.environ.get ('USER', '#{uid}'.format (uid = os.getuid ()))
	home = os.environ.get ('HOME', '.')
syscfg.user = user
#
if len (sys.argv) > 0 and sys.argv[0]:
	(_basename, _extension) = os.path.splitext (os.path.basename (sys.argv[0]))
	program = _basename if _extension.lower ().startswith ('.py') else _basename + _extension
else:
	program = 'unset'
#
version = os.environ.get ('VERSION', 'unknown')
