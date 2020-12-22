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
# include	<stdarg.h>
# include	<ctype.h>
# include	<unistd.h>
# include	<fcntl.h>
# include	<errno.h>
# include	<string.h>
# include	<time.h>
# include	<limits.h>
# include	<dirent.h>
# include	<sys/wait.h>
# include	<sysexits.h>
# include	"xmlback.h"

typedef struct sendmail	sendmail_t;

typedef struct { /*{{{*/
	bool_t		istemp;		/* temp. filenames		*/
	char		*acclog;	/* optional accounting log	*/
	char		*bnclog;	/* optional bounce log		*/
	char		*midlog;	/* optional message-id log	*/
	char		*tracklog;	/* optional mailtracking log	*/
	sendmail_t	*s;		/* output generating for mails	*/
	/*}}}*/
}	gen_t;

static bool_t
boolean (const char *str) /*{{{*/
{
	return ((! str) || atob (str)) ? true : false;
}/*}}}*/
static bool_t
write_content (int fd, const byte_t *ptr, long len, const char *nl, int nllen) /*{{{*/
{
	bool_t	st;
	
	st = true;
	if (len > 0) {
		int	n;
			
		if (nl) {
			int	nlen;
				
			while (len > 0) {
				for (nlen = 0; nlen < len; ++nlen)
					if ((ptr[nlen] == '\r') || (ptr[nlen] == '\n'))
						break;
				if (nlen > 0) {
					if (write (fd, ptr, nlen) == nlen) {
						ptr += nlen;
						len -= nlen;
					} else {
						st = false;
						break;
					}
				}
				if (len > 0) {
					if ((len > 1) && (ptr[0] == '\r') && (ptr[1] == '\n')) {
						ptr += 2;
						len -= 2;
					} else if ((ptr[0] == '\n') || (ptr[0] == '\r')) {
						ptr += 1;
						len -= 1;
					}
					if (write (fd, nl, nllen) != nllen) {
						st = false;
						break;
					}
				}
			}
		} else {
			while (len > 0)
				if ((n = write (fd, ptr, len)) > 0) {
					ptr += n;
					len -= n;
				} else {
					st = false;
					break;
				}
		}
	}
	return st;
}/*}}}*/
static bool_t
write_file (const char *fname, const buffer_t *content, const char *nl, int nllen) /*{{{*/
{
	bool_t	st;
	int	fd;
	
	st = false;
	if ((fd = open (fname, O_WRONLY | O_CREAT | O_TRUNC, 0644)) != -1) {
		if (nl)
			st = write_content (fd, content -> buffer, content -> length, nl, nllen);
		else
			st = write (fd, content -> buffer, content -> length) == content -> length ? true : false;
		if (close (fd) == -1)
			st = false;
	}
	return st;
}/*}}}*/
static bool_t
write_bounce_log (gen_t *g, blockmail_t *blockmail, receiver_t *rec, const char *dsn, const char *reason) /*{{{*/
{
	bool_t	st;
	FILE	*fp;

	st = false;
	if (g -> bnclog && (fp = fopen (g -> bnclog, "a"))) {
		time_t		now;
		struct tm	*tt;
		char		ts[128];
			
		time (& now);
		if (tt = localtime (& now)) {
			snprintf (ts, sizeof (ts) - 1, "timestamp=%04d-%02d-%02d %02d:%02d:%02d\t", tt -> tm_year + 1900, tt -> tm_mon + 1, tt -> tm_mday, tt -> tm_hour, tt -> tm_min, tt -> tm_sec);
		} else {
			ts[0] = '\0';
		}
		st = true;
		if (fprintf (fp, "%s;%d;%d;%d;%d;%s%s\n", dsn, blockmail -> licence_id, blockmail -> mailing_id, (rec -> media ? rec -> media -> type : Mediatype_EMail), rec -> customer_id, ts, reason) < 0) {
			st = false;
		}
		if (fclose (fp) == EOF) {
			st = false;
		}
	}
	return st;
}/*}}}*/
static bool_t
create_bcc_head (buffer_t *target, blockmail_t *blockmail, const char *bcc, int nr) /*{{{*/
{
	bool_t		rc;
	int		length = buffer_length (blockmail -> head);
	const char	*ptr = (const char *) buffer_content (blockmail -> head);
	bool_t		found_receiver = false,
			found_message_id = false,
			found_to = false;
	bool_t		ignore_current = false;
	const char	*cur;
	int		len;
	
	rc = true;
	buffer_clear (target);
	while (rc && (length > 0)) {
		cur = ptr;
		while ((*ptr != '\n') && (length > 0))
			++ptr, --length;
		if (length > 0)
			++ptr, --length;
		len = ptr - cur;
		if (*cur == 'R') {
			rc = buffer_appendsn (target, "R<", 2) &&
				buffer_appends (target, bcc) &&
				buffer_appendsn (target, ">\n", 2);
			found_receiver = true;
		} else if (*cur == 'H') {
# define	H_MESSAGE_ID		"HMessage-ID: <"
# define	H_TO			"HTo: "
			ignore_current = false;
			if ((! found_message_id) && (! strncmp (cur, H_MESSAGE_ID, sizeof (H_MESSAGE_ID) - 1)) && (len > sizeof (H_MESSAGE_ID))) {
				rc = buffer_appendsn (target, cur, sizeof (H_MESSAGE_ID) - 1) &&
					buffer_format (target, "V%d-", nr) &&
					buffer_appendsn (target, cur + sizeof (H_MESSAGE_ID) - 1, len - (sizeof (H_MESSAGE_ID) - 1));
				found_message_id = true;
			} else {
				rc = buffer_appendsn (target, cur, len);
				if (rc && (! found_to) && (! strncmp (cur, H_TO, sizeof (H_TO) - 1))) {
					rc = buffer_appendsn (target, "HBcc: <", 7) &&
						buffer_appends (target, bcc) &&
						buffer_appendsn (target, ">\n", 2);
					found_to = true;
				}
			}
		} else if ((! isspace (*cur)) || (! ignore_current)) {
			rc = buffer_appendsn (target, cur, len);
		}
	}
	return rc && found_receiver && found_message_id && found_to;
}/*}}}*/
static bool_t
flatten_header (buffer_t *target, buffer_t *header) /*{{{*/
{
	bool_t		rc;
	const byte_t	*head = buffer_content (header);
	int		hlen = buffer_length (header);
	const byte_t	*ptr;
	int		pos, len;

	rc = true;
	buffer_clear (target);
	pos = 0;
	while (rc && (pos < hlen)) {
		ptr = head + pos;
		while (pos < hlen && (head[pos] != '\n'))
			++pos;
		if (pos < hlen)
			++pos;
		len = (head + pos) - ptr;
		if (isspace (*ptr)) {
			rc = buffer_append (target, ptr, len);
		} else if (*ptr == 'H') {
			++ptr, --len;
			if (*ptr == '?') {
				++ptr, --len;
				while ((len > 0) && (*ptr != '?'))
					++ptr, --len;
				if (len > 0)
					++ptr, --len;
			}
			rc = buffer_append (target, ptr, len);
		}
	}
	if (rc)
		rc = buffer_appendch (target, '\n');
	return rc;
}/*}}}*/

