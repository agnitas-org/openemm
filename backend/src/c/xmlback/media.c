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
# include	<string.h>
# include	"xmlback.h"

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
			xmlBufferFree (p -> value);
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
	parm_t	*p;

	if ((p = media_find_parameter (m, pattern)) && p -> value)
		return xml2string (p -> value);
	return NULL;
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
media_type (mediatype_t type) /*{{{*/
{
	switch (type) {
	default:
	case Mediatype_Unspec:
		return NULL;
	case Mediatype_EMail:
		return "email";
	}
}/*}}}*/
const char *
media_typeid (mediatype_t type) /*{{{*/
{
	switch (type) {
	default:
	case Mediatype_Unspec:
		return "";
	case Mediatype_EMail:
		return "0";
	}
}/*}}}*/

