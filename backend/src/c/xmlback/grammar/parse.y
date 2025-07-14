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
%include {
# include	<ctype.h>
# include	<assert.h>	
# include	"grammar.h"
# include	"parse.h"

	static inline void
	perr (private_t *priv, const char *str)
	{
		if (priv -> parse_error) {
			buffer_appends (priv -> parse_error, str);
			buffer_appendch (priv -> parse_error, '\n');
		}
	}

	static inline buffer_t *
	tok2buf (token_t *t)
	{
		buffer_t	*b = buffer_alloc (256);
		
		buffer_sets (b, t -> token);
		token_free (t);
		return b;
	}
	
	static inline buffer_t *
	operation (buffer_t *a, const char *op, buffer_t *b)
	{
		buffer_t	*r = buffer_alloc (a -> length + b -> length + 16);
		
		buffer_format (r, "(%s) %s (%s)", buffer_string (a), op, buffer_string (b));
		buffer_free (a);
		buffer_free (b);
		return r;
	}
	
	static inline buffer_t *
	conversion (const char *funcname, buffer_t *a, xconv_t *xconv, const xchar_t *(*func) (xconv_t *, const xchar_t *, int, int *))
	{
		const char	*str = buffer_string (a);
		bool_t		purestring = false;
		buffer_t	*r = buffer_alloc (a -> length + 64);
		const xchar_t	*rplc;
		int		rlen;

		if (*str == '"') {
			do {
				if ((*str == '\\') && *(str + 1))
					++str;
				++str;
			}	while (*str && (*str != '"'));
			if ((*str == '"') && (! *(str + 1)))
				purestring = true;
		}
		if (purestring && (rplc = (*func) (xconv, (const xchar_t *) a -> buffer, a -> length, & rlen)))
			buffer_append (r, rplc, rlen);
		else
			buffer_format (r, "%s (%s)", funcname, buffer_string (a));
		buffer_free (a);
		return r;
	}
	
	static inline buffer_t *
	interval (buffer_t *a, int div)
	{
		buffer_t	*r = buffer_alloc (a -> length + 64);
		
		buffer_format (r, "((%s) / %d.0)", buffer_string (a), div);
		buffer_free (a);
		return r;
	}
}
%parse_failure {
	perr (priv, "Failed in parsing.");
}
%stack_overflow {
	perr (priv, "Stackoverflow detected.");
}
%syntax_error {
	perr (priv, "Syntax error hit.");
}
%stack_size		0
%default_type		{buffer_t *}
%default_destructor	{buffer_free ($$);}
%token_prefix		T_
%token_type		{token_t *}
%token_destructor	{token_free ($$);}
%extra_argument		{private_t *priv}

%left	OR.
%left	AND.
%right	NOT.
%left	EQ NE LIKE IS IN BETWEEN.
%left	GT GE LT LE.
%left	PLUS MINUS.
%left	STAR SLASH.

