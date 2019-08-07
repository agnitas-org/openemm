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
# ifndef	__XMLBACK_H
# define	__XMLBACK_H		1
# include	<stdio.h>
# include	<libxml/parser.h>
# include	<libxml/parserInternals.h>
# include	<libxml/xmlmemory.h>
# include	<libxml/xmlerror.h>
# include	"agn.h"
# include	"xml.h"

/*
 * These macros are used for variables with these properties
 * (assuming type = XXX):
 * 1.) The type fo the variable is XXX_t
 * 2.) It is part of the structure `what'
 * 3.) The structure has a member XXX declared as `XXX_t **XXX;'
 * 4.) The structure has a memeber XXX_count of type int
 *     (currently used elements in XXX)
 * 5.) The structure has a memeber XXX_size of type int
 *     (currently allocated space in XXX)
 * 
 * Special notice per macro:
 * - DO_EXPAND
 *   * grab is of type `XXX_t *' and is the next free slot (on success)
 *     or NULL on failure
 * - DO_SHRINK
 *   * removes and frees the last element in the array. A function with
 *     the prototype `XXX_t * XXX_free (XXX_t *);' is required to free
 *     that element
 *//*{{{*/
# define	DO_DECL(type)											\
		int		type ## _count;									\
		int		type ## _size;									\
		type ## _t	**type
# define	DO_ZERO(what, type)										\
		do {												\
			(what) -> type ## _count = 0;								\
			(what) -> type ## _size = 0;								\
			(what) -> type = NULL;									\
		}	while (0)
