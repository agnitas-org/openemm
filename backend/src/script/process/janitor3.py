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
import	sys, os, time, re, stat, errno
import	functools, shutil
from	datetime import datetime
from	dataclasses import dataclass
from	typing import Any, Callable, Optional
from	typing import List, NamedTuple, Tuple
import	agn3.janitor
from	agn3.definitions import base, syscfg
from	agn3.emm.metafile import METAFile
from	agn3.ignore import Ignore
from	agn3.io import create_path
from	agn3.log import log
from	agn3.runtime import CLI
from	agn3.stream import Stream
#
logger = logging.getLogger (__name__)
#
def partial (function: Callable[..., Any], *args: Any, **kwargs: Any) -> Callable[[], Any]:
	return functools.partial (function, *args, **kwargs)

Rule = agn3.janitor.Janitor.Rule
class Janitor (agn3.janitor.Janitor):
	__slots__ = ['to_execute']
	service = 'janitor'
	class Entry (NamedTuple):
		description: str
		method: Callable[[], None]
	def __init__ (self) -> None:
		super ().__init__ (self.__class__.service)
		self.to_execute: List[Janitor.Entry] = []
		tasks: List[Tuple[str, Callable[[], None]]] = [
			('Cleanup Logfiles', partial (self.cleanup_logfiles, [Rule (None, 2)], [Rule (re.compile ('.*-account\\.log.*'), 120), Rule (None, 40)])),
			('Cleanup FSDB', partial (self.cleanup_fsdb, 7)),
			('Cleanup outdated virtual environments', partial (self.cleanup_venv, 30))
		]
		if syscfg.service ('merger', 'openemm'):
			tasks += [
				('Cleanup log/done', self.cleanup_log_done),
				('Cleanup spool', self.cleanup_spool)
			]
		if syscfg.service ('mailer', 'openemm'):
			tasks += [
				('Cleanup Archive', self.cleanup_mail_archive)
			]
		if syscfg.service ('mailloop', 'openemm'):
			tasks += [
				('Cleanup filter', self.cleanup_filter)
			]
		if syscfg.service ('merger', 'mailer', 'mailloop', 'openemm'):
			tasks += [
				('Cleanup Mailspool', partial (self.cleanup_mailspool, 2, 4)),
			]
		#
		for (description, method) in tasks:
			self.add (description, method)
	
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

	def cleanup_venv (self, remove_after_days: int) -> None:
		mark_file = '.marked-for-removal'
		pattern = re.compile ('^{home}/\\.venv(\\.[0-9]+){{3}}[^/]*$'.format (home = re.escape (base)))
		expire = time.time () - remove_after_days * 24 * 60 * 60
		with Ignore (NameError):
			if os.path.isdir (sys.prefix) and pattern.match (sys.prefix) is not None:
				mark_path = os.path.join (sys.prefix, mark_file)
				if os.path.isfile (mark_path):
					try:
						os.remove (mark_path)
						logger.info (f'{mark_path}: removed mark for deletion file')
					except OSError as e:
						logger.warning (f'{mark_path}: failed to removed mark for deletion file: {e}')
				for path in (Stream (os.listdir (base))
					.map (lambda f: os.path.join (base, f))
					.filter (lambda p: pattern.match (p) is not None and p != sys.prefix and os.path.isdir (p))
					.drain ()
				):
					mark_path = os.path.join (path, mark_file)
					try:
						st = os.stat (mark_path)
						if st.st_mtime < expire:
							logger.info (f'{path}: removing as time for deletion of {remove_after_days} days had been reached')
							shutil.rmtree (path, onerror = lambda f, p, e: logger.warning (f'{p}: {e[1]}'))
					except OSError as e:
						if e.errno == errno.ENOENT:
							with open (mark_path, 'w'):
								pass
							logger.info (f'{path}: created mark for deletion file {mark_path}')
						else:
							logger.warning (f'{mark_path}: failed to access file: {e}')
		
	def cleanup_mail_archive (self) -> None:
		archive = os.path.join (base, 'ARCHIVE')
		if os.path.isdir (archive):
			self.cleanup_timestamp_directories (archive, 1, 1)
		else:
			logger.warning ('Expected path "%s" not available' % archive)

	def cleanup_log_done (self) -> None:
		def compare_log_done (path: str, fname: str, st: Optional[os.stat_result]) -> bool:
			return self.mktimestamp (fname) + 30 < self.today

		for path in self.select (os.path.join (base, 'log', 'done'), False, compare_log_done)[0]:
			self.remove (path)

	def cleanup_spool (self) -> None:
		meta = os.path.join (base, 'var', 'spool', 'META')
		admin = os.path.join (base, 'var', 'spool', 'ADMIN')
		def show_count (cnt: List[int], s: str) -> None:
			logger.info ('%d files found, %d successful, %d failed: %s' % (sum (cnt), cnt[0], cnt[1], s))
		#
		count = [0, 0]
		moved = []
		for path in self._select_xml (meta, 2, False):
			if self.move (path, admin):
				count[0] += 1
				moved.append (os.path.join (admin, os.path.basename (path)))
			else:
				count[1] += 1
		show_count (count, 'moved from %s to %s' % (meta, admin))
		if moved:
			self.compress (moved)
			logger.info ('%d files compressed' % len (moved))
		#
		count = [0, 0]
		for path in self._select_xml (admin, 30, True):
			if self.remove (path):
				count[0] += 1
			else:
				count[1] += 1
		show_count (count, 'removed from %s' % admin)
		#
		deleted = os.path.join (base, 'var', 'spool', 'DELETED')
		count = [0, 0]
		for path in self._select_xml (deleted, 30, True):
			if self.remove (path):
				count[0] += 1
			else:
				count[1] += 1
		show_count (count, 'removed from %s' % deleted)
		#
		outdated = os.path.join (base, 'var', 'spool', 'OUTDATED')
		self.cleanup_timestamp_directories (
			path = outdated,
			pack_after_days = 2,
			remove_after_days = 14,
			compress = 'gz'
		)
		count = [0, 0]
		for path in self._select_xml (outdated, 15, True):
			if self.remove (path):
				count[0] += 1
			else:
				count[1] += 1
		show_count (count, f'removed from {outdated}')
		#
		archive = os.path.join (base, 'var', 'spool', 'ARCHIVE')
		if os.path.isdir (archive):
			self.cleanup_timestamp_directories (archive, 7, 30)

	def _select_xml (self, path: str, offset: int, all_extensions: bool) -> List[str]:
		dt = time.localtime (time.time () - offset * 24 * 60 * 60)
		ts = '%04d%02d%02d' % (dt.tm_year, dt.tm_mon, dt.tm_mday)
		def selector (path: str, fname: str, st: Optional[os.stat_result]) -> bool:
			m = METAFile (fname)
			if m.valid and (all_extensions or m.extension == 'xml'):
				chk = m.timestamp[:8]
				return chk < ts
			return False
		return self.select (path, False, selector)[0]

	def cleanup_filter (self) -> None:
		file_pattern = re.compile ('^[a-z0-9]+-[a-z0-9]+$', re.IGNORECASE)
		fpath = os.path.join (base, 'var', 'spool', 'filter')
		if os.path.isdir (fpath):
			yday = datetime.now ().fromordinal (self.today - 1)
			apath = os.path.join (fpath, '%04d%02d%02d' % (yday.year, yday.month, yday.day))
			if not os.path.isdir (apath):
				create_path (apath)
				for fname in [_f for _f in os.listdir (fpath) if file_pattern.match (_f)]:
					path = os.path.join (fpath, fname)
					if os.path.isfile (path):
						self.move (path, os.path.join (apath, fname))
				self.compress ([os.path.join (apath, _f) for _f in os.listdir (apath)])
			self.cleanup_timestamp_directories (fpath, 30, 180)

class Main (CLI):
	__slots__ = ['dryrun']
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
			'services', nargs = '*',
			help = 'backward compatibility, no more in use'
		)
	
	def use_arguments (self, args: argparse.Namespace) -> None:
		if args.verbose:
			log.loglevel = logging.DEBUG
			log.outlevel = logging.DEBUG
			log.outstream = sys.stderr
		self.dryrun = args.dryrun
	
	def executor (self) -> bool:
		Janitor ().run (not self.dryrun)
		return True
#
if __name__ == '__main__':
	Main.main ()
