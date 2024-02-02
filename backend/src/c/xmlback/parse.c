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
# include	<ctype.h>
# include	<string.h>
# include	"xmlback.h"

static char *
xml2strn (const xmlChar *in, size_t length) /*{{{*/
{
	char	*out;
	
	if (out = malloc (length + 1)) {
		if (length > 0)
			memcpy (out, in, length);
		out[length] = '\0';
	}
	return out;
}/*}}}*/
static char *
xml2str (const xmlChar *in) /*{{{*/
{
	return xml2strn (in, xmlStrlen (in));
}/*}}}*/
static bool_t
str2long (const char *str, long *value) /*{{{*/
{
	bool_t	st;
	long	val;
	char	*eptr;
	
	st = false;
	val =  strtol (str, & eptr, 0);
	if (! *eptr) {
		*value = val;
		st = true;
	}
	return st;
}/*}}}*/
static bool_t
str2longlong (const char *str, long long *value) /*{{{*/
{
	bool_t		st;
	long long	val;
	char		*eptr;
	
	st = false;
	val =  strtoll (str, & eptr, 0);
	if (! *eptr) {
		*value = val;
		st = true;
	}
	return st;
}/*}}}*/
static bool_t
str2bool (const char *str, bool_t *value) /*{{{*/
{
	bool_t	st;
	
	st = true;
	if (! strcasecmp (str, "true"))
		*value = true;
	else if (! strcasecmp (str, "false"))
		*value = false;
	else
		st = false;
	return st;
}/*}}}*/

static var_t *
extract_xml_properties (blockmail_t *blockmail, xmlNodePtr node) /*{{{*/
{
	xmlAttrPtr	attr;
	var_t		*root, *prev, *cur;
	xmlChar		*val;
	
	root = NULL;
	prev = NULL;
	for (attr = node -> properties; attr; attr = attr -> next)
		if (attr -> name && (val = xmlGetProp (node, attr -> name))) {
			if (cur = var_alloc (xml2char (attr -> name), xml2char (val))) {
				if (prev)
					prev -> next = cur;
				else
					root = cur;
				prev = cur;
			}
			xmlFree (val);
		}
	return root;
}/*}}}*/
static xmlBufferPtr
extract_xml_property (xmlNodePtr node, const char *prop) /*{{{*/
{
	xmlBufferPtr	buf;
	xmlChar		*val;

	buf = NULL;
	if (val = xmlGetProp (node, char2xml (prop))) {
		if (buf = xmlBufferCreate ())
			xmlBufferCat (buf, val);
		xmlFree (val);
	}
	return buf;
}/*}}}*/
static char *
extract_property (blockmail_t *blockmail, xmlNodePtr node, const char *prop) /*{{{*/
{
	char	*value;
	xmlChar	*val;
	
	value = NULL;
	if (val = xmlGetProp (node, char2xml (prop))) {
		value = xml2str (val);
		xmlFree (val);
	}
	return value;
}/*}}}*/
static bool_t
extract_numeric_property (blockmail_t *blockmail, long *value, xmlNodePtr node, const char *prop) /*{{{*/
{
	bool_t	st;
	char	*temp;
	
	st = false;
	if (temp = extract_property (blockmail, node, prop)) {
		st = str2long (temp, value);
		free (temp);
	}
	return st;
}/*}}}*/
static bool_t
extract_boolean_property (blockmail_t *blockmail, bool_t *value, xmlNodePtr node, const char *prop) /*{{{*/
{
	bool_t	st;
	char	*temp;
	
	st = false;
	if (temp = extract_property (blockmail, node, prop)) {
		st = str2bool (temp, value);
		free (temp);
	}
	return st;
}/*}}}*/

static bool_t
extract_content (xmlBufferPtr *buf, xmlDocPtr doc, xmlNodePtr base) /*{{{*/
{
	bool_t		st;
	xmlNodePtr	node;
	bool_t		isnew;
	
	st = true;
	isnew = *buf == NULL;
	if (*buf)
		xmlBufferEmpty (*buf);
	else {
		*buf = xmlBufferCreate ();
		if (! buf)
			st = false;
	}
	for (node = base -> children; st && node; node = node -> next)
		if (node -> type == XML_TEXT_NODE) {
			xmlChar	*ptr;

			if (ptr = xmlNodeListGetString (doc, node, 1)) {
				xmlBufferAdd (*buf, ptr, xmlStrlen (ptr));
				xmlFree (ptr);
			} else
				st = false;
		}
	if ((! st) && isnew && *buf) {
		xmlBufferFree (*buf);
		*buf = NULL;
	}
	return st;
}/*}}}*/
static char *
extract_simple_content (blockmail_t *blockmail, xmlDocPtr doc, xmlNodePtr node) /*{{{*/
{
	char		*value;
	xmlBufferPtr	buf;
	
	value = NULL;
	buf = NULL;
	if (extract_content (& buf, doc, node) && buf) {
		value = xml2strn (xmlBufferContent (buf), xmlBufferLength (buf));
		xmlBufferFree (buf);
	}
	return value;
}/*}}}*/
static bool_t
extract_numeric_content (blockmail_t *blockmail, long *value, xmlDocPtr doc, xmlNodePtr node) /*{{{*/
{
	bool_t	st;
	char	*temp;
	
	st = false;
	if (temp = extract_simple_content (blockmail, doc, node)) {
		st = str2long (temp, value);
		free (temp);
	}
	return st;
}/*}}}*/
static bool_t
extract_large_numeric_content (blockmail_t *blockmail, long long *value, xmlDocPtr doc, xmlNodePtr node) /*{{{*/
{
	bool_t	st;
	char	*temp;
	
	st = false;
	if (temp = extract_simple_content (blockmail, doc, node)) {
		st = str2longlong (temp, value);
		free (temp);
	}
	return st;
}/*}}}*/
static void
unknown (blockmail_t *blockmail, xmlNodePtr node) /*{{{*/
{
	log_out (blockmail -> lg, LV_VERBOSE, "Ignore unknown element %s in %s", node -> name, blockmail -> fname);
}/*}}}*/
static void
invalid (blockmail_t *blockmail, xmlNodePtr node) /*{{{*/
{
	log_out (blockmail -> lg, LV_ERROR, "Unable to find valid element %s in %s", node -> name, blockmail -> fname);
}/*}}}*/

