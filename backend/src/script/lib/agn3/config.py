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
import	os, re, time, csv
from	io import StringIO
from	datetime import datetime
from	xml.sax import xmlreader
from	typing import Any, Callable, Optional, Union
from	typing import Dict, IO, Iterator, List, Set, Tuple, Type
from	typing import overload
from	.definitions import base, host, program
from	.exceptions import error
from	.ignore import Ignore
from	.io import Line, csv_default, csv_named_reader
from	.parser import unit, parse_timestamp
from	.stream import Stream
from	.template import MessageCatalog, Template
from	.tools import Plugin, atob, listsplit
from	.xml import XMLReader, XMLWriter
#
__all__ = ['Config']
#
class Config:
	"""Configuration file handling

This class provides a complex handling for configuration file in INI
style. A configuration file is typically build out of a global section
and other sections with configuration parameter, e.g.:

param1 = value1
param2 = value2
[section1]
param2 = value3
param4 = value4
[section2]
param5 = value5

NOTE: Section names starting and ending with two underscores are not
allowed to be used by an application, these names are (like in python)
reserverd words for internal usage!

In general a variable is referenced by its section name, followed by a
dot and the parameter name. If the section is omited then the global
section is used. If a section is used and the parameter could not be
found in this section, the global section is consulted for that value,
e.g. related to the sample above:

- param2 -> value2
- section1.param2 -> value3
- section2.param2 -> value2
- section2.param4 -> *not existing*

The application may use different methods to retrieve a value which
allowes an interpretation of the value by this class. See the various
*get methods how they interpret the value. These returns a default
value if the parameter is not existing. One can also access the
parameter using a dict like syntax where a missing parameter results
in a KeyError exception.

A simple substitution logic can optional be enabled so you can
reference other configuration values using $section.parameter or
${section.parameter}. If the configuration files has this content:

location = nowhere
place = There is no place like $location
[home]
location = home place

results to
- place -> There is no place like nowhere
- home.place -> There is no place like home place
"""
	__slots__ = [
		'sections', 'fallback', 'overwrite', 'default_section', 'default_section_stack', 'section_sequence',
		'ns', 'lang', 'mc', 'getter', '_section'
	]
	comment_pattern = re.compile ('^[ \t]*(#.*)?$')
	section_pattern = re.compile ('^\\[([^]]+)\\]')
	include_pattern = re.compile ('^([^(]+)\\(([^)]*)\\)$')
	command_pattern = re.compile ('^:([a-z]+)([ \t]+(.*))?$')
	data_pattern = re.compile ('^[ \t]*([^ \t=]+)[ \t]*=[ \t]*(.*)$')
	def __init__ (self, fallback: Optional[Config] = None, overwrite: Optional[Config] = None, default_section: Optional[str] = None) -> None:
		"""``fallback'' allows to chain multiple instances, so
if a key is not found in this instance, ``fallback'' is consulted for
the value. ``overwrite'' can also be an instance which will be
consulted before the own data of this instance. With
``default_section'' one can select a section to be used if a
parametername has no section part."""
		self.sections: Dict[Optional[str], Dict[str, str]] = {None: {}}
		self.fallback = fallback
		self.overwrite = overwrite
		self.default_section = default_section
		self.default_section_stack: List[Optional[str]] = []
		self.section_sequence: List[str] = []
		self.ns: Dict[str, Any] = {}
		self.lang: Optional[str] = None
		self.mc: Optional[MessageCatalog] = None
		self.clear ()
		self.getter = self.__get
		self._section: Optional[str]
	
	def setup_namespace (self, **kwargs: Any) -> None:
		"""Setup the namespace used for tget method. Some
standard values are set for the namespace:
	- now: current date as time.struct_time
	- today: current date as datetime.datetime

This method also copies the environment of the process and any passed
keyword argument to the namespace. To further populate you can access
the namespace directly using the ``ns'' attribute."""
		self.ns['now'] = time.localtime ()
		self.ns['today'] = datetime.today ()
		self.ns.update (os.environ)
		self.ns.update (kwargs)
	
	def setup_date (self,
		offset: Union[int, float, str] = 0,
		name: Optional[str] = None,
		default: Optional[int] = None
	) -> None:
		"""Setup an additional datetime.datetime object for
the namespace. ``offset'' is the offset in days for the current day
(int) or as a key for the configuration file (str) where ``default''
is used as the default value if there is no value found in the current
configuration. ``name'' is the name to use for the namespace entry or
None to use the default "date"."""
		if name is None:
			name = 'date'
		if isinstance (offset, str):
			offset = self.iget (offset, default if default is not None else 0)
		try:
			now = self.ns['today']
		except KeyError:
			now = datetime.now ()
		if offset:
			then = now.fromordinal (now.toordinal () - offset)
			then = datetime (then.year, then.month, then.day, now.hour, now.minute, now.second)
		else:
			then = now
		self.ns[name] = then
	
	def enable_substitution (self) -> None:
		"""enable value substitution"""
		self.getter = self.__vget

	def disable_substitution (self) -> None:
		"""disable value substitution"""
		self.getter = self.__get
	
	def __parse_variable (self, var: str) -> Tuple[str, List[Optional[str]]]:
		parts = var.split ('.', 1)
		if len (parts) == 2:
			sections: List[Optional[str]] = [parts[0]]
			var = parts[1]
		else:
			sections = [self.default_section] + self.default_section_stack
		return (var, sections)
	
	def __get (self, var: str) -> str:
		if self.overwrite is not None:
			with Ignore (KeyError):
				return self.overwrite[var]
		(bvar, sections) = self.__parse_variable (var)
		for section in sections:
			with Ignore (KeyError):
				return self.sections[section][bvar]
		if sections[-1] is not None:
			with Ignore (KeyError):
				return self.sections[None][bvar]
		if self.fallback is not None:
			return self.fallback[var]
		raise KeyError (var)

	__pattern = re.compile ('\\$(\\$|[a-z0-9_-]+(\\.[a-z0-9_-]+)?|{([^}]*)})', re.IGNORECASE)
	def __rget (self, var: str, seen: Set[str]) -> str:
		val = self.__get (var)
		cur = 0
		while cur < len (val):
			m = self.__pattern.search (val, cur)
			if m is None:
				break
			#
			(start, end) = m.span ()
			g = m.groups ()
			var = g[2] if g[2] is not None else g[0]
			rplc: Optional[str]
			if var == '$':
				rplc = '$'
			elif var not in seen:
				copy = seen.copy ()
				copy.add (var)
				try:
					rplc = self.__rget (var, copy)
				except KeyError:
					rplc = None
			else:
				rplc = None
			if rplc is not None:
				val = val[:start] + rplc + val[end:]
				cur = start + len (rplc)
			else:
				cur = end
		return val

	def __vget (self, var: str) -> str:
		return self.__rget (var, set ())
		
	def __getitem__ (self, var: str) -> str:
		return self.getter (var)
	
	def __setitem__ (self, var: str, val: str) -> None:
		(var, sections) = self.__parse_variable (var)
		try:
			self.sections[sections[0]][var] = val
		except KeyError:
			self.sections[sections[0]] = {var: val}
			if sections[0] is not None:
				self.section_sequence.append (sections[0])
	
	def __contains__ (self, var: str) -> bool:
		try:
			self[var]
		except KeyError:
			return False
		return True
	
	def push (self, section: str) -> None:
		"""Use ``section'' as the new default section, keep previous for pop()"""
		self.default_section_stack.insert (0, self.default_section)
		self.default_section = section
	
	def pop (self) -> Optional[str]:
		"""Pop the last pushed section and use the previous default section"""
		if not self.default_section_stack:
			return None
		rc = self.default_section
		self.default_section = self.default_section_stack.pop (0)
		return rc

	@overload
	def get (self, var: str, default: None = ...) -> Optional[str]: ...
	@overload
	def get (self, var: str, default: str) -> str: ...
	def get (self, var: str, default: Optional[str] = None) -> Optional[str]:
		"""Retrieve the value for ``var'' as string, use ``default'' as default if ``var'' is not found"""
		try:
			return self[var]
		except KeyError:
			return default

	def iget (self, var: str, default: int = 0) -> int:
		"""Retrieve the value for ``var'' as integer, use ``default'' as default if ``var'' is not found"""
		try:
			return int (self[var])
		except (KeyError, ValueError, TypeError):
			return default
	
	def eget (self, var: str, default: Union[int, str] = 0) -> int:
		"""Retrieve the value for ``var'' as integer where the value is parsed from a unit string, use ``default'' as default if ``var'' is not found"""
		if isinstance (default, str):
			rc = unit.parse (self.get (var), unit.parse (default))
		else:
			rc = unit.parse (self.get (var), default)
		return rc if rc is not None else default
	
	def fget (self, var: str, default: float = 0.0) -> float:
		"""Retrieve the value for ``var'' as float, use ``default'' as default if ``var'' is not found"""
		try:
			return float (self[var])
		except (KeyError, ValueError, TypeError):
			return default
	
	def bget (self, var: str, default: bool = False) -> bool:
		"""Retrieve the value for ``var'' as boolean, use ``default'' as default if ``var'' is not found"""
		try:
			return atob (self[var])
		except KeyError:
			return default
	
	@overload
	def tget (self, var: str, default: None = ..., **kwargs: Any) -> Optional[str]: ...
	@overload
	def tget (self, var: str, default: str, **kwargs: Any) -> str: ...
	def tget (self, var: str, default: Optional[str] = None, **kwargs: Any) -> Optional[str]:
		"""Retrieve the value for ``var'' as str where the value is used as a template which is filled using the current namespace, use ``default'' as default if ``var'' is not found"""
		val = self.get (var, default)
		if val is not None:
			tmpl = Template (val)
			if kwargs:
				if self.ns:
					ns = self.ns.copy ()
					ns.update (kwargs)
				else:
					ns = kwargs
			else:
				ns = self.ns
			val = tmpl.fill (ns, lang = self.lang, mc = self.mc)
		return val

	def __dateparse (self, s: str) -> datetime:
		rc = parse_timestamp (s)
		if rc is None:
			raise ValueError (f'unparsable date exprssion {s!r}')
		return rc

	@overload
	def dget (self, var: str, default: None = ...) -> Optional[datetime]: ...
	@overload
	def dget (self, var: str, default: Union[str, datetime]) -> datetime: ...
	def dget (self, var: str, default: Union[None, str, datetime] = None) -> Optional[datetime]:
		"""Retrieve the value for ``var'' as datetime.datetime, use ``default'' as default if ``var'' is not found"""
		try:
			return self.__dateparse (self[var])
		except (KeyError, ValueError):
			if default is None:
				return None
			elif isinstance (default, str):
				try:
					return self.__dateparse (default)
				except ValueError:
					return None
			else:
				return default

	def __listsplit (self,
		val: Union[None, List[str], Tuple[str], str],
		list_separator: Optional[str],
		modify: Optional[Callable[[str], Any]],
		modify_default: Any,
		not_null: bool,
		not_empty: bool
	) -> List[Any]:
		rc: List[Any]
		if isinstance (val, list):
			rc = val
		elif isinstance (val, tuple):
			rc = list (val)
		elif val and isinstance (val, str):
			rc = re.compile (list_separator if list_separator is not None else ', *', re.MULTILINE).split (val)
			if rc and not_empty:
				rc = [_v for _v in rc if _v]
			if rc:
				if modify is None:
					rc = [_v.strip () for _v in rc]
				elif callable (modify):
					def modifier (v: str) -> Any:
						try:
							return modify (v)
						except:
							return modify_default
					rc = [modifier (_v) for _v in rc]
			if rc and not_null:
				rc = [_v for _v in rc if _v is not None]
		else:
			rc = []
		return rc
		
	def lget (self,
		var: str,
		default: Any = None,
		list_separator: Optional[str] = None,
		modify: Optional[Callable[[str], Any]] = None,
		modify_default: Any = None,
		not_null: bool = False,
		not_empty: bool = False
	) -> List[Any]:
		"""Retrieve the value for ``var'' as list, use ``default'' as default if ``var'' is not found

``listSeparater'' is the regex to separate each element of the list
(default ", *", a comma separated list with option spaces after the
comma), ``modify'' can be a callable which is is called to modify each
element, ``modify_default'' is used for an element, if ``modify''
raises an exception. ``not_null'' removes all elements from the which
are None, ``not_empty'' removes all elements from the list, which bool
value is False."""
		return self.__listsplit (self.get (var, default), list_separator, modify, modify_default, not_null, not_empty)
	
	def mget (self,
		var: str,
		default: Any = None,
		record_separator: Optional[str] = None,
		key_separator: Optional[str] = None,
		key_convert: Optional[Callable[[str], Any]] = None,
		value_convert: Optional[Callable[[str, str], Any]] = None
	) -> Dict[Any, Any]:
		"""Retrieve the value for ``var'' as dict, use ``default'' as default if ``var'' is not found

``record_separator''is the regex to separate each key/value pair
(default newlines), ``key_separator'' is the regex to separate the key
from its value (default ": *"). ``key_convert'' and ``value_convert''
are optional callables which are used to convert the key respective
the value before storing them to the dict."""
		rc = {}
		val = self.get (var, default)
		if val:
			rsep = re.compile (record_separator if record_separator is not None else '(\n\r?)+', re.MULTILINE)
			ksep = re.compile (key_separator if key_separator is not None else ': *', re.MULTILINE)
			for r in rsep.split (val):
				e = ksep.split (r, 1)
				if len (e) == 2:
					value = e[1].strip ()
					if len (value) >= 2 and value[0] in '\'"' and value[0] == value[-1]:
						value = value[1:-1]
					rc[key_convert (e[0]) if key_convert is not None else e[0].strip ()] = value_convert (e[0], value) if value_convert is not None else value
		return rc
	
	def mlget (self,
		var: str,
		default: Any = None,
		record_separator: Optional[str] = None,
		key_separator: Optional[str] = None,
		key_convert: Optional[Callable[[str], Any]] = None,
		value_convert: Optional[Callable[[str, str], Any]] = None,
		list_separator: Optional[str] = None,
		modify: Optional[Callable[[str], Any]] = None,
		modify_default: Any = None,
		not_null: bool = False,
		not_empty: bool = False
	) -> Dict[Any, List[Any]]:
		"""Retrieve the value for ``var'' as dict with list as values, use ``default'' as default if ``var'' is not found

this is a combination of mget and lget which results in a dict where
the values are parsed as list. For description of the parameter,
consult mget and lget repective."""
		rc = self.mget (var, default, record_separator, key_separator, key_convert, value_convert)
		for (var, val) in rc.items ():
			rc[var] = self.__listsplit (val, list_separator, modify, modify_default, not_null, not_empty)
		return rc

	def csvget (self,
		var: str,
		dialect: Optional[str] = None
	) -> List[Line]:
		"""Retrieve the value for ``var'' as a csv and parse it into a two dimensional array. Use ``default'' if ``var is not found"""
		return list (csv_named_reader (csv.reader (StringIO (self[var]), dialect = dialect if dialect else csv_default)))
		
	def __pget (self,
		code: Optional[str],
		name: Optional[str],
		ns: Optional[Dict[str, Any]],
		plugin: Optional[Type[Plugin]]
	) -> Any:
		if code is not None:
			return (plugin if plugin is not None else Plugin) (code, name, ns)
		return None

	def pget (self,
		var: str,
		default: Any = None,
		name: Optional[str] = None,
		ns: Optional[Dict[str, Any]] = None,
		plugin: Optional[Type[Plugin]] = None
	) -> Any:
		"""Retrieve the value for ``var'' as a plugin, use ``default'' as default if ``var'' is not found

``name'' and ``ns'' are passed to Plugin() if a new copy is created,
otherwise ``plugin'' is used. The application using this method can
then use the returned object to call functions from this plugin."""
		return self.__pget (self.get (var, default), name, ns, plugin)
	def tpget (self,
		var: str,
		default: Any = None,
		name: Optional[str] = None,
		ns: Optional[Dict[str, Any]] = None,
		plugin: Optional[Type[Plugin]] = None,
		**kwargs: Any
	) -> Any:
		"""Retrieve the value for ``var'' as a plugin after tempalting, use ``default'' as default if ``var'' is not found

like pget(), but uses the tget() instead of get() to retrieve the
plugin code to prefill it using its own namespace."""
		return self.__pget (self.tget (var, default, **kwargs), name, ns, plugin)

	def keys (self) -> Iterator[str]:
		return iter (Stream (self.sections.items ())
			.map (lambda kv: (('{section}.{name}'.format (section = kv[0], name = _v)) if kv[0] is not None else _v for _v in kv[1]))
			.chain (str)
		)

	__internal = re.compile ('^__(.*)__$')
	def get_sections (self) -> List[str]:
		"""Retrieves a list of all known sections"""
		return [_k for _k in self.sections.keys () if _k is not None and self.__internal.match (_k) is None]
	
	def get_section_sequence (self) -> List[str]:
		"""Retrieves a list of all known sections in the sequence they are created"""
		return [_s for _s in self.section_sequence if self.__internal.match (_s) is None]
	
	def get_section (self, section: Optional[str]) -> Dict[str, str]:
		"""Get a complete ``section'' as a dict, no conversion of values take place"""
		if section in self.sections:
			rc = self.sections[section].copy ()
		else:
			rc = {}
		if self.overwrite:
			with Ignore (KeyError):
				ov = self.overwrite.get_section (section)
				for (var, val) in ov.items ():
					rc[var] = val
		if self.fallback:
			with Ignore (KeyError):
				fb = self.fallback.get_section (section)
				for (var, val) in fb.items ():
					if var not in rc:
						rc[var] = val
		return rc
	
	def get_section_as_config (self, section: str) -> Config:
		"""Get a complete ``section'' as a new instance of this class, no conversion of values take place"""
		data = self.get_section (section)
		rc = Config (self)
		for (var, val) in data.items ():
			rc.sections[None][var] = val
		rc.ns = self.ns.copy ()
		return rc
	
	def clear (self) -> None:
		"""Purge all configurations"""
		self.sections = {None: {}}
		self.section_sequence = []
	
	def filename (self) -> str:
		"""Returns the default filename for the configuration"""
		return os.path.join (base, 'scripts', f'{program}.cfg')
	
	def filenames (self) -> List[str]:
		"""Returns a list of possible locations for a configuration filename for reading"""
		return [_p for _p in (
			self.filename (),
			os.path.join (base, 'etc', f'{program}.cfg'),
			os.path.join (base, f'.{program}.rc'),
			f'{program}.cfg'
		) if os.path.isfile (_p) and os.access (_p, os.R_OK)]
		
	def write (self, fname: str) -> None:
		"""Write the configuration to ``fname''"""
		fd = open (fname, 'w')
		for section in sorted (self.sections, key = lambda a: '' if a is None else a):
			block = self.sections[section]
			if section is None:
				section = '*'
			if block:
				fd.write (f'[{section}]\n')
				for key in sorted (block):
					value = block[key]
					if '\n' in value:
						if value[-1] == '\n':
							value = value[:-1]
						fd.write (f'{key} = {{\n{value}\n}}\n')
					else:
						fd.write (f'{key} = "{value}"\n')
		fd.close ()

	def read (self, stream: Union[None, IO[Any], str] = None) -> None:
		"""Read configuration from ``fname''"""
		cur = self.sections[None]
		block: Optional[str] = None
		fd: Optional[IO[Any]] = None
		if stream is None:
			for path in self.filenames ():
				self.read (path)
			with Ignore (KeyError):
				fd = StringIO (os.environ['{program}_CONFIG'.format (program = program.upper ())])
		elif isinstance (stream, str):
			fd = open (stream)
		else:
			fd = stream
		fds: List[IO[Any]] = []
		while fd is not None:
			line = fd.readline ()
			if line == '':
				block = None
				if fd is not stream:
					fd.close ()
				try:
					fd = fds.pop (0)
				except IndexError:
					fd = None
			else:
				line = line.rstrip ('\r\n')
				if block is not None:
					if line.startswith ('}') and line.strip () == '}':
						block = None
					else:
						cur[block] += f'{line}\n'
				elif line and self.comment_pattern.match (line) is None:
					mtch = self.section_pattern.match (line)
					if mtch is not None:
						section_name = mtch.group (1)
						mtch = self.include_pattern.match (section_name)
						sname: Optional[str]
						includes: Optional[str]
						if mtch is not None:
							(sname, includes) = mtch.groups ()
						else:
							(sname, includes) = (section_name, None)
						if sname == '*':
							sname = None
						try:
							cur = self.sections[sname]
						except KeyError:
							cur = {}
							self.sections[sname] = cur
							if sname is not None:
								self.section_sequence.append (sname)
						if includes:
							seen = set ([sname])
							for include in listsplit (includes):
								if include not in seen:
									seen.add (include)
									if include in self.sections:
										for (var, val) in self.sections[include].items ():
											cur[var] = val
					elif (mtch := self.command_pattern.match (line)) is not None:
						grps = mtch.groups ()
						command = grps[0]
						param = grps[2]
						if command == 'abort':
							fd.close ()
							while len (fds) > 0:
								fds.pop (0).close ()
							fd = None
						elif command == 'close':
							fd.close ()
							if fds:
								fd = fds.pop (0)
							else:
								fd = None
						elif command == 'include':
							nfd = open (param, 'rt')
							fds.insert (0, fd)
							fd = nfd
					elif (mtch := self.data_pattern.match (line)) is not None:
						(var, val) = mtch.groups ()
						if val == '{':
							block = var
							cur[var] = ''
						else:
							if len (val) > 1 and val[0] in '\'"' and val[-1] == val[0]:
								val = val[1:-1]
							cur[var] = val
					else:
						raise error (f'Unparsable line: {line}')

	def write_xml (self, fname: str) -> None:
		"""Write configuration as xml file"""
		with open (fname, 'w') as fd:
			wr = XMLWriter (fd)
			wr.start ()
			wr.open ('config')
			for section in sorted (self.sections, key = lambda a: '' if a is None else a):
				block = self.sections[section]
				if section is None:
					section = "*"
				if block:
					wr.open ('section', name = section)
					for key in sorted (block):
						wr.text (key, block[key])
					wr.close ('section')
			wr.close ('config')
			wr.end ()
	
	def __read_section (self, path: str, name: str, attrs: xmlreader.AttributesImpl) -> None:
		try:
			sname: Optional[str] = attrs['name']
			if sname == '*':
				sname = None
		except KeyError:
			sname = None
		self._section = sname
	
	def __read_entry (self, path: str, name: str, attrs: xmlreader.AttributesImpl, content: Union[None, bool, str]) -> None:
		value = content if isinstance (content, str) else ''
		try:
			self.sections[self._section][name] = value
		except KeyError:
			self.sections[self._section] = {name: value}
			if self._section is not None:
				self.section_sequence.append (self._section)
	
	def read_xml (self, fname: str) -> None:
		"""Read configuration from xml file"""
		self._section = None
		rd = XMLReader ()
		rd.add_handler ('config.section', self.__read_section, None)
		rd.add_handler ('config.section.', None, self.__read_entry)
		rd.process_file (fname)

class HostConfig (Config):
	"""Derviated Configuration from agn3.config.Config

This class uses the hostname of the machine running the program as the
name of the default section. This enables using different
configuration for different instances depending on their hostname."""
	__slots__ = ['hostname']
	def __init__ (self, hostname: Optional[str] = None, **kwargs: Any):
		"""``hostname'', if not None, is used for the name of the default section, ``kwargs'' are added to the local namespace"""
		super ().__init__ (default_section = hostname if hostname is not None else host)
		self.setup_namespace (**kwargs)
		self.hostname = hostname if hostname is not None else host
