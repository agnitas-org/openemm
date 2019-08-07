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
#	-*- mode: python; mode: fold -*-
#
"""XML related library for AGNITAS runtime

This file provides some convinient class to handle XML related stuff

- XMLWriter: a counterpart to a SAX XML reading class
- XMLReader: a higher level XML reader
- XMLRPCConfig: a simple class to be used for XMLRPC and XMLRPCClient or use eagn.Config for a full configuration class solution
- XMLRPC: A server framework to be used to write a XMLRPC server
- XMLRPCClient: A client framework to be used to write a XMLRPC client
- XMLRPCCall: an easy way for firing a single XMLRPC request to a server
"""
#
import	re, codecs, signal, socket
import	SimpleXMLRPCServer, select, xmlrpclib, urlparse, base64
import	xml.sax
from	xml.sax.handler import ContentHandler
import	agn
try:
	parse_qs = urlparse.parse_qs
except AttributeError:
	import	cgi
	parse_qs = cgi.parse_qs
#
# Versioning {{{
_changelog = [

	('1.0.0', '2019-01-01', 'XML related library', 'openemm@agnitas.de')
]
__version__ = _changelog[-1][0]
__date__ = _changelog[-1][1]
__author__ = ', '.join (sorted (list (set ([_c[3] for _c in _changelog]))))
def require (checkversion):
	agn.__require (checkversion, __version__, 'xagn')
#}}}
#
# XML Serial writer {{{
class XMLWriter (object):
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
- startDocument ()
- create Nodes using the methods
  - Simple: openNode, closeNode, simpleNode, textNode, cdataNode
  - Advanced: createNode, createTextNode, createText, createCDataNode,
    createCData, closeNode
  The main difference between simple and advance is, you pass a dictionary
  to the advance routines while you pass attributes as named paramter to
  the simple routings. Both share the closeNode method to close a node.
- endDocument ()

And for the lazybones beneath us, here are some shortcuts for the start,
end and simple constructs:
- start = startDocument
- end = endDocument
- open = openNode
- close = closeNode
- node = simpleNode
- text = textNode
- cdata = cdataNode

A simple example:
from cStringIO import StringIO
output = StringIO ()
writer = XMLWriter (output)
writer.startDocument ()
writer.openNode ('root', version = '2.0', date = '2008-08-05')
writer.simpleNode ('revision', nr = '1.1')
writer.textNode ('author', 'Open EMM <openemm@agnitas.de>')
writer.closeNode ('root')
writer.endDocument ()
print (output.getvalue ())

will create this output:
<?xml version="1.0" encoding="UTF-8"?>
<root date="2008-08-05" version="2.0">
 <revision nr="1.1"/>
 <author>Open EMM &lt;openemm@agnitas.de&gt;</author>
</root>
"""
	escmap = {'<': '&lt;', '>': '&gt;', '&': '&amp;', '"': '&quot;', '\'': '&apos;'}
	escape = re.compile ('[%s]' % ''.join (escmap.keys ()))
	
	def __init__ (self, output, inputCharset = None, outputCharset = None, strict = True, partial = False):
		"""``output'' is a file like object to write the