typedef struct { /*{{{*/
	char	*dir;		/* the spool directory			*/
	char	*buf;		/* buffer for creating files		*/
	char	*ptr;		/* pointer to start of filepart		*/
	char	*dptr;		/* pointer to start of vairant dir	*/
	char	*fptr;		/* pointer to start of variant part	*/
	char	*temp;		/* temp.file in directory		*/
	bool_t	devnull;	/* if we want to write to /dev/null	*/
	/*}}}*/
}	spool_t;
static spool_t *
spool_free (spool_t *s) /*{{{*/
{
	if (s) {
		if (s -> dir)
			free (s -> dir);
		if (s -> buf)
			free (s -> buf);
		if (s -> temp) {
			if (s -> temp[0])
				unlink (s -> temp);
			free (s -> temp);
		}
		free (s);
	}
	return NULL;
}/*}}}*/
static spool_t *
spool_alloc (const char *dir, bool_t subdirs) /*{{{*/
{
	spool_t	*s;
	
	if (s = (spool_t *) malloc (sizeof (spool_t))) {
		if (! strcmp (dir, "/dev/null")) {
			s -> dir = NULL;
			s -> buf = NULL;
			s -> ptr = NULL;
			s -> dptr = NULL;
			s -> fptr = NULL;
			s -> temp = NULL;
			s -> devnull = true;
		} else {
			int	dlen = strlen (dir);

			s -> dir = strdup (dir);
			s -> buf = malloc (dlen + PATH_MAX + 1);
			s -> temp = malloc (dlen + PATH_MAX + 1);
			if (s -> temp)
				s -> temp[0] = '\0';
			if (s -> dir && s -> buf && s -> temp) {
				strcpy (s -> buf, dir);
				s -> ptr = s -> buf + dlen;
				if (subdirs) {
					*(s -> ptr)++ = '/';
					s -> dptr = s -> ptr;
					*(s -> ptr)++ = 'q';
					*(s -> ptr)++ = 'f';
				} else
					s -> dptr = NULL;
				*(s -> ptr)++ = '/';
				sprintf (s -> temp, "%s/.xmlgen.%06d", dir, (int) getpid ());
				s -> fptr = s -> ptr;
				s -> devnull = false;
			} else
				s = spool_free (s);
		}
	}
	return s;
}/*}}}*/
static void
spool_setprefix (spool_t *s, const char *prefix) /*{{{*/
{
	if (! s -> devnull) {
		s -> fptr = s -> ptr;
		strcpy (s -> fptr, prefix);
		while (*(s -> fptr))
			s -> fptr++;
	}
}/*}}}*/
static void
spool_addprefix (spool_t *s, const char *prefix) /*{{{*/
{
	if (! s -> devnull) {
		strcpy (s -> fptr, prefix);
		while (*(s -> fptr))
			s -> fptr++;
	}
}/*}}}*/
static void
spool_tmpprefix (spool_t *s) /*{{{*/
{
	if (! s -> devnull) {
		char	prefix[64];
	
		sprintf (prefix, "%lxT%04lx", (unsigned long) getpid () & 0xffff, ((unsigned long) time (NULL) >> 6) & 0xffff);
		spool_addprefix (s, prefix);
	}
}/*}}}*/

static const char	uniques[] = "GHIJKLMNOPQRSTUVWXYZghijklmnopqrstuvwxyz";
static bool_t
spool_unique (spool_t *s) /*{{{*/
{
	if (! s -> devnull) {
		const char	*u;
		int		fd;
		char		*mod;
		
		for (u = uniques, fd = -1, mod = NULL; *u && (fd == -1); ++u) {
			fd = open (s -> buf, O_CREAT | O_EXCL, 0644);
			if (fd == -1) {
				if (errno != EEXIST)
					break;
				if (! mod) {
					for (mod = s -> ptr; *mod; ++mod)
						;
					*(mod + 1) = '\0';
				}
				*mod = *u;
			}
		}
		if (fd == -1)
			return false;
		close (fd);
	}
	return true;
}/*}}}*/
static bool_t
spool_unique_by_number (spool_t *s) /*{{{*/
{
	if (! s -> devnull) {
		unsigned int	nr;
		int		fd;
		char		*mod;

		for (nr = 1, fd = -1, mod= NULL; (nr != 0) && (fd == -1); ++nr) {
			fd = open (s -> buf, O_CREAT | O_EXCL, 0644);
			if (fd == -1) {
				if (errno != EEXIST)
					break;
				if (! mod) {
					for (mod = s -> ptr; *mod; ++mod)
						;
				}
				sprintf (mod, "%u", nr);
			}
		}
		if (fd == -1)
			return false;
		close (fd);
	}
	return true;
}/*}}}*/
static bool_t
spool_write (spool_t *s, buffer_t *content, const char *nl, int nllen) /*{{{*/
{
	return s -> devnull ? true : write_file (s -> buf, content, nl, nllen);
}/*}}}*/
static bool_t
spool_write_temp (spool_t *s, buffer_t *content, const char *nl, int nllen) /*{{{*/
{
	return s -> devnull ? true : write_file (s -> temp, content, nl, nllen);
}/*}}}*/
static bool_t
spool_validate (spool_t *s) /*{{{*/
{
	if (! s -> devnull)
		if (rename (s -> temp, s -> buf) == -1)
			return false;
	return true;
}/*}}}*/

# define	DEF_DESTDIR		"."
# define	DEF_FLUSHCMD		"fqu"

