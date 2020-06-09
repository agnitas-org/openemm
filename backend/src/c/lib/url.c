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
# include	"agn.h"

static inline void
xclear (byte_t **v) /*{{{*/
{
	if (*v) {
		free (*v);
		*v = NULL;
	}
}/*}}}*/
static inline bool_t
xcopyn (byte_t **v, const byte_t *s, int slen) /*{{{*/
{
	if (*v)
		free (*v);
	if (*v = malloc (slen + 1)) {
		if (slen > 0)
			memcpy (*v, s, slen);
		(*v)[slen] = 0;
	}
	return *v ? true : false;
}/*}}}*/
static inline bool_t
xcopy (byte_t **v, const byte_t *s) /*{{{*/
{
	return xcopyn (v, s, strlen ((const char *) s));
}/*}}}*/
static inline byte_t *
decode (const byte_t *s, int slen, int *olen) /*{{{*/
{
	byte_t	*rc;
	int	n, pos;
	
	if (rc = (byte_t *) malloc (slen + 1)) {
		for (n = 0, pos = 0; n < slen; )
			if ((s[n] == '%') && (n + 3 <= slen)) {
				rc[pos++] = (byte_t) unhexn ((const char *) (s + n + 1), 2);
				n += 3;
			} else
				rc[pos++] = s[n++];
		if (n < slen)
			xclear (& rc);
		else
			rc[pos] = 0;
		*olen = pos;
	} else
		*olen = 0;
	return rc;
}/*}}}*/
static inline bool_t
replace (buffer_t *buf, const byte_t *s, int slen, bool_t (*callback) (void *, buffer_t *, const byte_t *, int), void *priv) /*{{{*/
{
	bool_t		rc;
	int		n;
	int		start;
	
	rc = true;
	for (n = 0, start = -1; (n < slen) && rc; ) {
		if ((s[n] == '#') && (n + 1 < slen) && (s[n + 1] == '#')) {
			if (start == -1)
				start = n;
			else {
				if (! (*callback) (priv, buf, s + start + 2, n - start - 2))
					rc = false;
				start = -1;
			}
			n += 2;
		} else {
			if (start == -1)
				buffer_append (buf, s + n, 1);
			++n;
		}
	}
	if (start != -1)
		rc = false;
	return rc;
}/*}}}*/
static inline bool_t
encode (buffer_t *buf, buffer_t *scratch, const char *extra_encode, const byte_t *s, int slen, bool_t (*callback) (void *, buffer_t *, const byte_t *, int), void *priv) /*{{{*/
{
	bool_t		rc;
	int		n;
	int		dyn;
	
	rc = true;
	if (callback) {
		buffer_clear (scratch);
		if (rc = replace (scratch, s, slen, callback, priv)) {
			s = scratch -> buffer;
			slen = scratch -> length;
		}
	}
	for (n = 0, dyn = 0; n < slen; ) {
		if (s[n] == '#') {
			if ((dyn == 0) || (dyn == 2)) {
				if ((n + 1 < slen) && (s[n + 1] == '#'))
					++dyn;
			} else if (dyn == 4)
				dyn = 1;
			else
				++dyn;
		} else if (dyn)
			dyn &= 0x2;
		if (dyn || (isascii (s[n]) && isprint (s[n]) && (s[n] != '%') && (s[n] != '&') && (s[n] != '#') && (s[n] != ' ') && ((! extra_encode) || (! strchr (extra_encode, s[n]))))) {
			buffer_appendb (buf, s[n++]);
		} else {
			buffer_format (buf, "%%%02X", s[n++]);
		}
	}
	return rc;
}/*}}}*/
		
static void
purl_clear (purl_t *p) /*{{{*/
{
	xclear (& p -> orig);
	xclear (& p -> proto);
	xclear (& p -> host);
	p -> port = 0;
	xclear (& p -> path);
	xclear (& p -> param);
	xclear (& p -> anchor);
	purl_clr_param (p);
	if (p -> build)
		buffer_clear (p -> build);
}/*}}}*/
purl_t *
purl_free (purl_t *p) /*{{{*/
{
	if (p) {
		purl_clear (p);
		if (p -> parsed)
			free (p -> parsed);
		if (p -> build)
			buffer_free (p -> build);
		if (p -> scratch)
			buffer_free (p -> scratch);
		free (p);
	}
	return NULL;
}/*}}}*/
purl_t *
purl_alloc (const byte_t *url) /*{{{*/
{
	purl_t	*p;
	
	if (p = (purl_t *) malloc (sizeof (purl_t))) {
		p -> orig = NULL;
		p -> proto = NULL;
		p -> host = NULL;
		p -> port = 0;
		p -> path = NULL;
		p -> param = NULL;
		p -> anchor = NULL;
		p -> parsed = NULL;
		p -> psize = 0;
		p -> pused = 0;
		p -> build = NULL;
		p -> scratch = NULL;
		if (url && (! purl_parse (p, url)))
			p = purl_free (p);
	}
	return p;
}/*}}}*/
purl_t *
purl_allocs (const char *url) /*{{{*/
{
	return purl_alloc ((const byte_t *) url);
}/*}}}*/
static inline const byte_t *
skipto (const byte_t *ptr, const char *delim) /*{{{*/
{
	bool_t	ok;
	
	while (ptr && *ptr) {
		if ((*ptr == '#') && (*(ptr + 1) == '#')) {
			ptr += 2;
			ok = false;
			while (*ptr) {
				if ((*ptr == '#') && (*(ptr + 1) == '#')) {
					ptr += 2;
					ok = true;
					break;
				} else
					++ptr;
			}
			if (! ok)
				ptr = NULL;
		} else {
			if (strchr (delim, (char) *ptr))
				break;
			else
				++ptr;
		}
	}
	return ptr;
}/*}}}*/

