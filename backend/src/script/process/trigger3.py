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
import	logging, argparse
import	threading, socket
from	typing import Optional, Protocol
from	typing import Dict, List, Tuple
from	typing import cast
from	agn3.config import Config
from	agn3.db import DB
from	agn3.definitions import syscfg
from	agn3.rpc import XMLRPC, XMLRPCClient
from	agn3.runtime import Runtime
from	agn3.tools import atoi
#
logger = logging.getLogger (__name__)
#
class MergerProxy (Protocol):
	class Merger:
		@staticmethod
		def remote_control (command: str, parameter: str) -> str: ...

class DBAccess (DB):
	lock = threading.Lock ()
	def __init__ (self) -> None:
		super ().__init__ ()
		self.lock.acquire ()
	
	def done (self) -> None:
		self.lock.release ()
		super ().done ()

class Trigger:
	def __init__ (self, cfg: Config) -> None:
		self.execlock = threading.Lock ()
		self.cfg = cfg
		self.merger = cast (MergerProxy, XMLRPCClient (self.cfg, 'merger'))
		self.timeout = self.cfg.iget ('merger.timeout', 5)

	def fire (self, statusID: int) -> Tuple[int, str]:
		(rc, report) = (-1, '')
		if self.execlock.acquire ():
			otimeout = socket.getdefaulttimeout ()
			socket.setdefaulttimeout (self.timeout)
			try:
				report = self.merger.Merger.remote_control ('fire', str (statusID))
				logger.debug ('statusID %d fired: %r' % (statusID, report))
				rc = 0
			except Exception as e:
				logger.warning ('Failed to trigger statusID %d: %s' % (statusID, str (e)))
				report = 'Failed to start merger: %s' % str (e)
			finally:
				socket.setdefaulttimeout (otimeout)
				self.execlock.release ()
		else:
			logger.error ('Failed to get exclusive lock to trigger statusID %d' % statusID)
			report += 'Failed to get exclusive access\n'
		return (rc, report)
	
	def demand (self, mailingID: int) -> Tuple[int, str]:
		(rc, report) = (-1, '')
		statusID: Optional[int] = None
		with DBAccess () as dba:
			genStatus: Optional[int] = None
			for r in dba.queryc ('SELECT status_id, genstatus FROM maildrop_status_tbl WHERE mailing_id = :mid AND status_field = \'D\'', {'mid': mailingID}):
				if r[0] and (statusID is None or statusID < r[0]):
					if statusID is None or r[1] in (1, 3):
						statusID = r[0]
						genStatus = r[1]
			if statusID is None:
				logger.warning ('No entry in maildrop_status_tbl for mailing_id %r found' % mailingID)
				report += 'Mailing %r is not an on demand mailing\n' % mailingID
			elif genStatus is not None:
				if genStatus not in (1, 3):
					logger.warning ('Mailing %d has genstatus %d, unable to restart at the moment' % (mailingID, genStatus))
					report += 'Mailing %r has genstatus %r and can not be restarted at this point\n' % (mailingID, genStatus)
				else:
					if genStatus == 3:
						logger.debug ('Reset mailing')
						rows = dba.update ('UPDATE maildrop_status_tbl SET genstatus = 1 WHERE status_id = :statusID AND genstatus = 3', {'statusID': statusID})
						dba.sync (rows == 1)
					else:
						logger.debug ('Found mailing %r with statusID %d and genstatus %d' % (mailingID, statusID, genStatus))
		if statusID is not None:
			(rc, report) = self.fire (statusID)
		return (rc, report)
	
	def reset (self, statusID: int) -> Tuple[int, str]:
		(rc, report) = (-1, '')
		with DBAccess () as dba:
			rows = dba.update ('UPDATE maildrop_status_tbl SET genstatus = 1, genchange = sysdate WHERE status_id = :sid', {'sid': statusID})
			if rows == 1:
				rc = 0
			else:
				report += 'Expected one row to update, updated %d rows' % rows
			dba.sync (rc == 0)
		return (rc, report)
	
	def __answer (self, ok: bool, s: str) -> XMLRPC.Answer:
		if ok:
			logger.debug ('OK : %s' % s)
			content = '+OK: %s\n' % s
		else:
			logger.info ('ERR: %s' % s)
			content = '-ERR: %s\n' % s
		return XMLRPC.Answer (code = 200, mime = 'text/plain', headers = [], content = content)

	def doPing (self, path: str, param: Optional[str], parsed_param: Optional[Dict[str, List[str]]]) -> XMLRPC.Answer:
		logger.debug ('PING')
		return self.__answer (True, 'pong')
	
	def doDemand (self, path: str, param: Optional[str], parsed_param: Optional[Dict[str, List[str]]]) -> XMLRPC.Answer:
		mailingID = atoi (param)
		if not mailingID:
			logger.debug ('DEMAND without mailingID')
			return self.__answer (False, 'Invalid parameter %r, mailingID expected' % param)
		logger.debug ('DEMAND %r' % (mailingID, ))
		(rc, report) = self.demand (mailingID)
		return self.__answer (rc == 0, report)
	
	def doFire (self, path: str, param: Optional[str], parsed_param: Optional[Dict[str, List[str]]]) -> XMLRPC.Answer:
		statusID = atoi (param)
		if not statusID:
			logger.debug ('FIRE without statusID')
			return self.__answer (False, 'Invalid parameter %r, statusID expected' % param)
		logger.debug ('FIRE %r' % (statusID, ))
		(rc, report) = self.fire (statusID)
		return self.__answer (rc == 0, report)

	def doReset (self, path: str, param: Optional[str], parsed_param: Optional[Dict[str, List[str]]]) -> XMLRPC.Answer:
		statusID = atoi (param)
		if not statusID:
			logger.debug ('RESET without statusID')
			return self.__answer (False, 'Invalid parameter %r, statusID expected' % param)
		logger.debug ('RESET %r' % (statusID, ))
		(rc, report) = self.reset (statusID)
		return self.__answer (rc == 0, report)

class Callback (XMLRPC.RObject):
	def __init__ (self, trigger: Trigger) -> None:
		super ().__init__ ()
		self.trigger = trigger
	
	def __response (self, rc: Tuple[int, str]) -> Tuple[bool, str]:
		return (rc[0] == 0, rc[1])

	def startMailing (self, statusID: int) -> Tuple[bool, str]:
		logger.debug ('startMailing %r' % (statusID, ))
		return self.__response (self.trigger.fire (statusID))

class Main (Runtime):
	__slots__ = ['port']
	def supports (self, option: str) -> bool:
		return option != 'dryrun'

	def add_arguments (self, parser: argparse.ArgumentParser) -> None:
		parser.add_argument ('-P', '--port', action = 'store', type = int, default = syscfg.get_int ('trigger-port', 8080))
	
	def use_arguments (self, args: argparse.Namespace) -> None:
		self.port = args.port
		
	def executor (self) -> bool:
		for (option, value) in [
			('xmlrpc.port', str (self.port)),
			('xmlrpc.server', 'forking'),
			('merger.port', '8089')
		]:
			if option not in self.cfg:
				self.cfg[option] = value
		server = XMLRPC (self.cfg, full = True)
		trigger = Trigger (self.cfg)
		server.add_handler ('/ping', trigger.doPing)
		server.add_handler ('/demand', trigger.doDemand)
		server.add_handler ('/fire', trigger.doFire)
		server.add_handler ('/reset', trigger.doReset)
		cb = Callback (trigger)
		server.add_instance (cb)
		server.run ()
		return True

if __name__ == '__main__':
	Main.main ()
