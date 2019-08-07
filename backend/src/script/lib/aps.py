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
"""
AGNITAS plugin service

Generic, small, simple plugin infrastructure.
"""
#
import	os, re, xmlrpclib
import	ConfigParser, imp, marshal
import	agn
#
# Versioning #{{{
_changelog = [

	('1.0.0', '2019-01-01', 'Plugin library', 'openemm@agnitas.de')
]
__version__ = _changelog[-1][0]
__date__ = _changelog[-1][1]
__author__ = ', '.join (sorted (list (set ([_c[3] for _c in _changelog]))))
def require (checkversion):
	agn.__require (checkversion, __version__, 'aps')
#
__all__ = ['Plugin']
#}}}
#
# class APIDescription {{{
#
class APIDescription (object):
	def __init__ (self, description = None):
		self.description = description
		self.methods = None
	
	def reset (self):
		self.methods = None

	def register (self, method):
		if self.methods is None:
			self.methods = set ()
		self.methods.add (method)

	methodPattern = re.compile ('^[ \t]*def[ \t]+([a-z_][a-z0-9_]*)([ \t]*\\(.*\\))?[ \t]*:[ \t]*$', re.IGNORECASE)
	def parse (self, description = None):
		if description is None:
			description = self.description
		self.reset ()
		if description is not None:
			for line in description.split ('\n'):
				m = self.methodPattern.match (line)
				if m is not None:
					self.register (m.group (1))
		return self.methods is not None

	def valid (self, method):
		return self.methods is None or method in self.methods
