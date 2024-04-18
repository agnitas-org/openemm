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
import	os, errno, time, stat, gzip, bz2, logging
import	csv, hashlib, shlex, json
from	collections import namedtuple
from	functools import partial
from	typing import Any, Callable, Iterable, Literal, Optional, Protocol, Union
from	typing import Dict, IO, Iterator, List, Pattern, Set, TextIO, Tuple, Type
from	typing import cast, overload
from	.definitions import base
from	.exceptions import error
from	.ignore import Ignore
from	.parser import Line, Field
from	.pattern import isnum
from	.stream import Stream
from	.template import Placeholder
#
__all__ = [
	'relink', 'which', 'mkpath', 'expand_path', 'normalize_path', 'create_path',
	'ArchiveDirectory', 'Filepos', 'Filesystem', 'file_access',
	'copen', 'cstreamopen', 'gopen', 'fingerprint', 'expand_command',
	'csv_dialect', 'csv_default', 'csv_reader', 'csv_named_reader', 'csv_writer',
	'Line', 'Field'
]
#
logger = logging.getLogger (__name__)
#
_ReadText = Union[Literal['r'], Literal['rt']]
_ReadBinary = Literal['rb']
_ReadModes = Union[_ReadText, _ReadBinary]
_WriteText = Union[Literal['w'], Literal['a'], Literal['x'], Literal['wt'], Literal['at'], Literal['xt']]
_WriteBinary = Union[Literal['wb'], Literal['ab'], Literal['xb']]
_modes = Union[_ReadText, _ReadBinary, _WriteText, _WriteBinary]
_bz2modes = Union[Literal[''], Literal['r'], Literal['rb'], Literal['w'], Literal['wb'], Literal['x'], Literal['xb'], Literal['a'], Literal['ab']]
#
def relink (source: str, target: str, pattern: Optional[List[Pattern[str]]] = None) -> None:
	"""Updateds symbolic links in target from source, optional only these files matching pattern"""
	def make_real (path: str) -> str:
		return os.path.realpath (path) if not os.path.isdir (path) and os.path.islink (path) else path
	#
	real_source = make_real (source)
	real_target = make_real (target)
	if not os.path.isdir (real_source):
		raise error (f'{source}: source not a directory')
	if not os.path.isdir (real_target):
		raise error (f'{target}: target not a directory')
	#
	def match (filename: str) -> bool:
		return pattern is None or sum (1 for _p in pattern if _p.match (filename)) > 0
	#
	Filepath = namedtuple ('Filepath', ['filename', 'source_path', 'target_path'])
	installed = [_f for _f in os.listdir (real_target) if os.path.islink (os.path.join (real_target, _f)) and match (_f)]
	available = [_f for _f in os.listdir (real_source) if match (_f)]
	for fp in (Stream (available)
		.map (lambda f: Filepath (f, os.path.join (real_source, f), os.path.join (real_target, f)))
		.filter (lambda fp: fp.filename not in installed or (os.path.islink (fp.target_path) and os.readlink (fp.target_path) != fp.source_path))
	):
		if fp.filename in installed:
			os.unlink (fp.target_path)
		os.symlink (fp.source_path, fp.target_path)
	#
	def resolvable (path: str) -> bool:
		seen: Set[str] = set ()
		while os.path.islink (path) and path not in seen:
			seen.add (path)
			new_path = os.readlink (path)
			if not os.path.isabs (new_path):
				new_path = os.path.join (os.path.dirname (path), new_path)
			path = os.path.realpath (new_path)
		return os.access (path, os.F_OK)
	#
	(Stream (installed)
		.map (lambda f: os.path.join (real_target, f))
		.filter (lambda p: os.path.islink (p) and not resolvable (p))
		.each (lambda p: os.unlink (p))
	)

@overload
def which (program: str, *args: str, default: None = ..., mode: int = ...) -> Optional[str]: ...
@overload
def which (program: str, *args: str, default: str, mode: int = ...) -> str: ...
def which (program: str, *args: str, default: Optional[str] = None, mode: int = os.F_OK | os.X_OK) -> Optional[str]:
	"""Finds the path to an executable

``args'' may contain more directories to search for if the programn
can be expected in a known directory which is not part of $PATH.
"""
	return cast (Optional[str], Stream (os.environ.get ('PATH', '').split (':') + list (args))
		.distinct ()
		.map (lambda p: os.path.join (p if p else os.path.curdir, program))
		.filter (lambda p: os.access (p, mode))
		.first (no = default)
	)

