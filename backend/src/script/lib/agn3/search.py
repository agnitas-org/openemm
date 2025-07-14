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
import	re, shlex, logging
from	datetime import datetime
from	fnmatch import fnmatchcase
from	typing import Any, Callable, Final, Optional
from	typing import Dict, List, Pattern, Type
from	.ignore import Ignore
from	.parser import parse_timestamp
from	.stream import Stream
from	.tools import atob
#
__all__ = ['Search']
#
logger = logging.getLogger (__name__)
#
class OP:
	__slots__ = ['source', 'ignorecase', 'str_source', 'value']
	internal_conversion: Dict[Type[Any], Callable[[str], Any]] = {
		datetime: parse_timestamp
	}
	def __init__ (self, source: Any, value: str, ignorecase: bool) -> None:
		self.source = source
		self.ignorecase = ignorecase
		self.str_source = str (source).lower () if ignorecase else str (source)
		self.value = value
	
	def __call__ (self, method: str, fallback: Optional[Callable[[str, str], bool]] = None) -> bool:
		if self.source is None:
			return False
		if not isinstance (self.source, str):
			try:
				if isinstance (self.source, bool):
					converter = atob
				else:
					converter = type (self.source)
				return bool (getattr (self.source, method) (converter (self.value)))
			except Exception:
				with Ignore ():
					result = getattr (self.source, method) (self.internal_conversion[type (self.source)] (self.value))
					if isinstance (result, bool):
						return result
				logger.warning (f'{self.source}: no conversion for {self.value!r} found')
		return fallback (self.str_source, self.value) if fallback is not None else bool (getattr (self.str_source, method) (self.value))
	
	def matches (self) -> bool:
		return self ('__eq__', fnmatchcase)
	def not_matches (self) -> bool:
		return self ('__ne__', lambda s, v: not fnmatchcase (s, v))
	def re_matches (self) -> bool:
		return self ('__eq__', lambda s, v: re.match (v, s, re.IGNORECASE if self.ignorecase else 0) is not None)
	def not_re_matches (self) -> bool:
		return self ('__ne__', lambda s, v: re.match (v, s, re.IGNORECASE if self.ignorecase else 0) is None)
	def equals (self) -> bool:
		return self ('__eq__')
	def not_equals (self) -> bool:
		return self ('__ne__')
	def less_than (self) -> bool:
		return self ('__lt__')
	def less_or_equal (self) -> bool:
		return self ('__le__')
	def greater_than (self) -> bool:
		return self ('__gt__')
	def greater_or_equal (self) -> bool:
		return self ('__ge__')
		
class Search:
	"""Poor men Search engine

This library allows to create somehow a search enigne like interface
for tools written in python. Originally part of maillog-search, this
had been generalized and provided as a library. Create an instance
with the search ``expression'', optional set ``default'' to the field
that is search by default, if no field is specified (default = '*')
and set ``ignorecase'' to True, if textual compares should be case
insensitive.

Call the instance with a dict ``source'' that contains searchable
content in each field. This returns either ``True'' on a match, else
``False''.

Example:
	
	s = Search ('email:*@test.com')
	print (s ({'email': 'info@test.com'})) # --> True
	

Simpler:
	
	s = Search ('*@test.com')
	print (s ({'*': 'info@test.com'})) # --> True

or:

	s = Search ('*@test.com', default = 'email')
	print (s ({'email': 'info@test.com'})) # --> True

Supported operators between the field name and its pattern are listed
here. If the field name is omitted, than ':' is the default operator.
If a field had not passed or its value is ``None'', each operator will
return ``False''.
	
	':': match, wildcardmatch on strings and on objects that are converted to strings and equal for all other types
	'!:': not match, inverse of above
	'=': equal, checks if pattern is equal to value
	'!=', '<>': not equal, inverse of above
	'<', '<=', '=<', less than and less or equal
	'>', '>=', '=>', greater than or greater or equal

It is possible to use brackets (but they have to stand alone, so you
have to surround them by spaces), and the boolean operations ``not'',
``and'' and ``or''.

For example:

>>> s = Search ('*@test.com or demo@*')
>>> s ({'*': 'info@test.com'})
True
>>> s ({'*': 'demo@example.com'})
True

or:

>>> s = Search ('*@test.com and demo@*')
>>> s ({'*': 'info@test.com'})
False
>>> s ({'*': 'demo@example.com'})
False
>>> s ({'*': 'demo@test.com'})
True

or:

>>> s = Search ('*@test.com and not demo@*')
>>> s ({'*': 'info@test.com'})
True
>>> s ({'*': 'demo@example.com'})
False
>>> s ({'*': 'demo@test.com'})
False


It's up to the developer to break up the searchable data into useful
fields.
"""
	__slots__ = ['expression', 'ns', 'default', 'ignorecase', 'auto_default', 'source', 'code']
	pattern: Final[Pattern[str]] = re.compile ('^([a-z_-]+)(!?[:=/]|=[<=>]?|[<>]=?)', re.IGNORECASE)
	def __init__ (self, expression: str, default: str = '*', ignorecase: bool = False, auto_default: bool = False) -> None:
		self.expression = expression
		self.ns: Dict[str, Any] = {}
		self.default = default
		self.ignorecase = ignorecase
		self.auto_default = auto_default
		statement: List[str] = []
		ops: Dict[str, str] = {
			':': 'matches',
			'!:': 'not_matches',
			'/': 're_matches',
			'!/': 'not_re_matches',
			'=': 'equals',
			'!=': 'not_equals',
			'<>': 'not_equals',
			'<': 'less_than',
			'<=': 'less_or_equal',
			'=<': 'less_or_equal',
			'>': 'greater_than',
			'>=': 'greater_or_equal',
			'=>': 'greater_or_equal',
		}
		op = 'OP'
		autoop: Optional[str] = None
		for (index, element) in enumerate (shlex.split (self.expression), start = 1):
			token = element.lower ()
			if token in ('(', ')', 'not', 'and', 'or'):
				statement.append (token)
				if token in ('and', 'or'):
					autoop = None
			else:
				if autoop is not None:
					statement.append (autoop)
				m = self.pattern.match (element)
				if m is not None:
					(field, operator) = m.groups ()
					pattern = element[m.end ():]
				else:
					(field, operator) = (default, ':')
					pattern = element
				placeholder = f'v{index}'
				self.ns[placeholder] = pattern.lower () if ignorecase else pattern
				statement.append ('{op} (source.get ({field!r}), {placeholder}, {ignorecase!r}).{operator} ()'.format (
					op = op,
					field = field,
					placeholder = placeholder,
					ignorecase = ignorecase,
					operator = ops[operator]
				))
				autoop = 'and'
		self.ns[op] = OP
		self.source = ' '.join (statement)
		self.code = compile (self.source, expression, 'eval')
	
	def __repr__ (self) -> str:
		return f'{self.__class__.__name__} (expression = {self.expression!r}, default = {self.default!r}, ignorecase = {self.ignorecase}, auto_default = {self.auto_default}) ->\n{self.source}'
	__str__ = __repr__

	def __call__ (self, source: Dict[str, Any]) -> bool:
		if self.auto_default and self.default not in source:
			auto_default = Stream (source.values ()).join ('\0')
			source = source.copy ()
			source[self.default] = auto_default
		return bool (eval (self.code, self.ns, {'source': source}))
