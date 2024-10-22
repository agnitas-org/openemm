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
# include	<stdarg.h>
# include	<string.h>
# include	<sys/utsname.h>
# include	"xmlback.h"

# define	SYNC_POSTFIX		".SYNC"

static var_t *
company_info_find (blockmail_t *blockmail, const char *key) /*{{{*/
{
	var_t	*tmp;
	char	*scratch;
	
	if (scratch = malloc (strlen (key) + 64)) {
		sprintf (scratch, "%s[%d]", key, blockmail -> mailing_id);
		tmp = var_find (blockmail -> company_info, scratch);
		free (scratch);
		if (tmp && tmp -> val)
			return tmp;
	}
	if ((tmp = var_find (blockmail -> company_info, key)) && tmp -> val)
		return tmp;
	return NULL;
}/*}}}*/
static bool_t
open_syncfile (blockmail_t *b) /*{{{*/
{
	bool_t		rc;
	int		oflen, nflen;
	char		fname[PATH_MAX + 1];
	char		*ptr;
	int		flen;
	
	rc = false;
	oflen = strlen (b -> fname);
	if (b -> fname[0] == '/') {
		if (oflen < sizeof (fname)) {
			strcpy (fname, b -> fname);
			rc = true;
		}
	} else if (getcwd (fname, sizeof (fname) - 1)) {
		nflen = strlen (fname);
		if (oflen + nflen + 1 < sizeof (fname)) {
			fname[nflen++] = '/';
			strcpy (fname + nflen, b -> fname);
			rc = true;
		}
	}
	if (rc) {
		if (ptr = strrchr (fname, '/'))
			++ptr;
		else
			ptr = fname;
		if (ptr = strchr (ptr, '.'))
			flen = ptr - fname;
		else
			flen = strlen (fname);
		if (flen + sizeof (SYNC_POSTFIX) <= sizeof (b -> syfname)) {
			strncpy (b -> syfname, fname, flen);
			strcpy (b -> syfname + flen, SYNC_POSTFIX);
			if ((! (b -> syfp = fopen (b -> syfname, "a+"))) || (fseek (b -> syfp, 0, SEEK_SET) == -1))
				rc = false;
		} else
			rc = false;
	}
	return rc;
}/*}}}*/
blockmail_t *
blockmail_alloc (const char *fname, bool_t syncfile, log_t *lg) /*{{{*/
{
	blockmail_t	*b;
	int		n;
	struct utsname	utsbuf;
	const char	*ptr;

	if (b = (blockmail_t *) malloc (sizeof (blockmail_t))) {
		b -> fname = fname;

		b -> syfname[0] = '\0';
		b -> syfp = NULL;
		b -> syeof = false;
		b -> lg = lg;
		b -> eval = NULL;
		b -> purl = NULL;
		b -> html = NULL;
		b -> cvt = NULL;
		b -> target_ids = NULL;
		b -> target_ids_count = 0;
		b -> tracker = NULL;

		b -> raw = false;
		b -> output = NULL;
		b -> outputdata = NULL;
		b -> counter = NULL;
		b -> active = false;
		b -> reason = REASON_UNSPEC;
		b -> reason_detail = 0;
		b -> reason_custom = NULL;
		b -> control = NULL;
		b -> body = NULL;
		b -> rblocks = NULL;

		b -> nodename[0] = '\0';
		if (uname (& utsbuf) != -1) {
			strncpy (b -> nodename, utsbuf.nodename, sizeof (b -> nodename) - 1);
			for (n = 0; b -> nodename[n] && (b -> nodename[n] != '.') && (n < sizeof (b -> nodename) - 1); ++n)
				;
			b -> nodename[n] = '\0';
		}
		b -> smap = string_map_setup ();
		b -> licence_id = 0;
		b -> owner_id = 0;
		if (ptr = getenv ("LICENCE"))
			b -> owner_id = atoi (ptr);
		b -> company_id = -1;
		b -> company_token = NULL;
		b -> allow_unnormalized_emails = false;
		b -> company_info = NULL;
		b -> mailinglist_id = -1;
		b -> mailinglist_name = NULL;
		b -> mailing_id = -1;
		b -> mailing_name = NULL;
		b -> mailing_description = NULL;
		b -> maildrop_status_id = -1;
		b -> status_field = '\0';
		b -> senddate = NULL;
		b -> epoch = 0;
		b -> rdir_content_links = false;
		b -> omit_list_informations_for_doi = true;
		b -> add_honeypot_link = Add_Top;
		b -> domain = NULL;
		b -> mailtrack = NULL;
		
		b -> email.subject = NULL;
		b -> email.from = NULL;
		b -> auto_url = NULL;
		b -> auto_url_is_dynamic = false;
		b -> auto_url_prefix = NULL;
		b -> gui = false;
		b -> anon = false;
		b -> anon_preserve_links = false;
		b -> selector = NULL;
		b -> convert_to_entities = false;
		b -> onepixel_url = NULL;
		b -> honeypot_url = NULL;
		b -> link_maker = NULL;
		b -> anon_url = NULL;
		b -> secret_key = NULL;
		b -> secret_timestamp = 0;
		b -> secret_timestamp1 = 0;
		b -> secret_timestamp2 = 0;
		b -> secret_uid = NULL;
		b -> secret_sig = NULL;
		b -> total_subscribers = 0;

		b -> blocknr = 0;
		b -> innerboundary = NULL;
		b -> outerboundary = NULL;
		b -> attachboundary = NULL;
		
		DO_ZERO (b, block);
		DO_ZERO (b, media);
		DO_ZERO (b, mailtypedefinition);

		b -> ltag = NULL;
		b -> taglist_count = 0;
		b -> clear_empty_dyn_block = true;
		b -> clear_empty_dyn_block_without_dvalue = true;

		b -> gtag = NULL;
		b -> globaltag_count = 0;

		b -> tfunc = NULL;

		b -> dyn = NULL;
		b -> dynamic_count = 0;
		
		b -> mtbuf[0] = NULL;
		b -> mtbuf[1] = NULL;
		b -> relaxed_url_resolver = false;
		b -> enhanced_url_resolver = false;
		
		DO_ZERO (b, url);
		DO_ZERO (b, link_resolve);

		b -> virtuals = NULL;
		
		DO_ZERO (b, field);
		b -> mailtype_index = -1;
		
		b -> target_groups = NULL;
		b -> target_groups_count = 0;

		b -> receiver_count = 0;

		b -> fqdn = NULL;
		b -> pointintime = 0;
		b -> xconv = NULL;
		b -> mfrom = NULL;
		b -> revalidate_mfrom = false;
		b -> signdkim = NULL;
		DO_ZERO (b, adkim);
		b -> spf = NULL;
		b -> vip = NULL;
		b -> onepix_template = NULL;
		b -> force_ecs_uid = false;
		b -> uid_version = 0;

		if ((syncfile && (! open_syncfile (b))) ||
		    (! (b -> eval = eval_alloc (b))) ||
		    (! (b -> purl = purl_alloc (NULL))) ||
		    (! (b -> html = html_alloc ())) ||
		    ( !(b -> cvt = cvt_alloc ())) ||
		    (! (b -> control = buffer_alloc (4096))) ||
		    (! (b -> body = buffer_alloc (65536))) ||
		    (! (b -> link_maker = buffer_alloc (1024))) ||
		    (! (b -> secret_uid = buffer_alloc (1024))) ||
		    (! (b -> secret_sig = buffer_alloc (1024))) ||
		    (! (b -> mtbuf[0] = xmlBufferCreate ())) ||
		    (! (b -> mtbuf[1] = xmlBufferCreate ())) ||
		    (! (b -> xconv = xconv_alloc (250))) ||
		    (! (b -> spf = spf_alloc ()))) {
			b = blockmail_free (b);
		} else {
			b -> control -> spare = 1024;
			b -> body -> spare = 8192;
			xmlBufferCCat (b -> mtbuf[0], "0");
			xmlBufferCCat (b -> mtbuf[1], "1");
		}
	}
	return b;
}/*}}}*/
blockmail_t *
blockmail_free (blockmail_t *b) /*{{{*/
{
	if (b) {
		if (b -> syfp)
			fclose (b -> syfp);
		if (b -> eval)
			eval_free (b -> eval);
		if (b -> purl)
			purl_free (b -> purl);
		if (b -> html)
			html_free (b -> html);
		if (b -> cvt)
			cvt_free (b -> cvt);
		if (b -> tracker)
			tracker_free (b -> tracker);
		if (b -> counter)
			counter_free_all (b -> counter);
		if (b -> reason_custom)
			free (b -> reason_custom);
		if (b -> control)
			buffer_free (b -> control);
		if (b -> body)
			buffer_free (b -> body);
		if (b -> rblocks)
			rblock_free_all (b -> rblocks);
		if (b -> mtbuf[0])
			xmlBufferFree (b -> mtbuf[0]);
		if (b -> mtbuf[1])
			xmlBufferFree (b -> mtbuf[1]);
		if (b -> xconv)
			xconv_free (b -> xconv);
		if (b -> smap)
			string_map_done (b -> smap);
		if (b -> company_token)
			free (b -> company_token);
		if (b -> company_info)
			var_free_all (b -> company_info);
		if (b -> mailinglist_name)
			xmlBufferFree (b -> mailinglist_name);
		if (b -> mailing_name)
			xmlBufferFree (b -> mailing_name);
		if (b -> mailing_description)
			xmlBufferFree (b -> mailing_description);
		if (b -> senddate)
			free (b -> senddate);
		if (b -> domain)
			free (b -> domain);
		if (b -> mailtrack)
			mailtrack_free (b -> mailtrack);
		
		if (b -> email.subject)
			xmlBufferFree (b -> email.subject);
		if (b -> email.from)
			xmlBufferFree (b -> email.from);
		if (b -> auto_url)
			xmlBufferFree (b -> auto_url);
		if (b -> auto_url_prefix)
			free (b -> auto_url_prefix);
		if (b -> selector)
			free (b -> selector);
		if (b -> onepixel_url)
			xmlBufferFree (b -> onepixel_url);
		if (b -> honeypot_url)
			xmlBufferFree (b -> honeypot_url);
		if (b -> link_maker)
			buffer_free (b -> link_maker);
		if (b -> anon_url)
			xmlBufferFree (b -> anon_url);
		if (b -> secret_key)
			xmlBufferFree (b -> secret_key);
		if (b -> secret_uid)
			buffer_free (b -> secret_uid);
		if (b -> secret_sig)
			buffer_free (b -> secret_sig);
		if (b -> innerboundary)
			free (b -> innerboundary);
		if (b -> outerboundary)
			free (b -> outerboundary);
		if (b -> attachboundary)
			free (b -> attachboundary);

		DO_FREE (b, block);
		DO_FREE (b, media);
		DO_FREE (b, mailtypedefinition);

		if (b -> ltag)
			tag_free_all (b -> ltag);
		if (b -> gtag)
			tag_free_all (b -> gtag);
		if (b -> tfunc)
			tfunc_free (b -> tfunc);
		if (b -> dyn)
			dyn_free_all (b -> dyn);
		
		DO_FREE (b, url);
		DO_FREE (b, link_resolve);
		if (b -> virtuals)
			var_free_all (b -> virtuals);
		DO_FREE (b, field);
		if (b -> target_groups)
			free (b -> target_groups);

		if (b -> mfrom)
			free (b -> mfrom);
		if (b -> signdkim)
			sdkim_free (b -> signdkim);
		DO_FREE (b, adkim);
		if (b -> spf)
			spf_free (b -> spf);
		if (b -> vip)
			xmlBufferFree (b -> vip);
		if (b -> onepix_template)
			xmlBufferFree (b -> onepix_template);
		free (b);
	}
	return NULL;
}/*}}}*/
time_t
blockmail_now (blockmail_t *b) /*{{{*/
{
	return b -> pointintime ? b -> pointintime : time (NULL);
}/*}}}*/
bool_t
blockmail_count (blockmail_t *b, const char *mediatype, int subtype, int chunks, long bytes, int bcccount) /*{{{*/
{
	counter_t	*run, *prv;

	for (run = b -> counter, prv = NULL; run; run = run -> next)
		if ((! strcmp (run -> mediatype, mediatype)) && (run -> subtype == subtype))
			break;
		else
			prv = run;
	if (run) {
		if (prv) {
			prv -> next = run -> next;
			run -> next = b -> counter;
			b -> counter = run;
		}
	} else if (run = counter_alloc (mediatype, subtype)) {
		run -> next = b -> counter;
		b -> counter = run;
	}
	if (run) {
		if ((bytes == 0) || (! b -> active)) {
			run -> unitskip++;
		} else {
			run -> unitcount++;
			run -> chunkcount += chunks;
			run -> bytecount += bytes;
			run -> bccunitcount += bcccount;
			run -> bccbytecount += bytes * bcccount;
		}
	}
	return run ? true : false;
}/*}}}*/
void
blockmail_count_sort (blockmail_t *b) /*{{{*/
{
	if (b -> counter && b -> counter -> next) {
		counter_t	*run, *prv, *tmp, *tpv;
		int		cmp;
		
		for (run = b -> counter, prv = NULL; run; ) {
			for (tmp = run -> next, tpv = run; tmp; tmp = tmp -> next) {
				cmp = strcmp (run -> mediatype, tmp -> mediatype);
				if (cmp == 0)
					cmp = run -> subtype - tmp -> subtype;
				if (cmp > 0)
					break;
				else
					tpv = tmp;
			}
			if (tmp) {
				tpv -> next = tmp -> next;
				tmp -> next = run;
				if (prv)
					prv -> next = tmp;
				else
					b -> counter = tmp;
				run = tmp;
			} else {
				prv = run;
				run = run -> next;
			}
		}
	}
}/*}}}*/
/*
 * each line in the syncfile has three fields, separated by semicolon:
 * - the customer number
 * - the size of the created mail for this customer
 * - the mediatype
 * - the subtype
 */
