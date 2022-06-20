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
import	os, re, logging
import	imp, marshal
from	configparser import RawConfigParser
from	typing import Any, Callable, Iterable, Optional, Union
from	typing import Dict, Generator, IO, List, Set, Tuple, Type
from	typing import cast
from	.definitions import base, program
from	.exceptions import error
from	.rpc import XMLRPCProxy
from	.tools import atoi, atob
#
__all__ = ['APIDescription', 'Manager', 'LoggingManager', 'Plugin']
#
logger = logging.getLogger (__name__)
#
# class APIDescription {{{
#
class APIDescription:
	__slots__ = ['description', 'methods']
	def __init__ (self, description: Optional[str] = None) -> None:
		self.description = description
		self.methods: Set[str] = set ()
	
	def reset (self) -> None:
		self.methods.clear ()

	def register (self, method: str) -> None:
		self.methods.add (method)

	method_pattern = re.compile ('^[ \t]*def[ \t]+([a-z_][a-z0-9_]*)([ \t]*\\(.*\\))?[ \t]*:[ \t]*$', re.IGNORECASE)
	def parse (self, description: Optional[str] = None) -> bool:
		if description is None:
			description = self.description
		self.reset ()
		if description is not None:
			for line in description.split ('\n'):
				m = self.method_pattern.match (line)
				if m is not None:
					self.register (m.group (1))
		return bool (self.methods)

	def valid (self, method: str) -> bool:
		return method in self.methods