# define	DO_EXPAND(grab, what, type) 									\
		do {												\
			if ((what) -> type ## _count >= (what) -> type ## _size) {				\
				int		__tmpsize;							\
				type ## _t	**__tmp;							\
														\
				__tmpsize = ((what) -> type ## _size ? ((what) -> type ## _size << 1) : 8);	\
				if (__tmp = (type ## _t **)							\
				    realloc ((what) -> type, __tmpsize * sizeof (type ## _t *))) {		\
					(what) -> type = __tmp;							\
					(what) -> type ## _size = __tmpsize;					\
				}										\
			}											\
			(grab) = NULL;										\
			if (((what) -> type ## _count < (what) -> type ## _size) &&				\
			    ((what) -> type[(what) -> type ## _count] = type ## _alloc ()))			\
				(grab) = (what) -> type[(what) -> type ## _count++];				\
		}	while (0)
# define	DO_SHRINK(what, type)										\
		do {												\
			if ((what) -> type ## _count > 0) {							\
				(what) -> type ## _count--;							\
				if ((what) -> type[(what) -> type ## _count])					\
					(what) -> type[(what) -> type ## _count] =				\
						type ## _free ((what) -> type[(what) -> type ## _count]);	\
			}											\
		}	while (0)
# define	DO_CLEAR(what, type)										\
		do {												\
			if ((what) -> type) {									\
				int	__tmp;									\
														\
				for (__tmp = 0; __tmp < (what) -> type ## _count; ++__tmp)			\
					if ((what) -> type[__tmp])						\
						what -> type[__tmp] = type ## _free ((what) -> type[__tmp]);	\
			}											\
			(what) -> type ## _count = 0;								\
		}	while (0)
# define	DO_FREE(what, type)										\
		do {												\
			DO_CLEAR ((what), type);								\
			if ((what) -> type)									\
				free ((what) -> type);								\
			DO_ZERO ((what), type);									\
		}	while (0)
/*}}}*/

/* Tagpositions types */
# define	TP_NONE			0
# define	TP_DYNAMIC		(1 << 0)
# define	TP_DYNAMICVALUE		(1 << 1)
# define	IS_DYNAMIC(xxx)		((xxx) & (TP_DYNAMIC | TP_DYNAMICVALUE))

/* evaluation spheres */
# define	SP_DYNAMIC		0
# define	SP_BLOCK		1

typedef enum { /*{{{*/
	EncNone,
	EncHeader,
	Enc8bit,
	EncQuotedPrintable,
	EncBase64
	/*}}}*/
}	encoding_t;

typedef enum { /*{{{*/
	Mailtype_Text = 0,
	Mailtype_HTML = 1,
	Mailtype_HTML_Offline = 2
	/*}}}*/
}	mailtype_t;
typedef enum { /*{{{*/
	Mediatype_Unspec = -1,
	Mediatype_EMail = 0
	/*}}}*/
}	mediatype_t;
typedef enum { /*{{{*/
	MS_Unused = 0,
	MS_Inactive = 1,
	MS_Active = 2
	/*}}}*/
}	mstat_t;
typedef struct pval { /*{{{*/
	xmlBufferPtr	v;	/* the value itself			*/
	struct pval	*next;
	/*}}}*/
}	pval_t;
typedef struct parm { /*{{{*/
	char		*name;	/* variable name			*/
	pval_t		*value;	/* list of variable values		*/
	struct parm	*next;
	/*}}}*/
}	parm_t;
typedef struct { /*{{{*/
	mediatype_t	type;	/* output type				*/
	long		prio;	/* priority				*/
	mstat_t		stat;	/* status				*/
	bool_t		empty;	/* skip creation if content empty	*/
	parm_t		*parm;	/* all parameter			*/
	/*}}}*/
}	media_t;
typedef struct encrypt		encrypt_t;
typedef struct output		output_t;
typedef struct block		block_t;
typedef struct blockmail	blockmail_t;
typedef struct receiver		receiver_t;
typedef struct tag		tag_t;
typedef struct { /*{{{*/
	xmlBufferPtr	name;		/* the name of this position	*/
	long		hash;		/* the hashvalue		*/
	long		start, end;	/* start/end of the occurance	*/
	unsigned long	type;		/* type of tag position		*/
	tag_t		*tag;		/* link to blockmail->ltag	*/
	bool_t		sort_enable;	/* sorting enabled for this pos	*/
	long		sort_value;	/* value for resorting		*/
	char		*tname;		/* optional the name part	*/
	block_t		*content;	/* optional content for blocks	*/
	char		*multi;		/* != NULL for repeating block	*/
	void		*ev;		/* != NULL eval. for select	*/
	void		*on_error;	/* != NULL eval on error	*/
	/*}}}*/
}	tagpos_t;
typedef enum { /*{{{*/
	TID_Unspec = 0,
	TID_EMail_Head = 1,
	TID_EMail_Text = 2,
	TID_EMail_HTML = 3
	/*}}}*/
}	tid_t;

typedef struct head { /*{{{*/
	buffer_t	*h;
	struct head	*next;
	/*}}}*/
}	head_t;


typedef struct protect { /*{{{*/
	long		start;		/* start of protected area	*/
	long		end;		/* end of protected area	*/
	struct protect	*next;		/* next protected area		*/
	/*}}}*/
}	protect_t;

struct block { /*{{{*/
	int		bid;		/* the unique blockID		*/
	int		nr;		/* the passed blocknumber	*/
	char		*mime;		/* mime type			*/
	char		*charset;	/* the used character set	*/
	char		*encode;	/* output encoding		*/
	encoding_t	method;		/* the real encoding method	*/
	char		*cid;		/* content ID			*/
	tid_t		tid;		/* type ID, get from cid	*/
	bool_t		binary;		/* if this is a binary		*/
	bool_t		attachment;	/* if this is an attachment	*/
	bool_t		precoded;	/* already coded for output	*/
	bool_t		pdf;		/* if this is a pdf document	*/
	bool_t		font;		/* if this is a font for pdf	*/
	char		*media;		/* related to which media?	*/
	mediatype_t	mediatype;	/* the mediatype		*/
	xmlBufferPtr	condition;	/* the condition for this block	*/
	long		target_id;	/* ID of target group		*/
	int		target_index;	/* index into values		*/
	xmlBufferPtr	content;	/* content in UTF-8 as parsed	*/
	xmlCharEncodingHandlerPtr
			translate;	/* translate UTF-8 to the ..	*/
					/* .. charset of this block	*/
	xmlBufferPtr	in, out;	/* temp. buffers for converting	*/
	buffer_t	*bcontent;	/* the converted binary content	*/
	buffer_t	*bout;		/* encoded binary content	*/
	DO_DECL (tagpos);		/* all tags with position in ..	*/
					/* .. content			*/
	tagpos_t	**sorted;	/* for output sorted tagpos	*/
	bool_t		inuse;		/* required during generation	*/
	/*}}}*/
};

typedef struct blockspec	blockspec_t;

typedef struct { /*{{{*/
	xmlBufferPtr	cont;		/* content w/o attachments	*/
	xmlBufferPtr	acont;		/* content w/ attachments	*/
	bool_t		dyn, adyn;	/* true, if one has dyn.cont.	*/
	/*}}}*/
}	fix_t;
typedef struct postfix { /*{{{*/
	fix_t		*c;		/* content for the postfix	*/
	char		*pid;		/* postfix ID			*/
	int		after;		/* output after which block	*/
	struct blockspec
			*ref;		/* backreference to blockspec	*/
	struct postfix	*stack;		/* for stack output		*/
	/*}}}*/
}	postfix_t;
typedef enum {
	OPL_None = 0,
	OPL_Top = 1,
	OPL_Bottom = 2
}	opl_t;
struct blockspec { /*{{{*/
	int		nr;		/* the block to use		*/
	block_t		*block;		/* reference to block		*/
	fix_t		*prefix;	/* optional prefix and ..	*/
	DO_DECL (postfix);		/* .. postfix buffer		*/
	/* optional modifiers						*/
	int		linelength;	/* if >0, enforce linebreaks	*/
	xmlChar		*linesep;	/* the line separator		*/
	int		seplength;	/* the length of the line sep.	*/
	opl_t		opl;		/* insert onepixel URLs		*/
	/*}}}*/
};
typedef struct { /*{{{*/
	char		*ident;		/* the mailtype identifier	*/
	int		idnr;		/* numeric value of ident	*/
	bool_t		offline;	/* is this a offline html?	*/
	DO_DECL (blockspec);
	/*}}}*/
}	mailtypedefinition_t;
struct tag { /*{{{*/
	xmlBufferPtr	name;		/* the name of the tag		*/
	char		*cname;		/* normalized name		*/
	long		hash;		/* the hashvalue		*/
	char		*ttype;		/* internal tag type		*/
	char		*topt;		/* itnernal tag options		*/
	xmlBufferPtr	value;		/* value of the tag		*/
	var_t		*parm;		/* parsed parameter		*/
	bool_t		used;		/* marker to avoid loops	*/
	void		*proc;		/* optional processing block	*/
	struct tag	*next;
	/*}}}*/
};

typedef struct dyn { /*{{{*/
	int		did;		/* unique ID			*/
	char		*name;		/* name of the dynamic part	*/
	char		*interest;	/* db column for interest sort	*/
	int		interest_index;	/* index into database fields	*/
	bool_t		disable_link_extension;	/* no link extension	*/
	int		order;		/* the order in the chain	*/
	xmlBufferPtr	condition;	/* the condition for this part	*/
	long		target_id;	/* ID of target group		*/
	int		target_index;	/* index into values		*/
	DO_DECL (block);		/* the content of the dyn part	*/
	struct dyn	*sibling;	/* all parts with the same name	*/
	struct dyn	*next;		/* next part chain		*/
	/*}}}*/
}	dyn_t;

typedef struct { /*{{{*/
	char		**names;	/* names of columns		*/
	int		size, count;	/* for allocation		*/
	int		*indexes;	/* index into user data		*/
	/*}}}*/
}	columns_t;
typedef struct resolved	resolved_t;
typedef struct { /*{{{*/
	resolved_t	*pure;		/* the link w/o url extensions	*/
	resolved_t	*extended;	/* and with url extensions	*/
	/*}}}*/
}	link_resolve_t;
typedef struct { /*{{{*/
	long		url_id;		/* unique URL ID		*/
	buffer_t	*dest;		/* destination (the url itself)	*/
	int		usage;		/* to use in which part?	*/
	bool_t		admin_link;	/* if this is an admin link	*/
	buffer_t	*orig;		/* original URL			*/
	link_resolve_t	*resolved;	/* the resolved link		*/
	/*}}}*/
}	url_t;

typedef struct { /*{{{*/
	char		*name;		/* name of the field		*/
	char		*lname;		/* lower case variant of name	*/
	char		*ref;		/* reference table		*/
	char		*uref;		/* upper case variant of ref	*/
	char		*rname;		/* if ref, then full name	*/
	char		type;		/* type of the field		*/
	/*}}}*/
}	field_t;

typedef struct { /*{{{*/
	void		*e;		/* evaluator specific data	*/
	blockmail_t	*blockmail;	/* reference to blockmail	*/
	bool_t		in_condition;	/* we have got conditions	*/
	bool_t		in_variables;	/* we have got variables	*/
	bool_t		in_data;	/* we have got data		*/
	bool_t		in_match;	/* we are doing matches		*/
	/*}}}*/
}	eval_t;

typedef struct counter { /*{{{*/
	char		*mediatype;	/* the mediatype		*/
	int		subtype;	/* subtype, e.g. the mailtype	*/
	long		unitcount;	/* # of units deliviered	*/
	long long	bytecount;	/* # of bytes delivered		*/
	long		bccunitcount;	/* dito for bcc addresses	*/
	long long	bccbytecount;	/* - " -			*/
	struct counter	*next;
	/*}}}*/
}	counter_t;
typedef struct rblock { /*{{{*/
	tid_t		tid;		/* type ID			*/
	char		*bname;		/* name of this block		*/
	xmlBufferPtr	content;	/* content of the block		*/
	struct rblock	*next;
	/*}}}*/
}	rblock_t;

typedef struct track	track_t;
struct track { /*{{{*/
	char	*name;
	bool_t	(*encode) (void *, xmlBufferPtr, xmlBufferPtr);
	void	(*deinit) (void *);
	void	*data;
	track_t	*next;
	/*}}}*/
};
typedef struct { /*{{{*/
	track_t		*head;
	track_t		*tail;
	xmlBufferPtr	scratch[2];
	/*}}}*/
}	tracker_t;

struct blockmail { /*{{{*/
	/* internal used only data */
	const char	*fname;		/* current filename		*/
	char		syfname[PATH_MAX + 1];	/* sync filename	*/
	bool_t		syeof;		/* if we hit EOF		*/
	FILE		*syfp;		/* filepointer to sync file	*/
	xmlBufferPtr	in, out;	/* temp. in/out buffer		*/
	xmlCharEncodingHandlerPtr
			translate;	/* required in parsing		*/
	log_t		*lg;		/* logging interface		*/
	eval_t		*eval;		/* to interpret dynamic content	*/
	/* output related data */
	bool_t		usecrlf;	/* use CRLF or LF on output	*/
	bool_t		raw;		/* just generate raw output	*/
	output_t	*output;	/* output information		*/
	void		*outputdata;	/* output related private data	*/
	counter_t	*counter;	/* counter for created mails	*/
	bool_t		active;		/* if user is active		*/
	buffer_t	*head;		/* the created head ..		*/
	buffer_t	*body;		/* .. and body			*/
	rblock_t	*rblocks;	/* the raw blocks		*/

	/*
	 * from here, the data is from the input file or from dynamic enviroment
	 */
	/* description part */
	map_t		*smap;
	int		licence_id;
	int		owner_id;
	char		nodename[96];
	int		company_id;
	var_t		*company_info;
	int		mailinglist_id;
	int		mailing_id;
	xmlBufferPtr	mailing_name;
	int		maildrop_status_id;
	char		status_field;
	int		*senddate;
	/* general part */
	xmlBufferPtr	profile_url;
	xmlBufferPtr	unsubscribe_url;
	xmlBufferPtr	auto_url;
	bool_t		auto_url_is_dynamic;
	char		*auto_url_prefix;
	bool_t		anon;
	char		*selector;
	bool_t		convert_to_entities;
	xmlBufferPtr	onepixel_url;
	buffer_t	*opxmaker;
	xmlBufferPtr	anon_url;
	xmlBufferPtr	secret_key;
	long long	secret_timestamp;
	unsigned short	secret_timestamp1;
	unsigned short	secret_timestamp2;
	buffer_t	*secret_uid;
	buffer_t	*secret_sig;
	long		total_subscribers;
	char		*domain;
	struct {
		xmlBufferPtr	subject;
		xmlBufferPtr	from;
	}	email;
	tracker_t	*tracker;
	/* mailcreation part */
	int		blocknr;
	char		*innerboundary;
	char		*outerboundary;
	char		*attachboundary;
	
	/* the more complex parts */
	
	/* blocks */
	DO_DECL (block);
	/* media */
	DO_DECL (media);
	/* mail types */
	DO_DECL (mailtypedefinition);
	/* all known tags */
	tag_t		*ltag;
	int		taglist_count;
	/* tag function global data */
	void		*tfunc;
	/* global tags */
	tag_t		*gtag;
	int		globaltag_count;
	/* dynamic definitions */
	dyn_t		*dyn;
	int		dynamic_count;
	xmlBufferPtr	mtbuf[2];
	
	/* URLs in the mailing */
	DO_DECL (url);
	DO_DECL (link_resolve);

	/* layout of the customer table */
	DO_DECL (field);
	int		mailtype_index;
	/* pre evaluated target groups */
	long		*target_groups;
	int		target_groups_count;
	
	/* counter for receivers */
	int		receiver_count;
	/* our full qualified domain name */
	char		*fqdn;
	/* cache for conversion functions */
	xconv_t		*xconv;
	/* envelope address */
	char		*mfrom;
	/* for VIP exploder, the parsed block */
	xmlBufferPtr	vip;
	/* optional template for onepixel link */
	xmlBufferPtr	onepix_template;
	/* if not NULL, use only pictures with this prefix
	 * for offline html */
	char		*offline_picture_prefix;
	/* length of offline_picture_prefix */
	int		opp_len;
	/* force usage of ecs UID */
	bool_t		force_ecs_uid;
	/* which version for uid to use */
	long		uid_version;
	
	/* XmlBackPlugin hook						*/
	void		*xbp;
	/*}}}*/
};

typedef struct { /*{{{*/
	var_t		*ids;		/* IDs of reference tables	*/
	tag_t		*tag;		/* all tag informations		*/
	xmlBufferPtr	*data;		/* the data itself		*/
	bool_t		*isnull;	/* if value is NULL		*/
	int		dsize;		/* # of data entries in record	*/
	int		dpos;		/* current position in data	*/
	bool_t		*target_group;	/* the pre evaluated tg results	*/
	int		target_group_count;	/* # of elements in tg	*/
	/*}}}*/
}	record_t;
typedef struct dsindex { /*{{{*/
	char		*idlist;	/* list of IDs for this list	*/
	int		*indexlist;	/* list of indexes for this IDs	*/
	int		size, use;	/* for dynamic allocation	*/
	struct dsindex	*next;
	/*}}}*/
}	dsindex_t;
typedef struct { /*{{{*/
	int		dsize;		/* # of data entries per record	*/
	int		tgsize;		/* # of target groups pre eval	*/
	record_t	**r;		/* all allocated records	*/
	int		rsize;		/* # of allocated records	*/
	int		ruse;		/* # of used records		*/
	record_t	*cur;		/* current record		*/
	/* indexes for iterating over dyn. content			*/
	int		empty[1];	/* the empty record		*/
	int		standard[2];	/* the standard record		*/
	dsindex_t	*indexes;	/* the indexes for multi record	*/
	/* keep track of currently used indexes				*/
	int		icount;		/* # of currently active idxs	*/
	int		ipos;		/* current position in act.idxs	*/
	/*}}}*/
}	dataset_t;

typedef struct dcache { /*{{{*/
	const char	*name;		/* for which dynamic part	*/
	const dyn_t	*dyn;		/* the resolved part itself	*/
	struct dcache	*next;
	/*}}}*/
}	dcache_t;
typedef struct media_target { /*{{{*/
	char		*media;		/* the media name		*/
	buffer_t	*value;		/* its value			*/
	struct media_target	
			*next;
	/*}}}*/
}	media_target_t;
struct receiver { /*{{{*/
	int		customer_id;	/* customer id, as in the dbase	*/
	xmlChar		*uuid;		/* unique user ID		*/
	char		**bcc;		/* optional Bcc addresses	*/
	char		user_type;	/* user type for mailing	*/
	bool_t		tracking_veto;	/* tracking-veto is enabled?	*/
	bool_t		disable_link_extension;
					/* are link extensions disabled?*/
	media_target_t	*media_target;	/* target information on media	*/
	xmlBufferPtr	message_id;	/* the message id to use	*/
	void		*mid_maker;	/* opaque to make message id	*/
	mailtype_t	mailtype;	/* which mailtype to use	*/
	char		*mediatypes;	/* permission for which medias?	*/
	media_t		*media;		/* pointer to found media	*/
	char		mid[32];	/* media ID			*/
	dataset_t	*rvdata;	/* dynamic data			*/
	encrypt_t	*encrypt;	/* for dynamic data passing	*/
	dcache_t	*cache;		/* dynamic cache		*/
	block_t		*base_block;	/* on which block we are	*/
	map_t		*smap;		/* for simple mappings		*/
	gnode_t		**slist;	/* list of nodes		*/
	bool_t		empty;		/* skip on empty content	*/
	/*}}}*/
};

typedef struct { /*{{{*/
	char	**l;
	bool_t	*seen;
	int	lcnt, lsiz;
	/*}}}*/
}	links_t;

struct output { /*{{{*/
	const char	*name;
	const char	*desc;
	bool_t		syncfile;
	void		*(*oinit) (blockmail_t *, var_t *);
	bool_t		(*odeinit) (void *, blockmail_t *, bool_t);
	bool_t		(*owrite) (void *, blockmail_t *, receiver_t *);
	/*}}}*/
};

extern bool_t		parse_file (blockmail_t *blockmail, xmlDocPtr doc, xmlNodePtr base);
extern bool_t		create_output (blockmail_t *blockmail, receiver_t *rec);
extern bool_t		replace_tags (blockmail_t *blockmail, receiver_t *rec, block_t *block, 
				      int level, bool_t code_urls,
				      const xmlChar *(*replace) (const xmlChar *, int, int *),
				      const char *selector,
				      bool_t ishtml, bool_t ispdf);
extern bool_t		modify_urls (blockmail_t *blockmail, receiver_t *rec, block_t *block, protect_t *protect, bool_t ishtml, record_t *record);
extern bool_t		modify_output (blockmail_t *blockmail, receiver_t *rec, block_t *block, blockspec_t *bspec, links_t *links);
extern int		convert_block (xmlCharEncodingHandlerPtr translate, xmlBufferPtr in, xmlBufferPtr out, bool_t isoutput);
extern bool_t		convert_charset (blockmail_t *blockmail, block_t *block);
extern bool_t		append_mixed (buffer_t *dest, const char *desc, ...);
extern bool_t		append_pure (buffer_t *dest, const xmlBufferPtr src);
extern bool_t		append_raw (buffer_t *dest, const buffer_t *src);
extern bool_t		append_cooked (buffer_t *dest, const xmlBufferPtr src,
				       const char *charset, encoding_t method);

extern protect_t	*protect_alloc (void);
extern protect_t	*protect_free (protect_t *p);
extern protect_t	*protect_free_all (protect_t *p);
extern tagpos_t		*tagpos_alloc (void);
extern tagpos_t		*tagpos_free (tagpos_t *t);
extern void		tagpos_find_name (tagpos_t *t);
extern void		tagpos_setup_tag (tagpos_t *t, blockmail_t *blockmail);
extern block_t		*block_alloc (void);
extern block_t		*block_free (block_t *b);
extern bool_t		block_setup_charset (block_t *b);
extern void		block_setup_tagpositions (block_t *b, blockmail_t *blockmail);
extern void		block_find_method (block_t *b);
extern bool_t		block_code_binary_out (block_t *b);
extern bool_t		block_code_binary (block_t *b);
extern bool_t		block_match (block_t *b, eval_t *eval, receiver_t *rec);
extern columns_t	*columns_parse (xmlBufferPtr s);
extern columns_t	*columns_update (columns_t *cur, xmlBufferPtr s);
extern columns_t	*columns_alloc (void);
extern columns_t	*columns_free (columns_t *c);
extern pval_t		*pval_alloc (void);
extern pval_t		*pval_free (pval_t *p);
extern pval_t		*pval_free_all (pval_t *p);
extern parm_t		*parm_alloc (void);
extern parm_t		*parm_free (parm_t *p);
extern parm_t		*parm_free_all (parm_t *p);
extern xmlBufferPtr	parm_valuecat (parm_t *p, const char *sep);
extern media_t		*media_alloc (void);
extern media_t		*media_free (media_t *m);
extern bool_t		media_set_type (media_t *m, const char *type);
extern bool_t		media_set_priority (media_t *m, long prio);
extern bool_t		media_set_status (media_t *m, const char *status);
extern parm_t		*media_find_parameter (media_t *m, const char *name);
extern void		media_postparse (media_t *m, blockmail_t *blockmail);
extern bool_t		media_parse_type (const char *str, mediatype_t *type);
extern const char	*media_typeid (mediatype_t type);
extern fix_t		*fix_alloc (void);
extern fix_t		*fix_free (fix_t *f);
extern void		fix_scan_for_dynamic_content (fix_t *f);

extern postfix_t	*postfix_alloc (void);
extern postfix_t	*postfix_free (postfix_t *p);
extern blockspec_t	*blockspec_alloc (void);
extern blockspec_t	*blockspec_free (blockspec_t *b);
extern bool_t		blockspec_set_lineseparator (blockspec_t *b, const xmlChar *sep, int slen);
extern bool_t		blockspec_find_lineseparator (blockspec_t *b);
extern mailtypedefinition_t
			*mailtypedefinition_alloc (void);
extern mailtypedefinition_t
			*mailtypedefinition_free (mailtypedefinition_t *m);
extern counter_t	*counter_alloc (const char *mediatype, int subtype);
extern counter_t	*counter_free (counter_t *c);
extern counter_t	*counter_free_all (counter_t *c);
extern rblock_t		*rblock_alloc (tid_t tid, const char *bname, xmlBufferPtr content);
extern rblock_t		*rblock_free (rblock_t *r);
extern rblock_t		*rblock_free_all (rblock_t *r);
extern bool_t		rblock_set_name (rblock_t *r, const char *bname);
extern bool_t		rblock_set_content (rblock_t *r, xmlBufferPtr content);
extern bool_t		rblock_retrieve_content (rblock_t *r, buffer_t *content);
extern blockmail_t	*blockmail_alloc (const char *fname, bool_t syncfile, log_t *lg);
extern blockmail_t	*blockmail_free (blockmail_t *b);
extern bool_t		blockmail_count (blockmail_t *b, const char *mediatype, int subtype, long bytes, int bcccount);
extern void		blockmail_count_sort (blockmail_t *b);
extern void		blockmail_unsync (blockmail_t *b);
extern bool_t		blockmail_insync (blockmail_t *b, int cid, const char *mediatype, int subtype, int bcccount);
extern bool_t		blockmail_tosync (blockmail_t *b, int cid, const char *mediatype, int subtype, int bcccount);
extern bool_t		blockmail_extract_mediatypes (blockmail_t *b);
extern void		blockmail_setup_senddate (blockmail_t *b, const char *date);
extern void		blockmail_setup_mfrom (blockmail_t *b);
extern void		blockmail_setup_vip_block (blockmail_t *b);
extern void		blockmail_setup_onepixel_template (blockmail_t *b);
extern void		blockmail_setup_tagpositions (blockmail_t *b);
extern void		blockmail_setup_offline_picture_prefix (blockmail_t *b);
extern void		blockmail_setup_auto_url_prefix (blockmail_t *b, const char *nprefix);
extern void		blockmail_setup_anon (blockmail_t *b, bool_t anon);
extern void		blockmail_setup_selector (blockmail_t *b, const char *selector);
extern void		blockmail_setup_preevaluated_targets (blockmail_t *blockmail);

extern int		*tf_parse_date (const char *s);
extern struct tm	tf_convert_date (int *date);

extern void		*tag_function_alloc (tag_t *t, blockmail_t *blockmail);
extern void		*tag_function_free (void *pd);
extern void		tag_function_proc (void *pd, tag_t *t, blockmail_t *blockmail, receiver_t *rec);
extern void		*tfunc_alloc (blockmail_t *blockmail);
extern void		tfunc_free (void *tp);

extern void		tf_lua_free (void *ilp);
extern void		*tf_lua_alloc (const char *func, tag_t *tag, blockmail_t *blockmail);
extern bool_t		tf_lua_load (void *ilp, buffer_t *code, blockmail_t *blockmail);
extern bool_t		tf_lua_setup (void *ilp, const char *func, tag_t *tag, blockmail_t *blockmail);
extern bool_t		tf_lua_proc (void *ilp, const char *func, tag_t *tag, blockmail_t *blockmail, receiver_t *rec);

extern char		*ev_lua_convert (blockmail_t *blockmail, const char *expression);
extern void		*ev_lua_free (void *ilp);
extern void		*ev_lua_alloc (blockmail_t *blockmail, const char *expression);
extern int		ev_lua_vevaluate (void *ilp, receiver_t *rec, va_list par);
extern int		ev_lua_evaluate (void *ilp, receiver_t *rec, ...);

extern tag_t		*tag_alloc (void);
extern tag_t		*tag_free (tag_t *t);
extern tag_t		*tag_free_all (tag_t *t);
extern void		tag_parse (tag_t *t, blockmail_t *blockmail);
extern bool_t		tag_match (tag_t *t, const xmlChar *name, int nlen);
extern const xmlChar	*tag_content (tag_t *t, blockmail_t *blockmail, receiver_t *rec, int *length);
extern dyn_t		*dyn_alloc (int did, int order);
extern dyn_t		*dyn_free (dyn_t *d);
extern dyn_t		*dyn_free_all (dyn_t *d);
extern bool_t		dyn_match (const dyn_t *d, eval_t *eval, receiver_t *rec);
extern bool_t		dyn_assign_interest_field (dyn_t *d, field_t **fields, int fcount);
extern bool_t		dyn_match_selector (const dyn_t *dyn, const char *selector);

extern link_resolve_t	*link_resolve_alloc (void);
extern link_resolve_t	*link_resolve_free (link_resolve_t *lr);
extern bool_t		link_resolve_prepare (link_resolve_t *lr, blockmail_t *blockmail, url_t *url);
extern buffer_t		*link_resolve_get (link_resolve_t *lr, blockmail_t *blockmail, block_t *block, url_t *url, receiver_t *rec, record_t *record);

extern url_t		*url_alloc (void);
extern url_t		*url_free (url_t *u);
extern bool_t		url_match (url_t *u, const xmlChar *check, int clen);
extern void		url_set_destination (url_t *u, xmlBufferPtr dest);
extern bool_t		url_match_original (url_t *u, const xmlChar *check, int clen);
extern void		url_set_original (url_t *u, xmlBufferPtr orig);

extern field_t		*field_alloc (void);
extern field_t		*field_free (field_t *f);
extern bool_t		field_normalize_name (field_t *f);
extern dataset_t	*dataset_alloc (int dsize, int tgsize);
extern dataset_t	*dataset_free (dataset_t *ds);
extern void		dataset_clear (dataset_t *ds);
extern bool_t		dataset_new_record (dataset_t *ds, var_t *ids);
extern record_t		*dataset_select_first_record (dataset_t *ds);
extern record_t		*dataset_select_record (dataset_t *ds, int pos);
extern int		*dataset_get_indexes (dataset_t *ds, tagpos_t *tp, receiver_t *rec, int *icount, bool_t *dynamic);
extern bool_t		dataset_match (dataset_t *ds, int target_index);
extern dcache_t		*dcache_alloc (const char *name, const dyn_t *dyn);
extern dcache_t		*dcache_free (dcache_t *d);
extern dcache_t		*dcache_free_all (dcache_t *d);
extern receiver_t	*receiver_alloc (blockmail_t *blockmail, int data_blocks);
extern receiver_t	*receiver_free (receiver_t *r);
extern void		receiver_clear (receiver_t *r);
extern void		receiver_set_data_l (receiver_t *rec, const char *key, long data);
extern void		receiver_set_data_i (receiver_t *rec, const char *key, int data);
extern void		receiver_set_data_s (receiver_t *rec, const char *key, const char *data);
extern void		receiver_set_data_b (receiver_t *rec, const char *key, xmlBufferPtr data);
extern void		receiver_set_data_buf (receiver_t *rec, const char *key, const buffer_t *data);
extern void		receiver_set_data_default (receiver_t *rec);
extern void		receiver_set_data (receiver_t *rec, const char *name, record_t *record);
extern void		receiver_make_message_id (receiver_t *rec, blockmail_t *blockmail);
extern media_target_t	*media_target_alloc (const char *media, const xmlChar *value);
extern media_target_t	*media_target_free (media_target_t *mt);
extern media_target_t	*media_target_free_all (media_target_t *mt);
extern const buffer_t	*media_target_find (media_target_t *mt, const char *media);
extern links_t		*links_alloc (void);
extern links_t		*links_free (links_t *l);
extern bool_t		links_expand (links_t *l);
extern bool_t		links_nadd (links_t *l, const char *lnk, int llen);


extern tracker_t	*tracker_alloc (void);
extern tracker_t	*tracker_free (tracker_t *tracker);
extern bool_t		tracker_add (tracker_t *t, blockmail_t *blockmail, const char *name, xmlBufferPtr content);
extern bool_t		tracker_fill (tracker_t *t, blockmail_t *blockmail, const xmlChar **url, int *ulength);

extern head_t		*head_alloc (void);
extern head_t		*head_free (head_t *h);
extern void		head_add (head_t *h, const char *str, int len);
extern void		head_trim (head_t *h);

/*
 * some support routines
 */
extern bool_t		decode_base64 (const xmlBufferPtr src, buffer_t *dest);
extern bool_t		encode_none (const xmlBufferPtr src, buffer_t *dest);
extern bool_t		encode_header (const xmlBufferPtr src, buffer_t *dest, const char *charset);
extern bool_t		encode_8bit (const xmlBufferPtr src, buffer_t *dest);
extern bool_t		encode_quoted_printable (const xmlBufferPtr src, buffer_t *dest);
extern bool_t		encode_base64 (const xmlBufferPtr src, buffer_t *dest);
extern bool_t		encode_encrypted (buffer_t *src, buffer_t *dest);
extern bool_t		encode_url (const byte_t *input, int ilen, buffer_t *dest);

# ifndef	__OPTIMIZE__
extern bool_t		xmlEqual (xmlBufferPtr p1, xmlBufferPtr p2);
extern int		xmlCharLength (xmlChar ch);
extern int		xmlStrictCharLength (xmlChar ch);
extern int		xmlValidPosition (const xmlChar *str, int length);
extern bool_t		xmlValid (const xmlChar *str, int length);
extern char		*xml2string (xmlBufferPtr p);
extern const char	*xml2char (const xmlChar *s);
extern const xmlChar	*char2xml (const char *s);
extern const char	*byte2char (const byte_t *b);
extern int		xmlstrcmp (const xmlChar *s1, const char *s2);
extern int		xmlstrncmp (const xmlChar *s1, const char *s2, size_t n);
extern long		xml2long (xmlBufferPtr p);
# else		/* __OPTIMIZE__ */
# define	I	static inline
# include	"misc.c"
# undef		I
# endif		/* __OPTIMIZE__ */
extern bool_t		xmlSQLlike (const xmlChar *pattern, int plen,
				    const xmlChar *string, int slen,
				    const xmlChar *escape, int elen);

extern char		*create_uid (blockmail_t *blockmail, const char *prefix, receiver_t *rec, long url_id);
extern char		*create_pubid (blockmail_t *blockmail, receiver_t *rec, const char *source, const char *parm);

extern encrypt_t	*encrypt_alloc (blockmail_t *blockmail);
extern encrypt_t	*encrypt_free (encrypt_t *e);
extern const char	*encrypt_do (encrypt_t *e, receiver_t *rec, buffer_t *buf, int version);
extern void		encrypt_build_reset (encrypt_t *e);
extern bool_t		encrypt_build_add (encrypt_t *e, const char *s, int slen);
extern const char	*encrypt_build_do (encrypt_t *e, receiver_t *rec, int version);

extern xmlBufferPtr	string_maps (const xmlBufferPtr src, map_t **maps, int mcount);
extern xmlBufferPtr	string_mapv (const xmlBufferPtr src, va_list par);
extern xmlBufferPtr	string_mapn (const xmlBufferPtr src, ...);
extern xmlBufferPtr	string_map (const xmlBufferPtr src, map_t *local, map_t *global);
extern map_t		*string_map_setup (void);
extern gnode_t		*string_map_addss (map_t *map, const char *key, const char *data);
extern gnode_t		*string_map_addsb (map_t *map, const char *key, xmlBufferPtr data);
extern gnode_t		*string_map_addsbuf (map_t *map, const char *key, const buffer_t *data);
extern gnode_t		*string_map_addsi (map_t *map, const char *key, long data);
extern void		string_map_done (map_t *map);

extern void		entity_replace (xmlBufferPtr in, xmlBufferPtr out, bool_t all);

extern eval_t		*eval_alloc (blockmail_t *blockmail);
extern eval_t		*eval_free (eval_t *e);
extern bool_t		eval_set_condition (eval_t *e, int sphere, int eid, xmlBufferPtr condition);
extern bool_t		eval_done_condition (eval_t *e);
extern bool_t		eval_set_variables (eval_t *e, field_t **fld, int fld_cnt, int *failpos);
extern bool_t		eval_done_variables (eval_t *e);
extern bool_t		eval_setup (eval_t *e);
extern bool_t		eval_set_data (eval_t *e, xmlBufferPtr *data, bool_t *dnull, int data_cnt);
extern bool_t		eval_done_data (eval_t *e);
extern bool_t		eval_change_data (eval_t *e, xmlBufferPtr data, bool_t dnull, int pos);
extern bool_t		eval_match (eval_t *e, int sphere, int eid);
extern bool_t		eval_done_match (eval_t *e);
extern void		eval_dump (eval_t *e, FILE *fp);

/*
 * outputmodules
 */
extern void		*none_oinit (blockmail_t *blockmail, var_t *opts);
extern bool_t		none_odeinit (void *data, blockmail_t *blockmail, bool_t success);
extern bool_t		none_owrite (void *data, blockmail_t *blockmail, receiver_t *rec);
extern void		*generate_oinit (blockmail_t *blockmail, var_t *opts);
extern bool_t		generate_odeinit (void *data, blockmail_t *blockmail, bool_t success);
extern bool_t		generate_owrite (void *data, blockmail_t *blockmail, receiver_t *rec);
extern void		*count_oinit (blockmail_t *blockmail, var_t *opts);
extern bool_t		count_odeinit (void *data, blockmail_t *blockmail, bool_t success);
extern bool_t		count_owrite (void *data, blockmail_t *blockmail, receiver_t *rec);
extern void		*preview_oinit (blockmail_t *blockmail, var_t *opts);
extern bool_t		preview_odeinit (void *data, blockmail_t *blockmail, bool_t success);
extern bool_t		preview_owrite (void *data, blockmail_t *blockmail, receiver_t *rec);

# include	"xmlback-plugin.h"
#endif		/* __XMLBACK_H */