void
blockmail_unsync (blockmail_t *b) /*{{{*/
{
	if (b -> syfp) {
		fclose (b -> syfp);
		b -> syfp = NULL;
		unlink (b -> syfname);
	}
}/*}}}*/
bool_t
blockmail_insync (blockmail_t *b, receiver_t *rec, int bcccount) /*{{{*/
{
	bool_t	rc;
	
	rc = false;
	if (b -> syfp && (! b -> syeof)) {
		char	*inp;
		char	buf[512];
		char	*ptr;
		char	*size, *mtyp, *temp;
		int	styp;
		int	ncid;
		int	chunks;
		long	bytes;
		
		while (inp = fgets (buf, sizeof (buf) - 1, b -> syfp)) {
			ncid = atoi (buf);
			bytes = 0;
			mtyp = NULL;
			styp = 0;
			if (ptr = strchr (buf, ';')) {
				*ptr++ = '\0';
				size = ptr;
				if (ptr = strchr (ptr, ';')) {
					*ptr++ = '\0';
					mtyp = ptr;
					if (ptr = strchr (ptr, ';')) {
						*ptr++ = '\0';
						temp = ptr;
						if (ptr = strchr (ptr, ';')) {
							*ptr++ = '\0';
							styp = atoi (temp);
							temp = ptr;
							if (ptr = strchr (ptr, '\n'))
								*ptr = '\0';
							chunks = atoi (temp);
							if (ncid) {
								bytes = atol (size);
								blockmail_count (b, mtyp, styp, chunks, bytes, bcccount);
							}
						}
					}
				}
			}
			if (mtyp && (rec -> customer_id == ncid) && (! strcmp (mtyp, rec -> mid)) && (styp == rec -> mailtype)) {
				if ((bytes > 0) && b -> mailtrack)
					mailtrack_add (b -> mailtrack, rec);
				rc = true;
				break;
			}
		}
		if ((! inp) || feof (b -> syfp))
			b -> syeof = true;
	}
	return rc;
}/*}}}*/
bool_t
blockmail_tosync (blockmail_t *b, receiver_t *rec, int bcccount) /*{{{*/
{
	bool_t	rc;

	rc = true;
	if (b -> syfp) {
		long	pos;
		
		if (! b -> syeof) {
			pos = ftell (b -> syfp);
			if ((pos == -1) || (fseek (b -> syfp, 0, SEEK_END) == -1))
				rc = false;
		} else
			pos = -1;
		if (rc) {
			if ((fprintf (b -> syfp, "%d;%ld;%s;%d;%d\n", rec -> customer_id, rec -> size, rec -> mid, rec -> mailtype, rec -> chunks) == -1) ||
			    (fflush (b -> syfp) == -1))
				rc = false;
			if (rc && (pos != -1) && (fseek (b -> syfp, pos, SEEK_SET) == -1))
				rc = false;
		}
	}
	if (rc && rec -> customer_id) {
		rc = blockmail_count (b, rec -> mid, rec -> mailtype, rec -> chunks, rec -> size, bcccount);
		if (b -> active && b -> mailtrack)
			mailtrack_add (b -> mailtrack, rec);
	}
	return rc;
}/*}}}*/

