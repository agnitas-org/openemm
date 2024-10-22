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
import	logging, argparse
from	typing import Callable, Iterable, Optional
from	typing import List, TypeVar
from	agn3.crontab import Crontab
from	agn3.db import DB
from	agn3.definitions import syscfg
from	agn3.runtime import CLI
from	agn3.sanity import Sanity, Report, File
from	agn3.stream import Stream
#
logger = logging.getLogger (__name__)
#
_T = TypeVar ('_T')
#
direct_path = syscfg.has ('direct-path', default = False)
#
def service (*svc: str, rc: List[_T]) -> List[_T]:
	return rc if syscfg.service (*svc) else []
def distinct (l: Iterable[_T]) -> List[_T]:
	return Stream (l).distinct ().list ()

directories = distinct (
	service ('openemm', rc = [
		'log', 'var',
		'log/done', 'log/fail',
		'var/lock', 'var/log', 'var/run', 'var/fsdb', 'var/spool', 'var/tmp', 'var/lib',
		'var/spool/ARCHIVE', 'var/spool/DELETED',
		'var/spool/META', 'var/spool/DIRECT',
		'var/spool/ADMIN', 'var/spool/ADMIN0', 'var/spool/RECOVER',
		'var/spool/QUEUE', 'var/spool/MIDQUEUE', 'var/spool/SLOWQUEUE',
		'var/spool/mail', 'var/spool/filter'
	])
)
files: List[File] = []
executables: List[str] = []
#

def check_openemm (r: Report) -> None:
	with DB () as db:
		db.update ('UPDATE company_tbl SET enabled_uid_version = 0 WHERE enabled_uid_version IS NULL OR enabled_uid_version != 0')
		db.update ('UPDATE mailing_tbl SET creation_date = COALESCE(change_date, CURRENT_TIMESTAMP) WHERE company_id = 1 AND creation_date IS NULL')
		db.sync ()
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
			data: dict[str, Optional[str]] = {
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
	Crontab ().update ([
		'10 2 * * * $home/bin/janitor.sh',
		'45 20 * * * $home/bin/bouncemanagement.sh',
	], runas = 'openemm')

checks: List[Callable[[Report], None]] = distinct (
	service ('openemm', rc = [check_openemm])
)
#
class EMM (Sanity):
	def __init__ (self) -> None:
		super ().__init__ (
			directories = directories,
			files = files,
			executables = executables,
			checks = checks,
			umask = 0o22
		)
	
class Main (CLI):
	__slots__: List[str] = []
	def add_arguments (self, parser: argparse.ArgumentParser) -> None:
		parser.add_argument ('to_check', nargs = '*', help = 'backward compatibility, no more in use')

	def executor (self) -> bool:
		try:
			EMM ()
		except Exception as e:
			logger.exception (f'sanity failed due to: {e}')
			raise
		return True
#
if __name__ == '__main__':
	Main.main ()
