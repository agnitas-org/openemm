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
# include	<stdlib.h>
# include	<ctype.h>
# include	<unistd.h>
# include	<fcntl.h>
# include	<string.h>
# include	<errno.h>
# include	<sys/types.h>
# include	<sys/stat.h>
# include	<sys/wait.h>
# include	<regex.h>
# ifdef		linux
# include	<paths.h>
# else		/* linux */
# define	_PATH_DEVNULL	"/dev/null"
# endif		/* linux */
# include	"qctrl.h"

typedef struct bad { /*{{{*/
	bool_t	inqueue;	/* if the file is still in the queue	*/
	char	*fname;		/* the filename, that caused problems	*/
	int	count;		/* # of times it caused problems	*/
	int	countdown;	/* countdown to next retry		*/
	int	increase;	/* counter increase value		*/
	struct bad
		*next;
	/*}}}*/
}	bad_t;
static bad_t *
bad_alloc (const char *fname) /*{{{*/
{
	bad_t	*b;
	
	if (b = (bad_t *) malloc (sizeof (bad_t)))
		if (b -> fname = strdup (fname)) {
			b -> inqueue = true;
			b -> count = 1;
			b -> countdown = 1;
			b -> increase = 1;
			b -> next = NULL;
		} else {
			free (b);
			b = NULL;
		}
	return b;
}/*}}}*/
static bad_t *
bad_free (bad_t *b) /*{{{*/
{
	if (b) {
		if (b -> fname)
			free (b -> fname);
		free (b);
	}
	return NULL;
}/*}}}*/
static bad_t *
bad_free_all (bad_t *b) /*{{{*/
{
	bad_t	*tmp;
	
	while (tmp = b) {
		b = b -> next;
		bad_free (b);
	}
	return NULL;
}/*}}}*/
static void
bad_set (bad_t *b, int count) /*{{{*/
{
	b -> count = count;
	b -> countdown = count;
}/*}}}*/
static void
bad_inc (bad_t *b) /*{{{*/
{
	bad_set (b, b -> count + b -> increase);
}/*}}}*/

typedef struct match { /*{{{*/
	char	*pattern;	/* the pattern itself			*/
	regex_t	*re;		/* if this is a regular expression	*/
	bool_t	result;		/* if pattern matches, what result	*/
	struct match
		*next;
	/*}}}*/
}	match_t;
static match_t *
match_free (match_t *m) /*{{{*/
{
	if (m) {
		if (m -> pattern)
			free (m -> pattern);
		if (m -> re) {
			regfree (m -> re);
			free (m -> re);
		}
		free (m);
	}
	return NULL;
}/*}}}*/
static match_t *
match_free_all (match_t *m) /*{{{*/
{
	match_t	*tmp;
	
	while (tmp = m) {
		m = m -> next;
		match_free (tmp);
	}
	return NULL;
}/*}}}*/
static match_t *
match_alloc (const char *pattern) /*{{{*/
{
	match_t	*m;
	
	if (m = (match_t *) malloc (sizeof (match_t))) {
		m -> pattern = NULL;
		m -> re = NULL;
		if (*pattern == '!') {
			m -> result = false;
			++pattern;
		} else
			m -> result = true;
		m -> next = NULL;
		if (*pattern == '/') {
			++pattern;
			if (m -> re = malloc (sizeof (regex_t))) {
				if (regcomp (m -> re, pattern, REG_EXTENDED | REG_NOSUB) != 0) {
					free (m -> re);
					m -> re = NULL;
					m = match_free (m);
				}
			} else
				m = match_free (m);
		} else {
			if (! (m -> pattern = strdup (pattern)))
				m = match_free (m);
		}
	}
	return m;
}/*}}}*/
static match_t *
match_find (match_t *m, const char *search) /*{{{*/
{
	while (m) {
		if (m -> pattern) {
			if (strstr (search, m -> pattern))
				break;
		} else if (m -> re) {
			if (regexec (m -> re, search, 0, NULL, 0) == 0)
				break;
		}
		m = m -> next;
	}
	return m;
}/*}}}*/

# define	MT_ALWAYS	1
# define	MT_TRIES	2
# define	MT_AGE		3
# define	MT_AGEINVAL	4

