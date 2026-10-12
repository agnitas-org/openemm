/********************************************************************************************************************************************************************************************************************************************************************
 *                                                                                                                                                                                                                                                                  *
 *                                                                                                                                                                                                                                                                  *
 *        Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)                                                                                                                                                                                              *
 *                                                                                                                                                                                                                                                                  *
 *        This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.    *
 *        This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.           *
 *        You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.                                                                                                       *
 *                                                                                                                                                                                                                                                                  *
 ********************************************************************************************************************************************************************************************************************************************************************/
# include	"xmlback.h"

# define	SMIME_EXTERN_IMPLEMENTATION		1
typedef struct bentry	bentry_t;
typedef struct cert	cert_t;
typedef struct ctx	ctx_t;
struct smime { /*{{{*/
	ctx_t	*ctx;
	cert_t	*head, *tail;
	/*}}}*/
};
struct bentry { /*{{{*/
	bool_t		private_key;
	bool_t		certificate;
	const char	*typ;
	int		tlen;
	const char	*pem;
	int		plen;
	const char	*raw;
	int		rlen;
	/*}}}*/
};
static bentry_t *
bentry_free (bentry_t *b) /*{{{*/
{
	if (b) {
		free (b);
	}
	return NULL;
}/*}}}*/
static bentry_t *
bentry_alloc (void) /*{{{*/
{
	bentry_t	*b;
	
	if (b = (bentry_t *) malloc (sizeof (bentry_t))) {
		b -> private_key = false;
		b -> certificate = false;
		b -> typ = NULL;
		b -> tlen = 0;
		b -> pem = NULL;
		b -> plen = 0;
		b -> raw = NULL;
		b -> rlen = 0;
	}
	return b;
}/*}}}*/
	
struct cert { /*{{{*/
	char		*email;
	buffer_t	*bag;
	DO_DECL (bentry);
	struct cert	*next;
	/*}}}*/
};
static cert_t *
cert_free (cert_t *c) /*{{{*/
{
	if (c) {
		if (c -> email)
			free (c -> email);
		DO_FREE (c, bentry);
		if (c -> bag)
			buffer_free (c -> bag);
		free (c);
	}
	return NULL;
}/*}}}*/
# define	MARK_PRIVATE_KEY	"PRIVATE KEY"
# define	MARK_PRIVATE_KEY_LEN	(sizeof (MARK_PRIVATE_KEY) - 1)
# define	MARK_CERTIFICATE	"CERTIFICATE"
# define	MARK_CERTIFICATE_LEN	(sizeof (MARK_CERTIFICATE) - 1)
# define	MARK_BEGIN		"-----BEGIN "
# define	MARK_END		"-----END "
# define	MARK_TRAIL		"-----"
# define	MARK_TRAIL_LEN		(sizeof (MARK_TRAIL) - 1)

static const char *
match_mark (const char *line, int len, const char *mark, int mlen, int *nlen) /*{{{*/
{
	const char	*rc = NULL;
	
	if (line) {
		while ((len > 0) && ((line[len - 1] == '\n') || (line[len - 1] == '\r')))
			--len;
		if ((len >= mlen + MARK_TRAIL_LEN) && (! strncmp (line, mark, mlen)) && (! strncmp (line + len - MARK_TRAIL_LEN, MARK_TRAIL, MARK_TRAIL_LEN))) {
			rc = line + mlen;
			*nlen = len - mlen - MARK_TRAIL_LEN;
		}
	}
	return rc;
}/*}}}*/
static bool_t
cert_parse (cert_t *c) /*{{{*/
{
	if (! c -> bentry) {
		bentry_t	*bentry;
		int		pos, len;
		const char	*line;
		const char	*name;
		int		nlen;
		
		for (bentry = NULL, pos = 0; (line = buffer_line (c -> bag, pos, & len)); pos += len)
			if (! bentry) {
				if (name = match_mark (line, len, MARK_BEGIN, sizeof (MARK_BEGIN) - 1, & nlen)) {
					DO_EXPAND (bentry, c, bentry);
					bentry -> typ = name;
					bentry -> tlen = nlen;
					bentry -> pem = line;
					bentry -> plen = len;
					bentry -> raw = NULL;
					bentry -> rlen = 0;
					if ((bentry -> tlen == MARK_PRIVATE_KEY_LEN) && (! strncmp (bentry -> typ, MARK_PRIVATE_KEY, MARK_PRIVATE_KEY_LEN)))
						bentry -> private_key = true;
					else if ((bentry -> tlen == MARK_CERTIFICATE_LEN) && (! strncmp (bentry -> typ, MARK_CERTIFICATE, MARK_CERTIFICATE_LEN)))
						bentry -> certificate = true;
				}
			} else {
				if (! bentry -> raw)
					bentry -> raw = line;
				bentry -> plen += len;
				if ((name = match_mark (line, len, MARK_END, sizeof (MARK_END) - 1, & nlen)) && (bentry -> tlen == nlen) && (! strncmp (bentry -> typ, name, bentry -> tlen)))
					bentry = NULL;
				else
					bentry -> rlen += len;
			}
		if (bentry)
			DO_SHRINK (c, bentry);
	}
	return c -> bentry ? true : false;
}/*}}}*/
static cert_t *
cert_alloc (const char *email, const char *certificate) /*{{{*/
{
	cert_t	*c = NULL;
	
	if (c = (cert_t *) malloc (sizeof (cert_t))) {
		int	clen = certificate ? strlen (certificate) : -1;
		
		c -> email = email ? strdup (email) : NULL;
		c -> bag = clen >= 0 ? buffer_alloc (clen + 1) : NULL;
		DO_ZERO (c, bentry);
		c -> next = NULL;
		if ((email && (! c -> email)) ||
		    (certificate && ((! c -> bag) || (! buffer_appendsn (c -> bag, certificate, clen)) || (! cert_parse (c)))))
			c = cert_free (c);
	}
	return c;
}/*}}}*/

