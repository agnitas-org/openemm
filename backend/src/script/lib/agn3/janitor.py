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
import	os, logging, time, datetime, stat
from	abc import abstractmethod
from	typing import Callable, Optional
from	typing import List, NamedTuple, Pattern, Set, Tuple
from	.definitions import base
from	.io import which
from	.pattern import isnum
from	.tools import sizefmt, call
#
__all__ = ['Janitor']
#
logger = logging.getLogger (__name__)
#
class Janitor:
	"""Framework for housekeeping

This class provides a collection of methods for housekeeping of
application typical log and archive files. The lower level methods can
be used to subclass this class and implmenet some custom cleanup jobs."""
	__slots__ = ['name', 'doit', 'timestamp', 'now', 'today']
	class Rule (NamedTuple):
		pattern: Optional[Pattern[str]]
		offset: int

	def __init__ (self, name: str) -> None:
		"""``name'' is the name for this job (informal usage only)"""
		self.name = name
		self.doit = True
		self.timestamp = time.time ()
		self.now = time.localtime (self.timestamp)
		self.today = datetime.datetime (self.now.tm_year, self.now.tm_mon, self.now.tm_mday).toordinal ()
	
	def show (self, s: str) -> None:
		try:
			print (s)
		except UnicodeEncodeError:
			print (s.encode ('UTF-8', errors = 'backslashreplace').decode ('UTF-8'))

	def mktimestamp (self, s: str) -> int:
		"""transforms ``s'' to an ordinal number for that day, otherwise for today"""
		if isnum (s) and len (s) == 8:
			rc = datetime.datetime (int (s[:4]), int (s[4:6]), int (s[6:])).toordinal ()
		else:
			rc = self.today
		return rc

	def call (self, cmd: List[str]) -> bool:
		"""executes cmd (which is expected as a list or print out command during dryrun"""
		if self.doit:
			try:
				rc = call (cmd)
				if rc != 0:
					logger.error (f'Command {cmd!r} returns {rc}')
			except OSError as e:
				logger.error (f'Failed to execute {cmd!r}: {e}')
				rc = -1
		else:
			self.show (f'CALL: {cmd}')
			rc = 0
		return rc == 0

	def pack (self, destination: str, sources: List[str], working_directory: Optional[str] = None) -> bool:
		"""Pack ``sources'' (str or list) using tar to
``destination''. Compression is selected depending on extension. If
``working_directory'' is set and is an existing directory, this will
be used as the directory to resolve relative pathnames"""
		if destination.endswith ('.gz'):
			cflag = 'z'
		elif destination.endswith ('.bz2'):
			cflag = 'j'
		elif destination.endswith ('.xz'):
			cflag = 'J'
		else:
			cflag = ''
		cmd = ['tar']
		if working_directory is not None:
			if not os.path.isdir (working_directory):
				logger.error (f'{working_directory}: working directory missing/not a directory for {destination}')
				return False
			cmd += ['-C', working_directory]
		cmd += [f'-c{cflag}f', destination] + sources
		return self.call (cmd)

	def compress (self, paths: List[str], method: str = 'gzip') -> bool:
		"""Compresses a ``paths'' (str or list) using method (gzip by default)"""
		if not paths:
			return True
		#
		command = None
		if method in ('bzip', 'bzip2', 'bz'):
			command = ['bzip2', '-9']
		elif method == 'xz':
			command = ['xz', '-9']
		if command is None or which (command[0]) is None:
			command = ['gzip', '-9']
		return self.call (command + paths)
	
	def move (self, src: str, dst: str) -> bool:
		"""Move (rename) ``src'' to ``dst''. Requires files to be on the same file system"""
		try:
			if os.path.isdir (dst):
				dst = os.path.join (dst, os.path.basename (src))
			if self.doit:
				os.rename (src, dst)
			else:
				self.show (f'MOVE: {src} to {dst}')
			rc = True
		except OSError as e:
			logger.error (f'Failed to move {src} to {dst}: {e}')
			rc = False
		return rc

	def remove (self, path: str) -> bool:
		"""Remove a ``path'' (a file or a directory); directories are removed with complete content, if possible!"""
		try:
			if os.path.isdir (path):
				for fname in os.listdir (path):
					self.remove (os.path.join (path, fname))
				if self.doit:
					os.rmdir (path)
				else:
					self.show (f'RMDIR: {path}')
			else:
				if self.doit:
					os.unlink (path)
				else:
					self.show (f'REMOVE: {path}')
			rc = True
		except OSError as e:
			logger.error (f'Failed to remove {path}: {e}')
			rc = False
		return rc
	
	def rotate (self, path: str, amount: int = 4, compress: bool = True) -> None:
		"""Rotate ``path'' up to ``amount'' (default 4) instances, compress the rotated file, if ``rotate'' is True"""
		source = amount
		target = None
		while source >= 0:
			if source > 0:
				fname_source = f'{path}.{source}'
				if compress:
					fname_source += '.gz'
			else:
				fname_source = path
			if os.path.isfile (fname_source):
				if target is None:
					logger.info (f'Removing old {fname_source}')
					self.remove (fname_source)
				else:
					fname_target = f'{path}.{target}'
					if source > 0 and compress:
						fname_target += '.gz'
					logger.info (f'Rename {fname_source} to {fname_target}')
					self.move (fname_source, fname_target)
					if source == 0 and compress:
						logger.info (f'Compress {fname_target}')
						self.compress (fname_target)
			target = source
			source -= 1

	def __count_size (self, seen: Set[str], path: str) -> int:
		rc = 0
		if path not in seen:
			seen.add (path)
			if os.path.isdir (path):
				for fname in os.listdir (path):
					npath = os.path.abspath (os.path.join (path, fname))
					if os.path.isdir (npath) or os.path.isfile (npath):
						rc += self.__count_size (seen, npath)
			else:
				st = os.stat (path)
				rc += st.st_size
		return rc

	def free_space (self, filesystem: str, files: List[str], multiply: float = 2.0) -> Tuple[bool, str]:
		"""Determinates if a ``filesystem'' has roughly enough room to handle cleanup of ``files'' asuming it will consum ``multiply'' times of space"""
		try:
			st = os.statvfs (filesystem)
			free_size = st.f_bsize * st.f_bavail
			required_size = 0
			seen: Set[str] = set ()
			for fname in files:
				path = os.path.abspath (fname)
				if os.path.isdir (path) or os.path.isfile (path):
					required_size += self.__count_size (seen, path)
			if required_size * multiply < free_size:
				rc = (True, 'Currently {free} available, {req} required'.format (free = sizefmt (free_size), req = sizefmt (required_size)))
			else:
				rc = (False, 'Not enough space available, {free} available, {req} required'.format (free = sizefmt (free_size), req = sizefmt (required_size)))
		except OSError as e:
			logger.error (f'Failed to stat {filesystem} for {files!r}: {e}')
			rc = (False, f'Failed to count required size on {filesystem} due to {e}')
		return rc
	
	def free (self, filesystem: str, files: List[str], multiply: float = 2.0) -> bool:
		"""Determinate if ``filesystem'' as enough room to handle cleanup of ``*args'' files"""
		(rc, msg) = self.free_space (filesystem, files, multiply)
		(logger.info if rc else logger.warning) (msg)
		return rc

	def select (self, path: str, recr: bool, *callbacks: Callable[[str, str, Optional[os.stat_result]], bool]) -> List[List[str]]:
		"""Preselect files form ``path'', recrusive if ``recr'' is True and store the selection for each ``*callbacks'' found"""
		selected: List[List[str]] = [[]] * len (callbacks)
		try:
			subdirs = []
			for fname in os.listdir (path):
				fpath = os.path.join (path, fname)
				st: Optional[os.stat_result]
				try:
					st = os.lstat (fpath)
				except OSError:
					st = None
				n = 0
				for callback in callbacks:
					if callback (fpath, fname, st):
						selected[n].append (fpath)
						break
					n += 1
				if recr and st is not None and stat.S_ISDIR (st.st_mode):
					subdirs.append (fpath)
			for subdir in subdirs:
				sselected = self.select (subdir, recr, *callbacks)
				n = 0
				while n < len (selected):
					if sselected[n]:
						selected[n] += sselected[n]
					n += 1
		except OSError as e:
			logger.warning (f'Directory "{path}" not scanable: {e}')
		return selected

	def cleanup_timestamp_files (self,
		path: str,
		compress_after_days: Optional[List[Janitor.Rule]],
		remove_after_days: Optional[List[Janitor.Rule]]
	) -> None:
		"""Cleanup files in ``path'' if they are prefixed by a
valid timestamp by compressing files older than ``compress_after_days''
days and remove older files than ``remove_after_days''"""
		to_compress: List[str] = []
		to_remove: List[str] = []
		try:
			for fname in os.listdir (path):
				parts = fname.split ('-', 1)
				if len (parts) == 2 and isnum (parts[0]):
					ts = self.mktimestamp (parts[0])
					found = False
					check: Optional[List[Janitor.Rule]]
					dest: List[str]
					ignoreSuffix: Optional[str]
					for (check, dest, ignoreSuffix) in (remove_after_days, to_remove, None), (compress_after_days, to_compress, '.gz'):
						if check is not None:
							for rule in check:
								if rule.pattern is None or rule.pattern.search (fname) is not None:
									if ts + rule.offset < self.today:
										if ignoreSuffix is None or not fname.endswith (ignoreSuffix):
											dest.append (fname)
										found = True
									break
							if found:
								break
		except OSError as e:
			logger.error (f'Failed to access log directory "{path}": {e}')
		report = []
		if compress_after_days is not None:
			report.append ('Found {count} files to compress'.format (count = len (to_compress)))
		if remove_after_days is not None:
			report.append ('Found {count} files to remove'.format (count = len (to_remove)))
		if report:
			logger.info (', '.join (report))
		for fname in to_remove:
			self.remove (os.path.join (path, fname))
		for fname in to_compress:
			self.compress ([os.path.join (path, fname)])

	def cleanup_timestamp_directories (self, path: str, pack_after_days: int, remove_after_days: int, compress: Optional[str] = None) -> None:
		"""Cleanup directories located in ``path'' which are
valid timestamps by packing them after ``pack_after_days'' days and
remove packed files after ``remove_after_days''. ``compress'' may by set
to a valid value for pack() to use compression for packed files."""
		to_pack: List[str] = []
		to_remove: List[str] = []
		if compress is not None:
			tar_extension = f'.tar.{compress}'
		else:
			tar_extension = '.tar'
		for fname in os.listdir (path):
			cpath = os.path.join (path, fname)
			if isnum (fname) and os.path.isdir (cpath):
				ts = self.mktimestamp (fname)
				if remove_after_days is not None and ts + remove_after_days < self.today:
					to_remove.append (cpath)
				elif pack_after_days is not None and ts + pack_after_days < self.today:
					to_pack.append (cpath)
			elif fname.endswith (tar_extension) and os.path.isfile (cpath):
				ts = self.mktimestamp (fname[:-len (tar_extension)])
				if remove_after_days is not None and ts + remove_after_days < self.today:
					to_remove.append (cpath)
		for cpath in to_remove:
			logger.debug (f'Removing "{cpath}"')
			self.remove (cpath)
		for cpath in to_pack:
			if self.free (os.path.dirname (cpath), [cpath]):
				logger.debug (f'Packing "{cpath}"')
				tar = f'{cpath}{tar_extension}'
				if self.pack (tar, [os.path.basename (cpath)], working_directory = os.path.dirname (cpath)):
					logger.debug (f'Removing "{cpath}"')
					self.remove (cpath)
			else:
				logger.warning (f'Skip {cpath}, not enough free disk space available')
	
	def __cleanup_filetime_files (self, path: str, rules: List[Janitor.Rule], recrusive: bool) -> None:
		to_remove = []
		descend = []
		for fname in os.listdir (path):
			cpath = os.path.join (path, fname)
			st = os.stat (cpath)
			if stat.S_ISDIR (st.st_mode):
				if recrusive:
					descend.append (cpath)
			elif stat.S_ISREG (st.st_mode):
				for rule in rules:
					if (rule.pattern is None and not fname.startswith ('.')) or (rule.pattern is not None and rule.pattern.search (fname) is not None):
						if st.st_ctime + rule.offset * 24 * 60 * 60 < self.timestamp:
							to_remove.append (cpath)
		for cpath in to_remove:
			logger.debug (f'Removing "{cpath}"')
			self.remove (cpath)
		for cpath in descend:
			logger.debug (f'Descending to "{cpath}"')
			self.__cleanup_filetime_files (cpath, rules, recrusive)

	def cleanup_filetime_files (self, path: str, rules: List[Janitor.Rule], recrusive: bool = False) -> None:
		"""Cleanup files based on their creation date located
in ``path'' (and below, if ``recrusive'' is True) applying ``rules''.
These is a list of tuple with two members, the first a callback with a
serach() method (e.g. a compiled regular expression pattern) and the
second the age in days to remove files that are older. A shortcut for
remove all files which are older than a given day is by passing an int
instead of list for ``rules''"""
		self.__cleanup_filetime_files (path, rules, recrusive)

	def cleanup_logfiles (self, compress_after_days: Optional[List[Janitor.Rule]], remove_after_days: Optional[List[Janitor.Rule]]) -> None:
		"""Cleanup AGNITAS logfiles, ``compress_after_days'' days and ``remove_after_days'' days"""
		self.cleanup_timestamp_files (os.path.join (base, 'var', 'log'), compress_after_days, remove_after_days)

	def prepare (self, doit: bool) -> None:
		"""Setup the cleanup, ``doit'' is True for real action or False for a dryrun"""
		self.doit = doit
		logger.info ('Janitor starting up')

	def done (self) -> None:
		"""Finalize cleanup"""
		logger.info ('Janitor going down')

	def run (self, doit: bool) -> None:
		"""Main entry point, ``doit'' is True for real action or False for a dryrun"""
		self.prepare (doit)
		self.execute ()
		self.done ()

	@abstractmethod
	def execute (self) -> None:
		"""Execute the cleanup, must be overwritten by subclass"""

