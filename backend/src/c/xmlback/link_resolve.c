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
# include	<ctype.h>
# include	<math.h>
# include	<time.h>
# include	<openssl/md5.h>
# include	"xmlback.h"

# define	SWAP(iii,ooo)				\
	do {						\
		buffer_t	*__temp = (iii);	\
		(iii) = (ooo);				\
		(ooo) = __temp;				\
		buffer_clear (ooo);			\
	}	while (0)

typedef struct coder	coder_t;
struct coder { /*{{{*/
	char		*name;
	void		(*codec) (coder_t *, bool_t, buffer_t *, buffer_t *);
	coder_t		*next;
	/*}}}*/
};

static char	hex[] = "0123456789abcdef";
static void
codec_encode_hex (coder_t *c, bool_t final, buffer_t *in, buffer_t *out) /*{{{*/
{
	if (buffer_size (out, in -> length * 2)) {
		byte_t	*ptr = in -> buffer;
		int	len = in -> length;
		byte_t	ch;
		
		while (len-- > 0) {
			ch = *ptr++;
			buffer_appendch (out, hex[ch >> 4]);
			buffer_appendch (out, hex[ch & 0xf]);
		}
	}
}/*}}}*/
static void
codec_md5 (coder_t *c, bool_t final, buffer_t *in, buffer_t *out) /*{{{*/
{
	MD5_CTX		hash;
	unsigned char	digest[MD5_DIGEST_LENGTH];

	MD5_Init (& hash);
	MD5_Update (& hash, in -> buffer, in -> length);
	MD5_Final (digest, & hash);
	buffer_set (out, digest, MD5_DIGEST_LENGTH);
	if (final) {
		SWAP (in, out);
		codec_encode_hex (c, final, in, out);
	}
}/*}}}*/
static coder_t *
coder_free (coder_t *c) /*{{{*/
{
	if (c) {
		if (c -> name)
			free (c -> name);
		free (c);
	}
	return NULL;
}/*}}}*/
static coder_t *
coder_free_all (coder_t *c) /*{{{*/
{
	coder_t	*tmp;
	
	while (tmp = c) {
		c = c -> next;
		coder_free (tmp);
	}
	return NULL;
}/*}}}*/
static coder_t *
coder_alloc (const char *name, void (*codec) (coder_t *, bool_t, buffer_t *, buffer_t *)) /*{{{*/
{
	coder_t	*c;
	
	if (c = (coder_t *) malloc (sizeof (coder_t))) {
		c -> name = name ? strdup (name) : NULL;
		c -> codec = codec;
		c -> next = NULL;
	}
	return c;
}/*}}}*/
static void
coder_encode (coder_t *c, bool_t final, buffer_t *in, buffer_t *out) /*{{{*/
{
	c -> codec (c, final, in, out);
}/*}}}*/

typedef struct hashtag	hashtag_t;
struct hashtag { /*{{{*/
	char		**elements;	/* elements from tag			*/
	int		ecount;		/* # of elements			*/
	char		*options;	/* buffer to create option strings	*/
	int		start, end;	/* position in link			*/
	bool_t		raw;		/* no encoding for output value		*/
	int		column;		/* column referenced			*/
	coder_t		*coder;		/* encoding logics			*/
	bool_t		(*creator) (buffer_t *, hashtag_t *, blockmail_t *, block_t *, url_t *, receiver_t *, record_t *);
					/* callback for value			*/
	buffer_t	*createbuf;	/* target for callback			*/
	buffer_t	*fixed;		/* fixed value				*/
	struct hashtag	*next;
	/*}}}*/
};
static hashtag_t *
hashtag_free (hashtag_t *h) /*{{{*/
{
	if (h) {
		if (h -> elements) {
			if (h -> elements[0])
				free (h -> elements[0]);
			free (h -> elements);
		}
		if (h -> options)
			free (h -> options);
		coder_free_all (h -> coder);
		buffer_free (h -> createbuf);
		buffer_free (h -> fixed);
		free (h);
	}
	return NULL;
}/*}}}*/
static hashtag_t *
hashtag_free_all (hashtag_t *h) /*{{{*/
{
	hashtag_t	*tmp;
	
	while (tmp = h) {
		h = h -> next;
		hashtag_free (tmp);
	}
	return NULL;
}/*}}}*/
static const char *
hashtag_options (hashtag_t *h, int start, const char *default_value) /*{{{*/
{
	if (h -> options && (start < h -> ecount)) {
		int	n;
		char	*ptr;
		
		for (n = start, ptr = h -> options; n < h -> ecount; ++n) {
			if (n > start)
				*ptr++ = ':';
			strcpy (ptr, h -> elements[n]);
			for (; *ptr; ++ptr)
				;
		}
		return h -> options;
	}
	return default_value;
}/*}}}*/

