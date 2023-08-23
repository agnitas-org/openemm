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
# ifndef	__LIB_XML_H
# define	__LIB_XML_H		1
# include	"agn.h"

# define	XML_DEFAULT_ENCODING	"UTF-8"

/* Thin wrapper around buffer_t for handling buffers with
 * XML data
 */
typedef byte_t		xchar_t;
typedef struct { /*{{{*/
	int	csize;
	cache_t	*lower,
		*upper,
		*title;
	/*}}}*/
}	xconv_t;

extern int		xchar_length (xchar_t ch);
extern int		xchar_strict_length (xchar_t ch);
extern int		xchar_valid_position (const xchar_t *s, int length);
extern bool_t		xchar_valid (const xchar_t *s, int length);
extern bool_t		xequal (const xchar_t *s1, const xchar_t *s2);
extern const char	*xchar_to_char (const xchar_t *s);
extern const xchar_t	*char_2_xchar (const char *s);
extern const char	*byte_to_char (const byte_t *b);
extern int		xstrlen (const xchar_t *s);
extern int		xstrnlen (const xchar_t *s, int slen);
extern int		xstrcmp (const xchar_t *s1, const char *s2);
extern int		xstrncmp (const xchar_t *s1, const char *s2, size_t n);
extern xchar_t		*xsubstr (const xchar_t *s, int start, int end);
extern const xchar_t	*xtolower (const xchar_t *s, int *slen, int *olen);
extern const xchar_t	*xtoupper (const xchar_t *s, int *slen, int *olen);
extern const xchar_t	*xtotitle (const xchar_t *s, int *slen, int *olen);
extern xchar_t		*xlowern (const xchar_t *s, int len, int *olen);
extern xchar_t		*xlower (const xchar_t *s, int *olen);
extern xchar_t		*xuppern (const xchar_t *s, int len, int *olen);
extern xchar_t		*xupper (const xchar_t *s, int *olen);
extern xchar_t		*xtitlen (const xchar_t *s, int len, int *olen);
extern xchar_t		*xtitle (const xchar_t *s, int *olen);

extern xconv_t		*xconv_free (xconv_t *xc);
extern xconv_t		*xconv_alloc (int cache_size);
extern const xchar_t	*xconv_lower (xconv_t *xc, const xchar_t *s, int slen, int *olen);
extern const xchar_t	*xconv_upper (xconv_t *xc, const xchar_t *s, int slen, int *olen);
extern const xchar_t	*xconv_title (xconv_t *xc, const xchar_t *s, int slen, int *olen);
#endif		/* __LIB_XML_H */
