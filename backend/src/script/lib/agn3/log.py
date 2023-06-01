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
import	sys, os, time, stat
import	logging, threading
from	dataclasses import dataclass
from	datetime import datetime, timedelta
from	traceback import format_exception
from	types import TracebackType
from	typing import Any, Callable, Optional, Union
from	typing import Dict, Generator, List, Set, TextIO, Tuple, Type
from	typing import overload
from	.definitions import base, host, program
from	.exceptions import error
from	.ignore import Ignore
from	.io import create_path
from	.parser import Parsable, unit
#
__all__ = ['LogID', 'log', 'mark', 'log_limit', 'interactive']
#
class Limiter:
	__slots__ = ['seen', 'lock', 'enabled']
	def __init__ (self) -> None:
		self.seen: Dict[str, Tuple[str, datetime, int]] = {}
		self.lock = threading.Lock ()
		self.enabled = True
	
	def __call__ (self, method: Callable[[str], None], message: str, *, key: Optional[str] = None, duration: Parsable = None, limit_identical: bool = False, threshold: int = 0) -> None:
		if self.enabled:
			with self.lock:
				now = datetime.now ()
				seen_key = key if key is not None else message
				amount = 0
				with Ignore (KeyError):
					(previous, expire, amount) = self.seen[seen_key]
					if amount > threshold:
						if expire > now and (not limit_identical or previous == message):
							return
						amount = 0
				#
				method (message)
				if duration is None:
					expire = datetime.fromordinal (now.toordinal () + 1)
				else:
					expire = now + timedelta (seconds = unit.parse (duration))
				self.seen[seen_key] = (message, expire, amount + 1)
		else:
			method (message)
	
	def enable (self) -> None:
		self.enabled = True
	
	def disable (self) -> None:
		self.enabled = False

log_limit = Limiter ()
#
class LogID:
	__slots__ = ['ref', 'new_id', 'saved_id', 'saved', 'id_stack']
	def __init__ (self, ref: _Log, new_id: str) -> None:
		self.ref = ref
		self.new_id = new_id
		self.saved_id: Optional[str] = None
		self.saved = False
		self.id_stack: List[str] = []
		
	def __enter__ (self) -> LogID:
		self.save ()
		self.set_id ()
		return self
			
	def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
		self.restore ()
		return None

	def save (self) -> None:
		if not self.saved:
			self.saved_id = self.ref.custom_id
			self.saved = True
		
	def restore (self) -> None:
		if self.saved:
			self.ref.custom_id = self.saved_id
			self.saved_id = None
			self.saved = False
		
	def set_id (self) -> None:
		self.ref.custom_id = self.new_id if not self.id_stack else '{new_id}/{stack}'.format (new_id = self.new_id, stack = '/'.join (self.id_stack))

	def push (self, extra: str) -> None:
		self.id_stack.append (extra)
		self.set_id ()
		
	def pop (self) -> None:
		if self.id_stack:
			self.id_stack.pop ()
			self.set_id ()
		
