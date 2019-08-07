// 
// Copyright (c) 2008 Beau D. Scott | http://www.beauscott.com
// 
// Permission is hereby granted, free of charge, to any person
// obtaining a copy of this software and associated documentation
// files (the "Software"), to deal in the Software without
// restriction, including without limitation the rights to use,
// copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following
// conditions:
// 
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
// OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
// HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.
// 

/**
 * HelpBalloon.js
 * Prototype/Scriptaculous based help balloons / dialog balloons
 * @version 2.0.1
 * @requires prototype.js <http://www.prototypejs.org/>
 * @author Beau D. Scott <beau_scott@hotmail.com>
 * 4/10/2008
 */
var HelpBalloon = Object.extend(Class.create(), {
	/**
	 * Enumerated value for dynamic rendering position.
	 * @static
	 */
	POS_DYNAMIC: -1,
	/**
	 * Enumerated value for the top-left rendering position.
	 * @static
	 */
	POS_TOP_LEFT: 0,
	/**
	 * Enumerated value for the top-right rendering position.
	 * @static
	 */
	POS_TOP_RIGHT: 1,
	/**
	 * Enumerated value for the bottom-left rendering position.
	 * @static
	 */
	POS_BOTTOM_LEFT: 2,
	/**
	 * Enumerated value for the bottom-right rendering position.
	 * @static
	 */
	POS_BOTTOM_RIGHT: 3,
	/**
	 * CSS Classname to look for when doing auto link associations.
	 * @static
	 */
	ELEMENT_CLASS_NAME: 'HelpBalloon',
	/**
	 * Global array of all HelpBalloon instances.
	 * (Cheaper than document.getElementByClassName with a property check)
	 * @static
	 * @private
	 */
	_balloons: [],
	/**
	 * Event listener that auto-associates anchors with a dynamic HelpBalloon.
	 * Also begins mouse movement registration
	 * @static
	 */
	registerClassLinks: function(e) {
		$A(document.getElementsByClassName(HelpBalloon.ELEMENT_CLASS_NAME))
			.each(function(obj){
			// Only apply any element with an href tag
			if(obj && obj.tagName && obj.href && obj.href != '')
			{
				new HelpBalloon({
					icon:obj,
					method: 'get'
				});
			}
		});
		
		Event.observe(document, 'mousemove', HelpBalloon._trackMousePosition);
		
	},
	
	/**
	 * Private cache of the client's mouseX position
	 */
	_mouseX: 0,
	
	/**
	 * Private cache of the client's mouseY position
	 */
	_mouseY: 0,
	
	/**
	 * @param {Event} e
	 */
	_trackMousePosition: function(e) {
		if(!e) e = window.event;
		HelpBalloon._mouseX = e.clientX;
		HelpBalloon._mouseY = e.clientY;
	}
});

//
// Event for activating HelpBalloon classed links
//
Event.observe(window, 'load', HelpBalloon.registerClassLinks);

