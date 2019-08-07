#!/usr/bin/env python2
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
#	-*- python -*-
#
import	sys, os, getopt, time
import	shlex, subprocess, signal
import	agn, eagn
try:
	import	activator
except ImportError:
	activator = None
#
class error (agn.error):
	def __init__ (self, msg, token, **kws):
		super (error, self).__init__ (msg)
		self.token = token
		self.ns = kws

class Plugin (eagn.Plugin):
	__slots__ = []
	def catchall (self, name):
		raise AttributeError ('%s: not implemented' % name)

class Process (object):
	__slots__ = ['cfg', 'name', 'plugin', 'status', 'comment', 'commands']
	knownCommands = ['status', 'start', 'stop', 'restart']
	knownStatuses = ['ok', 'fail', 'running', 'stopped', 'incomplete']
	def __init__ (self, cfg, name, plugin):
		self.cfg = cfg
		self.name = name
		self.plugin = plugin
		self.status = None
		self.comment = None
		self.commands = {}
		for command in self.knownCommands:
			cmd = self.cfg.tget ('cmd-%s' % command)
			if cmd is not None:
				try:
					self.commands[command] = shlex.split (cmd)
				except Exception as e:
					raise error ('Failed to parse cmd-%s: %s (%s)' % (command, cmd, str (e)), 'parse', name = name, command = command, cmd = cmd)
	
	def can (self, command):
		return command in self.commands
	
	def do (self, command, input = None):
		if not self.can (command):
			return (False, None, None)
		cmd = self.commands[command]
		#
		try:
			pp = subprocess.Popen (cmd, stdin = subprocess.PIPE, stdout = subprocess.PIPE, stderr = subprocess.PIPE)
			(out, err) = pp.communicate (input)
			if pp.returncode != 0:
				raise error ('%s: exited %s' % (command, ('due to signal %d' % -pp.returncode) if pp.returncode < 0 else ('with exit code %d' % pp.returncode)), 'execute-exit', name = self.name, command = command, cmd = cmd, out = out, err = err)
		except OSError as e:
			raise error ('%s: command failed: %s' % (command, str (e)), 'execute-exec', name = self.name, command = command, cmd = cmd)
		return (True, out, err)

	def doStatus (self):
		(found, out, err) = self.do ('status')
		if found:
			rc = out.strip ()
			if rc.split (None, 1)[0] not in self.knownStatuses:
				raise error ('status command returned unknown status %s (known are %s)' % (rc, ', '.join (self.knownStatuses)), 'status', name = self.name, status = rc)
		else:
			rc = None
		self.status = self.plugin ('status', (), rc)
		self.comment = None
		if self.status is not None:
			parts = str (self.status).split (None, 1)
			self.status = parts[0]
			if len (parts) == 2:
				self.comment = parts[1]

	def doStart (self):
		if self.status in ('running', 'incomplete'):
			self.do ('stop')
		self.do ('start')
	
	def doStop (self):
		self.do ('stop')
	
	def doRestart (self):
		self.doStop ()
		self.doStart ()
	
	def doRestore (self):
		if self.status == 'running':
			self.doStart ()
		elif self.status == 'stopped':
			self.doStop ()
		
