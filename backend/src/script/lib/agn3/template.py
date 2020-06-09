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
import	re
from	typing import Any, Optional
from	typing import Dict, Match
from	.exceptions import error
#
class MessageCatalog:
	"""Message Catalog for templating

This class is primary designed to be integrated in the templating system,
but can also be used stand alone. You instanciate the class with a file
name of the message file which contains of a default section (starting
from top or introduced by a section "[*]". For each supported language
you add a section with the language token, e.g. "[de]" for german and
a list of tokens with the translation. A message catalog file may look
like this:
#	comments start as usual with a hash sign
#	this is the default section
yes: Yes
no: No
#
#	this is the german version
[de]
yes: Ja
no: Nein

You may extend an entry over the current line with a trailing backslash.

If you pass a message catalog to the templating system, you can refer
to the catalog by either using ${_['token']} to just translate one token
or using ${_ ('In your mother language YES means %(yes)')}. There are
also shortcut versions for ${_['xxx']) can be written as _[xxx] and
${_ ('xxx')} can be written as _{xxx}.

In a stand alone variante, this looks like this:
>>> m = MessageCatalog ('/some/file/name')
>>> m.set_lang ('de')
>>> print m['yes']
Ja
>>> print m ('yes')
yes
>>> print m ('yes in your language is %(yes)')
yes in your language is Ja
>>> print m['unset']
*unset*
>>> m.set_fill (None)
>>> print m['unset']
unset

As you can see in the last example an unknown token is expanded to itself
surrounded by a fill string, if set (to easyly catch missing tokens). If
you unset the fill string, the token itself is used with no further
processing.
"""
	__slots__ = ['messages', 'lang', 'fill']
	message_parse = re.compile ('%\\(([^)]+)\\)')
	comment_parse = re.compile ('^[ \t]*#')
	def __init__ (self, fname: Optional[str], lang: Optional[str] = None, fill: Optional[str] = '*') -> None:
		"""``fname'' is the path to the message file, ``lang''
the language to use, ``fill'' used to mark unknown tokens"""
		self.messages: Dict[Optional[str], Dict[str, str]] = {None: {}}
		self.lang: Optional[str] = None
		self.fill = fill
		if fname is not None:
			cur = self.messages[None]
			fd = open (fname, 'r')
			for line in [_l.strip () for _l in fd.read ().replace ('\\\n', '').split ('\n') if _l and self.comment_parse.match (_l) is None]:
				if len (line) > 2 and line.startswith ('[') and line.endswith (']'):
					lang = line[1:-1]
					if lang == '*':
						lang = None
					if not lang in self.messages:
						self.messages[lang] = {}
					cur = self.messages[lang]
				else:
					parts = line.split (':', 1)
					if len (parts) == 2:
						(token, msg) = [_p.strip () for _p in parts]
						if len (msg) >= 2 and msg[0] in '\'"' and msg[-1] == msg[0]:
							msg = msg[1:-1]
						cur[token] = msg
			fd.close ()

	def __setitem__ (self, token: str, s: str) -> None:
		try:
			self.messages[self.lang][token] = s
		except KeyError:
			self.messages[self.lang] = {token: s}

	def __getitem__ (self, token: str) -> str:
		msg: Optional[str]
		try:
			msg = self.messages[self.lang][token]
		except KeyError:
			if not self.lang is None:
				try:
					msg = self.messages[None][token]
				except KeyError:
					msg = None
			else:
				msg = None
		return msg if msg is not None else (token if self.fill is None else f'{self.fill}{token}{self.fill}')

	def __call__ (self, s: str) -> str:
		return self.message_parse.sub (lambda m: self[m.groups ()[0]], s)

	def set_lang (self, lang: Optional[str]) -> None:
		"""set the language to use"""
		self.lang = lang

	def set_fill (self, fill: str) -> None:
		"""set the fill string to mark unknown tokens"""
		self.fill = fill

