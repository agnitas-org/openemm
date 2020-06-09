#!/usr/bin/env python3
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
from	__future__ import annotations
import	logging, argparse
import	os, time, re, errno
from	collections import namedtuple, defaultdict
from	functools import partial
from	datetime import datetime
from	dataclasses import dataclass, field
from	typing import Any, Callable, Final, Optional, Protocol, Union
from	typing import DefaultDict, Dict, Iterator, List, NamedTuple, Pattern, Set, TextIO, Tuple, Type
from	typing import cast
from	agn3.cache import Cache
from	agn3.db import DB
from	agn3.dbm import DBM
from	agn3.definitions import base, licence, syscfg
from	agn3.email import EMail
from	agn3.emm.companyconfig import CompanyConfig
from	agn3.emm.types import UserStatus
from	agn3.exceptions import error
from	agn3.ignore import Ignore
from	agn3.io import create_path, cstreamopen
from	agn3.log import log
from	agn3.parameter import Parameter
from	agn3.parser import Unit, ParseTimestamp
from	agn3.plugin import Plugin, LoggingManager
from	agn3.runtime import Runtime
from	agn3.stream import Stream
from	agn3.template import Template
from	agn3.tools import atoi, atob
from	agn3.tracker import Key, Tracker
#
logger = logging.getLogger (__name__)
#
class UpdatePlugin (Plugin): #{{{
	plugin_version = '2.0'
#}}}
class Duplicate: #{{{
	__slots__ = ['name', 'expiration', 'path', 'dbs']
	unit = Unit ()
	def __init__ (self, name: str, expiration: Unit.Parsable) -> None:
		self.name = name
		self.expiration = self.unit.parse (expiration, 7 * 24 * 60 * 60)
		self.path = os.path.join (base, 'var', 'run', f'duplicate-{name}')
		create_path (self.path)
		self.dbs: List[DBM] = []
	
	def __contains__ (self, line: str) -> bool:
		key = line.encode ('UTF-8')
		for db in self.dbs:
			if key in db:
				return True
		self.dbs[0][key] = b''
		return False
	
	def open (self) -> bool:
		self.close ()
		now = datetime.now ()
		today = now.toordinal ()
		filenames = (Stream.range (self.expiration + 1)
			.map (lambda n: now.fromordinal (today - n))
			.map (lambda d: '%04d%02d%02d.gdbm' % (d.year, d.month, d.day))
			.list ()
		)
		current = filenames[0]
		current_exists = False
		for filename in sorted (os.listdir (self.path), reverse = True):
			path = os.path.join (self.path, filename)
			if os.path.isfile (path):
				if filename in filenames:
					if filename == current:
						current_exists = True
						mode = 'w'
					else:
						mode = 'r'
					self.dbs.append (DBM (path, mode))
				else:
					logger.info ('Removing outdated file %s' % path)
					os.unlink (path)
		if not current_exists:
			path = os.path.join (self.path, current)
			self.dbs.insert (0, DBM (path, 'c'))
		return bool (self.dbs)
	
	def close (self, do_expire: bool = True) -> None:
		Stream (self.dbs).each (lambda d: d.close ())
		self.dbs.clear ()
#}}}
class Log: #{{{
	__slots__ = ['logpath', 'name', 'target']
	def __init__ (self, logpath: str, name: str) -> None:
		self.logpath = logpath
		self.name = name
		self.target = os.path.join (self.logpath, self.name)
		create_path (self.target)
	
	def unpack (self, s: str) -> Tuple[int, int]:
		(nr, seq) = (int (_v) for _v in s.split ('-'))
		return (nr, seq)
	
	valid = re.compile ('^[0-9]+-[0-9]+$')
	def __iter__ (self) -> Iterator[str]:
		return iter (Stream (os.listdir (self.target))
			.regexp (self.valid)
			.sorted (key = lambda f: self.unpack (f))
			.map (lambda f: os.path.join (self.target, f))
		)
	
	def __len__ (self) -> int:
		return sum (1 for _ in self)
	
	def add (self, path: str, max_lines: int = 10000) -> None:
		with open (path) as fd:
			fdw: Optional[TextIO] = None
			try:
				now = 0
				seq = 0
				cur = 0
				count = 0
				for line in fd:
					if fdw is None:
						ts = int (time.time ())
						if ts != now:
							now = ts
							seq = self.__next_sequence (now)
						while fdw is None:
							output = os.path.join (self.target, f'{now}-{seq}')
							seq += 1
							try:
								fno = os.open (output, os.O_CREAT |os.O_EXCL, 0o666)
								try:
									fdw = open (output, 'w')
								finally:
									os.close (fno)
							except OSError as e:
								if e.errno != errno.EEXIST:
									raise
						cur = 0
						count += 1
					fdw.write (line)
					cur += 1
					if cur >= max_lines:
						fdw.close ()
						fdw = None
			finally:
				if fdw is not None:
					fdw.close ()
			logger.info (f'Wrote {path} to {count:,d} chunks')
		try:
			os.unlink (path)
		except OSError as e:
			logger.error ('Failed to remove proccessed file: %s' % e)
	
	def __next_sequence (self, timestamp: int) -> int:
		seq = 0
		for filename in os.listdir (self.target):
			with Ignore (ValueError):
				(ts, sq) = self.unpack (filename)
				if ts == timestamp:
					seq = max (seq, sq + 1)
		return seq
#}}}
class Line (Protocol): #{{{
	def __getattr__ (self, name: str) -> Any: ...
	def __getitem__ (self, item: int) -> Any: ...
#}}}
class Field (NamedTuple): #{{{
	name: str
	converter: Callable[[str], Any]
#}}}
class Lineparser: #{{{
	__slots__ = ['splitter', 'target_class', 'column_count', 'converter']
	def __init__ (self, splitter: Callable[[str], List[str]], *fields: Union[Field, str]) -> None:
		self.splitter = splitter
		self.target_class = cast (Type[Line], namedtuple ('Line', tuple (cast (Field, _f).name if type (_f) is Field else cast (str, _f) for _f in fields)))
		self.column_count = len (fields)
		self.converter = (Stream (fields)
			.map (lambda f: f.converter if type (f) is Field else lambda a: a)
			.list ()
		)
	
	def __call__ (self, line: str) -> Line:
		elements = self.splitter (line)
		if len (elements) != self.column_count:
			raise error ('%s: expected %d elements, got %d elements' % (line, self.column_count, len (elements)))
		try:
			return self.target_class (*tuple (_c (_e) for (_c, _e) in zip (self.converter, elements)))
		except Exception as e:
			raise error ('%s: failed to parse: %s' % (line, e))
