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
# include	<stdlib.h>
# include	<ctype.h>
# include	"agn.h"

/** Calculates hash value.
 * @param key the string to calculate the hash for
 * @param len the length of the key
 * @return the hash code
 */
hash_t
hash_value (const byte_t *key, int len) /*{{{*/
{
	hash_t	hash = 0;
	
	while (len-- > 0) {
		hash *= 119;
		hash |= *key++;
	}
	return hash;
}/*}}}*/
hash_t
hash_svalue (const char *key, int len, bool_t icase) /*{{{*/
{
	if (! icase)
		return hash_value ((const byte_t *) key, len);
	else {
		hash_t	hash = 0;
		
		while (len-- > 0) {
			hash *= 119;
			hash |= (unsigned char) tolower (*key++);
		}
		return hash;
	}
}/*}}}*/
bool_t
hash_match (const byte_t *key, int klen, hash_t khash, const byte_t *match, int mlen, hash_t mhash) /*{{{*/
{
	if ((klen == mlen) && (khash == mhash)) {
		if (mlen > 0)
			return memcmp (key, match, klen) == 0;
		return true;
	}
	return false;
}/*}}}*/
bool_t
hash_smatch (const char *key, int klen, hash_t khash, const char *match, int mlen, hash_t mhash, bool_t icase) /*{{{*/
{
	if (! icase)
		return hash_match ((const byte_t *) key, klen, khash, (const byte_t *) match, mlen, mhash);
	if ((klen == mlen) && (khash == mhash)) {
		if (mlen > 0)
			return strncasecmp (key, match, klen) == 0;
		return true;
	}
	return false;
}/*}}}*/
/** Find useful hashsize.
 * Taken the amount of nodes, a "good" value for the size
 * of the hash array is searched
 * @param size the number of nodes in the collection
 * @return the proposed size of the hash array
 */
int
hash_size (int size) /*{{{*/
{
	int	htab[] = {
		113,
		311,
		733,
		1601,
		3313,
		5113,
		8677,
		13121,
		25457,
		50021,
		99607
	};
	int	n;
	int	hsize;
	
	size >>= 2;
	hsize = htab[0];
	for (n = 0; n < sizeof (htab) / sizeof (htab[0]); ++n)
		if (htab[n] >= size) {
			hsize = htab[n];
			break;
		}
	return hsize;
}/*}}}*/
