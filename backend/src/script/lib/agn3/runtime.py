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
import	sys, os, logging, argparse, time, signal, json
from	dataclasses import dataclass
from	fnmatch import fnmatch
from	traceback import print_exception
from	typing import Any, Callable, NoReturn, Sequence, TypeVar
from	typing import Dict, List, NamedTuple, Set, Type
from	.config import Config
from	.daemon import Daemonic, Watchdog
from	.definitions import host, program, syscfg
from	.emm.build import spec
from	.exceptions import error
from	.ignore import Ignore
from	.lock import Lock
from	.log import log, interactive
from	.parameter import Parameter
from	.parser import unit
from	.process import Processtitle, Parallel
from	.stream import Stream
from	.tools import atob, Plugin
#
__all__ = ['Runtime', 'CLI', 'Preset', 'Locate']
#
logger = logging.getLogger (__name__)
#
_T = TypeVar ('_T', bound = Type[object])
#
def _expand_inline (args: List[str], environment: None | dict[str, str] = None) -> list[str]:
	"""Expands element starting with an '@' from an environment variable or a file

>>> _expand_inline (['this', 'is', '@(sample)', 'test'], {})
Traceback (most recent call last):
...
agn3.exceptions.error: @(sample): specified environment not found
>>> _expand_inline (['this', 'is', '@(sample)', 'test'], {'sample': '-?\\n--target=destination\\nfile1\\nfile2\\n'})
['this', 'is', '-?', '--target=destination', 'file1', 'file2', 'test']
>>> import json
>>> _expand_inline (['this', 'is', '@(sample)', 'test'], {'sample': json.dumps (['very', 'great', 'example'])})
['this', 'is', 'very', 'great', 'example', 'test']
>>> _expand_inline (['this', 'is', '@(sample)', 'test'], {'sample': json.dumps (['very', 'great', 'example', ['even', 'better']])})
['this', 'is', 'very', 'great', 'example', 'even', 'better', 'test']
>>> _expand_inline (['this', 'is', '@(sample)', 'test'], {'sample': json.dumps (['very', 'great', 'example', {'even': 'better'}])})
['this', 'is', 'very', 'great', 'example', 'even', 'better', 'test']
>>> _expand_inline (['this', 'is', '@(sample)', 'test'], {'sample': json.dumps (['very', 'great', 'example', {'even': 'better', 'noone': None}])})
['this', 'is', 'very', 'great', 'example', 'even', 'better', 'noone', 'test']
"""
	expanded: List[str] = []
	for arg in args:
		if arg.startswith ('@'):
			if arg.startswith ('@(') and arg.endswith (')'):
				try:
					content = (environment if environment is not None else os.environ)[arg[2:-1]]
				except KeyError:
					raise error (f'{arg}: specified environment not found')
			else:
				try:
					with open (arg[1:]) as fd:
						content = fd.read ()
				except IOError as e:
					raise error (f'{arg}: failed to read file: {e}')
			try:
				def parsing (parsed: Any) -> None:
					if isinstance (parsed, list):
						for element in parsed:
							parsing (element)
					elif isinstance (parsed, dict):
						for (option, value) in parsed.items ():
							expanded.append (option)
							if value is not None:
								parsing (value)
					else:
						expanded.append (str (parsed))
				
				parsing (json.loads (content))
				
			except json.decoder.JSONDecodeError:
				expanded += content.strip ().split ('\n')
		else:
			expanded.append (arg)
	return expanded

class Frame:
	"""generalized runtime modification framework"""
	__slots__ = ['_namespace', '_plugin']
	def __init__ (self, **kwargs: Any) -> None:
		framing = syscfg.get (f'frame:{program}')
		self._namespace = kwargs.copy ()
		self._plugin: None | Plugin = Plugin (framing, name = program, ns = self._namespace) if framing else None
		if self._plugin is not None:
			with Ignore (KeyError):
				valid_for = self._plugin['valid_for']
				if valid_for is not None:
					is_valid = False
					if isinstance (valid_for, bool):
						is_valid = valid_for
					elif isinstance (valid_for, str):
						is_valid = fnmatch (spec.version, valid_for)
					elif isinstance (valid_for, tuple) or isinstance (valid_for, list):
						is_valid = len (list (filter (lambda p: fnmatch (spec.version, p), valid_for))) > 0
					if not is_valid:
						with log ('frame'):
							logger.warning (f'framing not available for current version "{spec.version}"')
						try:
							if self._plugin['valid_enforce']:
								raise error ('framing: validation failed, termination enforced')
						finally:
							self._plugin = None
	
	def __getitem__ (self, option: str) -> Any:
		return self._namespace[option]
		
	def __setitem__ (self, option: str, value: Any) -> None:
		self._namesapce[option] = value
	
	def __delitem__ (self, option: str) -> None:
		del self._namespace[option]

	def _na (self, *args: Any, **kwargs: Any) -> None:
		return None
		
	def __getattr__ (self, name: str) -> Any:
		if self._plugin is not None:
			return getattr (self._plugin, name)
		return self._na