#}}}
class Update: #{{{
	__slots__ = ['basename', 'failpath', 'current', 'pid', 'lineno', 'log', 'plugin', 'check_for_duplicates', 'duplicate', 'tracker', 'tracker_age', 'tracker_expire']
	name = 'update'
	path = '/dev/null'
	def __init__ (self) -> None:
		directory = os.path.dirname (self.path)
		self.basename = os.path.basename (self.path).split ('.')[0]
		self.failpath = os.path.join (directory, f'{self.basename}.fail')
		self.current = 1
		self.pid = os.getpid ()
		self.lineno = 0
		self.log = Log (directory, self.name)
		self.plugin = UpdatePlugin (manager = LoggingManager)
		self.check_for_duplicates = True
		self.duplicate: Optional[Duplicate] = None
		self.tracker = Tracker (os.path.join (base, 'var', 'run', f'update-{self.name}.track'))
		self.tracker_age: Unit.Parsable = None
		self.tracker_expire = 0
	
	def setup (self) -> None:
		if self.check_for_duplicates:
			self.duplicate = Duplicate (name = self.name, expiration = '7d')

	def done (self) -> None:
		self.tracker.close ()

	def step (self) -> None:
		if self.tracker_age is not None:
			day = datetime.now ().toordinal ()
			if self.tracker_expire != day:
				self.tracker_expire = day
				logger.info ('Start expiration')
				self.tracker.expire (created = self.tracker_age)
				logger.info ('Expiration done')

	def exists (self) -> bool:
		return os.access (self.path, os.F_OK) or bool (self.log)

	def rename_to_temp (self) -> str:
		tfname = self.path + '.%d.%d.%d' % (self.pid, time.time (), self.current)
		self.current += 1
		try:
			os.rename (self.path, tfname)
			logger.info ('Renamed %s to %s' % (self.path, tfname))
		except OSError as e:
			logger.error ('Unable to rename %s to %s: %s' % (self.path, tfname, e))
			raise error (f'failed to rename {self.path} to {tfname}', e)
		else:
			time.sleep (2)
		return tfname

	def __save (self, fname: str, line: str) -> bool:
		try:
			with open (fname, 'a') as fd:
				fd.write (line + '\n')
			return True
		except IOError as e:
			logger.error ('Failed to write to %s: %s' % (fname, e))
		return False

	def save_to_fail (self, line: str) -> bool:
		return self.__save (self.failpath, line)

	def save_to_log (self, line: str) -> bool:
		return self.__save (log.data_filename (self.basename), line)

	def update_prepare (self) -> bool:
		return True
	def update_finished (self) -> bool:
		return True
	def update_start (self, db: DB) -> bool:
		raise error ('Need to overwrite update_start in your subclass')
	def update_end (self, db: DB) -> bool:
		raise error ('Need to overwrite update_end in your subclass')
	def update_line (self, db: DB, line: str) -> bool:
		raise error ('Need to overwrite update_line in your subclass')

	def execute (self, is_active: Callable[[], bool], delay: Optional[int]) -> None:
		self.setup ()
		while is_active ():
			self.step ()
			if self.exists ():
				count = self.__fill_and_count_log ()
				if count > 0:
					if self.update_prepare ():
						with DB () as db:
							if not self.update (db, count, is_active):
								logger.info ('Update failed')
							else:
								logger.debug ('Update successed')
						if not self.update_finished ():
							logger.info ('Finished failed')
					else:
						logger.info ('Prepare failed')
			if delay is None:
				break
			else:
				n = delay
				while is_active () and n > 0:
					time.sleep (1)
					n -= 1
		self.done ()
	
	def __fill_and_count_log (self) -> int:
		if os.path.isfile (self.path):
			try:
				tfname = self.rename_to_temp ()
			except error as e:
				logger.error (f'Failed creating temp.file: {e}')
			else:
				try:
					count = cstreamopen (tfname).count ()
					logger.info (f'{tfname} with {count:,d} entries to process')
					self.log.add (tfname, 10000)
					logger.info (f'Added {tfname} to log')
				except IOError as e:
					logger.error (f'Unable to open {tfname}: {e}')
		count = 0
		for path in self.log:
			with Ignore (IOError):
				count += cstreamopen (path).count ()
		logger.info (f'Files with {count:,d} entries to process')
		return count

	def update (self, db: DB, count: int, is_active: Callable[[], bool]) -> bool:
		self.plugin ().start (self, db.cursor)
		rc = self.update_start (db)
		if rc:
			if self.duplicate is not None:
				self.duplicate.open ()
			self.lineno = 0
			for path in self.log:
				if os.path.isfile (path):
					do_remove = False
					new_path = '%s-%.3f' % (path, time.time ())
					try:
						os.rename (path, new_path)
						with open (new_path) as fd:
							do_remove = True
							for line in (_l.strip () for _l in fd):
								self.lineno += 1
								if self.duplicate is not None and line in self.duplicate:
									logger.debug (f'Ignore duplicate line: {line}')
								else:
									if not self.update_line (db, line):
										if not self.save_to_fail (line):
											do_remove = False
										rc = False
									else:
										if not self.save_to_log (line):
											do_remove = False
						if do_remove:
							os.unlink (new_path)
					except (OSError, IOError) as e:
						logger.error ('Failed to process %s as %s: %s' % (path, new_path, e))
					logger.info (f'{self.name}: Now at line {self.lineno:,d} of {count:,d}')
					db.sync ()
				else:
					logger.warning ('%s: File %s vanished' % (self.name, path))
				if not rc or not is_active ():
					break
			if self.duplicate is not None:
				self.duplicate.close ()
		if not self.update_end (db):
			rc = False
		self.plugin ().end (self, db.cursor, rc)
		self.tracker.close ()
		return rc
#}}}

class Detail: #{{{
	Ignore = 0
	Internal = 100
	Success = 200
	SoftbounceOther = 400
	SoftbounceReceiver = 410
	SoftbounceMailbox = 420
	HardbounceOther = 510
	HardbounceReceiver = 511
	HardbounceSystem = 512
	Softbounces = (SoftbounceOther, SoftbounceReceiver, SoftbounceMailbox)
	Hardbounces = (HardbounceOther, HardbounceReceiver, HardbounceSystem)
	names = {
		Ignore:			'Ignore',
		Internal:		'Internal',
		Success:		'Success',
		SoftbounceOther:	'SB-Other',
		SoftbounceReceiver: 	'SB-Recv',
		SoftbounceMailbox:	'SB-MBox',
		HardbounceOther:	'HB-Other',
		HardbounceReceiver: 	'HB-Recv',
		HardbounceSystem:	'HB-Sys'
	}
