/**
 * Copyright 2005 Darren L. Spurgeon
 * Copyright 2007 Jens Kapitza
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 

/**
 * Global Variables
 */
AJAX_DEFAULT_PARAMETER = "ajaxParameter";
AJAX_PORTLET_MAX = 1;
AJAX_PORTLET_MIN = 2;
AJAX_PORTLET_CLOSE = 3;
AJAX_CALLOUT_OVERLIB_DEFAULT = "STICKY,CLOSECLICK,DELAY,250,TIMEOUT,5000,VAUTO,WRAPMAX,240,CSSCLASS,FGCLASS,'olfg',BGCLASS,'olbg',CGCLASS,'olcg',CAPTIONFONTCLASS,'olcap',CLOSEFONTCLASS,'olclo',TEXTFONTCLASS,'oltxt'";


/**
 * Type Detection
 */
function isAlien(a) {
  return isObject(a) && typeof a.constructor != 'function';
}

function isArray(a) {
  return isObject(a) && a.constructor == Array;
}

function isBoolean(a) {
  return typeof a == 'boolean';
}

function isEmpty(o) {
  var i, v;
  if (isObject(o)) {
    for (i in o) {
      v = o[i];
      if (isUndefined(v) && isFunction(v)) {
        return false;
      }
    }
  }
  return true;
}

function isFunction(a) {
  return typeof a == 'function';
}

function isNull(a) {
  return typeof a == 'object' && !a;
}

function isNumber(a) {
  return typeof a == 'number' && isFinite(a);
}

function isObject(a) {
  return (a && typeof a == 'object') || isFunction(a);
}

function isString(a) {
  return typeof a == 'string';
}

function isUndefined(a) {
  return typeof a == 'undefined';
}


/**
 * Utility Functions
 */

function addOnLoadEvent(func) {
  var oldonload = window.onload;
  if (isFunction(func)) {
  	if (!isFunction(oldonload)) {
  	  window.onload = func;
  	} else {
  	  window.onload = function() {
  	    oldonload();
  	    func();
  	  };
  	}
  } else {
  	if (isObject(func) && isFunction(func.onload)) {
  		// callback event?
  	  window.onload = function() {
  	   if (isFunction(oldonload)) {
  		 oldonload();
  	   }
  	   // onload des objektes aufrufen
  	   func.onload();
  	  };
  	}
  }
}

/*
 * Extract querystring from a URL
 */
function extractQueryString(url) {
  return ( (url.indexOf('?') >= 0) && (url.indexOf('?') < (url.length-1))) ? url.substr(url.indexOf('?')+1): '';
}

/*
 * Trim the querystring from a URL
 */
function trimQueryString(url) {
  return (url.indexOf('?') >= 0) ? url.substring(0, url.indexOf('?')) : url;
}

function delimitQueryString(qs) {
  var ret = '';
  var params = "";
  if (qs.length > 0) {
	params = qs.split('&');
    for (i=0; i<params.length; i++) {
      if (i > 0)
      { 
      	ret += ',';
      }
      ret += params[i];
    }
  }
  return ret;
}

function trim(str) {
	return str.replace(/^\s*/,"").replace(/\s*$/,"");
}

// encode , =
function buildParameterString(parameterList) {
  var returnString = '';
  var params = (parameterList || '').split(',');
  if (params !== null) {
    for (p=0; p<params.length; p++) {
     	pair = params[p].split('=');
       	key = trim(pair[0]); // trim string no spaces allowed in key a, b should work
       	val = pair[1];
      // if val is not null and it contains a match for a variable, then proceed
      if (!isEmpty(val) || isString(val)) {
        	varList = val.match( new RegExp("\\{[\\w\\.\\(\\)\\[\\]]*\\}", 'g') );
        if (!isNull(varList)) {
          	field = $(varList[0].substring(1, varList[0].length-1));
          switch (field.type) {
            case 'checkbox':
            case 'radio':
            case 'text':
            case 'textarea':
            case 'password':
            case 'hidden':
            case 'select-one':
              returnString += '&' + key + '=' + encodeURIComponent(field.value);
              break;
            case 'select-multiple':
              fieldValue = $F(varList[0].substring(1, varList[0].length-1));
              for (i=0; i<fieldValue.length; i++) {
                returnString += '&' + key + '=' + encodeURIComponent(fieldValue[i]);
              }
              break;
            default:
              returnString += '&' + key + '=' + encodeURIComponent(field.innerHTML);
              break;
          }
        } else {
          // just add back the pair
          returnString += '&' + key + '=' + encodeURIComponent(val);
        }
      }
    }
  }

  if (returnString.charAt(0) == '&') {
    returnString = returnString.substr(1);
  }
  return returnString;
}