typedef struct { /*{{{*/
	log_t	*lg;		/* where to write log to		*/
	char	*srcpath;	/* source path				*/
	char	*destpath;	/* destination path			*/
	bool_t	isdevnull;	/* if destpath is /dev/null		*/
	bool_t	always;		/* if we should always move		*/
	int	maxtries;	/* if this is reached, move it!		*/
	long	maxage;		/* dito, if the msg is older than this	*/
	long	maxageinval;	/* dito, if the msg is older AND ..	*/
				/* .. is invalid			*/
	char	*informer;	/* a program to inform about the move	*/
	match_t	*rmtch;		/* receiver matching			*/

	pid_t	pid;		/* our own PID				*/
	time_t	now;		/* current time				*/
	unsigned long long
		moved;		/* just for the statistic fans		*/
				/* scratch buffer for building ..	*/
	char	dbuf[PATH_MAX + 1];	/* .. files in the dest. ..	*/
	char	*dptr;		/* .. path				*/
	bad_t	*bad;		/* list of problematic files		*/
	/*}}}*/
}	move_t;

static long
timeparse (const char *str) /*{{{*/
{
	long	tp;
	long	tmp, unit, mult;
	int	n;
	
	tp = 0;
	tmp = 0;
	unit = 0;
	mult = 1;
	n = 0;
	for (;;) {
		switch (str[n]) {
		default:
			unit = -1;
			break;
		case '-':	/* subtract */
			if (! tmp)
				mult = -1;
			else
				unit = -1;
			break;
		case '+':	/* add */
			if (! tmp)
				mult = 1;
			else
				unit = -1;
			break;
		case 's':
		case ' ':
		case '\0':	/* second */
			unit = 1;
			break;
		case 'm':	/* minute */
			unit = 60;
			break;
		case 'h':	/* hour */
			unit = 60 * 60;
			break;
		case 'd':	/* day */
			unit = 60 * 60 * 24;
			break;
		case 'W':	/* week */
			unit = 60 * 60 * 24 * 7;
			break;
		case 'M':	/* ~month */
			unit = 60 * 60 * 24 * 30;
			break;
		case 'Y':	/* ~year */
			unit = 60 * 60 * 24 * 365;
			break;
		case '0': case '1': case '2': case '3': case '4':
		case '5': case '6': case '7': case '8': case '9':
			tmp *= 10;
			switch (str[n]) {
			case '0':	tmp += 0;	break;
			case '1':	tmp += 1;	break;
			case '2':	tmp += 2;	break;
			case '3':	tmp += 3;	break;
			case '4':	tmp += 4;	break;
			case '5':	tmp += 5;	break;
			case '6':	tmp += 6;	break;
			case '7':	tmp += 7;	break;
			case '8':	tmp += 8;	break;
			case '9':	tmp += 9;	break;
			}
			break;
		}
		if (unit == -1) {
			tp = -1;
			break;
		} else if (unit > 0) {
			tp += tmp * unit * mult;
			tmp = 0;
			unit = 0;
		}
		if (str[n] == '\0')
			break;
		++n;
	}
	return tp;
}/*}}}*/

