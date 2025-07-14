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
import	re, time, os
from	datetime import datetime
from	typing import Union
from	typing import List
from	..definitions import base, licence
from	..ignore import Ignore
#
__all__ = ['METAFile']
#
class METAFile:
	"""Handles XML files from mail generation

this class can help to interpret the filenames of files created by the
merger which contains serveral informations."""
	__slots__ = [
		'valid', 'error', 'path', 'directory', 'filename', 'extension', 'basename',
		'licence_id', 'company_id', 'timestamp', 'mailid', 'mailing_id', 'blocknr', 'blockid',
		'typ', 'single'
	]
	temp_directory = os.path.join (base, 'var', 'spool', 'TEMP')
	sideshow_directory = os.path.join (base, 'var', 'spool', 'SIDESHOW')
	meta_directory = os.path.join (base, 'var', 'spool', 'META')
	archive_directory = os.path.join (base, 'var', 'spool', 'ARCHIVE')
	recover_directory = os.path.join (base, 'var', 'spool', 'RECOVER')
	deleted_directory = os.path.join (base, 'var', 'spool', 'DELETED')
	outdated_directory = os.path.join (base, 'var', 'spool', 'OUTDATED')
	splitter = re.compile ('[^0-9]+')
	def __init__ (self, path: str) -> None:
		"""Sets the path to the XML file

this sets the path to the XML file and parses the coded content of the
filename."""
		self.valid = False
		self.error: List[str] = []
		self.path = path
		self.directory = os.path.dirname (self.path)
		self.filename = os.path.basename (self.path)
		try:
			(self.basename, self.extension) = self.filename.split ('.', 1)
		except ValueError:
			(self.basename, self.extension) = (self.filename, '')
		parts = self.basename.split ('=')
		if len (parts) != 6:
			self.__error ('Invalid format of input file')
		else:
			self.valid = True
			try:
				(_, licence_expr) = parts[0].split ('-')
				self.licence_id = int (licence_expr)
			except ValueError:
				self.licence_id = licence
			try:
				self.company_id = int (parts[2].split ('-', 1)[0])
			except ValueError:
				self.company_id = -1
				self.__error (f'Unparseable company ID in "{parts[2]}" found')
			self.timestamp = self.__parse_timestamp (parts[1])
			self.mailid = parts[3]
			mparts = self.splitter.split (self.mailid)
			if len (mparts) == 0:
				self.__error (f'Unparseable mailing ID in "{parts[3]}" found')
			else:
				try:
					self.mailing_id = int (mparts[-1])
				except ValueError:
					self.__error (f'Unparseable mailing ID in mailid "{self.mailid}" found')
			m = self.splitter.search (self.mailid)
			self.typ = m.group ()[:1] if m is not None else 'W'
			self.single = self.typ in 'ATCEV'
			try:
				self.blocknr = int (parts[4])
				self.blockid = str (self.blocknr)
			except ValueError:
				self.blocknr = 0
				self.blockid = parts[4]
				self.single = True
			if self.single:
				self.mailid = self.basename

	def __make_timestamp (self, ts: Union[int, float]) -> str:
		tt = datetime.fromtimestamp (ts)
		return f'{tt.year:04d}{tt.month:02d}{tt.day:02d}{tt.hour:02d}{tt.minute:02d}{tt.second:02d}'

	def __parse_timestamp (self, ts: str) -> str:
		if ts[0] == 'D' and len (ts) == 15:
			return ts[1:]
		with Ignore (ValueError):
			return self.__make_timestamp (int (ts))
		return self.__make_timestamp (int (time.time ()))

	def __error (self, s: str) -> None:
		self.error.append (s)
		self.valid = False

	def is_ready (self, timestamp: Union[None, int, float, str] = None) -> bool:
		"""Checks if file is ready for sending

according to the coded timestamp of the filename, this method checks,
if the file is ready for sending."""
		if timestamp is None:
			ts = self.__make_timestamp (time.time ())
		elif isinstance (timestamp, str):
			ts = timestamp
		else:
			ts = self.__make_timestamp (float (timestamp))
		return self.valid and self.timestamp <= ts

	def get_error (self) -> str:
		"""Returns errors

if there are errors during parsing, this returns a string with a list
of all errors or the string "no error", if there had been no error.
Primary used for logging."""
		if not self.error:
			return 'no error'
		return ', '.join (self.error)

	def as_datetime (self) -> datetime:
		return datetime (
			year = int (self.timestamp[:4]),
			month = int (self.timestamp[4:6]),
			day = int (self.timestamp[6:8]),
			hour = int (self.timestamp[8:10]),
			minute = int (self.timestamp[10:12]),
			second = int (self.timestamp[12:])
		)
