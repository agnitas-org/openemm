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
# ifndef	__XMLBACK_PLUGIN_H
# define	__XMLBACK_PLUGIN_H		1
# include	"plugin.h"

typedef struct { /*{{{*/
	log_t		*lg;		/* logging interface		*/
	plugin_t	*plugins;	/* the plugins themselves	*/
	/* all method callbacks						*/
	invoke_t	*prepare;	/* prepare after initial setup	*/
	invoke_t	*cleanup;	/* cleanup before exiting	*/
	invoke_t	*section_start;	/* start of XML section		*/
	invoke_t	*section_end;	/* end of XML section		*/
	invoke_t	*create_output_start;	/* start of output creation	*/
	invoke_t	*create_output_mail;	/* each stage of mail creation	*/
	invoke_t	*create_output_end;	/* end of output creation	*/
	invoke_t	*tag_replace;	/* each replaced tag		*/
	/*}}}*/
}	xbp_t;

extern xbp_t	*xbp_create (log_t *lg);
extern xbp_t	*xbp_alloc (log_t *lg);
extern xbp_t	*xbp_free (xbp_t *xbp);
extern prc_t	xbp_prepare (xbp_t *xbp, blockmail_t *blockmail);
extern prc_t	xbp_cleanup (xbp_t *xbp, blockmail_t *blockmail);
extern prc_t	xbp_section_start (xbp_t *xbp, blockmail_t *blockmail, const xmlChar *name);
extern prc_t	xbp_section_end (xbp_t *xbp, blockmail_t *blockmail, const xmlChar *name, bool_t status);
extern prc_t	xbp_create_output_start (xbp_t *xbp, blockmail_t *blockmail, receiver_t *rec);
extern prc_t	xbp_create_output_mail (xbp_t *xbp, blockmail_t *blockmail, receiver_t *rec, const char *stage, block_t *block);
extern prc_t	xbp_create_output_end (xbp_t *xbp, blockmail_t *blockmail, receiver_t *rec, bool_t status);
extern prc_t	xbp_tag_replace (xbp_t *xbp, blockmail_t *blockmail, receiver_t *rec, tag_t *tag, const xmlChar *cont, int clen);

/* Prototypes for the plugin itself					*/
extern prc_t	p_prepare (void *priv, blockmail_t *blockmail);
extern prc_t	p_cleanup (void *priv, blockmail_t *blockmail);
extern prc_t	p_section_start (void *priv, blockmail_t *blockmail, const xmlChar *name);
extern prc_t	p_section_end (void *priv, blockmail_t *blockmail, const xmlChar *name, bool_t status);
extern prc_t	p_create_output_start (void *priv, blockmail_t *blockmail, receiver_t *rec);
extern prc_t	p_create_output_mail (void *priv, blockmail_t *blockmail, receiver_t *rec, const char *stage, block_t *block);
extern prc_t	p_create_output_end (void *priv, blockmail_t *blockmail, receiver_t *rec, bool_t status);
extern prc_t	p_tag_replace (void *priv, blockmail_t *blockmail, receiver_t *rec, tag_t *tag, const xmlChar *cont, int clen);
# endif		/* __XMLBACK_PLUGIN_H */
