/********************************************************************************************************************************************************************************************************************************************************************
 *                                                                                                                                                                                                                                                                  *
 *                                                                                                                                                                                                                                                                  *
 *        Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)                                                                                                                                                                                              *
 *                                                                                                                                                                                                                                                                  *
 *        This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.    *
 *        This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.           *
 *        You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.                                                                                                       *
 *                                                                                                                                                                                                                                                                  *
 ********************************************************************************************************************************************************************************************************************************************************************/
# include	<sys/types.h>
# include	<sys/socket.h>
# include	<sys/un.h>
# include	<sys/select.h>
# include	<mpack.h>
# include	"xmlback.h"

typedef struct typed_parameter	typed_parameter_t;
struct typed_parameter { /*{{{*/
	char			*option;
	int			option_length;
	enum {
		TP_Nil,
		TP_Boolean,
		TP_Integer,
		TP_Float,
		TP_String,
		TP_Binary
	}			type;
	union {
		bool_t		value_boolean;
		long		value_integer;
		double		value_float;
		char		*value_string;
		buffer_t	*value_binary;
	}			value;
	typed_parameter_t	*next;
	/*}}}*/
};
struct hparam { /*{{{*/
	typed_parameter_t	*head;
	typed_parameter_t	*tail;
	/*}}}*/
};
static typed_parameter_t *
typed_parameter_free (typed_parameter_t *tp) /*{{{*/
{
	if (tp) {
		if (tp -> option)
			free (tp -> option);
		switch (tp -> type) {
		case TP_Nil:
		case TP_Boolean:
		case TP_Integer:
		case TP_Float:
			break;
		case TP_String:
			if (tp -> value.value_string)
				free (tp -> value.value_string);
			break;
		case TP_Binary:
			if (tp -> value.value_binary)
				buffer_free (tp -> value.value_binary);
			break;
		}
		free (tp);
	}
	return NULL;
}/*}}}*/
static typed_parameter_t *
typed_parameter_free_all (typed_parameter_t *tp) /*{{{*/
{
	typed_parameter_t	*temp;
	
	while (temp = tp) {
		tp = tp -> next;
		typed_parameter_free (temp);
	}
	return NULL;
}/*}}}*/
static typed_parameter_t *
typed_parameter_alloc (const char *option, int option_length) /*{{{*/
{
	typed_parameter_t	*tp;
	
	if (tp = (typed_parameter_t *) malloc (sizeof (typed_parameter_t))) {
		if (option_length < 0)
			option_length = option ? strlen (option) : 0;
		
		if (tp -> option = malloc (option_length + 1)) {
			if (option_length > 0)
				memcpy (tp -> option, option, option_length);
			tp -> option[option_length] = 0;
			tp -> option_length = option_length;
			tp -> type = TP_Nil;
			tp -> next = NULL;
		} else {
			free (tp);
			tp = NULL;
		}
	}
	return tp;
}/*}}}*/
static typed_parameter_t *
typed_parameter_new_nil (const char *option, int option_length) /*{{{*/
{
	typed_parameter_t	*tp;
	
	if (tp = typed_parameter_alloc (option, option_length))
		tp -> type = TP_Nil;
	return tp;
}/*}}}*/
static typed_parameter_t *
typed_parameter_new_boolean (const char *option, int option_length, bool_t value) /*{{{*/
{
	typed_parameter_t	*tp;
	
	if (tp = typed_parameter_alloc (option, option_length)) {
		tp -> type = TP_Boolean;
		tp -> value.value_boolean = value;
	}
	return tp;
}/*}}}*/
static typed_parameter_t *
typed_parameter_new_integer (const char *option, int option_length, long value) /*{{{*/
{
	typed_parameter_t	*tp;
	
	if (tp = typed_parameter_alloc (option, option_length)) {
		tp -> type = TP_Integer;
		tp -> value.value_integer = value;
	}
	return tp;
}/*}}}*/
static typed_parameter_t *
typed_parameter_new_float (const char *option, int option_length, double value) /*{{{*/
{
	typed_parameter_t	*tp;
	
	if (tp = typed_parameter_alloc (option, option_length)) {
		tp -> type = TP_Float;
		tp -> value.value_float = value;
	}
	return tp;
}/*}}}*/
static typed_parameter_t *
typed_parameter_new_stringn (const char *option, int option_length, const char *value, int length) /*{{{*/
{
	typed_parameter_t	*tp;
	
	if (tp = typed_parameter_alloc (option, option_length)) {
		char	*copy;
		
		if (copy = malloc (length + 1)) {
			if (length > 0)
				memcpy (copy, value, length);
			copy[length] = 0;
			tp -> type = TP_String;
			tp -> value.value_string = copy;
		} else
			tp = typed_parameter_free (tp);
	}
	return tp;
}/*}}}*/
static typed_parameter_t *
typed_parameter_new_string (const char *option, int option_length, const char *value) /*{{{*/
{
	return typed_parameter_new_stringn (option, option_length, value, value ? strlen (value) : 0);
}/*}}}*/
static typed_parameter_t *
typed_parameter_new_binary (const char *option, int option_length, const byte_t *value, int value_size) /*{{{*/
{
	typed_parameter_t	*tp;
	
	if (tp = typed_parameter_alloc (option, option_length)) {
		buffer_t	*binary;
		
		if (binary = buffer_alloc (value_size)) {
			if (buffer_set (binary, value, value_size)) {
				tp -> type = TP_Binary;
				tp -> value.value_binary = binary;
			} else {
				buffer_free (binary);
				tp = typed_parameter_free (tp);
			}
		} else
			tp = typed_parameter_free (tp);
	}
	return tp;
}/*}}}*/
	