content to. ``inputCharset'' and ``outputCharset'', ``strict'' is True
to refuse invalid input, ``partial'' is False to emit valid XML
declaration."""
		self.output = output
		if inputCharset is None:
			inputCharset = 'UTF-8'
		self.inputCharset = inputCharset
		if outputCharset is None:
			outputCharset = 'UTF-8'
		self.outputCharset = outputCharset
		try:
			self.convertOption = {
				True: 'strict',
				False: 'ignore'
			}[strict]
		except KeyError:
			if type (strict) not in (str, unicode):
				raise
			self.convertOption = strict
		self.backlog = []
		self.state = 0
		self.encode = codecs.getencoder (self.outputCharset)
		self.partial = partial
	
	def __enter__ (self):
		return self
	
	def __exit__ (self, exc_type, exc_value, traceback):
		if exc_type is None:
			self.endDocument ()
		return False

	def __out (self, s):
		if self.state == 0:
			self.startDocument ()
		if self.state == 1:
			self.output.write (s)
	
	def __convert (self, s, cdata = False):
		if type (s) is not unicode:
			if type (s) is not str:
				s = str (s)
			s = unicode (s, self.inputCharset, self.convertOption)
		encoded = self.encode (s)[0]
		if cdata:
			chunks = encoded.split (']]>')
			clen = len (chunks)
			if clen > 1:
				combined = []
				for n in range (clen):
					combined.append ('<![CDATA[%s%s%s]]>' % ('>' if n > 0 else '', chunks[n], ']]' if n + 1 < clen else ''))
				return ''.join (combined)
			else:
				return '<![CDATA[%s]]>' % encoded
		else:
			return self.escape.subn (lambda ch: self.escmap[ch.group ()], encoded)[0]
	
	def __newNode (self, name, attrs, simple, text, cdata):
		out = '%s<%s' % (' ' * len (self.backlog), name)
		if not attrs is None:
			for (var, val) in attrs.items ():
				out += ' %s="%s"' % (var, self.__convert (val))
		if simple:
			if not text is None:
				out += '>%s</%s>\n' % (self.__convert (text, cdata), name)
			else:
				out += '/>\n'
		else:
			self.backlog.append (name)
			out += '>\n'
			if text:
				out += '%s\n' % self.__convert (text)
		self.__out (out)
	
	def __endNode (self, name):
		self.__out ('%s</%s>\n' % (' ' * len (self.backlog), name))

	def openNode (_self, _name, **_attrs):
		"""open a new node with ``name'' using keyword style attributes"""
		_self.__newNode (_name, _attrs, False, None, False)
	
	def closeNode (self, name = None):
		"""close an open node, if ``name'' is None, close the
last opened node, otherwise search for the node with the ``name'' and
close it (implicit close all inner nodes as well)"""
		if not self.backlog:
			raise agn.error ('Empty backlog')
		if name is None:
			name = self.backlog.pop ()
			self.__endNode (name)
		else:
			if not name in self.backlog:
				raise agn.error ('%s not found in backlog' % name)
			while self.backlog:
				pname = self.backlog.pop ()
				self.__endNode (pname)
				if pname == name:
					break

	def simpleNode (_self, _name, **_attrs):
		"""write a simple node, e.g. <node/>"""
		_self.__newNode (_name, _attrs, True, None, False)
	
	def textNode (_self, _name, _text, **_attrs):
		"""write a simple node containing embedded text, e.g. <node>text</node>"""
		_self.__newNode (_name, _attrs, True, _text, False)
		
	def cdataNode (_self, _name, _text, **_attrs):
		"""write a simple node containing embedded cdata"""
		_self.__newNode (_name, _attrs, True, _text, True)
	
	def createNode (self, name, attrs, simple = False):
		"""creates a new node ``name'' with ``attrs'' attributes as dict. Close the node if ``simple'' is True"""
		self.__newNode (name, attrs, simple, None, False)
	
	def createTextNode (self, name, text, attrs, simple = False):
		"""create a new node ``name'' with ``attrs'' attributes as dict with embeded text. Close the node if ``simple'' is True"""
		self.__newNode (name, attrs, simple, text, False)
	
	def createCDataNode (self, name, text, attrs, simple = False):
		"""create a new node ``name'' with ``attrs'' attributes as dict with embeded cdata. Close the node if ``simple'' is True"""
		self.__newNode (name, attrs, simple, text, True)
	
	def createText (self, text):
		"""output inlined text"""
		self.__out (self.__convert (text))
	
	def createCData (self, text):
		"""output inlined cdata"""
		self.__out (self.__convert (text, True))
	
	def startDocument (self, doctype = None):
		"""start the document, write the required XML header"""
		self.state = 1
		if not self.partial:
			self.__out ('<?xml version="1.0" encoding="%s"?>\n' % self.outputCharset)
		if doctype:
			self.__out (doctype)
	
	def endDocument (self):
		"""end the document, close all open nodes and flushes the output"""
		if self.state == 0:
			self.startDocument ()
		if self.state == 1:
			while self.backlog:
				self.closeNode ()
			try:
				self.output.flush ()
			except AttributeError:
				pass
			self.state = 2
	
	start = startDocument
	end = endDocument
	open = openNode
	close = closeNode
	node = simpleNode
	text = textNode
	cdata = cdataNode
#}}}
#
# XMLReader {{{
class XMLReader (ContentHandler):
	"""Provides a higher level XML reader based on SAX

