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
 
 
var AjaxJspTag = {
  Version: '1.3'
};

/**
 * AjaxTags
 */

AjaxJspTag.Base = function() {};
AjaxJspTag.Base.prototype = {

  resolveParameters: function() {
    // Strip URL of querystring and append it to parameters
    var qs = delimitQueryString(extractQueryString(this.url));
    if (this.options.parameters) {
      this.options.parameters += ',' + qs;
    } else {
      this.options.parameters = qs;
    }
    this.url = trimQueryString(this.url);
    
    if ((this.options.parameters.length > 0 ) && (this.options.parameters.charAt(this.options.parameters.length - 1) === ',')) {
      this.options.parameters = this.options.parameters.substr(0,this.options.parameters.length-1);
    }
  }

};

/**
 * Prefunction Invoke Ajax.Update TAG
 */
AjaxJspTag.PreFunctionUpdateInvoke = Class.create();
AjaxJspTag.PreFunctionUpdateInvoke.prototype = Object.extend(new AjaxJspTag.Base(), {

  initialize: function(ajaxupdateData) {
  this.preFunction = ajaxupdateData.preFunction;
  if (isFunction(this.preFunction))
  { 
  	this.preFunction();
  }
  if (this.cancelExecution) {
	    	this.cancelExecution = false;
	    	return ;
      	}
  var thisCall = new Ajax.Updater(ajaxupdateData.id,ajaxupdateData.href,{onComplete: ajaxupdateData.postFunction});
  }


});


/**
 * UPDATEFIELD TAG
 */
AjaxJspTag.UpdateField = Class.create();
AjaxJspTag.UpdateField.prototype = Object.extend(new AjaxJspTag.Base(), {

  initialize: function(url, options) {
    this.url = url;
    this.setOptions(options);
    this.setListeners();
    addAjaxListener(this);
  },
  reload: function () {
    this.setListeners();
  },
  setOptions: function(options) {
    this.options = Object.extend({
      parameters: options.parameters || '',
      doPost: options.doPost || false,
      valueUpdateByName:  options.valueUpdateByName || false,
      eventType: options.eventType ? options.eventType : "click",
      parser: options.parser ? options.parser :  ( options.valueUpdateByName ? new ResponseXmlParser(): new ResponseTextParser()),
      handler: options.handler ? options.handler : this.handler
    }, options || {});
  },

  setListeners: function() {
    eval("$(this.options.action).on"+this.options.eventType+" = this.execute.bindAsEventListener(this)");
  },

  execute: function(e) {
    if (isFunction(this.options.preFunction)) 
    {
    	this.options.preFunction();
	}
	if (this.options.cancelExecution) {
	    this.cancelExecution = false;
	    return ;
      }
    // parse parameters and do replacements
    var params = buildParameterString(this.options.parameters);

    // parse targets
    var targetList = this.options.target.split(',');

    var obj = this; // required because 'this' conflict with Ajax.Request
    var setFunc = this.setField;
    var aj = new Ajax.Request(this.url, {
      asynchronous: true,
      method: obj.options.doPost ? 'post':'get',
      evalScripts: true,
      parameters: params,
      onSuccess: function(request) {
        obj.options.parser.load(request);
        var results = obj.options.parser.itemList;
        obj.options.handler(request, {targets: targetList, items: results});
      },
      onFailure: function(request) {
        if (isFunction(obj.options.errorFunction)){
         obj.options.errorFunction(request,obj.options.parser);
     	}
      },
      onComplete: function(request) {
        if (isFunction(obj.options.postFunction)) { obj.options.postFunction(); }
      }
    });
  },

  handler: function(request, optionsArr) {
  // this points to options
    for (var i=0; i<optionsArr.targets.length && i<optionsArr.items.length; i++) {
   	namedIndex = i;
   	if (this.valueUpdateByName) {
    	for (j=0; j <optionsArr.items.length; j++) {
    		if (optionsArr.targets[i]  ===  optionsArr.items[j][0]) {
    			namedIndex = j;
    		}
    	}
    }
    $(optionsArr.targets[i]).value = optionsArr.items[namedIndex][1];
    }
  }

});



/**
 * CALLBACK TAG
 */
