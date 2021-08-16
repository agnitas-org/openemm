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
from	typing import Any, Optional, Protocol
from	typing import Dict, List, Tuple
from	typing import cast
from	agn3.config import Config
from	agn3.db import DB
from	agn3.definitions import licence, user, syscfg
from	agn3.emm.companyconfig import CompanyConfig
from	agn3.ignore import Ignore
from	agn3.process import Processentry, Processtable
from	agn3.rpc import XMLRPC, XMLRPCClient
from	agn3.runtime import Runtime
from	agn3.stream import Stream
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

	def fire (self, status_id: int) -> Tuple[int, str]:
		(rc, report) = (-1, '')
		if self.execlock.acquire ():
			otimeout = socket.getdefaulttimeout ()
			socket.setdefaulttimeout (self.timeout)
			try:
				report = self.merger.Merger.remote_control ('fire', str (status_id))
				logger.debug ('status_id %d fired: %r' % (status_id, report))
				rc = 0
			except Exception as e:
				logger.warning ('Failed to trigger status_id %d: %s' % (status_id, str (e)))
				report = 'Failed to start merger: %s' % str (e)
			finally:
				socket.setdefaulttimeout (otimeout)
				self.execlock.release ()
		else:
			logger.error ('Failed to get exclusive lock to trigger status_id %d' % status_id)
			report += 'Failed to get exclusive access\n'
		return (rc, report)
	
	def demand (self, mailing_id: int) -> Tuple[int, str]:
		(rc, report) = (-1, '')
		status_id: Optional[int] = None
		with DBAccess () as dba:
			genstatus: Optional[int] = None
			for r in dba.queryc ('SELECT status_id, genstatus FROM maildrop_status_tbl WHERE mailing_id = :mid AND status_field = \'D\'', {'mid': mailing_id}):
				if r[0] and (status_id is None or status_id < r[0]):
					if status_id is None or r[1] in (1, 3):
						status_id = r[0]
						genstatus = r[1]
			if status_id is None:
				logger.warning ('No entry in maildrop_status_tbl for mailing_id %r found' % mailing_id)
				report += 'Mailing %r is not an on demand mailing\n' % mailing_id
			elif genstatus is not None:
				if genstatus not in (1, 3):
					logger.warning ('Mailing %d has genstatus %d, unable to restart at the moment' % (mailing_id, genstatus))
					report += 'Mailing %r has genstatus %r and can not be restarted at this point\n' % (mailing_id, genstatus)
				else:
					if genstatus == 3:
						logger.debug ('Reset mailing')
						rows = dba.update ('UPDATE maildrop_status_tbl SET genstatus = 1 WHERE status_id = :status_id AND genstatus = 3', {'status_id': status_id})
						dba.sync (rows == 1)
					else:
						logger.debug ('Found mailing %r with status_id %d and genstatus %d' % (mailing_id, status_id, genstatus))
		if status_id is not None:
			(rc, report) = self.fire (status_id)
		return (rc, report)
	
	def reset (self, status_id: int) -> Tuple[int, str]:
		(rc, report) = (-1, '')
		with DBAccess () as dba:
			rows = dba.update ('UPDATE maildrop_status_tbl SET genstatus = 1, genchange = sysdate WHERE status_id = :sid', {'sid': status_id})
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

	def do_ping (self, path: str, param: Optional[str], parsed_param: Optional[Dict[str, List[str]]]) -> XMLRPC.Answer:
		logger.debug ('PING')
		return self.__answer (True, 'pong')
	
	def do_demand (self, path: str, param: Optional[str], parsed_param: Optional[Dict[str, List[str]]]) -> XMLRPC.Answer:
		mailing_id = atoi (param)
		if not mailing_id:
			logger.debug ('DEMAND without mailing_id')
			return self.__answer (False, 'Invalid parameter %r, mailing_id expected' % param)
		logger.debug ('DEMAND %r' % (mailing_id, ))
		(rc, report) = self.demand (mailing_id)
		return self.__answer (rc == 0, report)
	
	def do_fire (self, path: str, param: Optional[str], parsed_param: Optional[Dict[str, List[str]]]) -> XMLRPC.Answer:
		status_id = atoi (param)
		if not status_id:
			logger.debug ('FIRE without status_id')
			return self.__answer (False, 'Invalid parameter %r, status_id expected' % param)
		logger.debug ('FIRE %r' % (status_id, ))
		(rc, report) = self.fire (status_id)
		return self.__answer (rc == 0, report)

	def do_reset (self, path: str, param: Optional[str], parsed_param: Optional[Dict[str, List[str]]]) -> XMLRPC.Answer:
		status_id = atoi (param)
		if not status_id:
			logger.debug ('RESET without status_id')
			return self.__answer (False, 'Invalid parameter %r, status_id expected' % param)
		logger.debug ('RESET %r' % (status_id, ))
		(rc, report) = self.reset (status_id)
		return self.__answer (rc == 0, report)

