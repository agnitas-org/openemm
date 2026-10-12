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
import	sys, os, argparse, re, shutil
from	contextlib import suppress
from	tempfile import mkstemp
from	types import TracebackType
from	typing import Callable, Self
from	typing import Match, NamedTuple
from	agn3.db import DB
from	agn3.definitions import base, program
from	agn3.io import which
from	agn3.exceptions import error
from	agn3.runtime import CLI
from	agn3.stream import Stream
from	agn3.tools import call
#
def show (message: str) -> None:
	sys.stdout.write (message)
	sys.stdout.flush ()

def ask (prompt: str, options: list[str], default: None | str = None) -> str:
	fullprompt = '{prompt} ({options}){default}: '.format (
		prompt = prompt,
		options = Stream (options).join (', '),
		default = f' [{default}]' if default else ''
	)
	while True:
		show (fullprompt)
		answer = sys.stdin.readline ().strip ().lower ()
		if not answer:
			if default is not None:
				return default
		else:
			for option in options:
				if option.startswith (answer):
					return option
			show (f'unknown option "{answer}" selected\n')

class Editor:
	editor: None | str = None
	@classmethod
	def edit (cls, path: str) -> None:
		if cls.editor is None:
			for env in 'VISUAL', 'EDITOR', 'EDIT':
				if (editor := os.environ.get (env)) is not None:
					break
			else:
				for edit in 'vim', 'vi', 'emacs':
					if (editor := which (edit)) is not None:
						break
			if editor is not None:
				cls.editor = editor
		if cls.editor is not None:
			call ([cls.editor, path])
		else:
			raise error ('no editor found')

