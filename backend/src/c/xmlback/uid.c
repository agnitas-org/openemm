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
# include	<openssl/sha.h>
# include	<mpack.h>
# include	"xmlback.h"

# define	OPTION(flag,value)		((flag) ? (value) : 0)
# define	OPTION_TRACKING_VETO		(1 << 0)
# define	OPTION_DISABLE_LINK_EXTENSION	(1 << 1)

static char *
create_ecs_uid (blockmail_t *blockmail, int uid_version, const char *prefix, receiver_t *rec, url_t *url) /*{{{*/
{
	if (url && url -> url_id) {
		char	uid[128];

			snprintf (uid, sizeof (uid) - 1, "%ld", url -> url_id);
		return strdup (uid);
	}
	return strdup ("0");
}/*}}}*/
static inline char *
code36 (char *ptr, long val) /*{{{*/
{
	if (val == 0)
		*ptr++ = '0';
	else {
		char	scratch[32];
		int	len;
		
		if (val < 0) {
			*ptr++ = '-';
			val = -val;
		}
		len = sizeof (scratch) - 1;
		scratch[len] = '\0';
		while ((len > 0) && (val > 0)) {
			scratch[--len] = "0123456789abcdefghijklmnopqrstuvwxyz"[val % 36];
			val /= 36;
		}
		while (scratch[len])
			*ptr++ = scratch[len++];
	}
	return ptr;
}/*}}}*/
static const char	symbols[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
static int
iencode (char *buf, int buflen, unsigned long value) /*{{{*/
{
	char	temp[32];
	char	*ptr;
	int	len;
	
	ptr = temp + sizeof (temp) - 1;
	if (value == 0)
		*--ptr = symbols[0];
	else {
		while (value && (ptr > temp)) {
			*--ptr = symbols[value & 0x3f];
			value >>= 6;
		}
	}
	len = temp + sizeof (temp) - 1 - ptr;
	if (len > buflen)
		len = buflen;
	memcpy (buf, ptr, len);
	return len;
}/*}}}*/
static int
encode (char *buf, int buflen, const byte_t *data, int datalen) /*{{{*/
{
	int	n;
	int	pos;
	
	for (n = 0, pos = 0; (n < datalen) && (pos + 4 < buflen); ) {
		buf[pos++] = symbols[data[n] >> 2];
		buf[pos++] = symbols[((data[n] & 0x3) << 4) | (n + 1 < datalen ? data[n + 1] >> 4 : 0)];
		++n;
		if (n < datalen) {
			buf[pos++] = symbols[((data[n] & 0xf) << 2) | (n + 1 < datalen ? data[n + 1] >> 6 : 0)];
			++n;
			if (n < datalen) {
				buf[pos++] = symbols[data[n] & 0x3f];
				++n;
			}
		}
	}
	return pos;
}/*}}}*/
static byte_t *
create_packed (blockmail_t *blockmail, receiver_t *rec, url_t *url, bool_t add_status_field, size_t *size) /*{{{*/
{
	char		*data;
	mpack_writer_t	writer;
	mpack_error_t	rc;
	long		option;
	
	mpack_writer_init_growable (& writer, & data, size);
	mpack_build_map (& writer);
	if (blockmail -> company_id) {
		mpack_write_cstr (& writer, "_c");
		mpack_write_int (& writer, blockmail -> company_id);
	}
	if (blockmail -> licence_id) {
		mpack_write_cstr (& writer, "_l");
		mpack_write_int (& writer, blockmail -> licence_id);
	}
	if (blockmail -> mailing_id) {
		mpack_write_cstr (& writer, "_m");
		mpack_write_int (& writer, blockmail -> mailing_id);
	}
	option = OPTION (rec -> tracking_veto, OPTION_TRACKING_VETO) | OPTION (rec -> disable_link_extension, OPTION_DISABLE_LINK_EXTENSION);
	if (option) {
		mpack_write_cstr (& writer, "_o");
		mpack_write_int (& writer, option);
	}
	if (rec -> customer_id) {
		mpack_write_cstr (& writer, "_r");
		mpack_write_int (& writer, rec -> customer_id);
	}
	if (blockmail -> epoch) {
		mpack_write_cstr (& writer, "_s");
		mpack_write_int (& writer, blockmail -> epoch);
	}
	if (add_status_field && blockmail -> status_field && (blockmail -> status_field != 'W')) {
		mpack_write_cstr (& writer, "_f");
		mpack_write_str (& writer, & blockmail -> status_field, 1);
	}
	if (url && url -> url_id) {
		mpack_write_cstr (& writer, "_u");
		mpack_write_int (& writer, url -> url_id);
	}
	mpack_complete_map (& writer);
	rc = mpack_writer_destroy (& writer);
	if (rc != mpack_ok) {
		return NULL;
	}
	return (byte_t *) data;
}/*}}}*/
static char *
create_xuid (blockmail_t *blockmail, int uid_version, const char *prefix, receiver_t *rec, url_t *url, bool_t add_status_field) /*{{{*/
{
	const char	*rc;
	enum {
		V2 = 2,
		V3 = 3,
		V4 = 4,
		V5 = 5
	}		uid_version_used,
			fallback_version = V5;		/* must be always be the most recent stable one */
	int		n;
	long		url_id = url ? url -> url_id : 0L;
	unsigned long	vu = 0;
	long		vs = 0;
	char		scratch[4096];
	int		len = 0;
	byte_t		*packed;
	size_t		psize;
	
	buffer_clear (blockmail -> secret_uid);
	buffer_clear (blockmail -> secret_sig);
	if (prefix && *prefix) {
		buffer_stiffs (blockmail -> secret_uid, prefix);
		buffer_stiffch (blockmail -> secret_uid, '.');
		buffer_stiffs (blockmail -> secret_sig, prefix);
		buffer_stiffch (blockmail -> secret_sig, '.');
	}
	
	switch (uid_version) {
	default:
	case 0:		uid_version_used = fallback_version;	break;
	case 2:		uid_version_used = V2;			break;
	case 3:		uid_version_used = V3;			break;
	case 4:		uid_version_used = V4;			break;
	case 5:		uid_version_used = V5;			break;
	}
	
	switch (uid_version_used) {
	case V2:
		for (n = 0; n < 5; ++n) {
			switch (n) {
			case 0:	/* version */
				vu = uid_version_used;
				vs = uid_version_used;
				break;
			case 1:	/* licenceID */
				vu = blockmail -> licence_id;
				vs = blockmail -> licence_id;
				break;
			case 2:	/* mailingID */
				vu = blockmail -> mailing_id;
				vs = blockmail -> mailing_id;
				break;
			case 3:	/* customerID */
				vu = ((unsigned long) rec -> customer_id) ^ blockmail -> secret_timestamp1;
				vs = (long) rec -> customer_id;
				break;
			case 4:	/*  URLID */
				vu = ((unsigned long) url_id) ^ blockmail -> secret_timestamp2 ^ ((unsigned long) blockmail -> company_id);
				vs = url_id;
				break;
			}
			len = iencode (scratch, sizeof (scratch), vu);
			buffer_stiffsn (blockmail -> secret_uid, scratch, len);
			buffer_stiffch (blockmail -> secret_uid, '.');
			len = snprintf (scratch, sizeof (scratch), "%ld", vs);
			buffer_stiffsn (blockmail -> secret_sig, scratch, len);
			buffer_stiffch (blockmail -> secret_sig, '.');
		}
		break;
	case V3:
		for (n = 0; n < 6; ++n) {
			switch (n) {
			case 0:	/* version */
				vu = uid_version_used;
				vs = uid_version_used;
				break;
			case 1:	/* licenceID */
				vu = blockmail -> licence_id;
				vs = blockmail -> licence_id;
				break;
			case 2:	/* mailingID */
				vu = blockmail -> mailing_id;
				vs = blockmail -> mailing_id;
				break;
			case 3:	/* customerID */
				vu = rec -> customer_id;
				vs = rec -> customer_id;
				break;
			case 4:	/*  URLID */
				vu = url_id;
				vs = url_id;
				break;
			case 5:	/* bitOption */
				vu = OPTION (rec -> tracking_veto, OPTION_TRACKING_VETO) |
				     OPTION (rec -> disable_link_extension, OPTION_DISABLE_LINK_EXTENSION);
				vs = (long) vu;
				break;
			}
			len = iencode (scratch, sizeof (scratch), vu);
			buffer_stiffsn (blockmail -> secret_uid, scratch, len);
			buffer_stiffch (blockmail -> secret_uid, '.');
			len = snprintf (scratch, sizeof (scratch), "%ld", vs);
			buffer_stiffsn (blockmail -> secret_sig, scratch, len);
			buffer_stiffch (blockmail -> secret_sig, '.');
		}
		break;
	case V4:
		for (n = 0; n < 7; ++n) {
			switch (n) {
			case 0:	/* version */
				vu = uid_version_used;
				vs = uid_version_used;
				break;
			case 1:	/* licenceID */
				vu = blockmail -> licence_id;
				vs = blockmail -> licence_id;
				break;
			case 2:	/* companyID */
				vu = blockmail -> company_id;
				vs = blockmail -> company_id;
				break;
			case 3:	/* mailingID */
				vu = blockmail -> mailing_id;
				vs = blockmail -> mailing_id;
				break;
			case 4:	/* customerID */
				vu = rec -> customer_id;
				vs = rec -> customer_id;
				break;
			case 5:	/*  URLID */
				vu = url_id;
				vs = url_id;
				break;
			case 6:	/* bitOption */
				vu = OPTION (rec -> tracking_veto, OPTION_TRACKING_VETO) |
				     OPTION (rec -> disable_link_extension, OPTION_DISABLE_LINK_EXTENSION);
				vs = (long) vu;
				break;
			}
			len = iencode (scratch, sizeof (scratch), vu);
			buffer_stiffsn (blockmail -> secret_uid, scratch, len);
			buffer_stiffch (blockmail -> secret_uid, '.');
			len = snprintf (scratch, sizeof (scratch), "%ld", vs);
			buffer_stiffsn (blockmail -> secret_sig, scratch, len);
			buffer_stiffch (blockmail -> secret_sig, '.');
		}
		break;
	case V5:
		len = iencode (scratch, sizeof (scratch), uid_version_used);
		buffer_stiffsn (blockmail -> secret_uid, scratch, len);
		buffer_stiffch (blockmail -> secret_uid, '.');
		len = snprintf (scratch, sizeof (scratch), "%d", uid_version_used);
		buffer_stiffsn (blockmail -> secret_sig, scratch, len);
		buffer_stiffch (blockmail -> secret_sig, '.');
		if (packed = create_packed (blockmail, rec, url, add_status_field, & psize)) {
			if (encode_uid_parameter (packed, psize, blockmail -> secret_uid))
				buffer_stiffch (blockmail -> secret_uid, '.');
			buffer_stiff (blockmail -> secret_sig, packed, psize);
			buffer_stiffch (blockmail -> secret_sig, '.');
			MPACK_FREE (packed);
		}
		break;
	}
	buffer_stiff (blockmail -> secret_sig, xmlBufferContent (blockmail -> secret_key), xmlBufferLength (blockmail -> secret_key));
	switch (uid_version_used) {
	case V2:
	case V3:
	case V4:
	case V5:
		{
			SHA512_CTX	hash;
			unsigned char	digest[SHA512_DIGEST_LENGTH];
			
			SHA512_Init (& hash);
			SHA512_Update (& hash, buffer_content (blockmail -> secret_sig), buffer_length (blockmail -> secret_sig));
			SHA512_Final (digest, & hash);
			len = encode (scratch, sizeof (scratch), digest, sizeof (digest));
		}
		break;
	}
	buffer_stiffsn (blockmail -> secret_uid, scratch, len);
	rc = buffer_string (blockmail -> secret_uid);
	return rc ? strdup (rc) : NULL;
}/*}}}*/
char *
create_uid (blockmail_t *blockmail, int uid_version, const char *prefix, receiver_t *rec, url_t *url, bool_t add_status_field) /*{{{*/
{
	if (blockmail -> ecs) {
		return create_ecs_uid (blockmail, uid_version, prefix, rec, url);
	}
	return create_xuid (blockmail, uid_version, prefix, rec, url, add_status_field);
}/*}}}*/

char *
create_pubid (blockmail_t *blockmail, receiver_t *rec, const char *source, const char *parm) /*{{{*/
{
	static const char	cl[] = "w5KMCHOXE_PTuLcfF6D1ZI3BydeplQaztVAnUj0bqos7k49YgWhxiS-RrGJm8N2v";
	static const char	vc[] = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_-";
	char			*rc;
	int			srclen, parmlen;
	char			*temp;
	
	rc = NULL;
	if ((srclen = source ? strlen (source) : 0) > 20)
		srclen = 20;
	parmlen = parm ? strlen (parm) : 0;
	if (temp = malloc (256 + srclen + parmlen)) {
		buffer_t	*scratch;
		int		len;
		int		n;

		len = sprintf (temp, "%d;%d;", blockmail -> mailing_id, rec -> customer_id);
		if (srclen) {
			for (n = 0; n < srclen; ++n)
				temp[len++] = strchr (vc, source[n]) ? source[n] : '_';
		}
		if (parm) {
			temp[len++] = ';';
			memcpy (temp + len, parm, parmlen);
			len += parmlen;
		}
		temp[len] = '\0';
		if (scratch = buffer_alloc (((len + 3) * 4) / 3 + 8)) {
			int		cs;
			unsigned long	d;
			char		add[4];
			const char	*s;
			
			for (n = 0, cs = 12; n < len; n += 3) {
				d = (((unsigned char) temp[n]) << 16) |
				    (n + 1 < len ? (((unsigned char) temp[n + 1]) << 8) : 0) |
				    (n + 2 < len ? ((unsigned char) temp[n + 2]) : 0);
				add[0] = cl[(d >> 18) & 0x3f];
				add[1] = cl[(d >> 12) & 0x3f];
				add[2] = cl[(d >> 6) & 0x3f];
				add[3] = cl[d & 0x3f];
				cs += (int) add[0] + (int) add[1] + (int) add[2] + (int) add[3];
				if (n == 3) {
					buffer_appendch (scratch, add[0]);
					buffer_appendch (scratch, '?');
					buffer_appendsn (scratch, add + 1, 3);
				} else
					buffer_appendsn (scratch, add, 4);
			}
			if ((s = buffer_string (scratch)) && (rc = strdup (s)))
				rc[5] = cl[cs & 0x3f];
			buffer_free (scratch);
		}
		free (temp);
	}
	return rc;
}/*}}}*/