#}}}
class UpdateBounce (Update): #{{{
	__slots__ = [
		'mailing_map',
		'igcount', 'sucount', 'sbcount', 'hbcount', 'blcount', 'rvcount', 'ccount',
		'translate', 'cache', 'succeeded',
		'has_mailtrack', 'has_mailtrack_last_read'
	]
	name = 'bounce'
	path = os.path.join (base, 'log', 'extbounce.log')
	class Info: #{{{
		def __init__ (self, info: str) -> None:
			self.info = info
			self.map: Dict[str, str] = {}
			for elem in info.split ('\t'):
				parts = elem.split ('=', 1)
				if len (parts) == 2:
					self.map[parts[0]] = parts[1]
				elif len (parts) == 1:
					self.map['stat'] = elem

		def __str__ (self) -> str:
			return Stream (self.map.items ()).map (lambda kv: '%s="%s"' % kv).join (', ')
		__repr__ = __str__
		
		def __contains__ (self, var: str) -> bool:
			return var in self.map
		
		def __getitem__ (self, var: str) -> Optional[str]:
			return self.get (var)

		def get (self, var: str, default: Optional[str] = None) -> Optional[str]:
			try:
				return self.map[var]
			except KeyError:
				return default
	#}}}
	class Translate: #{{{
		default = {
			Detail.Ignore:			[4, 40, 500, 41, 42, 43, 44, 45, 46, 47, 5, 515, 52, 53, 54, 55, 56, 57],
			Detail.Success:			[2],
			Detail.SoftbounceOther:		[50, 532, 541, 544, 570, 574],
			Detail.SoftbounceReceiver:	[411, 412, 413, 414, 416, 417, 418, 510, 514, 571, 51],
			Detail.SoftbounceMailbox:	[421, 422, 520, 521, 522, 523, 524],
			Detail.HardbounceOther:		[531],
			Detail.HardbounceReceiver:	[511, 513, 516, 517, 572],
 			Detail.HardbounceSystem:	[512, 518]
		}
		class Pattern:
			controlPattern = re.compile ('^/(.*)/([a-z]*)$')
			def __init__ (self, data: str, debug: bool) -> None:
				try:
					self.data = Parameter (data).data
				except Exception as e:
					logger.warning ('Failed to parse input data %r: %s' % (data, e))
					self.data = {}
				self.debug = debug
				self.checks: Dict[str, Pattern[str]] = {}
				flags: Union[int, re.RegexFlag]
				for (key, value) in self.data.items ():
					try:
						if key == 'stat':
							pattern = value
							flags = re.IGNORECASE
						elif key == 'relay':
							if len (value) > 2 and value.startswith ('/') and value.endswith ('/'):
								pattern = value[1:-1]
							else:
								relays: List[str] = []
								for relay in value.split ('|'):
									if relay.endswith ('.'):
										relay = relay[:-1]
										post = ''
									else:
										post = '.*'
									if relay.startswith ('.'):
										relay = relay[1:]
										pre = '(.+\\.)?'
									else:
										pre = ''
									relays.append ('{pre}{relay}{post}'.format (
										pre = pre,
										relay = re.escape (relay),
										post = post
									))
								pattern = '^({relays})$'.format (relays = '|'.join (relays))
							flags = re.IGNORECASE
						else:
							flags = 0
							mtch = self.controlPattern.match (value)
							if mtch is not None:
								(pattern, opts) = mtch.groups ()
								for opt in opts:
									if opt == 'i':
										flags |= re.IGNORECASE
							else:
								pattern = value
						if self.debug:
							print ('\tTranslate: %s="%s" -> "%s"%s' % (key, value, pattern, ' (ignorecase)' if flags & re.IGNORECASE else ''))
						self.checks[key] = re.compile (pattern, flags)
					except re.error as e:
						logger.warning ('Failed to parse regex in %s="%s": %s' % (key, value, e))

			def valid (self) -> bool:
				return len (self.checks) > 0

			def match (self, infos: UpdateBounce.Info) -> bool:
				for (key, pattern) in [(_k.lower (), _v) for (_k, _v) in self.checks.items ()]:
					value = infos[key]
					if value is None:
						return False
					if key == 'relay':
						value = (value.split (':')[0]).split ('[')[0]
					if pattern.search (value) is None:
						return False
				return True
		
		@dataclass
		class Element:
			detail: int
			pattern: Optional[UpdateBounce.Translate.Pattern]

		def __init__ (self, debug: bool = False) -> None:
			self.debug = debug
			self.default_tab: Dict[int, List[UpdateBounce.Translate.Element]] = {}
			for (key, value) in self.default.items ():
				for dsn in value:
					self.default_tab[dsn] = [UpdateBounce.Translate.Element (detail = key, pattern = None)]
			self.tab: DefaultDict[int, Dict[int, List[UpdateBounce.Translate.Element]]] = defaultdict (dict)
		
		def clear (self) -> None:
			self.tab.clear ()

		def add (self, company_id: int, dsn: int, detail: int, pattern_expr: Optional[str] = None) -> None:
			tab = self.tab[company_id]
			if pattern_expr is not None:
				if self.debug:
					print ('Pattern for company_id=%r for DSN=%r leading to Detail=%r using pattern %r' % (company_id, dsn, detail, pattern_expr))
				pattern: Optional[UpdateBounce.Translate.Pattern] = UpdateBounce.Translate.Pattern (pattern_expr, self.debug)
				if pattern is None or not pattern.valid ():
					logger.error ('Invalid pattern "%s" for company %d found' % (pattern_expr, company_id))
					return
			else:
				pattern = None
			if dsn in tab:
				if pattern is not None:
					tab[dsn].insert (0, UpdateBounce.Translate.Element (detail, pattern))
				else:
					ltab = tab[dsn]
					for (index, element) in enumerate (ltab):
						if element.pattern is None:
							ltab[index].detail = detail
							break
					else:
						ltab.append (UpdateBounce.Translate.Element (detail, pattern))
			else:
				tab[dsn] = [UpdateBounce.Translate.Element (detail, pattern)]

		def setup (self, db: DB) -> None:
			for row in db.query ('SELECT company_id, dsn, detail, pattern FROM bounce_translate_tbl'):
				self.add (row.company_id, row.dsn, row.detail, row.pattern)

		def trans (self, company: int, dsn: int, infos: UpdateBounce.Info) -> int:
			for tab in [self.tab[_k] for _k in (company, 0) if _k in self.tab] + [self.default_tab]:
				nr = dsn
				while nr > 0:
					if nr in tab:
						for element in tab[nr]:
							if element.pattern is None or element.pattern.match (infos):
								return element.detail
					nr //= 10
			return Detail.Ignore
	#}}}
	def __init__ (self) -> None:
		super ().__init__ ()
		self.tracker_age = '3d'
		self.mailing_map: Dict[int, int] = {}
		self.igcount = 0
		self.sucount = 0
		self.sbcount = 0
		self.hbcount = 0
		self.blcount = 0
		self.rvcount = 0
		self.ccount = 0
		self.translate = UpdateBounce.Translate ()
		self.cache = Cache (limit = 65536)
		self.succeeded: DefaultDict[int, int] = defaultdict (int)
		self.has_mailtrack: Dict[int, bool] = {}
		self.has_mailtrack_last_read = 0

	dsnparse = re.compile ('^([0-9])\\.([0-9])\\.([0-9])$')
	user_unknown = re.compile ('user unknown|unknown user', re.IGNORECASE)
	timestamp_parser = ParseTimestamp ()
	@dataclass
	class Breakdown:
		timestamp: datetime
		detail: int
		code: int
		bounce_type: UserStatus
		bounce_remark: Optional[str]
		mailloop_remark: Optional[str]
		infos: Optional[UpdateBounce.Info]

		@staticmethod
		def new (dsn: str, info: str, company_id: int, translate: UpdateBounce.Translate) -> UpdateBounce.Breakdown:
			rc = UpdateBounce.Breakdown (
				timestamp = datetime.now (),
				detail = Detail.Ignore,
				code = 0,
				bounce_type = UserStatus.BOUNCE,
				bounce_remark = None,
				mailloop_remark = None,
				infos = None
			)
			match = UpdateBounce.dsnparse.match (dsn)
			if match is not None:
				grp = match.groups ()
				rc.code = int (grp[0]) * 100 + int (grp[1]) * 10 + int (grp[2])
				rc.infos = infos = UpdateBounce.Info (info)
				timestamp = UpdateBounce.timestamp_parser (infos['timestamp'])
				if timestamp is not None:
					rc.timestamp = timestamp
				rc.mailloop_remark = infos['mailloop']
				if rc.mailloop_remark is not None:
					if UpdateBounce.user_unknown.search (rc.mailloop_remark) is not None:
						rc.code = 511
				if rc.code % 100 == 99:
					if rc.code // 100 == 5:
						rc.detail = Detail.HardbounceOther
					elif rc.code // 100 == 4:
						rc.detail = Detail.SoftbounceOther
					else:
						rc.detail = Detail.Internal
					admin = infos['admin']
					if admin is not None:
						rc.bounce_type = UserStatus.ADMOUT
						rc.bounce_remark = admin
					status = infos['status']
					if status is not None:
						with Ignore (KeyError):
							rc.bounce_type = UserStatus.find_status (status)
				else:
					rc.detail = translate.trans (company_id, rc.code, infos)
			if rc.bounce_remark is None:
				rc.bounce_remark = f'bounce:{rc.detail}'
			return rc

	def __map_mailing_to_company (self, db: DB, mailing_id: int) -> int:
		try:
			return self.mailing_map[mailing_id]
		except KeyError:
			rq = db.querys ('SELECT company_id FROM mailing_tbl WHERE mailing_id = :mailing_id', {'mailing_id': mailing_id})
			if rq is None:
				logger.error ('No company_id for mailing %d found' % mailing_id)
				return 0
			self.mailing_map[mailing_id] = rq.company_id
			return self.mailing_map[mailing_id]

	def __log_success (self, db: DB, company_id: int, now: int) -> bool:
		if self.has_mailtrack_last_read + 300 < now:
			temp: Dict[int, bool] = {}
			for row in db.query ('SELECT company_id, mailtracking FROM company_tbl'):
				temp[row.company_id] = row.mailtracking == 1
			self.has_mailtrack = temp
			self.has_mailtrack_last_read = now
		for check_company_id in (company_id, 0):
			with Ignore (KeyError):
				return self.has_mailtrack[check_company_id]
		self.has_mailtrack[company_id] = syscfg.get_bool ('log-success', False)
		return self.has_mailtrack[company_id]

	def __track_store (self, now: int, mailing: int, customer: int, detail: int) -> bool:
		store_it = True
		key = Key (f'{mailing}', f'{customer}')
		record: Dict[str, Any]
		with Ignore (KeyError):
			try:
				record = self.cache[key]
			except KeyError:
				record = self.tracker[key]
			if record['detail'] in Detail.Hardbounces and detail not in Detail.Hardbounces:
				store_it = False
		record = {
			'detail': detail
		}
		self.tracker[key] = record
		self.cache[key] = record
		return store_it

	def update_prepare (self) -> bool:
		self.cache.reset ()
		return True

	def update_start (self, db: DB) -> bool:
		self.igcount = 0
		self.sucount = 0
		self.sbcount = 0
		self.hbcount = 0
		self.blcount = 0
		self.rvcount = 0
		self.ccount = 0
		self.succeeded.clear ()
		self.translate.clear ()
		self.translate.setup (db)
		self.plugin ().start_bounce (db.cursor, Detail)
		return True

	def update_end (self, db: DB) -> bool:
		if self.succeeded:
			logger.info ('Add {mails:,d} mails to {mailings:d} mailings'.format (mails = cast (int, sum (self.succeeded.values ())), mailings = len (self.succeeded)))
			for mailing_id in sorted (self.succeeded):
				db.update ('UPDATE mailing_tbl SET delivered = delivered + :success WHERE mailing_id = :mailing_id', {'success': self.succeeded[mailing_id], 'mailing_id': mailing_id})
			db.sync ()
		logger.info ('Found %d hardbounces, %d softbounces (%d written), %d successes, %d blacklisted, %d revoked, %d ignored in %d lines' % (self.hbcount, self.sbcount, (self.sbcount - self.ccount), self.sucount, self.blcount, self.rvcount, self.igcount, self.lineno))
		return True

	def update_line (self, db: DB, line: str) -> bool:
		parts = line.split (';', 5)
		if len (parts) != 6:
			logger.warning (f'Got invalid line: {line}')
			return False
		try:
			(dsn, licence_id, mailing, media, customer, info) = (parts[0], int (parts[1]), int (parts[2]), int (parts[3]), int (parts[4]), parts[5])
		except ValueError:
			logger.warning (f'Unable to parse line: {line}')
			return False
		if licence_id != 0 and licence_id != licence:
			logger.debug ('Ignore bounce for other licenceID %d' % licence_id)
			return True
		if customer == 0:
			logger.debug ('Ignore virtual recipient')
			return True
		if mailing <= 0 or customer < 0:
			logger.warning (f'Got line with invalid mailing or customer: {line}')
			return False
		company = self.__map_mailing_to_company (db, mailing)
		if company <= 0:
			logger.warning ('Cannot map mailing %d to company for line: %s' % (mailing, line))
			return False
		breakdown = UpdateBounce.Breakdown.new (dsn, info, company, self.translate)
		if breakdown.detail == Detail.Ignore:
			logger.debug (f'Ignoring line: {line}')
			return True
		if breakdown.detail < 0:
			logger.warning ('Got line with invalid detail (%d): %s' % (breakdown.detail, line))
			return False
		now = int (time.time ())
		if breakdown.detail == Detail.Success:
			self.succeeded[mailing] += 1
			logging = self.__log_success (db, company, now)
		else:
			logging = True
		rc = True
		if logging:
			if breakdown.detail == Detail.Success:
				data = {
					'customer': customer,
					'mailing': mailing,
					'ts': breakdown.timestamp
				}
				try:
					query = (
						'INSERT INTO success_%d_tbl (customer_id, mailing_id, timestamp) '
						'VALUES (:customer, :mailing, :ts)' % company
					)
					db.update (query, data)
					self.plugin ().success (db.cursor, now, company, mailing, customer, breakdown.timestamp, breakdown.infos)
				except error as e:
					logger.error ('Unable to add success for company %d: %s' % (company, e))
					rc = False
				#
				self.sucount += 1
				if self.sucount % 1000 == 0:
					db.sync ()
			elif breakdown.bounce_type == UserStatus.BOUNCE:
				data = {
					'company': company,
					'customer': customer,
					'detail': breakdown.detail,
					'mailing': mailing,
					'dsn': breakdown.code,
					'ts': breakdown.timestamp
				}
				try:
					store = self.__track_store (now, mailing, customer, breakdown.detail)
					if store:
						query = db.qselect (
							oracle = (
								'INSERT INTO bounce_tbl '
								'       (bounce_id, company_id, customer_id, detail, mailing_id, dsn, timestamp) '
								'VALUES '
								'       (bounce_tbl_seq.nextval, :company, :customer, :detail, :mailing, :dsn, :ts)'
							), mysql = (
								'INSERT INTO bounce_tbl '
								'       (company_id, customer_id, detail, mailing_id, dsn, timestamp) '
								'VALUES '
								'       (:company, :customer, :detail, :mailing, :dsn, :ts)'
							)
						)
						db.update (query, data)
					elif breakdown.detail not in Detail.Hardbounces:
						self.ccount += 1
					self.plugin ().bounce (db.cursor, store, now, company, mailing, customer, breakdown.timestamp, breakdown.code, breakdown.detail, breakdown.infos)
				except error as e:
					logger.error ('Unable to add bounce %r to database: %s' % (data, e))
					rc = False
			if breakdown.detail in Detail.Hardbounces or (breakdown.detail == Detail.Internal and breakdown.bounce_type is not None and breakdown.bounce_remark is not None):
				if breakdown.bounce_type == UserStatus.BLACKLIST:
					self.blcount += 1
				else:
					self.hbcount += 1
				try:
					if breakdown.mailloop_remark is not None:
						query = 'DELETE FROM success_%d_tbl WHERE customer_id = :customerID AND mailing_id = :mailing_id' % company
						data = {
							'customerID': customer,
							'mailing_id': mailing
						}
						if db.update (query, data, commit = True) == 1:
							self.rvcount += 1
					query = (
						'UPDATE customer_%d_binding_tbl '
						'SET user_status = :status, timestamp = :ts, user_remark = :remark, exit_mailing_id = :mailing '
						'WHERE customer_id = :customer AND user_status = 1 AND mediatype = :media'
						% company
					)
					data = {
						'status': breakdown.bounce_type.value,
						'remark': breakdown.bounce_remark,
						'mailing': mailing,
						'ts': breakdown.timestamp,
						'customer': customer,
						'media': media
					}
					db.update (query, data, commit = True)
				except error as e:
					logger.error ('Unable to unsubscribe %r for company %d from database using %s: %s' % (data, company, query, e))
					rc = False
			elif breakdown.detail in Detail.Softbounces:
				self.sbcount += 1
				if self.sbcount % 1000 == 0:
					db.sync ()
		else:
			self.igcount += 1
		return rc
