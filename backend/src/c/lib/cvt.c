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
# include	<iconv.h>
# include	<errno.h>
# include	"xml.h"

struct convert { /*{{{*/
	cvt_t		*cvt;
	char		*charset;
	iconv_t		cd;
	iconv_t		alt;
	convert_t	*next;
	/*}}}*/
};
struct cvt { /*{{{*/
	convert_t	*converts;
	buffer_t	*encode;
	/*}}}*/
};
static convert_t *
convert_free (convert_t *c) /*{{{*/
{
	if (c) {
		if (c -> charset)
			free (c -> charset);
		if (c -> cd != (iconv_t) -1)
			iconv_close (c -> cd);
		if (c -> alt != (iconv_t) -1)
			iconv_close (c -> alt);
		free (c);
	}
	return NULL;
}/*}}}*/
static convert_t *
convert_free_all (convert_t *c) /*{{{*/
{
	convert_t	*tmp;
	
	while (tmp = c) {
		c = c -> next;
		convert_free (tmp);
	}
	return NULL;
}/*}}}*/
static convert_t *
convert_alloc (cvt_t *cvt, const char *charset) /*{{{*/
{
	convert_t	*c;
	char		*alt;
	
	if (c = (convert_t *) malloc (sizeof (convert_t))) {
		c -> cvt = cvt;
		c -> charset = charset ? strdup (charset) : NULL;
		c -> cd = (iconv_t) -1;
		c -> alt = (iconv_t) -1;
		c -> next = NULL;
		if (charset) {
			if (! c -> charset) {
				c = convert_free (c);
			} else if (strcasecmp (c -> charset, "utf8") && strcasecmp (c -> charset, "utf-8")) {
				c -> cd = iconv_open (c -> charset, "UTF-8");
				if (c -> cd == (iconv_t) -1)
					c = convert_free (c);
				else if (alt = malloc (strlen (charset) + 32)) {
					sprintf (alt, "%s//TRANSLIT", charset);
					c -> alt = iconv_open (alt, "UTF-8");
					free (alt);
				}
			}
		}
	}
	return c;
}/*}}}*/
bool_t
convert_match (convert_t *c, const char *charset) /*{{{*/
{
	if (((! c -> charset) && (! charset)) ||
	    (c -> charset && charset && (! strcmp (c -> charset, charset))))
		return true;
	return false;
}/*}}}*/
const char *
convert_charset (convert_t *c) /*{{{*/
{
	return c -> charset ? c -> charset : "UTF-8";
}/*}}}*/
const buffer_t *
convert_encode (convert_t *c, const byte_t *source_buffer, size_t source_length) /*{{{*/
{
	if (c -> cd == (iconv_t) -1) {
		if (c -> cvt -> encode || (c -> cvt -> encode = buffer_alloc (source_length + 1))) {
			buffer_set (c -> cvt -> encode, source_buffer, source_length);
			return c -> cvt -> encode;
		}
	} else if ((c -> cvt -> encode || (c -> cvt -> encode = buffer_alloc (0))) && buffer_size (c -> cvt -> encode, source_length + 1024)) {
		int	state;
		
		for (state = 0; state < 2; ++state) {
			iconv_t	cd = state == 0 ? c -> cd : c -> alt;
			bool_t		final;
			buffer_t	*out;
			char		*inbuf;
			size_t		inlen;
			char		*outbuf;
			size_t		outlen;
			size_t		size;
			
			if (cd == (iconv_t) -1)
				continue;
		
			final = false;
			out = c -> cvt -> encode;
			inbuf = (char *) source_buffer;
			inlen = source_length;
			outbuf = (char *) out -> buffer;
			outlen = out -> size;
			while (! final) {
				final = inlen == 0;
				size = iconv (cd, final ? NULL : & inbuf, & inlen, & outbuf, & outlen);
				out -> length = out -> size - outlen;
				if (size == -1) {
					if (errno == E2BIG) {
						if (! buffer_size (out, out -> size * 2 + 1024))
							break;
						final = false;
					} else if ((errno == EILSEQ) && (inlen > 0)) {
						int	mblength = xchar_strict_length (inbuf[0]);

						if ((mblength > 0) && (mblength <= inlen)) {
							unsigned long	codepoint = 0;
						
							if (xchar_codepoint ((const xchar_t *) inbuf, inlen, & codepoint)) {
								if (! buffer_size (out, out -> size + 32))
									break;
								buffer_format (out, "&#%lu;", codepoint);
							}
							inbuf += mblength;
							inlen -= mblength;
						} else
							break;
					}
					outbuf = (char *) out -> buffer + out -> length;
					outlen = out -> size - out -> length;
				}
			}
			if (size != -1)
				return out;
			iconv (cd, NULL, NULL, NULL, NULL);
		}
	}
	return NULL;
}/*}}}*/
const buffer_t *
convert_encode_buffer (convert_t *c, const buffer_t *source) /*{{{*/
{
	if (c -> cd == (iconv_t) -1)
		return source;
	return convert_encode (c, buffer_content (source), buffer_length (source));
}/*}}}*/
cvt_t *
cvt_alloc (void) /*{{{*/
{
	cvt_t	*c;
	
	if (c = (cvt_t *) malloc (sizeof (cvt_t))) {
		c -> converts = NULL;
		c -> encode = NULL;
	}
	return c;
}/*}}}*/
cvt_t *
cvt_free (cvt_t *c) /*{{{*/
{
	if (c) {
		convert_free_all (c -> converts);
		buffer_free (c -> encode);
		free (c);
	}
	return NULL;
}/*}}}*/
convert_t *
cvt_find (cvt_t *c, const char *charset) /*{{{*/
{
	convert_t	*temp, *prev;
	
	for (temp = c -> converts, prev = NULL; temp; temp = temp -> next) {
		if (convert_match (temp, charset)) {
			if (prev) {
				prev -> next = temp -> next;
				temp -> next = c -> converts;
				c -> converts = temp;
			}
			break;
		}
		prev = temp;
	}
	if ((! temp) && (temp = convert_alloc (c, charset))) {
		temp -> next = c -> converts;
		c -> converts = temp;
	}
	return temp;
}/*}}}*/
