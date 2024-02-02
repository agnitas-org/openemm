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
# include	<ctype.h>
# include	"xmlback.h"

typedef struct vlist { /*{{{*/
	char		*value;
	struct vlist	*next;
	/*}}}*/
}	vlist_t;
typedef struct scanner { /*{{{*/
	char		*key;
	vlist_t		*values;
	struct scanner	*next;
	/*}}}*/
}	scanner_t;
static vlist_t *
vlist_free (vlist_t *v) /*{{{*/
{
	vlist_t		*temp;
	
	while (temp = v) {
		v = v -> next;
		if (temp -> value)
			free (temp -> value);
		free (temp);
	}
	return NULL;
}/*}}}*/
static vlist_t *
vlist_alloc (const char *value) /*{{{*/
{
	vlist_t	*v;
	
	if (v = (vlist_t *) malloc (sizeof (vlist_t))) {
		v -> value = NULL;
		v -> next = NULL;
		if (value && (! (v -> value = strdup (value))))
			v = vlist_free (v);
	}
	return v;
}/*}}}*/
static vlist_t *
vlist_find (vlist_t *v, const char *value) /*{{{*/
{
	for (; v; v = v -> next)
		if (! strcmp (v -> value, value))
			break;
	return v;
}/*}}}*/
static scanner_t *
scanner_free (scanner_t *s) /*{{{*/
{
	scanner_t	*temp;
	
	while (temp = s) {
		s = s -> next;
		if (temp -> key)
			free (temp -> key);
		vlist_free (temp -> values);
		free (temp);
	}
	return NULL;
}/*}}}*/
static scanner_t *
scanner_alloc (const char *keys) /*{{{*/
{
	bool_t		ok;
	scanner_t	*root, *prev, *temp;
	const char	*ptr, *save;
	int		len;
	
	ok = true;
	root = NULL;
	prev = NULL;
	for (ptr = keys; ok && ptr; ) {
		save = ptr;
		if (ptr = strchr (ptr, ',')) {
			len = ptr - save;
			++ptr;
			while (isspace (*ptr))
				++ptr;
		} else {
			len = strlen (save);
		}
		if (temp = (scanner_t *) malloc (sizeof (scanner_t))) {
			if (temp -> key = malloc (len + 1)) {
				strncpy (temp -> key, save, len);
				temp -> key[len] = '\0';
				temp -> values = NULL;
				temp -> next = NULL;
				if (prev)
					prev -> next = temp;
				else
					root = temp;
				prev = temp;
			} else {
				free (temp);
				ok = false;
			}
		} else {
			ok = false;
		}
	}
	if (! ok)
		root = scanner_free (root);
	return root;
}/*}}}*/
static bool_t
scanner_use (scanner_t *s, record_t *r) /*{{{*/
{
	int		refs, seen;
	var_t		*run;
	scanner_t	*srun;
	vlist_t		*value;
	
	for (refs = 0, seen = 0, run = r -> ids; run; run = run -> next) {
		for (srun = s; srun; srun = srun -> next)
			if (! strcmp (srun -> key, run -> var)) {
				++refs;
				if (vlist_find (s -> values, run -> val))
					++seen;
				else if (value = vlist_alloc (run -> val)) {
					value -> next = srun -> values;
					srun -> values = value;
				}
				break;
			}
	}
	return seen < refs;
}/*}}}*/

static int *
filter_indexes (dataset_t *ds, tagpos_t *tp, receiver_t *rec, int *indexes, int *icount) /*{{{*/
{
	int	*rc;

	if (rc = (int *) malloc ((*icount + 1) * sizeof (int))) {
		record_t	*save;
		int		idx, use;
	
		save = ds -> cur;
		for (idx = 0, use = 0; indexes[idx] != -1; ++idx) {
			dataset_select_record (ds, indexes[idx]);
			if (tag_filter (tp -> tag, rec, 10, "i:avail", *icount, "i:index", idx + 1, "i:count", use + 1, NULL))
				rc[use++] = indexes[idx];
		}
		if (use == 0) {
			free (rc);
			rc = NULL;
		} else {
			rc[use] = -1;
		}
		ds -> cur = save;
		*icount = use;
	}
	return rc;
}/*}}}*/