This class offers a wrapper around the SAX interface by allowing to
provide callbacks for specific elements of the XML document.

To use this code, you should follow these steps:
- create an instance of XMLReader or a subclassed version of it:
  reader = XMLReader ()
- add callback handler for specific topics:
  reader.addHandler ('root.node.subnode', enterCallback, leaveCallback)
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
  reader.addHandler ('root.node.subnode', callback, callback)
  If "content is False", then this is called as the enter method,
  otherwise its the leave variant.
- add wildcard callback for whole bunch of elements:
  reader.addHandler ('root.otherNode.', enterCallbackWC, leaveCallbackWC)
  The final dot in the path does the trick.
- add regular expression callback for whole bunch of elemnts:
  reader.addHandlerRe ('.*\\.text$', enterCallbackRE, leaveCallbackRE)
- add class callback handler:
  reader.addHandlerCB (callback)
- then call either processFile (filename) or processString (s) to handle
  the incoming XML document. The result is either XMLReader.OK on success
  or one of XMLReader.WARNING, ERROR, FATAL on failure.

An optional parameter, border, is supported by all addHandler methods. If
this is present, then the handler is only called, if an entry matches
between start and end of the border. To add more than one order, you have
to save the return value of an addHandler call and pass it as first
parameter to addBorder with the additional border as the second parameter,
e.g.:
point = reader.addHandlerRe ('.*\\.text$', enterCallback, leaveCallback, 'root.node.subnode')
reader.addBorder (point, 'root.node.othernode')