def mkpath (*parts: str, **opts: Any) -> str:
	"""Create a pathname from elements

Optional keyword is absolute (boolean) to make the path absolute, i.e.
with leading slash.
"""
	absolute = bool (opts.get ('absolute', False))
	rc = os.path.join (*(str (_p) for _p in parts))
	if absolute and not os.path.isabs (rc):
		rc = os.path.sep + rc
	return os.path.normpath (rc)

def expand_path (path: str, base_path: Optional[str] = None) -> str:
	"""expand and normalize a filesystem path"""
	path = os.path.expandvars (os.path.expanduser (path))
	if not os.path.isabs (path):
		path = os.path.abspath (os.path.join (base_path, path) if base_path is not None else path)
	return path

def normalize_path (path: str) -> str:
	"""expand and normalize a filesystem path relative to home directory"""
	return expand_path (path, base_path = base)
	
def create_path (path: str, mode: int = 0o777) -> bool:
	"""create a path and all missing elements

returns ``False'' if ``path''' already exists and ``True'' if
``path'' had been created. On failure, an exception is raised.
"""
	if os.path.isdir (path):
		return False
	try:
		os.mkdir (path, mode)
	except OSError as e:
		if e.errno == errno.EEXIST or e.errno != errno.ENOENT:
			raise error (f'failed to create already existing {path}: {e}')
		elements = path.split (os.path.sep)
		target = ''
		for element in elements:
			target += element
			if target and not os.path.isdir (target):
				try:
					os.mkdir (target, mode)
				except OSError as e:
					raise error (f'failed to create {path} at {target}: {e}')
			target += os.path.sep
	return True

class ArchiveDirectory:
	__slots__: List[str] = []
	track: Set[str] = set ()
	@classmethod
	def make (cls, path: str, mode: int = 0o777) -> str:
		"""create an archive directory

if missing, creates a path to a directory with the given path as base
directory followed a directory based on the current day (YYYYMMDD) to
be used to archive files on a daily base."""
		tt = time.localtime (time.time ())
		ts = f'{tt.tm_year:04d}{tt.tm_mon:02d}{tt.tm_mday:02d}'
		arch = os.path.join (path, ts)
		if arch not in cls.track:
			try:
				st = os.stat (arch)
				if not stat.S_ISDIR (st[stat.ST_MODE]):
					raise error (f'{arch} is not a directory')
			except OSError as e:
				if e.args[0] != errno.ENOENT:
					raise error (f'Unable to stat {arch}: {e}')
				try:
					create_path (arch, mode)
				except OSError as e:
					raise error (f'Unable to create {arch}: {e}')
			cls.track.add (arch)
		return arch