void *
move_init (log_t *lg, bool_t force, char **args, int alen) /*{{{*/
{
	move_t		*m;
	bool_t		st;
	struct stat	fst;
	char		*copy;
	
	if (m = (move_t *) malloc (sizeof (move_t))) {
		m -> lg = lg;
		m -> srcpath = args[0];
		m -> destpath = args[1];
		m -> isdevnull = (! strcmp (m -> destpath, _PATH_DEVNULL)) ? true : false;
		m -> always = false;
		m -> maxtries = -1;
		m -> maxage = -1;
		m -> maxageinval = -1;
		m -> informer = NULL;
		m -> rmtch = NULL;
		m -> pid = getpid ();
		m -> now = 0;
		m -> moved = 0;
		strcpy (m -> dbuf, m -> destpath);
		for (m -> dptr = m -> dbuf; *(m -> dptr); m -> dptr++)
			;
		*(m -> dptr++) = '/';
		m -> bad = NULL;
		st = true;
		if (! strcmp (m -> srcpath, m -> destpath)) {
			log_out (lg, LV_ERROR, "Source and destination are the same directories");
			st = false;
		}
		if (chdir (m -> srcpath) == -1) {
			log_out (lg, LV_ERROR, "Unable to chdir to sourcepath %s (%d, %m)", m -> srcpath, errno);
			st = false;
		}
		if (! m -> isdevnull) {
			if ((stat (m -> destpath, & fst) == -1) || (! S_ISDIR (fst.st_mode))) {
				log_out (lg, LV_ERROR, "Destination directory %s not accessible", m -> destpath);
				st = false;
			}
		} else if (! force) {
			log_out (lg, LV_ERROR, "If you want to remove queue files, you have to force it");
			st = false;
		}
		if (alen == 2) {
			m -> always = true;
		} else if (copy = strdup (args[2])) {
			char	*ptr, *var, *val;
			
			for (ptr = copy; ptr; ) {
				var = ptr;
				if (ptr = strchr (var, ',')) {
					*ptr++ = '\0';
					while (isspace ((unsigned char) *ptr))
						++ptr;
				}
				if (val = strchr (var, ':')) {
					*val++ = '\0';
					if (! strcasecmp (var, "allways"))
						m -> always = atob (val);
					else if (! strcasecmp (var, "tries"))
						m -> maxtries = atoi (val);
					else if (! strcasecmp (var, "maxage"))
						m -> maxage = timeparse (val);
					else if (! strcasecmp (var, "maxageinval"))
						m -> maxageinval = timeparse (val);
					else if (! strcasecmp (var, "informer")) {
						if (! (m -> informer = strdup (val)))
							st = false;
					} else if (! strcasecmp (var, "recv")) {
						match_t	*tmp, *prv;
						
						if (tmp = match_alloc (val)) {
							if (m -> rmtch) {
								for (prv = m -> rmtch; prv -> next; prv = prv -> next)
									;
								prv -> next = tmp;
							} else
								m -> rmtch = tmp;
						} else
							st = false;
					} else {
						log_out (lg, LV_ERROR, "Unknown limit ID: %s", var);
						st = false;
					}
				} else {
					log_out (lg, LV_ERROR, "Missing value for limit ID: %s", var);
					st = false;
				}
			}
			free (copy);
		} else
			st = false;
		if (m -> always && (! force)) {
			log_out (lg, LV_ERROR, "If you want to move without condition, you have to force it");
			st = false;
		}
		if ((! m -> always) && (m -> maxtries == -1) && (m -> maxage == -1) && (m -> maxageinval == -1)) {
			log_out (lg, LV_ERROR, "No method found to mark a message for moving");
			st = false;
		} else {
			if (m -> maxtries != -1)
				if (m -> maxtries < 1) {
					log_out (lg, LV_ERROR, "Maximum tries must be greater than zero (%d)", m -> maxtries);
					st = false;
				} else if ((m -> maxtries < 3) && (! force)) {
					log_out (lg, LV_ERROR, "If this small value of maximum tries (%d) is desired, you have to force it", m -> maxtries);
					st = false;
				}
			if (m -> maxage != -1)
				if (m -> maxage < 1) {
					log_out (lg, LV_ERROR, "Maximum age must be greater than zero (%ld)", m -> maxage);
					st = false;
				} else if ((m -> maxage < 60) && (! force)) {
					log_out (lg, LV_ERROR, "If this small value for message aging (%ld) is desired, you have to force it", m -> maxage);
					st = false;
				}
			if (m -> maxageinval != -1)
				if (m -> maxageinval < 0) {
					log_out (lg, LV_ERROR, "Maximum age must be greater than or equal zero (%ld)", m -> maxageinval);
					st = false;
				}
		}
		if (! st) {
			free (m);
			m = NULL;
		}
	}
	return m;
}/*}}}*/
bool_t
move_deinit (void *data) /*{{{*/
{
	move_t	*m = (move_t *) data;

	log_out (m -> lg, LV_INFO, "Moved %llu mail%s", m -> moved, (m -> moved == 1ULL ? "" : "s"));
	if (m -> informer)
		free (m -> informer);
	if (m -> rmtch)
		match_free_all (m -> rmtch);
	if (m -> bad)
		bad_free_all (m -> bad);
	free (m);
	return true;
}/*}}}*/

