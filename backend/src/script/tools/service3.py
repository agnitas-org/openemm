#!/usr/bin/env python3
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
import	argparse, logging
import	sys, os, time, fcntl
import	shlex, subprocess, signal
from	dataclasses import dataclass, field
from	datetime import datetime
from	types import FrameType
from	typing import Any, Callable, Literal, Optional
from	typing import Dict, List, Set, Tuple
from	io import StringIO
from	agn3.config import Config
from	agn3.db import DB
from	agn3.daemon import Signal
from	agn3.definitions import licence, base, fqdn, user
from	agn3.emm.build import spec
from	agn3.exceptions import error
from	agn3.flow import Transaction
from	agn3.log import log
from	agn3.runtime import CLI
from	agn3.tools import Plugin
from	activator3 import Activator
#
logger = logging.getLogger (__name__)
#
class service_error (error):
	def __init__ (self, msg: str, token: str, **kwargs: Any) -> None:
		super ().__init__ (msg)
		self.token = token
		self.ns = kwargs

class PluginService (Plugin):
	__slots__: List[str] = []
	def catchall (self, name: str) -> None:
		raise AttributeError ('%s: not implemented' % name)

class Process:
	__slots__ = ['cfg', 'name', 'plugin', 'status', 'comment', 'commands']
	known_commands = ['status', 'start', 'stop', 'restart']
	known_statuses = ['ok', 'fail', 'running', 'stopped', 'incomplete']
	def __init__ (self, cfg: Config, name: str, plugin: Callable[[str, Tuple[Any, ...], Any], Any]) -> None:
		self.cfg = cfg
		self.name = name
		self.plugin = plugin
		self.status = None
		self.comment = None
		self.commands: Dict[str, List[str]] = {}
		for command in self.known_commands:
			cmd = self.cfg.tget ('cmd-%s' % command)
			if cmd is not None:
				try:
					self.commands[command] = shlex.split (cmd)
				except Exception as e:
					raise service_error ('Failed to parse cmd-%s: %s (%s)' % (command, cmd, str (e)), 'parse', name = name, command = command, cmd = cmd)
	
	def can (self, command: str) -> bool:
		return command in self.commands
	
	def do (self, command: str, input: Optional[str] = None) -> Tuple[bool, Optional[str], Optional[str]]:
		if not self.can (command):
			return (False, None, None)
		cmd = self.commands[command]
		#
		try:
			pp = subprocess.Popen (cmd, stdin = subprocess.PIPE, stdout = subprocess.PIPE, stderr = subprocess.PIPE, text = True, errors = 'backslashreplace')
			(out, err) = pp.communicate (input)
			if pp.returncode != 0:
				raise service_error ('%s: exited %s' % (command, ('due to signal %d' % -pp.returncode) if pp.returncode < 0 else ('with exit code %d' % pp.returncode)), 'execute-exit', name = self.name, command = command, cmd = cmd, out = out, err = err)
		except OSError as e:
			raise service_error ('%s: command failed: %s' % (command, str (e)), 'execute-exec', name = self.name, command = command, cmd = cmd)
		return (True, out.strip (), err.strip ())

	def do_status (self) -> None:
		(found, out, err) = self.do ('status')
		if found and out is not None:
			response = out.split (None, 1)
			if not response or response[0] not in self.known_statuses:
				raise service_error ('status command returned unknown status %s (known are %s)' % (out, ', '.join (self.known_statuses)), 'status', name = self.name, status = out)
		self.status = self.plugin ('status', (), out)
		self.comment = None
		if self.status is not None:
			parts = str (self.status).split (None, 1)
			self.status = parts[0]
			if len (parts) == 2:
				self.comment = parts[1]

	def do_start (self) -> None:
		if self.status in ('running', 'incomplete'):
			self.do ('stop')
		self.do ('start')
	
	def do_stop (self) -> None:
		self.do ('stop')
	
	def do_restart (self) -> None:
		self.do_stop ()
		self.do_start ()
	
	def do_restore (self) -> None:
		if self.status == 'running':
			self.do_start ()
		elif self.status == 'stopped':
			self.do_stop ()
		
