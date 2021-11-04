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
import	re, json
from	typing import Any, Callable, Optional
from	typing import Dict, ItemsView, KeysView, List, NamedTuple, ValuesView
from	typing import cast
from	.exceptions import error
from	.ignore import Ignore
from	.stream import Stream
from	.tools import atob

__all__ = ['Parameter', 'SimpleParameter', 'MailingParameter']
#
class Parameter:
	"""Parsing comma separated list of key value pairs

This class offers a parser for key-value pairs in a comma separted
list with different methods of persistance.
"""
	__slots__ = ['methods', 'data']
	class Codec (NamedTuple):
		decode: Callable[[Dict[str, Any], str, Dict[str, str]], None]
		encode: Callable[[Dict[str, Any], Dict[str, str]], str]
		ctx: Dict[str, Any]
	skip_pattern = re.compile (',[ \t]*')
	decode_pattern = re.compile ('([@$a-z0-9_-]+)[ \t]*=[ \t]*"([^"]*)"', re.IGNORECASE | re.MULTILINE)
	def __decode (self, ctx: Dict[str, Any], s: str, target: Dict[str, str]) -> None:
		while s:
			mtch = self.skip_pattern.match (s)
			if mtch is not None:
				s = s[mtch.end ():]
			mtch = self.decode_pattern.match (s)
			if mtch is not None:
				(var, val) = mtch.groups ()
				target[var] = val
				s = s[mtch.end ():]
			else:
				break

	def __encode (self, ctx: Dict[str, Any], source: Dict[str, str]) -> str:
		for value in source.values ():
			if '"' in value:
				raise ValueError (f'Unable to enocde "{value}" due to "')
		return ', '.join (['{var}="{val}"'.format (var = str (_d[0]), val = str (_d[1])) for _d in source.items ()])

	def __init__ (self, s: Optional[str] = None) -> None:
		"""String to be parsed"""
		self.methods: Dict[Optional[str], Parameter.Codec] = {}
		self.add_method (None, self.__decode, self.__encode)
		def jsonDecode (ctx: Dict[str, Any], s: str, target: Dict[str, str]) -> None:
			d = cast (json.JSONDecoder, ctx['decoder']).decode (s)
			if type (d) != dict:
				raise ValueError (f'JSON: input {s} did not lead into a dictionary')
			for (var, val) in d.items ():
				target[var] = val
		def jsonEncode (ctx: Dict[str, Any], source: Dict[str, str]) -> str:
			return cast (json.JSONEncoder, ctx['encoder']).encode (source)
		ctx = self.add_method ('json', jsonDecode, jsonEncode)
		ctx['decoder'] = json.JSONDecoder ()
		ctx['encoder'] = json.JSONEncoder ()
		self.data: Dict[str, str] = {}
		if s is not None:
			self.loads (s)
	
	def __str__ (self) -> str:
		return '{name} <{repr}>'.format (
			name = self.__class__.__name__,
			repr = Stream (self.data.items ())
				.map (lambda kv: '{key}={value}'.format (key = kv[0], value = kv[1]))
				.join (', ')
		)
	
	def __eq__ (self, other: object) -> bool:
		if not isinstance (other, Parameter):
			raise NotImplementedError ()
		return self.data == other.data

	def __getitem__ (self, var: str) -> str:
		return self.data[var]

	def __setitem__ (self, var: str, val: str) -> None:
		self.data[var] = val
	
	def __delitem__ (self, var: str) -> None:
		del self.data[var]

	def __contains__ (self, var: str) -> bool:
		return var in self.data

	def __iter__ (self) -> KeysView[str]:
		return self.keys ()
	
	def __call__ (self, var: str, default: Any = None) -> Any:
		"""Alias for Parameter.get"""
		return self.get (var, default)
	
	def keys (self) -> KeysView[str]:
		return self.data.keys ()
	
	def values (self) -> ValuesView[str]:
		return self.data.values ()
	
	def items (self) -> ItemsView[str, str]:
		return self.data.items ()

	def set (self, newdata: Dict[str, str]) -> None:
		"""Set whole content"""
		self.data = newdata

	def get (self, var: str, default: Any = None) -> Any:
		"""Get value as string"""
		try:
			return self.data[var]
		except KeyError:
			return default

	def iget (self, var: str, default: int = 0) -> int:
		"""Get value as integer"""
		try:
			return int (self.data[var])
		except (KeyError, ValueError, TypeError):
			return default

	def fget (self, var: str, default: float = 0.0) -> float:
		"""Get value as float"""
		try:
			return float (self.data[var])
		except (KeyError, ValueError, TypeError):
			return default

	def bget (self, var: str, default: bool = False) -> bool:
		"""Get value as boolean"""
		try:
			return atob (self.data[var])
		except KeyError:
			return default

	def add_method (self,
		method: Optional[str],
		decoder: Callable[[Dict[str, Any], str, Dict[str, str]], None],
		encoder: Callable[[Dict[str, Any], Dict[str, str]], str]
	) -> Dict[str, Any]:
		"""Add persistance coding

Add a new method for encoding/decoding data for persistance. If this
method already exists, it is overwritten wirh the new codec."""
		m = self.Codec (decoder, encoder, {})
		self.methods[method] = m
		return m.ctx

	def has_method (self, method: Optional[str]) -> bool:
		"""Check if a named method exists"""
		return method in self.methods

	def loads (self, s: str, method: Optional[str] = None) -> None:
		"""Parse a persistent data chunk

parses the chunk passed in s according to the ``method''. If the
method is missing, the default method is used """
		self.data.clear ()
		if method not in self.methods:
			raise LookupError (f'Unknown decode method: {method}')
		m = self.methods[method]
		m.decode (m.ctx, s, self.data)

	def dumps (self, method: Optional[str] = None) -> str:
		"""Build a persistant representation of current state"""
		if method not in self.methods:
			raise LookupError (f'Unknown encode method: {method}')
		m = self.methods[method]
		return m.encode (m.ctx, self.data)
	
	def clear (self) -> None:
		"""Clear out all data"""
		self.data.clear ()