static bool_t
creator_agnuid (buffer_t *target, hashtag_t *h, blockmail_t *blockmail, block_t *block, url_t *url, receiver_t *rec, record_t *record) /*{{{*/
{
	char	*uid = create_uid (blockmail, blockmail -> uid_version, NULL, rec, url, false);
	
	if (uid) {
		buffer_appends (target, uid);
		free (uid);
	}
	return uid ? true : false;
}/*}}}*/
static bool_t
creator_pubid (buffer_t *target, hashtag_t *h, blockmail_t *blockmail, block_t *block, url_t *url, receiver_t *rec, record_t *record) /*{{{*/
{
	char	*pubid = create_pubid (blockmail, rec, h -> ecount > 1 ? h -> elements[1] : "NL", hashtag_options (h, 2, NULL));
	
	if (pubid) {
		buffer_appends (target, pubid);
		free (pubid);
	}
	return pubid ? true : false;
}/*}}}*/
static void
format_date (buffer_t *target, const char *format, int *date) /*{{{*/
{
	if (format) {
		char	last, ch;
		int	count, value;
		
		last = '\0';
		count = 0;
		for (;;) {
			ch = *format++;
			if (last == '\'') {
				if (ch == '\'')
					last = '\0';
				else
					buffer_stiffch (target, ch);
			} else if (ch != last) {
				if (last) {
					switch (last) {
					default:	value = -1;		break;
					case 'y':	value = date[0];	break;
					case 'M':	value = date[1];	break;
					case 'd':	value = date[2];	break;
					case 'h':	value = date[3];	break;
					case 'm':	value = date[4];	break;
					case 's':	value = date[5];	break;
					}
					if (value != -1) {
						value %= (int) (pow (10, count));
						buffer_format (target, "%*.*d", count, count, value);
					} else
						while (count-- > 0)
							buffer_stiffch (target, last);
				}
				count = 1;
				last = ch;
			} else
				++count;
			if (! ch)
				break;
		}
	} else
		buffer_format (target, "%d.%d.%04d", date[2], date[1], date[0]);
}/*}}}*/
static hashtag_t *
hashtag_alloc (int start, int end, const byte_t *tag, int tlen, blockmail_t *blockmail, url_t *url) /*{{{*/
{
	hashtag_t	*h;
	
	if (h = (hashtag_t *) malloc (sizeof (hashtag_t))) {
		char	*scratch;

		h -> elements = NULL;
		h -> ecount = 0;
		h -> start = start;
		h -> end = end;
		h -> options = NULL;
		h -> raw = false;
		h -> column = -1;
		h -> coder = NULL;
		h -> creator = NULL;
		h -> createbuf = NULL;
		h -> fixed = NULL;
		h -> next = NULL;
		if ((tlen > 0) && (scratch = malloc (tlen + 1))) {
			int	esize = 8;
			
			memcpy (scratch, tag, tlen);
			scratch[tlen] = '\0';
			h -> options = malloc (tlen + 1);
			if (h -> elements = (char **) malloc (esize * sizeof (char *))) {
				char	*ptr;

				h -> elements[h -> ecount++] = scratch;
				for (ptr = strchr (scratch, ':'); ptr; ptr = strchr (ptr, ':')) {
					*ptr++ = '\0';
					if (h -> ecount == esize) {
						esize += esize;
						if (! (h -> elements = (char **) realloc (h -> elements, esize * sizeof (char *))))
							break;
					}
					h -> elements[h -> ecount++] = ptr;
				}
			}
			if (h -> elements) {
				char	*first = h -> elements[0];
			
				if (! strcasecmp (first, "MAILING_ID")) {
					if (h -> fixed = buffer_alloc (32))
						buffer_format (h -> fixed, "%d", blockmail -> mailing_id);
				} else if (! strcasecmp (first, "URL_ID")) {
					if (h -> fixed = buffer_alloc (32))
						buffer_format (h -> fixed, "%ld", url -> url_id);
				} else if (! strcasecmp (first, "SENDDATE-UNENCODED")) {
					if (h -> fixed = buffer_alloc (0)) {
						h -> raw = true;
						format_date (h -> fixed, hashtag_options (h, 1, NULL), blockmail -> senddate);
					}
				} else if (! strcasecmp (first, "DATE")) {
					time_t		now;
					struct tm	*tt;
					
					time (& now);
					if ((tt = localtime (& now)) && (h -> fixed = buffer_alloc (0))) {
						int	date[6] = { tt -> tm_year + 1900, tt -> tm_mon + 1, tt -> tm_mday, tt -> tm_hour, tt -> tm_min, tt -> tm_sec };

						format_date (h -> fixed, hashtag_options (h, 1, NULL), date);
					}
				} else if (! strcasecmp (first, "AGNUID")) {
					h -> creator = creator_agnuid;
				} else if (! strcasecmp (first, "PUBID")) {
					h -> creator = creator_pubid;
				}
				if (h -> creator)
					h -> createbuf = buffer_alloc (0);
			}
		}
	}
	return h;
}/*}}}*/
static hashtag_t *
hashtag_parse (buffer_t *lnk, blockmail_t *blockmail, url_t *url) /*{{{*/
{
	hashtag_t	*root, *prev, *temp;
	const xmlChar	*ptr;
	int		n;
	
	root = NULL;
	prev = NULL;
	for (ptr = lnk -> buffer, n = 0; n < lnk -> length; )
		if ((*ptr == '#') && (n + 1 < lnk -> length) && (*(ptr + 1) == '#')) {
			int	start, end;

			start = n;
			ptr += 2;
			n += 2;
			while ((n < lnk -> length - 1) && ((*ptr != '#') || (*(ptr + 1) != '#')))
				++ptr, ++n;
			if (n < lnk -> length + 1) {
				ptr += 2;
				n += 2;
				end = n;
				if (temp = hashtag_alloc (start, end, lnk -> buffer + start + 2, end - start - 4, blockmail, url)) {
					if (prev)
						prev -> next = temp;
					else
						root = temp;
					prev = temp;
				}
			} else
				n = lnk -> length;
		} else
			++ptr, ++n;
	return root;
}/*}}}*/
static void
to_target (hashtag_t *h, const buffer_t *input1, xmlBufferPtr input2, buffer_t *target, block_t *block) /*{{{*/
{
	if (input1 || input2) {
		buffer_t	*scratch[3] = { NULL, NULL, NULL };
		const buffer_t	*input;
		int		n;
		
		if (input1)
			input = input1;
		else {
			if (scratch[0] = buffer_alloc (xmlBufferLength (input2) + 1))
				buffer_set (scratch[0], xmlBufferContent (input2), xmlBufferLength (input2));
			input = scratch[0];
		}
		if (input) {
			if (h -> coder) {
				scratch[1] = buffer_alloc (0);
				scratch[2] = buffer_alloc (0);
				if (scratch[1] && scratch[2]) {
					coder_t		*run;
				
					buffer_setbuf (scratch[1], input);
					for (run = h -> coder; run; run = run -> next) {
						coder_encode (run, run -> next ? false : true, scratch[1], scratch[2]);
						SWAP (scratch[1], scratch[2]);
					}
					input = scratch[1];
				}
			}
		}
		if (input)
			if (h -> raw) {
				buffer_appendbuf (target, input);
			} else {
				if (block -> convert) {
					input = convert_encode_buffer (block -> convert, input);
				}
				if (input)
					encode_url (buffer_content (input), buffer_length (input), target);
			}
		for (n = 0; n < sizeof (scratch) / sizeof (scratch[0]); ++n)
			buffer_free (scratch[n]);
	}
}/*}}}*/
static void
hashtag_process (hashtag_t *h, buffer_t *target, blockmail_t *blockmail, block_t *block, url_t *url, receiver_t *rec, record_t *record) /*{{{*/
{
	if (h -> fixed) {
		if (h -> fixed -> length > 0)
			to_target (h, h -> fixed, NULL, target, block);
	} else if (h -> creator) {
		buffer_clear (h -> createbuf);
		if (((*h -> creator) (h -> createbuf, h, blockmail, block, url, rec, record)) && h -> createbuf -> length)
			to_target (h, h -> createbuf, NULL, target, block);
	} else if ((h -> column >= 0) && (h -> column < record -> dsize)) {
		if ((! record -> isnull[h -> column]) && (xmlBufferLength (record -> data[h -> column]) > 0)) {
			if (h -> ecount == 1)
				to_target (h, NULL, record -> data[h -> column], target, block);
		}
	}
}/*}}}*/
struct resolved { /*{{{*/
	buffer_t	*link;		/* the link itself	*/
	buffer_t	*scratch;	/* used, if hashtags	*/
	hashtag_t	*hashtags;	/* hashtags in link	*/
	/*}}}*/
};
static resolved_t *
resolved_free (resolved_t *r) /*{{{*/
{
	if (r) {
		buffer_free (r -> link);
		buffer_free (r -> scratch);
		hashtag_free_all (r -> hashtags);
		free (r);
	}
	return NULL;
}/*}}}*/
static resolved_t *
resolved_alloc (const byte_t *source, int len, blockmail_t *blockmail, url_t *url) /*{{{*/
{
	resolved_t	*r;
	
	if (r = (resolved_t *) malloc (sizeof (resolved_t))) {
		r -> link = NULL;
		r -> scratch = NULL;
		r -> hashtags = NULL;
		if (r -> link = buffer_alloc (len + 1)) {
			buffer_set (r -> link, source, len);
			if (r -> hashtags = hashtag_parse (r -> link, blockmail, url))
				if (! (r -> scratch = buffer_alloc (r -> link -> length + 256)))
					r = resolved_free (r);
		} else
			r = resolved_free (r);
	}
	return r;
}/*}}}*/
static buffer_t *
resolved_link (resolved_t *r, blockmail_t *blockmail, block_t *block, url_t *url, receiver_t *rec, record_t *record) /*{{{*/
{
	buffer_t	*rc;
	
	if (r -> hashtags) {
		int		pending = 0;
		hashtag_t	*run = r -> hashtags;
		int		start;
		
		buffer_clear (r -> scratch);
		for (run = r -> hashtags; ; run = run -> next) {
			if ( !run) {
				start = r -> link -> length;
			} else {
				start = run -> start;
			}
			if (pending < start)
				buffer_append (r -> scratch, r -> link -> buffer + pending, start - pending);
			if (run) {
				hashtag_process (run, r -> scratch, blockmail, block, url, rec, record);
				pending = run -> end;
			} else
				break;
		}
		rc = r -> scratch;
	} else
		rc = r -> link;
	return rc;
}/*}}}*/

