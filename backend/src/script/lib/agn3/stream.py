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
import	sys, re
from	functools import reduce
from	collections import Counter, Iterable, Iterator, deque, defaultdict
from	itertools import filterfalse, dropwhile, takewhile, islice, chain
from	types import TracebackType
from	typing import Any, Callable, Optional, Reversible, Sized, TypeVar, Union
from	typing import DefaultDict, Dict, Generic, List, Match, Pattern, Set, Tuple, Type
from	typing import cast, overload
import	typing
from	.exceptions import error
#
__all__ = ['Stream']
#
T = TypeVar ('T')
O = TypeVar ('O')
#
class Stream (Generic[T]):
	"""Stream implementation as inspired by Java 1.8

Original based on pystreams but as this project seems to be abandoned
a subset of these methods are implemented here by giving up parallel
execution at all."""
	__slots__ = ['iterator']
	__sentinel = cast (T, object ())
	@classmethod
	def defer (cls, obj: Iterable[T], defer: Optional[Callable[[Iterable[T]], None]] = None) -> Stream[T]:
		"""Create a stream from an iterable ``obj'' and defer cleanup to the end"""
		def provider (obj: Iterable[T], defer: Optional[Callable[[Iterable[T]], None]]) -> Iterator[T]:
			try:
				for elem in obj:
					yield elem
			finally:
				if defer is not None:
					defer (obj)
				else:
					del obj
		return cls (provider (obj, defer))

	@classmethod
	def concat (cls, *args: Iterable[T]) -> Stream[T]:
		def concater (args: Iterable[Iterable[T]]) -> Iterator[T]:
			for element in args:
				for subelement in element:
					yield subelement
		return cls (concater (args))

	@classmethod
	def merge (cls, *args: Union[T, Iterable[T]]) -> Stream[T]:
		"""Like concat, but use items which are not iterable as literal to the target stream"""
		def merger (args: Tuple[Union[Iterable[T], T], ...]) -> Iterator[T]:
			for element in args:
				if isinstance (element, Iterable):
					for subelement in element:
						yield subelement
				else:
					yield element
		return cls (merger (args))
			
	@classmethod
	def range (cls, *args: Any, **kwargs: Any) -> Stream[int]:
		"""Creaste a new stream using range (``args'' and ``kwargs'' are pased to range)"""
		return cast (Type[Stream[int]], cls) (range (*args, **kwargs))
	
	@staticmethod
	def ifelse (value: Any,	predicate: Optional[Callable[[Any], bool]] = None, mapper: Any = None, alternator: Any = None) -> Any:
		"""if/then/else like for single values in the spirit of streams"""
		if predicate (value) if predicate is not None else bool (value):
			if mapper is None:
				return value
			elif callable (mapper):
				return mapper (value)
			return mapper
		if callable (alternator):
			return alternator (value)
		return alternator
	
	@staticmethod
	def loop (supplier: Any, predicate: Callable[[Any], bool], executor: Callable[[Any], Any], finalizer: Optional[Callable[[Any], Any]] = None, repeat_until: bool = False) -> Any:
		"""while/do or repeat/until like loop in the spirit of streams"""
		value = supplier () if callable (supplier) else supplier
		while repeat_until or predicate (value):
			repeat_until = False
			value = executor (value)
		return finalizer (value) if finalizer is not None else value
	
	@staticmethod
	def reloop (pattern: Union[str, Pattern[str]], s: str, flags: int = 0, position: int = 1, finalizer: Optional[Callable[[Any], Any]] = None) -> Any:
		"""loop to find last occurance of nested regex"""
		expression: Pattern[str] = re.compile (pattern, flags) if isinstance (pattern, str) else pattern
		while s:
			m = expression.search (s)
			if m is None:
				break
			ns = m.group (position)
			if ns == s:
				break
			s = ns
		return finalizer (s) if finalizer is not None else s
	
	@staticmethod
	def execute (supplier: Any, predicate: Callable[[Any], bool], *executor_list: Callable[[Any], Any], finalizer: Optional[Callable[[Any], Any]] = None) -> Any:
		value = supplier () if callable (supplier) else supplier
		executors = list (executor_list)
		while predicate (value) and executors:
			value = (executors.pop (0)) (value)
		return finalizer (value) if finalizer is not None else value
	
	@staticmethod
	def multi (*methods: Callable[..., Any]) -> Callable[..., Any]:
		"""execute multi methods and return the result of the last one, work around for lambda limitations"""
		def multier (*args: Any, **kwargs: Any) -> Any:
			value = None
			for method in methods:
				value = method (*args, **kwargs)
			return value
		return multier
	
	@staticmethod
	def multichain (*methods: Callable[..., Any]) -> Callable[..., Any]:
		"""execute multi methods and feeding the previous
return value as first argument to next method (initial this is None),
returning the final value"""
		def multichainer (*args: Any, **kwargs: Any) -> Any:
			value = None
			for method in methods:
				value = method (value, *args, **kwargs)
			return value
		return multichainer
	
	def __init__ (self, iterator: Iterable[T]) -> None:
		self.iterator = iter (iterator)

	def __str__ (self) -> str:
		return f'{self.__class__.__name__} < {self.iterator} >'
	
	def __repr__ (self) -> str:
		return f'{self.__class__.__name__} ({self.iterator!r})'

	def __len__ (self) -> int:
		return self.count ()
		
	def __iter__ (self) -> Iterator[T]:
		return self.iterator
		
	def __reversed__ (self) -> Iterator[T]:
		try:
			return reversed (cast (Reversible[T], self.iterator))
		except TypeError:
			return reversed (list (self.iterator))

	def __enter__ (self) -> Stream[T]:
		return self

	def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
		return None
	
	def __contains__ (self, o: T) -> bool:
		return sum ((1 for _o in self.iterator if _o == o)) > 0

	def new (self, iterator: Iterable[O]) -> Stream[O]:
		"""Create a new stream using ``iterator''"""
		return cast (Type[Stream[O]], self.__class__) (iterator)
	#
	# Intermediates
	#
	def filter (self, predicate: Callable[[T], bool]) -> Stream[T]:
		"""Create a new stream for each element ``predicate'' returns True"""
		return self.new (filter (predicate, self.iterator))
		
	def exclude (self, predicate: Callable[[T], bool]) -> Stream[T]:
		"""Create a new stream excluding each element ``prdicate'' returns True"""
		return self.new (filterfalse (predicate, self.iterator))
		
	def error (self,
		predicate: Callable[[T], bool],
		exception: Union[None, str, Exception, Callable[[T], Exception]] = None
	) -> Stream[T]:
		"""Raise an error, if ``predicate'' returns False"""
		def check_for_error (e: Any) -> bool:
			if predicate (e):
				if exception is not None:
					if isinstance (exception, str):
						if '{element}' in exception:
							raise error (exception.format (element = e))
						raise error (exception)
					elif isinstance (exception, Exception):
						raise exception
					raise exception (e)
				raise error (str (e))
			return True
		#
		return self.new (filter (check_for_error, self.iterator))

	def regexp (self,
		pattern: Union[str, Pattern[str]],
		flags: int = 0,
		key: Optional[Callable[[T], str]] = None,
		predicate: Optional[Callable[[Pattern[str], Match[str], T], T]] = None
	) -> Stream[T]:
		"""Create a new stream for each element matching
regular expression ``pattern''. ``flags'' is passed to re.compile. If
``predicate'' is not None, this must be a callable which accepts three
arguments, the compiled regular expression, the regular expression
matching object and the element itself."""
		expression = re.compile (pattern, flags) if isinstance (pattern, str) else pattern
		def regexper () -> Iterator[T]:
			for elem in self.iterator:
				m = expression.match (key (elem) if key is not None else str (elem))
				if m is not None:
					yield predicate (expression, m, elem) if predicate is not None else elem
		return self.new (regexper ())
	
	def map (self, predicate: Callable[[T], O]) -> Stream[O]:
		"""Create a new stream for each element mapped with ``predicate''"""
		return self.new ((predicate (_v) for _v in self.iterator))
	
	def map_to (self, t: Type[O], predicate: Callable[[T], Any]) -> Stream[O]:
		"""Like map, but passing a type as a hint for the return type of predicate for type checking"""
		return self.new ((cast (O, predicate (_v)) for _v in self.iterator))
	
	def distinct (self, key: Optional[Callable[[T], Any]] = None) -> Stream[T]:
		"""Create a new stream eleminating duplicate elements. If ``key'' is not None, it is used to build the key for checking identical elements"""
		def distincter () -> Iterator[T]:
			seen: Set[Any] = set ()
			for elem in self.iterator:
				keyvalue = key (elem) if key is not None else elem
				if keyvalue not in seen:
					seen.add (keyvalue)
					yield elem
		return self.new (distincter ())

	def gather (self) -> Stream[T]:
		"""Gather all input elemnts into a list before further processing, useful if the source is modified i a later stage of the stream"""
		return self.new (list (self.iterator))

	def sorted (self, key: Optional[Callable[[T], Any]] = None, reverse: bool = False) -> Stream[T]:
		"""Create a new stream with sorted elements ``key'' and ``reverse'' are passed to sorted()"""
		return self.new (sorted (cast (Iterable, self.iterator), key = key, reverse = reverse))
		
	def reversed (self) -> Stream[T]:
		"""Create a new stream in reverse order"""
		return self.new (reversed (self))
		
	def peek (self, predicate: Union[None, str, Callable[[T], Any]] = None) -> Stream[T]:
		"""Create a new stream while executing ``predicate'' for each element

If predicate is None or a string, then each object is printed to
stderr, if predicate is a string, the output is prefixed by this
string, otherwise with a generic 'Peek'"""
		if predicate is None or isinstance (predicate, str):
			format = '{id}: {{value!r}}\n'.format (id = predicate if predicate is not None else 'Peek')
			predicater: Callable[[Any], Any] = lambda v: sys.stderr.write (format.format (value = v))
		else:
			predicater = predicate
		return self.filter (lambda v: predicater (v) or True)
	
	class Progress:
		__slots__: List[str] = []
		def tick (self, elem: Any) -> None:
			pass
		def final (self, count: int) -> None:
			pass
	
	def __progress (self, p: Union[str, Stream.Progress], checkpoint: Optional[int]) -> Stream.Progress:
		if isinstance (p, str):
			class Progress (Stream.Progress):
				__slots__ = ['name', 'checkpoint', 'count', 'shown']
				def __init__ (self, name: str, checkpoint: Optional[int] = None) -> None:
					self.name = name
					self.checkpoint: int = checkpoint if checkpoint is not None and checkpoint > 0 else 100
					self.count = 0
					self.shown = -1
				def show (self) -> None:
					if self.count != self.shown:
						sys.stderr.write (f'{self.name}: Now at {self.count:,d}\n')
						self.shown = self.count
				def tick (self, elem: Any) -> None:
					self.count += 1
					if self.count % self.checkpoint == 0:
						self.show ()
				def final (self, count: int) -> None:
					self.show ()
			progress: Stream.Progress = Progress (name = p, checkpoint = checkpoint)
		else:
			progress = p
		return progress

	def progress (self, p: Union[str, Stream.Progress], checkpoint: Optional[int] = None) -> Stream[T]:
		"""Create a new stream which copies the stream calling
the instance of ``p'' (an instance of Stream.Progress or a string)
on each iteration. If ``p'' is a string, then ``checkpoint'' is an
optional integer value which specifies in which intervals the a
progression messsages is emitted'"""
		progress = self.__progress (p, checkpoint)
		def progressor () -> Iterator[T]:
			count = 0
			for elem in self.iterator:
				count += 1
				progress.tick (elem)
				yield elem
			progress.final (count)
		return self.new (progressor ())

	def __functions (self,
		condition: Callable[[T], bool],
		modifier: Callable[[T], O],
		*args: Union[Callable[[T], bool], Callable[[T], O]]
	) -> Tuple[List[Tuple[Callable[[T], bool], Callable[[T], O]]], Optional[Callable[[T], O]]]:
		conditions: List[Tuple[Callable[[T], bool], Callable[[T], O]]] = [(condition, modifier)]
		while len (args) > 1:
			conditions.append ((cast (Callable[[T], bool], args[0]), cast (Callable[[T], O], args[1])))
			args = args[2:]
		return (conditions, cast (Callable[[T], O], args[0]) if args else None)

	def switch (self,
		condition: Callable[[T], bool],
		modifier: Callable[[T], O],
		*args: Union[Callable[[T], bool], Callable[[T], O]]
	) -> Stream[O]:
		"""Create a new stream for using mulitple condition/mapping pairs.

If an odd number of arguments are passed, the last one is considered
as default mapping. Each pair is evaluated, if the condition returns a
true value, the mapping is executed and the return value of the
mapping is added to the new stream. No further mapping is applied. If
a default mapping is used and no other condition/mapping pair has
matched, this mapping is called for the element, otherwise the element
is added unmapped to the new stream."""
		(conditions, default) = self.__functions (condition, modifier, *args)
		def switcher (elem: Any) -> Any:
			for (predicate, callback) in conditions:
				if predicate (elem):
					return callback (elem)
			return default (elem) if default else elem
		return self.new ((switcher (_e) for _e in self.iterator))
	
	def snap (self, target: List[T]) -> Stream[T]:
		"""Create a new stream saving each element in ``target'' (which must provide an append method)"""
		return self.peek (lambda v: target.append (v))
		
	def dropwhile (self, predicate: Callable[[T], bool]) -> Stream[T]:
		"""Create a new stream ignore all elements where ``predicate'' returns False up to first match"""
		return self.new (dropwhile (predicate, self.iterator))
	
	def takewhile (self, predicate: Callable[[T], bool]) -> Stream[T]:
		"""Create a new stream as long as ``predicate'' returns the first time False"""
		return self.new (takewhile (predicate, self.iterator))
	
	def limit (self, size: Optional[int]) -> Stream[T]:
		"""Create a new stream with a maximum of ``size'' elements"""
		if size is None:
			return self.new (self.iterator)
		return self.new (islice (self.iterator, 0, size))
		
	def skip (self, size: Optional[int]) -> Stream[T]:
		"""Create a new stream where the first ``size'' elements are skipped"""
		if size is None:
			return self.new (self.iterator)
		return self.new (islice (self.iterator, size, None))
	
	def remain (self, size: Optional[int]) -> Stream[T]:
		"""Create a new stream which contains the remaining ``size'' elements"""
		if size is None:
			return self.new (self.iterator)
		return self.new (deque (self.iterator, maxlen = size))
	
	def slice (self, *args: int) -> Stream[T]:
		"""Create a new stream selecting slice(*``args'')"""
		return self.new (islice (self.iterator, *args))
	
	def chain (self, t: Type[O]) -> Stream[O]:
		"""Create a new stream flatten the elements of the stream."""
		return self.new (chain.from_iterable (self.iterator))
	#
	# Terminals
	#
	def dismiss (self) -> None:
		"""Dismiss all elements to terminate the stream"""
		deque (self.iterator, maxlen = 0)

	def reduce (self, predicate: Callable[[T, T], T], identity: T = __sentinel) -> T:
		"""Reduce the stream by applying ``predicate''. If ``identity'' is available, use this as the initial value"""
		if identity is self.__sentinel:
			return reduce (predicate, self.iterator)
		return reduce (predicate, self.iterator, identity)

	def __checkNo (self, no: Any, where: str) -> Any:
		if no is self.__sentinel:
			raise ValueError (f'no value available for Stream.{where}: empty result set')
		return no
		
	def __position (self, finisher: Optional[Callable[[T], Any]], no: Any, position: Callable[[int], int], where: str) -> Any:
		collect: DefaultDict[Any, int] = defaultdict (int)
		for elem in self.iterator:
			collect[elem] += 1
		if collect:
			value = (sorted (collect.items (), key = lambda a: a[1])[position (len (collect))])[0]
			return value if finisher is None else finisher (value)
		return self.__checkNo (no, where)

	@overload
	def first (self, finisher: None = ..., consume: bool = ..., no: T = ...) -> T: ...
	@overload
	def first (self, finisher: None = ..., consume: bool = ..., no: Any = ...) -> Any: ...
	@overload
	def first (self, finisher: Callable[[T], T], consume: bool = ..., no: T = ...) -> T: ...
	@overload
	def first (self, finisher: Callable[[T], O], consume: bool = ..., no: O = ...) -> O: ...
	def first (self, finisher: Optional[Callable[[T], Any]] = None, consume: bool = True, no: Any = __sentinel) -> Any:
		"""Returns the first element, ``no'' if stream is empty. ``finisher'', if not None, is called on a found element"""
		try:
			rc = next (self.iterator)
			if consume:
				deque (self.iterator, maxlen = 0)
			return rc if finisher is None else finisher (rc)
		except StopIteration:
			return self.__checkNo (no, 'first')

	@overload
	def last (self, finisher: None = ..., no: T = ...) -> T: ...
	@overload
	def last (self, finisher: None = ..., no: Any = ...) -> Any: ...
	@overload
	def last (self, finisher: Callable[[T], T], no: T = ...) -> T: ...
	@overload
	def last (self, finisher: Callable[[T], O], no: O = ...) -> O: ...
	def last (self, finisher: Optional[Callable[[T], Any]] = None, no: Any = __sentinel) -> Any:
		"""Returns the last element, ``no'' if stream is empty. ``finisher'', if not None, is called on a found element"""
		rc = deque (self.iterator, maxlen = 1)
		if len (rc):
			return rc[0] if finisher is None else finisher (rc[0])
		return self.__checkNo (no, 'last')

	@overload
	def most (self, finisher: None = ..., no: T = ...) -> T: ...
	@overload
	def most (self, finisher: None = ..., no: Any = ...) -> Any: ...
	@overload
	def most (self, finisher: Callable[[T], T], no: T = ...) -> T: ...
	@overload
	def most (self, finisher: Callable[[T], O], no: O = ...) -> O: ...
	def most (self, finisher: Optional[Callable[[T], Any]] = None, no: Any = __sentinel) -> Any:
		"""Returns the element with the most often occurance, ``no'' if stream is empty. ``finisher'', if not None, is called on a found element"""
		return self.__position (finisher, no, lambda c: -1, 'most')
	
	@overload
	def least (self, finisher: None = ..., no: T = ...) -> T: ...
	@overload
	def least (self, finisher: None = ..., no: Any = ...) -> Any: ...
	@overload
	def least (self, finisher: Callable[[T], T], no: T = ...) -> T: ...
	@overload
	def least (self, finisher: Callable[[T], O], no: O = ...) -> O: ...
	def least (self, finisher: Optional[Callable[[T], Any]] = None, no: Any = __sentinel) -> Any:
		"""Returns the element with the least often occurance, ``no'' if stream is empty. ``finisher'', if not None, is called on a found element"""
		return self.__position (finisher, no, lambda c: 0, 'least')
	
	def sum (self, start: Optional[int] = None) -> int:
		"""Returns the sum of the stream"""
		if start is not None:
			return sum (cast (Iterable, self.iterator), start)
		return sum (cast (Iterable, self.iterator))

	@overload
	def min (self, no: T = ...) -> T: ...
	@overload
	def min (self, no: None = ...) -> Optional[T]: ...
	def min (self, no: Any = __sentinel) -> Any:
		"""Returns the minimum value of the stream"""
		try:
			return min (cast (Iterable, self.iterator))
		except ValueError:
			return self.__checkNo (no, 'min')

	@overload
	def max (self, no: T = ...) -> T: ...
	@overload
	def max (self, no: None = ...) -> Optional[T]: ...
	def max (self, no: Any = __sentinel) -> Any:
		"""Returns the maximum value of the stream"""
		try:
			return max (cast (Iterable, self.iterator))
		except ValueError:
			return self.__checkNo (no, 'max')
	
	def count (self, *args: T) -> int:
		"""Without arguments returns the number of elements or return all elements which are part of ``args''"""
		if len (args) == 0:
			try:
				return len (cast (Sized, self.iterator))
			except TypeError:
				return sum ((1 for _ in self.iterator))
		return sum ((1 for _v in self.iterator if _v in args))
	
	def counter (self) -> typing.Counter[T]:
		return Counter (self.iterator)
		
	def any (self, predicate: Callable[[T], bool] = bool) -> bool:
		"""Return True if at least one element matches ``predicate''"""
		return sum ((1 for _v in self.iterator if predicate (_v))) > 0
		
	def all (self, predicate: Callable[[T], bool] = bool) -> bool:
		"""Return True if all element match ``predicate''"""
		counter = [0]
		def predicate_and_count (v: Any) -> bool:
			counter[0] += 1
			return predicate (v)
		matches = sum ((1 for _v in self.iterator if predicate_and_count (_v)))
		return matches == counter[0]

	def each (self, predicate: Callable[[T], Any]) -> None:
		"""Calls ``predicate'' on each element of the stream like java forEach()"""
		deque (filter (predicate, self.iterator), maxlen = 0)
	
	def dispatch (self,
		condition: Callable[[T], bool],
		modifier: Callable[[T], O],
		*args: Union[Callable[[T], bool], Callable[[T], O]],
		exclusive: bool = False
	) -> None:
		"""``args'' is a list of filter/handler functions

If the number of arguments is odd, then the last method is called if
no other pair had matched. For each pair the the filter method is
called and if it returns a boolean value, the handler is invoked and
the dispatching for this element ends. If the keyword argument
exclusive is False (the default), then each method where the filter
matches (and also the default method, if available) are called for
each element. """
		(conditions, default) = self.__functions (condition, modifier, *args)
		def dispatcher (elem: Any) -> bool:
			for (predicate, callback) in conditions:
				if predicate (elem):
					callback (elem)
					if exclusive:
						return False
			if default:
				default (elem)
			return False
		deque (filter (dispatcher, self.iterator), maxlen = 0)
	
	def list (self) -> List[T]:
		"""Returns the stream as a list like java asList()"""
		return list (self.iterator)

	def tuple (self) -> Tuple[T, ...]:
		"""Returns the stream as a tuple"""
		return tuple (self.iterator)

	def set (self) -> Set[T]:
		"""Returns the stream as set"""
		return set (self.iterator)
	
	def dict (self) -> Dict[Any, Any]:
		"""Returns the stream as a dictionary"""
		return dict (cast (Iterable, self.iterator))
	
	def deque (self) -> typing.Deque[T]:
		"""Return the stream as collections.deque"""
		return deque (self.iterator)
	
	def group (self, predicate: Optional[Callable[[Any], Tuple[Any, Any]]] = None, finisher: Optional[Callable[[Dict[Any, List[Any]]], Any]] = None) -> Any:
		"""Returns a dict of grouped elements as separated by ``predicate'', optional modify the final dict by ``finisher''."""
		rc: DefaultDict[Any, List[Any]] = defaultdict (list)
		if predicate is None:
			for (key, value) in cast (Iterable, self.iterator):
				rc[key].append (value)
		else:
			for elem in self.iterator:
				(key, value) = predicate (elem)
				rc[key].append (value)
		return rc if finisher is None else finisher (rc)

	def join (self, separator: str = '', finisher: Optional[Callable[[str], str]] = None) -> str:
		"""Returns a string joining all elements of stream with separator, optional apply ``finisher'' on result, if not None"""
		rc = separator.join ((str (_s) for _s in self.iterator))
		return rc if finisher is None else finisher (rc)
	
	class Collector:
		__slots__: List[str] = []
		def supplier (self) -> Any:
			return self
		
		def accumulator (self, supplier: Any, element: Any) -> None:
			pass
		
		def finisher (self, supplier: Any, count: int) -> Any:
			return supplier
			
	def collect (self,
		supplier: Any,
		accumulator: Optional[Callable[[Any, T], None]] = None,
		finisher: Optional[Callable[[Any, int], Any]] = None,
		progress: Union[None, str, Stream.Progress] = None,
		checkpoint: Optional[int] = None
	) -> Any:
		"""A generic terminal like the java collect().
``supplier'' is called for the initial element (or, if not callable,
as the inital element), ``accumulator'' is called with the supplied
value and each element of the stream and the optional ``finisher'' is
called with the supplied value and the number of elements processed to
create the final result

Optional ``supplier'' may be an instance of a subclass of
``Stream.Collector'' and its methods supplier, accumulator and
finisher are used instead. This is useful if you need some context
during collecting.

Optional ``progress'' is used to display the progression of the
collection where ``checkpoint'' definies the interval of showing the
progression. See method ``progress'' for further details."""
		if isinstance (supplier, Stream.Collector):
			collector = supplier
		else:
			class Collector (Stream.Collector):
				__slots__: List[str] = []
				def supplier (self) -> Any:
					return supplier () if callable (supplier) else supplier
		
				def accumulator (self, supplier: Any, element: T) -> None:
					if accumulator is not None:
						accumulator (supplier, element)
		
				def finisher (self, supplier: Any, count: int) -> Any:
					return supplier if finisher is None else finisher (supplier, count)
			collector = Collector ()
		s = collector.supplier ()
		progressor = self.__progress (progress, checkpoint) if progress is not None else None
		counter = 0
		for elem in self.iterator:
			collector.accumulator (s, elem)
			counter += 1
			if progressor:
				progressor.tick (elem)
		if progressor:
			progressor.final (counter)
		return collector.finisher (s, counter)

	def collect_to (self,
		t: Type[O],
		supplier: Any,
		accumulator: Optional[Callable[[Any, T], None]] = None,
		finisher: Optional[Callable[[Any, int], O]] = None,
		progress: Union[None, str, Stream.Progress] = None,
		checkpoint: Optional[int] = None
	) -> O:
		"""Like collect, but passing a type as a hint for the return type for type checking"""
		return cast (O, self.collect (supplier, accumulator, finisher, progress, checkpoint))
