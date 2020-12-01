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
from	__future__ import annotations
import	sys, os, re, csv, subprocess, base64, logging
import	socket, mimetypes
from	datetime import datetime
from	dataclasses import dataclass
import	email
import	email.policy
from	email.charset import QP, BASE64, add_charset
from	email.message import EmailMessage
from	email.header import Header
from	email.policy import EmailPolicy, compat32
from	email.utils import parseaddr
from	typing import Any, Callable, Optional, Union
from	typing import Dict, Generator, List, Match, Pattern, Set, TextIO, Tuple
from	typing import cast
from	.definitions import fqdn, user, program
from	.exceptions import error
from	.id import IDs
from	.io import which, CSVDefault
from	.log import log
from	.parser import ParseTimestamp
from	.stream import Stream
from	.uid import UID, UIDHandler
#
__all__ = ['EMail', 'CSVEMail', 'StatusMail', 'ParseEMail']
#
logger = logging.getLogger (__name__)
#
class EMail (IDs):
	"""Create multipart E-Mails"""
	__slots__ = [
		'company_id', 'mfrom', 'sender', 'receivers',
		'subject', 'headers', 'content', 'charset', 'attachments',
		'host', 'user', 'sender'
	]
	TO = 0
	CC = 1
	BCC = 2
	@staticmethod
	def force_encoding (charset: str, encoding: str) -> None:
		"""Enforce an ``encoding'' for a definied ``charset'' overwriting system default behaviour"""
		try:
			charset_encoding = {
				'quoted-printable':	QP,
				'qp':			QP,
				'base64':		BASE64,
				'b64':			BASE64
			}[encoding.lower ()]
			add_charset (charset = charset.lower (), header_enc = charset_encoding, body_enc = charset_encoding)
		except KeyError as e:
			raise error (f'Invalid character set or encoding: {e}')
	
	@staticmethod
	def from_string (content: str) -> EmailMessage:
		return cast (EmailMessage, email.message_from_string (content, policy = email.policy.default))
	nofold_policy = EmailPolicy (refold_source = 'none')
	@staticmethod
	def as_string (msg: EmailMessage, unixfrom: bool) -> str:
		try:
			return msg.as_string (unixfrom)
		except Exception:
			try:
				return msg.as_string (unixfrom, policy = EMail.nofold_policy)
			except Exception:
				return msg.as_string (unixfrom, policy = compat32)

	class Content:
		"""Stores one part of a multipart message"""
		__slots__ = ['content', 'charset', 'content_type', 'related']
		def __init__ (self, content: str, charset: Optional[str], content_type: Optional[str]) -> None:
			self.content = content
			self.charset = charset
			self.content_type = content_type
			self.related: List[EMail.Content] = []

		def set_message (self, msg: EmailMessage, charset: Optional[str]) -> None:
			if self.content_type is not None:
				msg.set_type (self.content_type)
			msg.set_payload (self.content, self.charset if self.charset is not None else charset)

	class Attachment (Content):
		"""Stores an attachemnt as part of a multipart message"""
		__slots__ = ['raw_content', 'filename']
		def __init__ (self, raw_content: bytes, charset: Optional[str], content_type: Optional[str], filename: Optional[str]) -> None:
			super ().__init__ ('', charset, content_type)
			self.raw_content = raw_content
			self.filename = filename

		def set_message (self, msg: EmailMessage, charset: Optional[str]) -> None:
			content_type = self.content_type if self.content_type is not None else 'application/octet-stream'
			if self.filename:
				content_type += f'; name="{self.filename}"'
			if charset is not None:
				content_type += f'; charset="{charset}"'
			msg['Content-Type'] = content_type
			if self.charset is not None:
