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
/** @file map.c
 * Handles hash collections.
 */
# include	<stdlib.h>
# include	<ctype.h>
# include	"agn.h"

/** Creates the node key.
 * According to flag on howto compare keys, the main key
 * is generated here
 * @param m the map
 * @param key the key to modify
 * @return the main key on success, NULL otherwise
 */
static const char *
mkmkey (map_t *m, const char *key) /*{{{*/
{
	const char	*mkey;
	char		*temp;

	mkey = NULL;
	switch (m -> mode) {
	case MAP_Generic:
		break;
	case MAP_CaseSensitive:
		mkey = key;
		break;
	case MAP_CaseIgnore:
		if (temp = malloc (strlen (key) + 1)) {
			int	n;
			
			for (n = 0; key[n]; ++n)
				if (isupper ((int) ((unsigned char) key[n])))
					temp[n] = tolower (key[n]);
				else
					temp[n] = key[n];
			temp[n] = '\0';
			mkey = temp;
		}
		break;
	}
	return mkey;
}/*}}}*/
/** Find generic node in map.
 * @param m the map
 * @param key the key to search for
 * @param len the legnth of the key
 * @param hash its hashvalue
 * @param prv store the previous node in subling chain here, if *prv not NULL
 * @return the node on success, NULL otherwise
 */
static gnode_t *
glocate (map_t *m, const byte_t *key, int klen, hash_t hash, gnode_t **prv) /*{{{*/
{
	gnode_t	*g;
	
	if (prv)
		*prv = NULL;
	for (g = m -> cont.g[hash % m -> hsize]; g; g = g -> next)
		if ((g -> hash == hash) && (g -> klen == klen) && ((klen == 0) || (! memcmp (g -> key, key, klen))))
			break;
		else if (prv)
			*prv = g;
	return g;
}/*}}}*/
/** Find node in map.
 * @param m the map
 * @param key the key to search for
 * @param hash its hashvalue
 * @param prv store the previous node in sibling chain here, if *prv not NULL
 * @return the node on success, NULL otherwise
 */
static node_t *
locate (map_t *m, const char *key, hash_t hash, node_t **prv) /*{{{*/
{
	node_t	*n;
	
	if (prv)
		*prv = NULL;
	for (n = m -> cont.n[hash % m -> hsize]; n; n = n -> next)
		if ((n -> hash == hash) && (n -> mkey[0] == key[0]) && (! strcmp (n -> mkey, key)))
			break;
		else if (prv)
			*prv = n;
	return n;
}/*}}}*/
/** Allocate a map.
 * @param mode mapping mode
 * @param aproxsize the aprox. number of nodes in this map
 * @return the new map on success, NULL otherwise
 */
map_t *
map_alloc (mapmode_t mode, int aproxsize) /*{{{*/
{
	map_t	*m;
	
	if (m = (map_t *) malloc (sizeof (map_t))) {
		m -> mode = mode;
		m -> hsize = hash_size (aproxsize);
		if (m -> cont.u = (void **) malloc (m -> hsize * sizeof (void *))) {
			int	n;
			
			for (n = 0; n < m -> hsize; ++n)
				m -> cont.u[n] = NULL;
		} else {
			free (m);
			m = NULL;
		}
	}
	return m;
}/*}}}*/
/** Free map.
 * Returns all allocated memory used by this map an its nodes
 * @param m the map to free
 * @return NULL
 */
map_t *
map_free (map_t *m) /*{{{*/
{
	if (m) {
		if (m -> cont.u) {
			int	n;
			
			for (n = 0; n < m -> hsize; ++n)
				switch (m -> mode) {
				case MAP_Generic:
					if (m -> cont.g[n])
						gnode_free_all (m -> cont.g[n]);
					break;
				case MAP_CaseSensitive:
				case MAP_CaseIgnore:
					if (m -> cont.n[n])
						node_free_all (m -> cont.n[n]);
					break;
				}
			free (m -> cont.u);
		}
		free (m);
	}
	return NULL;
}/*}}}*/

