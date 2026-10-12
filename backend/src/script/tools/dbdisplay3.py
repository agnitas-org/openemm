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
import	argparse, sys, os, re, getpass, shlex, fnmatch, subprocess, csv, json
import	readline, pickle
from	code import interact
from	collections import namedtuple
from	dataclasses import dataclass, make_dataclass
from	datetime import date, time, datetime, timedelta
from	difflib import unified_diff
from	enum import Enum
from	keyword import iskeyword
from	time import monotonic
from	traceback import print_exc
from	types import TracebackType
from	typing import Any, Callable, Literal, Optional, Union
from	typing import Dict, List, Match, NamedTuple, Sequence, Set, TextIO, Type
from	typing import cast
from	agn3.config import Config
from	agn3.db import Row, DB
from	agn3.dbconfig import DBConfig
from	agn3.dbcore import DBType
from	agn3.dbdriver import DBDriver
from	agn3.definitions import base, program, user
from	agn3.exceptions import error
from	agn3.ignore import Ignore
from	agn3.io import which, csv_default, csv_writer
from	agn3.log import interactive
from	agn3.runtime import CLI
from	agn3.stream import Stream
from	agn3.tools import atob, listsplit, call, defer, Plugin
#
dbdisplay_help = """
dbdisplay - a database viewing (and more) tool
==============================================

Starting a line with an exclamation mark, this line is interpreted as
a standard unix shell escape and no further input processing as
described below takes place. Without an argument, a subshell ist
started, with an argument, this is passed to a subshell to be executed
as a shell command.

This tool is primary designed to view database content as the standard
command line client which is part of oracle (sqlplus) does not provide
a nice output out of the box. Beyond this the tool has got some more
useful features over time.

A statment can span several lines but must be either ended by a
semicolon (;) or \\g or \\G. The later two are added for compatibility
with mariadb clients where \\g behaves like the semicolon while
\\G switches to pivot mode for this statement (depending of the
statment this will be taken into account, at the moment it is only
used in select alike statements.) You can switch permanently to pivot
mode by setting the configuration to True, e.g.:

set pivot = True;

This tool supports a limited completion mechanism for table and column
names.

Starting a line with the character sequence "f'" enables python style
formating for this input line where the values from the namespace
which is populated using the "let" command are used as positional
parameter, e.g.:

let company_id = 145;
f'select count(*) from company_{company_id}_tbl;

Setting the configuration parameter (see command "set")
"always_expand" to True this beahviour is enabled for all queries
without requiring the prefix "f'", e.g.:

set always_expand = True;


Beside normal SQL statements, these commands are available:


exit / quit
-----------

Leave the tool WITHOUT AN IMPLICIT commit. You can use your
End-Of-File key (usually ^D), too.


rollback
--------

Undos previous updates, inserts and deletes. However this can not be
used to undo all changes, as e.g. DDL commands are executed
immediately.


commit
------

Saves all not yet written changes to the database.


pending
-------

Shows all statements that changed the database and had not yet been
commited.


[ desc | vdesc] <table> [<pattern>]
-----------------------------------

Shows the definition of a table. If <pattern> is given (this is a list
of whitespace separated wildcard pattern) then only these columns are
shown in the description. This is handy for tables with lots of
columns. Using vdesc instead of desc gives a more verbose description
of the table.


export [<filename> [<charset>]]
-------------------------------------------------------

Without any parameter cancels a previous export command. Otherwise the
next SELECT (or WITH) statement is not displayed but redirected as a
csv file to <filename> using character set <charset> (UTF-8 on
default).


store [<name>]
--------------

Store the next SELECT (or WITH) statement using the name <name> for
further analysing it using the command ``interpret''. If <name> is
already in use, then the previous result is overwritten. If no <name>
is specified, a previous store command, that had not been filled is
canceled.


save <name>
-----------

Save the result of the last query to the same namespace as the
``store'' command to avoid duplicate (and possible database cost
intensive) queries.


clear
-----

Flush all stored results and restart with an empty store.


interpret
---------

An interactive console is started with all stored results in one tuple
named ``r''. You have access to the database interface using ``db''.
Be aware, as you acting the same environment as the program itself, to
not alter or scramble it and render the current instance unusable!


reload
------

Reload the script from the script file (usful during development of a
script.)


let [<variable> = <exprssion>]
------------------------------

Set or show elements of the namespace which can be used to execute
statements with prepared values, e.g.:
	
let mailing_id = 123;
update mailing_tbl set mailerset = 1 where mailing_id = :mailing_id;

You can reference the content of the last query using the variable
``last'' and for convient the first row of the last query (if there
had been at least one row in result) using ``row'', e.g.:

select name from config_tbl;
let a = last[0].name;
let b = row.name;

are equal, if config_tbl had not been empty.


clr
---

Clears every entry from namespace managed using "let".


set [<variable> = <expression>]
-------------------------------

Simular to "let", but set internal configuration variables. This is
separat from the "let" namespace to avoid name clashes.


edit <table> <column> [where] <clause>
--------------------------------------

Allows to edit the content of exactly one column using your preferred
editor. The <clause> must ensure that exactly one row is selected. The
content of the field is then opened with an editor and, if changed, is
written back to the database. This is a simple way to change large
fields or to add special characters to a content like apostrophe or
newlines.


jedit <table> <column> [where] <clause>
---------------------------------------

As above but before storing the value in the database it is validated
as a "json" object. If it cannot be parsed as valid json, it can be
reedited.


diff
----

Shows the unified diff from last modified content using edit that had
been saved to review changes.


force
-----

In general all commands or statements the tool does not know are not
executed. This includes e.g. statements starting with DROP. If you are
sure to execute these statements, you have to use this command to
enforce the execution. Which may damage the database. You have been
warned!

Alternative, you can also prefix your dangerous command with force so
it is executed without further check.
"""

def de_control (s: str) -> str:
	if [ctrl for ctrl in '\t\r\f\b\v' if ctrl in s]:
		o: List[str] = []
		for ch in s:
			if ch == '\t':
				o.append (' ' * (8 - len (o) % 8))
			elif ch not in ('\r', '\f', '\b', '\v'):
				o.append (ch)
		return ''.join (o)
	return s

class Readline:
	__slots__ = ['active', 'state']
	class StateSetting (NamedTuple):
		history_file: str
		completer: Optional[Callable[[str, int], Optional[str]]]
		completion_display: Optional[Callable[[str, Sequence[str], int], None]]
		delims: str
	State = Literal[None, 'dbclient', 'interpret']
	def __init__ (self,
		history_file: str,
		completer: Callable[[str, int], Optional[str]],
		completion_display: Callable[[str, Sequence[str], int], None],
		delims: str,
		readline_config: List[str]
	) -> None:
		self.active: Readline.State = None
		self.state: Dict[Readline.State, Readline.StateSetting] = {
			'dbclient': Readline.StateSetting (
				history_file = history_file,
				completer = completer,
				completion_display = completion_display,
				delims = delims
			), 'interpret': Readline.StateSetting (
				history_file = f'{history_file}-interpret',
				completer = readline.get_completer (),
				completion_display =  None,
				delims = readline.get_completer_delims ()
			)
		}
		readline.parse_and_bind ('tab: complete')
		if readline_config:
			for line in readline_config:
				readline.parse_and_bind (line)
		self.switch ('dbclient')
	
	def __del__ (self) -> None:
		self.done ()
	
	def done (self) -> None:
		self.switch (None)
	
	def switch (self, target: Readline.State) -> None:
		if target != self.active:
			if self.active is not None:
				readline.write_history_file (self.state[self.active].history_file)
			if target is not None:
				setting = self.state[target]
				readline.clear_history ()
				readline.set_completer (setting.completer)
				readline.set_completion_display_matches_hook (setting.completion_display)
				readline.set_completer_delims (setting.delims)
				if os.path.isfile (setting.history_file):
					readline.read_history_file (setting.history_file)
			self.active = target

class Force (Enum):
	Disabled = 0
	Once = 1
	Enabled = 2