The callback is executed if at least one border has crossed, so do not expect
that all of them had been crossed.
"""
	class Point (object):
		def __init__ (self, key, enter, leave):
			self.key = key
			self.enter = enter
			self.leave = leave
			self.active = 1
	
	class PointRe (Point):
		def __init__ (self, pattern, enter, leave):
			XMLReader.Point.__init__ (self, pattern, enter, leave)
			self.regexp = re.compile (pattern)
		
		def match (self, key):
			return self.regexp.search (key) is not None
	
	class PointCB (Point):
		def __init__ (self, callback):
			XMLReader.Point.__init__ (self, None, self, self)
			self.callback = callback
			self.methodCache = {}

		__normalizePattern = re.compile ('[^A-Za-z0-9_]')
		def __call__ (self, path, name, attrs, content = False):
			try:
				method = self.methodCache[path]
			except KeyError:
				method = 'cb%s' % ''.join ([self.__normalizePattern.sub ('_', _p).capitalize () for _p in path.split ('.')])
				self.methodCache[path] = method
			if method is not None:
				try:
					self.callback.__class__.__dict__[method] (self.callback, path, name, attrs, content)
				except (KeyError, TypeError):
					self.methodCache[path] = None
	
	class Level (object):
		def __init__ (self, path, name, attrs):
			self.path = path
			self.name = name
			self.attrs = attrs
			self.content = None
			self.points = []
			self.active = 0
		
		def addPoints (self, points):
			if points:
				self.points += points
		
		def addPoint (self, point):
			self.points.append (point)
		
		def enter (self):
			for point in self.points:
				if point.active > 0:
					if point.enter:
						point.enter (self.path, self.name, self.attrs)
					self.active += 1
		
		def leave (self):
			if self.active:
				for point in self.points:
					if point.active > 0 and point.leave:
						if type (self.content) is list:
							self.content = u''.join (self.content)
						point.leave (self.path, self.name, self.attrs, self.content)
				self.content = None
		
		def addContent (self, content):
			if self.active:
				if self.content is None:
					self.content = [content]
				else:
					self.content.append (content)

	OK = 0
	WARNING = 1
	ERROR = 2
	FATAL = 3
	
	def __init__ (self):
		ContentHandler.__init__ (self)
		self.dispatch = {}
		self.borders = {}
		self.dispatchRe = []
		self.dispatchCB = []
		self.stage = None
		self.level = None
		self.status = self.OK
	
	def __setStatus (self, status):
		if status > self.status:
			self.status = status
	
	def resetStatus (self):
		"""resets the working status to OK"""
		self.status = self.OK
	
	def addBorder (self, point, border):
		"""add a border for a callback point"""
		if border:
			try:
				self.borders[border].append (point)
			except KeyError:
				self.borders[border] = [point]
			point.active = 0
	
	def addHandler (self, key, enter, leave, border = None):
		"""add a handler for a specific XML key"""
		point = self.Point (key, enter, leave)
		try:
			self.dispatch[key].append (point)
		except KeyError:
			self.dispatch[key] = [point]
		self.addBorder (point, border)
		return point
		
	def addHandlerRe (self, pattern, enter, leave, border = None):
		"""add a handler for a XML key regex"""
		point = self.PointRe (pattern, enter, leave)
		self.dispatchRe.append (point)
		self.addBorder (point, border)
		return point

	def addHandlerCB (self, callback, border = None):
		"""add a generic callback handler"""
		point = self.PointCB (callback)
		self.dispatchCB.append (point)
		self.addBorder (point, border)
		return point
	
	def processStream (self, stream):
		"""process a stream using the configured callbacks"""
		xml.sax.parse (stream, self, self)
	
	def processFile (self, filename):
		"""process a file using the configured callbacks"""
		if filename.endswith ('.gz'):
			import	gzip
			fd = gzip.open (filename, 'r')
		else:
			fd = None
		if fd is not None:
			try:
				self.processStream (fd)
			finally:
				fd.close ()
		else:
			self.processStream (filename)
		return self.status
	
	def processString (self, content):
		"""process a string using the configured callbacks"""
		xml.sax.parseString (content, self, self)
		return self.status

	#
	# Error handler
	def log (self, level, s):
		"""writes a log entry"""
		agn.log (level, 'xmlreader', str (s))
		
	def error (self, exc):
		"""writes an error"""
		self.__setStatus (self.ERROR)
		self.log (agn.LV_ERROR, exc)
	def fatalError (self, exc):
		"""writes a fatal error"""
		self.__setStatus (self.FATAL)
		self.log (agn.LV_FATAL, exc)
	def warning (self, exc):
		"""writes a warning"""
		self.__setStatus (self.WARNING)
		self.log (agn.LV_WARNING, exc)
	
	#
	# Content handler
	def startDocument (self):
		self.stage = []
		self.level = None
	
	def endDocument (self):
		self.stage = None
		self.level = None
	
	def startElement (self, name, attrs):
		elem = [_s.name for _s in self.stage] + [name]
		key = '.'.join (elem)
		self.level = self.Level (key, name, attrs)
		self.stage.append (self.level)
		if key in self.dispatch:
			self.level.addPoints (self.dispatch[key])
		while elem:
			elem.pop ()
			wkey = '.'.join (elem) + '.'
			if wkey in self.dispatch:
				self.level.addPoints (self.dispatch[wkey])
		for point in self.dispatchRe:
			if point.match (key):
				self.level.addPoint (point)
		self.level.addPoints (self.dispatchCB)
		if key in self.borders:
			for point in self.borders[key]:
				point.active += 1
		self.level.enter ()
	
	def endElement (self, name):
		if self.level.name != name:
			self.__setStatus (self.ERROR)
			raise ValueError ('endElement: Expecting "%s", got "%s"' % (self.level.name, name))
		self.level.leave ()
		if self.level.path in self.borders:
			for point in self.borders[self.level.path]:
				point.active -= 1
		self.stage.pop ()
		if self.stage:
			self.level = self.stage[-1]
		else:
			self.level = None

	def characters (self, content):
		self.level.addContent (content)
#}}}
#
# XML-RPC Server frame {{{
class XMLRPCConfig (object):
	"""Simple configuration