function evalBoolean(value, defaultValue) {
  if (!isNull(value) && isString(value)) {
    return (parseBoolean(value)) ? "true" : "false";
  } else {
    return defaultValue === true ? "true" : "false";
  }
}
function parseBoolean(value) {
  if (!isNull(value) && isString(value)) {
    return ( "true" == value.toLowerCase() || "yes" == value.toLowerCase());
  } else {
  	if (isBoolean(value)) {return value; }
    return false;
  }
}

// read function parameterstring
function evalJScriptParameters(paramString) {
	if (isNull(paramString) || !isString(paramString))
	{
		return null;
	} 
	return eval("new Array("+paramString+")");
}

// listener wieder anhaengen fuer TREE tag wird von htmlcontent benutzt
function reloadAjaxListeners(){
	for (i=0; i < this.ajaxListeners.length; i++){
		if ( isFunction(this.ajaxListeners[i].reload) ) {
			this.ajaxListeners[i].reload();
		}
	}
}

function addAjaxListener(obj) {
	if (!this.ajaxListeners) {
		this.ajaxListeners = new Array(obj);
	} else {
		this.ajaxListeners.push(obj);
	}
}

/* ---------------------------------------------------------------------- */
/* Example File From "_JavaScript and DHTML Cookbook"
   Published by O'Reilly & Associates
   Copyright 2003 Danny Goodman
*/

// http://jslint.com/ 
// Missing radix parameter -- setDate setHours setMinutes

// utility function to retrieve a future expiration date in proper format;
// pass three integer parameters for the number of days, hours,
// and minutes from now you want the cookie to expire; all three
// parameters required, so use zeros where appropriate
function getExpDate(days, hours, minutes) {
  var expDate = new Date();
  if (typeof days == "number" && typeof hours == "number" && typeof hours == "number") {
    expDate.setDate(expDate.getDate() + parseInt(days));
    expDate.setHours(expDate.getHours() + parseInt(hours));
    expDate.setMinutes(expDate.getMinutes() + parseInt(minutes));
    return expDate.toGMTString();
  }
}

// utility function called by getCookie()
function getCookieVal(offset) {
  var endstr = document.cookie.indexOf (";", offset);
  if (endstr == -1) {
    endstr = document.cookie.length;
  }
  return unescape(document.cookie.substring(offset, endstr));
}

// primary function to retrieve cookie by name
function getCookie(name) {
  var arg = name + "=";
  var alen = arg.length;
  var clen = document.cookie.length;
  var i = 0;
  var j;
  while (i < clen) {
    j = i + alen;
    if (document.cookie.substring(i, j) == arg) {
      return getCookieVal(j);
    }
    i = document.cookie.indexOf(" ", i) + 1;
    if (i == 0) {
    	break;
    }
  }
  return null;
}

// store cookie value with optional details as needed
function setCookie(name, value, expires, path, domain, secure) {
  document.cookie = name + "=" + escape (value) +
    ((expires) ? "; expires=" + expires : "") +
    ((path) ? "; path=" + path : "") +
    ((domain) ? "; domain=" + domain : "") +
    ((secure) ? "; secure" : "");
}

// remove the cookie by setting ancient expiration date
function deleteCookie(name,path,domain) {
  if (getCookie(name)) {
    document.cookie = name + "=" +
      ((path) ? "; path=" + path : "") +
      ((domain) ? "; domain=" + domain : "") +
      "; expires=Thu, 01-Jan-70 00:00:01 GMT";
  }
}
/* ---------------------------------------------------------------------- */
/* End Copyright 2003 Danny Goodman */
 