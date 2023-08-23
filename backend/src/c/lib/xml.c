/********************************************************************************************************************************************************************************************************************************************************************
 *                                                                                                                                                                                                                                                                  *
 *                                                                                                                                                                                                                                                                  *
 *        Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)                                                                                                                                                                                                   *
 *                                                                                                                                                                                                                                                                  *
 *        This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.    *
 *        This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.           *
 *        You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.                                                                                                            *
 *                                                                                                                                                                                                                                                                  *
 ********************************************************************************************************************************************************************************************************************************************************************/
# include	<stdlib.h>
# include	<ctype.h>
# include	"xml.h"
# include	"utfmap.h"

static int	utf8_length_tab[256] = { /*{{{*/
	1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
	1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
	1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
	1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
	1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
	1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
	1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
	1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
	1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
	1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
	1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
	1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
	2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
	2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
	3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
	4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 6, 6, 1, 1
	/*}}}*/
},		utf8_strict_length_tab[256] = { /*{{{*/
	1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
	1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
	1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
	1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
	1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
	1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
	1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
	1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
	-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
	2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
	3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
	4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 6, 6, -1, -1
	/*}}}*/
};
int
xchar_length (xchar_t ch) /*{{{*/
{
	return utf8_length_tab[ch];
}/*}}}*/
int
xchar_strict_length (xchar_t ch) /*{{{*/
{
	return utf8_strict_length_tab[ch];
}/*}}}*/
int
xchar_valid_position (const xchar_t *s, int length) /*{{{*/
{
# define	VALID(ccc)	(((ccc) & 0xc0) == 0x80)
	int	len, n;
	
	if (((len = xchar_strict_length (*s)) > 0) && (length >= len))
		for (n = len; n > 1; ) {
			--n;
			if (! VALID (*(s + n))) {
				len = -1;
				break;
			}
		}
	else
		len = -1;
	return len;
# undef		VALID	
}/*}}}*/
bool_t
xchar_valid (const xchar_t *s, int length) /*{{{*/
{
	int	n;
	
	while (length > 0)
		if ((n = xchar_valid_position (s, length)) > 0) {
			s += n;
			length -= n;
		} else
			break;
	return length == 0 ? true : false;
}/*}}}*/
bool_t
xequal (const xchar_t *s1, const xchar_t *s2) /*{{{*/
{
	return strcmp ((const char *) s1, (const char *) s2) == 0 ? true : false;
}/*}}}*/
const char *
xchar_to_char (const xchar_t *s) /*{{{*/
{
	return (const char *) s;
}/*}}}*/
const xchar_t *
char_2_xchar (const char *s) /*{{{*/
{
	return (const xchar_t *) s;
}/*}}}*/
const char *
byte_to_char (const byte_t *b) /*{{{*/
{
	return (const char *) b;
}/*}}}*/
int
xstrlen (const xchar_t *s) /*{{{*/
{
	int	len, clen;
	
	for (len = 0; *s; ++len) {
		clen = xchar_length (*s);
		while ((clen-- > 0) && *s)
			++s;
	}
	return len;
}/*}}}*/
int
xstrnlen (const xchar_t *s, int slen) /*{{{*/
{
	int	len, clen;
	
	for (len = 0; slen > 0; ++len) {
		clen = xchar_length (*s);
		slen -= clen;
		if (slen >= 0)
			while (clen-- > 0)
				++s;
	}
	return len;
}/*}}}*/
int
xstrcmp (const xchar_t *s1, const char *s2) /*{{{*/
{
	return strcmp (xchar_to_char (s1), s2);
}/*}}}*/
int
xstrncmp (const xchar_t *s1, const char *s2, size_t n) /*{{{*/
{
	return strncmp (xchar_to_char (s1), s2, n);
}/*}}}*/
static inline int
calcpos (int len, int pos) /*{{{*/
{
	if (pos < 0) {
		pos = len + pos + 1;
		if (pos < 0)
			pos = 0;
	}
	return pos;
}/*}}}*/
xchar_t *
xsubstr (const xchar_t *s, int start, int end) /*{{{*/
{
	int	rlen = strlen ((const char *) s);
	int	len = xstrlen (s);
	int	pstart, pend;
	int	rn, n, clen;
	xchar_t	*rc;
	
	start = calcpos (len, start);
	end = calcpos (len, end);
	pstart = -1;
	pend = -1;
	if (start < end) {
		for (rn = 0, n = 0; rn < rlen; ) {
			if (n == start)
				pstart = rn;
			if (n == end) {
				pend = rn;
				break;
			}
			clen = xchar_length (s[rn]);
			while ((clen > 0) && (rn < rlen)) {
				--clen;
				++rn;
			}
			++n;
		}
		if (n == end)
			pend = rn;
		if ((pstart == -1) || (pend == -1))
			len = -1;
		else
			len = pend - pstart;
	} else
		len = 0;
	
	if ((len >= 0) && (rc = (xchar_t *) malloc (len + 1))) {
		if (len > 0)
			memcpy (rc, s + pstart, len);
		rc[len] = 0;
	} else
		rc = NULL;
	return rc;		
}/*}}}*/
static inline unsigned long
mkcp (const xchar_t *s, int *len) /*{{{*/
{
	unsigned long	cp;
	int		n;
	
	*len = xchar_length (*s);
	for (n = 0, cp = 0; n < *len; ++n) {
		cp <<= 8;
		cp |= s[n];
	}
	return cp;
}/*}}}*/
static inline const utfmap_t *
mapfind (const xchar_t *s, int *len, const utfmap_t *map, int msize) /*{{{*/
{
	unsigned long	cp;
	int		low, high, pos;
	int		dummy;

	cp = mkcp (s, len ? len : & dummy);
	for (low = 0, high = msize; low < high; ) {
		pos = (low + high) >> 1;
		if (map[pos].cp == cp)
			return & map[pos];
		else if (map[pos].cp < cp)
			low = pos + 1;
		else
			high = pos;
	}
	return NULL;
}/*}}}*/
static inline bool_t
isword (const xchar_t *s) /*{{{*/
{
	unsigned long	cp;
	int		len;
	
	cp = mkcp (s, & len);
	if (len == 1)
		return isalnum (s[0]) ? true : false;
	else {
		int	low, high, pos;
		
		for (low = 0, high = is_word_length; low < high;) {
			pos = (low + high) >> 1;
			if (is_word[pos] == cp)
				return true;
			if (is_word[pos] < cp)
				low = pos + 1;
			else
				high = pos;
		}
	}
	return false;
}/*}}}*/
static inline const xchar_t *
mapper (const utfmap_t *map, int msize, const xchar_t *s, int *slen, int *olen) /*{{{*/
{
	const utfmap_t	*m = mapfind (s, slen, map, msize);
	
	if (m) {
		if (olen)
			*olen = m -> dlen;
		return m -> dst;
	}
	return NULL;
}/*}}}*/
const xchar_t *
xtolower (const xchar_t *s, int *slen, int *olen) /*{{{*/
{
	return mapper (utflower, utflower_length, s, slen, olen);
}/*}}}*/
const xchar_t *
xtoupper (const xchar_t *s, int *slen, int *olen) /*{{{*/
{
	return mapper (utfupper, utfupper_length, s, slen, olen);
}/*}}}*/
const xchar_t *
xtotitle (const xchar_t *s, int *slen, int *olen) /*{{{*/
{
	return mapper (utftitle, utftitle_length, s, slen, olen);
}/*}}}*/
static xchar_t *
mappers (const xchar_t *s, int len, int *olen,
	 const xchar_t *(*first) (const xchar_t *, int *, int *),
	 const xchar_t *(*next) (const xchar_t *, int *, int *)) /*{{{*/
{
	xchar_t	*rc;
	int	rsize, ruse;
	
	rsize = len + 32;
	if (rc = (xchar_t *) malloc (rsize + 1)) {
		const xchar_t	*rplc;
		int		slen, rlen;
		bool_t		isfirst = true;
		bool_t		isletter;
		
		ruse = 0;
		while (len > 0) {
			if (next) {
				isletter = isword (s);
				if (isfirst || (! isletter)) {
					if (isletter) {
						rplc = (*first) (s, & slen, & rlen);
						isfirst = false;
					} else {
						rplc = NULL;
						mkcp (s, & slen);
						isfirst = true;
					}
				} else
					rplc = (*next) (s, & slen, & rlen);
			} else {
				rplc = (*first) (s, & slen, & rlen);
			}
			if (! rplc) {
				rplc = s;
				rlen = slen;
			}
			if (ruse + rlen > rsize) {
				rsize += 128;
				if (! (rc = realloc (rc, rsize + 1)))
					break;
			}
			while (rlen-- > 0)
				rc[ruse++] = *rplc++;
			len -= slen;
			s += slen;
		}
		if (rc) {
			rc[ruse] = 0;
			if (olen)
				*olen = ruse;
		}
	}
	return rc;
}/*}}}*/
xchar_t *
xlowern (const xchar_t *s, int len, int *olen) /*{{{*/
{
	return mappers (s, len, olen, xtolower, NULL);
}/*}}}*/
xchar_t *
xlower (const xchar_t *s, int *olen) /*{{{*/
{
	return mappers (s, strlen ((const char *) s), olen, xtolower, NULL);
}/*}}}*/
xchar_t *
xuppern (const xchar_t *s, int len, int *olen) /*{{{*/
{
	return mappers (s, len, olen, xtoupper, NULL);
}/*}}}*/
xchar_t *
xupper (const xchar_t *s, int *olen) /*{{{*/
{
	return mappers (s, strlen ((const char *) s), olen, xtoupper, NULL);
}/*}}}*/
xchar_t *
xtitlen (const xchar_t *s, int len, int *olen) /*{{{*/
{
	return mappers (s, len, olen, xtotitle, xtolower);
}/*}}}*/
xchar_t *
xtitle (const xchar_t *s, int *olen) /*{{{*/
{
	return mappers (s, strlen ((const char *) s), olen, xtotitle, xtolower);
}/*}}}*/