class Service (object):
	__slots__ = ['cfg', 'id', 'ec', 'plugins']
	def __init__ (self, cfg, id):
		self.cfg = cfg
		self.id = id
		self.ec = 0
		self.plugins = {}
	
	def outn (self, msg):
		sys.stderr.write (msg)
		sys.stderr.flush ()
		
	def out (self, msg):
		self.outn (msg + '\n')

	def fail (self, msg):
		self.out (msg)
		self.ec = 1
		return False
	
	def info (self, token, ns):
		msg = self.cfg.tget ('info-%s' % token, **ns)
		if msg:
			self.out (msg)
	
	def call (self, name, method, args, default):
		try:
			plugin = self.plugins[name]
		except KeyError:
			plugin = self.cfg.tpget ('plugin', plugin = Plugin, name = name)
			self.plugins[name] = plugin
		rc = default
		if plugin:
			try:
				m = getattr (plugin, method)
			except AttributeError:
				m = None
			if m and callable (m):
				rc = m (*args)
		return rc
	
	def active (self, name):
		if activator is None:
			return True
		a = activator.Activator ()
		if not a.check ([name]):
			self.out ('%s: marked as inactive' % name)
			return False
		return self.call (name, 'active', (name, ), True)

	def sanityCheck (self):
		rc = True
		try:
			import	sanity
		
			if self.active ('sanity'):
				sanity.sanity (self.cfg.tget ('sanity-id', self.id))
		except ImportError:
			agn.log (agn.LV_DEBUG, 'sanity', 'No sanity module for %s available, no sanity check performed' % agn.user)
		except agn.error as e:
			agn.log (agn.LV_ERROR, 'sanity', 'Sanity check failed: %s' % e)
			self.fail (str (e))
			rc = False
		return rc
	
	def execute (self, param):
		if len (param) == 0:
			command = Process.knownCommands[0]
			selective = None
		else:
			command = param[0]
			if command not in Process.knownCommands:
				self.fail ('Command %s unknown, available commands are %s' % (command, ', '.join (Process.knownCommands)))
				return
			selective = set (param[1:])
		found = set ()
		#
		procs = []
		depends = agn.mutable (require = set (), resolved = set ())
		for name in [_n for _n in self.cfg.getSectionSequence () if not _n.startswith ('_')]:
			if not selective or name in selective or name in depends.require:
				self.cfg.push (name)
				self.cfg.ns['service'] = name
				if self.active (name):
					try:
						proc = Process (self.cfg, name, lambda method, args, default: self.call (name, method, args, default))
						proc.doStatus ()
						procs.append (proc)
						depend = self.cfg.lget ('depends-on')
						if depend:
							depends.require.update (depend)
						if name in depends.require:
							depends.resolved.add (name)
					except error as e:
						self.fail ('%s: unable to setup: %s' % (proc.name, e))
						self.info (e.token, e.ns)
				self.cfg.pop ()
				found.add (name)
		if selective:
			diff = selective.difference (found)
			if diff:
				self.fail ('unknown modules %s selected' % ', '.join (list (diff)))
		if depends.require != depends.resolved:
			self.fail ('Failed to resolve required modules %s' % ', '.join (list (depends.require.difference (depends.resolved))))
		if self.ec != 0:
			return
		#
		if command in ('start', 'stop', 'restart'):
			ta = eagn.Transaction ()
			if command == 'stop':
				use = list (reversed (procs))
			else:
				use = procs
			for proc in use:
				if command == 'start':
					execute = proc.doStart if proc.can ('start') else None
					rollback = proc.doStop if proc.can ('stop') else None
				elif command == 'stop':
					execute = proc.doStop if proc.can ('stop') else None
					rollback = proc.doStart if proc.can ('start') else None
				elif command == 'restart':
					execute = proc.doRestart if proc.can ('start') else None
					rollback = proc.doRestore
				else:
					agn.log (agn.LV_ERROR, 'execute', 'Unknown command %s for %s' % (command, proc.name))
					continue
				#
				ta.add (proc.name, method = execute, rollback = rollback)
			def callback (name, what, e = None):
				if what in ('start', 'rollback'):
					if what == 'start':
						what = command
					self.outn ('%s %s .. ' % (what, name))
				else:
					self.out ('%s%s' % (what, (' (%s)' % str (e)) if e else ''))
					if what == 'fail':
						time.sleep (2)
						for proc in use:
							try:
								proc.doStatus ()
							except error as e:
								agn.log (agn.LV_ERROR, 'rollback', 'Failed to get status for %s before rollback' % proc.name)
			def handler (sig, stack):
				pass
			handlers = []
			for (sig, hand) in [(signal.SIGINT, handler)]:
				ohand = signal.signal (sig, hand)
				handlers.append ((sig, ohand))
			try:
				ta.execute (rollbackFailed = True, callback = callback)
			except Exception as e:
				self.fail ('Rollback executed during %s: %s' % (command, str (e)))
				if isinstance (e, error):
					self.info (e.token, e.ns)
			for (sig, hand) in handlers:
				signal.signal (sig, hand)
			time.sleep (2)
			for proc in use:
				try:
					proc.doStatus ()
				except error as e:
					self.fail ('%s: unable to query status: %s' % (proc.name, e))
					self.info (e.token, e.ns)
		#
		if procs:
			nlen = max ([len (_p.name) for _p in procs]) + 1
			format = '%%-%d.%ds: %%s' % (nlen, nlen)
			for proc in procs:
				if proc.status:
					if proc.comment:
						status = '%s (%s)' % (proc.status, proc.comment)
					else:
						status = proc.status
					self.out (format % (proc.name, status))
		else:
			self.out ('No active processes')
#
def main ():
	help = None
	id = agn.user
	configFilename = None
	configSource = None
	options = []
	try:
		(opts, param) = getopt.getopt (sys.argv[1:], 'vh?i:c:C:o:')
		for opt in opts:
			if opt[0] == '-v':
				agn.loglevel = agn.LV_DEBUG
				agn.outlevel = agn.LV_DEBUG
				agn.outstream = sys.stderr
			elif opt[0] in ('-h', '-?'):
				help = ''
			elif opt[0] == '-i':
				id = opt[1]
			elif opt[0] == '-c':
				configFilename = opt[1]
			elif opt[0] == '-C':
				configSource = opt[1]
			elif opt[0] == '-o':
				parts = opt[1].split ('=', 1)
				if len (parts) != 2:
					help = 'Option -o requires <var>=<val> expression, not "%s"' % opt[1]
				else:
					options.append (parts)
	except getopt.GetoptError as e:
		help = str (e)
	try:
		try:
			cur = os.getcwd ()
		except OSError:
			cur = None
		if agn.base and agn.base != '.' and agn.base != cur:
			os.chdir (agn.base)
	except OSError as e:
		raise agn.error ('Cannot change to home directory %s: %s' % (agn.base, str (e)))
	cfg = eagn.Config ()
	cfg.setupNamespace (id = id)
	cfg.enableSubstitution ()
	if os.path.isfile (cfg.filename ()):
		cfg.read ()
	if configSource is not None:
		cfg.read (filedesc = eagn.StringIO.StringIO (configSource))
	elif configFilename is not None:
		cfg.read (configFilename)
	if help is not None:
		print (cfg.tget ('help', 'no help available', id = id, cfg = cfg))
		if help:
			print ('\n%s' % help)
	else:
		for (var, val) in options:
			cfg[var] = val
		svc = Service (cfg, id)
		if svc.sanityCheck ():
			svcfdName = 'SVCFD'
			fd = None
			stderr = sys.stderr.fileno ()
			if stderr is not None:
				try:
					fd = os.dup (stderr)
					os.environ[svcfdName] = str (fd)
				except OSError as e:
					agn.log (agn.LV_WARNING, 'main', 'Failed to setup reporting FD: %s' % str (e))
			svc.execute (param)
			if fd is not None:
				os.close (fd)
				del os.environ[svcfdName]
		sys.exit (svc.ec)

if __name__ == '__main__':
	main ()
