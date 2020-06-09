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
import	os, time, shutil, errno, logging
from	sched import scheduler
from	signal import Signals
from	types import FrameType, TracebackType
from	typing import Any, Callable, Optional, Union
from	typing import Iterator, List, TextIO, Tuple, Type
from	typing import cast
from	.daemon import Watchdog
from	.definitions import base
from	.exceptions import error
from	.io import file_access
from	.log import log
from	.parser import Unit
#
__all__ = ['Batch', 'Transaction', 'Schedule', 'Jobqueue']
#
logger = logging.getLogger (__name__)
#
class Batch:
	"""Handling of batch files

This class provides a frame work for batch processing using a file"""
	__slots__ = ['path', 'directory', 'filename', 'instance_unique']
	batch_unique = 0
	def __init__ (self, path: str) -> None:
		"""``path'' is the file to use for data exchange"""
		self.path = path
		self.directory = os.path.dirname (path)
		self.filename = os.path.basename (path)
		Batch.batch_unique += 1
		self.instance_unique = [Batch.batch_unique, 0]
	#
	# Producer (simple, ey)
	def write (self, s: str) -> None:
		"""write a line to the file"""
		with open (self.path, 'a') as fd:
			fd.write (f'{s}\n')
	
	#
	# Consumer (rest of the class)
	class Temp: #{{{
		__slots__ = ['orig', 'base_target', 'path', 'fail', 'fd']
		def __init__ (self, orig: str, base_target: str) -> None:
			self.orig = orig
			self.base_target = base_target
			self.path: Optional[str] = None
			self.fail = False
			self.fd: Optional[TextIO] = None
			
		def __del__ (self) -> None:
			if not self.fail:
				self.remove ()

		def __enter__ (self) -> Batch.Temp:
			if not self.open () and self.path is not None and os.path.isfile (self.path):
				raise error (f'{self.orig}: failed to open')
			return self
		
		def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
			self.close ()
			if exc_value is None:
				self.remove ()
			return None
			
		def remove (self) -> None:
			if self.path is not None:
				self.close ()
				try:
					os.unlink (self.path)
					self.path = None
				except OSError as e:
					if e.args[0] != errno.ENOENT:
						logger.error ('Failed to remove temp.file "%s": %s' % (self.path, e))
						self.fail = True
					else:
						self.path = None
		
		def move (self, destination: str) -> None:
			if self.path is not None:
				self.close ()
				if os.path.isdir (destination):
					dpath = os.path.join (destination, self.orig)
					if os.path.isfile (dpath):
						dpath = os.path.join (destination, self.path)
				else:
					dpath = destination
					n = 0
					while n < 128 and os.path.isfile (dpath):
						n += 1
						dpath = '%s-%d' % (destination, n)
				try:
					shutil.move (self.path, dpath)
					self.path = None
				except OSError as e:
					logger.exception ('Failed to move "%s" to "%s": %s' % (self.path, dpath, e))
					self.fail = True
		
		def save (self, name: str, line: str) -> None:
			with open (log.data_filename (name), 'a') as fd:
				fd.write (f'{line}\n')
		
		def open (self) -> bool:
			if self.fd is None:
				if self.path is not None:
					self.fd = open (self.path, 'r')
			else:
				self.fd.seek (0)
			return self.fd is not None

		def close (self) -> None:
			if self.fd is not None:
				self.fd.close ()
				self.fd = None
		
		def read (self, size: Optional[int] = None) -> str:
			if self.fd is not None:
				if size is None:
					return self.fd.read ()
				return self.fd.read (size)
			return ''
			
		def readline (self) -> str:
			if self.fd is not None:
				return self.fd.readline ()
			return ''
		
		def __iter__ (self) -> Iterator[str]:
			self.open ()
			if self.fd is not None:
				for line in self.fd:
					yield line.strip ()
			self.close ()

		def __save (self, what: str, s: str) -> None:
			now = time.localtime ()
			target = os.path.join (base, 'var', 'log', '%04d%02d%02d-%s.log' % (now.tm_year, now.tm_mon, now.tm_mday, self.base_target))
			output = '[%02d.%02d.%04d %02d:%02d:%02d] %s: %s\n' % (now.tm_mday, now.tm_mon, now.tm_year, now.tm_hour, now.tm_min, now.tm_sec, what, s)
			try:
				with open (target, 'a') as fd:
					fd.write (output)
			except IOError as e:
				logger.exception ('Failed to write to "%s": %s' % (target, e))
				self.fail = True
		
		def success (self, s: str) -> None:
			self.__save ('success', s)
		def failure (self, s: str) -> None:
			self.__save ('failure', s)
	#}}}
	def open (self, delay: int = 2, retry: int = 3) -> Batch.Temp:
		"""opens current batch, ``retry'' time to access the
file with ``delay'' seconds waiting between each try.
		
On success this returns a Batch.Temp instance, which behaves like a
file for reading. This instance provides some helpful methods for
further handling of the processed file:
	- open(): open or rewind the file (must be called at least once)
	- remove(): delete the file
	- move(destination): move the file to destination
	- success(s): writes the string ``s'' to agnitas style data logfile for successful processed lines
	- failure(s): writes the string ``s'' to agnitas style data logfile for failed processed lines
"""
		rc = Batch.Temp (self.path, self.filename)
		if os.path.isfile (self.path):
			self.instance_unique[1] += 1
			temp = os.path.join (self.directory, '.%s.%d.%d.%d.%.3f.temp' % (self.filename, os.getpid (), self.instance_unique[0], self.instance_unique[1], time.time ()))
			try:
				os.rename (self.path, temp)
			except OSError as e:
				logger.exception ('Failed to rename "%s" to "%s": %s' % (self.path, temp, e))
			else:
				fail = False
				if delay > 0:
					time.sleep (delay)
				if retry > 0:
					while retry > 0 and len (file_access (temp)[0]) > 0:
						time.sleep (1)
						retry -= 1
					if retry == 0:
						logger.error ('File "%s" still in usage' % temp)
						fail = True
				if not fail:
					rc.path = temp
		return rc

