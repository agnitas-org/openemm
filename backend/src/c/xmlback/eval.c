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
# include	<unistd.h>
# include	<ctype.h>
# include	<string.h>
# include	"xmlback.h"

# define	USE_SLANG

static void	*do_init (blockmail_t *);
static void	do_deinit (void *);
static bool_t	do_start_code (void *);
static bool_t	do_handle_code (void *, int, int, xmlBufferPtr);
static bool_t	do_end_code (void *);
static bool_t	do_start_vars (void *);
static bool_t	do_handle_variables (void *, field_t **, int, int *);
static bool_t	do_end_vars (void *);
static bool_t	do_setup (void *);
static bool_t	do_start_data (void *);
static bool_t	do_handle_data (void *, xmlBufferPtr *, bool_t *, int);
static bool_t	do_end_data (void *);
static bool_t	do_change_data (void *, xmlBufferPtr, bool_t, int);
static bool_t	do_start_eval (void *);
static bool_t	do_handle_eval (void *, int, int);
static bool_t	do_end_eval (void *);
static void	do_dump (void *, FILE *);

eval_t *
eval_alloc (blockmail_t *blockmail) /*{{{*/
{
	eval_t	*e;
	
	if (e = (eval_t *) malloc (sizeof (eval_t)))
		if (e -> e = do_init (blockmail)) {
			e -> blockmail = blockmail;
			e -> in_condition = false;
			e -> in_variables = false;
			e -> in_data = false;
			e -> in_match = false;
		} else {
			free (e);
			e = NULL;
		}
	return e;
}/*}}}*/
eval_t *
eval_free (eval_t *e) /*{{{*/
{
	if (e) {
		eval_done_match (e);
		eval_done_data (e);
		eval_done_variables (e);
		eval_done_condition (e);
		if (e -> e)
			do_deinit (e -> e);
		free (e);
	}
	return NULL;
}/*}}}*/
bool_t
eval_set_condition (eval_t *e, int sphere, int eid, xmlBufferPtr condition) /*{{{*/
{
	if (! e -> in_condition) {
		if (! do_start_code (e -> e))
			return false;
		e -> in_condition = true;
	}
	return do_handle_code (e -> e, sphere, eid, condition);
}/*}}}*/
bool_t
eval_done_condition (eval_t *e) /*{{{*/
{
	if (e -> in_condition) {
		if (! do_end_code (e -> e))
			return false;
		e -> in_condition = false;
	}
	return true;
}/*}}}*/
bool_t
eval_set_variables (eval_t *e, field_t **fld, int fld_cnt, int *failpos) /*{{{*/
{
	if (! e -> in_variables) {
		if (! do_start_vars (e -> e)) {
			if (failpos)
				*failpos = -1;
			return false;
		}
		e -> in_variables = true;
	}
	return do_handle_variables (e -> e, fld, fld_cnt, failpos);
}/*}}}*/
bool_t
eval_done_variables (eval_t *e) /*{{{*/
{
	if (e -> in_variables) {
		if (! do_end_vars (e -> e))
			return false;
		e -> in_variables = false;
	}
	return true;
}/*}}}*/
bool_t
eval_setup (eval_t *e) /*{{{*/
{
	return do_setup (e -> e);
}/*}}}*/
bool_t
eval_set_data (eval_t *e, xmlBufferPtr *data, bool_t *dnull, int data_cnt) /*{{{*/
{
	if (! e -> in_data) {
		if (! do_start_data (e -> e))
			return false;
		e -> in_data = true;
	}
	return do_handle_data (e -> e, data, dnull, data_cnt);
}/*}}}*/
bool_t
eval_change_data (eval_t *e, xmlBufferPtr data, bool_t dnull, int pos) /*{{{*/
{
	return do_change_data (e -> e, data, dnull, pos);
}/*}}}*/
bool_t
eval_done_data (eval_t *e) /*{{{*/
{
	if (e -> in_data) {
		if (! do_end_data (e -> e))
			return false;
		e -> in_data = false;
	}
	return true;
}/*}}}*/
bool_t
eval_match (eval_t *e, int sphere, int eid) /*{{{*/
{
	if (! e -> in_match) {
		if (! do_start_eval (e -> e))
			return false;
		e -> in_match = true;
	}
	return do_handle_eval (e -> e, sphere, eid);
}/*}}}*/
bool_t
eval_done_match (eval_t *e) /*{{{*/
{
	if (e -> in_match) {
		if (! do_end_eval (e -> e))
			return false;
		e -> in_match = false;
	}
	return true;
}/*}}}*/

void
eval_dump (eval_t *e, FILE *fp) /*{{{*/
{
	do_dump (e -> e, fp);
}/*}}}*/
# ifdef		USE_SLANG
# include	<pwd.h>
# include	<slang.h>
# include	"grammar/grammar.h"

# define	MY_DATE_TYPE		131

static buffer_t		*parse_error = NULL;
static blockmail_t	*ctx = NULL;
static inline void
ctx_set (blockmail_t *blockmail) /*{{{*/
{
	ctx = blockmail;
}/*}}}*/
static inline void
ctx_clr (void) /*{{{*/
{
	ctx = NULL;
}/*}}}*/
static inline void
check_error (void) /*{{{*/
{
	if (SLang_Error) {
		SLang_restart (1);
		SLang_Error = 0;
	}
}/*}}}*/
static void
record_error (char *msg) /*{{{*/
{
	if (parse_error) {
		buffer_appends (parse_error, msg);
		buffer_appendch (parse_error, '\n');
	}
}/*}}}*/