AjaxJspTag.Callback = Class.create();
AjaxJspTag.Callback.prototype = Object.extend(new AjaxJspTag.Base(), {

  initialize: function(url, options) {
    this.url = url;
    this.setOptions(options); 
    this.errorCount = 0;
    addOnLoadEvent(this );
  },
  setOptions: function(options) {
    this.options = Object.extend({
      parameters: options.parameters || '',
      parser: options.parser ? options.parser : new ResponseCallBackXmlParser(),
      plainText: options.plainText ?   true : false ,
      handler: options.handler ? options.handler : this.handler
    }, options || {});
  },
  onload: function(){
  	this.run();
  },
  run: function(){
  // wenn fehler kommen den client veranlassen eben nicht mehr versuchen sich anzumelden
    if (!this.isRunning && this.errorCount < 100) {
      this.execute();
    }  
  },
  execute: function(e) {
    if (isFunction(this.options.preFunction)) { this.options.preFunction();}
	if (this.options.cancelExecution) {
	    this.cancelExecution = false;
	    return ;
      }
    // parse parameters and do replacements
    //var params = buildParameterString(this.options.parameters);

    // parse targets
    this.isRunning = true;
    var obj = this; // required because 'this' conflict with Ajax.Request
    var aj = new Ajax.Request(this.url, {
      asynchronous: true,
      method:  'post',
      evalScripts: true,
      onSuccess: function(request) {
        obj.options.parser.load(request);
        obj.options.list = obj.options.parser.items; 
        obj.errorCount = 0;
      
      },
      onFailure: function(request) {
        if (isFunction(obj.options.errorFunction)) {obj.options.errorFunction();}
        obj.isRunning = false;
        obj.errorCount++;
      },
      onComplete: function(request) {
      	// nun this.list kann mit der antwor alles gemacht werden was man will
        if (isFunction(obj.options.postFunction)) {obj.options.postFunction();}
        obj.isRunning = false;
        obj.run();
      }
    });
  }
});
/// callback -- ende





/**
 * SELECT TAG
 */
AjaxJspTag.Select = Class.create();
AjaxJspTag.Select.prototype = Object.extend(new AjaxJspTag.Base(), {

  initialize: function(url, options) {
    this.url = url;
    this.setOptions(options);
    this.setListeners();

    if (parseBoolean(this.options.executeOnLoad)) {
      this.execute();
    }
   addAjaxListener(this);
  },
  reload: function () {
    this.setListeners();
  },
  
  setOptions: function(options) {
    this.options = Object.extend({
      parameters: options.parameters || '',
      doPost: options.doPost || false,
      emptyOptionValue: options.emptyOptionValue || '',
      emptyOptionName:  options.emptyOptionName || '',
      eventType: options.eventType ? options.eventType : "change",
      parser: options.parser ? options.parser : new ResponseXmlParser(),
      handler: options.handler ? options.handler : this.handler
    }, options || {});
  },

  setListeners: function() {
  $(this.options.source).ajaxSelect = this; 
  
    Event.observe($(this.options.source),
      this.options.eventType,
      this.execute.bindAsEventListener(this),
      false);
    eval("$(this.options.source).on"+this.options.eventType+" = function(){return false;};");
  },

  execute: function(e) {
    if (isFunction(this.options.preFunction)) {this.options.preFunction();}
	if (this.options.cancelExecution) {
	    this.cancelExecution = false;
	    return ;
      }
    // parse parameters and do replacements
    var params = buildParameterString(this.options.parameters);

    var obj = this; // required because 'this' conflict with Ajax.Request
    var aj = new Ajax.Request(this.url, {
      asynchronous: true,
      method: obj.options.doPost ? 'post':'get',
      evalScripts: true,
      parameters: params,
      onSuccess: function(request) {
        obj.options.parser.load(request);
        var results = obj.options.parser.itemList;
        obj.options.handler(request, {target: obj.options.target,
                                      items: results });
      },
      onFailure: function(request) {
        if (isFunction(obj.options.errorFunction)){ obj.options.errorFunction();}
      },
      onComplete: function(request) {
        if (isFunction(obj.options.postFunction)) {obj.options.postFunction();}
      }
    });
  },

  handler: function(request, options) {
    // build an array of option values to be set as selected
    
    $(options.target).options.length = 0;
    $(options.target).disabled = false;
    for (var i=0; i<options.items.length; i++) {
      var newOption = new Option(options.items[i][0], options.items[i][1]);
      //$(options.target).options[i] = new Option(options.items[i][0], options.items[i][1]);
      // set the option as selected if it is in the default list
      if ( newOption.selected == false && options.items[i].length == 3 && parseBoolean(options.items[i][2]) ){
           newOption.selected = true;
      }
      $(options.target).options[i] = newOption;
    }
    
    
    if (options.items.length == 0)
    {
      $(options.target).options[i] = new Option(this.emptyOptionName, this.emptyOptionValue);
    	$(options.target).disabled = true;
    }
    // auch ein SELECT TAG ?
   	if ($(options.target).ajaxSelect && $(options.target).ajaxSelect.execute)
   	{
   		$(options.target).ajaxSelect.execute();
   	}
  }

});


/**
 * HTMLCONTENT TAG
 */
