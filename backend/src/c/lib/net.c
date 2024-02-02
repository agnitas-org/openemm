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
/** @file net.c
 * Network related routines.
 */
# include	<stdlib.h>
# include	<ctype.h>
# include	<netdb.h>
# include	<sys/utsname.h>
# include	"agn.h"

/** Get full qualified domain name for current system.
 * Retrieve the fqdn for the local machine
 * @return the fqdn on success, NULL otherwise
 */
char *
get_fqdn (void) /*{{{*/
{
	char		*fqdn;
	struct utsname	un;
	struct hostent	*hent;
	
	fqdn = NULL;
	if (uname (& un) != -1) {
		sethostent (0);
		if ((hent = gethostbyname (un.nodename)) && hent -> h_name)
			fqdn = strdup (hent -> h_name);
		endhostent ();
	}
	return fqdn;
}/*}}}*/

/** Encode input to replace all dangerous character
 * to be used as an URL parameter
 * @param input the source to be encoded
 * @param ilen the length of the source
 * @param dest the target buffer to write encoded output to
 * @return true on success, false otherwise
 */
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
static const char *
find_address (const char *chunk, int length, int *matchlength) /*{{{*/
{
	while ((length > 0) && isspace (*chunk))
		++chunk, --length;
	while ((length > 0) && isspace (chunk[length - 1]))
		--length;
	if (length > 0) {
		int	at;
		
		for (at = 0; at < length; ++at)
			if (chunk[at] == '@')
				break;
		if ((at > 0) && (at < length)) {
			int	start, end;
			
			start = at - 1;
			if (chunk[start] == '"') {
				if (start > 0) {
					--start;
					while ((start >= 0) && (chunk[start] != '"'))
						--start;
				}
			} else {
				while ((start >= 0) && (! isspace (chunk[start])))
					--start;
				++start;
			}
			if (start != -1) {
				end = at + 1;
				while ((end < length) && (! isspace (chunk[end])))
					++end;
				while ((end > at + 1) && (! isalnum (chunk[end - 1]) && (! strchr ("-_]", chunk[end - 1]))))
					--end;
				if (end > at) {
					*matchlength = end - start;
					return chunk + start;
				}
			}
		}
	}
	return NULL;
}/*}}}*/

/** Get address from a header line value
 * @param the header line containing an address
 * @return the pure email address, if successful, otherwise NULL
 */