#				msg['Content-Transfer-Encoding'] = '8bit'
				content = self.raw_content.decode (self.charset)
			else:
				msg['Content-Transfer-Encoding'] = 'base64'
				content = base64.encodestring (self.raw_content).decode ('us-ascii')
			if self.filename:
				msg['Content-Description'] = self.filename
				msg['Content-Location'] = self.filename
				msg['Content-ID'] = f'<{self.filename}>'
			msg.set_payload (content, self.charset)

	def __init__ (self) -> None:
		super ().__init__ ()
		self.company_id: Optional[int] = None
		self.mfrom: Optional[str] = None
		self.sender: Optional[str] = None
		self.receivers: List[Tuple[int, str]] = []
		self.subject: Optional[str] = None
		self.headers: List[str] = []
		self.content: List[EMail.Content] = []
		self.charset: Optional[str] = None
		self.attachments: List[EMail.Content] = []
		try:
			self.host = socket.getfqdn ()
		except Exception:
			self.host = fqdn
		pw = self.get_user ()
		self.user = pw.pw_name if pw is not None else user
		if self.user and self.host:
			self.mfrom = self.sender = f'{self.user}@{self.host}'

	def set_company_id (self, company_id: Optional[int]) -> None:
		"""Set company_id, used when signing the message"""
		self.company_id = company_id

	def set_envelope (self, mfrom: str) -> None:
		"""Set the envelope from address"""
		self.mfrom = mfrom
	set_mfrom = set_envelope
	
	def set_sender (self, sender: str) -> None:
		"""Set the sender of for the mail"""
		self.sender = sender
	set_from = set_sender
	
	def add_receiver (self, recv: str) -> None:
		"""Add a receiver for the mail"""
		self.receivers.append ((self.TO, recv))
	add_to = add_receiver
	
	def add_cc (self, recv: str) -> None:
		"""Add a carbon copy receiver for the mail"""
		self.receivers.append ((self.CC, recv))
	
	def add_bcc (self, recv: str) -> None:
		"""Add a blind carbon copy receiver for the mail"""
		self.receivers.append ((self.BCC, recv))
	
	def reset_recipients (self) -> None:
		"""Clears all receivers of the mail"""
		self.receivers.clear ()
	
	def set_subject (self, subject: str) -> None:
		"""Set the content of the subject header"""
		self.subject = subject
	
	def add_header (self, head: str) -> None:
		"""Add a header"""
		self.headers.append (head)
	
	def reset_header (self) -> None:
		"""Clears all definied header"""
		self.headers.clear ()
	
	def add_content (self, content: str, charset: Optional[str], content_type: str) -> EMail.Content:
		"""Add ``content'' (str), store it using ``charset'' and mark it of type ``content_type''"""
		rc = self.Content (content, charset, content_type)
		self.content.append (rc)
		return rc
	
	def reset_content (self) -> None:
		"""Clearts all parts of the multipart message"""
		self.content.clear ()
		
	def set_text (self, text: str, charset: Optional[str] = None) -> EMail.Content:
		"""Add a plain ``text'' variant for the mail using ``charset''"""
		return self.add_content (text, charset, 'text/plain')
	
	def set_html (self, html: str, charset: Optional[str] = None) -> EMail.Content:
		"""Add a ``html'' variant for the mail using ``charset''"""
		return self.add_content (html, charset, 'text/html')

	def set_charset (self, charset: str) -> None:
		"""Set global ``charset'' to be used for this mail"""
		self.charset = charset

	def __content_type (self, content_type: Optional[str], filename: Optional[str], default: Optional[str]) -> Optional[str]:
		if content_type is None and filename is not None:
			content_type = mimetypes.guess_type (filename)[0]
		if content_type is None:
			content_type = default
		return content_type
			
	def add_text_attachment (self,
		content: str,
		charset: Optional[str] = None,
		content_type: Optional[str] = None,
		filename: Optional[str] = None,
		related: Optional[EMail.Content] = None
	) -> EMail.Attachment:
		"""Add a textual attachment"""
		if charset is None:
			charset = 'UTF-8'
		content_type = self.__content_type (content_type, filename, 'text/plain')
		at = self.Attachment (content.encode (charset), charset, content_type, filename)
		if related is not None:
			related.related.append (at)
		else:
			self.attachments.append (at)
		return at
	
	def add_binary_attachment (self,
		raw_content: bytes,
		content_type: Optional[str] = None,
		filename: Optional[str] = None,
		related: Optional[EMail.Content] = None
	) -> EMail.Attachment:
		"""Add a binary attachment"""
		content_type = self.__content_type (content_type, filename, 'application/octet-stream')
		at = self.Attachment (raw_content, None, content_type, filename)
		if related is not None:
			related.related.append (at)
		else:
			self.attachments.append (at)
		return at
	
	def add_excel_attachment (self,
		raw_content: bytes,
		filename: Optional[str],
		related: Optional[EMail.Content] = None
	) -> EMail.Attachment:
		"""Add an excel sheet binary representation attachement"""
		return self.add_binary_attachment (raw_content, content_type = 'application/vnd.ms-excel', filename = filename, related = related)
	
	def reset_Attachment (self) -> None:
		"""Clears all attachments"""
		self.attachments.clear ()
	
	__name_pattern = re.compile ('^([a-z][a-z0-9_-]*):', re.IGNORECASE)
	def __cleanup_header (self, head: str) -> Tuple[Optional[str], str]:
		head = head.replace ('\r\n', '\n').rstrip ('\n')
		mtch = self.__name_pattern.match (head)
		return (mtch.group (1).lower () if mtch else None, head)
	
	def __finalize_header (self) -> Tuple[List[str], str]:
		headers: List[str] = []
		avail_headers: Set[str] = set ()
		for head in self.headers:
			(name, header) = self.__cleanup_header (head)
			if name is not None and not name.startswith ('content-') and not name in ('mime-version', ):
				headers.append (header)
				avail_headers.add (name)
		if not 'from' in avail_headers and self.sender:
			headers.append (f'From: {self.sender}')
		for (hid, sid) in [('to', self.TO), ('cc', self.CC)]:
			if not hid in avail_headers:
				recvs = [_r[1] for _r in self.receivers if _r[0] == sid]
				if recvs:
					headers.append ('{name}: {receivers}'.format (
						name = hid.capitalize (),
						receivers = ', '.join (recvs)
					))
		if not 'subject' in avail_headers and self.subject:
			headers.append (f'Subject: {self.subject}')
		charset = self.charset if self.charset is not None else 'UTF-8'
		nheaders = []
		for header in headers:
			(name, value) = header.split (':', 1)
			try:
				value.encode ('ascii')
			except UnicodeEncodeError:
				nheaders.append ('{name}: {content}'.format (
					name = name, 
					content = Header (value.strip (), charset).encode ().replace ('\n', ' ')
				))
			else:
				nheaders.append (header.replace ('\n', ' '))
		return (nheaders, charset)
	
	def build_mail (self) -> str:
		"""Build the multipart mail and return it as a string"""
		(headers, charset) = self.__finalize_header ()
		root = EmailMessage ()
		for header in headers:
			(name, value) = header.split (':', 1)
			root[name] = value.strip ()
		msgs = []
		parts = []
		if len (self.content) == 1:
			if not self.attachments:
				parts.append ((root, self.content[0]))
			else:
				msg = EmailMessage ()
				msgs.append (msg)
				root.attach (msg)
				parts.append ((msg, self.content[0]))
		else:
			if self.content:
				if self.attachments:
					parent = EmailMessage ()
					msgs.append (parent)
					root.attach (parent)
				else:
					parent = root
				parent.set_type ('multipart/alternative')
				for content in self.content:
					msg = EmailMessage ()
					msgs.append (msg)
					parts.append ((msg, content))
					if content.related:
						base_message = EmailMessage ()
						msgs.append (base_message)
						base_message.set_type ('multipart/related')
						base_message.attach (msg)
						for related in content.related:
							r = EmailMessage ()
							msgs.append (r)
							parts.append ((r, related))
							base_message.attach (r)
						parent.attach (base_message)
					else:
						parent.attach (msg)
		for (msg, content) in parts:
			content.set_message (msg, charset)
		if self.attachments:
			root.set_type ('multipart/related')
			for attachment in self.attachments:
				at = EmailMessage ()
				msgs.append (at)
				root.attach (at)
				attachment.set_message (at, None)
		for msg in msgs:
			del msg['MIME-Version']
		return self.sign (EMail.as_string (root, False) + '\n')
	
	def send_mail (self) -> Tuple[bool, int, str, str]:
		"""Build and send the mail"""
		(status, returnCode, out, err) = (False, 0, None, None)
		mail = self.build_mail ()
		mfrom: Optional[str]
		if self.mfrom is not None:
			mfrom = self.mfrom
		elif self.sender:
			mfrom = parseaddr (self.sender)[1]
		else:
			mfrom = None
		recvs = [parseaddr (_r[1])[1] for _r in self.receivers]
		sendmail = which ('sendmail')
		if not sendmail:
			sendmail = '/usr/sbin/sendmail'
		cmd = [sendmail]
		if mfrom is not None:
			cmd += ['-f', mfrom]
		cmd += ['--'] + recvs
		pp = subprocess.Popen (cmd, stdin = subprocess.PIPE, stdout = subprocess.PIPE, stderr = subprocess.PIPE, text = True, errors = 'backslashreplace')
		(out, err) = pp.communicate (mail)
		returnCode = pp.returncode
		if returnCode == 0:
			status = True
		return (status, returnCode, out, err)
	
	def sign (self, message: str) -> str:
		return message