void
purl_clr_param (purl_t *p) /*{{{*/
{
	if (p -> parsed && p -> pused) {
		int	n;
		
		for (n = 0; n < p -> pused; ++n) {
			if (p -> parsed[n].name)
				free (p -> parsed[n].name);
			if (p -> parsed[n].value)
				free (p -> parsed[n].value);
		}
		p -> pused = 0;
	}
}/*}}}*/
static bool_t
purl_add_param_allocated (purl_t *p, byte_t *param, int plen, byte_t *value, int vlen) /*{{{*/
{
	bool_t	rc;
	
	rc = false;
	if (p -> pused >= p -> psize) {
		int	nsize = p -> psize + (p -> psize ? p -> psize : 8);
		param_t	*np = (param_t *) realloc (p -> parsed, nsize * sizeof (param_t));
		
		if (np) {
			p -> psize = nsize;
			p -> parsed = np;
		}
	}
	if (p -> pused < p -> psize) {
		param_t	*use;
		
		use = & p -> parsed[p -> pused++];
		use -> name = param;
		use -> value = value;
		use -> nlen = plen;
		use -> vlen = vlen;
		rc = true;
	}
	if (! rc) {
		free (param);
		free (value);
	}
	return rc;
}/*}}}*/
bool_t
purl_add_paramn (purl_t *p, const byte_t *param, int plen, const byte_t *value, int vlen) /*{{{*/
{
	bool_t	rc;
	byte_t	*mp, *mv;
	
	rc = false;
	if (mp = (byte_t *) malloc (plen + 1)) {
		if (mv = (byte_t *) malloc (vlen + 1)) {
			if (plen > 0)
				memcpy (mp, param, plen);
			mp[plen] = 0;
			if (vlen > 0)
				memcpy (mv, value, vlen);
			mv[vlen] = 0;
			rc = purl_add_param_allocated (p, mp, plen, mv, vlen);
		} else
			free (mp);
	}
	return rc;
}/*}}}*/
bool_t
purl_add_param (purl_t *p, const byte_t *param, const byte_t *value) /*{{{*/
{
	return purl_add_paramn (p, param, strlen ((const char *) param), value, strlen ((const char *) value));
}/*}}}*/
bool_t
purl_add_params (purl_t *p, const char *key, const char *value) /*{{{*/
{
	return purl_add_paramn (p, (const byte_t *) key, strlen (key), (const byte_t *) value, strlen (value));
}/*}}}*/
int
purl_find_paramn (purl_t *p, const byte_t *key, int klen, int startpos) /*{{{*/
{
	int	n;
	
	for (n = startpos; n < p -> pused; ++n)
		if ((p -> parsed[n].nlen == klen) && ((klen == 0) || (! memcmp (p -> parsed[n].name, key, klen))))
			break;
	return n < p -> pused ? n : -1;
}/*}}}*/
int
purl_find_param (purl_t *p, const byte_t *key, int startpos) /*{{{*/
{
	return purl_find_paramn (p, key, strlen ((const char *) key), startpos);
}/*}}}*/
int
purl_find_params (purl_t *p, const char *key, int startpos) /*{{{*/
{
	return purl_find_paramn (p, (const byte_t *) key, strlen (key), startpos);
}/*}}}*/
bool_t
purl_update_paramn (purl_t *p, int pos, const byte_t *value, int vlen) /*{{{*/
{
	bool_t	rc;
	
	rc = false;
	if ((pos >= 0) && (pos < p -> pused)) {
		byte_t	*nvalue;
		
		if (nvalue = malloc (vlen + 1)) {
			if (vlen > 0)
				memcpy (nvalue, value, vlen);
			nvalue[vlen] = 0;
			if (p -> parsed[pos].value)
				free (p -> parsed[pos].value);
			p -> parsed[pos].value = nvalue;
			rc = true;
		}
	}
	return rc;
}/*}}}*/
bool_t
purl_update_param (purl_t *p, int pos, const byte_t *value) /*{{{*/
{
	return purl_update_paramn (p, pos, value, strlen ((const char *) value));
}/*}}}*/
bool_t
purl_update_params (purl_t *p, int pos, const char *value) /*{{{*/
{
	return purl_update_paramn (p, pos, (const byte_t *) value, strlen (value));
}/*}}}*/
void
purl_remove_param (purl_t *p, int pos) /*{{{*/
{
	if ((pos >= 0) && (pos < p -> pused)) {
		int	n;
		
		if (p -> parsed[pos].name)
			free (p -> parsed[pos].name);
		if (p -> parsed[pos].value)
			free (p -> parsed[pos].value);
		for (n = pos + 1; n < p -> pused; ++n)
			p -> parsed[n - 1] = p -> parsed[n];
		p -> pused--;
	}
}/*}}}*/
const byte_t *
purl_get_param (purl_t *p, int pos, int *rlen) /*{{{*/
{
	if ((pos >= 0) && (pos < p -> pused)) {
		if (rlen)
			*rlen = p -> parsed[pos].vlen;
		return p -> parsed[pos].value;
	}
	return NULL;
}/*}}}*/

