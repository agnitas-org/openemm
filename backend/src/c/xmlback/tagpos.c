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
# include	<stdlib.h>
# include	<ctype.h>
# include	"xmlback.h"

static struct { /*{{{*/
	const char	*dyntype;
	int		dtlen;
	/*}}}*/
}	dyntypes[] = { /*{{{*/
# define	MKDY(xxx)	{	xxx,	sizeof (xxx) - 1	}
	MKDY ("multi")
# undef		MKDY
	/*}}}*/
};
static void
setup_dyntype (tagpos_t *t, const char *dyntype) /*{{{*/
{
	int		n;
	int		dtlen;
	const char	*opt;
	
	dtlen = strlen (dyntype);
	opt = NULL;
	for (n = 0; n < sizeof (dyntypes) / sizeof (dyntypes[0]); ++n)
		if ((! strncmp (dyntype, dyntypes[n].dyntype, dyntypes[n].dtlen)) &&
		    ((dtlen == dyntypes[n].dtlen) || (dyntype[dyntypes[n].dtlen] == ':'))) {
			if (dtlen > dyntypes[n].dtlen)
				opt =  dyntype + dyntypes[n].dtlen + 1;
			break;
		}
	switch (n) {
	case 0:		/* multi */
		t -> multi = strdup (opt ? opt : "");
		break;
	}
}/*}}}*/
static void
setup_filter (tagpos_t *t, blockmail_t *blockmail, const char *filter, const char *on_error) /*{{{*/
{
	if (filter) {
		char	*expression;
		
		if (expression = ev_lua_convert (blockmail, filter)) {
			t -> ev = ev_lua_alloc (blockmail, expression);
			if (! t -> ev)
				log_out (blockmail -> lg, LV_ERROR, "Failed to interpret filter \"%s\" (%s)", filter, expression);
			free (expression);
			if (t -> ev && on_error) {
				if (expression = ev_lua_convert (blockmail, on_error)) {
					t -> on_error = ev_lua_alloc (blockmail, expression);
					if (! t -> on_error)
						log_out (blockmail -> lg, LV_ERROR, "Failed to interpret on_error \"%s\" (%s)", on_error, expression);
					free (expression);
				} else
					log_out (blockmail -> lg, LV_ERROR, "Failed to convert on_error \"%s\" as expression", on_error);
			}
		} else
			log_out (blockmail -> lg, LV_ERROR, "Failed to convert filter \"%s\" as expression", filter);
	}
}/*}}}*/
tagpos_t *
tagpos_alloc (void) /*{{{*/
{
	tagpos_t	*t;
	
	if (t = (tagpos_t *) malloc (sizeof (tagpos_t)))
		if (t -> name = xmlBufferCreate ()) {
			t -> hash = 0;
			t -> start = 0;
			t -> end = 0;
			t -> type = TP_NONE;
			t -> tag = NULL;
			t -> sort_enable = false;
			t -> sort_value = -1;
			t -> tname = NULL;
			t -> content = NULL;
			t -> multi = NULL;
			t -> ev = NULL;
			t -> on_error = NULL;
		} else {
			free (t);
			t = NULL;
		}
	return t;
}/*}}}*/
tagpos_t *
tagpos_free (tagpos_t *t) /*{{{*/
{
	if (t) {
		if (t -> name)
			xmlBufferFree (t -> name);
		if (t -> tname)
			free (t -> tname);
		if (t -> content)
			block_free (t -> content);
		if (t -> multi)
			free (t -> multi);
		if (t -> ev)
			ev_lua_free (t -> ev);
		if (t -> on_error)
			ev_lua_free (t -> on_error);
		free (t);
	}
	return NULL;
}/*}}}*/
void
tagpos_find_name (tagpos_t *t) /*{{{*/
{
	int		len;
	const xmlChar	*ptr;
	const xmlChar	pattern[] = "name=";
	int		n;

	if (t -> tname) {
		free (t -> tname);
		t -> tname = NULL;
	}
	len = xmlBufferLength (t -> name);
	ptr = xmlBufferContent (t -> name);
	n = 0;
	while (len > 0) {
		if (*ptr == pattern[n])
			++n;
		else
			n = 0;
		++ptr;
		--len;
		if (! pattern[n])
			break;
	}
	if (len > 0) {
		bool_t		isquote;
		const xmlChar	*start, *end;
		
		if (*ptr == '"') {
			++ptr;
			--len;
			isquote = true;
		} else
			isquote = false;
		start = ptr;
		end = NULL;
		while ((len > 0) && (! end)) {
			if (isquote) {
				if (*ptr == '"')
					end = ptr;
			} else if (isspace (*ptr) || (*ptr == ']') || (*ptr == '/'))
				end = ptr;
			++ptr;
			--len;
		}
		if ((! end) && (! isquote))
			end = ptr;
		if (end) {
			len = end - start;
			if (t -> tname = malloc (len + 1)) {
				memcpy (t -> tname, start, len);
				t -> tname[len] = '\0';
			}
		}
	}
}/*}}}*/
void
tagpos_setup_tag (tagpos_t *t, blockmail_t *blockmail) /*{{{*/
{
	if (! t -> tag) {
		tag_t	*tag;
		
		for (tag = blockmail -> ltag; tag; tag = tag -> next)
			if (((t -> hash == 0) || (tag -> hash == 0) || (t -> hash == tag -> hash)) &&
			    tag_match (tag, xmlBufferContent (t -> name), xmlBufferLength (t -> name))) {
				t -> tag = tag;
				break;
			}
		if (t -> tag && (t -> type & TP_DYNAMIC)) {
			var_t	*p, *type, *filter, *on_error;
			
			type = NULL;
			filter = NULL;
			on_error = NULL;
			for (p = t -> tag -> parm; p; p = p -> next)
				if (p -> val && *(p -> val))
					if ((! type) && (! strcmp (p -> var, "type")))
						type = p;
					else if ((! filter) && (! strcmp (p -> var, "filter")))
						filter = p;
					else if ((! on_error) && (! strcmp (p -> var, "onerror")))
						on_error = p;
			if (type != NULL) {
				setup_dyntype (t, type -> val);
			}
			if (filter != NULL) {
				setup_filter (t, blockmail, filter -> val, on_error ? on_error -> val : NULL);
			}
		}
		if (t -> content)
			block_setup_tagpositions (t -> content, blockmail);
	}
}/*}}}*/
