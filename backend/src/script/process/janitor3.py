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
import	logging, argparse
import	sys, os, time, re, stat
import	functools
from	datetime import datetime
from	dataclasses import dataclass
from	typing import Any, Callable, Optional
from	typing import List, NamedTuple
import	agn3.janitor
from	agn3.definitions import base
from	agn3.emm.metafile import METAFile
from	agn3.ignore import Ignore
from	agn3.io import create_path
from	agn3.log import log
from	agn3.runtime import CLI
from	agn3.stream import Stream
#
logger = logging.getLogger (__name__)
#
def partial (function: Callable[..., Any], *args: Any, **kws: Any) -> Callable[[], Any]:
	return functools.partial (function, *args, **kws)
	
Rule = agn3.janitor.Janitor.Rule
class Janitor (agn3.janitor.Janitor): #{{{
	__slots__ = ['to_execute']
	service = 'janitor'
	class Entry (NamedTuple):
		description: str
		method: Callable[[], None]
	def __init__ (self) -> None:
		super ().__init__ (self.__class__.service)
		self.to_execute: List[Janitor.Entry] = []
	
	def add (self, description: str, method: Callable[[], None]) -> None:
		self.to_execute.append (Janitor.Entry (description, method))

	def execute (self) -> None:
		if self.to_execute:
			logger.info ('Starting janitor')
			for entry in self.to_execute:
				logger.info (entry.description)
				try:
					entry.method ()
				except Exception as e:
					logger.exception ('%s failed: %s' % (entry.description, e))
			logger.info ('Janitor done')

	def cleanup_mailspool (self, compress_after_days: int, remove_after_days: int) -> None:
		spath = os.path.join (base, 'var', 'spool', 'mail', 'store')
		if os.path.isdir (spath):
			self.cleanup_timestamp_files (
				spath,
				[Rule (None, compress_after_days)],
				[Rule (None, remove_after_days)]
			)
	
	def cleanup_fsdb (self, remove_after_days: int) -> None:
		expire = time.time () - (remove_after_days * 24 * 60 * 60)
		@dataclass
		class Stats:
			files: int = 0
			directories: int = 0
		stats = Stats ()
		def remover (basepath: str) -> bool:
			with Ignore (OSError):
				files = []
				directories = []
				count = 0
				for filename in os.listdir (basepath):
					count += 1
					path = os.path.join (basepath, filename)
					st = os.lstat (path)
					if stat.S_ISREG (st.st_mode):
						if st.st_ctime < expire and st.st_mtime < expire:
							files.append (path)
					elif stat.S_ISDIR (st.st_mode):
						directories.append (path)
				for path in files:
					with Ignore (OSError):
						if self.doit:
							os.unlink (path)
						else:
							self.show ('REMOVE: %s' % path)
							stats.files += 1
						count -= 1
				for path in directories:
					if remover (path):
						with Ignore (OSError):
							if self.doit:
								os.rmdir (path)
							else:
								self.show ('RMDIR: %s' % path)
							count -= 1
							stats.directories += 1
				return count == 0
			return False
		remover (os.path.join (base, 'var', 'fsdb'))
		logger.info ('fsdb: remove %d files and %d empty directories' % (stats.files, stats.directories))