class DBInstall:
	__slots__ = ['dryrun', 'sqlbase', 'dbid', 'db', 'dbms', 'dbmsbase', 'tablespaces']
	def __init__ (self, dryrun: bool = False, sqlbase: None | str = None, dbid: None | str = None) -> None:
		self.dryrun = dryrun
		self.sqlbase: str = sqlbase if isinstance (sqlbase, str) else os.path.join (base, 'webapps', 'emm', 'WEB-INF', 'sql')
		self.dbid = dbid
		self.db: DB
		self.dbms: str
		self.tablespaces: bool
	
	def __enter__ (self) -> Self:
		with suppress (AttributeError):
			self.db.sync (False)
			self.db.close ()
		if not os.path.isdir (self.sqlbase):
			raise error (f'{self.sqlbase}: missing sql base directory')
		self.db = DB (dbid = self.dbid)
		if not self.db.isopen ():
			last_error = self.db.last_error ()
			del self.db
			raise error (last_error)
		if (dbms := self.db.dbms) is not None:
			self.dbms = dbms
		else:
			raise error (f'{self.dbid}: missing database type via "dbms" configuration parameter in "dbcfg"')
		self.dbmsbase = os.path.join (self.sqlbase, self.dbms)
		if not os.path.isdir (self.dbmsbase):
			raise error (f'{self.dbmsbase}: missing dbms {self.dbms} specific sql directory')
		self.tablespaces = self._use_tablespaces ()
		return self

	def __exit__ (self, exc_type: None | type[BaseException], exc_value: None | BaseException, traceback: None | TracebackType) -> None | bool:
		self.db.sync (exc_type is None and not self.dryrun)
		self.db.close ()
		del self.db
		return None
	
	def database_is_empty (self) -> bool:
		return len (self.db.tables ()) == 0
	
	def install_fulldb (self) -> None:
		self.install_files (
			pattern = 'fulldb',
			minimum_number_of_files = 1
		)
	
	def install_messages (self) -> None:
		self.install_files (pattern = 'messages', minimum_number_of_files = 2)
		
	def install_updates (self) -> None:
		installed: set[str]
		try:
			installed = self.db.stream ('SELECT version_number FROM agn_dbversioninfo_tbl').map (lambda r: r.version_number).set ()
		except error as e:
			if not self.dryrun:
				raise
			show (f'failed to retrieve already installed update files due to: {e}\n')
			installed = set ()
			
		def select_missing (filename: str, mtch: Match[str]) -> bool:
			return mtch.group (2) not in installed
		#
		self.install_files (pattern = 'update-([0-9]{2}\\.(01|04|05|07|10|11)\\.[0-9]{3})', predicate = select_missing)
	
	def install_userrights (self) -> None:
		self.install_files (pattern = 'userrights', dbms_specific = False, minimum_number_of_files = 1)
	
	def install_migrations (self) -> None:
		self.install_files (pattern = 'migration')

	def install_files (self,
		pattern: str,
		*,
		dbms_specific: bool = True,
		minimum_number_of_files: int = 0,
		predicate: None | Callable[[str, Match[str]], bool] = None
	) -> None:
		paths = self._select_files (pattern, dbms_specific, predicate)
		if len (paths) < minimum_number_of_files:
			raise error (f'{pattern}: expect at least {minimum_number_of_files} files for pattern, but only found {len (paths)}')
		if paths:
			for path in paths:
				self.install_file (path)
		else:
			show (f'{pattern}: no files found, continue\n')

	def install_file (self, path: str) -> None:
		if self.dryrun:
			show (f'{path}: would execute\n')
			return
		#
		show (f'{path}: start processing .. ')
		if not self.tablespaces and self.db.support_tablespaces:
			procpath = self._remove_tablespaces (path)
		else:
			procpath = path
		#
		with self.db.cli () as cli:
			retry = True
			skip = False
			while retry:
				retry = False
				if not skip:
					(rc, output) = cli.process_file (procpath)
				else:
					skip = False
					rc = -1
				if rc != 0:
					show (f'failed:\n{output}\n\n')
					if ask ('Edit and retry', ['yes', 'no'], 'yes') == 'yes':
						if procpath == path:
							procpath = self._temppath (path)
							shutil.copyfile (path, procpath)
						retry = True
						with open (procpath, 'rb') as fdb:
							current = fdb.read ()
						Editor.edit (procpath)
						with open (procpath, 'rb') as fdb:
							if current == fdb.read ():
								skip = True
								show (f'**** {procpath}: not changed\n')
							del current
				else:
					show ('succeeded.\n')
		if rc:
			raise error ('{path}: failed to install{output}{temppath}'.format (
				path = path,
				output = f'\nFailed due to:\n{output}\n' if output else '',
				temppath = f'\nprocessed file for last attempt is "{procpath}"' if procpath != path else ''
			))
		if procpath != path:
			with suppress (OSError):
				os.unlink (procpath)
	
	def _remove_tablespaces (self, path: str) -> str:
		temppath = self._temppath (path)
		pattern = re.compile (' (using index )?tablespace\\s+[a-z0-9_]+(\\s*;|\\s+AS(\\s+|$))', re.IGNORECASE)
		with open (path) as fdi, open (temppath, 'w') as fdo:
			for line in fdi:
				fdo.write (pattern.sub ('\\2', line))
		return temppath

	def _temppath (self, path: str) -> str:
		(fdo, temppath) = mkstemp (prefix = f'{program}.', suffix = os.path.splitext (path)[-1], text = True)
		os.close (fdo)
		return temppath

	def _select_files (self, pattern: str, dbms_specific: bool, predicate: None | Callable[[str, Match[str]], bool]) -> list[str]:
		if dbms_specific:
			filepattern = re.compile (f'emm-{self.db.dbms}-({pattern})(-[a-z]+)?\\.sql$')
			basedirectory = self.dbmsbase
		else:
			filepattern = re.compile (f'({pattern})(-[a-z]+)?\\.sql$')
			basedirectory = self.sqlbase
		#
		class Selected (NamedTuple):
			pattern: str
			order_id: int
			filename: str
		#
		selected: list[Selected] = []
		for filename in os.listdir (basedirectory):
			if (mtch := filepattern.match (filename)) is not None:
				if predicate is None or predicate (filename, mtch):
					g = mtch.groups ()
					selected.append (Selected (
						pattern = g[0],
						order_id = {
							None: 0,
							'-basic': 1,
							'-extended': 2
						}.get (g[-1], 3),
						filename = filename
					))
		return Stream (selected).sorted ().map (lambda s: os.path.join (basedirectory, s.filename)).list ()
	
	def _use_tablespaces (self) -> bool:
		if not self.db.has_tablespaces:
			show ('No tablespaces used due to configuration from "dbcfg"\n')
			return False
		if not self.db.support_tablespaces:
			show ('No tablespaces used due to not supported by {self.dbms}\n')
			return False
		paths = self._select_files ('create-tablespaces', dbms_specific = True, predicate = None)
		if not paths:
			show ('No tablespace creation file found, skip check for existing tablespaces\n')
			return False
		#
		pathlist = Stream (paths).join (', ')
		tablespaces: list[str] = []
		match self.dbms:
			case 'oracle' | 'postgres':
				pattern = re.compile ('^CREATE TABLESPACE ([a-z0-9_]+) .*', re.IGNORECASE)
				tablespace_position = 1
			case _:
				raise error (f'{self.dbms}: no rules on how to path tablespace files {pathlist}')
		#
		for path in paths:
			with open (path) as fd:
				for line in fd.readlines ():
					if (mtch := pattern.match (line)) is not None:
						tablespace = mtch.group (tablespace_position).lower ()
						if tablespace not in tablespaces:
							tablespaces.append (tablespace)
		if not tablespaces:
			raise error (f'{self.dbms}: no tablespaces found in {pathlist}, expecting failed parsing logic')
		#
		available: set[str]
		match self.dbms:
			case 'oracle':
				available = self.db.stream ('SELECT tablespace_name FROM user_tablespaces').map_to (str, lambda r: r.tablespace_name.lower ()).set ()
			case 'postgres':
				available = self.db.stream ('SELECT spcname FROM pg_tablespace').map_to (str, lambda r: r.spcname.lower ()).set ()
		#
		missing = Stream (tablespaces).filter (lambda ts: ts not in available).list ()
		if missing:
			raise error ('{dbms}: missing tablespaces: {missing}\n'.format (
				dbms = self.dbms,
				missing = Stream (missing).join (', ')
			))
		return True

