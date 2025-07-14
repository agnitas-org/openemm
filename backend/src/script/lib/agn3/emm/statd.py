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
import	asyncio
from	typing import Optional, Protocol
from	typing import List
from	typing import cast
from	..definitions import syscfg
from	..rpc import XMLRPCProxy, AIO_XMLRPCProxy, AIO_XMLRPCProtocol
#
__all__ = ['statd_proxy', 'aio_statd_proxy']
#
class StatdProxy (Protocol):
	def whoami (self) -> str: ...
	def get_last_error (self) -> str: ...
	def count_free (self) -> int: ...
	def count_incoming (self) -> int: ...
	def list_incoming (self) -> List[str]: ...
	def list_outgoing (self, licence: int, server: str, claim_all: bool = False) -> List[str]: ...
	def stat_file (self, fname: str) -> str: ...
	def proxy (self, contents: List[str]) -> List[str]: ...
class AIO_StatdProxy (AIO_XMLRPCProtocol, Protocol):
	async def whoami (self) -> str: ...
	async def get_last_error (self) -> str: ...
	async def count_free (self) -> int: ...
	async def count_incoming (self) -> int: ...
	async def list_incoming (self) -> List[str]: ...
	async def list_outgoing (self, licence: int, server: str, claim_all: bool = False) -> List[str]: ...
	async def stat_file (self, fname: str) -> str: ...
	async def proxy (self, contents: List[str]) -> List[str]: ...
	
def _target (hostname: str, user: Optional[str]) -> str:
	port = syscfg.get (f'statd-port-{user}') if user is not None else None
	if port is None:
		port = syscfg.get ('statd-port', '8300')
	parts = hostname.split (':', 1)
	if len (parts) == 2:
		(hostname, port) = parts
	return f'http://{hostname}:{port}'
	
def statd_proxy (hostname: str, user: Optional[str] = None) -> StatdProxy:
	"""provides a xml-rpc proxy to access the statd daemon

``hostname'': is the host to connect to
``port'': is the optional port to connect to (default 8300)"""
	return cast (StatdProxy, XMLRPCProxy (_target (hostname, user)))

def aio_statd_proxy (hostname: str, user: Optional[str] = None, loop: Optional[asyncio.AbstractEventLoop] = None) -> AIO_StatdProxy:
	"""provides an async xml-rpc proxy to access the statd daemon

``hostname'': is the host to connect to
``port'': is the optional port to connect to (default 8300)"""
	return cast (AIO_StatdProxy, AIO_XMLRPCProxy (_target (hostname, user), loop = loop))
