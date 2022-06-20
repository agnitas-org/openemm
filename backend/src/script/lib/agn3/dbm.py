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
import	os, logging, time, signal, errno
import	dbm.gnu
from	types import TracebackType
from	typing import Optional
from	typing import Generator, Iterator, Set, Tuple, Type
from	.daemon import Signal, Daemonic
from	.ignore import Ignore
from	.stream import Stream
#
__all__ = ['DBM', 'dbmerror']
#
logger = logging.getLogger (__name__)
#
dbmerror = dbm.gnu.error
class DBM:
	__slots__ = ['dbf', 'path', 'mode']
	def __init__ (self, path: str, mode: str = 'r') -> None:
		self.path = path
		self.mode = Stream (mode).filter (lambda ch: ch in dbm.gnu.open_flags).join ('')
		self.open ()

	def __del__ (self) -> None:
		with Ignore (AttributeError, dbm.gnu.error):
			self.close ()
		
	def __enter__ (self) -> DBM:
		return self

	def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
		self.close ()
		return None
		
	def __iter__ (self) -> Generator[bytes, None, None]:
		key = self.dbf.firstkey ()
		while key is not None:
			yield key
			key = self.dbf.nextkey (key)
	
	def __getitem__ (self, key: bytes) -> bytes:
		return self.dbf[key]
	
	def __setitem__ (self, key: bytes, value: bytes) -> None:
		self.dbf[key] = value
	
	def __delitem__ (self, key: bytes) -> None:
		del self.dbf[key]
	
	def __len__ (self) -> int:
		return len (self.dbf)
	
	def __contains__ (self, key: bytes) -> bool:
		return key in self.dbf
	
	def open (self) -> None:
		self.__validate_database_file ()
		self.dbf = dbm.gnu.open (self.path, self.mode)
	
	def close (self) -> None:
		self.dbf.close ()

	def sync (self) -> None:
		self.dbf.sync ()

	def keys (self) -> Iterator[bytes]:
		return iter (self)
	
	def values (self) -> Generator[bytes, None, None]:
		for key in self:
			yield self[key]
	
	def items (self) -> Generator[Tuple[bytes, bytes], None, None]:
		for key in self:
			yield (key, self[key])

	def reorganize (self) -> None:
		if os.path.isfile (self.path):
			self.close ()
			def reorg (filename: str) -> None:
				try:
					with DBM (filename, 'w') as db:
						db.dbf.reorganize ()
				except dbm.gnu.error as e:
					logger.error (f'DBM: reorganize of {self.path} leads to {e}, try manual reorganization')
					tempbase = os.path.join (os.path.dirname (filename), '.{basename}.{pid}.{time:.02f}'.format (basename = os.path.basename (filename), pid = os.getpid (), time = time.time ()))
					tempfile = tempbase
					nr = 0
					while os.path.isfile (tempfile):
						nr += 1
						tempfile = f'{tempbase}.{nr}'
					try:
						os.rename (filename, tempfile)
						with DBM (tempfile, 'r') as dbin, DBM (filename, 'n') as dbout:
							for (key, value) in dbin.items ():
								dbout[key] = value
					except Exception as e:
						logger.exception (f'DBM: reorganize failed during copy from {tempfile} to {filename}: {e}')
					finally:
						if os.path.isfile (tempfile) and os.path.isfile (filename):
							try:
								os.unlink (tempfile)
							except OSError as e:
								logger.error (f'DBM: reorganize failed to remove {tempfile}: {e}')
			Daemonic.call (reorg, self.path)
			self.open ()

	def truncate (self) -> None:
		self.close ()
		with dbm.gnu.open (self.path, 'n'):
			pass
		self.open ()

	validation_seen: Set[str] = set ()
	def __validate_database_file (self) -> None:
		if self.path not in self.validation_seen and os.path.isfile (self.path):
			with Signal (chld = signal.SIG_DFL):
				pid = os.fork ()
				if pid == 0:
					ec = 0
					try:
						dbf = dbm.gnu.open (self.path, self.mode)
						'' in dbf
						dbf.close ()
					except dbm.gnu.error as e:
						if e.errno is None:
							logger.warning (f'DBM: validation of {self.path} failed due to dbmerror: {e}')
							ec = 1
					os._exit (ec)
				while True:
					try:
						(wait_pid, status) = os.waitpid (pid, 0)
					except OSError as e:
						if e.errno == errno.ECHILD:
							logger.warning (f'DBM: validation of {self.path} failed due to missing sub process: {e}')
							break
						else:
							logger.info (f'DBM: wait for validation subprocess for {self.path} failed: {e}')
					else:
						if wait_pid == pid:
							if os.WIFEXITED (status):
								exit_code = os.WEXITSTATUS (status)
								if exit_code != 0 and os.path.isfile (self.path):
									logger.warning (f'DBM: {self.path} seems to be invalid due to exit code {exit_code}, try to remove it')
									try:
										os.unlink (self.path)
									except OSError as e:
										logger.error (f'DBM: failed to remove {self.path}: {e}')
									finally:
										try:
											dbf = dbm.gnu.open (self.path, 'n')
										except Exception as e:
											logger.error (f'DBM: try to recreate empty database {self.path} failed: {e}')
										else:
											logger.info (f'DBM: recreated empty database {self.path}')
										finally:
											with Ignore ():
												dbf.close ()
							elif os.WIFSIGNALED (status):
								exit_signal = os.WTERMSIG (status)
								logger.warning (f'DBM: validation process for {self.path} failed due to signal {exit_signal}')
							else:
								logger.warning (f'DBM: validation process for {self.path} failed tue to unknown exit status of {status}')
							self.validation_seen.add (self.path)
							break
