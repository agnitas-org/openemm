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
# include	<opendkim/dkim.h>
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
	return (! str) || atob (str);
}/*}}}*/
static bool_t
write_content (int fd, const byte_t *ptr, long len) /*{{{*/
{
	bool_t	st;
	int	n;
	
	st = true;
	while (len > 0)
		if ((n = write (fd, ptr, len)) > 0) {
			ptr += n;
			len -= n;
		} else {
			st = false;
			break;
		}
	return st;
}/*}}}*/
static bool_t
write_file (const char *fname, const buffer_t *content) /*{{{*/
{
	bool_t	st;
	int	fd;
	
	st = false;
	if ((fd = open (fname, O_WRONLY | O_CREAT | O_TRUNC, 0644)) != -1) {
		st = write (fd, content -> buffer, content -> length) == content -> length;
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
		if (fprintf (fp, "%s;%d;%d;%d;%d;%s%s\n", dsn, blockmail -> licence_id, blockmail -> mailing_id, (rec -> media ? rec -> media -> type : Mediatype_Unspec), rec -> customer_id, ts, reason) < 0) {
			st = false;
		}
		if (fclose (fp) == EOF) {
			st = false;
		}
	}
	return st;
}/*}}}*/
static header_t *
create_bcc_head (receiver_t *rec, blockmail_t *blockmail, const char *bcc, int nr) /*{{{*/
{
	header_t	*header;
	
	if (header = header_copy (rec -> header)) {
		bool_t		status;
		head_t		*head;
		head_t		*message_id, *to;
		buffer_t	*scratch;

		status = header_set_recipient (header, bcc, ! blockmail -> allow_unnormalized_emails);
		header_remove (header, "bcc");
		for (head = header -> head, message_id = to = NULL; status && head; head = head -> next)
			if ((! message_id) && head_matchn (head, "message-id", 10)) {
				message_id = head;
			} else if ((! to) && head_matchn (head, "to", 2)) {
				to = head;
			}
		if (status && message_id && (blockmail -> status_field != 'V')) {
			const char	*value = head_value (message_id);
		
			if (value && (scratch = header_scratch (header, buffer_length (message_id -> h)))) {
				buffer_format (scratch, "<V%d-", nr);
				if (*value == '<') 
					++value;
				buffer_appends (scratch, value);
				head_set_value (message_id, scratch);
			} else
				status = false;
		}
		if (status)
			if ((scratch = header_scratch (header, 1024)) && buffer_format (scratch, "Bcc: <%s>", bcc))
				status = header_insert (header, buffer_string (scratch), to);
			else
				status = false;
		if (! status)
			header = header_free (header);
	}
	return header;
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
spool_write (spool_t *s, buffer_t *content) /*{{{*/
{
	return s -> devnull ? true : write_file (s -> buf, content);
}/*}}}*/
static bool_t
spool_write_temp (spool_t *s, buffer_t *content) /*{{{*/
{
	return s -> devnull ? true : write_file (s -> temp, content);
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

struct sendmail { /*{{{*/
	spool_t	*	spool;		/* spool directory		*/
	unsigned long	nr;		/* an incremental counter	*/
	char		**inject;	/* alt: command to inject mail	*/
	int		ipos_sender,	/* position to set sender ..	*/
			ipos_recipient;	/* .. and recipient		*/
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
		if (! (s -> spool = spool_alloc (opt -> val, subdirs == SD_ALL)))
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
		if (s -> spool || (s -> spool = spool_alloc (DEF_DESTDIR, false)))
			spool_setprefix (s -> spool, "?f");
		else
			st = false;
	}
	return st;
}/*}}}*/
static bool_t
sendmail_write_spoolfile (blockmail_t *blockmail, sendmail_t *s, spool_t *spool, bool_t istemp, int customer_id, buffer_t *head) /*{{{*/
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
	else if (! spool_write (spool, blockmail -> body))
		log_out (blockmail -> lg, LV_ERROR, "Unable to write data file %s (%m)", spool -> ptr);
	else if (! spool_write_temp (spool, head))
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
sendmail_owrite_spool (sendmail_t *s, gen_t *g, blockmail_t *blockmail, receiver_t *rec) /*{{{*/
{
	bool_t		st;
	spool_t		*spool;
	
	if (s -> nr == 0) {
		if (g -> istemp) {
			spool_tmpprefix (s -> spool);
		} else {
			char	prefix[64];

			sprintf (prefix, "%06X%03X", blockmail -> mailing_id, blockmail -> licence_id);
			spool_addprefix (s -> spool, prefix);
		}
	}
	spool = s -> spool;
	if (! spool -> devnull) {
		header_t	*bcc_head;
		
		st = sendmail_write_spoolfile (blockmail, s, spool, g -> istemp, rec -> customer_id, header_create_sendmail_spoolfile_header (rec -> header));
		if (st && rec -> bcc) {
			int	n;

			for (n = 0; rec -> bcc[n]; ++n)
				if (bcc_head = create_bcc_head (rec, blockmail, rec -> bcc[n], n)) {
					if (rec -> dkim)
						sign_mail_using_dkim (blockmail, bcc_head);
					sendmail_write_spoolfile (blockmail, s, spool, false, 0, header_create_sendmail_spoolfile_header (bcc_head));
					header_free (bcc_head);
				} else
					log_out (blockmail -> lg, LV_ERROR, "Unable to create temp. bcc header for %s", rec -> bcc[n]);
		}
	} else
		st = true;
	return st;
}/*}}}*/
static bool_t
sendmail_inject_mail (blockmail_t *blockmail, sendmail_t *s, gen_t *g, receiver_t *rec, header_t *header) /*{{{*/
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
				if (header -> sender && header -> recipient) {
					if (s -> ipos_sender != -1)
						s -> inject[s -> ipos_sender] = (char *) buffer_string (header -> sender);
					if (s -> ipos_recipient != -1)
						s -> inject[s -> ipos_recipient] = (char *) buffer_string (header -> recipient);
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
			int		size;

			size = 0;
			if (flatten = header_create (header, false)) {
				st = write_content (fds[1], buffer_content (flatten), buffer_length (flatten));
				if (! st)
					log_out (blockmail -> lg, LV_ERROR, "Failed to write header: %m");
				else
					size = buffer_length (flatten);
			} else {
				log_out (blockmail -> lg, LV_ERROR, "Failed to flatten header: %m");
				st = false;
			}
			if (st) {
				st = write_content (fds[1], buffer_content (blockmail -> body), buffer_length (blockmail -> body));
				if (! st)
					log_out (blockmail -> lg, LV_ERROR, "Failed to write body: %m");
				else if (rec)
					rec -> size = size + buffer_length (blockmail -> body);
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
							}
							break;
						}
					if (exit_status != EX_OK)
						st = false;
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
sendmail_owrite_inject (sendmail_t *s, gen_t *g, blockmail_t *blockmail, receiver_t *rec) /*{{{*/
{
	bool_t		st;
	header_t	*bcc_head;
	
	st = sendmail_inject_mail (blockmail, s, g, rec, rec -> header);
	if (st && rec -> bcc) {
		int	n;

		for (n = 0; rec -> bcc[n]; ++n)
			if (bcc_head = create_bcc_head (rec, blockmail, rec -> bcc[n], n)) {
				if (rec -> dkim)
					sign_mail_using_dkim (blockmail, bcc_head);
				sendmail_inject_mail (blockmail, s, NULL, NULL, bcc_head);
				header_free (bcc_head);
			} else
				log_out (blockmail -> lg, LV_ERROR, "Unable to create temp. bcc header for %s", rec -> bcc[n]);
	}
	return st;
}/*}}}*/
static bool_t
sendmail_owrite (sendmail_t *s, gen_t *g, blockmail_t *blockmail, receiver_t *rec) /*{{{*/
{
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
		return sendmail_owrite_spool (s, g, blockmail, rec);
	else
		return sendmail_owrite_inject (s, g, blockmail, rec);
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
		if (success && blockmail -> counter) {
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
		char		dsn[32];
		char		*custom;
		const char	*reason;
		
		snprintf (dsn, sizeof (dsn) - 1, "1.%d.%d", blockmail -> reason, blockmail -> reason_detail);
		custom = NULL;
		switch (blockmail -> reason) {
		case REASON_UNSPEC:		reason = "skip=unspec reason";		break;
		case REASON_NO_MEDIA:		reason = "skip=no media";		break;
		case REASON_EMPTY_DOCUMENT:	reason = "skip=no document";		break;
		case REASON_UNMATCHED_MEDIA:	reason = "skip=unmatched media";	break;
		case REASON_CUSTOM:
		default:
			if ((blockmail -> reason == REASON_CUSTOM) && blockmail -> reason_custom) {
				if (custom = malloc (strlen (blockmail -> reason_custom) + 6))
					sprintf (custom, "skip=%s", blockmail -> reason_custom);
			} else if (custom = malloc (32)) {
				sprintf (custom, "skip=reason %d", blockmail -> reason);
			}
			reason = custom;
			break;
		}
		st = write_bounce_log (g, blockmail, rec, dsn, reason ? reason : "skip=not specified");
		if (custom)
			free (custom);
	} else if (! rec -> media) {
		st = write_bounce_log (g, blockmail, rec, "1.0.0", "skip=missing media");
	} else if (rec -> media -> type == Mediatype_EMail) {
		st = sendmail_owrite (g -> s, g, blockmail, rec);
	} else {
		st = false;
	}
	return st;
}/*}}}*/