HelpBalloon.prototype = {
	
//
// Properties
// 	
	/**
	 * Configuration options
	 * @var {HelpBalloon.Options}
	 */
	options: null,

	/**
	 * Containing element of the balloon
	 * @var {Element}
	 */
	container: null,
	/**
	 * Inner content container
	 * @var {Element}
	 */
	inner: null,
	/**
	 * A reference to the anchoring element/icon
	 * @var {Element}
	 */
	icon: null,
	/**
	 * Content container
	 * @var {Element}
	 */
	content: null,
	/**
	 * Closing button element
	 * @var {Element}
	 */
	button: null,
	/**
	 * The closer object. This can be the same as button, but could 
	 * also be a div with a png loaded as the back ground, browser dependent.
	 * @var {Element}
	 */
	closer: null,
	/**
	 * Title container
	 * @var {Element}
	 */
	titleContainer: null,
	/**
	 * Background container (houses the balloon images
	 * @var {Element}
	 */
	bgContainer: null,
	/**
	 * Array of balloon image references
	 * @var {Array}
	 */
	balloons: null,
	
	/**
	 * The local store of 'title'. Will change if the balloon is making a remote call
	 * unless options.title is specified
	 * @var {String}
	 */
	_titleString: null,
	
	/**
	 * The balloons visibility state.
	 * @var {Boolean}
	 */
	visible: false,
	
	/**
	 * Rendering status
	 * @var {Boolean}
	 */
	drawn: false,

	/**
	 * Stores the balloon coordinates
	 * @var {Object}
	 */
	balloonCoords: null,
		
	/**
	 * Width,height of the balloons
	 * @var {Array}
	 */
	balloonDimensions: null,
	
	/**
	 * ID for HelpBalloon
	 * @var {String}
	 */
	id: null,
	
	/**
	 * Used at render time to measure the dimensions of the loaded balloon
	 * @private
	 */
	_lastBalloon: null,

//
// Methods
//

	/**
	 * @param {Object} options
	 * @see HelpBalloon.Options
	 * @constructor
	 */
	initialize: function(options)
	{
		
		this.options = new HelpBalloon.Options();
		Object.extend(this.options, options || {});

		this._titleString = this.options.title;
		this.balloonDimensions = [0,0];
		
		//
		// Preload the balloon and button images so they're ready
		// at render time
		//
		// 0 1
		//  X
		// 2 3
		//
		this.balloons = [];
		for(var i = 0; i < 4; i++)
		{
			var balloon = new Element('img', {
				src: this.options.balloonPrefix + i + this.options.balloonSuffix
			});
			this.balloons.push(balloon.src);
		}
		
		this._lastBalloon = balloon;
		
		this.button = new Element('img', {
			src: this.options.button
		});
		
		//
		// Create the anchoring icon, or attach the balloon to the given icon element
		// If a string is passed in, assume it's a URL, if it's an object, assume it's
		// a DOM member.
		//
		if(typeof this.options.icon == 'string')
		{
			this.icon = new Element('img', {
				src: this.options.icon,
				id: this.id + "_icon"
			});
            this.icon.removeAttribute("width");
			Element.setStyle(this.icon, this.options.iconStyle);
		}
		else
		{
			//
			// Not a string given (most likely an object. Do not append the element
			// Kind of a hack for now, but I'll fix it in the next version.
			//
			this.icon = this.options.icon;
			this.options.returnElement = true;
		}
		
		this.icon._HelpBalloon = this;
			
		//
		// Attach rendering events
		//

		for(i = 0; i < this.options.useEvent.length; i++)
			Event.observe(this.icon, this.options.useEvent[i], this.toggle.bindAsEventListener(this));
		
		this.container = new Element('div');
		this.container._HelpBalloon = this;
		
		this.id = 'HelpBalloon_' + Element.identify(this.container);
		
		HelpBalloon._balloons.push(this);

		//
		// If we are not relying on other javascript to attach the anchoring icon
		// to the DOM, we'll just do where the script is called from. Default behavior.
		//
		// If you want to use external JavaScript to attach it to the DOM, attach this.icon
		//
		if(!this.options.returnElement)
		{
			document.write('<span id="' + this.id + '"></span>');
			var te = $(this.id);
			var p = te.parentNode;
			p.insertBefore(this.icon, te);
			p.removeChild(te);
		}
	},
	
	/**
	 * Toggles the help balloon
	 * @param {Object} e Event
	 */
	toggle: function(event)
	{
		if(!event) event = window.event || {type: this.options.useEvent, target: this.icon};
		var icon = Event.element(event);
		Event.stop(event);
		if(event.type == this.options.useEvent && !this.visible && icon == this.icon)
		{
			this.show(event);
		}
		else
			this.hide();
	},

	/**
	 * Triggers the balloon to appear
	 */
	show: function(event)
	{
		if(!this.visible){
			if(!event) event = window.event;
			if(!this.drawn || !this.options.cacheRemoteContent) this._draw();
			this._reposition(event);
			this._hideOtherHelps();
			if(this.options.showEffect)
			{
				this.options.showEffect(this.container, Object.extend(this.options.showEffectOptions, {
					afterFinish: this._afterShow.bindAsEventListener(this)
				}));
			}
			else
			{
				this._afterShow();
			}
			Event.observe(window, 'resize', this._reposition.bindAsEventListener(this));
		}
	},
	
	/**
	 * Sets the container to block styling and hides the elements below the
	 * container (if in IE)
	 * @private
	 */
	_afterShow: function()
	{
		Element.setStyle(this.container, {
			'display': 'block'
		});
		this._hideLowerElements();
		this.visible = true;
		if(this.options.autoHideTimeout)
		{
			setTimeout(this._hideQueue.bind(this), this.options.autoHideTimeout);
		}
	},
	
	/**
	 * Checks the mouse position and triggers a hide after the time specified in autoHideTimeout
	 * if the mouse is not currently over the balloon, otherwise it requeue's a hide for later.
	 */
	_hideQueue: function()
	{
		if(Position.within(this.container, HelpBalloon._mouseX, HelpBalloon._mouseY))
			setTimeout(this._hideQueue.bind(this), this.options.autoHideTimeout);
		else
			this.hide();
	},
	
	/**
	 * Hides the balloon
	 */
	hide: function()
	{
		if(this.visible)
		{
			this._showLowerElements();
			if(this.options.hideEffect)
			{
				this.options.hideEffect(this.container, Object.extend(this.options.hideEffectOptions, {
					afterFinish: this._afterHide.bindAsEventListener(this)
				}));
			}
			else
			{
				this._afterHide();
			}
			Event.stopObserving(window, 'resize', this._reposition.bindAsEventListener(this));
		}
	},
	
	/**
	 * Sets the container's display to block
	 * @private
	 */
	_afterHide: function()
	{
		Element.setStyle(this.container, {
			'display': 'none'
		});
		this.visible = false;
	},
	
	/**
	 * Redraws the balloon based on the current coordinates of the icon.
	 * @private
	 */
	_reposition: function(event)
	{
		if(this.icon.tagName.toLowerCase() == 'area' || !!this.icon.isMap)
		{
			this.balloonCoords = Event.pointer(event);
		}
		else
		{
			this.balloonCoords = this._getXY(this.icon);
			//Horizontal and vertical offsets in relation to the icon's 0,0 position.
			// Default is the middle of the object
			var ho = this.icon.offsetWidth / 2;
			var vo = this.icon.offsetHeight / 2;
			
			var offsets = this.options.anchorPosition.split(/\s+/gi);
			// Only use the first two specified values
			if(offsets.length > 2)
				offsets.length = 2;
			
			for(var i = 0; i < offsets.length; i++)
			{
				switch(offsets[i].toLowerCase())
				{
					case 'left':
							ho = 0;
						break;
					case 'right':
							ho = this.icon.offsetWidth;
						break;
					case 'center':
							ho = this.icon.offsetWidth / 2;
						break;
                
					case 'top':
							vo = 0;
						break;
					case 'middle':
							vo = this.icon.offsetHeight / 2;
						break;
					case 'bottom':
							vo = this.icon.offsetHeight;
						break;
					default:
						var numVal = parseInt(offsets[i]); 
						if(!isNaN(numVal))
						{
							// 0 = width, 1 = height (WxH)
							if(i == 0)
							{
								if(numVal < 0)
								{
									ho = 0;
								}
								else
								{
									if(numVal > this.icon.offsetWidth)
										ho = this.icon.offsetWidth;
									else
										ho = numVal
								}
							}
							else
							{
								if(numVal < 0)
								{
									vo = 0;
								}
								else
								{
									if(numVal > this.icon.offsetHeight)
										vo = this.icon.offsetHeight;
									else
										vo = numVal
								}
							}
						}
						break;	
				}
			}
			this.balloonCoords.x += ho;
			this.balloonCoords.y += vo;
		}
		
		//
		// Figure out what position to show based on available realestate
		// unless 
		// 0 1
		//  X
		// 2 3
		// Number indicates position of corner opposite anchor
		//
		var pos = 1;
		if(this.options.fixedPosition == HelpBalloon.POS_DYNAMIC)
		{
			var offsetHeight = this.balloonCoords.y - this.balloonDimensions[1];
			if(offsetHeight < 0)
				pos += 2;
	
			var offsetWidth = this.balloonCoords.x + this.balloonDimensions[0];
			var ww = Prototype.Browser.IE ? document.body.clientWidth : window.outerWidth;
			if(offsetWidth > ww)
				pos -- ;
		}
		else
			pos = this.options.fixedPosition;

		var zx = 0;
		var zy = 0;
		
		//
		// 0 1
		//  X
		// 2 3
		//
		switch(pos)
		{
			case 0:
				zx = this.balloonCoords.x - this.balloonDimensions[0];
				zy = this.balloonCoords.y - this.balloonDimensions[1];
				break;
			
			case 1:
				zx = this.balloonCoords.x;
				zy = this.balloonCoords.y - this.balloonDimensions[1];
				break;
			
			case 2:
				zx = this.balloonCoords.x - this.balloonDimensions[0];
				zy = this.balloonCoords.y;
				break;
			
			case 3:
				zx = this.balloonCoords.x;
				zy = this.balloonCoords.y;
				break;
		}
		var containerStyle = {
			/*'backgroundRepeat': 'no-repeat',
			'backgroundColor': 'transparent',
			'backgroundPosition': 'top left',*/
			'left' 	: zx + "px",
			'top'	: zy + "px",
			'width' : this.balloonDimensions[0] + 'px',
			'height' : this.balloonDimensions[1] + 'px'
		}
		if(Prototype.Browser.IE)
		{
			//
			// Fix for IE alpha transparencies
			//
			if(this.balloons[pos].toLowerCase().indexOf('.png') > -1)
			{
				Element.setStyle(this.bgContainer, {
					'left' 		: '0px',
					'top'		: '0px',	
					'filter'	: "progid:DXImageTransform.Microsoft.AlphaImageLoader(src='" + this.balloons[pos] + "', sizingMethod='scale')",
					'width' 	: this.balloonDimensions[0] + 'px',
					'height' 	: this.balloonDimensions[1] + 'px',
					'position'	: 'absolute'
				});
			}
			else
				containerStyle['background'] = 'transparent url(' + this.balloons[pos] + ') top left no-repeat';
		}
		else
		{
				containerStyle['background'] = 'transparent url(' + this.balloons[pos] + ') top left no-repeat';
		}
		Element.setStyle(this.container, containerStyle);
	},

	/**
	 * Renders the Balloon
	 * @private
	 */
	_draw: function()
	{
		Element.setStyle(
			this.container, 
			Object.extend(this.options.balloonStyle, {
				'position': 	'absolute',
				'display': 		'none'
			})
		);
		
		var url = this.options.dataURL;
		
		//
		// Play nicely with anchor tags being used as the icon. Use it's specified href as our
		// data URL unless one has already been used specified in this.options.dataURL. 
		// We'll also force a new request with this as it may be an image map.
		//
		if(this.icon.className == 'a')
		{
			if(!this.options.dataURL && this.icon.href != ''){
				url = this.icon.href;
				this.options.cacheRemoteContent = false;
			}
		}
		
		if(url && (!this.drawn || !this.options.cacheRemoteContent))
		{
			var cont = new Ajax.Request(this.options.dataURL, {asynchronous: false, method: this.options.method});
			//
			// Expects the following XML format:
			// <HelpBalloon>
			// 		<title>My Title</title>
			// 		<content>My content</content>
			// </HelpBaloon>
			//
			var doHTML = false;
			if(cont.transport.responseXML)
			{
				var xml = cont.transport.responseXML.getElementsByTagName('HelpBalloon')[0];

				if(xml)
				{
					if(!this.options.title)
					{
						xmlTitle = xml.getElementsByTagName('title')[0];
						if(xmlTitle) this._titleString = xmlTitle.firstChild.nodeValue;
					}

					xmlContent = xml.getElementsByTagName('content')[0];
					if(xmlContent) this.options.content = xmlContent.firstChild.nodeValue;
				}
				else
					doHTML = true;
			}
			else
				doHTML = true;

			if(doHTML)
			{
				// Attempt to get the title from a <title/> HTML tag, unless the title option has been set. If so, use that.
				if(!this.options.title)
				{
					var htmlTitle = cont.transport.responseText.match(/\<title\>([^\<]+)\<\/title\>/gi);
					if(htmlTitle)
					{
						htmlTitle = htmlTitle.toString().replace(/\<title\>|\<\/title\>/gi, '');
						this._titleString = htmlTitle;
					}
				}
				this.options.content = cont.transport.responseText;
			}
		}
		
		this.balloonDimensions[0] = this._lastBalloon.width;
		this.balloonDimensions[1] = this._lastBalloon.height;
		
		var contentDimensions = [
			this.balloonDimensions[0] - (2 * this.options.contentMargin),
			this.balloonDimensions[1] - (2 * this.options.contentMargin)
		];
		
		var buttonDimensions = [
			this.button.width,
			this.button.height
		];

		//
		// Create all the elements on demand if they haven't been created yet
		//
		if(!this.drawn)
		{
			this.inner = new Element('div');
		
			this.titleContainer = new Element('div');
			this.inner.appendChild(this.titleContainer);
			
			// PNG fix for IE
			if(Prototype.Browser.IE && this.options.button.toLowerCase().indexOf('.png') > -1)
			{
				this.bgContainer = new Element('div');
				
				// Have to create yet-another-child of container to house the background for IE... when it was set in
				// the main container, it for some odd reason prevents child components from being clickable.
				this.container.appendChild(this.bgContainer);
				
				this.closer =  new Element('div');
				Element.setStyle(this.closer, {
					'filter':
						"progid:DXImageTransform.Microsoft.AlphaImageLoader(src='" + this.options.button + "', sizingMethod='scale')"
				});
			}
			else
			{
				this.closer = this.button;
			}
			
			Event.observe(this.closer, 'click', this.toggle.bindAsEventListener(this));
			this.inner.appendChild(this.closer);
			
			this.content =  new Element('div');
			this.inner.appendChild(this.content);
			
			this.container.appendChild(this.inner);


            this.container.style.zIndex=1050;
			
			document.getElementsByTagName('body')[0].appendChild(this.container);
			
			this.drawn = true;
		}

		// Reset the title element and reappend the title value (could have changed with a new URL)
		this.titleContainer.innerHTML = '';
		this.titleContainer.appendChild(document.createTextNode(this._titleString));
		
		// Reset content value:
		this.content.innerHTML = this.options.content;

		//
		// Reapply styling to components as values might have changed
		//
		
		Element.setStyle(this.inner, {
			'position': 	'absolute',
			'top':			this.options.contentMargin + 'px',
			'left':			this.options.contentMargin + 'px',
			'width': 		contentDimensions[0] + 'px',
			'height': 		contentDimensions[1] + 'px'
		});

		Element.setStyle(this.titleContainer, {
			'width':		(contentDimensions[0] - buttonDimensions[0]) + 'px',
			'height':		buttonDimensions[1] + 'px',
			'position':		'absolute',
			'overflow':		'hidden',
			'top': 			'0px',
			'left': 		'0px'
		});
		
		Element.setStyle(this.titleContainer, this.options.titleStyle);
		
		Element.setStyle(this.closer, {
			'width': buttonDimensions[0] + 'px',
			'height': buttonDimensions[1] + 'px',
			'cursor': 	'pointer',
			'position':	'absolute',
			'top': 		'0px',
			'right': 	'0px'
		});
		
		Element.setStyle(this.content, {
			'width':		contentDimensions[0] + 'px',
			'height': 		(contentDimensions[1] - this.button.height) + 'px',
			'overflow': 	'auto',
			'position': 	'absolute',
			'top': 			buttonDimensions[1] + 'px',
			'left': 		'0px',
			'fontFamily': 	'verdana',
			'fontSize': 	'11px',
			'fontWeight': 	'normal',
			'color': 		'black'
		});
		
	},

	/**
	 * Gets the current position of the obj
	 * @param {Element} element to get position of
	 * @return Object of (x, y, x2, y2)
	 */
	_getXY: function(obj)
	{
		var pos = Position.cumulativeOffset(obj)
		var y = pos[1];
		var x = pos[0];
		var x2 = x + parseInt(obj.offsetWidth);
		var y2 = y + parseInt(obj.offsetHeight);
		return {'x':x, 'y':y, 'x2':x2, 'y2':y2};

	},

	/**
	 * Determins if the object is a child of the balloon element
	 * @param {Element} Element to check parentage
	 * @return {Boolean}
	 * @private
	 */
	_isChild: function(obj)
	{
		var i = 15;
		do{
			if(obj == this.container)
				return true;
			obj = obj.parentNode;
		}while(obj && i--);
		return false
	},

	/**
	 * Determines if the balloon is over this_obj object
	 * @param {Element} Object to look under
	 * @return {Boolean}
	 * @private
	 */
	_isOver: function(this_obj)
	{
		if(!this.visible) return false;
		if(this_obj == this.container || this._isChild(this_obj)) return false;
		var this_coords = this._getXY(this_obj);
		var that_coords = this._getXY(this.container);
		if(
			(
			 (
			  (this_coords.x >= that_coords.x && this_coords.x <= that_coords.x2)
			   ||
			  (this_coords.x2 >= that_coords.x &&  this_coords.x2 <= that_coords.x2)
			 )
			 &&
			 (
			  (this_coords.y >= that_coords.y && this_coords.y <= that_coords.y2)
			   ||
			  (this_coords.y2 >= that_coords.y && this_coords.y2 <= that_coords.y2)
			 )
			)

		  ){
			return true;
		}
		else
			return false;
	},

	/**
	 * Restores visibility of elements under the balloon
	 * (For IE)
	 * TODO: suck yourself
	 * @private
	 */
	_showLowerElements: function()
	{
		if(this.options.hideUnderElementsInIE)
		{
			var elements = this._getWeirdAPIElements();
			for(var i = 0; i < elements.length; i++)
			{
				if(this._isOver(elements[i]))
				{
					if(elements[i].style.visibility != 'visible' && elements[i].hiddenBy == this)
					{
						elements[i].style.visibility = 'visible';
						elements[i].hiddenBy = null;
					}
				}
			}
		}
	},

	/**
	 * Hides elements below the balloon
	 * (For IE)
	 * @private
	 */
	_hideLowerElements: function()
	{
		if(this.options.hideUnderElementsInIE)
		{
			var elements = this._getWeirdAPIElements();
			for(var i = 0; i < elements.length; i++)
			{
				if(this._isOver(elements[i]))
				{
					if(elements[i].style.visibility != 'hidden')
					{
						elements[i].style.visibility = 'hidden';
						elements[i].hiddenBy = this;
					}
				}
			}
		}
	},

	/**
	 * Determines which elements need to be hidden
	 * (For IE)
	 * @return {Array} array of elements
	 */
	_getWeirdAPIElements: function()
	{
		if(!Prototype.Browser.IE) return [];
		var objs = ['select', 'input', 'object'];
		var elements = [];
		for(var i = 0; i < objs.length; i++)
		{
			var e = document.getElementsByTagName(objs[i]);
			for(var j = 0; j < e.length; j++)
			{
				elements.push(e[j]);
			}
		}
		return elements;
	},

	/**
	 * Hides the other visible help balloons
	 * @param {Event} e
	 */
	_hideOtherHelps: function(e)
	{
		if(this.options.hideOtherBalloonsOnDisplay)
		{
			$A(HelpBalloon._balloons).each(function(obj){
				if(obj != this)
				{
					obj.hide();
				}
			}.bind(this));
		}
	}
};