static bool_t
parse_info (blockmail_t *blockmail, xmlDocPtr doc, xmlNodePtr base, var_t **vbase, const char *name) /*{{{*/
{
	bool_t		st;
	xmlNodePtr	node;
	var_t		*prev, *temp;
	char		*var, *val;
	
	st = true;
	if (*vbase)
		for (prev = *vbase; prev -> next; prev = prev -> next)
			;
	else
		prev = NULL;
	log_idpush (blockmail -> lg, name);
	for (node = base; node && st; node = node -> next)
		if (node -> type == XML_ELEMENT_NODE) {
			if (! xmlstrcmp (node -> name, "info")) {
				if (var = extract_property (blockmail, node, "name")) {
					if (val = extract_simple_content (blockmail, doc, node)) {
						if ((*var == '_') && blockmail -> smap)
							string_map_addss (blockmail -> smap, var + 1, val);
						if (temp = var_alloc (NULL, NULL)) {
							temp -> var = var;
							temp -> val = val;
							var = NULL;
							if (prev)
								prev -> next = temp;
							else
								*vbase = temp;
							prev = temp;
						}
					} else
						st = false;
					if (var)
						free (var);
				} else
					st = false;
			}
		}
	log_idpop (blockmail -> lg);
	return st;
}/*}}}*/
static bool_t
parse_company_info (blockmail_t *blockmail, xmlDocPtr doc, xmlNodePtr base) /*{{{*/
{
	return parse_info (blockmail, doc, base, & blockmail -> company_info, "company_info");
}/*}}}*/
static bool_t
parse_description (blockmail_t *blockmail, xmlDocPtr doc, xmlNodePtr base) /*{{{*/
{
	bool_t		st;
	xmlNodePtr	node;
	long		val;
	char		*ptr;
	
	st = true;
	log_idpush (blockmail -> lg, "description");
	for (node = base; node && st; node = node -> next)
		if (node -> type == XML_ELEMENT_NODE) {
			if (! xmlstrcmp (node -> name, "licence")) {
				if (st = extract_numeric_property (blockmail, & val, node, "id")) {
					blockmail -> licence_id = (int) val;
					if (blockmail -> owner_id < 1)
						blockmail -> owner_id = blockmail -> licence_id;
				}
			} else if (! xmlstrcmp (node -> name, "company")) {
				if (st = extract_numeric_property (blockmail, & val, node, "id")) {
					blockmail -> company_id = (int) val;
					blockmail -> company_token = extract_property (blockmail, node, "token");
					extract_boolean_property (blockmail, & blockmail -> allow_unnormalized_emails, node, "allow_unnormalized_emails");
					st = parse_company_info (blockmail, doc, node -> children);
				}
			} else if (! xmlstrcmp (node -> name, "mailinglist")) {
				if (st = extract_numeric_property (blockmail, & val, node, "id"))
					blockmail -> mailinglist_id = (int) val;
				blockmail -> mailinglist_name = extract_xml_property (node, "name");
			} else if (! xmlstrcmp (node -> name, "mailing")) {
				if (st = extract_numeric_property (blockmail, & val, node, "id"))
					blockmail -> mailing_id = (int) val;
				blockmail -> mailing_name = extract_xml_property (node, "name");
			} else if (! xmlstrcmp (node -> name, "mailing-description")) {
				st = extract_content (& blockmail -> mailing_description, doc, node);
			} else if (! xmlstrcmp (node -> name, "maildrop")) {
				if (st = extract_numeric_property (blockmail, & val, node, "status_id"))
					blockmail -> maildrop_status_id = (int) val;
			} else if (! xmlstrcmp (node -> name, "status")) {
				if (ptr = extract_property (blockmail, node, "field")) {
					blockmail -> status_field = *ptr;
					free (ptr);
				} else
					st = false;
			} else if (! xmlstrcmp (node -> name, "send")) {
				if (ptr = extract_property (blockmail, node, "date")) {
					if (! extract_numeric_property (blockmail, & val, node, "epoch"))
						val = 0;
					blockmail_setup_senddate (blockmail, ptr, val);
					free (ptr);
				} else
					st = false;
			} else
				unknown (blockmail, node);
			if (! st)
				invalid (blockmail, node);
		}
	if (blockmail -> smap) {
		string_map_addsi (blockmail -> smap, "licence_id", blockmail -> licence_id);
		string_map_addsi (blockmail -> smap, "owner_id", blockmail -> owner_id);
		string_map_addsi (blockmail -> smap, "company_id", blockmail -> company_id);
		if (blockmail -> company_token)
			string_map_addss (blockmail -> smap, "company_token", blockmail -> company_token);
		string_map_addsi (blockmail -> smap, "mailinglist_id", blockmail -> mailinglist_id);
		if (blockmail -> mailinglist_name)
			string_map_addsb (blockmail -> smap, "mailinglist_name", blockmail -> mailinglist_name);
		string_map_addsi (blockmail -> smap, "mailing_id", blockmail -> mailing_id);
		if (blockmail -> mailing_name)
			string_map_addsb (blockmail -> smap, "mailing_name", blockmail -> mailing_name);
		if (blockmail -> mailing_description)
			string_map_addsb (blockmail -> smap, "mailing_description", blockmail -> mailing_description);
	}
	log_idpop (blockmail -> lg);
	return st;
}/*}}}*/
static bool_t
parse_general (blockmail_t *blockmail, xmlDocPtr doc, xmlNodePtr base) /*{{{*/
{
	bool_t		st;
	xmlNodePtr	node;
	
	st = true;
	log_idpush (blockmail -> lg, "general");
	for (node = base; node && st; node = node -> next)
		if (node -> type == XML_ELEMENT_NODE) {
			if (! xmlstrcmp (node -> name, "domain"))
				blockmail -> domain = extract_simple_content (blockmail, doc, node);
			else if (! xmlstrcmp (node -> name, "subject"))
				st = extract_content (& blockmail -> email.subject, doc, node);
			else if (! xmlstrcmp (node -> name, "from_email"))
				st = extract_content (& blockmail -> email.from, doc, node);
			else if (! xmlstrcmp (node -> name, "auto_url")) {
				st = extract_content (& blockmail -> auto_url, doc, node);
				if (st) {
					int		len;
					const xmlChar	*ptr;
				
					len = xmlBufferLength (blockmail -> auto_url);
					ptr = xmlBufferContent (blockmail -> auto_url);
					if ((len > 0) && ((ptr[len - 1] == '?') || (ptr[len - 1] == '&')))
						blockmail -> auto_url_is_dynamic = true;
					else
						blockmail -> auto_url_is_dynamic = false;
				}
			} else if (! xmlstrcmp (node -> name, "onepixel_url"))
				st = extract_content (& blockmail -> onepixel_url, doc, node);
			else if (! xmlstrcmp (node -> name, "anon_url"))
				st = extract_content (& blockmail -> anon_url, doc, node);
			else if (! xmlstrcmp (node -> name, "uid_version"))
				st = extract_numeric_content (blockmail, & blockmail -> uid_version, doc, node);
			else if (! xmlstrcmp (node -> name, "secret_key"))
				st = extract_content (& blockmail -> secret_key, doc, node);
			else if (! xmlstrcmp (node -> name, "secret_timestamp")) {
				st = extract_large_numeric_content (blockmail, & blockmail -> secret_timestamp, doc, node);
				if (st) {
					blockmail -> secret_timestamp1 = (unsigned short) blockmail -> secret_timestamp;
					blockmail -> secret_timestamp2 = (unsigned short) (blockmail -> secret_timestamp * 37);
				}
			}
			else if (! xmlstrcmp (node -> name, "total_subscribers"))
				st = extract_numeric_content (blockmail, & blockmail -> total_subscribers, doc, node);
			else if (! xmlstrcmp (node -> name, "mailtracking")) {
				char	*value = extract_simple_content (blockmail, doc, node);
				
				if (value) {
					if (! strcmp (value, "extended")) {
						blockmail -> mailtrack = mailtrack_alloc (blockmail -> licence_id, blockmail -> company_id, blockmail -> mailing_id, blockmail -> maildrop_status_id);
					}
					free (value);
				} else
					st = false;
			} else
				unknown (blockmail, node);
			if (! st)
				invalid (blockmail, node);
		}
	if (blockmail -> smap) {
		string_map_addsi (blockmail -> smap, "total_subscribers", blockmail -> total_subscribers);
	}
	log_idpop (blockmail -> lg);
	return st;
}/*}}}*/
static bool_t
parse_mailcreation (blockmail_t *blockmail, xmlDocPtr doc, xmlNodePtr base) /*{{{*/
{
	bool_t		st;
	xmlNodePtr	node;
	long		val;
	
	st = true;
	log_idpush (blockmail -> lg, "mailcreation");
	for (node = base; node && st; node = node -> next)
		if (node -> type == XML_ELEMENT_NODE) {
			if (! xmlstrcmp (node -> name, "blocknr")) {
				if (st = extract_numeric_content (blockmail, & val, doc, node))
					blockmail -> blocknr = (int) val;
			} else if (! xmlstrcmp (node -> name, "innerboundary")) {
				if (! (blockmail -> innerboundary = extract_simple_content (blockmail, doc, node)))
					st = false;
			} else if (! xmlstrcmp (node -> name, "outerboundary")) {
				if (! (blockmail -> outerboundary = extract_simple_content (blockmail, doc, node)))
					st = false;
			} else if (! xmlstrcmp (node -> name, "attachboundary")) {
				if (! (blockmail -> attachboundary = extract_simple_content (blockmail, doc, node)))
					st = false;
			} else
				unknown (blockmail, node);
			if (! st)
				invalid (blockmail, node);
		}
	log_idpop (blockmail -> lg);
	return st;
}/*}}}*/
static bool_t
parse_trackers (blockmail_t *blockmail, xmlDocPtr doc, xmlNodePtr base) /*{{{*/
{
	bool_t		st;
	xmlNodePtr	node;
	
	st = true;
	log_idpush (blockmail -> lg, "trackers");
	for (node = base; node && st; node = node -> next)
		if (node -> type == XML_ELEMENT_NODE) {
			if (! xmlstrcmp (node -> name, "tracker")) {
				char		*name;
				xmlBufferPtr	content;

				if (name = extract_property (blockmail, node, "name")) {
					content = NULL;
					st = extract_content (& content, doc, node);
					if (st) {
						if (blockmail -> tracker || (blockmail -> tracker = tracker_alloc ()))
							st = tracker_add (blockmail -> tracker, blockmail, name, content);
						else
							st = false;
						xmlBufferFree (content);
					}
					free (name);
				} else
					st = false;
			} else
				unknown (blockmail, node);
			if (! st)
				invalid (blockmail, node);
		}
	log_idpop (blockmail -> lg);
	return st;
}/*}}}*/
static bool_t
parse_media_parameter (blockmail_t *blockmail, xmlDocPtr doc, xmlNodePtr base, media_t *media) /*{{{*/
{
	bool_t		st;
	xmlNodePtr	node, child;
	char		*name;
	parm_t		*tmp, *prv;
	xmlBufferPtr	buf;
			
	st = true;
	for (tmp = media -> parm, prv = NULL; tmp; tmp = tmp -> next)
		prv = tmp;
	for (node = base; st && node; node = node -> next)
		if ((node -> type == XML_ELEMENT_NODE) && (! xmlstrcmp (node -> name, "variable")))
			if (name = extract_property (blockmail, node, "name")) {
				if (tmp = parm_alloc ()) {
					tmp -> name = name;
					if (prv)
						prv -> next = tmp;
					else
						media -> parm = tmp;
					prv = tmp;
					for (child = node -> children; st && child; child = child -> next)
						if ((child -> type == XML_ELEMENT_NODE) && (! xmlstrcmp (child -> name, "value"))) {
							buf = NULL;
							if (extract_content (& buf, doc, child)) {
								tmp -> value = buf;
								break;
							}
						}
				} else
					free (name);
			} else {
				log_out (blockmail -> lg, LV_ERROR, "Missing name in variable in %s", blockmail -> fname);
				st = false;
			}
	return st;
}/*}}}*/
static bool_t
parse_media (blockmail_t *blockmail, xmlDocPtr doc, xmlNodePtr node, media_t *media) /*{{{*/
{
	bool_t	st;
	char	*type, *stat;
	long	prio;
	
	type = extract_property (blockmail, node, "type");
	stat = extract_property (blockmail, node, "status");
	if (type && stat && extract_numeric_property (blockmail, & prio, node, "priority")) {
		st = true;
		
		if (media_set_type (media, type) &&
		    media_set_priority (media, prio) &&
		    media_set_status (media, stat)) {
			st = parse_media_parameter (blockmail, doc, node -> children, media);
			if (st)
				media_postparse (media, blockmail);
		} else {
			log_out (blockmail -> lg, LV_ERROR, "Invalid data for media: %s/%ld/%s in %s", type, prio, stat, blockmail -> fname);
			st = false;
		}
	} else {
		log_out (blockmail -> lg, LV_ERROR, "Missing name in media in %s", blockmail -> fname);
		st = false;
	}
	if (type)
		free (type);
	if (stat)
		free (stat);
	return st;
}/*}}}*/
static bool_t
parse_mediatypes (blockmail_t *blockmail, xmlDocPtr doc, xmlNodePtr base) /*{{{*/
{
	bool_t		st;
	xmlNodePtr	node;
	
	st = true;
	log_idpush (blockmail -> lg, "mediatypes");
	for (node = base; node && st; node = node -> next)
		if (node -> type == XML_ELEMENT_NODE) {
			if (! xmlstrcmp (node -> name, "media")) {
				media_t	*media;

				DO_EXPAND (media, blockmail, media);
				if (media) {
					st = parse_media (blockmail, doc, node, media);
					if (! st)
						DO_SHRINK (blockmail, media);
				} else
					st = false;
			} else
				unknown (blockmail, node);
			if (! st)
				invalid (blockmail, node);
		}
	log_idpop (blockmail -> lg);
	return st;
}/*}}}*/