class Transaction:
	"""handling of transaction/rollback sequence
	
This class will be filled with a list of methods and its coresponding
rollback method to build ab a sequence of methods. During execution
each method will be called in sequence. If one method fails, the
rollback methods assigned to the already executed methods are called."""
	__slots__ = ['spool', 'back']
	def __init__ (self) -> None:
		self.spool: List[Tuple[str, Optional[Callable[..., None]], Tuple[Any, ...], Optional[Callable[..., None]], Tuple[Any, ...]]] = []
		self.back: List[Tuple[str, Optional[Callable[..., None]], Tuple[Any, ...]]] = []
	
	def __mkargs (self, args: Any) -> Tuple[Any, ...]:
		if args is None:
			return ()
		if type (args) is tuple:
			return cast (Tuple[Any], args)
		if type (args) is list:
			return tuple (args)
		if type (args) is not tuple:
			return (args, )
		raise error (f'invalid arguments: {args!r}')

	def add (self,
		name: str,
		method: Optional[Callable[..., None]],
		method_arguments: Any = None,
		rollback: Optional[Callable[..., None]] = None,
		rollback_arguments: Any = None
	) -> None:
		"""add a method and its rollback method with each arguments"""
		self.spool.append ((name, method, method_arguments, rollback, rollback_arguments))
	
	def execute (self,
		silent: bool = False,
		rollback_failed: bool = False,
		callback: Optional[Callable[[str, str], None]] = None
	) -> None:
		"""executes the sequence, on failure and ``silent'' is
False, the exeption causing the error is raised. If ``rollback_failed''
is True, the rollback method for the method failed will be executed,
too, otherwise only all rollback methods for the successful methods
are executed. If ``callback''is not None, it is invoked with two
arguments, the current name of the method and a state."""
		error = None
		self.reset ()
		for (name, method, method_arguments, rollback, rollback_arguments) in self.spool:
			try:
				if method is not None:
					if callback: callback (name, 'start')
					if rollback_failed:
						self.back.append ((name, rollback, rollback_arguments))
						method (*self.__mkargs (method_arguments))
					else:
						method (*self.__mkargs (method_arguments))
						self.back.append ((name, rollback, rollback_arguments))
					if callback: callback (name, 'done')
				else:
					self.back.append ((name, rollback, rollback_arguments))
			except Exception as e:
				if callback: callback (name, 'fail')
				logger.error ('Execution failed, transaction aborted due to %s' % e)
				error = e
				break
		if error is not None:
			self.rollback (callback)
			if not silent:
				raise error
	
	def rollback (self, callback: Optional[Callable[[str, str], None]] = None) -> None:
		"""If a method fails, this methods unwinds the
rollback stack and executes the rollback methods. If ``callback''is
not None, it is invoked with two arguments, the current name of the
method and a state."""
		while self.back:
			(name, rollback, rollback_arguments) = self.back.pop ()
			if rollback is not None:
				try:
					if callback: callback (name, 'rollback')
					rollback (*self.__mkargs (rollback_arguments))
					if callback: callback (name, 'done')
				except Exception as e:
					if callback: callback (name, 'fail')
					logger.error ('Rollback failed due to %s' % e)
	
	def reset (self) -> None:
		"""clears the rollback stack"""
		self.back = []