class Filepos:
	"""read a file and keep track of position
	
this class can be used to read a file which is managed by another
process. So it stores the last position in a file and also detects if
a file is new (e.g. due to a log rotate) and starts reading from the
beginning of the new file.
"""
	__slots__ = ['fname', 'info', 'checkpoint', 'fd', 'inode', 'ctime', 'count']
	def __init__ (self, fname: str, info: str, checkpoint: int = 1000) -> None:
		"""Opens a file and stores every ``checkpoint'' lines the current position in info"""
		self.fname = fname
		self.info = info
		self.checkpoint = checkpoint
		self.fd: Optional[TextIO] = None
		self.inode = -1
		self.ctime = 0
		self.count = 0
		self.__open ()
	
	def __iter__ (self) -> Iterator[str]:
		for line in iter (lambda: self.readline (), None):
			yield line
		self.save ()

	def __stat (self, stat_file: bool) -> Optional[os.stat_result]:
		fd = cast (TextIO, self.fd)
		try:
			return os.stat (self.fname) if stat_file else os.fstat (fd.fileno ())
		except (OSError, IOError):
			logger.exception ('Failed to stat file {f}'.format (
				f = self.fname if stat_file else 'open file #{fno}'.format (fno = fd.fileno ())
			))
		return None

	def __open (self) -> None:
		try:
			pos = 0
			if os.access (self.info, os.F_OK):
				try:
					with open (self.info) as fd:
						parts = fd.readline ().strip ().split (':')
						if len (parts) == 3:
							(self.inode, self.ctime, pos) = (int (_p) for _p in parts)
							logger.debug (f'Read file information from {self.info}: inode={self.inode}, ctime={self.ctime}, pos={pos}')
						else:
							fd.seek (0)
							logger.error ('Corrupted info file {path}, try to remove it, content of file is: {content}'.format (
								path = self.info,
								content = fd.read (4096)
							))
							with Ignore (OSError):
								os.unlink (self.info)
				except (IOError, ValueError) as e:
					logger.exception (f'Failed to access {self.info}')
					raise error (f'Unable to read info file {self.info}: {e}')
			try:
				self.fd = open (self.fname, errors = 'backslashreplace')
				st = self.__stat (False)
				if st:
					ninode = st.st_ino
					nctime = int (st.st_ctime)
					if ninode == self.inode:
						if st.st_size >= pos:
							if pos > 0:
								self.fd.seek (pos)
								logger.debug (f'Seek to last position {pos} of {st.st_size} of current size')
						else:
							logger.error (f'Old position {pos} is larger than current file size {st.st_size}, file had been truncated?')
					else:
						logger.info (f'File inode has changed from {self.inode} to {ninode}, start reading from beginning')
					self.inode = ninode
					self.ctime = nctime
				else:
					raise error (f'Failed to stat {self.fname}')
			except IOError as e:
				logger.exception (f'Failed to access {self.fname}: {e}')
				raise error (f'Unable to open {self.fname}: {e}')
		except Exception:
			if self.fd:
				self.fd.close ()
				self.fd = None
			raise

	def save (self) -> None:
		"""Save current position for recovery"""
		for state in 0, 1:
			try:
				with open (self.info, 'w') as fd:
					fd.write ('{inode}:{ctime}:{pos}\n'.format (
						inode = self.inode,
						ctime = self.ctime,
						pos = cast (TextIO, self.fd).tell ()
					))
				break
			except IOError as e:
				logger.exception (f'Failed to write {self.info}: {e}', e)
				if state == 0:
					with Ignore (OSError):
						os.unlink (self.info)
				else:
					raise
		self.count = 0

	def close (self) -> None:
		"""closes the file"""
		if self.fd:
			self.save ()
			self.fd.close ()
			self.fd = None

	def __is_same_file (self) -> bool:
		st = self.__stat (True)
		return st is not None and st.st_ino == self.inode and int (st.st_ctime) == self.ctime and st.st_size > cast (TextIO, self.fd).tell ()

	def __readline (self) -> Optional[str]:
		line = cast (TextIO, self.fd).readline ()
		if line != '':
			self.count += 1
			if self.count >= self.checkpoint:
				self.save ()
			return line.rstrip ('\r\n')
		else:
			return None

	def readline (self) -> Optional[str]:
		"""reads a line from the file

returns the line read with trailing line termination removed or None
if no more lines are available."""
		line = self.__readline ()
		if line is None and self.__is_same_file ():
			self.close ()
			self.__open ()
			line = self.__readline ()
		return line
	
	def log (self, level: int, message: str) -> None:
		pass

class Filesystem:
	"""Filesystem related queries

With this class one can retrieve information about free and used space
and inodes on a filesystem for a specific path."""
	__slots__ = ['path', 'stat']
	def __init__ (self, path: str) -> None:
		"""``path'' is a path in the filesystem to examine"""
		self.path = path
		self.stat = os.statvfs (path)
	
	def percentBlockFree (self) -> int:
		"""returns the percentage of free blocks"""
		return int ((self.stat.f_bavail * 100) / self.stat.f_blocks)
	
	def percentBlockUsed (self) -> int:
		"""returns the percentage of used blocks"""
		return 100 - self.percentBlockFree ()
		
	def percentInodeFree (self) -> int:
		"""returns the percentage of free inodes"""
		return int ((self.stat.f_favail * 100) / self.stat.f_files)
		
	def percentInodeUsed (self) -> int:
		"""returns the percentage of used inodes"""
		return 100 - self.percentInodeFree ()
		
	def usage (self) -> Tuple[int, int, int, int]:
		"""returns a four element tuple containing the percentage of free and used blocks and free and used inodes"""
		return (self.percentBlockFree (), self.percentBlockUsed (), self.percentInodeFree (), self.percentInodeUsed ())