#}}}
class UpdateAccount (Update): #{{{
	__slots__ = ['tscheck', 'ignored', 'inserted', 'bccs', 'failed', 'status', 'changed', 'tracker']
	name = 'account'
	path = os.path.join (base, 'log', 'account.log')
	status_template: Final[str] = os.path.join (base, 'scripts', 'mailstatus3.tmpl')
	track_path: Final[str] = os.path.join (base, 'var', 'run', 'update-account.track')
	track_section_mailing: Final[str] = 'mailing'
	track_section_status: Final[str] = 'status'
	@dataclass
	class Mailinfo:
		dirty: bool = True
		active: bool = True
		status_id: int = 0
		mailing_id: int = 0
		in_production: bool = True
		produced_mails: int = 0
		created_mails: int = 0
		skiped_mails: int = 0
		send_start: int = 0
		send_last: int = 0
		send_count: int = 0
		mail_receiver: str = ''
		mail_sent: bool = False
		mail_percent: int = 50
		def __hash__ (self) -> int:
			return hash ((self.status_id, self.mailing_id))

	def __init__ (self) -> None:
		super ().__init__ ()
		self.tracker_age = '90d'
		self.tscheck = re.compile ('^([0-9]{4})-([0-9]{2})-([0-9]{2}):([0-9]{2}):([0-9]{2}):([0-9]{2})$')
		self.ignored = 0
		self.inserted = 0
		self.bccs = 0
		self.failed = 0
		self.status: Dict[int, UpdateAccount.Mailinfo] = {}
		self.changed: Set[UpdateAccount.Mailinfo] = set ()
	
	def __mailing_status (self, db: DB, status_id: int, count: int, skip: int, ts: int) -> None:
		try:
			minfo = self.status[status_id]
			if not minfo.active:
				logger.debug ('Found inactive mailing %d for new accounting information' % minfo.mailing_id)
			elif not minfo.mailing_id:
				logger.debug ('Found unset mailing_id for new account information with status %d' % status_id)
			else:
				logger.debug ('Found active record with mailing_id %d' % minfo.mailing_id)
			minfo.skiped_mails += skip
			minfo.send_start = max (minfo.send_start, ts)
			minfo.send_last = max (minfo.send_last, ts)
			minfo.send_count += count
			minfo.dirty = True
		except KeyError:
			minfo = UpdateAccount.Mailinfo (
				status_id = status_id,
				skiped_mails = skip,
				send_start = ts,
				send_last = ts,
				send_count = count
			)
			with Ignore (KeyError):
				track = self.tracker[Key (UpdateAccount.track_section_mailing, str (minfo.status_id))]
				minfo.produced_mails = track['produced']
				minfo.skiped_mails += track['skiped']
				minfo.send_count += track['count']
				logger.debug (f'Loaded entry {track!r}')
			db.sync ()
			for r in db.query ('SELECT mailing_id FROM maildrop_status_tbl WHERE status_id = :status', { 'status': status_id }):
				minfo.mailing_id = r[0]
				break
			if minfo.mailing_id:
				for r in db.queryc ('SELECT statmail_recp, deleted FROM mailing_tbl WHERE mailing_id = :mid', {'mid': minfo.mailing_id}):
					if r[0] is not None:
						receiver = r[0].strip ()
						try:
							(percent, nreceiver) = receiver.split ('%', 1)
							minfo.mail_receiver = nreceiver.strip ()
							minfo.mail_percent = int (percent)
							if minfo.mail_percent < 0 or minfo.mail_percent > 100:
								raise ValueError ('percent value out of range')
						except ValueError:
							minfo.mail_receiver = receiver
					if r[1]:
						logger.info ('Mailing with ID %d is marked as deleted, set to inactive' % minfo.mailing_id)
						minfo.active = False
				db.update ('UPDATE mailing_tbl SET work_status = \'mailing.status.sending\' WHERE mailing_id = :mid AND (work_status IS NULL OR work_status != \'mailing.status.sending\')', { 'mid': minfo.mailing_id }, commit = True)
				logger.debug ('Created new record for status_id %d with mailing_id %d' % (minfo.status_id, minfo.mailing_id))
			else:
				logger.debug ('Created new record for status_id %d with no assigned mailing_id' % status_id)
			self.status[status_id] = minfo
		#
		if minfo.active and minfo.mailing_id:
			self.changed.add (minfo)

	def __mailing_send_status (self, db: DB, minfo: UpdateAccount.Mailinfo) -> None:
		try:
			with open (UpdateAccount.status_template) as fd:
				template = fd.read ()
		except IOError as e:
			logger.warning ('Failed to read template %s: %s' % (self.status_template, e))
		else:
			sender = syscfg.get ('status-sender')
			if minfo.send_last == minfo.send_start:
				last = int (time.time ())
			else:
				last = minfo.send_last
			diff = ((last - minfo.send_start) * minfo.created_mails) / minfo.send_count
			end = minfo.send_start + diff
			start = time.localtime (minfo.send_start)
			then = time.localtime (end)
			rc = db.querys ('SELECT shortname, company_id FROM mailing_tbl WHERE mailing_id = :mid', {'mid': minfo.mailing_id})
			if not rc is None and not None in rc:
				mailingName = rc.shortname
				company_id = rc.company_id
			else:
				mailingName = ''
				company_id = 0
			receiver = [_r for _r in minfo.mail_receiver.split () if _r]
			ns = {
				'sender': sender,
				'receiver': ', '.join (receiver),
				'current': minfo.send_count,
				'count': minfo.created_mails,
				'start': datetime (start[0], start[1], start[2], start[3], start[4], start[5]),
				'end': datetime (then[0], then[1], then[2], then[3], then[4], then[5]),
				'mailing_id': minfo.mailing_id,
				'mailingName': mailingName,
				'company_id': company_id,

				'format': lambda a: f'{a:,d}'
			}
			tmpl = Template (template)
			try:
				email = tmpl.fill (ns)
			except error as e:
				logger.warning ('Failed to fill template %s: %s' % (UpdateAccount.status_template, e))
			else:
				mail = EMail ()
				if sender:
					mail.set_sender (sender)
				isTo = True
				for r in receiver:
					if isTo:
						mail.add_to (r)
						isTo = False
					else:
						mail.add_cc (r)
				charset = tmpl.property ('charset')
				if charset:
					mail.set_charset (charset)
				subject = tmpl.property ('subject')
				if not subject:
					subject = tmpl['subject']
					if not subject:
						subject = 'Status report for mailing %d [%s]' % (minfo.mailing_id, mailingName)
				else:
					tmpl = Template (subject)
					subject = tmpl.fill (ns)
				mail.set_subject (subject)
				mail.set_text (email)
				st = mail.send_mail ()
				if not st[0]:
					logger.warning ('Failed to send status mail to %s: [%s/%s]' % (minfo.mail_receiver, st[2].strip (), st[3].strip ()))
				else:
					logger.info ('Status mail for %s (%d) sent to %s' % (mailingName, minfo.mailing_id, minfo.mail_receiver))

	def __mailing_reached (self, minfo: UpdateAccount.Mailinfo) -> bool:
		if minfo.mail_percent == 100:
			return minfo.created_mails <= minfo.send_count
		return int (float (minfo.created_mails * minfo.mail_percent) / 100.0) <= minfo.send_count

	def __mailing_summary (self, db: DB) -> None:
		for minfo in self.status.values ():
			if minfo.in_production and minfo.active:
				self.changed.add (minfo)
		if self.changed:
			db.sync ()
			for minfo in self.changed:
				if minfo.in_production:
					for r in db.queryc ('SELECT genstatus FROM maildrop_status_tbl WHERE status_id = :status', { 'status': minfo.status_id }):
						if r.genstatus == 3:
							for r in db.query ('SELECT total_mails FROM mailing_backend_log_tbl WHERE status_id = :status', { 'status': minfo.status_id }):
								minfo.in_production = False
								minfo.produced_mails = r.total_mails
								logger.debug ('Changed status for mailing_id %d from production to finished with %d mails produced' % (minfo.mailing_id, minfo.produced_mails))
								break
						break
					if minfo.in_production:
						for r in db.queryc ('SELECT deleted FROM mailing_tbl WHERE mailing_id = :mailing_id', {'mailing_id': minfo.mailing_id}):
							if r.deleted:
								logger.info ('Mailing with ID %d had been deleted, mark as inactive' % minfo.mailing_id)
								minfo.active = False
				if not minfo.in_production:
					minfo.created_mails = minfo.produced_mails - minfo.skiped_mails
					if minfo.mail_receiver and not minfo.mail_sent:
						if self.__mailing_reached (minfo):
							key = Key (UpdateAccount.track_section_status, str (minfo.mailing_id))
							if key not in self.tracker:
								try:
									self.__mailing_send_status (db, minfo)
								except Exception as e:
									logger.exception ('Failed to send status for %s (%d): %s' % (minfo, minfo.mailing_id, e))
								self.tracker[key] = {'sent': int (time.time ()), 'receiver': minfo.mail_receiver}
							minfo.mail_sent = True
					for r in db.query ('SELECT sum(no_of_mailings) FROM mailing_account_tbl WHERE mailing_id = :mid AND status_field = \'W\'', { 'mid': minfo.mailing_id }):
						if r[0] == minfo.created_mails:
							if db.update ('UPDATE mailing_tbl SET work_status = \'mailing.status.sent\' WHERE mailing_id = :mid', { 'mid': minfo.mailing_id }, commit = True) == 1:
								logger.info ('Changed work status for mailing_id %d to sent' % minfo.mailing_id)
							else:
								logger.error ('Failed to change work status for mailing_id %d to sent: %s' % (minfo.mailing_id, db.last_error ()))
							minfo.active = False
							logger.debug ('Changed status for mailing_id %d from active to inactive' % minfo.mailing_id)
						else:
							logger.debug ('Mailing %d has currently %d out of %d sent mails' % (minfo.mailing_id, r[0], minfo.created_mails))
						break
		#
		for minfo in self.status.values ():
			if minfo.dirty:
				self.tracker[Key (UpdateAccount.track_section_mailing, str (minfo.status_id))] = {
					'produced': minfo.produced_mails,
					'skiped': minfo.skiped_mails,
					'count': minfo.send_count
				}
				minfo.dirty = False
				logger.debug (f'Saved entry {minfo}')

	def update_start (self, db: DB) -> bool:
		self.ignored = 0
		self.inserted = 0
		self.bccs = 0
		self.failed = 0
		self.changed.clear ()
		return True

	def update_end (self, db: DB) -> bool:
		logger.info ('Insert %d (%d bccs), failed %d, ignored %d records in %d lines' % (self.inserted, self.bccs, self.failed, self.ignored, self.lineno))
		self.__mailing_summary (db)
		self.changed.clear ()
		return True

	@dataclass
	class BCCCount:
		mail_count: int = 0
		byte_count: int = 0
	def update_line (self, db: DB, line: str) -> bool:
		sql = 'INSERT INTO mailing_account_tbl ('
		values = 'VALUES ('
		sep = ''
		if db.dbms == 'oracle':
			sql += 'mailing_account_id'
			values += 'mailing_account_tbl_seq.nextval'
			sep = ', '
		timestamp = datetime.now ()
		data: Dict[str, Any] = {}
		ignore = False
		skip = 0
		record: Dict[str, str] = {}
		bcc = UpdateAccount.BCCCount ()
		for tok in line.split ():
			tup = tok.split ('=', 1)
			if len (tup) == 2:
				name = None
				(var, val) = tup
				record[var] = val
				if var == 'licence':
					name = 'licence_id'
					try:
						licence_id = int (val)
						if licence_id != 0 and licence_id != licence:
							ignore = True
					except ValueError:
						ignore = True
					name = None
				elif var == 'company':
					name = 'company_id'
				elif var == 'mailinglist':
					name = 'mailinglist_id'
				elif var == 'mailing':
					name = 'mailing_id'
				elif var == 'maildrop':
					name = 'maildrop_id'
				elif var == 'status_field':
					name = 'status_field'
				elif var == 'mediatype':
					name = 'mediatype'
				elif var in ('mailtype', 'subtype'):
					name = 'mailtype'
				elif var == 'count':
					name = 'no_of_mailings'
				elif var == 'bytes':
					name = 'no_of_bytes'
				elif var == 'skip':
					try:
						skip = int (val)
					except ValueError:
						logger.warning ('Failed to parse skip count %r' % (val, ))
				elif var == 'block':
					name = 'blocknr'
				elif var == 'mailer':
					name = 'mailer'
				elif var == 'timestamp':
					m = self.tscheck.match (val)
					if m is not None:
						ts = [int (_g) for _g in m.groups ()]
						timestamp = datetime (ts[0], ts[1], ts[2], ts[3], ts[4], ts[5])
				elif var == 'bcc-count':
					bcc.mail_count = atoi (val)
				elif var == 'bcc-bytes':
					bcc.byte_count = atoi (val)
				if not name is None:
					sql += '%s%s' % (sep, name)
					values += '%s:%s' % (sep, name)
					sep = ', '
					data[name] = val
		sql += '%stimestamp) %s%s:timestamp)' % (sep, values, sep)
		data['timestamp'] = timestamp
		
		rc = True
		if not ignore:
			try:
				db.update (sql, data, commit = True)
				self.inserted += 1
				try:
					if record['status_field'] == 'W':
						status_id = int (record['maildrop'])
						count = int (record['count'])
						self.__mailing_status (db, status_id, count, skip, int (timestamp.timestamp ()))
				except (KeyError, ValueError) as e:
					logger.warning ('Failed to track %s: %s' % (line, e))
				if bcc.mail_count > 0:
					stmt = db.qselect (
						oracle = 'INSERT INTO bcc_mailing_account_tbl (mailing_account_id, no_of_mailings, no_of_bytes) VALUES (mailing_account_tbl_seq.currval, :bcc_count, :bcc_bytes)',
						mysql = 'INSERT INTO bcc_mailing_account_tbl (mailing_account_id, no_of_mailings, no_of_bytes) VALUES (last_insert_id(), :bcc_count, :bcc_bytes)'
					)
					db.update (
						stmt,
						{
							'bcc_count': bcc.mail_count,
							'bcc_bytes': bcc.byte_count
						},
						commit = True
					)
					self.bccs += 1
			except error as e:
				logger.error ('Failed to insert %s into database: %s' % (line, e))
				rc = False
				self.failed += 1
		else:
			self.ignored += 1
		return rc
