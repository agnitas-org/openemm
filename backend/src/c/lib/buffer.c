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
/** @file buffer.c
 * Buffer handling.
 * This module offers routines to work with a buffer, memory
 * allocation, resizing etc. is handled here.
 */
# include	<stdlib.h>
# include	<ctype.h>
# include	"agn.h"

# ifdef		__OPTIMIZE__
# undef		buffer_valid
# undef		buffer_clear
# undef		buffer_length
# undef		buffer_content
# undef		buffer_stiff
# undef		buffer_stiffb
# undef		buffer_stiffch
# undef		buffer_stiffnl
# undef		buffer_stiffcrlf
# undef		buffer_peek
# undef		buffer_poke
# endif		/* __OPTIMIZE__ */

static inline bool_t
do_size (buffer_t *b, int nsize) /*{{{*/
{
	if (b -> valid && (nsize > b -> size)) {
		byte_t	*temp;
		
		if (temp = realloc (b -> buffer, nsize + b -> spare)) {
			b -> buffer = temp;
			b -> size = nsize + b -> spare;
		} else
			b -> valid = false;
	}
	return b -> valid;
}/*}}}*/

/** Allocate a buffer.
 * All elements are set and a buffer is preallocated, 
 * if <i>nsize</i> is bigger than 0
 * @param nsize the initial size of the buffer
 * @return the allocated struct on success, otherwise NULL
 */
buffer_t *
buffer_alloc (int nsize) /*{{{*/
{
	buffer_t	*b;
	
	if (b = (buffer_t *) malloc (sizeof (buffer_t))) {
		b -> length = 0;
		b -> size = nsize;
		b -> buffer = NULL;
		b -> spare = 0;
		b -> valid = true;
		b -> link = NULL;
		if ((b -> size > 0) && (! (b -> buffer = malloc (b -> size))))
			b = buffer_free (b);
	}
	return b;
}/*}}}*/
/** (Re)allocate a buffer
 * if the passed buffer is not NULL, try to resize the buffer,
 * otherwise allocate a new one. Returns the buffer pointer on
 * success, NULL on failure.
 * 
 * The content of the buffer may be altered!
 * 
 * @param b the buffer to reallocate or NULL
 * @param nsize the size to reallocate the buffer ot
 * @return the buffer on success, NULL otherwise
 */
buffer_t *
buffer_realloc (buffer_t *b, int nsize) /*{{{*/
{
	if (b) {
		if (do_size (b, nsize))
			return b;
		buffer_free (b);
	}
	return buffer_alloc (nsize);
}/*}}}*/
/** Frees a buffer.
 * The memeory used by the buffer, if in use, and the struct
 * are returned to the system
 * @param b the buffer to free
 * @return NULL
 */
buffer_t *
buffer_free (buffer_t *b) /*{{{*/
{
	if (b) {
		if (b -> buffer)
			free (b -> buffer);
		free (b);
	}
	return NULL;
}/*}}}*/
/** Returns status if buffer is valid
 * @param b the buffer
 * @return true, if the buffer is valid, false otherwise
 */
bool_t
buffer_valid (buffer_t *b) /*{{{*/
{
	return b && b -> valid;
}/*}}}*/
/** Clear buffer content.
 * @param b the buffer
 */
void
buffer_clear (buffer_t *b) /*{{{*/
{
	b -> length = 0;
	b -> valid = true;
}/*}}}*/
/** Truncate buffer to length, if buffer is larger
 * @param b the buffer
 * @param length new length of buffer
 */
void
buffer_truncate (buffer_t *b, long length) /*{{{*/
{
	if (b -> valid && (b -> length > length)) {
		b -> length = length;
		b -> valid = true;
	}
}/*}}}*/
/** Retrieves the used bytes in buffer
 * @param b the buffer
 * @return the bytecount
 */
int
buffer_length (const buffer_t *b) /*{{{*/
{
	return b -> length;
}/*}}}*/
/** Retrieves the buffer content
 * @param b the buffer
 * @return the start of the buffer
 */