#}}}
#
# class Manager {{{
#
class Manager:
	"""
To use the AGNITAS plugin service the main program has to create one
(or more) instance(s) of Manager

You can pass a list of directories as the paths to search for plugin
files or omit it, then the current directory is used to load external
plugins. Another optional parameter is tolerant, set this to True to
avoid ImportError exception if a plugin file can not be found.
Parameter ignore can be set to True to ignore errors in plugin code.
Parameter api_version can be set to a version so plugins can be checked
to be compatible with this version. Parameter api_description is
either a subclass of APIDescription or a string which is parsable by
the class APIDescription.

If you'd like to automatically load all plguins found in the plugin
directories, just call the bootstrap method. In this case you should
call the shutdown method as well at the end of the lifetime.

You can also add methods or functions programmatically by calling the
register method or class instances by calling register_instance or a
XML-RPC remote instance by calling register_remote_xmlrpc (this only
works when primitive data is passed around). Either the name of the
method or function is used or an optional argument, the name, is used.
If you use the magic Module.catchall as name, then the function is
called regardless, which plugin method is called. But be aware, the
return value of a catchall method is not passed to the caller. A
catchall method should have this signature:

def catchall (name):
	...

and must return a callable object which will be called next with
parameter for this method. So an implementation may look like this:

def catchall (name):
	def catchallHandler (*args, **kwargs):
		...
	return catchallHandler


You can also load external files using the load method, but there will
be no special action as taken by using the bootstrap method (see
below).

If an external file is loaded as a plugin, all callable names found in
the module, not starting with an underscore, are registered as
callbacks. If this is loaded using the bootstrap mechanism, the
function _bootstrap is called after loading and _shutdown when
finishing. The plugin manager instance itself is available through the
global variable "_m". During bootstrap, there must be a dictionary
called __aps__ to define this file as a plugin for the AGNITAS plugin
service. It may be empty, but can contain some meta data, such as a
version string etc. These values are currently used:
	
- api: a list of versions to match the version of the calling
interface. This may be a numeric tuple like (1, 7, 3), a string like
'1.7.3' or subset to match only a part of the interface version, like
'1.7' or (1, ) or even just 1 (the interger one). As an API change
should only occur, if the major number changes, the later version
should be enough for most implementations. The minor(s) may be larger
in the interface as using the same major, only additions should be
made. Attention: only numeric values are allowed, in configuration
files, a comma separated list of versions is expected.

- order: a numeric value which is used to sort the modules for some
rough sequence for registering. Default is None.

- depend: a list of module names this module depends on. If possible
this module is bootstrapped after the modules it depends on. This is a
soft dependency, so if tehre is a circular chain or an unresolved
relationship, the system still starts.

- active: a boolean value which tells that this module should be used.
Default is True.

- load: a list of method and classes which should be loaded. Here you
even can use entries starting with an underscore (which are normally
ignored.)

- path: this is set to the directory where the module is found, if not
already definied by the module itself. In this case, it will not be
overwritten.


If there is a separate configuration file (default name is
plugin.cfg), this is read and if there is a section with the name of
the plugin, the plugins __aps__ dictionary is populated with the
values from the configuration file. Be aware the special name
"DEFAULT" is reserved by the using parser (ConfigParser) and all names
surrounded by double underscores are used or at least reserved for
internal use.

Simple programmatic example:

def addTwoNumbers (a, b):
	return a + b

m = Manager ()
m.register (addTwoNumers)
print (m ().addTwoNumbers (1, 2))

will result in

[3]

adding another method:

def addTwoFloatNumbers (a, b):
	return float (a) + float (b)

m.register (addTwoFloatNumbers, name = 'addTwoNumbers')
print (m ().addTwoNumbers (1, 2))

will result in

[3, 3.0]

As you can see, the manager always returns a list of results,
depending of plugins found.

print (m ().notExisting ())

results in

[]

In this, no plugin is available, so an empty list is returned. This
decision is made, so you can directly iterate over the result, even if
there is no plugin available at all. There are also a bunch of methods
to extract one entry from the result, if this is applicated to your
requirements. They all start with "rc" and a meaningful name. These
ending with "NN" (which stands for not "not None" or "NOT NULL")
eliminating all None from the result set before applying the
selection. For example:
	
print (m.rcMax (m ().addTwoNumbers (1, 2)))

will result in

3

or

print (m.rcSum (m ().addTwoNumbers (1, 2)))

will result in

6.0

Using the public method "reduce", you can write your own code, e.g:
	
print (m.reduce (m ().addTwoNumbers (1, 2), lambda a: float (sum (a)) / len (a), select = lambda a: type (a) in (int, float)))

And a more complete example using bootstrapping:

m = Manager (['/path/to/plugin/1', '/path/to/plugin/2'])
m.bootstrap ()
...
m.shutdown ()


The bootstrap method accepts two optional parameter for handling an
external configuration file, which should be passed using keywords:

- config_file: the filename for the configuration file, default is
"plugin.cfg". If you pass "-", then no configuration file is used at
all.

- force_config: if this is true, then a module is only loaded, if there
is a section in the configuration file. If no configuration file is
found, no module is loaded at all.
"""
	__slots__ = ['paths', 'tolerant', 'ignore', 'api_version', 'api_description', 'modules', 'dispatch', 'plugins', 'always', 'logid']
	class Callback: #{{{
		__slots__ = ['name', 'pose', 'source', 'path', 'method', 'indirect', 'discard', 'faulty', 'faulty_counter', 'calls']
		def __init__ (self, name: str, pose: str, source: Optional[str], path: Optional[str], method: Callable[..., Any]) -> None:
			self.name = name
			self.pose = pose
			self.source = source
			self.path = path
			self.method = method
			self.indirect = False
			self.discard = False
			self.faulty = 0
			self.faulty_counter = 0
			self.calls = 0
		
		def __call__ (self, name: str, *args: Any, **kwargs: Any) -> Any:
			if self.indirect:
				return self.method (name) (*args, **kwargs)
			else:
				return self.method (*args, **kwargs)
	#}}}
	class Dispatch: #{{{
		__slots__ = ['ref', 'adl']
		class Proxy:
			__slots__ = ['name', 'adl', 'ref']
			def __init__ (self, name: str, adl: Set[str], ref: Manager) -> None:
				self.name = name
				self.adl = adl
				self.ref = ref
		
			def __call__ (self, *args: Any, **kwargs: Any) -> Any:
				if self.name in self.adl:
					return None
				rc: List[Any] = []
				self.adl.add (self.name)
				try:
					callbacks = self.ref._callbacks (self.name)
					if callbacks:
						for callback in callbacks:
							callback.calls += 1
							try:
								if callback.faulty > 0:
									cbrc = None
									callback.faulty -= 1
								else:
									cbrc = callback (self.name, *args, **kwargs)
									if callback.faulty_counter > 0:
										callback.faulty_counter -= 1
							except Exception as e:
								self.ref.warning (f'Faled to call {callback.name}: {e}')
								if not self.ref.ignore:
									raise
								callback.faulty_counter += 1
								callback.faulty = callback.faulty_counter
								cbrc = None
							if not callback.discard:
								rc.append (cbrc)
				finally:
					self.adl.remove (self.name)
				return rc
		
		def __init__ (self, ref: Manager) -> None:
			self.ref = ref
			self.adl: Set[str] = set ()
			
		def __getattr__ (self, var: str) -> Manager.Dispatch.Proxy:
			return Manager.Dispatch.Proxy (var, self.adl, self.ref)
	#}}}
	def __parse_version (self, v: Any) -> Optional[Tuple[int, ...]]:
		if v is None:
			return None
		try:
			if isinstance (v, float):
				return (int (v), int (str (v).split ('.', 1)[1]))
			if isinstance (v, str):
				return tuple ([int (_v) for _v in v.split ('.')])
			if isinstance (v, list) or isinstance (v, tuple):
				return tuple ([int (_v) for _v in v])
			return (int (v), )
		except (TypeError, ValueError):
			return (atoi (v), )
	
	def __parse_description (self, d: Union[None, str, APIDescription]) -> Optional[APIDescription]:
		rc: Optional[APIDescription]
		if d is None or isinstance (d, APIDescription):
			return d
		#
		rc = APIDescription (d)
		if not rc.parse ():
			rc = None
		return rc

	def __init__ (self,
		paths: Union[None, str, List[str]] = None,
		tolerant: bool = False,
		ignore: bool = False,
		api_version: Optional[Tuple[int, ...]] = None,
		api_description: Union[None, str, APIDescription] = None
	) -> None:
		if paths is None:
			self.paths = [os.getcwd ()]
		elif isinstance (paths, str):
			self.paths = [paths]
		else:
			self.paths = paths
		self.tolerant = tolerant
		self.ignore = ignore
		self.api_version = self.__parse_version (api_version)
		self.api_description = self.__parse_description (api_description)
		self.modules: List[Manager.Module] = []
		self.dispatch = Manager.Dispatch (self)
		self.plugins: Dict[str, List[Manager.Callback]] = {}
		self.always: Optional[List[Manager.Callback]] = None
		self.logid = 'plugin'
	
	def __call__ (self) -> Manager.Dispatch:
		return self.dispatch
		
	def _callbacks (self, name: str) -> Optional[List[Manager.Callback]]:
		if name in self.plugins:
			if self.always:
				return self.always + self.plugins[name]
			return self.plugins[name]
		return self.always

	catchall = '*'
	def register (self,
		func: Callable[..., Any],
		name: Optional[str] = None,
		class_name: Optional[str] = None,
		path: Optional[str] = None
	) -> Manager.Callback:
		try:
			funcname = func.__name__
		except AttributeError:
			funcname = func.__class__.__name__
			if class_name is None:
				class_name = funcname
		if name is None:
			name = funcname
		cb = Manager.Callback (funcname, name, class_name, path, func)
		if name == self.catchall:
			self.verbose (f'Register {cb.name} as a catchall method')
			cb.indirect = True
			cb.discard = True
			if self.always is None:
				self.always = [cb]
			else:
				self.always.append (cb)
		else:
			self.verbose (f'Register {cb.name} as {cb.pose}')
			if self.api_description is not None and not self.api_description.valid (cb.pose):
				self.warning (f'Registered {cb.name} as {cb.pose} is a not available in this API')
			try:
				self.plugins[name].append (cb)
			except KeyError:
				self.plugins[name] = [cb]
		return cb
	
	def register_instance (self,
		i: type,
		name_mapper: Optional[Dict[str, str]] = None,
		path: Optional[str] = None
	) -> None:
		ivar = dir (i)
		cname = i.__class__.__name__
		for m in [_m for _m in ivar if not _m.startswith ('_') or _m == '__call__' or (name_mapper is not None and _m in name_mapper)]:
			func = i.__getattribute__ (m)
			if callable (func):
				name: Optional[str]
				if name_mapper is not None and m in name_mapper:
					name = name_mapper[m]
				elif m == '__call__':
					name = i.__class__.__name__
				else:
					name = None
				self.register (func, name, cname, path)

	class XMLRPCProxy: #{{{
		__slots__ = ['address', 'remote', 'methods']
		def __init__ (self, host: str, port: Optional[int], protocol: Optional[str], path: Optional[str]) -> None:
			self.address = '{protocol}://{host}'.format (
				protocol= protocol if protocol is not None else 'http',
				host = host
			)
			if port is not None:
				self.address += f':{port}'
			if path is not None:
				if not path.startswith ('/'):
					self.address += '/'
				self.address += path
			self.remote = XMLRPCProxy (self.address, allow_none = True)
			remote_methods = self.remote.system.listMethods ()
			if isinstance (remote_methods, Iterable):
				self.methods = set (filter (lambda m: not m.startswith ('system.'), remote_methods))
		
		def __call__ (self, name: str) -> Callable[..., Any]:
			method = name if name in self.methods else None
			def proxy (*args: Any, **kwargs: Any) -> Any:
				if method is None:
					return None
				return getattr (self.remote, method) (*args, **kwargs)
			return proxy
	#}}}
	def register_remote_xmlrpc (self,
		host: str,
		port: Optional[int] = None,
		protocol: Optional[str] = None,
		path: Optional[str] = None
	) -> Manager.XMLRPCProxy:
		rem = Manager.XMLRPCProxy (host, port, protocol, path)
		for method in rem.methods:
			cb = self.register (rem, name = method, path = rem.address)
			cb.indirect = True
		return rem
	
	def __import (self, fname: str, paths: List[str], ns: Optional[Dict[str, Any]]) -> Optional[Dict[str, Any]]:
		rc: Optional[Dict[str, Any]] = None
		for path in paths:
			fpath = os.path.join (path, fname)
			if os.path.isfile (fpath):
				(base, ext) = os.path.splitext (fname)
				try:
					fd: IO[Any]
					if ext in ('.pyc', '.pyo'):
						with open (fpath, 'rb') as fd:
							compiled_code = fd.read ()
						if len (compiled_code) < 8:
							raise Exception ('truncated binary')
						magic = imp.get_magic ()
						if compiled_code[:len (magic)] != magic:
							raise Exception ('binary version mismatch')
						m = marshal.loads (compiled_code[8:])
					else:
						with open (fpath, 'r') as fd:
							code = fd.read ()
						m = compile (code, fpath, 'exec')
					if ns is not None:
						rc = ns.copy ()
						rc['_m'] = self
					else:
						rc = {'_m': self}
					exec (m, rc)
				except Exception as e:
					self.warning (f'Failed to load {fpath}: {e}')
					if not self.tolerant:
						raise
					rc = None
				break
		return rc
	
	def __load (self, m: Optional[Dict[str, Any]], restrict: Optional[List[str]], path: Optional[str]) -> None:
		if m:
			for name in [_n for _n in m if ((_n in restrict) if restrict is not None else (not _n.startswith ('_')))]:
				method = m[name]
				if callable (method):
					try:
						if issubclass (method, object):
							instance = method ()
							try:
								name_mapper = instance.name_mapper
							except AttributeError:
								name_mapper = None
							self.register_instance (instance, name_mapper, path)
					except TypeError:
						try:
							self.register (method, name = method.name, path = path)
						except AttributeError:
							self.register (method, path = path)
	
	def load (self, fname: str, ns: Optional[Dict[str, Any]] = None) -> None:
		self.info (f'Load plugin from {fname}')
		path = fname
		if os.path.isabs (fname):
			paths = [os.path.dirname (fname)]
			fname = os.path.basename (fname)
		else:
			paths = self.paths
			if os.path.sep in fname:
				fname = fname.replace (os.path.sep, '.')
		self.__load (self.__import (fname, paths, ns), None, path)
	
	class Module: #{{{
		__slots__ = ['name', 'path', 'ctrl', 'm', 'order', 'depend', 'active', 'load', 'api']
		def get (self, var: str, default: Any = None) -> Any:
			try:
				return self.ctrl[var]
			except KeyError:
				return default
		
		def iget (self, var: str, default: int = 0) -> int:
			try:
				val = self.ctrl[var]
				return val if isinstance (val, int) else atoi (val, default = default)
			except KeyError:
				return default
		
		def bget (self, var: str, default: bool = False) -> bool:
			try:
				val = self.ctrl[var]
				return val if isinstance (val, bool) else atob (str (val))
			except KeyError:
				return default
		
		def lget (self, var: str, default: Optional[List[str]] = None) -> Optional[List[str]]:
			try:
				val = self.ctrl[var]
				if isinstance (val, list):
					return val
				if isinstance (val, tuple):
					return list (val)
				if isinstance (val, str):
					return [_v.strip () for _v in val.split (',')] if val else None
				return [str (val)]
			except KeyError:
				return default
			
		def __init__ (self, name: str, path: str, ctrl: Dict[str, Any], m: Any) -> None:
			self.name = name
			self.path = path
			self.ctrl = ctrl
			self.m = m
			self.order = self.iget ('order')
			self.depend = self.lget ('depend')
			self.active = self.bget ('active', True)
			self.load = self.lget ('load')
			self.api = self.lget ('api')
			if 'path' not in self.ctrl:
				self.ctrl['path'] = os.path.dirname (path) if path else '.'
			self.ctrl['module'] = self
		
		def __execute (self, method_name: str, *args: Any) -> None:
			if method_name in self.m:
				method = self.m[method_name]
				if callable (method):
					method (*args)

		def bootstrap (self) -> None:
			self.__execute ('_bootstrap', *())
		
		def shutdown (self) -> None:
			self.__execute ('_shutdown', *())
	#}}}
	def __valid (self, api: Optional[List[str]]) -> bool:
		rc = True
		if api is not None and self.api_version is not None:
			rc = False
			for a in [self.__parse_version (_a) for _a in api]:
				if a is not None:
					cnt = min (len (a), len (self.api_version))
					ok = True
					while cnt > 0:
						cnt -= 1
						if cnt > 0:
							ok = a[cnt] <= self.api_version[cnt]
						else:
							ok = a[cnt] == self.api_version[cnt]
						if not ok:
							break
					if ok:
						rc = True
						break
		return rc

	default_config_file = 'plugin.cfg'
	remote_section = '__remote__'
	remote_xmlrpc_parse = re.compile ('([a-z0-9]+)://([^/:]+)(:[0-9]+)?(/.*)?')
	def bootstrap (self,
		config_file: Optional[str] = None,
		force_config: bool = False,
		ns: Optional[Dict[str, Any]] = None
	) -> None:
		if config_file is None:
			config_file = self.default_config_file
		collect: List[Manager.Module] = []
		remotes: List[Tuple[Callable[..., Any], Tuple[Any, ...]]] = []
		self.info ('Bootstrap for paths {paths}'.format (paths = ';'.join (self.paths)))
		for path in self.paths:
			if not os.path.isdir (path):
				self.verbose (f'{path}: not a directory')
				continue
			seen: Set[str] = set ()
			files = os.listdir (path)
			cfg: Optional[RawConfigParser]
			if config_file != '-' and config_file in files:
				cfg = RawConfigParser ()
				cfg.read (os.path.join (path, config_file))
				self.verbose (f'{path}: Using configuration file {config_file}')
			else:
				cfg = None
				self.verbose (f'{path}: No separate configuration file found')
				if force_config:
					continue
			sources = {}
			binaries = {}
			nfiles = []
			for fname in [_f for _f in files if not _f.startswith ('_')]:
				(base, ext) = os.path.splitext (fname)
				if ext == '.py':
					sources[base] = fname
					nfiles.append (fname)
				elif ext in ('.pyc', '.pyo'):
					binaries[base] = fname
			for binary in binaries:
				if binary not in sources:
					nfiles.append (binaries[binary])
			for fname in nfiles:
				(base, ext) = os.path.splitext (fname)
				if base not in seen and (not force_config or cast (RawConfigParser, cfg).has_section (base)):
					m = self.__import (fname, [path], ns)
					magic = '__aps__'
					if m is None:
						self.warning (f'{path}: {base} skiped as it is unparsable')
					elif magic not in m:
						self.verbose (f'{path}: Skip {base} as no {magic} attribute is found')
					else:
						ctrl = m[magic]
						if cfg is not None and cfg.has_section (base):
							self.debug (f'{path}: {base} configuration is modified through config file')
							for opt in cfg.options (base):
								try:
									t = type (ctrl[opt])
									val: Any
									if t is int:
										val = cfg.getint (base, opt)
									elif t is float:
										val = cfg.getfloat (base, opt)
									elif t is bool:
										val = cfg.getboolean (base, opt)
									else:
										val = cfg.get (base, opt)
								except KeyError:
									val = cfg.get (base, opt)
								ctrl[opt] = val
						md = self.Module (base, os.path.join (path, fname), ctrl, m)
						if not self.__valid (md.api):
							self.info (f'{path}: Version conflict for module {base}, disabled')
						elif md.active:
							collect.append (md)
							self.info (f'{path}: Loaded module {base}')
						else:
							self.info (f'{path}: module {base} is marked as inactive')
					seen.add (base)
			if cfg is not None and cfg.has_section (self.remote_section):
				for opt in cfg.options (self.remote_section):
					parts = opt.split ('.')
					if len (parts) == 2:
						val = cfg.get (self.remote_section, opt)
						if parts[0].lower () in ('xmlrpc', 'xml-rpc'):
							mtch = self.remote_xmlrpc_parse.match (val)
							if mtch is not None:
								(protocol, host, port, path) = mtch.groups ()
								remotes.append ((self.register_remote_xmlrpc, (host, int (port[1:]) if port is not None else None, protocol, path)))
							else:
								self.warning ('{path}: invalid value {value} for option {option} in section {section}'.format (
									path = os.path.join (path, config_file),
									value = val,
									option = opt,
									section = self.remote_section
								))
					else:
						self.warning ('{path}: invalid option for section {section}'.format (
							path = os.path.join (path, config_file),
							section = self.remote_section
						))
		backlog: List[Manager.Module] = []
		seen = set ()
		def resolve (md: Manager.Module) -> None:
			incomplete = False
			if md.depend is not None:
				for d in md.depend:
					if d not in seen:
						incomplete = True
						break
			if not incomplete:
				self.modules.append (md)
				seen.add (md.name)
				if md in backlog:
					backlog.remove (md)
			elif md not in backlog:
				backlog.append (md)
				
		for module in sorted (collect, key = lambda m: (m.order, m.name)):
			resolve (module)
			for module in backlog[:]:
				resolve (module)
		cnt = len (backlog) + 1
		while cnt > len (backlog):
			cnt = len (backlog)
			for module in backlog[:]:
				resolve (module)
		self.modules += backlog
		for module in self.modules:
			self.info (f'Adding module {module.name}')
			module.bootstrap ()
			self.__load (module.m, module.load, module.path)
		for (method, args) in remotes:
			rem = method (*args)
			self.info (f'Registered remote module {rem.address}')
		self.info ('Bootstrapping finished')
	
	def shutdown (self) -> None:
		def names_available () -> Generator[Optional[str], None, None]:
			yield None
			for key in self.plugins.keys ():
				yield key
		for names in names_available ():
			plugins = self.always if names is None else self.plugins[names]
			if plugins:
				for cb in plugins:
					if cb.name != cb.pose:
						name = f'{cb.name} (known as {cb.pose})'
					else:
						name = cb.name
					if cb.source is not None:
						name += f' derivated from {cb.source}'
					if cb.path is not None:
						name += f' loaded from {cb.path}'
					if cb.calls == 0:
						self.warning (f'registered method "{name}" is never called')
					else:
						self.verbose (f'fregistered method "{name}" is called {cb.calls} times')
		self.info ('Shuting down modules')
		for m in reversed (self.modules):
			self.info (f'Shuting down {m.name}')
			m.shutdown ()
		self.info ('Shutdown completed')

	def reduce (self,
		s: Any,
		method: Callable[[Any], Any],
		select: Optional[Callable[[Any], bool]] = None,
		modify: Optional[Callable[[Any], Any]] = None
	) -> Any:
		if type (s) is not list:
			return s
		if select is not None:
			s = [_s for _s in s if select (_s)]
			if not s:
				return None
		if modify is not None:
			s = modify (s)
		try:
			return method (s)
		except:
			return s[0]
	def rcMax (self, rc: Any) -> Any:
		return self.reduce (rc, max, select = lambda a: type (a) in (int, float))
	def rcMin (self, rc: Any) -> Any:
		return self.reduce (rc, min, select = lambda a: type (a) in (int, float))
	def rcSum (self, rc: Any) -> Any:
		return self.reduce (rc, sum, select = lambda a: type (a) in (int, float))
	def rcFirst (self, rc: Any) -> Any:
		return self.reduce (rc, lambda a: a[0])
	def rcLast (self, rc: Any) -> Any:
		return self.reduce (rc, lambda a: a[-1])
	def rcFirstNN (self, rc: Any) -> Any:
		return self.reduce (rc, lambda a: a[0], select = lambda a: a is not None)
	def rcLastNN (self, rc: Any) -> Any:
		return self.reduce (rc, lambda a: a[-1], select = lambda a: a is not None)
	def rcSorted (self, rc: Any) -> Any:
		return self.reduce (rc, lambda a: a[0], modify = sorted)
	def rcReversed (self, rc: Any) -> Any:
		return self.reduce (rc, lambda a: a[-1], modify = sorted)
	def rcSortedNN (self, rc: Any) -> Any:
		return self.reduce (rc, lambda a: a[0], select = lambda a: a is not None, modify = sorted)
	def rcReversedNN (self, rc: Any) -> Any:
		return self.reduce (rc, lambda a: a[-1], select = lambda a: a is not None, modify = sorted)

	def error (self, m: str) -> None:
		pass
	def warning (self, m: str) -> None:
		pass
	def info (self, m: str) -> None:
		pass
	def verbose (self, m: str) -> None:
		pass
	def debug (self, m: str) -> None:
		pass