/*
 * extract the relevant informations from the media types and add it
 * to the current variables
 */
static void
replace (xmlBufferPtr *buf, media_t *m, const char *mid) /*{{{*/
{
	parm_t		*p;

	if (p = media_find_parameter (m, mid)) {
		if (*buf)
			xmlBufferEmpty (*buf);
		else
			*buf = xmlBufferCreate ();
		if (p -> value)
			xmlBufferAdd (*buf, xmlBufferContent (p -> value), xmlBufferLength (p -> value));
	}
}/*}}}*/
static inline void
cat (xmlBufferPtr buf, const char *what, ...) /*{{{*/
{
	va_list	par;
	void	*ptr;
	int	n;
	
	va_start (par, what);
	for (n = 0; what[n]; ++n)
		if (ptr = va_arg (par, void *))
			if (what[n] == 's')
				xmlBufferCCat (buf, (const char *) ptr);
			else if (what[n] == 'b')
				xmlBufferAdd (buf, xmlBufferContent ((xmlBufferPtr) ptr), xmlBufferLength ((xmlBufferPtr) ptr));
	va_end (par);
}/*}}}*/
bool_t
blockmail_extract_mediatypes (blockmail_t *b) /*{{{*/
{
	int	n;
	bool_t	st;

	st = true;
	for (n = 0; n < b -> media_count; ++n) {
		media_t	*m = b -> media[n];
		
		switch (m -> type) {
		case Mediatype_EMail:
			replace (& b -> email.subject, m, "subject");
			replace (& b -> email.from, m, "from");
			break;
		case Mediatype_Unspec:
			log_out (b -> lg, LV_ERROR, "Invalid/unsupported target %d", m -> type);
			st = false;
			break;
		}
	}
	return st;
}/*}}}*/