const byte_t *
buffer_content (const buffer_t *b) /*{{{*/
{
	return b -> buffer;
}/*}}}*/
/** Set buffer size.
 * The buffer size is set to the new value. If the new value
 * is bigger than the old one, the buffer storage is increased
 * @param b the buffer
 * @param nsize the new size
 * @return true if new size could be used, false otherwise
 */
bool_t
buffer_size (buffer_t *b, int nsize) /*{{{*/
{
	return do_size (b, nsize);
}/*}}}*/
/** Set buffer content from byte array.
 * Set new content to the buffer
 * @param b the buffer to use
 * @param data the content to use
 * @param dlen length of content
 * @return true if content could be set, false otherwise
 */
bool_t
buffer_set (buffer_t *b, const byte_t *data, int dlen) /*{{{*/
{
	if (do_size (b, dlen)) {
		if (dlen > 0)
			memcpy (b -> buffer, data, dlen);
		b -> length = dlen;
	}
	return b -> valid;
}/*}}}*/
/** Set buffer content from buffer.
 * @param b the buffer to use
 * @param data the source data to use
 * @return true if content could be set, false otherwise
 */
bool_t
buffer_setbuf (buffer_t *b, buffer_t *data) /*{{{*/
{
	return data ? buffer_set (b, data -> buffer, data -> length) : true;
}/*}}}*/
/** Set buffer content from byte.
 * @param b the buffer to use
 * @param data the content
 * @return true on success, false otherwise
 * @see buffer_set
 */
bool_t
buffer_setb (buffer_t *b, byte_t data) /*{{{*/
{
	return buffer_set (b, & data, 1);
}/*}}}*/
/** Set buffer content from string with length.
 * @param b the buffer to use
 * @param str the content
 * @param len the length of content
 * @return true on success, false otherwise
 * @see buffer_set
 */
bool_t
buffer_setsn (buffer_t *b, const char *str, int len) /*{{{*/
{
	return buffer_set (b, (const byte_t *) str, len);
}/*}}}*/
/** Set buffer content from string.
 * @param b the buffer to use
 * @param str the content
 * @return true on success, false otherwise
 * @see buffer_set
 */
bool_t
buffer_sets (buffer_t *b, const char *str) /*{{{*/
{
	return buffer_set (b, (const byte_t *) str, strlen (str));
}/*}}}*/
/** Set buffer content from character.
 * @param b the buffer to use
 * @param ch the content
 * @return true on success, false otherwise
 * @see buffer_set
 */
bool_t
buffer_setch (buffer_t *b, char ch) /*{{{*/
{
	return buffer_setb (b, (byte_t) ch);
}/*}}}*/
/** Append byte array to buffer.
 * Append content to the buffer, allocating memory, if required
 * @param b the buffer to use
 * @param data the content
 * @param dlen the length of the content
 * @return true if content could be appended, false otherwise
 */
bool_t
buffer_append (buffer_t *b, const byte_t *data, int dlen) /*{{{*/
{
	if (do_size (b, b -> length + dlen)) {
		if (dlen > 0) {
			memcpy (b -> buffer + b -> length, data, dlen);
			b -> length += dlen;
		}
	}
	return b -> valid;
}/*}}}*/
/** Append buffer content to buffer.
 * @param b the buffer to use
 * @param data the content
 * @return true on success, false otherwise
 * @see buffer_append
 */
bool_t
buffer_appendbuf (buffer_t *b, buffer_t *data) /*{{{*/
{
	return data ? buffer_append (b, data -> buffer, data -> length) : true;
}/*}}}*/
/** Append byte to buffer.
 * @param b the buffer to use
 * @param data the content
 * @return true on success, false otherwise
 * @see buffer_append
 */
bool_t
buffer_appendb (buffer_t *b, byte_t data) /*{{{*/
{
	return buffer_append (b, & data, 1);
}/*}}}*/
/** Append string with length to buffer.
 * @param b the buffer to use
 * @param str the string
 * @param len its length
 * @return true on success, false otherwise
 * @see buffer_append
 */
