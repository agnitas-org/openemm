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
#	-*- mode: python; mode: fold -*-
#
import	sys, getopt
import	time, datetime, collections
import	agn
agn.loglevel = agn.LV_INFO

try:
	import	os, gdbm

	class Cache:
		incarnation = 0
		def __init__ (self, inmem):
			self.incarnation += 1
			fname = '/var/tmp/sbcache.%d.%d' % (os.getpid (), self.incarnation)
			self.gdb = gdbm.open (fname, 'c')
			if self.gdb is not None:
				os.unlink (fname)
			self.inmem = inmem
			self.mem = {}
			self.timeline = collections.deque ()
			self.count = 0
		
		def done (self):
			if self.gdb:
				self.gdb.close ()
				self.gdb = None
		
		def valid (self):
			return self.gdb is not None
		
		def add (self, key):
			if key in self.mem:
				self.mem[key] += 1
			else:
				if self.count >= self.inmem:
					store = self.timeline.popleft ()
					self.gdb[str (store)] = str (self.mem[store])
					del self.mem[store]
				else:
					self.count += 1
				skey = str (key)
				if self.gdb.has_key (skey):
					self.mem[key] = int (self.gdb[skey]) + 1
				else:
					self.mem[key] = 1
				self.timeline.append (key)
		
		def get (self, key, dflt):
			if key in self.mem:
				rc = self.mem[key]
			else:
				try:
					rc = int (self.gdb[str (key)])
				except KeyError:
					rc = dflt
			return rc
except ImportError:
	Cache = None

exts = lambda a: a != 1 and 's' or ''
exty = lambda a: a != 1 and 'ies' or 'y'

