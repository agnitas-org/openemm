#!/usr/bin/env python3
####################################################################################################################################################################################################################################################################
#                                                                                                                                                                                                                                                                  #
#                                                                                                                                                                                                                                                                  #
#        Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)                                                                                                                                                                                                   #
#                                                                                                                                                                                                                                                                  #
#        This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.    #
#        This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.           #
#        You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.                                                                                                            #
#                                                                                                                                                                                                                                                                  #
####################################################################################################################################################################################################################################################################
#
from	__future__ import annotations
import	os, logging, argparse
import	time, re
from	datetime import datetime, timedelta, tzinfo
from	enum import Enum
from	typing import Any, Callable, Final, Optional
from	typing import Dict, List, Match, NamedTuple, Set, Tuple
from	agn3.daemon import Daemonic
from	agn3.definitions import base, fqdn, syscfg
from	agn3.email import ParseMessageID
from	agn3.exceptions import error
from	agn3.fsdb import FSDB
from	agn3.ignore import Ignore
from	agn3.io import Filepos, normalize_path
from	agn3.log import LogID, log, mark, log_filter
from	agn3.mta import MTA
from	agn3.plugin import Plugin, LoggingManager
from	agn3.runtime import Runtime
from	agn3.stream import Stream
from	agn3.tools import atob
from	agn3.tracker import Key, Tracker
from	agn3.uid import UIDHandler
#
logger = logging.getLogger (__name__)
#
class SlrtscnPlugin (Plugin):
	plugin_version = '2.0'

class ScanFor (Enum):
	none = 'none'
	all = 'all'
	test = 'test'
	regular = 'regular'
	def __call__ (self, test: bool) -> bool:
		match self:
			case ScanFor.none:
				return False
			case ScanFor.all:
				return True
			case ScanFor.test:
				return test
			case ScanFor.regular:
				return not test

	@staticmethod
	def default () -> ScanFor:
		return ScanFor.all