typedef struct { /*{{{*/
	int	type;
	union {
		double	n;
		char	*s;
	}	v;
	/*}}}*/
}	type_t;
static type_t *
type_release (int argc, type_t *argv) /*{{{*/
{
	int	n;

	for (n = 0; n < argc; ++n)
		switch (argv[n].type) {
		case SLANG_STRING_TYPE:
			if (argv[n].v.s)
				SLfree (argv[n].v.s);
			break;
		}
	return NULL;
}/*}}}*/
static type_t *
type_retrieve (int argc) /*{{{*/
{
	type_t	*argv;
	int	n;
	bool_t	st;
	int	dummy;

	if (! (argv = (type_t *) malloc (argc * sizeof (type_t)))) {
		SLang_Error = SL_MALLOC_ERROR;
		return NULL;
	}
	for (n = 0; n < argc; ++n)
		argv[n].type = SLANG_UNDEFINED_TYPE;
	st = true;
	for (n = argc - 1; st && (n >= 0); --n) {
		argv[n].type = SLang_peek_at_stack ();
		switch (argv[n].type) {
		default:
			st = false;
			break;
		case SLANG_INT_TYPE:
			if (SLang_pop_integer (& dummy) == -1)
				st = false;
			else
				argv[n].v.n = (double) dummy;
			break;
		case SLANG_DOUBLE_TYPE:
			if (SLang_pop_double (& argv[n].v.n, & dummy, & dummy) == -1)
				st = false;
			break;
		case SLANG_STRING_TYPE:
			if (SLpop_string (& argv[n].v.s))
				st = false;
			break;
		}
		if (! st)
			argv[n].type = SLANG_UNDEFINED_TYPE;
	}
	if (! st)
		argv = type_release (argc, argv);
	return argv;
}/*}}}*/
static inline bool_t
dcompare (double chk, double val) /*{{{*/
{
	return ((chk >= val - 0.005) && (chk <= val + 0.005)) ? true : false;
}/*}}}*/
typedef struct { /*{{{*/
	int	year, month, day;
	int	hour, min, sec;
	/*}}}*/
}	slang_date;
static inline void
norm (int *val, int low, int high, int *carry) /*{{{*/
{
	if (*val >= high) {
		*carry += *val / high;
		*val %= high;
	} else if (*val < low) {
		*carry += (*val - high + 1) / high;
		*val = high - *val % high;
	}
}/*}}}*/
static inline int
daypermonth (slang_date *sd) /*{{{*/
{
	switch (sd -> month) {
	default:	return -1;
	case 1:		return 31;
	case 2:
		if ((! (sd -> year % 4)) && ((sd -> year % 100) || (! (sd -> year % 400))))
			return 29;
		return 28;
	case 3:		return 31;
	case 4:		return 30;
	case 5:		return 31;
	case 6:		return 30;
	case 7:		return 31;
	case 8:		return 31;
	case 9:		return 30;
	case 10:	return 31;
	case 11:	return 30;
	case 12:	return 31;
	}
}/*}}}*/
static inline void
slang_date_norm (slang_date *sd) /*{{{*/
{
	int	dpm;
	
	norm (& sd -> sec, 0, 60, & sd -> min);
	norm (& sd -> min, 0, 60, & sd -> hour);
	norm (& sd -> hour, 0, 24, & sd -> day);
	sd -> month--;
	norm (& sd -> month, 0, 12, & sd -> year);
	sd -> month++;
	if (sd -> day > 0) {
		while ((dpm = daypermonth (sd)) < sd -> day) {
			sd -> day -= dpm;
			sd -> month++;
			if (sd -> month == 13) {
				sd -> month = 1;
				sd -> year++;
			}
		}
	} else {
		while (sd -> day <= 0) {
			sd -> month--;
			if (sd -> month == 0) {
				sd -> month = 12;
				sd -> year--;
			}
			sd -> day += daypermonth (sd);
		}
	}
}/*}}}*/
static inline int
undigit (xmlChar ch) /*{{{*/
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
	}
}/*}}}*/
static inline int
cut (const xmlChar *str, int start, int len) /*{{{*/
{
	int	n, v;
	
	for (n = 0, v = 0; n < len; ) {
		v *= 10;
		v += undigit (str[start + n]);
		n += xmlCharLength (str[start + n]);
	}
	return v;
}/*}}}*/
static inline bool_t
valid (const xmlChar *str, const char *pattern) /*{{{*/
{
	while (*pattern) {
		if (*pattern == '0') {
			if (! isdigit ((char) *str))
				break;
		} else {
			if ((char) *str != *pattern)
				break;
		}
		++str;
		++pattern;
	}
	return *pattern ? false : true;
}/*}}}*/
static void
slang_date_parse (slang_date *sd, const xmlChar *str, int len) /*{{{*/
{
	if ((len == 19) && valid (str, "0000-00-00 00:00:00")) {
		sd -> year = cut (str, 0, 4);
		sd -> month = cut (str, 5, 2);
		sd -> day = cut (str, 8, 2);
		sd -> hour = cut (str, 11, 2);
		sd -> min = cut (str, 14, 2);
		sd -> sec = cut (str, 17, 2);
	} else if ((len == 10) && valid (str, "0000-00-00")) {
		sd -> year = cut (str, 0, 4);
		sd -> month = cut (str, 5, 2);
		sd -> day = cut (str, 8, 2);
		sd -> hour = 0;
		sd -> min = 0;
		sd -> sec = 0;
	} else if ((len == 8) && valid (str, "00:00:00")) {
		sd -> year = 0;
		sd -> month = 0;
		sd -> day = 0;
		sd -> hour = cut (str, 0, 2);
		sd -> min = cut (str, 3, 2);
		sd -> sec = cut (str, 6, 2);
	}
}/*}}}*/

