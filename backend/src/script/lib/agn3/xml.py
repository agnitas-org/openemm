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
import	re, logging
import	xml.sax
from	xml.sax import xmlreader
from	xml.sax.handler import ContentHandler, ErrorHandler
from	types import TracebackType
from	typing import Any, Callable, NoReturn, Optional, Union
from	typing import Dict, IO, List, Tuple, Type
from	.exceptions import error
from	.ignore import Ignore
from	.io import copen
#
__all__ = ['XMLWriter', 'XMLReader']
#
logger = logging.getLogger (__name__)
#
class XMLWriter:
	"""Writing XML files

This class offers a simple interface to write XML files in sequence
instead of the classic way to build a tree (DOM). This class has
two advantages over the DOM approach:
- Much simpler API
- Less resources (memory) are required, especially when writing
  huge XML files

The obvious disadvantage is the limited flexibility.

To use this class you instanciate it using an output class (which must
at least support a write() method) and optional the input character set
(UTF-8 is the default, if you pass unicode strings as input, the characher
set is not applied.)

The typical sequence in processing:
- start ()
- create Nodes using the methods
  - Simple: open, close, node, text, cdata
  - Advanced: create_node, create_text_node, create_text, create_cdate_node,
    create_cdata, close
  The main difference between simple and advance is, you pass a dictionary
  to the advance routines while you pass attributes as named paramter to
  the simple routings. Both share the close method to close a node.
- end ()

A simple example:
from io import StringIO
output = StringIO ()
writer = XMLWriter (output)
writer.start ()
writer.open ('root', version = '2.0', date = '2008-08-05')
writer.node ('revision', nr = '1.1')
writer.text ('author', 'Open EMM <openemm@agnitas.de>')
writer.close ('root')
writer.end ()
print (output.getvalue ())

will create this output:
<?xml version="1.0" encoding="UTF-8"?>
<root date="2008-08-05" version="2.0">
 <revision nr="1.1"/>
 <author>Open EMM &lt;openemm@agnitas.de&gt;</author>
</root>
"""
	__slots__ = ['output', 'output_charset', 'output_errors', 'backlog', 'state', 'partial']
	escmap = {'<': '&lt;', '>': '&gt;', '&': '&amp;', '"': '&quot;', '\'': '&apos;'}
	escape = re.compile ('[{special}]'.format (special = ''.join (escmap.keys ())))
	
	def __init__ (self, output: IO[Any], output_charset: Optional[str] = None, output_errors: Optional[str] = None, partial: bool = False) -> None:
		"""``output'' is a file like object to write the
content to. ``output_charset'', ``output_errors'' is passed to string
encoder to refuse invalid input, ``partial'' is False to emit valid
XML declaration."""
		self.output = output
		self.output_charset = output_charset if output_charset is not None else 'UTF-8'
		self.output_errors = output_errors if output_errors is not None else 'strict'
		self.backlog: List[str] = []
		self.state = 0
		self.partial = partial
	
	def __enter__ (self) -> XMLWriter:
		return self
	
	def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
		if exc_type is None:
			self.end ()
		return None

	def __out (self, s: str) -> None:
		if self.state == 0:
			self.start ()
		if self.state == 1:
			self.output.write (s.encode (self.output_charset, errors = self.output_errors))
	
	def __convert (self, s: str, cdata: bool = False) -> str:
		if cdata:
			chunks = s.split (']]>')
			clen = len (chunks)
			if clen > 1:
				combined = []
				for n in range (clen):
					combined.append ('<![CDATA[{prefix}{chunk}{postfix}]]>'.format (
						prefix = '>' if n > 0 else '',
						chunk = chunks[n],
						postfix = ']]' if n + 1 < clen else ''
					))
				return ''.join (combined)
			else:
				return f'<![CDATA[{s}]]>'
		else:
			return self.escape.sub (lambda ch: self.escmap[ch.group ()], s)
	
	def __new_node (self, name: str, attrs: Optional[Dict[str, str]], simple: bool, text: Optional[str], cdata: bool) -> None:
		out = '{indent}<{name}'.format (
			indent = ' ' * len (self.backlog),
			name = name
		)
		if attrs is not None:
			for (var, val) in attrs.items ():
				out += ' {var}="{val}"'.format (var = var, val =  self.__convert (val))
		if simple:
			if text is not None:
				out += '>{text}</{name}>\n'.format (text = self.__convert (text, cdata), name = name)
			else:
				out += '/>\n'
		else:
			self.backlog.append (name)
			out += '>\n'
			if text is not None:
				out += '{text}\n'.format (text = self.__convert (text))
		self.__out (out)
	
	def __end_node (self, name: str) -> None:
		self.__out ('{indent}</{name}>\n'.format (
			indent = ' ' * len (self.backlog),
			name = name
		))

	def open (_self, _name: str, **_attrs: Any) -> None:
		"""open a new node with ``name'' using keyword style attributes"""
		_self.__new_node (_name, _attrs, False, None, False)
	
	def close (self, name: Optional[str] = None) -> None:
		"""close an open node, if ``name'' is None, close the
last opened node, otherwise search for the node with the ``name'' and
close it (implicit close all inner nodes as well)"""
		if not self.backlog:
			raise error ('Empty backlog')
		if name is None:
			name = self.backlog.pop ()
			self.__end_node (name)
		else:
			if name not in self.backlog:
				raise error (f'{name} not found in backlog')
			while self.backlog:
				pname = self.backlog.pop ()
				self.__end_node (pname)
				if pname == name:
					break

	def node (_self, _name: str, **_attrs: Any) -> None:
		"""write a simple node, e.g. <node/>"""
		_self.__new_node (_name, _attrs, True, None, False)
	
	def text (_self, _name: str, _text: str, **_attrs: Any) -> None:
		"""write a simple node containing embedded text, e.g. <node>text</node>"""
		_self.__new_node (_name, _attrs, True, _text, False)
		
	def cdata (_self, _name: str, _text: str, **_attrs: Any) -> None:
		"""write a simple node containing embedded cdata"""
		_self.__new_node (_name, _attrs, True, _text, True)
	
	def create_node (self, name: str, attrs: Dict[str, Any], simple: bool = False) -> None:
		"""creates a new node ``name'' with ``attrs'' attributes as dict. Close the node if ``simple'' is True"""
		self.__new_node (name, attrs, simple, None, False)
	
	def create_text_node (self, name: str, text: str, attrs: Dict[str, Any], simple: bool = False) -> None:
		"""create a new node ``name'' with ``attrs'' attributes as dict with embeded text. Close the node if ``simple'' is True"""
		self.__new_node (name, attrs, simple, text, False)
	
	def create_cdata_node (self, name: str, text: str, attrs: Dict[str, Any], simple: bool = False) -> None:
		"""create a new node ``name'' with ``attrs'' attributes as dict with embeded cdata. Close the node if ``simple'' is True"""
		self.__new_node (name, attrs, simple, text, True)
	
	def create_text (self, text: str) -> None:
		"""output inlined text"""
		self.__out (self.__convert (text))
	
	def create_cdata (self, text: str) -> None:
		"""output inlined cdata"""
		self.__out (self.__convert (text, True))
	
	def start (self, doctype: Optional[str] = None) -> None:
		"""start the document, write the required XML header"""
		self.state = 1
		if not self.partial:
			self.__out (f'<?xml version="1.0" encoding="{self.output_charset}"?>\n')
		if doctype is not None:
			self.__out (doctype)
	
	def end (self) -> None:
		"""end the document, close all open nodes and flushes the output"""
		if self.state == 0:
			self.start ()
		if self.state == 1:
			while self.backlog:
				self.close ()
			with Ignore (AttributeError):
				self.output.flush ()
			self.state = 2
	
