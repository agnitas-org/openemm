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
import	os, stat
from	datetime import datetime
from	types import TracebackType
from	typing import Optional
from	typing import Dict, Generator, NamedTuple, Type
from	.definitions import base
from	.ignore import Ignore
from	.io import create_path
from	.tools import calc_hash
#
__all__ = ['FSDB']
#
class FSDB:
	__slots__ = ['cache']
	class Value (NamedTuple):
		content: str
		timestamp: datetime

	path = os.path.join (base, 'var', 'fsdb')
	def __init__ (self) -> None:
		self.cache: Dict[str, Optional[FSDB.Value]] = {}

	def clear (self) -> None:
		self.cache.clear ()
	
	def get (self, key: str, default: Optional[str] = None) -> Optional[str]:
		try:
			return self[key]
		except KeyError:
			return default

	def __enter__ (self) -> FSDB:
		return self

	def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
		return None

	def __iter__ (self) -> Generator[str, None, None]:
		depth = [FSDB.path]
		while depth:
			current = depth.pop (0)
			with Ignore (OSError):
				for filename in sorted (os.listdir (current)):
					path = os.path.join (current, filename)
					st = os.stat (path)
					if stat.S_ISDIR (st.st_mode):
						depth.append (path)
					elif stat.S_ISREG (st.st_mode):
						key = self.__convert_path_to_key (path)
						if key is not None:
							yield key

	def __getitem__ (self, key: str) -> str:
		return self.retrieve (key).content
	
	def __setitem__ (self, key: str, value: str) -> None:
		self.store (key, value)
	
	def __delitem__ (self, key: str) -> None:
		self.remove (key)

	def retrieve (self, key: str) -> FSDB.Value:
		with Ignore (KeyError):
			value = self.cache[key]
			if value is None:
				raise KeyError (key)
			return value
		path = self.__convert_key_to_path (key)
		if path is not None:
			with Ignore (IOError, UnicodeDecodeError), open (path) as fd:
				content = fd.read ()
				try:
					timestamp = datetime.fromtimestamp (os.stat (fd.fileno ()).st_mtime)
				except OSError:
					timestamp = datetime.now ()
				value = self.cache[key] = FSDB.Value (content, timestamp)
				return value
		self.cache[key] = None
		raise KeyError (key)
	
	def store (self, key: str, content: str) -> None:
		path = self.__convert_key_to_path (key)
		if path is not None:
			with Ignore (KeyError):
				del self.cache[key]
			try:
				with Ignore (IOError, UnicodeDecodeError), open (path) as fd:
					if fd.read () == content:
						with Ignore (OSError):
							os.utime (fd.fileno ())
						return
				try:
					with open (path, 'w') as fd:
						fd.write (content)
				except FileNotFoundError:
					create_path (os.path.dirname (path))
					with open (path, 'w') as fd:
						fd.write (content)
			finally:
				try:
					timestamp = datetime.fromtimestamp (os.stat (path).st_mtime)
				except OSError:
					timestamp = datetime.now ()
				self.cache[key] = FSDB.Value (content, timestamp)
		else:
			raise ValueError (f'{key}: invalid key')
	
	def remove (self, key: str) -> None:
		path = self.__convert_key_to_path (key)
		if path is not None:
			with Ignore (KeyError):
				del self.cache[key]
			with Ignore (OSError):
				os.unlink (path)
		else:
			raise ValueError (f'{key}: invalid key')

	hash_chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_-'
	def __convert_key_to_path (self, key: str) -> Optional[str]:
		if os.path.sep in key:
			return None
		elements = key.split (':')
		elements.insert (-1, self.__hash_representation (elements[-1]))
		return os.path.join (FSDB.path, os.path.join (*elements))

	def __convert_path_to_key (self, path: str) -> Optional[str]:
		if path.startswith (FSDB.path):
			elements = path[-(len (FSDB.path) - 2):].split ('/')
			with Ignore (UnicodeEncodeError):
				if len (elements) > 1 and elements.pop (-2) == self.__hash_representation (elements[-1]):
					return ':'.join (elements)
		return None

	def __hash_representation (self, name: str) -> str:
		hash_value = calc_hash (name)
		return FSDB.hash_chars[(hash_value >> 6) % len (FSDB.hash_chars)] + FSDB.hash_chars[hash_value % len (FSDB.hash_chars)]