static char *
SLdate_string (unsigned char type, VOID_STAR ptr) /*{{{*/
{
	slang_date	*sd = *((slang_date **) ptr);
	char		buf[64];
	
	sprintf (buf, "%04d-%02d-%02d %02d:%02d:%02d",
		 sd -> year, sd -> month, sd -> day,
		 sd -> hour, sd -> min, sd -> sec);
	return SLmake_string (buf);
}/*}}}*/
static void
SLdate_destroy (unsigned char type, VOID_STAR ptr) /*{{{*/
{
	SLfree ((char *) (*((slang_date **) ptr)));
}/*}}}*/
static int
SLdate_push (unsigned char type, VOID_STAR ptr) /*{{{*/
{
	slang_date	**src;
	slang_date	*sd;
	
	src = (slang_date **) ptr;
	if (! *src)
		return SLang_push_null ();
	if (sd = (slang_date *) SLmalloc (sizeof (slang_date))) {
		*sd = **src;
		if (SLclass_push_ptr_obj (MY_DATE_TYPE, (VOID_STAR) sd) == -1) {
			SLfree ((char *) sd);
			return -1;
		}
		return 0;
	}
	return -1;
}/*}}}*/
static int
SLdate_bin_date (int op,
		 unsigned char a_type, VOID_STAR ap, unsigned int na,
		 unsigned char b_type, VOID_STAR bp, unsigned int nb,
		 VOID_STAR cp) /*{{{*/
{
	slang_date	*src;
	slang_date	*result;
	double		offset;
	int		doffset, soffset;

	src = *((slang_date **) ap);
	switch (b_type) {
	default:
		return 0;
	case SLANG_INT_TYPE:
		offset = (double) *((int *) bp);
		break;
	case SLANG_DOUBLE_TYPE:
		offset = *((double *) bp);
		break;
	}
	switch (op) {
	default:
		return 0;
	case SLANG_MINUS:
		offset = -offset;
		/* Fall through . . */
	case SLANG_PLUS:
		doffset = (int) offset;
		soffset = (int) ((offset - (double) doffset) * 86400);
		if (! (result = (slang_date *) SLmalloc (sizeof (slang_date))))
			return 0;
		result -> year = src -> year;
		result -> month = src -> month;
		result -> day = src -> day + doffset;
		result -> hour = src -> hour + (soffset / 3600);
		result -> min = src -> min + ((soffset / 60) % 60);
		result -> sec = src -> sec + (soffset % 60);
		break;
	}
	slang_date_norm (result);
	((slang_date **) cp)[0] = result;
	return 1;
}/*}}}*/
static int
SLdate_result (int op, unsigned char a, unsigned char b, unsigned char *c) /*{{{*/
{
	if (((b != SLANG_INT_TYPE) && (b != SLANG_DOUBLE_TYPE)) ||
	    ((op != SLANG_MINUS) && (op != SLANG_PLUS)))
		return 0;
	*c = MY_DATE_TYPE;
	return 1;
}/*}}}*/
static int
SLdate_setup (slang_date *sd) /*{{{*/
{
	SLang_Class_Type	*cl;
	time_t			now;
	struct tm		*tt;
	
	if (! (cl = SLclass_allocate_class ((char *) "Date_Type")))
		return -1;
	SLclass_set_string_function (cl, SLdate_string);
	SLclass_set_destroy_function (cl, SLdate_destroy);
	SLclass_set_push_function (cl, SLdate_push);
	if (SLclass_register_class (cl, MY_DATE_TYPE, sizeof (slang_date), SLANG_CLASS_TYPE_PTR) == -1)
		return -1;
	if ((SLclass_add_binary_op (MY_DATE_TYPE, SLANG_INT_TYPE, SLdate_bin_date, SLdate_result) == -1) ||
	    (SLclass_add_binary_op (MY_DATE_TYPE, SLANG_DOUBLE_TYPE, SLdate_bin_date, SLdate_result) == -1))
		return -1;
	time (& now);
	if (! (tt = localtime (& now)))
		return -1;
	sd -> year = tt -> tm_year + 1900;
	sd -> month = tt -> tm_mon + 1;
	sd -> day = tt -> tm_mday;
	sd -> hour = tt -> tm_hour;
	sd -> min = tt -> tm_min;
	sd -> sec = tt -> tm_sec;
	return 0;
}/*}}}*/