this class is a very limited version of eagn.Config to remove the
dependency to this class. It provides minimal set/get interface to
build up a required configuration for the XMLRPC and XMLRPCClient
classes. Use eagn.Config if this class lacks a feature."""
	def __init__ (self, prefix = 'xmlrpc', **kws):
		self.cfg = {}
		for (key, value) in kws.items ():
			self.cfg['%s.%s' % (prefix, key)] = value
		
	def __getitem__ (self, key):
		try:
			return self.cfg[key]
		except KeyError:
			p = key.split ('.', 1)
			if len (p) == 2:
				return self.cfg[p[1]]
			raise
		
	def __setitem__ (self, key, value):
		self.cfg[key] = value
		
	def get (self, key, default = None):
		"""returns a configuratin value for ``key'', if available, else ``default''"""
		try:
			return self.cfg[key]
		except KeyError:
			return default
		
	def iget (self, key, default = 0):
		"""returns a configuratin value for ``key'' as an integer, if available, else ``default''"""
		try:
			return int (self.cfg[key])
		except (KeyError, ValueError, TypeError):
			return default
		
	def bget (self, key, default = False):
		"""returns a configuratin value for ``key'' as a boolean, if available, else ``default''"""
		try:
			return agn.atob (self.cfg[key])
		except KeyError:
			return default
			
class XMLRPC (object):
	"""XML-RPC Server framework

This class provides a framework for easy building a xml-rpc server. In
general this class is used by invoking these steps:

1.) Create an instance of the class:

cfg = XMLRPCConfig ()
# setup config, sample showing default values, if omitted:
cfg['xmlrpc.host'] = ''
cfg['xmlrpc.port'] = 8080
cfg['xmlrpc.allow_none'] = False	# set to True to allow None values as part of communication (this is a non standard xml-rpc extension!)
cfg['xmlrpc.server'] = None		# can be either 'forking' or 'threading', leaving it out means a single, non threading server
server = XMLRPC (cfg)			# this class takes serveral keyword arguments (even cfg is optional):
					# - full (bool), default False: if True, then a full server enviroment with signal handling is set up
					# - prefix (str), default 'xmlrpc': use this as the configuration section for accessing configuration values from cfg
					# - timeout (int), default None: if set, use this as a communcation timeout

2.) Register methods
2.1.) Register a single method

server.addMethod (m)			# m must be a callable
					# optional keyword argument:
					# - name (str), default None: if not None, expose the method using this name instead of its own name

2.2.) Register an instance of a class

Such a class should inherit the class XMLRPC.RObject which provides
standard methods (currently its only one to automatically allow list
available methods for this class.

class MyMethods (XMLRPC.RObject):
	def method (self, a, b):
		return a + b

server.addInstance (MyMethods ())	# registers all methods from the instance

2.3.) (optional) Add a handler for non XML-RPC requests

To allow handling of non XML-RPC requests (e.g. regular HTTP
requests), you can optional add a handler for this. This may be useful
to output a help page to explain the usage of this server or to
implement a hybrid XML-RPC/HTTP server.

def handler (path, param, parsedParam):
	# either return a text, which is interpreted as text/html:
	return '<html> ... </html>\n'
	# or return a mutable which must contain these elements:
	# - code: the HTTP answer code
	# - mime: the Mime type for the content
	# - headers: a list of HTTP headers (can be empty or None)
	# - content: the content
	return xagn.XMLRPC.Answer (
		code = 200,
		mime = 'text/html',
		headers = None,
		content = '<html> ... </html>\n'
	)