#
EMail.force_encoding ('UTF-8', 'qp')
#
class CSVEMail:
	"""Create a mail using a CSV content

An instance of this class must be created passing all relevant
content. These are the ``sender'', a list of ``receivers'', the
``text'' part of the mail, the iterable ``data'' which is used as the
source for the csv content, the ``filename'' of the csv attachment,
the ``charset'' of the csv content and the csv ``dialect'' to be used."""
	__slots__ = ['status']
	def __init__ (self,
		sender: Optional[str],
		receivers: List[str],
		subject: Optional[str],
		text: Optional[str],
		data: List[Union[List[Any], Tuple[Any]]],
		filename: Optional[str] = None,
		charset: Optional[str] = None,
		dialect: str = CSVDefault
	) -> None:
		mail = EMail ()
		if sender is not None:
			mail.set_sender (sender)
		for r in receivers:
			mail.add_receiver (r.strip ())
		if subject is not None:
			mail.set_subject (subject)
		if charset:
			mail.set_charset (charset)
		if text:
			mail.set_text (text)
		self.data = ''
		excel = csv.writer (self, dialect = dialect)
		for row in data:
			excel.writerow (row)
		mail.add_text_attachment (self.data, charset, content_type = 'text/csv', filename = filename)
		self.status = mail.send_mail ()
			
	def write (self, s: str, n: Optional[int] = None) -> int:
		"""Hook for the csv writing instance"""
		if n is not None and n > len (s):
			n = None
		if n is not None:
			self.data += s[:n]
			rc = n
		else:
			self.data += s
			rc = len (s)
		return rc