class SyslogParser:
	__slots__ = ['parser', 'tzcache']
	class Info (NamedTuple):
		timestamp: datetime
		server: str
		service: str
		queue_id: str
		status: str
		content: str
		items: Dict[str, str]
		keywords: Set[str]

	def __init__ (self) -> None:
		self.parser: Callable[[str], Optional[SyslogParser.Info]] = self.guess
		self.tzcache: Dict[str, Optional[tzinfo]] = {}
	
	def __call__ (self, line: str) -> Optional[SyslogParser.Info]:
		return self.parser (line)

	def guess (self, line: str) -> Optional[SyslogParser.Info]:
		for parser in self.parse_rfc3164, self.parse_rfc5424:
			rc = parser (line)
			if rc is not None:
				self.parser = parser
				return rc
		return None
	
	pattern_rfc3164 = re.compile ('^([A-Z][a-z]{2}) +([0-9]+) ([0-9]{2}):([0-9]{2}):([0-9]{2}) +([^ ]+) +([^] ]+)\\[[0-9]+\\]: *(.*)$')
	def parse_rfc3164 (self, line: str) -> Optional[SyslogParser.Info]:
		m = self.pattern_rfc3164.match (line)
		if m is not None:
			with Ignore (ValueError, KeyError):
				(month_name, day, hour, minute, second, server, service, content) = m.groups ()
				month = {
					'Jan':  1, 'Feb':  2, 'Mar':  3, 'Apr':  4, 'May':  5, 'Jun':  6,
					'Jul':  7, 'Aug':  8, 'Sep':  9, 'Oct': 10, 'Nov': 11, 'Dec': 12
				}[month_name]
				now = datetime.now ()
				year = now.year
				if now.month < month:
					year -= 1
				return self.new_info (
					timestamp = datetime (year, month, int (day), int (hour), int (minute), int (second)),
					server = server,
					service = service,
					content = content
				)
		return None
			
	pattern_rfc5424 = re.compile ('^([0-9]{4})-([0-9]{2})-([0-9]{2})T([0-9]{2}):([0-9]{2}):([0-9]{2})(\\.[0-9]+)?(Z|[-+][0-9]{2}(:[0-9]{2})?)? +([^ ]+) +([^] ]+)\\[[0-9]+\\]: *(.*)$')
	def parse_rfc5424 (self, line: str) -> Optional[SyslogParser.Info]:
		m = self.pattern_rfc5424.match (line)
		if m is not None:
			with Ignore (ValueError):
				(year, month, day, hour, minute, second, fraction, timezone, _, server, service, content) = m.groups ()
				microseconds = int (float (fraction) * 1000 * 1000) if fraction else 0
				#
				# ignore TZ for now as it cannot be pickled to be stored in tracker
				# tz = self.parse_tz (timezone) if timezone else None
				return self.new_info (
					timestamp = datetime (int (year), int (month), int (day), int (hour), int (minute), int (second), microseconds),
					server = server,
					service = service,
					content = content
				)
		return None

	pattern_tz = re.compile ('^([-+])([0-9]+)(:[0-9]+)?$')
	def parse_tz (self, tz: str) -> Optional[tzinfo]:
		try:
			return self.tzcache[tz]
		except KeyError:
			try:
				delta: Optional[timedelta]
				if tz == 'Z':
					delta = timedelta ()
				else:
					m = self.pattern_tz.match (tz)
					if m is not None:
						(sign, hours, minutes) = m.groups ()
						multi = 1 if sign == '+' else -1
						delta = timedelta (hours = int (hours) *  multi, minutes = (int (minutes[1:]) * multi) if minutes is not None else 0)
					else:
						delta = None
				if delta is not None:
					timezone_info: Optional[tzinfo] = type (tz, (tzinfo, ), {
						'utcoffset': lambda self, dt: delta,
						'dst': lambda self, dt: False,
						'tzname': lambda self, dt: tz
					}) ()
				else:
					timezone_info = None
				self.tzcache[tz] = timezone_info
				return timezone_info
			except Exception as e:
				logger.debug ('Failed to parse "%s": %s' % (tz, e))
		self.tzcache[tz] = None
		return None

	pattern_queue_id = re.compile ('^([^: ]+): *(.*)$')
	pattern_stat = re.compile ('(, *)(stat(us)?)=(.*)$')
	def new_info (self, timestamp: datetime, server: str, service: str, content: str) -> SyslogParser.Info:
		m = self.pattern_queue_id.match (content)
		if m is not None:
			(queue_id, options) = m.groups ()
		else:
			(queue_id, options) = ('', content)
		m = self.pattern_stat.search (options)
		if m is not None:
			status_key: Optional[str] = m.group (2)
			status: Optional[str] = m.group (4)
			options = options[:m.start ()]
		else:
			status_key = status = None
		rc = self.Info (
			timestamp = timestamp,
			server = server,
			service = service,
			queue_id = queue_id,
			status = status if status is not None else '',
			content = options,
			items = {},
			keywords = set ()
		)
		for p in options.split (', '):
			try:
				(var, val) = [_p.strip () for _p in p.split ('=', 1)]
				vars = var.split ()
				if len (vars) > 1:
					var = vars.pop ()
					for kw in vars:
						rc.keywords.add (kw)
				rc.items[var] = val
			except ValueError:
				rc.keywords.add (p.strip ())
		if status_key is not None and status is not None:
			rc.items[status_key] = status
		return rc

