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
import	os, logging, re
import	zipfile, mimetypes
import	html.parser, html.entities
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
	def __init__ (self, name: Optional[str] = None, language: Optional[str] = None) -> None:
		self.name = name if name is not None else program
		with open (os.path.join (base, 'scripts', f'{self.name}.tmpl')) as fd:
			content = fd.read ()
		current_position = 0
		current_language: Optional[str] = None
		sources: Dict[Optional[str], str] = {}
		for match in self.language_splitter.finditer (content):
			(start, end) = match.span ()
			sources[current_language] = content[current_position:start].strip ()
			current_language = match.group ()[1:].lower ()
			current_position = end + 1
		sources[current_language] = content[current_position:].strip ()
		self.template = Template (sources.get (language.lower () if isinstance (language, str) else language, sources[None]))
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
			bodies[mode] = self.template.fill (ns).strip ()
		if not bodies['text'] and bodies['html']:
			bodies['text'] = Report.HTMLConverter ().convert (bodies['html'])
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
					try:
						image = self.images[iname]
					except KeyError:
						image = self.images[iname.split ('/')[-1]]
					mail.add_binary_attachment (image.content, content_type = image.mime, filename = iname, related = content)
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