class StatusMail:
	"""Create status mail
	
Create a status mail for a process for internal use"""
	__slots__ = ['name', 'email', 'recv', 'cc', 'sender']
	OK = 'OK'
	WARN = 'WARNING'
	ERROR = 'ERROR'
	FATAL = 'FATAL'
	recv_ok = ['dagents@agnitas.de']
	recv_fail = ['dagents@agnitas.de', 'admins@agnitas.de']
	def __init__ (self, status: str, name: Optional[str] = None, sender: Optional[str] = None, assetID: Optional[str] = None) -> None:
		"""``status'' is the final status of the process
(these are one of the class constants OK, WARN, ERROR or FATAL),
``name'' is the name of the process (or the name of program, if None),
the mail is send by ``sender'' (or the user running the process, if
None) and ``assetID'' represents the unique ID of the process. The
receiver are statically definied and are taken depending on the
status."""
		self.name = name if name is not None else program
		self.email = EMail ()
		now = datetime.now ()
		self.recv = self.recv_ok[:] if status == self.OK else self.recv_fail[:]
		self.cc: List[str] = []
		self.sender = sender
		self.email.set_subject ('[{status}] {year:04d}{month:02d}{day:02d} {name} (on {user}@{fqdn}){asset_id}'.format (
			status = status,
			year = now.year,
			month = now.month,
			day = now.day,
			name = name,
			user = user,
			fqdn = fqdn,
			asset_id = f' {assetID}' if assetID is not None else ''
		))
	
	def add_cc (self, cc: str) -> None:
		"""Add ``cc'' for a carbon copy of the mail (which may either be a str or a list)"""
		self.cc.append (cc)
	
	def set_text (self, text: str, charset: Optional[str] = None) -> None:
		"""Set the text for the status mail"""
		self.email.set_text (text, charset)
	
	is_id = re.compile ('^[a-z0-9_][a-z0-9_.-]*$', re.IGNORECASE)
	parse_log = re.compile ('^\\[([0-9]{2}\\.[0-9]{2}\\.[0-9]{4}  [0-9]{2}:[0-9]{2}:[0-9]{2})\\] ([0-9]+) ([^/]+)/.*')
	def add_logfile (self,
		name: Optional[str] = None,
		charset: str = 'UTF-8',
		limit: int = 65536,
		unlimited: bool = False,
		keep_debug: bool = False,
		start_when: Optional[datetime] = None,
		my_pid: bool = False
	) -> None:
		"""Add the logfile produces by the process from file
``name'' (if None, the default AGNTIAS logfile for this process is
used) encoded in character set ``charset'' (UTF-8, if None), ``limit''
is the size in bytes to limit the size of the appended logfile, to
enforce no limit, set ``unlimited'' to True. If ``keep_debug'' is
True, then loglevel DEBUG messages are copied as well to the output.
If ``start_when'' is an instance of datetime, then only these logfile
entries are added, that had a timestamp matching this one or later. If
``my_pid'' is True, then only logfile entries with my own PID are
added."""
		fname = name if name is not None else (self.name if self.name is not None else program)
		if not os.path.isfile (fname) and self.is_id.match (fname) is not None:
			fname = log.filename (fname)
		skip_levels: Set[str] = set ()
		if not keep_debug:
			skip_levels.add ('DEBUG')
		if start_when is not None:
			start_when = datetime (start_when.year, start_when.month, start_when.day, start_when.hour, start_when.minute, start_when.second)
			timestamp_parser = ParseTimestamp ()
		pid = os.getpid () if my_pid else None
		try:
			content: str
			with open (fname, 'r', encoding = charset) as fd:
				if not skip_levels:
					if not unlimited and limit > 0:
						content = fd.read (limit)
						if len (content) == limit:
							pos = content.rfind ('\n')
							if pos > 0:
								content = content[:pos]
							content += '\n...\n';
					else:
						content = fd.read ()
				else:
					collect: List[str] = []
					collected = 0
					def match_date (ts: str) -> bool:
						if start_when is None:
							return True
						parsed = timestamp_parser (ts)
						if parsed is not None:
							return parsed >= start_when
						return True
					for line in fd:
						m = self.parse_log.match (line)
						if m is not None:
							(timestamp, logpid, level) = m.groups ()
							if (
									level not in skip_levels
								and
									match_date (timestamp)
								and 
									(pid is None or pid == int (logpid))
							):
								if not unlimited and collected >= limit:
									collect.append ('...\n')
									break
								collect.append (line)
								collected += len (line)
					content = ''.join (collect)
		except IOError as e:
			content = f'** Failed to open {fname}: {e} **'
		self.email.add_text_attachment (content, charset = charset, filename = fname)
	
	def send_mail (self, print_mail: bool = False, outstream: TextIO = sys.stdout) -> bool:
		"""Send the mail or print it, if ``print_mail'' is True to ``outstream''"""
		if self.sender is not None:
			self.email.set_from (self.sender)
		for r in self.recv:
			self.email.add_to (r)
		if self.cc:
			for r in self.cc:
				self.email.add_cc (r)
		if print_mail:
			outstream.write (self.email.build_mail ())
			outstream.flush ()
			status = True
		else:
			(status, rc, out, err) = self.email.send_mail ()
		return status