class Main (CLI):
	__slots__ = ['dryrun', 'dbid', 'sqlbase']
	def add_arguments (self, parser: argparse.ArgumentParser) -> None:
		parser.add_argument (
			'-n', '--dryrun', action = 'store_true',
			help = 'just show what would be done without executing it'
		)
		parser.add_argument (
			'-d', '--dbid', action = 'store',
			help = 'set database id to use for database setup'
		)
		parser.add_argument (
			'-p', '--sqlbase', action = 'store', default = os.path.join (base, 'webapps', 'emm', 'WEB-INF', 'sql'),
			help = 'set sql base directory'
		)
	
	def use_arguments (self, args: argparse.Namespace) -> None:
		self.dryrun: bool = args.dryrun
		self.dbid: None | str = args.dbid
		self.sqlbase: str = args.sqlbase

	def executor (self) -> bool:
		try:
			with DBInstall (self.dryrun, self.sqlbase, self.dbid) as dbinstall:
				if dbinstall.database_is_empty ():
					dbinstall.install_fulldb ()
					if not self.dryrun and dbinstall.database_is_empty ():
						raise error ('full db install failed')
				dbinstall.install_messages ()
				dbinstall.install_updates ()
				dbinstall.install_userrights ()
				dbinstall.install_migrations ()
		except error as e:
			show (f'** installation failed: {e}\n')
			return False
		return True

if __name__ == '__main__':
	Main.main ()
