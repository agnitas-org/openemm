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
# include	<stdlib.h>
# include	<unistd.h>
# include	<fcntl.h>
# include	<ctype.h>
# include	<string.h>
# include	<signal.h>
# include	<dirent.h>
# include	<errno.h>
# include	<sys/ioctl.h>
# include	"agn.h"

# include	<sys/types.h>
# include	<sys/stat.h>
# include	<sys/wait.h>
# include	<regex.h>
# include	<pty.h>

# define	MTA_SENDMAIL		0
# define	MTA_POSTFIX		1

# define	SENDMAILBASE		"/etc/mail"
# define	RELAY_DOMAINS		SENDMAILBASE "/relay-domains"
# define	MAILERTABLE		SENDMAILBASE "/mailertable"
# define	ACCESS			SENDMAILBASE "/access"

# define	POSTFIXBASE		"/etc/postfix"
# define	MAIN_CF			POSTFIXBASE "/main.cf"
# define	KEY_QUEUE		"queue_directory"
# define	DFLT_QUEUE		"/var/spool/postfix"

# define	MAILLOG			"/var/log/maillog"
# define	BAVRC			"lib/bav.rc"

typedef struct { /*{{{*/
	char		*content;
	char		**split;
	int		count;
	/*}}}*/
}	split_t;

static split_t *
split_free (split_t *s) /*{{{*/
{
	if (s) {
		if (s -> split)
			free (s -> split);
		if (s -> content)
			free (s -> content);
		free (s);
	}
	return NULL;
}/*}}}*/
static split_t *
split_alloc (const char *content, int length) /*{{{*/
{
	split_t	*s;
	
	if (s = (split_t *) malloc (sizeof (split_t))) {
		s -> content = NULL;
		s -> split = NULL;
		s -> count = 0;
		if (s -> content = malloc (length + 1)) {
			int	size;
			char	*ptr;
			
			memcpy (s -> content, content, length);
			s -> content[length] = '\0';
			size = 0;
			for (ptr = s -> content; ptr; ) {
				if (s -> count >= size) {
					size += size ? size : 128;
					if (! (s -> split = (char **) realloc (s -> split, sizeof (char *) * (size + 1)))) {
						s  = split_free (s);
						break;
					}
				}
				s -> split[s -> count++] = ptr;
				if (ptr = strchr (ptr, '\n'))
					*ptr++ = '\0';
			}
		} else
			s = split_free (s);
	}
	return s;
}/*}}}*/

typedef struct { /*{{{*/
	char		*fname;
	buffer_t	*src;
	buffer_t	*dest;
	/*}}}*/
}	fmod_t;

static fmod_t *
fmod_free (fmod_t *fm) /*{{{*/
{
	if (fm) {
		if (fm -> fname)
			free (fm -> fname);
		if (fm -> src)
			buffer_free (fm -> src);
		if (fm -> dest)
			buffer_free (fm -> dest);
		free (fm);
	}
	return NULL;
}/*}}}*/
static fmod_t *
fmod_alloc (const char *fname) /*{{{*/
{
	fmod_t	*fm;
	
	if (fm = (fmod_t *) malloc (sizeof (fmod_t))) {
		fm -> fname = NULL;
		fm -> src = NULL;
		fm -> dest = NULL;
		if (fm -> fname = strdup (fname)) {
			bool_t		ok;
			int		fd;
			struct stat	st;
			
			ok = false;
			if ((fd = open (fname, O_RDONLY)) != -1) {
				if (fstat (fd, & st) != -1) {
					if (fm -> src = buffer_alloc (st.st_size + 1)) {
						if (read (fd, fm -> src -> buffer, st.st_size) == st.st_size) {
							fm -> src -> length = st.st_size;
							if (fm -> dest = buffer_alloc (st.st_size + 1)) {
								if (buffer_appendbuf (fm -> dest, fm -> src))
									ok = true;
							}
						}
					}
				}
				close (fd);
			} else if (errno == ENOENT) {
				if ((fd = open (fname, O_WRONLY | O_CREAT, 0644)) != -1) {
					close (fd);
				}
				ok = true;
			}
			if (! ok)
				fm = fmod_free (fm);
		} else
			fm = fmod_free (fm);
	}
	return fm;
}/*}}}*/
static split_t *
fmod_split (fmod_t *fm) /*{{{*/
{
	return split_alloc ((char *) fm -> src -> buffer, fm -> src -> length);
}/*}}}*/
static bool_t
fmod_changed (fmod_t *fm) /*{{{*/
{
	return (fm -> src -> length != fm -> dest -> length) || (fm -> src -> length && memcmp (fm -> src -> buffer, fm -> dest -> buffer, fm -> src -> length));
}/*}}}*/
static bool_t
fmod_flush (fmod_t *fm) /*{{{*/
{
	bool_t	ok;
	
	if (fmod_changed (fm)) {
		int	fd;
		
		ok = false;
		if ((fd = open (fm -> fname, O_WRONLY | O_CREAT | O_TRUNC, 0644)) != -1) {
			if ((! fm -> dest -> length) || (write (fd, fm -> dest -> buffer, fm -> dest -> length) == fm -> dest -> length))
				ok = true;
			close (fd);
		}
	} else
		ok = true;
	return ok;
}/*}}}*/

