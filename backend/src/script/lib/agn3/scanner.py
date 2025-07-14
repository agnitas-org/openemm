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
import	os, io, logging
from	datetime import datetime
from	typing import Any, Callable
from	typing import IO, Iterator, List, Tuple
from	.ignore import Ignore
from	.io import copen
from	.log import log
#
__all__ = ['Logscanner']
#
logger = logging.getLogger (__name__)
#
class Logscanner:
	__slots__ = ['name', 'start', 'end']
	def __init__ (self, name: str, start: datetime, end: datetime) -> None:
		self.name = name
		self.start = start
		self.end = end
	
	def scan (self, timeparser: Callable[[str], datetime]) -> Iterator[str]:
		files_to_scan: List[Tuple[datetime, str]] = []
		available_files = os.listdir (log.path)
		for day in (datetime.fromordinal (_d) for _d in range (self.start.toordinal (), self.end.toordinal () + 2)):
			filename = f'{day.year:04d}{day.month:02d}{day.day:02d}-{self.name}'
			for ext in ['', '.gz']:
				full_filename = f'{filename}{ext}'
				if full_filename in available_files:
					files_to_scan.append ((day, os.path.join (log.path, full_filename)))
					break
		with Ignore (StopIteration):
			for (day, path) in files_to_scan:
				with copen (path) as fd:
					if day.year == self.start.year and day.month == self.start.month and day.day == self.start.day:
						for line in self.__find_start (fd, timeparser):
							yield line
					for line in fd:
						try:
							current = timeparser (line)
							if current > self.end:
								raise StopIteration ()
							if current >= self.start:
								yield line
						except Exception as e:
							logger.debug (f'Failed to parse {line}: {e}')
	
	def __find_start (self, fd: IO[Any], timeparser: Callable[[str], datetime]) -> List[str]:
		backlog: List[str] = []
		try:
			low = 0
			high = fd.seek (0, io.SEEK_END)
			target = 0
			while low + 4096 < high:
				middle = (low + high) // 2
				fd.seek (middle, io.SEEK_SET)
				fd.readline ()
				target = fd.tell ()
				while (line := fd.readline ()):
					try:
						current = timeparser (line)
						if current < self.start:
							low = middle + 1
						else:
							high = middle
						break
					except Exception as e:
						logger.debug (f'Failed to parse {line}: {e}')
				else:
					high = middle
			min_chunk = 4096
			max_chunk = 65536
			chunk_size = min_chunk
			while target > 0:
				prescan = max (0, target - chunk_size)
				fd.seek (prescan, io.SEEK_SET)
				if prescan > 0:
					fd.readline ()
				if fd.tell () < target:
					line = fd.readline ()
					if line:
						current = timeparser (line)
						if current < self.start:
							target = fd.tell ()
							while (line := fd.readline ()):
								current = timeparser (line)
								if current >= self.start:
									break
								target = fd.tell ()
							break
				elif chunk_size < max_chunk:
					chunk_size += chunk_size
				target = prescan
			fd.seek (target, io.SEEK_SET)
		except Exception as e:
			logger.debug (f'Failed during search ({e}), retry sequential')
			with Ignore ():
				fd.seek (0, io.SEEK_SET)
			while True:
				target = fd.tell ()
				line = fd.readline ()
				if line:
					with Ignore ():
						current = timeparser (line)
						if current >= self.start:
							backlog.append (line)
							break
				else:
					break
		return backlog
