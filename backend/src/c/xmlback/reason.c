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
# include	"xmlback.h"

/* possible reasons for inactive user */
typedef enum { /*{{{*/
	Reason_Unspec,
	Reason_No_Media,
	Reason_Empty_Document,
	Reason_Templating_Failed,
	Reason_Unmatched_Media,
	Reason_Reject,
	Reason_Custom
	/*}}}*/
}	rtyp_t;
struct reason { /*{{{*/
	rtyp_t		typ;		/* reason type 			*/
	buffer_t	*detail;	/* detailed information		*/
	/*}}}*/
};

static void	reason_vset (reason_t *r, rtyp_t typ, const char *format, va_list par) __attribute__ ((format (printf, 3, 0)));
static void
reason_vset (reason_t *r, rtyp_t typ, const char *format, va_list par) /*{{{*/
{
	if (r && (r -> typ == Reason_Unspec)) {
		r -> typ = typ;
		buffer_sets (r -> detail, "skip=");
		buffer_vformat (r -> detail, format, par);
	}
}/*}}}*/
static void	reason_set (reason_t *r, rtyp_t typ, const char *format, ...) __attribute__ ((format (printf, 3, 4)));
static void
reason_set (reason_t *r, rtyp_t typ, const char *format, ...) /*{{{*/
{
	va_list	par;
	
	va_start (par, format);
	reason_vset (r, typ, format, par);
	va_end (par);
}/*}}}*/

reason_t *
reason_alloc (void) /*{{{*/
{
	reason_t	*r;
	
	if (r = (reason_t *) malloc (sizeof (reason_t))) {
		r -> typ = Reason_Unspec;
		r -> detail = buffer_alloc (1024);
		if (! r -> detail)
			r = reason_free (r);
	}
	return r;
}/*}}}*/
reason_t *
reason_free (reason_t *r) /*{{{*/
{
	if (r) {
		if (r -> detail)
			buffer_free (r -> detail);
		free (r);
	}
	return NULL;
}/*}}}*/
void
reason_reset (reason_t *r) /*{{{*/
{
	if (r) {
		r -> typ = Reason_Unspec;
		buffer_clear (r -> detail);
	}
}/*}}}*/
const char *
reason_build (reason_t *r, char *dsn, int dsnsize) /*{{{*/
{
	const char	*rc;
	
	if (dsn)
		snprintf (dsn, dsnsize, "1.1.%d", r ? r -> typ : 0);
	if (r && (r -> typ != Reason_Unspec) && (rc = buffer_string (r -> detail)))
		return rc;
	return "skip=unspec";
}/*}}}*/
void
reason_no_media (reason_t *r, mediatype_t mediatype) /*{{{*/
{
	const char	*mt = media_type (mediatype);
	
	if (mt)
		reason_set (r, Reason_No_Media, "no-media:%s", mt);
	else
		reason_set (r, Reason_No_Media, "no-media:%d", mediatype);
}/*}}}*/
void
reason_unmatched_media (reason_t *r) /*{{{*/
{
	reason_set (r, Reason_Unmatched_Media, "unmatched-media");
}/*}}}*/
void
reason_reject (reason_t *r, int exit_code) /*{{{*/
{
	reason_set (r, Reason_Reject, "exit:%d", exit_code);
}/*}}}*/
void
reason_empty_document (reason_t *r, block_t *block) /*{{{*/
{
	reason_set (r, Reason_Empty_Document, "empty-document:%s", block -> cid ? block -> cid : "");
}/*}}}*/
void
reason_template_failure (reason_t *r, block_t *block, const char *message) /*{{{*/
{
	reason_set (r, Reason_Templating_Failed, "templating-failed:%s:%s", block -> cid ? block -> cid : "", message ? message : "");
}/*}}}*/
void
reason_custom (reason_t *r, const char *custom) /*{{{*/
{
	reason_set (r, Reason_Custom, "custom:%s", custom);
}/*}}}*/
