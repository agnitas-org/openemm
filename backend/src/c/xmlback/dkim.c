/********************************************************************************************************************************************************************************************************************************************************************
 *                                                                                                                                                                                                                                                                  *
 *                                                                                                                                                                                                                                                                  *
 *        Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)                                                                                                                                                                                                   *
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

adkim_t *
adkim_alloc (void) /*{{{*/
{
	adkim_t	*a;
	
	if (a = (adkim_t *) malloc (sizeof (adkim_t))) {
		a -> id = 0;
		a -> key = NULL;
		a -> domain = NULL;
		a -> selector = NULL;
		a -> domainlength = -1;
	}
	return a;
}/*}}}*/
adkim_t *
adkim_free (adkim_t *a) /*{{{*/
{
	if (a) {
		if (a -> key)
			free (a -> key);
		if (a -> domain)
			free (a -> domain);
		if (a -> selector)
			free (a -> selector);
		free (a);
	}
	return NULL;
}/*}}}*/
static bool_t
adkim_match (adkim_t *a, const char *domain) /*{{{*/
{
	int		domainlength = strlen (domain);
	const char	*checkdomain = NULL;
	
	if (a -> domainlength == -1)
		a -> domainlength = strlen (a -> domain);
	if (domainlength == a -> domainlength)
		checkdomain = domain;
	else if (domainlength > a -> domainlength) {
		checkdomain = domain + domainlength - a -> domainlength;
		if (checkdomain[-1] != '.')
			checkdomain = NULL;
	}
	if (checkdomain && (! strcasecmp (checkdomain, a -> domain)))
		return true;
	return false;
}/*}}}*/

