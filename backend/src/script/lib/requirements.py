#!/usr/bin/env python3
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
from	__future__ import annotations
import	sys, os, re
from	contextlib import suppress
from	subprocess import Popen, PIPE
from	agn3.exceptions import error
from	agn3.lock import Lock
#
class Filter:
	__slots__ = ['cache_conditions', 'cache_version', 'dbmss']
	def __init__ (self) -> None:
		self.cache_conditions: dict[str, bool] = {}
		self.cache_version: dict[str, tuple[None | str, tuple[int, ...]]] = {}
		self.dbmss = {'mariadb'}
	
	pattern_condition = re.compile ('^\\[([^]]+)\\][ \t]*(.*)$')
	def __call__ (self, line: str) -> None | str:
		if not line or line.startswith ('#'):
			return None
		while (mtch := self.pattern_condition.match (line)) is not None:
			(condition, line) = (_m.strip () for _m in mtch.groups ())
			(ctype, cvalue) = condition.split (':', 1)
			try:
				result = self.cache_conditions[condition]
			except KeyError:
				result = self.cache_conditions[condition] = (method := getattr (self, f'_{ctype}')) is not None and callable (method) and method (cvalue)
			if not result:
				return None
		return line
	
	def _db (self, value: str) -> bool:
		expression: None | str
		try:
			(dbms, expression) = (_v.strip () for _v in value.split (None, 1))
		except ValueError:
			(dbms, expression) = (value, None)
		if dbms in self.dbmss:
			if expression is None:
				return True
			(version_string, version) = self._find_version (dbms)
			if version_string is not None:
				return bool (eval (expression, {'version_string': version_string, 'version': version}))
			raise error (f'** {dbms}: failed to determinate version, possibly missing mandatory development packages')
		return False
	pattern_version = re.compile ('([0-9]+(\\.[0-9]+)*)')
	def _find_version (self, dbms: str) -> tuple[None | str, tuple[int, ...]]:
		try:
			return self.cache_version[dbms]
		except KeyError:
			version_string: None | str = None
			version: tuple[int, ...] = ()
			if dbms == 'mariadb':
				version_string = self._call (['mariadb-config', '--version'])
				if version_string is None:
					version_string = self._call (['mariadb_config', '--version'])
			if version_string and (mtch := self.pattern_version.search (version_string)) is not None and (plain := mtch.group (1)):
				version = tuple (int (_v) for _v in plain.split ('.'))
			self.cache_version[dbms] = (version_string, version)
			return (version_string, version)
			
	def _call (self, cmd: list[str]) -> None | str:
		with suppress (BaseException):
			pp = Popen (cmd, stdin = PIPE, stdout = PIPE, stderr = PIPE, text = True, errors = 'replace')
			(out, err) = pp.communicate (None)
			if pp.returncode == 0:
				return out.strip ()
		return None
		
def requirements (basepath: str) -> int:
	rc = 0
	source = os.path.join (basepath, 'requirements.txt')
	target = os.path.join (basepath, '.requirements.txt')
	if os.path.isfile (source):
		try:
			original: None | str = None
			if os.path.isfile (target):
				with open (target) as fdi:
					original = fdi.read ()
			with open (source) as fdi:
				filter = Filter ()
				collect: list[str] = []
				for line in fdi:
					if (output := filter (line.strip ())):
						collect.append (f'{output}\n')
				current = ''.join (collect)
			if original is None or original != current:
				with open (target, 'w') as fdo:
					fdo.write (current)
			else:
				rc = 3
		except Exception as e:
			with suppress (OSError):
				os.unlink (target)
			rc = 1
			print (f'** failed determinating proper modules: {e}')
	else:
		rc = 2
	return rc

def main () -> None:
	rc = 0
	with Lock (lazy = True) as lock:
		if lock is not None:
			rc = requirements (os.path.abspath (os.path.dirname (sys.argv[0])))
	sys.exit (rc)
	
if __name__ == '__main__':
	main ()
