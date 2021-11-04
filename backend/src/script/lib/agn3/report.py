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
import	os, logging
import	html.parser, zipfile, mimetypes
from	typing import Any, Optional
from	typing import Dict, List, NamedTuple, Set, Tuple
from	typing import cast
from	.definitions import base, user, fqdn, program
from	.email import EMail
from	.ignore import Ignore
from	.stream import Stream
from	.template import Template
#
__all__ = ['Report']
#
logger = logging.getLogger (__name__)
#
class Report:
	__slots__ = ['name', 'template', 'images']
	class Image (NamedTuple):
		name: str
		content: bytes
		mime: str

	class ImageFinder (html.parser.HTMLParser):
		__slots__ = ['images']
		def __init__ (self, *args: Any, **kwargs: Any) -> None:
			super ().__init__ ()
			self.images: Set[str] = set ()
	
		def handle_starttag (self, tag: str, attrs: List[Tuple[str, Optional[str]]]) -> None:
			if tag == 'img':
				for (name, value) in attrs:
					if name == 'src' and value is not None:
						self.images.add (value)

	def __init__ (self, name: Optional[str] = None) -> None:
		self.name = name if name is not None else program
		with open (os.path.join (base, 'scripts', f'{self.name}.tmpl')) as fd:
			self.template = Template (fd.read ())
		self.template.compile ()
		self.images: Dict[str, Report.Image] = {}
		with Ignore (IOError), zipfile.ZipFile (os.path.join (base, 'scripts', f'{self.name}.zip'), 'r') as zip:
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
					self.images[entry.filename] = Report.Image (
						name = entry.filename,
						content = fdi.read (),
						mime = mimetype[0] if mimetype is not None and mimetype[0] is not None else f'image/{extension}'
					)

	def create (self,
		recipients: List[str],
		carbon_copies: Optional[List[str]] = None,
		blind_carbon_copies: Optional[List[str]] = None,
		namespace: Optional[Dict[str, Any]] = None,
		dryrun: bool = False
	) -> bool:
		ns = namespace.copy () if namespace is not None else {}
		#
		def retrieve (key: str, default: str) -> str:
			return Template (self.template.property (key, default)).fill (ns).strip ()
		#
		sender = retrieve ('sender', f'{user}@{fqdn}')
		subject = retrieve ('subject', f'Report for {self.name}')
		bodies: Dict[str, str] = {}
		for mode in 'text', 'html':
			ns['_mode'] = mode
			bodies[mode] = self.template.fill (ns)
		#
		mail = EMail ()
		mail.set_charset ('UTF-8')
		if sender:
			mail.set_sender (sender)
		if subject:
			mail.set_subject (subject)
		recvs: Optional[List[str]]
		for (recvs, method) in [
			(recipients, mail.add_to),
			(carbon_copies, mail.add_cc),
			(blind_carbon_copies, mail.add_bcc)
		]:
			if recvs:
				for recv in recvs:
					method (recv)
		if bodies['text']:
			mail.set_text (bodies['text'])
		if bodies['html']:
			content = mail.set_html (bodies['html'])
			ifinder = Report.ImageFinder ()
			ifinder.feed (bodies['html'])
			ifinder.close ()
			for iname in ifinder.images:
				try:
					image = self.images[iname]
					mail.add_binary_attachment (image.content, content_type = image.mime, filename = image.name, related = content)
					logger.debug (f'Image {iname} added')
				except KeyError:
					logger.warning (f'Failed to find used image {iname}')
		if dryrun:
			print (mail.build_mail ())
			return True
		#
		(status, return_code, out, err) = mail.send_mail ()
		pretty_recv = (Stream ([('to', recipients), ('cc', carbon_copies), ('bcc', blind_carbon_copies)])
			.filter (lambda lc: bool (lc[1]))
			.map (lambda lc: '{lable}="{recv}"'.format (lable = lc[0], recv = Stream (cast (List[str], lc[1])).join (',')))
			.join (', ')
		)
		if not status:
			logger.error (f'Failed to send mail to {pretty_recv} ({return_code}):\n{out}\n{err}')
		else:
			logger.info (f'Mail sent to {pretty_recv}')
		return status