class Template:
	"""Simple general templating

This class offers a simple templating system. One instance the class
using the template in string from. The syntax is inspirated by velocity,
but differs in serveral ways (and is even simpler). A template can start
with an optional code block surrounded by the tags '#code' and '#end'
followed by the content of the template. Access to variables and
expressions are realized by $... where ... is either a simple varibale
(e.g. $var) or something more complex, then the value must be
surrounded by curly brackets (e.g. ${var.strip ()}). To get a literal
'$'sign, just type it twice, so '$$' in the template leads into '$'
in the output. A trailing backslash removes the following newline to
join lines.

Handling of message catalog is either done by calling ${_['...']} and
${_('...')} or by using the shortcut _[this is the origin] or
_{%(message): %(error)}. As this is a simple parser the brackets
must not part of the string in the shortcut, in this case use the
full call.

Control constructs must start in a separate line, leading whitespaces
ignoring, with a hash '#' sign. These constructs are supported and
are mostly transformed directly into a python construct:

## ...                      this introduces a comment up to end of line
#property(expr)             this sets a property of the template
#pragma(expr)               alias for property
#include(expr)              inclusion of file, subclass must realize this
#if(pyexpr)             --> if pyexpr:
#elif(pyexpr)           --> elif pyexpr:
#else                   --> else
#do(pycmd)              --> pycmd
#pass                   --> pass [same as #do(pass)]
#break			--> break [..]
#continue		--> continue [..]
#for(pyexpr)            --> for pyexpr:
#while(pyexpr)          --> while pyexpr:
#try                    --> try:
#except(pyexpr)         --> except pyexpr:
#finally                --> finally
#with(pyexpr)           --> with pyexpr:
#end                        ends an indention level
#stop                       ends processing of input template

To fill the template you call the method fill(self, namespace, lang = None)
where 'namespace' is a dictonary with names accessable by the template.
Beside, 'lang' could be set to a two letter string to post select language
specific lines from the text. These lines must start with a two letter
language ID followed by a colon, e.g.:

en:This is an example.
de:Dies ist ein Beispiel.

Depending on 'lang' only one (or none of these lines) are outputed. If lang
is not set, these lines are put (including the lang ID) both in the output.
If 'lang' is set, it is also copied to the namespace, so you can write the
above lines using the template language:

#if(lang=='en')
This is an example.
#elif(lang=='de')
Dies ist ein Beispiel.
#end

And for failsafe case, if lang is not set:

#try
 #if(lang=='en')
This is an example.
 #elif(lang=='de')
Dies ist ein Beispiel.
 #end
#except(NameError)
 #pass
#end
"""
	__slots__ = ['content', 'precode', 'postcode', 'compiled', 'properties', 'namespace', 'code', 'indent', 'empty', 'compile_errors']
	codeStart = re.compile ('^[ \t]*#code[^\n]*\n', re.IGNORECASE)
	codeEnd = re.compile ('(^|\n)[ \t]*#end[^\n]*(\n|$)', re.IGNORECASE | re.MULTILINE)
	token = re.compile ('((^|\n)[ \t]*#(#|property|pragma|include|if|elif|else|do|pass|break|continue|for|while|try|except|finally|with|end|stop)|\\$(\\$|[0-9a-z_]+(\\.[0-9a-z_]+)*|\\{[^}]*\\})|_(\\[[^]]+\\]|{[^}]+}))', re.IGNORECASE | re.MULTILINE)
	rplc = re.compile ('\\\\|"|\'|\n|\r|\t|\f|\v', re.MULTILINE)
	rplcMap = {'\n': '\\n', '\r': '\\r', '\t': '\\t', '\f': '\\f', '\v': '\\v'}
	langID = re.compile ('^([ \t]*)([a-z][a-z]):', re.IGNORECASE)
	emptyCatalog = MessageCatalog (None, fill = None)
	def __init__ (self, content: str, precode: Optional[str] = None, postcode: Optional[str] = None) -> None:
		"""``content'' is the template itself, ``precode'' and
``postcode'' will be copied literally to the generated code before
resp. after the auto generated code"""
		self.content = content
		self.precode = precode
		self.postcode = postcode
		self.compiled = None
		self.properties: Dict[str, Any] = {}
		self.namespace: Optional[Dict[str, Any]] = None
		self.code = ''
		self.indent = 0
		self.empty = False
		self.compile_errors: Optional[str] = None

	def __getitem__ (self, var: str) -> Optional[str]:
		return self.namespace.get (var, '') if self.namespace is not None else None

	def __setProperty (self, expr: str) -> None:
		try:
			(var, val) = [_e.strip () for _e in expr.split ('=', 1)]
			if len (val) >= 2 and val[0] in '"\'' and val[-1] == val[0]:
				quote = val[0]
				self.properties[var] = val[1:-1].replace ('\\%s' % quote, quote).replace ('\\\\', '\\')
			elif val.lower () in ('true', 'on', 'yes'):
				self.properties[var] = True
			elif val.lower () in ('false', 'off', 'no'):
				self.properties[var] = False
			else:
				try:
					self.properties[var] = int (val)
				except ValueError:
					self.properties[var] = val
		except ValueError:
			var = expr.strip ()
			if var:
				self.properties[var] = True

	def __indent (self) -> None:
		if self.indent:
			self.code += ' ' * self.indent

	def __code (self, code: str) -> None:
		self.__indent ()
		self.code += '%s\n' % code
		if code:
			if code[-1] == ':':
				self.empty = True
			else:
				self.empty = False

	def __deindent (self) -> None:
		if self.empty:
			self.__code ('pass')
		self.indent -= 1

	def __compileError (self, start: int, errtext: str) -> None:
		if not self.compile_errors:
			self.compile_errors = ''
		self.compile_errors += '** %s: %s ...\n\n\n' % (errtext, self.content[start:start + 60])

	def __replacer (self, mtch: Match[str]) -> str:
		rc = []
		for ch in mtch.group (0):
			try:
				rc.append (self.rplcMap[ch])
			except KeyError:
				rc.append ('\\x%02x' % ord (ch))
		return ''.join (rc)

	def __escaper (self, s: str) -> str:
		return s.replace ('\'', '\\\'')

	def __compileString (self, s: str) -> None:
		self.__code ('__result.append (\'%s\')' % re.sub (self.rplc, self.__replacer, s))

	def __compileExpr (self, s: str) -> None:
		self.__code ('__result.append (str (%s))' % s)

	def __compileCode (self, token: Optional[str], arg: Optional[str]) -> None:
		if token is not None:
			if arg:
				self.__code ('%s %s:' % (token, arg))
			else:
				self.__code ('%s:' % token)
		elif arg:
			self.__code (arg)

	def __compileContent (self) -> None:
		self.code = ''
		if self.precode:
			self.code += self.precode
			if self.code[-1] != '\n':
				self.code += '\n'
		pos = 0
		clen = len (self.content)
		mtch = self.codeStart.search (self.content)
		if not mtch is None:
			start = mtch.end ()
			mtch = self.codeEnd.search (self.content, start)
			if not mtch is None:
				(end, pos) = mtch.span ()
				self.code += self.content[start:end] + '\n'
			else:
				self.__compileError (0, 'Unfinished code segment')
		self.indent = 0
		self.empty = False
		self.code += '__result = []\n'
		while pos < clen:
			mtch = self.token.search (self.content, pos)
			if mtch is None:
				start = clen
				end = clen
			else:
				(start, end) = mtch.span ()
				groups = mtch.groups ()
				if groups[1]:
					start += len (groups[1])
			if start > pos:
				self.__compileString (self.content[pos:start])
			pos = end
			if not mtch is None:
				tstart = start
				if not groups[2] is None:
					token = groups[2]
					arg = ''
					if token != '#':
						if pos < clen and self.content[pos] == '(':
							pos += 1
							level = 1
							quote = None
							escape = False
							start = pos
							end = -1
							while pos < clen and level > 0:
								ch = self.content[pos]
								if escape:
									escape = False
								elif ch == '\\':
									escape = True
								elif not quote is None:
									if ch == quote:
										quote = None
								elif ch in '\'"':
									quote = ch
								elif ch == '(':
									level += 1
								elif ch == ')':
									level -= 1
									if level == 0:
										end = pos
								pos += 1
							if start < end:
								arg = self.content[start:end]
							else:
								self.__compileError (tstart, 'Unfinished statement')
						if pos < clen and self.content[pos] == '\n':
							pos += 1
					if token == '#':
						while pos < clen and self.content[pos] != '\n':
							pos += 1
						if pos < clen:
							pos += 1
					elif token in ('property', 'pragma'):
						self.__setProperty (arg)
					elif token in ('include', ):
						try:
							included = self.include (arg)
							if included is not None:
								self.content = self.content[:pos] + included + self.content[pos:]
								clen += len (included)
						except error as e:
							self.__compileError (tstart, 'Failed to include "%s": %s' % (arg, e))
					elif token in ('if', 'else', 'elif', 'for', 'while', 'try', 'except', 'finally', 'with'):
						if token in ('else', 'elif', 'except', 'finally'):
							if self.indent > 0:
								self.__deindent ()
							else:
								self.__compileError (tstart, 'Too many closeing blocks')
						if (arg and token in ('if', 'elif', 'for', 'while', 'except', 'with')) or (not arg and token in ('else', 'try', 'finally')):
							self.__compileCode (token, arg)
						elif arg:
							self.__compileError (tstart, 'Extra arguments for #%s detected' % token)
						else:
							self.__compileError (tstart, 'Missing statement for #%s' % token)
						self.indent += 1
					elif token in ('pass', 'break', 'continue'):
						if arg:
							self.__compileError (tstart, 'Extra arguments for #%s detected' % token)
						else:
							self.__compileCode (None, token)
					elif token in ('do', ):
						if arg:
							self.__compileCode (None, arg)
						else:
							self.__compileError (tstart, 'Missing code for #%s' % token)
					elif token in ('end', ):
						if arg:
							self.__compileError (tstart, 'Extra arguments for #end detected')
						if self.indent > 0:
							self.__deindent ()
						else:
							self.__compileError (tstart, 'Too many closing blocks')
					elif token in ('stop', ):
						pos = clen
				elif not groups[3] is None:
					expr = groups[3]
					if expr == '$':
						self.__compileString ('$')
					else:
						if len (expr) >= 2 and expr[0] == '{' and expr[-1] == '}':
							expr = expr[1:-1]
						self.__compileExpr (expr)
				elif not groups[5] is None:
					expr = groups[5]
					if expr[0] == '[':
						self.__compileExpr ('_[\'%s\']' % self.__escaper (expr[1:-1]))
					elif expr[0] == '{':
						self.__compileExpr ('_ (\'%s\')' % self.__escaper (expr[1:-1]))
				elif not groups[0] is None:
					self.__compileString (groups[0])
		if self.indent > 0:
			self.__compileError (0, 'Missing %d closing #end statement(s)' % self.indent)
		if self.compile_errors is None:
			if self.postcode:
				if self.code and self.code[-1] != '\n':
					self.code += '\n'
				self.code += self.postcode
			self.compiled = compile (self.code, '<template>', 'exec')

	def include (self, arg: Any) -> Optional[str]:
		"""method to overwrite to implement the #include statement"""
		raise error ('Subclass responsible for implementing "include (%r)"' % arg)

	def property (self, var: str, default: Any = None) -> Any:
		"""returns a property from the template if found, else ``default''"""
		try:
			return self.properties[var]
		except KeyError:
			return default

	def compile (self) -> None:
		"""compiles the template (generates internal code from template)"""
		if self.compiled is None:
			try:
				self.__compileContent ()
				if self.compiled is None:
					raise error ('Compilation failed: %s' % self.compile_errors)
			except Exception as e:
				raise error ('Failed to compile [%r] %r:\n%s\n' % (type (e), e.args, self.code))

	def fill (self, namespace: Optional[Dict[str, Any]], lang: Optional[str] = None, mc: Optional[MessageCatalog] = None) -> str:
		"""uses the template to fill it using the parameter
from ``namespace'' for language ``lang'' by using the message catalog
``mc''"""
		if self.compiled is None:
			self.compile ()
		if namespace is None:
			self.namespace = {}
		else:
			self.namespace = namespace.copy ()
		if not lang is None:
			self.namespace['lang'] = lang
		self.namespace['property'] = self.properties
		if mc is None:
			mc = self.emptyCatalog
		mc.set_lang (lang)
		self.namespace['_'] = mc
		try:
			if self.compiled is not None:
				exec (self.compiled, self.namespace)
			else:
				raise error ('code failed to compile')
		except Exception as e:
			raise error ('Execution failed [%s]: %s' % (e.__class__.__name__, str (e)))
		result = ''.join (self.namespace['__result'])
		if not lang is None:
			nresult = []
			for line in result.split ('\n'):
				mtch = self.langID.search (line)
				if mtch is None:
					nresult.append (line)
				else:
					(pre, lid) = mtch.groups ()
					if lid.lower () == lang:
						nresult.append (pre + line[mtch.end ():])
			result = '\n'.join (nresult)
		result = result.replace ('\\\n', '')
		self.namespace['result'] = result
		return result