static bool_t	parse_block (blockmail_t *blockmail, xmlDocPtr doc, xmlNodePtr node, block_t *block);
static bool_t
parse_tagposition (blockmail_t *blockmail, xmlDocPtr doc, xmlNodePtr base, tagpos_t *tpos) /*{{{*/
{
	bool_t		st;
	xmlNodePtr	node;
	
	st = true;
	log_idpush (blockmail -> lg, "tagposition");
	for (node = base; node && st; node = node -> next)
		if (node -> type == XML_ELEMENT_NODE) {
			if (! xmlstrcmp (node -> name, "block")) {
				if (! tpos -> content) {
					if (tpos -> content = block_alloc ())
						st = parse_block (blockmail, doc, node, tpos -> content);
					else
						st = false;
				} else {
					log_out (blockmail -> lg, LV_ERROR, "Duplicate block for tagpos in %s", blockmail -> fname);
					st = false;
				}
			} else
				unknown (blockmail, node);
			if (! st)
				invalid (blockmail, node);
		}
	log_idpop (blockmail -> lg);
	return st;
}/*}}}*/
static bool_t
parse_block (blockmail_t *blockmail, xmlDocPtr doc, xmlNodePtr node, block_t *block) /*{{{*/
{
	bool_t	st;
	long	bid;
	long	val;

	if (extract_numeric_property (blockmail, & bid, node, "id") &&
	    extract_numeric_property (blockmail, & val, node, "nr")) {
		xmlNodePtr	child;

		st = true;
		block -> bid = (int) bid;
		block -> nr = (int) val;
		block -> mime = extract_property (blockmail, node, "mimetype");
		block -> charset = extract_property (blockmail, node, "charset");
		block -> encode = extract_property (blockmail, node, "encode");
		block_find_method (block);
		block -> cid = extract_property (blockmail, node, "cid");
		block -> tid = TID_Unspec;
		if (block -> cid) {
			if (! strcmp (block -> cid, "agnHead"))
				block -> tid = TID_EMail_Head;
			else if (! strcmp (block -> cid, "agnText"))
				block -> tid = TID_EMail_Text;
			else if (! strcmp (block -> cid, "agnHtml"))
				block -> tid = TID_EMail_HTML;
			else if (! strcmp (block -> cid, "agnPreheader"))
				block -> tid = TID_EMail_HTML_Preheader;
			else if (! strcmp (block -> cid, "agnClearance"))
				block -> tid = TID_EMail_HTML_Clearance;
		}
		extract_boolean_property (blockmail, & block -> binary, node, "is_binary");
		extract_boolean_property (blockmail, & block -> attachment, node, "is_attachment");
		extract_boolean_property (blockmail, & block -> precoded, node, "is_precoded");
		block -> media = extract_property (blockmail, node, "media");
		if (block -> media)
			media_parse_type (block -> media, & block -> mediatype);
		block -> condition = extract_xml_property (node, "condition");
		if (! extract_numeric_property (blockmail, & block -> target_id, node, "target_id")) {
			block -> target_id = 0;
		}
		for (child = node -> children; st && child; child = child -> next)
			if (child -> type == XML_ELEMENT_NODE) {
				if (! xmlstrcmp (child -> name, "content")) {
					if (st = extract_content (& block -> content, doc, child)) {
						if (st = block_setup_charset (block, blockmail -> cvt)) {
							if (block -> binary && (! (st = block_code_binary (block))))
								log_out (blockmail -> lg, LV_ERROR, "Unable to decode binary part in block %d in %s", block -> nr, blockmail -> fname);
						} else
							log_out (blockmail -> lg, LV_ERROR, "Unable to setup charset in block %d in %s", block -> nr, blockmail -> fname);
					} else
						log_out (blockmail -> lg, LV_ERROR, "Unable to extract content of block %d in %s", block -> nr, blockmail -> fname);
				} else if (! xmlstrcmp (child -> name, "tagposition")) {
					xmlChar		*name;
					tagpos_t	*tpos;
				
					name = xmlGetProp (child, char2xml ("name"));
					if (name) {
						DO_EXPAND (tpos, block, tagpos);
						if (tpos) {
							xmlBufferCat (tpos -> name, name);
							if (extract_numeric_property (blockmail, & val, child, "hash"))
								tpos -> hash = val;
							else
								tpos -> hash = 0;
							if (extract_numeric_property (blockmail, & val, child, "type"))
								tpos -> type = val;
							tagpos_find_name (tpos);
							if (child -> children)
								st = parse_tagposition (blockmail, doc, child -> children, tpos);
						} else
							st = false;
					} else {
						log_out (blockmail -> lg, LV_ERROR, "Missing properties in element %s in %s", child -> name, blockmail -> fname);
						st = false;
					}
					if (name)
						xmlFree (name);
				} else
					unknown (blockmail, child);
				if (! st)
					invalid (blockmail, child);
			}
		if (st && block -> condition)
			if (! eval_set_condition (blockmail -> eval, SP_BLOCK, block -> bid, block -> condition)) {
				log_out (blockmail -> lg, LV_ERROR, "Unable to setup block for BlockID %d in %s", block -> bid, blockmail -> fname);
				st = false;
			}
	} else {
		log_out (blockmail -> lg, LV_ERROR, "Missing number in block in %s", blockmail -> fname);
		st = false;
	}
	return st;
}/*}}}*/
static bool_t
parse_blocks (blockmail_t *blockmail, xmlDocPtr doc, xmlNodePtr base) /*{{{*/
{
	bool_t		st;
	xmlNodePtr	node;
	
	st = true;
	log_idpush (blockmail -> lg, "blocks");
	for (node = base; node && st; node = node -> next)
		if (node -> type == XML_ELEMENT_NODE) {
			if (! xmlstrcmp (node -> name, "block")) {
				block_t	*block;

				DO_EXPAND (block, blockmail, block);
				if (block) {
					st = parse_block (blockmail, doc, node, block);
					if (! st) {
						DO_SHRINK (blockmail, block);
					}
				} else
					st = false;
			} else
				unknown (blockmail, node);
			if (! st)
				invalid (blockmail, node);
		}
	log_idpop (blockmail -> lg);
	return st;
}/*}}}*/
static bool_t
parse_fixdata (blockmail_t *blockmail, xmlDocPtr doc, xmlNodePtr base, fix_t *f) /*{{{*/
{
	bool_t		st;
	xmlNodePtr	node;
	
	st = true;
	log_idpush (blockmail -> lg, "fixdata");
	for (node = base; node && st; node = node -> next)
		if (node -> type == XML_ELEMENT_NODE) {
			if (! xmlstrcmp (node -> name, "fixdata")) {
				char	*valid;
				int	mode;
				
				if (valid = extract_property (blockmail, node, "valid")) {
					if (! strcmp (valid, "simple"))
						mode = 1;
					else if (! strcmp (valid, "attach"))
						mode = 2;
					else if (! strcmp (valid, "all"))
						mode = 3;
					else
						mode = 0;
					if ((! (mode & 1 ? extract_content (& f -> cont, doc, node) : true)) ||
					    (! (mode & 2 ? extract_content (& f -> acont, doc, node) : true))) {
						log_out (blockmail -> lg, LV_ERROR, "Unable to get fixdata for %s (%d) in %s", valid, mode, blockmail -> fname);
						st = false;
					}
					fix_scan_for_dynamic_content (f);
					free (valid);
				} else {
					log_out (blockmail -> lg, LV_ERROR, "Missing valid in fixdata in %s", blockmail -> fname);
					st = false;
				}
			} else
				unknown (blockmail, node);
			if (! st)
				invalid (blockmail, node);
		}
	log_idpop (blockmail -> lg);
	return st;
}/*}}}*/
static bool_t
parse_blockspec (blockmail_t *blockmail, blockspec_t *bspec, xmlDocPtr doc, xmlNodePtr base) /*{{{*/
{
	bool_t		st;
	xmlNodePtr	node;
	
	st = true;
	log_idpush (blockmail -> lg, "blockspec");
	for (node = base; node && st; node = node -> next)
		if (node -> type == XML_ELEMENT_NODE) {
			if (! xmlstrcmp (node -> name, "prefix")) {
				if (! (st = parse_fixdata (blockmail, doc, node -> children, bspec -> prefix)))
					log_out (blockmail -> lg, LV_ERROR, "Unable to get prefix in blockspec %d in %s", bspec -> nr, blockmail -> fname);
			} else if (! xmlstrcmp (node -> name, "postfix")) {
				long		val;
				postfix_t	*postfix;

				if (extract_numeric_property (blockmail, & val, node, "output")) {
					DO_EXPAND (postfix, bspec, postfix);
					if (postfix) {
						postfix -> pid = extract_property (blockmail, node, "pid");
						postfix -> after = (int) val;
						postfix -> ref = bspec;
						if (! (st = parse_fixdata (blockmail, doc, node -> children, postfix -> c)))
							log_out (blockmail -> lg, LV_ERROR, "Unable to get postfix in blockspec %d in %s", bspec -> nr, blockmail -> fname);
					} else
						st = false;
				} else {
					log_out (blockmail -> lg, LV_ERROR, "Missing output in postfix in blockspec %d in %s", bspec -> nr, blockmail -> fname);
					st = false;
				}
			} else
				unknown (blockmail, node);
			if (! st)
				invalid (blockmail, node);
		}
	log_idpop (blockmail -> lg);
	return st;
}/*}}}*/
static bool_t
parse_type (blockmail_t *blockmail, mailtypedefinition_t *mtyp, xmlDocPtr doc, xmlNodePtr base) /*{{{*/
{
	bool_t		st;
	xmlNodePtr	node;
	long		val;
	char		*ptr;
	
	st = true;
	log_idpush (blockmail -> lg, "type");
	for (node = base; node && st; node = node -> next)
		if (node -> type == XML_ELEMENT_NODE) {
			if (! xmlstrcmp (node -> name, "blockspec")) {
				if (st = extract_numeric_property (blockmail, & val, node, "nr")) {
					int		n;
					blockspec_t	*bspec;
					
					for (n = 0; n < blockmail -> block_count; ++n)
						if (blockmail -> block[n] -> nr == (int) val)
							break;
					if (n < blockmail -> block_count) {
						DO_EXPAND (bspec, mtyp, blockspec);
						if (bspec) {
							bspec -> nr = (int) val;
							bspec -> block = blockmail -> block[n];
							if (extract_numeric_property (blockmail, & val, node, "linelength"))
								bspec -> linelength = (int) val;
							if (ptr = extract_property (blockmail, node, "onepixlog")) {
								if (! strcmp (ptr, "top"))
									bspec -> opl = OPL_Top;
								else if (! strcmp (ptr, "bottom"))
									bspec -> opl = OPL_Bottom;
								else
									bspec -> opl = OPL_None;
								free (ptr);
							}
							if (ptr = extract_property (blockmail, node, "clearance")) {
								bspec -> clearance = atob (ptr);
								free (ptr);
							}
							if (! bspec -> block -> binary)
								blockspec_find_lineseparator (bspec);
							st = parse_blockspec (blockmail, bspec, doc, node -> children);
							if (! st)
								DO_SHRINK (mtyp, blockspec);
						} else
							st = false;
					} else if ((! blockmail -> offline_picture_prefix) || (! blockmail -> opp_len)) {
						log_out (blockmail -> lg, LV_ERROR, "blockspec %d has no matching block in %s", (int) val, blockmail -> fname);
						st = false;
					}
				} else
					log_out (blockmail -> lg, LV_ERROR, "Missing number in blockspec in %s", blockmail -> fname);
			} else
				unknown (blockmail, node);
			if (! st)
				invalid (blockmail, node);
		}
	log_idpop (blockmail -> lg);
	return st;
}/*}}}*/
static bool_t
parse_types (blockmail_t *blockmail, xmlDocPtr doc, xmlNodePtr base) /*{{{*/
{
	bool_t		st;
	xmlNodePtr	node;
	
	st = true;
	log_idpush (blockmail -> lg, "types");
	for (node = base; node && st; node = node -> next)
		if (node -> type == XML_ELEMENT_NODE) {
			if (! xmlstrcmp (node -> name, "type")) {
				char	*sval;

				if (sval = extract_property (blockmail, node, "id")) {
					mailtypedefinition_t	*mtyp;
					
					DO_EXPAND (mtyp, blockmail, mailtypedefinition);
					if (mtyp) {
						mtyp -> ident = sval;
						mtyp -> idnr = atoi (sval);
						mtyp -> offline = mtyp -> idnr == 2;
						st = parse_type (blockmail, mtyp, doc, node -> children);
						if (! st)
							DO_SHRINK (blockmail, mailtypedefinition);
					} else {
						free (sval);
						st = false;
					}
				} else
					log_out (blockmail -> lg, LV_ERROR, "Missing mailtype definition in type in %s", blockmail -> fname);
			} else
				unknown (blockmail, node);
			if (! st)
				invalid (blockmail, node);
		}
	log_idpop (blockmail -> lg);
	return st;
}/*}}}*/
static bool_t
parse_layout (blockmail_t *blockmail, xmlDocPtr doc, xmlNodePtr base) /*{{{*/
{
	bool_t		st;
	xmlNodePtr	node;
	char		*name;
	char		*ref;
	char		*type;
	field_t		*field;
	
	st = true;
	log_idpush (blockmail -> lg, "layout");
	for (node = base; node && st; node = node -> next)
		if (node -> type == XML_ELEMENT_NODE) {
			if (! xmlstrcmp (node -> name, "element")) {
				name = extract_property (blockmail, node, "name");
				ref = extract_property (blockmail, node, "ref");
				type = extract_property (blockmail, node, "type");
				if (name && type) {
					int	pos;
					
					pos = blockmail -> field_count;
					DO_EXPAND (field, blockmail, field);
					if (field) {
						if (type[0] == 'i' && (! type[1]))
							type[0] = 'n';
						field -> name = name;
						name = NULL;
						field -> ref = ref;
						ref = NULL;
						field_normalize_name (field);
						field -> type = *type;
						if ((blockmail -> mailtype_index == -1) &&
						    (! field -> ref) &&
						    (! strcmp (field -> lname, "mailtype")))
							blockmail -> mailtype_index = pos;
					} else
						st = false;
				}
				if (name)
					free (name);
				if (ref)
					free (ref);
				if (type)
					free (type);
			} else
				unknown (blockmail, node);
			if (! st)
				invalid (blockmail, node);
		}
	if (st && blockmail -> field_count > 0) {
		if (blockmail -> eval) {
			int	failpos;
		
			if (! eval_set_variables (blockmail -> eval, blockmail -> field, blockmail -> field_count, & failpos)) {
				log_out (blockmail -> lg, LV_ERROR, "Unable to set variables for evaluator [%d: %s]", failpos, ((failpos >= 0) && (failpos < blockmail -> field_count) && blockmail -> field[failpos] ? blockmail -> field[failpos] -> name : "*unknown*"));
				st = false;
			}
		}
	}
	log_idpop (blockmail -> lg);
	return st;
}/*}}}*/
static bool_t
parse_tag (blockmail_t *blockmail, tag_t **tbase, xmlDocPtr doc, xmlNodePtr base) /*{{{*/
{
	bool_t		st;
	xmlNodePtr	node;
	tag_t		*prev, *temp;
	
	st = true;
	if (*tbase)
		for (prev = *tbase; temp = prev -> next; prev = temp)
			;
	else
		prev = NULL;
	log_idpush (blockmail -> lg, "tag");
	for (node = base; node && st; node = node -> next)
		if (node -> type == XML_ELEMENT_NODE) {
			if (! xmlstrcmp (node -> name, "tag")) {
				xmlChar		*name;
				xmlBufferPtr	value;
				long		hash;
				
				name = xmlGetProp (node, char2xml ("name"));
				if (name) {
					if (temp = tag_alloc ()) {
						xmlBufferCat (temp -> name, name);
						if (temp -> cname = xml2string (temp -> name)) {
							temp -> ttype = extract_property (blockmail, node, "type");
							if (temp -> ttype && (temp -> topt = strchr (temp -> ttype, ':')))
								*(temp -> topt)++ = '\0';
							if (extract_numeric_property (blockmail, & hash, node, "hash"))
								temp -> hash = hash;
							else
								temp -> hash = 0;
							value = NULL;
							if (extract_content (& value, doc, node)) {
								xmlBufferAdd (temp -> value, xmlBufferContent (value), xmlBufferLength (value));
								xmlBufferFree (value);
							}
							tag_parse (temp, blockmail);
							if (prev)
								prev -> next = temp;
							else
								*tbase = temp;
							prev = temp;
						} else {
							temp = tag_free (temp);
							st = false;
						}
					} else
						st = false;
				} else {
					log_out (blockmail -> lg, LV_ERROR, "Missing properties in tag in %s", blockmail -> fname);
					st = false;
				}
				if (name)
					xmlFree (name);
			} else
				unknown (blockmail, node);
			if (! st)
				invalid (blockmail, node);
		}
	log_idpop (blockmail -> lg);
	return st;
}/*}}}*/
static bool_t
parse_dyncont (blockmail_t *blockmail, xmlDocPtr doc, xmlNodePtr base, dyn_t *dyn) /*{{{*/
{
	bool_t		st;
	xmlNodePtr	node;
	
	st = true;
	log_idpush (blockmail -> lg, "content");
	for (node = base; node && st; node = node -> next)
		if (node -> type == XML_ELEMENT_NODE) {
			if (! xmlstrcmp (node -> name, "block")) {
				block_t	*block;

				DO_EXPAND (block, dyn, block);
				if (block) {
					st = parse_block (blockmail, doc, node, block);
					if (! st)
						DO_SHRINK (dyn, block);
				} else
					st = false;
			} else
				unknown (blockmail, node);
			if (! st)
				invalid (blockmail, node);
		}
	log_idpop (blockmail -> lg);
	return st;
}/*}}}*/
static bool_t
parse_dynamic (blockmail_t *blockmail, xmlDocPtr doc, xmlNodePtr base,
	       char *name, dyn_t **root) /*{{{*/
{
	bool_t		st;
	xmlNodePtr	node;
	dyn_t		*prv, *run, *tmp;
	
	st = true;
	*root = NULL;
	prv = NULL;
	log_idpush (blockmail -> lg, "dynamic");
	for (node = base; node && st; node = node -> next)
		if (node -> type == XML_ELEMENT_NODE) {
			if (! xmlstrcmp (node -> name, "dyncont")) {
				long	did, order;

				if (extract_numeric_property (blockmail, & did, node, "id") &&
				    extract_numeric_property (blockmail, & order, node, "order")) {
					if (tmp = dyn_alloc (did, order)) {
						tmp -> condition = extract_xml_property (node, "condition");
						if (! extract_numeric_property (blockmail, & tmp -> target_id, node, "target_id")) {
							tmp -> target_id = 0;
						}
						if (! *root) {
							if (! (tmp -> name = strdup (name)))
								st = false;
							*root = tmp;
						} else {
							for (run = *root, prv = NULL; run; run = run -> sibling)
								if (run -> order > tmp -> order)
									break;
								else
									prv = run;
							if (! prv) {
								tmp -> sibling = *root;
								tmp -> name = (*root) -> name;
								(*root) -> name = NULL;
								*root = tmp;
							} else {
								tmp -> sibling = prv -> sibling;
								prv -> sibling = tmp;
							}
						}
						st = parse_dyncont (blockmail, doc, node -> children, tmp);
						if (st && tmp -> condition) {
							if (! eval_set_condition (blockmail -> eval, SP_DYNAMIC, tmp -> did, tmp -> condition)) {
								log_out (blockmail -> lg, LV_ERROR, "Unable to setup condition for DynID %d in %s", tmp -> did, blockmail -> fname);
								st = false;
							}
						}
					} else
						st = false;
				} else {
					log_out (blockmail -> lg, LV_ERROR, "Missing properties for dynamic content");
					st = false;
				}
			} else
				unknown (blockmail, node);
			if (! st)
				invalid (blockmail, node);
		}
	log_idpop (blockmail -> lg);
	return st;
}/*}}}*/
static bool_t
parse_dynamics (blockmail_t *blockmail, xmlDocPtr doc, xmlNodePtr base) /*{{{*/
{
	bool_t		st;
	xmlNodePtr	node;
	dyn_t		*prv, *tmp;
	
	st = true;
	prv = NULL;
	log_idpush (blockmail -> lg, "dynamics");
	for (node = base; node && st; node = node -> next)
		if (node -> type == XML_ELEMENT_NODE) {
			if (! xmlstrcmp (node -> name, "dynamic")) {
				long	did;
				char	*name;
				
				if (extract_numeric_property (blockmail, & did, node, "id") &&
				    (name = extract_property (blockmail, node, "name"))) {
					char	*interest = extract_property (blockmail, node, "interest");

					tmp = NULL;
					st = parse_dynamic (blockmail, doc, node -> children, name, & tmp);
					if (tmp)
						if (st) {
							extract_boolean_property (blockmail, & tmp -> disable_link_extension, node, "disable_link_extension");
							if (interest) {
								tmp -> interest = interest;
								interest = NULL;
								if (! dyn_assign_interest_field (tmp, blockmail -> field, blockmail -> field_count)) {
									log_out (blockmail -> lg, LV_ERROR, "Unable to find interest field \"%s\" for dynamic \"%s\"", tmp -> interest, tmp -> name);
									st = false;
								}
							}
							if (prv)
								prv -> next = tmp;
							else
								blockmail -> dyn = tmp;
							prv = tmp;
							blockmail -> dynamic_count++;
						} else
							dyn_free (tmp);
					if (interest)
						free (interest);
					free (name);
				} else {
					log_out (blockmail -> lg, LV_ERROR, "Missing properties for dynamic part");
					st = false;
				}
			} else
				unknown (blockmail, node);
			if (! st)
				invalid (blockmail, node);
		}
	log_idpop (blockmail -> lg);
	return st;
}/*}}}*/
static bool_t
parse_urls (blockmail_t *blockmail, xmlDocPtr doc, xmlNodePtr base) /*{{{*/
{
	bool_t		st;
	xmlNodePtr	node;
	
	st = true;
	log_idpush (blockmail -> lg, "urls");
	for (node = base; node && st; node = node -> next)
		if (node -> type == XML_ELEMENT_NODE) {
			if (! xmlstrcmp (node -> name, "url")) {
				long		url_id, usage;
				xmlBufferPtr	dest;

				if (extract_numeric_property (blockmail, & url_id, node, "id") &&
				    extract_numeric_property (blockmail, & usage, node, "usage") &&
				    (dest = extract_xml_property (node, "destination"))) {
					url_t	*url;

					DO_EXPAND (url, blockmail, url);
					if (url) {
						url -> url_id = url_id;
						url_set_destination (url, dest);
						url -> usage = usage;
						extract_boolean_property (blockmail, & url -> admin_link, node, "admin_link");
						url_set_original (url, extract_xml_property (node, "original_url"));
					} else {
						xmlBufferFree (dest);
						st = false;
					}
				} else {
					log_out (blockmail -> lg, LV_ERROR, "Missing properties for url");
					st = false;
				}
			} else
				unknown (blockmail, node);
			if (! st)
				invalid (blockmail, node);
		}
	log_idpop (blockmail -> lg);
	return st;
}/*}}}*/
static bool_t
parse_details (blockmail_t *blockmail, xmlDocPtr doc, xmlNodePtr base,
	       receiver_t *rec) /*{{{*/
{
	bool_t		st;
	xmlNodePtr	node;
	record_t	*cur;
	
	st = true;
	cur = NULL;
	log_idpush (blockmail -> lg, "details");
	for (node = base; node && st; node = node -> next)
		if (node -> type == XML_ELEMENT_NODE) {
			if (! xmlstrcmp (node -> name, "record")) {
				st = dataset_new_record (rec -> rvdata, extract_xml_properties (blockmail, node));
				if (st) {
					cur = rec -> rvdata -> cur;
				}
			} else {
				if (! cur) {
					st = dataset_new_record (rec -> rvdata, NULL);
					if (st) {
						cur = rec -> rvdata -> cur;
					}
				}
				if (! xmlstrcmp (node -> name, "tags")) {
					st = parse_tag (blockmail, & cur -> tag, doc, node -> children);
					if (! st)
						log_out (blockmail -> lg, LV_ERROR, "Unable to parse tag for receiver %d in %s", rec -> customer_id, blockmail -> fname);
				} else if (! xmlstrcmp (node -> name, "data")) {
					if (cur -> data) {
						if (cur -> dpos < cur -> dsize) {
							extract_boolean_property (blockmail, & cur -> isnull[cur -> dpos], node, "null");
							st = extract_content (& cur -> data[cur -> dpos], doc, node);
							if (st) {
								cur -> dpos++;
							}
						} else
							log_out (blockmail -> lg, LV_WARNING, "Got more data as expected (%d) for receiver %d in %s", cur -> dsize, rec -> customer_id, blockmail -> fname);
					} else
						log_out (blockmail -> lg, LV_WARNING, "Got data even no data is expected for receiver %d in %s", rec -> customer_id, blockmail -> fname);
				} else if (! xmlstrcmp (node -> name, "target_group")) {
					if (blockmail -> target_groups && cur -> target_group) {
						char	*target_group_value = extract_simple_content (blockmail, doc, node);
						int	target_group_len = target_group_value ? strlen (target_group_value) : 0;
						int	n;
					
						for (n = 0; n < cur -> target_group_count; ++n) {
							cur -> target_group[n] = (n < target_group_len) && (target_group_value[n] == '1');
						}
						if (target_group_value)
							free (target_group_value);
					}
				} else
					unknown (blockmail, node);
			}
		}
	dataset_select_first_record (rec -> rvdata);
	log_idpop (blockmail -> lg);
	return st;
}/*}}}*/
static bool_t
parse_receivers (blockmail_t *blockmail, xmlDocPtr doc, xmlNodePtr base) /*{{{*/
{
	bool_t		st;
	receiver_t	*rec;
	xmlNodePtr	node;
	long		val;
	char		*ptr;
	
	st = false;
	log_idpush (blockmail -> lg, "receivers");
	if (rec = receiver_alloc (blockmail, blockmail -> field_count)) {
		st = true;
		for (node = base; node && st; node = node -> next) {
			if (node -> type == XML_ELEMENT_NODE) {
				if (! xmlstrcmp (node -> name, "receiver")) {
					xmlChar	*temp;

					st = false;
					if (extract_numeric_property (blockmail, & val, node, "customer_id")) {
						int		bcccount = 0;
						xmlAttrPtr	attr;
						media_target_t	*cur, *prev;

						rec -> customer_id = (int) val;
						for (attr = node -> properties, prev = NULL; attr; attr = attr -> next) {
							if (attr -> name && (! memcmp (attr -> name, "to_", 3))) {
								char	*media = strchr ((const char *) attr -> name, '_');
								
								if (media) {
									++media;
									if (temp = xmlGetProp (node, attr -> name)) {
										if (cur = media_target_alloc (media, temp)) {
											if (prev)
												prev -> next = cur;
											else
												rec -> media_target = cur;
											prev = cur;
										}
										xmlFree (temp);
									}
								}
							}
						}
						if (ptr = extract_property (blockmail, node, "user_type")) {
							rec -> user_type = *ptr;
							free (ptr);
						}
						if (ptr = extract_property (blockmail, node, "tracking_veto")) {
							rec -> tracking_veto = atob (ptr);
							free (ptr);
						}
						if (temp = xmlGetProp (node, char2xml ("message_id"))) {
							xmlBufferCat (rec -> message_id, temp);
							xmlFree (temp);
						} else
							receiver_make_message_id (rec, blockmail);
						if (ptr = extract_property (blockmail, node, "bcc")) {
							char	*i;
							int	count;
							
							if (rec -> bcc) {
								if (rec -> bcc[0])
									free (rec -> bcc[0]);
								free (rec -> bcc);
							}
							for (i = ptr, count = 0; i; ++count)
								if (i = strchr (i, ','))
									++i;
							if (rec -> bcc = (char **) malloc (sizeof (char *) * (count + 1))) {
								for (i = ptr, count = 0; i; ++count) {
									rec -> bcc[count] = i;
									if (i = strchr (i, ','))
										*i++ = '\0';
								}
								rec -> bcc[count] = NULL;
								bcccount = count;
							} else
								free (ptr);
						}

						receiver_set_data_default (rec);
						if (extract_numeric_property (blockmail, & val, node, "mailtype")) {
							rec -> mailtype = (int) val;
							if (rec -> mailtype == 3) {
								rec -> mailtype = Mailtype_HTML_Offline;
							}
							rec -> mediatypes = extract_property (blockmail, node, "mediatypes");
							rec -> user_statuses = extract_property (blockmail, node, "user_status");
							if (parse_details (blockmail, doc, node -> children, rec)) {
								st = true;
								rec -> dkim = blockmail -> signdkim && sdkim_should_sign (blockmail -> signdkim, rec);
								log_idpush (blockmail -> lg, "create");
								st = create_output (blockmail, rec);
								if (blockmail -> eval)
									eval_done_match (blockmail -> eval);
								log_idpop (blockmail -> lg);
								if (st) {
									log_idpush (blockmail -> lg, "write");
									if (! blockmail_insync (blockmail, rec, bcccount)) {
										st = (*blockmail -> output -> owrite) (blockmail -> outputdata, blockmail, rec);
										if (st)
											st = blockmail_tosync (blockmail, rec, bcccount);
									}
									log_idpop (blockmail -> lg);
									if (! st)
										log_out (blockmail -> lg, LV_ERROR, "Unable to write output for receiver %d in %s", rec -> customer_id, blockmail -> fname);
									else
										blockmail -> receiver_count++;
								} else
									log_out (blockmail -> lg, LV_ERROR, "Unable to create output for receiver %d in %s", rec -> customer_id, blockmail -> fname);
								if (blockmail -> eval)
									eval_done_data (blockmail -> eval);
							} else
								log_out (blockmail -> lg, LV_ERROR, "Unable to parse details for receiver %d in %s", rec -> customer_id, blockmail -> fname);
						} else
							log_out (blockmail -> lg, LV_ERROR, "Missing property mailtype in receiver %d in %s", rec -> customer_id, blockmail -> fname);
						receiver_clear (rec);
					} else
						log_out (blockmail -> lg, LV_ERROR, "Missing cutomer_id in receiver in %s", blockmail -> fname);
				} else
					unknown (blockmail, node);
			}
			if (! st)
				invalid (blockmail, node);
		}
		receiver_free (rec);
	}
	log_idpop (blockmail -> lg);
	return st;
}/*}}}*/

