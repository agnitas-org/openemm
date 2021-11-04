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
# include	<stdio.h>
# include	<unistd.h>
# include	<fcntl.h>
# include	<sys/types.h>
# include	<sys/stat.h>
# include	<sys/time.h>
# include	<regex.h>
# include	"tflua.c"

# define	F_MAIN		"main"

typedef struct codeblock	codeblock_t;
struct codeblock { /*{{{*/
	char		*condition;
	char		*code;
	int		length;
	codeblock_t	*next;
	/*}}}*/
};
static codeblock_t *
codeblock_free (codeblock_t *cb) /*{{{*/
{
	if (cb) {
		if (cb -> condition)
			free (cb -> condition);
		if (cb -> code)
			free (cb -> code);
		free (cb);
	}
	return NULL;
}/*}}}*/
static codeblock_t *
codeblock_free_all (codeblock_t *cb) /*{{{*/
{
	codeblock_t	*tmp;
	
	while (tmp = cb) {
		cb = cb -> next;
		codeblock_free (tmp);
	}
	return NULL;
}/*}}}*/
static codeblock_t *
codeblock_alloc (char *condition, const char *code, int length) /*{{{*/
{
	codeblock_t	*cb;
	
	if (cb = (codeblock_t *) malloc (sizeof (codeblock_t))) {
		cb -> condition = condition;
		cb -> next = NULL;
		if (cb -> code = malloc (length + 1)) {
			if (length > 0)
				memcpy (cb -> code, code, length);
			cb -> code[length] = '\0';
			cb -> length = length;
		} else
			cb = codeblock_free (cb);
	}
	return cb;
}/*}}}*/
static codeblock_t *
split_code (const char *buffer) /*{{{*/
{
	codeblock_t	*root, *previous, *current;
	regex_t	re;

	root = previous = NULL;
	if (regcomp (& re, "^.*#<(.*)>#$", REG_EXTENDED | REG_NEWLINE) == 0) {
		const char	*ptr;
		regmatch_t	match[2];
		char		*condition;
		
		condition = NULL;
		for (ptr = buffer; ptr; ) {
			if (regexec (& re, ptr, 2, match, 0) == 0) {
				current = codeblock_alloc (condition, ptr, match[0].rm_so);
				if (condition = malloc (match[1].rm_eo - match[1].rm_so + 1)) {
					int	state;
					int	pos;
					char	*target;
					
					for (state = 0, pos = match[1].rm_so, target = condition; pos < match[1].rm_eo; ++pos) {
						if (state == 0) {
							if (isspace (ptr[pos]))
								continue;
							state = 1;
						}
						if (state == 1) {
							*target++ = ptr[pos];
						}
					}
					while ((target > condition) && isspace (*(target - 1)))
						--target;
					*target = '\0';
				}
				ptr += match[0].rm_eo;
				if (*ptr == '\n')
					++ptr;
			} else {
				current = codeblock_alloc (condition, ptr, strlen (ptr));
				ptr = NULL;
			}
			if (! current) {
				fprintf (stderr, "Failed to allocated code block: %m\n");
				exit (1);
			}
			if (previous)
				previous -> next = current;
			else
				root = current;
			previous = current;
		}
	}
	return root;
}/*}}}*/
	
