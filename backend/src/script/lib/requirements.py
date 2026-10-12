#!/usr/bin/env python3
####################################################################################################################################################################################################################################################################
#                                                                                                                                                                                                                                                                  #
#                                                                                                                                                                                                                                                                  #
#        Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)                                                                                                                                                                                              #
#                                                                                                                                                                                                                                                                  #
#        This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.    #
#        This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.           #
#        You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.                                                                                                       #
#                                                                                                                                                                                                                                                                  #
####################################################################################################################################################################################################################################################################
#
from	__future__ import annotations
import	sys, os, argparse, logging, re
from	contextlib import suppress
from	importlib import import_module
from	subprocess import Popen, PIPE
from	typing import NoReturn
from	agn3.definitions import home, syscfg, program
from	agn3.dbconfig import DBConfig
from	agn3.exceptions import error
from	agn3.lock import Lock
from	agn3.log import log
from	agn3.runtime import CLI, Preset
from	agn3.tools import atoi, listsplit
#
logger = logging.getLogger (__name__)
#
class Filter:
	__slots__ = ['all', 'cache_conditions', 'cache_version', 'dbmss', 'select']
	def __init__ (self, all: bool) -> None:
		self.all = all
		self.cache_conditions: dict[str, bool] = {}
		self.cache_version: dict[str, tuple[None | str, tuple[int, ...]]] = {}
		try:
			self.dbmss = set ([_r['dbms'] for _r in DBConfig ().values () if 'dbms' in _r])
		except:
			self.dbmss = set ()
		self.select: None | set[str] = None
		if (select := os.environ.get ('RQ_SELECT')) is not None:
			self.select = set ()
			for token in (_t.strip () for _t in select.split ()):
				if token:
					self.select.add (token)

	pattern_condition = re.compile ('^\\[([^]]+)\\][ \t]*(.*)$')
	def __call__ (self, line: str) -> None | str:
		if not line or line.startswith ('#'):
			return None
		while (mtch := self.pattern_condition.match (line)) is not None:
			(condition, line) = (_m.strip () for _m in mtch.groups ())
			if not self.all:
				try:
					result = self.cache_conditions[condition]
				except KeyError:
					if self.select is not None:
						parts = condition.split ()
						if len (parts) > 1 and len (line.split ()) > 1:
							result = False
						else:
							result = parts[0] in self.select
					else:
						(ctype, cvalue) = condition.split (':', 1)
						result = (method := getattr (self, f'_{ctype}')) is not None and callable (method) and method (cvalue)
					self.cache_conditions[condition] = result
				if not result:
					return None
		if self.select is not None:
			return line.split ()[0]
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

	def _cmd (self, value: str) -> bool:
		for cmd in listsplit (value):
			if os.path.isfile (os.path.join (home, 'scripts', cmd)):
				return True
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
			elif dbms == 'postgres':
				version_string = self._call (['pg_config', '--version'])
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