AjaxJspTag.HtmlContent = Class.create();
AjaxJspTag.HtmlContent.prototype = Object.extend(new AjaxJspTag.Base(), {

  initialize: function(url, options) {
    this.url = url;
    this.setOptions(options);
    this.setListeners();
    addAjaxListener(this);
    
  },
  reload: function(){
  	this.setListeners();
  },
  setOptions: function(options) {
    this.options = Object.extend({
      parameterName: options.parameterName ? options.parameterName : AJAX_DEFAULT_PARAMETER,
      parameters: options.parameters || '',
      doPost: options.doPost || false,
      
      preFunctionParameter:options.preFunctionParameter || null,
      errorFunctionParameter:  options.errorFunctionParameter || null,
      postFunctionParameter: options.postFunctionParameter || null,
      
      eventType: options.eventType ? options.eventType : "click",
      parser: options.parser ? options.parser : new ResponseHtmlParser(),
      handler: options.handler ? options.handler : this.handler
    }, options || {});
  },

  setListeners: function() {
    if (this.options.source) {
      eval("$(this.options.source).on"+this.options.eventType+" = this.execute.bindAsEventListener(this)");
    } else if (this.options.sourceClass) {
      var elementArray = document.getElementsByClassName(this.options.sourceClass);
      for (var i=0; i<elementArray.length; i++) {
        eval("elementArray[i].on"+this.options.eventType+" = this.execute.bindAsEventListener(this)");
      }
    }
  },

  execute: function(e) {
  	this.options.preFunctionParameters = evalJScriptParameters(  this.options.preFunctionParameter);
   
    if (isFunction(this.options.preFunction)) {this.options.preFunction();}
	if (this.options.cancelExecution) {
	    this.cancelExecution = false;
	    return ;
      }
    // replace default parameter with value/content of source element selected
    var ajaxParameters = this.options.parameters;
    if (this.options.sourceClass) {
      var re = new RegExp("(\\{"+this.options.parameterName+"\\})", 'g');
      var elem = Event.element(e);
      if (elem.type) {
        ajaxParameters = ajaxParameters.replace(re, $F(elem));
      } else {
        ajaxParameters = ajaxParameters.replace(re, elem.innerHTML);
      }
    }

    // parse parameters and do replacements
    var params = buildParameterString(ajaxParameters);

    var obj = this; // required because 'this' conflict with Ajax.Request
    var aj = new Ajax.Updater(this.options.target, this.url, {
      asynchronous: true,
      method: obj.options.doPost ? 'post':'get',
      evalScripts: true,
      parameters: params,
      onFailure: function(request) {
        obj.options. errorFunctionParameters =  evalJScriptParameters(  obj.options.errorFunctionParameter  );
        if (isFunction(obj.options.errorFunction)) {obj.options.errorFunction();}
      },
      onComplete: function(request) {
        obj.options. postFunctionParameters =  evalJScriptParameters(   obj.options.postFunctionParameter);
        if (isFunction(obj.options.postFunction)) {obj.options.postFunction();}
      }
    });
  }

});

/**
 * TREE TAG
 */
AjaxJspTag.Tree = Class.create();
AjaxJspTag.Tree.prototype = Object.extend(new AjaxJspTag.Base(), {

  initialize: function(url, options) {
    this.url = url;
    this.setOptions(options);
    this.execute();
  },

  setOptions: function(options) {
    this.options = Object.extend({
      parameters: options.parameters || '',
      eventType: options.eventType ? options.eventType : "click",
      parser: options.parser ? options.parser : new ResponseXmlToHtmlLinkListParser(),
      handler: options.handler ? options.handler : this.handler,
      collapsedClass: options.collapsedClass ? options.collapsedClass : "collapsedNode",
      expandedClass: options.expandedClass ? options.expandedClass : "expandedNode",
      treeClass: options.treeClass ? options.treeClass : "tree",
      nodeClass: options.nodeClass || ''
    }, options || {});
    this.calloutParameter = AJAX_DEFAULT_PARAMETER;
  },

  execute: function(e) {
    if (isFunction(this.options.preFunction)) {this.options.preFunction();}
	if (this.options.cancelExecution) {
	    this.cancelExecution = false;
	    return ;
      }
       
    //if the node is expanded, just collapse it
    if(this.options.target != null) {
      var imgElem = $("span_" + this.options.target);
      if(imgElem != null) {
        var expanded = this.toggle(imgElem);
        if(!expanded) {
          $(this.options.target).innerHTML = "";
             if (! $(this.options.target).style)
       			$(this.options.target).setAttribute("style","");
    
     		$(this.options.target).style.display ="none";
    
          return;
        }
      }
    }
    // indicator 
    
    // parse parameters and do replacements
    var ajaxParameters = this.options.parameters || '';
    var re = new RegExp("(\\{"+this.calloutParameter+"\\})", 'g');
    ajaxParameters = ajaxParameters.replace(re, this.options.target);
    
    var params = buildParameterString(ajaxParameters);
      
    var obj = this; // required because 'this' conflict with Ajax.Request
    var aj = new Ajax.Request(this.url, {
      asynchronous: true,
      method: 'get',
      evalScripts: true,
      parameters: params,
      onSuccess: function(request) {
      // IE 5,6 BUG 
      	objx = new Object();
		objx.responseXML = request.responseXML;
      
         obj.options.parser.load(Object.extend(objx, {
                                                   collapsedClass: obj.options.collapsedClass,
                                                   treeClass:      obj.options.treeClass,
                                                   nodeClass:      obj.options.nodeClass}));
         obj.options.handler(objx, {target:    obj.options.target,
                                       parser:    obj.options.parser,
                                       eventType: obj.options.eventType,
                                       url:       obj.url});
      },
      onFailure: function(request) {
        if (isFunction(obj.options.errorFunction)){ obj.options.errorFunction();}
      },
      onComplete: function(request) {
        if (isFunction(obj.options.postFunction)) {obj.options.postFunction();}
		// damit htmlcontent wieder geht
        reloadAjaxListeners();
      }
    });
  },
  
  toggle: function (e) {
    var expanded =  e.className == this.options.expandedClass;
    e.className =  expanded ? this.options.collapsedClass : this.options.expandedClass;
    return !expanded;
  },
  
  handler: function(request, options) {
    var parser = options.parser;
    var target = $(options.target);
    if (parser.content == null) {

         // div.setAttribute("style","");
        //  div.style.display ="none";
          
    if (!target.style)
      target.setAttribute("style","");
    
    target.style.display ="none";
    
    	 target.innerHTML = "";
     	return;
    }
      

     
    target.appendChild(parser.content);   
    
    if (!target.style)
      target.setAttribute("style","");
    
    target.style.display ="block";
    
    var images = target.getElementsByTagName("span");
    for (var i=0; i<images.length; i++) {
      //get id
      var id = images[i].id.substring(5);
      var toggleFunction = "function() {toggleTreeNode('" +  id + "', '" + options.url + "', null);}";
      eval("images[i].on" + options.eventType + "=" + toggleFunction); 
    }
   
    //toggle the one that must be expanded
    var expandedNodes = parser.expandedNodes;
    for (var i=0; i<expandedNodes.length; i++) {
       toggleTreeNode(expandedNodes[i], options.url, null);
    }
  }

});