class _Log:
	__slots__ = [
		'loglevel', 'outlevel', 'outstream',
		'verbosity', 'host', 'name', 'path',
		'intercept', 'custom_ids', 'lock']
	log_directories_seen: Set[str] = set ()
	lognames: Dict[int, str] = {
		logging.DEBUG:		'DEBUG',
		logging.INFO:		'INFO',
		logging.WARNING:	'WARNING',
		logging.ERROR:		'ERROR',
		logging.CRITICAL:	'CRITICAL',
		logging.FATAL:		'FATAL'
	}
	def __init__ (self) -> None:
		self.loglevel = logging.INFO
		self.outlevel = logging.WARNING
		self.outstream: Optional[TextIO] = None
		self.verbosity = 0
		self.host = host
		self.name = program
		self.path = os.environ.get ('LOG_HOME', os.path.join (base, 'var', 'log'))
		self.intercept: Optional[Callable[[logging.LogRecord, str], None]] = None
		#
		self.custom_ids: Dict[int, Optional[str]] = {}
		self.lock = threading.RLock ()
	
	def __call__ (self, new_id: str) -> LogID:
		return LogID (self, new_id)
	
	def __make_filename (self, ts: Optional[datetime], epoch: Union[None, int, float], name: str) -> str:
		if ts is None:
			if epoch is not None:
				dt = datetime.fromtimestamp (epoch)
			else:
				dt = datetime.now ()
		else:
			dt = ts
		return os.path.join (self.path, f'{dt.year:04d}{dt.month:02d}{dt.day:02d}-{name}.log')
	
	def _get_custom_id (self) -> Optional[str]:
		return self.custom_ids.get (threading.get_ident ())
	def _set_custom_id (self, custom_id: Optional[str]) -> None:
		self.custom_ids[threading.get_ident ()] = custom_id
	def _del_custom_id (self) -> None:
		with Ignore (KeyError):
			del self.custom_ids[threading.get_ident ()]
	custom_id = property (_get_custom_id, _set_custom_id, _del_custom_id)

	_sentinel = object ()
	@overload
	def get_loglevel (self, default: str) -> str: ...
	@overload
	def get_loglevel (self, default: None) -> Optional[str]: ...
	def get_loglevel (self, default: Any = _sentinel) -> Any:
		try:
			return _Log.lognames[self.loglevel]
		except KeyError:
			if default is _Log._sentinel:
				raise
		return default
		
	def set_loglevel (self, loglevel: str) -> None:
		loglevel = loglevel.upper ()
		for (level, name) in _Log.lognames.items ():
			if name.startswith (loglevel):
				self.loglevel = level
				break
		else:
			try:
				self.loglevel = int (loglevel)
			except ValueError:
				raise error (f'{loglevel}: unknown logging level')
	
	def set_verbosity (self, verbosity: int) -> None:
		if verbosity < 0:
			self.loglevel = logging.ERROR
		elif verbosity == 0:
			self.loglevel = logging.INFO
		else:
			self.loglevel = logging.DEBUG
			self.outlevel = logging.DEBUG
			self.outstream = sys.stderr
			self.verbosity = verbosity

	def filename (self, name: Optional[str] = None, epoch: Union[None, int, float] = None, ts: Optional[datetime] = None) -> str:
		"""Build a logfile in the defined conventions

a logfilename is created from the global logpath as the target
directory and is created using a timestamp
(YYYYMMDD-<hostname>-<name>.log

as timestamp the current time is used, but may be either set using an
own version in ts or by setting the epoch (seconds since 1.1.1970)
"""
		return self.__make_filename (ts, epoch, '{host}-{name}'.format (
			host = self.host,
			name = name if name is not None else self.name
		))

	def data_filename (self, name: str, epoch: Union[None, int, float] = None, ts: Optional[datetime] = None) -> str:
		"""Build a logfile for storing data.

see ``logfilename'', in this case the name is required and no host
name is part of the final filename"""
		return self.__make_filename (ts, epoch, name)

	def append (self, s: str) -> None:
		"""Writes data to a logfile

s may either be a string or a list or tuple containing strings. If it
is neither then it tries to write a string representation of the
passed object."""
		fname = self.filename ()
		with self.lock:
			try:
				with open (fname, 'a') as fd:
					if isinstance (s, str):
						fd.write (s)
					elif isinstance (s, list) or isinstance (s, tuple):
						for l in s:
							fd.write (l)
					else:
						fd.write (str (s) + '\n')
					fd.close ()
			except Exception as e:
				directory = os.path.dirname (fname)
				if directory in self.log_directories_seen:
					sys.stderr.write ('LOGFILE write failed[{typ!r}, {e}, {fname}]: {s!r}\n'.format (
						typ = type (e),
						e = e,
						fname = fname,
						s = s
					))
				else:
					self.log_directories_seen.add (directory)
					with Ignore (OSError):
						st = os.stat (directory)
						if stat.S_ISDIR (st.st_mode):
							sys.stderr.write (f'{fname}: failed to write to logfile or log directory {directory}: {e}\n')
						else:
							sys.stderr.write (f'{directory}: expected a directory, access failed due to {e}\n')
					try:
						create_path (directory)
						self.append (s)
					except error as e:
						sys.stderr.write (f'{directory}: failed to create missing log directory: {e}\n')

	def add (self, record: logging.LogRecord) -> None:
		"""add an entry to the logfile
	
main logfile writing method. Dependig on set global loglevel and
output methods, write logfile to file or defined output stream.

If ``self.intercept'' is not None, this method is called instead. This
can be used to avoid writing logfiles for interactive applications."""
		lid = self.custom_id if self.custom_id else record.module
		if self.verbosity > 1:
			lid += f' ({record.name}.{record.funcName}:{record.lineno})'
		if self.intercept is not None:
			self.intercept (record, lid)
		elif record.levelno >= self.loglevel or (record.levelno >= self.outlevel and self.outstream is not None):
			now = datetime.now ()
			prefix = f'[{now.day:02d}.{now.month:02d}.{now.year:04d}  {now.hour:02d}:{now.minute:02d}:{now.second:02d}] {record.process} {record.levelname}/{lid}'
			messages = [record.getMessage () + '\n']
			if record.exc_info is not None and type (record.exc_info) is tuple and len (record.exc_info) == 3:
				tb = format_exception (*record.exc_info)
				if tb:
					for line in tb:
						messages += [_l + '\n' for _l in line.rstrip ('\n').split ('\n')]
			for message in messages:
				output_string = f'{prefix}: {message}'
				if record.levelno >= self.loglevel:
					self.append (output_string)
				if record.levelno >= self.outlevel and self.outstream is not None:
					self.outstream.write (output_string)
					self.outstream.flush ()

	class Datareader:
		@dataclass
		class Track:
			path: str
			position: int = 0
			@classmethod
			def new (cls, name: str, timestamp: datetime) -> _Log.Datareader.Track:
				return cls (path = log.data_filename (name = name, ts = timestamp))

		__slots__ = ['name', 'track']
		def __init__ (self, name: str) -> None:
			self.name = name
			self.track: Dict[int, _Log.Datareader.Track] = {}
		
		def __call__ (self, reference: Optional[datetime] = None) -> Generator[str, None, None]:
			now = datetime.now ()
			now_ordinal = now.toordinal ()
			ref_ordinal = reference.toordinal () if reference is not None else now_ordinal
			for offset in (-1, 0, 1):
				current = ref_ordinal + offset
				if current <= now_ordinal:
					try:
						track = self.track[current]
					except KeyError:
						track = self.track[current] = _Log.Datareader.Track.new (self.name, datetime.fromordinal (current))
					with Ignore (IOError), open (track.path, 'rb') as fd:
						if track.position > 0:
							fd.seek (track.position)
						for line in fd:
							yield line.decode ('UTF-8', errors = 'backslashreplace')
							track.position += len (line)

	def datareader (self, name: str) -> _Log.Datareader:
		return _Log.Datareader (name)

