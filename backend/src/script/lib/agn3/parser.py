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
import	re, time, csv
from	collections import namedtuple
from	datetime import datetime
from	functools import partial
from	io import StringIO
from	itertools import takewhile, zip_longest
from	urllib.parse import quote, unquote
from	typing import Any, Callable, Iterable, Optional, Protocol, Union
from	typing import Dict, Iterator, List, NamedTuple, Set, Tuple, Type
from	typing import cast
from	.exceptions import error
from	.ignore import Ignore
from	.stream import Stream
#
__all__ = ['ParseTimestamp', 'Period', 'Unit', 'unit', 'Line', 'Field', 'Lineparser', 'Tokenparser']
#
Parsable = Union[None, int, float, str]
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
>>> unit = Unit ()
>>> unit.parse ('30m')
1800
>>> unit.parse ('3m30')
210
>>> unit.parse ('2K 512B')
2560
"""
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
	__decode = {
		'time': [(60, 'sec'), (60, 'min'), (24, 'hour'), (0, 'day')],
		'size': [(1024, 'B'), (1024, 'K'), (1024, 'M'), (1024, 'G'), (1024, 'T')]
	}
	@classmethod
	def decode (cls, value: int, name: str = 'time', seps: Tuple[str, str] = (' ', ' ')) -> str:
		rc: List[Tuple[int, str]] = []
		decoder = cls.__decode[name]
		if value < 0:
			sign = '-'
			value = abs (value)
		else:
			sign = ''
		if value == 0:
			rc.append ((0, decoder[0][1]))
		else:
			while value > 0 and decoder:
				(div, unit) = decoder.pop (0)
				if div == 0:
					current = value
					value = 0
				else:
					current = value % div
					value //= div
				if current > 0:
					rc.insert (0, (current, unit))
		return Stream (rc).map (lambda vu: '{v}{s}{u}{m}'.format (v = vu[0], s = seps[0], u = vu[1], m = '' if vu[0] == 1 else 's')).join (seps[1], finisher = lambda v: f'{sign}{v}')

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

	def __call__ (self, expr: Parsable, default: int = 0, default_multiply: int = 1) -> int:
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
			for m in self.__eparse.finditer (expr):
				(i, u) = m.groups ()
				if u:
					mult = self.eunit[u]
				else:
					mult = default_multiply
				rc += int (i) * mult
			return rc
		return default
	parse = __call__
unit = Unit ()

class Line (Protocol):
	def __init__ (self, *args: Any, **kws: Any): ...
	def __getattr__ (self, name: str) -> Any: ...
	def __getitem__ (self, item: int) -> Any: ...

class Field (NamedTuple):
	name: str
	converter: Optional[Callable[[str], Any]] = None
	optional: bool = False
	default: Optional[Callable[[str], Any]] = None
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
			elements = self.splitter (line.rstrip ('\r\n'))
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
					return f.converter (e) if f.converter is not None else e
				if not f.optional:
					raise error (f'{f.name}: missing value')
				return f.default (f.name) if f.default is not None else None
			return self.target_class (*tuple (convert (_f, _e) for (_f, _e) in zip_longest (self.fields, elements)))
		except Exception as e:
			raise error (f'{line}: failed to parse: {e}')
	
	def make (self, **kws: Any) -> Line:
		return self.target_class (*tuple (kws.get (_f.name, _f.default (_f.name) if _f.default is not None else None) for _f in self.fields))

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

class Coder:
	class Logic (NamedTuple):
		encode: Callable[[str], str]
		decode: Callable[[str], str]
		save_encode: Optional[Callable[[str], str]] = None
		save_decode: Optional[Callable[[str], str]] = None
	coders: Dict[str, Logic] = {
		'default': Logic (
			encode = quote,
			decode = unquote,
			save_decode = lambda s: unquote (s, errors = 'backslashreplace')
		)
	}
	@classmethod
	def find (cls, name: str, save: bool = True) -> Tuple[Callable[[str], str], Callable[[str], str]]:
		logic = cls.coders[name]
		encode, decode = logic.encode, logic.decode
		if save and (logic.save_encode or logic.save_decode):
			def save_coder (coder: Callable[[str], str], save_coder: Callable[[str], str], s: str) -> str:
				try:
					return coder (s)
				except:
					return save_coder (s)
			if logic.save_encode is not None:
				encode = partial (save_coder, logic.encode, logic.save_encode)
			if logic.save_decode is not None:
				decode = partial (save_coder, logic.decode, logic.save_decode)
		return (encode, decode)

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
	__slots__ = ['fields', 'fieldmap', 'target_class', 'post_encoder', 'pre_decoder']
	def __init__ (self, *fields: Union[Field, str]) -> None:
		self.fields: List[Field] = [_f if isinstance (_f, Field) else Field (_f, lambda a: a) for _f in fields]
		self.fieldmap: Dict[str, Field] = Stream (self.fields).map (lambda f: (f.name, f)).dict ()
		self.target_class = cast (Type[Line], namedtuple ('Line', tuple (_f.name for _f in self.fields)))
		self.post_encoder: Optional[Callable[[str], str]] = None
		self.pre_decoder: Optional[Callable[[str], str]] = None

	def set_pre_decoder (self, name: Optional[str] = None) -> None:
		if name is not None:
			self.post_encoder, self.pre_decoder = Coder.find (name)
		else:
			self.post_encoder, self.pre_decoder = None, None
			
	def __call__ (self, line: Union[None, str, Dict[str, str]]) -> Line:
		if line is None:
			tokens: Dict[str, str] = {}
		elif isinstance (line, str):
			tokens = self.parse (line)
		else:
			tokens = line
		def get (field: Field, tokens: Dict[str, str]) -> Any:
			try:
				value = tokens[field.source if field.source is not None else field.name]
				return field.converter (value) if field.converter is not None else value
			except KeyError:
				if not field.optional:
					raise
				return field.default (field.name) if field.default is not None else None
		return self.target_class (*tuple (get (_f, tokens) for _f in self.fields))
	
	def parse (self, line: str) -> Dict[str, str]:
		return (Stream (line.split ('\t'))
			.map (lambda l: tuple (_l.strip () for _l in l.split ('=', 1)))
			.filter (lambda kv: len (kv) == 2)
			.map (lambda kv: kv if self.pre_decoder is None else (kv[0], self.pre_decoder (kv[1])))
			.dict ()
		)
	
	def create (self, record: Dict[str, Any]) -> str:
		seen: Set[str] = set ()
		use: Dict[str, Any] = (Stream (record.items ())
			.filter (lambda kv: kv[0] in self.fieldmap)
			.peek (lambda kv: seen.add (kv[0]))
			.dict ()
		)
		use.update (Stream (self.fields)
			.filter (lambda f: f.name not in seen and not f.optional)
			.map (lambda f: (f.name, f.default ('') if f.default is not None else ''))
			.dict ()
		)
		return (Stream (use.items ())
			.map (lambda kv: '{k}={v}'.format (k = kv[0], v = str (kv[1]) if self.post_encoder is None else self.post_encoder (str (kv[1]))))
			.join ('\t')
		)