bool_t
purl_parse_param (purl_t *p, const byte_t *src) /*{{{*/
{
	const byte_t	*ptr;
	const byte_t	*param, *value;
	int		plen, vlen;
	byte_t		*decoded_param, *decoded_value;
	int		dplen, dvlen;

	for (ptr = src; ptr && *ptr; ) {
		param = ptr;
		ptr = skipto (ptr, "&");
		if (ptr) {
			value = skipto (param, "=");
			if (value) {
				if (value > ptr)
					value = ptr;
				plen = value - param;
				if (decoded_param = decode (param, plen, & dplen)) {
					if (value < ptr) {
						++value;
						vlen = ptr - value;
						if (! (decoded_value = decode (value, vlen, & dvlen)))
							ptr = NULL;
					} else {
						decoded_value = NULL;
						dvlen = 0;
					}
					if (! purl_add_param_allocated (p, decoded_param, dplen, decoded_value, dvlen))
						ptr = NULL;
				} else {
					free (decoded_param);
					ptr = NULL;
				}
				if (ptr && *ptr) {
					++ptr;
					if (! *ptr)
						if (! purl_add_param_allocated (p, NULL, 0, NULL, 0))
							ptr = NULL;
				}
			} else
				ptr = NULL;
		}
	}
	return ptr || (! src) ? true : false;
}/*}}}*/
bool_t
purl_parsen (purl_t *p, const byte_t *url, int ulen) /*{{{*/
{
	bool_t	rc;
	
	rc = false;
	purl_clear (p);
	if (xcopyn (& p -> orig, url, ulen)) {
		const byte_t	*save, *ptr;
		int		state;
		
		rc = true;
		for (ptr = p -> orig, state = 0; ptr && *ptr && rc; ) {
			save = ptr;
			switch (state) {
			case 0:		/* protocol */
				ptr = skipto (ptr, ":");
				if (ptr && *ptr && (*(ptr + 1) == '/') && (*(ptr + 2) == '/')) {
					rc = xcopyn (& p -> proto, save, ptr - save);
					ptr += 3;
					state = 1;
				} else
					rc = false;
				break;
			case 1:		/* host */
				ptr = skipto (ptr, ":/?#");
				if (ptr) {
					rc = xcopyn (& p -> host, save, ptr - save);
					if (*ptr) {
						if (*ptr == ':')
							state = 2;
						else if (*ptr == '/')
							state = 3;
						else if (*ptr == '?')
							state = 4;
						else if (*ptr == '#')
							state = 5;
						else
							rc = false;
						if (*ptr != '/')
							++ptr;
					} else
						state = 3;
				} else
					rc = false;
				break;
			case 2:		/* port (optional) */
				p -> port = 0;
				while (*ptr && isdigit (*ptr)) {
					p -> port *= 10;
					switch ((char) *ptr++) {
					case '0':				break;
					case '1':	p -> port += 1;		break;
					case '2':	p -> port += 2;		break;
					case '3':	p -> port += 3;		break;
					case '4':	p -> port += 4;		break;
					case '5':	p -> port += 5;		break;
					case '6':	p -> port += 6;		break;
					case '7':	p -> port += 7;		break;
					case '8':	p -> port += 8;		break;
					case '9':	p -> port += 9;		break;
					}
				}
				if ((ptr != save) && (p -> port > 0) && (p -> port < 65536))
					state = 3;
				else
					rc = false;
				break;
			case 3:		/* path (optional) */
				ptr = skipto (ptr, "?#");
				if (ptr) {
					rc = xcopyn (& p -> path, save, ptr - save);
					if (*ptr) {
						if (*ptr == '?')
							state = 4;
						else if (*ptr == '#')
							state = 5;
						else
							rc = false;
						if (rc)
							++ptr;
					}
				} else
					rc = false;
				break;
			case 4:		/* parameter (optional) */
				ptr = skipto (ptr, "#");
				if (ptr) {
					rc = xcopyn (& p -> param, save, ptr - save);
					if (*ptr) {
						state = 5;
						++ptr;
					}
				} else
					rc = false;
				break;
			case 5:		/* anchor (optional) */
				if (*ptr) {
					while (*ptr)
						++ptr;
					rc = xcopyn (& p -> anchor, save, ptr - save);
				}
				break;
			}
		}
		if (rc && ptr)
			if ((state == 4) && (! p -> param))
				rc = xcopyn (& p -> param, ptr, 0);
			else if ((state == 5) && (! p -> anchor))
				rc = xcopyn (& p -> anchor, ptr, 0);
		if (rc)
			rc = purl_parse_param (p, p -> param);
	}
	return rc;
}/*}}}*/
bool_t
purl_parse (purl_t *p, const byte_t *url) /*{{{*/
{
	return purl_parsen (p, url, strlen ((const char *) url));
}/*}}}*/
bool_t
purl_set_hostn (purl_t *p, const byte_t *host, int hlen) /*{{{*/
{
	return xcopyn (& p -> host, host, hlen);
}/*}}}*/
bool_t
purl_set_host (purl_t *p, const byte_t *host) /*{{{*/
{
	return purl_set_hostn (p, host, strlen ((const char *) host));
}/*}}}*/
bool_t
purl_set_port (purl_t *p, int port) /*{{{*/
{
	if ((port >= 0) && (port < 65536)) {
		p -> port = port;
		return true;
	}
	return false;
}/*}}}*/
bool_t
purl_set_pathn (purl_t *p, const byte_t *path, int plen) /*{{{*/
{
	return xcopyn (& p -> path, path, plen);
}/*}}}*/
bool_t
purl_set_path (purl_t *p, const byte_t *path) /*{{{*/
{
	return purl_set_pathn (p, path, strlen ((const char *) path));
}/*}}}*/
bool_t
purl_set_anchorn (purl_t *p, const byte_t *anchor, int alen) /*{{{*/
{
	return xcopyn (& p -> anchor, anchor, alen);
}/*}}}*/
bool_t
purl_set_anchor (purl_t *p, const byte_t *anchor) /*{{{*/
{
	return purl_set_anchorn (p, anchor, strlen ((const char *) anchor));
}/*}}}*/

