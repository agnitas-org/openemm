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
# include	<errno.h>
# include	"xmlback.h"

# if	0
static int
use_iconv (iconv_t ic, xmlBufferPtr out, xmlBufferPtr in) /*{{{*/
{
	int		rc;
	const char	*iptr;
	int		ilen;
	int		isize;
	char		*optr;
	int		olen;
	char		*obuf;
	int		osize;
	int		nsize;
	char		*temp;
	
	if (iconv (ic, NULL, NULL, NULL, NULL) == -1)
		return -1;
	rc = 0;
	iptr = xmlBufferContent (in);
	ilen = xmlBufferLength (in);
	osize = 0;
	obuf = NULL;
	nsize = -1;
	while ((rc != -1) && (ilen > 0)) {
		if (ilen < 8192)
			nsize = 8192;
		else
			nsize = ilen + 1;
		if (nsize > osize) {
			if (! (temp = realloc (obuf, nsize)))
				break;
			obuf = temp;
			osize = nsize;
		}
		optr = obuf;
		olen = osize;
		isize = ilen;
		if ((iconv (ic, & iptr, & ilen, & optr, & olen) != -1) || (errno == E2BIG)) {
			if (ilen < isize)
				if (osize - olen > 0)
					xmlBufferAdd (out, obuf, osize - olen);
		} else
			break;
	}
	if (obuf)
		free (obuf);
	if (ilen > 0)
		rc = -1;
	return rc;
}/*}}}*/
# endif
int
convert_block (xmlCharEncodingHandlerPtr translate, xmlBufferPtr in, xmlBufferPtr out, bool_t isoutput) /*{{{*/
{
	int	rc;
	
	rc = 0;
	if (translate)
		if (isoutput) {
# if	1
			if (translate -> output || translate -> iconv_out)
				if (xmlCharEncOutFunc (translate, out, in) < 0)
					rc = -1;
				else
					rc = 1;
# else			
			if (translate -> output) {
				if (xmlCharEncOutFunc (translate, out, in) < 0)
					rc = -1;
				else
					rc = 1;
			} else if (translate -> iconv_out) {
				if (use_iconv (translate -> iconv_out, out, in) < 0)
					rc = -1;
				else
					rc = 1;
			}
# endif			
		} else {
# if	1			
			if (translate -> input || translate -> iconv_in)
				if (xmlCharEncInFunc (translate, out, in) < 0)
					rc = -1;
				else
					rc = 1;
# else			
			if (translate -> input) {
				if (xmlCharEncInFunc (translate, out, in) < 0)
					rc = -1;
				else
					rc = 1;
			} else if (translate -> iconv_in) {
				if (use_iconv (translate -> iconv_in, out, in) < 0)
					rc = -1;
				else
					rc = 1;
			}
# endif			
		}
	return rc;
}/*}}}*/
bool_t
convert_charset (blockmail_t *blockmail, block_t *block) /*{{{*/
{
	bool_t	st;
	
	st = true;
	xmlBufferEmpty (block -> out);
	switch (convert_block (block -> translate, block -> in, block -> out, true)) {
	default:
	case -1:
		log_out (blockmail -> lg, LV_ERROR, "Unable to convert block %d to %s", block -> nr, block -> charset);
		st = false;
		break;
	case 0:
		xmlBufferAdd (block -> out, xmlBufferContent (block -> in), xmlBufferLength (block -> in));
		break;
	case 1:
		break;
	}
	return st;
}/*}}}*/
