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
import	time, collections
from	typing import Any, Optional
from	typing import Deque, Dict
from	.parser import Unit
#
__all__ = ['Cache']
#
class Cache:
	"""Generic caching

this class provides a generic caching implementation with limitiation
of stored entries (LRU cache) and optional time based expiration of
entries."""
	__slots__ = ['limit', 'timeout', 'active', 'count', 'cache', 'cacheline']
	unit = Unit ()
	class Entry:
		"""Represents a single caching entry"""
		__slots__ = ['created', 'value']
		def __init__ (self, value: Any, active: bool) -> None:
			if active:
				self.created = time.time ()
			self.value = value

		def valid (self, now: float, timeout: int) -> bool:
			"""if the entry is still valid"""
			return self.created + timeout >= now

	def __init__ (self, limit: int = 0, timeout: Optional[int] = None) -> None:
		"""``limit'' is the maximum number of elements of the
cache (use 0 for no limits) and ``timeout'' is the timeout for entries
to be valid. ``timeout'' can either be specified as int in seconds or
as a str using modfiers "s" for seconds, "m" for minutes, "h" for
hours, "d" for days and "w" for weeks, e.g.:
	30m: means 30 minutes 
	2h30m: means 2 hours and 30 minutes
	2h 30m: dito, spaces are ignored and can be inserted for better readability"""
		self.limit = limit
		self.timeout = self.unit.parse (timeout, -1)
		self.active = self.timeout >= 0
		self.count = 0
		self.cache: Dict[Any, Any] = {}
		self.cacheline: Deque[Any] = collections.deque ()

	def __getitem__ (self, key: Any) -> Any:
		e = self.cache[key]
		if self.active and not e.valid (time.time (), self.timeout):
			self.remove (key)
			raise KeyError ('%r: expired' % (key, ))
		self.cacheline.remove (key)
		self.cacheline.append (key)
		return e.value

	def __setitem__ (self, key: Any, value: Any) -> None:
		if key in self.cache:
			self.cacheline.remove (key)
		else:
			if self.limit and self.count >= self.limit:
				drop = self.cacheline.popleft ()
				del self.cache[drop]
			else:
				self.count += 1
		self.cache[key] = self.Entry (value, self.active)
		self.cacheline.append (key)

	def __delitem__ (self, key: Any) -> None:
		del self.cache[key]
		self.cacheline.remove (key)
		self.count -= 1

	def __len__ (self) -> int:
		return len (self.cache)

	def __contains__ (self, key: Any) -> bool:
		return key in self.cache

	def reset (self) -> None:
		"""clears the cache"""
		self.count = 0
		self.cache = {}
		self.cacheline = collections.deque ()

	def remove (self, key: Any) -> None:
		"""remove ``key'' from cache, if ``key'' is in cache"""
		if key in self.cache:
			del self[key]

	def expire (self, blocksize: int = 1000) -> None:
		"""expires outdated cache entries, work in ``blocksize'' chunks"""
		if self.active:
			now = time.time ()
			while True:
				toRemove = []
				for key, e in self.cache.items ():
					if not e.valid (now, self.timeout):
						toRemove.append (key)
						if blocksize and len (toRemove) == blocksize:
							break
				if not toRemove:
					break
				for key in toRemove:
					del self[key]
		if self.limit:
			while self.count > self.limit:
				del self[self.cacheline[0]]