bool_t
buffer_appendsn (buffer_t *b, const char *str, int len) /*{{{*/
{
	return buffer_append (b, (byte_t *) str, len);
}/*}}}*/
/** Append string to buffer.
 * @param b the buffer to use
 * @param str the content
 * @return true on success, false otherwise
 * @see buffer_append
 */
bool_t
buffer_appends (buffer_t *b, const char *str) /*{{{*/
{
	return buffer_append (b, (byte_t *) str, strlen (str));
}/*}}}*/
/** Append character to buffer.
 * @param b the buffer to use
 * @param ch the content
 * @return true on success, false otherwise
 * @see buffer_append
 */
bool_t
buffer_appendch (buffer_t *b, char ch) /*{{{*/
{
	return buffer_appendb (b, (byte_t) ch);
}/*}}}*/
/** Append newline to buffer.
 * @param b the buffer to use
 * @return true on success, false otherwise
 * @see buffer_append
 */
bool_t
buffer_appendnl (buffer_t *b) /*{{{*/
{
	return buffer_appendb (b, (byte_t) '\n');
}/*}}}*/
/** Append CR+LF to buffer.
 * @param b the buffer to use
 * @return true on success, false otherwise
 * @see buffer_append
 */
bool_t
buffer_appendcrlf (buffer_t *b) /*{{{*/
{
	return buffer_append (b, (byte_t *) "\r\n", 2);
}/*}}}*/
/** Insert data at given position
 * @param b the buffer to use
 * @param pos the position to insert data
 * @param data the content
 * @param dlen length of content
 * @return true if content could be inserted, false otherwise
 */
bool_t
buffer_insert (buffer_t *b, int pos, const byte_t *data, int dlen) /*{{{*/
{
	if (do_size (b, b -> length + dlen)) {
		if (pos < 0)
			pos = 0;
		if (pos < b -> length)
			memmove (b -> buffer + pos + dlen, b -> buffer + pos, b -> length - pos);
		if (dlen > 0) {
			memcpy (b -> buffer + pos, data, dlen);
			b -> length += dlen;
		}
	}
	return b -> valid;
}/*}}}*/
bool_t
buffer_insertbuf (buffer_t *b, int pos, buffer_t *data) /*{{{*/
{
	return data ? buffer_insert (b, pos, data -> buffer, data -> length) : true;
}/*}}}*/
bool_t
buffer_insertsn (buffer_t *b, int pos, const char *str, int len) /*{{{*/
{
	return buffer_insert (b, pos, (byte_t *) str, len);
}/*}}}*/
bool_t
buffer_inserts (buffer_t *b, int pos, const char *str) /*{{{*/
{
	return buffer_insert (b, pos, (byte_t *) str, strlen (str));
}/*}}}*/
/** Stiff (append) byte array to buffer.
 * This is like <i>buffer_append</i>, but more buffer is allocated
 * than currently required. These class of functions are useful when
 * there are many small appends to avoid a memory reallocation on
 * each call
 * @param b the buffer to use
 * @param data the content
 * @param dlen the size of the content
 * @return true if content could be appended, false otherwise
 */
bool_t
buffer_stiff (buffer_t *b, const byte_t *data, int dlen) /*{{{*/
{
	if (b -> valid && ((b -> length + dlen < b -> size) || do_size (b, b -> length + dlen + b -> size))) {
		if (dlen > 0) {
			memcpy (b -> buffer + b -> length, data, dlen);
			b -> length += dlen;
		}
	}
	return b -> valid;
}/*}}}*/
/** Stiff (append) buffer to buffer.
 * @param b the buffer to use
 * @param data content
 * @return true on success, false otherwise
 * @see buffer_stiff
 */
bool_t
buffer_stiffbuf (buffer_t *b, buffer_t *data) /*{{{*/
{
	return data ? buffer_stiff (b, data -> buffer, data -> length) : true;
}/*}}}*/
/** Stiff (append) byte to buffer.
 * @param b the buffer to use
 * @param data content
 * @return true on success, false otherwise
 * @see buffer_stiff
 */