/**
 * HelpBalloon.Options
 * Helper class for defining options for the HelpBalloon object
 * @author Beau D. Scott <beau_scott@hotmail.com>
 */
HelpBalloon.Options = Class.create();
HelpBalloon.Options.prototype = {
	
	/**
	 * @constructor
	 * @param {Object} overriding options
	 */
	initialize: function(values){
		// Apply the overriding values to this
		Object.extend(this, values || {});
	},
	
	/**
	 * Show Effect
	 * The Scriptaculous (or compatible) showing effect function
	 * @var Function
	 */
	showEffect: window.Scriptaculous ? Effect.Appear : null,
	
	/**
	 * Show Effect options
	 */
	showEffectOptions: {duration: 0.2},
	
	/**
	 * Hide Effect
	 * The Scriptaculous (or compatible) hiding effect function
	 * @var Function
	 */
	hideEffect: window.Scriptaculous ? Effect.Fade : null,
	
	/**
	 * Show Effect options
	 */
	hideEffectOptions: {duration: 0.2},
	
	/**
	 * For use with embedding this object into another. If true, the icon is not created
	 * and not appeneded to the DOM at construction.
	 * Default is false
	 * @var {Boolean}
	 */
	returnElement: false,
	
	/**
	 * URL to the anchoring icon image file to use. This can also be a direct reference 
	 * to an existing element if you're using that as your anchoring icon.
	 * @var {Object}
	 */
	icon: 'assets/core/images/info_icon.png',
	
	/**
	 * Alt text of the help icon
	 * @var {String}
	 */
	altText: 'Click here for help with this topic.',
	
	/**
	 * URL to pull the title/content XML
	 * @var {String}
	 */
	dataURL: null,
	
	/**
	 * Static title of the balloon
	 * @var {String}
	 */
	title: null,
	
	/**
	 * Static content of the balloon
	 * @var {String}
	 */
	content: null,
	
	/**
	 * The event type to listen for on the icon to show the balloon.
	 * Default 'click'
	 * @var {String}
	 */
	useEvent: ['click'],
	
	/**
	 * Request method for dynamic content. (get, post)
	 * Default 'get'
	 * @var {String}
	 */
	method:	'get',
	
	/**
	 * Flag indicating cache the request result. If this is false, every
	 * time the balloon is shown, it will retrieve the remote url and parse it
	 * before the balloon appears, updating the content. Otherwise, it will make
	 * the call once and use the same content with each subsequent showing.
	 * Default true
	 * @var {Boolean}
	 */
	cacheRemoteContent: true,
	
	/**
	 * Vertical and horizontal margin of the content pane
	 * @var {Number}
	 */
	contentMargin: 35,
	
	/**
	 * X coordinate of the closing button
	 * @var {Number}
	 */
	buttonX: 246,
	
	/**
	 * Y coordinate of the closing button
	 * @var {Number}
	 */
	buttonY: 35,
	
	/**
	 * Closing button image path
	 * @var {String}
	 */
	button: 'images/button.png',
	
	/**
	 * Balloon image path prefix. There are 4 button images, numerically named, starting with 0.
	 * 0 1
	 *  X
	 * 2 3
	 * X indicates the anchor corner
	 * @var {String}
	 */
	balloonPrefix: 'images/balloon-',
	
	/**
	 * The image filename suffix, including the file extension
	 * @var {String}
	 */
	balloonSuffix: '.png',
	
	/**
	 * Position of the balloon's anchor relative to the icon element.
	 * Combine one horizontal indicator (left, center, right) and one vertical indicator (top, middle, bottom).
	 * Numeric values can also be used in an X Y order. So a value of 9 13 would place the anchor 9 pixels from
	 * the left and 13 pixels below the top. (0,0 is top left). If values are greater than the width or height
	 * the width or height of the anchor are used instead. If less than 0, 0 is used.
	 * Default is 'center middle'
	 * @var {String}
	 */
	anchorPosition: 'center middle',
	
	/**
	 * Flag indicating whether to hide the elements under the balloon in IE.
	 * Setting this to false can cause rendering issues in Internet Explorer
	 * as some elements appear on top of the balloon if they're not hidden.
	 * Default is true.
	 * @var {Boolean}
	 */
	hideUnderElementsInIE: true,
	
	/**
	 * Default Balloon styling
	 * @var {Object}
	 */	
	balloonStyle: {},
	
	/**
	 * Default Title Bar style
	 * @var {Object}
	 */
	titleStyle: {
		'color': 'black',
		'fontSize': '16px',
		'fontWeight': 'bold',
		'fontFamily': 'Verdana'
	},
	
	/**
	 * Icon custom styling
	 * @var {Object}
	 */
	iconStyle: {
		'cursor': 'pointer'
	},
	
	/**
	 * Flag indication whether to automatically hide any other visible HelpBalloon on the page before showing the current one.
	 * @var {Boolean}
	 */
	hideOtherBalloonsOnDisplay: true,
	
	/**
	 * If you want the balloon to always display in a particular location, set this 
	 */
	fixedPosition: HelpBalloon.POS_DYNAMIC,
	
	/**
	 * Number of milliseconds to hide the balloon after showing and after the mouse is not over the balloon.
	 * A value of 0 means it will not auto-hide
	 * @var {Number}
	 */
	autoHideTimeout: 0
	
};