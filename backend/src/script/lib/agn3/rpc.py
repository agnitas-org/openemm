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
import	socket, logging
import	socketserver, urllib.parse, base64
import	xmlrpc.server, xmlrpc.client
import	aiohttp_xmlrpc.client
from	datetime import datetime
from	threading import Thread
from	types import FrameType, TracebackType
from	typing import Any, Callable, Iterable, Optional, Protocol, Union
from	typing import Dict, List, NamedTuple, Tuple, Type
from	typing import cast
from	.config import Config
from	.daemon import Signal
from	.exceptions import error
from	.ignore import Ignore
#
__all__ = ['XMLRPCProxy', 'XMLRPCError', 'XMLRPC', 'XMLRPCClient', 'XMLRPCCall']
#
logger = logging.getLogger (__name__)
#
_marshallable = Union[None, bool, int, float, str, bytes, Tuple[Any, ...], List[Any], Dict[Any, Any], datetime]
#
XMLRPCProxy = xmlrpc.client.ServerProxy
XMLRPCError = xmlrpc.client.Error
#
AIO_XMLRPCProxy = aiohttp_xmlrpc.client.ServerProxy
AIO_XMLRPCError = aiohttp_xmlrpc.client.exceptions.XMLRPCError
class AIO_XMLRPCProtocol (Protocol):
	async def close (self) -> None: ...
def aio_xmlrpc_loglevel (level: Optional[int] = None) -> None:
	aio_logger = logging.getLogger ('aiohttp_xmlrpc.client')
	aio_logger.setLevel (level if level is not None else logging.WARNING)
#
class Configable (Protocol):
	def get (self, key: str, default: Any = None) -> Any: ...
	def iget (self, key: str, default: int = 0) -> int: ...
	def bget (self, key: str, default: bool = False) -> bool: ...
	
