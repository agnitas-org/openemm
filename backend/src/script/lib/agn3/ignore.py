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
import	logging, time
import	asyncio
from	types import TracebackType
from	typing import Any, Callable, Optional, TypeVar, Union
from	typing import Coroutine, Type
from	typing import cast, overload
from	.stream import Stream
#
__all__ = ['Ignore', 'ignore', 'Experimental', 'Retry']
#
logger = logging.getLogger (__name__)
#
_T = TypeVar ('_T')
#
class Ignore:
	"""Context to ignore selected exceptions as a replacement for

try:
	...
except ...:
	pass

If no exception are specified, all exceptions are ignored and loglevel
is set to WARNING. This is general a bad idea and should be used with
care."""
	__slots__ = ['exceptions', 'on_exception', 'template', 'loglevel', 'logexception']
	def __init__ (self, *exceptions: Type[BaseException], **kwargs: Any) -> None:
		self.exceptions = exceptions
		self.on_exception: Optional[Callable[[Optional[Type[BaseException]], Optional[BaseException], Optional[TracebackType]], None]] = None
		do_log = kwargs.get ('log')
		if do_log or (do_log is None and ('template' in kwargs or 'loglevel' in kwargs or 'logexception' in kwargs)):
			self.on_exception = self.__on_exception
		self.template: str = kwargs.get ('template', 'caught and ignored: {exc_value}')
		self.loglevel: int = kwargs.get ('loglevel', logging.DEBUG)
		self.logexception: bool = kwargs.get ('logexception', False)
	
	def __str__ (self) -> str:
		return '{class_name} ({exceptions}, log = {log}, template = {template!r}, loglevel = {loglevel}, logexception = {logexception!r})'.format (
			class_name = self.__class__.__name__,
			exceptions = Stream (self.exceptions).join (', '),
			log = 'False' if self.on_exception is None else 'True',
			template = self.template,
			loglevel = logging.getLevelName (self.loglevel),
			logexception = self.logexception
		)

	def __enter__ (self) -> Ignore:
		return self
		
	def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
		if exc_type is not None:
			try:
				if not self.exceptions or exc_type in self.exceptions or len ([_e for _e in self.exceptions if issubclass (exc_type, _e)]) > 0:
					if self.on_exception is not None:
						self.on_exception (exc_type, exc_value, traceback)
					return True
			except Exception:
				logger.exception ('ignore: failed during expection reporting')
		return False

	def __on_exception (self,
		exc_type: Optional[Type[BaseException]],
		exc_value: Optional[BaseException],
		traceback: Optional[TracebackType]
	) -> None:
		logger.log (
			self.loglevel,
			self.template.format (
				exc_type = exc_type,
				exc_value = exc_value,
				traceback = traceback,
				self = self
			),
			exc_info = self.logexception
		)
		
def ignore (callback: Callable[[], Any], default: Any, *exceptions: Type[BaseException]) -> Any:
	with Ignore (*exceptions):
		return callback ()
	return default

class Experimental (Ignore):
	"""This is a derivated class from Ignore to provide a context
manager for experimental code. This could be used, if new code is
added to an already productive program to avoid abortion of the
program due to bugs in the new code. This should NOT be used, if
failure of the new code harms the result of the program.

Instead of passing excpetions, use a speaking name as an argument to
create an instance."""
	__slots__ = ['name']
	def __init__ (self, name: str) -> None:
		super ().__init__ (
			template = '{self.name}: failed in experimental code {exc_value}',
			loglevel = logging.ERROR,
			logexception = True
		)
		self.name = name

class Retry:
	"""Decorator to retry on exception
	
This decorator can either be applied to conventional or async
functions. It automatically retries the function, if an exception has
occured, either for all exceptions or a given list of exception
(including their subclasses). Keyword arguments control the behaviour:

- retries: number of retries (int, default 1)
- delay: optional delay between retries (None | int | float, default None)
- log: optional callback for logging in between failures which are otherwise ignored silently (None | funciton(exception, str), default None)

for example:

@Retry (retries = 3, delay = 2.0, log = lambda e, n: logger.warning (f'{n}: retry schedulued after exception: {e}'))
def function (...): ...

"""
	__slots__ = ['args', 'kwargs']
	default_retries: int = 1
	default_delay: Union[None, int, float] = None
	default_log: Optional[Callable[[BaseException, str], None]] = None
	def __init__ (self, *args: Type[BaseException], **kwargs: Any) -> None:
		self.args = args
		self.kwargs = kwargs

	@overload
	def __call__ (self, method: Callable[..., _T]) -> Callable[..., _T]: ...
	@overload
	def __call__ (self, method: Coroutine[Any, Any, _T]) -> Callable[..., Coroutine[Any, Any, _T]]: ...
	def __call__ (self, method: Union[Callable[..., _T], Coroutine[Any, Any, _T]]) -> Callable[..., Union[_T, Coroutine[Any, Any, _T]]]:
		retries = max (1, int (self.kwargs.get ('retries', self.default_retries)))
		delay: Union[None, int, float] = self.kwargs.get ('delay', self.default_delay)
		log: Optional[Callable[[BaseException, str], None]] = self.kwargs.get ('log', self.__class__.default_log)
		exceptions = self.args
		try:
			name = method.__name__
		except AttributeError:
			name = str (method)
		if asyncio.iscoroutinefunction (method):
			async def aiowrapper (*args: Any, **kwargs: Any) -> _T:
				for retry in range (retries):
					try:
						return await cast (Coroutine[Any, Any, _T], method (*args, **kwargs))
					except BaseException as e:
						if retry + 1 == retries or (exceptions and type (e) not in exceptions and not [_e for _e in exceptions if issubclass (type (e), _e)]):
							raise
						if log is not None:
							log (e, name)
					if retry + 1 < retries and delay is not None and delay > 0.0:
						await asyncio.sleep (delay)
				raise
			return aiowrapper
		else:
			def wrapper (*args: Any, **kwargs: Any) -> _T:
				for retry in range (retries):
					try:
						return cast (Callable[..., _T], method) (*args, **kwargs)
					except BaseException as e:
						if retry + 1 == retries or (exceptions and type (e) not in exceptions and not [_e for _e in exceptions if issubclass (type (e), _e)]):
							raise
						if log is not None:
							log (e, name)
					if retry + 1 < retries and delay is not None and delay > 0.0:
						time.sleep (delay)
				raise
			return wrapper