#}}}
class UpdateDeliver (Update): #{{{
	__slots__ = ['timestamp_parser', 'parser', 'existing_deliver_tables', 'mailings_to_companies', 'count']
	name = 'deliver'
	path = os.path.join (base, 'log', 'deliver.log')
	def __init__ (self) -> None:
		super ().__init__ ()
		self.timestamp_parser = ParseTimestamp ()
		self.parser = Lineparser (
			lambda a: a.split (';', 4),
			Field ('licence_id', int),
			Field ('mailing_id', int),
			Field ('customer_id', int),
			Field ('timestamp', lambda n: self.timestamp_parser (n)),
			'line'
		)
		self.existing_deliver_tables: Set[str] = set ()
		self.mailings_to_companies: Dict[int, int] = {}
		self.count = 0

	def update_start (self, db: DB) -> bool:
		self.count = 0
		return True
		
	def update_end (self, db: DB) -> bool:
		logger.info (f'Added {self.count:,d} out of {self.lineno:,d} new lines')
		return True
	
	def update_line (self, db: DB, line: str) -> bool:
		try:
			record = self.parser (line)
			if record.licence_id != licence:
				logger.debug (f'{record.licence_id}: ignore foreign licence id (own is {licence})')
			else:
				try:
					company_id = self.mailings_to_companies[record.mailing_id]
				except KeyError:
					rq = db.querys (
						'SELECT company_id FROM mailing_tbl WHERE mailing_id = :mailing_id',
						{'mailing_id': record.mailing_id}
					)
					company_id = self.mailings_to_companies[record.mailing_id] = rq.company_id if rq is not None else None
					if company_id is None:
						logger.info ('mailing {mailing_id}: not found in databsae'.format (mailing_id = record.mailing_id))
				if company_id is not None:
					table = 'deliver_{company_id}_tbl'.format (company_id = company_id)
					if table not in self.existing_deliver_tables:
						if not db.exists (table):
							if db.dbms == 'oracle':
								tablespace = db.find_tablespace ('DATA_SUCCESS')
								db.execute (
									'CREATE TABLE {table} ('
									'id number primary key,'
									'mailing_id number,'
									'customer_id number,'
									'timestamp date,'
									'line varchar2(4000)'
									'){tablespace}'
									.format (table = table, tablespace = (' TABLESPACE {tablespace}'.format (tablespace = tablespace)) if tablespace else '')
								)
								db.execute (
									'CREATE SEQUENCE {table}_seq NOCACHE'.format (table = table)
								)
							else:
								db.execute (
									'CREATE TABLE {table} ('
									'id int auto_increment primary key,'
									'mailing_id int,'
									'customer_id int,'
									'timestamp datetime,'
									'line varchar(4000)'
									')'
									.format (table = table)
								)
						self.existing_deliver_tables.add (table)
					self.count += db.update (db.qselect (
						oracle = (
							'INSERT INTO {table} '
							'       (id, mailing_id, customer_id, timestamp, line) '
							'VALUES '
							'       ({table}_seq.nextval, :mailing_id, :customer_id, :timestamp, :line)'
							.format (table = table)
						), mysql = (
							'INSERT INTO {table} '
							'       (mailing_id, customer_id, timestamp, line) '
							'VALUES '
							'       (:mailing_id, :customer_id, :timestamp, :line)'
							.format (table = table)
						)),
						{
							'mailing_id': record.mailing_id,
							'customer_id': record.customer_id,
							'timestamp': record.timestamp,
							'line': record.line
						}
					)
		except Exception as e:
			logger.warning (f'{line}: invalid line: {e}')
			return False
		else:
			return True