bool_t
buffer_stiffb (buffer_t *b, byte_t data) /*{{{*/
{
	return buffer_stiff (b, & data, 1);
}/*}}}*/
/** Stiff (append) string with length to buffer.
 * @param b the buffer to use
 * @param str content
 * @param len length of string
 * @return true on success, false otherwise
 * @see buffer_stiff
 */
bool_t
buffer_stiffsn (buffer_t *b, const char *str, int len) /*{{{*/
{
	return buffer_stiff (b, (byte_t *) str, len);
}/*}}}*/
/** Stiff (append) string to buffer.
 * @param b the buffer to use
 * @param str content
 * @return true on success, false otherwise
 * @see buffer_stiff
 */
bool_t
buffer_stiffs (buffer_t *b, const char *str) /*{{{*/
{
	return buffer_stiff (b, (byte_t *) str, strlen (str));
}/*}}}*/
/** Stiff (append) character to buffer.
 * @param b the buffer to use
 * @param ch content
 * @return true on success, false otherwise
 * @see buffer_stiff
 */
bool_t
buffer_stiffch (buffer_t *b, char ch) /*{{{*/
{
	return buffer_stiffb (b, (byte_t) ch);
}/*}}}*/
/** Stiff (append) newline to buffer.
 * @param b the buffer to use
 * @return true on success, false otherwise
 * @see buffer_stiff
 */
bool_t
buffer_stiffnl (buffer_t *b) /*{{{*/
{
	return buffer_stiffb (b, (byte_t) '\n');
}/*}}}*/
/** Stiff (append) CR+LF to buffer.
 * @param b the buffer to use
 * @return true on success, false otherwise
 * @see buffer_stiff
 */
bool_t
buffer_stiffcrlf (buffer_t *b) /*{{{*/
{
	return buffer_stiff (b, (byte_t *) "\r\n", 2);
}/*}}}*/
/** Append vprinf like format to buffer.
 * Build new string using the format and given paramter using
 * the printf rules
 * @param b the buffer to use
 * @param fmt the prinf like format
 * @param par the parameter for format
 * @return true if formated string could be appended, false otherwise
 */
bool_t
buffer_vformat (buffer_t *b, const char *fmt, va_list par) /*{{{*/
{
	if (b -> valid) {
		int	len, room;
		int	n;
		va_list	temp;
	
		n = 128;
		do {
			len = n + 1;
			room = b -> size - b -> length;
			if ((room < len) && (! do_size (b, b -> length + len + 1)))
				break;
			va_copy (temp, par);
			n = vsnprintf ((char *) (b -> buffer + b -> length), len, fmt, temp);
			va_end (temp);
			if (n == -1)
				n = len * 2;
		}	while (n >= len);
		if (b -> valid)
			b -> length += n;
	}
	return b -> valid;
}/*}}}*/
/** Append printf like format to buffer.
 * Like <i>buffer_vformat</i>, but takes variable number of arguments
 * @param b the buffer to use
 * @param fmt the printf like format
 * @param ... the paramter list
 * @return true on success, false otherwise
 * @see buffer_vformat
 */
bool_t
buffer_format (buffer_t *b, const char *fmt, ...) /*{{{*/
{
	va_list	par;
	
	va_start (par, fmt);
	buffer_vformat (b, fmt, par);
	va_end (par);
	return b -> valid;
}/*}}}*/
/** Append time format to buffer.
 * Use the formating capabilities of <b>strftime(3)</b> to append
 * date/time informations to the buffer
 * @param b the buffer to use
 * @param fmt the strftime format
 * @param tt the time to use
 * @return true on success, false otherwise
 */
