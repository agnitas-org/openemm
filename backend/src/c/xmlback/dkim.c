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
# include	<ctype.h>
# include	<opendkim/dkim.h>
# include	"xmlback.h"

# ifndef	DKIM_SIGNHEADER_LEN
# define	DKIM_SIGNHEADER_LEN	(sizeof (DKIM_SIGNHEADER) - 1)
# endif		/* DKIM_SIGNHEADER_LEN */

typedef struct { /*{{{*/
	DKIM_LIB	*lib;
	char		*domain;
	dkim_sigkey_t	key;
	char		*ident;
	char		*selector;
	char		*ref;
	char		*column;
	int		cindex;
	/*}}}*/
}	sdkim_t;
static dkim_sigkey_t
copykey (const char *key) /*{{{*/
{
	int		klen = strlen (key);
	dkim_sigkey_t	rc;
	
	if (rc = malloc (klen + 1)) {
		if (klen > 0)
			memcpy (rc, key, klen);
		rc[klen] = '\0';
	}
	return rc;
}/*}}}*/
static bool_t
parse_column (sdkim_t *s, const char *column) /*{{{*/
{
	bool_t	rc;
	
	if (column) {
		const char	*ptr;
		int		len;
		int		n;
		
		rc = false;
		if (ptr = strchr (column, '.')) {
			len = ptr - column;
			if ((s -> column = strldup (ptr + 1)) && (s -> ref = malloc (len + 1))) {
				for (n = 0; n < len; ++n)
					s -> ref[n] = toupper (column[n]);
				s -> ref[n] = '\0';
				rc = true;
			}
		} else {
			if (s -> column = strldup (column))
				rc= true;
		}
	} else
		rc = true;
	return rc;
}/*}}}*/
static buffer_t *
normalize_eol_to_crlf (buffer_t *input, buffer_t *output) /*{{{*/
{
	buffer_t	*rc = input;
	const xmlChar	*ptr = buffer_content (input);
	int		ilen = buffer_length (input);

	if (buffer_size (output, ilen + 128)) {
		int	i;

		buffer_clear (output);
		for (i = 0; i < ilen; ++i) {
			if ((ptr[i] == '\n') && ((i == 0) || (ptr[i - 1] != '\r'))) {
				if (! buffer_stiffch (output, '\r')) {
					break;
				}
			}
			if (! buffer_stiff (output, ptr + i, 1)) {
				break;
			}
		}
		if (i == ilen) {
			rc = output;
		}
	}
	return rc;
}/*}}}*/
void *
sdkim_alloc (blockmail_t *blockmail, const char *domain, const char *key, const char *ident,
	     const char *selector, const char *column, bool_t enable_report, bool_t enable_debug) /*{{{*/
{
	sdkim_t	*s;

	if (s = (sdkim_t *) malloc (sizeof (sdkim_t))) {
		s -> lib = NULL;
		s -> domain = NULL;
		s -> key = NULL;
		s -> ident = NULL;
		s -> selector = NULL;
		s -> ref = NULL;
		s -> column = NULL;
		s -> cindex = -1;
		if ((s -> domain = strdup (domain)) &&
		    (s -> key = copykey (key)) &&
		    ((! ident) || (s -> ident = strdup (ident))) &&
		    (s -> selector = strdup (selector)) &&
		    parse_column (s, column) &&
		    (s -> lib = dkim_init (NULL, NULL))) {
			DKIM_STAT	st;
			u_int		flags = 0;
			u_int		minbitlength;

			if (blockmail -> pointintime) {
				if (dkim_options (s -> lib, DKIM_OP_SETOPT, DKIM_OPTS_FIXEDTIME, & blockmail -> pointintime, sizeof (blockmail -> pointintime)) != DKIM_STAT_OK)
					log_out (blockmail -> lg, LV_ERROR, "Failed to set point-in-time to %ld", (long) blockmail -> pointintime);
			}
			if ((st = dkim_options (s -> lib, DKIM_OP_GETOPT, DKIM_OPTS_FLAGS, & flags, sizeof (flags))) == DKIM_STAT_OK) {
				flags |= DKIM_LIBFLAGS_CACHE;
				if (enable_report) {
					flags |= DKIM_LIBFLAGS_REQUESTREPORTS;
				}
				if (enable_debug) {
					flags |= DKIM_LIBFLAGS_ZTAGS;
				}
				if ((st = dkim_options (s -> lib, DKIM_OP_SETOPT, DKIM_OPTS_FLAGS, & flags, sizeof (flags))) != DKIM_STAT_OK) {
					log_out (blockmail -> lg, LV_ERROR, "Failed to set dkim option flags %u: %d", flags, st);
				}
			} else {
				log_out (blockmail -> lg, LV_ERROR, "Failed to get dkim option flags: %d", st);
			}
			if ((st = dkim_options (s -> lib, DKIM_OP_GETOPT, DKIM_OPTS_MINKEYBITS, & minbitlength, sizeof (minbitlength))) == DKIM_STAT_OK) {
				if (minbitlength > 768) {
					minbitlength = 768;
					if ((st = dkim_options (s -> lib, DKIM_OP_SETOPT, DKIM_OPTS_MINKEYBITS, & minbitlength, sizeof (minbitlength))) != DKIM_STAT_OK) {
						log_out (blockmail -> lg, LV_ERROR, "Failed to set dkim minimum bit length to %u: %d", minbitlength, st);
					}
				}
			} else {
				log_out (blockmail -> lg, LV_ERROR, "Failed to get dkim minimum bit length: %d", st);
			}
			if (s -> column && blockmail -> field) {
				int	n;
				
				for (n = 0; n < blockmail -> field_count; ++n) {
					field_t	*f = blockmail -> field[n];
					
					if (((! s -> ref) && (! f -> uref)) || (s -> ref && f -> uref && (! strcmp (s -> ref, f -> uref))))
						if (! strcmp (f -> lname, s -> column)) {
							s -> cindex = n;
							break;
						}
				}
			}
		} else
			s = sdkim_free (s);
	}
	return s;
}/*}}}*/
void *
sdkim_free (void *sp) /*{{{*/
{
	sdkim_t	*s = (sdkim_t *) sp;
	
	if (s) {
		if (s -> lib)
			dkim_close (s -> lib);
		if (s -> domain)
			free (s -> domain);
		if (s -> key)
			free (s -> key);
		if (s -> ident)
			free (s -> ident);
		if (s -> selector)
			free (s -> selector);
		if (s -> ref)
			free (s -> ref);
		if (s -> column)
			free (s -> column);
		free (s);
	}
	return NULL;
}/*}}}*/
bool_t
sdkim_should_sign (void *sp, receiver_t *rec) /*{{{*/
{
	sdkim_t	*s = (sdkim_t *) sp;

	if (s -> cindex != -1) {
		record_t	*record = rec -> rvdata -> cur;
		xmlBufferPtr	check = record -> data[s -> cindex];
		const xmlChar	*content = xmlBufferContent (check);
		int		len = xmlBufferLength (check);
		int		n;
		int		rc;
		
		for (rc = 0, n = 0; (n < len) && isdigit (content[n]); ++n) {
			rc *= 10;
			switch (content[n]) {
			case '0':			break;
			case '1':	rc += 1;	break;
			case '2':	rc += 2;	break;
			case '3':	rc += 3;	break;
			case '4':	rc += 4;	break;
			case '5':	rc += 5;	break;
			case '6':	rc += 6;	break;
			case '7':	rc += 7;	break;
			case '8':	rc += 8;	break;
			case '9':	rc += 9;	break;
			}
		}
		if ((n < len) || (rc == 0))
			return false;
	}
	return true;
}/*}}}*/
char *
sdkim_sign (blockmail_t *blockmail, head_t *head, buffer_t *body) /*{{{*/
{
	char		*rc;
	bool_t		ok;
	buffer_t	*scratch;
	sdkim_t		*s;
	DKIM		*dkim;
	DKIM_STAT	st;
	buffer_t	*use;

	rc = NULL;
	ok = true;
	if (! (scratch = buffer_alloc (512)))
		return rc;
	s = (sdkim_t *) blockmail -> dkim;
	if (! (dkim = dkim_sign (s -> lib,
				 (const unsigned char *) s -> domain,
				 NULL,
				 s -> key,
				 (const unsigned char *) s -> selector,
				 (const unsigned char *) s -> domain,
				 DKIM_CANON_RELAXED,
				 DKIM_CANON_DEFAULT,
				 DKIM_SIGN_DEFAULT,
				 -1, & st))) {
		log_out (blockmail -> lg, LV_ERROR, "Failed to sign message: %d", st);
		ok = false;
	}
	if (ok) {
		if (s -> ident) {
			if ((st = dkim_set_signer (dkim, (const unsigned char *) s -> ident)) != DKIM_STAT_OK) {
				log_out (blockmail -> lg, LV_ERROR, "Failed to set signer to \"%s\": %d", blockmail -> mfrom, st);
				ok = false;
			}
		} else if (blockmail -> mfrom) {
			const char	*ptr;
			
			if ((ptr = strchr (blockmail -> mfrom, '@')) && (! strcmp (ptr + 1, s -> domain)) && ((st = dkim_set_signer (dkim, (const unsigned char *) blockmail -> mfrom)) != DKIM_STAT_OK)) {
				log_out (blockmail -> lg, LV_ERROR, "Failed to set signer to \"%s\": %d", blockmail -> mfrom, st);
				ok = false;
			}
		}
	}
	for (; ok && head; head = head -> next) {
		use = normalize_eol_to_crlf (head -> h, scratch);
		if ((st = dkim_header (dkim, use -> buffer, use -> length)) != DKIM_STAT_OK) {
			log_out (blockmail -> lg, LV_ERROR, "Failed to sign header \"%s\": %d", buffer_string (head -> h), st);
			ok = false;
		}
	}
	if (ok)
		if ((st = dkim_eoh (dkim)) != DKIM_STAT_OK) {
			log_out (blockmail -> lg, LV_ERROR, "Failed to finalize header: %d", st);
			ok = false;
		}
	if (ok) {
		use = normalize_eol_to_crlf (body, scratch);
		if ((st = dkim_body (dkim, use -> buffer, use -> length)) != DKIM_STAT_OK) {
			log_out (blockmail -> lg, LV_ERROR, "Failed to process body: %d", st);
			ok = false;
		}
	}
	if (ok)
		if ((st = dkim_eom (dkim, NULL)) != DKIM_STAT_OK) {
			log_out (blockmail -> lg, LV_ERROR, "Failed to finalize body: %d", st);
			ok = false;
		}
	if (ok) {
		unsigned char	scratch_head[8192];
		int		used;

		scratch_head[0] = 'H';
		used = 1;
		memcpy (scratch_head + used, DKIM_SIGNHEADER, DKIM_SIGNHEADER_LEN);
		used += DKIM_SIGNHEADER_LEN;
		memcpy (scratch_head + used, ": ", 2);
		used += 2;
		if ((st = dkim_getsighdr (dkim, scratch_head + used, sizeof (scratch_head) - used, used - 1)) != DKIM_STAT_OK) {
			log_out (blockmail -> lg, LV_ERROR, "Failed to get signature header: %d", st);
			ok = false;
		} else {
			int	n;

			for (n = used; n < sizeof (scratch_head) - 3 && scratch_head[n]; ++n)
				if (scratch_head[n] != '\r')
					if (n != used)
						scratch_head[used++] = scratch_head[n];
					else
						++used;
			memcpy (scratch_head + used, "\n", 2);
			used += 2;
			if (! (rc = malloc (used))) {
				log_out (blockmail -> lg, LV_ERROR, "Failed to copy signature header: %s", scratch_head);
				ok = false;
			}
			memcpy (rc, scratch_head, used);
		}
	}
	if (! ok) {
		const char	*err;
		
		if (err = dkim_geterror (dkim)) {
			log_out (blockmail -> lg, LV_ERROR, "Failed due to: %s", err);
		}
	}
	if (dkim)
		dkim_free (dkim);
	buffer_free (scratch);
	return rc;
}/*}}}*/