/**
 * TABPANEL TAG
 */
AjaxJspTag.TabPanel = Class.create();
AjaxJspTag.TabPanel.prototype = Object.extend(new AjaxJspTag.Base(), {

  initialize: function(url, options) {
    this.url = url;
    this.setOptions(options);
    this.execute();
  },

  setOptions: function(options) {
    this.options = Object.extend({
      parameters: options.parameters || '',
      eventType: options.eventType ? options.eventType : "click",
      parser: options.parser ? options.parser : new ResponseHtmlParser(),
      handler: options.handler ? options.handler : this.handler
    }, options || {});
  },

  execute: function(e) {
    if (isFunction(this.options.preFunction)){ this.options.preFunction();}
	if (this.options.cancelExecution) {
	    this.cancelExecution = false;
	    return ;
      }
    // parse parameters and do replacements
    this.resolveParameters();
    var params = buildParameterString(this.options.parameters);

    var obj = this; // required because 'this' conflict with Ajax.Request
    var aj = new Ajax.Updater(this.options.target, this.url, {
      asynchronous: true,
      method: 'get',
      evalScripts: true,
      parameters: params,
      onSuccess: function(request) {
        var src;
        if (obj.options.source) {
          src = obj.options.source;
        } else {
          src = document.getElementsByClassName(obj.options.currentStyleClass,
                                                $(obj.options.panelId))[0];
        }
        obj.options.handler(request, {source: src,
                                      panelStyleId: obj.options.panelId,
                                      currentStyleClass: obj.options.currentStyleClass});
      },
      onFailure: function(request) {
        if (isFunction(obj.options.errorFunction)){ obj.options.errorFunction();}
      },
      onComplete: function(request) {
        if (isFunction(obj.options.postFunction)){ obj.options.postFunction();}
      }
    });
  },

  handler: function(request, options) {
    // find current anchor
    var cur = document.getElementsByClassName(options.currentStyleClass, $(options.panelStyleId));
    // remove class
    if(cur.length > 0)
        cur[0].className = '';
    // add class to selected tab
    options.source.className = options.currentStyleClass;
  }

});


/**
 * PORTLET TAG
 */