static void
SLDate (int *year, int *month, int *day, int *hour, int *min, int *sec) /*{{{*/
{
	slang_date	*sd;
	
	if (sd = (slang_date *) SLmalloc (sizeof (slang_date))) {
		sd -> year = *year;
		sd -> month = *month;
		sd -> day = *day;
		sd -> hour = *hour;
		sd -> min = *min;
		sd -> sec = *sec;
		if (SLclass_push_ptr_obj (MY_DATE_TYPE, (VOID_STAR) sd) == -1)
			SLfree ((char *) sd);
	}
}/*}}}*/
static int
fmt_month2 (slang_date *sd, char *buf, int len) /*{{{*/
{
	return snprintf (buf, len, "%02d", sd -> month);
}/*}}}*/
static int
fmt_day2 (slang_date *sd, char *buf, int len) /*{{{*/
{
	return snprintf (buf, len, "%02d", sd -> day);
}/*}}}*/
static int
fmt_year2 (slang_date *sd, char *buf, int len) /*{{{*/
{
	return snprintf (buf, len, "%02d", sd -> year % 100);
}/*}}}*/
static int
fmt_year4 (slang_date *sd, char *buf, int len) /*{{{*/
{
	return snprintf (buf, len, "%04d", sd -> year);
}/*}}}*/
static int
fmt_12hour2 (slang_date *sd, char *buf, int len) /*{{{*/
{
	int	hour = sd -> hour % 12;
	
	return snprintf (buf, len, "%02d", hour ? hour : 12);
}/*}}}*/
static int
fmt_24hour2 (slang_date *sd, char *buf, int len) /*{{{*/
{
	return snprintf (buf, len, "%02d", sd -> hour);
}/*}}}*/
static int
fmt_min2 (slang_date *sd, char *buf, int len) /*{{{*/
{
	return snprintf (buf, len, "%02d", sd -> min);
}/*}}}*/
static int
fmt_sec2 (slang_date *sd, char *buf, int len) /*{{{*/
{
	return snprintf (buf, len, "%02d", sd -> sec);
}/*}}}*/
static int
fmt_ampm2 (slang_date *sd, char *buf, int len) /*{{{*/
{
	strncpy (buf, (sd -> hour >= 12 ? "PM" : "AM"), 2);
	return 2;
}/*}}}*/
static int
fmt_ampm4 (slang_date *sd, char *buf, int len) /*{{{*/
{
	strncpy (buf, (sd -> hour >= 12 ? "P.M." : "A.M."), 4);
	return 4;
}/*}}}*/
typedef struct { /*{{{*/
	const char	*fmt;
	int		expect;
	int		(*func) (slang_date *, char *, int);
	/*}}}*/
}	fmt_t;
static fmt_t	ftab[5][24] = { /*{{{*/
	{
		{	"D",		1,	NULL		},
		{	"Y",		1,	NULL		},
		{	"I",		1,	NULL		},
		{	"Q",		1,	NULL		},
		{	"W",		1,	NULL		},
		{	"J",		-1,	NULL		},
		{	"E",		-1,	NULL		},
		{	NULL,		-1,	NULL		}
	}, {
		{	"MM",		2,	fmt_month2	},
		{	"RM",		-1,	NULL		},
		{	"DD",		2,	fmt_day2	},
		{	"DY",		3,	NULL		},
		{	"YY",		2,	fmt_year2	},
		{	"IY",		2,	NULL		},
		{	"RR",		2,	NULL		},
		{	"CC",		2,	NULL		},
		{	"WW",		2,	NULL		},
		{	"IW",		2,	NULL		},
		{	"HH",		2,	fmt_12hour2	},
		{	"MI",		2,	fmt_min2	},
		{	"SS",		2,	fmt_sec2	},
		{	"AM",		2,	fmt_ampm2	},
		{	"PM",		2,	fmt_ampm2	},
		{	"BC",		2,	NULL		},
		{	"AD",		2,	NULL		},
		{	NULL,		-1,	NULL		}
	}, {
		{	"MON",		3,	NULL		},
		{	"DDD",		3,	NULL		},
		{	"YYY",		3,	NULL		},
		{	"IYY",		3,	NULL		},
		{	"SCC",		-1,	NULL		},
		{	NULL,		-1,	NULL		}
	}, {
		{	"YYYY",		4,	fmt_year4	},
		{	"IYYY",		4,	NULL		},
		{	"RRRR",		4,	NULL		},
		{	"YEAR",		4,	NULL		},
		{	"HH12",		2,	fmt_12hour2	},
		{	"HH24",		2,	fmt_24hour2	},
		{	"A.M.",		4,	fmt_ampm4	},
		{	"P.M.",		4,	fmt_ampm4	},
		{	"B.C.",		4,	NULL		},
		{	"A.D.",		4,	NULL		},
		{	NULL,		-1,	NULL		}
	}, {
		{	"MONTH",	-1,	NULL		},
		{	"Y,YYY",	5,	NULL		},
		{	"SYYYY",	5,	NULL		},
		{	"SYEAR",	5,	NULL		},
		{	"SSSSS",	5,	NULL		},
		{	NULL,		-1,	NULL		}
	}
	/*}}}*/
};
static void
SLto_char (slang_date *sd, char *fmt) /*{{{*/
{
	char	*dest;
	int	dlen;

	dest = NULL;
	dlen = fmt ? strlen (fmt) + 64 : 0;
	if (fmt && (dest = SLmalloc (dlen + 1))) {
		int	n;
		bool_t	quote;
		
		for (n = 0, quote = false; *fmt && (n < dlen); )
			if (quote) {
				if (*fmt == '"')
					quote = false;
				else
					dest[n] = *fmt;
				++fmt;
			} else if (*fmt == '"') {
				quote = true;
				++fmt;
			} else {
				fmt_t	*use = NULL;
				int	l, f;
				
				for (l = 4; l >= 0; --l) {
					for (f = 0; ftab[l][f].fmt; ++f)
						if (! strncasecmp (ftab[l][f].fmt, fmt, l + 1)) {
							use = & ftab[l][f];
							break;
						}
					if (use)
						break;
				}
				if (use) {
					if (use -> func &&
					    (((use -> expect > 0) && (n + use -> expect < dlen)) || (use -> expect == -1)))
						n += (*use -> func) (sd, dest + n, dlen - n);
					fmt += l + 1;
				} else
					dest[n++] = *fmt++;
			}
		dest[n] = '\0';
		SLang_push_malloced_string (dest);
	} else
		SLang_push_string (NULL);
}/*}}}*/
static void
SLdate_format (slang_date *sd, char *fmt) /*{{{*/
{
	buffer_t	*scratch;
	
	if (fmt && (scratch = buffer_alloc (strlen (fmt) + 64))) {
		char	*ptr;
		bool_t	ispercent;
		
		ispercent = false;
		for (ptr = fmt; *ptr; ++ptr)
			if (ispercent) {
				switch (*ptr) {
				default:
				case '%':
					buffer_stiffch (scratch, *ptr);
					break;
				case 'c':
					buffer_format (scratch, "%d", sd -> month);
					break;
				case 'd':
					buffer_format (scratch, "%02d", sd -> day);
					break;
				case 'e':
					buffer_format (scratch, "%d", sd -> day);
					break;
				case 'H':
					buffer_format (scratch, "%02d", sd -> hour);
					break;
				case 'h':
				case 'I':
					if ((sd -> hour == 0) || (sd -> hour == 12))
						buffer_appends (scratch, "12");
					else
						buffer_format (scratch, "%02d", sd -> hour % 12);
					break;
				case 'i':
					buffer_format (scratch, "%02d", sd -> min);
					break;
				case 'k':
					buffer_format (scratch, "%d", sd -> hour);
					break;
				case 'l':
					if ((sd -> hour == 0) || (sd -> hour == 12))
						buffer_appends (scratch, "12");
					else
						buffer_format (scratch, "%d", sd -> hour);
					break;
				case 'm':
					buffer_format (scratch, "%02d", sd -> month);
					break;
				case 'p':
					buffer_appends (scratch, (sd -> hour >= 12 ? "PM" : "AM"));
					break;
				case 'r':
					buffer_format (scratch, "%02d:%02d:%02d %s",
						       ((sd -> hour == 0) || (sd -> hour == 12) ? 12 : (sd -> hour % 12)),
						       sd -> min, sd -> sec,
						       (sd -> hour >= 12 ? "PM" : "AM"));
					break;
				case 'S':
				case 's':
					buffer_format (scratch, "%02d", sd -> sec);
					break;
				case 'T':
					buffer_format (scratch, "%02d:%02d:%02d", sd -> hour, sd -> min, sd -> sec);
					break;
				case 'Y':
					buffer_format (scratch, "%04d", sd -> year);
					break;
				case 'y':
					buffer_format (scratch, "%02d", sd -> year % 100);
					break;
				}
				ispercent = false;
			} else if (*ptr == '%')
				ispercent = true;
			else
				buffer_stiffch (scratch, *ptr);
		SLang_push_string ((char *) buffer_string (scratch));
		buffer_free (scratch);
	} else
		SLang_push_string (NULL);
}/*}}}*/
static inline void
converter (char *str, const xchar_t *(*func) (xconv_t *, const xchar_t *, int, int *)) /*{{{*/
{
	const xchar_t	*rplc;
	int		olen;
	char		*dest;
		
	if (rplc = (*func) (ctx -> xconv, (const xchar_t *) str, strlen (str), & olen)) {
		if (dest = SLmalloc (olen + 1)) {
			if (olen > 0)
				memcpy (dest, rplc, olen);
			dest[olen] = '\0';
		}
	} else
		dest = NULL;
	SLang_push_malloced_string (dest);
}/*}}}*/
static void
SLlower (char *str) /*{{{*/
{
	converter (str, xconv_lower);
}/*}}}*/
static void
SLupper (char *str) /*{{{*/
{
	converter (str, xconv_upper);
}/*}}}*/
static void
SLcapitalize (char *str) /*{{{*/
{
	converter (str, xconv_title);
}/*}}}*/
static int
SLlength (char *str) /*{{{*/
{
	return xmlStrlen (char2xml (str));
}/*}}}*/
static int
SLlike (char *str, char *pattern, char *escape) /*{{{*/
{
	if (escape && (! *escape))
		escape = NULL;
	return xmlSQLlike (char2xml (pattern), strlen (pattern), char2xml (str), strlen (str), escape ? char2xml (escape) : NULL, escape ? strlen (escape) : 0);
}/*}}}*/
static int
SLmodulo (double *d1, double *d2) /*{{{*/
{
	int	i1 = (int) *d1,
		i2 = (int) *d2;
	if (i2)
		return i1 % i2;
	return 0;
}/*}}}*/
static void
SLsubstring (char *str, double *dpos, double *dlength) /*{{{*/
{
	int	pos = (int) *dpos,
		length = (int) *dlength;
	char	*rc;
	int	len;
	char	*copy;
	
	rc = NULL;
	len = 0;
	if (str) {
		int	slen = strlen (str);
		int	start;
		
		if (pos < 0) {
			if ((start = slen + pos) < 0)
				start = 0;
		} else if (pos > 0) {
			if (pos >= slen)
				start = slen - 1;
			else
				start = pos;
		} else
			start = 0;
		rc = str + start;
		len = length;
		if (len + start > slen)
			len = slen - start;
	}
	if (copy = SLmalloc (len + 1)) {
		if (len > 0)
			memcpy (copy, rc, len);
		copy[len] = '\0';
	}
	SLang_push_malloced_string (copy);
}/*}}}*/
static int
SLin (void) /*{{{*/
{
	int	rc;
	int	argc;
	int	type;
	int	n;
	
	argc = SLang_Num_Function_Args;
	if (argc < 2) {
		SLang_Error = SL_SYNTAX_ERROR;
		return -1;
	}
	rc = 0;
	type = SLang_peek_at_stack ();
	switch (type) {
	default:
		rc = -1;
		break;
	case SLANG_INT_TYPE:
		{
			int	*i;
			
			if (i = (int *) malloc (argc * sizeof (int))) {
				for (n = 0; n < argc; ++n)
					if (SLang_pop_integer (& i[n]) == -1) {
						rc = -1;
						break;
					}
				if (rc != -1)
					for (n = 0; n < argc - 1; ++n)
						if (i[argc - 1] == i[n]) {
							rc = 1;
							break;
						}
				free (i);
			} else {
				SLang_Error = SL_MALLOC_ERROR;
				rc = -1;
			}
		}
		break;
	case SLANG_DOUBLE_TYPE:
		{
			double	*d;
			int	dummy;
			
			if (d = (double *) malloc (argc * sizeof (double))) {
				for (n = 0; n < argc; ++n)
					if (SLang_pop_double (& d[n], & dummy, & dummy) == -1) {
						rc = -1;
						break;
					}
				if (rc != -1)
					for (n = 0; n < argc - 1; ++n)
						if (dcompare (d[argc - 1], d[n])) {
							rc = 1;
							break;
						}
				free (d);
			} else {
				SLang_Error = SL_MALLOC_ERROR;
				rc = -1;
			}
		}
		break;
	case SLANG_STRING_TYPE:
		{
			char	**s;
			
			if (s = (char **) malloc (argc * sizeof (char *))) {
				for (n = 0; n < argc; ++n)
					if (SLpop_string (& s[n]) == -1) {
						while (--n >= 0)
							SLfree (s[n]);
						rc = -1;
						break;
					}
				if (rc != -1) {
					for (n = 0; n < argc - 1; ++n)
						if (! strcmp (s[argc - 1], s[n])) {
							rc = 1;
							break;
						}
					for (n = 0; n < argc; ++n)
						SLfree (s[n]);
				}
				free (s);
			} else {
				SLang_Error = SL_MALLOC_ERROR;
				rc = -1;
			}
		}
		break;
	}
	if ((rc == -1) && (! SLang_Error))
		SLang_Error = SL_TYPE_MISMATCH;
	return rc;
}/*}}}*/
static int
SLbetween (void) /*{{{*/
{
	int	argc;
	type_t	*argv;
	int	rc;
	int	n;
	
	argc = SLang_Num_Function_Args;
	if (argc != 3) {
		SLang_Error = SL_SYNTAX_ERROR;
		return -1;
	}
	if (! (argv = type_retrieve (argc)))
		return -1;
	rc = 0;
	for (n = 1; n < 3; ++n)
		if (argv[0].type != argv[n].type) {
			rc = -1;
			break;
		}
	if (rc != -1)
		switch (argv[0].type) {
		default:
			rc = -1;
			break;
		case SLANG_INT_TYPE:
			if (((long) argv[0].v.n >= (long) argv[1].v.n) && ((long) argv[0].v.n <= (long) argv[2].v.n))
				rc = 1;
			break;
		case SLANG_DOUBLE_TYPE:
			if ((argv[0].v.n >= argv[1].v.n - 0.005) && (argv[0].v.n <= argv[2].v.n + 0.005))
				rc = 1;
			break;
		case SLANG_STRING_TYPE:
			if ((strcmp (argv[0].v.s, argv[1].v.s) >= 0) && (strcmp (argv[0].v.s, argv[2].v.s) <= 0))
				rc = 1;
			break;
		}
	type_release (argc, argv);
	if ((rc == -1) && (! SLang_Error))
		SLang_Error = SL_TYPE_MISMATCH;
	return rc;
}/*}}}*/
static void
SLdecode (void) /*{{{*/
{
	int	n;
	bool_t	st;
	int	argc;
	type_t	*argv;
	
	argc = SLang_Num_Function_Args;
	if ((argc < 4) || (argc % 2 != 0)) {
		SLang_Error = SL_SYNTAX_ERROR;
		return;
	}
	if (! (argv = type_retrieve (argc)))
		return;
	/* 1.) test if values */
	st = true;
	for (n = 1; n < argc - 1; n += 2)
		if (argv[n].type != argv[0].type) {
			st = false;
			break;
		}
	/* 2.) test then values */
	if (st)
		for (n = 4; n < argc; n += 2)
			if (argv[n].type != argv[2].type) { 
				st = false;
				break;
			}
	/* 3.) test else value */
	if (st)
		if (argv[argc - 1].type != argv[2].type)
			st = false;
	if (! st)
		SLang_Error = SL_TYPE_MISMATCH;
	else {
		int	hit = -1;
		
		for (n = 1; (n < argc - 1) && (hit == -1); n += 2)
			switch (argv[0].type) {
			case SLANG_INT_TYPE:
				if ((long) argv[0].v.n == (long) argv[n].v.n)
					hit = n + 1;
				break;
			case SLANG_DOUBLE_TYPE:
				if (dcompare (argv[0].v.n, argv[n].v.n))
					hit = n + 1;
				break;
			case SLANG_STRING_TYPE:
				if (! strcmp (argv[0].v.s, argv[n].v.s))
					hit = n + 1;
				break;
			}
		if (hit == -1)
			hit = argc - 1;
		switch (argv[hit].type) {
		default:
			st = false;
			SLang_Error = SL_TYPE_MISMATCH;
			break;
		case SLANG_INT_TYPE:
			SLang_push_integer ((int) argv[hit].v.n);
			break;
		case SLANG_DOUBLE_TYPE:
			SLang_push_double (argv[hit].v.n);
			break;
		case SLANG_STRING_TYPE:
			SLang_push_malloced_string (argv[hit].v.s);
			argv[hit].v.s = NULL;
			break;
		}
	}
	type_release (argc, argv);
}/*}}}*/
static void
SLprint (char *str) /*{{{*/
{
	fputs (str, stdout);
	fflush (stdout);
}/*}}}*/
SLang_Intrin_Fun_Type	functab[] = { /*{{{*/
	MAKE_INTRINSIC_6 ((char *) "Date", SLDate, SLANG_VOID_TYPE,
			  SLANG_INT_TYPE, SLANG_INT_TYPE, SLANG_INT_TYPE,
			  SLANG_INT_TYPE, SLANG_INT_TYPE, SLANG_INT_TYPE),
	MAKE_INTRINSIC_2 ((char *) "to_char", SLto_char, SLANG_VOID_TYPE,
			  MY_DATE_TYPE, SLANG_STRING_TYPE),
	MAKE_INTRINSIC_2 ((char *) "date_format", SLdate_format, SLANG_VOID_TYPE,
			  MY_DATE_TYPE, SLANG_STRING_TYPE),
	MAKE_INTRINSIC_S ((char *) "lower", SLlower, SLANG_VOID_TYPE),
	MAKE_INTRINSIC_S ((char *) "upper", SLupper, SLANG_VOID_TYPE),
	MAKE_INTRINSIC_S ((char *) "captialize", SLcapitalize, SLANG_VOID_TYPE),
	MAKE_INTRINSIC_S ((char *) "length", SLlength, SLANG_INT_TYPE),
	MAKE_INTRINSIC_SSS ((char *) "like", SLlike, SLANG_INT_TYPE),
	MAKE_INTRINSIC_3 ((char *) "substring", SLsubstring, SLANG_STRING_TYPE,
			   SLANG_STRING_TYPE, SLANG_DOUBLE_TYPE, SLANG_DOUBLE_TYPE),
	MAKE_INTRINSIC_2 ((char *) "modulo", SLmodulo, SLANG_INT_TYPE,
			 SLANG_DOUBLE_TYPE, SLANG_DOUBLE_TYPE),
	MAKE_INTRINSIC_0 ((char *) "between", SLbetween, SLANG_INT_TYPE),
	MAKE_INTRINSIC_0 ((char *) "in", SLin, SLANG_INT_TYPE),
	MAKE_INTRINSIC_0 ((char *) "decode", SLdecode, SLANG_VOID_TYPE),
	
	MAKE_INTRINSIC_S ((char *) "print", SLprint, SLANG_VOID_TYPE),
	SLANG_END_TABLE
	/*}}}*/
};
	
