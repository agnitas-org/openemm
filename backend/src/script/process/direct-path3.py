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
import	logging, multiprocessing
import	os, time, shutil
from	dataclasses import dataclass
from	typing import Any
from	typing import Dict
from	agn3.daemon import Worker
from	agn3.definitions import base, syscfg
from	agn3.exceptions import error
from	agn3.io import ArchiveDirectory
from	agn3.mta import MTA
from	agn3.rpc import XMLRPC
from	agn3.runtime import Runtime
from	agn3.stream import Stream
#
logger = logging.getLogger (__name__)
#
class Processor:
	__slots__ = ['incoming', 'archive', 'recover', 'queues', 'cur', 'mta']
	def __init__ (self) -> None:
		self.incoming = syscfg.get_str ('direct-path-incoming', os.path.join (base, 'DIRECT'))
		self.archive = syscfg.get_str ('direct-path-archive', os.path.join (base, 'ARCHIVE'))
		self.recover = syscfg.get_str ('direct-path-recover', os.path.join (base, 'RECOVER'))
		self.queues = syscfg.get_list ('direct-path-queues', ',', Stream (os.listdir (base))
			.filter (lambda f: f.startswith ('QUEUE'))
			.map (lambda f: os.path.join (base, f))
			.filter (lambda p: os.path.isdir (p) and not os.path.isfile (os.path.join (p, '.ignore')))
			.list ()
		)
		if len (self.queues) == 0:
			raise error ('No queues for spooling found')
		self.queues.sort ()
		self.cur = multiprocessing.Value ('i', 0)
		self.mta = MTA ()
	
	def __next_queue (self) -> str:
		cval = self.cur.value
		if cval < 0 or cval >= len (self.queues):
			cval = 0
		rc: str = self.queues[cval]
		cval += 1
		if cval == len (self.queues):
			cval = 0
		self.cur.value = cval
		return rc
	
	def done (self) -> None:
		pass

	def doit (self, basename: str) -> None:
		(src, stamp, final) = [os.path.join (self.incoming, '%s.%s' % (basename, _e)) for _e in ('xml.gz', 'stamp', 'final')]
		if os.path.isfile (src):
			target = self.recover
			ok = True
			for path in stamp, final:
				if os.path.isfile (path):
					try:
						os.unlink (path)
					except OSError as e:
						logger.error ('Failed to remove %s: %s' % (path, str (e)))
						ok = False
				else:
					logger.error ('Failed to find file %s' % path)
					ok = False
			if ok:
				queue = self.__next_queue ()
				if self.mta (src, target_directory = queue, flush_count = '2'):
					logger.info ('Unpacked %s in %s' % (src, queue))
					try:
						target = ArchiveDirectory.make (self.archive)
					except error as e:
						logger.error ('Failed to setup archive directory %s: %s' % (self.archive, e))
						target = self.archive
				else:
					logger.error ('Failed to unpack %s in %s' % (src, queue))
					target = self.recover
			else:
				logger.error ('Do not process %s as control file(s) is/are missing' % src)
			dst = os.path.join (target, os.path.basename (src))
			try:
				shutil.move (src, dst)
			except (shutil.Error, IOError, OSError) as e:
				logger.error ('Failed to move %s to %s: %s' % (src, dst, str (e)))
				try:
					os.unlink (src)
				except OSError as e:
					logger.error ('Failed to remove file %s: %s' % (src, str (e)))
		else:
			logger.debug ('Skip requested file %s which is already processed' % src)
			for path in stamp, final:
				if os.path.isfile (path):
					try:
						os.unlink (path)
					except OSError as e:
						logger.error ('Failed to remove stale file %s: %s' % (path, str (e)))
	
	def process (self, basename: str) -> None:
		basename = os.path.basename (basename)
		logger.info ('Requested %s to process' % basename)
		self.doit (basename)

	def scan (self) -> None:
		files = sorted (os.listdir (self.incoming))
		for fname in [_f for _f in files if _f.endswith ('.final')]:
			basename = fname.split ('.')[0]
			if '%s.xml.gz' % basename in files and '%s.stamp' % basename in files:
				logger.info ('Found %s to process' % fname)
				self.doit (basename)

class DirectPath (Worker):
	def __unpack (self, filename: str) -> bool:
		if filename:
			self.enqueue (filename)
		return True

	@dataclass
	class ControllerCTX:
		last: int = 0
	def controller_setup (self) -> Any:
		defaults: Dict[str, Any] = {
			'xmlrpc.host': 'localhost',
			'xmlrpc.port':  9400,
			'xmlrpc.allow_none': True
		}
		for (var, value) in defaults.items ():
			if var not in self.cfg:
				self.cfg[var] = value
		return DirectPath.ControllerCTX ()

	def controller_register (self, ctx: Any, serv: XMLRPC) -> None:
		serv.add_method (self.__unpack, name = 'unpack')
	
	def controller_step (self, ctx: Any) -> None:
		now = time.time ()
		if ctx.last + 60 < now:
			self.enqueue (None, oob = True)
			ctx.last = now

	def executor_config (self, what: str, default: Any) -> Any:
		if what == 'handle-multiple-requests':
			return True
		return super ().executor_config (what, default)

	@dataclass
	class ExecutorCTX:
		process: Processor
	def executor_setup (self) -> DirectPath.ExecutorCTX:
		return DirectPath.ExecutorCTX (process = Processor ())

	def executor_teardown (self, ctx: DirectPath.ExecutorCTX) -> None: 
		ctx.process.done ()
		
	def executor_request_handle (self, ctx: DirectPath.ExecutorCTX, rq: Any) -> None:
		if len (rq) > 1 or rq[0] is None:
			ctx.process.scan ()
		else:
			ctx.process.process (rq[0])

class Main (Runtime):
	def supports (self, option: str) -> bool:
		return option != 'dryrun'

	def executor (self) -> bool:
		dpath = DirectPath ('direct-path')
		dpath.start ()
		return True
#
if __name__ == '__main__':
	Main.main ()
