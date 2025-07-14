####################################################################################################################################################################################################################################################################
#                                                                                                                                                                                                                                                                  #
#                                                                                                                                                                                                                                                                  #
#        Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)                                                                                                                                                                                                   #
#                                                                                                                                                                                                                                                                  #
#        This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.    #
#        This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.           #
#        You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.                                                                                                            #
#                                                                                                                                                                                                                                                                  #
####################################################################################################################################################################################################################################################################
#
from	__future__ import annotations
import	logging, pickle, time
from	collections import defaultdict
from	types import TracebackType
from	typing import Any, Callable, Final, Optional
from	typing import DefaultDict, Dict, Generator, NamedTuple, Tuple, Type
from	.dbm import DBM
from	.ignore import Ignore
from	.parser import Parsable, unit
from	.stream import Stream
#
__all__ = ['Key', 'Tracker']
#
logger = logging.getLogger (__name__)
#
class Key (NamedTuple):
	section: str
	option: str
	def encode (self) -> bytes:
		return f'{self.section}:{self.option}'.encode ('UTF-8')
	@staticmethod
	def parse (coded_key: bytes) -> Key:
		return Key (*coded_key.decode ('UTF-8').split (':', 1))
	
class Tracker:
	__slots__ = ['filename', 'db', 'decode', 'encode']
	key_created: Final[str] = '@created'
	key_updated: Final[str] = '@updated'
	def __init__ (self, filename: str) -> None:
		self.filename = filename
		self.db: Optional[DBM] = None
		self.decode: Callable[[bytes], Dict[str, Any]] = pickle.loads
		self.encode: Callable[[Dict[str, Any]], bytes] = pickle.dumps
	
	def __enter__ (self) -> Tracker:
		self.open ()
		return self

	def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
		self.close ()
		return None

	def __iter__ (self) -> Generator[Key, None, None]:
		db = self.open ()
		for dbkey in db:
			with Ignore (ValueError):
				yield Key.parse (dbkey)
	
	def __contains__ (self, key: Key) -> bool:
		return key.encode () in self.open ()
	
	def __getitem__ (self, key: Key) -> Dict[str, Any]:
		try:
			return self.decode (self.open ()[key.encode ()])
		except KeyError:
			raise KeyError (key)
	
	def __setitem__ (self, key: Key, value: Dict[str, Any]) -> None:
		now = int (time.time ())
		old = self.get (key)
		value[Tracker.key_created] = old.get (Tracker.key_created, now) if old else now
		value[Tracker.key_updated] = now
		self.open ()[key.encode ()] = self.encode (value)
	
	def __delitem__ (self, key: Key) -> None:
		try:
			del self.open ()[key.encode ()]
		except KeyError:
			raise KeyError (key)
	
	def items (self) -> Generator[Tuple[Key, Dict[str, Any]], None, None]:
		for key in self:
			yield (key, self[key])
	
	def values (self) -> Generator[Dict[str, Any], None, None]:
		for key in self:
			yield self[key]
	
	def open (self) -> DBM:
		if self.db is None:
			self.db = DBM (self.filename, 'c')
		return self.db
	
	def close (self) -> None:
		if self.db is not None:
			self.db.close ()
			self.db = None

	def get (self, key: Key, default: Optional[Dict[str, Any]] = None) -> Dict[str, Any]:
		try:
			return self[key]
		except KeyError:
			rc = default if default is not None else {}
			now = int (time.time ())
			for timestamp_key in Tracker.key_created, Tracker.key_updated:
				if timestamp_key not in rc:
					rc[timestamp_key] = now
			return rc
	
	def update (self, key: Key, **kwargs: Any) -> None:
		if kwargs:
			cur = self.get (key)
			cur.update (kwargs)
			self[key] = cur

	def delete (self, key: Key) -> None:
		with Ignore (KeyError):
			del self[key]
	
	def expire (self, created: Parsable = None, updated: Parsable = None, max_count: int = 10000) -> None:
		created = unit.parse (created)
		updated = unit.parse (updated)
		if created or updated:
			db = self.open ()
			logger.info ('Expire old tracking entries')
			stats: DefaultDict[str, int] = defaultdict (int)
			now = int (time.time ())
			while True:
				collect: Dict[Key, bool] = {}
				for dbkey in db:
					with Ignore (ValueError):
						key = Key.parse (dbkey)
						value = self.get (key)
						if value:
							for (expiration, timestamp_key) in [
								(created, Tracker.key_created),
								(updated, Tracker.key_updated)
							]:
								if expiration:
									try:
										if value[timestamp_key] + expiration < now:
											collect[key] = True
											stats[key.section] += 1
											break
									except KeyError:
										collect[key] = False
							if len (collect) >= max_count:
								break
				if collect:
					logger.info ('Process {count} tracking entries'.format (count = len (collect)))
					for (key, to_delete) in collect.items ():
						if to_delete:
							del self[key]
						else:
							self[key] = self[key] 	# create missing timestamps
				else:
					break
			logger.info ('Expiration finished ({status}), reorganize database'.format (
				status = Stream (stats.items ())
					.map (lambda kv: '{key}: {value}'.format (key = kv[0], value = kv[1]))
					.join (', ') if stats else '-', )
			)
			db.reorganize ()
			logger.info ('Reorganization finished')
			self.close ()