class Requirements (CLI, Preset):
	__slots__ = ['source', 'frozen', 'target', 'all', 'freeze', 'silent', 'logname', 'validate']
	def setup (self) -> None:
		basepath = os.path.abspath (os.path.dirname (sys.argv[0]))
		self.source = os.path.join (basepath, 'requirements.txt')
		self.frozen = os.path.join (basepath, 'frozen.txt')
		self.target = os.path.join (basepath, '.requirements.txt')
		self.validate = False
		self.preset ({
			'all': 'bool',
			'freeze': 'bool',
			'silent': 'bool',
			'logname': 'str'
		})

	def add_arguments (self, parser: argparse.ArgumentParser) -> None:
		parser.add_argument ('-a', '--all', action = 'store_true', default = self.all, help = 'assume including all optional modules')
		parser.add_argument ('-f', '--freeze', action = 'store_true', default = self.freeze, help = 'use frozen.txt, if available')
		parser.add_argument ('-s', '--silent', action = 'store_true', default = self.silent, help = 'use non zero exit code only on errors')
		parser.add_argument ('-l', '--logname', action = 'store', default = self.logname, help = 'use given logname')
		parser.add_argument ('-v', '--validate', action = 'store_true', default = self.validate, help = 'validate python installation')
	
	def use_arguments (self, args: argparse.Namespace) -> None:
		self.all: bool = args.all
		self.freeze: bool = args.freeze
		self.silent: bool = args.silent
		self.logname: str = args.logname if args.logname else program
		self.validate = args.validate

	def prepare (self) -> None:
		if self.freeze and (syscfg.get ('os.id') == 'centos' or 'rhel' in syscfg.lget ('os.id_like', [])) and atoi (syscfg.get ('os.version_id', '0').split ('.')[0]) < 8:
			logger.debug ('{osid}: OS too old for using freezed file'.format (osid = syscfg.get ('os.pretty_name', '-')))
			self.freeze = False

	def executor (self) -> bool:
		if self.validate:
			return self._validate ()
		#
		with Lock (lazy = True, silent = True) as lock, log (self.logname):
			if lock is not None:
				self._requirements ()
			else:
				logger.debug ('currently locked')
		return True

	def _validate (self) -> bool:
		filename = self.target if os.path.isfile (self.target) else (self.frozen if self.freeze and os.path.isfile (self.frozen) else self.source)
		#
		def complain (message: str) -> bool:
			output = f'{filename}: {message}'
			logger.error (output)
			print (f'{output}\n', file = sys.stderr, flush = True)
			return False
		#
		if not os.path.isfile (filename):
			return complain ('missing file')
		try:
			with open (filename) as fd:
				content = fd.read ()
		except OSError as e:
			return complain (f'failed to read file: {e}')
		#
		missing: list[str] = []
		filter = Filter (self.all)
		for line in (_l.strip () for _l in content.split ('\n')):
			if not line or line.startswith ('#'):
				continue
			if (output := filter (line)) is None:
				continue
			module = self._module_name (output)
			try:
				import_module ({
					'dkimpy':	'dkim',
					'dnspython':	'dns',
					'py3dns':	'DNS',
					'pyspf':	'spf',
					'PyNaCl':	'nacl',
				}.get (module, module.replace ('-', '_')))
			except Exception as e:
				missing.append (f'{module} ({e})')
		if missing:
			return complain ('missing modules: {modules}'.format (
				modules = ', '.join (sorted (missing))
			))
		#
		return True

	def _requirements (self) -> NoReturn:
		rc = 10
		filename = self.frozen if self.freeze and os.path.isfile (self.frozen) else self.source
		if os.path.isfile (filename):
			try:
				original: None | str = None
				if os.path.isfile (self.target):
					with open (self.target) as fdi:
						original = fdi.read ()
				logger.debug (f'{filename}: use file to create target {self.target}')
				with open (filename) as fdi:
					filter = Filter (self.all)
					collect: list[str] = []
					seen: dict[str, str] = {}
					for (lineno, line) in enumerate (fdi, start = 1):
						if (output := filter (line.strip ())):
							key = self._module_name (output)
							if key not in seen:
								collect.append (f'{output}\n')
								seen[key] = output
							elif seen[key] != output:
								raise error (f'{filename}:{lineno}: {key}: duplicate line with different content found: "{seen[key]}" vs. "{output}"')
					current = ''.join (sorted (collect, key = lambda s: s.lower ()))
				if original is None or original != current:
					with open (self.target, 'w') as fdo:
						fdo.write (current)
				else:
					rc = 11
			except error as e:
				logger.error (str (e))
				rc = 13
			except Exception as e:
				logger.exception (f'failed module detection: {e}')
				with suppress (OSError):
					os.unlink (self.target)
				rc = 14
		else:
			logger.error (f'{filename}: source file not found')
			rc = 12
		if self.silent:
			sys.exit (0 if rc in (10, 11) else 1)
		sys.exit (rc)

	_split_pattern = re.compile ('\\s*[<>=]+\\s*')
	def _module_name (self, line: str) -> str:
		return (self._split_pattern.split (line)[0]).strip ()

if __name__ == '__main__':
	Requirements.main (silent = True)