# if		SMIME_EXTERN_IMPLEMENTATION
# include	<unistd.h>
# include	<fcntl.h>
# include	<ctype.h>
# include	<errno.h>
# include	<sys/wait.h>
# include	<string.h>

struct ctx { /*{{{/*/
	char	*openssl;
	/*}}}*/
};
static ctx_t *
ctx_free (ctx_t *ctx) /*{{{*/
{
	if (ctx) {
		if (ctx -> openssl)
			free (ctx -> openssl);
		free (ctx);
	}
	return NULL;
}/*}}}*/
static ctx_t *
ctx_alloc (void) /*{{{*/
{
	ctx_t	*ctx;
	
	if (ctx = (ctx_t *) malloc (sizeof (ctx_t))) {
		ctx -> openssl = which ("openssl");
		if (! ctx -> openssl)
			ctx = ctx_free (ctx);
	}
	return ctx;
}/*}}}*/
	
static void
update_header (header_t *header, buffer_t *update) /*{{{*/
{
	const byte_t	*source = buffer_content (update);
	int		length = buffer_length (update);
	char		*line;
	
	if ((length > 0) && (line = malloc (length + 1))) {
		const char	*ptr;
		int		len;
		char		*name;
		int		n;
		head_t		*head;

		memcpy (line, source, length);
		line[length] = '\0';
		for (ptr = line, len = length; (*ptr != ':') && (len > 0); ++ptr, --len)
			;
		if ((len < length) && (name = malloc (ptr - line + 1))) {
			for (n = 0; n < ptr - line; ++n)
				name[n] = tolower (line[n]);
			name[n] = '\0';
			header_remove (header, name);
			for (head = header -> head; head && head -> next; head = head -> next)
				;
			header_insert (header, line, head);
			free (name);
		}
		free (line);
	}
}/*}}}*/
static void
reorder_header (header_t *header) /*{{{*/
{
# define	MIME_VERSION		"mime-version"
	head_t	*head, *prev;
	head_t	*mime_version = NULL;
	
	for (head = header -> head, prev = NULL; head && head -> next; )
		if ((! mime_version) && head_matchn (head, MIME_VERSION, sizeof (MIME_VERSION) - 1)) {
			if (prev)
				prev -> next = head -> next;
			else
				header -> head = head -> next;
			mime_version = head;
			head = head -> next;
			mime_version -> next = NULL;
		} else {
			prev = head;
			head = head -> next;
		}
	if (mime_version)
		if (head)
			head -> next = mime_version;
		else
			header -> head = mime_version;
# undef		MIME_VERSION	
}/*}}}*/
static void
sign_smime (blockmail_t *blockmail, header_t *header, const char *path) /*{{{*/
{
	int		plen = strlen (path);
	char		*input, *output;
	buffer_t	*scratch;

	input = malloc (plen + 12);
	output = malloc (plen + 12);
	scratch = buffer_alloc (buffer_length (blockmail -> body) + 4096);
	if (input && output && scratch) {
		bool_t	ok = true;
		int	fd;
		
		sprintf (input, "%s.input", path);
		sprintf (output, "%s.output", path);
		if (ok && ((fd = open (input, O_WRONLY | O_CREAT | O_TRUNC, 0600)) != -1)) {
			head_t	*ct;
			
			for (ct = header -> head; ok && ct; ct = ct -> next)
				if (head_startswith (ct, "content-"))
					if (! buffer_appendbuf (scratch, ct -> h))
						ok = false;
			if (ok && (! buffer_appendch (scratch, '\n')))
				ok = false;
			if (ok && (! buffer_appendbuf (scratch, blockmail -> body)))
				ok = false;
			if (ok && (! buffer_universal_newline (scratch, 0)))
				ok = false;
			if (ok && (buffer_write (scratch, fd) != buffer_length (scratch)))
				ok = false;
			close (fd);
		} else
			ok = false;
		if (ok) {
			pid_t	pid, npid;
			int	st;
			
			if ((pid = fork ()) == 0) {
				execl (blockmail -> smime -> ctx -> openssl, blockmail -> smime -> ctx -> openssl, 
				       "smime", "-sign", "-certfile", path, "-signer", path, "-md", "sha256", "-in", input, "-out", output,
				       NULL);
				_exit (127);
			} else if (pid > 0) {
				while (((npid = waitpid (pid, & st, 0)) != pid) && (errno == EINTR))
					;
				if ((npid != pid) || st)
					ok = false;
				if (ok && ((fd = open (output, O_RDONLY)) != -1)) {
					buffer_clear (scratch);
					if (buffer_read (scratch, fd) == -1)
						ok = false;
					close (fd);
				} else
					ok = false;
			} else
				ok = false;
		}
		if (ok) {
			buffer_t	*head;

			buffer_universal_newline (scratch, 0);
			if (head = buffer_alloc (1024)) {
				const byte_t	*ptr = buffer_content (scratch);
				int		len = buffer_length (scratch);
				const byte_t	*line;
				int		linelen, eollen;
					
				for (line = ptr; len > 0; ) {
					if (((len >= 2) && (*ptr == '\r') && (*(ptr + 1) == '\n')) || (*ptr == '\n')) {
						linelen = ptr - line;
						eollen = *ptr == '\n' ? 1 : 2;
						len -= eollen;
						ptr += eollen;
						if (linelen == 0)
							break;
						if ((len == 0) || ((*line != ' ') && (*line != '\t'))) {
							update_header (header, head);
							buffer_clear (head);
						}
						buffer_stiff (head, line, linelen);
						buffer_stiffnl (head);
						line = ptr;
					} else
						--len, ++ptr;
				}
				update_header (header, head);
				buffer_free (head);
				reorder_header (header);
				buffer_set (blockmail -> body, ptr, len);
			}
		}
		unlink (input);
		unlink (output);
	}
	if (input)
		free (input);
	if (output)
		free (output);
	if (scratch)
		buffer_free (scratch);
}/*}}}*/
static void
sign_mail (blockmail_t *blockmail, receiver_t *rec, header_t *header, cert_t *cert) /*{{{*/
{
	char	filename[128];
	char	*path;

	snprintf (filename, sizeof (filename) - 1, ".smime.key.%06d", getpid ());
	if (path = mkpath (path_home (), "var", "tmp", filename, NULL)) {
		int	fd;
		bool_t	written = false;
				
		if ((fd = open (path, O_WRONLY | O_CREAT | O_TRUNC, 0600)) != -1) {
			written = write (fd, buffer_content (cert -> bag), buffer_length (cert -> bag)) == buffer_length (cert -> bag);
			close (fd);
		}
		if (written)
			sign_smime (blockmail, header, path);
		unlink (path);
		free (path);
	}
}/*}}}*/
# else		/* SMIME_EXTERN_IMPLEMENTATION */
struct ctx { /*{{{*/
	/*}}}*/
};
static ctx_t *
ctx_free (ctx_t *ctx) /*{{{*/
{
	if (ctx) {
		free (ctx);
	}
	return NULL;
}/*}}}*/
static ctx_t *
ctx_alloc (void) /*{{{*/
{
	ctx_t	*ctx;
	
	if (ctx = (ctx_t *) malloc (sizeof (ctx_t))) {
	}
	return ctx;
}/*}}}*/
static buffer_t *
sign_smime (blockmail_t *blockmail, buffer_t *message, cert_t *cert) /*{{{*/
{
	return NULL;
}/*}}}*/
static void
sign_mail (blockmail_t *blockmail, receiver_t *rec, header_t *header, cert_t *cert) /*{{{*/
{
	buffer_t	*message;
	
	if (message = buffer_alloc (buffer_length (blockmail -> body) + 4096)) {
		head_t	*ct;
		bool_t	ok;
			
		for (ct = header -> head, ok = true; ok && ct; ct = ct -> next)
			if (head_startswith (ct, "content-") && (! buffer_appendbuf (message, ct -> h)))
				ok = false;
		if (ok && (! buffer_appendnl (message)) && (! buffer_appendbuf (message, blockmail -> body)))
			ok = false;
		
		buffer_t	*signature, *body;
		
		if (ok &&
		    buffer_appendbuf (message, blockmail -> body) &&
		    (signature = sign_smime (blockmail, message, cert))) {
			if (body = buffer_alloc (buffer_length (blockmail -> body) + buffer_length (signature) + 4096)) {
				const char	*boundary = "--==_OuterOuter";
				cert_t		*ctemp;
				
				buffer_format (body, "--%s\n", boundary);
				buffer_appendbuf (body, message);
				buffer_format (body, "--%s\n", boundary);
				buffer_appends (body, "Content-Type: application/pkcs7-signature; name=\"smime.p7s\"\n");
				buffer_appends (body, "Content-Transfer-Encoding: base64\n");
				buffer_appends (body, "Content-Disposition: attachment; filename=\"smime.p7s\"\n\n");
				if (ctemp = cert_alloc (NULL, NULL)) {
					ctemp -> bag = signature;
					signature = NULL;
					if (cert_parse (ctemp) &&
					    (ctemp -> bentry_count == 1) &&
					    buffer_appendsn (body, ctemp -> bentry[0] -> raw, ctemp -> bentry[0] -> rlen)) {
						buffer_format (body, "--%s--\n", boundary);
						
						buffer_t	*temp = blockmail -> body;
						
						blockmail -> body = body;
						body = temp;
						header_remove (header, "content-type");
						header_remove (header, "content-transfer-encoding");
						char	scratch[4096];
						snprintf (scratch, sizeof (scratch), "Content-Type: multipart/signed; protocol=\"application/pkcs7-signature\"; micalg=sha-256; boundary=\"%s\"", boundary);
						
						header_append (header, scratch);
						header_append (header, "Content-Transfer-Encoding: 8bit");
					} else
						ok = false;
					cert_free (ctemp);
				} else
					ok = false;
				buffer_free (body);
			} else
				ok = false;
			buffer_free (signature);
		} else
			ok = false;
		buffer_free (message);
	}
}/*}}}*/
# endif		/* SMIME_EXTERN_IMPLEMENTATION */

