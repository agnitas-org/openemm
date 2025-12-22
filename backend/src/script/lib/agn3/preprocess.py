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
import	re
from	typing import Callable
from	typing import NamedTuple
#
__all__ = ['preprocess', 'Preprocess']
#
Callback = Callable[[str, str, None | str, str], None | str]
#
def preprocess (
	source: str,
	definitions: dict[str, int | str],
	*,
	filename: str = '<undef>',
	prefix: str = '#',
	expand: bool = True,
	modifier: None | Callable[[str], None | str] = None,
	callback: None | Callback = None
) -> str:
	class State (NamedTuple):
		enable: bool = True
		matched: bool = False
		final: bool = False
	#
	def defined (definition: str) -> bool:
		return definition in definitions
	#
	definition_parser = re.compile ('[a-z_][a-z0-9_]*|\\|\\||&&|.', re.IGNORECASE)
	def matcher (logid: str, token: str, option: None | str, by_definition: bool, by_expression: bool, invert: bool) -> bool:
		if option is None:
			raise ValueError (f'{token}: missing expression')
		if by_definition:
			expression = ' '.join ([
				_m if _m in {' ', '\t', '(', ')'} else {
					'!':    'not',
					'||':   'or',
					'&&':   'and'
				}.get (_m, f'defined ({_m!r})')
				for _m in definition_parser.findall (option)
			])
		elif by_expression:
			expression = option
		else:
			raise ValueError (f'{logid}: {token}: missing matcher method')
		return invert ^ bool (eval (expression, {
			'defined': defined
		} | definitions))
	#
	output: list[str] = []
	if modifier is not None:
		def save (chunk: str) -> None:
			for line in chunk.split ('\n'):
				if (update := modifier (line)) is not None:
					output.append (update)
	else:
		def save (chunk: str) -> None:
			output.append (chunk)
	#
	conditions: list[State] = []
	current = State ()
	line_pattern = re.compile (f'^{prefix}\\s*(.*)$')
	macro_pattern = re.compile ('\\$(\\([^)]+\\)|\\$)')
	token: str
	option: None | str
	for (lineno, line) in enumerate (source.split ('\n'), start = 1):
		if (mtch := line_pattern.match (line)) is not None:
			logid = f'{filename}:{lineno}:{line}'
			if not (statement := mtch.group (1)):
				(token, option) = ('', None)
			else:
				try:
					(token, option) = statement.split (None, 1)
				except ValueError:
					(token, option) = (statement.strip (), None)
			match token:
				case 'stop' | 'abort':
					if current.enable:
						break
				case 'ifdef' | 'ifndef' | 'if':
					conditions.append (current)
					if current.enable:
						hit = matcher (
							logid = logid,
							token = token,
							option = option,
							by_definition = token in ('ifdef', 'ifndef'),
							by_expression = token == 'if',
							invert = token == 'ifndef'
						)
						current = State (enable = hit, matched = hit)
					else:
						current = State (enable = False, matched = True)
				case 'else':
					if current.final:
						raise ValueError (f'{logid}: already in else branch')
					current = current._replace (enable = not current.matched, matched = True, final = True)
				case 'elifdef' | 'elifndef' | 'elif':
					if current.final:
						raise ValueError (f'{logid}: already been in else branch')
					if not current.matched:
						if matcher (
							logid = logid,
							token = token,
							option = option,
							by_definition = token in ('elifdef', 'elifndef'),
							by_expression = token == 'elif',
							invert = token == 'elifndef'
						):
							current = current._replace (enable = True, matched = True)
					elif current.enable:
						current = current._replace (enable = False)
				case 'endif':
					if not conditions:
						raise IndexError (f'{logid}: out of stack')
					current = conditions.pop ()
				case 'define':
					if current.enable:
						if option is None:
							raise ValueError (f'{logid}: {token}: missing definition')
						parts = option.split (None, 1)
						definitions[parts[0]] = 1 if len (parts) == 1 else parts[1]
				case 'undef' | 'undefine':
					if current.enable:
						if option is None:
							raise ValueError (f'{logid}: {token}: missing definition')
						for definition in option.split ():
							definitions.pop (definition, None)
				case _:
					if current.enable:
						if callback is not None:
							if (result := callback (logid, token, option, line)) is not None:
								save (result[:-1] if result.endswith ('\n') else result)
						else:
							raise SyntaxError (f'{logid}: unknown token "{token}"')
		elif current.enable:
			if expand and definitions:
				pos = 0
				chunks: list[str] = []
				for mmtch in macro_pattern.finditer (line):
					(start, end) = mmtch.span ()
					if start > pos:
						chunks.append (line[pos:start])
					macro = mmtch.group ()
					if macro == '$$':
						chunks.append ('$')
					else:
						chunks.append (str (definitions.get (macro[2:-1], macro)))
					pos = end
				if pos < len (line):
					chunks.append (line[pos:])
				line = ''.join (chunks)
			save (line)
	rc = '\n'.join (output)
	if rc and not rc.endswith ('\n'):
		rc += '\n'
	return rc

class Preprocess:
	__slots__ = ['prefix', 'definitions', 'expand', 'included']
	def __init__ (self, prefix: None | str = None, definitions: None | dict[str, int | str] = None) -> None:
		self.prefix = prefix if prefix is not None else '#'
		self.definitions = definitions if definitions is not None else {}
		self.expand = False
		self.included: set[str] = set ()

	def filter (self, source: str, *, filename: None | str = None) -> str:
		return preprocess (
			source = source,
			definitions = self.definitions,
			filename = filename if filename is not None else '<string>',
			prefix = self.prefix,
			expand = self.expand,
			modifier = self.modifier,
			callback = self.callback
		)

	_token_pattern = re.compile ('^[a-z]+$', re.IGNORECASE)
	def callback (self, logid: str, token: str, option: None | str, line: str) -> None | str:
		match token:
			case 'include' | 'import':
				track = token == 'import'
				if option is not None and (not track or option not in self.included):
					if track:
						self.included.add (option)
					with open (option) as fd:
						return self.filter (fd.read (), filename = option)
			case 'pragma':
				if option is not None:
					self.pragma (option)
			case _:
				if self._token_pattern.match (token) is not None:
					raise SyntaxError (f'{logid}: unknown token "{token}"')
				return line
		return None
		
	def pragma (self, pragma: str) -> None:
		pass
	
	def modifier (self, line: str) -> None | str:
		return line