hparam_t *
hparam_alloc (void) /*{{{*/
{
	hparam_t	*hp;
	
	if (hp = (hparam_t *) malloc (sizeof (hparam_t)))
		hp -> head = hp -> tail = NULL;
	return hp;
}/*}}}*/
hparam_t *
hparam_free (hparam_t *hp) /*{{{*/
{
	if (hp) {
		typed_parameter_free_all (hp -> head);
		free (hp);
	}
	return NULL;
}/*}}}*/
static bool_t
hparam_add (hparam_t *hp, typed_parameter_t *tp) /*{{{*/
{
	if (tp) {
		if (hp -> tail)
			hp -> tail -> next = tp;
		else
			hp -> head = tp;
		hp -> tail = tp;
		return true;
	}
	return false;
}/*}}}*/
bool_t
hparam_add_nil (hparam_t *hp, const char *option, int option_length) /*{{{*/
{
	return hparam_add (hp, typed_parameter_new_nil (option, option_length));
}/*}}}*/
bool_t
hparam_add_boolean (hparam_t *hp, const char *option, int option_length, bool_t value) /*{{{*/
{
	return hparam_add (hp, typed_parameter_new_boolean (option, option_length, value));
}/*}}}*/
bool_t
hparam_add_integer (hparam_t *hp, const char *option, int option_length, long value) /*{{{*/
{
	return hparam_add (hp, typed_parameter_new_integer (option, option_length, value));
}/*}}}*/
bool_t
hparam_add_float (hparam_t *hp, const char *option, int option_length, double value) /*{{{*/
{
	return hparam_add (hp, typed_parameter_new_float (option, option_length, value));
}/*}}}*/
bool_t
hparam_add_string (hparam_t *hp, const char *option, int option_length, const char *value) /*{{{*/
{
	return hparam_add (hp, typed_parameter_new_string (option, option_length, value));
}/*}}}*/
bool_t
hparam_add_stringn (hparam_t *hp, const char *option, int option_length, const char *value, int length) /*{{{*/
{
	return hparam_add (hp, typed_parameter_new_stringn (option, option_length, value, length));
}/*}}}*/
bool_t
hparam_add_binary (hparam_t *hp, const char *option, int option_length, const byte_t *value, int value_size) /*{{{*/
{
	return hparam_add (hp, typed_parameter_new_binary (option, option_length, value, value_size));
}/*}}}*/