class XMLReader (ContentHandler, ErrorHandler):
	"""Provides a higher level XML reader based on SAX

This class offers a wrapper around the SAX interface by allowing to
provide callbacks for specific elements of the XML document.

To use this code, you should follow these steps:
- create an instance of XMLReader or a subclassed version of it:
  reader = XMLReader ()
- add callback handler for specific topics:
  reader.add_handler ('root.node.subnode', enterCallback, leaveCallback)
  where enterCallback and leaveCallback has these signatures:
  enterCallback (path, name, attrs)
  leaveCallback (path, name, attrs, content)
  The difference is that enterCallback is called when the tag is opened
  and leaveCallback when the tag is closed, where additional text between
  the start end end tag are passed, too. "path" contains the whole path
  to this node, for the above example 'root.node.subnode', "name" holds
  the name of the tag, in this example 'subnode'.
  Simple idea to use one callback for both cases:
  callback (path, name, attrs, content = False)
  reader.add_handler ('root.node.subnode', callback, callback)
  If "content is False", then this is called as the enter method,
  otherwise its the leave variant.
- add wildcard callback for whole bunch of elements:
  reader.add_handler ('root.otherNode.', enterCallbackWC, leaveCallbackWC)
  The final dot in the path does the trick.
- add regular expression callback for whole bunch of elemnts:
  reader.add_handler_regex ('.*\\.text$', enterCallbackRE, leaveCallbackRE)
- add class callback handler:
  reader.add_handler_callback (callback)
- then call either process_file (filename) or process_string (s) to handle
  the incoming XML document. The result is either XMLReader.OK on success
  or one of XMLReader.WARNING, ERROR, FATAL on failure.

An optional parameter, border, is supported by all add_handler methods. If
this is present, then the handler is only called, if an entry matches
between start and end of the border. To add more than one order, you have
to save the return value of an add_handler call and pass it as first
parameter to add_border with the additional border as the second parameter,
e.g.:
point = reader.add_handler_regex ('.*\\.text$', enterCallback, leaveCallback, 'root.node.subnode')
reader.add_border (point, 'root.node.othernode')

The callback is executed if at least one border has crossed, so do not expect
that all of them had been crossed.
"""
	__slots__ = ['dispatch', 'borders', 'dispatch_regex', 'dispatch_callback', 'stage', 'level', 'status']
	class Point:
		__slots__ = ['key', 'enter', 'leave', 'active']
		def __init__ (self,
			key: Optional[str],
			enter: Optional[Callable[[str, str, xmlreader.AttributesImpl], Any]],
			leave: Optional[Callable[[str, str, xmlreader.AttributesImpl, Optional[str]], Any]]
		) -> None:
			self.key = key
			self.enter = enter
			self.leave = leave
			self.active = 1
		
		def match (self, key: str) -> bool:
			return self.key is None or self.key == key
	
	class PointRe (Point):
		__slots__ = ['regexp']
		def __init__ (self,
			pattern: str,
			enter: Optional[Callable[[str, str, xmlreader.AttributesImpl], Any]],
			leave: Optional[Callable[[str, str, xmlreader.AttributesImpl, Optional[str]], Any]]
		) -> None:
			super ().__init__ (pattern, enter, leave)
			self.regexp = re.compile (pattern)
		
		def match (self, key: str) -> bool:
			return self.regexp.search (key) is not None
	
	class PointCB (Point):
		__slots__ = ['callback', 'method_cache']
		def __init__ (self, callback: Type[Any]) -> None:
			super ().__init__ (None, self, self)
			self.callback = callback
			self.method_cache: Dict[str, Optional[str]] = {}

		__normalizePattern = re.compile ('[^A-Za-z0-9_]')
		def __call__ (self, path: str, name: str, attrs: xmlreader.AttributesImpl, content: Union[None, bool, str] = False) -> None:
			try:
				method = self.method_cache[path]
			except KeyError:
				method = self.method_cache[path] = 'cb{name}'.format (name = ''.join ([self.__normalizePattern.sub ('_', _p).capitalize () for _p in path.split ('.')]))
			if method is not None:
				try:
					self.callback.__class__.__dict__[method] (self.callback, path, name, attrs, content)
				except (KeyError, TypeError):
					self.method_cache[path] = None
	
	class Level:
		__slots__ = ['path', 'name', 'attrs', 'content', 'points', 'active']
		def __init__ (self, path: str, name: str, attrs: xmlreader.AttributesImpl) -> None:
			self.path = path
			self.name = name
			self.attrs = attrs
			self.content: Optional[List[str]] = None
			self.points: List[XMLReader.Point] = []
			self.active = 0
		
		def add_points (self, points: List[XMLReader.Point]) -> None:
			if points:
				self.points += points
		
		def add_point (self, point: XMLReader.Point) -> None:
			self.points.append (point)
		
		def enter (self) -> None:
			for point in self.points:
				if point.active > 0:
					if point.enter:
						point.enter (self.path, self.name, self.attrs)
					self.active += 1
		
		def leave (self) -> None:
			if self.active:
				for point in self.points:
					if point.active > 0 and point.leave:
						point.leave (self.path, self.name, self.attrs, ''.join (self.content) if self.content is not None else None)
				self.content = None
		
		def add_content (self, content: str) -> None:
			if self.active:
				if self.content is None:
					self.content = [content]
				else:
					self.content.append (content)

	OK = 0
	WARNING = 1
	ERROR = 2
	FATAL = 3
	
	def __init__ (self) -> None:
		super ().__init__ ()
		self.dispatch: Dict[Optional[str], List[XMLReader.Point]] = {}
		self.borders: Dict[str, List[XMLReader.Point]] = {}
		self.dispatch_regex: List[XMLReader.Point] = []
		self.dispatch_callback: List[XMLReader.Point] = []
		self.stage: List[XMLReader.Level] = []
		self.level: Optional[XMLReader.Level] = None
		self.status = self.OK
	
	def __set_status (self, status: int) -> None:
		if status > self.status:
			self.status = status
	
	def reset_status (self) -> None:
		"""resets the working status to OK"""
		self.status = self.OK
	
	def add_border (self, point: XMLReader.Point, border: Optional[str]) -> None:
		"""add a border for a callback point"""
		if border is not None:
			try:
				self.borders[border].append (point)
			except KeyError:
				self.borders[border] = [point]
			point.active = 0
	
	def add_handler (self,
		key: Optional[str],
		enter: Optional[Callable[[str, str, xmlreader.AttributesImpl], Any]],
		leave: Optional[Callable[[str, str, xmlreader.AttributesImpl, Optional[str]], Any]],
		border: Optional[str] = None
	) -> XMLReader.Point:
		"""add a handler for a specific XML key"""
		point = self.Point (key, enter, leave)
		try:
			self.dispatch[key].append (point)
		except KeyError:
			self.dispatch[key] = [point]
		self.add_border (point, border)
		return point
		
	def add_handler_regex (self,
		pattern: str,
		enter: Optional[Callable[[str, str, xmlreader.AttributesImpl], Any]],
		leave: Optional[Callable[[str, str, xmlreader.AttributesImpl, Optional[str]], Any]],
		border: Optional[str] = None
	) -> XMLReader.PointRe:
		"""add a handler for a XML key regex"""
		point = self.PointRe (pattern, enter, leave)
		self.dispatch_regex.append (point)
		self.add_border (point, border)
		return point

	def add_handler_callback (self, callback: Type[Any], border: Optional[str] = None) -> XMLReader.PointCB:
		"""add a generic callback handler"""
		point = self.PointCB (callback)
		self.dispatch_callback.append (point)
		self.add_border (point, border)
		return point
	
	def process_stream (self, stream: IO[Any]) -> int:
		"""process a stream using the configured callbacks"""
		xml.sax.parse (stream, self, self)
		return self.status
	
	def process_file (self, filename: str) -> int:
		"""process a file using the configured callbacks"""
		with copen (filename, 'rb') as fd:
			self.process_stream (fd)
		return self.status
	
	def process_string (self, content: str) -> int:
		"""process a string using the configured callbacks"""
		xml.sax.parseString (content, self, self)
		return self.status

	#
	# Error handler
	def error (self, exc: BaseException) -> NoReturn:
		"""writes an error"""
		self.__set_status (self.ERROR)
		logger.exception (exc)
		raise exc
	def fatalError (self, exc: BaseException) -> NoReturn:
		"""writes a fatal error"""
		self.__set_status (self.FATAL)
		logger.exception (exc)
		raise exc
	def warning (self, exc: BaseException) -> None:
		"""writes a warning"""
		self.__set_status (self.WARNING)
		logger.warning (exc)
	#
	# Content handler
	def startDocument (self) -> None:
		self.stage = []
		self.level = None
	
	def endDocument (self) -> None:
		self.stage = []
		self.level = None
	
	def startElement (self, name: str, attrs: xmlreader.AttributesImpl) -> None:
		elem = [_s.name for _s in self.stage] + [name]
		key = '.'.join (elem)
		self.level = self.Level (key, name, attrs)
		self.stage.append (self.level)
		for exact in [None, key]:
			if exact in self.dispatch:
				self.level.add_points (self.dispatch[exact])
		while elem:
			elem.pop ()
			wkey = '.'.join (elem) + '.'
			if wkey in self.dispatch:
				self.level.add_points (self.dispatch[wkey])
		for point in self.dispatch_regex:
			if point.match (key):
				self.level.add_point (point)
		self.level.add_points (self.dispatch_callback)
		if key in self.borders:
			for point in self.borders[key]:
				point.active += 1
		self.level.enter ()
	
	def endElement (self, name: str) -> None:
		if self.level is not None:
			if self.level.name != name:
				self.__set_status (self.ERROR)
				raise ValueError (f'endElement: Expecting "{self.level.name}", got "{name}"')
			self.level.leave ()
			if self.level.path in self.borders:
				for point in self.borders[self.level.path]:
					point.active -= 1
			self.stage.pop ()
			if self.stage:
				self.level = self.stage[-1]
			else:
				self.level = None

	def characters (self, content: str) -> None:
		if self.level is not None:
			self.level.add_content (content)

	def setDocumentLocator (self, locator: xmlreader.Locator) -> None:
		pass
	def startPrefixMapping (self, prefix: Optional[str], uri: str) -> None:
		pass
	def endPrefixMapping (self, prefix: Optional[str]) -> None:
		pass
	def startElementNS (self, name: Tuple[str, str], qname: str, attrs: xmlreader.AttributesNSImpl) -> None:
		pass
	def endElementNS (self, name: Tuple[str, str], qname: str) -> None:
		pass
	def ignorableWhitespace (self, whitespace: str) -> None:
		pass
	def processingInstruction (self, target: str, data: str) -> None:
		pass
	def skippedEntity (self, name: str) -> None:
		pass
