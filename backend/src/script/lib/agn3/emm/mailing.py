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
import	socket, time, logging
from	typing import Callable, Optional, Protocol, Union
from	typing import Tuple
from	typing import cast
from	..dbcore import Cursor
from	..definitions import syscfg
from	..exceptions import error
from	..rpc import XMLRPCError, XMLRPCClient
from	..tools import Config
#
__all__ = ['Mailing']
#
logger = logging.getLogger (__name__)
#
class TriggerProxy (Protocol):
	def isActive (self) -> bool: ...
	def startOnDemandMailing (self, mailing_id: int) -> Tuple[bool, Optional[str]]: ...
	def startMailing (self, status_id: int) -> Tuple[bool, Optional[str]]: ...

class Mailing:
	"""Start mailing generation

This class wraps the communication to the merger to start generation
of mailings. These ca be regular and on demand mailings. (To control
on demand mailings, agn3.emm.ODMailing provides a more useful
alternative and is based on this class.) The communication uses the
trigger.py process as the endpoint for the merger. It does not talk
directly to the java process."""
	__slots__ = ['merger', 'port', 'timeout', 'retries', 'rpc']
	def __init__ (self,
		merger: Optional[str] = None,
		port: Optional[int] = None,
		timeout: Union[None, int, float] = None,
		retries: Optional[int] = None
	) -> None:

		"""``merger'' is the address of the merger (or None to
use the default), ``port'' is the port the merger listens to (or None
to use the default 8080). ``timeout'' is the communication (socket)
timeout in seconds and ``retries'' is the number of attempts to try
starting a mailing if a failure occurs."""
		self.merger = merger if merger is not None else syscfg.get_str ('merger-address', '127.0.0.1')
		self.port = port if port is not None else syscfg.get_int ('trigger-port', 8080)
		self.timeout = timeout if timeout is not None else 30
		self.retries = retries if retries is not None else 3
		self.rpc = cast (TriggerProxy, XMLRPCClient (Config (host = self.merger, port = self.port)))

	def active (self) -> bool:
		"""Checks if the merger itself is ready and active"""
		def cbActive () -> bool:
			return self.rpc.isActive ()
		return self.__rpc (cbActive, limit_retries = 1)

	def fire (self, status_id: int, cursor: Optional[Cursor] = None) -> bool:
		"""Start a regular mailing, use keyword arguments for options:

- status_id: the maildrop_status_tbl.status_id to use this record for starting the mailing (required)
- cursor: an open database cursor to retrieve informations from database, if required"""
		return self.__start (False, status_id = status_id, cursor = cursor)

	def demand (self, status_id: int, cursor: Optional[Cursor] = None) -> bool:
		"""Starts an on demand mailing, use keyword arguments for options:

- status_id: the maildrop_status_tbl.status_id to use this record for starting the mailing (required)
- cursor: an open database cursor to retrieve informations from database, if required"""
		return self.__start (True, status_id = status_id, cursor = cursor)

	def __start (self, is_on_demand_mailing: bool, status_id: int, cursor: Optional[Cursor]) -> bool:
		rc = False
		if status_id is None:
			raise error ('missing status_id for starting mailing')
		mailing_id: Optional[int] = None
		if cursor is not None:
			query = (
				'SELECT mailing_id '
				'FROM maildrop_status_tbl '
				'WHERE status_id = :status_id'
			)
			rq = cursor.querys (query, {'status_id': status_id})
			if rq is None:
				raise error ('no entry for status_id %d found' % status_id)
			mailing_id = rq.mailing_id
		if is_on_demand_mailing and mailing_id is None:
			raise error (f'no mailing_id for status_id {status_id} found (required to start on demand mailing)')
		#
		mailing_name = ('#%d' % mailing_id) if mailing_id is not None else ('#(status_id)%d' % status_id)
		if cursor is not None and mailing_id is not None:
			query = (
				'SELECT m.shortname AS mailing_name, m.company_id, c.shortname AS company_name '
				'FROM mailing_tbl m INNER JOIN company_tbl c ON c.company_id = m.company_id '
				'WHERE m.mailing_id = :mailing_id'
			)
			rq = cursor.querys (query, {'mailing_id': mailing_id})
			if rq is not None:
				mailing_name = '%s (ID %d, status_id %d for %s, ID %d)' % (rq.mailing_name, mailing_id, status_id, rq.company_name, rq.company_id)
		#
		genchange = None
		gcQuery = 'SELECT genstatus, genchange FROM maildrop_status_tbl WHERE status_id = :status_id'
		gcData = {'status_id': status_id}
		if cursor is not None:
			rq = cursor.querys (gcQuery, gcData)
			if rq is None:
				logger.warning ('%s: failed to retrieve activty status from maildrop_status_tbl for status_id %d' % (mailing_name, status_id))
			elif rq.genchange is None:
				logger.warning ('%s: no genchange for status_id %d found' % (mailing_name, status_id))
			else:
				genchange = rq.genchange
				if rq.genstatus not in (1, 3):
					logger.error ('%s: unexpected genstatus=%r during startup, aborting' % (mailing_name, rq.genstatus))
					return False
				else:
					logger.debug ('%s: found genstatus=%r and genchange=%s' % (mailing_name, rq.genstatus, genchange))
				time.sleep (1)
		retries = self.retries
		if genchange is None and retries > 1:
			retries = 1
		#
		def cbStart () -> bool:
			if is_on_demand_mailing:
				rc = self.rpc.startOnDemandMailing (cast (int, mailing_id))
			else:
				rc = self.rpc.startMailing (status_id)
			if not rc[0]:
				logger.error ('%s: failed to start mailing: %s' % (mailing_name, rc[1]))
			return rc[0]
		def cbRetry (retries: int) -> Tuple[bool, int]:
			rc = False
			if cursor is not None:
				time.sleep (2)
				if genchange is not None:
					rq = cursor.querys (gcQuery, gcData)
					if rq is None or rq.genchange is None:
						logger.error ('%s: no genchange found, no retry takes place' % mailing_name)
					elif rq.genchange != genchange:
						logger.warning ('%s: genchange changed from %s to %s, assume successful mail triggering' % (mailing_name, genchange, rq.genchange))
						rc = True
					else:
						logger.info ('%s: genchange not changed, retry starting mailing after a second' % mailing_name)
			return (rc, retries)
		rc = self.__rpc (cbStart, retry = cbRetry)
		#
		if rc and cursor is not None and genchange is not None:
			startup = 30
			while startup > 0:
				startup -= 1
				rq = cursor.querys (gcQuery, gcData)
				if rq is None:
					logger.error ('%s: entry for status_id %d vanished' % (mailing_name, status_id))
					break
				elif rq.genstatus == 2:
					logger.debug ('%s: mailing in process' % mailing_name)
					break
				elif rq.genchange != genchange:
					logger.debug ('%s: mailing already finished' % mailing_name)
					break
				elif startup > 0:
					time.sleep (1)
		return rc
		
	def __rpc (self,
		callback: Callable[[], bool],
		retry: Optional[Callable[[int], Tuple[bool, int]]] = None,
		limit_retries: Optional[int] = None
	) -> bool:
		retries = (self.retries if self.retries > 0 else 1) if limit_retries is None else limit_retries
		#
		if self.timeout > 0:
			otimeout = socket.getdefaulttimeout ()
		rc = False
		while not rc and retries > 0:
			retries -= 1
			try:
				if self.timeout > 0:
					socket.setdefaulttimeout (self.timeout)
				rc = callback ()
				(logger.info if rc else logger.warning) ('Call to merger %s results in %s' % (self.merger, str (rc).lower ()))
			except XMLRPCError as e:
				logger.error ('Failed to communicate with merger %s: %s' % (self.merger, str (e)))
			except socket.error as e:
				logger.error ('Failed to call merger %s: %s' % (self.merger, str (e)))
			finally:
				if self.timeout > 0:
					socket.setdefaulttimeout (otimeout)
			if not rc and retry is not None and retries > 0:
				(rc, retries) = retry (retries)
		return rc