AjaxJspTag.Portlet = Class.create();
AjaxJspTag.Portlet.prototype = Object.extend(new AjaxJspTag.Base(), {

  initialize: function(url, options) {
    this.url = url;
    this.setOptions(options);
    this.setListeners();
    if (parseBoolean(this.options.executeOnLoad )) {
      this.execute();
    }
    if (this.preserveState) this.checkCookie();
    
    if (parseBoolean(this.options.startMinimize)) {
   		this.togglePortlet();
    }
    addAjaxListener(this);
    // should i reloadAjaxListeners() after execute?
  },
  reload: function () {
    this.setListeners();
  },
  
  setOptions: function(options) {
    this.options = Object.extend({
      parameters: options.parameters || '',
      target: options.source+"Content",
      close: options.source+"Close",
      startMinimize: options.startMinimize || false,
      refresh: options.source+"Refresh",
      toggle: options.source+"Size",
      isMaximized: true,
      expireDays: options.expireDays || "0",
      expireHours: options.expireHours || "0",
      expireMinutes: options.expireMinutes || "0",
      executeOnLoad: evalBoolean(options.executeOnLoad, true),
      refreshPeriod: options.refreshPeriod || null,
      eventType: options.eventType ? options.eventType : "click",
      parser: options.parser ? options.parser : new ResponseHtmlParser(),
      handler: options.handler ? options.handler : this.handler
    }, options || {});

    if (parseInt(this.options.expireDays) > 0
        || parseInt(this.options.expireHours) > 0
        || parseInt(this.options.expireMinutes) > 0) {
      this.preserveState = true;
      this.options.expireDate = getExpDate(
        parseInt(this.options.expireDays),
        parseInt(this.options.expireHours),
        parseInt(this.options.expireMinutes));
    }

    this.isAutoRefreshSet = false;
  },

  setListeners: function() {
    if (this.options.imageClose) {
      eval("$(this.options.close).on"+this.options.eventType+" = this.closePortlet.bindAsEventListener(this)");
    }
    if (this.options.imageRefresh) {
      eval("$(this.options.refresh).on"+this.options.eventType+" = this.refreshPortlet.bindAsEventListener(this)");
    }
    if (this.options.imageMaximize && this.options.imageMinimize) {
      eval("$(this.options.toggle).on"+this.options.eventType+" = this.togglePortlet.bindAsEventListener(this)");
    }
  },

  execute: function(e) {
    if (isFunction(this.options.preFunction)){ this.options.preFunction();}
	if (this.options.cancelExecution) {
	    this.cancelExecution = false;
	    return ;
      }
    // parse parameters and do replacements
    this.resolveParameters();
    var params = buildParameterString(this.options.parameters);

    var obj = this; // required because 'this' conflict with Ajax.Request
    if (this.options.refreshPeriod && this.isAutoRefreshSet == false) {
      // periodic updater
      var freq = this.options.refreshPeriod;
      this.ajaxPeriodicalUpdater = new Ajax.PeriodicalUpdater(this.options.target, this.url, {
        asynchronous: true,
        method: 'get',
        evalScripts: true,
        parameters: params,
        frequency: freq,
        onFailure: function(request) {
          if (isFunction(obj.options.errorFunction)){ obj.options.errorFunction();}
        },
        onComplete: function(request) {},
        onSuccess: function(request) {
          if (isFunction(obj.options.postFunction)) {obj.options.postFunction();}
        }
      });

      this.isAutoRefreshSet = true;
    } else {
      // normal updater
      this.ajaxUpdater = new Ajax.Updater(this.options.target, this.url, {
        asynchronous: true,
        method: 'get',
        parameters: params,
        evalScripts: true,
        onFailure: function(request) {
          if (isFunction(obj.options.errorFunction)) {obj.options.errorFunction();}
        },
        onComplete: function(request) {
          if (isFunction(obj.options.postFunction)) {obj.options.postFunction();}
        }
      });
    }
    
  },

  checkCookie: function() {
    // Check cookie for save state
    var cVal = getCookie("AjaxJspTag.Portlet."+this.options.source);
    if (cVal != null) {
      if (cVal == AJAX_PORTLET_MIN) {
        this.togglePortlet();
      } else if (cVal == AJAX_PORTLET_CLOSE) {
        this.closePortlet();
      }
    }
  },

  stopAutoRefresh: function() {
    // stop auto-update if present
    if (this.ajaxPeriodicalUpdater != null
        && this.options.refreshPeriod
        && this.isAutoRefreshSet == true) {
      this.ajaxPeriodicalUpdater.stop();
    }
  },

  startAutoRefresh: function() {
    // stop auto-update if present
    if (this.ajaxPeriodicalUpdater != null && this.options.refreshPeriod) {
      this.ajaxPeriodicalUpdater.start();
    }
  },

  refreshPortlet: function(e) {
    // clear existing updater
    this.stopAutoRefresh();
    if (this.ajaxPeriodicalUpdater != null) {
      this.startAutoRefresh();
    } else {
      this.execute();
    }
  },

  closePortlet: function(e) {
    this.stopAutoRefresh();
    Element.remove(this.options.source);
    // Save state in cookie
    if (this.preserveState) {
      setCookie("AjaxJspTag.Portlet."+this.options.source,
        AJAX_PORTLET_CLOSE,
        this.options.expireDate);
    }
  },

  togglePortlet: function(e) {
    Element.toggle(this.options.target);
    if (this.options.isMaximized) {
    if (this.options.imageMaximize){
      $(this.options.toggle).src = this.options.imageMaximize;
      }
      this.stopAutoRefresh();
    } else {
     if (this.options.imageMinimize){
      $(this.options.toggle).src = this.options.imageMinimize;
      }
      this.startAutoRefresh();
    }
    this.options.isMaximized = !this.options.isMaximized;
    // Save state in cookie
    if (this.preserveState) {
      setCookie("AjaxJspTag.Portlet."+this.options.source,
        (this.options.isMaximized === true ? AJAX_PORTLET_MAX : AJAX_PORTLET_MIN),
        this.options.expireDate);
    }
  }

});


