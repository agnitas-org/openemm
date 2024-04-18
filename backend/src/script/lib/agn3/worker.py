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
import	os, logging, time
import	select, multiprocessing
from	collections import deque
from	multiprocessing.queues import Queue
from	queue import Empty
from	typing import Any, Optional
from	typing import Deque, List
from	.config import Config
from	.daemon import Daemon
from	.definitions import base, host
from	.exceptions import error
from	.ignore import Ignore
from	.log import log
from	.process import Parallel
from	.rpc import XMLRPC
#
__all__ = ['Worker']
#
logger = logging.getLogger (__name__)
#
class Worker (Daemon):
	"""Framework for a XML-RPC daemon with worker process

This provides a two process framework where one serves client requests
via XML-RPC )controller) while the other performs the operation as a
worker (executor). Subclass this class and implement the desired
overwritable methods. 

1.) Controller:
- controller_config: request a configuration value for a parameter, currently these are available:
	- xmlrpc-server-timeout (float, 5.0): defines the timeout for the XML-RPC server communication
- controller_setup: is called once at the beginning to inititalize the controller part of the service
	This method returns a generic context which is passed to further methods as first parameter
- controller_teardown: is called once at the end of the controller
- controller_register: is used to register all XML-RPC methods which should be callable
- controller_step: is regulary called during server operation

The controller methods can call ``enqueue'' to pass an object to the
worker part, the executor.

2.) Executor:
- executor_config: request a configuration value for a parameter, currently these are available:
	- ignore-duplicate-requests (bool, False): If identical requests are found in the queue, only one is passed for processing, if this is set to True
	- handle-multiple-requests (bool, False): If False, enqueued objects are passed one by one for processing, otherwise they are passed in chunks
- executor_setup: is called once at the beginning to inititalize the executor part of the service
	This method returns a generic context which is passed to further methods as first parameter
- executor_teardown: is called once at the end of the executor
- executor_step: is regulary called during server operation
- executor_request_preparse: preparses a received object from the controller, must return the final object
- executor_request_next: prepare the next object (or objects as a list, if "handle-multiple-requests" is True); must return the value to finally process
- executor_request_handle: processes the object (or the list of objects) in a subprocess
"""
	__slots__ = ['cfg', 'queue', 'backlog', 'ctrl']
	nameCtrl = 'ctrl'
	nameExec = 'exec'
	
	def controller_config (self, what: str, default: Any) -> Any:
		"""returns configuration value for ``what'', using ``default'' if no custom value is available"""
		return default
	def controller_setup (self) -> Any:
		"""setup up context to use in further calls"""
		return None
	def controller_teardown (self, ctx: Any) -> None:
		"""cleanup used resources"""
	def controller_register (self, ctx: Any, serv: XMLRPC) -> None:
		"""register methods to XML-RPC server ``serv''"""
	def controller_step (self, ctx: Any) -> None:
		"""called periodically"""
		
	def __controller (self) -> None:
		logger.debug ('Controller starting')
		ctx = self.controller_setup ()
		timeout = self.controller_config ('xmlrpc-server-timeout', 5.0)
		serv = XMLRPC (self.cfg, timeout = timeout)
		self.controller_register (ctx, serv)
		while self.running:
			self.controller_step (ctx)
			while self.backlog and not self.queue.full ():
				self.queue.put (self.backlog.popleft ())
			with Ignore (select.error):
				serv.server.handle_request ()
		self.controller_teardown (ctx)
		logger.debug ('Controller terminating')

	def executor_config (self, what: str, default: Any) -> Any:
		"""returns configuration value for ``what'', using ``default'' if no custom value is available"""
		return default
	def executor_setup (self) -> Any:
		"""setup up context to use in further calls"""
		return None
	def executor_teardown (self, ctx: Any) -> None:
		"""cleanup used resources"""
	def executor_step (self, ctx: Any) -> None:
		"""called periodically"""
	def executor_request_preparse (self, ctx: Any, rq: Any) -> Any:
		"""preparses request ``rq'' after fetching from queue"""
		return rq
	def executor_request_next (self, ctx: Any, rq: Any) -> Any:
		"""prepare request(s) ``rq'' before being processed"""
		return rq
	def executor_request_handle (self, ctx: Any, rq: Any) -> None:
		"""process request(s) ``rq''"""
		
	def __executor (self) -> None:
		logger.debug ('Executor starting')
		ctx = self.executor_setup ()
		ignoreDups = self.executor_config ('ignore-duplicate-requests', False)
		multipleRequests = self.executor_config ('handle-multiple-requests', False)
		pending: List[Any] = []
		child: Optional[multiprocessing.Process] = None
		while self.running:
			self.executor_step (ctx)
			with Ignore (IOError, Empty, error):
				rq = self.queue.get (timeout = 1.0)
				logger.debug ('Received {rq!r} while {count} requests pending'.format (
					rq = rq,
					count = len (pending)
				))
				rq = self.executor_request_preparse (ctx, rq)
				if not ignoreDups or rq not in pending:
					pending.append (rq)
				else:
					logger.debug (f'Ignore request {rq!r} as already one is pending')
			#
			while len (pending) > 0:
				logger.debug ('{count} pending requests'.format (count = len (pending)))
				if child is not None:
					child.join (0)
					if not child.is_alive ():
						child.close ()
						child = None
						logger.debug ('Previous instance had finished')
				if child is None:
					if multipleRequests:
						rq = pending
						pending = []
					else:
						rq = pending.pop (0)
					try:
						rq = self.executor_request_next (ctx, rq)
						child = multiprocessing.Process (target = self.executor_request_handle, args = (ctx, rq))
						child.start ()
						logger.debug ('Started instance process')
					except error as e:
						logger.error (f'Failed to start child: {e}')
				else:
					logger.debug ('Busy')
					break
		if child is not None:
			child.join ()
			child.close ()
		self.executor_teardown (ctx)
		logger.debug ('Executor terminating')
	
	def enqueue (self, obj: Any, oob: bool = False) -> None:
		"""Put ``obj'' from controller to executor, in front of queue, when ``oob'' is set"""
		if self.queue.full () or self.backlog:
			if oob:
				self.backlog.appendleft (obj)
			else:
				self.backlog.append (obj)
		if not self.queue.full ():
			if self.backlog:
				while self.backlog and not self.queue.full ():
					self.queue.put (self.backlog.popleft ())
			else:
				self.queue.put (obj)

	def run (self, *args: Any, **kwargs: Any) -> Any:
		"""Entry point for starting process"""
		path = os.path.join (base, 'scripts', '{name}.cfg'.format (name = self.name if self.name is not None else host))
		self.cfg = Config ()
		if os.path.isfile (path):
			self.cfg.read (path)
		self.queue: Queue[Any] = multiprocessing.Queue ()
		self.backlog: Deque[Any] = deque ()
		self.ctrl = Parallel ()
		self.ctrl.fork (self.__controller, name = self.nameCtrl, logname = f'{log.name}-{self.nameCtrl}')
		self.ctrl.fork (self.__executor, name = self.nameExec, logname = f'{log.name}-{self.nameExec}')
		while self.running:
			time.sleep (1)
		self.ctrl.wait (timeout = 2.0)
		self.ctrl.term ()
		self.ctrl.done ()
