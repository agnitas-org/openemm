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
# include	<ctype.h>
# include	<string.h>
# include	"xmlback.h"

static bool_t
use_block (block_t *block, links_t *links) /*{{{*/
{
	bool_t	rc;

	rc = false;
	if (block -> attachment) {
		rc = true;
	} else {
		if (links && block -> cid) {
			int	n;
		
			for (n = 0; n < links -> lcnt; ++n)
				if (((! links -> seen) || (! links -> seen[n])) &&
				    (! strcmp (block -> cid, links -> l[n]))) {
					if (links -> seen)
						links -> seen[n] = true;
					rc = true;
					break;
				}
		}
	}
	return rc;
}/*}}}*/
static bool_t
match_block (blockmail_t *blockmail, block_t *block, receiver_t *rec) /*{{{*/
{
	return block_match (block, blockmail -> eval, rec);
}/*}}}*/
static const xmlChar *
replace_head (const xmlChar *ch, int clen, int *rlen) /*{{{*/
{
	if ((clen == 1) && iscntrl (*ch))
		return NULL;
	*rlen = clen;
	return ch;
}/*}}}*/
static xmlBufferPtr
fixer (fix_t *fix, int attcount, blockmail_t *blockmail, receiver_t *rec, bool_t *dyn) /*{{{*/
{
	xmlBufferPtr	cont;
	xmlBufferPtr	prefix;

	if (attcount) {
		cont = fix -> acont;
		*dyn = fix -> adyn;
	} else {
		cont = fix -> cont;
		*dyn = fix -> dyn;
	}
	if (*dyn)
		prefix = string_map (cont, rec -> smap, blockmail -> smap);
	else
		prefix = cont;
	return prefix;
}/*}}}*/
static bool_t
create_mail (blockmail_t *blockmail, receiver_t *rec) /*{{{*/
{
	int			n, m;
	bool_t			st;
	int			attcount;
	links_t			*links;
	postfix_t		*postfixes;
	mailtypedefinition_t	*mtyp;
	blockspec_t		*bspec;
	block_t			*block;
	block_t			*header;
	block_t			*preheader;
	block_t			*clearance;
	rblock_t		*rbprev;
	bool_t			changed;
	
	mtyp = (rec -> mailtype >= 0) && (rec -> mailtype < blockmail -> mailtypedefinition_count) ? blockmail -> mailtypedefinition[rec -> mailtype] : NULL;
	if (! mtyp) {
		log_out (blockmail -> lg, LV_DEBUG, "No mailtype definition (%d) for receiver %d found, fallback to default", rec -> mailtype, rec -> customer_id);
		if (blockmail -> mailtypedefinition_count > 1) {
			mtyp = blockmail -> mailtypedefinition[1];
		} else {
			log_out (blockmail -> lg, LV_ERROR, "No default mailtype definition found, aborting");
			return false;
		}
	}
	st = true;
	attcount = 0;

	/*
	 * 1. Stage: check for usful blocks, count attachments and
	 *           create the content part */
	links = mtyp -> offline ? links_alloc () : NULL;
	header = NULL;
	preheader = NULL;
	clearance = NULL;
	for (n = 0; st && (n < mtyp -> blockspec_count); ++n) {
		bspec = mtyp -> blockspec[n];
		block = bspec -> block;
		changed = false;
		if (blockmail -> eval && (blockmail -> mailtype_index != -1)) {
			int	idx = -1;
			
			if (block -> tid == TID_EMail_Text) {
				idx = 0;
				changed = true;
			} else if (block -> tid == TID_EMail_HTML) {
				idx = 1;
				changed = true;
			}
			if (changed)
				eval_change_data (blockmail -> eval, blockmail -> mtbuf[idx], false, blockmail -> mailtype_index);
		}
		if ((block -> mediatype != Mediatype_Unspec) && (block -> mediatype != Mediatype_EMail)) {
			block -> inuse = false;
		} else {
			block -> inuse = match_block (blockmail, block, rec);
		}
		
		if (block -> inuse) {
			if ((block -> tid != TID_EMail_Head) &&
			    (block -> tid != TID_EMail_Text) &&
			    (block -> tid != TID_EMail_HTML) &&
			    (block -> tid != TID_EMail_HTML_Preheader) &&
			    (block -> tid != TID_EMail_HTML_Clearance)) {
				block -> inuse = use_block (block, links);
			} else if ((block -> tid == TID_EMail_HTML) || (block -> tid == TID_EMail_HTML_Preheader) || (block -> tid == TID_EMail_HTML_Clearance)) {
				block -> inuse = rec -> mailtype != Mailtype_Text;
			}
		}
		if (block -> inuse) {
			rec -> base_block = block;
			if (block -> attachment)
				attcount++;
			if (! block -> binary) {
				if (st) {
					log_idpush (blockmail -> lg, "replace_tags");
					st = replace_tags (blockmail, rec, block,
							   0, true,
							   (block -> tid != TID_EMail_Head ? NULL : replace_head),
							   (block -> tid == TID_EMail_Head ? NULL : blockmail -> selector),
							   block -> tid != TID_EMail_Text,
							   block -> pdf);
					log_idpop (blockmail -> lg);
					if (! st)
						log_out (blockmail -> lg, LV_ERROR, "Unable to replace tags in block %d for %d", block -> nr, rec -> customer_id);
					else if (block -> tid == TID_EMail_Head) {
						if (block -> nr == 0) {
							header = block;
						}
						if (st && header) {
							header_set_charset (rec -> header, blockmail -> cvt, header -> charset);
							header_set_content (rec -> header, header -> in);
						}
					} else if ((block -> tid == TID_EMail_HTML_Preheader) || (block -> tid == TID_EMail_HTML_Clearance)) {
						if (block -> tid == TID_EMail_HTML_Preheader) {
							if (! preheader)
								preheader = block;
						} else if (block -> tid == TID_EMail_HTML_Clearance) {
							if (! clearance)
								clearance = block;
						}
						block -> inuse = false;
					}
				}
				if (st && block -> inuse && (block -> tid != TID_EMail_Head)) {
					log_idpush (blockmail -> lg, "modify_output");
					st = modify_output (blockmail, rec, block, bspec, preheader, clearance, links);
					log_idpop (blockmail -> lg);
					if (! st)
						log_out (blockmail -> lg, LV_ERROR, "Unable to modify output in block %d for %d", block -> nr, rec -> customer_id);
				}
			}
		}
		if (changed && rec -> rvdata -> cur)
			eval_change_data (blockmail -> eval, rec -> rvdata -> cur -> data[blockmail -> mailtype_index], rec -> rvdata -> cur -> isnull[blockmail -> mailtype_index], blockmail -> mailtype_index);
	}
	if (links)
		links_free (links);
	
	/*
	 * 2.1. Stage finalize header */
	if (st && blockmail -> revalidate_mfrom) {
		st = header_revalidate_mfrom (rec -> header, blockmail -> spf);
		if (! st)
			log_out (blockmail -> lg, LV_ERROR, "Failed to revalidate mfrom for %d", rec -> customer_id);
	}

	/*
	 * 2.2. Stage: finalize blocks */
	for (n = 0; st && (n < mtyp -> blockspec_count); ++n) {
		bspec = mtyp -> blockspec[n];
		block = bspec -> block;
		if (! block -> binary) {
			if (block -> inuse) {
				if ((! blockmail -> raw) && (! block -> precoded)) {
					log_idpush (blockmail -> lg, "convert_character_set");
					st = convert_character_set (blockmail, block);
					log_idpop (blockmail -> lg);
					if (! st)
						log_out (blockmail -> lg, LV_ERROR, "Unable to convert chararcter set in block %d for %d", block -> nr, rec -> customer_id);
				} else {
					block_swap_inout (block);
				}
			} else {
				block_swap_inout (block);
			}
		}
	}

	/*
	 * 3. Stage: determinate the required postfixes */
	postfixes = NULL;
	for (n = 0; st && (n < mtyp -> blockspec_count); ++n) {
		bspec = mtyp -> blockspec[n];
		block = bspec -> block;
		if (block -> inuse) {
			postfix_t	*cur, *tmp, *prv;
				
			for (m = bspec -> postfix_count - 1; m >= 0; --m) {
				cur = bspec -> postfix[m];
				if (cur -> pid) {
					for (tmp = postfixes, prv = NULL; tmp; tmp = tmp -> stack)
						if (tmp -> pid && (! strcmp (tmp -> pid, cur -> pid)))
							break;
						else
							prv = tmp;
					if (tmp) {
						cur -> stack = tmp -> stack;
						if (prv)
							prv -> stack = cur;
						else
							postfixes = cur;
						cur = NULL;
					}
				}
				if (cur) {
					cur -> stack = postfixes;
					postfixes = cur;
				}
			}
		}
	}

	/*
	 * 4. Stage: create the output */
	rbprev = NULL;
	for (n = 0; st && (n <= mtyp -> blockspec_count); ++n) {
		if (n < mtyp -> blockspec_count) {
			bspec = mtyp -> blockspec[n];
			block = bspec -> block;
		} else {
			bspec = NULL;
			block = NULL;
		}
		if (blockmail -> raw) {
			if (st && block && (block -> inuse || (block == preheader) || (block == clearance))) {
				const char	*name;
				rblock_t	*rbtmp;
				
				switch (block -> tid) {
				default:			name = block -> cid;	break;
				case TID_EMail_Head:		name = "__head__";	break;
				case TID_EMail_Text:		name = "__text__";	break;
				case TID_EMail_HTML:		name = "__html__";	break;
				case TID_EMail_HTML_Preheader:	name = "__preheader__";	break;
				case TID_EMail_HTML_Clearance:	name = "__clearance__";	break;
				}
				if (rbtmp = rblock_alloc (block -> tid, name, block -> tid == TID_EMail_Head ? NULL : block -> out, block -> tid == TID_EMail_Head ? header_create (rec -> header, true) : NULL)) {
					if (rbprev)
						rbprev -> next = rbtmp;
					else
						blockmail -> rblocks = rbtmp;
					rbprev = rbtmp;
					buffer_appends (blockmail -> body, name);
					buffer_appends (blockmail -> body, "\n\n");
					append_cooked (blockmail -> body, rbtmp -> content, block -> charset, block -> method == EncBase64 && (! block -> precoded) ? block -> method : EncNone);
					buffer_appends (blockmail -> body, "\n\n");
				}
			}
		} else {
			if (postfixes) {
				postfix_t	*run, *prv;
				bool_t		dyn;
				xmlBufferPtr	postfix;
			
				for (run = postfixes, prv = NULL; st && run; run = run -> stack)
					if ((! block) || (run -> after < block -> nr)) {
						postfix = fixer (run -> c, attcount, blockmail, rec, & dyn);
						if (! postfix) {
							log_out (blockmail -> lg, LV_ERROR, "Unable to find postfix for block %d for %d", run -> ref -> block -> nr, rec -> customer_id);
							st = false;
						} else if (run -> ref -> block -> tid == TID_EMail_Head) {
							if (! header_append_content (rec -> header, postfix)) {
								log_out (blockmail -> lg, LV_ERROR, "Unable to append postfix for block %d for %d", run -> ref -> block -> nr, rec -> customer_id);
								st = false;
							}
						} else if (! append_cooked (blockmail -> body, postfix, run -> ref -> block -> charset, Enc8bit)) {
							log_out (blockmail -> lg, LV_ERROR, "Unable to append postfix for block %d for %d", run -> ref -> block -> nr, rec -> customer_id);
							st = false;
						}
						if (dyn && postfix)
							xmlBufferFree (postfix);
						if (prv)
							prv -> stack = run -> stack;
						else
							postfixes = run -> stack;
					} else
						prv = run;
			}
			if (st && block && block -> inuse) {
				bool_t		dyn;
				xmlBufferPtr	prefix;
			
				prefix = fixer (bspec -> prefix, attcount, blockmail, rec, & dyn);
				if ((! prefix) || (! append_cooked (blockmail -> body, prefix, block -> charset, Enc8bit))) {
					log_out (blockmail -> lg, LV_ERROR, "Unable to append %sprefix for block %d for %d", (dyn ? "dynamic " : ""), block -> nr, rec -> customer_id);
					st = false;
				}
				if (dyn && prefix)
					xmlBufferFree (prefix);
				if (st && (block -> tid != TID_EMail_Head)) {
					if (block -> precoded) {
						if (! append_cooked (blockmail -> body, block -> out, block -> charset, EncNone))
							st = false;
					} else if (! block -> binary) {
						if (! append_cooked (blockmail -> body, block -> out, block -> charset, block -> method))
							st = false;
					} else {
						if (! append_raw (blockmail -> body, block -> bout))
							st = false;
					}
					if (! st)
						log_out (blockmail -> lg, LV_ERROR, "Unable to append content of block %d for %d", block -> nr, rec -> customer_id);
				}
			}
		}
	}
	/*
	 * 5. cleanup header */
	header_cleanup (rec -> header, blockmail -> omit_list_informations_for_doi && (rec -> user_status == 5));
	/*
	 * 6. optional sign mail */
	if (rec -> dkim)
		sign_mail (blockmail, rec -> header);
	if (st) {
		rec -> size = header_size (rec -> header) + 2 + buffer_length (blockmail -> body);
	}
	return st;
}/*}}}*/
bool_t
create_output (blockmail_t *blockmail, receiver_t *rec) /*{{{*/
{
	bool_t	st;
	bool_t	(*docreate) (blockmail_t *, receiver_t *);
	media_t	*m;
	
	st = true;
	m = NULL;
	blockmail -> active = true;
	blockmail -> reason = REASON_UNSPEC;
	blockmail -> reason_detail = 0;
	if (blockmail -> reason_custom) {
		free (blockmail -> reason_custom);
		blockmail -> reason_custom = NULL;
	}
	buffer_clear (blockmail -> control);
	buffer_clear (blockmail -> body);
	if (blockmail -> raw && blockmail -> rblocks)
		blockmail -> rblocks = rblock_free_all (blockmail -> rblocks);
	if (rec -> mediatypes) {
		char		*copy, *cur, *ptr;
		const char	*user_statuses;
		mediatype_t	type;
		
		docreate = NULL;
		if (copy = strdup (rec -> mediatypes)) {
			for (cur = copy, user_statuses = rec -> user_statuses; st && cur && (! m); ) {
				if (ptr = strchr (cur, ','))
					*ptr++ = '\0';
				if (user_statuses) {
					rec -> user_status = atoi (user_statuses);
					if (user_statuses = strchr (user_statuses, ','))
						++user_statuses;
				} else
					rec -> user_status = 0;
				if (media_parse_type (cur, & type)) {
					int	n;

					for (n = 0; n < blockmail -> media_count; ++n)
						if (blockmail -> media[n] -> type == type) {
							m = blockmail -> media[n];
							if (m -> stat == MS_Active) {
								switch (type) {
								case Mediatype_EMail:
									docreate = create_mail;
									break;
								case Mediatype_Unspec:
									log_out (blockmail -> lg, LV_ERROR, "Invalid/unsupported target %d", type);
									st = false;
									break;
								}
							} else {
								blockmail -> active = false;
								blockmail -> reason = REASON_NO_MEDIA;
								blockmail -> reason_detail = type;
							}
							break;
						}
				} else
					st = false;
				cur = ptr;
			}
			free (copy);
		} else
			st = false;
	} else {
		docreate = create_mail;
		rec -> user_status = 0;
	}
	if (st) {
		rec -> media = m;
		rec -> chunks = 1;
		rec -> size = 0;
		strcpy (rec -> mid, m ? media_typeid (m -> type)  : "?");
		if ((! rec -> media) && blockmail -> active) {
			blockmail -> active = false;
			blockmail -> reason = REASON_UNMATCHED_MEDIA;
			blockmail -> reason_detail = 0;
		}
		if (blockmail -> active && docreate)
			st = (*docreate) (blockmail, rec);
	}
	return st;
}/*}}}*/