class ParseEMail:
	"""Parse an EMail to identify recipient

If an EMail contains agnUIDs, this class can be used to parse the
EMail and try to find these IDs and resolve them to the related
customer.

This class can be subclassed and the method parse() can be
overwritten to implement further logic for resolving the customer."""
	__slots__ = [
		'content', 'invalids', 'message', 'uid_handler', 'uid_handler_borrow',
		'uids', 'nvuids', 'unparsed', 'ignore', 'unsubscribe', 'status', 'feedback'
	]
	def __init__ (self, content: str, invalids: bool = False, uid_handler: Optional[UIDHandler] = None) -> None:
		"""Parses EMail found in ``content''"""
		self.content = content
		self.invalids = invalids
		self.message = EMail.from_string (self.content)
		if uid_handler is not None:
			self.uid_handler = uid_handler
			self.uid_handler_borrow = True
		else:
			self.uid_handler = UIDHandler (enable_cache = True)
			self.uid_handler_borrow = False
		self.uids: List[UID] = []
		self.nvuids: List[UID] = []
		self.unparsed = True
		self.ignore = False
		self.unsubscribe = False
		self.status: List[Dict[str, Union[str, Header]]] = []
		self.feedback: List[Dict[str, Union[str, Header]]] = []

	def __del__ (self) -> None:
		if not self.uid_handler_borrow:
			self.uid_handler.done ()

	def parsed_uid (self, uid: str, validate: bool = True) -> UID:
		return self.uid_handler.parse (uid, validate = validate)

	def __iter__ (self) -> Generator[Tuple[bool, UID], None, None]:
		for (valid, uids) in [(False, self.nvuids), (True, self.uids)]:
			if uids is not None:
				for uid in uids:
					yield (valid, uid)

	def __add_uid (self, uid: str) -> None:
		try:
			self.uids.append (self.parsed_uid (uid))
		except error as e1:
			if self.invalids:
				try:
					self.nvuids.append (self.parsed_uid (uid, False))
				except error as e2:
					logger.debug (f'Unparsable UID "{uid}" found: {e1}/{e2}')
			else:
				logger.debug (f'Unparsable UID "{uid}" found: {e1}')
	
	pattern_link = re.compile ('https?://[^/]+/[^?]*\\?.*uid=(3D)?([0-9a-z_-]+(\\.[0-9a-z_-]+){5,7})', re.IGNORECASE)
	pattern_message_id = re.compile ('<(V[^-]*-)?([0-9]{14}_([0-9]+)(\\.[0-9a-z_-]+){6,7})@[^>]+>', re.IGNORECASE)
	pattern_subject = re.compile ('unsubscribe:([0-9a-z_-]+(\\.[0-9a-z_-]+){5,7})', re.IGNORECASE)
	pattern_list_unsubscribe_separator = re.compile ('<([^>]*)>')
	pattern_list_unsubscribe = re.compile ('mailto:[^?]+\\?subject=unsubscribe:([0-9a-z_-]+(\\.[0-9a-z_-]+){5,7})', re.IGNORECASE)
	def __find (self,
		s: Union[None, str, Header],
		pattern: Pattern[str],
		position: int,
		callback: Optional[Callable[[Match[str]], None]] = None
	) -> None:
		if s is not None:
			for m in pattern.finditer (str (s)):
				if callable (callback):
					callback (m)
				self.__add_uid (m.group (position))
	def __find_link (self, s: Union[None, str, Header]) -> None:
		self.__find (s, self.pattern_link, 2)
	def __find_message_id (self, s: Union[None, str, Header]) -> None:
		def checkIgnore (m: Match[str]) -> None:
			self.ignore = m.group (1) is not None
		self.__find (s, self.pattern_message_id, 2, callback = checkIgnore)
	def __find_subject (self, s: Union[None, str, Header]) -> None:
		def setUnsubscribe (m: Match[str]) -> None:
			self.unsubscribe = True
		self.__find (s, self.pattern_subject, 1, callback = setUnsubscribe)
	def __find_list_unsubscribe (self, s: Union[None, str, Header]) -> None:
		if s is not None:
			for elem in self.pattern_list_unsubscribe_separator.findall (str (s)):
				self.__find_link (elem)
				self.__find (elem, self.pattern_list_unsubscribe, 1)
			
	def __parse_header (self, payload: EmailMessage) -> None:
		for header in ['message-id', 'references', 'in-reply-to']:
			try:
				self.__find_message_id (payload[header])
			except Exception as e:
				logger.warning (f'{header}: cannot find header due to: {e}')
		self.__find_subject (payload['subject'])
		self.__find_list_unsubscribe (payload['list-unsubscribe'])
	
	def __parse_payload (self, payload: EmailMessage, level: int) -> None:
		self.__parse_header (payload)
		p = payload.get_payload (decode = True)
		if self.parse_payload (payload, p, level):
			ct = payload.get_content_type ()
			if p is not None:
				content = payload.get_payload ()
				if ct == 'text/rfc822-headers':
					submessage = EMail.from_string (content)
					self.__parse_payload (submessage, level + 1)
				else:
					self.__find_link (content)
			else:
				target: Optional[List[Dict[str, Union[str, Header]]]] = None
				if ct:
					ct = ct.lower ()
					if ct == 'message/feedback-report':
						target = self.feedback
					elif ct == 'message/delivery-status':
						target = self.status
				for pl in cast (List[Union[str, EmailMessage]], payload.get_payload ()):
					if isinstance (pl, str):
						self.__find_link (pl)
					elif target is not None:
						target.append (dict (pl.items ()))
					else:
						self.__parse_payload (pl, level + 1)
		
	def parse (self) -> None:
		self.uids.clear ()
		self.nvuids.clear ()
		self.__parse_payload (self.message, 1)
		self.unparsed = False

	@dataclass
	class Origin:
		valid: bool = False
		customer_id: int = 0
		mailing_id: int = 0
		company_id: int = 0
		licence_id: Optional[int] = None
		counter: int = 0
	def get_origins (self) -> List[ParseEMail.Origin]:
		"""Returns all found customer, sorted by their validity and frequency"""
		if self.unparsed:
			self.parse ()
		m: Dict[Tuple[bool, int, int, int, Optional[int]], ParseEMail.Origin] = {}
		for (valid, uid) in self:
			key = (valid, uid.customer_id, uid.mailing_id, uid.company_id, uid.licence_id)
			try:
				m[key].counter += 1
			except KeyError:
				temp = ParseEMail.Origin (
					valid = valid,
					customer_id = uid.customer_id,
					mailing_id = uid.mailing_id,
					company_id = uid.company_id,
					licence_id = uid.licence_id,
					counter = 1
				)
				m[key] = temp
		return sorted (m.values (), key = lambda a: (a.valid, a.counter))
	
	def get_origin (self) -> Optional[ParseEMail.Origin]:
		"""Returns the most probable customer found in the mail"""
		return cast (Optional[ParseEMail.Origin], Stream (self.get_origins ()).last (no = None))

	def parse_payload (self, message: EmailMessage, content: Optional[str], level: int) -> bool:
		"""Hook for custom parsing"""
		return True
