#!/usr/bin/env python2
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
#	-*- python -*-
#
import	sys, getopt, os, time, datetime, collections
import	agn, eagn
agn.loglevel = agn.LV_INFO
#
class Pickdist (eagn.Watchdog.Job):
	queue_limit = 5000
	def __init__ (self):
		super (Pickdist, self).__init__ (
			name = 'pickdist',
			method = self.run,
			args = ()
		)
		self.spool = agn.mkpath (agn.base, 'var', 'spool')
		self.meta = agn.mkpath (self.spool, 'META')
		self.archive = agn.mkpath (self.spool, 'ARCHIVE')
		self.recover = agn.mkpath (self.spool, 'RECOVER')
		self.deleted = agn.mkpath (self.spool, 'DELETED')
		self.queue = agn.mkpath (self.spool, 'QUEUE')
		self.mta = agn.MTA ()
		self.day = None
		self.existing = set ()
	
	def run (self):
		while self.watchdog.running:
			agn.mark (agn.LV_INFO, 'loop', 180)
			self.step ()
			delay = 30
			while self.watchdog.running and delay > 0:
				delay -= 1
				time.sleep (1)
	
	def step (self):
		now = datetime.datetime.now ()
		self.day = '%04d%02d%02d' % (now.year, now.month, now.day)
		count = 0
		for info in self.get_ready_to_run ():
			if info.single or not self.queue_is_full ():
				self.process (info)
				count += 1
		if count > 0:
			self.scan_ready_to_run ()
		
	def scan_ready_to_run (self):	
		finals = collections.defaultdict (list)
		stamps = {}
		availables = set ()
		basenames = set ()
		data = []
		for filename in (agn.Stream (os.listdir (self.meta))
			.filter (lambda f: f.startswith ('AgnMail') and (f.endswith ('.xml.gz') or f.endswith ('.stamp') or f.endswith ('.final')))
		):
			info = agn.METAFile (agn.mkpath (self.meta, filename))
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
				if info.isReady ():
					data.append (info)
		for info in agn.Stream (finals.values ()).chain ().filter (lambda i: i.mailid not in availables):
			agn.log (agn.LV_INFO, 'final', 'No more data file for final %s found, archive it' % info.filename)
			self.move (info.path, self.archive)
		for info in agn.Stream (stamps.values ()).filter (lambda i: i.basename not in basenames):
			agn.log (agn.LV_INFO, 'stamp', 'Move dangeling stamp file %s to archive' % info.filename)
			self.move (info.path, self.archive)
		isfull = self.queue_is_full ()
		ready = (agn.Stream (data)
			.filter (lambda i: i.basename in stamps and i.mailid in finals and (not isfull or i.single))
			.sorted (key = lambda i: (not i.single, i.timestamp, i.blocknr))
			.list ()
		)
		return (ready, stamps, finals)
		
	def get_ready_to_run (self):
		(ready, stamps, finals) = self.scan_ready_to_run ()
		if ready:
			for info in ready:
				info.stamp = stamps[info.basename]
			with eagn.DB () as db:
				invalids = set ()
				for mailing in (agn.Stream (ready)
					.map (lambda i: i.mailing)
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
						agn.log (agn.LV_INFO, 'ready', 'Mailing %d no more existing' % mailing)
						invalids.add (mailing)
					elif rq.deleted:
						agn.log (agn.LV_INFO, 'ready', 'Mailing %d is marked as deleted' % mailing)
						invalids.add (mailing)
				if invalids:
					for info in (agn.Stream (ready)
						.filter (lambda i: i.mailing in invalids)
					):
						self.move (info.path, self.deleted)
						self.move (info.stamp.path, self.deleted)
					ready = (agn.Stream (ready)
						.filter (lambda i: i.mailing not in invalids)
						.list ()
					)
		if ready:				
			agn.log (agn.LV_INFO, 'ready', '%s files are ready to send' % agn.numfmt (len (ready)))
		return ready

	def process (self, info):
		success = False
		try:
			success = self.mta (info.path, targetDirectory = self.queue)
		finally:
			agn.log (agn.LV_INFO if success else agn.LV_ERROR, 'process', '%s processed %s' % ('Successfully' if success else 'Failed to', info.path))
			target = self.archive if success else self.recover
			self.move (info.path, target)
			self.move (info.stamp.path, target)
			if not success:
				sync = os.path.join (info.directory, '%s.SYNC' % info.basename)
				if os.path.isfile (sync):
					self.move (sync, target)

	def queue_is_full (self):
		if self.mta.mta == 'sendmail':
			count = sum (1 for _f in os.listdir (self.queue) if _f.startswith ('qf'))
			return count >= self.queue_limit
		return False
	
	def move (self, path, destination):
		n = 0
		filename = os.path.basename (path)
		destination = agn.mkpath (destination, self.day)
		if destination not in self.existing:
			agn.createPath (destination)
			self.existing.add (destination)
		target = agn.mkpath (destination, filename)
		while os.path.isfile (target):
			n += 1
			target = agn.mkpath (destination, '%s~%d~' % (filename, n))
		try:
			os.rename (path, target)
			agn.log (agn.LV_INFO, 'move', 'Moved %s to %s' % (path, target))
		except OSError as e:
			agn.log (agn.LV_ERROR, 'move', 'Failed to move %s to %s: %s' % (path, target, e))
			if destination != self.recover:
				self.move (path, self.recover)
			else:
				agn.log (agn.LV_FATAL, 'move', 'Giving up to move %s to %s' % (path, target))
				raise

def main ():
	single = False
	(opts, param) = getopt.getopt (sys.argv[1:], 'v')
	for opt in opts:
		if opt[0] == '-v':
			agn.outlevel = agn.LV_DEBUG
			agn.outstream = sys.stderr
			single = True
	pd = Pickdist ()
	if single:
		pd.step ()
	else:
		agn.lock ()
		agn.log (agn.LV_INFO, 'main', 'Starting up')
		wd = eagn.Watchdog ()
		wd.mstart ([pd])
		agn.log (agn.LV_INFO, 'main', 'Going down')
		agn.unlock ()

if __name__ == '__main__':
	main ()
