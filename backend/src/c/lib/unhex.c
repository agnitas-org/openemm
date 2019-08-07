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
# include	<string.h>
# include	"agn.h"

static inline unsigned long
unhexch (char ch) /*{{{*/
{
	switch (ch) {
	default:
	case '0':	return 0;
	case '1':	return 1;
	case '2':	return 2;
	case '3':	return 3;
	case '4':	return 4;
	case '5':	return 5;
	case '6':	return 6;
	case '7':	return 7;
	case '8':	return 8;
	case '9':	return 9;
	case 'a':
	case 'A':	return 10;
	case 'b':
	case 'B':	return 11;
	case 'c':
	case 'C':	return 12;
	case 'd':
	case 'D':	return 13;
	case 'e':
	case 'E':	return 14;
	case 'f':
	case 'F':	return 15;
	}
}/*}}}*/
unsigned long
unhexn (const char *str, int len) /*{{{*/
{
	unsigned long	r = 0;
	int		n;
	
	for (n = 0; n < len; ++n) {
		r <<= 4;
		r |= unhexch (str[n]);
	}
	return r;
}/*}}}*/
unsigned long
unhex (const char *str) /*{{{*/
{
	return unhexn (str, strlen (str));
}/*}}}*/