server.addHandler ('/', handler)	# if you use None instead of the path '/' this is a catchall for all requests without an explicit handler

2.4.) Start the server:
	
server.run ()

"""
	Answer = agn.namedmutable ('Answer', ['code', 'mime', 'headers', 'content'])
	class RObject (object):
		def _listMethods (self):
			return SimpleXMLRPCServer.list_public_methods (self)
	
	class XMLRPCRequest (SimpleXMLRPCServer.SimpleXMLRPCRequestHandler):
		def log_message (self, fmt, *args):
			agn.log (agn.LV_DEBUG, 'rq', fmt % args)
		def log_error (self, fmt, *args):
			agn.log (agn.LV_VERBOSE, 'rq', fmt % args)
		
		getHandler = {}
		@classmethod
		def addHandler (cls, path, handler):
			if path not in cls.getHandler:
				cls.getHandler[path] = handler
			else:
				raise agn.error ('path "%s" already handled' % path)
		
		baseAuthorization = []
		@classmethod
		def addBasicAuthorization (cls, user, password):
			pair = (user, password)
			if pair not in cls.baseAuthorization:
				cls.baseAuthorization.append (pair)
		
		def parse_request (self):
			rc = SimpleXMLRPCServer.SimpleXMLRPCRequestHandler.parse_request (self)
			if rc and self.baseAuthorization:
				rc = False
				authKey = 'authorization'
				if authKey in self.headers:
					auth = self.headers[authKey].split (None, 1)
					if len (auth) == 2 and auth[0].lower () == 'basic':
						try:
							cred = base64.b64decode (auth[1]).split (':', 1)
							if len (cred) == 2:
								for (user, password) in self.baseAuthorization:
									if user == cred[0] and password == cred[1]:
										rc = True
										break
						except TypeError:
							pass
				if not rc:
					self.__error (401, [('WWW-Authenticate', 'Basic realm="agn"')])
			return rc
		
		def __header (self, code, headers):
			self.send_response (code)
			if headers:
				for (var, val) in headers:
					self.send_header (var, val)
			self.end_headers ()
			
		def __error (self, code, headers, content = None):
			if content:
				answer = [('Content-Type', 'text/plain'),
					  ('Content-Length', str (len (content)))]
			else:
				answer = []
			if headers:
				answer += headers
			self.__header (code, answer)
			if content:
				self.wfile.write (content)
			self.wfile.flush ()
			
		def __answer (self, code, mime, headers, content):
			if content is None:
				content = ''
			elif type (content) is unicode:
				content = agn.toutf8 (content)
			answer = [('Content-Type', '%s; charset=UTF-8' % mime),
				  ('Content-Length', str (len (content)))]
			if headers:
				answer += headers
			self.__header (code, answer)
			self.wfile.write (content)
			self.wfile.flush ()
		
		def do_GET (self):
			try:
				(path, param) = self.path.split ('?', 1)
				parsedParam = parse_qs (param, True)
			except ValueError:
				path = self.path
				param = None
				parsedParam = None
			if path == '':
				path = '/'
			if path in self.getHandler:
				handler = self.getHandler[path]
			elif None in self.getHandler:
				handler = self.getHandler[None]
			else:
				handler = None
			if handler is not None:
				try:
					answer = handler (path, param, parsedParam)
				except Exception, e:
					self.log_error ('Failed to handle "%s": %r' % (self.path, e))
					answer = None
				if answer is None:
					self.__error (500, None, 'Failed to process request\n')
				else:
					if type (answer) in (str, unicode):
						code = 200
						mime = 'text/html'
						headers = None
						content = answer
					else:
						code = answer.code
						mime = answer.mime
						headers = answer.headers
						content = answer.content
					self.__answer (code, mime, headers, content)
			else:
				self.__error (404, None, 'Page not found\n')

	class XMLRPCServer (SimpleXMLRPCServer.SimpleXMLRPCServer):
		allow_reuse_address = True
		def __init__ (self, port, host = '', timeout = None, allow_none = False):
			SimpleXMLRPCServer.SimpleXMLRPCServer.__init__ (self, (host, port), XMLRPC.XMLRPCRequest, allow_none = allow_none)
			if timeout is not None:
				self.timeout = timeout
	
		def handle_error (self, request, client_address):
			agn.logexc (agn.LV_ERROR, 'rq', 'Request failed for %s: %r' % (client_address[0], request))

	class XMLRPCServerForking (SimpleXMLRPCServer.SocketServer.ForkingMixIn, XMLRPCServer):
		pass
	class XMLRPCServerThreading (SimpleXMLRPCServer.SocketServer.ThreadingMixIn, XMLRPCServer):
		pass
	
	def __handler (self, sig, stack):
		if sig in (signal.SIGINT, signal.SIGTERM):
			self.term = True

	def __setupSignals (self):
		signal.signal (signal.SIGTERM, self.__handler)
		signal.signal (signal.SIGINT, self.__handler)
		signal.signal (signal.SIGHUP, signal.SIG_IGN)
		signal.signal (signal.SIGPIPE, signal.SIG_IGN)

	def __init__ (self, cfg = None, full = False, prefix = 'xmlrpc', timeout = None):
		self.term = False
		host = ''
		port = 8080
		allowNone = False
		serverClass = XMLRPC.XMLRPCServer
		if not cfg is None:
			host = cfg.get ('%s.host' % prefix, host)
			port = cfg.iget ('%s.port' % prefix, port)
			allowNone = cfg.bget ('%s.allow_none' % prefix, allowNone)
			server = cfg.get ('%s.server' % prefix, None)
			if server is not None:
				if server == 'forking':
					serverClass = XMLRPC.XMLRPCServerForking
				elif server == 'threading':
					serverClass = XMLRPC.XMLRPCServerThreading
				else:
					agn.log (agn.LV_WARNING, 'xmlrpc', 'Requested unknown server type %s, fallback to simple server' % server)
		if full:
			self.__setupSignals ()
		self.server = serverClass (port, host, timeout, allowNone)
		self.server.register_introspection_functions ()
		self.server.register_multicall_functions ()
	
	def addMethod (self, m, name = None):
		"""add method ``m'', use optional ``name'' as name of the method"""
		self.server.register_function (m, name)

	def addInstance (self, i):
		"""add all found methods of instance ``i''"""
		self.server.register_instance (i)
	
	def addHandler (self, path, handler):
		"""add a ``handler'' for ``path'' to handle regular HTTP requests"""
		XMLRPC.XMLRPCRequest.addHandler (path, handler)
	
	def run (self):
		"""start the server"""
		while not self.term:
			try:
				self.server.handle_request ()
			except select.error:
				pass
			except Exception, e:
				agn.logexc (agn.LV_ERROR, 'run', 'Unexpected exception caught: %s' % str (e))

class XMLRPCClient (xmlrpclib.ServerProxy):
	"""XML-RPC Client Framework