class SimpleParameter (Parameter):
	"""Handle simple parameter constructs without quotes"""
	__slots__: List[str] = []
	def __simple_decode (self, ctx: Dict[str, Any], s: str, target: Dict[str, str]) -> None:
		for entry in s.split (','):
			with Ignore (ValueError):
				(key, value) = entry.split ('=', 1)
				target[key.strip ()] = value.strip ()

	def __simple_encode (self, ctx: Dict[str, Any], source: Dict[str, str]) -> str:
		return (Stream (source.items ())
			.error (lambda kv: ',' in kv[0] or ',' in kv[1], lambda e: error (f'no comma allowed for SimpleParameter in key "{e[0]}" or value "{e[1]}"'))
			.map (lambda kv: f'{kv[0]}={kv[1]}')
			.join (',')
		)
		
	def __init__ (self, s: Optional[str] = None) -> None:
		super ().__init__ ()
		self.add_method (None, self.__simple_decode, self.__simple_encode)
		if s is not None:
			self.loads (s)

class MailingParameter (Parameter):
	"""Derivated class to handle format as found in mailing_mt_tbl"""
	__slots__: List[str] = []
	import	string
	
	startOfName = string.ascii_lowercase
	partOfName = string.ascii_lowercase + string.digits
	def __mpDecode (self, ctx: Dict[str, Any], s: str, target: Dict[str, str]) -> None:
		pos = 0
		state = 0
		while pos < len (s):
			ch = s[pos]
			if state == 0:
				if ch in self.startOfName:
					start = pos
					state = 1
			elif state == 1:
				if ch == '=':
					variable = s[start:pos]
					state = 2
				elif ch not in self.partOfName:
					state = 0
			elif state == 2:
				if ch == '"':
					state = 3
					value = ''
				else:
					state = 0
			elif state == 3:
				if ch == '"':
					target[variable] = value
					state = 0
				elif ch == '\\':
					state = 4
				else:
					value += ch
			elif state == 4:
				value += ch
				state = 3
			pos += 1

	__mpPattern = re.compile ('["\\\\]')
	def __mpEncode (self, ctx: Dict[str, Any], source: Dict[str, str]) -> str:
		out = []
		def escape (s: str) -> str:
			return self.__mpPattern.sub (lambda a: '\\{match}'.format (match = a.group (0)), s)
		for (variable, value) in source.items ():
			out.append ('{var}="{val}"'.format (var = variable, val = escape (value)))
		return ', '.join (out)

	def __init__ (self, s: Optional[str] = None) -> None:
		super ().__init__ ()
		self.add_method (None, self.__mpDecode, self.__mpEncode)
		if s is not None:
			self.loads (s)