#}}}
class OpenEMM (Janitor): #{{{
	__slots__: List[str] = []
	service = 'openemm'
	def __init__ (self) -> None:
		super ().__init__ ()
		for (description, method) in [
			('Cleanup log/done', self.__cleanup_log_done),
			('Cleanup spool', self.__cleanup_spool),
			('Cleanup filter', self.__cleanup_filter),
			('Cleanup Logfiles', self.__cleanup_logfiles),
			('Cleanup mailspool', partial (self.cleanup_mailspool, 2, 4)),
			('Cleanup FSDB', partial (self.cleanup_fsdb, 7))
		]:
			self.add (description, method)
		
	def __cleanup_log_done (self) -> None:
		def compare_log_done (path: str, fname: str, st: Optional[os.stat_result]) -> bool:
			return self.mktimestamp (fname) + 30 < self.today
		for path in self.select (os.path.join (base, 'log', 'done'), False, compare_log_done)[0]:
			self.remove (path)

	def __select_xml (self, path: str, offset: int, all_extensions: bool) -> List[str]:
		dt = time.localtime (time.time () - offset * 24 * 60 * 60)
		ts = '%04d%02d%02d' % (dt.tm_year, dt.tm_mon, dt.tm_mday)
		def selector (path: str, fname: str, st: Optional[os.stat_result]) -> bool:
			m = METAFile (fname)
			if m.valid and (all_extensions or m.extension == 'xml'):
				chk = m.timestamp[:8]
				return chk < ts
			return False
		return self.select (path, False, selector)[0]
		
	def __cleanup_spool (self) -> None:
		meta = os.path.join (base, 'var', 'spool', 'META')
		deleted = os.path.join (base, 'var', 'spool', 'DELETED')
		def show_count (cnt: List[int], s: str) -> None:
			logger.info ('%d files found, %d successful, %d failed: %s' % (sum (cnt), cnt[0], cnt[1], s))
		#
		count = [0, 0]
		for path in self.__select_xml (meta, 30, True):
			if self.remove (path):
				count[0] += 1
			else:
				count[1] += 1
		show_count (count, 'removed from %s' % meta)
		#
		count = [0, 0]
		for path in self.__select_xml (deleted, 30, True):
			if self.remove (path):
				count[0] += 1
			else:
				count[1] += 1
		show_count (count, 'removed from %s' % deleted)
		#
		archive = os.path.join (base, 'var', 'spool', 'ARCHIVE')
		if os.path.isdir (archive):
			self.cleanup_timestamp_directories (archive, 7, 30)
		
	__filePattern = re.compile ('^[a-z0-9]+-[a-z0-9]+$', re.IGNORECASE)
	def __cleanup_filter (self) -> None:
		fpath = os.path.join (base, 'var', 'spool', 'filter')
		if os.path.isdir (fpath):
			yday = datetime.now ().fromordinal (self.today - 1)
			apath = os.path.join (fpath, '%04d%02d%02d' % (yday.year, yday.month, yday.day))
			if not os.path.isdir (apath):
				create_path (apath)
				for fname in [_f for _f in os.listdir (fpath) if self.__filePattern.match (_f)]:
					path = os.path.join (fpath, fname)
					if os.path.isfile (path):
						self.move (path, os.path.join (apath, fname))
				self.compress ([os.path.join (apath, _f) for _f in os.listdir (apath)])
			self.cleanup_timestamp_directories (fpath, 30, 180)

	def __cleanup_logfiles (self) -> None:
		self.cleanup_logfiles (
			[Rule (None, 2)],
			[Rule (re.compile ('.*-account\\.log.*'), 180), Rule (None, 90)]
		)
#}}}
class Main (CLI):
	__slots__ = ['dryrun', 'services']
	def add_arguments (self, parser: argparse.ArgumentParser) -> None:
		parser.add_argument (
			'-v', '--verbose', action = 'store_true',
			help = 'Enable verbose logging to logfile and output'
		)
		parser.add_argument (
			'-n', '--dryrun', action = 'store_true',
			help = 'Just show, but do not execute any cleanup'
		)
		parser.add_argument (
			'services', nargs = '+',
			help = 'services to run janitor for'
		)
	
	def use_arguments (self, args: argparse.Namespace) -> None:
		if args.verbose:
			log.loglevel = logging.DEBUG
			log.outlevel = logging.DEBUG
			log.outstream = sys.stderr
		self.dryrun = args.dryrun
		self.services = args.services
	
	def executor (self) -> bool:
		available = (Stream (globals ().values ())
			.filter (lambda t: type (t) is type and issubclass (t, Janitor) and t is not Janitor)
			.map (lambda t: (t.service, t))
			.dict ()
		)
		for service in self.services:
			if service not in available:
				logger.error ('{service}: not known, available are {services}'.format (
					service = service,
					services = Stream (self.services).sorted ().join (', ')
				))
				return False
		for service in self.services:
			available[service] ().run (not self.dryrun)
		return True
#
if __name__ == '__main__':
	Main.main ()
