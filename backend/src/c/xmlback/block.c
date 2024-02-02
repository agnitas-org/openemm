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
# include	<string.h>
# include	"xmlback.h"

static const xmlChar *
tagsearch (const xmlChar *haystack, int haystack_size, const xmlChar *needle, int needle_size, int *match_size) /*{{{*/
{
	const xmlChar	*match = NULL;
	int		match_index = 0;
	int		clen;

	while (haystack_size > 0) {
		clen = xmlCharLength (*haystack);
		if ((clen <= haystack_size) && (clen <= needle_size - match_index)) {
			if ((clen == 1) && isspace (*haystack) && match && (xmlCharLength (needle[match_index]) == 1) && isspace (needle[match_index])) {
				++haystack;
				--haystack_size;
				++match_index;
				while ((haystack_size > 0) && (xmlCharLength (*haystack) == 1) && isspace (*haystack))
					++haystack, --haystack_size;
				while ((match_index < needle_size) && (xmlCharLength (needle[match_index]) == 1) && isspace (needle[match_index]))
					++match_index;
				continue;
			}
			if (match && memcmp (haystack, needle + match_index, clen))
				match_index = 0;
			if (! memcmp (haystack, needle + match_index, clen)) {
				if (! match_index)
					match = haystack;
				match_index += clen;
				if (match_index == needle_size) {
					*match_size = haystack - match + clen;
					return match;
				}
			}
		} else
			match_index = 0;
		haystack += clen;
		haystack_size -= clen;
	}
	return NULL;
}/*}}}*/
block_t *
block_alloc (void) /*{{{*/
{
	block_t	*b;
	
	if (b = (block_t *) malloc (sizeof (block_t))) {
		b -> bid = -1;
		b -> nr = -1;
		b -> mime = NULL;
		b -> charset = NULL;
		b -> encode = NULL;
		b -> method = EncNone;
		b -> cid = NULL;
		b -> tid = TID_Unspec;
		b -> binary = false;
		b -> attachment = false;
		b -> precoded = false;
		b -> pdf = false;
		b -> font = false;
		b -> media = NULL;
		b -> mediatype = Mediatype_Unspec;
		b -> condition = NULL;
		b -> target_id = 0;
		b -> target_index = -1;
		b -> content = NULL;
		b -> convert = NULL;
		b -> in = NULL;
		b -> out = NULL;
		b -> bcontent = NULL;
		b -> bout = NULL;
		DO_ZERO (b, tagpos);
		b -> inuse = false;
	}
	return b;
}/*}}}*/
block_t *
block_free (block_t *b) /*{{{*/
{
	if (b) {
		if (b -> mime)
			free (b -> mime);
		if (b -> charset)
			free (b -> charset);
		if (b -> encode)
			free (b -> encode);
		if (b -> cid)
			free (b -> cid);
		if (b -> media)
			free (b -> media);
		if (b -> condition)
			xmlBufferFree (b -> condition);
		if (b -> content)
			xmlBufferFree (b -> content);
		if (b -> in)
			xmlBufferFree (b -> in);
		if (b -> out)
			xmlBufferFree (b -> out);
		if (b -> bcontent)
			buffer_free (b -> bcontent);
		if (b -> bout)
			buffer_free (b -> bout);
		DO_FREE (b, tagpos);
		free (b);
	}
	return NULL;
}/*}}}*/
void
block_swap_inout (block_t *b) /*{{{*/
{
	xmlBufferPtr	temp = b -> in;
	
	b -> in = b -> out;
	b -> out = temp;
}/*}}}*/
bool_t
block_setup_charset (block_t *b, cvt_t *cvt) /*{{{*/
{
	b -> convert = cvt_find (cvt, b -> charset);
	if (! b -> in)
		b -> in = xmlBufferCreate ();
	if (! b -> out)
		b -> out = xmlBufferCreate ();
	if (b -> in && b -> out)
		return true;
	return false;
}/*}}}*/
void
block_setup_tagpositions (block_t *b, blockmail_t *blockmail) /*{{{*/
{
	int		n;
	int		position;
	const xmlChar	*content = b -> content ? xmlBufferContent (b -> content) : NULL;
	int		length = b -> content ? xmlBufferLength (b -> content) : 0;

	for (n = 0, position = 0; n < b -> tagpos_count; ++n) {
		tagpos_t	*tp = b -> tagpos[n];
		
		if (content && tp -> name) {
			int		match_size;
			const xmlChar	*hit = tagsearch (content + position, length - position, xmlBufferContent (tp -> name), xmlBufferLength (tp -> name), & match_size);
			
			if (hit) {
				tp -> start = hit - content;
				tp -> end = position = tp -> start + match_size;
			} else {
				tp -> start = tp -> end = position = length;
			}
		}
		tagpos_setup_tag (tp, blockmail);
	}
}/*}}}*/
void
block_find_method (block_t *b) /*{{{*/
{
	static struct {
		const char	*name;
		encoding_t	code;
	}	codetab[] = {
		{	"none",			EncNone			},
		{	"8bit",			Enc8bit			},
		{	"quoted-printable",	EncQuotedPrintable	},
		{	"base64",		EncBase64		}
	};
	
	b -> method = EncNone;
	if (b -> encode) {
		int	n;
		
		for (n = 0; n < sizeof (codetab) / sizeof (codetab[0]); ++n)
			if (! strcmp (b -> encode, codetab[n].name)) {
				b -> method = codetab[n].code;
				break;
			}
	}
}/*}}}*/
bool_t
block_code_binary_out (block_t *b) /*{{{*/
{
	bool_t	st;
	int	current, assume;
	
	st = false;
	current = b -> bcontent -> length;
	assume = -1;
	switch (b -> method) {
	case EncNone:			/* 100% */
		assume = current;
		break;
	case Enc8bit:			/* 110% */
		assume = (current * 11) / 10;
		break;
	case EncQuotedPrintable:	/* 150% */
		assume = (current * 3) / 2;
		break;
	case EncBase64:			/* 150% */
		assume = (current * 3) / 2;
		break;
	}
	if (assume != -1) {
		if (! b -> bout)
			b -> bout = buffer_alloc (assume + 4);
		else
			b -> bout -> length = 0;
		if (b -> bout) {
			xmlBuffer	temp;	/* to avoid copying around */
			
			temp.content = b -> bcontent -> buffer;
			temp.use = b -> bcontent -> length;
			temp.size = b -> bcontent -> size;
			temp.alloc = -1;
			switch (b -> method) {
			case EncNone:
				st = encode_none (& temp, b -> bout);
				break;
			case Enc8bit:
				st = encode_8bit (& temp, b -> bout);
				break;
			case EncQuotedPrintable:
				st = encode_quoted_printable (& temp, b -> bout);
				break;
			case EncBase64:
				st = encode_base64 (& temp, b -> bout);
				break;
			}
		}
	}
	return st;
}/*}}}*/
bool_t
block_code_binary (block_t *b) /*{{{*/
{
	bool_t	st;
	
	st = false;
	if (b -> bcontent)
		b -> bcontent = buffer_free (b -> bcontent);
	if (b -> binary && b -> content && (b -> bcontent = buffer_alloc (xmlBufferLength (b -> content) + 1))) {
		st = decode_base64 (b -> content, b -> bcontent);
		if (! st)
			b -> bcontent = buffer_free (b -> bcontent);
	}
	if (st)
		st = block_code_binary_out (b);
	return st;
}/*}}}*/
static inline bool_t
code_binary (buffer_t **b, xmlBufferPtr buf) /*{{{*/
{
	bool_t	st;
	int	nsize;
	
	st = false;
	nsize = xmlBufferLength (buf) + 1;
	if (*b) {
		(*b) -> length = 0;
		if (! buffer_size (*b, nsize))
			*b = buffer_free (*b);
	} else
		*b = buffer_alloc (nsize);
	if (*b)
		if (! (st = decode_base64 (buf, *b)))
			*b = buffer_free (*b);
	return st;
}/*}}}*/
bool_t
block_match (block_t *b, eval_t *eval, receiver_t *rec) /*{{{*/
{
	if ((b -> target_id > 0) && (b -> target_index != -1))
		return dataset_match (rec -> rvdata, b -> target_index);
	if (! b -> condition) {
		return true;
	}
	return eval_match (eval, SP_BLOCK, b -> bid);
}/*}}}*/