static const typed_parameter_t *
hparam_find (hparam_t *hp, const char *option, int type) /*{{{*/
{
	const typed_parameter_t	*run;
	
	for (run = hp -> head; run; run = run -> next)
		if ((run -> type == type) && (! strcmp (run -> option, option)))
			return run;
	return NULL;
}/*}}}*/
bool_t
hparam_get_boolean (hparam_t *hp, const char *option) /*{{{*/
{
	const typed_parameter_t	*run = hparam_find (hp, option, TP_Boolean);
	
	return run ? run -> value.value_boolean : false;
}/*}}}*/
const char *
hparam_get_string (hparam_t *hp, const char *option) /*{{{*/
{
	const typed_parameter_t	*run = hparam_find (hp, option, TP_String);
	
	return run ? run -> value.value_string : NULL;
}/*}}}*/

static char *
build_request (const hparam_t *request, int *request_size) /*{{{*/
{
	mpack_writer_t		writer;
	char			*data;
	size_t			size;
	typed_parameter_t	*run;
	
	mpack_writer_init_growable (& writer, & data, & size);
	mpack_build_map (& writer);
	for (run = request -> head; run; run = run -> next) {
		mpack_write_cstr (& writer, run -> option);
		switch (run -> type) {
		case TP_Nil:
			mpack_write_nil (& writer);
			break;
		case TP_Boolean:
			mpack_write_bool (& writer, run -> value.value_boolean);
			break;
		case TP_Integer:
			mpack_write_int (& writer, run -> value.value_integer);
			break;
		case TP_Float:
			mpack_write_double (& writer, run -> value.value_float);
			break;
		case TP_String:
			mpack_write_cstr (& writer, run -> value.value_string);
			break;
		case TP_Binary:
			mpack_write_bin (& writer, (const char *) buffer_content (run -> value.value_binary), buffer_length (run -> value.value_binary));
			break;
		}
	}
	mpack_complete_map (& writer);
	if (mpack_writer_destroy (& writer) == mpack_ok) {
		*request_size = size;
		return data;
	}
	return NULL;
}/*}}}*/
static hparam_t *
parse_response (buffer_t *response) /*{{{*/
{
	hparam_t	*rc;
	
	if (rc = hparam_alloc ()) {
		mpack_tree_t	tree;
		mpack_node_t	root;
		const byte_t	*buffer = buffer_content (response);
		int		size = buffer_length (response);
		
		mpack_tree_init_data (& tree, (const char *) buffer, size);
		mpack_tree_parse (& tree);
		root = mpack_tree_root (& tree);
		if (mpack_node_type (root) == mpack_type_map) {
			int		count;
			int		n;
			mpack_node_t	key, value;
			const char	*option;
			int		option_length;
			
			count = mpack_node_map_count (root);
			for (n = 0; rc && (n < count); ++n) {
				key = mpack_node_map_key_at (root, n);
				value = mpack_node_map_value_at (root, n);
				if (mpack_node_type (key) == mpack_type_str) {
					option = mpack_node_str (key);
					option_length = mpack_node_strlen (key);
					switch (mpack_node_type (value)) {
					case mpack_type_nil:
						hparam_add_nil (rc, option, option_length);
						break;
					case mpack_type_bool:
						hparam_add_boolean (rc, option, option_length, mpack_node_bool (value) ? true : false);
						break;
					case mpack_type_int:
						hparam_add_integer (rc, option, option_length, mpack_node_int (value));
						break;
					case mpack_type_uint:
						hparam_add_integer (rc, option, option_length, (long) mpack_node_uint (value));
						break;
					case mpack_type_float:
						hparam_add_float (rc, option, option_length, mpack_node_float (value));
						break;
					case mpack_type_double:
						hparam_add_float (rc, option, option_length, mpack_node_double (value));
						break;
					case mpack_type_str:
						hparam_add_stringn (rc, option, option_length, mpack_node_str (value), mpack_node_strlen (value));
						break;
					case mpack_type_bin:
						hparam_add_binary (rc, option, option_length, (const byte_t *) mpack_node_bin_data (value), mpack_node_bin_size (value));
						break;
					default:
						rc = hparam_free (rc);
						break;
					}
				}
			}
		} else
			rc = hparam_free (rc);
		if ((mpack_tree_destroy (& tree) != mpack_ok) && rc)
			rc = hparam_free (rc);
	}
	return rc;
}/*}}}*/
static buffer_t *
communicate (const char *method, int connect_timeout, int request_timeout, int response_timeout, const char *request, int request_size) /*{{{*/
{
	buffer_t	*rc = NULL;
	char		filename[256];
	char		*path;
	
	sprintf (filename, "%s.sock", method);
	if (path = mkpath (path_home (), "var", "comm", filename)) {
		int			path_length = strlen (path);
		struct sockaddr_un	addr;
		int			sd;
		
		if (path_length < sizeof (addr.sun_path)) {
			memset (& addr, 0, sizeof (addr));
			addr.sun_family = AF_UNIX;
			strcpy (addr.sun_path, path);
			sd = socket (AF_UNIX, SOCK_STREAM | SOCK_NONBLOCK, 0);
			if (sd != -1) {
				bool_t		connected = false;
				fd_set		fset;
				struct timeval	timeout;
				
				if (connect (sd, (const struct sockaddr *) & addr, sizeof (addr)) == -1) {
					if (errno == EINPROGRESS) {
						FD_ZERO (& fset);
						FD_SET (sd, & fset);
						timeout.tv_sec = connect_timeout;
						timeout.tv_usec = 0;
						if ((select (sd + 1, NULL, & fset, NULL, & timeout) == 1) && FD_ISSET (sd, & fset))
							connected = true;
					}
				} else
					connected = true;
				if (connected) {
					bool_t	eof = false;
					int	n;
					
					FD_ZERO (& fset);
					timeout.tv_sec = request_timeout;
					timeout.tv_usec = 0;
					while ((request_size > 0) && ((timeout.tv_sec > 0) || (timeout.tv_usec > 0))) {
						FD_SET (sd, & fset);
						if ((select (sd + 1, NULL, & fset, NULL, & timeout) == 1) && FD_ISSET (sd, & fset) && ((n = send (sd, request, request_size, 0)) > 0)) {
							request += n;
							request_size -= n;
						}
					}
					if ((shutdown (sd, SHUT_WR) != -1) && (request_size == 0) && (rc = buffer_alloc (4096))) {
						byte_t	scratch[4096];
						
						FD_SET (sd, & fset);
						timeout.tv_sec = response_timeout;
						timeout.tv_usec = 0;
						while ((! eof) && ((timeout.tv_sec > 0) || (timeout.tv_usec > 0)))
							if ((select (sd + 1, & fset, NULL, NULL, & timeout) == 1) && FD_ISSET (sd, & fset))
								if ((n = recv (sd, scratch, sizeof (scratch), 0)) > 0)
									buffer_append (rc, scratch, n);
								else if (n == 0)
									eof = true;
					}
					if ((shutdown (sd, SHUT_RD) == -1) || (! eof))
						rc = buffer_free (rc);
				}
				close (sd);
			}
		}
		free (path);
	}
	return rc;
}/*}}}*/

hparam_t *
helper_call_timeout (blockmail_t *blockmail, const char *method, const hparam_t *request, int connect_timeout, int request_timeout, int response_timeout) /*{{{*/
{
	hparam_t	*rc = NULL;
	char		*packed_request;
	int		packed_request_size;
	
	if (packed_request = build_request (request, & packed_request_size)) {
		buffer_t	*response;
		
		if (response = communicate (method, connect_timeout, request_timeout, response_timeout, packed_request, packed_request_size)) {
			rc = parse_response (response);
			buffer_free (response);
		}
		free (packed_request);
	}
	return rc;
}/*}}}*/
hparam_t *
helper_call (blockmail_t *blockmail, const char *method, const hparam_t *request) /*{{{*/
{
	return helper_call_timeout (blockmail, method, request, 2, 5, 30);
}/*}}}*/