class XMLRPC:
	"""XML-RPC Server framework

This class provides a framework for easy building a xml-rpc server. In
general this class is used by invoking these steps:

1.) Create an instance of the class:

cfg = Config ()
# setup config, sample showing default values, if omitted:
cfg['xmlrpc.host'] = ''
cfg['xmlrpc.port'] = 8080
cfg['xmlrpc.allow_none'] = False	# set to True to allow None values as part of communication (this is a non standard xml-rpc extension!)
cfg['xmlrpc.server'] = None		# can be either 'forking' or 'threading', leaving it out means a single, non threading server
server = XMLRPC (cfg)			# this class takes serveral keyword arguments (even cfg is optional):
					# - prefix (str), default 'xmlrpc': use this as the configuration section for accessing configuration values from cfg
					# - timeout (float), default None: if set, use this as a communcation timeout

2.) Register methods
2.1.) Register a single method

server.add_method (m)			# m must be a callable
					# optional keyword argument:
					# - name (str), default None: if not None, expose the method using this name instead of its own name

2.2.) Register an instance of a class

Such a class should inherit the class XMLRPC.RObject which provides
standard methods (currently its only one to automatically allow list
available methods for this class.

class MyMethods (XMLRPC.RObject):
	def method (self, a, b):
		return a + b

server.add_instance (MyMethods ())	# registers all methods from the instance

2.3.) (optional) Add a handler for non XML-RPC requests

To allow handling of non XML-RPC requests (e.g. regular HTTP
requests), you can optional add a handler for this. This may be useful
to output a help page to explain the usage of this server or to
implement a hybrid XML-RPC/HTTP server.

def handler (path, param, parsedParam):
	# either return a text, which is interpreted as text/html:
	return '<html> ... </html>\n'
	# or return a XMLRPC.Answer which must contain these elements:
	# - code: the HTTP answer code
	# - mime: the Mime type for the content
	# - headers: a list of HTTP headers (can be empty or None)
	# - content: the content
	return agn3.rpc.XMLRPC.Answer (
		code = 200,
		mime = 'text/html',
		headers = None,
		content = '<html> ... </html>\n'
	)
server.add_handler ('/', handler)	# if you use None instead of the path '/' this is a catchall for all requests without an explicit handler

2.4.) Start the server:
	
server.run ()

"""
	__slots__ = ['server']
	class Answer (NamedTuple):
		code: int
		mime: str
		headers: Optional[List[Tuple[str, str]]]
		content: Union[None, bytes, str]
		
	class RObject:
		__slots__: List[str] = []
		def _listMethods (self) -> List[str]:
			return xmlrpc.server.list_public_methods (self)
	
	class XMLRPCRequest (xmlrpc.server.SimpleXMLRPCRequestHandler):
		__slots__: List[str] = []
		def __format (self, fmt: str, args: Tuple[Any, ...]) -> str:
			rc = fmt % args
			if self.client_address:
				rc = '[{client}] {s}'.format (client = self.client_address[0], s = rc)
			return rc
		def log_message (self, fmt: str, *args: Any) -> None:
			logger.debug (self.__format (fmt, args))
		def log_error (self, fmt: str, *args: Any) -> None:
			logger.debug (self.__format (fmt, args))
		
		get_handler: Dict[Optional[str], Callable[[str, Optional[str], Optional[Dict[str, List[str]]]], Union[str, XMLRPC.Answer]]] = {}
		@classmethod
		def add_handler (cls, path: Optional[str], handler: Callable[[str, Optional[str], Optional[Dict[str, List[str]]]], Union[str, XMLRPC.Answer]]) -> None:
			if path not in cls.get_handler:
				cls.get_handler[path] = handler
			else:
				raise error (f'path "{path}" already handled')
		
		base_authorization: List[Tuple[str, str]] = []
		@classmethod
		def add_basic_authorization (cls, user: str, password: str) -> None:
			pair = (user, password)
			if pair not in cls.base_authorization:
				cls.base_authorization.append (pair)
		
		def parse_request (self) -> bool:
			rc = xmlrpc.server.SimpleXMLRPCRequestHandler.parse_request (self)
			if rc and self.base_authorization:
				rc = False
				with Ignore (KeyError, TypeError):
					auth = self.headers['authorization'].split (None, 1)
					if len (auth) == 2 and auth[0].lower () == 'basic':
						cred = base64.b64decode (auth[1]).decode ('UTF-8').split (':', 1)
						if len (cred) == 2:
							for (user, password) in self.base_authorization:
								if user == cred[0] and password == cred[1]:
									rc = True
									break
				if not rc:
					self.__error (401, [('WWW-Authenticate', 'Basic realm="agn"')])
			return rc
		
		def __header (self, code: int, headers: List[Tuple[str, str]]) -> None:
			self.send_response (code)
			if headers:
				for (var, val) in headers:
					self.send_header (var, val)
			self.end_headers ()
			
		def __error (self, code: int, headers: List[Tuple[str, str]], content: Optional[bytes] = None) -> None:
			answer: List[Tuple[str, str]] = [
				('Content-Type', 'text/plain'),
				('Content-Length', str (len (content)))
			] if content else []
			if headers:
				answer += headers
			self.__header (code, answer)
			if content is not None:
				self.wfile.write (content)
			self.wfile.flush ()
		
		def __answer (self, answer: XMLRPC.Answer) -> None:
			if answer.content is None:
				content = b''
			elif isinstance (answer.content, str):
				content = answer.content.encode ('UTF-8')
			else:
				content = answer.content
			self.__header (answer.code, [
				('Content-Type', f'{answer.mime}; charset=UTF-8'),
				('Content-Length', str (len (content)))
			] + (answer.headers if answer.headers is not None else []))
			self.wfile.write (content)
			self.wfile.flush ()
		
		def do_GET (self) -> None:
			path: str
			param: Optional[str]
			parsed_param: Optional[Dict[str, List[str]]]
			try:
				(path, param) = self.path.split ('?', 1)
				parsed_param = urllib.parse.parse_qs (param, True)
			except ValueError:
				path = self.path
				param = None
				parsed_param = None
			if path == '':
				path = '/'
			handler = self.get_handler.get (path, self.get_handler.get (None))
			if handler is not None:
				try:
					handlers_answer = handler (path, param, parsed_param)
				except Exception as e:
					self.log_error (f'Failed to handle "{self.path}": {e}')
					self.__error (500, [], b'Failed to process request\n')
				else:
					if isinstance (handlers_answer, str):
						answer = XMLRPC.Answer (200, 'text/html', [], handlers_answer)
					else:
						answer = handlers_answer
					self.__answer (answer)
			else:
				self.__error (404, [], b'Page not found\n')

	class XMLRPCServer (xmlrpc.server.SimpleXMLRPCServer):
		__slots__ = ['timeout', 'action_callback']
		allow_reuse_address = True
		def __init__ (self,
			port: int,
			host: str,
			timeout: Optional[float] = None,
			allow_none: bool = False,
			action_callback: Optional[Callable[[], None]] = None
		) -> None:
			super ().__init__ ((host, port), XMLRPC.XMLRPCRequest, allow_none = allow_none)
			if timeout is not None:
				self.timeout = timeout
			self.action_callback = action_callback
	
		def handle_error (self, request: Any, client_address: Union[Tuple[str, int], str]) -> None:
			logger.exception ('Request failed for {client}: {request!r}'.format (
				client = client_address[0],
				request = request
			))
		
		def _dispatch (self, method: str, params: Iterable[_marshallable]) -> _marshallable:
			try:
				rc = super ()._dispatch (method, params)
				logger.debug (f'INVOKE {method} {params!r} = {rc!r}')
			except Exception as e:
				logger.error (f'INVOKE {method} {params!r} raises {e}')
				raise
			return rc
		
		def service_actions (self) -> None:
			super ().service_actions ()
			if self.action_callback is not None:
				self.action_callback ()

	class XMLRPCServerForking (socketserver.ForkingMixIn, XMLRPCServer):
		__slots__: List[str] = []
	class XMLRPCServerThreading (socketserver.ThreadingMixIn, XMLRPCServer):
		__slots__: List[str] = []
		daemon_threads = True


	def __init__ (self,
		cfg: Optional[Configable] = None,
		prefix: str = 'xmlrpc',
		timeout: Optional[float] = None,
		action_callback: Optional[Callable[[], None]] = None
	) -> None:
		host = ''
		port = 8080
		allow_none = False
		server_class = XMLRPC.XMLRPCServer
		if cfg is not None:
			host = cast (str, cfg.get (f'{prefix}.host', host))
			port = cfg.iget (f'{prefix}.port', port)
			allow_none = cfg.bget (f'{prefix}.allow_none', allow_none)
			server = cfg.get (f'{prefix}.server', None)
			if server is not None:
				if server == 'forking':
					server_class = XMLRPC.XMLRPCServerForking
				elif server == 'threading':
					server_class = XMLRPC.XMLRPCServerThreading
				else:
					logger.warning (f'Requested unknown server type {server}, fallback to simple server')
		self.server = server_class (port, host, timeout, allow_none, action_callback)
		self.server.register_introspection_functions ()
		self.server.register_multicall_functions ()
	
	def shutdown (self) -> None:
		Thread (target = lambda: self.server.shutdown (), daemon = True).start ()
	
	def add_method (self, m: Callable[..., Any], name: Optional[str] = None) -> None:
		"""add method ``m'', use optional ``name'' as name of the method"""
		self.server.register_function (m, name)

	def add_instance (self, i: object) -> None:
		"""add all found methods of instance ``i''"""
		self.server.register_instance (i)
	
	def add_handler (self, path: Optional[str], handler: Callable[[str, Optional[str], Optional[Dict[str, List[str]]]], Union[str, XMLRPC.Answer]]) -> None:
		"""add a ``handler'' for ``path'' to handle regular HTTP requests"""
		XMLRPC.XMLRPCRequest.add_handler (path, handler)
	
	def run (self) -> None:
		"""start the server"""
		def handler (sig: int, stack: Optional[FrameType]) -> Any:
			logger.info (f'signal {sig} recieved, initiating shutdown')
			self.shutdown ()
		with Signal (
			term = handler,
			int = handler,
			hup = 'ign',
			pipe = 'ign'
		):
			self.serve ()
	
	def serve (self) -> None:
		"""start the server when signal handling is done somewhere else"""
		self.server.serve_forever (poll_interval = 1.0)