/**
 * AUTOCOMPLETE TAG
 */
Ajax.XmlToHtmlAutocompleter = Class.create();
Object.extend(Object.extend(Ajax.XmlToHtmlAutocompleter.prototype,  Autocompleter.Base.prototype), {
  initialize: function(element, update, url, options) {
    this.baseInitialize(element, update, options);
    this.options.asynchronous  = true;
    this.options.onComplete    = this.onComplete.bind(this);
    this.options.defaultParams = this.options.parameters || null;
    this.url                   = url;
  },
  // onblur hack IE works with FF
     onBlur: function (event) {
  	  // Dont hide the div on "blur" if the user clicks scrollbar 
	if(Element.getStyle(this.update, 'height') != ''){ 
 		var x=999999;
 		var y=999999;
 		var offsets = Position.positionedOffset(this.update);
 		var top = offsets[1];
 		var left = offsets[0];
 		var data = Element.getDimensions(this.update);
 		var width = data.width;
 		var height = data.height;
 		if (event)
 		{
 			x=event.x-left;
 			y=event.y -top;
 		} 
 		
        if (x > 0 && x <  width && y > 0 && y < height )
        { 
      	this.element.focus();
        return;
        } 
      }
      
        // needed to make click events working
    setTimeout(this.hide.bind(this), 250);
    this.hasFocus = false;
    this.active = false;     
      
      
  },
  getUpdatedChoices: function() {
   if (isFunction(this.options.preFunction)){ this.options.preFunction();}
      // preFunction can cancelExecution set this.cancelExecution = true;
	  if (this.options.cancelExecution) {
	    this.cancelExecution = false;
	    this.stopIndicator();
	    return ;
      }
    entry = encodeURIComponent(this.options.paramName) + '=' + 
      encodeURIComponent(this.getToken());

    this.options.parameters = this.options.callback ?
      this.options.callback(this.element, entry) : entry;

    // parse parameters and do replacements
    var params = buildParameterString(this.options.defaultParams);
    if (!isEmpty(params) || (isString(params) && params.length > 0)) {
      this.options.parameters += '&' + params;
    }

    new Ajax.Request(this.url, this.options);
  },
  onComplete: function(request) {
    var parser = this.options.parser;
    parser.load(request);
    this.updateChoices(parser.content);
  } 

});

AjaxJspTag.Autocomplete = Class.create();
AjaxJspTag.Autocomplete.prototype = Object.extend(new AjaxJspTag.Base(), {

  initialize: function(url, options) {
    this.url = url;
    this.setOptions(options);
    // create DIV
    new Insertion.After(this.options.source, '<div id="' + this.options.divElement + '" class="' + this.options.className + '"></div>');
    this.execute();

  },

  setOptions: function(options) {
    this.options = Object.extend({
      divElement: "ajaxAuto_" + options.source,
      indicator: options.indicator || '',
      parameters: options.parameters || '',
      parser: options.parser ? options.parser : new ResponseXmlToHtmlListParser(),
      handler: options.handler ? options.handler : this.handler
    }, options || {});
  },

  execute: function(e) {
      // preFunction moved bevor request now
    var obj = this; // required because 'this' conflict with Ajax.Request
    var aj = new Ajax.XmlToHtmlAutocompleter(
                     this.options.source,
                     this.options.divElement,
                     this.url, {minChars: obj.options.minimumCharacters,
                                tokens: obj.options.appendSeparator,
                                indicator: obj.options.indicator,
                                parameters: obj.options.parameters,
                                evalScripts: true,
                                preFunction: obj.options.preFunction,
                                parser: obj.options.parser,
                                afterUpdateElement: function(inputField, selectedItem) {
                                  obj.options.handler(null, {
                                    selectedItem: selectedItem,
                                    tokens: obj.options.appendSeparator,
                                    target: obj.options.target,
                                    inputField: inputField,
                                    postFunction: obj.options.postFunction,
                                    list:obj.options.parser.getArray(),
                                    options: obj.options,
                                    autocomplete:aj
                                    }
                                  );
                                }
                               }
             );
  },

  handler: function(request, options) {
    if (options.target) {
      if (options.tokens) {
        if ($(options.target).value.length > 0) {
          $(options.target).value += options.tokens;
        }
        $(options.target).value += options.selectedItem.id;
      } else {
        $(options.target).value = options.selectedItem.id;
      }
    }
    options.selectedIndex = options.autocomplete.index;
    options.selectedObject = options.list[options.autocomplete.index];
    
    if (isFunction(options.postFunction)) {
      //Disable onupdate event handler of input field
      //because, postFunction can change the content of
      //input field and get into eternal loop.
      var onupdateHandler = $(options.inputField).onupdate;
      $(options.inputField).onupdate = '';
      
      options.postFunction();
      //Enable onupdate event handler of input field
      $(options.inputField).onupdate = onupdateHandler;
    }
  }

});


/**
 * TOGGLE TAG
 */