class Option (NamedTuple):
	option: str
	value: str

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
	__slots__ = ['cfg', 'ctx', 'frame', 'round']
	program_description: None | str = None
	program_epilog: None | str = None

	@classmethod
	def main (cls, *args: Any, **kwargs: Any) -> NoReturn:
		rt = cls (*args, **kwargs)
		try:
			sys.exit (0 if rt.run () else 1)
		except error as e:
			logger.error (f'{program}: failed: {e}')
			sys.exit (1)

	@dataclass
	class Context:
		processtitle: Processtitle = Processtitle ()
		verbose: int = 0
		debug: bool = False
		dryrun: bool = False
		expected_instances: bool = False
		lock_lazy: bool = False
		lock_id: None | str = None
		background: bool = False
		watchdog: bool = False
		restart_delay: None | int = None
		termination_delay: None | int = None
		max_incarnations: None | int = None
		heartbeat: None | int = None
		exit_code: None | int = None
		job: None | Watchdog.Job = None
		
	def __init__ (self, cfg: None | Config = None, **kwargs: Any) -> None:
		super ().__init__ ()
		if cfg is not None:
			self.cfg = cfg
		else:
			self.cfg = Config ()
			self.cfg.setup_namespace (**kwargs)
			self.cfg.enable_substitution ()
			self.cfg.read ()
			for key in [f'option:{program}', f'{program}:option']:
				with Ignore (KeyError):
					for (option, value) in Parameter (syscfg[key]).items ():
						self.cfg[option] = value
					if key == f'option:{program}':
						logger.warning (f'{key}: deprecated')
		self.ctx = Runtime.Context ()
		self.frame = Frame (runtime = self, ctx = self.ctx)
		self.round = 0

	def run (self, *args: Any, **kwargs: Any) -> Any:
		#
		self.cfg.push (host)
		with log ('setup'):
			self.frame.setup ()
			self.setup ()
		with log ('argparse'):
			self.__argument_parsing ()
		#
		executors = self.executors ()
		if self.ctx.expected_instances:
			count = 1 if self.ctx.watchdog else 0
			if executors is not None:
				count += len (executors)
			else:
				count += 1
			print (count, flush = True)
			return True
		#
		if self.ctx.background:
			if self.push_to_background ():
				logger.info ('Started background daemon process')
				os._exit (0)
			logger.info ('Background process started')
		self.setup_handler (True)
		with log ('prepare'):
			self.frame.prepare ()
			self.prepare ()
		ok = True
		with Lock (id = self.ctx.lock_id, lazy = self.ctx.lock_lazy) as lck, log ('main'):
			if lck is not None:
				logger.info (f'Starting up ({spec.typ}:{spec.version})')
				current_config = self.cfg.get_section (None)
				current_config.update (self.cfg.get_section (host))
				if current_config:
					logger.info ('  with {config}'.format (
						config = Stream (current_config.items ())
							.sorted ()
							.map (lambda kv: '{key}="{value}"'.format (key = kv[0], value = kv[1]))
							.join (', ')
					))
				try:
					def get_name (function: Callable[..., Any]) -> str:
						try:
							return function.__name__
						except AttributeError:
							try:
								return str (function)
							except:
								return program
					#
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
							self.ctx.max_incarnations
						)
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
				logger.info ('Going down ({status})'.format (status = 'success' if ok else 'fail'))
			else:
				logger.info ('Lock exists')
		with log ('cleanup'):
			self.frame.cleanup ()
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
	
	def title (self, title: str) -> Processtitle.ProcesstitleContext:
		return self.ctx.processtitle.title (title)
	
	def is_running (self, delay: int) -> bool:
		if self.round > 0:
			while delay > 0 and self.running:
				delay -= 1
				time.sleep (1)
		self.round += 1
		return self.running
			
	def __argument_parsing (self) -> None:
		parser = argparse.ArgumentParser (
			description = self.program_description,
			epilog = self.program_epilog,
			formatter_class = argparse.RawDescriptionHelpFormatter,
			add_help = False
		)
		parser.add_argument ('-h', '-?', '--help', action = 'help', help = 'shows help and exits')
		parser.add_argument ('-v', '--verbose', action = 'count', default = self.ctx.verbose, help = 'increase verbosity')
		parser.add_argument ('-q', '--quiet', action = 'count', default = 0, help = 'decrease verbosity')
		parser.add_argument ('--loglevel', action = 'store', help = 'set log level to value')
		parser.add_argument ('-c', '--config', action = 'store', help = 'read configuration from specified file')
		parser.add_argument ('-d', '--debug', action = 'store_true', default = self.ctx.debug, help = 'switch on debug mode')
		if self.supports ('dryrun'):
			parser.add_argument ('-n', '--dryrun', action = 'store_true', default = self.ctx.dryrun, help = 'switch to dry run mode, if supported')
		parser.add_argument ('-i', '--expected-instances', action = 'store_true', default = self.ctx.expected_instances, help = 'display expected instances for choosen invocation', dest = 'expected_instances')
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
		args = parser.parse_args (args = _expand_inline (sys.argv[1:]))
		self.use_arguments (args)
		self.ctx.verbose = args.verbose - args.quiet
		log.set_verbosity (self.ctx.verbose)
		if args.loglevel:
			log.set_loglevel (args.loglevel)
		if args.config:
			self.cfg.clear ()
			self.cfg.read (args.config)
		self.ctx.debug = args.debug
		if self.supports ('dryrun'):
			self.ctx.dryrun = args.dryrun
		self.ctx.expected_instances = args.expected_instances
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
	def setup (self) -> None:
		pass
	def supports (self, option: str) -> bool:
		return True
	def add_arguments (self, parser: argparse.ArgumentParser) -> None:
		pass
	def use_arguments (self, args: argparse.Namespace) -> None:
		pass
	def prepare (self) -> None:
		pass
	def executor (self) -> bool:
		return False
	def executors (self) -> None | list[Callable[[], bool]]:
		return None
	def cleanup (self) -> None:
		pass

