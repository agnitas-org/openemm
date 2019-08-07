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
import	sys, time, datetime
import	agn
#
def isActive (genStatus, genChange, statusField):
	now = int (time.time ())
	change = time.mktime ((genChange.year, genChange.month, genChange.day, genChange.hour, genChange.minute, genChange.second, 0, 0, -1))
	if genStatus == 2:
		if statusField == 'W':
			offset = 2 * 60 * 60
		else:
			offset = 15 * 60
	else:
		offset = 10 * 60
	return change + offset > now
def main ():
	db = agn.DBaseID ()
	if db is None:
		agn.die (s = 'Failed to setup database')
	c = db.cursor ()
	if c is None:
		agn.die (s = 'Failed to connect to database')
	active = 0
	companies = {}
	query = ('SELECT status_id, mailing_id, genstatus, genchange, status_field '
		 'FROM maildrop_status_tbl '
		 'WHERE genchange > :limit AND (genstatus = 2 OR (genstatus = 1 AND status_field IN (\'A\', \'T\', \'W\')))')
	now = datetime.datetime.now ()
	limit = now.fromordinal (now.toordinal () - 1)
	for (statusID, mailingID, genstatus, genchange, statusField) in c.queryc (query, {'limit': limit}):
		if isActive (genstatus, genchange, statusField):
			active += 1
			rc = c.querys ('SELECT company_id, shortname FROM mailing_tbl WHERE mailing_id = :mid', {'mid': mailingID})
			if not rc is None and not None in rc:
				(companyID, mailingName) = rc
				try:
					company = companies[companyID]
				except KeyError:
					rc = c.querys ('SELECT shortname FROM company_tbl WHERE company_id = :cid', {'cid': companyID})
					if not rc is None and not rc[0] is None:
						company = rc[0]
					else:
						company = '#%d' % companyID
					companies[companyID] = company
				if genstatus == 1:
					status = 'is starting up to generate'
				else:
					status = 'is in generation'
				rc = c.querys ('SELECT current_mails, total_mails FROM mailing_backend_log_tbl WHERE status_id = :sid', {'sid': statusID})
				if (rc is None or None in rc) and statusField == 'W':
					rc = c.querys ('SELECT current_mails, total_mails FROM world_mailing_backend_log_tbl WHERE mailing_id = :mid', {'mid': mailingID})
				if not rc is None and not None in rc:
					status += ' (%d of %d are created)' % (rc[0], rc[1])
				else:
					status += ' (nothing created until now)'
				print 'Mailing %s (%d) for Company %s (%d) %s' % (mailingName, mailingID, company, companyID, status)
	c.close ()
	db.close ()
	if active > 0:
		print '%d jobs still active' % active
		sys.exit (1)
	sys.exit (0)
main ()