static char *
read_file (const char *fname, size_t *length) /*{{{*/
{
	char	*buf;
	int	fd;
		
	buf = NULL;
	if ((fd = open (fname, O_RDONLY)) != -1) {
		struct stat	st;

		if ((fstat (fd, & st) != -1) && (buf = malloc (st.st_size + 1))) {
			if (read (fd, buf, st.st_size) == st.st_size) {
				buf[st.st_size] = '\0';
				*length = st.st_size;
			} else {
				fprintf (stderr, "Failed to read while file %s: %m\n", fname);
				free (buf);
				buf = NULL;
			}
		} else
			fprintf (stderr, "Failed to setup reading for %s: %m\n", fname);
		close (fd);
	} else
		fprintf (stderr, "Failed to open %s: %m\n", fname);
	return buf;
}/*}}}*/
static int
l_fileread (lua_State *lua) /*{{{*/
{
	const char	*fname;
	
	if (fname = lua_tostring (lua, -1)) {
		char	*buf;
		size_t	length;
		
		if (buf = read_file (fname, & length)) {
			lua_pushlstring (lua, buf, length);
			free (buf);
		} else
			lua_pushnil (lua);
	} else
		lua_pushnil (lua);
	return 1;
}/*}}}*/
static int
l_silent (lua_State *lua) /*{{{*/
{
	return 0;
}/*}}}*/
static int
sorter (const void *a, const void *b) /*{{{*/
{
	return strcmp (*((const char **) a), *((const char **) b));
}/*}}}*/
static bool_t
do_unittest (iflua_t *il, bool_t quiet, int benchmark) /*{{{*/
{
	bool_t		rc;
	const char	*funcname;
	char		**flist;
	int		count, size;
	int		n;
	const char	*txt;
			
	rc = true;
	flist = NULL;
	count = 0;
	size = 0;
# if	LUA_VERSION_NUM >= 502
	lua_pushglobaltable (il -> lua);
# else	
	lua_pushvalue (il -> lua, LUA_GLOBALSINDEX);
# endif	
	lua_pushnil (il -> lua);
	while (lua_next (il -> lua, -2)) {
		if ((lua_type (il -> lua, -2) == LUA_TSTRING) &&
		    (lua_type (il -> lua, -1) == LUA_TFUNCTION) &&
		    (funcname = lua_tostring (il -> lua, -2)) &&
		    (! strncmp (funcname, "test", 4) && funcname[4])) {
			if (count >= size) {
				char	**nlist;
				int	nsize;
										
				nsize = size ? size * 2 : 16;
				if (nlist = (char **) realloc (flist, nsize * sizeof (char *))) {
					flist = nlist;
					size = nsize;
				} else {
					fprintf (stderr, "FATAL: Failed to alloc for function \"%s\".\n", funcname);
					rc = false;
				}
			}
			if (count < size) {
				if (flist[count] = strdup (funcname))
					++count;
				else {
					fprintf (stderr, "FATAL: Failed to store function name \"%s\".\n", funcname);
					rc = false;
				}
			}
		}
		lua_pop (il -> lua, 1);
	}
	lua_pop (il -> lua, 1);
	if (rc) {
		if (count == 0) {
			if (! quiet)
				printf ("unittest: found no test functions in block.\n");
		} else {
			if (count > 1)
				qsort (flist, count, sizeof (flist[0]), sorter);
			for (n = 0; n < count; ++n) {
				struct timeval	start, end;
				int		round;
				int		marker;
				
				if ((marker = benchmark / 20) < 1)
					marker = 1;
				if (! quiet) {
					printf ("unittest: %s: ", flist[n] + 4);
					fflush (stdout);
				}
				if (benchmark)
					gettimeofday (& start, NULL);
				for (round = 0; round <= benchmark; ++round) {
					lua_settop (il -> lua, 0);
					lua_getglobal (il -> lua, flist[n]);
					if (lua_pcall (il -> lua, 0, LUA_MULTRET, 0) == 0) {
						if (! quiet)
							if (benchmark == 0) {
								if ((lua_gettop (il -> lua) == 1) && (txt = lua_tostring (il -> lua, -1)))
									printf ("ok: %s.\n", txt);
								else
									printf ("ok.\n");
							} else {
								if (round % marker == 0) {
									putc ('.', stdout);
									fflush (stdout);
								}
							}
					} else {
						rc = false;
						if (! quiet)
							printf ("fail.\n");
						else
							printf ("unittest: %s: fail.\n", flist[n] + 4);
						if ((lua_gettop (il -> lua) > 0) && (txt = lua_tostring (il -> lua, -1)))
							printf ("  ** %s\n", txt);
						break;
					}
				}
				if (benchmark) {
					gettimeofday (& end, NULL);
					if (round > benchmark) {
						long	diff = (end.tv_sec * 1000000 + end.tv_usec) - (start.tv_sec * 1000000 + start.tv_usec);
						
						printf (" %ld.%03ld seconds for %d rounds.\n", diff / 1000000, (diff % 1000000) / 1000, benchmark);
					} else
						printf ("Failed to benchmark due to failure during execution.\n");
				}
			}
		}
	}
	return rc;
}/*}}}*/
static bool_t
validate (const char *fname, codeblock_t *cb, bool_t quiet, bool_t postproc, bool_t unittest, int benchmark) /*{{{*/
{
	bool_t		rc;
	log_t		*lg;
	blockmail_t	*blockmail;
	codeblock_t	*run;
	receiver_t	*r;

	rc = false;
	if (lg = log_alloc (NULL, fname, NULL)) {
		lg -> level = LV_DEBUG;
		log_tofd (lg, STDERR_FILENO);
		log_suspend_push (lg, LS_LOGFILE | LS_SYSLOG, false);
		srandom (time (NULL));
		if (blockmail = blockmail_alloc (NULL, false, lg)) {
			var_t	*cur, *prv;
			struct {
				const char	*var;
				const char	*val;
			}	info[] = {
				{	"_rdir_domain",			"http://rdir.de"		},
				{	"_envelope_from",		"aml_4711@filter.agnitas.de"	},
				{	"_mailloop_domain",		"filter.agnitas.de"		},
				{	"use-extended-usertypes",	"true"				},
				{	"url-default",			"http://rdir.de"		}
			};
			int	n;
			
			blockmail -> licence_id = 1;
			blockmail -> owner_id = 1;
			blockmail -> company_id = 2;
			blockmail -> mailinglist_id = 3;
			blockmail -> mailinglist_name = xmlBufferCreate ();
			xmlBufferCat (blockmail -> mailinglist_name, (const xmlChar *) "Mailinglist");
			blockmail -> mailing_id = 4;
			blockmail -> mailing_name = xmlBufferCreate ();
			xmlBufferCat (blockmail -> mailing_name, (const xmlChar *) "Mailing");
			blockmail -> maildrop_status_id = 5;
			blockmail -> status_field = 'W';
			blockmail -> senddate = tf_parse_date ("2010-03-20 12:34:56");
			blockmail -> total_subscribers = 6;
			blockmail -> blocknr = 7;
			blockmail -> secret_key = xmlBufferCreate ();
			xmlBufferCCat (blockmail -> secret_key, "This is secret");
			blockmail -> secret_timestamp = 8;
			string_map_addsi (blockmail -> smap, "licence_id", blockmail -> licence_id);
			string_map_addsi (blockmail -> smap, "owner_id", blockmail -> owner_id);
			string_map_addsi (blockmail -> smap, "company_id", blockmail -> company_id);
			string_map_addsi (blockmail -> smap, "mailinglist_id", blockmail -> mailinglist_id);
			string_map_addsb (blockmail -> smap, "mailinglist_name", blockmail -> mailinglist_name);
			string_map_addsi (blockmail -> smap, "mailing_id", blockmail -> mailing_id);
			string_map_addsb (blockmail -> smap, "mailing_name", blockmail -> mailing_name);
			string_map_addsi (blockmail -> smap, "total_subscribers", blockmail -> total_subscribers);
			for (n = 0, prv = NULL; n < sizeof (info) / sizeof (info[0]); ++n) {
				cur = var_alloc (info[n].var, info[n].val);
				if (prv)
					prv -> next = cur;
				else
					blockmail -> company_info = cur;
				prv = cur;
				if (cur -> var[0] == '_')
					string_map_addss (blockmail -> smap, cur -> var + 1, cur -> val);
			}
			for (run = cb; run; run = run ->next) {
				char	id[1024];
				
				if (run -> condition)
					snprintf (id, sizeof (id) - 1, "%s - %s", fname, run -> condition);
				else
					strcpy (id, fname);
				rc = false;
				if (r = receiver_alloc (blockmail, 0)) {
					r -> customer_id = 100;
					r -> user_type = 'W';
					if (postproc) {
						fprintf (stderr, "Postprocessing not available for %s\n", id);
					} else {
						iflua_t		*il;

						if (il = iflua_alloc (blockmail)) {
							il -> rec = r;
							alua_setup_function (il -> lua, NULL, "fileread", l_fileread, il);
							if (quiet)
								alua_setup_function (il -> lua, NULL, "print", l_silent, NULL);
							if (alua_load (il -> lua, id, run -> code, run -> length)) {
								if (unittest)
									rc = do_unittest (il, quiet, benchmark);
								else {
									rc = true;
									lua_getglobal (il -> lua, F_MAIN);
									if (lua_isfunction (il -> lua, -1)) {
										if (lua_pcall (il -> lua, 0, 0, 0) != 0) {
											fprintf (stderr, "Failed to execute function \"" F_MAIN "\"\n");
											fprintf (stderr, "*** %s\n", lua_tostring (il -> lua, -1));
											rc = false;
										}
									} else
										lua_pop (il -> lua, 1);
								}
							} else {
								fprintf (stderr, "Failed to execute code for %s\n", id);
								fprintf (stderr, "*** %s\n", lua_tostring (il -> lua, -1));
							}
							iflua_free (il);
						} else
							fprintf (stderr, "Failed to setup interpreter interface for %s\n", id);
					}
					receiver_free (r);
				} else
					fprintf (stderr, "Failed to setup receiver structure for %s\n", id);
			}
			blockmail_free (blockmail);
		} else
			fprintf (stderr, "Failed to setup blockmail structure for %s\n", fname);
		log_free (lg);
	} else
		fprintf (stderr, "Failed to setup logging interface for %s\n", fname);
	return rc;
}/*}}}*/
int
main (int argc, char **argv) /*{{{*/
{
	int	n;
	bool_t	quiet;
	bool_t	postproc;
	bool_t	unittest;
	int	benchmark;
	int	rc;

	quiet = false;
	postproc = false;
	unittest = false;
	benchmark = 0;
	while ((n = getopt (argc, argv, "hqpub:")) != -1)
		switch (n) {
		case 'q':
			quiet = true;
			break;
		case 'p':
			postproc = true;
			break;
		case 'u':
			unittest = true;
			break;
		case 'b':
			benchmark = atoi (optarg);
			if (benchmark <= 0) {
				fprintf (stderr, "Use a number bigger than 0 to run benchmarks on unittests.\n");
				return 1;
			}
			break;
		case 'h':
		default:
			fprintf (stderr, "Usage: %s [-q] [-p | -u [-b <cycles>]] <fname(s)>\n", argv[0]);
			return n != 'h';
		}
	if (postproc && unittest) {
		fprintf (stderr, "Warning: unittest in postprocess mode not supported, switched off.\n");
		unittest = false;
	}
	rc = 0;
	for (n = optind; n < argc; ++n) {
		char	*buf;
		size_t	length;
		
		if (buf = read_file (argv[n], & length)) {
			codeblock_t	*cb;
			
			if (cb = split_code (buf)) {
				if (! validate (argv[n], cb, quiet, postproc, unittest, benchmark))
					rc = 1;
				codeblock_free_all (cb);
			} else {
				fprintf (stderr, "Failed to split code for %s\n", argv[n]);
				rc = 1;
			}
			free (buf);
		} else
			rc = 1;
	}
	return rc;
}/*}}}*/
