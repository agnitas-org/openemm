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
import	sys, os, logging, argparse, time
from	collections import namedtuple
from	dataclasses import dataclass
from	typing import Any, Callable, Optional
from	typing import List
from	.config import Config
from	.daemon import Daemonic, Watchdog
from	.definitions import host, program
from	.lock import Lock
from	.log import log
from	.parser import Unit
from	.process import Processtitle, Parallel
#
__all__ = ['Runtime', 'CLI']
#
logger = logging.getLogger (__name__)
#
class Runtime (Watchdog):
	"""class Runtime (Watchdog)

This is a general class to implement a boilerplate for a runtime
system. You should subclass it, implement one of the following
described methods and start it using something like this:

class MyRuntime (agn3.runtime.Runtime):
	def executor (self) -> bool:
		# do something useful
		return True

if __name__ == '__main__':
	MyRuntime ().main ()

These methods are called in this sequence. Only the executor() (or
executors()) method is required, but at least the fillhelp() method
should be implemented for a more user friendly behaviour. When
overwriting watchdog specific callbacks, please ensure that the
super() method is called as well.
def supports (self, option: str) -> bool:
	Returns if an option is supported by this application.

def setup (self):
	Application sepcific setup.

def prepare (self):
	This is called after option parsing and daemon setup.

def startup (self, jobs): [watchdog]
	This is called before the watchdog itself gets active.

def spawning (self, job): [watchdog]
	This is called each time before the execute method is called
	in a subprocess.

def executor (self):
	This is the main entry point for the application. It should
	return True in case of success or False otherwise.

def executors (self):
	Return a list of execute entry points to run under watchdog
	control in parallel.

def joining (self, job, ec): [watchdog]
	This is called directly after termination of the subprocess.

def terminating (self, jobs, done): [watchdog]
	This is called after the watchdog has finished, but before the
	running child process is terminated.

def teardown (self, done): [watchdog]
	This is called after the watchdog and the child process has
	finished.

def cleanup (self):
	The final cleanup entry point before termination.

"""
	__slots__ = ['cfg', 'ctx']
	program_description: Optional[str] = None
	program_epilog: Optional[str] = None

	@classmethod
	def main (cls, *args: Any, **kwargs: Any) -> None:
		rt = cls (*args, **kwargs)
		sys.exit (0 if rt.run () else 1)

	@dataclass
	class Context:
		processtitle: Processtitle = Processtitle ()
		verbose: int = 0
		dryrun: bool = False
		lock_lazy: bool = False
		lock_id: Optional[str] = None
		background: bool = False
		watchdog: bool = False
		restart_delay: Optional[int] = None
		termination_delay: Optional[int] = None
		max_incarnations: Optional[int] = None
		heartbeat: Optional[int] = None
		exit_code: Optional[int] = None
		job: Optional[Watchdog.Job] = None
		
	def __init__ (self, cfg: Optional[Config] = None, **kwargs: Any) -> None:
		super ().__init__ ()
		if cfg is not None:
			self.cfg = cfg
		else:
			self.cfg = Config ()
			self.cfg.setup_namespace (**kwargs)
			self.cfg.enable_substitution ()
			filename = self.cfg.filename ()
			if os.path.isfile (filename):
				self.cfg.read (filename)
		self.ctx = Runtime.Context ()

	def run (self, *args: Any, **kwargs: Any) -> Any:
		#
		self.cfg.push (host)
		with log ('setup'):
			self.setup ()
		with log ('argparse'):
			self.__argument_parsing ()
		if self.ctx.background:
			if self.push_to_background ():
				logger.info ('Started background daemon process')
				os._exit (0)
			logger.info ('Background process started')
		self.setup_handler (True)
		with log ('prepare'):
			self.prepare ()
		ok = True
		with Lock (id = self.ctx.lock_id, lazy = self.ctx.lock_lazy) as lck, log ('main'):
			if lck is not None:
				logger.info ('Starting up')
				try:
					def get_name (function: Callable[..., Any]) -> str:
						try:
							return function.__name__
						except AttributeError:
							return str (function)
					executors = self.executors ()
					if self.ctx.watchdog:
						self.ctx.processtitle ('watchdog')
						if executors is not None:
							jobs = [(get_name (_e), _e, ()) for _e in executors]
						else:
							jobs = [(program, self.executor,  ())]
						self.mstart (
							[Watchdog.Job (name = _n, method = _e, args =_a, heartbeat = self.ctx.heartbeat) for (_n, _e, _a) in jobs],
							self.ctx.restart_delay,
							self.ctx.termination_delay,
							self.ctx.max_incarnations)
						ok = self.ctx.exit_code == self.EC_EXIT
					else:
						if executors is not None:
							parallel = Parallel ()
							for executor in executors:
								name = get_name (executor)
								def exec_wrapper () -> bool:
									self.ctx.processtitle (name)
									return executor ()
								parallel.fork (exec_wrapper, name = name)
							self.ctx.processtitle ('controller')
							while parallel.living ()[0] and self.running:
								time.sleep (1)
							if parallel.living ():
								parallel.term ()
							for (name, (ec, rc)) in parallel.wait ().items ():
								logger.debug (f'{name} exited with {ec} and returns {rc}')
								if (ec is not None and ec != 0) or (rc is not None and not bool (rc)):
									ok = False
						else:
							ok = self.executor ()
				except Exception as e:
					logger.exception (f'Execute failed due to: {e}')
					ok = False
				logger.info ('Going down (%s)' % ('success' if ok else 'fail', ))
			else:
				logger.info ('Lock exists')
		with log ('cleanup'):
			self.cleanup ()
		self.cfg.pop ()
		return ok
	
	def joining (self, job: Watchdog.Job, ec: Daemonic.Status) -> None:
		exitcode = ec.exitcode if ec.exitcode is not None else ec.signal
		if exitcode is not None:
			self.ctx.exit_code = exitcode if self.ctx.exit_code is None else max (self.ctx.exit_code, exitcode)

	def started (self, job: Watchdog.Job) -> None:
		self.ctx.processtitle (job.name)
		self.ctx.job = job

	def ended (self, job: Watchdog.Job, rc: Any) -> Any:
		self.ctx.job = None
		self.ctx.processtitle ()
		return rc

	def beat (self) -> None:
		if self.ctx.job is not None:
			self.ctx.job.beat ()

	def restart (self) -> None:
		if self.ctx.job is not None:
			self.ctx.job.restart ()
			
	def __argument_parsing (self) -> None:
		Option = namedtuple ('Option', ['option', 'value'])
		unit = Unit ()
		parser = argparse.ArgumentParser (
			description = self.program_description,
			epilog = self.program_epilog,
			formatter_class = argparse.RawDescriptionHelpFormatter,
			fromfile_prefix_chars = '@',
			add_help = False
		)
		parser.add_argument ('-h', '-?', '--help', action = 'help', help = 'shows help and exits')
		parser.add_argument ('-v', '--verbose', action = 'count', default = self.ctx.verbose, help = 'increase verbosity')
		parser.add_argument ('-q', '--quiet', action = 'count', default = 0, help = 'decrease verbosity')
		parser.add_argument ('--loglevel', action = 'store', help = 'set log level to value')
		parser.add_argument ('-c', '--config', action = 'store', help = 'read configuration from specified file')
		if self.supports ('dryrun'):
			parser.add_argument ('-n', '--dryrun', action = 'store_true', default = self.ctx.dryrun, help = 'switch to dry run mode, if supported')
		if self.supports ('background'):
			parser.add_argument ('-b', '--background', action = 'store_true', default = self.ctx.background, help = 'start process in background')
		if self.supports ('watchdog'):
			parser.add_argument ('-w', '--watchdog', action = 'store_true', default = self.ctx.watchdog, help = 'start under watchdog control')
			parser.add_argument ('-g', '--restart-delay', action = 'store', type = unit, default = self.ctx.restart_delay, dest = 'restart_delay')
			parser.add_argument ('-t', '--termination-delay', action = 'store', type = unit, default = self.ctx.termination_delay, dest = 'termination_delay')
			parser.add_argument ('-f', '--max-incarnations', action = 'store', type = int, default = self.ctx.max_incarnations, dest = 'max_incarnations')
			parser.add_argument ('-r', '--heartbeat', action = 'store', type = unit, default = self.ctx.heartbeat)
		parser.add_argument ('-z', '--lock-lazy', action = 'store_true', default = self.ctx.lock_lazy, dest = 'lock_lazy')
		parser.add_argument ('-l', '--lock-id', action = 'store', default = self.ctx.lock_id, dest = 'lock_id')
		parser.add_argument ('-o', '--option', action = 'append', default = [], type = lambda v: Option (*tuple (v.split ('=', 1))))
		if self.supports ('parameter'):
			parser.add_argument ('parameter', nargs = '*', help = 'command line parameter')
		self.add_arguments (parser)
		args = parser.parse_args ()
		self.use_arguments (args)
		self.ctx.verbose = args.verbose - args.quiet
		if self.ctx.verbose < 0:
			log.loglevel = logging.ERROR
		elif self.ctx.verbose == 0:
			log.loglevel = logging.INFO
		else:
			log.loglevel = logging.DEBUG
			log.outlevel = logging.DEBUG
			log.outstream = sys.stderr
			log.verbosity = self.ctx.verbose
		if args.loglevel:
			log.set_loglevel (args.loglevel)
		if args.config:
			self.cfg.clear ()
			self.cfg.read (args.config)
		if self.supports ('dryrun'):
			self.ctx.dryrun = args.dryrun
		if self.supports ('background'):
			self.ctx.background = args.background
		if self.supports ('watchdog'):
			self.ctx.watchdog = args.watchdog
			self.ctx.restart_delay = args.restart_delay
			self.ctx.termination_delay = args.termination_delay
			self.ctx.max_incarnations = args.max_incarnations
			self.ctx.heartbeat = args.heartbeat
		self.ctx.lock_lazy = args.lock_lazy
		self.ctx.lock_id = args.lock_id
		for option in args.option:
			self.cfg[option.option] = option.value
	#
	# Methods to be overwritten by client
	def supports (self, option: str) -> bool:
		return True
	def setup (self) -> None:
		pass
	def prepare (self) -> None:
		pass
	def cleanup (self) -> None:
		pass
	def add_arguments (self, parser: argparse.ArgumentParser) -> None:
		pass
	def use_arguments (self, args: argparse.Namespace) -> None:
		pass
	def executor (self) -> bool:
		return False
	def executors (self) -> Optional[List[Callable[[], bool]]]:
		return None