#}}}
class UpdateMailtrack (Update): #{{{
	__slots__ = ['mailtrack_process_table', 'companies', 'count', 'parser', 'insert_statement']
	name = 'mailtrack'
	path = os.path.join (base, 'log', 'mailtrack.log')
	mailtrack_process_table_default = 'mailtrack_process_tbl'
	mailtrack_config_key = 'mailtrack-extended'
	@dataclass
	class CompanyCounter:
		count: int = 0
		mailings: Set[int] = field (default_factory = set)

	def __init__ (self) -> None:
		super ().__init__ ()
		self.mailtrack_process_table = syscfg.get_str ('mailtrack-process-table', UpdateMailtrack.mailtrack_process_table_default)
		self.companies: DefaultDict[int, UpdateMailtrack.CompanyCounter] = defaultdict (UpdateMailtrack.CompanyCounter)
		self.count = 0
		self.parser = Lineparser (
			lambda a: a.split (';', 6),
			'id',
			Field ('timestamp', lambda n: datetime.fromtimestamp (int (n))),
			Field ('licence_id', int),
			Field ('company_id', int),
			Field ('mailing_id', int),
			Field ('maildrop_status_id', int),
			Field ('customer_ids', lambda n: [int (_n) for _n in n.split (',') if _n])
		)
		self.insert_statement = (
			'INSERT INTO {table} '
			'       (company_id, mailing_id, maildrop_status_id, customer_id, timestamp) '
			'VALUES '
			'       (:company_id, :mailing_id, :maildrop_status_id, :customer_id, :timestamp)'
			.format (table = self.mailtrack_process_table)
		)
		with DB () as db:
			if not db.exists (self.mailtrack_process_table):
				tablespace = db.find_tablespace ('DATA_TEMP')
				tablespace_expr = (' TABLESPACE %s' % tablespace) if tablespace else ''
				db.execute (db.qselect (
					oracle = (
						'CREATE TABLE {table} (\n'
						'	company_id		number,\n'
						'	mailing_id		number,\n'
						'	maildrop_status_id	number,\n'
						'	customer_id		number,\n'
						'	timestamp		date\n'
						'){tablespace}'
						.format (table = self.mailtrack_process_table, tablespace = tablespace_expr)
					), mysql = (
						'CREATE TABLE {table} (\n'
						'	company_id		int(11),\n'
						'	mailing_id		int(11),\n'
						'	maildrop_status_id	int(11),\n'
						'	customer_id		integer unsigned,\n'
						'	timestamp		timestamp\n'
						')'
						.format (table = self.mailtrack_process_table)
					)
				))
				mailtrack_index_prefix = syscfg.get_str ('mailtrack-process-index-prefix', 'mtproc')
				for (index_id, index_column) in [
					('cid', 'company_id'),
					('cuid', 'customer_id'),
					('tstamp', 'timestamp')
				]:
					db.execute (db.qselect (
						oracle = (
							'CREATE INDEX {prefix}${id}$idx '
							'ON {table} ({column}){tablespace}'
							.format (prefix = mailtrack_index_prefix, id = index_id, column = index_column, table = self.mailtrack_process_table, tablespace = tablespace_expr)
						), mysql = (
							'CREATE INDEX {prefix}${id}$idx '
							'ON {table} ({column})'
							.format (prefix = mailtrack_index_prefix, id = index_id, column = index_column, table = self.mailtrack_process_table)
						)
					))
		
	def update_start (self, db: DB) -> bool:
		db.execute ('TRUNCATE TABLE %s' % self.mailtrack_process_table)
		db.sync ()
		self.companies.clear ()
		self.count = 0
		return True

	def update_end (self, db: DB) -> bool:
		if self.companies:
			ccfg = CompanyConfig (db = db)
			ccfg.read ()
			for (company_id, counter) in sorted (self.companies.items ()):
				try:
					active = ccfg.get_company_info (self.mailtrack_config_key, company_id = company_id)
				except KeyError:
					logger.debug ('%s: no value set for company %d, do not process entries' % (self.mailtrack_config_key, company_id))
					continue
				else:
					if not atob (active):
						logger.debug ('%s: value %s set for company %d results to disable processing' % (self.mailtrack_config_key, active, company_id))
						continue
				#
				logger.info (f'{company_id}: processing {counter} update mailtracking')
				self.__update_mailtracking (db, company_id, counter)
				logger.info (f'{company_id}: processing profile updates')
				self.__update_profile (db, company_id, counter)
				db.sync ()
				logger.info (f'{company_id}: done')
		logger.info ('Added mailtracking for {count} companies'.format (count = len (self.companies)))
		return True	

	def update_line (self, db: DB, line: str) -> bool:
		try:
			record = self.parser (line)
			if record.licence_id != licence:
				logger.debug (f'{record.licence_id}: ignore foreign licence id (own is {licence})')
			elif record.customer_ids:
				data = {
					'timestamp': record.timestamp,
					'company_id': record.company_id,
					'mailing_id': record.mailing_id,
					'maildrop_status_id': record.maildrop_status_id
				}
				for customer_id in record.customer_ids:
					data['customer_id'] = customer_id
					db.update (self.insert_statement, data)
					self.count += 1
					if self.count % 10000 == 0:
						db.sync ()
						logger.info (f'now at #{self.count:,d}')
				company = self.companies[record.company_id]
				company.count += len (record.customer_ids)
				company.mailings.add (record.mailing_id)
		except Exception as e:
			logger.warning (f'{line}: invalid line: {e}')
			return False
		else:
			return True

	def __update_mailtracking (self, db: DB, company_id: int, counter: UpdateMailtrack.CompanyCounter) -> None:
		rq = db.querys ('SELECT mailtracking FROM company_tbl WHERE company_id = :company_id', {'company_id': company_id})
		if rq is not None and rq.mailtracking:
			mailtrack_table = 'mailtrack_{company_id}_tbl'.format (company_id = company_id)
			if db.exists (mailtrack_table):
				with db.request () as cursor:
					count = cursor.update (
						'INSERT INTO {mailtrack_table} '
						'       (mailing_id, maildrop_status_id, customer_id, timestamp) '
						'SELECT mailing_id, maildrop_status_id, customer_id, timestamp '
						'FROM {table} WHERE company_id = :company_id'
						.format (mailtrack_table = mailtrack_table, table = self.mailtrack_process_table),
						{
							'company_id': company_id
						},
						commit = True
					)
					if count == counter.count:
						logger.debug (f'{company_id}: inserted {count:,d} in {mailtrack_table} as expected')
					else:
						logger.error (f'{company_id}: inserted {count:,d} in {mailtrack_table}, but expected {counter.count:,d}')
			else:
				logger.error ('%d: missing mailtrack table %s' % (company_id, mailtrack_table))
		else:
			logger.debug ('%d: mailtracking is disabled' % company_id)

	def __update_profile (self, db: DB, company_id: int, counter: UpdateMailtrack.CompanyCounter) -> None:
		customer_table = 'customer_{company_id}_tbl'.format (company_id = company_id)
		lastsend_date = 'lastsend_date'
		if not db.exists (customer_table):
			logger.error ('%d: missing customer table %s' % (company_id, customer_table))
			return
		#
		layout = db.layout (customer_table, normalize = True)
		columns = set (_l.name for _l in layout)
		if lastsend_date in columns:
			logger.info (f'{company_id}: update for {lastsend_date} in {customer_table} started')
			count = 0
			query = (
				'UPDATE {table} '
				'SET {lastsend_date} = :{lastsend_date} '
				'WHERE customer_id = :customer_id AND ({lastsend_date} IS NULL OR {lastsend_date} < :{lastsend_date})'
				.format (table = customer_table, lastsend_date = lastsend_date)
			)
			with db.request () as cursor:
				for (row_count, row) in enumerate (db.query (
					'SELECT customer_id, timestamp '
					'FROM {table} '
					'WHERE company_id = :company_id'
					.format (table = self.mailtrack_process_table),
					{
						'company_id': company_id
					}
				), start = 1):
					count += cursor.update (
						query,
						{
							'customer_id': row.customer_id,
							lastsend_date: row.timestamp
						}
					)
					if row_count % 10000 == 0:
						logger.info (f'Now at #{row_count:,d} of #{counter.count:,d}')
						cursor.sync ()
				cursor.sync ()
			logger.info (f'{company_id}: update for {count:,d} {lastsend_date} in {customer_table} while having sent {counter.count:,d}')
		else:
			logger.info (f'{company_id}: no {lastsend_date} in database layout found')