typedef struct action { /*{{{*/
	int		interval;	/* how often to start		*/
	int		instance;	/* # of times executed in run	*/
	char		*cmd;		/* command to use		*/
	char		**ags;		/* prepared for execv/execvp	*/
	int		ccnt;		/* # of slots used for cmd	*/
	int		size;		/* # of allocated slots		*/
	struct action	*next;
	/*}}}*/
}	action_t;
static action_t *
action_free (action_t *a) /*{{{*/
{
	if (a) {
		if (a -> cmd)
			free (a -> cmd);
		if (a -> ags)
			free (a -> ags);
		free (a);
	}
	return NULL;
}/*}}}*/
static action_t *
action_free_all (action_t *a) /*{{{*/
{
	action_t	*tmp;
	
	while (tmp = a) {
		a = a -> next;
		action_free (tmp);
	}
	return NULL;
}/*}}}*/
static action_t *
action_alloc (int interval, const char *cmd) /*{{{*/
{
	action_t	*a;
	
	if (a = (action_t *) malloc (sizeof (action_t))) {
		a -> interval = interval;
		a -> instance = 0;
		a -> cmd = NULL;
		a -> ags = NULL;
		a -> ccnt = 0;
		a -> size = 0;
		a -> next = NULL;
		if (a -> cmd = strdup (cmd)) {
			char	*ptr, *sav;
			char	quote;
			int	n, m;

			for (ptr = a -> cmd; *ptr; ) {
				if (a -> ccnt >= a -> size) {
					a -> size += 16;
					if (! (a -> ags = (char **) realloc (a -> ags, (a -> size + 1) * sizeof (char *))))
						break;
				}
				if ((*ptr == '"') || (*ptr == '\''))
					quote = *ptr++;
				else
					quote = '\0';
				a -> ags[a -> ccnt++] = ptr;
				for (n = 0, m = 0; ptr[n]; ++n) {
					if ((ptr[n] == '\\') && ptr[n + 1])
						++n;
					else if ((quote && (ptr[n] == quote)) ||
						 ((! quote) && isspace ((int) ((unsigned char) ptr[n]))))
						break;
					if (n != m)
						ptr[m++] = ptr[n];
					else
						++m;
				}
				sav = ptr;
				ptr += n;
				if (*ptr) {
					*ptr++ = '\0';
					while (isspace ((int) ((unsigned char) *ptr)))
						++ptr;
				}
				sav[m] = '\0';
			}
			if (! a -> ags)
				a = action_free (a);
		} else
			a = action_free (a);
	}
	return a;
}/*}}}*/
static bool_t
action_go (action_t *a, log_t *lg, ...) /*{{{*/
{
	va_list		par;
	bool_t		st;
	int		n;
	char		*ptr;

	va_start (par, lg);
	st = false;
	n = a -> ccnt;
	while (ptr = va_arg (par, char *)) {
		if (n >= a -> size) {
			a -> size += 16;
			if (! (a -> ags = (char **) realloc (a -> ags, (a -> size + 1) * sizeof (char *)))) {
				log_out (lg, LV_ERROR, "Unable to increase execution array to %d slots", a -> size);
				break;
			}
		}
		a -> ags[n++] = ptr;
	}
	if ((! ptr) && a -> ags && a -> ags[0]) {
		pid_t	pid, npid;
		int	rc;
		
		a -> ags[n] = NULL;
		if ((pid = fork ()) == -1) {
			log_out (lg, LV_ERROR, "Unable to fork (%m) for %s", a -> cmd);
		} else if (pid == 0) {
			if (a -> ags[0][0] == '/')
				execv (a -> ags[0], a -> ags);
			else
				execvp (a -> ags[0], a -> ags);
			_exit (127);
		} else {
			while (((npid = waitpid (pid, & rc, 0)) != pid) && ((npid != -1) || (errno == EINTR)))
				;
			if (npid != pid)
				log_out (lg, LV_ERROR, "No child process found for %s (%m)", a -> cmd);
			else if (WIFEXITED (rc) && WEXITSTATUS (rc))
				log_out (lg, LV_INFO, "Child process %s terminated with status %d", a -> cmd, WEXITSTATUS (rc));
			else if (WIFSIGNALED (rc))
				log_out (lg, LV_ERROR, "Child process %s died due to signal %d", a -> cmd, WTERMSIG (rc));
			else if (rc)
				log_out (lg, LV_ERROR, "Child process %s returns %d", a -> cmd, rc);
			else
				st = true;
		}
	}
	va_end (par);
	return st;
}/*}}}*/

# define	dmatch(aa,bb)		(((aa)[0] == (bb)[0]) && (! strcmp ((aa), (bb))))
typedef struct { /*{{{*/
	char	*name;			/* the domain name		*/
	int	count;			/* the number of occurances	*/
	/*}}}*/
}	domain_t;
typedef struct { /*{{{*/
	int	maxcount;		/* max # of domains to flush	*/
	char	*cmd;			/* command to use		*/
	char	*idpattern;		/* pattern for spool ID		*/
	domain_t
		*d;			/* all stored domains		*/
	int	dcnt, dsiz;		/* used/allocated slots in d	*/
	/*}}}*/
}	qflush_t;
static qflush_t *
qflush_alloc (int maxcount, const char *cmd) /*{{{*/
{
	qflush_t	*q;
	
	if (q = (qflush_t *) malloc (sizeof (qflush_t))) {
		q -> maxcount = maxcount;
		q -> cmd = NULL;
		q -> idpattern = NULL;
		q -> d = NULL;
		q -> dcnt = 0;
		q -> dsiz = 0;
		if (cmd && (! (q -> cmd = strdup (cmd)))) {
			free (q);
			q = NULL;
		}
	}
	return q;
}/*}}}*/
static qflush_t *
qflush_free (qflush_t *q) /*{{{*/
{
	if (q) {
		if (q -> cmd)
			free (q -> cmd);
		if (q -> idpattern)
			free (q -> idpattern);
		if (q -> d) {
			int	n;
			
			for (n = 0; n < q -> dcnt; ++n)
				if (q -> d[n].name)
					free (q -> d[n].name);
			free (q -> d);
		}
		free (q);
	}
	return NULL;
}/*}}}*/
static bool_t
qflush_set_command (qflush_t *q, const char *cmd) /*{{{*/
{
	return struse (& q -> cmd, cmd);
}/*}}}*/
static bool_t
qflush_set_idpattern (qflush_t *q, const char *idpattern) /*{{{*/
{
	return struse (& q -> idpattern, idpattern);
}/*}}}*/
static bool_t
qflush_add (qflush_t *q, const char *domain) /*{{{*/
{
	bool_t	rc;
	int	n, m;

	rc = false;
	for (n = 0; n < q -> dcnt; ++n)
		if (dmatch (q -> d[n].name, domain))
			break;
	if (n < q -> dcnt) {
		q -> d[n].count++;
		for (m = n - 1; m >= 0; --m)
			if (q -> d[m].count > q -> d[n].count)
				break;
		if (++m != n) {
			domain_t	tmp;
			
			tmp = q -> d[m];
			q -> d[m] = q -> d[n];
			q -> d[n] = tmp;
		}
		rc = true;
	} else {
		if (q -> dcnt == q -> dsiz) {
			int		nsiz = q -> dsiz ? q -> dsiz * 2 : 128;
			domain_t	*tmp;
			
			if (tmp = (domain_t *) realloc (q -> d, nsiz * sizeof (domain_t))) {
				q -> dsiz = nsiz;
				q -> d = tmp;
			}
		}
		if ((q -> dcnt < q -> dsiz) && (q -> d[q -> dcnt].name = strdup (domain))) {
			q -> d[q -> dcnt].count = 1;
			q -> dcnt++;
			rc = true;
		}
	}
	return rc;
}/*}}}*/
static bool_t
qflush_flush (qflush_t *q, log_t *lg, const char *destdir) /*{{{*/
{
	bool_t	rc;
	char	**av;
	int	ac;
	int	domains;
	
	rc = false;
	domains = q -> maxcount;
	if (domains > q -> dcnt)
		domains = q -> dcnt;
	ac = domains + 3;
	if (av = (char **) malloc ((ac + 1) * sizeof (char *))) {
		int	n, idx;
		char	*ddir;
		char	*ipat;
		pid_t	pid, npid;
		int	st;
		
		idx = 0;
		ddir = NULL;
		av[idx++] = q -> cmd;
		if (destdir && (ddir = malloc (strlen (destdir) + 8))) {
			sprintf (ddir, "-d%s", destdir);
			av[idx++] = ddir;
		}
		ipat = NULL;
		if (q -> idpattern && (ipat = malloc (strlen (q -> idpattern) + 8))) {
			sprintf (ipat, "-i%s", q -> idpattern);
			av[idx++] = ipat;
		}
		for (n = 0; n < domains; ++n)
			av[idx + n] = q -> d[n].name;
		av[idx + n] = NULL;
		if ((pid = fork ()) == -1) {
			log_out (lg, LV_ERROR, "Unable to fork (%m) for flush-queue %s", q -> cmd);
		} else if (pid == 0) {
			if (av[0][0] == '/')
				execv (av[0], av);
			else
				execvp (av[0], av);
			_exit (127);
		} else {
			while (((npid = waitpid (pid, & st, 0)) != pid) && ((npid != -1) || (errno == EINTR)))
				;
			if (npid != pid)
				log_out (lg, LV_ERROR, "No child process found for flush-queue %s (%m)", q -> cmd);
			else if (st)
				log_out (lg, LV_ERROR, "Child process for flush-queue %s returns %d", q -> cmd, st);
			else
				st = true;
		}
		if (ddir)
			free (ddir);
		if (ipat)
			free (ipat);
		free (av);
	}
	return rc;
}/*}}}*/