def file_access (path: str) -> Tuple[List[str], List[List[Any]]]:
	"""Check if a process access a file

returns two lists, the first is a list of process-ids which accessing
the path, the second is a list of failures (e.g. due to missing
permissions) while trying to determinate the access to the file."""
	try:
		st = os.stat (path)
	except OSError as e:
		raise error (f'failed to stat "{path}": {e}')
	device = st[stat.ST_DEV]
	inode = st[stat.ST_INO]
	rc: List[str] = []
	fail: List[List[Any]] = []
	seen: Dict[str, bool] = {}
	for pid in [_p for _p in os.listdir ('/proc') if isnum (_p)]:
		bpath = f'/proc/{pid}'
		checks = [f'{bpath}/{_c}' for _c in ('cwd', 'exe', 'root')]
		try:
			fdp = f'{bpath}/fd'
			for fds in os.listdir (fdp):
				checks.append (f'{fdp}/{fds}')
		except OSError as e:
			fail.append ([e, f'{bpath}/fd: {e}'])
		try:
			fd = open (f'{bpath}/maps', 'r')
			for line in fd:
				parts = line.split ()
				if len (parts) == 6 and parts[5].startswith ('/'):
					checks.append (parts[5].strip ())
			fd.close ()
		except IOError as e:
			fail.append ([e, f'{bpath}/maps: {e}'])
		for check in checks:
			try:
				if seen[check]:
					rc.append (pid)
			except KeyError:
				seen[check] = False
				cpath = check
				try:
					fpath: Optional[str] = None
					count = 0
					while fpath is None and count < 128 and cpath.startswith ('/'):
						count += 1
						st = os.lstat (cpath)
						if stat.S_ISLNK (st[stat.ST_MODE]):
							cpath = os.readlink (cpath)
						else:
							fpath = cpath
					if fpath is not None and st[stat.ST_DEV] == device and st[stat.ST_INO] == inode:
						rc.append (pid)
						seen[check] = True
				except OSError as e:
					fail.append ([e, f'{cpath}: {e}'])
	return (rc, fail)

def force_text (mode: _modes) -> _modes:
	if 'b' not in mode and 't' not in mode:
		return cast (_modes, f'{mode}t')
	return mode
def force_bz2 (mode: _modes) -> _bz2modes:
	return cast (_bz2modes, force_text (mode))

@overload
def copen (path: str, mode: _ReadText = ..., encoding: Optional[str] = ..., errors: Optional[str] = ...) -> IO[str]: ...
@overload
def copen (path: str, mode: Union[_ReadBinary, _WriteBinary], encoding: Optional[str] = ..., errors: Optional[str] = ...) -> IO[bytes]: ...
@overload
def copen (path: str, mode: Union[_ReadText, _WriteText], encoding: Optional[str] = ..., errors: Optional[str] = ...) -> IO[str]: ...
def copen (path: str, mode: _modes = 'r', encoding: Optional[str] = None, errors: Optional[str] = None) -> Union[IO[str], IO[bytes]]:
	"""Opens a file according to its extension

this opens a file dependig of the extension of the filename (and if a
the required module is available). Fall back to standard open() if no
match is found."""
	istext = 'b' not in mode
	if path.endswith ('.gz'):
		if istext:
			return cast (IO[str], gzip.open (path, force_text (mode), encoding = encoding, errors = errors))
		return cast (IO[bytes], gzip.open (path, mode))
	elif path.endswith ('.bz2'):
		if istext:
			return bz2.open (path, force_bz2 (mode), encoding = encoding, errors = errors)
		return bz2.open (path, mode)
	if istext:
		return open (path, mode, encoding = encoding, errors = errors)
	return open (path, mode)

@overload
def cstreamopen (path: str, mode: _ReadText = ..., encoding: Optional[str] = ..., errors: Optional[str] = ...) -> Stream[str]: ...
@overload
def cstreamopen (path: str, mode: _ReadBinary, encoding: Optional[str] = ..., errors: Optional[str] = ...) -> Stream[bytes]: ...
def cstreamopen (path: str, mode: _ReadModes = 'r', encoding: Optional[str] = None, errors: Optional[str] = None) -> Union[Stream[str], Stream[bytes]]:
	"""Openes a file for reading using copen and return a stream to process the file."""
	def defer (o: Any) -> None:
		o.close ()
	return cast (Union[Stream[str], Stream[bytes]], Stream.defer (copen (path, mode, encoding = encoding, errors = errors), defer))

