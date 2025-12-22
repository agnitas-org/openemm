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
import	os, errno, re, csv, subprocess, base64, logging
import	socket, mimetypes
from	collections import defaultdict
from	datetime import datetime
from	dataclasses import dataclass, field
import	email
import	email.policy
from	email.charset import QP, BASE64, add_charset
from	email.message import Message, EmailMessage, MIMEPart
from	email.header import Header
from	email.policy import EmailPolicy, compat32
from	email.utils import parseaddr
from	functools import partial
from	typing import Any, Callable, ClassVar, Optional, Union
from	typing import DefaultDict, Dict, Generator, IO, Iterator, List, Match, NamedTuple, Pattern, Set, Tuple
from	.db import DB, TempDB
from	.definitions import base, fqdn, user, syscfg
from	.emm.config import EMMCompany
from	.exceptions import error
from	.id import IDs
from	.ignore import Ignore
from	.io import csv_default
from	.mailkey import Key, MailKey
from	.stream import Stream
from	.tools import atob
from	.uid import UID, UIDHandler
#
__all__ = ['Message', 'EmailMessage', 'sendmail', 'manage_procmailrc', 'EMail', 'CSVEMail', 'ParseMessageID', 'ParseEMail', 'EMailValidator']
#
logger = logging.getLogger (__name__)
#
class SentStatus (NamedTuple):
	status: bool = False
	return_code: int = 0
	command_output: str = ''
	command_error: str = ''

def manage_procmailrc (content: str, procmailrc: str = os.path.join (base, '.procmailrc')) -> None:
	try:
		with open (procmailrc, 'r') as fd:
			ocontent = fd.read ()
	except IOError as e:
		if e.args[0] != errno.ENOENT:
			logger.warning (f'{procmailrc}: failed to read: {e}')
		ocontent = ''
	if ocontent != content:
		try:
			with open (procmailrc, 'w') as fd:
				fd.write (content)
				os.fchmod (fd.fileno (), 0o600)
			logger.info (f'{procmailrc}: written')
		except (IOError, OSError) as e:
			logger.exception (f'{procmailrc}: failed to write: {e}')