typedef struct bad { /*{{{*/
	spool_t	*spool;			/* spool for bad domains	*/
	char	**domains;		/* list of bad domains		*/
	int	size;			/* # of slots availbale		*/
	int	use;			/* # of slots used		*/
	/*}}}*/
}	bad_t;
static bad_t *
bad_alloc (const char *dir) /*{{{*/
{
	bad_t	*b;
	
	if (b = (bad_t *) malloc (sizeof (bad_t))) {
		if (b -> spool = spool_alloc (dir, false)) {
			b -> domains = NULL;
			b -> size = 0;
			b -> use = 0;
		} else {
			free (b);
			b = NULL;
		}
	}
	return b;
}/*}}}*/
static bad_t *
bad_free (bad_t *b) /*{{{*/
{
	if (b) {
		if (b -> spool)
			spool_free (b -> spool);
		if (b -> domains) {
			int	n;
			
			for (n = 0; n < b -> use; ++n)
				if (b -> domains[n])
					free (b -> domains[n]);
			free (b -> domains);
		}
		free (b);
	}
	return NULL;
}/*}}}*/
static bool_t
bad_add (bad_t *b, const char *domain) /*{{{*/
{
	bool_t	rc;
	
	rc = false;
	if (b -> use >= b -> size) {
		int	nsize = b -> size ? b -> size << 1 : 256;
		char	**ndomains;
		
		if (ndomains = (char **) realloc (b -> domains, nsize * sizeof (char *))) {
			b -> domains = ndomains;
			b -> size = nsize;
		}
	}
	if (b -> use < b -> size) {
		if (b -> domains[b -> use] = strdup (domain)) {
			b -> use++;
			rc = true;
		}
	}
	return rc;
}/*}}}*/
static bool_t
bad_readfile (bad_t *b, const char *fname) /*{{{*/
{
	bool_t	rc;
	FILE	*fp;
	char	scratch[256];
	char	*ptr;
	
	rc = false;
	if (fp = fopen (fname, "r")) {
		rc = true;
		while (rc && fgets (scratch, sizeof (scratch) - 1, fp))
			if (ptr = strchr (scratch, '\n')) {
				*ptr = '\0';
				rc = bad_add (b, scratch);
			} else
				rc = false;
		fclose (fp);
	}
	return rc;
}/*}}}*/
static bool_t
bad_match (bad_t *b, const buffer_t *email) /*{{{*/
{
	bool_t		rc;
	
	rc = false;
	if (email) {
		int		len = buffer_length (email);
		const byte_t	*ptr = buffer_content (email);
		int		n;
		
		while ((len > 0) && (*ptr != '@')) {
			n = xmlCharLength (*ptr);
			len -= n;
			ptr += n;
		}
		if (len > 0) {
			++ptr;
			--len;
			for (n = 0; n < b -> use; ++n)
				if ((! strncasecmp (b -> domains[n], (const char *) ptr, len)) && (! b -> domains[n][len])) {
					rc = true;
					break;
				}
		}
	}
	return rc;
}/*}}}*/

struct sendmail { /*{{{*/
	spool_t	*	spool;		/* spool directory		*/
	unsigned long	nr;		/* an incremental counter	*/
	char		**inject;	/* alt: command to inject mail	*/
	int		ipos_sender,	/* position to set sender ..	*/
			ipos_recipient;	/* .. and recipient		*/
	action_t	*act;		/* optional actions to start	*/
	qflush_t	*flush;		/* optional queue flushing	*/
	bad_t	*	bad;		/* optional bad domain list	*/
	/*}}}*/
};

static sendmail_t *
sendmail_alloc (void) /*{{{*/
{
	sendmail_t	*s;
	
	if (s = (sendmail_t *) malloc (sizeof (sendmail_t))) {
		s -> spool = NULL;
		s -> nr = 0;
		s -> inject = NULL;
		s -> ipos_sender = -1;
		s -> ipos_recipient = -1;
		s -> act = NULL;
		s -> flush = NULL;
		s -> bad = NULL;
	}
	return s;
}/*}}}*/
static sendmail_t *
sendmail_free (sendmail_t *s) /*{{{*/
{
	if (s) {
		if (s -> spool)
			spool_free (s -> spool);
		if (s -> inject) {
			int	n;
			
			for (n = 0; s -> inject[n]; ++n)
				free (s -> inject[n]);
			free (s -> inject);
		}
		if (s -> act)
			action_free_all (s -> act);
		if (s -> flush)
			qflush_free (s -> flush);
		if (s -> bad)
			bad_free (s -> bad);
		free (s);
	}
	return NULL;
}/*}}}*/

