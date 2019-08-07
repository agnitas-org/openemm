/*
 htmlspecialcharsmws.js - Foteos Macrides (author and Copyright holder)
    Initial: January 26, 2008 - Last Revised: March 28, 2008
 Wrapper function set for performing the equivalent of the php
 htmlspecialchars(sting, quote_style) function on html fragments
 (see: http://us2.php.net/manual/en/function.htmlspecialchars.php)
 to display the raw markup via overlibmws STICKY, EXCLUSIVE popups.
 */


/* Optional second argment values (ampersands and angle brackets always converted)
       none or 0: convert double qoutes but not single quotes
    ENT_NOQUOTES: don't covert either
      ENT_QUOTES: convert both
*/
var ENT_NOQUOTES=1,ENT_QUOTES=2;

/* Covert an html fragment */
function OLhtmlspecialchars(str,quo){
 var s=(str||'').toString().replace(/&/g,"&amp;"),q=(quo||0);
 if(q!=ENT_NOQUOTES)s=s.replace(/"/g,"&quot;");
 if(q==ENT_QUOTES)s=s.replace(/'/g,"&#039;");
 return s.replace(/</g,"&lt;").replace(/>/g,"&gt;");
}

/* Convert and show arbitary Markup */
function OLshowMarkup(str,quo){
 var so=OLoverHTML,s=(str||'null').toString(),q=(quo||0);
 overlib(OLhtmlspecialchars(s,q), CAPTION,'<div align="center">Markup</div>', EXCLUSIVEOVERRIDE, STICKY, EXCLUSIVE,
  BGCLASS,'', BORDER,1, BGCOLOR,'#666666', BASE,0, CGCLASS,'', CAPTIONFONTCLASS,'', CLOSEFONTCLASS,'', CAPTIONPADDING,6,
  CGCOLOR,'#999999', CAPTIONSIZE,'12px', CAPCOLOR,'#ffffff', CLOSESIZE,'11px', CLOSECOLOR,'#ffffff', FGCLASS,'',
  TEXTFONTCLASS,'', TEXTPADDING,6, FGCOLOR,'#eeeeee', TEXTSIZE,'12px', TEXTCOLOR,'#000000', MIDX,0, RELY,5, WRAP,
  (OLfilterPI)?-FILTER:DONOTHING, (OLshadowPI)?-SHADOW:DONOTHING);
 OLoverHTML=so;
}

/* Convert and show most recent OLoverHTML */
function OLoverHTMLshow(quo){
 var so=OLoverHTML,s=(so||'null').toString(),q=(quo||0);
 overlib(OLhtmlspecialchars(s,q), CAPTION,'<div align="center">OLoverHTML</div>', EXCLUSIVEOVERRIDE, STICKY, EXCLUSIVE,
  BGCLASS,'', BORDER,1, BGCOLOR,'#666666', BASE,0, CGCLASS,'', CAPTIONFONTCLASS,'', CLOSEFONTCLASS,'', CAPTIONPADDING,6,
  CGCOLOR,'#999999', CAPTIONSIZE,'12px', CAPCOLOR,'#ffffff', CLOSESIZE,'11px', CLOSECOLOR,'#ffffff', FGCLASS,'',
  TEXTFONTCLASS,'', TEXTPADDING,6, FGCOLOR,'#eeeeee', TEXTSIZE,'12px', TEXTCOLOR,'#000000', MIDX,0, RELY,5, WRAP,
  (OLfilterPI)?-FILTER:DONOTHING, (OLshadowPI)?-SHADOW:DONOTHING);
 OLoverHTML=so;
}

/* Convert and show most recent OLover2HTML */
function OLover2HTMLshow(quo){
 var so=OLoverHTML,s2=(OLover2HTML||'null').toString(),q=(quo||0);
 overlib(OLhtmlspecialchars(s2,q), CAPTION,'<div align="center">OLover2HTML</div>', EXCLUSIVEOVERRIDE, STICKY, EXCLUSIVE,
  BGCLASS,'', BORDER,1, BGCOLOR,'#666666', BASE,0, CGCLASS,'', CAPTIONFONTCLASS,'', CLOSEFONTCLASS,'', CAPTIONPADDING,6,
  CGCOLOR,'#aaaaaa', CAPTIONSIZE,'12px', CAPCOLOR,'#ffffff', CLOSESIZE,'11px', CLOSECOLOR,'#ffffff', FGCLASS,'',
  TEXTFONTCLASS,'', TEXTPADDING,6, FGCOLOR,'#eeeeee', TEXTSIZE,'12px', TEXTCOLOR,'#000000', MIDX,0, RELY,5, WRAP,
  (OLfilterPI)?-FILTER:DONOTHING, (OLshadowPI)?-SHADOW:DONOTHING);
 OLoverHTML=so;
}

/* Convert and show most recent OLresponseAJAX */
function OLresponseAJAXshow(quo){
 var so=OLoverHTML,s=(OLresponseAJAX||'null').toString(),q=(quo||0);
 overlib(OLhtmlspecialchars(s,q), CAPTION,'<div align="center">OLresponseAJAX</div>', EXCLUSIVEOVERRIDE, STICKY, EXCLUSIVE,
  BGCLASS,'', BORDER,1, BGCOLOR,'#666666', BASE,0, CGCLASS,'', CAPTIONFONTCLASS,'', CLOSEFONTCLASS,'', CAPTIONPADDING,6,
  CGCOLOR,'#999999', CAPTIONSIZE,'12px', CAPCOLOR,'#ffffff', CLOSESIZE,'11px', CLOSECOLOR,'#ffffff', FGCLASS,'',
  TEXTFONTCLASS,'', TEXTPADDING,6, FGCOLOR,'#eeeeee', TEXTSIZE,'12px', TEXTCOLOR,'#000000', MIDX,0, RELY,5, WRAP,
  (OLfilterPI)?-FILTER:DONOTHING, (OLshadowPI)?-SHADOW:DONOTHING);
 OLoverHTML=so;
}