static char *
mailertableoption (fmod_t *fm) /*{{{*/
{
	char	*opt;
	split_t	*s;
	
	opt = NULL;
	if (s = fmod_split (fm)) {
		int	n;
		char	*ptr;
		
		for (n = 0; (! opt) && (n < s -> count); ++n) {
			ptr = skip (s -> split[n]);
			if (! strncmp (ptr, "procmail:", 9))
				opt = strdup (ptr);
		}
		split_free (s);
		if (! opt) {
			const char	*home;
			
			if ((home = getenv ("HOME")) && (opt = malloc (strlen (home) + 64)))
				sprintf (opt, "procmail:%s/lib/bav.rc", home);
		}
	}
	return opt;
}/*}}}*/
static bool_t
addline (fmod_t *fm, const char *line) /*{{{*/
{
	if (buffer_appends (fm -> dest, line) && buffer_appendch (fm -> dest, '\n'))
		return true;
	return false;
}/*}}}*/
static bool_t
addmtline (fmod_t *fm, const char *line, const char *opt) /*{{{*/
{
	if (buffer_appends (fm -> dest, line) &&
	    buffer_appendch (fm -> dest, '\t') &&
	    buffer_appends (fm -> dest, opt) &&
	    buffer_appendch (fm -> dest, '\n'))
		return true;
	return false;
}/*}}}*/

# define	SEPLINE		"\n# -=[ do not remove this line unless you know what you doing ]=-\n"
static char *
accessdump (fmod_t *fm) /*{{{*/
{
	int	pos, len;
	char	*rc;
	
	pos = buffer_indexsn (fm -> src, SEPLINE, sizeof (SEPLINE) - 1);
	if (pos == -1)
		len = 0;
	else {
		pos += sizeof (SEPLINE) - 1;
		len = buffer_length (fm -> src) - pos;
	}
	if (rc = malloc (len + 1)) {
		if (len > 0)
			memcpy (rc, fm -> src -> buffer + pos, len);
		rc[len] = '\0';
	}
	return rc;
}/*}}}*/
static void
accessappend (fmod_t *fm, char *content, bool_t trunc) /*{{{*/
{
	int	pos;
	regex_t	re;
	char	*ptr, *sav;
	
	pos = buffer_indexsn (fm -> src, SEPLINE, sizeof (SEPLINE) - 1);
	if (pos != -1) {
		pos += sizeof (SEPLINE) - 1;
		if (trunc)
			buffer_truncate (fm -> dest, pos);
	} else
		buffer_appendsn (fm -> dest, SEPLINE, sizeof (SEPLINE) - 1);
	regcomp (& re, "^[0-9]+(\\.[0-9]+){3}$", REG_NOSUB | REG_EXTENDED);
	for (ptr = content; isspace (*ptr); ++ptr)
		;
	while (*ptr) {
		sav = ptr;
		ptr = skip (ptr);
		if (regexec (& re, sav, 0, NULL, 0) == 0)
			buffer_format (fm -> dest, "Connect:%s\t\tRELAY\n", sav);
	}
	regfree (& re);
}/*}}}*/
static void
detach (void) /*{{{*/
{
	int	master, slave;

	setuid (geteuid ());
	setgid (getegid ());
	setsid ();
	if ((tcgetpgrp (0) == -1) && (errno == ENOTTY)) {
		if (openpty (& master, & slave, NULL, NULL, NULL) != -1) {
			ioctl (slave, TIOCSCTTY, 0);
		}
	}
	csig_alloc (SIGINT, SIG_IGN, SIGHUP, SIG_IGN, SIGCHLD, SIG_DFL, NULL);
}/*}}}*/
static bool_t
make (void) /*{{{*/
{
	pid_t	pid;
	bool_t	ok;
	
	ok = false;
	switch (pid = fork ()) {
	case 0:
		{
			int	fd, n;
			
			detach ();
			if ((fd = open (_PATH_DEVNULL, O_RDWR)) != -1) {
				for (n = 0; n < 3; ++n) {
					close (n);
					dup (fd);
				}
				close (fd);
			}
			execl ("/usr/bin/make", "make", "-C", SENDMAILBASE, NULL);
		}
		_exit (127);
	case -1:
		break;
	default:
		{
			int	st;
			
			if ((waitpid (pid, & st, 0) == pid) && (st == 0))
				ok = true;
		}
		break;
	}
	return ok;
} /*}}}*/

