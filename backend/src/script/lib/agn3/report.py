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
import	os, logging, re, json
import	zipfile, mimetypes
import	html.parser, html.entities
from	email.utils import parseaddr
from	urllib.parse import urlparse
from	typing import Any, Optional, Sequence
from	typing import Dict, List, NamedTuple, Set, Tuple
from	typing import cast
from	.db import DB
from	.definitions import base, user, fqdn, program, syscfg
from	.email import EMail, EMailValidator
from	.emm.config import EMMConfig, EMMCompany, EMMMessages
from	.emm.merger import Merger
from	.exceptions import error
from	.ignore import Ignore
from	.log import log_limit
from	.stream import Stream
from	.template import Template
from	.tools import listsplit
#
__all__ = ['Recipient', 'Report']
#
logger = logging.getLogger (__name__)
#
class Recipient:
	__slots__ = ['email', 'info']
	def __init__ (self, email: str, **kwargs: None | str):
		self.email = email
		self.info: dict[str, str] = {}
		for (option, value) in kwargs.items ():
			if value is not None:
				self.info[option] = value
	
	def __str__ (self) -> str:
		return f'{self.__class__.__name__} (email = {self.email!r}, info = {self.info!r})'
	__repr__ = __str__

	@classmethod
	def parse_list (cls, s: None | str) -> None | list[Recipient]:
		if s is not None:
			return [Recipient (email = _e) for _e in listsplit (s)]
		return []

	@classmethod
	def retrieve (cls, db: DB, company_id: int, company_admin: bool = True) -> list[Recipient]:
		with db.request () as cursor:
			recipients: list[Recipient] = []
			seen: set[int] = set ()
			query = (
				'SELECT adt.admin_id, adt.email, adt.firstname, adt.fullname, adt.admin_lang '
				'FROM admin_tbl adt '
			)
			clause = (
				'WHERE adt.company_id = :company_id AND adt.email IS NOT NULL '
			)
			data: dict[str, int | str] = {
				'company_id': company_id
			}
			if company_admin:
				query += (
					'INNER JOIN company_tbl co ON co.company_id = adt.company_id'
				)
				clause += (
					'AND co.stat_admin = adt.admin_id'
				)
			else:
				query += (
					'INNER JOIN admin_to_group_tbl atg ON atg.admin_id = adt.admin_id '
					'INNER JOIN admin_group_tbl ag ON ag.admin_group_id = atg.admin_group_id'
				)
				clause += (
					'AND (ag.deleted IS NULL OR ag.deleted = 0) AND ag.shortname = :role'
				)
				data['role'] = syscfg.get ('administrator-role', 'Administrator')
			for row in cursor.query (f'{query} {clause}', data):
				if row.admin_id not in seen:
					seen.add (row.admin_id)
					recipients.append (Recipient (email = row.email, firstname = row.firstname, lastname = row.fullname, language = row.admin_lang))
			return recipients
		return []

