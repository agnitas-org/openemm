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
import	os, logging
from	typing import Optional
from	typing import Dict
from	agn3.crontab import Crontab
from	agn3.db import DB
from	agn3.definitions import base
from	agn3.io import relink
from	agn3.sanity import Sanity, Report
#
logger = logging.getLogger (__name__)
#
class OpenEMM (Sanity):
	def __init__ (self) -> None:
		relink (os.path.join (base, 'release', 'backend', 'current', 'bin'), os.path.join (base, 'bin'))
		super ().__init__ (
			directories = [
				'log', 'var',
				'log/done', 'log/fail',
				'var/lock', 'var/log', 'var/run', 'var/fsdb', 'var/spool', 'var/tmp', 'var/lib',
				'var/spool/ARCHIVE', 'var/spool/DELETED',
				'var/spool/META', 'var/spool/DIRECT',
				'var/spool/ADMIN', 'var/spool/ADMIN0', 'var/spool/RECOVER',
				'var/spool/QUEUE', 'var/spool/MIDQUEUE', 'var/spool/SLOWQUEUE',
				'var/spool/mail', 'var/spool/filter'
			], executables = [
				'java'
			], checks = [
				self.__db_sanity,
				self.__crontab
			], runas = 'openemm', startas = 'openemm', umask = 0o22
		)
	
	def __db_sanity (self, r: Report) -> None:
		with DB () as db:
			for (key, value) in [
				('mask-envelope-from', 'false'),
				('direct-path', 'true')
			]:
				rq = db.querys (
					'SELECT count(*) AS cnt '
					'FROM company_info_tbl '
					'WHERE company_id = 0 AND cname = :cname',
					{'cname': key}
				)
				if rq is None or not rq.cnt:
					count = db.update (
						'INSERT INTO company_info_tbl ('
						'       company_id, cname, cvalue, description, creation_date, timestamp'
						') VALUES ('
						'       0, :cname, :cvalue, NULL, current_timestamp, current_timestamp'
						')', {
							'cname': key,
							'cvalue': value
						}, commit = True
					)
					if count == 1:
						logger.info (f'Added configuration value {value} for {key}')
					else:
						logger.error (f'Failed to set configuration value {value} for {key}: {db.last_error ()}')
			#
			for (key, value) in [
				('LOGLEVEL', 'DEBUG'),
				('MAILDIR', '${home}/var/spool/ADMIN'),
				('BOUNDARY', 'OPENEMM'),
				('MAILER', 'OpenEMM ${ApplicationMajorVersion}.${ApplicationMinorVersion}')
			]:
				data: Dict[str, Optional[str]] = {
					'cls': 'mailout',
					'name': 'ini.{key}'.format (key = key.lower ())
				}
				rq = db.querys (
					'SELECT value '
					'FROM config_tbl '
					'WHERE class = :cls AND name = :name AND hostname IS NULL',
					data
				)
				if rq is not None:
					if rq.value != value:
						logger.info (f'{key}: keep DB value "{rq.value}" and not overwrite it with default value {value}')
				else:
					data['value'] = value
					db.update (
						'INSERT INTO config_tbl '
						'       (class, name, value, hostname, description, creation_date, change_date) '
						'VALUES '
						'       (:cls, :name, :value, NULL, NULL, current_timestamp, current_timestamp)',
						data,
						commit = True
					)
	
	def __crontab (self, r: Report) -> None:
		Crontab ().update ([
			'10 2 * * * $home/bin/janitor.sh openemm',
			'45 20 * * * $home/bin/bouncemanagement.sh',
		], runas = 'openemm')

class Sanities:
	checks = {'openemm': OpenEMM}

def main () -> None:
	try:
		OpenEMM ()
	except Exception as e:
		logger.exception (f'Sanity check failed: {e}')
		raise

if __name__ == '__main__':
	main ()