const byte_t *
purl_build (purl_t *p, const char *extra_encode, int *rlen, bool_t (*callback) (void *, buffer_t *, const byte_t *, int), void *priv) /*{{{*/
{
	if (p -> proto && p -> host && (p -> build || (p -> build = buffer_alloc (512))) && (p -> scratch || (p -> scratch = buffer_alloc (256)))) {
		buffer_clear (p -> build);
		buffer_format (p -> build, "%s://", (char *) p -> proto);
		if (callback)
			replace (p -> build, p -> host, strlen ((const char *) p -> host), callback, priv);
		else
			buffer_appends (p -> build, (const char *) p -> host);
		if (p -> port)
			buffer_format (p -> build, ":%d", p -> port);
		if (p -> path && p -> path[0])
			if (callback)
				replace (p -> build, p -> path, strlen ((const char *) p -> path), callback, priv);
			else
				buffer_appends (p -> build, (const char *) p -> path);
		if (p -> parsed && (p -> pused > 0)) {
			int	n;
			
			for (n = 0; n < p -> pused; ++n) {
				buffer_appendch (p -> build, n ? '&' : '?');
				encode (p -> build, p -> scratch, extra_encode, p -> parsed[n].name, p -> parsed[n].nlen, callback, priv);
				if (p -> parsed[n].value) {
					buffer_appendch (p -> build, '=');
					encode (p -> build, p -> scratch, extra_encode, p -> parsed[n].value, p -> parsed[n].vlen, callback, priv);
				}
			}
		} else if (p -> param && (! *p -> param))
			buffer_appendch (p -> build, '?');
		if (p -> anchor)
			buffer_format (p -> build, "#%s", (char *) p -> anchor);
		buffer_appendch (p -> build, '\0');
		if (rlen)
			*rlen = p -> build -> length - 1;
		return  p -> build -> buffer;
	}
	if (rlen)
		*rlen = 0;
	return NULL;
}/*}}}*/