class Report:
	__slots__ = ['name', 'template', 'static_mail', 'images']
	class Image (NamedTuple):
		name: str
		content: bytes
		mime: str

	class ImageFinder (html.parser.HTMLParser):
		__slots__ = ['images']
		def __init__ (self, *args: Any, **kwargs: Any) -> None:
			super ().__init__ (*args, **kwargs)
			self.images: Set[str] = set ()
	
		def handle_starttag (self, tag: str, attrs: List[Tuple[str, Optional[str]]]) -> None:
			if tag == 'img':
				for (name, value) in attrs:
					if name == 'src' and value is not None:
						self.images.add (value)
	
	class HTMLConverter (html.parser.HTMLParser):
		__slots__ = ['output', 'inscript']
		def __init__ (self, *args: Any, **kwargs: Any) -> None:
			super ().__init__ (*args, **kwargs)
			self.output: List[str] = []
			self.inscript = False
		
		compact_pattern = re.compile ('([ \t]*\n){2,}', re.MULTILINE)
		remove_trailing_spaces = re.compile ('[ \t]+$')
		def convert (self, html: str) -> str:
			self.output.clear ()
			self.inscript = False
			self.reset ()
			self.feed (html)
			self.close ()
			return self.remove_trailing_spaces.sub ('', self.compact_pattern.sub ('\n\n', ''.join (self.output)))

		def handle_starttag (self, tag: str, attrs: List[Tuple[str, Optional[str]]]) -> None:
			if tag == 'script':
				self.inscript = True
			elif tag == 'p':
				self.output.append ('\n\n')
			elif tag == 'br':
				self.output.append ('\n')
			
		def handle_endtag (self, tag: str) -> None:
			if tag == 'script':
				self.inscript = False
		
		def handle_data (self, data: str) -> None:
			if not self.inscript:
				self.output.append (data)

		def handle_entityref (self, name: str) -> None:
			try:
				cp = html.entities.name2codepoint[name]
				self.output.append (f'{cp:c}')
			except KeyError:
				self.output.append ('?')
	
		def handle_charref (self, name: str) -> None:
			if name.startswith ('x'):
				cp = int (name[1:], 16)
			else:
				cp = int (name)
			self.output.append (f'{cp:c}')

	language_splitter = re.compile ('^>[a-z]{2}$', re.MULTILINE | re.IGNORECASE)
	@classmethod
	def from_template (cls, name: None | str = None, language: None | str = None) -> Report:
		report_name = name if name is not None else program
		with open (os.path.join (base, 'scripts', f'{report_name}.tmpl')) as fd:
			content = fd.read ()
		current_position = 0
		current_language: Optional[str] = None
		sources: Dict[Optional[str], str] = {}
		for match in cls.language_splitter.finditer (content):
			(start, end) = match.span ()
			sources[current_language] = content[current_position:start].strip ()
			current_language = match.group ()[1:].lower ()
			current_position = end + 1
		sources[current_language] = content[current_position:].strip ()
		template = Template (sources.get (language.lower () if isinstance (language, str) else language, sources[None]))
		template.compile ()
		images: Dict[str, Report.Image] = {}
		with Ignore (IOError), zipfile.ZipFile (os.path.join (base, 'scripts', f'{report_name}.zip'), 'r') as zip:
			for entry in list (zip.infolist ()):
				with zip.open (entry, 'r') as fdi:
					mimetype = mimetypes.guess_type (entry.filename)
					try:
						(_, extension) = os.path.splitext (entry.filename)
						if extension.startswith ('.'):
							extension = extension[1:]
					except ValueError:
						extension = ''
					if not extension:
						extension = 'jpg'
					images[entry.filename] = Report.Image (
						name = entry.filename,
						content = fdi.read (),
						mime = mimetype[0] if mimetype is not None and mimetype[0] is not None else f'image/{extension}'
					)
		return cls (report_name, template = template, static_mail = None, images = images)

	class StaticMail (NamedTuple):
		sender: None | str
		subject: None | str
		text: None | str
		html: None | str
		def empty (self) -> bool:
			return self.subject is None and self.text is None and self.html is None
	
	@classmethod
	def from_database (cls, name: str, message_key_base: str, language: None | str = None, company_info: None | Dict[str, str] = None, virtual: None | Dict[str, str] = None) -> Report:
		images: Dict[str, Report.Image] = {}
		with DB () as db:
			mailing_id = 0
			for mailing_language in Stream ([language, 'en']).distinct ():
				if mailing_language is not None:
					mailing_id = db.streamc (
						'SELECT mailing_id '
						'FROM mailing_tbl '
						'WHERE company_id = 1 AND deleted = 0 AND shortname = :name '
						'ORDER BY mailing_id',
						{
							'name': f'{name}_{mailing_language.upper ()}'
						}
					).map (lambda r: r.mailing_id).last (no = mailing_id)
					if mailing_id > 0:
						break
			if mailing_id > 0:
				try:
					parameter: None | Dict[str, str] = None
					if company_info or virtual:
						parameter = {}
						if company_info:
							parameter['company-info'] = json.dumps (company_info)
						if virtual:
							parameter['virtual'] = json.dumps (virtual)
					result = Merger ().preview (mailing_id, parameter = parameter)
					try:
						preview = json.loads (result)
					except json.JSONDecodeError as e:
						raise error (f'{result}: not a json document: {e}')
					else:
						static_mail = Report.StaticMail (
							sender = parseaddr (preview['from'])[1],
							subject = preview['subject'],
							text = preview['text'],
							html = preview['html']
						)
				except Exception as e:
					logger.warning (f'{mailing_id}: failed to create preview, using message key fallback: {e}')
					mailing_id = 0
				else:
					for row in db.query (
						'SELECT compname, mtype, binblock '
						'FROM component_tbl '
						'WHERE mailing_id = :mailing_id AND comptype IN (1, 5) AND binblock IS NOT NULL',
						{
							'mailing_id': mailing_id
						}
					):
						images[row.compname] = Report.Image (
							name = row.compname,
							content = row.binblock,
							mime = row.mtype
						)
			if mailing_id == 0 or static_mail.empty ():
				subject: None | str = None
				text: None | str = None
				html: None | str = None
				with EMMMessages (db = db, key_bases = [message_key_base]) as emmmsg:
					for (key, value) in emmmsg.scan (language):
						key_for = key[len (message_key_base) + 1:]
						if key_for == 'mail.subject':
							subject = value
						elif key_for == 'mail.body.text':
							text = value
						elif key_for == 'mail.body.html':
							html = value
				static_mail = Report.StaticMail (
					sender = None,
					subject = subject,
					text = text,
					html = html
				)
			if static_mail.empty ():
				raise error (f'{name}@{language if language else "default"} (message base "{message_key_base}"): empty mail')
			if not static_mail.sender:
				with EMMConfig (db = db, class_names = ['mailaddress']) as emmcfg:
					sender = emmcfg.get ('mailaddress', 'sender', default = None)
					if sender:
						static_mail = static_mail._replace (sender = sender)
		return cls (name, None, static_mail, images)
	
	@classmethod
	def to_admin (cls,
		name: str,
		message_key_base: str,
		company_id: int,
		mailing_id: int,
		*,
		default_enabled: bool = False,
		template: bool = False,
		namespace: None | Dict[str, str] = None,
		parameter: Optional[List[str]] = None,
		company_info: None | Dict[str, str] = None,
		company_admin: bool = False,
		predefined_recipients: None | List[Recipient] = None,
		send_copy: bool = False,
		dryrun: bool = False
	) -> bool:
		detail: Dict[str, str] = namespace.copy () if namespace else {}
		recipients: List[Recipient] = predefined_recipients if predefined_recipients else []
		ccs: None | List[Recipient] = None
		bccs: None | List[Recipient] = None
		image_pool: Dict[str, Report.Image] = {}
		with DB () as db:
			if mailing_id != 0:
				rq = db.querys (
					'SELECT mt.company_id, mt.shortname AS mailing_name, ct.shortname AS company_name '
					'FROM mailing_tbl mt '
					'     INNER JOIN company_tbl ct ON ct.company_id = mt.company_id '
					'WHERE mt.mailing_id = :mailing_id',
					{
						'mailing_id': mailing_id
					}
				)
				if rq is not None:
					if company_id == 0:
						company_id = rq.company_id
					detail['mailing-id'] = str (mailing_id)
					detail['mailing-name'] = rq.mailing_name if rq.mailing_name else f'mailing #{mailing_id}'
					detail['company-id'] = str (rq.company_id)
					detail['company-name'] = rq.company_name if rq.company_name else f'company #{rq.company_id}'
				else:
					logger.warning (f'{name}: no mailing for mailing_id {mailing_id} found')
			if company_id > 0:
				rq = db.querys ('SELECT shortname, status FROM company_tbl WHERE company_id = :company_id', {'company_id': company_id})
				if rq is not None:
					if rq.status != 'active':
						logger.warning (f'{name}: do not send report to admins from {rq.status} company {rq.shortname} ({company_id})')
					else:
						if mailing_id == 0:
							detail['company-id'] = str (company_id)
							detail['company-name'] = rq.shortname if rq.shortname else f'company #{company_id}'
						if not recipients:
							recipients = Recipient.retrieve (db, company_id, company_admin)
						enable_key = f'report:enable[{name}]'
						cc_key = f'report:cc[{name}]'
						bcc_key = f'report:bcc[{name}]'
						keys = [enable_key]
						if send_copy and not predefined_recipients:
							keys += [cc_key, bcc_key]
						with EMMCompany (db = db, keys = keys) as emmcompany:
							enabled = emmcompany.enabled (enable_key, default_enabled) (company_id)
							if enabled:
								for value in emmcompany.scan (company_id = company_id):
									if value.name == cc_key:
										ccs = Recipient.parse_list (value.value)
									elif value.name == bcc_key:
										bccs = Recipient.parse_list (value.value)
						if enabled:
							for row in db.query (
								'SELECT filename, mime_type, content '
								'FROM grid_mediapool_element_tbl '
								'WHERE company_id = 1 AND deleted = 0 AND content IS NOT NULL AND mime_type LIKE :mime',
								{
									'mime': 'image/%'
								}
							):
								image_pool[row.filename] = Report.Image (
									name = row.filename,
									content = row.content,
									mime = row.mime_type
								)
						else:
							log_limit (logger.warning, f'{name}: report is not enabled for company_id {company_id}')
							recipients.clear ()
				else:
					logger.warning (f'{name}: no entry for company_id {company_id} found')
		reports: Dict[None | str, Report] = {}
		recipients_lang: Dict[None | str, List[Recipient]] = {}
		seen: Set[None | str] = set ()
		for recipient in recipients:
			language = recipient.info.get ('language')
			if language not in seen:
				seen.add (language)
				try:
					reports[language] = report = (
						cls.from_template (name, language)
						if template else
						cls.from_database (name, message_key_base, language, company_info, detail)
					)
				except error as e:
					logger.warning (f'{name}: no valid report for language "{language if language else "default"}" found: {e}')
				else:
					report.add_images (image_pool)
			if language in reports:
				try:
					recipients_lang[language].append (recipient)
				except KeyError:
					recipients_lang[language] = [recipient]
		rc = True
		for (language, chunk) in recipients_lang.items ():
			detail['language'] = language if language is not None else 'default'
			if not reports[language].create (
				chunk,
				carbon_copies = ccs,
				blind_carbon_copies = bccs,
				namespace = detail,
				parameter = parameter,
				dryrun = dryrun
			):
				logger.warning (f'{name}: failed to send mailings for language {language}')
				rc = False
		return rc
		
	def __init__ (self, name: str, template: None | Template, static_mail: None | Report.StaticMail, images: Dict[str, Report.Image]) -> None:
		self.name = name
		self.template = template
		self.static_mail = static_mail
		self.images = images
	
	def add_images (self, images: Dict[str, Report.Image]) -> None:
		if images:
			if self.images:
				images = images.copy ()
				images.update (self.images)
			self.images = images
		
	def create (self,
		recipients: Sequence[str | Recipient],
		carbon_copies: Optional[Sequence[str | Recipient]] = None,
		blind_carbon_copies: Optional[Sequence[str | Recipient]] = None,
		namespace: Optional[Dict[str, Any]] = None,
		parameter: Optional[List[str]] = None,
		dryrun: bool = False
	) -> bool:
		bodies: Dict[str, str] = {}
		if self.template is not None:
			ns = namespace.copy () if namespace is not None else {}
			#
			def retrieve (key: str, default: str) -> str:
				return Template (self.template.property (key, default) if self.template else default).fill (ns).strip ()
			#
			sender = retrieve ('sender', f'{user}@{fqdn}')
			subject = retrieve ('subject', f'Report for {self.name}')
			for mode in 'text', 'html':
				ns['_mode'] = mode
				bodies[mode] = self.template.fill (ns).strip ()
		elif self.static_mail is not None:
			sender = self.static_mail.sender if self.static_mail.sender else f'{user}@{fqdn}'
			subject = self.static_mail.subject if self.static_mail.subject else self.name
			if self.static_mail.text:
				bodies['text'] = self.static_mail.text
			if self.static_mail.html:
				bodies['html'] = self.static_mail.html
		else:
			raise error (f'{self.name}: nor template neither static information available')

		if 'text' not in bodies and 'html' in bodies:
			bodies['text'] = Report.HTMLConverter ().convert (bodies['html'])
		#
		validator = EMailValidator ()
		def validate (r: Recipient) -> bool:
			try:
				if r.email is None:
					raise error ('email is unset')
				validator.valid (r.email, company_id = 1)
			except error as e:
				logger.warning (f'{self.name}: invalid recipient {r} found: {e}')
				return False
			return True
		#
		def normalize (r: Optional[Sequence[str | Recipient]]) -> List[Recipient]:
			if r:
				return (Stream (r)
					.map (lambda e: e if isinstance (e, Recipient) else Recipient (email = e))
					.distinct (lambda e: e.email)
					.filter (validate)
					.list ()
				)
			return []
		#
		def fill (s: str, ns: Dict[str, Any], param: Optional[List[str]] = None) -> str:
			pattern = '\\{[0-9]+\\}'
			if ns:
				pattern += '|{ns}'.format (
					ns = Stream (ns).map (lambda s: re.escape (s)).join ('|', finisher = lambda s: f'\\[({s})\\]')
				)
			regexp = re.compile (pattern)
			out: List[str] = []
			previous = 0
			for mtch in regexp.finditer (s):
				(start, end) = mtch.span ()
				if previous < start:
					out.append (s[previous:start])
				previous = end
				hit = replace = mtch.group ()
				if hit.startswith ('{') and hit.endswith ('}'):
					if param:
						with Ignore (ValueError, IndexError, KeyError):
							replace = ns[param[int (hit[1:-1])]]
				elif hit.startswith ('[') and hit.endswith (']'):
					replace = ns[hit[1:-1]]
				out.append (replace)
			if previous < len (s):
				out.append (s[previous:])
			return ''.join (out)
		#
		rc = True
		ccs = normalize (carbon_copies)
		bccs = normalize (blind_carbon_copies)
		for recipient in normalize (recipients):
			ns = namespace.copy () if namespace is not None else {}
			ns.update (recipient.info)
			mail = EMail ()
			mail.set_charset ('UTF-8')
			if sender:
				if 'sender' not in ns:
					ns['sender'] = sender
				mail.set_sender (sender)
			if 'to' not in ns:
				ns['to'] = recipient.email
			if subject:
				mail.set_subject (fill (subject, ns, parameter))
			mail.add_to (recipient.email)
			recvs: List[Recipient]
			for (recvs, method) in [
				(ccs, mail.add_cc),
				(bccs, mail.add_bcc)
			]:
				for recv in recvs:
					method (recv.email)
			if 'text' in bodies:
				mail.set_text (fill (bodies['text'], ns, parameter))
			if 'html' in bodies:
				content = mail.set_html (fill (bodies['html'], ns, parameter))
				ifinder = Report.ImageFinder ()
				ifinder.feed (bodies['html'])
				ifinder.close ()
				for iname in ifinder.images:
					try:
						try:
							image = self.images[iname]
						except KeyError:
							image = self.images[iname.split ('/')[-1]]
						mail.add_binary_attachment (image.content, content_type = image.mime, filename = iname, related = content)
						logger.debug (f'Image {iname} added')
					except KeyError:
						url = urlparse (iname)
						expected = url.path.endswith ('.html') or url.path.endswith ('/g')
						(logger.debug if expected else logger.warning) (f'Failed to find used image {iname}')
			if dryrun:
				print (mail.build_mail ())
			else:
				#
				(status, return_code, out, err) = mail.send_mail ()
				pretty_recv = (Stream ([('to', [recipient]), ('cc', ccs), ('bcc', bccs)])
					.filter (lambda lc: bool (lc[1]))
					.map (lambda lc: '{lable}="{recv}"'.format (lable = lc[0], recv = Stream (cast (List[str], lc[1])).join (',')))
					.join (', ')
				)
				if not status:
					logger.error (f'Failed to send mail to {pretty_recv} ({return_code}):\n{out}\n{err}')
					rc = False
				else:
					logger.info (f'Mail sent to {pretty_recv}')
		return rc
