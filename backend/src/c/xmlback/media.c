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
# include	<string.h>
# include	"xmlback.h"

pval_t *
pval_alloc (void) /*{{{*/
{
	pval_t	*p;
	
	if (p = (pval_t *) malloc (sizeof (pval_t))) {
		p -> v = NULL;
		p -> next = NULL;
	}
	return p;
}/*}}}*/
pval_t *
pval_free (pval_t *p) /*{{{*/
{
	if (p) {
		if (p -> v)
			xmlBufferFree (p -> v);
		free (p);
	}
	return NULL;
}/*}}}*/
pval_t *
pval_free_all (pval_t *p) /*{{{*/
{
	pval_t	*tmp;
	
	while (tmp = p) {
		p = p -> next;
		pval_free (tmp);
	}
	return NULL;
}/*}}}*/
parm_t *
parm_alloc (void) /*{{{*/
{
	parm_t	*p;
	
	if (p = (parm_t *) malloc (sizeof (parm_t))) {
		p -> name = NULL;
		p -> value = NULL;
		p -> next = NULL;
	}
	return p;
}/*}}}*/
parm_t *
parm_free (parm_t *p) /*{{{*/
{
	if (p) {
		if (p -> name)
			free (p -> name);
		if (p -> value)
			pval_free_all (p -> value);
		free (p);
	}
	return NULL;
}/*}}}*/
parm_t *
parm_free_all (parm_t *p) /*{{{*/
{
	parm_t	*tmp;
	
	while (tmp = p) {
		p = p -> next;
		parm_free (tmp);
	}
	return NULL;
}/*}}}*/
xmlBufferPtr
parm_valuecat (parm_t *p, const char *sep) /*{{{*/
{
	xmlBufferPtr	rc;
	
	rc = NULL;
	if (p && p -> value && (rc = xmlBufferCreate ())) {
		pval_t	*tmp;
		
		for (tmp = p -> value; tmp; tmp = tmp -> next) {
			if (sep && (tmp != p -> value))
				xmlBufferCCat (rc, sep);
			if (tmp -> v)
				xmlBufferAdd (rc, xmlBufferContent (tmp -> v), xmlBufferLength (tmp -> v));
		}
	}
	return rc;
}/*}}}*/
media_t *
media_alloc (void) /*{{{*/
{
	media_t	*m;
	
	if (m = (media_t *) malloc (sizeof (media_t))) {
		m -> type = Mediatype_EMail;
		m -> prio = 0;
		m -> stat = MS_Active;
		m -> empty = false;
		m -> parm = NULL;
	}
	return m;
}/*}}}*/
media_t *
media_free (media_t *m) /*{{{*/
{
	if (m) {
		if (m -> parm)
			parm_free_all (m -> parm);
		free (m);
	}
	return NULL;
}/*}}}*/
bool_t
media_set_type (media_t *m, const char *type) /*{{{*/
{
	return media_parse_type (type, & m -> type);
}/*}}}*/
bool_t
media_set_priority (media_t *m, long prio) /*{{{*/
{
	m -> prio = prio;
	return true;
}/*}}}*/
bool_t
media_set_status (media_t *m, const char *status) /*{{{*/
{
	if (! strcmp (status, "unused"))
		m -> stat = MS_Unused;
	else if (! strcmp (status, "inactive"))
		m -> stat = MS_Inactive;
	else if (! strcmp (status, "active"))
		m -> stat = MS_Active;
	else
		return false;
	return true;
}/*}}}*/
parm_t *
media_find_parameter (media_t *m, const char *name) /*{{{*/
{
	parm_t	*run;
	
	for (run = m -> parm; run; run = run -> next)
		if (! strcasecmp (run -> name, name))
			break;
	return run;
}/*}}}*/
static char *
find_value (media_t *m, const char *pattern) /*{{{*/
{
	char	*rc;
	parm_t	*p;
	pval_t	*v;
	
	rc = NULL;
	if (p = media_find_parameter (m, pattern)) {
		for (v = p -> value; v && (! v -> v); v = v -> next)
			;
		if (v)
			rc = xml2string (v -> v);
	}
	return rc;
}/*}}}*/
void
media_postparse (media_t *m, blockmail_t *blockmail) /*{{{*/
{
	char	*val;

	if ((blockmail -> status_field != 'A') && (blockmail -> status_field != 'T')) {
		if (val = find_value (m, "skipempty")) {
			m -> empty = atob (val);
			free (val);
		}
	}
}/*}}}*/

bool_t
media_parse_type (const char *str, mediatype_t *type) /*{{{*/
{
	if (! strcmp (str, "email"))
		*type = Mediatype_EMail;
	else
		return false;
	return true;
}/*}}}*/
const char *
media_typeid (mediatype_t type) /*{{{*/
{
	switch (type) {
	case Mediatype_Unspec:
		return "";
	case Mediatype_EMail:
		return "0";
	}
	return "";
}/*}}}*/