class Softbounce (object):
	timestampName = 'bounce-conversion'
	def __init__ (self, db, curs): #{{{
		self.db = db
		self.curs = curs
		self.timestamp = None
		self.ok = True
		self.config = {}
		self.tables = set ()
		query = self.curs.qselect (
			oracle = 'SELECT table_name FROM user_tables',
			mysql = 'SHOW TABLES'
		)
		for record in self.curs.query (query):
			if record[0]:
				self.tables.add (record[0].lower ())
	#}}}
	def __cfg (self, companyID, var, dflt): #{{{
		try:
			val = int (self.config[companyID][var])
		except (KeyError, ValueError):
			if companyID != 0:
				val = self.__cfg (0, var, dflt)
			else:
				val = dflt
		return val
	#}}}
	def done (self): #{{{
		agn.log (agn.LV_INFO, 'done', 'Cleanup starting')
		self.finalizeTimestamp ()
		agn.log (agn.LV_INFO, 'done', 'Cleanup done')
	#}}}
	def setup (self): #{{{
		rc = True
		agn.log (agn.LV_INFO, 'setup', 'Setup starting')
		agn.log (agn.LV_INFO, 'setup', 'Reading parameter from company_info_tbl')
		for row in self.curs.query ('SELECT company_id, cvalue FROM company_info_tbl WHERE cname = :cname', {'cname': 'bounce-conversion-parameter'}):
			try:
				self.config[row[0]] = agn.Parameter (row[1])
			except Exception:
				agn.log (agn.LV_ERROR, 'setup', 'Failed to parse parameter %r for companyID %d' % (row[1], row[0]))
				rc = False
		agn.log (agn.LV_INFO, 'setup', '%d parameter read' % len (self.config))
		agn.log (agn.LV_INFO, 'setup', 'Setup done')
		return rc
	#}}}
	def __do (self, query, data, what, cursor = None): #{{{
		try:
			if cursor is None:
				cursor = self.curs
			agn.log (agn.LV_INFO, 'do', what)
			rows = cursor.update (query, data, commit = True)
			agn.log (agn.LV_INFO, 'do', '%s: affected %d row%s' % (what, rows, exts (rows)))
		except agn.error as e:
			agn.log (agn.LV_ERROR, 'do', '%s: failed using query %s %r: %s (%s)' % (what, query, data, e, self.db.lastError ()))
	#}}}
	def __ccoll (self, query, data, keys, dflts, cursor = None): #{{{
		rc = {}
		if keys is None:
			rkeys = 'no keys'
		else:
			rkeys = 'key%s %s' % (exts (len (keys)), ', '.join (list (keys)))
		try:
			if cursor is None:
				cursor = self.curs
			agn.log (agn.LV_INFO, 'collect', 'Collect compamy info for %s' % rkeys)
			count = 0
			for row in cursor.query (query, data):
				if keys:
					values = tuple ([self.__cfg (row[0], _k, _d) for (_k, _d) in zip (keys, dflts)])
					try:
						rc[values].append (row[0])
					except KeyError:
						rc[values] = [row[0]]
				else:
					rc[row[0]] = True
				count += 1
			agn.log (agn.LV_INFO, 'collect', 'Collected %d compan%s for %s distributed over %d entr%s' % (count, exty (count), rkeys, len (rc), exty (len (rc))))
		except agn.error as e:
			agn.log (agn.LV_ERROR, 'collect', 'Failed to collect companies for %s using %s: %s (%s)' % (rkeys, query, e, self.db.lastError ()))
		return rc
	#}}}
	def __cdo (self, collection, query, data, filler, what, cursor = None): #{{{
		for (key, companies) in collection.items ():
			if data:
				ndata = data.copy ()
			else:
				ndata = {}
			if callable (query):
				basequery = query (key)
			else:
				basequery = key
			filler (ndata, key)
			if basequery is not None:
				companies = sorted (companies)
				while companies:
					chunk = companies[:20]
					companies = companies[20:]
					if len (chunk) > 1:
						nquery = '%s IN (%s)' % (basequery, ', '.join ([str (_c) for _c in chunk]))
					else:
						nquery = '%s = %d' % (basequery, chunk[0])
					self.__do (nquery, ndata, '%s for %s' % (what, ', '.join ([str (_c) for _c in chunk])), cursor)
			else:
				agn.log (agn.LV_INFO, 'cdo', 'Skip execution for key %r' % (key, ))
	#}}}
	def removeOldEntries (self): #{{{
		agn.log (agn.LV_INFO, 'expire', 'Remove old entries from softbounce_email_tbl')
		coll = self.__ccoll ('SELECT distinct company_id FROM softbounce_email_tbl', None, ('max-age-create', 'max-age-change'), (180, 60))
		def queryData (key):
			q = ['DELETE FROM softbounce_email_tbl WHERE']
			if key[0] > 0:
				if key[1] > 0:
					q.append ('(creation_date <= :expireCreate OR timestamp <= :expireChange)')
				else:
					q.append ('creation_date <= :expireCreate')
			else:
				if key[1] > 0:
					q.append ('timestamp <= :expireChange')
				else:
					q = None
			if q:
				q.append ('AND company_id')
				return ' '.join (q)
			return None
		def fillData (data, key):
			if key[0] > 0:
				ts = time.localtime (time.time () - key[0] * 24 * 60 * 60)
				data['expireCreate'] = datetime.datetime (ts.tm_year, ts.tm_mon, ts.tm_mday)
			if key[1] > 0:
				ts = time.localtime (time.time () - key[1] * 24 * 60 * 60)
				data['expireChange'] = datetime.datetime (ts.tm_year, ts.tm_mon, ts.tm_mday)
		self.__cdo (coll, queryData, None, fillData, 'Remove old addresses from softbounce_email_tbl')
		agn.log (agn.LV_INFO, 'expire', 'Removing of old entries from softbounce_email_tbl done')
	#}}}
	def setupTimestamp (self): #{{{
		agn.log (agn.LV_INFO, 'timestamp', 'Setup timestamp')
		self.timestamp = agn.Timestamp (self.timestampName)
		self.timestamp.setup (self.db)
		time.sleep (1)
		agn.log (agn.LV_INFO, 'timestamp', 'Setup done')
	#}}}
	def collectNewBounces (self): #{{{
		agn.log (agn.LV_INFO, 'collect', 'Start collecting new bounces')
		cursor = self.db.cursor ()
		if cursor is None:
			raise agn.error ('collectNewBounces: Failed to get new cursor for collecting')
		#
		Update = collections.namedtuple ('Update', ['customer_id', 'company_id', 'mailing_id', 'detail'])
		data = {}
		query =  'SELECT customer_id, company_id, mailing_id, detail FROM bounce_tbl WHERE %s ORDER BY company_id, customer_id' % self.timestamp.makeBetweenClause ('timestamp', data)
		class Collect (agn.Stream.Collector):
			def supplier (self):
				self.data = {}
				self.uniques = 0

			def accumulator (self, supplier, element):
				update = Update (*element)
				if update.detail >= 400 and update.detail < 520:
					key = (update.company_id, update.customer_id)
					try:
						if update.detail > self.data[key].detail:
							self.data[key] = update
					except KeyError:
						self.data[key] = update
						self.uniques += 1

			def finisher (self, supplier, count):
				return (count, self.uniques, self.data)
		(records, uniques, updates) = cursor.stream (query, data).collect (Collect ())
		agn.log (agn.LV_INFO, 'collect', 'Read %s records (%s uniques) and have %s for insert' % (agn.numfmt (records), agn.numfmt (uniques), agn.numfmt (len (updates))))
		#
		inserts = 0		
		query = self.curs.rselect ('INSERT INTO bounce_collect_tbl (customer_id, company_id, mailing_id, timestamp) VALUES (:customer, :company, :mailing, %(sysdate)s)')
		for update in (
			agn.Stream (updates.itervalues ())
			.sorted ()
		):
			cursor.update (query, {
				'customer': update.customer_id,
				'company': update.company_id,
				'mailing': update.mailing_id
			})
			inserts += 1
			if inserts % 10000 == 0:
				cursor.sync ()
		agn.log (agn.LV_INFO, 'collect', 'Inserted %s records into bounce_collect_tbl' % agn.numfmt (inserts))
		cursor.sync ()
		cursor.close ()
		#
		agn.log (agn.LV_INFO, 'collect', 'Read %d records (%d uniques) and inserted %d' % (records, uniques, inserts))
		companyIDs = []
		query = 'SELECT distinct company_id FROM bounce_collect_tbl'
		for record in self.curs.query (query):
			if record[0] is not None and record[0] > 0:
				companyIDs.append (record[0])
		agn.log (agn.LV_INFO, 'collect', 'Remove active receivers from being watched for %d compan%s' % (len (companyIDs), exty (len (companyIDs))))
		allCount = 0
		for companyID in companyIDs:
			table = 'success_%d_tbl' % companyID
			if table in self.tables:
				data = {'companyID': companyID}
				query = self.curs.qselect (
					oracle = 'DELETE FROM bounce_collect_tbl mail WHERE EXISTS (SELECT 1 FROM %s su WHERE ' % table + \
						 'mail.customer_id = su.customer_id AND mail.company_id = :companyID AND %s)' % self.timestamp.makeBetweenClause ('timestamp', data),
					mysql = 'DELETE mail.* FROM bounce_collect_tbl mail, %s su WHERE ' % table + \
						'mail.customer_id = su.customer_id AND mail.company_id = :companyID AND %s' % self.timestamp.makeBetweenClause ('su.timestamp', data)
				)
				count = self.curs.execute (query, data, commit = True)
				agn.log (agn.LV_INFO, 'collect', 'Removed %s active receiver%s for companyID %d' % (agn.numfmt (count), exts (count), companyID))
				allCount += count
			else:
				agn.log (agn.LV_INFO, 'collect', 'Skip removing active receivers for companyID %d due to missing table %s' % (companyID, table))
		agn.log (agn.LV_INFO, 'collect', 'Finished removing %s receiver%s' % (agn.numfmt (allCount), exts (allCount)))
	#}}}
	def finalizeTimestamp (self): #{{{
		agn.log (agn.LV_INFO, 'timestamp', 'Finalizing timestamp')
		if self.timestamp:
			self.timestamp.done (self.ok)
			self.timestamp = None
		agn.log (agn.LV_INFO, 'timestamp', 'Timestamp finalized')
	#}}}
	def mergeNewBounces (self): #{{{
		agn.log (agn.LV_INFO, 'merge', 'Start merging new bounces into softbounce_email_tbl')
		iquery = self.curs.rselect ('INSERT INTO softbounce_email_tbl (company_id, email, bnccnt, mailing_id, creation_date, timestamp) VALUES (:company, :email, 1, :mailing, %(sysdate)s, %(sysdate)s)')
		uquery = self.curs.rselect ('UPDATE softbounce_email_tbl SET mailing_id = :mailing, timestamp = %(sysdate)s, bnccnt=bnccnt+1 WHERE company_id = :company AND email = :email')
		icurs = self.db.cursor ()
		ucurs = self.db.cursor ()
		squery = 'SELECT count(*) FROM softbounce_email_tbl WHERE company_id = :company AND email = :email'
		scurs = self.db.cursor ()
		dquery = 'DELETE FROM bounce_collect_tbl WHERE company_id = :company'
		dcurs = self.db.cursor ()
		if None in [ icurs, ucurs, scurs, dcurs ]:
			raise agn.error ('mergeNewBounces: Unable to setup curses for merging')
		coll = self.__ccoll ('SELECT distinct company_id FROM bounce_collect_tbl WHERE company_id IN (SELECT company_id FROM company_tbl WHERE status = \'active\')', None, None, None)
		for company in sorted (coll):
			agn.log (agn.LV_INFO, 'merge', 'Working on %d' % company)
			query =  'SELECT mt.customer_id, mt.mailing_id, cust.email '
			query += 'FROM bounce_collect_tbl mt, customer_%d_tbl cust ' % company
			query += 'WHERE cust.customer_id = mt.customer_id '
			query += 'AND mt.company_id = %d ' % company
			query += 'ORDER BY cust.email, mt.mailing_id'

			for record in self.curs.query (query):
				(cuid, mid, eml) = record
				parm = {
					'company': company,
					'customer': cuid,
					'mailing': mid,
					'email': eml
				}
				data = scurs.querys (squery, parm, cleanup = True)
				if not data is None:
					if data[0] == 0:
						icurs.update (iquery, parm, cleanup = True)
					else:
						ucurs.update (uquery, parm, cleanup = True)
			parm = {
				'company': company
			}
			dcurs.update (dquery, parm, cleanup = True)
			self.db.commit ()
		icurs.close ()
		ucurs.close ()
		scurs.close ()
		dcurs.close ()
		agn.log (agn.LV_INFO, 'merge', 'Merging of new bounces done')
		agn.log (agn.LV_INFO, 'merge', 'Fade out addresses')
		coll = self.__ccoll ('SELECT distinct company_id FROM softbounce_email_tbl', None, ('fade-out', ), (14, ))
		def queryData (key):
			if key[0] > 0:
				return 'UPDATE softbounce_email_tbl SET bnccnt = bnccnt - 1 WHERE timestamp <= :fade AND bnccnt > 0 AND company_id'
			return None
		def fillData (data, key):
			ts = time.localtime (time.time () - key[0] * 24 * 60 * 60)
			data['fade'] = datetime.datetime (ts.tm_year, ts.tm_mon, ts.tm_mday)
		self.__cdo (coll, queryData, None, fillData, 'Fade out non bounced watched')
		query = 'DELETE FROM softbounce_email_tbl WHERE bnccnt = 0'
		self.__do (query, None, 'Remove faded out addresses from softbounce_email_tbl')
		agn.log (agn.LV_INFO, 'merge', 'Fade out completed')
	#}}}
	def convertToHardbounce (self): #{{{
		agn.log (agn.LV_INFO, 'conv', 'Start converting softbounces to hardbounce')
		coll = self.__ccoll ('SELECT distinct company_id FROM softbounce_email_tbl WHERE company_id IN (SELECT company_id FROM company_tbl WHERE status = \'active\')', None, None, None)
		stats = []
		for company in sorted (coll):
			cstat = [company, 0, 0]
			stats.append (cstat)
			agn.log (agn.LV_INFO, 'conv', 'Working on %d' % company)
			dquery = 'DELETE FROM softbounce_email_tbl WHERE company_id = %d AND email = :email' % company
			dcurs = self.db.cursor ()
			uquery = self.curs.rselect ('UPDATE customer_%d_binding_tbl SET user_status = 2, user_remark = \'bounce:soft\', exit_mailing_id = :mailing, timestamp = %%(sysdate)s WHERE customer_id = :customer AND user_status = 1' % company)
			bquery = self.curs.rselect ('INSERT INTO bounce_tbl (company_id, customer_id, detail, mailing_id, timestamp, dsn) VALUES (%d, :customer, 510, :mailing, %%(sysdate)s, 599)' % company)
			ucurs = self.db.cursor ()
			squery =  'SELECT email, mailing_id, bnccnt, creation_date, timestamp FROM softbounce_email_tbl '
			bnccnt = self.__cfg (company, 'convert-bounce-count', 40)
			daydiff = self.__cfg (company, 'convert-bounce-duration', 30)
			squery += self.curs.qselect (
				oracle = 'WHERE company_id = %d AND bnccnt > %d AND timestamp-creation_date > %d' % (company, bnccnt, daydiff),
				mysql = 'WHERE company_id = %d AND bnccnt > %d AND DATEDIFF(timestamp,creation_date) > %d' % (company, bnccnt, daydiff)
			)
			scurs = self.db.cursor ()
			if None in [dcurs, ucurs, scurs]:
				raise agn.error ('Failed to setup curses')
			lastClick = self.__cfg (company, 'last-click', 30)
			lastOpen = self.__cfg (company, 'last-open', 30)
			def toDatetime (offset):
				tm = time.localtime (time.time () -offset * 24 * 60 * 60)
				return datetime.datetime (tm.tm_year, tm.tm_mon, tm.tm_mday)
			lastClickTS = toDatetime (lastClick)
			lastOpenTS = toDatetime (lastOpen)
			if Cache is not None:
				rcache = Cache (1000)
				ocache = Cache (1000 * 1000)
				ccurs = self.db.cursor ()
				if None in [rcache, ocache, ccurs] or not rcache.valid () or not ocache.valid ():
					raise agn.error ('Failed to setup caching')
				agn.log (agn.LV_INFO, 'cache', 'Setup rdir log cache for %d' % company)
				query = 'SELECT customer_id FROM rdirlog_%d_tbl WHERE timestamp > :ts' % company
				parm = {'ts': lastClickTS}
				for record in ccurs.query (query, parm):
					rcache.add (record[0])
				agn.log (agn.LV_INFO, 'cache', 'Setup one pixel log cache for %d' % company)
				query = 'SELECT customer_id FROM onepixellog_%d_tbl WHERE timestamp > :ts' % company
				parm = {'ts': lastOpenTS}
				for record in ccurs.query (query, parm):
					ocache.add (record[0])
				ccurs.close ()
				agn.log (agn.LV_INFO, 'cache', 'Setup completed')
			ccount = 0
			for record in scurs.query (squery):
				parm = {
					'email': record[0],
					'mailing': record[1],
					'bouncecount': record[2],
					'creationdate': record[3],
					'timestamp': record[4],
					'customer': None
				}
				query =  'SELECT customer_id FROM customer_%d_tbl WHERE email = :email ' % company
				data = self.curs.querys (query, parm, cleanup = True)
				if data is None:
					continue
				custs = [agn.mutable (id = _d, click = 0, open = 0) for _d in data if _d]
				if not custs:
					continue
				if len (custs) == 1:
					cclause = 'customer_id = %d' % custs[0].id
				else:
					cclause = 'customer_id IN (%s)' % ', '.join ([str (_c.id) for _c in custs])
				if Cache is not None:
					for c in custs:
						c.click += rcache.get (c.id, 0)
						c.open += ocache.get (c.id, 0)
				else:
					parm['ts'] = lastClickTS
					query =  'SELECT customer_id, count(*) FROM rdirlog_%d_tbl WHERE %s AND timestamp > :ts GROUP BY customer_id' % (company, cclause)
					for r in self.curs.queryc (query, parm, cleanup = True):
						for c in custs:
							if c.id == r[0]:
								c.click += r[1]
					parm['ts'] = lastOpenTS
					query =  'SELECT customer_id, count(*) FROM onepixellog_%d_tbl WHERE %s AND timestamp > :ts GROUP BY customer_id' % (company, cclause)
					for r in self.curs.queryc (query, parm, cleanup = True):
						for c in custs:
							if c.id == r[0]:
								c.open += r[1]
				for c in custs:
					if c.click > 0 or c.open > 0:
						cstat[1] += 1
						agn.log (agn.LV_INFO, 'conv', 'Email %s [%d] has %d klick(s) and %d onepix(es) -> active' % (parm['email'], c.id, c.click, c.open))
					else:
						cstat[2] += 1
						agn.log (agn.LV_INFO, 'conv', 'Email %s [%d] has no klicks and no onepixes -> hardbounce' % (parm['email'], c.id))
						parm['customer'] = c.id
						ucurs.update (uquery, parm, cleanup = True)
						ucurs.execute (bquery, parm, cleanup = True)
				dcurs.update (dquery, parm, cleanup = True)
				ccount += 1
				if ccount % 1000 == 0:
					agn.log (agn.LV_INFO, 'conv', 'Commiting at %s' % agn.numfmt (ccount))
					self.db.commit ()
			if Cache is not None:
				rcache.done ()
				ocache.done ()
			self.db.commit ()
			scurs.close ()
			ucurs.close ()
			dcurs.close ()
		for cstat in stats:
			agn.log (agn.LV_INFO, 'conv', 'Company %d has %d active and %d marked as hardbounced users' % tuple (cstat))
		agn.log (agn.LV_INFO, 'conv', 'Converting softbounces to hardbounce done')
	#}}}
