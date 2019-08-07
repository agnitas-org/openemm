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
# include	<openssl/conf.h>
# include	<openssl/evp.h>
# include	<openssl/err.h>
# include	<zlib.h>
# include	"xmlback.h"

struct encrypt { /*{{{*/
	unsigned char	key[32];	/* 256 bit key			*/
	unsigned char	iv[16];		/* 128 bit init. vector		*/
	buffer_t	*build;		/* build input buffer		*/
	buffer_t	*compress;	/* scratch buffer for compress	*/
	buffer_t	*encrypt;	/* scratch buffer for encrypt	*/
	buffer_t	*output;	/* scratch buffer for output	*/
	/*}}}*/
};

static void
compressor (encrypt_t *e, buffer_t *source) /*{{{*/
{
	buffer_clear (e -> compress);
	if (buffer_size (e -> compress, source -> length + 256)) {
		z_stream	stream;

		stream.next_in = source -> buffer;
		stream.avail_in = source -> length;
		stream.total_in = source -> length;
		stream.next_out = e -> compress -> buffer;
		stream.avail_out = e -> compress -> size;
		stream.total_out = e -> compress -> size;
		stream.zalloc = Z_NULL;
		stream.zfree = Z_NULL;
		stream.opaque = Z_NULL;
		if (deflateInit (& stream, Z_BEST_COMPRESSION) == Z_OK) {
			if (deflate (& stream, Z_FINISH) == Z_STREAM_END) {
				e -> compress -> length = e -> compress -> size - stream.avail_out;
			}
			deflateEnd (& stream);
		}
	}
}/*}}}*/

static bool_t	isinit = false;
static void
encrypt_init (void) /*{{{*/
{
	if (! isinit) {
		ERR_load_crypto_strings ();
		OpenSSL_add_all_algorithms ();
		OPENSSL_config (NULL);
		isinit = true;
	}
}/*}}}*/
encrypt_t *
encrypt_alloc (blockmail_t *blockmail) /*{{{*/
{
	encrypt_t	*e;

	encrypt_init ();
	if (e = (encrypt_t *) malloc (sizeof (encrypt_t))) {
		const xmlChar	*skey = xmlBufferContent (blockmail -> secret_key);
		int		slen = xmlBufferLength (blockmail -> secret_key);
		int		n;
		
		for (n = 0; n < sizeof (e -> key); ++n)
			e -> key[n] = n < slen ? skey[n] : '\0';
		e -> build = buffer_alloc (512);
		e -> compress = buffer_alloc (1024);
		e -> encrypt = buffer_alloc (1024);
		e -> output = buffer_alloc (2048);
		if ((! e -> build) || (! e -> compress) || (! e -> encrypt) || (! e -> output))
			e = encrypt_free (e);
	}
	return e;
}/*}}}*/
encrypt_t *
encrypt_free (encrypt_t *e) /*{{{*/
{
	if (e) {
		if (e -> build)
			buffer_free (e -> build);
		if (e -> compress)
			buffer_free (e -> compress);
		if (e -> encrypt)
			buffer_free (e -> encrypt);
		if (e -> output)
			buffer_free (e -> output);
		free (e);
	}
	return NULL;
}/*}}}*/

static char	hex[] = "0123456789abcdef";
const char *
encrypt_do (encrypt_t *e, receiver_t *rec, buffer_t *buf, int version) /*{{{*/
{
	const char	*rc = NULL;

	if (buffer_size (e -> encrypt, buffer_length (buf) + 1024)) {
		int		n;
		unsigned int	siv;
		EVP_CIPHER_CTX	*ctx;
		buffer_t	*source;
		bool_t		compressed;

		source = buf;
		compressed = false;
		switch (version) {
		case 1:
			compressor (e, buf);
			if ((e -> compress -> length > 0) && (e -> compress -> length < buf -> length)) {
				source = e -> compress;
				compressed = true;
			} else {
				source = buf;
				compressed = false;
			}
			break;
		}
		for (n = 0, siv = (unsigned int) rec -> customer_id; n < sizeof (e -> iv); ++n) {
			e -> iv[n] = (unsigned char) siv;
			siv >>= 8;
		}
		if (ctx = EVP_CIPHER_CTX_new ()) {
			if (EVP_EncryptInit_ex (ctx, EVP_aes_256_cbc (), NULL, e -> key, e -> iv)) {
				unsigned char	*scont;
				int		slen;
				
				buffer_clear (e -> encrypt);
				scont = e -> encrypt -> buffer;
				if (EVP_EncryptUpdate (ctx, scont, & slen, source -> buffer, source -> length) == 1) {
					if (slen < e -> encrypt -> size) {
						e -> encrypt -> length = slen;
						if (EVP_EncryptFinal_ex (ctx, scont + slen, & slen) == 1) {
							if (e -> encrypt -> length + slen < e -> encrypt -> size) {
								e -> encrypt -> length += slen;
								switch (version) {
								default:
								case 0:
									if (buffer_size (e -> output, buffer_length (e -> encrypt) * 2 + 1)) {
										unsigned char	*out = e -> output -> buffer;
										int		olen = 0;
										
										for (n = 0; n < e -> encrypt -> length; ++n) {
											out[olen++] = hex[scont[n] >> 4];
											out[olen++] = hex[scont[n] & 0xf];
										}
										e -> output -> length = olen;
										rc = buffer_string (e -> output);
									}
									break;
								case 1:
									if (buffer_size (e -> output, (e -> encrypt -> length * 4) / 3 + 12)) {
										buffer_setsn (e -> output, compressed ? "Va" : "VA", 2);
										if (encode_encrypted (e -> encrypt, e -> output)) {
											rc = buffer_string (e -> output);
										}
									}
									break;
								}
							}
						}
					}
				}
			}
			if (! rc) {
				ERR_print_errors_fp (stderr);
			}
			EVP_CIPHER_CTX_free (ctx);
		}
	}
	return rc;
}/*}}}*/
void
encrypt_build_reset (encrypt_t *e) /*{{{*/
{
	buffer_clear (e -> build);
}/*}}}*/
bool_t
encrypt_build_add (encrypt_t *e, const char *s, int slen) /*{{{*/
{
	return buffer_stiffsn (e -> build, s, slen);
}/*}}}*/
const char *
encrypt_build_do (encrypt_t *e, receiver_t *rec, int version) /*{{{*/
{
	return encrypt_do (e, rec, e -> build, version);
}/*}}}*/
