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
# include	<ctype.h>
# include	"xmlback.h"

head_t *
head_alloc (void) /*{{{*/
{
	head_t	*h;
	
	if (h = (head_t *) malloc (sizeof (head_t)))
		if (h -> h = buffer_alloc (256)) {
			h -> _name = NULL;
			h -> _namelength = -1;
			h -> _valuepos = -1;
			h -> next = NULL;
		} else {
			free (h);
			h = NULL;
		}
	return h;
}/*}}}*/
static head_t *
head_copy (head_t *source) /*{{{*/
{
	head_t	*h;
	
	if (h = head_alloc ()) {
		if (! head_add (h, buffer_content (source -> h), buffer_length (source -> h)))
			h = head_free (h);
	}
	return h;
}/*}}}*/
static head_t *
head_copy_all (head_t *source) /*{{{*/
{
	head_t	*root, *prev, *temp;
	
	for (root = NULL, prev = NULL; source; source = source -> next)
		if (temp = head_copy (source)) {
			if (prev)
				prev -> next = temp;
			else
				root = temp;
			prev = temp;
		} else {
			root = head_free_all (root);
			break;
		}
	return root;
}/*}}}*/
head_t *
head_free (head_t *h) /*{{{*/
{
	if (h) {
		if (h -> h)
			buffer_free (h -> h);
		if (h -> _name)
			free (h -> _name);
		free (h);
	}
	return NULL;
}/*}}}*/
head_t *
head_free_all (head_t *h) /*{{{*/
{
	head_t	*tmp;
	
	while (tmp = h) {
		h = h -> next;
		head_free (tmp);
	}
	return NULL;
}/*}}}*/
const char *
head_find_name (head_t *h, int *namelength) /*{{{*/
{
	if (! h -> _name) {
		int		length = buffer_length (h -> h);
		const byte_t	*content = buffer_content (h -> h);
		int		n, pos;
			
		for (pos = 0; pos < length; ++pos)
			if (content[pos] == ':') {
				if (h -> _name = malloc (pos + 1)) {
					for (n = 0; n < pos; ++n)
						h -> _name[n] = tolower (content[n]);
					h -> _name[pos] = '\0';
					h -> _namelength = pos;
				} else
					return NULL;
				++pos;
				while ((pos < length) && isspace (content[pos]))
					++pos;
				h -> _valuepos = pos;
				break;
			}
	}
	if (namelength)
		*namelength = h -> _namelength;
	return h -> _name;
}/*}}}*/
static bool_t
head_valid (head_t *h) /*{{{*/
{
	if (head_find_name (h, NULL) && (h -> _valuepos != -1) && (h -> _valuepos <= buffer_length (h -> h)))
		return true;
	return false;
}/*}}}*/
static bool_t
head_empty (head_t *h) /*{{{*/
{
	if (h -> _valuepos == buffer_length (h -> h))
		return true;
	return false;
}/*}}}*/
bool_t
head_add (head_t *h, const byte_t *chunk, int len) /*{{{*/
{
	int	old_len = buffer_length (h -> h);

	if (buffer_append (h -> h, chunk, len)) {
		buffer_universal_newline (h -> h, old_len);
		if (head_find_name (h, NULL))
			return true;
	}
	return false;
}/*}}}*/
static bool_t
head_addsn (head_t *h, const char *s, int len) /*{{{*/
{
	return head_add (h, (const byte_t *) s, len);
}/*}}}*/
const char *
head_value (head_t *h) /*{{{*/
{
	if ((h -> _valuepos >= 0) && (h -> _valuepos < buffer_length (h -> h)))
		return buffer_string (h -> h) + h -> _valuepos;
	return NULL;
}/*}}}*/
bool_t
head_set_value (head_t *h, buffer_t *value) /*{{{*/
{
	if (h -> _valuepos >= 0) {
		buffer_truncate (h -> h, h -> _valuepos);
		return buffer_appendbuf (h -> h, value);
	}
	return false;
}/*}}}*/
bool_t
head_matchn (head_t *h, const char *name, int namelength) /*{{{*/
{
	if (head_find_name (h, NULL) && (h -> _namelength == namelength) && (! strcmp (h -> _name, name)))
		return true;
	return false;
}/*}}}*/
bool_t
head_match (head_t *h, const char *name) /*{{{*/
{
	return head_matchn (h, name, strlen (name));
}/*}}}*/
bool_t
head_startswithn (head_t *h, const char *name, int namelength) /*{{{*/
{
	if (head_find_name (h, NULL) && (h -> _namelength >= namelength) && (! strncmp (h -> _name, name, namelength)))
		return true;
	return false;
}/*}}}*/
bool_t
head_startswith (head_t *h, const char *name) /*{{{*/
{
	return head_startswithn (h, name, strlen (name));
}/*}}}*/
char *
head_is (head_t *h, const char *name) /*{{{*/
{
	const byte_t	*content = buffer_content (h -> h);
	int		length = buffer_length (h -> h);
	char		*rc;
	
	while (*name && (length > 0)) {
		if (tolower (*name) != tolower (*content))
			break;
		++name, ++content, --length;
	}
	if ((! *name) && (length > 0) && (*content == ':')) {
		++content, --length;
		while ((length > 0) && (*content == ' '))
			++content, --length;
		if (rc = malloc (length + 1)) {
			if (length)
				memcpy (rc, content, length);
			rc[length] = '\0';
			return rc;
		}
	}
	return NULL;
}/*}}}*/