struct sdkim { /*{{{*/
	DKIM_LIB	*lib;
	adkim_t		*default_key;
	char		*ident;
	char		*ref;
	char		*column;
	int		cindex;
	/*}}}*/
};
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
	
	if (rc) {
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
	}
	return rc;
}/*}}}*/
sdkim_t *
sdkim_alloc (blockmail_t *blockmail, const char *domain, const char *key, const char *ident,
	     const char *selector, const char *column, bool_t enable_report, bool_t enable_debug) /*{{{*/
{
	sdkim_t	*s;

	if (s = (sdkim_t *) malloc (sizeof (sdkim_t))) {
		s -> lib = NULL;
		s -> default_key = NULL;
		s -> ident = NULL;
		s -> ref = NULL;
		s -> column = NULL;
		s -> cindex = -1;
		if ((s -> default_key = adkim_alloc ()) &&
		    (s -> default_key -> domain = strdup (domain)) &&
		    (s -> default_key -> key = strdup (key)) &&
		    (s -> default_key -> selector = strdup (selector)) &&
		    ((! ident) || (s -> ident = strdup (ident))) &&
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
sdkim_t *
sdkim_free (sdkim_t *s) /*{{{*/
{
	if (s) {
		if (s -> lib)
			dkim_close (s -> lib);
		if (s -> default_key)
			adkim_free (s -> default_key);
		if (s -> ident)
			free (s -> ident);
		if (s -> ref)
			free (s -> ref);
		if (s -> column)
			free (s -> column);
		free (s);
	}
	return NULL;
}/*}}}*/
bool_t
sdkim_should_sign (sdkim_t *s, receiver_t *rec) /*{{{*/
{
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
static bool_t
ignore_header (head_t *h) /*{{{*/
{
	static struct {
		const char	*name;
		int		namelength;
	}	ignores[] = {
# define	IHEAD(nnn)	{	nnn, sizeof (nnn) - 1	}
		IHEAD ("return-path"),
		IHEAD ("received"),
		IHEAD ("comments"),
		IHEAD ("keywords"),
		IHEAD ("bcc"),
		IHEAD ("resent-bcc"),
		IHEAD ("dkim-signature")
# undef		IHEAD			
	};
	int	n;
	
	for (n = 0; n < sizeof (ignores) / sizeof (ignores[0]); ++n)
		if (head_matchn (h, ignores[n].name, ignores[n].namelength))
			return true;
	return false;
}/*}}}*/
static char *
sdkim_sign (blockmail_t *blockmail, header_t *header, adkim_t *adkim, const char *ident, buffer_t *body) /*{{{*/
{
	char		*rc;
	bool_t		ok;
	buffer_t	*scratch;
	sdkim_t		*s;
	const char	*domain, *selector;
	dkim_sigkey_t	key;
	DKIM		*dkim;
	DKIM_STAT	st;
	buffer_t	*use;
	head_t		*head;

	rc = NULL;
	ok = true;
	if (! (scratch = buffer_alloc (512)))
		return rc;
	s = blockmail -> signdkim;
	if (! adkim)
		adkim = s -> default_key;
	domain = adkim -> domain;
	selector = adkim -> selector;
	key = (dkim_sigkey_t) adkim -> key;
	if (! (dkim = dkim_sign (s -> lib,
				 (const unsigned char *) domain,
				 NULL,
				 key,
				 (const unsigned char *) selector,
				 (const unsigned char *) domain,
				 DKIM_CANON_RELAXED,
				 DKIM_CANON_DEFAULT,
				 DKIM_SIGN_DEFAULT,
				 -1, & st))) {
		log_out (blockmail -> lg, LV_ERROR, "Failed to sign message: %d", st);
		ok = false;
	}
	if (ok) {
		if (! ident) {
			const char	*ptr;
			
			if (s -> ident) {
				ident = s -> ident;
			} else if ((ptr = strchr (blockmail -> mfrom, '@')) && (! strcasecmp (ptr + 1, domain))) {
				ident = blockmail -> mfrom;
			}
		}
		if (ident && ((st = dkim_set_signer (dkim, (const unsigned char *) ident)) != DKIM_STAT_OK)) {
			log_out (blockmail -> lg, LV_ERROR, "Failed to set signer to \"%s\": %d", ident, st);
			ok = false;
		}
	}
	for (head = header -> head; ok && head; head = head -> next)
		if (! ignore_header (head)) {
			use = normalize_eol_to_crlf (header_encode (header, head), scratch);
			if (use && ((st = dkim_header (dkim, use -> buffer, use -> length)) != DKIM_STAT_OK)) {
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

		used = 0;
		memcpy (scratch_head + used, DKIM_SIGNHEADER, DKIM_SIGNHEADER_LEN);
		used += DKIM_SIGNHEADER_LEN;
		memcpy (scratch_head + used, ": ", 2);
		used += 2;
		if ((st = dkim_getsighdr (dkim, scratch_head + used, sizeof (scratch_head) - used, used)) != DKIM_STAT_OK) {
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
void
sign_mail_using_dkim (blockmail_t *blockmail, header_t *header) /*{{{*/
{
	if (blockmail -> signdkim && header && header -> head) {
		head_t		*cur;
		head_t		*received;
		char		*dkhd;
		const char	*from;
		adkim_t		*adkim;
		char		*ident;
		const char	*use_ident;
		char		*domain;
		char		*ptr;
		int		n;

		received = NULL;
		adkim = NULL;
		ident = NULL;
		for (cur = header -> head; cur; cur = cur -> next)
			if (head_match (cur, "received"))
				received = cur;
			else if ((! ident) && head_match (cur, "from") && (from = head_value (cur)))
				ident = extract_address (from);
		use_ident = NULL;
		if (ident && (domain = strchr (ident, '@'))) {
			++domain;
			for (ptr = domain; *ptr; ++ptr)
				*ptr = tolower (*ptr);
			for (n = 0; n < blockmail -> adkim_count; ++n)
				if (adkim_match (blockmail -> adkim[n], domain)) {
					adkim = blockmail -> adkim[n];
					break;
				}
			if (adkim || adkim_match (blockmail -> signdkim -> default_key, domain))
				use_ident = ident;
		}
		header_remove (header, "dkim-signature");
		if (dkhd = sdkim_sign (blockmail, header, adkim, use_ident, blockmail -> body)) {
			header_insert (header, dkhd, received);
			free (dkhd);
		}
		if (ident)
			free (ident);
	}
}/*}}}*/
