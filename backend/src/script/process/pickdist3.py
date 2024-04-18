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
import	logging, os, time
from	collections import defaultdict
from	datetime import datetime
from	typing import Optional
from	typing import DefaultDict, Dict, List, Set, Tuple
from	agn3.db import DBIgnore, DB
from	agn3.definitions import base
import	agn3.emm.metafile
from	agn3.io import create_path
from	agn3.log import mark
from	agn3.mta import MTA
from	agn3.runtime import Runtime
from	agn3.stream import Stream
#
logger = logging.getLogger (__name__)
#
class METAFile (agn3.emm.metafile.METAFile):
	__slots__ = ['stamp']
	def __init__ (self, path: str) -> None:
		super ().__init__ (path)
		self.stamp: Optional[METAFile] = None

class Pickdist (Runtime):
	__slots__ = [
		'queue_limit', 'meta', 'archive', 'recover', 'deleted', 'queue',
		'mta', 'day', 'existing'
	]
	def supports (self, option: str) -> bool:
		return option != 'dryrun'
	
	def executor (self) -> bool:
		self.queue_limit = self.cfg.iget ('queue-limit', 5000)
		spool = os.path.join (base, 'var', 'spool')
		self.meta = os.path.join (spool, 'META')
		self.archive = os.path.join (spool, 'ARCHIVE')
		self.recover = os.path.join (spool, 'RECOVER')
		self.deleted = os.path.join (spool, 'DELETED')
		self.queue = os.path.join (spool, 'QUEUE')
		self.mta = MTA ()
		self.day = ''
		self.existing: Set[str] = set ()
		delay = self.cfg.eget ('delay', 30)
		while self.running:
			mark (180)
			self.step ()
			n = delay
			while self.running and n > 0:
				n -= 1
				time.sleep (1)
		return True
	
	def step (self) -> None:
		now = datetime.now ()
		self.day = '%04d%02d%02d' % (now.year, now.month, now.day)
		count = 0
		for info in self.get_ready_to_run ():
			self.process (info)
			count += 1
		if count > 0:
			self.scan_ready_to_run ()
		
	def scan_ready_to_run (self) -> Tuple[List[METAFile], Dict[str, METAFile], DefaultDict[str, List[METAFile]]]:
		finals: DefaultDict[str, List[METAFile]] = defaultdict (list)
		stamps: Dict[str, METAFile] = {}
		availables: Set[str] = set ()
		basenames: Set[str] = set ()
		data: List[METAFile] = []
		for filename in (Stream (os.listdir (self.meta))
			.filter (lambda f: bool (f.startswith ('AgnMail') and (f.endswith ('.xml.gz') or f.endswith ('.stamp') or f.endswith ('.final'))))
		):
			info = METAFile (os.path.join (self.meta, filename))
			if not info.valid:
				continue
			#
			if info.extension == 'final':
				finals[info.mailid].append (info)
			elif info.extension == 'stamp':
				stamps[info.basename] = info
			elif info.extension.startswith ('xml'):
				availables.add (info.mailid)
				basenames.add (info.basename)
				if info.is_ready ():
					data.append (info)
		for info in Stream (finals.values ()).chain (METAFile).filter (lambda i: i.mailid not in availables):
			logger.info ('No more data file for final %s found, archive it' % info.filename)
			self.move (info.path, self.archive)
		for info in Stream (stamps.values ()).filter (lambda i: i.basename not in basenames):
			logger.info ('Move dangeling stamp file %s to archive' % info.filename)
			self.move (info.path, self.archive)
		ready = (Stream (data)
			.filter (lambda i: i.basename in stamps and i.mailid in finals)
			.sorted (key = lambda i: (not i.single, i.timestamp, i.blocknr))
			.list ()
		)
		return (ready, stamps, finals)
		
	def get_ready_to_run (self) -> List[METAFile]:
		(ready, stamps, finals) = self.scan_ready_to_run ()
		if ready:
			for info in ready:
				info.stamp = stamps[info.basename]
			with DBIgnore (), DB () as db:
				invalids: Set[int] = set ()
				for mailing in (Stream (ready)
					.map (lambda i: i.mailing_id)
					.distinct ()
					.sorted ()
				):
					rq = db.querys (
						'SELECT deleted '
						'FROM mailing_tbl '
						'WHERE mailing_id = :mailingID',
						{
							'mailingID': mailing
						}
					)
					if rq is None:
						logger.info ('Mailing %d no more existing' % mailing)
						invalids.add (mailing)
					elif rq.deleted:
						logger.info ('Mailing %d is marked as deleted' % mailing)
						invalids.add (mailing)
				if invalids:
					for info in (Stream (ready)
						.filter (lambda i: i.mailing_id in invalids)
					):
						self.move (info.path, self.deleted)
						if info.stamp is not None:
							self.move (info.stamp.path, self.deleted)
					ready = (Stream (ready)
						.filter (lambda i: i.mailing_id not in invalids)
						.list ()
					)
		if ready:
			logger.info ('{count:,d} files are ready to send'.format (count = len (ready)))
		return ready

	def process (self, info: METAFile) -> None:
		success = False
		try:
			success = self.mta (info.path, target_directory = self.queue)
		finally:
			(logger.info if success else logger.error) ('%s processed %s' % ('Successfully' if success else 'Failed to', info.path))
			target = self.archive if success else self.recover
			self.move (info.path, target)
			if info.stamp is not None:
				self.move (info.stamp.path, target)
			if not success:
				sync = os.path.join (info.directory, '%s.SYNC' % info.basename)
				if os.path.isfile (sync):
					self.move (sync, target)

	def move (self, path: str, destination: str) -> None:
		n = 0
		filename = os.path.basename (path)
		destination = os.path.join (destination, self.day)
		if destination not in self.existing:
			create_path (destination)
			self.existing.add (destination)
		target = os.path.join (destination, filename)
		while os.path.isfile (target):
			n += 1
			target = os.path.join (destination, '%s~%d~' % (filename, n))
		try:
			os.rename (path, target)
			logger.info ('Moved %s to %s' % (path, target))
		except OSError as e:
			logger.error ('Failed to move %s to %s: %s' % (path, target, e))
			if destination != self.recover:
				self.move (path, self.recover)
			else:
				logger.critical ('Giving up to move %s to %s' % (path, target))
				raise

if __name__ == '__main__':
	Pickdist.main ()