static bool_t
sendmail_oinit (sendmail_t *s, blockmail_t *blockmail, var_t *opt) /*{{{*/
{
	bool_t	st;

	st = true;
	if (var_partial_imatch (opt, "path")) {
# define	SD_NONE		0
# define	SD_QF		(1 << 0)
# define	SD_DF		(1 << 1)
# define	SD_XF		(1 << 2)
# define	SD_ALL		(SD_QF | SD_DF | SD_XF)		
		int		subdirs;
		DIR		*dp;
		struct dirent	*ent;
		
		if (s -> spool)
			spool_free (s -> spool);
		subdirs = SD_NONE;
		if (dp = opendir (opt -> val)) {
			while ((ent = readdir (dp)) && (subdirs != SD_ALL))
				if ((! (subdirs & SD_QF)) && (! strcmp (ent -> d_name, "qf")))
					subdirs |= SD_QF;
				else if ((! (subdirs & SD_DF)) && (! strcmp (ent -> d_name, "df")))
					subdirs |= SD_DF;
				else if ((! (subdirs & SD_XF)) && (! strcmp (ent -> d_name, "xf")))
					subdirs |= SD_XF;
			closedir (dp);
		}
		if (! (s -> spool = spool_alloc (opt -> val, (subdirs == SD_ALL ? true : false))))
			st = false;
# undef		SD_NONE
# undef		SD_QF
# undef		SD_DF
# undef		SD_XF
# undef		SD_ALL
	} else if (var_partial_imatch (opt, "inject-command")) {
		int		isize, iuse;
		char		quote;
		const char	*ptr, *start, *end;
		
		isize = 0;
		iuse = 0;
		ptr = opt -> val;
		while (*ptr) {
			while (isspace (*ptr))
				++ptr;
			if ((*ptr == '"') || (*ptr == '\'')) {
				quote = *ptr++;
				start = ptr;
				while (*ptr && (*ptr != '"'))
					++ptr;
				end = ptr;
				if (*ptr == quote)
					++ptr;
			} else {
				start = ptr;
				while (*ptr && (! isspace (*ptr)))
					++ptr;
				end = ptr;
			}
			if (start < end) {
				if (iuse >= isize) {
					isize += 16;
					if (! (s -> inject = (char **) realloc (s -> inject, sizeof (char *) * (isize + 1)))) {
						while (iuse > 0)
							free (s -> inject[--iuse]);
						free (s -> inject);
						s -> inject = NULL;
						break;
					}
				}
# define	SENDER		"%(sender)"
# define	RECIPIENT	"%(recipient)"
# define	match(ppp)	((sizeof (ppp) - 1 == end - start) && (! strncmp (start, ppp, end - start)))
				if ((s -> ipos_sender == -1) && match (SENDER)) {
					s -> inject[iuse] = NULL;
					s -> ipos_sender = iuse++;
				} else if ((s -> ipos_recipient == -1) && match (RECIPIENT)) {
					s -> inject[iuse] = NULL;
					s -> ipos_recipient = iuse++;
				} else if (s -> inject[iuse] = malloc (end - start + 1)) {
					strncpy (s -> inject[iuse], start, end - start);
					s -> inject[iuse][end - start] = '\0';
					++iuse;
				}
# undef		match
# undef		RECIPIENT
# undef		SENDER
			}
		}
		if (s -> inject && iuse) {
			s -> inject[iuse] = NULL;
		}
	} else if (var_partial_imatch (opt, "action")) {
		int		interval;
		const char	*ptr;
		action_t	*temp, *prev;

		for (temp = s -> act, prev = NULL; temp; temp = temp -> next)
			prev = temp;
		interval = 0;
		for (ptr = opt -> val; isdigit ((int) ((unsigned char) *ptr)); ++ptr)
			;
		if (*ptr == ':') {
			++ptr;
			interval = atoi (opt -> val);
		} else
			ptr = opt -> val;
		if (temp = action_alloc (interval, ptr)) {
			if (prev)
				prev -> next = temp;
			else
				s -> act = temp;
		} else
			st = false;
	} else if (var_partial_imatch (opt, "queue-flush")) {
		if (s -> flush || (s -> flush = qflush_alloc (0, NULL)))
			s -> flush -> maxcount = atoi (opt -> val);
		else
			st = false;
	} else if (var_partial_imatch (opt, "queue-flush-command")) {
		if (s -> flush || (s -> flush = qflush_alloc (0, NULL)))
			st = qflush_set_command (s -> flush, opt -> val);
		else
			st = false;
	} else if (var_partial_imatch (opt, "bad-path")) {
		if (s -> bad)
			bad_free (s -> bad);
		s -> bad = bad_alloc (opt -> val);
	} else if (var_partial_imatch (opt, "bad-file")) {
		if (s -> bad)
			bad_readfile (s -> bad, opt -> val);
	} else
		log_out (blockmail -> lg, LV_WARNING, "Unknown option \"%s\" using \"%s\"", opt -> var, opt -> val);
	return st;
}/*}}}*/
static bool_t
sendmail_osanity (sendmail_t *s, blockmail_t *blockmail) /*{{{*/
{
	bool_t	st;
	
	st = true;
	if (s -> spool || (! s -> inject)) {
		if (! s -> spool)
			if (! (s -> spool = spool_alloc (DEF_DESTDIR, false)))
				st = false;
		if (st)
			spool_setprefix (s -> spool, "?f");
		if (st && s -> flush && (! s -> flush -> cmd))
			st = qflush_set_command (s -> flush, DEF_FLUSHCMD);
		if (st) {
			action_t	*run;
		
			for (run = s -> act; st && run; run = run -> next)
				st = action_go (run, blockmail -> lg, "start", s -> spool -> dir, "0", NULL);
		}
		if (st && s -> bad)
			spool_setprefix (s -> bad -> spool, "?f");
	}
	return st;
}/*}}}*/
static bool_t
sendmail_odeinit (sendmail_t *s, gen_t *g, blockmail_t *blockmail, bool_t success) /*{{{*/
{
	bool_t	st;
	
	st = true;
	if (s && s -> spool) {
		action_t	*run;
		char		count[32];
		char		instance[32];
		
		sprintf (count, "%lu", s -> nr);
		for (run = s -> act; run; run = run -> next) {
			if ((run -> interval > 0) && ((s -> nr % run -> interval) != 0)) {
				run -> instance++;
				sprintf (instance, "%d", run -> instance);
				if (! action_go (run, blockmail -> lg, "run", s -> spool -> dir, count, instance, NULL))
					st = false;
			}
			if (! action_go (run, blockmail -> lg, "stop", s -> spool -> dir, count, NULL))
				st = false;
		}
		if (s -> flush && s -> flush -> dcnt && (! s -> spool -> devnull))
			qflush_flush (s -> flush, blockmail -> lg, s -> spool -> dir);
	}
	return st;
}/*}}}*/
static bool_t
sendmail_write_spoolfile (blockmail_t *blockmail, sendmail_t *s, spool_t *spool, bool_t istemp, int customer_id, buffer_t *head, const char *nl, int nllen) /*{{{*/
{
	bool_t	st = false;
	
	s -> nr++;
	if (istemp)
		sprintf (spool -> fptr, "%08lx", s -> nr);
	else if (customer_id == 0)
		sprintf (spool -> fptr, "F%07lX", s -> nr);
	else
		sprintf (spool -> fptr, "%08X", customer_id);
	if (spool -> dptr)
		spool -> dptr[0] = 'd';
	spool -> ptr[0] = 'd';
	if (! spool_unique (spool))
		log_out (blockmail -> lg, LV_ERROR, "Unable to create unique file %s (%m)", spool -> ptr);
	else if (! spool_write (spool, blockmail -> body, nl, nllen))
		log_out (blockmail -> lg, LV_ERROR, "Unable to write data file %s (%m)", spool -> ptr);
	else if (! spool_write_temp (spool, head, nl, nllen))
		log_out (blockmail -> lg, LV_ERROR, "Unable to write control file %s (%m)", spool -> temp);
	else {
		if (spool -> dptr)
			spool -> dptr[0] = 'q';
		spool -> ptr[0] = 'q';
		if (! spool_validate (spool))
			log_out (blockmail -> lg, LV_WARNING, "Unable to rename temp.file %s to %s (%m)", spool -> temp, spool -> ptr);
		else
			st = true;
	}
	return st;
}/*}}}*/
static bool_t
sendmail_owrite_spool (sendmail_t *s, gen_t *g, blockmail_t *blockmail, receiver_t *rec, const char *nl, int nllen) /*{{{*/
{
	bool_t		st;
	spool_t		*spool;
	const buffer_t	*to_email;
	buffer_t	*bcc_head;
	
	if (s -> nr == 0) {
		if (g -> istemp) {
			spool_tmpprefix (s -> spool);
			if (s -> bad)
				s -> bad = bad_free (s -> bad);
		} else {
			char	prefix[64];

			sprintf (prefix, "%06X%03X", blockmail -> mailing_id, blockmail -> licence_id);
			if (s -> flush)
				qflush_set_idpattern (s -> flush, prefix);
			spool_addprefix (s -> spool, prefix);
			if (s -> bad)
				spool_addprefix (s -> bad -> spool, prefix);
		}
	}
	spool = s -> spool;
	to_email = media_target_find (rec -> media_target, "email");
	if (s -> bad && bad_match (s -> bad, to_email))
		spool = s -> bad -> spool;
	if (! spool -> devnull) {
		st = sendmail_write_spoolfile (blockmail, s, spool, g -> istemp, rec -> customer_id, blockmail -> head, nl, nllen);
		if (st && rec -> bcc && (bcc_head = buffer_alloc (buffer_length (blockmail -> head) + 1024))) {
			int	n;

			for (n = 0; rec -> bcc[n]; ++n)
				if (! create_bcc_head (bcc_head, blockmail, rec -> bcc[n], n))
					log_out (blockmail -> lg, LV_ERROR, "Unable to create temp. bcc header for %s", rec -> bcc[n]);
				else {
					sendmail_write_spoolfile (blockmail, s, spool, false, 0, bcc_head, nl, nllen);
				}
			buffer_free (bcc_head);
		}
	} else
		st = true;
	if (st) {
		action_t	*run;
		char		count[32];
		char		instance[32];

		sprintf (count, "%lu", s -> nr);
		for (run = s -> act; run && st; run = run -> next)
			if ((run -> interval > 0) && ((s -> nr % run -> interval) == 0)) {
				run -> instance++;
				sprintf (instance, "%d", run -> instance);
				if (! (st = action_go (run, blockmail -> lg, "run", s -> spool -> dir, count, instance, NULL)))
					log_out (blockmail -> lg, LV_ERROR, "Failed in executing %s", run -> cmd);
			}
	}
	if (s -> flush && to_email && (spool == s -> spool)) {
		int		len = buffer_length (to_email);
		const byte_t	*cont = buffer_content (to_email);
		char		*domain;
		
		if ((len > 0) && (domain = malloc (len + 1))) {
			int	n, m, clen;
			bool_t	start;
			
			for (n = 0, m = 0, start = 0; n < len; ) {
				clen = xmlCharLength (cont[n]);
				if (clen == 1)
					if (! start) {
						if (cont[n] == '@')
							start = true;
					} else
						domain[m++] = tolower (cont[n]);
				n += clen;
			}
			if (m > 0) {
				domain[m] = '\0';
				qflush_add (s -> flush, domain);
			}
			free (domain);
		}
	}
	return st;
}/*}}}*/
static bool_t
sendmail_inject_mail (blockmail_t *blockmail, sendmail_t *s, gen_t *g, receiver_t *rec, buffer_t *header, const char *nl, int nllen) /*{{{*/
{
	bool_t	st;
	csig_t	*csig;
	int	fds[2];
	pid_t	pid;
	
	st = false;
	csig = csig_alloc (SIGPIPE, SIG_IGN, SIGHUP, SIG_IGN, -1);
	if (pipe (fds) != -1) {
		if ((pid = fork ()) == 0) {
			int	nfd;
			
			close (fds[1]);
			close (0);
			nfd = dup (fds[0]);
			close (fds[0]);
			if (nfd == 0) {
				char		*sender = NULL, *recipient = NULL;
				int		hlen = buffer_length (header);
				const byte_t	*head = buffer_content (header);
				int		pos = 0;
				
				while ((! (sender && recipient)) && (pos < hlen)) {
					if (((head[pos] == 'S') || (head[pos] == 'R')) && (pos + 2 < hlen)) {
						char		**target = head[pos] == 'S' ? & sender : & recipient;
						const xmlChar	*ptr;
						int		len;
						
						if (head[++pos] == '<')
							++pos;
						ptr = head + pos;
						len = 0;
						while (pos < hlen && (head[pos] != '>') && (head[pos] != '\r') && (head[pos] != '\n'))
							++pos, ++len;
						if (*target = malloc (len + 1)) {
							memcpy (*target, ptr, len);
							(*target)[len] = '\0';
						}
					}
					while ((pos < hlen) && (head[pos] != '\n'))
						++pos;
					if (pos < hlen)
						++pos;
				}
				if (sender && recipient) {
					if (s -> ipos_sender != -1)
						s -> inject[s -> ipos_sender] = sender;
					if (s -> ipos_recipient != -1)
						s -> inject[s -> ipos_recipient] = recipient;
					if (s -> inject[0][0] == '/')
						execv (s -> inject[0], s -> inject);
					else
						execvp (s -> inject[0], s -> inject);
					log_out (blockmail -> lg, LV_ERROR, "Failed to start injection program %s: %m", s -> inject[0]);
				} else
					log_out (blockmail -> lg, LV_ERROR, "Failed to determinate sender or recipient");
			} else
				log_out (blockmail -> lg, LV_ERROR, "Failed to dup %d to 0", fds[1]);
			_exit (127);
		}
		close (fds[0]);
		if (pid > 0) {
			pid_t		npid;
			int		status;
			buffer_t	*flatten;

			if (flatten = buffer_alloc (buffer_length (header))) {
				st = flatten_header (flatten, header);
				if (st) {
					st = write_content (fds[1], buffer_content (flatten), buffer_length (flatten), nl, nllen);
					if (! st)
						log_out (blockmail -> lg, LV_ERROR, "Failed to write header: %m");
				}
				buffer_free (flatten);
			}
			if (st) {
				st = write_content (fds[1], buffer_content (blockmail -> body), buffer_length (blockmail -> body), nl, nllen);
				if (! st)
					log_out (blockmail -> lg, LV_ERROR, "Failed to write body: %m");
			}
			close (fds[1]);
			while (((npid = waitpid (pid, & status, 0)) != pid) && (npid != -1))
				;
			if (npid != pid) {
				log_out (blockmail -> lg, LV_ERROR, "Waited for pid %d, but got %d", pid, npid);
				st = false;
			} else if (status != 0) {
				if (WIFEXITED (status)) {
					int	exit_status = WEXITSTATUS (status);
					
					log_out (blockmail -> lg, LV_ERROR, "Inject processes return with exit code %d", exit_status);
					if (g)
						switch (exit_status) {
						case EX_TEMPFAIL:
							st = write_bounce_log (g, blockmail, rec, "4.9.9", "inject=tempfail");
							break;
						case EX_UNAVAILABLE:
							st = write_bounce_log (g, blockmail, rec, "4.9.9", "inject=service unavailable");
							break;
						default:
							if ((exit_status >= EX__BASE) && (exit_status <= EX__MAX)) {
								char	reason[128];
								
								snprintf (reason, sizeof (reason) - 1, "inject=exit %d", exit_status);
								st = write_bounce_log (g, blockmail, rec, "4.9.9", reason);
							} else
								st = false;
							break;
						}
				} else {
					if (WIFSIGNALED (status))
						log_out (blockmail -> lg, LV_ERROR, "Inject processes return due to signal %d", WTERMSIG (status));
					else
						log_out (blockmail -> lg, LV_ERROR, "Inject processes return due to status %d", status);
					st = false;
				}
			}
		} else {
			log_out (blockmail -> lg, LV_ERROR, "Failed to fork for inject %m");
			close (fds[1]);
		}
	} else
		log_out (blockmail -> lg, LV_ERROR, "Failed to create pipe for inject %m");
	csig_free (csig);
	return st;
}/*}}}*/
static bool_t
sendmail_owrite_inject (sendmail_t *s, gen_t *g, blockmail_t *blockmail, receiver_t *rec, const char *nl, int nllen) /*{{{*/
{
	bool_t		st;
	buffer_t	*bcc_head;
	
	st = sendmail_inject_mail (blockmail, s, g, rec, blockmail -> head, nl, nllen);
	if (st && rec -> bcc && (bcc_head = buffer_alloc (buffer_length (blockmail -> head) + 1024))) {
		int	n;

		for (n = 0; rec -> bcc[n]; ++n)
			if (! create_bcc_head (bcc_head, blockmail, rec -> bcc[n], n))
				log_out (blockmail -> lg, LV_ERROR, "Unable to create temp. bcc header for %s", rec -> bcc[n]);
			else
				sendmail_inject_mail (blockmail, s, NULL, NULL, bcc_head, nl, nllen);
		buffer_free (bcc_head);
	}
	return st;
}/*}}}*/
static bool_t
sendmail_owrite (sendmail_t *s, gen_t *g, blockmail_t *blockmail, receiver_t *rec) /*{{{*/
{
	const char	*nl = NULL;
	int		nllen = 0;
		
	if (blockmail -> usecrlf) {
		nl = "\r\n";
		nllen = 2;
	}
	if ((s -> nr == 0) && blockmail -> mfrom) {
		fsdb_t	*fsdb;
		char	key[256];

		if ((snprintf (key, sizeof (key) - 1, "envelope:%d:%d", blockmail -> licence_id, blockmail -> mailing_id) != -1) && (fsdb = fsdb_alloc (NULL))) {
			if (fsdb_put (fsdb, key, blockmail -> mfrom, strlen (blockmail -> mfrom)))
				log_out (blockmail -> lg, LV_DEBUG, "Put \"%s\" to \"%s\"", blockmail -> mfrom, key);
			else
				log_out (blockmail -> lg, LV_ERROR, "Failed to put \"%s\" to \"%s\"", blockmail -> mfrom, key);
			fsdb_free (fsdb);
		}		
	}
	if (s -> spool)
		return sendmail_owrite_spool (s, g, blockmail, rec, nl, nllen);
	else
		return sendmail_owrite_inject (s, g, blockmail, rec, nl, nllen);
}/*}}}*/


