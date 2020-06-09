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
import	os, logging, time
import	dbm.gnu
from	types import TracebackType
from	typing import Optional
from	typing import Generator, Iterator, Tuple, Type
from	.daemon import Daemonic
from	.ignore import Ignore
#
__all__ = ['DBM', 'dbmerror']
#
logger = logging.getLogger (__name__)
#
dbmerror = dbm.gnu.error
class DBM:
	__slots__ = ['dbf', 'path', 'mode']
	def __init__ (self, path: str, mode: str) -> None:
		self.path = path
		self.mode = mode
		self.__open ()

	def __open (self) -> None:
		self.dbf = dbm.gnu.open (self.path, self.mode)
	
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
			self.__open ()

	def truncate (self) -> None:
		self.close ()
		with dbm.gnu.open (self.path, 'n'):
			pass
		self.__open ()