AjaxJspTag.Toggle = Class.create();
AjaxJspTag.Toggle.prototype = Object.extend(new AjaxJspTag.Base(), {

  initialize: function(url, options) {
    this.url = url;
    this.setOptions(options);

    // create message DIV
    if (this.options.messageClass) {
      this.messageContainer = new Insertion.Top($(this.options.source),
        '<div id="'+ this.options.source +'_message" class="' + this.options.messageClass +'"></div>');
    }

    this.setListeners();
    addAjaxListener(this);
  },
  reload: function () {
    this.setListeners();
  },
  
  setOptions: function(options) {
    this.options = Object.extend({
      parameters: options.parameters || 'rating={ajaxParameter}',
      parser: options.parser ? options.parser : new ResponseTextParser(),
      handler: options.handler ? options.handler : this.handler,
      updateFunction: options.updateFunction || false
    }, options || {});
    this.ratingParameter = AJAX_DEFAULT_PARAMETER;
  },

  setListeners: function() {
    // attach events to anchors
    var elements = $(this.options.source).getElementsByTagName('a');
    for (var j=0; j<elements.length; j++) {
      elements[j].onmouseover = this.raterMouseOver.bindAsEventListener(this);
      elements[j].onmouseout = this.raterMouseOut.bindAsEventListener(this);
      elements[j].onclick = this.raterClick.bindAsEventListener(this);
    }
  },

  getCurrentRating: function(list) {
    var selectedIndex = -1;
    for (var i=0; i<list.length; i++) {
      if (Element.hasClassName(list[i], this.options.selectedClass)) {
        selectedIndex = i;
      }
    }
    return selectedIndex;
  },

  getCurrentIndex: function(list, elem) {
    var currentIndex = 0;
    for (var i=0; i<list.length; i++) {
      if (elem == list[i]) {
        currentIndex = i;
      }
    }
    return currentIndex;
  },

  raterMouseOver: function (e) {
    // get containing div
    var container = Event.findElement(e, 'div');

    // get list of all anchors
    var elements = container.getElementsByTagName('a');

    // find the current rating
    var selectedIndex = this.getCurrentRating(elements);

    // find the index of the 'hovered' element
    var currentIndex = this.getCurrentIndex(elements, Event.element(e));

    // set message
    if (this.options.messageClass) {
      $(container.id+'_message').innerHTML = Event.element(e).title;
    }

    // iterate over each anchor and apply styles
    for (var i=0; i<elements.length; i++) {
      if (selectedIndex > -1) {
        if (i <= selectedIndex && i <= currentIndex)
          Element.addClassName(elements[i], this.options.selectedOverClass);
        else if (i <= selectedIndex && i > currentIndex)
          Element.addClassName(elements[i], this.options.selectedLessClass);
        else if (i > selectedIndex && i <= currentIndex)
          Element.addClassName(elements[i], this.options.overClass);
      } else {
        if (i <= currentIndex) Element.addClassName(elements[i], this.options.overClass);
      }
    }
  },

  raterMouseOut: function (e) {
    // get containing div
    var container = Event.findElement(e, 'div');

    // get list of all anchors
    var elements = container.getElementsByTagName('a');

    // clear message
    if (this.options.messageClass) {
      $(container.id+'_message').innerHTML = '';
    }

    // iterate over each anchor and apply styles
    for (var i=0; i<elements.length; i++) {
      Element.removeClassName(elements[i], this.options.selectedOverClass);
      Element.removeClassName(elements[i], this.options.selectedLessClass);
      Element.removeClassName(elements[i], this.options.overClass);
    }
  },

  raterClick: function (e) {
    // get containing div
    var container = Event.findElement(e, 'div');

    // get list of all anchors
    var elements = container.getElementsByTagName('a');

    // find the index of the 'hovered' element
    var currentIndex = this.getCurrentIndex(elements, Event.element(e));

    // update styles
    for (var i=0; i<elements.length; i++) {
      Element.removeClassName(elements[i], this.options.selectedOverClass);
      Element.removeClassName(elements[i], this.options.selectedLessClass);
      Element.removeClassName(elements[i], this.options.overClass);
      if (i <= currentIndex) {
        if (Element.hasClassName(container, 'onoff')
              && Element.hasClassName(elements[i], this.options.selectedClass)) {
          Element.removeClassName(elements[i], this.options.selectedClass);
        } else {
          Element.addClassName(elements[i], this.options.selectedClass);
        }
      } else if (i > currentIndex) {
        Element.removeClassName(elements[i], this.options.selectedClass);
      }
    }

    // send AJAX
    var ratingToSend = elements[currentIndex].title;
    if (Element.hasClassName(container, 'onoff')) {
      // send opposite of what was selected
      var ratings = this.options.ratings.split(',');
      if (ratings[0] == ratingToSend) ratingToSend = ratings[1];
      else ratingToSend = ratings[0];
      elements[currentIndex].title = ratingToSend;
    }
    this.execute(ratingToSend);

    // set field (if defined)
    if (this.options.state) {
      $(this.options.state).value = ratingToSend;
    }
  },

  execute: function(ratingValue) {
    if (isFunction(this.options.preFunction)){ this.options.preFunction();}
	if (this.options.cancelExecution) {
	    this.cancelExecution = false;
	    return ;
      }
    // parse parameters and do replacements
    var ajaxParameters = this.options.parameters || '';
    var re = new RegExp("(\\{"+this.ratingParameter+"\\})", 'g');
    ajaxParameters = ajaxParameters.replace(re, ratingValue);
    var params = buildParameterString(ajaxParameters);

    var obj = this; // required because 'this' conflict with Ajax.Request
    var toggleStateFunc = this.getToggleStateValue;
    var aj = new Ajax.Request(this.url, {
      asynchronous: true,
      method: 'get',
      evalScripts: true,
      parameters: params,
      onSuccess: function(request) {
        obj.options.parser.load(request);
        var results = obj.options.parser.itemList;
        obj.options.handler(request, {items: results});
      },
      onFailure: function(request) {
        if (isFunction(obj.options.errorFunction)){ obj.options.errorFunction();}
      },
      onComplete: function(request) {
        if (isFunction(obj.options.postFunction)) {obj.options.postFunction();}
      }
    });
  },

  handler: function(request, roptions) {
  //daten in items
  	var erg = roptions.items[0][0] ; // on/off / 1,2,3
  	try  {
  	this.updateFunction(erg);
    // TODO: anything?
    } catch (e) {} // muss nicht forhanden sein
  },

  getToggleStateValue: function(name, results) {
    for (var i=0; i<results.length; i++) {
      if (results[i][0] == name) {return results[i][1];}
    }
    return "";
  }

});


