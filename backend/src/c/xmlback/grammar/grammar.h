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
# ifndef	__GRAMMAR_H
# define	__GRAMMAR_H		1
# include	"xmlback.h"

typedef struct { /*{{{*/
	int		tid;
	char		*token;
	/*}}}*/
}	token_t;

typedef struct { /*{{{*/
	buffer_t	*buf;
	unsigned long	errcnt;
	buffer_t	*parse_error;
	xconv_t		*xconv;
	/*}}}*/
}	private_t;

extern token_t		*token_alloc (int tid, const char *token);
extern token_t		*token_free (token_t *t);

extern bool_t		transform (buffer_t *buf, const xmlChar *input, int input_length, buffer_t *parse_error, xconv_t *xconv);
# ifndef	NDEBUG
extern bool_t		transformtable_check (buffer_t *out);
# endif		/* NDEBUG */

extern void		ParseTrace (FILE *, char *);
extern const char	*ParseTokenName (int);
extern void		*ParseAlloc (void *(*) (size_t));
extern void		ParseFree (void *, void (*) (void *));
extern void		Parse (void *, int, token_t *, private_t *);
#endif		/* __GRAMMAR_H */