typedef struct code { /*{{{*/
	int		sphere;
	int		eid;
	char		fname[36];
	buffer_t	*code;
	SLang_Name_Type	*func;
	struct code	*next;
	/*}}}*/
}	code_t;
static code_t *
code_free (code_t *c) /*{{{*/
{
	if (c) {
		if (c -> code)
			buffer_free (c -> code);
		free (c);
	}
	return NULL;
}/*}}}*/
static code_t *
code_free_all (code_t *c) /*{{{*/
{
	code_t	*tmp;
	
	while (tmp = c) {
		c = c -> next;
		code_free (tmp);
	}
	return NULL;
}/*}}}*/
static code_t *
code_alloc (int sphere, int eid, const xmlBufferPtr desc) /*{{{*/
{
	code_t	*c;
	
	if (c = (code_t *) malloc (sizeof (code_t)))
		if (c -> code = buffer_alloc (xmlBufferLength (desc) + 256)) {
			bool_t	ok;
			
			c -> sphere = sphere;
			c -> eid = eid;
			sprintf (c -> fname, "F%d_%d", c -> sphere, c -> eid);
			c -> func = NULL;
			c -> next = NULL;
			ok = true;
			buffer_format (c -> code, "define %s () {\n\t return (", c -> fname);
			ok = transform (c -> code, xmlBufferContent (desc), xmlBufferLength (desc), parse_error, ctx -> xconv);
			buffer_appends (c -> code, ");\n}\n");
			if (! ok)
				c = code_free (c);
		} else {
			free (c);
			c = NULL;
		}
	return c;
}/*}}}*/
static bool_t
code_compile (code_t *c) /*{{{*/
{
	bool_t	ok;
	
	ok = false;
	if (SLang_load_string ((char *) buffer_string (c -> code)) != -1) {
		c -> func = SLang_get_function (c -> fname);
		if (c -> func)
			ok = true;
	}
	if (! ok)
		check_error ();
	return ok;
}/*}}}*/
static bool_t
code_execute (code_t *c) /*{{{*/
{
	bool_t	rc;
	int	val;
	
	rc = false;
	if ((SLexecute_function (c -> func) == -1) ||
	    (SLang_pop_integer (& val) == -1)) {
		check_error ();
	} else if (val)
		rc = true;
	return rc;
}/*}}}*/