class Schedule:
	"""An enhanced scheduler

This class is build around the python sched module but supports
repeating execution of a job. Each job must be encapsuled using the
class Schedule.Job so the housekeeping is working.
"""
	__slots__ = ['_schedule', '_active', '_running', '_jobclass']
	class Job:
		"""Single job in Schedule"""
		__slots__ = ['name', 'repeat', 'delay', 'priority', 'method', 'arguments']
		unit = Unit ()
		def __init__ (self,
			name: str,
			repeat: bool,
			delay: Union[None, int, str],
			priority: int,
			method: Callable[..., Any],
			arguments: Tuple[Any, ...]
		) -> None:
			"""Create a job with ``name'', if
``repeat'' is True, repeat it with a ``delay'' in seconds between each
execution. ``priority'' controls the sequence of jobs which are ready
at the same time while a lower value means a higher priority.
``method'' is invoked using ``arguments'', if using the class itself,
otherwise the execute() method can be overwritten to implement the
job."""
			self.name = name
			self.repeat = repeat
			self.delay = self.unit.parse (delay) if type (delay) is str else cast (int, delay)
			self.priority = priority
			self.method = method
			self.arguments = arguments
		
		def __str__ (self) -> str:
			return '%s <name = %s, repeast = %s, delay = %s, priority = %s>' % (self.__class__.__name__, self.name, self.repeat, self.delay, self.priority)
			
		def __call__ (self, schedule: Schedule) -> None:
			schedule.log ('job', '%s started' % self.name)
			start = time.time ()
			rc = self.execute ()
			end = time.time ()
			schedule.log ('job', '%s terminated %s after %.2f seconds' % (self.name, ('successful (%r)' % rc) if type (rc) is bool else ('with error (%r)' % (rc, )), end - start))
			if self.repeat:
				if rc:
					schedule.log ('job', '%s reactivate' % self.name)
					schedule._add (self)
				else:
					schedule.log ('job', '%s not reactivated' % self.name)
			else:
				schedule.log ('job', '%s not reactivated due to single run' % self.name)
			
		def execute (self) -> Any:
			"""Entry point for execution, must return boolean value, False for error, otherwise True"""
			return self.method (*self.arguments)

	def __init__ (self, jobclass: Optional[Type[Schedule.Job]] = None) -> None:
		"""Create an instance using ``jobclass'' for encapsulation, Schedule.Job is used, if ``jobclass'' is None"""
		self._schedule = scheduler (self._timer, self._delayer)
		self._active = True
		self._running = True
		self._jobclass = jobclass if jobclass is not None else self.Job
		
	def _timer (self) -> float:
		return time.time ()
		
	def _delayer (self, amount: float) -> None:
		self.log ('delay', 'for %.2f seconds' % amount)
		while self._running and amount > 0.0:
			if amount >= 1.0:
				delay = 1.0
				amount -= 1.0
			else:
				delay = amount
				amount = 0.0
			self.intercept ()
			if self._running:
				time.sleep (delay)
		self.log ('delay', 'done' if amount == 0.0 else f'terminated with {amount} seconds left')
		
	def _offset (self, job: Schedule.Job, immediately: bool) -> int:
		if not immediately and job.repeat and job.delay is not None and job.delay > 0:
			time.tzset ()
			now = int (self._timer ()) - time.timezone
			offset = (now // job.delay + 1) * job.delay - now
			while offset <= 0:
				offset += job.delay
			return offset
		return 0
			
	def _add (self, job: Schedule.Job, immediately: bool = False) -> Schedule.Job:
		if self._active:
			offset = self._offset (job, immediately)
			self.log ('add', 'job %s with offset in %d seconds with priority %d' % (job.name, offset, job.priority))
			self._schedule.enter (offset, job.priority, job, (self, ))
		else:
			self.log ('add', 'not add job %s due inactive scheduler' % job.name)
		return job
			
	def every (self,
		name: str,
		immediately: bool,
		delay: Union[None, int, str],
		priority: int,
		method: Callable[..., Any],
		arguments: Tuple[Any, ...]
	) -> Schedule.Job:
		"""Add a repeating job with ``name''. Execute
it ``immediately'', if True, repeat after ``delay'' seconds using
``priority''. Call ``method'' with ``arguments'' for execution."""
		return self._add (self._jobclass (name, True, delay, priority, method, arguments), immediately)
	def once (self,
		name: str,
		delay: Union[None, int, str],
		priority: int,
		method: Callable[..., Any],
		arguments: Tuple[Any, ...]
	) -> Schedule.Job:
		"""Add a single job with ``name'', start it
after ``delay'' seconds using ``priority''. Call ``method'' with
``arguments''."""
		return self._add (self._jobclass (name, False, delay, priority, method, arguments))
		
	def start (self) -> None:
		"""Start the scheduling"""
		self.log ('run', 'start')
		self._active = True
		while not self._schedule.empty ():
			self._schedule.run ()
		self.log ('run', 'end')
		
	def stop (self) -> None:
		"""Stop scheduling and cancel all pending jobs"""
		self.log ('stop', 'request')
		self._active = False
		while not self._schedule.empty ():
			self.log ('stop', 'cancel %s' % self._schedule.queue[0].action)
			self._schedule.cancel (self._schedule.queue[0])
		self.log ('stop', 'finished')
		
	def term (self) -> None:
		"""Terminate scheduling"""
		self.log ('term', 'request')
		self.stop ()
		self._running = False
		self.log ('term', 'finished')

	def log (self, area: str, what: str) ->None:
		"Hook: logging of scheduling events"
		
	def intercept (self) -> None:
		"Hook: intercept is called during delaying"""
	
class Jobqueue (Watchdog):
	"""class Jobqueue (Watchdog):

a scheduler running as a daemon process with watchdog to restart on
errors. To use this class, you should subclass class Schedule and
overwrite the method start() to add jobs to the scheduler, e.g.:

class MySchedule (agn3.flow.Schedule):
	def start (self):
		self.every ('foo', False, '1h', 1, self.foo, ())
		self.every ('bar', False, '6h', 2, self.bar, ())
		self.once ('restart', '1w', 0, self.stop, ()) # this will force the watchdog to restart the subprocess each week
		return super ().start ()
	
	def foo (self):
		# do something evey hour
		return True
	
	def bar (self):
		# do something every six hour
		return True

Then create an instance of jobqueue and start it:

jq = agn3.flow.Jobqueue (MySchedule ())
jq.start ()
"""
	__slots__ = ['schedule', 'schedule_pid']
	def __init__ (self, schedule: Schedule) -> None:
		"""Setup using ``schedule'' instance"""
		super ().__init__ ()
		self.schedule = schedule
		self.schedule_pid: Optional[int] = None

	def signal_handler (self, sig: Signals, stack: FrameType) -> None:
		"""Catch signal for watchdog"""
		super ().signal_handler (sig, stack)
		if not self.running:
			if self.schedule_pid == os.getpid ():
				self.schedule.term ()

	def run (self, *args: Any, **kwargs: Any) -> Any:
		"""Entry point"""
		self.schedule_pid = os.getpid ()
		self.schedule.start ()
		if self.schedule._running:
			raise self.Job.Restart ()
		return True
			
	def start (self, *args: Any, **kwargs: Any) -> None:
		"""Start the jobqueue"""
		self.mstart (self.Job ('schedule', self.run, ()), *args, **kwargs)