static bool_t
parse_blockmail (blockmail_t *blockmail, xmlDocPtr doc, xmlNodePtr base) /*{{{*/
{
	bool_t		st;
	xmlNodePtr	node;
	
	st = true;
	log_idpush (blockmail -> lg, "blockmail");
	for (node = base; node && st; node = node -> next)
		if (node -> type == XML_ELEMENT_NODE) {
			if (! xmlstrcmp (node -> name, "description"))
				st = parse_description (blockmail, doc, node -> children);
			else if (! xmlstrcmp (node -> name, "general"))
				st = parse_general (blockmail, doc, node -> children);
			else if (! xmlstrcmp (node -> name, "mailcreation"))
				st = parse_mailcreation (blockmail, doc, node -> children);
			else if (! xmlstrcmp (node -> name, "trackers"))
                                st = parse_trackers (blockmail, doc, node -> children);
			else if (! xmlstrcmp (node -> name, "mediatypes")) {
				st = parse_mediatypes (blockmail, doc, node -> children);
				if (st)
					st = blockmail_extract_mediatypes (blockmail);
			} else if (! xmlstrcmp (node -> name, "blocks")) {
				blockmail_setup_offline_picture_prefix (blockmail);
				st = parse_blocks (blockmail, doc, node -> children);
			} else if (! xmlstrcmp (node -> name, "types"))
				st = parse_types (blockmail, doc, node -> children);
			else if (! xmlstrcmp (node -> name, "layout"))
				st = parse_layout (blockmail, doc, node -> children);
			else if (! xmlstrcmp (node -> name, "target_groups")) {
				char	*value = extract_simple_content (blockmail, doc, node);
				
				if (value) {
					long		target_id;
					long		*target_groups;
					int		count, size;
					char		*cur, *ptr;
					
					target_groups = NULL;
					count = 0;
					size = 0;
					for (ptr = value; ptr; ) {
						cur = ptr;
						if (ptr = strchr (ptr, ',')) {
							*ptr++ = '\0';
							while (isspace (*ptr))
								++ptr;
						}
						target_id = atol (cur);
						if (target_id > 0) {
							if (count >= size) {
								size += size + 8;
								if (! (target_groups = (long *) realloc (target_groups, size * sizeof (long)))) {
									size = 0;
									count = 0;
								}
							}
							if (count < size)
								target_groups[count++] = target_id;
						}
					}
					blockmail -> target_groups = target_groups;
					blockmail -> target_groups_count = count;
					free (value);
				}
			} else if (! xmlstrcmp (node -> name, "taglist")) {
				tag_t	*tmp;
				
				st = parse_tag (blockmail, & blockmail -> ltag, doc, node -> children);
				for (tmp = blockmail -> ltag, blockmail -> taglist_count = 0; tmp; tmp = tmp -> next)
					blockmail -> taglist_count++;
			} else if (! xmlstrcmp (node -> name, "global_tags")) {
				tag_t	*tmp;
				
				st = parse_tag (blockmail, & blockmail -> gtag, doc, node -> children);
				for (tmp = blockmail -> gtag, blockmail -> globaltag_count = 0; tmp; tmp = tmp -> next)
					blockmail -> globaltag_count++;
			} else if (! xmlstrcmp (node -> name, "dynamics"))
				st = parse_dynamics (blockmail, doc, node -> children);
			else if (! xmlstrcmp (node -> name, "urls"))
				st = parse_urls (blockmail, doc, node -> children);
			else if (! xmlstrcmp (node -> name, "receivers")) {
				blockmail_setup_company_configuration (blockmail);
				blockmail_setup_mfrom (blockmail);
				blockmail_setup_dkim (blockmail);
				blockmail_setup_vip_block (blockmail);
				blockmail_setup_onepixel_template (blockmail);
				blockmail_setup_tagpositions (blockmail);
				blockmail_setup_preevaluated_targets (blockmail);
				st = eval_setup (blockmail -> eval);
				if (! st)
					log_out (blockmail -> lg, LV_ERROR, "Failed to setup evaluation");
				else
					st = parse_receivers (blockmail, doc, node -> children);
			} else
				unknown (blockmail, node);
			if (! st)
				invalid (blockmail, node);
		}
	log_idpop (blockmail -> lg);
	return st;
}/*}}}*/

bool_t
parse_file (blockmail_t *blockmail, xmlDocPtr doc, xmlNodePtr base) /*{{{*/
{
	bool_t		st;
	xmlNodePtr	node;

	st = false;
	for (node = base; node; node = node -> next)
		if ((node -> type == XML_ELEMENT_NODE) && (! xmlstrcmp (node -> name, "blockmail"))) {
			st = parse_blockmail (blockmail, doc, node -> children);
			if (blockmail -> eval) {
				eval_done_variables (blockmail -> eval);
				eval_done_condition (blockmail -> eval);
			}
		}
	return st;
}/*}}}*/
