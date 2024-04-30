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
from	__future__ import annotations
import	logging, argparse
import	os, time, shutil
import	asyncio
from	dataclasses import dataclass
from	typing import Callable, Final, Optional
from	typing import Dict, List
from	agn3.aioruntime import AIORuntime
from	agn3.config import Config
from	agn3.definitions import base, syscfg
from	agn3.emm.metafile import METAFile
from	agn3.exceptions import error
from	agn3.io import create_path, ArchiveDirectory
from	agn3.mta import MTA
from	agn3.rpc import XMLRPC
#
logger = logging.getLogger (__name__)
#
@dataclass
class XMLPack:
	data: Optional[str] = None
	stamp: Optional[str] = None
	final: Optional[str] = None

class DPath (AIORuntime):
	__slots__ = ['worker', 'mta', 'queue']
	incoming_directory: Final[str] = syscfg.get ('direct-path-incoming', os.path.join (base, 'DIRECT') if not syscfg.service ('console', 'rdir') else os.path.join (base, 'var', 'spool', 'DIRECT'))
	archive_directory: Final[str] = syscfg.get ('direct-path-archive', os.path.join (base, 'ARCHIVE'))
	recover_directory: Final[str] = syscfg.get ('direct-path-recover', os.path.join (base, 'RECOVER'))
	def add_arguments (self, parser: argparse.ArgumentParser) -> None:
		parser.add_argument (
			'--worker', action = 'store', type = int, default = 4,
			help = 'set maximum number of parallel workers'
		)
		
	def use_arguments (self, args: argparse.Namespace) -> None:
		self.worker: int = args.worker

	def setup (self) -> None:
		for path in self.incoming_directory, self.archive_directory, self.recover_directory:
			create_path (path)
				
	def executors (self) -> Optional[List[Callable[[], bool]]]:
		return [
			self.swallow,
			self.executor
		]
	
	def swallow (self) -> bool:
		config = Config ()
		config['xmlrpc.host'] = 'localhost'
		config['xmlrpc.port'] = syscfg.get ('direct-path-port', '9400')
		config['xmlrpc.allow_none'] = 'true'
		server = XMLRPC (config)
		server.add_method (lambda f: True, name = 'unpack')
		server.run ()
		return True
	
	async def controller (self) -> None:
		self.mta = MTA ()
		self.queue = self.channel ('incoming', XMLPack)
		unpacker = self.task ('unpacker', self.unpacker ())
		collect: Dict[str, XMLPack] = {}
		seen: Dict[str, float] = {}
		async for filename in self.scan (self.incoming_directory):
			path = os.path.join (self.incoming_directory, filename)
			meta = METAFile (path)
			if not meta.valid:
				logger.warning (f'{path}: invalid metafile, move to {self.recover_directory}')
				self.move (path, self.recover_directory)
			else:
				try:
					xmlpack = collect[meta.basename]
				except KeyError:
					xmlpack = collect[meta.basename] = XMLPack ()
				if meta.extension.startswith ('xml'):
					xmlpack.data = path
				elif meta.extension == 'stamp':
					xmlpack.stamp = path
				elif meta.extension == 'final':
					xmlpack.final = path
				if xmlpack.data and xmlpack.stamp and xmlpack.final:
					now = time.time ()
					xmlpack = collect.pop (meta.basename)
					if meta.basename in seen and seen[meta.basename] + 90 >= now:
						logger.warning (f'{xmlpack.data}: already seen in last 90 seconds, skip')
					else:
						logger.debug (f'{xmlpack.data}: full pack forwarded for unpacking')
						await self.queue.put (xmlpack)
					for basename in [_i[0] for _i in seen.items () if _i[1] + 90 < now]:
						del seen[basename]
					seen[meta.basename] = now
		await unpacker.join ()

	async def unpacker (self) -> None:
		async with self.workers ('unpack', limit = self.worker) as workers:
			async for xmlpack in self.queue:
				await workers.launch (self.unpack (xmlpack))
	
	async def unpack (self, xmlpack: XMLPack) -> None:
		await asyncio.shield (self.shielded_unpack (xmlpack))
	
	async def shielded_unpack (self, xmlpack: XMLPack) -> None:
		if xmlpack.data is not None:
			target = self.recover_directory
			try:
				command = self.mta.make (xmlpack.data)
				process = await self.process (f'xmlback {xmlpack.data}', command)
				(returncode, out, err) = await process.communicate_text (errors = 'backslashreplace')
				if returncode == 0:
					if self.archive_directory != os.devnull:
						try:
							target = ArchiveDirectory.make (self.archive_directory)
						except error as e:
							logger.error (f'{self.archive_directory}: failed to setup archive directory: {e}')
							target = self.archive_directory
					else:
						target = os.devnull
					logger.info (f'{xmlpack.data}: successfully unpacked, move to {target}')
				else:
					logger.error (f'{xmlpack.data}: failed to unpack, command {command} return {returncode}')
					if out:
						logger.error (f'\tStdout: {out}')
					if err:
						logger.error (f'\tStderr: {err}')
			finally:
				self.move (xmlpack.data, target)
				self.remove (xmlpack.stamp)
				self.remove (xmlpack.final)
				if target == self.recover_directory:
					sync = os.path.join (os.path.dirname (xmlpack.data), '{basename}.SYNC'.format (basename = os.path.basename (xmlpack.data).split ('.', 1)[0]))
					if os.path.isfile (sync):
						self.move (sync, target)
					
	def remove (self, path: Optional[str]) -> None:
		if path is not None:
			try:
				os.unlink (path)
				logger.debug (f'{path}: removed')
			except OSError as e:
				if os.path.isfile (path):
					logger.error (f'{path}: failed to remove: {e}')
					self.move (path, self.recover_directory)

	def move (self, source: Optional[str], target: str) -> None:
		if source is not None:
			if target != os.devnull:
				try:
					shutil.move (source, target)
					logger.debug (f'moved "{source}" to "{target}"')
				except Exception as e:
					logger.error (f'failed to move "{source}" to "{target}": {e}')
					if os.path.isfile (source):
						basename = os.path.basename (source)
						dirname = os.path.dirname (source)
						if not basename.startswith ('.'):
							new_target = os.path.join (dirname, f'.{basename}')
							if not os.path.isfile (new_target):
								try:
									shutil.move (source, new_target)
									logger.debug (f'moved "{source}" to "{new_target}"')
								except Exception as e:
									logger.error (f'failed to move "{source}" to "{new_target}": {e}')
						if os.path.isfile (source):
							os.rename (source, os.path.join (dirname, '.{basename}-{ts:.2f}'.format (basename = basename, ts = time.time ())))
			else:
				try:
					os.unlink (source)
					logger.debug ('removed "{source}" due to target is "{target}"')
				except OSError as e:
					logger.error (f'failed to remove "{source}" for target "{target}": {e}')

if __name__ == '__main__':
	DPath.main ()