class _RootHandler (logging.Handler):
	__slots__ = ['log', 'last']
	def __init__ (self, *args: Any, **kwargs: Any) -> None:
		super ().__init__ (*args, **kwargs)
		self.log = _Log ()
		self.last = 0
			
	def emit (self, record: logging.LogRecord) -> None:
		if record.levelno >= self.log.loglevel or (record.levelno >= self.log.outlevel and self.log.outstream is not None):
			self.log.add (record)
			self.last = int (time.time ())
	
	def mark (self, duration_in_minutes: int = 60, message: Optional[str] = None) -> None:
		now = int (time.time ())
		if self.last + duration_in_minutes * 60 < now:
			saved_id = self.log.custom_id
			if message is not None:
				self.log.custom_id = message
			logging.info ('-- MARK --')
			if message is not None:
				self.log.custom_id = saved_id
			self.last = now

_rootLogger = logging.getLogger ()
_rootLogger.setLevel (logging.NOTSET)
_rootHandler = _RootHandler ()
_rootLogger.addHandler (_rootHandler)

log = _rootHandler.log
mark = _rootHandler.mark

def log_filter (predicate: Callable[[logging.LogRecord], bool]) -> None:
	class Filter (logging.Filter):
		def filter (self, record: logging.LogRecord) -> bool:
			return predicate (record)
	_rootHandler.addFilter (Filter ())

def _except (type_: Type[BaseException], value: BaseException, traceback: Optional[TracebackType]) -> None:
	logging.critical (f'CAUGHT EXCEPTION: {value}', exc_info = value)
	
_original_excepthook = sys.excepthook
sys.excepthook = _except

def interactive (on: bool = True) -> None:
	def interceptor (record: logging.LogRecord, lid: str) -> None:
		sys.stderr.write ('{process} {level}/{lid} {message}\n'.format (
			process = record.process,
			level = record.levelname,
			lid = lid,
			message = record.getMessage ()
		))
		sys.stderr.flush ()
	log.intercept = interceptor if on else None
	sys.excepthook = _original_excepthook if on else _except

if program == 'unset':
	interactive ()