#}}}
#
class Main (Runtime):
	def supports (self, option: str) -> bool:
		return option != 'dryrun'

	def prepare (self) -> None:
		if self.single:
			self.ctx.watchdog = False
		else:
			self.ctx.watchdog = True
			
	def add_arguments (self, parser: argparse.ArgumentParser) -> None:
		parser.add_argument (
			'-S', '--single', action = 'store_true',
			help = 'Execute in a single run without for each module without forking subprocesses'
		)
		parser.add_argument (
			'-D', '--delay', action = 'store', type = int, default = 30,
			help = 'Delay in seconds between scan for new files to process'
		)
		
	def use_arguments (self, args: argparse.Namespace) -> None:
		self.single = args.single
		self.delay = args.delay
		available = dict ((_c.name, _c) for _c in globals ().values () if type (_c) is type and issubclass (_c, Update) and _c is not Update)
		self.modules: List[Type[Update]] = [available[_m] for _m in args.parameter]
		if self.single:
			self.ctx.background = False
			self.ctx.watchdog = False

	def start_update (self, update_class: Type[Update]) -> bool:
		self.ctx.processtitle (update_class.name)
		with log (update_class.name):
			upd = update_class ()
			upd.execute (lambda: self.running, self.delay)
		return True

	def executors (self) -> Optional[List[Callable[[], bool]]]:
		if not self.single:
			executors: List[Callable[[], bool]] = []
			for module in self.modules:
				to_execute = partial (self.start_update, module)
				setattr (to_execute, '__name__', module.name)
				executors.append (to_execute)
			return executors
		return None

	def executor (self) -> bool:
		if self.single:
			for module in self.modules:
				upd = module ()
				upd.execute (lambda: True, None)
			return True
		return False
#
if __name__ == '__main__':
	Main.main ()
