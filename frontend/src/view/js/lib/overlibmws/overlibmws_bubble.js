/*
 overlibmws_bubble.js plug-in module - Copyright Foteos Macrides 2003-2008. All rights reserved.
   For support of the BUBBLE feature.
   Initial: July 26, 2003 - Last Revised: January 26, 2008
 See the Change History and Command Reference for overlibmws via:

	http://www.macridesweb.com/oltest/

 Published under an open source license: http://www.macridesweb.com/oltest/license.html
*/

OLloaded=0;
var OLbubbleCmds='bubble,bubbletype,adjbubble,rcbubblecolor,bubbleclose';OLregCmds(OLbubbleCmds);

// DEFAULT CONFIGURATION
if(OLud('bubble'))var ol_bubble=0;
if(OLud('bubbletype'))var ol_bubbletype='';
if(OLud('adjbubble'))var ol_adjbubble=0;
if(OLud('rcbubblecolor'))var ol_rcbubblecolor='#ffffcc';
if(OLud('bubbleclose'))var ol_bubbleclose=0;
// END CONFIGURATION

function OLund(v){return eval('typeof '+v+'=="undefined"')?1:0;}
if(OLund('OLbubbleImageSet'))var OLbubbleImageSet='flower,oval,square,pushpin,quotation,roundcorners';
if(OLund('OLbubbleImages'))var OLbubbleImages=OLbubbleImageSet;
if(OLund('OLbubbleImageDir'))var OLbubbleImageDir='./';
if(OLund('OLbubbleIMGsuffix'))var OLbubbleIMGsuffix='';
if(OLund('OLbubbleRCsuffix'))var OLbubbleRCsuffix='';
if(OLund('OLimgWidth'))var OLimgWidth=[250,330,144,202,200];
if(OLund('OLimgHeight'))var OLimgHeight=[150,160,190,221,66];
if(OLund('OLcontentWidth'))var OLcontentWidth=[200,250,130,184,190];
if(OLund('OLcontentHeight'))var OLcontentHeight=[80,85,150,176,46];
if(OLund('OLpadLeft'))var OLpadLeft=[30,40,7,9,5];
if(OLund('OLpadTop'))var OLpadTop=[25,48,10,34,4];
if(OLund('OLarwTipX'))var OLarwTipX=[180,50,51,9,19];
if(OLund('OLarwTipY'))var OLarwTipY=[148,5,180,221,64];

var o3_bubble=0,o3_bubbletype='',o3_adjbubble=0,o3_rcbubblecolor='',o3_bubbleclose=0,
OLbubbleHt=0,OLbI,OLbContentWd=OLcontentWidth;
OLregisterImages(OLbubbleImageSet,OLbubbleImageDir,OLbubbleIMGsuffix,OLbubbleRCsuffix);

function OLloadBubble(){
OLload(OLbubbleCmds);OLbubbleHt=0;
}

function OLparseBubble(pf,i,ar){
var k=i,t=OLtoggle,q=OLparQuo;if(k<ar.length){
if(Math.abs(ar[k])==BUBBLE){t(ar[k],pf+'bubble');return k;}
if(ar[k]==BUBBLETYPE){q(ar[++k],pf+'bubbletype');return k;}
if(Math.abs(ar[k])==ADJBUBBLE){t(ar[k],pf+'adjbubble');return k;}
if(ar[k]==RCBUBBLECOLOR){q(ar[++k],pf+'rcbubblecolor');return k;}
if(Math.abs(ar[k])==BUBBLECLOSE){t(ar[k],pf+'bubbleclose');return k;}}
return -1;
}