class DBClient:
	__slots__ = [
		'prompt', 'prompt2', 'cprompt', 'editor',
		'dbid', 'dbcfg', 'db', 'ns', 'state', 'mod',
		'export', 'last', 'store_name', 'store',
		'let_globals', 'interpret_locals', 'scripts',
		'force', 'force_rollback',
		'caches', 'pending', 'diff', 'null',
		'readline', 'completion'
	]
	class Callback (NamedTuple):
		convert: Callable[[Any], str]
		lenght: Callable[[str], int]
		align: str
	__callback_map: Dict[DBType, DBClient.Callback] = {}
	@classmethod
	def setup_client (cls) -> None:
		if not cls.__callback_map:
			#
			def c_blob (i: Any) -> str:
				rc: List[str] = []
				bc: bytes
				with Ignore (AttributeError):
					i = i.read ()
				bc = i if isinstance (i, bytes) else str (i).encode ('UTF-8')
				for ch in bc:
					if ch == ord (b'\\'):
						rc.append ('\\\\')
					elif ch == ord (b'"'):
						rc.append ('\\"')
					elif ch >= 32 and ch < 128:
						rc.append (chr (ch))
					else:
						rc.append (f'\\{ch:02x}')
				return 'b"{bc}"'.format (bc =''.join (rc))
			#
			def c_string (i: Any) -> str:
				with Ignore (AttributeError):
					i = i.read ()
				if isinstance (i, bytes):
					return '{i!r}\''.format (i = i.replace (b'\'', b'\'\''))
				return '\'{i}\''.format (i = str (i).replace ('\'', '\'\''))
			#
			def c_datetime (i: Union[date, time, datetime]) -> str:
				if type (i) is date:
					return f'{i:%Y-%m-%d}'
				elif type (i) is time:
					return f'{i:%H:%M:%S}'
				return f'{i:%Y-%m-%d %H:%M:%S}'
			#
			def c_number (i: Union[int, float]) -> str:
				if float (int (i)) == float (i):
					return str (int (i))
				return str (float (i))
			#
			def c_rowid (i: Any) -> str:
				return 'ROWID: {s}'.format (s = c_string (i))
			#
			def strlen (s: str) -> int:
				return max ([len (de_control (_s)) for _s in s.split ('\n')])
			#
			cls.__callback_map = {
				DBType.BINARY: DBClient.Callback (c_blob, len, '-'),
				DBType.DATETIME: DBClient.Callback (c_datetime, len, '-'),
				DBType.STRING: DBClient.Callback (c_string, strlen, '-'),
				DBType.NUMBER: DBClient.Callback (c_number, len, ''),
				DBType.ROWID: DBClient.Callback (c_rowid, len, '')
			}

	class Export (NamedTuple):
		filename: str
		charset: str

	interpret_rc_file = os.path.join (base, f'.{program}.py')
	state_file = os.path.join (base, f'.{program}.state')
	state_key = '__state__'
	ns_key = '__ns__'
	mod_key = '__mod__'
	def __init__ (self, prompt: str, editor: str, caching: bool, forcing: bool, force_rollback: bool) -> None:
		self.prompt = prompt
		self.prompt2 = '.' * (len (prompt) - 1) + prompt[-1] if len (prompt) > 2 else prompt
		self.cprompt = self.prompt
		self.editor = editor
		self.dbid = 'dbclient.{user}'.format (user = getpass.getuser ())
		self.dbcfg = DBConfig ()
		self.db = DB (dbid = self.dbid)
		persist: dict[str, dict[str, Any]] = {}
		with Ignore (IOError), open (DBClient.state_file, 'rb') as fdb:
			persist = pickle.load (fdb)
			if isinstance (persist, dict) and DBClient.state_key not in persist:
				persist = {
					DBClient.state_key: persist
				}
		self.ns: Dict[str, Any] = persist.get (DBClient.ns_key, {})
		self.state: Dict[str, Any] = persist.get (DBClient.state_key, {
			'repeat_header': 30,
			'pivot': False,
			'min_execution_time': 0.1,
			'always_expand': False
		})
		self.mod: dict[str, dict[str, Any]] = persist.get (DBClient.mod_key, {})
		self.export: Optional[DBClient.Export] = None
		self.last: List[Row] = []
		self.store_name: Optional[str] = None
		self.store: Dict[str, List[Row]] = {}
		self.let_globals: Dict[str, Any] = {
			'datetime': datetime,
			'timedelta': timedelta,
			'Stream': Stream,
			'last': self.last,
			'store': self.store
		}
		self.interpret_locals: Dict[str, Any] = self.let_globals.copy ()
		self.let_globals.update ({
			'row': None
		})
		self.interpret_locals.update ({
			'db': self.db,
			'ns': self.ns,
			'mod': self.mod,
			'r': None
		})
		self.scripts: Optional[Plugin] = None
		if os.path.isfile (DBClient.interpret_rc_file):
			with open (DBClient.interpret_rc_file) as fd:
				rc_content = fd.read ()
			try:
				exec (rc_content, self.interpret_locals)
			except Exception as e:
				print (f'Failed to load rc-file "{DBClient.interpret_rc_file}": {e}')
				raise
		self.force: Force = Force.Disabled if not forcing else Force.Enabled
		self.force_rollback = force_rollback
		self.caches: Optional[Dict[str, List[str]]] = {} if caching else None
		self.pending: List[str] = []
		self.diff = ''
		self.null = 'NULL'
		self.readline: Optional[Readline] = None
		DBClient.setup_client ()

	def close (self) -> None:
		self.db.close ()
		with open (DBClient.state_file, 'wb') as fdb:
			pickle.dump ({
				DBClient.state_key: self.state,
				DBClient.ns_key: self.ns,
				DBClient.mod_key: self.mod
			}, fdb, protocol = -1)
	
	def open (self) -> None:
		self.close ()
		if not self.db.isopen ():
			try:
				raise error ('Failed to open database: {error}'.format (error = self.db.last_error ()))
			finally:
				self.close ()
	
	def cache (self, key: str, retrieve: Callable[[], List[str]]) -> List[str]:
		with Ignore (KeyError):
			if self.caches is not None:
				return self.caches[key]
		result = retrieve ()
		if self.caches is not None:
			self.caches[key] = result
		return result
	
	__pattern_access = re.compile ('^([a-z0-9_#]+)(\\[([a-z0-9_]+)\\])?@([a-z0-9_.]+)$', re.IGNORECASE)
	__pattern_user = re.compile ('^[a-z0-9_#]+(\\[([a-z0-9_]+)\\])?', re.IGNORECASE)
	def __getparam (self, forced_dbid: Optional[str], user: str) -> Dict[str, str]:
		m = self.__pattern_access.match (user)
		if m is not None:
			(username, userproxy, proxy, target) = m.groups ()
			userfull = f'{username}{userproxy}' if userproxy else None
			target = target.lower ()
			for dbid in self.dbcfg:
				if forced_dbid is None or dbid == forced_dbid:
					with Ignore (KeyError):
						rec = self.dbcfg[dbid]
						if proxy and (userfull is None or rec['user'] != userfull):
							check = (Stream ([rec['user']])
								.map (lambda u: self.__pattern_user.match (u))
								.filter (lambda m: m is not None)
								.map (lambda m: cast (Match[str], m).group (2))
								.reduce (lambda a, b: b if b is not None else a, rec['user'])
							)
							if check == proxy and rec['dbms'] == 'oracle' and rec['sid'].lower () == target:
								rc = rec.copy ()
								rc['user'] = f'{username}[{proxy}]'
								with Ignore (KeyError):
									del rc['password']
								return rc
						elif rec['user'] == username or (userfull is not None and rec['user'] == userfull):
							if (
								(rec['dbms'] == 'oracle' and rec['sid'].lower () == target)
								or
								(rec['dbms'] in ('mariadb', 'postgres') and rec['name'].lower () == target)
							):
								return rec.copy ()
		raise error (f'{user}: no matching database configuration found')

	def setup (self,
		record: Optional[Dict[str, str]],
		dbid: Optional[str],
		user: str,
		password: Optional[str] = None,
		password_callback: Optional[Callable[[], Optional[str]]] = None
	) -> None:
		self.close ()
		if record is not None:
			dbrec = record.copy ()
		else:
			dbrec = self.__getparam (dbid, user)
		pwkey = 'password'
		if pwkey not in dbrec or dbrec[pwkey] is None:
			if password is None:
				if password_callback is not None and callable (password_callback):
					password = password_callback ()
				if password is None:
					raise error (f'{user}: no matching password found and non provided')
			dbrec[pwkey] = password
		DBDriver.inject (self.dbid, dbrec)
		self.open ()
	
	class Page:
		__slots__ = ['_content']
		def __init__ (self) -> None:
			self._content: List[str] = []
		
		def __enter__ (self) -> DBClient.Page:
			return self
		
		def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
			self.show ()
			return None

		@property
		def content (self) -> str:
			return ''.join (self._content)
			
		def append (self, content: str) -> None:
			self._content.append (content)
		
		def appendnl (self, content: str) -> None:
			self.append (f'{content}\n')
		
		def show (self) -> None:
			pass

	def __page (self) -> DBClient.Page:
		method = self.state.get ('paging')
		if method == 'pager':
			class PagePager (DBClient.Page):
				__slots__ = ['pager']
				def __init__ (self, pager: str) -> None:
					super ().__init__ ()
					self.pager = pager
				def show (self) -> None:
					pp = subprocess.Popen (shlex.split (self.pager), stdin = subprocess.PIPE, text = True)
					pp.communicate (self.content)
			return PagePager (self.state.get ('pager', os.environ.get ('DBDISPLAY_PAGER', os.environ.get ('PAGER', 'more'))))
		#
		show = self.__show
		class PageDefault (DBClient.Page):
			def show (self) -> None:
				show (self.content)
		return PageDefault ()
	
	def __out (self, msg: str) -> str:
		return msg

	def __err (self, msg: str) -> None:
		sys.stderr.write (self.__out (msg))
		sys.stderr.flush ()
	
	def __errnl (self, msg: str) -> None:
		self.__err ('{msg}\n'.format (msg = self.__out (msg)))
	
	def __show (self, msg: str) -> None:
		sys.stdout.write (self.__out (msg))
		sys.stdout.flush ()
	
	def __shownl (self, msg: str) -> None:
		self.__show ('{msg}\n'.format (msg = self.__out (msg)))
	
	def __ask (self, question: str, answers: List[str]) -> str:
		while True:
			self.__show ('{q}? [{a}]: '.format (
				q = question,
				a= ', '.join (answers)
			))
			answer = sys.stdin.readline ().strip ()
			if answer in answers:
				return answer
			else:
				self.__errnl ('** try again')

	def __show_pendings (self) -> None:
		with self.__page () as page:
			page.append ('Pending changes not commited:\n\n{pending}\n\n'.format (
				pending = Stream (self.pending).map (lambda s: f'{s};').join ('\n')
			))

	def __expander (self, text: str, lazy: bool = True) -> str:
		if (prefix := text.startswith ('f\'')) or atob (self.state.get ('always_expand')):
			if prefix:
				text = text[2:]
			try:
				return text.format (**self.ns)
			except KeyError:
				if not lazy:
					raise
		return text
		
	__pattern_parameter = re.compile ('(:[a-z0-9_]+|\'[^\']*\')', re.IGNORECASE)
	def __add_pending (self, query: str, param: Optional[Dict[str, Any]] = None) -> None:
		ns: Dict[str, Any] = self.ns if param is None else param
		parameter = (Stream[str] (DBClient.__pattern_parameter.findall (query))
			.filter (lambda p: bool (p.startswith (':')))
			.map (lambda p: p[1:])
			.map (lambda p: '{var}={val!r}'.format (var = p, val = ns.get (p, '?')))
			.join (', ')
		)
		if parameter:
			self.pending.append (f'{query} [{parameter}]')
		else:
			self.pending.append (query)
	
	def __save_diff (self, old: str, new: str) -> None:
		if old != new:
			self.diff = Stream (
				unified_diff (
					old.split ('\n'),
					new.split ('\n'),
					lineterm = ''
				)
			).skip (2).join ('\n')
		else:
			self.diff = ''

	@dataclass
	class Column:
		header: str
		maxlength: int = 0
		convert: Callable[[Any], str] = str
		length: Callable[[Any], int] = len
		align: str = ''
	class Element (NamedTuple):
		text: str
	class Layout (NamedTuple):
		sep: str
		fmt: str
	def __format (self,
		page: DBClient.Page,
		rower: List[DBClient.Column],
		content: List[List[DBClient.Element]]
	) -> None:
		if content:
			try:
				repeat_header = int (self.state.get ('repeat_header', 30))
			except ValueError:
				repeat_header = 30
			layout: List[DBClient.Layout] = []
			for detail in rower:
				layout.append (
					DBClient.Layout (
						sep = '-' * (detail.maxlength + 2),
						fmt = ' %%%s%d.%ds ' % (detail.align, detail.maxlength, detail.maxlength)
					)
				)
			header = '.%s.' % 'v'.join ([_l.sep for _l in layout])
			sep = '+%s+' % '+'.join ([_l.sep for _l in layout])
			bottom = '`%s\'' % '^'.join ([_l.sep for _l in layout])
			formatter = '|%s|' % '|'.join ([_l.fmt for _l in layout])
			headline = formatter % tuple ([_r.header for _r in rower])
			n = 0
			for line in content:
				if n == 0 or (repeat_header > 0 and n % repeat_header == 0):
					page.appendnl (sep if n else header)
					page.appendnl (headline)
					page.appendnl (sep)
				try:
					lines: List[List[str]] = []
					for element in line:
						if '\n' in element.text:
							lines.append ([de_control (_c) for _c in element.text.split ('\n')])
						else:
							lines.append ([de_control (element.text)])
					lcount = max ([len (_l) for _l in lines])
					for column in lines:
						column += [''] * (lcount - len (column))
					for pos in range (lcount):
						page.appendnl (formatter % tuple ([_l[pos] for _l in lines]))
				except UnicodeDecodeError:
					raise error (f'Failed to display line due to convertion rules: {line}')
				n += 1
			page.appendnl (bottom)

	def __format_pivot (self,
		page: DBClient.Page,
		rower: List[DBClient.Column],
		content: List[List[DBClient.Element]]
	) -> None:
		if content:
			max_head_length = max (len (_c.header) for _c in rower) + 1
			header: List[str] = []
			for detail in rower:
				header.append ('{spacer}{name}: '.format (
					spacer = ' ' * (max_head_length - len (detail.header)),
					name = detail.header
				))
			for (lineno, line) in enumerate (content, start = 1):
				page.appendnl (f'******************** row {lineno:,d} ********************')
				for (index, element) in enumerate (line):
					page.appendnl (header[index] + de_control (element.text))
					if '\n' in element.text:
						page.appendnl ('')
				page.appendnl ('')

	def __deref (self, value: Any) -> Any:
		if type (value) not in (bool, int, float, str, datetime, date, time):
			with Ignore (AttributeError):
				return value.read ()
		return value

	__pattern_normalize = re.compile ('^[a-z][0-9a-z_]*$', re.IGNORECASE)
	def __normalize (self, seen: Set[str], pos: int, name: str) -> str:
		if name in seen:
			name = f'{name}_{pos}'
		if DBClient.__pattern_normalize.match (name) is None or iskeyword (name) or name in seen:
			return f'C{pos}'
		seen.add (name)
		return name

	def __row_type (self, header: List[str]) -> Type[Row]:
		seen: Set[str] = set ()
		return cast (Type[Row], namedtuple ('row', [self.__normalize (seen, _n, _h) for (_n, _h) in enumerate (header, start = 1)]))

	def __ex_query (self, query: str) -> tuple[int, int]:
		count_exec = 0
		count_retr = 0
		rower: List[DBClient.Column] = []
		fd: Optional[TextIO] = None
		wr: csv_writer
		if self.export is None:
			self.last.clear ()
		if self.export is not None or self.store_name is not None:
			if self.export is not None:
				try:
					fd = open (self.export.filename, 'w')
					wr = csv.writer (fd, csv_default)
				except IOError as e:
					raise error (f'Failed to open export file {self.export.filename}: {e}')
			if self.store_name is not None:
				store: List[Row] = []
			show = False
		else:
			show = True
			content: List[List[DBClient.Element]] = []
		
		with self.db.request () as cursor:
			start = monotonic ()
			count_exec = cursor.update (query, self.ns)
			diff_exec = monotonic () - start
			for (count_retr, row) in enumerate (cursor, start = 1):
				if count_retr == 1:
					desc = cursor.description (normalize = True)
					row_type = self.__row_type ([_d.name for _d in desc])
					for elem in desc:
						try:
							convert, length, align = self.__callback_map[elem.typ]
						except KeyError:
							convert, length, align = self.__callback_map[DBType.STRING]
						c = DBClient.Column (header = elem[0], maxlength = len (elem[0]), convert = convert, length = length, align = align)
						rower.append (c)
					if self.export is not None:
						wr.writerow ([_r.header for _r in rower])
				#
				preparsed = [self.__deref (_r) if _r is not None else None for _r in row]
				named = row_type (*preparsed)
				if self.export is None:
					self.last.append (named)
				if self.export is not None or self.store_name is not None:
					if self.export is not None:
						wr.writerow (preparsed)
					if self.store_name is not None:
						store.append (named)
				if show:
					line: List[DBClient.Element] = []
					for (elem, detail) in zip (row, rower):
						celem = detail.convert (elem) if elem is not None else self.null
						clen = detail.length (celem) if celem is not None else len (self.null)
						if clen > detail.maxlength:
							detail.maxlength = clen
						line.append (DBClient.Element (text = celem))
					content.append (line)
			diff_retr = monotonic () - start

		with self.__page () as page:
			if self.export is None:
				self.let_globals['row'] = self.last[0] if self.last else None
			if self.export is not None:
				if fd is not None:
					fd.close ()
				page.appendnl (f'Exported to {self.export.filename} in {self.export.charset}')
				self.export = None
			if self.store_name is not None:
				self.store[self.store_name] = store
				page.appendnl (f'Stored query to {self.store_name}')
				self.store_name = None
			if show:
				if self.state.get ('pivot-once', atob (self.state.get ('pivot'))):
					self.__format_pivot (page, rower, content)
				else:
					self.__format (page, rower, content)
			page.appendnl ('{executed}query affected {count_exec} row{s1}, retrieved {count_retr} record{s2}'.format (
				executed = f'executed in {diff_exec:.2f} seconds, retrieved in {diff_retr:.2f} seconds, ' if diff_retr >= self.state.get ('min_execution_time', 0.1) else '',
				count_exec = count_exec,
				count_retr = count_retr,
				s1 = '' if count_exec == 1 else 's',
				s2 = '' if count_retr == 1 else 's'
			))
		return (count_exec, count_retr)

	def __ex_explain (self, query: str) -> None:
		(_, query) = query.split (None, 1)
		statement_id = f'{user}-{datetime.now ().timestamp ():.2f}'
		explain_query = f'EXPLAIN PLAN SET STATEMENT_ID = \'{statement_id}\' INTO plan_table FOR {query}'
		with self.db.request () as cursor:
			cursor.update (explain_query)
			self.__ex_query (
				'SELECT id, LPAD(\' \',2*(LEVEL-1))||operation operation, options, object_name, object_alias, position '
				'FROM plan_table '
				f'START WITH id = 0 AND statement_id = \'{statement_id}\' '
				f'CONNECT BY PRIOR id = parent_id AND statement_id = \'{statement_id}\' '
				'ORDER BY id'
			)
			cursor.update ('DELETE FROM plan_table WHERE statement_id = :statement_id', {'statement_id': statement_id})
			
	def __ex_describe (self, table: str, pattern: Optional[List[str]], verbose: bool) -> None:
		extra = []
		rower = [
			DBClient.Column (header = 'Column', align = '-'),
			DBClient.Column (header = 'Type', align = '-'),
			DBClient.Column (header = 'Null?', align = '-'),
			DBClient.Column (header = 'Comment', align = '-')
		]
		types: Dict[str, Dict[str, Any]] = {}
		def c_type (column: str, t: DBType, display_size: int, size: int, prec: int) -> str:
			if t == DBType.STRING:
				rc = 'STRING'
				if display_size is not None and display_size > 0 and size != -1:
					rc += f'({display_size})'
				elif size is not None and size > 0:
					rc += f'({size} bytes)'
			elif t == DBType.BINARY:
				rc = 'BINARY'
				if size is not None and size > 0:
					rc += f'({size} bytes)'
			elif t == DBType.NUMBER:
				rc = 'NUMBER'
				if size is not None and size > 0:
					rc += f'({size}'
					if prec is not None and prec > 0:
						rc += f',{prec}'
					rc += ')'
			elif t == DBType.DATETIME:
				rc = 'DATETIME'
			elif t == DBType.ROWID:
				rc = 'ROWID'
			elif t is None:
				rc = 'unspec'
			else:
				rc = f'?{t!r}'
				if size is not None and size > 0:
					rc += f'({size})'
			if (rec := types.get (column)) is not None and rec['primaryKey']:
				rc += ' PRIMARY KEY'
			return rc
		#
		table_lower = table.lower ()
		table_comment: Optional[str] = None
		column_comment: Dict[str, str] = {}
		if (query := self.db.qselect (
			oracle = 'SELECT table_name, comments AS table_comment FROM user_tab_comments WHERE lower(table_name) = :table_name',
			mariadb = 'SELECT table_name, table_comment FROM information_schema.tables WHERE lower(table_name) = :table_name AND table_schema = :schema',
			postgres = 'SELECT table_name, NULL AS table_comment FROM information_schema.tables WHERE lower(table_name) = :table_name AND table_schema = :schema AND table_type = \'BASE TABLE\'',
		)):
			rq = self.db.querys (query, {'table_name': table_lower, 'schema': self.db.schema})
			if rq is not None:
				table_name = rq.table_name
				table_comment = rq.table_comment
				if table_comment is not None:
					table_comment = table_comment.strip ()
				query = self.db.qselect (
					oracle = 'SELECT column_name, comments AS column_comment FROM user_col_comments WHERE table_name = :table_name',
					mariadb = 'SELECT column_name, column_comment FROM information_schema.columns WHERE table_name = :table_name AND table_schema = :schema',
					postgres = 'SELECT column_name, NULL AS column_comment FROM information_schema.columns WHERE table_name = :table_name AND table_schema = :schema'
				)
				for row in self.db.query (query, {'table_name': table_name, 'schema': self.db.schema}):
					if row.column_comment is not None:
						comment = row.column_comment.strip ()
						if comment:
							column_comment[row.column_name] = comment
		#
		def add_rec (column: str, key: str, value: Any) -> None:
			rec: dict[str, Any]
			try:
				rec = types[column]
			except KeyError:
				rec = {
					'type': None,
					'primaryKey': False,
					'default': None,
					'constraints': [],
					'indexes': [],
					'comments': []
				}
				types[column] = rec
			if isinstance (rec[key], list):
				rec[key].append (value)
			else:
				rec[key] = value
		if self.db.dbms == 'oracle':
			query = (
				'SELECT ucc.constraint_name, ucc.column_name, uc.constraint_type, uc.search_condition '
				'FROM user_cons_columns ucc '
				'     INNER JOIN user_constraints uc ON uc.constraint_name = ucc.constraint_name '
				'WHERE lower(ucc.table_name) = :table_name'
			)
			for (cName, cColumn, cType, cDefinition) in self.db.query (query, {'table_name': table_lower}):
				if cType == 'P':
					add_rec (cColumn, 'primaryKey', True)
				elif cType == 'C':
					add_rec (cColumn, 'constraints', f'Constraint: {cName}: {cDefinition}')
				#
			if verbose:
				query = (
					'SELECT ic.index_name, ic.column_name, ie.column_expression '
					'FROM user_ind_columns ic '
					'     LEFT OUTER JOIN user_ind_expressions ie ON ie.index_name = ic.index_name '
					'WHERE lower (ic.table_name) = :table_name'
				)
				for (iName, iColumn, iExpr) in self.db.query (query, {'table_name': table_lower}):
					add_rec (iColumn, 'indexes', 'Index: {name}: {column}{expr}'.format (
						name = iName,
						column = iColumn,
						expr = f' ({iExpr})' if iExpr else ''
					))
				for row in self.db.query (
					'SELECT column_name, data_type, data_length, data_precision, data_scale, data_default, character_set_name, collation '
					'FROM user_tab_columns '
					'WHERE lower(table_name) = :table_name',
					{
						'table_name': table_lower
					}
				):
					if row.data_type:
						typ = row.data_type
						detail = (row.data_length, row.data_precision, row.data_scale)
						if (details := Stream (detail).filter (lambda v: v is not None).join (',')):
							typ += f'({details})'
						add_rec (row.column_name, 'type', typ)
					if (value := row.data_default) is not None and (value := value.strip ()) and value != 'NULL':
						add_rec (row.column_name, 'default', value)
					if row.character_set_name:
						add_rec (row.column_name, 'comments', f'Character set: {row.character_set_name}')
					if row.collation:
						add_rec (row.column_name, 'comments', f'Collation {row.collation}')
		elif self.db.dbms == 'mariadb':
			if verbose:
				query = (
					'SELECT column_name, column_default, character_set_name, collation_name, column_type, column_key '
					'FROM information_schema.columns '
					'WHERE lower(table_name) = :table_name AND table_schema=(SELECT SCHEMA())'
				)
				for row in self.db.query (query, {'table_name': table_lower}):
					if (value := row.column_default) is not None and (value := value.strip ()) and value != 'NULL':
						add_rec (row.column_name, 'default', value)
					if row.character_set_name:
						add_rec (row.column_name, 'comments', f'Character set: {row.character_set_name}')
					if row.collation_name:
						add_rec (row.column_name, 'comments', f'Collation {row.collation_name}')
					if row.column_type:
						add_rec (row.column_name, 'type', row.column_type)
			query = (
				'SELECT index_name, column_name '
				'FROM information_schema.statistics '
				'WHERE lower(table_name) = :table_name AND table_schema=(SELECT SCHEMA())'
			)
			for (iName, iColumn) in self.db.query (query, {'table_name': table_lower}):
				if iName == 'PRIMARY':
					add_rec (iColumn, 'primaryKey', True)
				else:
					add_rec (iColumn, 'indexes', f'Index: {iName}: {iColumn}')
		elif self.db.dbms == 'sqlite':
			if verbose:
				query = (
					'SELECT name, tbl_name, sql '
					'FROM sqlite_master '
					'WHERE type = \'index\' AND lower(name) = :table_name'
				)
				column_pattern = re.compile ('on +[^ ]+ +\\(([^)]+)\\)', re.IGNORECASE | re.MULTILINE)
				for (iName, iSQL) in self.db.query (query, {'table_name': table_lower}):
					if (mtch := column_pattern.search (iSQL)) is not None:
						for iColumn in listsplit (mtch.group (1)):
							if iColumn:
								add_rec (iColumn, 'indexes', f'Index {iName}: {iSQL}')
		#
		with self.db.request () as cursor:
			cursor.querys (f'SELECT * FROM {table} WHERE 1 = 0')
			desc = cursor.description ()
		content: List[List[DBClient.Element]] = []
		for entry in desc:
			if pattern:
				match = False
				column_name = entry[0].lower ()
				for p in pattern:
					if fnmatch.fnmatch (column_name, p):
						match = True
						break
			else:
				match = True
			if match:
				cparts: List[str] = []
				if entry[0] in column_comment and column_comment[entry[0]]:
					cparts.append (column_comment[entry[0]])
				if (rec := types.get (entry[0])) is not None:
					rec = types[entry[0]]
					if verbose:
						if (value := rec['default']):
							cparts.append (f'Default: {value}')
						for key in 'constraints', 'indexes', 'comments':
							if (value := rec[key]):
								cparts.append (value if isinstance (value, str) else '\n'.join (rec[key]))
					if (typ := rec['type']) is not None and rec.get ('primaryKey', False):
						typ += ' PRIMARY KEY'
				else:
					typ = None
				content.append ([
					DBClient.Element (entry[0]),
					DBClient.Element (typ if typ is not None else c_type (entry[0], self.db.check_open ().typ (entry), entry[2], entry[3], entry[4])),
					DBClient.Element ( 'NOT NULL' if entry[6] == 0 else ''),
					DBClient.Element ('\n'.join (cparts))
				])
		column: DBClient.Column
		for (n, column) in enumerate (rower):
			column.maxlength = max ([len (column.header)] + [max (len (_l) for _l in _c[n].text.split ('\n')) for _c in content])
		#
		table_description = table
		if self.db.dbms == 'oracle':
			query = (
				'SELECT table_name '
				'FROM user_synonyms '
				'WHERE lower(synonym_name) = :table_name'
			)
			table_name = table
			table_description_extra = []
			rq = self.db.querys (query, {'table_name': table_lower})
			if rq is not None:
				table_name = rq[0]
				table_description_extra.append (f'synonym of {table_name}')
			query = (
				'SELECT text '
				'FROM user_views '
				'WHERE view_name = :table_name'
			)
			rq = self.db.querys (query, {'table_name': table_name})
			if rq is not None:
				if verbose:
					extra.append (f'Is a view definied as:\n{rq.text}')
				table_description_extra.append ('is a view')
			else:
				query = (
					'SELECT tablespace_name, temporary '
					'FROM user_tables '
					'WHERE table_name = :table_name'
				)
				rq = self.db.querys (query, {'table_name': table_name})
				if rq is not None:
					if rq.tablespace_name is not None:
						table_description_extra.append (f'in tablespace {rq.tablespace_name}')
					if rq.temporary is not None and rq.temporary == 'Y':
						table_description_extra.append ('global temporary table')
			query = (
				'SELECT synonym_name '
				'FROM user_synonyms '
				'WHERE table_name = :table_name'
			)
			syns = []
			for (sName, ) in self.db.query (query, {'table_name': table_name}):
				syns.append (sName)
			if syns:
				table_description_extra.append ('a.k.a {synonyms}'.format (synonyms = ', '.join (syns)))
			if table_description_extra:
				table_description += ' ({desc})'.format (desc = ' '.join (table_description_extra))
		elif self.db.dbms == 'mariadb':
			query = (
				'SELECT view_definition '
				'FROM information_schema.views '
				'WHERE lower(table_name) = :table_name AND table_schema=(SELECT SCHEMA())'
			)
			rq = self.db.querys (query, {'table_name': table_lower})
			if rq is not None:
				if verbose:
					extra.append (f'Is a view defined as:\n{rq.view_definition}')
				table_description += ' (is a view)'
		elif self.db.dbms == 'sqlite':
			query = (
				'SELECT sql '
				'FROM sqlite_master '
				'WHERE type = \'view\' AND lower(name) = :table_name'
			)
			rq = self.db.querys (query, {'table_name': table_lower})
			if rq is not None:
				if verbose:
					extra.append (f'Is a view defined as:\n{rq.sql}')
				table_description += ' (is a view)'
		#
		with self.__page () as page:
			page.appendnl (table_description)
			page.appendnl ('=' * len (table_description))
			if table_comment:
				page.appendnl (table_comment)
			self.__format (page, rower, content)
			page.appendnl ('')
			if verbose:
				columnNames = set ([_d[0] for _d in desc])
				for (rname, rec) in types.items ():
					if rname not in columnNames:
						for key in 'constraints', 'indexes':
							extra += rec[key]
				#
				if self.db.dbms == 'oracle':
					query = (
						'SELECT trigger_name, trigger_type, triggering_event, trigger_body '
						'FROM user_triggers '
						'WHERE lower(table_name) = :table_name'
					)
					for (tName, tType, tEvent, tBody) in self.db.query (query, {'table_name': table_lower}):
						extra.append (f'Trigger {tName} {tType} {tEvent}:\n{tBody}')
				elif self.db.dbms == 'mariadb':
					query = (
						'SELECT trigger_name, event_manipulation, action_orientation, action_timing, action_statement '
						'FROM information_schema.triggers '
						'WHERE lower(event_object_table) = :table_name AND event_object_schema=(SELECT SCHEMA())'
					)
					for (tName, tManipulation, tOrientation, tTiming, tStatement) in self.db.query (query, {'table_name': table_lower}):
						extra.append (f'Trigger {tName} {tManipulation} {tOrientation} {tTiming}:\n{tStatement}')
				elif self.db.dbms == 'sqlite':
					query = (
						'SELECT name, sql '
						'FROM sqlite_master '
						'WHERE type = \'trigger\' AND lower(tbl_name) = :table_name'
					)
					for (tName, tSQL) in self.db.query (query, {'table_name': table_lower}):
						extra.append (f'Trigger {tName}:\n{tSQL}')
			#
			if extra:
				page.appendnl ('\n'.join (extra) + '\n')

	def __ex_diff (self) -> None:
		if self.diff:
			with self.__page () as page:
				page.appendnl (self.diff)
		
	def __ex_edit (self, table: str, column: str, clause: str, as_json: bool) -> None:
		tempfile = os.path.join (base, 'var', 'tmp', 'edit-{column}-{pid}-{timestamp}.temp'.format (
			column = column,
			pid = os.getpid (),
			timestamp = int (datetime.now ().timestamp ())
		))
		try:
			query = f'SELECT {column} FROM {table} WHERE {clause}'
			cnt = 0
			content: Optional[str] = None
			for row in self.db.query (query, self.ns):
				cnt += 1
				if cnt == 1:
					try:
						content = str (row[0]) if row[0] is not None else ''
					except Exception as e:
						raise error (f'failed to query content due to {e}, only textual content can be edited')
			if cnt != 1 or content is None:
				raise error (f'query leads to {cnt} records, exactly one allowed')
			if as_json:
				try:
					json.loads (content)
				except Exception as e:
					self.__shownl (f'current value is no valid JSON: {e}')
			retry = True
			write = True
			while retry:
				retry = False
				if write:
					with open (tempfile, 'wb') as fd:
						fd.write (content.encode ('UTF-8'))
					write = False
				n = call ([self.editor, tempfile])
				if n != 0:
					raise error (f'editor {self.editor} returns {n}, no change occured')
				cmd = 'save'
				try:
					with open (tempfile, 'rb') as fd:
						ncontent = fd.read ().decode ('UTF-8')
				except OSError as e:
					raise error (f'file {tempfile} no more accessable: {e}')
				except UnicodeDecodeError as e:
					cmd = self.__ask (f'No valid UTF-8 anymore ({e}), what now', ['edit', 'reset', 'abort'])
				else:
					if as_json:
						try:
							json.loads (ncontent)
						except Exception as e:
							cmd = self.__ask (f'No valid JSON anymore ({e}), what now', ['edit', 'reset', 'abort', 'save'])

				if cmd == 'save':
					if (not content or content[-1] != '\n') and ncontent and ncontent[-1] == '\n':
						ncontent = ncontent[:-1]
					if content == ncontent:
						self.__shownl ('content not changed, no update to database')
					else:
						placeholder = 'p'
						n = 0
						while placeholder in self.ns:
							n += 1
							placeholder = f'p{n}'
						query = f'UPDATE {table} SET {column} = :{placeholder} WHERE {clause}'
						try:
							data = self.ns.copy ()
							data[placeholder] = ncontent if ncontent else None
							cnt = self.db.update (query, data)
							if cnt == 1:
								self.__shownl ('changes saved, use "commit" to store permanent, use "diff" to review changes')
								self.__add_pending (query, data)
								self.__save_diff (content, ncontent)
							else:
								self.__errnl ('save failed (saved {count} rows): {error}'.format (
									count = cnt,
									error = self.db.last_error ()
								))
						except error as e:
							self.__errnl (f'save failed due to {e}')
							cnt = -1
						if cnt != 1:
							cmd = self.__ask ('Not proper saved, what now', ['edit', 'reset', 'abort'])
				elif cmd == 'edit':
					retry = True
				elif cmd == 'reset':
					retry = True
					write = True
				elif cmd == 'abort':
					raise error ('abort')
		except error as e:
			self.__errnl (f'** {e}')
		finally:
			if os.path.isfile (tempfile):
				os.unlink (tempfile)

	def __setup_scripts (self) -> None:
		script_path = os.path.join (base, f'.{program}.scripts')
		try:
			with open (script_path) as fd:
				code = fd.read ()
		except IOError:
			code = ''
		#
		def select_wrapper (query: str, parameter: Optional[Dict[str, Any]] = None) -> tuple[int, int]:
			save = self.ns
			try:
				self.ns = parameter if parameter is not None else {}
				return self.__ex_query (query)
			finally:
				self.ns = save

		def update_wrapper (query: str, parameter: Optional[Dict[str, Any]] = None) -> int:
			count = self.db.update (query, parameter)
			if count > 0:
				self.__add_pending (query, parameter if parameter is not None else {})
			return count
				
		def pending_wrapper (query: str, parameter: Optional[Dict[str, Any]] = None) -> None:
			self.__add_pending (query, parameter if parameter is not None else {})

		def catchall (method: str) -> Callable[..., None]:
			self.__errnl (f'** script {method} not found')
			def wrapper (*args: Any, **kwargs: Any) -> None:
				pass
			return wrapper

		try:
			self.scripts = Plugin (
				code = code,
				ns = {
					'db': self.db,
					'ns': self.ns,
					'mod': self.mod,
					'last': self.last,
					'store': self.store,
					'print': self.__shownl,
					'select': select_wrapper,
					'update': update_wrapper,
					'pending': pending_wrapper
				},
				catchall = catchall
			)
		except Exception as e:
			print_exc (file = sys.stderr)
			self.__errnl (f'\n** failed loading script from {script_path}: {e}')
			self.scripts = None
		
	def __ex_interpret (self) -> None:
		names = sorted (self.store.keys ())
		try:
			if self.readline is not None:
				self.readline.switch ('interpret')
			self.interpret_locals['r'] = make_dataclass ('Result', [(_n, List[Row]) for _n in names]) (*[self.store[_n] for _n in names])
			if names:
				self.__shownl ('Stored values are available through: {names}'.format (
					names = Stream (names).map (lambda n: f'r.{n}').join (', ')
				))
			else:
				self.__shownl ('No stored values available')
			interactive ()
			interact (
				banner = 'Starting internal interpreter for analysing, hit ^D for exit',
				local = self.interpret_locals,
				exitmsg = 'Returning from internal interpreter'
			)
		except Exception as e:
			self.__errnl (f'** interpreter aborted due to {e}')
		finally:
			interactive (False)
			if self.readline is not None:
				self.readline.switch ('dbclient')

	def __ex_letset (self, use: Dict[str, Any], expression: str) -> None:
		try:
			saved = use.copy ()
			exec (expression, self.let_globals, use)
		except Exception as e:
			use.clear ()
			use.update (saved)
			self.__errnl (f'** "{expression}" failed: {e}')

	def validate (self, query: str) -> bool:
		(status, missing, unused) = self.db.qvalidate (query, self.ns)
		if not status and missing:
			self.__errnl ('** missing parameter in namesapce: {names}'.format (names = Stream (missing).sorted ().join (', ')))
			return False
		return True
	
	def valid_store_name (self, name: str) -> bool:
		name = name.strip ()
		if not name:
			self.__errnl ('** no empty name allowed')
		elif name.startswith ('_'):
			self.__errnl ('** no leading underscore in name allowed')
		elif iskeyword (name):
			self.__errnl (f'** {name} is a reserved keyword and thus not allowed')
		else:
			return True
		return False

	__allowedCommands = set (['create', 'comment'])
	def execute (self, query: str) -> bool:
		nforce: Force = Force.Disabled if self.force != Force.Enabled else self.force
		if self.caches is not None:
			self.caches.clear ()
		try:
			elem: Optional[List[str]] = [_e.lower () for _e in shlex.split (query)]
		except ValueError as e:
			self.__errnl (f'** failed to preparse "{query}": {e}')
			elem = None
		if elem:
			cmd = elem[0]
			if cmd in ('quit', 'exit'):
				return False
			try:
				if cmd == 'help':
					with self.__page () as page:
						page.appendnl (dbdisplay_help)
				elif cmd in ('select', 'with', 'explain', 'show'):
					if self.validate (query):
						if cmd == 'show' and self.db.dbms not in ('mariadb', 'postgres'):
							self.__errnl (f'** "{cmd}" not supported on "{self.db.dbms}"')
						elif len (elem) == 1:
							self.__errnl (f'** {cmd}: incomplete')
						elif cmd == 'explain' and self.db.dbms == 'oracle':
							self.__ex_explain (query)
						else:
							self.__ex_query (query)
				elif cmd in ('insert', 'update', 'delete'):
					if cmd == 'delete' and 'where' not in elem:
						self.__errnl ('** delete not allowed without where clause')
					elif self.validate (query):
						(count, _) = self.__ex_query (query)
						if count > 0:
							self.__add_pending (query)
				elif cmd == 'rollback':
					self.db.sync (False)
					self.__shownl ('changes rollbacked')
					self.pending.clear ()
				elif cmd == 'commit':
					self.db.sync (True)
					self.__shownl ('changes commited')
					self.pending.clear ()
				elif cmd == 'pending':
					if self.pending:
						self.__show_pendings ()
					else:
						self.__shownl ('no pending commits found')
				elif cmd in ('desc', 'vdesc'):
					if len (elem) > 1:
						table_name = elem[1]
						pattern = [_p.lower () for _p in elem[2:]] if len (elem) > 2 else None
						self.__ex_describe (table_name, pattern, cmd[0] == 'v')
				elif cmd == 'export':
					if len (elem) == 1:
						self.export = None
					else:
						start = monotonic ()
						self.export = DBClient.Export (
							filename = elem[1],
							charset = elem[2] if len (elem) > 2 else 'UTF-8'
						)
						diff = monotonic () - start
						if diff >= self.state.get ('min_execution_time', 0.1):
							self.__shownl (f'executed in {diff:.2f} seconds')
				elif cmd == 'store':
					if len (elem) == 1:
						self.store_name = None
					elif len (elem) == 2:
						if self.valid_store_name (elem[1]):
							self.store_name = elem[1]
					else:
						self.__errnl (f'** usage: {cmd} [<name>]')
				elif cmd == 'save':
					if len (elem) == 2:
						if self.valid_store_name (elem[1]):
							self.store[elem[1]] = self.last.copy ()
					else:
						self.__errnl (f'** usage: {cmd} <name>')
				elif cmd == 'clear':
					self.store.clear ()
				elif cmd == 'interpret':
					if len (elem) == 1:
						self.__ex_interpret ()
					else:
						self.__errnl (f'** usage: {cmd}')
				elif '.' in cmd:
					if self.scripts is None:
						self.__setup_scripts ()
					parameter: None | str
					(module, command) = cmd.split ('.', 1)
					parameter = query.split (None, 1)[1] if len (elem) > 1 else None
					if self.scripts is not None:
						try:
							getattr (self.scripts, module) (command, parameter)
						except Exception as e:
							self.__errnl (f'** call {module} with {parameter} failed: {e}')
				elif cmd == 'reload':
					self.__setup_scripts ()
				elif cmd in ('let', 'set'):
					use = self.ns if cmd == 'let' else self.state
					if len (elem) == 1:
						with self.__page () as page:
							page.appendnl ('Known elements in {where}:\n{elements}'.format (
								where = 'namespace' if cmd == 'let' else 'configuration',
								elements = Stream (use.items ())
									.sorted (lambda kv: kv[0])
									.filter (lambda kv: '-' not in kv[0])
									.map (lambda kv: '\t{k} = {v!r}'.format (k = kv[0], v = kv[1]))
									.join ('\n')
							))
					else:
						self.__ex_letset (use, query.split (None, 1)[-1])
				elif cmd == 'clr':
					self.ns.clear ()
				elif cmd in ('edit', 'jedit'):
					check = query.split ()
					table: Optional[str] = None
					column: Optional[str] = None
					clause: Optional[str] = None
					if len (check) > 1:
						next = 0
						try:
							(table, column) = check[1].split ('.')
							next = 2
						except ValueError:
							table = check[1]
							if len (check) > 2:
								column = check[2]
								next = 3
						if next > 0 and len (check) > next and check[next].lower () == 'where':
							next += 1
						if len (check) > next:
							clause = query.split (None, next)[-1]
					
					if table is not None and column is not None and clause is not None:
						self.__ex_edit (table, column, clause, as_json = cmd == 'jedit')
					else:
						self.__errnl ('** edit requires table column and clause as parameter')
				elif cmd == 'diff':
					self.__ex_diff ()
				elif cmd == 'force':
					if len (elem) > 1:
						if self.force is Force.Disabled:
							self.force = Force.Once
						if not self.execute (query.split (None, 1)[-1]):
							return False
					else:
						nforce = Force.Once if self.force is Force.Disabled else self.force
				elif cmd in self.__allowedCommands or self.force is not Force.Disabled:
					(count, _) = self.__ex_query (query)
					if count > 0:
						self.__add_pending (query)
				else:
					self.__errnl (f'** {cmd} is prohibited')
			except error as e:
				self.__errnl (f'** {e}')
		if self.force_rollback and not self.pending and self.db.dbms in ('mariadb', 'postgres'):
			self.db.sync (False)
		self.force = nforce
		return True
	
	def __get_tables (self, prefix: str) -> List[str]:
		def collect_tables () -> List[str]:
			queries = self.db.qselect (
				oracle = [
					'SELECT lower(table_name) FROM user_tables',
					'SELECT lower(view_name) FROM user_views'
				], mariadb = [
					'SELECT lower(table_name) FROM information_schema.tables WHERE table_schema = :schema',
					'SELECT lower(table_name) FROM information_schema.views WHERE table_schema = :schema'
				], postgres = [
					'SELECT lower(table_name) FROM information_schema.tables WHERE table_schema = :schema'
				], sqlite = [
					'SELECT lower(name) FROM sqlite_master WHERE type IN (\'table\', \'view\') ORDER BY type'
				]
			)
			data: dict[str, None | str] = {
				'schema': self.db.schema
			}
			collect = set ()
			for query in queries:
				for row in self.db.queryc (query, data):
					if row[0] not in collect:
						collect.add (row[0])
			if self.db.dbms == 'oracle':
				for row in self.db.queryc ('SELECT lower(synonym_name) AS synonym_name, lower(table_name) AS table_name FROM user_synonyms'):
					if row.table_name in collect:
						collect.add (row.synonym_name)
			return sorted (collect)
		#
		return [_t for _t in self.cache ('tables', collect_tables) if _t.startswith (prefix)]
	
	def __get_columns (self, prefix: str, table: str) -> List[str]:
		def collect_columns () -> List[str]:
			with self.db.request () as cursor:
				cursor.querys (f'SELECT * FROM {table} WHERE 1 = 0')
				return sorted ([_d[0].lower () for _d in cursor.description ()])
		#
		return [_c for _c in self.cache (f'table:{table}', collect_columns) if _c.startswith (prefix)]

	__csqlcommands = [
		'quit', 'exit', 'help', 
		'select', 'with', 'show', 'explain',
		'insert', 'update', 'delete', 'call',
		'rollback', 'commit', 'pending', 'desc', 'vdesc',
		'export', 'store', 'save', 'clear',
		'interpret', 'reload',
		'let', 'set', 'clr', 'edit', 'jedit', 'diff',
		'force'
	] + list (__allowedCommands)
	__kwTable = {'into', 'from', 'update', 'table', 'join', 'desc', 'vdesc', 'edit', 'jedit'}
	__kwColumn = {'select', 'set', 'on', 'where', 'by', 'having'}
	def __completer (self, text: str, state: int) -> Optional[str]:
		if state == 0:
			elem = [_e.lower () for _e in shlex.split (self.__expander (text))]
			if len (elem) == 0:
				pattern = ''
				prevStart = -1
			else:
				if text[-1] == ' ':
					pattern = ''
					prevStart = len (elem) - 1
				else:
					pattern = elem[-1]
					prevStart = len (elem) - 2
			strings: Optional[List[str]] = None
			pos = prevStart
			while strings is None and pos >= 0:
				cur = elem[pos]
				if cur in self.__kwTable:
					strings = self.__get_tables (pattern)
				elif cur in self.__kwColumn:
					npos = pos - 1
					while npos >= 0:
						if elem[npos] in self.__kwTable:
							strings = self.__get_columns (pattern, elem[npos + 1])
							break
						npos -= 1
				pos -= 1
			#
			pattern_length = len (pattern)
			self.completion = (Stream (strings if strings is not None else self.__csqlcommands)
				.filter (lambda v: v.startswith (pattern))
				.map (lambda v: '{text}{option} '.format (text = text, option = v[pattern_length:]))
				.list ()
			)
		if self.completion:
			return self.completion.pop (0)
		return None
	
	def __completer_display (self, substitution: str, matches: Sequence[str], longest_match_length: int) -> None:
		realmatches = [_m.split ()[-1] for _m in matches]
		realmaxlen = max ([len (_m) for _m in realmatches])
		n = 0
		pos = 0
		out = ''
		while n < len (realmatches):
			out += '%-*.*s' % (realmaxlen, realmaxlen, realmatches[n])
			pos += realmaxlen
			if pos + 2 + realmaxlen < 78:
				out += '  '
			elif n + 1 < len (realmatches):
				out += '\n'
				pos = 0
			n += 1
		sys.stdout.write ('\n%s\n%s%s' % (out, self.cprompt, substitution))

	def input (self) -> str:
		return input (self.cprompt)

	def restart (self) -> None:
		self.open ()
		self.export = None
		self.store_name = None
		self.cprompt = self.prompt

	def banner (self) -> None:
		version_parts: list[str] = []
		extras: list[str] = []
		match self.db.dbms:
			case 'oracle':
				version_parts.append (
					self.db.stream ('SELECT banner FROM v$version')
					.filter (lambda r: bool (r.banner))
					.map (lambda r: r.banner)
					.join ('\n')
				)
			case 'mariadb':
				for row in (self.db.stream ('SHOW variables')
					.filter (lambda r: 'version' in r.variable_name.lower ())
					.sorted (lambda r: r.variable_name)
				):
					if row.variable_name not in {'version_malloc_library', 'version_source_revision', 'version_ssl_library', 'wsrep_patch_version'}:
						if row.variable_name.startswith ('version'):
							version_parts.append (row.value)
						elif row.variable_name.endswith ('_version'):
							extras.append ('{typ} {value}'.format (typ = row.variable_name.split ('_')[0], value = row.value))
			case 'postgres':
				rq = self.db.querys ('SHOW server_version')
				if rq is not None and rq.server_version:
					version_parts.append (f'server version "{rq.server_version}"')
				rq = self.db.querys ('SELECT current_database() AS dbname, current_user')
				if rq is not None:
					if rq.dbname is not None:
						if rq.current_user is not None:
							extras.append (f'connected as "{rq.current_user}@{rq.dbname}"')
						else:
							extras.append (f'connected on database "{rq.dbame}"')
					elif rq.current_user is not None:
						extras.append (f'connected as user "{rq.current_user}"')
		version = ' '.join (version_parts)
		if (schema := self.db.schema) is not None and schema:
			extras.append (f'using schema "{schema}"')
		if extras:
			if version:
				version += ' ({extras})'.format (extras = ', '.join (extras))
			else:
				version = Stream (extras).join (', ')
		print (version)

	def interactive (self, history_file: str, readline_config: List[str], show_banner: bool) -> None:
		self.readline = Readline (history_file, self.__completer, self.__completer_display, '', readline_config)
		try:
			if show_banner:
				with Ignore ():
					self.banner ()
			print ('For help type "help;"')
			try:
				query = ''
				shell = os.environ.get ('SHELL', '/bin/sh')
				env = os.environ.copy ()
				env['PS1'] = '[dbdisplay] {ps1}'.format (ps1 = env.get ('PS1', '$ '))
				#
				eoq_pattern = re.compile ('(;|\\\\g)\\s*$', re.IGNORECASE)
				#
				while True:
					try:
						line = self.input ()
						if line.startswith ('!'):
							command = line[1:].strip ()
							subprocess.call ([shell, '-c', command] if command else [shell, '-i'], env = env)
						elif line:
							query += line
							if (mtch := eoq_pattern.search (query)) is None:
								self.cprompt = self.prompt2
								query += '\n'
							else:
								query = (query[:mtch.start ()]).strip ()
								how = mtch.group (1)
								if how in ('\\g', '\\G'):
									self.state['pivot-once'] = how == '\\G'
								with defer (self.state, lambda c, ex: c.pop ('pivot-once', None)):
									self.cprompt = self.prompt
									try:
										spacer = '  ' if query.startswith ('f\'') else ''
										if (expanded := self.__expander (query, False)) != query:
											query = expanded
											self.__shownl (f'---> {spacer}{query};')
									except (KeyError, IndexError, ValueError) as e:
										self.__errnl (f'** failed to format (incomplete namespace): {query}: {e}')
										query = ''
									if query:
										if not self.execute (query):
											break
										query = ''
										
					except KeyboardInterrupt:
						print (' ** abort')
						query = ''
						self.restart ()
			except error as e:
				print (f'** failed due to {e}')
			except EOFError:
				print ('exit;')
			finally:
				if self.pending:
					self.__show_pendings ()
					self.db.sync (self.__ask ('Commit changed now', ['yes', 'no']) == 'yes')
		finally:
			if self.readline is not None:
				self.readline.done ()
				self.readline = None

