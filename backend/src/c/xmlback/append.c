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
/*	-*- mode: c; mode: fold -*-	*/
# include	"xmlback.h"

static bool_t
final_eol (buffer_t *dest) /*{{{*/
{
	if ((dest -> length > 0) && (! buffer_iseol (dest, dest -> length - 1)))
		return buffer_appendnl (dest);
	return true;
}/*}}}*/
bool_t
append_mixed (buffer_t *dest, const char *desc, ...) /*{{{*/
{
	va_list		par;
	bool_t		rc;
	int		n;
	
	va_start (par, desc);
	rc = true;
	for (n = 0; rc && desc[n]; ++n)
		switch (desc[n]) {
		case 's':
			rc = buffer_stiffs (dest, va_arg (par, const char *));
			break;
		case 'b':
			{
				xmlBufferPtr	temp;
				
				temp = va_arg (par, xmlBufferPtr);
				rc = buffer_stiff (dest, xmlBufferContent (temp), xmlBufferLength (temp));
			}
			break;
		case 'u':
			rc = buffer_stiffbuf (dest, va_arg (par, buffer_t *));
			break;
		case 'i':
			{
				int		len;
				char		scratch[32];
				
				len = sprintf (scratch, "%d", va_arg (par, int));
				rc = buffer_stiffsn (dest, scratch, len);
			}
			break;
		default:
			rc = false;
			break;
		}
	return rc;
}/*}}}*/
bool_t
append_pure (buffer_t *dest, const xmlBufferPtr src) /*{{{*/
{
	return buffer_stiff (dest, xmlBufferContent (src), xmlBufferLength (src));
}/*}}}*/
bool_t
append_raw (buffer_t *dest, const buffer_t *src) /*{{{*/
{
	if (src -> length)
		return (buffer_stiff (dest, src -> buffer, src -> length) && buffer_stiffnl (dest)) ? true : false;
	return true;
}/*}}}*/
bool_t
append_cooked (buffer_t *dest, const xmlBufferPtr src,
	       const char *charset, encoding_t method) /*{{{*/
{
	bool_t	st;
	
	st = false;
	switch (method) {
	case EncNone:
		st = encode_none (src, dest);
		break;
	case EncHeader:
		st = encode_header (src, dest, charset);
		break;
	case Enc8bit:
		st = encode_8bit (src, dest);
		break;
	case EncQuotedPrintable:
		st = encode_quoted_printable (src, dest);
		break;
	case EncBase64:
		st = encode_base64 (src, dest);
		break;
	}
	if (st)
		st = final_eol (dest);
	return st;
}/*}}}*/