void
blockmail_setup_senddate (blockmail_t *b, const char *date, time_t epoch) /*{{{*/
{
	if (b -> senddate)
		free (b -> senddate);
	b -> senddate = tf_parse_date (date);
	if (epoch) {
		b -> epoch = epoch;
	} else if (b -> senddate) {
		struct tm	temp;
		
		temp.tm_sec = b -> senddate[5];
		temp.tm_min = b -> senddate[4];
		temp.tm_hour = b -> senddate[3];
		temp.tm_mday = b -> senddate[2];
		temp.tm_mon = b -> senddate[1] - 1;
		temp.tm_year = b -> senddate[0] - 1900;
		temp.tm_isdst = -1;
		b -> epoch = mktime (& temp);
		if (b -> epoch == (time_t) -1) {
			time (& b -> epoch);
		}
	} else {
		time (& b -> epoch);
	}
}/*}}}*/
void
blockmail_setup_company_configuration (blockmail_t *b) /*{{{*/
{
	var_t	*tmp;
	
	if ((tmp = var_find (b -> company_info, "rdir.UseRdirContextLinks")) && tmp -> val) {
		b -> rdir_content_links = atob (tmp -> val);
	}
	if (tmp = company_info_find (b, "omit-list-informations-for-doi")) {
		b -> omit_list_informations_for_doi = atob (tmp -> val);
	}
	if (tmp = company_info_find (b, "add-honeypot-link")) {
		b -> add_honeypot_link = add_parse (tmp -> val, Add_Top);
	}
}/*}}}*/
void
blockmail_setup_mfrom (blockmail_t *b) /*{{{*/
{
	var_t	*tmp;
	
	if ((tmp = var_find (b -> company_info, "_envelope_from")) && tmp -> val) {
		b -> mfrom = strdup (tmp -> val);
		if ((tmp = var_find (b -> company_info, "_envelope_forced")) && tmp -> val && (! atob (tmp -> val))) {
			b -> revalidate_mfrom = true;
		}
	}
	if ((! b -> revalidate_mfrom) && (tmp = company_info_find (b, "revalidate-envelope-from")) && atob (tmp -> val)) {
		b -> revalidate_mfrom = true;
	}
	if ((! b -> mfrom) && b -> email.from) {
		const xmlChar	*cont = xmlBufferContent (b -> email.from);
		int		len = xmlBufferLength (b -> email.from);
		int		n, start, end;
		char		quote;
		char		ch;
		int		clen;
		
		for (n = 0, start = -1, end = -1, quote = '\0'; n < len; ) {
			ch = cont[n];
			clen = xmlCharLength (ch);
			if (clen == 1) {
				if (quote) {
					if (ch == quote)
						quote = '\0';
				} else if ((start == -1) && (ch == '<')) {
					start = n + 1;
				} else if ((start != -1) && (end == -1)) {
					if (ch == '>')
						end = n;
				} else if (ch == '"') {
					quote = ch;
				}
			}
			n += clen;
		}
		if ((start != -1) && (end != -1) && (start < end) && (b -> mfrom = malloc (end - start + 1))) {
			memcpy (b -> mfrom, cont + start, end - start);
			b -> mfrom[end - start] = '\0';
		}
	}
}/*}}}*/
void
blockmail_setup_dkim (blockmail_t *b) /*{{{*/
{
	var_t	*dom, *key, *ident, *sel, *col;
	bool_t	r, z;
	var_t	*tmp;
	
	dom = NULL;
	key = NULL;
	ident = NULL;
	sel = NULL;
	col = NULL;
	r = false;
	z = false;
	for (tmp = b -> company_info; tmp; tmp = tmp -> next)
		if (var_match (tmp, "_dkim_domain")) {
			dom = tmp;
		} else if (var_match (tmp, "_dkim_key")) {
			key = tmp;
		} else if (var_match (tmp, "_dkim_ident")) {
			ident = tmp;
		} else if (var_match (tmp, "_dkim_selector")) {
			sel = tmp;
		} else if (var_match (tmp, "_dkim_column")) {
			col = tmp;
		} else if (var_match (tmp, "_dkim_report")) {
			if (tmp -> val && atob (tmp -> val))
				r = true;
		} else if (var_match (tmp, "_dkim_z")) {
			if (tmp -> val && strchr (tmp -> val, b -> status_field))
				z = true;
		}
	if (dom && key && sel)
		b -> signdkim = sdkim_alloc (b, dom -> val, key -> val, ident ? ident -> val : NULL, sel -> val, col ? col -> val : NULL, r, z);
}/*}}}*/
void
blockmail_setup_vip_block (blockmail_t *b) /*{{{*/
{
	if (b -> smap) {
		var_t	*tmp;

		if (tmp = company_info_find (b, "vip")) {
			int		vlen;
			xmlBufferPtr	temp;

			vlen = strlen (tmp -> val);
			if (temp = xmlBufferCreateSize (vlen + 1)) {
				xmlBufferCCat (temp, tmp -> val);
				b -> vip = string_map (temp, b -> smap, NULL);
				xmlBufferFree (temp);
			}
		}
	}
}/*}}}*/
void
blockmail_setup_onepixel_template (blockmail_t *b) /*{{{*/
{
	var_t	*tmp;
	
	if (tmp = company_info_find (b, "onepixel-template")) {
		if (b -> onepix_template)
			xmlBufferEmpty (b -> onepix_template);
		else
			b -> onepix_template = xmlBufferCreate ();
		if (b -> onepix_template)
			xmlBufferCCat (b -> onepix_template, tmp -> val);
	}
}/*}}}*/
void
blockmail_setup_tagpositions (blockmail_t *b) /*{{{*/
{
	var_t	*tmp;
	
	if (tmp = company_info_find (b, "clear-empty-dyn-block"))
		b -> clear_empty_dyn_block = atob (tmp -> val);
	if (tmp = company_info_find (b, "clear-empty-dyn-block-enhanced"))
		b -> clear_empty_dyn_block_without_dvalue = atob (tmp -> val);
	if (b -> ltag) {
		int	n, m;
		dyn_t	*d, *dd;
		
		for (n = 0; n < b -> block_count; ++n)
			block_setup_tagpositions (b -> block[n], b);
		for (d = b -> dyn; d; d = d -> next) {
			for (dd = d; dd; dd = dd -> sibling)
				for (m = 0; m < dd -> block_count; ++m)
					block_setup_tagpositions (dd -> block[m], b);
		}
	}
}/*}}}*/
void
blockmail_setup_auto_url_prefix (blockmail_t *b, const char *nprefix) /*{{{*/
{
	if (b -> auto_url_prefix)
		free (b -> auto_url_prefix);
	b -> auto_url_prefix = nprefix && *nprefix ? strdup (nprefix) : NULL;
}/*}}}*/
void
blockmail_setup_anon (blockmail_t *b, bool_t anon, bool_t anon_preserve_links) /*{{{*/
{
	b -> anon = anon;
	b -> anon_preserve_links = anon_preserve_links;
}/*}}}*/
void
blockmail_setup_selector (blockmail_t *b, const char *selector) /*{{{*/
{
	if (b -> selector)
		free (b -> selector);
	b -> selector = selector ? strdup (selector) : NULL;
}/*}}}*/
static int
find_preevaluated_index (blockmail_t *blockmail, long target_id) /*{{{*/
{
	int	n;
	
	for (n = 0; n < blockmail -> target_groups_count; ++n)
		if (blockmail -> target_groups[n] == target_id)
			return n;
	return -1;
}/*}}}*/
void
blockmail_setup_preevaluated_targets (blockmail_t *blockmail) /*{{{*/
{
	if (blockmail -> target_groups) {
		int	n;
		dyn_t	*cur, *sib;
		
		for (n = 0; n < blockmail -> block_count; ++n) {
			blockmail -> block[n] -> target_index = find_preevaluated_index (blockmail, blockmail -> block[n] -> target_id);
		}
		for (cur = blockmail -> dyn; cur; cur = cur -> next) {
			cur -> target_index = find_preevaluated_index (blockmail, cur -> target_id);
			for (sib = cur -> sibling; sib; sib = sib -> sibling)
				sib -> target_index = find_preevaluated_index (blockmail, sib -> target_id);
		}
	}
}/*}}}*/
