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
import	os, errno, time, stat, gzip, bz2, re, logging
import	csv, codecs, hashlib, shlex
from	collections import namedtuple
from	dataclasses import dataclass
from	functools import partial
from	io import StringIO
from	types import TracebackType
from	typing import Any, Callable, Iterable, Literal, Optional, Union
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
	'relink', 'which', 'mkpath', 'normalize_path', 'create_path',
	'ArchiveDirectory', 'Filepos', 'Filesystem', 'file_access',
	'copen', 'cstreamopen', 'gopen', 'fingerprint', 'expand_command',
	'CSVDialects', 'CSVDefault',
	'CSVWriter', 'CSVDictWriter',
	'CSVReader', 'CSVDictReader', 'CSVAutoDictReader',
	'CSVNamedReader', 'Line', 'Field',
	'CSVAuto'
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
def which (program: str, *args: str, default: None = ...) -> Optional[str]: ...
@overload
def which (program: str, *args: str, default: str) -> str: ...
def which (program: str, *args: str, default: Optional[str] = None) -> Optional[str]:
	"""Finds the path to an executable

``args'' may contain more directories to search for if the programn
can be expected in a known directory which is not part of $PATH.
"""
	return cast (Optional[str], Stream (os.environ.get ('PATH', '').split (':') + list (args))
		.distinct ()
		.map (lambda p: os.path.join (p if p else os.path.curdir, program))
		.filter (lambda p: os.access (p, os.X_OK))
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

def normalize_path (path: str) -> str:
	"""normalize a relative path to be relative to home directory"""
	if path is not None and not os.path.isabs (path):
		path = os.path.expandvars (os.path.expanduser (path))
		if not os.path.isabs (path):
			path = os.path.abspath (os.path.join (base, path))
	return path
	
def create_path (path: str, mode: int = 0o777) -> None:
	"""create a path and all missing elements"""
	if not os.path.isdir (path):
		try:
			os.mkdir (path, mode)
		except OSError as e:
			if e.args[0] != errno.EEXIST:
				if e.args[0] != errno.ENOENT:
					raise error (f'Failed to create {path}: {e}')
				elements = path.split (os.path.sep)
				target = ''
				for element in elements:
					target += element
					if target and not os.path.isdir (target):
						try:
							os.mkdir (target, mode)
						except OSError as e:
							raise error (f'Failed to create {path} at {target}: {e}')
					target += os.path.sep

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
					fpath = None
					count = 0
					while fpath is None and count < 128 and cpath.startswith ('/'):
						count += 1
						st = os.lstat (cpath)
						if stat.S_ISLNK (st[stat.ST_MODE]):
							cpath = os.readlink (cpath)
						else:
							fpath = cpath
					if not fpath is None and st[stat.ST_DEV] == device and st[stat.ST_INO] == inode:
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
def copen (path: str, mode: _ReadText = ..., errors: Optional[str] = ...) -> IO[str]: ...
@overload
def copen (path: str, mode: Union[_ReadBinary, _WriteBinary], errors: Optional[str] = ...) -> IO[bytes]: ...
@overload
def copen (path: str, mode: Union[_ReadText, _WriteText], errors: Optional[str] = ...) -> IO[str]: ...
def copen (path: str, mode: _modes = 'r', errors: Optional[str] = None) -> Union[IO[str], IO[bytes]]:
	"""Opens a file according to its extension

this opens a file dependig of the extension of the filename (and if a
the required module is available). Fall back to standard open() if no
match is found."""
	if path.endswith ('.gz'):
		return cast (IO[Any], gzip.open (path, force_text (mode), errors = errors))
	elif path.endswith ('.bz2'):
		return bz2.open (path, force_bz2 (mode), errors = None)
	return open (path, mode, errors = errors)

@overload
def cstreamopen (path: str, mode: _ReadText = ..., errors: Optional[str] = ...) -> Stream[str]: ...
@overload
def cstreamopen (path: str, mode: _ReadBinary, errors: Optional[str] = ...) -> Stream[bytes]: ...
def cstreamopen (path: str, mode: _ReadModes = 'r', errors: Optional[str] = None) -> Union[Stream[str], Stream[bytes]]:
	"""Openes a file for reading using copen and return a stream to process the file."""
	def defer (o: Any) -> None:
		o.close ()
	return cast (Union[Stream[str], Stream[bytes]], Stream.defer (copen (path, mode, errors), defer))

def gopen (path: str, mode: _modes = 'r', errors: Optional[str] = None) -> IO[Any]:
	"""Tries to open a compressed version of a file, if available

looks up, if there is a compressed version of the file available and
opens this, otherwiese falls back to the standard open()."""
	for (ext, method) in ('.gz', gzip.open), ('.bz2', bz2.open):
		npath = f'{path}{ext}'
		if os.path.isfile (npath):
			return cast (Callable[..., IO[Any]], method) (npath, force_text (mode), errors = errors)
	return open (path, mode, errors = errors)

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

class _CSVBase (csv.Dialect):
	doublequote = True
	escapechar = None
	lineterminator = '\r\n'
	quotechar = '"'
	quoting = csv.QUOTE_NONE
	skipinitialspace = True
class _CSVSemicolon (_CSVBase):
	delimiter = ';'
class _CSVSemicolon1 (_CSVSemicolon):
	quoting = csv.QUOTE_MINIMAL
class _CSVSemicolon2 (_CSVSemicolon):
	quoting = csv.QUOTE_ALL
class _CSVComma (_CSVBase):
	delimiter = ','
class _CSVComma1 (_CSVComma):
	quoting = csv.QUOTE_MINIMAL
class _CSVComma2 (_CSVComma):
	quoting = csv.QUOTE_ALL
class _CSVTAB (_CSVBase):
	delimiter = '\t'
class _CSVTAB1 (_CSVTAB):
	quoting = csv.QUOTE_MINIMAL
class _CSVTAB2 (_CSVTAB):
	quoting = csv.QUOTE_ALL
class _CSVBar (_CSVBase):
	delimiter = '|'
class _CSVBar1 (_CSVBar):
	quoting = csv.QUOTE_MINIMAL
class _CSVBar2 (_CSVBar):
	quoting = csv.QUOTE_ALL
class _CSVSpace (_CSVBase):
	delimiter = ' '
class _CSVSpace1 (_CSVSpace):
	quoting = csv.QUOTE_MINIMAL
class _CSVSpace2 (_CSVSpace):
	quoting = csv.QUOTE_ALL
class _CSVAuto (_CSVBase):
	pass
_csvregister = [
	('agn-default', _CSVSemicolon1),
	('agn-semicolon-none', _CSVSemicolon),
	('agn-semicolon-minimal', _CSVSemicolon1),
	('agn-semicolon-full', _CSVSemicolon2),
	('agn-comma-none', _CSVComma),
	('agn-comma-minimal', _CSVComma1),
	('agn-comma-full', _CSVComma2),
	('agn-tab-none', _CSVTAB),
	('agn-tab-minimal', _CSVTAB1),
	('agn-tab-full', _CSVTAB2),
	('agn-bar-none', _CSVBar),
	('agn-bar-minimal', _CSVBar1),
	('agn-bar-full', _CSVBar2),
	('agn-space-none', _CSVSpace),
	('agn-space-minimal', _CSVSpace1),
	('agn-space-full', _CSVSpace2)
]
CSVDialects = []
for (_csvname, _csvclass) in _csvregister:
	csv.register_dialect (_csvname, _csvclass)
	CSVDialects.append (_csvname)
CSVDefault = CSVDialects[0]

BOM = namedtuple ('BOM', ['name', 'bom'])
class CSVIO:
	"""Base class for all CSV wrapper

in general you will not instance this class directly but some other
class (CSVReader, CSVWriter) which inherits this class.

The module ``agn'' defines some often used dialects which can be used
in constructors for the various derivated classes. These all are
starting with "agn-" following a (hopefully) descriptive name:
	- agn-default
	- agn-semicolon-none
	- agn-semicolon-minimal
	- agn-semicolon-full
	- agn-comma-none
	- agn-comma-minimal
	- agn-comma-full
	- agn-tab-none
	- agn-tab-minimal
	- agn-tab-full
	- agn-bar-none
	- agn-bar-minimal
	- agn-bar-full
	- agn-space-none
	- agn-space-minimal
	- agn-space-full

There is als an ``CSVDefault'' which is "agn-semicolon-minimal" as
a default (and has the alias name "agn-default"). The other names the
middle part describes the separator for this dialct and tha last part
the handling of text marks (where ``none'' means no marks at all,
``minimal'' use textmark if required and ``full' use always
textmarks). Textmark is the double quote sign. But you can always
create new dialects either using one of these as a base or starting
with an empty one as supported by the standard "csv" module."""
	__slots__ = ['fd', 'foreign', 'bom', 'charset', 'empty']
	boms = [BOM (_b, codecs.__dict__[_b]) for _b in dir (codecs) if _b.startswith ('BOM') and type (codecs.__dict__[_b]) is bytes]
	maxBomLength = max ([len (_b.bom) for _b in boms])
	bom2charset: Dict[bytes, str] = {
		codecs.BOM_UTF16_BE:	'UTF-16-BE',
		codecs.BOM_UTF16_LE:	'UTF-16-LE',
		codecs.BOM_UTF32_BE:	'UTF-32-BE',
		codecs.BOM_UTF32_LE:	'UTF-32-LE',
		codecs.BOM_UTF8:	'UTF-8'
	}
	charset2bom: Dict[str, bytes] = dict ([(_i[1], _i[0]) for _i in bom2charset.items ()])
	def __init__ (self) -> None:
		self.fd: Optional[IO[Any]] = None
		self.foreign = False
		self.bom: Optional[BOM] = None
		self.charset: Optional[str] = None
		self.empty = False

	def __del__ (self) -> None:
		self.done ()

	def open (self, stream: Union[str, IO[Any]], mode: _modes, bom_charset: Optional[str] = None) -> None:
		"""opens a stream for reading or writiing

``stream'' may either be a file like object or a string in the later
case it is used as a filename.

In reading mode it tries to find a BOM (byte order mark) and skip this
for the reading of the content, but set the atribute ``charset'' to
the corrosponding name of the found BOM.

In writing mode and if ``bom_charset'' is not None and a BOM is found
for this name, the BOM is written to the beginning of a file (in
append mode it is only written, if the file had zero length on open)."""
		if isinstance (stream, str):
			self.fd = copen (stream, mode)
			self.foreign = False
		else:
			self.fd = stream
			self.foreign = True
		if not isinstance (stream, StringIO) and self.fd is not None:
			self.bom = None
			self.charset = None
			if mode is None or 'r' in mode:
				with Ignore (AttributeError):
					self.fd.seek
					pos = 0
					start: bytes
					if isinstance (stream, str):
						with copen (stream, 'rb') as raw:
							start = raw.read (self.maxBomLength)
					else:
						with open (self.fd.fileno (), 'rb', closefd = False) as raw:
							pos = raw.tell ()
							start = raw.read (self.maxBomLength)
					for bom in self.boms:
						if start.startswith (bom.bom):
							pos += len (bom.bom)
							self.bom = bom.name
							self.charset = self.bom2charset.get (bom.bom)
							self.fd.seek (pos)
			elif 'w' in mode or 'a' in mode:
				bom_seq: Optional[bytes] = None
				if bom_charset is not None:
					try:
						self.charset = bom_charset.upper ()
						bom_seq = self.charset2bom[self.charset]
						for bom in self.boms:
							if bom.bom == bom_seq:
								self.bom = bom.name
								break
					except (AttributeError, KeyError) as e:
						raise error (f'{bom_charset}: BOM not found: {e}')
				if self.fd.tell () == 0:
					if bom_seq is not None and len (bom_seq) > 0:
						raw = open (self.fd.fileno (), 'wb', closefd = False)
						raw.write (bom_seq)
						raw.close ()
						self.fd.seek (len (bom_seq))
					self.empty = True

	def done (self) -> None:
		"""cleanup resources"""
		if not self.fd is None:
			if not self.foreign:
				self.fd.close ()
			else:
				with Ignore (AttributeError):
					if not self.fd.closed:
						self.fd.flush ()
			self.fd = None

	def close (self) -> None:
		"""closes an open file descriptor"""
		self.done ()

class _CSVWriter (CSVIO):
	__slots__ = ['writer', 'header']
	def __init__ (self,
		stream: Union[str, IO[Any]],
		mode: Optional[_modes],
		bom_charset: Optional[str],
		header: Optional[List[str]]
	) -> None:
		super ().__init__ ()
		self.writer: Any = None
		self.open (stream, mode if mode is not None else 'w', bom_charset)
		self.header = header

	def __enter__ (self) -> _CSVWriter:
		return self

	def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
		self.close ()
		return None

	def write (self, row: List[Any]) -> None:
		if self.writer is not None:
			if self.empty:
				if self.header is not None:
					self.writer.writerow (self.header)
				self.empty = False
			self.writer.writerow (row)

class CSVWriter (_CSVWriter):
	"""Wrapper to write a CSV file"""
	__slots__: List[str] = []
	def __init__ (self,
		stream: Union[str, IO[Any]],
		dialect: str,
		mode: Optional[_modes] = None,
		bom_charset: Optional[str] = None,
		header: Optional[List[str]] = None
	) -> None:
		"""for meaning of ``stream'' and ``bom_charset''see
CSVIO.open (), for availble ``dialect'' values see CSVIO.__doc__"""
		super ().__init__ (stream, mode, bom_charset, header)
		self.writer = csv.writer (cast (IO[Any], self.fd), dialect = dialect)

class CSVDictWriter (_CSVWriter):
	"""Wrapper to write a CSV file from a dict"""
	__slots__: List[str] = []
	def __init__ (self,
		stream: Union[str, IO[Any]],
		field_list: List[str],
		dialect: str,
		mode: Optional[_modes] = None,
		bom_charset: Optional[str] = None,
		header: Optional[List[str]] = None,
		relaxed: bool = False
	) -> None:
		"""for meaning of ``stream'' and ``bom_charset''see
CSVIO.open (), for availble ``dialect'' values see CSVIO.__doc__,
``field_list'' is an array for the keys of the dictonary and if
``relaxed'' is True, errors are ignored otherwise raised (as
implemented by the csv.DictWriter module)"""
		super ().__init__ (stream, mode, bom_charset, header)
		if relaxed:
			extrasaction = 'ignore'
		else:
			extrasaction = 'raise'
		self.writer = csv.DictWriter (self.fd, field_list, dialect = dialect, extrasaction = extrasaction)

class _CSVReader (CSVIO):
	__slots__ = ['reader']
	def __init__ (self, stream: Union[str, IO[Any]]) -> None:
		super ().__init__ ()
		self.reader: Any = None
		self.open (stream, 'r')

	def __iter__ (self) -> Iterator[Tuple[Any, ...]]:
		return iter (self.reader)

	def __enter__ (self) -> _CSVReader:
		return self

	def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
		self.close ()
		return None

	def read (self) -> Any:
		return next (self.reader, None)
	
	def stream (self) -> Stream[List[str]]:
		return Stream.defer (self.reader, lambda o: self.close ())

class CSVReader (_CSVReader):
	"""Wrapper to read a CSV file"""
	__slots__: List[str] = []
	def __init__ (self, stream: Union[str, IO[Any]], dialect: str) -> None:
		"""for meaning of ``stream'' and ``bom_charset''see
CSVIO.open (), for availble ``dialect'' values see CSVIO.__doc__"""
		super ().__init__ (stream)
		self.reader = csv.reader (cast (IO[Any], self.fd), dialect = dialect)

class CSVDictReader (_CSVReader):
	"""Wrapper to read a CSV file as a dict"""
	__slots__: List[str] = []
	def __init__ (self,
		stream: Union[str, IO[Any]],
		field_list: List[str],
		dialect: str,
		rest_key: Optional[str] = None,
		rest_value: Any = None
	) -> None:
		"""for meaning of ``stream'' and ``bom_charset''see
CSVIO.open (), for availble ``dialect'' values see CSVIO.__doc__,
``field_list'', ``rest_key'' and ``rest_value'' are passed to
csv.DictReader to fill the dictionary."""
		super ().__init__ (stream)
		self.reader = csv.DictReader (cast (IO[Any], self.fd), field_list, dialect = dialect, restkey = rest_key, restval = rest_value)

class CSVAutoDictReader (_CSVReader):
	"""Wrapper to a read a CSV file as dict, determinating the field list form the first header line of the CSV"""
	__slots__: List[str] = []
	def __init__ (self, stream: Union[str, IO[Any]], dialect: str) -> None:
		"""for meaning of ``stream'' and ``bom_charset''see
CSVIO.open (), for availble ``dialect'' values see CSVIO.__doc__"""
		super ().__init__ (stream)
		header = next (csv.reader (cast (IO[Any], self.fd), dialect = dialect))
		self.reader = csv.DictReader (cast (IO[Any], self.fd), header, dialect = dialect)

class CSVNamedReader:
	__slots__ = ['csv', 'maker']
	def __init__ (self,
		stream: Union[str, IO[Any]],
		dialect: str,
		*fields: Field
	) -> None:
		self.csv = CSVReader (stream, dialect)
		header = next (self.csv.reader)
		if header is not None:
			typ = cast (Type[Line], namedtuple ('record', header))
			#
			def passthru (s: str) -> str:
				return s
			def convert_row (row: List[str]) -> Line:
				return typ (*tuple (_c (_v) for (_c, _v) in zip (converter, row)))
			def passthru_row (row: List[str]) -> Line:
				return typ (*row)
			#
			if fields:
				converter: List[Callable[[str], Any]] = [passthru] * len (header)
				for field in fields:
					index = header.index (field.name)
					if index != -1:
						converter[index] = field.converter if field.converter is not None else lambda a: a
				self.maker = convert_row
			else:
				self.maker = passthru_row
		
	def __iter__ (self) -> Iterator[Line]:
		return (self.maker (_r) for _r in self.csv.reader)
	
	def __enter__ (self) -> CSVNamedReader:
		return self

	def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
		self.csv.close ()
		return None

	def read (self) -> Any:
		r = self.csv.reader.read ()
		return self.maker (r) if r is not None else None

	def stream (self) -> Stream[Line]:
		return Stream.defer (self, lambda o: self.csv.close ())

class CSVAuto:
	"""CSV reading class which tries to guess the dialect of the input file"""
	__slots__ = ['fname', 'dialect', 'linecount', 'header', 'header_line']
	validHeader = re.compile ('[0-9a-z_][0-9a-z_-]*', re.IGNORECASE)
	@dataclass
	class Guess:
		delimiter: Optional[str] = None
		doublequote: bool = False
		escapechar: Optional[str] = None
		lineterminator: str = '\n'
		quotechar: Optional[str] = None
		quoting: int = csv.QUOTE_NONE
		skipinitialspace: bool = False

	def __init__ (self, fname: str, dialect: str = 'agn-auto', linecount: int = 10) -> None:
		self.fname = fname
		self.dialect = dialect
		self.linecount = linecount
		self.header = None
		self.header_line: Optional[bool] = None

	def __enter__ (self) -> CSVAuto:
		self.setup ()
		return self

	def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
		self.done ()
		return None

	def __analyse (self) -> Any:
		err = None
		fd = open (self.fname, 'r')
		head = fd.readline ()
		datas = []
		if head == '':
			err = f'Empty input file "{self.fname}"'
		else:
			n = 0
			while n < self.linecount:
				data = fd.readline ()
				if data == '':
					break
				datas.append (data)
		fd.close ()
		if err:
			raise error (err)
		temp = self.Guess ()
		if len (head) > 1 and head[-2] == '\r':
			temp.lineterminator = '\r\n'
			head = head[:-2]
		else:
			head = head[:-1]
		if head[0] == '"' or head[0] == '\'':
			temp.quotechar = head[0]
			temp.quoting = csv.QUOTE_ALL
			n = 0
			while True:
				n = head.find (temp.quotechar, n + 1)
				if n != -1 and n + 1 < len (head):
					n += 1
					if head[n] == temp.quotechar:
						temp.doublequote = True
					else:
						temp.delimiter = head[n]
						if n + 1 < len (head) and (head[n + 1] == ' ' or head[n + 1] == '\t'):
							temp.skipinitialspace = True
						break
				else:
					break
		if temp.delimiter is None:
			Counter = namedtuple ('Counter', ['char', 'count'])
			use = Counter (None, 0)
			most = Counter (None, 0)
			for d in ',;|\t!@#$%^&*?':
				cnt1 = head.count (d)
				cnt2 = cnt1
				for data in datas:
					c = data.count (d)
					if c < cnt2:
						cnt2 = c
				if cnt1 > use.count and cnt1 == cnt2:
					use = Counter (d, cnt1)
				if cnt1 > most.count:
					most = Counter (d, cnt1)
			if use.char is None:
				if most.char is None:
					raise error ('No delimiter found')
				use = most
			temp.delimiter = use.char
		if not temp.doublequote:
			for data in datas:
				if '""' in data or '\'\'' in data:
					temp.doublequote = True
					break
		for data in datas:
			if '\\' in data:
				temp.escapechar = '\\'
				break
		return temp

	def setup (self) -> None:
		"""Starts the auto detection"""
		a = self.__analyse ()
		_CSVAuto.delimiter = a.delimiter
		_CSVAuto.doublequote = a.doublequote
		_CSVAuto.escapechar = a.escapechar
		_CSVAuto.lineterminator = a.lineterminator
		_CSVAuto.quotechar = a.quotechar
		_CSVAuto.quoting = a.quoting
		_CSVAuto.skipinitialspace = a.skipinitialspace
		csv.register_dialect (self.dialect, _CSVAuto)
		rd = CSVReader (self.fname, dialect = self.dialect)
		head = rd.read ()
		rd.close ()
		self.header_line = True
		for h in head:
			if self.validHeader.match (h) is None:
				self.header_line = False
				break
		if self.header_line:
			self.header = head
		else:
			self.header = None

	def done (self) -> None:
		"""Ends/cleanup the process"""
		csv.unregister_dialect (self.dialect)
		self.header = None
		self.header_line = None

	def reader (self) -> CSVReader:
		"""Provides a reader a file in the guessed dialect"""
		if self.header_line is None:
			self.setup ()
			temp = True
		else:
			temp = False
		try:
			rd = CSVReader (self.fname, dialect = self.dialect)
			if self.header_line:
				rd.read ()
		finally:
			if temp:
				self.done ()
		return rd

	def writer (self, fname: Optional[str] = None, force: bool = False) -> CSVWriter:
		"""Provides a writer based on the dialect of the original input file

``fname'' is the file to be written to (if None, the input file name
is used) and ``force'' must be set to True to overwrite an existing
file."""
		if fname is None or fname == self.fname:
			if fname is None:
				fname = self.fname
			if not force:
				raise error ('Will not overwrite source file w/o being forced to')
		if self.header_line is None:
			self.setup ()
			temp = True
		else:
			temp = False
		try:
			wr = CSVWriter (fname, dialect = self.dialect, header = self.header if self.header_line else None)
		finally:
			if temp:
				self.done ()
		return wr