class CLI (Daemonic):
	__slots__: List[str] = []
	program_description: Optional[str] = None
	program_epilog: Optional[str] = None

	@classmethod
	def main (cls) -> None:
		rt = cls ()
		sys.exit (0 if rt.run () else 1)

	def run (self) -> Any:
		#
		self.setup ()
		self.__argument_parsing ()
		self.setup_handler (True)
		self.prepare ()
		try:
			ok = self.executor ()
		except Exception as e:
			print (f'Execute failed due to: {e}', file = sys.stderr)
			ok = False
		self.cleanup (ok)
		return ok

	def __argument_parsing (self) -> None:
		parser = argparse.ArgumentParser (
			description = self.program_description,
			epilog = self.program_epilog,
			formatter_class = argparse.RawDescriptionHelpFormatter,
			fromfile_prefix_chars = '@',
			add_help = False
		)
		parser.add_argument ('-h', '-?', '--help', action = 'help', help = 'shows help and exits')
		self.add_arguments (parser)
		self.use_arguments (parser.parse_args ())
	#
	# Methods to be overwritten by client
	def setup (self) -> None:
		pass
	def prepare (self) -> None:
		pass
	def cleanup (self, success: bool) -> None:
		pass
	def add_arguments (self, parser: argparse.ArgumentParser) -> None:
		pass
	def use_arguments (self, args: argparse.Namespace) -> None:
		pass
	def executor (self) -> bool:
		return False