typedef struct { /*{{{*/
	char	typ;
	int	isnull;
	union {
		double		n;
		char		*s;
		slang_date	*d;
	}	v;
	/*}}}*/
}	val_t;
static void
val_zero (val_t *v) /*{{{*/
{
	memset (v, 0, sizeof (*v));
}/*}}}*/
static void
val_clear (val_t *v) /*{{{*/
{
	v -> isnull = 0;
	switch (v -> typ) {
	case 'n':
		v -> v.n = 0.0;
		break;
	case 's':
		if (v -> v.s) {
			free (v -> v.s);
			v -> v.s = NULL;
		}
		break;
	case 'd':
		if (v -> v.d) {
			free (v -> v.d);
			v -> v.d = NULL;
		}
		break;
	}
}/*}}}*/
static void
val_set (val_t *v, const xmlBufferPtr val, bool_t isnull) /*{{{*/
{
	int		len = xmlBufferLength (val);
	const xmlChar	*cont = xmlBufferContent (val);

	v -> isnull = isnull ? 1 : 0;
	switch (v -> typ) {
	case 'n':
		v -> v.n = atof ((const char *) cont);
		break;
	case 's':
		if (v -> v.s)
			v -> v.s = realloc (v -> v.s, len + 1);
		else
			v -> v.s = malloc (len + 1);
		if (v -> v.s) {
			memcpy (v -> v.s, cont, len);
			v -> v.s[len] = '\0';
		}
		break;
	case 'd':
		if (! v -> v.d)
			v -> v.d = malloc (sizeof (slang_date));
		if (v -> v.d)
			slang_date_parse (v -> v.d, cont, len);
		break;
	}
}/*}}}*/
typedef struct { /*{{{*/
	blockmail_t	*blockmail;
	SLang_NameSpace_Type
			*ns;
	slang_date	*sysdate;
	code_t		*code;
	val_t		*val;
	int		vcnt;
	/*}}}*/
}	slang_t;