static bool_t
ignore_header (const char *h, int hlen, bool_t *isrecv) /*{{{*/
{
	static struct {
		const char	*h;
		int		hlen;
		bool_t		isrecv;
	}	itab[] = {
		{	"Return-Path",		11,	false	},
		{	"Received",		8,	true	},
		{	"Comments",		8,	false	},
		{	"Keywords",		8,	false	},
		{	"Bcc",			3,	false	},
		{	"Resent-Bcc",		10,	false	},
		{	"DKIM-Signature",	14,	false	}
	};
	int	n;

	for (n = 0; n < sizeof (itab) / sizeof (itab[0]); ++n)
		if ((hlen > itab[n].hlen) && (h[itab[n].hlen] == ':') && (! strncasecmp (h, itab[n].h, itab[n].hlen))) {
			if (isrecv)
				*isrecv = itab[n].isrecv;
			return true;
		}
	return false;
}/*}}}*/
void
sign_mail (blockmail_t *blockmail, buffer_t *header) /*{{{*/
{
	const char	*content, *ptr, *save;
	int		len;
	head_t		*head, *prev, *cur;
	int		hpos;
	int		pos;
	
	head = NULL;
	prev = NULL;
	cur = NULL;
	hpos = -1;
	if (header == NULL) {
		header = blockmail -> head;
	}
	content = buffer_string (header);
	for (ptr = content; ptr; ) {
		save = ptr;
		if (ptr = strchr (ptr, '\n')) {
			++ptr;
			len = ptr - save;
		} else
			len = strlen (save);
		pos = save - content;
		if (save[0] == 'H') {
			char	ch;

			++save;
			--len;
			if ((len > 1) && (save[0] == '?')) {
				++save;
				--len;
				while (len > 0) {
					ch = save[0];
					--len;
					++save;
					if (ch == '?')
						break;
				}
			}
			if (len > 0) {
				bool_t	isrecv;

				if (ignore_header (save, len, & isrecv)) {
					cur = NULL;
				} else if (cur = head_alloc ()) {
					if (hpos == -1)
						hpos = pos;
					if (prev)
						prev -> next = cur;
					else
						head = cur;
					prev = cur;
					head_add (cur, save, len);
				}
			}
		} else if (isspace (save[0]) && cur) {
			head_add (cur, save, len);
		} else if (save[0] == '.') {
			if (hpos == -1)
				hpos = pos;
		}
	}
	if (head) {
		char	*dkhd;

		for (cur = head; cur; cur = cur -> next)
			head_trim (cur);
		if (dkhd = sdkim_sign (blockmail, head, blockmail -> body)) {
			if (hpos == -1)
				buffer_appends (header, dkhd);
			else
				buffer_inserts (header, hpos, dkhd);
			free (dkhd);
		}
		while (cur = head) {
			head = head -> next;
			head_free (cur);
		}
	}
}/*}}}*/
