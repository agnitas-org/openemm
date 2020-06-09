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
# include	<fcntl.h>
# include	<sys/types.h>
# include	<sys/stat.h>
# include	<dirent.h>
# include	"agn.h"

static centry_t *
centry_free (centry_t *c) /*{{{*/
{
	if (c) {
		if (c -> key)
			free (c -> key);
		if (c -> data)
			free (c -> data);
		free (c);
	}
	return NULL;
}/*}}}*/
static centry_t *
centry_alloc (hash_t hash, const byte_t *key, int klen, const byte_t *data, int dlen) /*{{{*/
{
	centry_t	*c;
	
	if (c = (centry_t *) malloc (sizeof (centry_t))) {
		c -> hash = hash;
		c -> key = NULL;
		c -> klen = klen;
		c -> data = NULL;
		c -> dlen = dlen;
		c -> prev = c -> next = NULL;
		c -> back = c -> forw = NULL;
		if ((c -> key = malloc (klen + 1)) && (c -> data = malloc (dlen + 1))) {
			if (klen > 0)
				memcpy (c -> key, key, klen);
			c -> key[klen] = 0;
			if (dlen > 0)
				memcpy (c -> data, data, dlen);
			c -> data[dlen] = 0;
		} else
			c = centry_free (c);
	}
	return c;
}/*}}}*/
static bool_t
centry_update_key (centry_t *ce, hash_t hash, const byte_t *key, int klen) /*{{{*/
{
	if (ce -> key = realloc (ce -> key, klen + 1)) {
		if (klen > 0)
			memcpy (ce -> key, key, klen);
		ce -> key[klen] = 0;
		ce -> klen = klen;
		ce -> hash = hash;
	} else {
		ce -> klen = 0;
		ce -> hash = 0;
	}
	return ce -> klen == klen ? true : false;
}/*}}}*/
static bool_t
centry_update_data (centry_t *ce, const byte_t *data, int dlen) /*{{{*/
{
	if (ce -> data = realloc (ce -> data, dlen + 1)) {
		if (dlen > 0)
			memcpy (ce -> data, data, dlen);
		ce -> data[dlen] = 0;
		ce -> dlen = dlen;
	} else
		ce -> dlen = 0;
	return ce -> dlen == dlen ? true : false;
}/*}}}*/
static bool_t
centry_update (centry_t *ce, hash_t hash, const byte_t *key, int klen, const byte_t *data, int dlen) /*{{{*/
{
	return centry_update_key (ce, hash, key, klen) && centry_update_data (ce, data, dlen) ? true : false;
}/*}}}*/

cache_t *
cache_free (cache_t *c) /*{{{*/
{
	if (c) {
		if (c -> store) {
			int	n;
			
			for (n = 0; n < c -> hsize; ++n) {
				centry_t	*cur, *temp;
				
				for (cur = c -> store[n]; cur; ) {
					temp = cur;
					cur = cur -> next;
					centry_free (temp);
				}
			}
			free (c -> store);
		}
		free (c);
	}
	return NULL;
}/*}}}*/
cache_t *
cache_alloc (int size) /*{{{*/
{
	cache_t	*c;
	
	if (c = (cache_t *) malloc (sizeof (cache_t))) {
		c -> size = size;
		c -> hsize = hash_size (size);
		c -> count = 0;
		c -> store = NULL;
		c -> head = c -> tail = NULL;
		if (c -> store = (centry_t **) malloc (c -> hsize * sizeof (centry_t))) {
			int	n;
			
			for (n = 0; n < c -> hsize; ++n)
				c -> store[n] = NULL;
		} else
			c = cache_free (c);
	}
	return c;
}/*}}}*/

static centry_t *
cache_unlink (cache_t *c, centry_t *ce, bool_t full) /*{{{*/
{
	if (ce) {
		if (ce -> back)
			ce -> back -> forw = ce -> forw;
		else
			c -> head = ce -> forw;
		if (ce -> forw)
			ce -> forw -> back = ce -> back;
		else
			c -> tail = ce -> back;
		ce -> back = ce -> forw = NULL;
		
		if (full) {
			int	idx = ce -> hash % c -> hsize;
			
			if (ce -> prev)
				ce -> prev -> next = ce -> next;
			else
				c -> store[idx] = ce -> next;
			if (ce -> next)
				ce -> next -> prev = ce -> prev;
			ce -> prev = ce -> next = NULL;
			c -> count--;
		}
	}
	return ce;
}/*}}}*/
static centry_t *
cache_link (cache_t *c, centry_t *ce, bool_t full) /*{{{*/
{
	if (ce) {
		ce -> forw = c -> head;
		if (ce -> forw)
			ce -> forw -> back = ce;
		else
			c -> tail = ce;
		c -> head = ce;

		if (full) {
			int	idx = ce -> hash % c -> hsize;

			ce -> next = c -> store[idx];
			if (ce -> next)
				ce -> next -> prev = ce;
			c -> store[idx] = ce;
			c -> count++;
		}
	}
	return ce;
}/*}}}*/