static bad_t *
add_bad (move_t *m, const char *fname) /*{{{*/
{
	bad_t	*bad;

	if (bad = bad_alloc (fname)) {
		bad -> next = m -> bad;
		m -> bad = bad;
	}
	return bad;
}/*}}}*/
static int
reached_limits (void *data, queue_t *q, const char *fname) /*{{{*/
{
	move_t	*m = (move_t *) data;
	int	match;
	
	match = 0;
	if (fname[0] == 'q') {
		bad_t	*bad;
		int	fd;
		bool_t	err;
		char	*ptr, *sav;
		int	tries;
		time_t	age;
		int	sender, receiver;

		for (bad = m -> bad; bad; bad = bad -> next)
			if (! strcmp (bad -> fname, fname)) {
				bad -> inqueue = true;
				break;
			}
		if ((! bad) || (bad -> countdown-- <= 0)) {
			bool_t	checkmatch = m -> rmtch ? true : false;
			bool_t	doesmatch = false;
			
			if ((! checkmatch) && m -> always)
				match = MT_ALWAYS;
			else if ((fd = open (fname, O_RDONLY)) != -1) {
				if (queue_readfd (q, fd)) {
					err = false;
					tries = -1;
					age = (time_t) -1;
					sender = 0;
					receiver = 0;
					for (ptr = (char *) q -> qf -> buffer; ptr && (! match); ) {
						sav = ptr;
						if (ptr = strchr (ptr, '\n'))
							*ptr++ = '\0';
						switch (*sav) {
						case 'N':
							tries = atoi (sav + 1);
							break;
						case 'T':
							age = (time_t) atol (sav + 1);
							break;
						case 'S':
							++sender;
							break;
						case 'R':
							if (m -> rmtch) {
								match_t	*mtch = match_find (m -> rmtch, sav + 1);
								
								if (mtch)
									doesmatch = mtch -> result;
							}
							++receiver;
							break;
						}
					}
					if ((! checkmatch) || doesmatch) {
						if (m -> always) {
							match = MT_ALWAYS;
						} else {
							if (tries == -1)
								log_out (m -> lg, LV_DEBUG, "No `N' entry in %s found, assuming new file", fname);
							else if ((m -> maxtries != -1) && (tries > m -> maxtries))
								match = MT_TRIES;
							if (age == (time_t) -1) {
								log_out (m -> lg, LV_WARNING, "No `T' entry in %s found", fname);
								err = true;
							} else {
								if ((m -> maxage != -1) && (age + m -> maxage < m -> now))
									match = MT_AGE;
								if ((m -> maxageinval != -1) && (age + m -> maxageinval < m -> now)) {
									if ((! sender) || (! receiver))
										match = MT_AGEINVAL;
								}
							}
						}
					}
				} else {
					log_out (m -> lg, LV_WARNING, "Unable to read content of %s (%d, %m)", fname, errno);
					err = true;
				}
				close (fd);
				if (err) {
					if (! bad)
						bad = add_bad (m, fname);
					if (bad) {
						if (bad -> increase < 12)
							bad -> increase = 12;
						bad_inc (bad);
					}
				}
			} else if (errno != ENOENT)
				log_out (m -> lg, LV_ERROR, "%s cannot be opened (%d, %m)", fname, errno);
			else if (bad)
				bad -> inqueue = false;
		}
	}
	return match;
}/*}}}*/
static bool_t
link_or_copy (const char *src, const char *dst) /*{{{*/
{
	if ((link (src, dst) != -1) || (errno == EEXIST))
		return true;
	if (errno == EXDEV) {
		bool_t		rc;
		int		fdi, fdo;
		struct stat	st;
		int		omask;
		
		rc = false;
		if ((fdi = open (src, O_RDONLY)) != -1) {
			if (fstat (fdi, & st) != -1) {
				if ((omask = umask (0)) != -1) {
					char		*tempdst, *tptr;
					const char	*ptr;
					
					if (tempdst = malloc (strlen (dst) + 128)) {
						if (ptr = strrchr (dst, '/')) {
							++ptr;
							strncpy (tempdst, dst, ptr - dst);
							tptr = tempdst + (ptr - dst);
						} else {
							ptr = dst;
							tptr = tempdst;
						}
						sprintf (tptr, ".%d.%s", getpid (), ptr);
						unlink (tempdst);
						if ((fdo = open (tempdst, O_CREAT | O_WRONLY | O_EXCL, st.st_mode)) != -1) {
							int	n;
							char	buf[65536];
						
							rc = true;
							while ((n = read (fdi, buf, sizeof (buf))) > 0)
								if (write (fdo, buf, n) != n)
									break;
							if (n || (fchown (fdo, st.st_uid, st.st_gid) == -1))
								rc = false;
							if (close (fdo) == -1)
								rc = false;
							if (rc)
								if (rename (tempdst, dst) == -1)
									rc = false;
							if (! rc)
								unlink (tempdst);
						}
						free (tempdst);
					}
					umask (omask);
				}
			}
			close (fdi);
		}
		return rc;
	}
	return false;
}/*}}}*/
static bool_t
move_file (move_t *m, bad_t *bad, const entry_t *file) /*{{{*/
{
	bool_t		st;
	const char	*why;
	
	st = false;
	strcpy (m -> dptr, file -> fname);
	if (m -> isdevnull) {
		if ((unlink (m -> dptr) != -1) || (errno == ENOENT)) {
			const char	tokill[] = "dtx";
			int		n;
		
			for (n = 0; tokill[n]; ++n) {
				m -> dptr[0] = tokill[n];
				unlink (m -> dptr);
			}
			st = true;
		} else
			log_out (m -> lg, LV_ERROR, "%s cannot be killed (%d, %m)", m -> dptr, errno);
	} else {
		m -> dptr[0] = 'd';
		if (link_or_copy (m -> dptr, m -> dbuf)) {
			m -> dptr[0] = 'q';
			if (link_or_copy (m -> dptr, m -> dbuf)) {
				unlink (m -> dptr);
				m -> dptr[0] = 'd';
				unlink (m -> dptr);
				if (file -> match == MT_ALWAYS)
					why = "always";
				else if (file -> match == MT_TRIES)
					why = "reached maximum tries";
				else if (file -> match == MT_AGE)
					why = "reached maximum age";
				else if (file -> match == MT_AGEINVAL)
					why = "reached maximum age for invalid control file";
				else
					why = "why???";
				log_out (m -> lg, LV_INFO, "%s moved to %s (%s)", file -> fname, m -> destpath, why);
				st = true;
			} else {
				log_out (m -> lg, LV_ERROR, "Unable to link %s to %s (%d, %m)", m -> dptr, m -> dbuf, errno);
				unlink (m -> dbuf);
				m -> dptr[0] = 'd';
				unlink (m -> dbuf);
			}
		} else {
			log_out (m -> lg, LV_ERROR, "Unable to link %s to %s (%d, %m)", m -> dptr, m -> dbuf, errno);
			unlink (m -> dbuf);
			m -> dptr[0] = 'q';
			unlink (m -> dbuf);
		}
	}
	if (! st) {
		if (bad) {
			bad_inc (bad);
			log_out (m -> lg, LV_INFO, "Moving of %s failed again (%d times)", file -> fname, bad -> count);
		} else {
			add_bad (m, file -> fname);
			log_out (m -> lg, LV_INFO, "Moving of %s marked as failed", file -> fname);
		}
	} else if (bad) {
		bad -> inqueue = false;
		log_out (m -> lg, LV_INFO, "Failed entry %s now succeeded after %d tr%s", file -> fname, bad -> count, (bad -> count == 1 ? "y" : "ies"));
	}
	if (st) {
		m -> moved++;
		if (m -> informer) {
			pid_t	pid, npid;
			
			m -> dptr[0] = 'q';
			if ((pid = fork ()) == -1)
				log_out (m -> lg, LV_WARNING, "Unable to fork for informer %s on %s", m -> informer, m -> dbuf);
			else if (pid == 0) {
				char	*av[3];
			
				av[0] = m -> informer;
				av[1] = m -> dbuf;
				av[2] = NULL;
				if (av[0][0] == '/')
					execv (av[0], av);
				else
					execvp (av[0], av);
				_exit (126);
			} else
				while ((npid = waitpid (pid, NULL, 0)) != pid)
					if ((npid == -1) && (errno != EINTR))
						break;
		}
	}
	return st;
}/*}}}*/
bool_t
move_exec (void *data) /*{{{*/
{
	move_t	*m = (move_t *) data;
	bool_t	st;
	bad_t	*bad, *prv;
	queue_t	*q;
	
	st = true;
	for (bad = m -> bad; bad; bad = bad -> next)
		bad -> inqueue = false;
	time (& m -> now);
	if ((q = queue_scan (m -> srcpath, reached_limits, m)) && (chdir (m -> srcpath) != -1)) {
		entry_t	*run;
		int	fd;

		for (run = q -> ent; run; run = run -> next) {
			for (bad = m -> bad; bad; bad = bad -> next)
				if (! strcmp (bad -> fname, run -> fname))
					break;
			if ((fd = queue_lock (q, run -> fname, m -> pid)) != -1) {
				move_file (m, bad, run);
				queue_unlock (q, fd, m -> pid);
			} else {
				if (bad)
					bad_inc (bad);
				else
					add_bad (m, run -> fname);
				log_out (m -> lg, LV_INFO, "%s cannot be locked, skipping", run -> fname);
			}
		}
		queue_free (q);
		for (bad = m -> bad, prv = NULL; bad; )
			if (! bad -> inqueue) {
				if (prv)
					prv -> next = bad -> next;
				else
					m -> bad = m -> bad -> next;
				bad_free (bad);
				if (prv)
					bad = prv -> next;
				else
					bad = m -> bad;
			} else {
				prv = bad;
				bad = bad -> next;
			}
	} else {
		log_out (m -> lg, LV_ERROR, "Unable to read sourcepath %s", m -> srcpath);
		st = false;
	}
	return st;
}/*}}}*/
