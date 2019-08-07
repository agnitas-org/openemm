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

import	agn, eagn

class Sanity (eagn.Sanity):
	def __init__ (self):
		super (Sanity, self).__init__ (
			directories = [
				'log', 'var',
				'log/done', 'log/fail',
				'var/lock', 'var/log', 'var/run', 'var/spool', 'var/tmp', 'var/lib',
				'var/spool/ARCHIVE', 'var/spool/DELETED',
				'var/spool/META', 'var/spool/DIRECT',
				'var/spool/ADMIN', 'var/spool/ADMIN0', 'var/spool/RECOVER',
				'var/spool/QUEUE', 'var/spool/MIDQUEUE', 'var/spool/SLOWQUEUE',
				'var/spool/mail', 'var/spool/filter'
			], files = [
				{	'name': 'bin/smctrl', 'issuid': True, 'uid': 0, 'gid': 0	},
				{	'name': 'bin/qctrl', 'issuid': True, 'uid': 0, 'gid': 0		},
			], executables = [
				'java', 'python2'
			], modules = [
				'sqlite3'
			], checks = [
				self.__relink,
				self.__db_sanity
			], runas = 'openemm', startas = 'openemm', umask = 022
		)
	
	def __relink (self, r):
		agn.relink (agn.mkpath (agn.base, 'release', 'backend', 'current', 'bin'), agn.mkpath (agn.base, 'bin'))
	
	def __db_sanity (self, r):
		with eagn.DB () as db:
			key = 'mask-envelope-from'
			rq = db.querys (
				'SELECT count(*) AS cnt '
				'FROM company_info_tbl '
				'WHERE company_id = 0 AND cname = :cname',
				{'cname': key}
			)
			if rq is None or not rq.cnt:
				count = db.execute (
					'INSERT INTO company_info_tbl ('
					'       company_id, cname, cvalue, description, creation_date, timestamp'
					') VALUES ('
					'       0, :cname, :cvalue, NULL, current_timestamp, current_timestamp'
					')', {
						'cname': key,
						'cvalue': 'false'
					}, commit = True
				)
				if count == 1:
					agn.log (agn.LV_INFO, 'sanity', 'Added configuration for envelope address')
				else:
					agn.log (agn.LV_ERROR, 'sanity', 'Failed to set configuration for envelope address: %s' % db.lastError ())

def sanity (what):
	Sanity ()
def main ():
	sanity (None)
if __name__ == '__main__':
	main ()