function OLchkForBubbleEffect(){
if(o3_bubble){o3_bubbletype=(o3_bubbletype)?o3_bubbletype:'flower';
for(var i=0;i<OLbTypes.length;i++){if(OLbTypes[i]==o3_bubbletype){OLbI=i;break;}}
// disable inappropriate parameters
o3_bgcolor=o3_fgcolor='';o3_border=o3_base=0;o3_fgbackground=o3_bgbackground=o3_cgbackground=o3_background='';
o3_cap='';if(o3_sticky)o3_noclose=(o3_bubbleclose)?0:1;o3_fullhtml=0;if(OLshadowPI)o3_shadow=0;
if(o3_bubbletype.indexOf('roundcorners')<0){o3_width=OLbContentWd[OLbI];o3_hpos=RIGHT;o3_vpos=BELOW;o3_vauto=0;
o3_hauto=0;o3_wrap=0;o3_nojusty=1;}}return true;
}

function OLregisterImages(imgStr,path,isuffix,rsuffix){
if(typeof imgStr!='string')return;var p=(path&&typeof path=='string')?path:'.',is=(typeof isuffix=='string')?isuffix:'',
rs=(typeof rsuffix=='string')?rsuffix:'',bT;if(p.charAt(p.length-1)=='/')p=p.substring(0,p.length-1);
if(OLund('OLbTypes'))OLbTypes=imgStr.split(',');if(OLund('OLbubbleImg')){OLbubbleImg=new Array();
for(var i=0;i<OLbTypes.length;i++){bT=OLbTypes[i];if(OLbubbleImages.indexOf(bT)<0)continue;
if(bT.indexOf('roundcorners')==0){OLbubbleImg[i]=new Array();
var o=OLbubbleImg[i],su=((bT.length>12)?bT.substring(12):'')+rs;
o[0]=new Image();o[0].src=p+'/cornerTL'+su+'.gif';o[1]=new Image();o[1].src=p+'/edgeT'+su+'.gif';
o[2]=new Image();o[2].src=p+'/cornerTR'+su+'.gif';o[3]=new Image();o[3].src=p+'/edgeL'+su+'.gif';
o[4]=new Image();o[4].src=p+'/edgeR'+su+'.gif';o[5]=new Image();o[5].src=p+'/cornerBL'+su+'.gif';
o[6]=new Image();o[6].src=p+'/edgeB'+su+'.gif';o[7]=new Image();o[7].src=p+'/cornerBR'+su+'.gif';}
else{OLbubbleImg[i]=new Image();OLbubbleImg[i].src=p+'/'+bT+is+'.gif';}}}
}

function OLgenerateBubble(content){
if(!o3_bubble)return;if(o3_bubbletype.indexOf('roundcorners')==0)return OLdoRoundCorners(content);
var ar,X,Y,W,fc=1.0,txt,sY,bHtDiff,bPadDiff=0,bLobj,bCobj,bTopPad=OLpadTop,bLeftPad=OLpadLeft,
bContentHt=OLcontentHeight,bHt=OLimgHeight,bWd=OLimgWidth,bArwTipX=OLarwTipX,bArwTipY=OLarwTipY;
bHtDiff=fc*bContentHt[OLbI]-(OLns4?over.clip.height:over.offsetHeight);if(o3_adjbubble){
fc=OLresizeBubble(bHtDiff,0.5,fc);ar=OLgetHeightDiff(fc);bHtDiff=ar[0];content=ar[1];}
if(bHtDiff>0)bPadDiff=(bHtDiff<2)?0:parseInt(0.5*bHtDiff);
Y=(bHtDiff<0)?fc*bTopPad[OLbI]:fc*bTopPad[OLbI]+bPadDiff;X=fc*bLeftPad[OLbI];
Y=Math.ceil(Y);X=Math.ceil(X);o3_width=Math.ceil(fc*bWd[OLbI]);W=Math.ceil(fc*OLbContentWd[OLbI]);
OLbubbleHt=Math.ceil((bHtDiff<0?fc*bHt[OLbI]-bHtDiff:fc*bHt[OLbI]));
txt='<img src="'+OLbubbleImg[OLbI].src+'" width="'+o3_width+'" height="'+OLbubbleHt+'" />'
+(OLns4?'<div id="bContent">':'<div id="bContent" style="position:absolute; top:'+Y+'px; left:'
+X+'px; width:'+W+'px; z-index:1;">')+content+'</div>';OLlayerWrite(txt);
if(OLns4){bCobj=over.document.layers['bContent'];if(typeof bCobj=='undefined')return;
bCobj.top=Y;bCobj.left=X;bCobj.clip.width=W;bCobj.zIndex=1;}
if(fc*bArwTipY[OLbI]<0.5*fc*bHt[OLbI])sY=Math.ceil(fc*bArwTipY[OLbI]);else sY= -(OLbubbleHt+20);
o3_offsetx -=Math.ceil(fc*bArwTipX[OLbI]);o3_offsety +=sY;
}