def gopen (path: str, mode: _modes = 'r', encoding: Optional[str] = None, errors: Optional[str] = None) -> IO[Any]:
	"""Tries to open a compressed version of a file, if available

looks up, if there is a compressed version of the file available and
opens this, otherwiese falls back to the standard open()."""
	for (ext, method) in ('.gz', gzip.open), ('.bz2', bz2.open):
		npath = f'{path}{ext}'
		if os.path.isfile (npath):
			return cast (Callable[..., IO[Any]], method) (npath, force_text (mode), encoding = encoding, errors = errors)
	return open (path, mode, encoding = encoding, errors = errors)

def fingerprint (path: str) -> str:
	fp = hashlib.new ('md5')
	with open (path, 'rb') as fd:
		for chunk in iter (partial (fd.read, 65536), b''):
			fp.update (chunk)
	return fp.hexdigest ()

def expand_command (command: List[str], ns: Dict[str, str], macros: Optional[Dict[str, Callable[[str], str]]] = None) -> List[str]:
	"""expand a list of strings with placeholder using namespace ``ns'' and ``macros''

>>> cmd = ['this', 'is', 'a', '$test']
>>> expand_command (cmd, {})
Traceback (most recent call last):
...
agn3.exceptions.error: test: not found/parsable: 'test'
>>> expand_command (cmd, {'test': 'Test'})
['this', 'is', 'a', 'Test']
>>> expand_command (cmd, {'test': 'great Test'})
['this', 'is', 'a', 'great', 'Test']
>>> expand_command (cmd, {'test': '"very great" Test'})
['this', 'is', 'a', 'very great', 'Test']
>>> expand_command (cmd + ['$test(super)'], {'test': 'great Test'}, {'test': lambda a: f'a very special {a} test'})
['this', 'is', 'a', 'great', 'Test', 'a', 'very', 'special', 'super', 'test']
>>> cmd = ['this', 'is', 'a', '$(test)']
>>> expand_command (cmd, {'test': 3})
['this', 'is', 'a', '3']
>>> cmd = ['this', 'is', 'a', '$(test + 4)']
>>> expand_command (cmd, {'test': 3})
['this', 'is', 'a', '7']
"""
	ph = Placeholder ()
	def processor () -> Iterable[str]:
		for element in command:
			expanded = ph (element, ns, macros)
			if expanded != element:
				if expanded:
					for subelement in shlex.split (expanded):
						yield subelement
			else:
				yield element
	return list (processor ())

_dialects: Dict[str, str] = {}
_dialect_base = {
	'delimiter': ';',
	'quotechar': '"',
	'escapechar': None,
	'doublequote': True,
	'skipinitialspace': True,
	'lineterminator': '\r\n',
	'quoting': csv.QUOTE_MINIMAL
}
def csv_dialect (name: str, base: Optional[str] = None, **kwargs: Any) -> str:
	definitions = _dialect_base.copy () if base is None else json.loads (_dialects[base])
	for (key, value) in kwargs.items ():
		if key in definitions:
			definitions[key] = value
		else:
			raise NameError (f'{key}: unexpected keyword for {name}')
	idstr = json.dumps (definitions, indent = 0, separators = (',', ':'), sort_keys = True)
	try:
		if _dialects[name] != idstr:
			raise NameError (f'{name}: already definied with different definition')
	except KeyError:
		csv.register_dialect (name, type (name, (csv.Dialect, ), definitions))
		_dialects[name] = idstr
	return name
csv_default = csv_dialect ('default')

class csv_reader (Protocol):
	def __iter__ (self) -> Iterator[List[str]]: ...
	def __next__ (self) -> List[str]: ...
class csv_writer (Protocol):
	def writerow (self, row: Iterable[Any]) -> Any: ...
	def writerows (self, rows: Iterable[Iterable[Any]]) -> None: ...

def csv_named_reader (reader: csv_reader, *fields: Field, header_from_fields: bool = False) -> Iterator[Line]:
	if header_from_fields:
		header = [_f.name for _f in fields]
	else:
		header = next (reader)
	if header is not None:
		typ = cast (Type[Line], namedtuple ('record', header))
		if fields:
			converter: List[Callable[[str], Any]] = [lambda a: a] * len (header)
			for field in fields:
				if (index := header.index (field.name)) != -1 and field.converter is not None:
					converter[index] = field.converter
			for row in reader:
				yield typ (*tuple (_c (_v) for (_c, _v) in zip (converter, row)))
		else:
			for row in reader:
				yield typ (*row)
