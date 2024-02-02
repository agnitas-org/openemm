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
import	argparse
import	sys, os, time
import	DNS
from	datetime import datetime
from	typing import Any, Optional
from	typing import Dict, List
from	agn3.db import DB
from	agn3.exceptions import error
from	agn3.parser import ParseTimestamp
from	agn3.runtime import CLI
from	agn3.stream import Stream
from	agn3.tools import silent_call
#
class DKIM (CLI):
	__slots__ = ['company_id', 'verbose', 'newkey', 'display_key', 'validate_only', 'skip_validation', 'dns_options', 'parameter']
	def add_arguments (self, parser: argparse.ArgumentParser) -> None:
		parser.add_argument (
			'-c', '--company-id', action = 'store', type = int, dest = 'company_id',
			help = 'the company id'
		)
		parser.add_argument (
			'-v', '--verbose', action = 'store_true',
			help = 'be more verbose'
		)
		parser.add_argument (
			'-n', '--newkey', action = 'store_true',
			help = 'create a new key'
		)
		parser.add_argument (
			'-D', '--display-key', action = 'store_true', dest = 'display_key',
			help = 'display public key for DNS integration'
		)
		parser.add_argument (
			'-V', '--validate-only', action = 'store_true', dest = 'validate_only',
			help = 'validate only'
		)
		parser.add_argument (
			'-S', '--skip-validation', action = 'store_true', dest = 'skip_validation',
			help = 'do not validate DNS entry at all'
		)
		parser.add_argument (
			'--dns-tcp', action = 'store_true', dest = 'dns_tcp',
			help = 'use TCP for DNS operations instead of UDP'
		)
		parser.add_argument (
			'--dns-server', action = 'append', default = [], dest = 'dns_server',
			help = 'use this DNS server instead of system defaults (may be used more than once)'
		)
		parser.add_argument (
			'parameter', nargs = '*'
		)
	
	def use_arguments (self, args: argparse.Namespace) -> None:
		self.company_id = args.company_id
		self.verbose = args.verbose
		self.newkey = args.newkey
		self.display_key = args.display_key
		self.validate_only = args.validate_only
		self.skip_validation = args.skip_validation
		if self.validate_only and self.skip_validation:
			raise error ('either just valid or skip validation, not both, supported')
		self.dns_options: Dict[str, Any] = {}
		if args.dns_tcp:
			self.dns_options['protocol'] = 'TCP'
		if args.dns_server:
			self.dns_options['server'] = args.dns_server
		self.parameter = args.parameter
	
	def executor (self) -> bool:
		if self.newkey:
			if len (self.parameter) < 2:
				raise error ('expect two filenames: the path to the private and public key to be written to, optional a third parameter, the length of the key')
			private_path = self.parameter[0]
			public_path = self.parameter[1]
			if len (self.parameter) > 2:
				length = int (self.parameter[2])
			else:
				length = 2048
			self.create (private_path, public_path, length)
		elif self.display_key:
			if len (self.parameter) != 1:
				raise error ('expect exactly one filename: the public key file')
			if not self.display (self.parameter[0], True):
				print (f'** invalid input format in {self.parameter[0]}', file = sys.stderr, flush = True)
		elif self.parameter:
			if len (self.parameter) < 3 or len (self.parameter) > 5 or (self.company_id is None and not self.validate_only):
				if self.company_id is None:
					raise error ('missing company-id')
				raise error ('unexpected number of parameter passed (expecting 3 to 5, got {count}'.format (count = len (self.parameter)))
			domain = self.parameter[0]
			selector = self.parameter[1]
			key_file = self.parameter[2]
			valid_start = None
			valid_end = None
			if len (self.parameter) > 3:
				parser = ParseTimestamp ()
				valid_start = parser (self.parameter[3])
				if len (self.parameter) > 4:
					valid_end = parser (self.parameter[4])
					if valid_end is not None:
						valid_end = datetime.fromordinal (valid_end.toordinal () + 1)
			with open (key_file) as fd:
				key = fd.read ()
			if (self.skip_validation or self.validate (domain, selector, key)) and not self.validate_only:
				self.insert (domain, selector, key, valid_start, valid_end)
		else:
			self.show ()
		return True

	def show (self) -> None:
		def showdate (d: Optional[datetime]) -> str:
			return '' if d is None else f'{d.year:04d}-{d.month:02d}-{d.day:02d}'
		#
		query = (
			'SELECT dkim_id, company_id, domain, selector, valid_start, valid_end, domain_key '
			'FROM dkim_key_tbl '
		)
		if self.company_id is not None:
			query += f' WHERE company_id = {self.company_id} ORDER BY valid_start, dkim_id'
		else:
			query += ' ORDER BY company_id, valid_start, dkim_id'
		print ('%-8.8s  %-10.10s  %-20.20s  %-16.16s  %-14.14s  %-14.14s' % ('ID', 'CompanyID', 'Domain', 'Selector', 'Start', 'End'))
		with DB () as db:
			for r in db.query (query):
				print ('%8d  %10d  %-20.20s  %-16.16s  %-14.14s  %-14.14s' % (r.dkim_id, r.company_id, r.domain, r.selector, showdate (r.valid_start), showdate (r.valid_end)))
				if self.verbose and r.domain_key:
					print ('%s\n' % r.domain_key)
	
	def sign (self, private_key: str, public_key: str) -> bool:
		rc = False
		pattern = '/var/tmp/dkim-mgr-%%s-%d.%f' % (os.getpid (), time.time ())
		sign_file = pattern % 'sign'
		private_file = pattern % 'priv'
		public_file = pattern % 'pub'
		data_file = pattern % 'data'
		try:
			try:
				with open ('/dev/urandom', 'rb') as fdb:
					data = fdb.read (65536)
				with open (data_file, 'wb') as fdb:
					fdb.write (data)
				with open (private_file, 'w') as fdt:
					fdt.write (private_key)
				with open (public_file, 'w') as fdt:
					fdt.write ('-----BEGIN PUBLIC KEY-----\n')
					klen = len (public_key)
					n = 0
					while n < klen:
						fdt.write (public_key[n:n+70] + '\n')
						n += 70
					fdt.write ('-----END PUBLIC KEY-----\n')
			except IOError as e:
				raise error (f'Failed to setup files: {e}')
			n = silent_call ('openssl', 'dgst', '-sha256', '-sign', private_file, '-out', sign_file, data_file)
			if n:
				raise error ('Failed to sign data, openssl returns with %d' % n)
			n = silent_call ('openssl', 'dgst', '-sha256', '-verify', public_file, '-signature', sign_file, data_file)
			if n:
				raise error ('Failed to verify data, openssl returns with %d' % n)
			rc = True
		finally:
			for fname in [sign_file, private_file, public_file, data_file]:
				if os.path.isfile (fname):
					try:
						os.unlink (fname)
					except OSError as e:
						print (f'*** Cleanup, failed to remove "{fname}": {e}')
		return rc
	
	def validate (self, domain: str, selector: str, key: str) -> bool:
		rc = False
		try:
			qtype = 'TXT'
			dkim_domain = f'{selector}._domainkey.{domain}'
			answer = DNS.Request (**self.dns_options).req (name = dkim_domain, qtype = qtype)
			text = (Stream (answer.answers)
				.filter (lambda a: bool (a['typename'] == qtype))
				.map_to (List[bytes], lambda a: a['data'])
				.chain (bytes)
				.map (lambda t: t.decode ('UTF-8', errors = 'backslashreplace'))
				.join ()
			)
		except Exception as e:
			print (f'*** Failed to query dkim domain "{dkim_domain}": {e}')
		else:
			if text:
				record = (Stream (text.split (';'))
					.map (lambda e: e.strip ().split ('=', 1))
					.filter (lambda kv: len (kv) == 2 and bool (kv[0]) and bool (kv[1]))
					.dict ()
				)
				if record.get ('v') != 'DKIM1':
					print (f'*** No (valid) DKIM1 entry for "{dkim_domain}" found: {text}')
				else:
					try:
						rc = self.sign (key, record['p'])
					except KeyError:
						print (f'*** No KEY in DKIM entry "{dkim_domain}" found:\n{text}')
					except error as e:
						print (f'*** Failed to sign signature for "{dkim_domain}": {e}')
			else:
				print (f'*** No {qtype} record for {dkim_domain} found')
		if self.validate_only:
			print ('{dkim_domain} validation {status}'.format (
				dkim_domain = dkim_domain,
				status = 'successful' if rc else 'failed'
			))
		return rc
	
	def insert (self, domain: str, selector: str, key: str, start: Optional[datetime], end: Optional[datetime]) -> None:
		with DB () as db:
			data = {
				'company_id': self.company_id,
				'domain': domain,
				'selector': selector,
				'key': key,
				'vstart': start,
				'vend': end
			}
			rq = db.querys (
				'SELECT count(*) '
				'FROM dkim_key_tbl '
				'WHERE company_id = :company_id AND domain = :domain AND selector = :selector',
				data,
				cleanup = True
			)
			if rq is not None and rq[0] > 0:
				print ('Updating exiting record')
				db.update (
					'UPDATE dkim_key_tbl '
					'SET domain_key = :key, valid_start = :vstart, valid_end = :vend, timestamp = CURRENT_TIMESTAMP '
					'WHERE company_id = :company_id AND domain = :domain AND selector = :selector',
					data,
					cleanup = True
				)
			else:
				print ('Inserting new record')
				db.update (
					db.qselect (
						oracle = (
							'INSERT INTO dkim_key_tbl '
							'       (dkim_id, creation_date, timestamp, company_id, valid_start, valid_end, domain, selector, domain_key) '
							'VALUES '
							'       (dkim_key_tbl_seq.nextval, sysdate, sysdate, :company_id, :vstart, :vend, :domain, :selector, :key)'
						), mysql = (
							'INSERT INTO dkim_key_tbl '
							'       (creation_date, timestamp, company_id, valid_start, valid_end, domain, selector, domain_key) '
							'VALUES '
							'       (CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :company_id, :vstart, :vend, :domain, :selector, :key)'
						)
					),
					data,
					cleanup = True
				)
			db.sync ()

	def create (self, private_file: str, public_file: str, length: int) -> None:
		n = silent_call ('openssl', 'genrsa', '-out', private_file, str (length))
		if n:
			raise error ('Failed to create private key %s, openssl returns %d' % (private_file, n))
		n = silent_call ('openssl', 'rsa', '-in', private_file, '-out', public_file, '-pubout', '-outform', 'PEM')
		if n:
			raise error ('Failed to extract public key %s, openssl returns %d' % (public_file, n))
		if self.verbose:
			if not self.display (public_file, False):
				raise error ('Failed to parse newly created public key %s, aborted' % public_file)
	
	def display (self, public_file: str, pure: bool) -> bool:
		with open (public_file, 'r') as fd:
			data = fd.read ()
		state = 0
		key = ''
		for line in [_l.strip () for _l in data.split ('\n')]:
			if state == 0 and line == '-----BEGIN PUBLIC KEY-----':
				state = 1
			elif state == 1:
				if line == '-----END PUBLIC KEY-----':
					state = 2
				else:
					key += line
		if state == 2:
			if pure:
				print (key)
			else:
				print (f'<selector>._domainkey.<domain> TXT\t"v=DKIM1; p={key}"')
			return True
		return False
#
if __name__ == '__main__':
	DKIM.main ()