This class provides a simple interface fo a XML-RPC client. Follow
these steps to use the class:

1.) Create an instance of the class

cfg = XMLRPCConfig ()
# setup config, sample showing default values, if omitted:
cfg['xmlrpc.host'] = '127.0.0.1'		# the host where the XML-RPC server runs
cfg['xmlrpc.post'] = 8080			# the port of the XML-RPC server
cfg['xmlrpc.secure'] = False			# if the server must be accessed using SSL
cfg['xmlrpc.path'] = ''				# the path of th URI to access the XML-RPC universe
cfg['xmlrpc.user'] = None			# username for basic authentication
cfg['xmlrpc.passwd'] = None			# password for basic authentication
cfg['xmlrpc.allow_nonw'] = False		# support None as value
cfg['xmlrpc.use_datetime'] = False		# support datetime.datetime as value
client = XMLRPCClient (cfg)

2.) Use the instance

client.some_remote_method ()

"""
	def __init__ (self, cfg = None, prefix = 'xmlrpc'):
		host = '127.0.0.1'
		port = 8080
		secure = False
		path = ''
		user = None
		passwd = None
		allowNone = False
		useDatetime = False
		if cfg is not None:
			host = cfg.get ('%s.host' % prefix, host)
			port = cfg.iget ('%s.port' % prefix, port)
			secure = cfg.bget ('%s.secure' % prefix, secure)
			path = cfg.get ('%s.path' % prefix, path)
			user = cfg.get ('%s.user' % prefix, user)
			passwd = cfg.get ('%s.passwd' % prefix, passwd)
			allowNone = cfg.bget ('%s.allow_none' % prefix, allowNone)
			useDatetime = cfg.bget ('%s.use_datetime' % prefix, useDatetime)
		if secure:
			proto = 'https'
		else:
			proto = 'http'
		auth = ''
		if user and passwd:
			auth = '%s:%s@' % (user, passwd)
		if path and not path.startswith ('/'):
			path = '/%s' % path
		uri = '%s://%s%s:%d%s' % (proto, auth, host, port, path)
		xmlrpclib.ServerProxy.__init__ (self, uri, allow_none = allowNone, use_datetime = useDatetime)

class XMLRPCCall (object):
	"""Single XML-RPC Calling