xconv_t *
xconv_free (xconv_t *xc) /*{{{*/
{
	if (xc) {
		if (xc -> lower)
			cache_free (xc -> lower);
		if (xc -> upper)
			cache_free (xc -> upper);
		if (xc -> title)
			cache_free (xc -> title);
		free (xc);
	}
	return NULL;
}/*}}}*/
xconv_t *
xconv_alloc (int cache_size) /*{{{*/
{
	xconv_t	*xc;
	
	if (xc = (xconv_t *) malloc (sizeof (xconv_t))) {
		xc -> csize = cache_size;
		xc -> lower = cache_alloc (xc -> csize);
		xc -> upper = cache_alloc (xc -> csize);
		xc -> title = cache_alloc (xc -> csize);
		if (! (xc -> lower && xc -> upper && xc -> title))
			xc = xconv_free (xc);
	}
	return xc;
}/*}}}*/
static inline const xchar_t *
converter (cache_t *c, xchar_t *(*func) (const xchar_t *, int, int *), const xchar_t *s, int slen, int *olen) /*{{{*/
{
	centry_t	*ce = cache_find (c, s, slen);
	
	if (! ce) {
		xchar_t	*rplc;
		int	rlen;
		
		if (rplc = (*func) (s, slen, & rlen)) {
			ce = cache_add (c, s, slen, rplc, rlen);
			free (rplc);
		}
	}
	if (ce) {
		*olen = ce -> dlen;
		return ce -> data;
	} else {
		*olen = slen;
		return s;
	}
}/*}}}*/
const xchar_t *
xconv_lower (xconv_t *xc, const xchar_t *s, int slen, int *olen) /*{{{*/
{
	return converter (xc -> lower, xlowern, s, slen, olen);
}/*}}}*/
const xchar_t *
xconv_upper (xconv_t *xc, const xchar_t *s, int slen, int *olen) /*{{{*/
{
	return converter (xc -> upper, xuppern, s, slen, olen);
}/*}}}*/
const xchar_t *
xconv_title (xconv_t *xc, const xchar_t *s, int slen, int *olen) /*{{{*/
{
	return converter (xc -> title, xtitlen, s, slen, olen);
}/*}}}*/