centry_t *
cache_find (cache_t *c, const byte_t *key, int klen) /*{{{*/
{
	hash_t		hash = hash_value (key, klen);
	int		idx = hash % c -> hsize;
	centry_t	*ce;

	for (ce = c -> store[idx]; ce; ce = ce -> next)
		if (hash_match (key, klen, hash, ce -> key, ce -> klen, ce -> hash)) {
			cache_unlink (c, ce, false);
			cache_link (c, ce, false);
			return ce;
		}
	return NULL;
}/*}}}*/
centry_t *
cache_add (cache_t *c, const byte_t *key, int klen, const byte_t *data, int dlen) /*{{{*/
{
	hash_t		hash = hash_value (key, klen);
	int		idx = hash % c -> hsize;
	centry_t	*ce;

	for (ce = c -> store[idx]; ce; ce = ce -> next)
		if (hash_match (key, klen, hash, ce -> key, ce -> klen, ce -> hash))
			break;
	if (ce)
		centry_update_data (ce, data, dlen);
	else {
		if (c -> count < c -> size)
			ce = centry_alloc (hash, key, klen, data, dlen);
		else
			ce = cache_unlink (c, c -> tail, true);
		if (ce) {
			centry_update (ce, hash, key, klen, data, dlen);
			cache_link (c, ce, true);
		}
	}
	return ce;
}/*}}}*/
void
cache_remove (cache_t *c, centry_t *ce) /*{{{*/
{
	if (ce) {
		cache_unlink (c, ce, true);
		centry_free (ce);
	}
}/*}}}*/
void
cache_delete (cache_t *c, const byte_t *key, int klen) /*{{{*/
{
	cache_remove (c, cache_find (c, key, klen));
}/*}}}*/

fcache_t *
fcache_free (fcache_t *fc) /*{{{*/
{
	if (fc) {
		if (fc -> path)
			free (fc -> path);
		free (fc);
	}
	return NULL;
}/*}}}*/
fcache_t *
fcache_alloc (const char *path) /*{{{*/
{
	fcache_t	*fc;
	
	if (fc = (fcache_t *) malloc (sizeof (fcache_t))) {
		int	plen = strlen (path);
		
		fc -> path = NULL;
		fc -> scratch = NULL;
		fc -> sptr = NULL;
		if (mkdirs (path, 0700) && (fc -> path = strdup (path)) && (fc -> scratch = malloc (plen + NAME_MAX + 2))) {
			strncpy (fc -> scratch, fc -> path, plen);
			fc -> sptr = fc -> scratch + plen;
			*(fc -> sptr)++ = '/';
			fc -> sptr[NAME_MAX] = '\0';
		} else
			fc = fcache_free (fc);
	}
	return fc;
}/*}}}*/
int
fcache_find (fcache_t *fc, const char *name, int timeout) /*{{{*/
{
	int	fd;
	
	strncpy (fc -> sptr, name, NAME_MAX);
	fd = open (fc -> scratch, O_RDONLY);
	if ((fd != -1) && (timeout > 0)) {
		time_t		now;
		struct stat	st;
		
		if (fstat (fd, & st) != -1) {
			time (& now);
			if (st.st_mtime + timeout < now) {
				close (fd);
				fd = -1;
				unlink (fc -> scratch);
			}
		}
	}
	return fd;
}/*}}}*/
bool_t
fcache_save (fcache_t *fc, const char *name, int fd) /*{{{*/
{
	bool_t	rc;
	int	nfd;
	
	rc = false;
	strncpy (fc -> sptr, ".save.XXXXXX", NAME_MAX);
	if ((nfd = mkstemp (fc -> scratch)) != -1) {
		char	buf[65536];
		int	n;
		bool_t	ok;
		char	*temp;
		
		ok = true;
		while ((n = read (fd, buf, sizeof (buf))) > 0)
			if (write (nfd, buf, n) != n) {
				ok = false;
				break;
			}
		close (nfd);
		if (temp = strdup (fc -> scratch)) {
			strncpy (fc -> sptr, name, NAME_MAX);
			if (ok && (link (temp, fc -> scratch) == -1))
				ok = false;
			if (! ok)
				unlink (temp);
			else
				rc = true;
			free (temp);
		} else
			unlink (fc -> scratch);
	}
	return rc;
}/*}}}*/
bool_t
fcache_expire (fcache_t *fc, int expire) /*{{{*/
{
	bool_t		rc;
	time_t		now;
	DIR		*dp;
	struct dirent	*ent;
	struct stat	st;
	
	rc = false;
	time (& now);
	if (dp = opendir (fc -> path)) {
		char		**expired;
		int		size, use;
		
		expired = NULL;
		size = 0;
		use = 0;
		while (ent = readdir (dp)) {
			rc = true;
			strncpy (fc -> sptr, ent -> d_name, NAME_MAX);
			if ((lstat (fc -> scratch, & st) != -1) && S_ISREG (st.st_mode) && (st.st_mtime + expire < now)) {
				if (use >= size) {
					size += 16;
					if (! (expired = (char **) realloc (expired, sizeof (char *) * size))) {
						rc = false;
						break;
					}
				}
				if (! (expired[use++] = strdup (ent -> d_name))) {
					--use;
					rc= false;
				}
			}
		}
		closedir (dp);
		if (expired) {
			int	n;
			
			for (n = 0; n < use; ++n) {
				if (rc) {
					strncpy (fc -> sptr, expired[n], NAME_MAX);
					unlink (fc -> scratch);
				}
				free (expired[n]);
			}
			free (expired);
		}
	}
	return rc;
}/*}}}*/
