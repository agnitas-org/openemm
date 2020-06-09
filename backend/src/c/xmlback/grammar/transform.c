/********************************************************************************************************************************************************************************************************************************************************************
 *                                                                                                                                                                                                                                                                  *
 *                                                                                                                                                                                                                                                                  *
 *        Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)                                                                                                                                                                                                   *
 *                                                                                                                                                                                                                                                                  *
 *        This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.    *
 *        This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.           *
 *        You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.                                                                                                            *
 *                                                                                                                                                                                                                                                                  *
 ********************************************************************************************************************************************************************************************************************************************************************/
# include	<stdlib.h>
# include	<ctype.h>
# include	<string.h>
# include	"grammar.h"
# include	"parse.h"

token_t *
token_alloc (int tid, const char *token) /*{{{*/
{
	token_t	*t;
	
	if (t = (token_t *) malloc (sizeof (token_t))) {
		t -> tid = tid;
		t -> token = NULL;
		if (token && (! (t -> token = strdup (token)))) {
			free (t);
			t = NULL;
		}
	}
	return t;
}/*}}}*/
token_t *
token_free (token_t *t) /*{{{*/
{
	if (t) {
		if (t -> token)
			free (t -> token);
		free (t);
	}
	return NULL;
}/*}}}*/
static inline bool_t
noword (xmlChar ch) /*{{{*/
{
	if ((xmlCharLength (ch) == 1) && (isalnum (ch) || (ch == '_')))
		return false;
	return true;
}/*}}}*/
static inline bool_t
match (const xmlChar *str1, const char *str2, int len) /*{{{*/
{
	while (len > 0) {
		if ((xmlCharLength (*str1) != 1) ||
		    (tolower (*str1) != tolower (*str2)))
			break;
		++str1;
		++str2;
		--len;
	}
	return len > 0 ? false : true;
}/*}}}*/
static inline int
isref (const xmlChar *str, int len, int *dotpos) /*{{{*/
{
	int	n;
	bool_t	valid;
	xmlChar	ch;
	int	clen;
	
	*dotpos = -1;
	for (n = 0, valid = false; n < len; ) {
		ch = str[n];
		clen = xmlCharLength (ch);
		if (clen != 1)
			break;
		if ((! n) && (! isalpha (ch)))
			break;
		if (*dotpos == -1) {
			if (ch == '.')
				*dotpos = n;
			else if (! isalnum (ch))
				break;
		} else if (*dotpos + 1 == n) {
			if ((! isalpha (ch)) && (ch != '_'))
				break;
			valid = true;
		} else {
			if ((! isalnum (ch)) && (ch != '_'))
				break;
		}
		n += clen;
	}
	return valid ? n : -1;
}/*}}}*/
static inline int
isname (const xmlChar *str, int len) /*{{{*/
{
	int	n;
	int	clen;
	xmlChar	ch;
	
	for (n = 0; n < len; ) {
		ch = str[n];
		clen = xmlCharLength (ch);
		if ((clen != 1) ||
		    ((! n) && (! isalpha (ch))) ||
		    (n && (! isalnum (ch)) && (ch != '_')))
			break;
		n += clen;
	}
	return n;
}/*}}}*/