#}}}
#
# class Manager {{{
#
class Manager (object):
	"""
To use the AGNITAS plugin service the main program has to create one
(or more) instance(s) of Manager

You can pass a list of directories as the paths to search for plugin
files or omit it, then the current directory is used to load external
plugins. Another optional parameter is tolerant, set this to True to
avoid ImportError exception if a plugin file can not be found.
Parameter ignore can be set to True to ignore errors in plugin code.
Parameter apiVersion can be set to a version so plugins can be checked
to be compatible with this version. Parameter apiDescription is
either a subclass of APIDescription or a string which is parsable by
the class APIDescription.

If you'd like to automatically load all plguins found in the plugin
directories, just call the bootstrap method. In this case you should
call the shutdown method as well at the end of the lifetime.

You can also add methods or functions programmatically by calling the
register method or class instances by calling registerInstance or a
XML-RPC remote instance by calling registerRemoteXMLRPC (this only
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
	def catchallHandler (*args, **kws):
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
print m ().addTwoNumbers (1, 2)

will result in

[3]

adding another method:

def addTwoFloatNumbers (a, b):
	return float (a) + float (b)

m.register (addTwoFloatNumbers, name = 'addTwoNumbers')
print m ().addTwoNumbers (1, 2)

will result in

[3, 3.0]

As you can see, the manager always returns a list of results,
depending of plugins found.

print m ().notExisting ()

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
	
print m.rcMax (m ().addTwoNumbers (1, 2))

will result in

3

or

print m.rcSum (m ().addTwoNumbers (1, 2))

will result in

6.0

Using the public method "reduce", you can write your own code, e.g:
	
print m.reduce (m ().addTwoNumbers (1, 2), lambda a: float (sum (a)) / len (a), select = lambda a: type (a) in (int, float))

And a more complete example using bootstrapping:

m = Manager (['/path/to/plugin/1', '/path/to/plugin/2'])
m.bootstrap ()
...
m.shutdown ()


The bootstrap method accepts two optional parameter for handling an
external configuration file, which should be passed using keywords:

- configFile: the filename for the configuration file, default is
"plugin.cfg". If you pass "-", then no configuration file is used at
all.

- forceConfig: if this is true, then a module is only loaded, if there
is a section in the configuration file. If no configuration file is
found, no module is loaded at all.
"""
	class Callback (object): #{{{
		def __init__ (self, name, pose, source, path, method):
			self.name = name
			self.pose = pose
			self.source = source
			self.path = path
			self.method = method
			self.indirect = False
			self.discard = False
			self.faulty = 0
			self.faultyCounter = 0
			self.calls = 0
		
		def __cmp__ (self, other):
			return cmp (self.name, other.name)
		
		def __call__ (self, name, *args, **kws):
			if self.indirect:
				return self.method (name) (*args, **kws)
			else:
				return self.method (*args, **kws)
	#}}}
	class Dispatch (object): #{{{
		class Proxy (object):
			def __init__ (self, name, adl, ref):
				self.name = name
				self.adl = adl
				self.ref = ref
		
			def __call__ (self, *args, **kws):
				if self.name in self.adl:
					return None
				rc = []
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
									cbrc = callback (self.name, *args, **kws)
									if callback.faultyCounter > 0:
										callback.faultyCounter -= 1
							except Exception, e:
								self.ref.warning ('Faled to call %s: %s' % (callback.name, str (e)))
								if not self.ref.ignore:
									raise
								callback.faultyCounter += 1
								callback.faulty = callback.faultyCounter
								cbrc = None
							if not callback.discard:
								rc.append (cbrc)
				finally:
					self.adl.remove (self.name)
				return rc
		
		def __init__ (self, ref):
			self.ref = ref
			self.adl = set ()
			
		def __getattr__ (self, var):
			return self.Proxy (var, self.adl, self.ref)
				
	#}}}
	def __parseVersion (self, v):
		if v is not None:
			try:
				if type (v) == float:
					rc = (int (v), int (str (v).split ('.', 1)[1]))
				elif type (v) in (str, unicode):
					rc = tuple ([int (_v) for _v in v.split ('.')])
				elif type (v) in (list, tuple):
					rc = tuple ([int (_v) for _v in v])
				else:
					rc = (int (v), )
			except (TypeError, ValueError):
				rc = (agn.atoi (v), )
		else:
			rc = None
		return rc
	
	def __parseDescription (self, d):
		if d is None or type (d) is APIDescription or issubclass (d.__class__, APIDescription):
			rc = d
		elif type (d) in (str, unicode):
			rc = APIDescription (d)
			if not rc.parse ():
				rc = None
		else:
			rc = None
		return rc

	def __init__ (self, paths = None, tolerant = False, ignore = False, apiVersion = None, apiDescription = None):
		if paths is None:
			self.paths = [os.getcwd ()]
		elif type (paths) not in (tuple, list):
			self.paths = [paths]
		else:
			self.paths = paths
		self.tolerant = tolerant
		self.ignore = ignore
		self.apiVersion = self.__parseVersion (apiVersion)
		self.apiDescription = self.__parseDescription (apiDescription)
		self.modules = []
		self.dispatch = self.Dispatch (self)
		self.plugins = {}
		self.always = None
		self.logid = 'aps'
	
	def __call__ (self):
		return self.dispatch
		
	def _callbacks (self, name):
		if name in self.plugins:
			if self.always:
				return self.always + self.plugins[name]
			return self.plugins[name]
		return self.always

	catchall = '*'
	def register (self, func, name = None, className = None, path = None):
		try:
			funcname = func.__name__
		except AttributeError:
			funcname = func.__class__.__name__
			if className is None:
				className = funcname
		if name is None:
			name = funcname
		cb = self.Callback (funcname, name, className, path, func)
		if name == self.catchall:
			self.verbose ('Register %s as a catchall method' % cb.name)
			cb.indirect = True
			cb.discard = True
			if self.always is None:
				self.always = [cb]
			else:
				self.always.append (cb)
		else:
			self.verbose ('Register %s as %s' % (cb.name, cb.pose))
			if self.apiDescription is not None and not self.apiDescription.valid (cb.pose):
				self.warning ('Registered %s as %s is a not available in this API' % (cb.name, cb.pose))
			try:
				self.plugins[name].append (cb)
			except KeyError:
				self.plugins[name] = [cb]
		return cb
	
	def registerInstance (self, i, nameMapper = None, path = None):
		ivar = dir (i)
		cname = i.__class__.__name__
		for m in [_m for _m in ivar if not _m.startswith ('_') or _m == '__call__' or (nameMapper is not None and _m in nameMapper)]:
			func = i.__getattribute__ (m)
			if callable (func):
				if nameMapper is not None and m in nameMapper:
					name = nameMapper[m]
				elif m == '__call__':
					name = i.__class__.__name__
				else:
					name = None
				self.register (func, name, cname, path)

	class XMLRPCProxy (object): #{{{
		def __init__ (self, host, port, protocol, path):
			if protocol is None:
				protocol = 'http'
			self.address = '%s://%s' % (protocol, host)
			if port is not None:
				self.address += ':%d' % port
			if path is not None:
				if not path.startswith ('/'):
					self.address += '/'
				self.address += path
			self.remote = xmlrpclib.ServerProxy (self.address, allow_none = True)
			self.methods = set ([_m for _m in self.remote.system.listMethods () if not _m.startswith ('system.')])
		
		def __call__ (self, name):
			if name in self.methods:
				method = name
			else:
				method = None
			def proxy (*args, **kws):
				if method is None:
					return None
				return getattr (self.remote, method) (*args, **kws)
			return proxy
	#}}}
	def registerRemoteXMLRPC (self, host, port = None, protocol = None, path = None):
		rem = self.XMLRPCProxy (host, port, protocol, path)
		for method in rem.methods:
			cb = self.register (rem, name = method, path = rem.address)
			cb.indirect = True
		return rem
	
	def __import (self, fname, paths, ns):
		rc = None
		for path in paths:
			fpath = os.path.join (path, fname)
			if os.path.isfile (fpath):
				(base, ext) = os.path.splitext (fname)
				try:
					if ext in ('.pyc', '.pyo'):
						fd = open (fpath, 'rb')
						code = fd.read ()
						fd.close ()
						if len (code) < 8:
							raise Exception ('truncated binary')
						magic = imp.get_magic ()
						if code[:len (magic)] != magic:
							raise Exception ('binary version mismatch')
						m = marshal.loads (code[8:])
					else:
						fd = open (fpath, 'r')
						code = fd.read ()
						fd.close ()
						m = compile (code, fpath, 'exec')
					if ns:
						rc = ns.copy ()
						rc['_m'] = self
					else:
						rc = {'_m': self}
					exec m in rc
				except Exception, e:
					self.warning ('Failed to load %s: %s' % (fpath, str (e)))
					if not self.tolerant:
						raise
					rc = None
				break
		return rc
	
	def __load (self, m, restrict, path):
		if m:
			if restrict is not None:
				selector = lambda a: a in restrict
			else:
				selector = lambda a: not a.startswith ('_')
			for name in [_n for _n in m if selector (_n)]:
				method = m[name]
				if callable (method):
					try:
						if issubclass (method, object):
							instance = method ()
							try:
								nameMapper = instance.nameMapper
							except AttributeError:
								nameMapper = None
							self.registerInstance (instance, nameMapper, path)
					except TypeError:
						try:
							self.register (method, name = method.name, path = path)
						except AttributeError:
							self.register (method, path = path)
	
	def load (self, fname, ns = None):
		self.info ('Load plugin from %s' % fname)
		path = fname
		if os.path.isabs (fname):
			paths = [os.path.dirname (fname)]
			fname = os.path.basename (fname)
		else:
			paths = self.paths
			if os.path.sep in fname:
				fname = fname.replace (os.path.sep, '.')
		self.__load (self.__import (fname, paths, ns), None, path)
	
	class Module (object): #{{{
		def get (self, var, dflt = None):
			try:
				return self.ctrl[var]
			except KeyError:
				return dflt
		
		def iget (self, var, dflt = 0):
			try:
				val = self.ctrl[var]
				if type (val) not in (int, long):
					val = agn.atoi (val, dflt = dflt)
			except KeyError:
				val = dflt
			return val
		
		def bget (self, var, dflt = False):
			try:
				val = self.ctrl[var]
				if type (val) is not bool:
					val = agn.atob (str (val))
			except KeyError:
				val = dflt
			return val
		
		def lget (self, var, dflt = None):
			try:
				val = self.ctrl[var]
				if type (val) is tuple:
					val = [_v for _v in val]
				elif type (val) in (str, unicode):
					if val:
						val = [_v.strip () for _v in val.split (',')]
					else:
						val = None
				elif val is not None and type (val) is not list:
					val = [val]
			except KeyError:
				val = dflt
			return val
			
		def __init__ (self, name, path, ctrl, m):
			self.name = name
			self.path = path
			self.ctrl = ctrl
			self.m = m
			self.order = self.iget ('order', None)
			self.depend = self.lget ('depend', None)
			self.active = self.bget ('active', True)
			self.load = self.lget ('load', None)
			self.api = self.lget ('api', None)
			if 'path' not in self.ctrl:
				if path:
					self.ctrl['path'] = os.path.dirname (path)
				else:
					self.ctrl['path'] = '.'
			self.ctrl['module'] = self
		
		def __cmp__ (self, other):
			if self.order is not None:
				if other.order is not None:
					return cmp (self.order, other.order)
				else:
					return -1
			elif other.order is not None:
				return 1
			return cmp (self.name, other.name)

		def __execute (self, methodName, *args):
			if methodName in self.m:
				method = self.m[methodName]
				if callable (method):
					method (*args)
		def bootstrap (self):
			self.__execute ('_bootstrap', *())
		
		def shutdown (self):
			self.__execute ('_shutdown', *())
	#}}}
	def __valid (self, api):
		rc = True
		if api is not None and self.apiVersion is not None:
			rc = False
			for a in [self.__parseVersion (_a) for _a in api]:
				cnt = min (len (a), len (self.apiVersion))
				ok = True
				while cnt > 0:
					cnt -= 1
					if cnt > 0:
						ok = a[cnt] <= self.apiVersion[cnt]
					else:
						ok = a[cnt] == self.apiVersion[cnt]
					if not ok:
						break
				if ok:
					rc = True
					break
		return rc

	defaultConfigFile = 'plugin.cfg'
	remoteSection = '__remote__'
	remoteXMLRPCParse = re.compile ('([a-z0-9]+)://([^/:]+)(:[0-9]+)?(/.*)?')
	def bootstrap (self, configFile = None, forceConfig = False, ns = None):
		if configFile is None:
			configFile = self.defaultConfigFile
		collect = []
		remotes = []
		self.info ('Bootstrap for paths %s' % ';'.join (self.paths))
		for path in self.paths:
			if not os.path.isdir (path):
				self.verbose ('%s: not a directory' % path)
				continue
			seen = set ()
			files = os.listdir (path)
			if configFile != '-' and configFile in files:
				cfg = ConfigParser.RawConfigParser ()
				cfg.read (os.path.join (path, configFile))
				self.verbose ('%s: Using configuration file %s' % (path, configFile))
			else:
				cfg = None
				self.verbose ('%s: No separate configuration file found' % path)
				if forceConfig:
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
				if base not in seen and (not forceConfig or cfg.has_section (base)):
					m = self.__import (fname, [path], ns)
					magic = '__aps__'
					if m is None:
						self.warning ('%s: %s skiped as it is unparsable' % (path, base))
					elif magic not in m:
						self.verbose ('%s: Skip %s as no %s attribute is found' % (path, base, magic))
					else:
						ctrl = m[magic]
						if cfg is not None and cfg.has_section (base):
							self.debug ('%s: %s configuration is modified through config file' % (path, base))
							for opt in cfg.options (base):
								try:
									t = type (ctrl[opt])
									if t is int or t is long:
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
							self.info ('%s: Version conflict for module %s, disabled' % (path, base))
						elif md.active:
							collect.append (md)
							self.info ('%s: Loaded module %s' % (path, base))
						else:
							self.info ('%s: module %s is marked as inactive' % (path, base))
					seen.add (base)
			if cfg is not None and cfg.has_section (self.remoteSection):
				for opt in cfg.options (self.remoteSection):
					parts = opt.split ('.')
					if len (parts) == 2:
						val = cfg.get (self.remoteSection, opt)
						if parts[0].lower () in ('xmlrpc', 'xml-rpc'):
							mtch = self.remoteXMLRPCParse.match (val)
							if mtch is not None:
								(protocol, host, port, path) = mtch.groups ()
								if port is not None:
									port = int (port[1:])
								remotes.append ([self.registerRemoteXMLRPC, (host, port, protocol, path)])
							else:
								self.warning ('%s: invalid value %s for option %s in section %s' % (os.path.join (path, configFile), val, opt, self.remoteSection))
					else:
						self.warning ('%s: invalid option for section %s' % (os.path.join (path, configFile), self.remoteSection))
		backlog = []
		seen = set ()
		def resolve (md):
			incomplete = False
			if md.depend:
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
				
		for m in sorted (collect):
			resolve (m)
			for m in backlog[:]:
				resolve (m)
		cnt = len (backlog) + 1
		while cnt > len (backlog):
			cnt = len (backlog)
			for m in backlog[:]:
				resolve (m)
		self.modules += backlog
		for m in self.modules:
			self.info ('Adding module %s' % m.name)
			m.bootstrap ()
			self.__load (m.m, m.load, m.path)
		for (method, args) in remotes:
			rem = method (*args)
			self.info ('Registered remote module %s' % rem.address)
		self.info ('Bootstrapping finished')
	
	def shutdown (self):
		for names in [None] + self.plugins.keys ():
			if names is None:
				plugins = self.always
			else:
				plugins = self.plugins[names]
			if plugins:
				for cb in plugins:
					if cb.name != cb.pose:
						name = '%s (known as %s)' % (cb.name, cb.pose)
					else:
						name = cb.name
					if cb.source is not None:
						name += ' derivated from %s' % cb.source
					if cb.path is not None:
						name += ' loaded from %s' % cb.path
					if cb.calls == 0:
						self.warning ('registered method "%s" is never called' % name)
					else:
						self.verbose ('registered method "%s" is called %d time%s' % (name, cb.calls, cb.calls != 1 and 's' or ''))
		self.info ('Shuting down modules')
		for m in reversed (self.modules):
			self.info ('Shuting down %s' % m.name)
			m.shutdown ()
		self.info ('Shutdown completed')

	def reduce (self, s, method, select = None, modify = None):
		if type (s) is not list:
			return s
		if select:
			s = [_s for _s in s if select (_s)]
			if not s:
				return None
		if modify:
			s = modify (s)
		try:
			return method (s)
		except:
			return s[0]
	def rcMax (self, rc):
		return self.reduce (rc, max, select = lambda a: type (a) in (int, float))
	def rcMin (self, rc):
		return self.reduce (rc, min, select = lambda a: type (a) in (int, float))
	def rcSum (self, rc):
		return self.reduce (rc, sum, select = lambda a: type (a) in (int, float))
	def rcFirst (self, rc):
		return self.reduce (rc, lambda a: a[0])
	def rcLast (self, rc):
		return self.reduce (rc, lambda a: a[-1])
	def rcFirstNN (self, rc):
		return self.reduce (rc, lambda a: a[0], select = lambda a: a is not None)
	def rcLastNN (self, rc):
		return self.reduce (rc, lambda a: a[-1], select = lambda a: a is not None)
	def rcSorted (self, rc):
		return self.reduce (rc, lambda a: a[0], modify = sorted)
	def rcReversed (self, rc):
		return self.reduce (rc, lambda a: a[-1], modify = sorted)
	def rcSortedNN (self, rc):
		return self.reduce (rc, lambda a: a[0], select = lambda a: a is not None, modify = sorted)
	def rcReversedNN (self, rc):
		return self.reduce (rc, lambda a: a[-1], select = lambda a: a is not None, modify = sorted)

	def error (self, m):
		pass
	def warning (self, m):
		pass
	def info (self, m):
		pass
	def verbose (self, m):
		pass
	def debug (self, m):
		pass