bool_t
buffer_strftime (buffer_t *b, const char *fmt, const struct tm *tt) /*{{{*/
{
	if (do_size (b, b -> length + strlen (fmt) * 4 + 256 + 1))
		b -> length += strftime ((char *) (b -> buffer + b -> length), b -> size - b -> length - 1, fmt, tt);
	return b -> valid;
}/*}}}*/
/** Cuts a piece of the buffer.
 * A part of the buffer is cut out, a new memory block is allocated
 * for the cut out copy to be returned (which must be freed using
 * <b>free(3)</b>).
 * @param b the buffer to use
 * @param start the start position to cut out
 * @param length the length to cut out
 * @param rlength the length of the returned buffer
 * @return the cut out piece in newly allocated memory on success, NULL otherwise
 */
byte_t *
buffer_cut (buffer_t *b, long start, long length, long *rlength) /*{{{*/
{
	byte_t	*ret;
	int	rlen;
	
	if (start >= b -> length)
		rlen = 0;
	else if (start + length >= b -> length)
		rlen = b -> length - start;
	else
		rlen = length;
	if (ret = (byte_t *) malloc ((rlen + 1) * sizeof (byte_t))) {
		if (rlen > 0)
			memcpy (ret, b -> buffer + start, rlen * sizeof (byte_t));
		ret[rlen] = 0;
	}
	if (rlength)
		*rlength = rlen;
	return ret;
}/*}}}*/
/** Returns the buffer as string.
 * A nul byte will be appended to current content and the
 * content of the buffer will be returned. Be sure not to
 * alter or free the returned value in any way!
 * @param b the buffer to use
 * @return the pointer to the start of the real buffer on success, NULL otherwise
 */
const char *
buffer_string (buffer_t *b) /*{{{*/
{
	if (do_size (b, b -> length + 1)) {
		b -> buffer[b -> length] = '\0';
		return (const char *) b -> buffer;
	}
	return NULL;
}/*}}}*/
char *
buffer_stealstring (buffer_t *b) /*{{{*/
{
	char	*rc = (char *) buffer_string (b);

	b -> length = 0;
	b -> size = 0;
	b -> buffer = NULL;
	b -> valid = true;
	return rc;
}/*}}}*/
/** Returns a copy of the buffer as string.
 * @param b the buffer to use
 * @return the new allocated string on success, or NULL on failure
 */
char *
buffer_copystring (buffer_t *b) /*{{{*/
{
	char	*rc;
	
	if (rc = malloc (b ? b -> length + 1 : 1)) {
		if (b) {
			if (b -> length > 0)
				memcpy (rc, b -> buffer, b -> length);
			rc[b -> length] = '\0';
		} else
			rc[0] = '\0';
	}
	return rc;
}/*}}}*/
/** Peek the byte at position
 * @param b the buffer to use
 * @param pos the position to peek for (negative means count from the end)
 * @return the byte value, or -1 if position is out of range
 */
int
buffer_peek (const buffer_t *b, int pos) /*{{{*/
{
	if (pos < 0)
		pos = b -> length + pos;
	if ((pos >= 0) && (pos < b -> length))
		return b -> buffer[pos];
	return -1;
}/*}}}*/
void
buffer_poke (buffer_t *b, int pos, byte_t value) /*{{{*/
{
	if (pos < 0)
		pos = b -> length + pos;
	if ((pos >= 0) && (pos < b -> length))
		b -> buffer[pos] = value;
}/*}}}*/
/** Find content in buffer
 * @param b the buffer to search in
 * @param content the content to look for
 * @param clen the size of the content
 */
int
buffer_index (const buffer_t *b, const byte_t *content, int clen) /*{{{*/
{
	int	n, pos;
	
	n = 0;
	pos = 0;
	for (n = 0, pos = 0; (pos < clen) && (n + clen - pos < b -> length); ++n) {
		if ((pos > 0) && (b -> buffer[n] != content[pos]))
			pos = 0;
		if (b -> buffer[n] == content[pos])
			++pos;
	}
	return pos == clen ? n - clen : -1;
}/*}}}*/
int
buffer_indexsn (const buffer_t *b, const char *s, int slen) /*{{{*/
{
	return buffer_index (b, (const byte_t *) s, slen);
}/*}}}*/
int
buffer_indexs (const buffer_t *b, const char *s) /*{{{*/
{
	return buffer_index (b, (const byte_t *) s, strlen (s));
}/*}}}*/

