#!/usr/bin/env python3
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
import	logging, argparse
import	sys, os, shlex
from	typing import Any, Optional
from	typing import Dict, List
from	agn3.daemon import Daemonic, Watchdog, EWatchdog
from	agn3.definitions import base, program, syscfg
from	agn3.emm.config import EMMConfig
from	agn3.exceptions import error
from	agn3.ignore import Ignore
from	agn3.io import expand_command
from	agn3.lock import Lock
from	agn3.log import log
from	agn3.parser import Unit
from	agn3.runtime import CLI
from	agn3.stream import Stream
#
logger = logging.getLogger (__name__)
#
class ExternalWatchdog (EWatchdog):
	__slots__ = ['prior', 'is_temp_output', 'output_path', 'limit']
	def __init__ (self, cmd: List[str], output: str) -> None:
		super ().__init__ (cmd)
		self.prior: Optional[List[str]] = None
		self.output_path: Optional[str]
		if output:
			self.is_temp_output = (output == '-')
			if self.is_temp_output:
				self.output_path = os.path.join (base, 'var', 'tmp', '%s-%06d.log' % (program, os.getpid ()))
				if os.path.isfile (self.output_path):
					os.unlink (self.output_path)
			else:
				self.output_path = output
		else:
			self.output_path = None
		self.limit = 0
		
	def set_prior (self, cmd: str) -> None:
		self.prior = shlex.split (cmd)
	
	def joining (self, job: Watchdog.Job, ec: Daemonic.Status) -> None:
		if self.output_path is not None and self.is_temp_output and os.path.isfile (self.output_path):
			if ec.exitcode is None or ec.exitcode != Watchdog.EC_EXIT:
				with open (self.output_path, errors = 'backslashreplace') as fd:
					if self.limit > 0:
						st = os.fstat (fd.fileno ())
						if st.st_size > self.limit * 1024:
							fd.seek (-self.limit * 1024, 2)
							truncated = True
						else:
							truncated = False
						lines = Stream (fd).remain (self.limit + 1).list ()
						if len (lines) > self.limit:
							truncated = True
						if truncated:
							lines[0] = '[..]'
						output = '\n'.join (lines) + '\n'
					else:
						output = fd.read ()
				if output:
					logger.info ('Output of unexpected terminated process:\n%s' % output)
			os.unlink (self.output_path)

	def run (self, *args: Any, **kwargs: Any) -> Any:
		if self.prior is not None:
			def doit (command: List[str]) -> None:
				self.execute (command)
			pid = self.spawn (doit, self.prior)
			self.join (pid)
		if self.output_path is not None:
			self.redirect (None, self.output_path)
		return super ().run ()

class Main (CLI):
	__slots__ = [
		'command', 'instance', 'background',
		'restart_delay', 'termination_delay',
		'output', 'prior', 'limit'
	]
	def add_arguments (self, parser: argparse.ArgumentParser) -> None:
		unit = Unit ()
		parser.add_argument ('-v', '--verbose', action = 'store_true')
		parser.add_argument ('-i', '--instance', action = 'store')
		parser.add_argument ('-b', '--background', action = 'store_true')
		parser.add_argument ('-r', '--restart-delay', action = 'store', type = unit, default = 60, dest = 'restart_delay')
		parser.add_argument ('-t', '--termination-delay', action = 'store', type = unit, default = 10, dest = 'termination_delay')
		parser.add_argument ('-o', '--output', action = 'store')
		parser.add_argument ('-p', '--prior', action = 'store')
		parser.add_argument ('-l', '--limit', action = 'store', type = int, default = 0)
		parser.add_argument ('-n', '--namespace', action = 'append', default = [])
		parser.add_argument ('command', nargs = '*')
	
	def use_arguments (self, args: argparse.Namespace) -> None:
		if args.verbose:
			log.loglevel = logging.DEBUG
			log.outlevel = logging.DEBUG
			log.outstream = sys.stderr
		self.instance = args.instance
		self.background = args.background
		self.restart_delay = args.restart_delay
		self.termination_delay = args.termination_delay
		self.output = args.output
		self.prior = args.prior
		self.limit = args.limit
		self.command = args.command
		if not self.command:
			raise error ('no command to start under watchdog control')
		if args.namespace:
			self.apply_namespaces (args.namespace)
		if self.instance:
			if self.instance == '-':
				log.name = '{basename}-wd'.format (basename = os.path.basename (self.command[0]).split ('.', 1)[0])
			else:
				log.name = self.instance
	
	def executor (self) -> bool:
		wd = ExternalWatchdog (self.command, self.output)
		if self.background and wd.push_to_background ():
			return True
		lock = Lock (self.instance) if self.instance and self.instance != '-' else None
		try:
			if lock is not None:
				lock.lock ()
			if self.prior:
				wd.set_prior (self.prior)
			wd.limit = self.limit
			wd.mstart (Watchdog.Job (log.name, wd.run, ()), self.restart_delay, self.termination_delay)
		finally:
			if lock is not None:
				lock.unlock ()
		return True
	
	def apply_namespaces (self, namespaces: List[str]) -> None:
		ns: Dict[str, str] = {}
		with EMMConfig () as emmcfg:
			for entry in namespaces:
				with Ignore (ValueError):
					(target, configuration) = entry.split ('=', 1)
					(class_name, name, syscfg_key, default) = configuration.strip ().split (None, 3)
					try:
						value = emmcfg.get (class_name, name)
					except KeyError:
						value = syscfg.get (syscfg_key) if syscfg_key and syscfg_key != '-' else None
						if value is None:
							value = default
					ns[target.strip ()] = value if value else ''
		self.command = expand_command (self.command, ns)

if __name__ == '__main__':
	Main.main ()
