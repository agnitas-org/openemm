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
import	sys, re
from	functools import partial, reduce
from	collections import Counter, Iterable, Iterator, deque, defaultdict
from	itertools import filterfalse, dropwhile, takewhile, islice, chain
from	types import TracebackType
from	typing import Any, Optional, Callable, Reversible, Sized, Union
from	typing import Dict, List, Match, Pattern, Set, Tuple, Type
from	typing import cast
import	typing

class Stream:
	"""Stream implementation as inspired by Java 1.8

Original based on pystreams but as this project seems to be abandoned
a subset of these methods are implemented here by giving up parallel
execution at all."""
	__slots__ = ['iterator']
	__sentinel = object ()
	@classmethod
	def of (cls, *args: Any) -> Stream:
		"""Creates a stream from an iterable object or a list of items"""
		if len (args) == 1:
			if args[0] is None:
				return cls (())
			try:
				return cls (args[0])
			except TypeError:
				pass
		elif len (args) > 1 and callable (args[0]):
			if len (args) > 2:
				method: Callable[[], Any] = partial (args[0], *args[1:-1])
			else:
				method = args[0]
			return cls (iter (method, args[-1]))
		return cls (args)
	
	@classmethod
	def defer (cls, obj: Iterable[Any], defer: Optional[Callable[[Iterable[Any]], None]] = None) -> Stream:
		"""Create a stream from an iterable ``obj'' and defer cleanup to the end"""
		def provider (obj: Iterable[Any], defer: Optional[Callable[[Iterable[Any]], None]]) -> Iterator[Any]:
			try:
				for elem in obj:
					yield elem
			finally:
				if defer is not None:
					defer (obj)
				else:
					del obj
		return cls.of (provider (obj, defer))

	@classmethod
	def empty (cls) -> Stream:
		"""Creates an empty stream"""
		return cls.of ()
	
	@classmethod
	def concat (cls, *args: Iterable[Any]) -> Stream:
		"""Create a new stream by concaternate all streams from ``args''"""
		return cls (args).chain ()
	
	@classmethod
	def merge (cls, *args: Any) -> Stream:
		"""Like concat, but use items which are not iterable as literal to the target stream"""
		return cls ((_a if isinstance (_a, (Iterable, Iterator)) else [_a] for _a in args)).chain ()
			
	@classmethod
	def range (cls, *args: Any, **kwargs: Any) -> Stream:
		"""Creaste a new stream using range (``args'' and ``kwargs'' are pased to xrange)"""
		return cls (range (*args, **kwargs))
	
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
		expression: Pattern[str] = re.compile (pattern, flags) if type (pattern) is str else cast (Pattern[str], pattern)
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
	
	@classmethod
	def multi (cls, *methods: Callable[..., Any]) -> Callable[..., Any]:
		"""execute multi methods and return the result of the last one, work around for lambda limitations"""
		def multier (*args: Any, **kwargs: Any) -> Any:
			return cls (methods).map (lambda m: m (*args, **kwargs)).last (no = None)
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
	
	def __init__ (self, iterator: Iterable[Any]) -> None:
		self.iterator = iter (iterator)

	def __str__ (self) -> str:
		return '%s <%s>' % (self.__class__.__name__, self.iterator)
	
	def __repr__ (self) -> str:
		return '%s (%r)' % (self.__class__.__name__, self.iterator)

	def __len__ (self) -> int:
		return self.count ()
		
	def __iter__ (self) -> Iterator[Any]:
		return self.iterator
		
	def __reversed__ (self) -> Iterator[Any]:
		try:
			return reversed (cast (Reversible[Any], self.iterator))
		except TypeError:
			return reversed (list (self.iterator))

	def __enter__ (self) -> Stream:
		return self

	def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
		return None
	
	def __contains__ (self, o: Any) -> bool:
		return sum ((1 for _o in self.iterator if _o == o)) > 0

	def new (self, iterator: Iterable[Any]) -> Stream:
		"""Create a new stream using ``iterator''"""
		return self.__class__ (iterator)
	#
	# Intermediates
	#
	def filter (self, predicate: Callable[[Any], bool]) -> Stream:
		"""Create a new stream for each element ``predicate'' returns True"""
		return self.new (filter (predicate, self.iterator))
		
	def exclude (self, predicate: Callable[[Any], bool]) -> Stream:
		"""Create a new stream excluding each element ``prdicate'' returns True"""
		return self.new (filterfalse (predicate, self.iterator))
		
	def regexp (self,
		pattern: Union[str, Pattern[str]],
		flags: int = 0,
		key: Optional[Callable[[Any], Any]] = None,
		predicate: Optional[Callable[[Pattern[str], Match[str], Any], Any]] = None) -> Stream:
		"""Create a new stream for each element matching
regular expression ``pattern''. ``flags'' is passed to re.compile. If
``predicate'' is not None, this must be a callable which accepts three
arguments, the compiled regular expression, the regular expression
matching object and the element itself."""
		expression = re.compile (pattern, flags) if type (pattern) is str else cast (Pattern[str], pattern)
		def regexper () -> Iterator[Any]:
			for elem in self.iterator:
				m = expression.match (key (elem) if key is not None else elem)
				if m is not None:
					yield predicate (expression, m, elem) if predicate is not None else elem
		return self.new (regexper ())
	
	def map (self, predicate: Callable[[Any], Any]) -> Stream:
		"""Create a new stream for each element mapped with ``predicate''"""
		return self.new ((predicate (_v) for _v in self.iterator))
	
	def distinct (self, key: Optional[Callable[[Any], Any]] = None) -> Stream:
		"""Create a new stream eleminating duplicate elements. If ``key'' is not None, it is used to build the key for checking identical elements"""
		def distincter () -> Iterator[Any]:
			seen: Set[Any] = set ()
			for elem in self.iterator:
				keyvalue = key (elem) if key is not None else elem
				if keyvalue not in seen:
					seen.add (keyvalue)
					yield elem
		return self.new (distincter ())
		
	def sorted (self, key: Optional[Callable[[Any], Any]] = None, reverse: bool = False) -> Stream:
		"""Create a new stream with sorted elements ``key'' and ``reverse'' are passed to sorted()"""
		return self.new (sorted (self.iterator, key = key, reverse = reverse))
		
	def reversed (self) -> Stream:
		"""Create a new stream in reverse order"""
		return self.new (reversed (self))
		
	def peek (self, predicate: Union[None, str, Callable[[Any], Any]] = None) -> Stream:
		"""Create a new stream while executing ``predicate'' for each element

If predicate is None or a string, then each object is printed to
stderr, if predicate is a string, the output is prefixed by this
string, otherwise with a generic 'Peek'"""
		if predicate is None or type (predicate) is str:
			format = '%s: %%r\n' % (predicate if predicate is not None else 'Peek', )
			predicater: Callable[[Any], Any] = lambda v: sys.stderr.write (format % (v, ))
		else:
			predicater = cast (Callable[[Any], Any], predicate)
		return self.filter (lambda v: predicater (v) or True)

	class Progress:
		__slots__: List[str] = []
		def tick (self, elem: Any) -> None:
			pass
		def final (self, count: int) -> None:
			pass
	
	def __progress (self, p: Union[str, Progress], checkpoint: Optional[int]) -> Progress:
		if type (p) is str:
			class Progress (Stream.Progress):
				__slots__ = ['name', 'checkpoint', 'count', 'shown']
				def __init__ (self, name: str, checkpoint: Optional[int] = None) -> None:
					self.name = name
					self.checkpoint: int = checkpoint if checkpoint is not None and checkpoint > 0 else 100
					self.count = 0
					self.shown = -1
				def show (self) -> None:
					if self.count != self.shown:
						sys.stderr.write (f'{self.name}: Now at {self.count:,d}')
						self.shown = self.count
				def tick (self, elem: Any) -> None:
					self.count += 1
					if self.count % self.checkpoint == 0:
						self.show ()
				def final (self, count: int) -> None:
					self.show ()
			progress = Progress (name = cast (str, p), checkpoint = checkpoint)
		else:
			progress = cast (Progress, p)
		return progress

	def progress (self, p: Union[str, Progress], checkpoint: Optional[int] = None) -> Stream:
		"""Create a new stream which copies the stream calling
the instance of ``p'' (an instance of agn.Stream.Progress or a string)
on each iteration. If ``p'' is a string, then ``checkpoint'' is an
optional integer value which specifies in which intervals the a
progression messsages is emitted'"""
		progress = self.__progress (p, checkpoint)
		def progressor () -> Iterator[Any]:
			count = 0
			for elem in self.iterator:
				count += 1
				progress.tick (elem)
				yield elem
			progress.final (count)
		return self.new (progressor ())

	def __functions (self, *args: Any) -> Tuple[List[Tuple[Callable[[Any], bool], Callable[[Any], Any]]], Optional[Callable[[Any], Any]]]:
		conditions: List[Tuple[Callable[[Any], bool], Callable[[Any], Any]]] = []
		while len (args) > 1:
			conditions.append ((args[0], args[1]))
			args = args[2:]
		return (conditions, args[0] if args else None)
		
	def switch (self, *args: Any) -> Stream:
		"""Create a new stream for using mulitple condition/mapping pairs.

If an odd number of arguments are passed, the last one is considered
as default mapping. Each pair is evaluated, if the condition returns a
true value, the mapping is executed and the return value of the
mapping is added to the new stream. No further mapping is applied. If
a default mapping is used and no other condition/mapping pair has
matched, this mapping is called for the element, otherwise the element
is added unmapped to the new stream."""
		(conditions, default) = self.__functions (*args)
		def switcher (elem: Any) -> Any:
			for (predicate, callback) in conditions:
				if predicate (elem):
					return callback (elem)
			return default (elem) if default else elem
		return self.new ((switcher (_e) for _e in self.iterator))
	
	def snap (self, target: List[Any]) -> Stream:
		"""Create a new stream saving each element in ``target'' (which must provide an append method)"""
		return self.peek (lambda v: target.append (v))
		
	def dropwhile (self, predicate: Callable[[Any], bool]) -> Stream:
		"""Create a new stream ignore all elements where ``predicate'' returns False up to first match"""
		return self.new (dropwhile (predicate, self.iterator))
	
	def takewhile (self, predicate: Callable[[Any], bool]) -> Stream:
		"""Create a new stream as long as ``predicate'' returns the first time False"""
		return self.new (takewhile (predicate, self.iterator))
	
	def limit (self, size: Optional[int]) -> Stream:
		"""Create a new stream with a maximum of ``size'' elements"""
		if size is None:
			return self.new (self.iterator)
		return self.new (islice (self.iterator, 0, size))
		
	def skip (self, size: Optional[int]) -> Stream:
		"""Create a new stream where the first ``size'' elements are skipped"""
		if size is None:
			return self.new (self.iterator)
		return self.new (islice (self.iterator, size, None))
	
	def remain (self, size: Optional[int]) -> Stream:
		"""Create a new stream which contains the remaining ``size'' elements"""
		if size is None:
			return self.new (self.iterator)
		return self.new (deque (self.iterator, maxlen = size))
	
	def slice (self, *args: int) -> Stream:
		"""Create a new stream selecting slice(*``args'')"""
		return self.new (islice (self.iterator, *args))
	
	def chain (self) -> Stream:
		"""Create a new stream flatten the elements of the stream."""
		return self.new (chain.from_iterable (self.iterator))
	#
	# Terminals
	#
	def dismiss (self) -> None:
		"""Dismiss all elements to terminate the stream"""
		deque (self.iterator, maxlen = 0)

	def reduce (self, predicate: Callable[[Any, Any], Any], identity: Any = __sentinel) -> Any:
		"""Reduce the stream by applying ``predicate''. If ``identity'' is available, use this as the initial value"""
		if identity is self.__sentinel:
			return reduce (predicate, self.iterator)
		return reduce (predicate, self.iterator, identity)

	def __checkNo (self, no: Any, where: str) -> Any:
		if no is self.__sentinel:
			raise ValueError ('no value available for Stream.%s: empty result set' % where)
		return no
		
	def __position (self, finisher:  Optional[Callable[[Any], Any]], no: Any, position: Callable[[Any], int], where: str) -> Any:
		collect: Dict[Any, int] = defaultdict (int)
		for elem in self.iterator:
			collect[elem] += 1
		if collect:
			value = (sorted (collect.items (), key = lambda a: a[1])[position (len (collect))])[0]
			return value if finisher is None else finisher (value)
		return self.__checkNo (no, where)

	def first (self, finisher: Optional[Callable[[Any], Any]] = None, no: Any = __sentinel) -> Any:
		"""Returns the first element, ``no'' if stream is empty. ``finisher'', if not None, is called on a found element"""
		try:
			rc = next (self.iterator)
			deque (self.iterator, maxlen = 0)
			return rc if finisher is None else finisher (rc)
		except StopIteration:
			return self.__checkNo (no, 'first')
	
	def last (self, finisher: Optional[Callable[[Any], Any]] = None, no: Any = __sentinel) -> Any:
		"""Returns the last element, ``no'' if stream is empty. ``finisher'', if not None, is called on a found element"""
		rc = deque (self.iterator, maxlen = 1)
		if len (rc):
			return rc[0] if finisher is None else finisher (rc[0])
		return self.__checkNo (no, 'last')

	def most (self, finisher: Optional[Callable[[Any], Any]] = None, no: Any = __sentinel) -> Any:
		"""Returns the element with the most often occurance, ``no'' if stream is empty. ``finisher'', if not None, is called on a found element"""
		return self.__position (finisher, no, lambda c: -1, 'most')
	
	def least (self, finisher: Optional[Callable[[Any], Any]] = None, no: Any = __sentinel) -> Any:
		"""Returns the element with the least often occurance, ``no'' if stream is empty. ``finisher'', if not None, is called on a found element"""
		return self.__position (finisher, no, lambda c: 0, 'least')
	
	def sum (self) -> int:
		"""Returns the sum of the stream"""
		return sum (self.iterator)
	
	def min (self, no: Any = __sentinel) -> Any:
		"""Returns the minimum value of the stream"""
		try:
			return min (self.iterator)
		except ValueError:
			return self.__checkNo (no, 'min')

	def max (self, no: Any = __sentinel) -> Any:
		"""Returns the maximum value of the stream"""
		try:
			return max (self.iterator)
		except ValueError:
			return self.__checkNo (no, 'max')
	
	def count (self, *args: Any) -> int:
		"""Without arguments returns the number of elements or return all elements which are part of ``args''"""
		if len (args) == 0:
			try:
				return len (cast (Sized, self.iterator))
			except TypeError:
				return sum ((1 for _ in self.iterator))
		return sum ((1 for _v in self.iterator if _v in args))
	
	def counter (self) -> typing.Counter[Any]:
		return Counter (self.iterator)
		
	def any (self, predicate: Callable[[Any], bool] = bool) -> bool:
		"""Return True if at least one element matches ``predicate''"""
		return sum ((1 for _v in self.iterator if predicate (_v))) > 0
		
	def all (self, predicate: Callable[[Any], bool] = bool) -> bool:
		"""Return True if all element match ``predicate''"""
		counter = [0]
		def predicate_and_count (v: Any) -> bool:
			counter[0] += 1
			return predicate (v)
		matches = sum ((1 for _v in self.iterator if predicate_and_count (_v)))
		return matches == counter[0]

	def each (self, predicate: Callable[[Any], Any]) -> None:
		"""Calls ``predicate'' on each element of the stream like java forEach()"""
		deque (filter (predicate, self.iterator), maxlen = 0)
	
	def dispatch (self, *args: Any, exclusive: bool = False) -> None:
		"""``args'' is a list of filter/handler functions

If the number of arguments is odd, then the last method is called if
no other pair had matched. For each pair the the filter method is
called and if it returns a boolean value, the handler is invoked and
the dispatching for this element ends. If the keyword argument
exclusive is False (the default), then each method where the filter
matches (and also the default method, if available) are called for
each element. """
		(conditions, default) = self.__functions (*args)
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
	
	def list (self) -> List[Any]:
		"""Returns the stream as a list like java asList()"""
		return list (self.iterator)

	def tuple (self) -> Tuple[Any, ...]:
		"""Returns the stream as a tuple"""
		return tuple (self.iterator)

	def set (self) -> Set[Any]:
		"""Returns the stream as set"""
		return set (self.iterator)
	
	def dict (self) -> Dict[Any, Any]:
		"""Returns the stream as a dictionary"""
		return dict (self.iterator)
	
	def deque (self) -> typing.Deque[Any]:
		"""Return the stream as collections.deque"""
		return deque (self.iterator)
	
	def group (self, predicate: Optional[Callable[[Any], Tuple[Any, Any]]] = None, finisher: Optional[Callable[[Dict[Any, List[Any]]], Any]] = None) -> Any:
		"""Returns a dict of grouped elements as separated by ``predicate'', optional modify the final dict by ``finisher''."""
		rc: Dict[Any, List[Any]] = defaultdict (list)
		if predicate is None:
			for (key, value) in self.iterator:
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
		accumulator: Optional[Callable[[Any, Any], Any]] = None,
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
		
				def accumulator (self, supplier: Any, element: Any) -> None:
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
