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
import	re, time
from	datetime import datetime
from	typing import Any, Callable, Optional, Union
from	typing import List
from	typing import cast
from	.exceptions import error
from	.ignore import Ignore
#
__all__ = ['ParseTimestamp', 'Period', 'Unit']
#
class ParseTimestamp:
	"""parses common used timestamps

the instance is called directly, i.e.:
	ParseTimestamp () ('2017-02-16 11:59:30')
"""
	__slots__: List[str] = []
	__months = {
		'jan':  1, 'feb':  2, 'mar':  3, 'apr':  4, 'may':  5, 'jun':  6,
		'jul':  7, 'aug':  8, 'sep':  9, 'oct': 10, 'nov': 11, 'dec': 12
	}
	__parser = [
		(re.compile ('([0-9]{4})-([0-9]{2})-([0-9]{2})([ :T]([0-9]{2}):([0-9]{2}):([0-9]{2}))?'), (0, 1, 2)),
		(re.compile (' *([0-9]{1,2})\\.([0-9]{1,2})\\.([0-9]{4})( +([0-9]{2}):([0-9]{2}):([0-9]{2}))?'), (2, 1, 0)),
		(re.compile ('(%s) +([0-9]+) ([0-9]{2}):([0-9]{2}):([0-9]{2})' % '|'.join (__months.keys ()), re.IGNORECASE), None)
	]

	def __parse_sendmail_logdate (self, month_name: str, day: str, hour: str, min: str, sec: str) -> datetime:
		month = self.__months[month_name.lower ()]
		now = datetime.now ()
		year = now.year
		if now.month < month:
			year -= 1
		return datetime (year, month, int (day), int (hour), int (min), int (sec))

	def __call__ (self, expr: Any, default: Union[None, datetime, Callable[..., datetime]] = None, *args: Any) -> Optional[datetime]:
		"""parses a string to a datetime.datetime representation

``expr'' may either be a supported time pattern or one of the static
strings "now" (for the current timestamp) or "epoch" (for the start of
the epoch, the 1.1.1970).

If the parsing failed and ``default'' is a callable method, then this is
called with ``*args'' as its argument to create the return value.
Otherwise None is returned."""
		if expr is not None:
			ty = type (expr)
			if ty is str:
				s = cast (str, expr)
				for (pattern, seq) in self.__parser:
					m = pattern.match (s)
					if m is not None:
						g = m.groups ()
						if seq is None:
							try:
								return self.__parse_sendmail_logdate (*g)
							except Exception as e:
								raise error (f'{s}: failed to parse: {e}')
						else:
							if g[3] is None:
								return datetime (int (g[seq[0]]), int (g[seq[1]]), int (g[seq[2]]))
							else:
								return datetime (
									int (g[seq[0]]),
									int (g[seq[1]]),
									int (g[seq[2]]),
									int (g[4]),
									int (g[5]),
									int (g[6])
								)
						break
				if s == 'now':
					now = time.localtime ()
					return datetime (now.tm_year, now.tm_mon, now.tm_mday, now.tm_hour, now.tm_min, now.tm_sec)
				if s == 'epoch':
					return datetime (1970, 1, 1)
			#
			if ty in (int, float):
				tm = time.localtime (cast (int, expr))
				return datetime (tm.tm_year, tm.tm_mon, tm.tm_mday, tm.tm_hour, tm.tm_min, tm.tm_sec)
			#
			if ty is time.struct_time:
				ts = cast (time.struct_time, expr)
				return datetime (ts.tm_year, ts.tm_mon, ts.tm_mday, ts.tm_hour, ts.tm_min, ts.tm_sec)
			#
			if ty is datetime:
				return cast (datetime, expr)
		return default (*args) if default is not None and callable (default) else default

	def parse (self, expr: Any, default: datetime) -> datetime:
		rc = self (expr)
		return rc if rc is not None else default

	def dump (self, d: datetime) -> str:
		"""returns ``d'' (datetime.datetime) as a parsable string representation"""
		return '%04d-%02d-%02d %02d:%02d:%02d' % (d.year, d.month, d.day, d.hour, d.minute, d.second)

class Period (ParseTimestamp):
	"""Represents a period of time"""
	__slots__ = ['start', 'end']
	def __init__ (self, start: Any, end: Any) -> None:
		super ().__init__ ()
		def fail (what: str, s: Any) -> datetime:
			raise error ('failed to parse %s: %r' % (what, s))
		self.start = self (start, fail, 'start', start)
		self.end = self (end, fail, 'end', end)
	
	def __str__ (self) -> str:
		return '%s .. %s' % (str (self.start), str (self.end))
	
	def __repr__ (self) -> str:
		return '<class Period (%r, %r)>' % (self.start, self.end)

class Unit:
	"""Parse a unit expression

Class to parse an expression containing common units. Custom unit
convertion values can be added. Examples:
unit = eagn.Unit ()
unit.parse ('3m30') -> 210
unit.parse ('2K 512B') -> 2560"""
	__slots__ = ['eunit']
	__eparse = re.compile ('([+-]?[0-9]+)([a-z]*)[ \t]*', re.IGNORECASE)
	__eunit = {
		's':	1,				# second
		'm':	60,				# minute
		'h':	60 * 60,			# hour
		'd':	24 * 60 * 60,			# day
		'w':	7 * 24 * 60 * 60,		# week
		
		'B':	1,				# byte
		'K':	1024,				# kByte
		'M':	1024 * 1024,			# MByte
		'G':	1024 * 1024 * 1024,		# GByte
		'T':	1024 * 1024 * 1024 * 1024,	# TByte
	}
	def __init__ (self, **kwargs: int) -> None:
		"""Further conversion units can be passed as keywords"""
		self.eunit = self.__eunit.copy ()
		self.eunit.update (kwargs)
		
	def __getitem__ (self, key: str) -> int:
		"""Returns unit value for ``key''"""
		return self.eunit[key]
	
	def __setitem__ (self, key: str, mult: int) -> None:
		"""Sets a unit value ``mult'' for unit ``key''"""
		self.eunit[key] = mult

	Parsable = Union[None, int, float, str]
	def __call__ (self, expr: Unit.Parsable, default: int = 0, default_multiply: int = 1) -> int:
		"""Parses a unit string"""
		if expr is None:
			return default
		if type (expr) is float:
			return int (expr)
		elif type (expr) is int:
			return cast (int, expr)
		#
		with Ignore (ValueError):
			rc = 0
			scan = self.__eparse.scanner (cast (str, expr))
			for m in iter (scan.search, None):
				(i, u) = m.groups ()
				if u:
					mult = self.eunit[u]
				else:
					mult = default_multiply
				rc += int (i) * mult
			return rc
		return default
	parse = __call__
