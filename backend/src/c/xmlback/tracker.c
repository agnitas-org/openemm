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
# include	"xmlback.h"

typedef struct { /*{{{*/
	purl_t	*build;
	int	redirect_index;
	/*}}}*/
}	iad_t;
static iad_t *
iad_free (iad_t *i) /*{{{*/
{
	if (i) {
		if (i -> build)
			purl_free (i -> build);
		free (i);
	}
	return NULL;
}/*}}}*/
static iad_t *
iad_alloc (const char *url, const char *redirect) /*{{{*/
{
	iad_t	*i;
	
	if (i = (iad_t *) malloc (sizeof (iad_t))) {
		i -> build = purl_allocs (url);
		if (i -> build && purl_add_params (i -> build, redirect, ""))
			i -> redirect_index = purl_find_params (i -> build, redirect, 0);
		else
			i = iad_free (i);
	}
	return i;
}/*}}}*/
static inline const char *
pfind (blockmail_t *blockmail, const char *key, const char *dflt) /*{{{*/
{
	var_t	*tmp = var_find (blockmail -> company_info, key);
	
	if (tmp && tmp -> val)
		return tmp -> val;
	return dflt;
}/*}}}*/
static void *
iad_init (blockmail_t *blockmail, xmlBufferPtr content) /*{{{*/
{
	const char	*baseurl = pfind (blockmail, "intelliad-url", "http://t23.intelliad.de/index.php");
	const char	*url_parameter = pfind (blockmail, "intelliad-redirect", "redirect");
	iad_t		*i;

	if (i = iad_alloc (baseurl, url_parameter)) {
		bool_t		st = true;
		int		n;
		const xmlChar	*src, *ptr;
		int		len, plen;
		const char	*key;
	
		for (n = 0, src = xmlBufferContent (content), len = xmlBufferLength (content); st && (n < 6); ++n) {
			int	clen;
		
			ptr = src;
			plen = 0;
			while ((len > 0) && (*src != '-')) {
				clen = xmlCharLength (*src);
				if (clen <= len) {
					len -= clen;
					src += clen;
					plen += clen;
				} else  {
					st = false;
					break;
				}
			}
			if ((*src == '-') && (len > 0)) {
				++src;
				--len;
			}
			if (((n == 5) && (len > 0)) || ((n < 5) && (len <= 0)))
				st = false;
			if (st) {
				switch (n) {
				case 0:		key = "cl";	break;
				case 1:		key = "bm";	break;
				case 2:		key = "bmcl";	break;
				case 3:		key = "cp";	break;
				case 4:		key = "ag";	break;
				case 5:		key = "crid";	break;
				default:	key = NULL;	break;
				}
				st = purl_add_paramn (i -> build, (const byte_t *) key, strlen (key), ptr, plen);
			}
		}
		if (n < 6)
			st = false;
		if (st) {
			char	mid[32];
			int	midlen;
			byte_t	*param;
			int	palen;

			key = "subid";
			midlen = snprintf (mid, sizeof (mid) - 1, "%d|", blockmail -> mailing_id);
			ptr = xmlBufferContent (blockmail -> mailing_name);
			plen = xmlBufferLength (blockmail -> mailing_name);
			palen = midlen + plen;
			if (param = malloc (palen)) {
				memcpy (param, mid, midlen);
				memcpy (param + midlen, ptr, plen);
				st = purl_add_paramn (i -> build, (const byte_t *) key, strlen (key), param, palen);
				free (param);
			} else
				st = false;
		}
		if (! st)
			i = iad_free (i);
	}
	return i;
}/*}}}*/
static bool_t
iad_encode (void *ip, xmlBufferPtr src, xmlBufferPtr dst) /*{{{*/
{
	iad_t		*i = (iad_t *) ip;
	const xmlChar	*url = xmlBufferContent (src);
	int		ulen = xmlBufferLength (src);
	const byte_t	*build;
	int		blen;
	
	purl_update_paramn (i -> build, i -> redirect_index, url, ulen);
	if (build = purl_build (i -> build, NULL, & blen, NULL, NULL))
		xmlBufferAdd (dst, build, blen);
	return build ? true : false;
}/*}}}*/
static void
iad_deinit (void *ip) /*{{{*/
{
	iad_t	*i = (iad_t *) ip;
	
	iad_free (i);
}/*}}}*/
static struct { /*{{{*/
	const char	*name;
	void		*(*init) (blockmail_t *, xmlBufferPtr);
	bool_t		(*encode) (void *, xmlBufferPtr, xmlBufferPtr);
	void		(*deinit) (void *);
	/*}}}*/
}	trackers[] = { /*{{{*/
	{	"intelliAd",	iad_init, iad_encode, iad_deinit	}
	/*}}}*/
};
static track_t *
track_alloc (const char *name, bool_t (*encode) (void *, xmlBufferPtr, xmlBufferPtr), void (*deinit) (void *), void *data) /*{{{*/
{
	track_t	*t;
	
	if (t = (track_t *) malloc (sizeof (track_t))) {
		if (t -> name = strdup (name)) {
			t -> encode = encode;
			t -> deinit = deinit;
			t -> data = data;
			t -> next = NULL;
		} else {
			free (t);
			t = NULL;
		}
	}
	return t;
}/*}}}*/
static track_t *
track_free (track_t *t) /*{{{*/
{
	if (t) {
		free (t -> name);
		if (t -> deinit)
			(*t -> deinit) (t -> data);
		free (t);
	}
	return NULL;
}/*}}}*/
static bool_t
track_fill (track_t *t, xmlBufferPtr src, xmlBufferPtr dst) /*{{{*/
{
	return (*t -> encode) (t -> data, src, dst);
}/*}}}*/
tracker_t *
tracker_alloc (void) /*{{{*/
{
	tracker_t	*t;
	
	if (t = (tracker_t *) malloc (sizeof (tracker_t))) {
		t -> head = NULL;
		t -> tail = NULL;
		t -> scratch[0] = xmlBufferCreate ();
		t -> scratch[1] = xmlBufferCreate ();
		if ((! t -> scratch[0]) || (! t -> scratch[1]))
			t = tracker_free (t);
	}
	return t;
}/*}}}*/
tracker_t *
tracker_free (tracker_t *t) /*{{{*/
{
	if (t) {
		track_t	*tmp;
		
		while (tmp = t -> head) {
			t -> head = t -> head -> next;
			track_free (tmp);
		}
		if (t -> scratch[0])
			xmlBufferFree (t -> scratch[0]);
		if (t -> scratch[1])
			xmlBufferFree (t -> scratch[1]);
		free (t);
	}
	return NULL;
}/*}}}*/
bool_t
tracker_add (tracker_t *t, blockmail_t *blockmail, const char *name, xmlBufferPtr content) /*{{{*/
{
	bool_t	st;
	int	n;
	
	st = false;
	for (n = 0; n < sizeof (trackers) / sizeof (trackers[0]); ++n)
		if (! strcmp (trackers[n].name, name))
			break;
	if (n < sizeof (trackers) / sizeof (trackers[0])) {
		void	*data;
		track_t	*track;
		
		if (data = (*trackers[n].init) (blockmail, content))
			if (track = track_alloc (name, trackers[n].encode, trackers[n].deinit, data)) {
				if (t -> tail)
					t -> tail -> next = track;
				else
					t -> head = track;
				t -> tail = track;
				st = true;
			} else if (trackers[n].deinit)
				(*trackers[n].deinit) (data);
	}
	return st;
}/*}}}*/
bool_t
tracker_fill (tracker_t *t, blockmail_t *blockmail, const xmlChar **url, int *ulength) /*{{{*/
{
	bool_t	st;
	int	src;
	track_t	*run;
	
	st = true;
	src = 0;
	xmlBufferEmpty (t -> scratch[src]);
	xmlBufferAdd (t -> scratch[src], *url, *ulength);
	for (run = t -> head; run && st; run = run -> next) {
		xmlBufferEmpty (t -> scratch[! src]);
		st = track_fill (run, t -> scratch[src], t -> scratch[! src]);
		src = ! src;
	}
	if (st) {
		*url = xmlBufferContent (t -> scratch[src]);
		*ulength = xmlBufferLength (t -> scratch[src]);
	}
	return st;
}/*}}}*/