static struct { /*{{{*/
	int		tid;
	const char	*token;
	int		len;
	bool_t		isword;
	/*}}}*/
}	toktab[] = { /*{{{*/
	{	T_OPEN,		"(",		1,	false	},
	{	T_CLOSE,	")",		1,	false	},
	{	T_COMMA,	",",		1,	false	},
	{	T_MINUS,	"-",		1,	false	},
	{	T_PLUS,		"+",		1,	false	},
	{	T_SLASH,	"/",		1,	false	},
	{	T_STAR,		"*",		1,	false	},
	{	T_NE,		"!=",		2,	false	},
	{	T_NE,		"<>",		2,	false	},
	{	T_NE,		"><",		2,	false	},
	{	T_GE,		">=",		2,	false	},
	{	T_GE,		"=>",		2,	false	},
	{	T_GT,		">",		1,	false	},
	{	T_LE,		"<=",		2,	false	},
	{	T_LE,		"=<",		2,	false	},
	{	T_LT,		"<",		1,	false	},
	{	T_EQ,		"==",		2,	false	},
	{	T_EQ,		"=",		1,	false	},
	{	T_AND,		"and",		3,	true	},
	{	T_AND,		"&&",		2,	false	},
	{	T_OR,		"or",		2,	true	},
	{	T_OR,		"||",		2,	false	},
	{	T_NOT,		"not",		3,	true	},
	{	T_NOT,		"!",		1,	false	},
	{	T_LIKE,		"like",		4,	true	},
	{	T_ESCAPE,	"escape",	6,	true	},
	{	T_ISNULL,	"isnull",	6,	true	},
	{	T_IS,		"is",		2,	true	},
	{	T_IN,		"in",		2,	true	},
	{	T_BETWEEN,	"between",	7,	true	},
	{	T_MOD,		"mod",		3,	true	},
	{	T_LOWER,	"lower",	5,	true	},
	{	T_UPPER,	"upper",	5,	true	},
	{	T_INITCAP,	"initcap",	7,	true	},
	{	T_LENGTH,	"length",	6,	true	},
	{	T_TO_CHAR,	"to_char",	7,	true	},
	{	T_DATE_FORMAT,	"date_format",	11,	true	},
	{	T_DECODE,	"decode",	6,	true	},
	{	T_NOW,		"now",		3,	true	},
	{	T_NULL,		"null",		4,	true	},
	{	T_SYSDATE,	"sysdate",	7,	true	},
	{	T_SYSDATE,	"current_timestamp", 17,true	}
	/*}}}*/
};
# define	TTSIZE		(sizeof (toktab) / sizeof (toktab[0]))

