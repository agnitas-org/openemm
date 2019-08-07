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
/*	-*- mode: c; mode: fold -*-	*/
# include	<ctype.h>
# include	<string.h>
# include	"xmlback.h"

# define	ID_PAD		0x40
# define	ID_INV		0xff

# define	VTABSIZE	(sizeof (valtab) / sizeof (valtab[0]))

# define	iswhitespace(bbb)		(((bbb) == ' ') || ((bbb) == '\t'))

static byte_t	valtab[128] = { /*{{{*/
/* -7 */	ID_INV,	ID_INV,	ID_INV,	ID_INV,	ID_INV,	ID_INV,	ID_INV,	ID_INV,
/* -15 */	ID_INV,	ID_INV,	ID_INV,	ID_INV,	ID_INV,	ID_INV,	ID_INV,	ID_INV,
/* -23 */	ID_INV,	ID_INV,	ID_INV,	ID_INV,	ID_INV,	ID_INV,	ID_INV,	ID_INV,
/* -31 */	ID_INV,	ID_INV,	ID_INV,	ID_INV,	ID_INV,	ID_INV,	ID_INV,	ID_INV,
/* -39 */	ID_INV,	ID_INV,	ID_INV,	ID_INV,	ID_INV,	ID_INV,	ID_INV,	ID_INV,
/* -47 */	ID_INV,	ID_INV,	ID_INV,	0x3e,	ID_INV,	ID_INV,	ID_INV,	0x3f,
/* -55 */	0x34,	0x35,	0x36,	0x37,	0x38,	0x39,	0x3a,	0x3b,
/* -63 */	0x3c,	0x3d,	ID_INV,	ID_INV,	ID_INV,	ID_PAD,	ID_INV,	ID_INV,
/* -71 */	ID_INV,	0x00,	0x01,	0x02,	0x03,	0x04,	0x05,	0x06,
/* -79 */	0x07,	0x08,	0x09,	0x0a,	0x0b,	0x0c,	0x0d,	0x0e,
/* -87 */	0x0f,	0x10,	0x11,	0x12,	0x13,	0x14,	0x15,	0x16,
/* -95 */	0x17,	0x18,	0x19,	ID_INV,	ID_INV,	ID_INV,	ID_PAD,	ID_INV,
/* -103 */	ID_INV,	0x1a,	0x1b,	0x1c,	0x1d,	0x1e,	0x1f,	0x20,
/* -111 */	0x21,	0x22,	0x23,	0x24,	0x25,	0x26,	0x27,	0x28,
/* -119 */	0x29,	0x2a,	0x2b,	0x2c,	0x2d,	0x2e,	0x2f,	0x30,
/* -127 */	0x31,	0x32,	0x33,	ID_INV,	ID_INV,	ID_INV,	ID_PAD,	ID_INV
/*}}}*/
};
static char	convstr_rfc[65] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
static char	convstr_urlsafe[65] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
static char	hexstr[17] = "0123456789ABCDEF";
static char	not_allowed_qp[] = "?=_<>,\"\\()";

static bool_t
isvalid (char ch) /*{{{*/
{
	if ((ch >= 0) && (ch < VTABSIZE) && (valtab[(int) ch] != ID_INV))
		return true;
	return false;
} /*}}}*/
static int
decode (const char *in, byte_t *out) /*{{{*/
{
	byte_t	tmp[4];
	int	n;
	
	for (n = 0; n < 4; ++n)
		tmp[n] = valtab[(int) in[n]];
	out[0] = (tmp[0] << 2) | (tmp[1] >> 4);
	out[1] = (tmp[1] << 4) | (tmp[2] >> 2);
	out[2] = (tmp[2] << 6) | tmp[3];
	return (in[2] == '=' ? 1 : (in[3] == '=' ? 2 : 3));
}/*}}}*/
bool_t
decode_base64 (const xmlBufferPtr src, buffer_t *dest) /*{{{*/
{
	bool_t		st;
	long		ospare;
	const xmlChar	*cont;
	int		len;
	char		in[4];
	byte_t		out[3];
	int		n, m, o;
	
	st = true;
	ospare = dest -> spare;
	cont = xmlBufferContent (src);
	len = xmlBufferLength (src);
	dest -> spare = ((len + 3) / 4) * 3;
	for (n = 0, m = 0; (n < len) && st; ++n)
		if (isascii (cont[n]) && isvalid (cont[n])) {
			in[m++] = cont[n];
			if (m == 4) {
				m = 0;
				o = decode (in, out);
				if ((o == -1) || (! buffer_stiff (dest, out, o)))
					st = false;
				if (o < 3)
					break;
			}
		}
	dest -> spare = ospare;
	if (st && (m != 0))
		st = false;
	return st;
}/*}}}*/