class XMLRPCClient (xmlrpc.client.ServerProxy):
	"""XML-RPC Client Framework

This class provides a simple interface fo a XML-RPC client. Follow
these steps to use the class:

1.) Create an instance of the class

cfg = Config ()
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
	__slots__: List[str] = []
	def __init__ (self, cfg: Optional[Configable] = None, prefix: str = 'xmlrpc') -> None:
		host = '127.0.0.1'
		port = 8080
		secure = False
		path = ''
		user = None
		passwd = None
		allow_none = False
		use_datetime = False
		if cfg is not None:
			host = cast (str, cfg.get (f'{prefix}.host', host))
			port = cfg.iget (f'{prefix}.port', port)
			secure = cfg.bget (f'{prefix}.secure', secure)
			path = cast (str, cfg.get (f'{prefix}.path', path))
			user = cfg.get (f'{prefix}.user', user)
			passwd = cfg.get (f'{prefix}.passwd', passwd)
			allow_none = cfg.bget (f'{prefix}.allow_none', allow_none)
			use_datetime = cfg.bget (f'{prefix}.use_datetime', use_datetime)
		if secure:
			proto = 'https'
		else:
			proto = 'http'
		auth = ''
		if user and passwd:
			auth = f'{user}:{passwd}@'
		if path and not path.startswith ('/'):
			path = f'/{path}'
		uri = f'{proto}://{auth}{host}:{port}{path}'
		super ().__init__ (uri, allow_none = allow_none, use_datetime = use_datetime)

class XMLRPCCall:
	"""Single XML-RPC Calling