void *
generate_oinit (blockmail_t *blockmail, var_t *opts) /*{{{*/
{
	gen_t	*g;
	
	if (g = (gen_t *) malloc (sizeof (gen_t))) {
		g -> istemp = false;
		g -> acclog = NULL;
		g -> bnclog = NULL;
		g -> midlog = NULL;
		g -> tracklog = NULL;
		g -> s = sendmail_alloc ();
		if (g -> s)
		{
			bool_t	st = true;
			char	media = '\0';
			var_t	*tmp;
			
			for (tmp = opts; st && tmp; tmp = tmp -> next)
				if ((! tmp -> var) || var_partial_imatch (tmp, "media")) {
					if (! strcasecmp (tmp -> val, "email"))
						media = 's';
					else {
						log_out (blockmail -> lg, LV_ERROR, "Unknown media %s", tmp -> val);
						st = false;
					}
				} else if (var_partial_imatch (tmp, "temporary")) {
					g -> istemp = boolean (tmp -> val);
				} else if (var_partial_imatch (tmp, "account-logfile")) {
					st = struse (& g -> acclog, tmp -> val);
				} else if (var_partial_imatch (tmp, "bounce-logfile")) {
					st = struse (& g -> bnclog, tmp -> val);
				} else if (var_partial_imatch (tmp, "messageid-logfile")) {
					st = struse (& g -> midlog, tmp -> val);
				} else if (var_partial_imatch (tmp, "mailtrack-logfile")) {
					st = struse (& g -> tracklog, tmp -> val);
				} else {
					switch (media) {
					default:
						log_out (blockmail -> lg, LV_ERROR, "Unknown option %s and no media type enabled", tmp -> var);
						st = false;
						break;
					case 's':
						st = sendmail_oinit (g -> s, blockmail, tmp);
						break;
					}
				}
			if ((! st) ||
			    (! sendmail_osanity (g -> s, blockmail))
			   ) {
				generate_odeinit (g, blockmail, false);
				g = NULL;
			}
		} else {
			generate_odeinit (g, blockmail, false);
			g = NULL;
		}
	}
	return g;
}/*}}}*/
bool_t
generate_odeinit (void *data, blockmail_t *blockmail, bool_t success) /*{{{*/
{
	gen_t	*g = (gen_t *) data;
	bool_t	st = true;
	
	if (g) {
		if ((g -> s && (! sendmail_odeinit (g -> s, g, blockmail, success)))
		   )
			st = false;
		if (st && success && blockmail -> counter) {
			counter_t	*crun;
			int		fd;
			int		len;
			char		ts[64];
			char		scratch[4096];
			
			if (g -> acclog)  {
				time_t		now;
				struct tm	*tt;
					
				time (& now);
				if (tt = localtime (& now))
					snprintf (ts, sizeof (ts), "%04d-%02d-%02d:%02d:%02d:%02d",
						  tt -> tm_year + 1900, tt -> tm_mon + 1, tt -> tm_mday,
						  tt -> tm_hour, tt -> tm_min, tt -> tm_sec);
				else
					ts[0] = '\0';
			}
			log_suspend_push (blockmail -> lg, ~LS_LOGFILE, false);
			for (crun = blockmail -> counter; crun; crun = crun -> next) {
				if ((! crun -> unitcount) && (! crun -> unitskip))
					continue;

# define	FORMAT(s1,s2)	s1 "%d;%d;%d;%d;%d;%c;%d;%s;%d;%ld;%ld;%ld;%lld;%ld;%lld" s2,	\
				blockmail -> licence_id,					\
				blockmail -> company_id, blockmail -> mailinglist_id,		\
				blockmail -> mailing_id, blockmail -> maildrop_status_id,	\
				blockmail -> status_field, blockmail -> blocknr, 		\
				crun -> mediatype, crun -> subtype, 				\
				crun -> unitcount, crun -> unitskip, crun -> chunkcount,		\
				crun -> bytecount,						\
				crun -> bccunitcount, crun -> bccbytecount
# define	WHAT		FORMAT ("mail creation: ", "")

				log_out (blockmail -> lg, LV_NOTICE, WHAT);
				if (g -> acclog) {
					len = snprintf (scratch, sizeof (scratch) - 1,
							"id=%d\t"
							"licence=%d\t"
							"owner=%d\t"
							"company=%d\t"
							"mailinglist=%d\t"
							"mailing=%d\t"
							"maildrop=%d\t"
							"status_field=%c\t"
							"block=%d\t"
							"mediatype=%s\t"
							"subtype=%d\t"
							"count=%ld\t"
							"skip=%ld\t"
							"chunks=%ld\t"
							"bytes=%lld\t"
							"bcc-count=%ld\t"
							"bcc-bytes=%lld\t"
							"mailer=%s\t"
							"timestamp=%s\n", getpid (),
							blockmail -> licence_id, blockmail -> owner_id,
							blockmail -> company_id, blockmail -> mailinglist_id,
							blockmail -> mailing_id, blockmail -> maildrop_status_id,
							blockmail -> status_field, blockmail -> blocknr,
							crun -> mediatype, crun -> subtype,
							crun -> unitcount, crun -> unitskip, crun -> chunkcount,
							crun -> bytecount,
							crun -> bccunitcount, crun -> bccbytecount,
							blockmail -> nodename, ts);
					if ((fd = open (g -> acclog, O_WRONLY | O_APPEND | O_CREAT, 0644)) == -1) {
						log_out (blockmail -> lg, LV_ERROR, "Unable to open separate accounting logfile %s (%m)", g -> acclog);
						free (g -> acclog);
						g -> acclog = NULL;
					} else {
						if (write (fd, scratch, len) != len)
							log_out (blockmail -> lg, LV_ERROR, "Unable to write to separate accounting logfile %s (%m)", g -> acclog);
						if (close (fd) == -1)
							log_out (blockmail -> lg, LV_ERROR, "Failed to close separate accounting logfile %s (%m)", g -> acclog);
					}
				}
# undef		WHAT
# undef		FORMAT
			}
			log_suspend_pop (blockmail -> lg);
		}
		if (st && success && blockmail -> mailtrack && g -> tracklog) {
			if (blockmail -> mailtrack -> count > 0) {
				buffer_t	*mt = blockmail -> mailtrack -> content;
				int		fd;
			
				buffer_appendch (mt, '\n');
				if ((fd = open (g -> tracklog, O_WRONLY | O_APPEND | O_CREAT, 0644)) == -1) {
					log_out (blockmail -> lg, LV_ERROR, "Unable to open separate mailtrack logfile %s (%m)", g -> tracklog);
				} else {
					if (write (fd, buffer_content (mt), buffer_length (mt)) != buffer_length (mt))
						log_out (blockmail -> lg, LV_ERROR, "Unable to write to separate mailtrack logfile %s (%m)", g -> tracklog);
					if (close (fd) == -1)
						log_out (blockmail -> lg, LV_ERROR, "Failed to close separate mailtrack logfile %s (%m)", g -> tracklog);
				}
			}
		}
		if (g -> acclog)
			free (g -> acclog);
		if (g -> bnclog)
			free (g -> bnclog);
		if (g -> midlog)
			free (g -> midlog);
		if (g -> tracklog)
			free (g -> tracklog);
		if (g -> s)
			sendmail_free (g -> s);
		free (g);
	}
	return st;
}/*}}}*/
bool_t
generate_owrite (void *data, blockmail_t *blockmail, receiver_t *rec) /*{{{*/
{
	gen_t	*g = (gen_t *) data;
	bool_t	st;

	if (g -> midlog && rec -> message_id) {
		FILE		*fp;
		int		midlen = xmlBufferLength (rec -> message_id);
		const xmlChar	*mid = xmlBufferContent (rec -> message_id);
		
		if (fp = fopen (g -> midlog, "a")) {
			fprintf (fp, "%d;%d;%d;%d;%d;%*.*s\n",
				 blockmail -> licence_id,
				 blockmail -> company_id,
				 blockmail -> mailinglist_id,
				 blockmail -> mailing_id,
				 rec -> customer_id,
				 midlen, midlen, (const char *) mid);
			fclose (fp);
		} else
			log_out (blockmail -> lg, LV_ERROR, "Failed to write to %s: %m", g -> midlog);
	}
	if (! blockmail -> active) {
		char	dsn[32];
		
		snprintf (dsn, sizeof (dsn) - 1, "1.%d.%d", blockmail -> reason, blockmail -> reason_detail);
		st = write_bounce_log (g, blockmail, rec, dsn, "skip=no document");
	} else if ((! rec -> media) || (rec -> media -> type == Mediatype_EMail)) {
		st = sendmail_owrite (g -> s, g, blockmail, rec);
	} else {
		st = false;
	}
	return st;
}/*}}}*/