class LoggingManager (Manager):
	__slots__: List[str] = []
	def error (self, m: str) -> None:
		logger.error (m)
	def warning (self, m: str) -> None:
		logger.warning (m)
	def info (self, m: str) -> None:
		logger.info (m)
	def verbose (self, m: str) -> None:
		logger.debug (m)
	def debug (self, m: str) -> None:
		logger.debug (m)
#}}}
#
# More formal class derivate {{{
class Plugin:
	__slots__ = ['mgr','pid']
	def __init__ (self, manager: Optional[Type[Manager]] = None, ns: Optional[Dict[str, Any]] = None) -> None:
		api_version = getattr (self, 'plugin_version') if  hasattr (self, 'plugin_version') else None
		plocal = os.path.join (base, 'plugins', program)
		pdist = os.path.join (base, 'scripts', 'plugins', program)
		self.mgr: Optional[Manager] = (manager if manager is not None else Manager) (paths = [pdist, plocal], api_version = api_version)
		if self.mgr is not None:
			self.mgr.bootstrap (config_file = f'{program}.cfg', ns = ns)
		self.pid = os.getpid ()
	
	def __del__ (self) -> None:
		self.shutdown ()
	
	def __call__ (self) -> Manager.Dispatch:
		return self.manager ()
	
	@property
	def manager (self) -> Manager:
		if self.mgr is not None:
			return self.mgr
		raise error ('plugin system already shutdown')
	
	def shutdown (self) -> None:
		if self.mgr is not None and self.pid == os.getpid ():
			self.mgr.shutdown ()
			self.mgr = None
#}}}