link_resolve_t *
link_resolve_alloc (void) /*{{{*/
{
	link_resolve_t	*lr;
	
	if (lr = (link_resolve_t *) malloc (sizeof (link_resolve_t))) {
		lr -> pure = NULL;
		lr -> extended = NULL;
	}
	return lr;
}/*}}}*/
link_resolve_t *
link_resolve_free (link_resolve_t *lr) /*{{{*/
{
	if (lr) {
		resolved_free (lr -> pure);
		resolved_free (lr -> extended);
		free (lr);
	}
	return NULL;
}/*}}}*/
bool_t
link_resolve_prepare (link_resolve_t *lr, blockmail_t *blockmail, url_t *url) /*{{{*/
{
	lr -> pure = resolved_alloc (url -> dest -> buffer, url -> dest -> length, blockmail, url);
	return lr -> pure ? true : false;
}/*}}}*/
static resolved_t *
lr_select (link_resolve_t *lr, receiver_t *rec) /*{{{*/
{
	return rec -> disable_link_extension || (! lr -> extended) ? lr -> pure : lr -> extended;
}/*}}}*/
buffer_t *
link_resolve_get (link_resolve_t *lr, blockmail_t *blockmail, block_t *block, url_t *url, receiver_t *rec, record_t *record) /*{{{*/
{
	return resolved_link (lr_select (lr, rec), blockmail, block, url, rec, record);
}/*}}}*/
