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
* Response Parsers
*/
var AbstractResponseParser = function () {
	this.getArray = function () {
		return null;
	};
}; 


/**
* parser to work easy with xml response
* create  to make them known
*/
var DefaultResponseParser = Class.create();
var ResponseTextParser = Class.create();
var ResponseXmlParser = Class.create();
var ResponseHtmlParser = Class.create();
var ResponseXmlToHtmlParser = Class.create();
var ResponseCallBackXmlParser = Class.create();
var ResponsePlainTextXmlToHtmlParser = Class.create();
var ResponseXmlToHtmlListParser = Class.create();
var ResponseXmlToHtmlLinkListParser = Class.create();

DefaultResponseParser.prototype = Object.extend(new AbstractResponseParser(), {
  initialize: function() {
    this.type = "xml";
  },
  getArray: function () {
    return this.itemList;
  },
  load: function(request) {
    this.content = request.responseXML;
    this.parse();
    this.prepareData( this.itemList);
  },
  // format <name><value><value><value>....<value>
  prepareData: function( dataarray ) {},
  
  parse: function() {
    root = this.content.documentElement;
    responseNodes = root.getElementsByTagName("response");
    this.itemList = [];
    if (responseNodes.length > 0) {
      responseNode = responseNodes[0];
      itemNodes = responseNode.getElementsByTagName("item");
      for (i=0; i<itemNodes.length; i++) {
        nameNodes = itemNodes[i].getElementsByTagName("name");
        valueNodes = itemNodes[i].getElementsByTagName("value");
        if (nameNodes.length > 0 && valueNodes.length > 0) {
          name = nameNodes[0].firstChild ? nameNodes[0].firstChild.nodeValue : "";
          myData = [];
          myData.push(name);
            for (j=0; j <valueNodes.length; j++) {
              value = valueNodes[j].firstChild ? valueNodes[j].firstChild.nodeValue: "";
       		  myData.push(value);
            }
          this.itemList.push(myData);
        }
      }
    }
  }
});


ResponseTextParser.prototype = Object.extend(new AbstractResponseParser(), {
  initialize: function() {
    this.type = "text";
  },

  load: function(request) {
    this.content = request.responseText;
    this.split();
  },

  split: function() {
    this.itemList = [];
    var lines = this.content.split('\n');
    for (i=0; i<lines.length; i++) {
      this.itemList.push(lines[i].split(','));
    }
  }
});

ResponseXmlParser.prototype = Object.extend(new DefaultResponseParser(), {
  prepareData: function(request,dataarray) {
  }
});


ResponseHtmlParser.prototype = Object.extend(new AbstractResponseParser(), {
  initialize: function() {
    this.type = "html";
  },

  load: function(request) {
    this.content = request.responseText;
  }
});

ResponseXmlToHtmlParser.prototype = Object.extend(new DefaultResponseParser(), {
  initialize: function() {
    this.type = "xmltohtml";
  	this.plaintext = false;
  },
  prepareData: function( dataarray) {
   this.contentdiv = document.createElement("div");
   
   for (i=0; i < dataarray.length; i++)
   {
     h1 =  document.createElement("h1");
     if (!this.plaintext) {
       h1.innerHTML += dataarray[i][0];
     } else {
       h1.appendChild(document.createTextNode(dataarray[i][0]));
     }
     this.contentdiv.appendChild(h1);
     for (j=1; j < dataarray[i].length; j++) {
       div =  document.createElement("div");
       if (!this.plaintext) {
         div.innerHTML += dataarray[i][j];
       } else {
         div.appendChild(document.createTextNode(dataarray[i][j]));
       }
       this.contentdiv.appendChild(div);
     }
   }
   //#4
   if (dataarray.length >= 1) {
   	this.content =  this.contentdiv.innerHTML;
   }
	else {
	   this.content = ""; // keine daten dann ''
	}
   // skip plz 
   
   
  } 
});

// server callback

