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
import	socket
import	asyncio
from	types import TracebackType
from	typing import Protocol
from	typing import Type
from	typing import cast
from	..definitions import syscfg
from	..parameter import MailingParameter
from	..rpc import XMLRPCProxy, AIO_XMLRPCProxy, AIO_XMLRPCProtocol
from	..sentinel import Sentinel, sentinel
#
__all__ = ['merger_proxy', 'aio_merger_proxy']
#
class MergerProxy (Protocol):
	class Merger (Protocol):
		@staticmethod
		def remote_control (command: str, parameter: str) -> str: ...
		@staticmethod
		def preview (parameter: str) -> str: ...

class AIO_MergerProxy (AIO_XMLRPCProtocol, Protocol):
	class Merger (Protocol):
		@staticmethod
		async def remote_control (command: str, parameter: str) -> str: ...
		@staticmethod
		async def preview (parameter: str) -> str: ...

def _target (hostname: None | str, port: None | int) -> str:
	return 'http://{host}:{port}'.format (
		host = hostname if hostname is not None else syscfg.get ('mailout-server', 'localhost'),
		port = port if port is not None else syscfg.iget ('mailout-port', 8089)
	)
	
def merger_proxy (hostname: None | str = None, port: None | int = None) -> MergerProxy:
	return cast (MergerProxy, XMLRPCProxy (_target (hostname, port)))

def aio_merger_proxy (hostname: None | str = None, port: None | int = None, loop: None | asyncio.AbstractEventLoop = None) -> AIO_MergerProxy:
	return cast (AIO_MergerProxy, AIO_XMLRPCProxy (_target (hostname, port), loop = loop))

class Merger:
	__slots__ = ['hostname', 'port', 'timeout', 'default_timeout']
	def __init__ (self, hostname: None | str = None, port: None | int = None, timeout: None | int | float | Sentinel = sentinel) -> None:
		self.hostname = hostname
		self.port = port
		self.timeout: None | int | float = syscfg.eget ('mailout-timeout', 120) if isinstance (timeout, Sentinel) else timeout
		self.default_timeout: None | int | float = None
	
	def __enter__ (self) -> MergerProxy:
		self.default_timeout = socket.getdefaulttimeout ()
		socket.setdefaulttimeout (self.timeout)
		return merger_proxy (self.hostname, self.port)
	
	def __exit__ (self, exc_type: None | Type[BaseException], exc_value: None | BaseException, traceback: None | TracebackType) -> None | bool:
		socket.setdefaulttimeout (self.default_timeout)
		return None

	def aio (self) -> Merger.AIOProxy:
		return Merger.AIOProxy (self)
		
	class AIOProxy:
		__slots__ = ['ref', 'proxy']
		def __init__ (self, ref: Merger) -> None:
			self.ref = ref
			self.proxy: AIO_MergerProxy
		
		def __enter__ (self) -> Merger.AIOProxy:
			return self
			
		def __exit__ (self, exc_type: None | Type[BaseException], exc_value: None | BaseException, traceback: None | TracebackType) -> None | bool:
			return None
			
		async def __aenter__ (self) -> AIO_MergerProxy:
			self.proxy = aio_merger_proxy (self.ref.hostname, self.ref.port)
			return self.proxy
	
		async def __aexit__ (self, exc_type: None | Type[BaseException], exc_value: None |  BaseException, traceback: None | TracebackType) -> None | bool:
			try:
				await self.proxy.close ()
			finally:
				del self.proxy
			return None
		
		async def fire (self, status_id: int, parameter: None | dict[str, str] = None) -> str:
			async with self as proxy:
				coro = proxy.Merger.remote_control ('fire', self.ref._fire_parameter (status_id, parameter))
				return await (asyncio.wait_for (coro, timeout = self.ref.timeout) if self.ref.timeout is not None else coro)
		
		async def preview (self, mailing_id: int, parameter: None | dict[str, str] = None) -> str:
			async with self as proxy:
				coro = proxy.Merger.preview (self.ref._preview_parameter (mailing_id, parameter))
				return await (asyncio.wait_for (coro, timeout = self.ref.timeout) if self.ref.timeout is not None else coro)
			
	
	def fire (self, status_id: int, parameter: None | dict[str, str] = None) -> str:
		with self as proxy:
			return proxy.Merger.remote_control ('fire', self._fire_parameter (status_id, parameter))
		
	def preview (self, mailing_id: int, parameter: None | dict[str, str] = None) -> str:
		with self as proxy:
			return proxy.Merger.preview (self._preview_parameter (mailing_id, parameter))
		
	mailing_parameter = MailingParameter ()
	def _fire_parameter (self, status_id: int, parameter: None | dict[str, str]) -> str:
		if parameter:
			self.mailing_parameter.set (parameter)
			return '{status_id} {parameter}'.format (
				status_id = status_id,
				parameter = self.mailing_parameter.dumps ()
			)
		return str (status_id)
	
	def _preview_parameter (self, mailing_id: int, parameter: None | dict[str, str]) -> str:
		self.mailing_parameter.set ({
			'mailing-id': str (mailing_id)
		})
		if parameter:
			self.mailing_parameter.update (parameter)
		return self.mailing_parameter.dumps ()