/**
 * CALLOUT TAG
 */
AjaxJspTag.Callout = Class.create();
AjaxJspTag.Callout.prototype = Object.extend(new AjaxJspTag.Base(), {

  initialize: function(url, options) {
    this.url = url;
    this.setOptions(options);
    this.setListeners();
    addAjaxListener(this);
  },
  reload: function () {
    this.setListeners();
  },
  
  setOptions: function(options) {
    this.options = Object.extend({
      parameters: options.parameters || '',
      overlib: options.overlib || AJAX_CALLOUT_OVERLIB_DEFAULT,
      parser: options.parser ? options.parser : new ResponseXmlToHtmlParser(),
      handler: options.handler ? options.handler : this.handler,
      doPost: options.doPost? true : false ,
      openEvent: options.openEvent ? options.openEvent : "mouseover",
      closeEvent: options.closeEvent ? options.closeEvent : "mouseout"
    }, options || {});
    this.calloutParameter = AJAX_DEFAULT_PARAMETER;
  },

  setListeners: function() {
    if (this.options.sourceClass) {
      var elemList = document.getElementsByClassName(this.options.sourceClass);
      for (var i=0; i<elemList.length; i++) {
        eval("elemList[i].on"+this.options.openEvent+" = this.calloutOpen.bindAsEventListener(this)");
        eval("elemList[i].on"+this.options.closeEvent+" = this.calloutClose.bindAsEventListener(this)");
      }
    }
  },

  calloutOpen: function(e) {
    this.execute(e);
  },

  calloutClose: function(e) {
    nd();
  },

  execute: function(e) {
    if (isFunction(this.options.preFunction)){ this.options.preFunction();}
	if (this.options.cancelExecution) {
	    this.cancelExecution = false;
	    return ;
      }
    // parse parameters and do replacements
    var ajaxParameters = this.options.parameters || '';
    var re = new RegExp("(\\{"+this.calloutParameter+"\\})", 'g');
    var elem = Event.element(e);
    if (elem.type) {
      ajaxParameters = ajaxParameters.replace(re, $F(elem));
    } else {
      ajaxParameters = ajaxParameters.replace(re, elem.innerHTML);
    }
    var params = buildParameterString(ajaxParameters);

    var obj = this; // required because 'this' conflict with Ajax.Request
    var aj = new Ajax.Request(this.url, {
      asynchronous: true,
      method: obj.options.doPost ? 'post':'get',
      evalScripts: true,
      parameters: params,
      onSuccess: function(request) {
        obj.options.parser.load(request);
        obj.options.handler(obj.options.parser.content, {title: obj.options.title,
                                                         overlib: obj.options.overlib});
      },
      onFailure: function(request) {
        if (isFunction(obj.options.errorFunction)){ obj.options.errorFunction();}
      },
      onComplete: function(request) {
        if (isFunction(obj.options.postFunction)){ obj.options.postFunction();}
      }
    });
  },

  handler: function(content, options) {
  if (content != "") { // #4 
    if (options.overlib) {
      if (options.title) {
        return eval("overlib(content,CAPTION,options.title,"+options.overlib+")");
      } else {
        return eval("overlib(content,"+options.overlib+")");
      }
    } else {
      if (options.title) {
        return overlib(content,CAPTION,options.title);
      } else {
        return overlib(content);
      }
    }
  }
 }
});
