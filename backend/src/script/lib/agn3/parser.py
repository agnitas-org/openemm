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
import	re, time, csv
from	collections import namedtuple
from	datetime import datetime
from	io import StringIO
from	itertools import takewhile, zip_longest
from	typing import Any, Callable, Iterable, Optional, Protocol, Union
from	typing import Dict, Iterator, List, NamedTuple, Type
from	typing import cast
from	.exceptions import error
from	.ignore import Ignore
from	.stream import Stream
#
__all__ = ['ParseTimestamp', 'Period', 'Unit', 'Line', 'Field', 'Lineparser', 'Tokenparser']
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
		(re.compile ('({months}) +([0-9]+) ([0-9]{{2}}):([0-9]{{2}}):([0-9]{{2}})'.format (months = '|'.join (__months.keys ())), re.IGNORECASE), None)
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
			if isinstance (expr, str):
				for (pattern, seq) in self.__parser:
					m = pattern.match (expr)
					if m is not None:
						g = m.groups ()
						if seq is None:
							try:
								return self.__parse_sendmail_logdate (*g)
							except Exception as e:
								raise error (f'{expr}: failed to parse: {e}')
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
				if expr == 'now':
					now = time.localtime ()
					return datetime (now.tm_year, now.tm_mon, now.tm_mday, now.tm_hour, now.tm_min, now.tm_sec)
				if expr == 'epoch':
					return datetime (1970, 1, 1)
			#
			if isinstance (expr, int) or isinstance (expr, float):
				return datetime.fromtimestamp (expr)
			#
			if isinstance (expr, time.struct_time):
				return datetime (expr.tm_year, expr.tm_mon, expr.tm_mday, expr.tm_hour, expr.tm_min, expr.tm_sec)
			#
			if isinstance (expr, datetime):
				return expr
		return default (*args) if default is not None and callable (default) else default

	def parse (self, expr: Any, default: datetime) -> datetime:
		rc = self (expr)
		return rc if rc is not None else default

	def dump (self, d: datetime) -> str:
		"""returns ``d'' (datetime.datetime) as a parsable string representation"""
		return f'{d.year:04d}-{d.month:02d}-{d.day:02d} {d.hour:02d}:{d.minute:02d}:{d.second:02d}'

class Period (ParseTimestamp):
	"""Represents a period of time"""
	__slots__ = ['start', 'end']
	def __init__ (self, start: Any, end: Any) -> None:
		super ().__init__ ()
		def fail (what: str, s: Any) -> datetime:
			raise error (f'failed to parse {what}: {s!r}')
		self.start = self (start, fail, 'start', start)
		self.end = self (end, fail, 'end', end)
	
	def __str__ (self) -> str:
		return f'{self.start} .. {self.end}'
	
	def __repr__ (self) -> str:
		return f'<class Period ({self.start!r}, {self.end!r})>'

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
		if isinstance (expr, int):
			return expr
		if isinstance (expr, float):
			return int (expr)
		#
		with Ignore (ValueError):
			rc = 0
			scan = self.__eparse.scanner (expr)
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

class Line (Protocol):
	def __init__ (self, *args: Any, **kws: Any): ...
	def __getattr__ (self, name: str) -> Any: ...
	def __getitem__ (self, item: int) -> Any: ...

class Field (NamedTuple):
	name: str
	converter: Callable[[str], Any]
	optional: bool = False
	default: Optional[Callable[[], Any]] = None
	source: Optional[str] = None