/** Add a generic node to the map
 * @param m the map
 * @param key the key
 * @param klen the key length
 * @param data the data
 * @param dlen the data length
 * @return the node on success, NULL otherwise
 */
gnode_t *
map_gadd (map_t *m, const byte_t *key, int klen, const byte_t *data, int dlen) /*{{{*/
{
	hash_t	hash;
	gnode_t	*g;
	
	hash = hash_value (key, klen);
	if (g = glocate (m, key, klen, hash, NULL)) {
		if (! gnode_setdata (g, data, dlen))
			g = NULL;
	} else if (g = gnode_alloc (key, klen, hash, data, dlen)) {
		int	hpos = hash % m -> hsize;
			
		g -> next = m -> cont.g[hpos];
		m -> cont.g[hpos] = g;
	}
	return g;
}/*}}}*/
/** Add a node to the map.
 * @param m the map
 * @param key the key of the node
 * @param data the value of the node
 * @return the node on success, NULL otherwise
 */
node_t *
map_add (map_t *m, const char *key, const char *data) /*{{{*/
{
	node_t		*n;
	const char	*mkey;
	
	n = NULL;
	if (mkey = mkmkey (m, key)) {
		hash_t	hash;

		hash = hash_value ((const byte_t *) mkey, strlen (mkey));
		if (n = locate (m, mkey, hash, NULL)) {
			if (! node_setdata (n, data))
				n = NULL;
		} else if (n = node_alloc (mkey, hash, key, data)) {
			int	hpos = hash % m -> hsize;
			
			n -> next = m -> cont.n[hpos];
			m -> cont.n[hpos] = n;
		}
		if (mkey != key)
			free ((void *) mkey);
	}
	return n;
}/*}}}*/
/** Remove node from map.
 * @param m the map
 * @param n the node to remove
 * @return true on success, false otherwise
 */
bool_t
map_delete_node (map_t *m, node_t *n) /*{{{*/
{
	bool_t	rc;
	int	hpos = n -> hash % m -> hsize;
	node_t	*run, *prv;
	
	rc = false;
	for (run = m -> cont.n[hpos], prv = NULL; run; run = run -> next)
		if (run == n)
			break;
		else
			prv = run;
	if (run) {
		if (prv)
			prv -> next = run -> next;
		else
			m -> cont.n[hpos] = run -> next;
		node_free (run);
		rc = true;
	}
	return rc;
}/*}}}*/
/** Delete node using key from map.
 * @param m the map
 * @param key the key to look for
 * @return true on success, false otherwise
 */
bool_t
map_delete (map_t *m, const char *key) /*{{{*/
{
	bool_t		rc;
	const char	*mkey;
	
	rc = false;
	if (mkey = mkmkey (m, key)) {
		node_t	*n, *prv;

		if (n = locate (m, mkey, hash_value ((const byte_t *) mkey, strlen (mkey)), & prv)) {
			if (prv)
				prv -> next = n -> next;
			else
				m -> cont.n[n -> hash % m -> hsize] = n -> next;
			node_free (n);
			rc = true;
		}
		if (mkey != key)
			free ((void *) mkey);
	}
	return rc;
}/*}}}*/

/** Find a generic node in the map.
 * @param m the map
 * @param key the key
 * @param klen the key length
 * @return the node if found, NULL otherwise
 */
gnode_t *
map_gfind (map_t *m, const byte_t *key, int klen) /*{{{*/
{
	return glocate (m, key, klen, hash_value (key, klen), NULL);
}/*}}}*/
/** Find a node in the map.
 * @param m the map
 * @param key the key
 * @return the node if found, NULL otherwise
 */
node_t *
map_find (map_t *m, const char *key) /*{{{*/
{
	node_t		*n;
	const char	*mkey;
	
	if (mkey = mkmkey (m, key)) {
		n = locate (m, mkey, hash_value ((const byte_t *) mkey, strlen (mkey)), NULL);
		if (mkey != key)
			free ((void *) mkey);
	} else
		n = NULL;
	return n;
}/*}}}*/