class LoggingManager (Manager):
	def __log (self, lvl, m):
		agn.log (lvl, self.logid, m)
	def error (self, m):
		self.__log (agn.LV_ERROR, m)
	def warning (self, m):
		self.__log (agn.LV_WARNING, m)
	def info (self, m):
		self.__log (agn.LV_INFO, m)
	def verbose (self, m):
		self.__log (agn.LV_VERBOSE, m)
	def debug (self, m):
		self.__log (agn.LV_DEBUG, m)
#}}}
#
# More formal class derivate {{{
class Plugin (object):
	def __init__ (self, manager = None, ns = None):
		if hasattr (self, 'pluginVersion'):
			apiVersion = self.pluginVersion
		else:
			apiVersion = None
		plocal = agn.mkpath (agn.base, 'plugins', agn.logname)
		pdist = agn.mkpath (agn.base, 'scripts', 'plugins', agn.logname)
		if manager is None:
			manager = Manager
		self.mgr = manager (paths = [pdist, plocal], apiVersion = apiVersion)
		self.mgr.bootstrap (configFile = '%s.cfg' % agn.logname, ns = ns)
	
	def __del__ (self):
		self.mgr.shutdown ()
	
	def __call__ (self):
		return self.mgr ()
	
	def manager (self):
		return self.mgr
#}}}