class Lineparser:
	"""Parse and convert a single line

A instance of this class is callable to validate and preparse an input
line. When creating an instance one must pass a splitter function
which splits an input line into its elements (if not used as splitting
is done elsewhere or the default split at semicolon is sufficient,
this can be ``None''). 

Every further parameter must either be of type ``str'' or ``Field''.
If a parameter is of type ``str'', then this is the name of the field
and no further conversion takes place (e.g. this is a shortcut for:
	Field(name, lambda a: a)

Calling the instance with a single line this will either result in a
namedtuple of type ``Line'' (representing the definied fields) or
raise an ``error''. """
	__slots__ = ['splitter', 'fields', 'target_class', 'column_count', 'required_count', 'scratch']
	def __init__ (self, splitter: Optional[Callable[[str], List[str]]] = None, *fields: Union[Field, str]) -> None:
		self.splitter: Callable[[str], List[str]] = splitter if splitter is not None else lambda a: a.split (';')
		self.fields: List[Field] = [_f if isinstance (_f, Field) else Field (_f, lambda a: a) for _f in fields]
		self.target_class = cast (Type[Line], namedtuple ('Line', tuple (_f.name for _f in self.fields)))
		self.column_count = len (fields)
		self.required_count = sum (1 for _ in takewhile (lambda f: not f.optional, self.fields))
		self.scratch = StringIO ()
	
	def __call__ (self, line: Union[str, List[str]]) -> Line:
		if isinstance (line, str):
			elements = self.splitter (line)
		else:
			elements = line
		if len (elements) < self.required_count or len (elements) > self.column_count:
			raise error ('{line}: expected {minimum} .. {maximum} elements, got {got} elements'.format (
				line = line,
				minimum = self.required_count,
				maximum = self.column_count,
				got = len (elements)
			))
		try:
			def convert (f: Field, e: Optional[str]) -> Any:
				if e is not None:
					return f.converter (e)
				if not f.optional:
					raise error (f'{f.name}: missing value')
				return f.default () if f.default is not None else None
			return self.target_class (*tuple (convert (_f, _e) for (_f, _e) in zip_longest (self.fields, elements)))
		except Exception as e:
			raise error (f'{line}: failed to parse: {e}')

	def from_csv (self, source: Iterable[str], dialect: Union[None, str, csv.Dialect, Type[csv.Dialect]] = None, **kws: Any) -> Iterator[Line]:
		rd = csv.reader (source, dialect = dialect, **kws) if dialect is not None else csv.reader (source, **kws)
		for row in rd:
			yield self (row)
	
	def as_csv (self, source: List[Any], dialect: Union[None, str, csv.Dialect, Type[csv.Dialect]] = None, **kws: Any) -> str:
		self.scratch.truncate (0)
		wr = csv.writer (self.scratch, dialect, **kws) if dialect is not None else csv.writer (self.scratch, **kws)
		wr.writerow (source)
		self.scratch.flush ()
		return self.scratch.getvalue ()

class Tokenparser:
	"""Parse a single line into name/value tokens

To use this class, you have to create a subclass and implement the
method ``parse'' to separate a line into its name/value tokens.

Or use the default implementation which uses TAB as token separater
and the equal sign for name/value separator.

Or parse the line into a ``dict'' and call the instance with this
dict. Useful, if you want to parse one single line into several parser
to get distinct results. This way, the overhead is marginal as the
line itself is only parsed once and processed only for the configured
target fields."""
	__slots__ = ['fields', 'target_class']
	def __init__ (self, *fields: Union[Field, str]) -> None:
		self.fields: List[Field] = [_f if isinstance (_f, Field) else Field (_f, lambda a: a) for _f in fields]
		self.target_class = cast (Type[Line], namedtuple ('Line', tuple (_f.name for _f in self.fields)))
	
	def __call__ (self, line: Union[None, str, Dict[str, str]]) -> Line:
		if line is None:
			tokens: Dict[str, str] = {}
		elif isinstance (line, str):
			tokens = self.parse (line)
		else:
			tokens = line
		def get (field: Field, tokens: Dict[str, str]) -> Any:
			try:
				return field.converter (tokens[field.source if field.source is not None else field.name])
			except KeyError:
				if not field.optional:
					raise
				return field.default () if field.default is not None else None
		return self.target_class (*tuple (get (_f, tokens) for _f in self.fields))
	
	def parse (self, line: str) -> Dict[str, str]:
		return (Stream (line.split ('\t'))
			.map (lambda l: tuple (_l.strip () for _l in l.split ('=', 1)))
			.filter (lambda kv: len (kv) == 2)
			.dict ()
		)