static record_t *
record_free (record_t *r) /*{{{*/
{
	if (r) {
		var_free_all (r -> ids);
		tag_free_all (r -> tag);
		if (r -> data) {
			int	n;
			
			for (n = 0; n < r -> dsize; ++n)
				if (r -> data[n])
					xmlBufferFree (r -> data[n]);
			free (r -> data);
		}
		if (r -> isnull)
			free (r -> isnull);
		if (r -> target_group)
			free (r -> target_group);
		free (r);
	}
	return NULL;
}/*}}}*/
static record_t *
record_alloc (int dsize, int tgsize) /*{{{*/
{
	record_t	*r;
	
	if (r = (record_t *) malloc (sizeof (record_t))) {
		r -> ids = NULL;
		r -> tag = NULL;
		r -> data = (xmlBufferPtr *) malloc (sizeof (xmlBufferPtr) * dsize);
		r -> isnull = (bool_t *) malloc (sizeof (bool_t) * dsize);
		r -> dsize = dsize;
		r -> dpos = 0;
		r -> target_group = tgsize > 0 ? (bool_t *) malloc (tgsize * sizeof (bool_t)) : NULL;
		r -> target_group_count = tgsize;
		if (r -> data && r -> isnull) {
			bool_t	ok = true;
			int	n;

			for (n = 0; n < r -> dsize; ++n) {
				if (! (r -> data[n] = xmlBufferCreate ()))
					ok = false;
				r -> isnull[n] = false;
			}
			if (! ok)
				r = record_free (r);
		} else
			r = record_free (r);
	}
	return r;
}/*}}}*/
static void
record_clear (record_t *r) /*{{{*/
{
	int	n;
	
	r -> ids = var_free_all (r -> ids);
	r -> tag = tag_free_all (r -> tag);
	for (n = 0; n < r -> dsize; ++n) {
		xmlBufferEmpty (r -> data[n]);
		r -> isnull[n] = false;
	}
	r -> dpos = 0;
	if (r -> target_group)
		memset (r -> target_group, 0, r -> target_group_count * sizeof (bool_t));
}/*}}}*/
static dsindex_t *
dsindex_free (dsindex_t *di) /*{{{*/
{
	if (di) {
		if (di -> idlist)
			free (di -> idlist);
		if (di -> indexlist)
			free (di -> indexlist);
		free (di);
	}
	return NULL;
}/*}}}*/
static dsindex_t *
dsindex_free_all (dsindex_t *di) /*{{{*/
{
	dsindex_t	*tmp;
	
	while (tmp = di) {
		di = di -> next;
		dsindex_free (tmp);
	}
	return NULL;
}/*}}}*/
static dsindex_t *
dsindex_alloc (const char *idlist, int isize) /*{{{*/
{
	dsindex_t	*di;
	
	if (di = (dsindex_t *) malloc (sizeof (dsindex_t))) {
		di -> idlist = idlist ? strdup (idlist) : NULL;;
		di -> indexlist = NULL;
		di -> size = 0;
		di -> use = 0;
		di -> next = NULL;
		if ((! idlist) || di -> idlist) {
			if (isize > 0) {
				if (di -> indexlist = (int *) malloc (sizeof (int) * (isize + 1))) {
					di -> indexlist[0] = -1;
					di -> size = isize;
					di -> use = 0;
				} else
					di = dsindex_free (di);
			}
		} else
			di = dsindex_free (di);
	}
	return di;
}/*}}}*/
static bool_t
dsindex_add (dsindex_t *di, int idx) /*{{{*/
{
	if (di -> use >= di -> size) {
		int	nsize;
		int	*nindexes;
		
		nsize = di -> size + 8;
		if (nindexes = (int *) realloc (di -> indexlist, sizeof (int) * (nsize + 1))) {
			di -> indexlist = nindexes;
			di -> size = nsize;
		}
	}
	if (di -> use < di -> size) {
		di -> indexlist[di -> use++] = idx;
		di -> indexlist[di -> use] = -1;
		return true;
	}
	return false;
}/*}}}*/
dataset_t *
dataset_alloc (int dsize, int tgsize) /*{{{*/
{
	dataset_t	*ds;
	
	if (ds = (dataset_t *) malloc (sizeof (dataset_t))) {
		ds -> dsize = dsize;
		ds -> tgsize = tgsize;
		ds -> r = NULL;
		ds -> rsize = 0;
		ds -> ruse = 0;
		ds -> cur = NULL;
		ds -> empty[0] = -1;
		ds -> standard[0] = 0;
		ds -> standard[1] = -1;
		ds -> indexes = NULL;
		ds -> icount = 0;
		ds -> ipos = 0;
	}
	return ds;
}/*}}}*/
dataset_t *
dataset_free (dataset_t *ds) /*{{{*/
{
	if (ds) {
		if (ds -> r) {
			int	n;
			
			for (n = 0; n < ds -> rsize; ++n)
				if (ds -> r[n])
					record_free (ds -> r[n]);
			free (ds -> r);
		}
		dsindex_free_all (ds -> indexes);
		free (ds);
	}
	return NULL;
}/*}}}*/
void
dataset_clear (dataset_t *ds) /*{{{*/
{
	ds -> ruse = 0;
	ds -> cur = NULL;
	ds -> indexes = dsindex_free_all (ds -> indexes);
}/*}}}*/
bool_t
dataset_new_record (dataset_t *ds, var_t *ids) /*{{{*/
{
	bool_t	st;
	
	st = false;
	if (ds -> ruse >= ds -> rsize) {
		int		nsize;
		record_t	**nr;
		int		n;
		
		nsize = ds -> rsize + 8;
		if (nr = (record_t **) realloc (ds -> r, sizeof (record_t *) * nsize)) {
			for (n = ds -> rsize; n < nsize; ++n)
				nr[n] = NULL;
			ds -> r = nr;
			ds -> rsize = nsize;
		}
	}
	if (ds -> ruse < ds -> rsize) {
		record_t	*record;
		
		if (record = ds -> r[ds -> ruse]) {
			record_clear (record);
		} else {
			record = record_alloc (ds -> dsize, ds -> tgsize);
			if (record)
				ds -> r[ds -> ruse] = record;
		}
		if (record) {
			record -> ids = ids;
			ds -> cur = record;
			ds -> ruse++;
			ids = NULL;
			st = true;
		}
	}
	var_free_all (ids);
	return st;
}/*}}}*/
record_t *
dataset_select_first_record (dataset_t *ds) /*{{{*/
{
	ds -> cur = ds -> ruse ? ds -> r[0] : NULL;
	return ds -> cur;
}/*}}}*/
record_t *
dataset_select_record (dataset_t *ds, int pos) /*{{{*/
{
	if (ds -> ruse) {
		if (pos < 0)
			pos = 0;
		else if (pos >= ds -> ruse)
			pos = ds -> ruse - 1;
		ds -> cur = ds -> r[pos];
	} else {
		ds -> cur = NULL;
	}
	return ds -> cur;
}/*}}}*/
int *
dataset_get_indexes (dataset_t *ds, tagpos_t *tp, receiver_t *rec, int *icount, bool_t *dynamic) /*{{{*/
{
	int	*rc = ds -> standard;
	
	*icount = 1;
	*dynamic = false;
	if (tp -> multi) {
		dsindex_t	*cur, *prev;
		int		n;
		
		for (cur = ds -> indexes, prev = NULL; cur; cur = cur -> next) {
			if (! strcmp (cur -> idlist, tp -> multi))
				break;
			prev = cur;
		}
		if (cur) {
			if (prev) {
				prev -> next = cur ->next;
				cur ->next = ds -> indexes;
				ds -> indexes = cur;
			}
		} else {
			if (! tp -> multi[0]) {
				if (cur = dsindex_alloc (tp -> multi, ds -> ruse)) {
					for (n = 0; n < ds -> ruse; ++n)
						dsindex_add (cur, n);
				}
			} else if (cur = dsindex_alloc (tp -> multi, 0)) {
				scanner_t	*scan = scanner_alloc (tp -> multi);
				
				if (scan) {
					for (n = 0; n < ds -> ruse; ++n)
						if (scanner_use (scan, ds -> r[n]))
							if (! dsindex_add (cur, n)) {
								cur = dsindex_free (cur);
								break;
							}
					scanner_free (scan);
				}
			}
			if (cur) {
				cur -> next = ds -> indexes;
				ds -> indexes = cur;
			}
		}
		if (cur) {
			if (cur -> indexlist) {
				rc = cur -> indexlist;
				*icount = cur -> use;
			} else {
				rc = ds -> empty;
				*icount = 0;
			}
		}
	}
	if (rc && (*icount > 0)) {
		if (tp -> tag && tp -> tag -> filter) {
			if (rc = filter_indexes (ds, tp, rec, rc, icount)) {
				*dynamic = true;
			} else {
				rc = ds -> empty;
				*icount = 0;
			}
		}
	}
	return rc;
}/*}}}*/
bool_t
dataset_match (dataset_t *ds, int target_index) /*{{{*/
{
	if (ds -> cur && ds -> cur -> target_group && (target_index >= 0) && (target_index < ds -> cur -> target_group_count))
		return ds -> cur -> target_group[target_index];
	return false;
}/*}}}*/