function OLdoRoundCorners(content){
var wd=(OLns4)?over.clip.width:over.offsetWidth,ht=(OLns4)?over.clip.height:over.offsetHeight,
o=OLbubbleImg[OLbI],ids=(OLns6?' style="display:block;"':''),wd14='" width="14',ht14='" height="14"',
t='<table cellpadding="0" cellspacing="0" border="0"><tr><td align="right" valign="bottom"><img src="'
+o[0].src+wd14+ht14+ids+' /></td><td valign="bottom"><img src="'
+o[1].src+'" width="'+wd+ht14+ids+' /></td><td align="left" valign="bottom"><img src="'
+o[2].src+wd14+ht14+ids+' /></td></tr><tr><td align="right"><img src="'
+o[3].src+wd14+'" height="'+ht+'"'+ids+' /></td><td bgcolor="'+o3_rcbubblecolor+'">'
+content+'</td><td align="left"><img src="'
+o[4].src+wd14+'" height="'+ht+'"'+ids+' /></td></tr><tr><td align="right" valign="top"><img src="'
+o[5].src+wd14+ht14+' /></td><td valign="top"><img src="'
+o[6].src+'" width="'+wd+ht14+' /></td><td align="left" valign="top"><img src="'
+o[7].src+wd14+ht14+' /></td></tr></table>';OLlayerWrite(t);o3_width=wd+28;OLbubbleHt=ht+28;
}

function OLresizeBubble(h1,dF,fold){
var df,h2,fnew,alpha,cnt=0;while(cnt<2){df= -OLsignOf(h1)*dF;fnew=fold+df;h2=OLgetHeightDiff(fnew)[0];
if(Math.abs(h2)<11)break;if(OLsignOf(h1)!=OLsignOf(h2)){alpha=Math.abs(h1)/(Math.abs(h1)+Math.abs(h2));
if(h1<0)fnew=alpha*fnew+(1.0-alpha)*fold;else fnew=(1.0-alpha)*fnew+alpha*fold;}else{
alpha=Math.abs(h1)/(Math.abs(h2)-Math.abs(h1));if(h1<0)fnew=(1.0+alpha)*fold-alpha*fnew;
else fnew=(1.0+alpha)*fnew-alpha*fold;}fold=fnew;h1=h2;dF*=0.5;cnt++;}return fnew;
}
function OLsignOf(x){return (x<0)? -1:1;}

function OLgetHeightDiff(f){
var lyrhtml;o3_width=Math.ceil(f*OLcontentWidth[OLbI]);lyrhtml=OLcontentSimple(o3_text);OLlayerWrite(lyrhtml)
return [f*OLcontentHeight[OLbI]-((OLns4)?over.clip.height:over.offsetHeight),lyrhtml];
}

OLregRunTimeFunc(OLloadBubble);OLregCmdLineFunc(OLparseBubble);

if(OLns4)
document.write('<style type="text/css">\n<!--\n#bContent{position:absolute;left:0px;top:0px;width:1024}\n'
+'-->\n<'+'\/style>');
OLbubblePI=1;
OLloaded=1;