smime_t *
smime_alloc (void) /*{{{*/
{
	smime_t	*s;
	
	if (s = (smime_t *) malloc (sizeof (smime_t))) {
		s -> ctx = ctx_alloc ();
		s -> head = NULL;
		s -> tail = NULL;
		if (! s -> ctx)
			s = smime_free (s);
	}
	return s;
}/*}}}*/
smime_t *
smime_free (smime_t *s) /*{{{*/
{
	if (s) {
		cert_t	*tmp;

		if (s -> ctx)
			ctx_free (s -> ctx);
		while (tmp = s -> head) {
			s -> head = s -> head -> next;
			cert_free (tmp);
		}
		free (s);
	}
	return NULL;
}/*}}}*/
bool_t
smime_add (smime_t *s, const char *email, const char *certificate) /*{{{*/
{
	cert_t	*c;
	
	if (c = cert_alloc (email, certificate)) {
		if (s -> tail)
			s -> tail -> next = c;
		else
			s -> head = c;
		s -> tail = c;
	}
	return c ? true : false;
}/*}}}*/
void
sign_mail_using_smime (blockmail_t *blockmail, receiver_t *rec) /*{{{*/
{
	if (rec -> header && rec -> header -> head) {
		head_t		*cur;
		cert_t		*cert;
		const char	*line;
		char		*ident;
		
		for (cur = rec -> header -> head, cert = NULL; cur; cur = cur -> next)
			if ((! cert) && head_match (cur, "from")) {
				if ((line = head_value (cur)) && (ident = extract_address (line))) {
					for (cert = blockmail -> smime -> head; cert; cert = cert -> next)
						if (match_address (ident, cert -> email))
							break;
					free (ident);
				}
				break;
			}
		if (cert)
			sign_mail (blockmail, rec, rec -> header, cert);
	}
}/*}}}*/