class Service:
	__slots__ = ['cfg', 'id', 'ec', 'plugins', 'command', 'selective']
	def __init__ (self, cfg: Config, id: str, parameter: List[str]) -> None:
		self.cfg = cfg
		self.id = id
		self.ec = 0
		self.plugins: Dict[str, PluginService] = {}
		if len (parameter) == 0:
			self.command = Process.known_commands[0]
			self.selective = None
		else:
			self.command = parameter[0]
			if self.command not in Process.known_commands:
				raise error ('Command %s unknown, available commands are %s' % (self.command, ', '.join (Process.known_commands)))
			self.selective = set (parameter[1:])
	
	def outn (self, msg: str) -> None:
		sys.stderr.write (msg)
		sys.stderr.flush ()
		
	def out (self, msg: str) -> None:
		self.outn (msg + '\n')

	def fail (self, msg: str) -> Literal[False]:
		self.out (msg)
		self.ec = 1
		return False
	
	def info (self, token: str, ns: Dict[str, Any]) -> None:
		msg = self.cfg.tget ('info-%s' % token, **ns)
		if msg:
			self.out (msg)
	
	def call (self, name: str, method: str, args: Tuple[Any, ...], default: bool) -> bool:
		try:
			plugin = self.plugins[name]
		except KeyError:
			plugin = self.cfg.tpget ('plugin', plugin = PluginService, name = name)
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
	
	def active (self, name: str, skip_failure: bool = False) -> bool:
		try:
			if not Activator ().check ([name]):
				self.out ('%s: marked as inactive' % name)
				return False
		except error:
			if skip_failure:
				return True
			raise
		return self.call (name, 'active', (name, ), True)

	def sanity_check (self) -> bool:
		rc = True
		if self.command in ('start', 'restart'):
			check_id = self.cfg.tget ('sanity-id', self.id)
			try:
				from	sanity3 import Sanities
		
				if self.active ('sanity', True):
					Sanities.checks[check_id] ()
			except ImportError:
				logger.debug (f'No sanity3 module for {user} available, no sanity check performed')
			except KeyError:
				logger.error (f'{check_id}: no sanity check found')
			except error as e:
				logger.error (f'Sanity check failed: {e}')
				self.fail (str (e))
				rc = False
			except Exception as e:
				logger.exception (f'Sanity check filed: {e}')
				self.fail (str (e))
				raise
		return rc
	
	def log_release (self) -> None:
		application = 'BACKEND-{id}'.format (id = self.id.upper ())
		now = datetime.now ()
		try:
			with DB () as db:
				data: Dict[str, Any] = {
					'host_name': fqdn,
					'application_name': application
				}

				rq = db.querys (db.qselect (
					oracle = (
						'SELECT version_number '
						'FROM release_log_tbl '
						'WHERE host_name = :host_name AND application_name = :application_name '
						'ORDER BY startup_timestamp DESC'
					), mysql = (
						'SELECT version_number '
						'FROM release_log_tbl '
						'WHERE host_name = :host_name AND application_name = :application_name '
						'ORDER BY startup_timestamp DESC '
						'LIMIT 1'
					)), data
				)
				if rq is None or rq.version_number != spec.version:
					data.update ({
						'version_number': spec.version,
						'startup_timestamp': now,
						'build_time': spec.timestamp,
						'build_host': spec.host,
						'build_user': spec.user
					})
					count = db.update (
						'INSERT INTO release_log_tbl '
						'       (host_name, application_name, version_number, startup_timestamp, build_time, build_host, build_user) '
						'VALUES '
						'       (:host_name, :application_name, :version_number, :startup_timestamp, :build_time, :build_host, :build_user)',
						data,
						commit = True
					)
					if count != 1:
						raise error (f'failed to create new record, expected 1 row, inserted {count} rows')
		except (IOError, error) as e:
			logger.debug (f'log_release: failed to write to database ({e}), try to spool information')
			log_path = os.path.join (base, 'log')
			if os.path.isdir (log_path):
				with open (os.path.join (log_path, 'release.log'), 'a') as fd:
					fd.write (f'{licence};{fqdn};{application};{spec.version};{now:%Y-%m-%d %H:%M:%S};{spec.timestamp:%Y-%m-%d %H:%M:%S};{spec.host};{spec.user}\n')
			else:
				logger.debug (f'no path {log_path} exists, no information is written')

	@dataclass
	class Depend:
		require: Set[str] = field (default_factory = set)
		resolved: Set[str] = field (default_factory = set)
		
	def execute (self) -> None:
		found: Set[str] = set ()
		#
		procs: List[Process] = []
		depends = Service.Depend ()
		for name in [_n for _n in self.cfg.get_section_sequence () if not _n.startswith ('_')]:
			if not self.selective or name in self.selective or name in depends.require:
				self.cfg.push (name)
				self.cfg.ns['service'] = name
				if self.active (name):
					try:
						proc = Process (self.cfg, name, lambda method, args, default: self.call (name, method, args, default))
						proc.do_status ()
						procs.append (proc)
						depend = self.cfg.lget ('depends-on')
						if depend:
							depends.require.update (depend)
						if name in depends.require:
							depends.resolved.add (name)
					except service_error as e:
						self.fail ('%s: unable to setup: %s' % (proc.name, e))
						self.info (e.token, e.ns)
				self.cfg.pop ()
				found.add (name)
		if self.selective:
			diff = self.selective.difference (found)
			if diff:
				self.fail ('unknown modules %s selected' % ', '.join (list (diff)))
		if depends.require != depends.resolved:
			self.fail ('Failed to resolve required modules %s' % ', '.join (list (depends.require.difference (depends.resolved))))
		if self.ec != 0:
			return
		#
		if self.command in ('start', 'stop', 'restart'):
			ta = Transaction ()
			if self.command == 'stop':
				use = list (reversed (procs))
			else:
				use = procs
			for proc in use:
				if self.command == 'start':
					execute = proc.do_start if proc.can ('start') else None
					rollback = proc.do_stop if proc.can ('stop') else None
				elif self.command == 'stop':
					execute = proc.do_stop if proc.can ('stop') else None
					rollback = proc.do_start if proc.can ('start') else None
				elif self.command == 'restart':
					execute = proc.do_restart if proc.can ('start') else None
					rollback = proc.do_restore
				else:
					logger.error ('Unknown command %s for %s' % (self.command, proc.name))
					continue
				#
				ta.add (proc.name, method = execute, rollback = rollback)
			with Signal () as sig:
				def handler (signr: signal.Signals, stack: FrameType) -> None:
					pass
				sig[signal.SIGINT] = handler
				try:
					def callback (name: str, what: str, exc: Optional[Exception] = None) -> None:
						if what in ('start', 'rollback'):
							if what == 'start':
								what = self.command
							self.outn ('%s %s .. ' % (what, name))
						else:
							self.out ('%s%s' % (what, (' (%s)' % str (exc)) if exc else ''))
							if what == 'fail':
								time.sleep (2)
								for proc in use:
									try:
										proc.do_status ()
									except service_error as e:
										logger.error (f'Failed to get status for {proc.name} before rollback: {e}')
					ta.execute (rollback_failed = True, callback = callback)
				except Exception as e:
					self.fail ('Rollback executed during %s: %s' % (self.command, str (e)))
					if isinstance (e, service_error):
						self.info (e.token, e.ns)
			time.sleep (2)
			for proc in use:
				try:
					proc.do_status ()
				except service_error as e:
					self.fail ('%s: unable to query status: %s' % (proc.name, e))
					self.info (e.token, e.ns)
			#
			if self.command == 'start' and spec.version:
				self.log_release ()
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
class Main (CLI):
	__slots__ = ['verbose', 'id', 'config_filename', 'config_source', 'options', 'parameter']
	def add_arguments (self, parser: argparse.ArgumentParser) -> None:
		parser.add_argument (
			'-v', '--verbose', action = 'store_true',
			help = 'switch verbose mode on'
		)
		parser.add_argument (
			'-i', '--id', action = 'store', default = user,
			help = f'Set ID for this service (default is {user})'
		)
		parser.add_argument (
			'-c', '--config-filename', action = 'store', dest = 'config_filename',
			help = 'Specifiy the filename to read service configuration from'
		)
		parser.add_argument (
			'-C', '--config-source', action = 'store', dest = 'config_source',
			help = 'Specifiy the configuration as parameter'
		)
		parser.add_argument (
			'-o', '--option', action = 'append', default = [],
			help = 'Set option using <option>=<value>'
		)
		parser.add_argument (
			'parameter', nargs = '*',
			help = 'command line parameter'
		)

	def use_arguments (self, args: argparse.Namespace) -> None:
		self.verbose = args.verbose
		self.id = args.id
		self.config_filename = args.config_filename
		self.config_source = args.config_source
		self.options = {}
		if args.option:
			for option in args.option:
				try:
					(o, v) = option.split ('=', 1)
					self.options[o] = v
				except ValueError:
					raise error (f'option: invalid syntax: {option}')
		self.parameter = args.parameter
	
	def executor (self) -> bool:
		if self.verbose:
			log.outlevel = logging.DEBUG
			log.outstream = sys.stderr
		try:
			cwd = os.getcwd ()
		except OSError:
			cwd = ''
		if base != cwd:
			try:
				os.chdir (base)
			except OSError as e:
				raise error (f'Failed to chdir to home directory "{base}" from "{cwd}": {e}')
		cfg = Config ()
		cfg.setup_namespace (id = self.id)
		cfg.enable_substitution ()
		if os.path.isfile (cfg.filename ()):
			cfg.read ()
		if self.config_source is not None:
			cfg.read (StringIO (self.config_source))
		elif self.config_filename is not None:
			cfg.read (self.config_filename)
		for (option, value) in self.options.items ():
			cfg[option] = value
		service = Service (cfg, self.id, self.parameter)
		if service.sanity_check ():
			serivce_filedescriptior_name = 'SVCFD'
			fd: Optional[int] = None
			if sys.stderr is not None:
				try:
					fd = fcntl.fcntl (sys.stderr.fileno (), fcntl.F_DUPFD)
					os.environ[serivce_filedescriptior_name] = str (fd)
				except OSError as e:
					logger.warning (f'Failed to setup reporting FD: {e}')
			service.execute ()
			if fd is not None:
				os.close (fd)
				del os.environ[serivce_filedescriptior_name]
		sys.exit (service.ec)

if __name__ == '__main__':
	Main.main ()