class CLI (Daemonic):
	__slots__: List[str] = []
	program_description: None | str = None
	program_epilog: None | str = None

	@classmethod
	def main (cls, silent: bool = False) -> None:
		if not silent:
			with Ignore ():
				if os.isatty (sys.stdin.fileno ()):
					interactive ()
		rt = cls ()
		sys.exit (0 if rt.run () else 1)
	
	def run (self) -> bool:
		#
		self.setup ()
		self.__argument_parsing ()
		self.setsignal (signal.SIGPIPE, signal.SIG_IGN)
		self.prepare ()
		try:
			ok = self.executor ()
		except Exception as e:
			print (f'Execute failed due to: {e}', file = sys.stderr)
			print_exception (*sys.exc_info ())
			ok = False
		self.cleanup (ok)
		return ok

	def __argument_parsing (self) -> None:
		parser = argparse.ArgumentParser (
			description = self.program_description,
			epilog = self.program_epilog,
			formatter_class = argparse.RawDescriptionHelpFormatter,
			add_help = False
		)
		parser.add_argument ('-h', '-?', '--help', action = 'help', help = 'shows help and exits')
		self.add_arguments (parser)
		self.use_arguments (parser.parse_args (args = _expand_inline (sys.argv[1:])))
	#
	# Methods to be overwritten by client
	def setup (self) -> None:
		pass
	def add_arguments (self, parser: argparse.ArgumentParser) -> None:
		pass
	def use_arguments (self, args: argparse.Namespace) -> None:
		pass
	def prepare (self) -> None:
		pass
	def executor (self) -> bool:
		return False
	def cleanup (self, success: bool) -> None:
		pass

#
#	Mixins
#
class Preset:
	__slots__: List[str] = []
	def preset (self, presets: Dict[str, str | Callable[..., Any]]) -> None:
		rpresets: Dict[str, Callable[..., Any]] = (Stream (presets.items ())
			.map (lambda kv: (kv[0], getattr (self, f'preset_{kv[1]}') if isinstance (kv[1], str) else kv[1]))
			.dict ()
		)
		for (option, method) in rpresets.items ():
			setattr (self, option, method ())
		if (env := os.getenv (f'{program.upper ()}_OPTION')) is not None:
			try:
				for (option, value) in json.loads (env).items ():
					if isinstance (value, type (getattr (self, option))):
						setattr (self, option, value)
			except json.decoder.JSONDecodeError:
				for (option, value_repr) in Parameter (env).items ():
					setattr (self, option, rpresets[option] (value_repr))
	
	def preset_bool (self, value: None | str = None) -> bool:
		return False if value is None else atob (value)
		
	def preset_int (self, value: None | str = None) -> int:
		return 0 if value is None else int (value)
	
	def preset_str (self, value: None | str = None) -> None | str:
		return value

class Locate:
	__slots__: List[str] = []
	@classmethod
	def locate (cls,
		subclasses_of: _T,
		*,
		use: None | str = None,
		source: None | Dict[str, Any] = None,
		skip: None | Sequence[str] | Set[str] = None,
		exclude: None | Sequence[_T] | Set[_T] = None
	) -> List[_T]:
		return (Stream ((source if source is not None else cls.__dict__).items ())
			.filter (lambda kv: not kv[0].startswith ('_') and (skip is None or kv[0] not in skip))
			.map (lambda kv: kv[1])
			.filter (lambda v: type (v) is type and issubclass (v, subclasses_of) and v is not subclasses_of)
			.filter (lambda v: exclude is None or v not in exclude)
			.filter (lambda v: use is None or (hasattr (v, use) and (method := getattr (v, use)) is not None and callable (method) and method ()))
			.list ()
		)