char *
extract_address (const char *hline) /*{{{*/
{
	const char	*addr;
	int		len;
	char		*rc;
	
	if ((addr = addr_element (hline, & len)) &&
	    ((! len) || (addr = find_address (addr, len, & len))) &&
	    (rc = malloc (len + 1))) {
		memcpy (rc, addr, len);
		rc[len] = '\0';
		return rc;
	}
	return NULL;
}/*}}}*/
char *
_extract_address (const char *hline) /*{{{*/
{
	const char	*ptr;
	const char	*start = NULL;
	int		length = -1;
	char		*rc;
	
	if (ptr = strchr (hline, '<')) {
		start = ptr + 1;
		if (ptr = strchr (start, '>')) {
			length = ptr - start;
		} else {
			start = NULL;
		}
	}
	if (! start) {
		bool_t		escape;
		int		comment;
		const char	*cstart;
		
		for (ptr = hline, escape = false, cstart = hline, comment = 0; *ptr && (! start); ++ptr) {
			if (escape)
				escape = false;
			else if (*ptr == '\\')
				escape = true;
			else if (*ptr == '(') {
				if (comment == 0)
					start = find_address (cstart, ptr - cstart, & length);
				++comment;
			} else if (*ptr == ')') {
				if (comment > 0)
					if (--comment == 0)
						cstart = ptr + 1;
			}
		}
		if ((! start) && (cstart < ptr))
			start = find_address (cstart, ptr - cstart, & length);
	}
	if (! start)
		start = find_address (hline, strlen (hline), & length);
	if (start && (length != -1)) {
		/*
		 * be "smart" and remove leading and trailing quotes,
		 * if existing and also leading and trailing <..> if 
		 * encoded in xml entities, so &lt; and &gt;
		 */
		if ((length >= 2) && (start[0] == '"') && (start[length - 1] == '"')) {
			++start;
			length -= 2;
		}
		if ((length >= 8) &&
		    (start[0] == '&') && (start[1] == 'l') && (start[2] == 't') && (start[3] == ';') &&
		    (start[length - 4] == '&') && (start[length - 3] == 'g') && (start[length - 2] == 't') && (start[length - 1] == ';')) {
			start += 4;
			length -= 8;
		}
		if (rc = malloc (length + 1)) {
			if (length)
				memcpy (rc, start, length);
			rc[length] = '\0';
			return rc;
		}
	}
	return NULL;
}/*}}}*/
const char *
addr_element (const char *chunk, int *matchlength) /*{{{*/
{
	int		paddr, aaddr, plen, alen, addr, len;
	int		n;
	int		comment;
	bool_t		escape, angle, quote;

	paddr = aaddr = plen = alen = -1;
	for (n = 0, comment = 0, escape = angle = quote = false; chunk[n]; ++n) {
		if (escape) {
			escape = false;
		} else if (chunk[n] == '\\') {
			escape = true;
		} else if (angle) {
			if (aaddr == -1)
				aaddr = n;
			if (chunk[n] == '>') {
				angle = false;
				if (alen == -1)
					alen = n - aaddr;
			}
		} else if (comment > 0) {
			if (chunk[n] == '(')
				++comment;
			else if (chunk[n] == ')')
				--comment;
		} else if (quote) {
			if (chunk[n] == '"')
				quote = false;
		} else if (chunk[n] == '<') {
			angle = true;
		} else if (chunk[n] == '(') {
			comment = true;
		} else if (chunk[n] == '"') {
			quote = true;
			if (paddr == -1)
				paddr = n;
		} else if (isspace (chunk[n])) {
			if ((paddr != -1) && (plen == -1))
				plen = n - paddr;
		} else {
			if (paddr == -1)
				paddr = n;
		}
	}
	if ((aaddr != -1) && (alen != -1)) {
		addr = aaddr;
		len = alen;
	} else if (paddr != -1) {
		if (plen == -1)
			plen = n - paddr;
		if ((plen > 1) && (chunk[paddr] == '"') && (chunk[paddr + plen - 1] == '"')) {
			paddr += 1;
			plen -= 2;
		}
		addr = paddr;
		len = plen;
	} else
		addr = len = -1;
	if ((addr != -1) && (len != -1)) {
		*matchlength = len;
		return chunk + addr;
	}
	return NULL;
}/*}}}*/
void
norm_element (char *chunk) /*{{{*/
{
	int	n, m;
	int	comment;
	bool_t	escape, angle, quote;
	bool_t	copy;

# define	COPY	do { if (copy) if (n != m) chunk[m++] = chunk[n]; else ++m; } while (0)
	for (n = 0, m = 0, comment = 0, escape = angle = quote = false, copy = true; chunk[n]; ++n) {
		if (escape) {
			escape = false;
			COPY;
		} else if (chunk[n] == '\\') {
			escape = true;
		} else if (angle) {
			if (chunk[n] == '>')
				angle = false;
			COPY;
		} else if (comment > 0) {
			if (chunk[n] == '(')
				++comment;
			else if (chunk[n] == ')')
				if (--comment == 0)
					copy = true;
		} else if (quote) {
			if (chunk[n] == '"')
				quote = false;
			else
				COPY;
		} else if (chunk[n] == '<') {
			angle = true;
			COPY;
		} else if (chunk[n] == '(') {
			comment = true;
			copy = false;
		} else if (chunk[n] == '"') {
			quote = true;
		} else if (isspace (chunk[n])) {
			if (copy && n && m && (! isspace (chunk[m - 1])))
				chunk[m++] = ' ';
			while (isspace (chunk[n + 1]))
				++n;
		} else
			COPY;
	}
	if (m != n)
		chunk[m] = '\0';
# undef		COPY
}/*}}}*/
char *
split_element (char *chunk) /*{{{*/
{
	int	n;
	int	comment;
	bool_t	escape, angle, quote;

	for (n = 0, comment = 0, escape = angle = quote = false; chunk[n]; ++n) {
		if (escape) {
			escape = false;
		} else if (chunk[n] == '\\') {
			escape = true;
		} else if (angle) {
			if (chunk[n] == '>')
				angle = false;
		} else if (comment > 0) {
			if (chunk[n] == '(')
				++comment;
			else if (chunk[n] == ')')
				--comment;
		} else if (quote) {
			if (chunk[n] == '"')
				quote = false;
		} else if (chunk[n] == '<') {
			angle = true;
		} else if (chunk[n] == '(') {
			comment = true;
		} else if (chunk[n] == '"') {
			quote = true;
		} else if (chunk[n] == ',')
			break;
	}
	if (chunk[n])
		chunk[n++] = '\0';
	return chunk + n;
}/*}}}*/
