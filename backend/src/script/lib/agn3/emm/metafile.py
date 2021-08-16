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
import	re, time, os
from	datetime import datetime
from	typing import Union
from	typing import List
from	..definitions import licence
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
		'licence', 'company', 'timestamp', 'mailid', 'mailing', 'blocknr', 'blockid',
		'single'
	]
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
			n = parts[0].find ('-')
			if n != -1:
				try:
					self.licence = int (parts[0][n + 1:])
				except ValueError:
					self.licence = -1
					self.__error (f'Unparsable licence ID in "{parts[0]}" found')
			else:
				self.licence = licence
			cinfo = parts[2].split ('-')
			try:
				self.company = int (cinfo[0])
			except ValueError:
				self.company = -1
				self.__error (f'Unparseable company ID in "{parts[2]}" found')
			self.timestamp = self.__parse_timestamp (parts[1])
			self.mailid = parts[3]
			mparts = [_m for _m in self.splitter.split (self.mailid) if _m]
			if len (mparts) == 0:
				self.__error (f'Unparseable mailing ID in "{parts[3]}" found')
			else:
				try:
					self.mailing = int (mparts[-1])
				except ValueError:
					self.__error (f'Unparseable mailing ID in mailid "{self.mailid}" found')
			try:
				self.blocknr = int (parts[4])
				self.blockid = str (self.blocknr)
				self.single = False
			except ValueError:
				self.blocknr = 0
				self.blockid = parts[4]
				self.single = True

	def __make_timestamp (self, epoch: Union[int, float]) -> str:
		tt = datetime.fromtimestamp (epoch)
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

	def is_ready (self, epoch: Union[None, int, float, str] = None) -> bool:
		"""Checks if file is ready for sending

according to the coded timestamp of the filename, this method checks,
if the file is ready for sending."""
		if epoch is None:
			ts = self.__make_timestamp (time.time ())
		elif isinstance (epoch, str):
			ts = epoch
		else:
			ts = self.__make_timestamp (float (epoch))
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