class Callback (XMLRPC.RObject):
	def __init__ (self, trigger: Trigger) -> None:
		super ().__init__ ()
		self.trigger = trigger
	
	def __response (self, rc: Tuple[int, str]) -> Tuple[bool, str]:
		return (rc[0] == 0, rc[1])

	def start_mailing (self, status_id: int) -> Tuple[bool, str]:
		logger.debug ('start_mailing %r' % (status_id, ))
		return self.__response (self.trigger.fire (status_id))
	startMailing = start_mailing

	def start_on_demand_mailing (self, mailing_id: int) -> Tuple[bool, str]:
		logger.debug ('start_on_demand_mailing %r' % (mailing_id, ))
		return self.__response (self.trigger.demand (mailing_id))
	startOnDemandMailing = start_on_demand_mailing
	
	def __state_check (self, t: Processentry) -> Optional[str]:
		err: Optional[str] = 'Unspecified'
		path = '/proc/%d/status' % t.stats.pid
		try:
			with open (path, 'r') as fd:
				status = fd.read ()
			for line in status.split ('\n'):
				with Ignore (ValueError):
					(var, val) = [_v.strip () for _v in line.split (':', 1)]
					if var == 'State' and val:
						state = val[0]
						if state == 'Z':
							err = 'is zombie'
						elif state == 'T':
							err = 'is stopped'
						else:
							err = None
						break
			else:
				err = 'Failed to find state for process %s in\n%s' % (t.stats, status)
		except IOError as e:
			err = 'Failed to read rocess path %s: %s' % (path, str (e))
		return err
		
	def is_active (self) -> bool:
		logger.debug ('is_active')
		rc = False
		reason = []
		proc = Processtable ()
		be = proc.select (user = user, comm = 'java', rcmd = 'org.agnitas.backend.MailoutServerXMLRPC')
		if len (be) == 1:
			err = self.__state_check (be[0])
			if err is None:
				rc = True
			else:
				reason.append ('Backend: %s' % err)
		elif len (be) > 1:
			reason.append ('There are more than one backend running: %s' % ', '.join ([str (_t.stats.pid) for _t in be]))
		else:
			reason.append ('No java process is active')
		if not rc:
			logger.warning ('Backend activity error: %s' % ', '.join (reason))
		elif reason:
			logger.info ('Backend is active: %s' % ', '.join (reason))
		return rc
	isActive = is_active

	def is_onhold (self, mailing_id: int) -> bool:
		logger.debug ('is_onhold')
		ms = self.__mailing_status (mailing_id, False)
		company = ms.get ('company')
		return (
			ms.get ('deleted', False)
			or
			ms.get ('onhold', False)
			or
			not ms.get ('exists', True)
			or
			(company is not None and company.get ('status', '') != 'active')
		)
	isOnhold = is_onhold

	def mailing_status (self, mailing_id: int, use_company_info: bool = False) -> Dict[str, Any]:
		logger.debug ('mailing_status')
		return self.__mailing_status (mailing_id, use_company_info)

	def __mailing_status (self, mailing_id: int, use_company_info: bool) -> Dict[str, Any]:
		rc: Dict[str, Any] = {
			'licence_id': licence,
			'mailing_id': mailing_id
		}
		with DBAccess () as dba:
			company_id = None
			exists = False
			deleted = False
			rq = dba.querys ('SELECT company_id, deleted FROM mailing_tbl WHERE mailing_id = :mailing_id', {'mailing_id': mailing_id})
			if rq is not None:
				company_id = rq.company_id
				exists = True
				if bool (rq.deleted):
					deleted = True
			else:
				for table in ['maildrop_status_tbl', 'mailing_account_tbl']:
					for row in dba.query (f'SELECT distinct company_id FROM {table} WHERE mailing_id = :mailing_id', {'mailing_id': mailing_id}):
						if row.company_id is not None:
							company_id = row.company_id
							break
					if company_id is not None:
						break
			if company_id is not None:
				rc['company_id'] = company_id
				rq = dba.querys ('SELECT company_id, shortname, rdir_domain, mailloop_domain, status, mailtracking, mailerset FROM company_tbl WHERE company_id = :company_id', {'company_id': company_id})
				if rq is not None:
					rc['company'] = dict (rq._asdict ())
			rc['exists'] = exists
			rc['deleted'] = deleted
			#
			onhold = False
			if company_id is not None:
				for rq in dba.queryc ('SELECT company_id, mailing_id, priority FROM serverprio_tbl WHERE company_id = :company_id OR mailing_id = :mailing_id',{'company_id': company_id, 'mailing_id': mailing_id}):
					if rq.company_id:
						if rq.company_id == company_id and (not rq.mailing_id or rq.mailing_id == mailing_id):
							onhold = True
					elif rq.mailing_id and rq.mailing_id == mailing_id:
						onhold = True
			rc['onhold'] = onhold
			if use_company_info:
				ccfg = CompanyConfig (db = dba)
				ccfg.read ()
				rc['cinfo'] = (Stream (ccfg.scan_company_info (company_id = company_id))
					.map (lambda cv: (cv.name, cv.value))
					.dict ()
				)
		return rc
		
class Main (Runtime):
	__slots__ = ['port']
	def supports (self, option: str) -> bool:
		return option != 'dryrun'

	def add_arguments (self, parser: argparse.ArgumentParser) -> None:
		parser.add_argument ('-P', '--port', action = 'store', type = int, default = syscfg.get_int ('trigger-port', 8080))
	
	def use_arguments (self, args: argparse.Namespace) -> None:
		self.port = args.port
		
	def executor (self) -> bool:
		with self.title (f'trigger3 @ {self.port}'):
			for (option, value) in [
				('xmlrpc.port', str (self.port)),
				('xmlrpc.server', 'forking'),
				('merger.port', '8089')
			]:
				if option not in self.cfg:
					self.cfg[option] = value
			server = XMLRPC (self.cfg)
			trigger = Trigger (self.cfg)
			server.add_handler ('/ping', trigger.do_ping)
			server.add_handler ('/demand', trigger.do_demand)
			server.add_handler ('/fire', trigger.do_fire)
			server.add_handler ('/reset', trigger.do_reset)
			cb = Callback (trigger)
			server.add_instance (cb)
			server.run ()
		return True

if __name__ == '__main__':
	Main.main ()