static char *
mkrcfile (const char *fname) /*{{{*/
{
	char		*path;
	const char	*dir;
	struct passwd	*pw;

	dir = ".";
	setpwent ();
	if ((pw = getpwuid (geteuid ())) && pw -> pw_dir)
		dir = pw -> pw_dir;
	endpwent ();
	if (path = malloc (strlen (fname) + strlen (dir) + 2))
		sprintf (path, "%s/%s", dir, fname);
	return path;
}/*}}}*/
static void *
do_init (blockmail_t *blockmail) /*{{{*/
{
	slang_t		*s;
	slang_date	sd;
	
	s = NULL;
	if (! parse_error) {
		parse_error = buffer_alloc (512);
	} else {
		parse_error -> length = 0;
	}
	ctx_set (blockmail);
	SLang_Error_Hook = record_error;
	if ((SLang_init_slang () != -1) &&
	    (SLang_init_slmath () != -1) &&
	    (SLang_init_slassoc () != -1) &&
	    (SLang_init_array () != -1) &&
	    (SLdate_setup (& sd) != -1) &&
	    (SLadd_intrin_fun_table (functab, (char *) "__XMLBACK__") != -1) &&
	    (s = (slang_t *) malloc (sizeof (slang_t)))) {
		char	*rcfile;

		s -> blockmail = blockmail;
		s -> ns = SLns_create_namespace ((char *) "custom");
		if (s -> sysdate = malloc (sizeof (slang_date)))
			*(s -> sysdate) = sd;
		s -> code = NULL;
		s -> val = NULL;
		s -> vcnt = 0;
		SLadd_intrinsic_variable ((char *) "sysdate", & s -> sysdate, MY_DATE_TYPE, 1);
		if (rcfile = mkrcfile (".xmlbackrc.sl")) {
			if (access (rcfile, R_OK) != -1)
				if (SLang_load_file (rcfile) == -1)
					check_error ();
			free (rcfile);
		}
	}
	ctx_clr ();
	return s;
}/*}}}*/
static void
do_deinit (void *sp) /*{{{*/
{
	slang_t	*s = (slang_t *) sp;
	
	if (s) {
		if (s -> sysdate)
			free (s -> sysdate);
		code_free_all (s -> code);
		if (s -> val) {
			int	n;
			
			for (n = 0; n < s -> vcnt; ++n)
				val_clear (& s -> val[n]);
			free (s -> val);
		}
		if (s -> ns)
			SLns_delete_namespace (s -> ns);
		free (s);
	}
}/*}}}*/
static bool_t
do_start_code (void *sp) /*{{{*/
{
	return true;
}/*}}}*/
static bool_t	
do_handle_code (void *sp, int sphere, int eid, xmlBufferPtr desc) /*{{{*/
{
	slang_t	*s = (slang_t *) sp;
	code_t	*code;

	if (parse_error)
		parse_error -> length = 0;
	ctx_set (s -> blockmail);
	code = code_alloc (sphere, eid, desc);
	if (parse_error && (parse_error -> length > 0))
		log_out (s -> blockmail -> lg, LV_ERROR, "Error in parsing:\n%s\nError description:\n%s", xmlBufferContent (desc), buffer_string (parse_error));
	else if (code && code -> code)
		log_out (s -> blockmail -> lg, LV_VERBOSE, "Code for ID %d:\n%s\nCompiles to:\n%s", eid, xmlBufferContent (desc), buffer_string (code -> code));
	if (code) {
		code_t	*run, *prv;
		
		for (run = s -> code, prv = NULL; run; run = run -> next)
			if ((run -> sphere > code -> sphere) || ((run -> sphere == code -> sphere) && (run -> eid > code -> eid)))
				break;
			else
				prv = run;
		if (prv) {
			code -> next = prv -> next;
			prv -> next = code;
		} else {
			code -> next = s -> code;
			s -> code = code;
		}
	}
	ctx_clr ();
	return code ? true : false;
}/*}}}*/
static bool_t
do_end_code (void *sp) /*{{{*/
{
	return true;
}/*}}}*/
static bool_t
do_start_vars (void *sp) /*{{{*/
{
	return true;
}/*}}}*/
static bool_t
do_handle_variables (void *sp, field_t **fld, int fld_cnt, int *failpos) /*{{{*/
{
	slang_t	*s = (slang_t *) sp;
	int	n;

	n = 0;
	if (s -> val = (val_t *) malloc (fld_cnt * sizeof (val_t))) {
		unsigned char	typ;
		char		*temp;
		int		tlen;
		
		for (n = 0; n < fld_cnt; ++n)
			val_zero (& s -> val[n]);
		for (n = 0; n < fld_cnt; ++n) {
			switch (fld[n] -> type) {
			default:	typ = SLANG_UNDEFINED_TYPE;	break;
			case 'n':	typ = SLANG_DOUBLE_TYPE;	break;
			case 's':	typ = SLANG_STRING_TYPE;	break;
			case 'd':	typ = MY_DATE_TYPE;		break;
			}
			tlen = strlen (fld[n] -> name) + 10;
			if (fld[n] -> ref)
				tlen += strlen (fld[n] -> ref) + 2;
			if (temp = malloc (tlen)) {
				char	*p1, *p2;
				
				strcpy (temp, "NULL$VAR$");
				p1 = temp + 9;
				if (fld[n] -> ref) {
					for (p2 = fld[n] -> ref; *p2; )
						*p1++ = toupper (*p2++);
					*p1++ = '$';
					*p1++ = '$';
				}
				for (p2 = fld[n] -> name; *p2; )
					*p1++ = toupper (*p2++);
				*p1 = '\0';
				if ((typ == SLANG_UNDEFINED_TYPE) ||
				    (SLadd_intrinsic_variable (temp + 5, & s -> val[n].v, typ, 1) == -1)) {
					check_error ();
					break;
				}
				if (SLadd_intrinsic_variable (temp, & s -> val[n].isnull, SLANG_INT_TYPE, 1) == -1) {
					check_error ();
					break;
				}
				free (temp);
			} else
				break;
			s -> val[n].typ = fld[n] -> type;
		}
		s -> vcnt = n;
	}
	if ((n < fld_cnt) && failpos)
		*failpos = n;
	return n < fld_cnt ? false : true;
}/*}}}*/
static bool_t
do_end_vars (void *sp) /*{{{*/
{
	return true;
}/*}}}*/
static bool_t
do_setup (void *sp) /*{{{*/
{
	slang_t	*s = (slang_t *) sp;
	code_t	*c;
	bool_t	st, rc;
	
	rc = true;
	ctx_set (s -> blockmail);
	for (c = s -> code; c; c = c -> next) {
		if (parse_error)
			parse_error -> length = 0;
		st = code_compile (c);
		if (parse_error && (parse_error -> length > 0))
			log_out (s -> blockmail -> lg, (st ? LV_WARNING : LV_ERROR), "SPH/EID %d/%d: SLang compile of code:\n%s\nresults to these error(s):\n%s", c -> sphere, c -> eid, buffer_string (c -> code), buffer_string (parse_error));
		if (! st)
			rc = false;
	}
	ctx_clr ();
	return rc;
}/*}}}*/
static bool_t
do_start_data (void *sp) /*{{{*/
{
	return true;
}/*}}}*/
static bool_t
do_handle_data (void *sp, xmlBufferPtr *data, bool_t *dnull, int data_cnt) /*{{{*/
{
	slang_t	*s = (slang_t *) sp;
	int	n;
	
	for (n = 0; n < s -> vcnt; ++n)
		if (n < data_cnt)
			val_set (& s -> val[n], data[n], dnull[n]);
		else
			val_clear (& s -> val[n]);
	return true;
}/*}}}*/
static bool_t
do_end_data (void *sp) /*{{{*/
{
	return true;
}/*}}}*/
static bool_t
do_change_data (void *sp, xmlBufferPtr data, bool_t dnull, int pos) /*{{{*/
{
	slang_t	*s = (slang_t *) sp;
	
	if (pos < s -> vcnt)
		val_set (& s -> val[pos], data, dnull);
	return true;
}/*}}}*/
static bool_t
do_start_eval (void *sp) /*{{{*/
{
	return true;
}/*}}}*/
static bool_t
do_handle_eval (void *sp, int sphere, int eid) /*{{{*/
{
	slang_t	*s = (slang_t *) sp;
	bool_t	rc;

	ctx_set (s -> blockmail);
	if (s -> code) {
		code_t	*run;
		
		rc = false;
		for (run = s -> code; run; run = run -> next)
			if ((run -> sphere == sphere) && (run -> eid == eid))
				break;
		if (run) {
			if (parse_error)
				parse_error -> length = 0;
			rc = code_execute (run);
			if (parse_error && (parse_error -> length > 0))
				log_out (s -> blockmail -> lg, (rc ? LV_WARNING : LV_ERROR), "SPH/EID %d/%d: SLang execution of code:\n%s\nresult to these error(s):\n%s", run -> sphere, run -> eid, buffer_string (run -> code), buffer_string (parse_error));
		}
	} else
		rc = true;
	ctx_clr ();
	return rc;
}/*}}}*/
static bool_t
do_end_eval (void *sp) /*{{{*/
{
	return true;
}/*}}}*/
static void
do_dump (void *sp, FILE *fp) /*{{{*/
{
}/*}}}*/
# endif		/* USE_SLANG */