A simple variante of the XMLRPCClient which handles the setup of the
Config class. Use these steps:

1.) Create an instance:

client = XMLRPCCall ()		# this class supports several keyword arguments:
				# - server (str), default '127.0.0.1': the server to connect to
				# - port (int), default 8080: the port to connect to the server
				# - secure (bool), default False: if True use SSL
				# - path (str), default None: the path to access the XML-RPC universe
				# - user (str), default None: username for basic authentication
				# - passwd (str), default None: password for basic authentication
				# - allow_none (bool), default False: if True, allow None as valid value
				# - use_datetime (bool), default False: if True, allow datetime.datetime as valid value
				# - timeout (float), default 2.0: Timeout for communication to the server
				# - silent (bool), default False: if used as a context manager, control propagation of an exception during execution
				# - callback (callable), default None: if used as a context manager, call this callable with the exception information, if an exception occurs

2.) Use the instance:

client ().some_remote_method ()

3.) Or used as a context manager:

with XMLRPCCall () as client:
	client ().some_remote_method ()
"""
	__slots__ = ['_remote', '_timeout', '_otimeout', '_silent', '_callback']
	def __init__ (self,
		server: str = '127.0.0.1',
		port: int = 8080,
		secure: bool = False,
		path: Optional[str] = None,
		user: Optional[str] = None,
		passwd: Optional[str] = None,
		allow_none: bool = False,
		use_datetime: bool = False,
		timeout: Optional[float] = 2.0,
		silent: bool = False,
		callback: Optional[Callable[[Optional[Type[BaseException]], Optional[BaseException], Optional[TracebackType]], bool]] = None
	) -> None:
		prefix = 'xmlrpc-call'
		cfg = Config ()
		cfg[f'{prefix}.host'] = server
		cfg[f'{prefix}.port'] = str (port)
		cfg[f'{prefix}.secure'] = str (secure)
		if path:
			cfg[f'{prefix}.path'] = path
		if user and passwd:
			cfg[f'{prefix}.user'] = user
			cfg[f'{prefix}.passwd'] = passwd
		cfg[f'{prefix}.allow_none'] = str (allow_none)
		cfg[f'{prefix}.use_datetime'] = str (use_datetime)
		self._remote = XMLRPCClient (cfg, prefix)
		self._timeout = timeout
		self._otimeout: Optional[float] = None
		self._silent = silent
		self._callback = callback
	
	def __enter__ (self) -> XMLRPCCall:
		self._otimeout = socket.getdefaulttimeout ()
		socket.setdefaulttimeout (self._timeout)
		return self
	
	def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
		socket.setdefaulttimeout (self._otimeout)
		rc = self._silent
		if self._callback is not None:
			rc = self._callback (exc_type, exc_value, traceback)
		return rc
	
	def __call__ (self) -> XMLRPCClient:
		return self._remote

	def __getattr__ (self, attr: str) -> Any:
		return getattr (self._remote, attr)