/** remove leading/trailing whitespaces
 */
void
buffer_ltrim (buffer_t *b) /*{{{*/
{
	if (b -> length > 0) {
		int	ws;
		
		for (ws = 0; (ws < b -> length) && isspace (b -> buffer[ws]); ++ws)
			;
		if (ws > 0) {
			if (ws == b -> length) {
				buffer_clear (b);
			} else {
				memmove (b -> buffer, b -> buffer + ws, b -> length - ws);
				b -> length -= ws;
			}
		}
	}
}/*}}}*/
void
buffer_rtrim (buffer_t *b) /*{{{*/
{
	while ((b -> length > 0) && isspace (b -> buffer[b -> length - 1]))
		b -> length--;
}/*}}}*/
void
buffer_trim (buffer_t *b) /*{{{*/
{
	buffer_rtrim (b);
	buffer_ltrim (b);
}/*}}}*/
struct pool { /*{{{*/
	buffer_t	*root;
	/*}}}*/
};
pool_t *
pool_alloc (void) /*{{{*/
{
	pool_t	*p;
	
	if (p = (pool_t *) malloc (sizeof (pool_t))) {
		p -> root = NULL;
	}
	return p;
}/*}}}*/
pool_t *
pool_free (pool_t *p) /*{{{*/
{
	if (p) {
		pool_flush (p);
		free (p);
	}
	return NULL;
}/*}}}*/
void
pool_flush (pool_t *p) /*{{{*/
{
	buffer_t	*temp;
	
	while (temp = p -> root) {
		p -> root = p -> root -> link;
		buffer_free (temp);
	}
}/*}}}*/
buffer_t *
pool_request (pool_t *p, int nsize) /*{{{*/
{
	if (p -> root) {
		buffer_t	*b, *prev;
	
		for (b = p -> root, prev = NULL; b && b -> link; ) {
			if ((b -> size < nsize) || ((b -> size >= nsize) && (b -> link -> size < nsize)))
				break;
			prev = b;
			b = b -> link;
		}
		if (prev)
			prev -> link = b -> link;
		else
			p -> root = b -> link;
		b -> link = NULL;
		buffer_clear (b);
		if (b -> size < nsize) {
			if (! buffer_size (b, nsize))
				buffer_clear (b);
		}
		return b;
	}
	return buffer_alloc (nsize);
}/*}}}*/
buffer_t *
pool_release (pool_t *p, buffer_t *b) /*{{{*/
{
	if ((! p -> root) || (p -> root -> size <= b -> size)) {
		b -> link = p -> root;
		p -> root = b;
	} else {
		buffer_t	*run, *prv;
		
		for (run = p -> root, prv = NULL; run; run = run -> link)
			if (run -> size <= b -> size)
				break;
			else
				prv = run;
		if (prv) {
			b -> link = prv -> link;
			prv -> link = b;
		} else {
			b -> link = p -> root;
			p -> root = b;
		}
	}
	return NULL;
}/*}}}*/

static pool_t	*pool = NULL;
static void
pool_cleanup (void) /*{{{*/
{
	if (pool)
		pool = pool_free (pool);
}/*}}}*/
static int
pool_init (void) /*{{{*/
{
	if ((! pool) && (pool = pool_alloc ())) {
		atexit (pool_cleanup);
	}
	return pool != NULL;
}/*}}}*/
buffer_t *
buffer_request (int nsize) /*{{{*/
{
	if (pool || pool_init ())
		return pool_request (pool, nsize);
	return NULL;
}/*}}}*/
buffer_t *
buffer_release (buffer_t *b) /*{{{*/
{
	if (pool || pool_init ())
		return pool_release (pool, b);
	return buffer_free (b);
}/*}}}*/