static char *
postfix_get_config (const char *key, const char *dflt) /*{{{*/
{
	char	*rc = NULL;
	FILE	*fp;
	
	if (fp = fopen (MAIN_CF, "r")) {
		int	klen = strlen (key);
		char	line[8192];
		char	*ptr, *temp;
		
		while (fgets (line, sizeof (line) - 1, fp)) {
			for (ptr = line; isspace (*ptr); ++ptr)
				;
			if ((! *ptr) || (*ptr == '#'))
				continue;
			for (temp = ptr; *ptr && (! isspace (*ptr)) && (*ptr != '='); ++ptr)
				;
			if (*ptr && (klen == ptr - temp) && (! strncmp (key, temp, klen))) {
				while (isspace (*ptr))
					++ptr;
				if (*ptr == '=') {
					++ptr;
					while (isspace (*ptr))
						++ptr;
					temp = ptr;
					if (ptr = strchr (ptr, '\n'))
						*ptr = '\0';
					struse (& rc, temp);
				}
			}
		}
	}
	return rc ? rc : strdup (dflt);
}/*}}}*/
			
static bool_t
match (const char *pidstr, char **pattern) /*{{{*/
{
	bool_t	rc;
	char	path[128];
	int	fd;
	
	rc = false;
	sprintf (path, "/proc/%s/cmdline", pidstr);
	if ((fd = open (path, O_RDONLY)) != -1) {
		int	n;
		char	buf[4096];
		
		if ((n = read (fd, buf, sizeof (buf) - 1)) > 0) {
			buf[n] = '\0';
			while ((n > 0) && (! buf[n - 1]))
				--n;
			--n;
			while (n >= 0) {
				if (! buf[n])
					buf[n] = ' ';
				--n;
			}
			for (n = 0; pattern[n]; ++n)
				if (! strstr (buf, pattern[n]))
					break;
			if (! pattern[n])
				rc = true;
		}
		close (fd);
	}
	return rc;
}/*}}}*/
static void
validate (int mta) /*{{{*/
{
	const char	*home = path_home ();
	char		bavrc[PATH_MAX + 1];
	struct stat	st;
	
	snprintf (bavrc, sizeof (bavrc) - 1, "%s/%s", home, BAVRC);
	if (stat (bavrc, & st) != -1) {
		if (mta == MTA_SENDMAIL) {
			if ((st.st_uid != 0) || (st.st_gid != 0)) {
				chown (bavrc, 0, 0);
			}
			if ((st.st_mode & S_IFMT) != 0600) {
				chmod (bavrc, 0600);
			}
		} else if (mta == MTA_POSTFIX) {
			if ((st.st_mode & S_IFMT) != 0644) {
				chmod (bavrc, 0644);
			}
		}
	}
}/*}}}*/
static int
queuecount (const char *path) /*{{{*/
{
	int	rc;
	DIR	*dp;

	rc = 0;
	if (dp = opendir (path)) {
		int	plen = strlen (path);
		char	*sub = malloc (plen + PATH_MAX + 2);
		
		if (sub) {
			char		*ptr;
			struct dirent	*ent;
			struct stat	st;

			strncpy (sub, path, plen);
			ptr = sub + plen;
			*ptr++ = '/';
			while (ent = readdir (dp)) {
				if ((ent -> d_name[0] != '.') && (strlen (ent -> d_name) < PATH_MAX)) {
					strcpy (ptr, ent -> d_name);
					if ((stat (sub, & st) != -1) && S_ISDIR (st.st_mode)) {
						rc += queuecount (sub);
					} else {
						++rc;
					}
				}
			}
			free (sub);
		}
		closedir (dp);
	}
	return rc;
}/*}}}*/
int
main (int argc, char **argv) /*{{{*/
{
	int		rc;
	int		mta;
	const char	*temp;
	
	if ((temp = getenv ("MTA")) && (! strcmp (temp, "postfix"))) {
		mta = MTA_POSTFIX;
	} else {
		mta = MTA_SENDMAIL;
	}
	
	if ((argc <= 1) || ((argc == 2) && (! strcasecmp (argv[1], "help")))) {
		const char	*mta_name;

		switch (mta) {
		case MTA_SENDMAIL:	mta_name = "sendmail";		break;
		case MTA_POSTFIX:	mta_name = "postfix";		break;
		default:		mta_name = "*unknown*";		break;
		}
		fprintf (stderr, "Usage: %s [stop [<pattern>] | service <option> | <mta-options>] (sendmail only)\n", argv[0]);
		fprintf (stderr, "       %s add <domain(s)> (sendmail only)\n", argv[0]);
		fprintf (stderr, "       %s access [dump|load|add] <filename> (sendmail only)\n", argv[0]);
		fprintf (stderr, "       %s queuesize (postfix only)\n", argv[0]);
		fprintf (stderr, "       %s logaccess\n", argv[0]);
		fprintf (stderr, "       using %s as mta\n", mta_name);
		return 0;
	}
	rc = 0;
	if (! strcasecmp (argv[1], "stop")) {
		if (mta == MTA_SENDMAIL) {
			int	n;
		
			for (n = 0; (n < 2) && (! rc); ++n) {
				int	sig;
				int	count;
				DIR	*dir;

				sig = n == 0 ? SIGTERM : SIGKILL;
				count = 0;
				if (dir = opendir ("/proc")) {
					struct dirent	*ent;

					while (ent = readdir (dir)) {
						char	*ptr;

						for (ptr = ent -> d_name; isdigit (*ptr); ++ptr)
							;
						if (! *ptr) {
							char	path[128];
							char	buf[512];
							FILE	*fp;
						
							sprintf (path, "/proc/%s/status", ent -> d_name);
							if (fp = fopen (path, "r")) {
								if ((fgets (buf, sizeof (buf) - 1, fp)) &&
								    (ptr = strchr (buf, '\n'))) {
									*ptr = '\0';
									if (! strcmp (skip (buf), "sendmail")) {
										int	pid;
									
										if ((argc == 2) || match (ent -> d_name, argv + 2)) {
											pid = atoi (ent -> d_name);
											printf (" -%d:%d", sig, pid);
											fflush (stdout);
											if (kill (pid, sig) == -1) {
												printf ("[failed %m]");
												fflush (stdout);
											} else
												++count;
										}
									}
								}
								fclose (fp);
							}
						}
					}
					closedir (dir);
				}
				if ((n == 0) && count)
					sleep (2);
			}
		}
	} else if (! strcasecmp (argv[1], "add")) {
		if (mta == MTA_SENDMAIL) {
			if (argc > 2) {
				fmod_t	*fm1 = fmod_alloc (RELAY_DOMAINS);
				fmod_t	*fm2 = fmod_alloc (MAILERTABLE);
				char	*mtopt;
				int	n;

				rc = 1;
				if (fm1 && fm2) {
					if (mtopt = mailertableoption (fm2)) {
						for (n = 2; n < argc; ++n) {
							addline (fm1, argv[n]);
							addmtline (fm2, argv[n], mtopt);
						}
						free (mtopt);
					}
					if (fmod_flush (fm1) && fmod_flush (fm2) && make ())
						rc = 0;
				}
				if (fm1)
					fmod_free (fm1);
				if (fm2)
					fmod_free (fm2);
			} else
				rc = 0;
		}
	} else if (! strcasecmp (argv[1], "access")) {
		if (mta == MTA_SENDMAIL) {
			rc = 1;
			if (argc == 4) {
				const char	*cmd = argv[2];
				const char	*fname = argv[3];
				fmod_t		*facc = fmod_alloc (ACCESS);
				FILE		*fp;
			
				if (facc) {
					if (! strcmp (cmd, "dump")) {
						char	*content;
					
						if (content = accessdump (facc)) {
							if (fp = fopen (fname, "w")) {
								regex_t		re;
								regmatch_t	mtch[3];
								char		*ptr, *cur;
							
								regcomp (& re, "^Connect:([0-9]+(\\.[0-9]+){3})[\t]+RELAY$", REG_EXTENDED);
								for (ptr = content; ptr; ) {
									cur = ptr;
									if (ptr = strchr (ptr, '\n'))
										*ptr++ = '\0';
									if (regexec (& re, cur, sizeof (mtch) / sizeof (mtch[0]), mtch, 0) == 0) {
										fwrite (cur + mtch[1].rm_so, sizeof (char), mtch[1].rm_eo - mtch[1].rm_so, fp);
										fputc ('\n', fp);
									}
								}
								regfree (& re);
								rc = 0;
								fclose (fp);
							}
							free (content);
						}
					} else if ((! strcmp (cmd, "load")) || (! strcmp (cmd, "add"))) {
						char		*content;
						struct stat	st;
					
						if (fp = fopen (fname, "r")) {
							if (fstat (fileno (fp), & st) != -1) {
								if (content = malloc (st.st_size + 1)) {
									if (fread (content, sizeof (char), st.st_size, fp) == st.st_size) {
										content[st.st_size] = '\0';
										accessappend (facc, content, (! strcmp (cmd, "load")) ? true : false);
										rc = 0;
									}
									free (content);
								}
							}
							fclose (fp);
						}
						if ((rc == 0) && fmod_changed (facc) && ((! fmod_flush (facc)) || (! make ())))
							rc = 1;
					}
					fmod_free (facc);
				}
			}
		}
	} else if (! strcasecmp (argv[1], "queuesize")) {
		int		size = 0;
		
		if (mta == MTA_POSTFIX) {
			char	*spool;
			
			if (spool = postfix_get_config (KEY_QUEUE, DFLT_QUEUE)) {
				char		path[PATH_MAX + 1];
				char		*bptr;
				int		blen, elen;
				DIR		*bdp;
				struct dirent	*bent;
				struct stat	st;
				int		n;
				struct {
					const char	*name;
					int		nlen;
				}		use[] = {
					{	"active",	6	},
					/*
					{	"deferred",	8	},
					{	"hold",		4	},
					 */
					{	"incoming",	8	}
					/*
					,{	"maildrop",	8	}
					 */
				};

				strncpy (path, spool, sizeof (path) - 1);
				path[sizeof (path) - 1] = '\0';
				for (bptr = path; *bptr; ++bptr)
					;
				if (bptr - path < sizeof (path) - 1) {
					*bptr++ = '/';
					blen = bptr - path;
					if (bdp = opendir (spool)) {
						while (bent = readdir (bdp)) {
							elen = strlen (bent -> d_name);
							if ((bent -> d_name[0] != '.') && (blen + elen < sizeof (path) - 1)) {
								for (n = 0; n < sizeof (use) / sizeof (use[0]); ++n)
									if ((use[n].nlen == elen) && (! strncmp (use[n].name, bent -> d_name, use[n].nlen)))
										break;
								if (n < sizeof (use) / sizeof (use[0])) {
									strncpy (bptr, bent -> d_name, elen);
									bptr[elen] = '\0';
									if ((stat (path, & st) != -1) && S_ISDIR (st.st_mode)) {
										size += queuecount (path);
									}
								}
							}
						}
						closedir (bdp);
					}
				}
				free (spool);
			}
		}
		fprintf (stdout, "%d\n", size);
		fflush (stdout);
	} else if (! strcasecmp (argv[1], "logaccess")) {
		struct stat	st;
		mode_t		mask = S_IRGRP | S_IROTH;
		char		*path;
		
		rc = 1;
		if (stat (MAILLOG, & st) != -1) {
			if (S_ISREG (st.st_mode)) {
				if ((st.st_mode & mask) != mask) {
					if (chmod (MAILLOG, st.st_mode | mask) != -1)
						rc = 0;
					else
						fprintf (stderr, "Failed to chmod %s (%m)\n", MAILLOG);
				} else
					rc = 0;
			} else
				fprintf (stderr, "File %s is not a regular file\n", MAILLOG);
		} else
			fprintf (stderr, "Failed to stat %s (%m)\n", MAILLOG);
		if ((rc == 0) && (path = mkpath (path_home (), "var", "run", "slrtscan.save", NULL))) {
			if (stat (path, & st) != -1) {
				uid_t	uid = getuid ();
				gid_t	gid = getgid ();
				
				if ((st.st_uid != uid) || (st.st_gid != gid)) {
					if (chown (path, uid, gid) == -1) {
						fprintf (stderr, "Failed to chown %s (%m)\n", path);
						rc = 1;
					}
				}
			} else if (errno != ENOENT) {
				fprintf (stderr, "Failed to stat %s (%m)\n", path);
				rc = 1;
			}
			free (path);
		}
	} else {
		if ((argc == 3) && (! strcmp (argv[1], "service"))) {
			const char	*args[4];
			int		n;

			validate (mta);
# define	CMD_SERVICE		"/sbin/service"
# define	CMD_INIT		(mta == MTA_SENDMAIL ? "/etc/init.d/sendmail" : "/etc/init.d/postfix")
# define	CMD_SH			"/bin/sh"			
			if (access (CMD_SERVICE, X_OK) != -1) {
				args[0] = CMD_SERVICE;
				args[1] = mta == MTA_SENDMAIL ? "sendmail" : "postfix";
				n = 2;
			} else if (access (CMD_INIT, X_OK) != -1) {
				args[0] = CMD_INIT;
				n = 1;
			} else if ((access (CMD_INIT, F_OK) != 1) && (access (CMD_SH, X_OK) != -1)) {
				args[0] = CMD_SH;
				args[1] = CMD_INIT;
				n = 2;
			} else
				n = -1;
			if (strcmp (argv[2], "stop") && strcmp (argv[2], "start") && strcmp (argv[2], "restart") && strcmp (argv[2], "reload") && strcmp (argv[2], "status"))
				fprintf (stderr, "Unknown service command \"%s\"\n", argv[2]);
			else if (n < 0)
				fprintf (stderr, "Unknown system process starting.\n");
			else {
				detach ();
				args[n++] = argv[2];
				args[n] = NULL;
				execv (args[0], (char *const *) args);
			}
		} else {
			const char	*mtas[] = {
				"/usr/sbin/sendmail",
				"/usr/lib/sendmail",
				"sendmail"
			};
			int		n;
			
			detach ();
			for (n = 0; n < sizeof (mtas) / sizeof (mtas[0]); ++n) {
				argv[0] = (char *) mtas[n];
				if ((argv[0][0] == '/') && (access (argv[0], X_OK) != -1))
					break;
			}
			if (argv[0][0] == '/')
				execv (argv[0], argv);
			else
				execvp (argv[0], argv);
			fprintf (stderr, "Failed to start %s (%m)\n", argv[0]);
		}
		rc = 1;
	}
	return rc;
}/*}}}*/