ResponseCallBackXmlParser.prototype = Object.extend(new DefaultResponseParser(), {
  initialize: function() {
    this.type = "xml";
  },
  prepareData: function( dataarray) {
   this.items = [];
   
   for (i=0; i < dataarray.length; i++)
   {
	this.items.push( [ dataarray[i][0],dataarray[i][1],(dataarray[i][2] ? true : false) ] );
   }
  } 
});



ResponsePlainTextXmlToHtmlParser.prototype = Object.extend(new ResponseXmlToHtmlParser(), {
  initialize: function() {
    this.type = "xmltohtml";
  	this.plaintext = true;
  }
});



ResponseXmlToHtmlListParser.prototype = Object.extend(new DefaultResponseParser(), {
  initialize: function() {
    this.type = "xmltohtmllist";
    this.plaintext =  true;
  },
 

  prepareData: function( dataarray) {
    this.contentdiv = document.createElement("div");
    ul = document.createElement("ul");
    for (i=0; i < dataarray.length; i++)
    {
      liElement = document.createElement("li");
      liElement.id=dataarray[i][1];
      if (this.plaintext) {
        liElement.appendChild(document.createTextNode(dataarray[i][0]));
      } else {
        liElement.innerHTML = dataarray[i][0];
      }
      ul.appendChild(liElement);
    }
    this.contentdiv.appendChild(ul);
    this.content = this.contentdiv.innerHTML;
  }
});

ResponseXmlToHtmlLinkListParser.prototype = Object.extend(new AbstractResponseParser(), {
  initialize: function() {
    this.type = "xmltohtmllinklist";
  },

  load: function(request) {
    this.xml = request.responseXML;
    this.collapsedClass = request.collapsedClass;
    this.treeClass = request.treeClass;
    this.nodeClass = request.nodeClass;
    this.expandedNodes = [];
    this.parse();
  },

  parse: function() {
    var ul = document.createElement('ul');
    ul.className = this.treeClass;
    var root = this.xml.documentElement;

    var responseNodes = root.getElementsByTagName("response");
    if (responseNodes.length > 0) {
      responseNode = responseNodes[0];
      itemNodes = responseNode.getElementsByTagName("item");
      
      if (itemNodes.length === 0) {
      	ul = null;
      }
      for (i=0; i<itemNodes.length; i++) {
       	nameNodes = itemNodes[i].getElementsByTagName("name");
        valueNodes = itemNodes[i].getElementsByTagName("value");
        
        
        urlNodes = itemNodes[i].getElementsByTagName("url");
        collapsedNodes = itemNodes[i].getElementsByTagName("collapsed");
        
        leafnodes = itemNodes[i].getElementsByTagName("leaf");
        
        if (nameNodes.length > 0 && valueNodes.length > 0) {
          name = nameNodes[0].firstChild.nodeValue;
          value = valueNodes[0].firstChild.nodeValue;
          url = "#";
          try {
          	url = urlNodes[0].firstChild.nodeValue;
          } catch (ex) {
          // default url is link
          }
          leaf = false;
          try {
          	leaf = leafnodes[0].firstChild.nodeValue;
          } catch (ex) {
          // no leaf flag found 
          }
          
          collapsed =  false;
          try {
	         collapsed = parseBoolean(collapsedNodes[0].firstChild.nodeValue);
            } catch (ex) {
          // it is not collapsed as default 
          }
          
          li = document.createElement('li');
          li.id = "li_" + value;
          ul.appendChild(li);
          
          if (!parseBoolean(leaf))
          {
          	span = document.createElement('span');
          	li.appendChild(span);
          	// img geht im IE nicht
          	span.id = "span_" + value;
          	span.className = this.collapsedClass;
		  }

          link  = document.createElement('a');
          li.appendChild(link);
          link.href = url;
          link.className = this.nodeClass;
          link.appendChild(document.createTextNode(name));
          
          div = document.createElement('div');
          li.appendChild(div);
          div.id = value;
          div.setAttribute("style","");
          div.style.display ="none";
          
          if(!collapsed) {
            this.expandedNodes.push(value);
          }
        }  
      }
    }  
    this.content = ul;
  }
});