#
def main ():
	rc = 1
	(opts, param) = getopt.getopt (sys.argv[1:], 'v')
	for opt in opts:
		if opt[0] == '-v':
			agn.outlevel = agn.LV_DEBUG
			agn.outstream = sys.stderr
	agn.lock ()
	agn.log (agn.LV_INFO, 'main', 'Starting up')
	db = agn.DBaseID ()
	if db is not None:
#		db.log = lambda a: agn.log (agn.LV_DEBUG, 'db', a)
		curs = db.cursor ()
		if curs is not None:
			softbounce = Softbounce (db, curs)
			if softbounce.setup ():
				try:
					softbounce.removeOldEntries ()
					softbounce.setupTimestamp ()
					softbounce.collectNewBounces ()
					softbounce.finalizeTimestamp ()
					softbounce.mergeNewBounces ()
					softbounce.convertToHardbounce ()
					rc = 0
				except agn.error as e:
					agn.logexc (agn.LV_ERROR, 'main', 'Failed due to %s' % e)
					softbounce.ok = False
				softbounce.done ()
			else:
				agn.log (agn.LV_ERROR, 'main', 'Setup of handling failed')
			curs.sync ()
			curs.close ()
		else:
			agn.log (agn.LV_ERROR, 'main', 'Failed to get database cursor')
		db.close ()
	else:
		agn.log (agn.LV_ERROR, 'main', 'Failed to setup database interface')
	agn.log (agn.LV_INFO, 'main', 'Going down')
	agn.unlock ()
	if rc:
		sys.exit (rc)

if __name__ == '__main__':
	main ()