class Scanner:
	__slots__ = [
		'maillog', 'save_file', 'bounce_log', 'deliver_log', 'provider_log', 'scan_for',
		'plugin', 'mtrack', 'fsdb', 'last_expired', 'last_processed'
	]
	tracker_path = ''
	SEC_MTAID: Final[str] = 'mta-id'
	KEY_LINES: Final[str] = '__lines__'
	def __init__ (self, maillog: str, save_file: str, bounce_log: str, deliver_log: str, provider_log: str, scan_for: ScanFor) -> None:
		self.maillog = maillog
		self.save_file = save_file
		self.bounce_log = bounce_log
		self.deliver_log = deliver_log
		self.provider_log = provider_log
		self.scan_for = scan_for
		self.plugin = SlrtscnPlugin (manager = LoggingManager)
		self.mtrack = Tracker (self.tracker_path)
		self.fsdb = FSDB ()
		self.last_expired = 0
		self.last_processed = 0

	def done (self) -> None:
		self.mtrack.close ()
		self.plugin.shutdown ()

	def write_bounce (self,
		dsn: str,
		licence_id: int,
		mailing_id: int,
		customer_id: int,
		timestamp: datetime,
		reason: str,
		queue_id: str,
		relay: str,
		recipient: str,
		test: bool
	) -> None:
		if self.scan_for (test):
			try:
				recipient = recipient.strip ('<>')
				info = [
					f'timestamp={timestamp.year:04d}-{timestamp.month:02d}-{timestamp.day:02d} {timestamp.hour:02d}:{timestamp.minute:02d}:{timestamp.second:02d}',
					f'stat={reason}',
					f'queue_id={queue_id}',
					f'relay={relay}',
					f'to={recipient}',
					f'server={fqdn}'
				]
				if test:
					info.append ('test=true')
				self.plugin ().add_bounce_info (dsn, licence_id, mailing_id, customer_id, info)
				with open (self.bounce_log, 'a') as fd:
					fd.write ('%s;%d;%d;0;%d;%s\n' % (dsn, licence_id, mailing_id, customer_id, '\t'.join (info)))
				if self.provider_log and dsn and dsn.count ('.') == 2:
					try:
						with open (self.provider_log, 'a') as fd:
							fd.write ('%d;%s;%s;%s;%s%s\n' % (
								time.mktime (timestamp.timetuple ()) if timestamp else time.time (),
								fqdn,
								dsn,
								relay,
								recipient.split ('@')[-1],
								(f';{reason}' if not dsn.startswith ('2') else '')
							))
					except IOError as e:
						logger.exception (f'Unable to write {self.provider_log}: {e}')
			except IOError as e:
				logger.exception (f'Unable to write {self.bounce_log}: {e}')
	
	def write_deliveries (self, record: Dict[str, Any], lines: List[Tuple[datetime, str]]) -> bool:
		with Ignore (KeyError):
			lines = record.get (self.KEY_LINES, []) + lines
			if lines and record['use']:
				licence_id = record['licence_id']
				mailing_id = record['mailing_id']
				customer_id = record['customer_id']
				enabled = self.fsdb.get (f'delivery:{licence_id}:{mailing_id}')
				if enabled is not None and atob (enabled):
					with open (self.deliver_log, 'a') as fd:
						for (timestamp, line) in lines:
							fd.write (
								'{licence_id};{mailing_id};{customer_id};{timestamp};{line}\n'.format (
									licence_id = licence_id,
									mailing_id = mailing_id,
									customer_id = customer_id,
									timestamp = f'{timestamp.year:04d}-{timestamp.month:02d}-{timestamp.day:02d} {timestamp.hour:02d}:{timestamp.minute:02d}:{timestamp.second:02d}',
									line = line
								)
							)
				with Ignore (KeyError):
					del record[self.KEY_LINES]
				return True
		return False

	def line (self, info: SyslogParser.Info, line: str) -> None:
		key = Key (self.SEC_MTAID, info.queue_id)
		record = self.mtrack.get (key)
		if not self.write_deliveries (record, [(info.timestamp, line)]):
			try:
				record[self.KEY_LINES].append ((info.timestamp, line))
			except KeyError:
				record[self.KEY_LINES] = [(info.timestamp, line)]
		self.mtrack[key] = record
	
	def scan (self, is_active: Callable[[], bool]) -> None:
		self.fsdb.clear ()
		self.expire_tracker ()
		rerun = True
		while rerun:
			rerun = False
			try:
				with Filepos (self.maillog, self.save_file, checkpoint = 1000) as fp:
					self.mtrack.open ()
					try:
						sp = SyslogParser ()
						for (lineno, line) in enumerate (fp, start = 1):
							try:
								info = sp (line)
								if info is not None:
									with log ('parse') as log_id:
										if not self.parse (log_id, info, line):
											logger.warning ('Unparsable line: %s (%r)' % (line, info))
								else:
									logger.warning ('Unparsable format: %s' % line)
							except Exception as e:
								logger.exception ('Failed to parse line: %s: %s' % (line, e))
							if not is_active ():
								break
							if lineno > 10000:
								rerun = True
								break
					finally:
						self.mtrack.close ()
			except error as e:
				logger.error (f'{self.maillog}: access failed: {e}')
			self.process_completed ()
	
	def expire_tracker (self) -> None:
		now = datetime.now ()
		if self.last_expired != now.day:
			self.mtrack.close ()
			Daemonic.call (self.mtrack.expire, '7d')
			self.last_expired = now.day

	def process_completed (self) -> None:
		now = int (time.time ())
		if self.last_processed + 60 < now:
			max_count = 25000		# max # of records to delete per batch
			outdated = 25 * 60 * 60		# if record reached this value, the record is assumed to be done; add one hour to compensate postfixs delay
			to_remove: List[Key] = []
			logger.debug ('Start processing completed records')
			for (key, value) in self.mtrack.items ():
				diff = int (time.time ()) - value[self.mtrack.key_created]
				if diff < 3600:
					diffstr = '%d:%02d' % (diff // 60, diff % 60)
				else:
					diffstr = '%d:%02d:%02d' % (diff // 3600, (diff // 60) % 60, diff % 60)
				#
				if key.section != self.SEC_MTAID:
					logger.debug ('Ignore non %s record: %s' % (self.SEC_MTAID, key))
					continue
				#
				if not value.get ('complete'):
					if diff < outdated:
						logger.debug (f'{key}: Ignore not (yet) completed record since {diffstr}')
						continue
					logger.info (f'{key}: Found outdated incomplete record since {diffstr}')
					value['outdated'] = True
				#
				self.completed (key, value, to_remove)
				#
				self.write_deliveries (value, [])
				to_remove.append (key)
				if len (to_remove) >= max_count:
					logger.info (f'Reached limit of {max_count:,d}, defer further processing')
					break
			#
			if to_remove:
				logger.debug ('Remove {:,d} processed keys'.format (len (to_remove)))
				for key in to_remove:
					with Ignore (KeyError):
						del self.mtrack[key]
						logger.debug (f'Removed {key}')
				logger.debug ('Removed processed keys done')
			else:
				self.last_processed = now
				logger.debug ('No records found to remove')
				
	def completed (self, key: Key, value: Dict[str, Any], to_remove: List[Key]) -> None:
		pass
		
	def parse (self, log_id: LogID, info: SyslogParser.Info, line: str) -> bool:
		raise error ('Subclass must implement parse()')
#
class ScannerPostfix (Scanner):
	__slots__ = ['uid']
	messageid_log = os.path.join (base, 'var', 'run', 'messageid.log')
	tracker_path = os.path.join (base, 'var', 'run', 'scanner-postfix.track3')
	SEC_MESSAGEID: Final[str] = 'message-id'
	
	def __init__ (self, *args: Any, **kwargs: Any) -> None:
		super ().__init__ (*args, **kwargs)
		self.uid = UIDHandler ()

	def process_completed (self) -> None:
		self.__handle_message_ids ()
		super ().process_completed ()

	def __handle_message_ids (self) -> None:
		if os.path.isfile (self.messageid_log):
			with Ignore (OSError):
				os.unlink (self.messageid_log)
	
	def __write_bounce (self, info: SyslogParser.Info, record: Dict[str, Any]) -> None:
		try:
			self.write_bounce (
				record['dsn'],
				record['licence_id'],
				record['mailing_id'],
				record['customer_id'],
				record.get ('timestamp', datetime.now ()),
				record.get ('status', ''),
				info.queue_id,
				record.get ('relay', ''),
				record.get ('to', ''),
				record.get ('test', False)
			)
		except KeyError as e:
			logger.debug ('Ignore incomplete record <%s>: %s' % (record.get ('message_id', ''), e))
	
	def completed (self, key: Key, value: Dict[str, Any], to_remove: List[Key]) -> None:
		if 'message_id' in value:
			message_id = value['message_id']
			if message_id:
				to_remove.append (Key (self.SEC_MESSAGEID, message_id))
		else:
			logger.info (f'Completed record without message_id found for {key}, remove')

	ignore_ids = frozenset (('statistics', 'NOQUEUE', 'warning'))
	ignore_services = frozenset (('postfix/smtpd', 'postfix/master', 'postfix/postfix-script', 'postfix/tlsproxy', 'postfix/trivial-rewrite'))
	pattern_envelope_from = re.compile ('from=<([^>]*)>')
	pattern_message_id = re.compile ('message-id=<([^>]*)>')
	pattern_host_said = re.compile ('host [^ ]+ said: +(.*)$')
	def parse (self, log_id: LogID, info: SyslogParser.Info, line: str) -> bool:
		if info.service in self.ignore_services:
			return True
		#
		if not info.queue_id:
			if info.service == 'postfix/smtp':
				if (
					info.content.startswith ('connect to ') or
					info.content.startswith ('SSL_connect error to ') or
					info.content.startswith ('Untrusted TLS connection established to') or
					info.content.startswith ('Untrusted TLS connection reused to') or
					info.content.startswith ('Anonymous TLS connection established to') or
					info.content.startswith ('Anonymous TLS connection reused to') or
					info.content.startswith ('Verified TLS connection established to') or
					info.content.startswith ('Verified TLS connection reused to') or
					info.content.startswith ('Trusted TLS connection established to') or
					info.content.startswith ('Trusted TLS connection reused to') or
					'offers SMTPUTF8 support, but not 8BITMIME' in info.content
				):
					return True
			return False
		#
		if info.queue_id in self.ignore_ids:
			return True
		#
		log_id.push (info.queue_id)
		self.line (info, line)
		key = Key (self.SEC_MTAID, info.queue_id)
		if info.service == 'postfix/pickup':
			match = self.pattern_envelope_from.search (info.content)
			if match is not None:
				envelopeFrom = match.group (1)
				self.mtrack.update (key, envelopeFrom = envelopeFrom)
				logger.debug ('Found envelopeFrom=%s' % envelopeFrom)
		elif info.service == 'postfix/cleanup':
			match = self.pattern_message_id.search (info.content)
			if match is not None:
				message_id = match.group (1)
				if message_id:
					rec = self.mtrack.get (key)
					try:
						mid = ParseMessageID.match (message_id if message_id.startswith ('<') and message_id.endswith ('>') else f'<{message_id}>')
						if mid is not None:
							uid = self.uid.parse (mid.uid, validate = False)
							rec.update ({
								'licence_id': uid.licence_id,
								'mailing_id': uid.mailing_id,
								'customer_id': uid.customer_id,
								'use': not mid.is_blind_carbon_copy,
								'test': uid.status_field in {'A', 'T'}
							})
					except error as e:
						logger.info (f'Failed to parse message_id <{message_id}>: {e}')
					rec['message_id'] = message_id
					self.mtrack[key] = rec
					logger.debug ('Found message_id=<%s>' % message_id)
		elif info.service == 'postfix/qmgr' and info.content == 'removed':
			with Ignore (KeyError):
				self.mtrack.update (key, complete = True)
				logger.debug ('postfix processing completed')
		elif info.service in ('postfix/qmgr', 'postfix/smtp', 'postfix/error', 'postfix/local'):
			rec = self.mtrack.get (key)
			update: Dict[str, Any] = {
				'timestamp': info.timestamp
			}
			def host_said () -> Optional[Match[str]]:
				if info.service == 'postfix/smtp':
					return self.pattern_host_said.match (info.content)
				return None
			match = host_said ()
			if match is not None:
				if 'status' not in rec:
					update['status'] = match.group (1)
			else:
				if 'from' in info.items:
					update['envelopeFrom'] = info.items['from']
				if 'to' in info.items and 'envelopeTo' not in rec:
					update['envelopeTo'] = info.items['to']
				for available in 'to', 'dsn', 'status', 'relay':
					if available in info.items:
						update[available] = info.items[available]
			if update:
				rec.update (update)
				self.mtrack[key] = rec
				logger.debug ('Update tracking entry: %s' % str (rec))
				if not rec.get ('use'):
					if 'use' in rec:
						logger.debug (f'{key}: {rec} marked as not usable')
					else:
						logger.debug (f'{key}: {rec} without valid mesasge-id discarded')
				elif 'dsn' in update:
					self.__write_bounce (info, rec)
		else:
			logger.info ('Not used: %s' % line)
		#
		return True

class Slrtscn (Runtime):
	def supports (self, option: str) -> bool:
		return option != 'dryrun'

	def setup (self) -> None:
		def filter_io_debug (r: logging.LogRecord) -> bool:
			return r.levelno > logging.DEBUG or r.name != 'agn3.io'
		log_filter (filter_io_debug)

	def add_arguments (self, parser: argparse.ArgumentParser) -> None:
		parser.add_argument (
			'-M', '--maillog',
			action = 'store', default = '/var/log/maillog',
			help = 'Filename of maillog to scan'
		)
		parser.add_argument (
			'-S', '--save-file',
			action = 'store', default = os.path.join (base, 'var', 'run', 'slrtscn.save'),
			help = 'Filename for store current state',
			dest = 'save_file'
		)
		parser.add_argument (
			'-B', '--bounce-log',
			action = 'store', default = os.path.join (base, 'log', 'extbounce.log'),
			help = 'Filename to store bounce information to',
			dest = 'bounce_log'
		)
		parser.add_argument (
			'-D', '--deliver-log',
			action = 'store', default = os.path.join (base, 'log', 'deliver.log'),
			help = 'Filename to store delivery information to',
			dest = 'deliver_log'
		)
		parser.add_argument (
			'-P', '--provider-log',
			action = 'store', default = normalize_path (syscfg.get ('provider-log', os.path.join (base, 'log', 'provider.log'))),
			help = 'Filename to store provider information to',
			dest = 'provider_log'
		)
		parser.add_argument (
			'--scan-for',
			action = 'store',
			help = 'explicit specify for scanning out of {available} [{current}]'.format (
				available = Stream (ScanFor.__members__.keys ()).map (lambda n: f'"{n}"').join (', '),
				current = ScanFor.default ().value
			),
			dest = 'scan_for'
		)
		
	def use_arguments (self, args: argparse.Namespace) -> None:
		self.maillog = args.maillog
		self.save_file = args.save_file
		self.bounce_log = args.bounce_log
		self.deliver_log = args.deliver_log
		self.provider_log = args.provider_log
		self.scan_for: None | str = args.scan_for

	def executor (self) -> bool:
		if self.scan_for is None:
			scan_for = ScanFor.default ()
		else:
			try:
				scan_for = ScanFor.__members__[self.scan_for]
			except KeyError:
				raise error (f'{self.scan_for}: not supported')
		#
		mta = MTA ()
		scanner = ScannerPostfix (self.maillog, self.save_file, self.bounce_log, self.deliver_log, self.provider_log, scan_for)
		logger.info ('Scanning for %s using %s' % (mta.mta, scanner.__class__.__name__))
		while self.running:
			time.sleep (1)
			mark (180)
			scanner.scan (lambda: self.running)
		scanner.done ()
		return True

if __name__ == '__main__':
	Slrtscn.main ()