def sendmail (recipients: List[str], mail: Union[str, bytes], sender: Optional[str] = None) -> SentStatus:
	"""Send out mail by invoking MTA CLI"""
	command = syscfg.sendmail (
		recipients = recipients,
		sender = sender
	)
	pp = subprocess.Popen (command, stdin = subprocess.PIPE, stdout = subprocess.PIPE, stderr = subprocess.PIPE)
	(out, err) = pp.communicate (mail.encode ('UTF-8') if isinstance (mail, str) else mail)
	return SentStatus (
		status = pp.returncode == 0,
		return_code = pp.returncode,
		command_output = out.decode ('UTF-8', errors = 'backslashreplace') if out else '',
		command_error = err.decode ('UTF-8', errors = 'backslashreplace') if err else ''
	)

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
		return email.message_from_string (content, policy = email.policy.default)

	@staticmethod
	def from_bytes (content: bytes) -> EmailMessage:
		return email.message_from_bytes (content, policy = email.policy.default)

	nofold_policy = EmailPolicy (refold_source = 'none')
	@staticmethod
	def as_string (msg: EmailMessage, unixfrom: bool) -> str:
		#
		#	workaround as EmailMessage.as_string
		#	has currently a bug not forwarding
		#	the "unixfrom" parameter to its
		#	ancestor
		method = partial ((Message if unixfrom else EmailMessage).as_string, msg)
		try:
			return method (unixfrom = unixfrom)
		except Exception:
			try:
				return method (unixfrom, policy = EMail.nofold_policy)
			except Exception:
				return method (unixfrom, policy = compat32)
	@staticmethod
	def as_bytes (msg: EmailMessage, unixfrom: bool) -> bytes:
		as_string = EMail.as_string (msg, unixfrom)
		try:
			return as_string.encode ('ascii')
		except UnicodeEncodeError:
			return as_string.encode ('UTF-8')

	@staticmethod
	def scanner (fd: IO[str]) -> Iterator[EmailMessage]:
		mail: List[str] = []
		def make () -> EmailMessage:
			return EMail.from_string (''.join (mail))
		#
		for line in fd:
			if line.startswith ('From '):
				if mail:
					yield make ()
					mail.clear ()
			elif line.startswith ('>From '):
				line = line[1:]
			mail.append (line)
		if mail:
			yield make ()
						
	@staticmethod
	def sign (message: str, sender: Optional[str] = None, company_id: Optional[int] = None) -> str:
		with Ignore (ImportError, SyntaxError, error):
			import	dkim
			
			with DB () as db:
				domains: List[str] = []
				if sender is not None:
					sender_domain = sender.split ('@')[-1]
					while sender_domain.find ('.') != -1:
						domains.append (sender_domain)
						sender_domain = sender_domain.split ('.', 1)[-1]
				emmcompany = EMMCompany (db, keys = ['dkim-local-key', 'dkim-global-key'])
				if company_id is not None:
					emmcompany.set_company_id (company_id)
				local_key = emmcompany.get ('dkim-local-key', company_id = company_id, default = False, convert = atob)
				global_key = emmcompany.get ('dkim-global-key', company_id = company_id, default = False, convert = atob)
				if company_id or global_key:
					mailkey = MailKey (company_id = company_id, db = db)
					found: None | Key = None
					for key in (_k for _k in mailkey.mailkeys if _k.method == 'dkim'):
						if key.company_id == 0:
							if global_key:
								found = key
								break
						elif local_key:
							found = key
						elif (domain := key.get ('domain')) is not None and domain in domains:
							found = key
							break
					if (
						found is not None
						and
						(domain := found.get ('domain')) is not None
						and
						(selector := found.get ('selector')) is not None
						and
						(privkey := mailkey.key (found)) is not None
					):
						logger.debug (f'Using {selector}._domainkey.{domain} for signing')
						try:
							signature = dkim.sign (
								message = message.encode ('UTF-8'),
								selector = selector.encode ('UTF-8'),
								domain = domain.encode ('UTF-8'),
								privkey = privkey if isinstance (privkey, bytes) else privkey.encode ('UTF-8'),
								linesep = b'\n',
								length = True
							)
						except Exception as e:
							logger.warning (f'failed to sign message: {e}')
						else:
							if signature:
								signature_string = signature.decode ('UTF-8')
								if message[:5].lower () == 'from ':
									with Ignore (ValueError):
										(unixfrom, message) = message.split ('\n', 1)
										message = f'{unixfrom}\n{signature_string}{message}'
								else:
									message = signature_string + message
							else:
								logger.error ('Mail NOT signed')
					else:
						logger.debug ('signing: no dkim information found')
				else:
					logger.debug ('signing: no global fallback allowed')
		return message

	class Content:
		"""Stores one part of a multipart message"""
		__slots__ = ['content', 'content_type', 'charset', 'related']
		def __init__ (self, content: str, content_type: Optional[str], charset: Optional[str]) -> None:
			self.content = content
			self.content_type = content_type
			self.charset = charset
			self.related: List[EMail.Content] = []

		def set_message (self, msg: MIMEPart, charset: Optional[str]) -> None:
			if self.content_type is not None:
				msg.set_type (self.content_type)
			msg.set_payload (self.content, self.charset if self.charset is not None else charset)

	class Attachment (Content):
		"""Stores an attachemnt as part of a multipart message"""
		__slots__ = ['raw_content', 'filename']
		def __init__ (self, raw_content: bytes, content_type: Optional[str], charset: Optional[str], filename: Optional[str]) -> None:
			super ().__init__ ('', content_type, charset)
			self.raw_content = raw_content
			self.filename = filename

		def set_message (self, msg: MIMEPart, charset: Optional[str]) -> None:
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
				content = base64.encodebytes (self.raw_content).decode ('us-ascii')
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
	
	def add_content (self, content: str, content_type: str, charset: Optional[str] = None) -> EMail.Content:
		"""Add ``content'' (str), store it using ``charset'' and mark it of type ``content_type''"""
		rc = self.Content (content, content_type, charset)
		self.content.append (rc)
		return rc
	
	def reset_content (self) -> None:
		"""Clearts all parts of the multipart message"""
		self.content.clear ()
		
	def set_text (self, text: str, charset: Optional[str] = None) -> EMail.Content:
		"""Add a plain ``text'' variant for the mail using ``charset''"""
		return self.add_content (text, 'text/plain', charset)
	
	def set_html (self, html: str, charset: Optional[str] = None) -> EMail.Content:
		"""Add a ``html'' variant for the mail using ``charset''"""
		return self.add_content (html, 'text/html', charset)

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
		content_type: Optional[str] = None,
		charset: Optional[str] = None,
		filename: Optional[str] = None,
		related: Optional[EMail.Content] = None
	) -> EMail.Attachment:
		"""Add a textual attachment"""
		if charset is None:
			charset = 'UTF-8'
		content_type = self.__content_type (content_type, filename, 'text/plain')
		at = self.Attachment (content.encode (charset), content_type, charset, filename)
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
		at = self.Attachment (raw_content, content_type, None, filename)
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
			if name is not None and not name.startswith ('content-') and name != 'mime-version':
				headers.append (header)
				avail_headers.add (name)
		if 'from' not in avail_headers and self.sender:
			headers.append (f'From: {self.sender}')
		for (hid, sid) in [('to', self.TO), ('cc', self.CC)]:
			if hid not in avail_headers:
				recvs = [_r[1] for _r in self.receivers if _r[0] == sid]
				if recvs:
					headers.append ('{name}: {receivers}'.format (
						name = hid.capitalize (),
						receivers = ', '.join (recvs)
					))
		if 'subject' not in avail_headers and self.subject:
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
	
	def build_message (self) -> EmailMessage:
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
		return root
		
	def build_mail (self) -> str:
		"""Build the multipart mail and return it as a string"""
		return EMail.sign (EMail.as_string (self.build_message (), False) + '\n', sender = self.sender, company_id = self.company_id)
	
	def send_mail (self) -> SentStatus:
		"""Build and send the mail"""
		mail = self.build_mail ()
		mfrom: Optional[str] = None
		if self.mfrom is not None:
			if self.mfrom:
				mfrom = self.mfrom
		elif self.sender:
			mfrom = parseaddr (self.sender)[1]
		return sendmail (
			recipients = [parseaddr (_r[1])[1] for _r in self.receivers],
			mail = mail,
			sender = mfrom
		)
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
	__slots__ = ['data', 'status']
	def __init__ (self,
		sender: Optional[str],
		receivers: List[str],
		subject: Optional[str],
		text: Optional[str],
		data: List[Union[List[Any], Tuple[Any, ...]]],
		filename: Optional[str] = None,
		charset: Optional[str] = None,
		dialect: str = csv_default
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
		mail.add_text_attachment (self.data, content_type = 'text/csv', charset = charset, filename = filename)
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

class ParseMessageID:
	"""Parse a message-id for using as agnUID

>>> from datetime import datetime
>>> from agn3.uid import UID, UIDCache, UIDHandler
>>> from agn3.definitions import licence
>>> uh = UIDHandler (enable_cache = False)
>>> class SimpleCache:
...  def done (self): pass
...  def find (self, uid): return (UIDCache.Company (company_id = 1, secret_key = 'abc123', enabled_uid_version = UIDHandler.default_version, minimal_uid_version = 0, status = 'active'), UIDCache.Mailing (mailing_id = 2, company_id = 1, creation_date = datetime (1970, 1, 1)))
... 
>>> uh.cache = SimpleCache ()
>>> prefix = f'Az19700101000000_{licence}'
>>> domain = 'local.host'
>>> for version in UIDHandler.available_versions:
...  uid = uh.create (uid = UID (company_id = 1, mailing_id = 2, customer_id = 3, prefix = prefix), version = version)
...  print (ParseMessageID.match (f'<{uid}@{domain}>'))
...
MessageID(message_id='<Az19700101000000_1.C.B.C.BGD.IeB.ZhspbvIcTKuPiDgoBx0_2R7SryqYhUATnonXUXu9uaefdCsFQpSfsBA6q7Tem37AsN0kfRjilTnr6Bwhp2gw2g@local.host>', is_blind_carbon_copy=False, uid='Az19700101000000_1.C.B.C.BGD.IeB.ZhspbvIcTKuPiDgoBx0_2R7SryqYhUATnonXUXu9uaefdCsFQpSfsBA6q7Tem37AsN0kfRjilTnr6Bwhp2gw2g', timestamp=datetime.datetime(1970, 1, 1, 0, 0), licence_id=1, domain='local.host')
MessageID(message_id='<Az19700101000000_1.D.B.C.D.A.A.N96H94GDxYWo5_tVOfCZ_gWwmLtyYddOe3U9IROkYewmrE1O4VbMTx3MdmOx5HE5zcSJWeIawiKwaXsM0gh2xA@local.host>', is_blind_carbon_copy=False, uid='Az19700101000000_1.D.B.C.D.A.A.N96H94GDxYWo5_tVOfCZ_gWwmLtyYddOe3U9IROkYewmrE1O4VbMTx3MdmOx5HE5zcSJWeIawiKwaXsM0gh2xA', timestamp=datetime.datetime(1970, 1, 1, 0, 0), licence_id=1, domain='local.host')
MessageID(message_id='<Az19700101000000_1.E.B.B.C.D.A.A.p3zV5C0s5dyN9d0sCrgzdoeb9vN1-HFPFE_W-H_JJEt1MCmZMIKuxkERcXc1a2qNL-cdk3JV6t6Nz7QOeOjtAw@local.host>', is_blind_carbon_copy=False, uid='Az19700101000000_1.E.B.B.C.D.A.A.p3zV5C0s5dyN9d0sCrgzdoeb9vN1-HFPFE_W-H_JJEt1MCmZMIKuxkERcXc1a2qNL-cdk3JV6t6Nz7QOeOjtAw', timestamp=datetime.datetime(1970, 1, 1, 0, 0), licence_id=1, domain='local.host')
MessageID(message_id='<Az19700101000000_1.F.hKJfYwGiX2wBol9tAqJfcgM.iwWK8mE4lsSz9ilnSmpYfYTNqTgblGCx1CmhHiQo7lBgJce9i6R6MRpmkNeI0fX2SCLzwVtZGRPgeVrfjotebQ@local.host>', is_blind_carbon_copy=False, uid='Az19700101000000_1.F.hKJfYwGiX2wBol9tAqJfcgM.iwWK8mE4lsSz9ilnSmpYfYTNqTgblGCx1CmhHiQo7lBgJce9i6R6MRpmkNeI0fX2SCLzwVtZGRPgeVrfjotebQ', timestamp=datetime.datetime(1970, 1, 1, 0, 0), licence_id=1, domain='local.host')
"""
	class MessageID (NamedTuple):
		message_id: str
		is_blind_carbon_copy: bool
		uid: str
		timestamp: Optional[datetime]
		licence_id: int
		domain: str
	pattern_generic = '<(V[^-]*-)?(([a-z]{2})?[0-9]{14}_([0-9]+)(\\.[0-9a-z_-]+){3,8})@([^>]+)>'
	pattern_match = re.compile (f'^{pattern_generic}$', re.IGNORECASE)
	pattern_search = re.compile (pattern_generic, re.IGNORECASE)
	@classmethod
	def match (cls, s: str) -> Optional[ParseMessageID.MessageID]:
		return cls.parse (cls.pattern_match.match (s))
	@classmethod
	def search (cls, s: str) -> Optional[ParseMessageID.MessageID]:
		return cls.parse (cls.pattern_search.search (s))
	@classmethod
	def parse (cls, match: Optional[Match[str]]) -> Optional[ParseMessageID.MessageID]:
		if match is not None:
			elements = match.groups ()
			with Ignore (ValueError):
				return ParseMessageID.MessageID (
					message_id = match.group (),
					is_blind_carbon_copy = elements[0] is not None,
					uid = elements[1],
					timestamp = cls.parse_timestamp (elements[1]),
					licence_id = int (elements[3]),
					domain = elements[5]
				)
		return None
	pattern_timestamp = re.compile ('^([a-z]{2})?([0-9]{14})', re.IGNORECASE)
	@classmethod
	def parse_timestamp (cls, s: str) -> Optional[datetime]:
		match = cls.pattern_timestamp.match (s)
		if match is not None and (timestamp := match.group (2)):
			return datetime (int (timestamp[:4]), int (timestamp[4:6]), int (timestamp[6:8]), int (timestamp[8:10]), int (timestamp[10:12]), int (timestamp[12:14]))
		return None

class ParseEMail:
	"""Parse an EMail to identify recipient

If an EMail contains agnUIDs, this class can be used to parse the
EMail and try to find these IDs and resolve them to the related
customer.

This class can be subclassed and the method parse() can be
overwritten to implement further logic for resolving the customer."""
	__slots__ = [
		'content', 'invalids', 'message', 'uid_handler', 'uid_handler_borrow', 'collect_content',
		'uids', 'unparsable', 'parsed', 'ignore', 'unsubscribe', 'message_status',
		'delivery_status'
	]
	@dataclass
	class ClassifiedUID:
		valid: bool
		uid: UID
		counter: int = 1

	@dataclass
	class NameType:
		name_type: str
		name: str
		@classmethod
		def parse (cls, s: str, default_type: str = '') -> None | ParseEMail.NameType:
			try:
				return cls (*(_n.strip () for _n in s.split (';', 1)))
			except:
				return cls (default_type, s)

	@dataclass
	class RecipientStatus:
		original_recipient: None | ParseEMail.NameType = None
		final_recipient: None | ParseEMail.NameType = None
		action: None | str = None
		status: None | str = None
		status_text: None | str = None
		dsn: None | int = None
		remote_mta: None | ParseEMail.NameType = None
		diagnostic_code: None | ParseEMail.NameType = None
		last_attempt_date: None | str = None
		final_log_id: None | str = None
		will_retry_until: None | str = None
		remain: dict[str, str] = field (default_factory = dict)
		
	@dataclass
	class DeliveryStatus:
		original_envelope_id: None | str = None
		reporting_mta: None | ParseEMail.NameType = None
		dsn_gateway: None | ParseEMail.NameType = None
		received_from_mta: None | ParseEMail.NameType = None
		arrival_date: None | str = None
		recipients: list[ParseEMail.RecipientStatus] = field (default_factory = list)
		@classmethod
		def parse (cls, headers: list[tuple[str, Any]]) -> ParseEMail.DeliveryStatus:
			rc = cls ()
			for (key, option) in cls._traverse (headers):
				if key == 'original-envelope-id':
					rc.original_envelope_id = option
				elif key == 'reporting-mta':
					rc.reporting_mta = ParseEMail.NameType.parse (option, default_type = 'dns')
				elif key == 'dsn-gateway':
					rc.dsn_gateway = ParseEMail.NameType.parse (option)
				elif key == 'received-from-mta':
					rc.received_from_mta = ParseEMail.NameType.parse (option)
				elif key == 'arrival-date':
					rc.arrival_date = option
			return rc
		
		pattern_status: ClassVar[Pattern[str]] = re.compile ('^([0-9]\\.[0-9]{1,3}\\.[0-9]{1,3}).*')
		pattern_dsn: ClassVar[Pattern[str]] = re.compile ('[0-9]\\.[0-9]+\\.[0-9]+')
		def add (self, headers: list[tuple[str, Any]]) -> None:
			def dsn_reduce (dsn_text: str) -> int:
				return Stream (dsn_text.split ('.')).map (lambda e: int (e[:1])).reduce (lambda s, e: s * 10 + e, identity = 0)
			recv = ParseEMail.RecipientStatus ()
			for (key, option) in self._traverse (headers):
				if key == 'original-recipient':
					recv.original_recipient = ParseEMail.NameType.parse (option, default_type = 'rfc822')
				elif key == 'final-recipient':
					recv.final_recipient = ParseEMail.NameType.parse (option, default_type = 'rfc822')
				elif key == 'action':
					recv.action = option
				elif key == 'status':
					if (mtch := self.pattern_status.match (option)) is not None:
						recv.status = mtch.group (1)
						recv.dsn = dsn_reduce (recv.status)
					recv.status_text = option
				elif key == 'remote-mta':
					recv.remote_mta = ParseEMail.NameType.parse (option, default_type = 'dns')
				elif key == 'diagnostic-code':
					recv.diagnostic_code = ParseEMail.NameType.parse (option, default_type = 'smtp')
				elif key == 'last-attempt-date':
					recv.last_attempt_date = option
				elif key == 'final-log-id':
					recv.final_log_id = option
				elif key == 'will-retry-until':
					recv.will_retry_until = option
				else:
					recv.remain[key] = option
			if recv.diagnostic_code is not None and recv.diagnostic_code.name:
				if recv.dsn is None or recv.dsn % 100 == 0:
					for check in (dsn_reduce (_m) for _m in self.pattern_dsn.findall (recv.diagnostic_code.name)):
						if (recv.dsn is None or check > recv.dsn) and check < 600:
							recv.dsn = check
			self.recipients.append (recv)
		
		@staticmethod
		def _traverse (headers: list[tuple[str, Any]]) -> Generator[tuple[str, str], None, None]:
			return ((_k.lower (), str (_v)) for (_k, _v) in headers)

	def __init__ (self, content: bytes | str, invalids: bool = False, uid_handler: Optional[UIDHandler] = None, collect_content: None | Callable[[str, str], None] = None) -> None:
		"""Parses EMail found in ``content''"""
		self.content = content
		self.invalids = invalids
		self.message = EMail.from_bytes (self.content) if isinstance (self.content, bytes) else EMail.from_string (self.content) 
		if uid_handler is not None:
			self.uid_handler = uid_handler
			self.uid_handler_borrow = True
		else:
			self.uid_handler = UIDHandler (enable_cache = True)
			self.uid_handler_borrow = False
		self.collect_content = collect_content
		self.uids: Dict[str, ParseEMail.ClassifiedUID] = {}
		self.unparsable: Set[str] = set ()
		self.parsed = False
		self.ignore = False
		self.unsubscribe = False
		self.message_status: DefaultDict[str, List[Dict[str, Union[str, Header]]]] = defaultdict (list)
		self.delivery_status: None | ParseEMail.DeliveryStatus = None

	def __del__ (self) -> None:
		with Ignore (AttributeError):
			if not self.uid_handler_borrow:
				self.uid_handler.done ()

	def __iter__ (self) -> Generator[Tuple[bool, UID], None, None]:
		for cuid in self.uids.values ():
			yield (cuid.valid, cuid.uid)
																		
	def parsed_uid (self, uid: str, validate: bool = True) -> UID:
		return self.uid_handler.parse (uid, validate = validate)

	def _add_uid (self, uidstr: str) -> None:
		if uidstr not in self.unparsable:
			try:
				self.uids[uidstr].counter += 1
			except KeyError:
				uid: Optional[UID] = None
				valid = False
				try:
					uid = self.parsed_uid (uidstr)
					valid = True
				except error as e1:
					if self.invalids:
						try:
							uid = self.parsed_uid (uidstr, False)
						except error as e2:
							logger.debug (f'Unparsable UID "{uidstr}" found: {e1}/{e2}')
					else:
						logger.debug (f'Unparsable UID "{uidstr}" found: {e1}')
				if uid is None:
					self.unparsable.add (uidstr)
				else:
					self.uids[uidstr] = ParseEMail.ClassifiedUID (valid = valid, uid = uid)
	
	pattern_link = re.compile ('https?://[^/]+([^ \t\r\n\v"\'>]+)', re.IGNORECASE)
	pattern_uid = re.compile ('uid=(3D)?([0-9a-z_-]+(\\.[0-9a-z_-]+){2,8})|/([0-9a-z_-]+(\\.[0-9a-z_-]+){2,8})/', re.IGNORECASE)
	pattern_subject = re.compile ('unsubscribe:([0-9a-z_-]+(\\.[0-9a-z_-]+){2,8})', re.IGNORECASE)
	pattern_list_unsubscribe_separator = re.compile ('<([^>]*)>')
	pattern_list_unsubscribe = re.compile ('mailto:[^?]+\\?subject=unsubscribe:([0-9a-z_-]+(\\.[0-9a-z_-]+){2,8})', re.IGNORECASE)
	def _find (self,
		s: Union[None, str, Header],
		pattern: Pattern[str],
		callback: Callable[[Match[str]], Union[None, str, List[str]]]
	) -> None:
		if s is not None:
			for m in pattern.finditer (str (s)):
				if (uids := callback (m)) is not None:
					if isinstance (uids, str):
						self._add_uid (uids)
					else:
						for uid in uids:
							self._add_uid (uid)
	def _find_link (self, s: Union[None, str, Header]) -> None:
		def find_uid (m: Match[str]) -> List[str]:
			rc: List[str] = []
			for um in self.pattern_uid.finditer (m.group (1)):
				g = um.groups ()
				if g[1]:
					rc.append (g[1])
				elif g[3]:
					rc.append (g[3])
			return rc
		self._find (s, self.pattern_link, find_uid)

	def _find_message_id (self, s: Union[None, str, Header]) -> None:
		def check_ignore (m: Match[str]) -> str:
			if (mid := ParseMessageID.parse (m)) is not None:
				self.ignore = mid.is_blind_carbon_copy
			return m.group (2)
		self._find (s, ParseMessageID.pattern_search, check_ignore)

	def _find_subject (self, s: Union[None, str, Header]) -> None:
		def set_unsubscribe (m: Match[str]) -> str:
			self.unsubscribe = True
			return m.group (1)
		self._find (s, self.pattern_subject, set_unsubscribe)

	def _find_list_unsubscribe (self, s: Union[None, str, Header]) -> None:
		if s is not None:
			for elem in self.pattern_list_unsubscribe_separator.findall (str (s)):
				self._find_link (elem)
				self._find (elem, self.pattern_list_unsubscribe, lambda m: m.group (1))
			
	def _parse_header (self, payload: Message) -> None:
		for header in ['message-id', 'references', 'in-reply-to']:
			try:
				value = payload[header]
				try:
					self._find_message_id (value)
				except Exception as e:
					logger.warning (f'{header}: cannot find header due to: {e}')
			except Exception as e:
				logger.debug (f'{header}: ignore invalid header: {e}')
		self._find_subject (payload['subject'])
		self._find_list_unsubscribe (payload['list-unsubscribe'])

	def _payload_decode (self, message: Message) -> None | str:
		payload = message.get_payload (decode = True)
		if payload is not None:
			if isinstance (payload, str):
				return payload
			if isinstance (payload, bytes):
				available_charsets = message.get_charsets ()
				charsets = [_c for _c in available_charsets if _c] if available_charsets else []
				for errors in 'strict', 'backslashreplace':
					for charset in charsets if charsets else ['UTF-8', 'ISO-8859-15', 'us-ascii']:
						with Ignore (UnicodeDecodeError, LookupError):
							return payload.decode (charset, errors = errors)
		return None
		
	def _parse_payload (self, message: Message, level: int, report: bool) -> None:
		self._parse_header (message)
		content = self._payload_decode (message)
		if self.parse_payload (message, content, level):
			content_type = message.get_content_type ()
			if content_type is None or content_type.startswith ('text/'):
				if content is not None:
					if content_type == 'text/rfc822-headers':
						embedded_message = EMail.from_string (content)
						self._parse_payload (embedded_message, level + 1, report)
					else:
						self._find_link (content)
					if self.collect_content:
						self.collect_content (content_type.split ('/', 1)[-1] if content_type else 'plain', content)
			elif message.is_multipart ():
				sub_report = report or content_type == 'multipart/report'
				delivery_status = report and content_type == 'message/delivery-status' and self.delivery_status is None
				store_status = content_type.startswith ('message/')
				for submessage in message.get_payload ():
					if isinstance (submessage, str):
						self._find_link (submessage)
					else:
						if delivery_status:
							if self.delivery_status is None:
								self.delivery_status = ParseEMail.DeliveryStatus.parse (submessage.items ())
							else:
								self.delivery_status.add (submessage.items ())
						if store_status:
							try:
								headers = submessage.items ()
							except Exception as e:
								logger.debug (f'failed to parse message block due to "{e}", try removing last line')
								try:
									headers = getattr (submessage, '_headers')
									try:
										faulty = headers.pop ()
										logger.debug (f'remove possible faulty last header line "{faulty}"')
										headers = submessage.items ()
									except Exception as e:
										logger.debug (f'failed removing last header and recollect header due to: {e}')
								except AttributeError:
									logger.debug (f'no headers (attribute) found in {submessage}')
									headers = []
							if headers:
								self.message_status[content_type].append (dict (headers))
						self._parse_payload (submessage, level + 1, sub_report)

	def _parse_message_status (self) -> None:
		for (content_type, status_list) in self.message_status.items ():
			for status_entry in status_list:
				for (header, content) in status_entry.items ():
					header = header.lower ()
					if header == 'message-id':
						self._find_message_id (content)
					elif header == 'list-unsubscribe':
						self._find_list_unsubscribe (content)

	def parse (self) -> None:
		self.uids.clear ()
		self._parse_payload (self.message, 1, False)
		self._parse_message_status ()
		self.parsed = True

	@dataclass
	class Origin:
		valid: bool = False
		customer_id: int = 0
		mailing_id: int = 0
		company_id: int = 0
		licence_id: Optional[int] = None
		context: Dict[str, Any] = field (default_factory = dict)
		counter: int = 0
	def get_origins (self) -> List[ParseEMail.Origin]:
		"""Returns all found customer, sorted by their validity and frequency"""
		if not self.parsed:
			self.parse ()
		m: Dict[Tuple[bool, int, int, int, Optional[int]], ParseEMail.Origin] = {}
		for cuid in self.uids.values ():
			key = (cuid.valid, cuid.uid.customer_id, cuid.uid.mailing_id, cuid.uid.company_id, cuid.uid.licence_id)
			try:
				temp = m[key]
				temp.context.update (cuid.uid.ctx)
				temp.counter += cuid.counter
			except KeyError:
				temp = m[key] = ParseEMail.Origin (
					valid = cuid.valid,
					customer_id = cuid.uid.customer_id,
					mailing_id = cuid.uid.mailing_id,
					company_id = cuid.uid.company_id,
					licence_id = cuid.uid.licence_id,
					context = cuid.uid.ctx.copy (),
					counter = cuid.counter
				)
		return sorted (m.values (), key = lambda a: (a.valid, a.counter))
	
	def get_origin (self) -> Optional[ParseEMail.Origin]:
		"""Returns the most probable customer found in the mail"""
		return Stream (self.get_origins ()).last (no = None)

	def parse_payload (self, message: Message, content: Optional[str], level: int) -> bool:
		"""Hook for custom parsing"""
		return True

class EMailValidator:
	"""Validate an EMail address

This class allows some basic checks against an email address. This
includes syntactical correctness as well as consulting available
blocklists."""
	__slots__ = ['blocklists']
	special = '][{control}\\()<>,::\'"'.format (control = ''.join ([chr (_n) for _n in range (32)]))
	pattern_user = re.compile (f'^("[^"]+"|[^{special}]+)$', re.IGNORECASE)
	pattern_domain_part = re.compile ('[a-z0-9][a-z0-9-]*', re.IGNORECASE)
	class Blocklist (NamedTuple):
		plain: Set[str]
		wildcards: List[Tuple[str, Pattern[str]]]

	def __init__ (self) -> None:
		self.blocklists: Optional[Dict[int, EMailValidator.Blocklist]] = None
	
	def __call__ (self, email: str, company_id: Optional[int] = None) -> None:
		"""Validate an ``email'' address for ``company_id''"""
		self.valid (email, company_id)

	def reset (self) -> None:
		self.blocklists = None
		
	def setup (self, db: Optional[DB] = None, company_id: Optional[int] = None) -> None:
		"""Setup processing ``db'' is an instance of agn3.db.DB or None for ``company_id''"""
		if self.blocklists is None or (company_id is not None and company_id not in self.blocklists):
			with TempDB (db) as mydb:
				if self.blocklists is None:
					self.blocklists = {0: self.__read_blocklist (mydb, 0)}
				if company_id is not None and company_id not in self.blocklists:
					self.blocklists[company_id] = self.__read_blocklist (mydb, company_id)

	def __read_blocklist (self, db: DB, company_id: int) -> EMailValidator.Blocklist:
		rc = EMailValidator.Blocklist (plain = set (), wildcards = [])
		table = 'cust_ban_tbl' if company_id == 0 else f'cust{company_id}_ban_tbl'
		if db.exists (table):
			seen: Set[str] = set ()
			with db.request () as cursor:
				for r in cursor.query (f'SELECT email FROM {table}'):
					if r.email:
						email = r.email.strip ().lower ()
						if email not in seen:
							seen.add (email)
							if '%' in email or '*' in email:
								rc.wildcards.append ((email, re.compile ('^{pattern}$'.format (pattern = re.escape (email.replace ('*', '%')).replace ('%', '.*')))))
							else:
								rc.plain.add (email)
		return rc
	
	def valid (self, email: str, company_id: Optional[int] = None) -> None:
		"""Validate an ``email'' address for ``company_id''"""
		self.setup (company_id = company_id)
		if not email:
			raise error ('empty email')
		email = email.strip ()
		if not email:
			raise error ('empty email (just whitespaces)')
		parts = email.split ('@')
		if len (parts) != 2:
			raise error ('expect exactly one "@" sign')
		(user, domain) = parts
		self.valid_user (user)
		self.valid_domain (domain)
		self.check_blocklist (email, company_id)
	
	def valid_user (self, user: str) -> None:
		"""Validates the local part ``user''"""
		if not user:
			raise error ('empty local part')
		if self.pattern_user.match (user) is None:
			raise error ('invalid local part')
	
	def valid_domain (self, domain: str) -> None:
		"""Validates the ``domain'' part"""
		if not domain:
			raise error ('emtpy domain')
		parts = domain.split ('.')
		if len (parts) == 1:
			raise error ('missing TLD')
		if parts[-1] == '':
			raise error ('empty TLD')
		if len (parts[-1]) < 2:
			raise error ('too short TLD (minium two character expected)')
		for p in parts:
			if p == '':
				raise error ('domain part is empty')
			if self.pattern_domain_part.match (p) is None:
				raise error (f'invalid domain part "{p}"')

	def check_blocklist (self, email: str, company_id: Optional[int] = None) -> None:
		if not self.blocklists:
			self.setup (company_id = company_id)
		if self.blocklists:
			email = email.lower ()
			company_ids = [0]
			if company_id is not None and company_id > 0:
				company_ids.append (company_id)
			for cid in company_ids:
				if cid in self.blocklists:
					where = 'global' if cid == 0 else 'local'
					blocklist = self.blocklists[cid]
					if email in blocklist.plain:
						raise error (f'matches plain in {where} blocklist')
					for (wildcard, pattern) in blocklist.wildcards:
						if pattern.match (email) is not None:
							raise error (f'matches wildcard {wildcard} in {where} blocklist')