static bool_t
header_encode_line (header_t *header, head_t *head, buffer_t *target) /*{{{*/
{
	if (header -> convert) {
		const buffer_t	*converted = convert_encode_buffer (header -> convert, head -> h);
		
		if (converted)
			return encode_head (converted, target, convert_charset (header -> convert));
	}
	return encode_head (head -> h, target, "UTF-8");
}/*}}}*/
header_t *
header_alloc (void) /*{{{*/
{
	header_t	*h;
	
	if (h = (header_t *) malloc (sizeof (header_t))) {
		h -> content = NULL;
		h -> scratch = NULL;
		h -> sender = NULL;
		h -> recipient = NULL;
		h -> head = NULL;
		h -> convert = NULL;
	}
	return h;
}/*}}}*/
header_t *
header_copy (header_t *source) /*{{{*/
{
	header_t	*h;
	
	if (h = header_alloc ()) {
		h -> content = buffer_copy (source -> content);
		h -> sender = buffer_copy (source -> sender);
		h -> recipient = buffer_copy (source -> recipient);
		h -> convert = source -> convert;
		h -> head = head_copy_all (source -> head);
		if ((source -> content && (! h -> content)) ||
		    (source -> sender && (! h -> sender)) ||
		    (source -> recipient && (! h -> recipient)) ||
		    (source -> head && (! h -> head)))
			h = header_free (h);
	}
	return h;
}/*}}}*/
header_t *
header_free (header_t *h) /*{{{*/
{
	if (h) {
		if (h -> content)
			buffer_free (h -> content);
		if (h -> scratch)
			buffer_free (h -> scratch);
		if (h -> sender)
			buffer_free (h -> sender);
		if (h -> recipient)
			buffer_free (h -> recipient);
		head_free_all (h -> head);
		free (h);
	}
	return NULL;
}/*}}}*/
void
header_clear (header_t *h) /*{{{*/
{
	if (h -> sender)
		buffer_clear (h -> sender);
	if (h -> recipient)
		buffer_clear (h -> recipient);
	h -> head = head_free_all (h -> head);
}/*}}}*/
buffer_t *
header_scratch (header_t *header, int size) /*{{{*/
{
	if (header -> scratch || (header -> scratch = buffer_alloc (size))) {
		buffer_clear (header -> scratch);
		return header -> scratch;
	}
	return NULL;
}/*}}}*/
bool_t
header_set_sender (header_t *h, const char *sender) /*{{{*/
{
	int	slen = strlen (sender);
	
	if (h -> sender || (h -> sender = buffer_alloc (slen + 1))) {
		return buffer_setsn (h -> sender, sender, slen);
	}
	return false;
}/*}}}*/
bool_t
header_set_recipient (header_t *h, const char *recipient, bool_t normalize) /*{{{*/
{
	int	rlen = strlen (recipient);
	
	if (h -> recipient || (h -> recipient = buffer_alloc (rlen + 1))) {
		return buffer_setsn (h -> recipient, recipient, rlen);
	}
	return false;
}/*}}}*/
void
header_set_charset (header_t *h, cvt_t *cvt, const char *charset) /*{{{*/
{
	if ((! h -> convert) || (! convert_match (h -> convert, charset)))
		h -> convert = cvt_find (cvt, charset);
}/*}}}*/
static bool_t
header_parse_raw (header_t *h, buffer_t *source, bool_t full) /*{{{*/
{
	bool_t		rc = true;
	const byte_t	*head = buffer_content (source);
	int		hlen = buffer_length (source);
	head_t		*current = NULL;
	head_t		*next;
	const byte_t	*ptr;
	int		pos, len;
	
	if (full)
		h -> head = head_free_all (h -> head);
	else if (h -> head)
		for (current = h -> head; current -> next; current = current -> next)
			;
	pos = 0;
	while (rc && (pos < hlen)) {
		ptr = head + pos;
		while (pos < hlen && (head[pos] != '\n'))
			++pos;
		if (pos < hlen)
			++pos;
		len = (head + pos) - ptr;
		if (len > 0) {
			if (isspace (*ptr)) {
				if ((len > 1) && current)
					rc = head_add (current, ptr, len);
			} else if (next = head_alloc ()) {
				if (current)
					current -> next = next;
				else
					h -> head = next;
				current = next;
				rc = head_add (current, ptr, len);
			} else
				rc = false;
		}
	}
	return rc;
}/*}}}*/
static bool_t
header_parse (header_t *h, buffer_t *source, bool_t full) /*{{{*/
{
	bool_t		rc = true;
	const byte_t	*head = buffer_content (source);
	int		hlen = buffer_length (source);
	head_t		*current = NULL;
	const byte_t	*ptr;
	int		pos, len;

	if (full)
		header_clear (h);
	else if (h -> head)
		for (current = h -> head; current -> next; current = current -> next)
			;
	pos = 0;
	while (rc && (pos < hlen)) {
		ptr = head + pos;
		while (pos < hlen && (head[pos] != '\n'))
			++pos;
		if (pos < hlen)
			++pos;
		len = (head + pos) - ptr;
		if (isspace (*ptr)) {
			if ((len > 1) && current)
				rc = head_add (current, ptr, len);
		} else if (full && ((*ptr == 'S') || (*ptr == 'R'))) {
			buffer_t	*dest;
				
			if ((*ptr == 'S') && (h -> sender || (h -> sender = buffer_alloc (256))))
				dest = h -> sender;
			else if ((*ptr == 'R') && (h -> recipient || (h -> recipient = buffer_alloc (256))))
				dest = h -> recipient;
			else
				dest = NULL;
			if (dest) {
				++ptr, --len;
					
				if ((len > 0) && (*ptr == '\n'))
					--len;
				buffer_clear (dest);
				if ((len > 0) && (*ptr == '<')) {
					++ptr, --len;
					while ((len > 0) && (ptr[len] != '>'))
						--len;
				}
				if (len > 0)
					rc = buffer_append (dest, ptr, len);
			} else
				rc = false;
		} else if (*ptr == 'H') {
			head_t	*next = head_alloc ();
			
			if (next) {
				++ptr, --len;
				if (current)
					current -> next = next;
				else
					h -> head = next;
				current = next;
				rc = head_add (current, ptr, len);
			} else
				rc = false;
		}
	}
	return rc;
}/*}}}*/
bool_t
header_set_content (header_t *h, xmlBufferPtr source) /*{{{*/
{
	if (h -> content || (h -> content = buffer_alloc (xmlBufferLength (source) + 128)))
		if (buffer_set (h -> content, xmlBufferContent (source), xmlBufferLength (source)))
			return header_parse (h, h -> content, true);
	return false;
}/*}}}*/
bool_t
header_append_content (header_t *h, xmlBufferPtr source) /*{{{*/
{
	if (header_scratch (h, xmlBufferLength (source) + 128) &&
	    buffer_set (h -> scratch, xmlBufferContent (source), xmlBufferLength (source)))
			return header_parse (h, h -> scratch, false);
	return false;
}/*}}}*/
bool_t
header_replace (header_t *h, xmlBufferPtr source) /*{{{*/
{
	if (header_scratch (h, xmlBufferLength (source) + 128) &&
	    buffer_set (h -> scratch, xmlBufferContent (source), xmlBufferLength (source)))
		return header_parse_raw (h, h -> scratch, true);
	return false;
}/*}}}*/
bool_t
header_insert (header_t *h, const char *line, head_t *after) /*{{{*/
{
	head_t	*tmp;
	
	if (tmp = head_alloc ()) {
		int	llen = strlen (line);

		if ((llen > 0) && (line[llen - 1] == '\r'))
			--llen;
		if (head_addsn (tmp, line, llen) && ((llen == 0) || (line[llen - 1] != '\n') ? head_addsn (tmp, "\n", 1) : true)) {
			if (after) {
				tmp -> next = after -> next;
				after -> next = tmp;
			} else {
				tmp -> next = h -> head;
				h -> head = tmp;
			}
		}
	}
	return tmp ? true : false;
}/*}}}*/
void
header_remove (header_t *h, const char *name) /*{{{*/
{
	head_t	*cur, *prev, *temp;
	int	namelength;
	
	for (cur = h -> head, prev = NULL, namelength = strlen (name); cur; ) {
		temp = cur;
		cur = cur -> next;
		if (head_matchn (temp, name, namelength)) {
			head_free (temp);
			if (prev)
				prev -> next = cur;
			else
				h -> head = cur;
		} else
			prev = temp;
	}
}/*}}}*/
bool_t
header_revalidate_mfrom (header_t *h, void *spf) /*{{{*/
{
	bool_t		rc;
	head_t		*run;
	const char	*line;
	char		*sender;
	
	for (run = h -> head, rc = true; run; run = run -> next)
		if (head_matchn (run, "from", 4)) {
			if ((line = head_value (run)) && *line) {
				if (sender = extract_address (line)) {
					if (*sender && spf_is_valid (spf, sender) && (h -> sender || (h -> sender = buffer_alloc (256))))
						buffer_sets (h -> sender, sender);
					free (sender);
				} else
					rc = false;
			}
			break;
		}
	return rc;
}/*}}}*/
int
header_size (header_t *h) /*{{{*/
{
	int	size;
	head_t	*head;

	for (head = h -> head, size = 0; head; head = head -> next)
		size += buffer_length (head -> h);
	return size;
}/*}}}*/
static bool_t
is_list_information (head_t *head) /*{{{*/
{
	int		hlen;
	const char	*name = head_find_name (head, & hlen);
	int		n;
	static struct {
		const char	*name;
		int		nlen;
	}	list_information_header[] = {
# define	LIH_ENTRY(nnn)		{	nnn, sizeof (nnn) - 1	}
		LIH_ENTRY ("list-unsubscribe"),
		LIH_ENTRY ("list-unsubscribe-post")
# undef		LIH_ENTRY			
	};
	
	if (name)
		for (n = 0; n < sizeof (list_information_header) / sizeof (list_information_header[0]); ++n)
			if ((hlen == list_information_header[n].nlen) && (! strcmp (name, list_information_header[n].name)))
				return true;
	return false;
}/*}}}*/
void
header_cleanup (header_t *h, bool_t remove_list_information) /*{{{*/
{
	head_t	*run, *prev, *temp;
	
	for (run = h -> head, prev = NULL; run; ) {
		temp = run;
		run = run -> next;
		if ((! head_valid (temp)) ||
		    (head_empty (temp) && (! head_matchn (temp, "subject", 7))) ||
		    (remove_list_information && is_list_information (temp))) {
			if (prev)
				prev -> next = run;
			else
				h -> head = run;
			head_free (temp);
		} else
			prev = temp;
	}
}/*}}}*/
buffer_t *
header_encode (header_t *h, head_t *head) /*{{{*/
{
	if (header_scratch (h, 1024) && header_encode_line (h, head, h -> scratch))
		return h -> scratch;
	return NULL;
}/*}}}*/
buffer_t *
header_create (header_t *h, bool_t raw) /*{{{*/
{
	if (header_scratch (h, 4096)) {
		head_t	*head;
		
		for (head = h -> head; head; head = head -> next)
			if (raw)
				buffer_appendbuf (h -> scratch, head -> h);
			else
				header_encode_line (h, head, h -> scratch);
		if (! raw)
			buffer_appendch (h -> scratch, '\n');
	}
	return h -> scratch;
}/*}}}*/
buffer_t *
header_create_sendmail_spoolfile_header (header_t *h) /*{{{*/
{
	if (header_scratch (h, 4096)) {
		const byte_t	*source = buffer_content (h -> content);
		int		length = buffer_length (h -> content);
		int		pos = 0;
		int		line, linelength;
		const byte_t	*ptr;
		head_t		*head;
		
		while (pos < length) {
			line = pos;
			linelength = 0;
			while ((pos < length) && (source[pos] != '\n'))
				++pos, ++linelength;
			if (pos < length)
				++pos;
			if (linelength > 0) {
				ptr = source + line;
				if ((*ptr == 'S') || (*ptr == 'R')) {
					buffer_append (h -> scratch, ptr, 1);
					buffer_appendch (h -> scratch, '<');
					buffer_appendbuf (h -> scratch, *ptr == 'S' ? h -> sender : h -> recipient);
					buffer_appendsn (h -> scratch, ">\n", 2);
				} else if ((! isspace (*ptr)) && (*ptr != 'H')) {
					buffer_append (h -> scratch, ptr, linelength + 1);
				}
			}
		}
		for (head = h -> head; head; head = head ->next) {
			buffer_appendch (h -> scratch, 'H');
			header_encode_line (h, head, h -> scratch);
		}
		buffer_appendsn (h -> scratch, ".\n", 2);
	}
	return h -> scratch;
}/*}}}*/