stmt	::=	expr(A).			{
	buffer_appendbuf (priv -> buf, A);
	buffer_free (A);
}
expr(R)	::=	expr(A) AND expr(B).		{
	R = buffer_alloc (A -> length + B -> length + 12);
	buffer_format (R, "(%s) and (%s)", buffer_string (A), buffer_string (B));
	buffer_free (A);
	buffer_free (B);
}
expr(R)	::=	expr(A) OR expr(B).		{
	R = buffer_alloc (A -> length + B -> length + 12);
	buffer_format (R, "(%s) or (%s)", buffer_string (A), buffer_string (B));
	buffer_free (A);
	buffer_free (B);
}
expr(R)	::=	OPEN expr(A) CLOSE.		{
	R = buffer_alloc (A -> length + 6);
	buffer_format (R, "(%s)", buffer_string (A));
	buffer_free (A);
}
expr(R)	::=	NOT expr(A).			{
	R = buffer_alloc (A -> length + 8);
	buffer_format (R, "not (%s)", buffer_string (A));
	buffer_free (A);
}
expr(R)	::=	value(A) LIKE value(B).		{
	R = buffer_alloc (A -> length + B -> length + 20);
	buffer_format (R, "like (%s, %s, \"\")", buffer_string (A), buffer_string (B));
	buffer_free (A);
	buffer_free (B);
}
expr(R)	::=	value(A) LIKE value(B) ESCAPE value(C).		{
	R = buffer_alloc (A -> length + B -> length + C -> length + 20);
	buffer_format (R, "like (%s, %s, %s)", buffer_string (A), buffer_string (B), buffer_string (C));
	buffer_free (A);
	buffer_free (B);
	buffer_free (C);
}
expr(R)	::=	value(A) NOT LIKE value(B).	{
	R = buffer_alloc (A -> length + B -> length + 32);
	buffer_format (R, "(not like (%s, %s, \"\"))", buffer_string (A), buffer_string (B));
	buffer_free (A);
	buffer_free (B);
}
expr(R)	::=	value(A) NOT LIKE value(B) ESCAPE value(C).	{
	R = buffer_alloc (A -> length + B -> length + C -> length + 32);
	buffer_format (R, "(not like (%s, %s, %s))", buffer_string (A), buffer_string (B), buffer_string (C));
	buffer_free (A);
	buffer_free (B);
	buffer_free (C);
}
expr(R)	::=	value(A) EQ value(B).		{
	R = operation (A, "==", B);
}
expr(R)	::=	value(A) NE value(B).		{
	R = operation (A, "!=", B);
}
expr(R)	::=	value(A) GT value(B).		{
	R = operation (A, ">", B);
}
expr(R)	::=	value(A) GE value(B).		{
	R = operation (A, ">=", B);
}
expr(R)	::=	value(A) LT value(B).		{
	R = operation (A, "<", B);
}
expr(R)	::=	value(A) LE value(B).		{
	R = operation (A, "<=", B);
}
value(R)::=	value(A) PLUS value(B).		{
	R = operation (A, "+", B);
}
value(R)::=	value(A) MINUS value(B).	{
	R = operation (A, "-", B);
}
value(R)::=	value(A) STAR value(B).		{
	R = operation (A, "*", B);
}
value(R)::=	value(A) SLASH value(B).	{
	R = operation (A, "/", B);
}
value(R)::=	MINUS value(A).			{
	R = buffer_alloc (A -> length + 2);
	buffer_format (R, "-%s", buffer_string (A));
	buffer_free (A);
}
list(R)	::=	value(A).			{
	R = A;
}
list(R)	::=	list(A) COMMA value(B).		{
	R = buffer_alloc (A -> length + B -> length + 8);
	buffer_format (R, "%s, %s", buffer_string (A), buffer_string (B));
	buffer_free (A);
	buffer_free (B);
}
expr(R)	::=	value(A) IN OPEN list(B) CLOSE.	{
	R = buffer_alloc (A -> length + B -> length + 16);
	buffer_format (R, "in (%s, %s)", buffer_string (A), buffer_string (B));
	buffer_free (A);
	buffer_free (B);
}
expr(R)	::=	value(A) NOT IN OPEN list(B) CLOSE.	{
	R = buffer_alloc (A -> length + B -> length + 24);
	buffer_format (R, "(not in (%s, %s))", buffer_string (A), buffer_string (B));
	buffer_free (A);
	buffer_free (B);
}
expr(R)	::=	value(A) BETWEEN value(B) AND value(C).	{
	R = buffer_alloc (A -> length + B -> length + C -> length + 32);
	buffer_format (R, "between (%s, %s, %s)", buffer_string (A), buffer_string (B), buffer_string (C));
	buffer_free (A);
	buffer_free (B);
	buffer_free (C);
}
expr(R)	::=	value(A) NOT BETWEEN value(B) AND value(C).	{
	R = buffer_alloc (A -> length + B -> length + C -> length + 32);
	buffer_format (R, "(not between (%s, %s, %s))", buffer_string (A), buffer_string (B), buffer_string (C));
	buffer_free (A);
	buffer_free (B);
	buffer_free (C);
}
value(R)::=	OPEN value(A) CLOSE.			{
	R = buffer_alloc (A -> length + 4);
	buffer_format (R, "(%s)", buffer_string (A));
	buffer_free (A);
}
value(R)::=	MOD OPEN value(A) COMMA value(B) CLOSE.	{
	R = buffer_alloc (A -> length + B -> length + 64);
	buffer_format (R, "modulo (%s, %s)", buffer_string (A), buffer_string (B));
	buffer_free (A);
	buffer_free (B);
}
value(R)::=	LOWER OPEN value(A) CLOSE.	{
	R = conversion ("lower", A, priv -> xconv, xconv_lower);
}
value(R)::=	UPPER OPEN value(A) CLOSE.	{
	R = conversion ("upper", A, priv -> xconv, xconv_upper);
}
value(R)::=	INITCAP OPEN value(A) CLOSE.	{
	R = conversion ("captialize", A, priv -> xconv, xconv_title);
}
value(R)::=	LENGTH OPEN value(A) CLOSE.	{
	R = buffer_alloc (A -> length + 16);
	buffer_format (R, "length (%s)", buffer_string (A));
	buffer_free (A);
}
value(R)::=	TO_CHAR OPEN value(A) COMMA value(B) CLOSE.	{
	R = buffer_alloc (A -> length + B -> length + 64);
	buffer_format (R, "to_char (%s, %s)", buffer_string (A), buffer_string (B));
	buffer_free (A);
	buffer_free (B);
}
value(R)::=	TO_CHAR OPEN value(A) CLOSE.	{
	R = buffer_alloc (A -> length + 64);
	buffer_format (R, "string (%s)", buffer_string (A));
	buffer_free (A);
}
value(R)::=	DATE_FORMAT OPEN value(A) COMMA value(B) CLOSE.	{
	R = buffer_alloc (A -> length + B -> length + 64);
	buffer_format (R, "date_format (%s, %s)", buffer_string (A), buffer_string (B));
	buffer_free (A);
	buffer_free (B);
}
value(R)::=	DECODE OPEN list(A) CLOSE.	{
	R = buffer_alloc (A -> length + 24);
	buffer_format (R, "decode (%s)", buffer_string (A));
	buffer_free (A);
}
value(R)::=	NAME(O) OPEN list(A) CLOSE.	{
	R = buffer_alloc (A -> length + 256);
	buffer_format (R, "custom->%s (%s)", O -> token, buffer_string (A));
	token_free (O);
	buffer_free (A);
}
value(R)::=	STR(O).				{
	R = tok2buf (O);
}
value(R)::=	NUM(O).				{
	R = tok2buf (O);
}
value(R)::=	VARIABLE(O).			{
	R = tok2buf (O);
}
expr(R)::=	VARIABLE(O) IS NULL.		{
	R = buffer_alloc (256);
	buffer_format (R, "NULL$%s", O -> token);
	token_free (O);
}
expr(R)::=	ISNULL OPEN VARIABLE(O) CLOSE.	{
	R = buffer_alloc (256);
	buffer_format (R, "NULL$%s", O -> token);
	token_free (O);
}
expr(R)::=	VARIABLE(O) IS NOT NULL.	{
	R = buffer_alloc (256);
	buffer_format (R, "(not NULL$%s)", O -> token);
	token_free (O);
}
value(R)::=	NOW OPEN CLOSE.			{
	R = buffer_alloc (16);
	buffer_sets (R, "sysdate");
}
value(R)::=	SYSDATE.			{
	R = buffer_alloc (16);
	buffer_sets (R, "sysdate");
}
value(R)::=	INTERVAL value(A) SECOND. {
	R = interval (A, 24 * 60 * 60);
}
value(R)::=	INTERVAL value(A) MINUTE. {
	R = interval (A, 24 * 60);
}
value(R)::=	INTERVAL value(A) HOUR. {
	R = interval (A, 24);
}
value(R)::=	INTERVAL value(A) DAY. {
	R = A;
}