class DBDisplay (CLI):
	def setup (self) -> None:
		self.history_file = os.path.join (base, f'.{program}.history')
		config_file = os.path.join (base, f'.{program}.rc')
		self.readline_config: List[str] = []
		self.cfg = Config ()
		if os.path.isfile (config_file):
			self.cfg.read (config_file)
		self.default_user = self.cfg.bget ('default-user', False)
		self.proxy_user = self.cfg.bget ('proxy-user', True)
		self.name = self.cfg.get ('name')
		self.dbid = self.cfg.get ('dbid')
		self.history_file = self.cfg.get ('history-file', self.history_file)
		rlconfig = self.cfg.get ('readline')
		if rlconfig:
			for line in [_l.strip () for _l in rlconfig.split ('\n')]:
				if line:
					self.readline_config.append (line)
		self.editor: str
		for key in 'EDITOR', 'EDIT', 'VISUAL':
			if key in os.environ:
				self.editor = os.environ[key]
				break
		else:
			for vi in ['vim', 'vi']:
				editor = which ('vi')
				if editor is not None:
					self.editor = editor
					break
		self.caching = False
		self.forcing = False
		self.force_rollback = True

	def add_arguments (self, parser: argparse.ArgumentParser) -> None:
		parser.add_argument (
			'-d', '--default-user', action = 'store_true', default = self.default_user, dest = 'default_user',
			help = 'use default user from local database configuration'
		)
		parser.add_argument (
			'-p', '--proxy_user', action = 'store_false', default = self.proxy_user, dest = 'proxy_user',
			help = 'disable usage of OS user as proxy user (oracle only)'
		)
		parser.add_argument (
			'-s', '--name', action = 'store', default = self.name,
			help = 'set database name (MariaDB), filename (sqlite3) or SID (oracle)'
		)
		parser.add_argument (
			'-i', '--dbid', action = 'store', default = self.dbid,
			help = 'set the dbid (database id) to be used to access local database configuration'
		)
		parser.add_argument (
			'-e', '--editor', action = 'store', default = self.editor,
			help = 'set the external editor for editing database content using the "edit" command'
		)
		parser.add_argument (
			'-c', '--cache', action = 'store_true', default = self.caching, dest = 'caching',
			help = 'enables caching of cost intensive internal queries like table layout'
		)
		parser.add_argument (
			'--jedi', '--may-the-force-be-with-you', action = 'store_true', default = self.forcing, dest = 'forcing',
			help = 'permanent enable forcing mode'
		)
		parser.add_argument (
			'--no-rollback-enforced', action = 'store_false', default = self.force_rollback, dest = 'force_rollback',
			help = 'disable forcing of rollbacks if nothing has modified (used to avoid locking transaction on MariaDB)'
		)
		parser.add_argument (
			'-r', '--section', action = 'store',
			help = 'get database configuration record from personal configuration file section with this name'
		)
		parser.add_argument (
			'-b', '--banner', action = 'store_true',
			help = 'show initial banner in interactive mode'
		)
		parser.add_argument (
			'parameter', nargs = '*'
		)

	def use_arguments (self, args: argparse.Namespace) -> None:
		self.default_user = args.default_user
		self.proxy_user = args.proxy_user
		self.name = args.name
		self.dbid = args.dbid
		self.editor = args.editor
		self.caching = args.caching
		self.forcing = args.forcing
		self.force_rollback = args.force_rollback
		self.section = args.section
		self.banner = args.banner
		self.parameter = args.parameter

	def executor (self) -> bool:
		username = getpass.getuser ()
		record: None | dict[str, str] = None
		if (param := self.cfg.get (f'{self.section}.record')) is not None and param:
			record = DBConfig.record (param)
		dbcfg = DBConfig ()
		user: Optional[str] = None
		queries = self.parameter[:]
		if not self.default_user:
			if len (queries) > 0:
				user = queries[0]
				queries = queries[1:]
		#
		standard_user: Optional[str] = None
		if record is not None:
			record = record.copy ()
			if user is None:
				user = record.get ('user')
			else:
				record['user'] = user
		elif user is None:
			try:
				dbstd = dbcfg[self.dbid]
				if dbstd['dbms'] == 'oracle':
					sid = self.name if self.name is not None else dbstd['sid']
					proxy_pattern = re.compile ('\\[([^]]+)\\]')
					if self.dbid != 'emm' and self.proxy_user:
						m = proxy_pattern.search (dbstd['user'])
						if m is None:
							standard_user = '{user}[{proxy}]@{sid}'.format (
								user = username,
								proxy = dbstd['user'],
								sid = sid
							)
						else:
							standard_user = '{user}[{proxy}]@{sid}'.format (
								user = username,
								proxy = m.group (1),
								sid = sid
							)
					else:
						standard_user = '{user}@{sid}'.format (
							user = dbstd['user'],
							sid = sid
						)
				elif dbstd['dbms'] in ('mariadb', 'postgres'):
					name = self.name if self.name is not None else dbstd['name']
					standard_user = '{user}@{name}'.format (
						user = dbstd['user'],
						name = name
					)
				else:
					standard_user = None
			except KeyError:
				standard_user = None
			if self.default_user:
				if standard_user is None:
					self.die ('Failed to deterimante default user')
				user = standard_user
			else:
				try:
					user = self.user_input ('Username', default = standard_user)
				except KeyboardInterrupt:
					self.die ('input aborted')
		elif self.dbid is not None:
			record = dbcfg[self.dbid].copy ()
			if user is not None:
				record['user'] = user
			else:
				with Ignore (KeyError):
					del record['user']
			with Ignore (KeyError):
				del record['password']
		#
		if user is None:
			raise error ('no user given and no user found')
		#
		dbc = DBClient ('SQL> ', self.editor, self.caching, self.forcing, self.force_rollback)
		try:
			try:
				dbc.setup (record, self.dbid, user, password_callback = lambda: self.user_input (f'Password for {user}', hidden = True))
			except error as e:
				self.err (f'** setup aborted: {e}')
			except KeyboardInterrupt:
				self.err ('** setup aborted')
			else:
				if queries:
					for query in queries:
						if query:
							print (f'{dbc.prompt}{query}')
							if not dbc.execute (query):
								break
				else:
					dbc.interactive (self.history_file, self.readline_config, self.banner)
		except Exception:
			print_exc (file = sys.stderr)
		finally:
			dbc.close ()
		return True
	
	def user_input (self, prompt: Optional[str] = None, default: Optional[str] = None, hidden: bool = False) -> Optional[str]:
		rc: Optional[str]
		if hidden:
			rc = getpass.getpass (f'{prompt}: ')
		else:
			if default:
				if prompt:
					prompt = f'{prompt} [{default}]: '
				else:
					prompt = f'[{default}]: '
			elif prompt:
				prompt = f'{prompt}: '
			if prompt:
				sys.stdout.write (prompt)
				sys.stdout.flush ()
			rc = sys.stdin.readline ().rstrip ('\r\n')
			if not rc:
				rc = default
		return rc

	def err (self, msg: str) -> None:
		sys.stderr.write (f'{msg}\n')
		sys.stderr.flush ()

	def die (self, msg: str) -> None:
		self.err (msg)
		sys.exit (1)

if __name__ == '__main__':
	DBDisplay.main ()