A simple variante of the XMLRPCClient which handles the setup of the
XMLRPCConfig class. Use these steps:

1.) Create an instance:

client = XMLRPCCall ()		# this class supports several keyword arguments:
				# - server (str), default '127.0.0.1': the server to connect to
				# - port (int), default 8080: the port to connect to the server
				# - secure (bool), default False: if True use SSL
				# - path (str), default None: the path to access the XML-RPC universe
				# - user (str), default None: username for basic authentication
				# - passwd (str), default None: password for basic authentication
				# - allowNone (bool), default False: if True, allow None as valid value
				# - useDatetime (bool), default False: if True, allow datetime.datetime as valid value
				# - timeout (int), default 2: Timeout for communication to the server
				# - silent (bool), default False: if used as a context manager, control propagation of an exception during execution
				# - callback (callable), default None: if used as a context manager, call this callable with the exception information, if an exception occurs

2.) Use the instance:

client ().some_remote_method ()

3.) Or used as a context manager:

with XMLRPCCall () as client:
	client ().some_remote_method ()
"""
	def __init__ (self,
		      server = '127.0.0.1', port = 8080, secure = False, path = None, user = None, passwd = None,
		      allowNone = False, useDatetime = False,
		      timeout = 2, silent = False, callback = None):
		prefix = 'xmlrpc-call'
		cfg = XMLRPCConfig ()
		cfg['%s.host' % prefix] = server
		cfg['%s.port' % prefix] = port
		cfg['%s.secure' % prefix] = secure
		if path:
			cfg['%s.path' % prefix] = path
		if user and passwd:
			cfg['%s.user' % prefix] = user
			cfg['%s.passwd' % prefix] = passwd
		cfg['%s.allow_none' % prefix] = allowNone
		cfg['%s.use_datetime' % prefix] = useDatetime
		self.remote = XMLRPCClient (cfg, prefix)
		self.timeout = timeout
		self.otimeout = None
		self.silent = silent
		self.callback = callback
	
	def __enter__ (self):
		self.otimeout = socket.getdefaulttimeout ()
		socket.setdefaulttimeout (self.timeout)
		return self
	
	def __exit__ (self, exc_type, exc_value, traceback):
		socket.setdefaulttimeout (self.otimeout)
		rc = self.silent
		if self.callback:
			rc = self.callback (exc_type, exc_value, traceback)
		return rc
	
	def __call__ (self):
		return self.remote
#}}}