bool_t
transform (buffer_t *buf, const xmlChar *input, int input_length, buffer_t *parse_error, xconv_t *xconv) /*{{{*/
{
	bool_t	ok;
	void	*parser;
	
	ok = false;
	if (parser = ParseAlloc (malloc)) {
		int		n;
		int		clen;
		token_t		*token;
		buffer_t	*scratch;
		private_t	priv;

		if (scratch = buffer_alloc (4096)) {
			priv.buf = buf;
			priv.errcnt = 0;
			priv.parse_error = parse_error;
			priv.xconv = xconv;
			for (n = 0; (n < input_length) && (! priv.errcnt); ) {
				clen = xmlCharLength (input[n]);
				token = NULL;
				if (clen > 1) {
					priv.errcnt++;
					n += clen;
					if (parse_error)
						buffer_appends (parse_error, "Non-ASCII character outside string detected.\n");
				} else if (isspace (input[n])) {
					++n;
					while (n < input_length) {
						clen = xmlCharLength (input[n]);
						if ((clen != 1) || (! isspace (input[n])))
							break;
						++n;
					}
				} else if (input[n] == '\'') {
					++n;
					buffer_setch (scratch, '"');
					while (n < input_length) {
						clen = xmlCharLength (input[n]);
						if ((clen != 1) || (input[n] != '\'')) {
							if ((clen == 1) && ((input[n] == '\\') || (input[n] == '"')))
								buffer_appendch (scratch, '\\');
							buffer_append (scratch, input + n, clen);
						} else if ((n + 1 < input_length) && (xmlCharLength (input[n + 1]) == 1) && (input[n + 1] == '\'')) {
							buffer_appendch (scratch, '\'');
							++clen;
						} else {
							++n;
							break;
						}
						n += clen;
					}
					buffer_appendch (scratch, '"');
					if (! (token = token_alloc (T_STR, buffer_string (scratch)))) {
						priv.errcnt++;
						if (parse_error)
							buffer_format (parse_error, "Unable to allocate token for string: %s.\n", buffer_string (scratch));
					}
				} else if (isdigit (input[n]) || ((input[n] == '.') && (n + 1 < input_length) && (xmlCharLength (input[n + 1] == 1) && isdigit (input[n + 1])))) {
					int	state, dcount;
					
					if (input[n] == '.') {
						buffer_setch (scratch, '0');
						state = 2;
						dcount = 1;
					} else {
						state = 1;
						if (isdigit (input[n]))
							dcount = 1;
						else
							dcount = 0;
					}
					buffer_setb (scratch, input[n]);
					++n;
					while (state && (n < input_length)) {
						clen = xmlCharLength (input[n]);
						if (clen == 1) {
							if (isdigit (input[n])) {
								buffer_appendb (scratch, input[n]);
								++n;
								if (state == 1)
									++dcount;
							} else if ((state == 1) && (input[n] == '.')) {
								if (! dcount) {
									buffer_appendch (scratch, '0');
									++dcount;
								}
								buffer_appendb (scratch, input[n]);
								++n;
								state = 2;
							} else
								state = 0;
						} else
							state = 0;
					}
					if (! (token = token_alloc (T_NUM, buffer_string (scratch))))
						priv.errcnt++;
				} else {
					int	t;
					int	tlen;
					int	dotpos;

					for (t = 0; t < TTSIZE; ++t)
						if (toktab[t].len == 1) {
							if (input[n] == toktab[t].token[0])
								break;
						} else if (toktab[t].len < input_length - n + 1) {
							if (toktab[t].isword) {
								if (match (input + n, toktab[t].token, toktab[t].len) &&
								    ((n + toktab[t].len == input_length) || noword (input[n + toktab[t].len])))
								    	break;
							} else {
								if (! memcmp (input + n, toktab[t].token, toktab[t].len * sizeof (xmlChar)))
									break;
							}
						}
					if (t < TTSIZE) {
						n += toktab[t].len;
						if (! (token = token_alloc (toktab[t].tid, toktab[t].token)))
							priv.errcnt++;
					} else if ((input_length - n > 5) && match (input + n, "cust.", 5)) {
						n += 5;
						buffer_sets (scratch, "VAR$");
						while (n < input_length)
							if (noword (input[n]))
								break;
							else {
								buffer_appendb (scratch, toupper (input[n]));
								++n;
							}
						if ((scratch -> length == 0) ||
						    (! (token = token_alloc (T_VARIABLE, buffer_string (scratch)))))
							priv.errcnt++;
					} else if ((tlen = isref (input + n, input_length - n, & dotpos)) > 0) {
						int	m;

						buffer_sets (scratch, "VAR$");
						for (m = 0; m < tlen; ++m)
							if (m == dotpos)
								buffer_appendsn (scratch, "$$", 2);
							else
								buffer_appendb (scratch, toupper (input[n + m]));
						n += tlen;
						if (! (token = token_alloc (T_VARIABLE, buffer_string (scratch))))
							priv.errcnt++;
					} else if ((tlen = isname (input + n, input_length - n)) > 0) {
						buffer_set (scratch, input + n, tlen);
						n += tlen;
						if (! (token = token_alloc (T_NAME, buffer_string (scratch))))
							priv.errcnt++;
					} else
						priv.errcnt++;
				}
				if (token) {
					int	plen = 0;
					
					if (parse_error)
						plen = parse_error -> length;
					Parse (parser, token -> tid, token, & priv);
					if (parse_error && (parse_error -> length > plen))
						priv.errcnt++;
				}
				if (priv.errcnt && parse_error) {
					int	m;

					buffer_append (parse_error, input, input_length);
					buffer_appendch (parse_error, '\n');
					for (m = 0; m < n; ++m)
						buffer_appendch (parse_error, isspace (input[m]) ? input[m] : ' ');
					buffer_appends (parse_error, "*\n");
				}
			}
			Parse (parser, 0, NULL, & priv);
			if (! priv.errcnt)
				ok = true;
			buffer_free (scratch);
		}
		ParseFree (parser, free);
	}
	if (ok && parse_error && (parse_error -> length > 0))
		ok = false;
	return ok;
}/*}}}*/
# ifndef	NDEBUG
bool_t
transformtable_check (buffer_t *out) /*{{{*/
{
	bool_t	ok;
	int	n, m, cnt;
	
	ok = true;
	buffer_appends (out, "Length check:\n");
	for (n = 0, cnt = 0; n < TTSIZE; ++n)
		if (strlen (toktab[n].token) != toktab[n].len) {
			++cnt;
			buffer_format (out, "\t`%s' claims to have length of %d, but has length of %d\n", toktab[n].token, toktab[n].len, (int) strlen (toktab[n].token));
			ok = false;
		}
	buffer_format (out, "%d error(s) detected.\n", cnt);
	buffer_appends (out, "\nUnique check:\n");
	for (n = 1, cnt = 0; n < TTSIZE; ++n)
		for (m = 0; m < n; ++m)
			if ((toktab[m].len <= toktab[n].len) && (! strncmp (toktab[m].token, toktab[n].token, toktab[m].len))) {
				++cnt;
				buffer_format (out, "\t`%s' makes `%s' useless\n", toktab[m].token, toktab[n].token);
				ok = false;
			}
	buffer_format (out, "%d error(s) detected.\n", cnt);
	return ok;
}/*}}}*/
# endif		/* NDEBUG */