static 
# ifdef		__OPTIMIZE__
inline
# endif		/* __OPTIMIZE__ */
int
iseol (const xmlChar *content, int length, int pos) /*{{{*/
{
	if (pos < length) {
		if (content[pos] == '\n')
			return 1;
		if ((content[pos] == '\r') && (pos + 1 < length) && (content[pos + 1] == '\n'))
			return 2;
	}
	return 0;
}/*}}}*/
bool_t
encode_none (const xmlBufferPtr src, buffer_t *dest) /*{{{*/
{
	return buffer_append (dest, xmlBufferContent (src), xmlBufferLength (src));
}/*}}}*/
static bool_t
encode_qphead (const byte_t *src, int srclen, buffer_t *dest, 
	       const char *charset) /*{{{*/
{
	bool_t	st;
	
	st = false;
	if (buffer_stiffsn (dest, "=?", 2) &&
	    buffer_stiffs (dest, charset) &&
	    buffer_stiffsn (dest, "?Q?", 3)) {
		int	n;
		byte_t	hex[3];
		
		st = true;
		hex[0] = '=';
		for (n = 0; (n < srclen) && st; ++n)
			if (src[n] == ' ')
				st = buffer_stiffch (dest, '_');
			else if ((! isascii (src[n])) || iscntrl (src[n]) || strchr (not_allowed_qp, src[n])) {
				hex[1] = hexstr[src[n] >> 4];
				hex[2] = hexstr[src[n] & 0xf];
				st = buffer_stiff (dest, hex, 3);
			} else
				st = buffer_stiff (dest, src + n, 1);
		if (st)
			st = buffer_stiffsn (dest, "?=", 2);
	}
	return st;
}/*}}}*/
bool_t
encode_header (const xmlBufferPtr src, buffer_t *dest, const char *charset) /*{{{*/
{
	bool_t		indata;
	long		ospare;
	const xmlChar	*content;
	int		length;
	int		n;
	int		eol;
	
	indata = false;
	ospare = dest -> spare;
	content = xmlBufferContent (src);
	length = xmlBufferLength (src);
	dest -> spare = length + (length / 5);	/* make spare 120% of original length */
	for (n = 0; n < length; )
		if (indata) {
			int	wstart;
			bool_t	ascii;
			char	quote;

			while ((n < length) && indata) {
				wstart = n;
				while ((n < length) && iswhitespace (content[n]))
					++n;
				if (wstart != n)
					if (! buffer_stiff (dest, content + wstart, n - wstart))
						return false;

				wstart = n;
				ascii = true;
				quote = '\0';
				eol = 0;
				if ((n < length) && ((content[n] == '"') || (content[n] == '('))) {
					char	qstart = content[n];
					char	qend = content[n] == '(' ? ')' : content[n];
					int	qeol;
					
					++n;
					while ((n < length) && (content[n] != qend)) {
						qeol = iseol (content, length, n);
						if (qeol) {
							if ((n + qeol >= length) || (! iswhitespace (content[n + qeol])))
								break;
							n += qeol;
						} else {
							if (! isascii (content[n]))
								ascii = false;
							if ((content[n] == '\\') && (n + 1 < length))
								++n;
							++n;
						}
					}
					if ((n < length) && (content[n] == qend)) {
						if (! buffer_stiffch (dest, qstart))
							return false;
						quote = qend;
						++wstart;
					} else {
						/* fallback due to missing closing quote: reset to start of chunk */
						n = wstart;
					}
				}
				if (! quote) {
					int	ws, lws; 	/* index of current white space found and last white space found to go back in case */
					bool_t	ow;		/* if this should be handled as just one word and not use as a chunk of words */
					
					lws = -1;
					ws = -1;
					ow = false;
					while ((n < length) && (! (eol = iseol (content, length, n)))) {
						if (! isascii (content[n]))
							ascii = false;
						else if (iswhitespace (content[n])) {
							if (ws == -1) {
								ws = n;
								lws = ws;
							}
							if (ow)
								break;
						} else if ((ws != -1) && (content[n] == '<')) {
							ow = true;
							if (! ascii) {
								n = ws;
								break;
							}
						} else {
							ws = -1;
							if (content[n] == '@') {
								if ((! ascii) && (lws != -1)) {
									n = lws;
									break;
								}
								ow = true;
							}
						}
						++n;
					}
				}
				if (wstart != n) {
					if (ascii) {
						if (! buffer_stiff (dest, content + wstart, n - wstart))
							return false;
					} else {
						if (! encode_qphead (content + wstart, n - wstart, dest, charset))
							return false;
					}
				}
				if (quote) {
					if (! buffer_stiffch (dest, quote))
						return false;
					++n;
					eol = iseol (content, length, n);
				}
				if (eol) {
					if (! buffer_stiffnl (dest))
						return false;
					n += eol;
					if ((n < length) && (! iswhitespace (content[n])))
						indata = false;
				}
				if ((n < length) && iswhitespace (content[n])) {
					wstart = n++;
					while ((n < length) && iswhitespace (content[n]))
						++n;
					if (! buffer_stiff (dest, content + wstart, n - wstart))
						return false;
				} else
					indata = false;
			}
		} else {
			int	start;

			start = n;
			eol = 0;
			while ((n < length) && (! (eol = iseol (content, length, n)))) {
				if ((content[n] == ':') && (n + 1 < length) && iswhitespace (content[n + 1])) {
					n += 2;
					break;
				}
				++n;
			}
			if (! buffer_stiff (dest, content + start, n - start))
				return false;
			if (eol) {
				if (! buffer_stiffnl (dest))
					return false;
				n += eol;
			} else {
				while ((n < length) && iswhitespace (content[n])) {
					if (! buffer_stiff (dest, content + n, 1))
						return false;
					++n;
				}
				indata = true;
			}
		}
	dest -> spare = ospare;
	return true;
}/*}}}*/
bool_t
encode_8bit (const xmlBufferPtr src, buffer_t *dest) /*{{{*/
{
	long		ospare;
	const xmlChar	*content;
	int		length;
	int		n;
	int		start, eol;
	
	ospare = dest -> spare;
	content = xmlBufferContent (src);
	length = xmlBufferLength (src);
	dest -> spare = length + (length / 20); /* make spare 105% of original size */
	for (n = 0, start = 0; n < length; )
		if (eol = iseol (content, length, n)) {
			if (n != start)
				if (! buffer_stiff (dest, content + start, n - start))
					return false;
			if (! buffer_stiffnl (dest))
				return false;
			n += eol;
			start = n;
		} else
			++n;
	dest -> spare = ospare;
	return true;
}/*}}}*/
bool_t
encode_quoted_printable (const xmlBufferPtr src, buffer_t *dest) /*{{{*/
{
	bool_t		st;
	byte_t		hex[3];
	long		ospare;
	const xmlChar	*content;
	int		length;
	int		n;
	int		ccnt;
	int		eol;
	byte_t		ch;
	
	st = true;
	hex[0] = '=';
	ospare = dest -> spare;
	content = xmlBufferContent (src);
	length = xmlBufferLength (src);
	dest -> spare = length + (length / 10); /* make spare 110% of original size */
	for (n = 0, ccnt = 0; (n < length) && st; ++n) {
		if ((ccnt > 72) && (! iseol (content, length, n))) {
			if ((! buffer_stiffch (dest, '=')) ||
			    (! buffer_stiffnl (dest))) {
				st = false;
				continue;
			}
			ccnt = 0;
		}
		ch = content[n];
		if (((ch >= 33) && (ch <= 60) && ((ch != 46) || ccnt)) ||
		    ((ch >= 62) && (ch <= 126)) ||
		    (((ch == 32) || (ch == 9)) && (! iseol (content, length, n + 1)))) {
			st = buffer_stiff (dest, & ch, 1);
			++ccnt;
		} else if (eol = iseol (content, length, n)) {
			n += eol - 1;
			st = buffer_stiffnl (dest);
			ccnt = 0;
		} else {
			hex[1] = hexstr[ch >> 4];
			hex[2] = hexstr[ch & 0xf];
			st = buffer_stiff (dest, hex, 3);
			ccnt += 3;
		}
	}
	dest -> spare = ospare;
	return st;
}/*}}}*/
static int
enb64 (const byte_t *in, int i, byte_t *out, const char *convstr, bool_t padding) /*{{{*/
{
	int	rc = 4;
	
	out[0] = convstr[in[0] >> 2];
	out[1] = convstr[((in[0] & 0x03) << 4) | (in[1] >> 4)];
	out[2] = convstr[((in[1] & 0x0f) << 2) | (in[2] >> 6)];
	out[3] = convstr[in[2] & 0x3f];
	if (padding) {
		if (i < 3) {
			out[3] = '=';
			if (i < 2)
				out[2] = '=';
		}
	} else {
		rc = i + 1;
	}
	return rc;
}/*}}}*/
static bool_t
do_encode_base64 (const byte_t *content, int length, buffer_t *dest, bool_t rfc, bool_t padding, bool_t linesplit) /*{{{*/
{
	bool_t	st;
	long	ospare;
	int	n, lcnt;
	byte_t	in[3];
	int	i, count;
	byte_t	out[4];

	st = true;
	ospare = dest -> spare;
	dest -> spare = length + ((length * 3) / 4); /* make spare 175% of original size */
	for (n = 0, lcnt = 0; (n < length) && st; ) {
		if (linesplit && (lcnt >= 76)) {
			lcnt = 0;
			st = buffer_stiffnl (dest);
			if (! st)
				continue;
		}
		if (n + 2 < length) {
			in[0] = content[n++];
			in[1] = content[n++];
			in[2] = content[n++];
			i = 3;
		} else {
			in[0] = content[n++];
			if (n < length) {
				in[1] = content[n++];
				i = 2;
			} else {
				in[1] = 0;
				i = 1;
			}
			in[2] = 0;
		}
		count = enb64 (in, i, out, rfc ? convstr_rfc : convstr_urlsafe, padding);
		st = buffer_stiff (dest, out, count);
		lcnt += count;
	}
	if (linesplit && st)
		st = buffer_stiffnl (dest);
	dest -> spare = ospare;
	return st;
}/*}}}*/
bool_t
encode_base64 (const xmlBufferPtr src, buffer_t *dest) /*{{{*/
{
	return do_encode_base64 (xmlBufferContent (src), xmlBufferLength (src), dest, true, true, true);
}/*}}}*/
bool_t
encode_encrypted (buffer_t *src, buffer_t *dest) /*{{{*/
{
	return do_encode_base64 (src -> buffer, src -> length, dest, false, false, false);
}/*}}}*/
bool_t
encode_url (const byte_t *input, int ilen, buffer_t *dest) /*{{{*/
{
	static const char
		*allowed = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_-~!@^*()[]{}|,./";
	bool_t	rc = true;
	int	n;
	
	for (n = 0; (n < ilen) && rc; ++n)
		if (strchr (allowed, input[n]))
			rc = buffer_appendch (dest, input[n]);
		else if (input[n] == ' ')
			rc = buffer_appendch (dest, '+');
		else
			rc = buffer_format (dest, "%%%02X", input[n]);
	return rc;
}/*}}}*/
