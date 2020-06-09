/**
* Override jquery >= 3.3.1 functions because of wkhtml2pdf 12.5 issues
*
* load module after jquery
*/

/**
 * .html()
 *
 * $(target).html()
 * $(target).html(contentToWrite)
 *
 */

(function ($) {
  var originalHtml = $.fn.html;
  $.fn.html = function(par) {
    if (arguments.length >= 1) {
    	//write mode
        if( typeof par == 'object') {
          var content = par instanceof Node ? par : par[0];
          var target = this instanceof Node ? this : this[0];
          $(target).empty();
          target.appendChild(content);
          return this;
        }
        return originalHtml.call(this, par);
    }
    return originalHtml.call(this);
  };
})(jQuery);

/**
 * .append()
 *
 * $(target).append(contentToBeAppended)
 *
 */
(function ($) {
  var originalAppend = $.fn.append;
  $.fn.append = function(par) {
      if (arguments.length >= 1 && typeof par == 'object') {
        var target = this instanceof Node ? this : this[0];
        var content = par instanceof Node ? par : par[0];
        target.appendChild(content);
        return this;
      }
      return originalAppend.call(this, par);
  };
})(jQuery);

/**
 * .prepend()
 *
 * $(target).prepend(contentToBePrepended)
 *
 */
(function ($) {
  var originalPrepend = $.fn.prepend;
  $.fn.prepend = function(par) {
      if (arguments.length >= 1 && typeof par == 'object') {
        var target = this instanceof Node ? this : this[0];
        var content = par instanceof Node ? par : par[0];
        target.insertBefore(content, target.firstChild);
        return this;
      }
      return originalPrepend.call(this, par);
  };
})(jQuery);


/**
 * .appendTo()
 *
 * $(contentToBeAppended).appendTo(jqTarget)
 */
(function ($) {
    var originalAppendTo = $.fn.appendTo;
    $.fn.appendTo = function(par) {
      if (arguments.length >= 1 && typeof par == 'object') {
        var target = par instanceof Node ? par : par[0];
        var content = this instanceof Node ? this : this[0];
        target.appendChild(content);
        return this;
      }
      return originalAppendTo.call(this, par);
    };
})(jQuery);

/**
 * .insertAfter()
 *
 * $(contentToBeAppended).insertAfter(jqTarget)
 */
(function ($) {
    var originalInsertAfter = $.fn.insertAfter;
    $.fn.insertAfter = function(par) {
      if (arguments.length >= 1 && typeof par == 'object') {
        var target = par instanceof Node ? par : par[0];
        var content = this instanceof Node ? this : this[0];
        target.parentNode.insertBefore(content, target.nextSibling);
        return this;
      }
      return originalInsertAfter.call(this, par);
    };
})(jQuery);

/**
 * .before()
 *
 * $(target).before(contentToBeInserted)
 *
 */
(function ($) {
  var originalBefore = $.fn.before;
  $.fn.before = function(par) {
      if (arguments.length >= 1 && typeof par == 'object') {
          var target = this instanceof Node ? this : this[0];
          var content = par instanceof Node ? par : par[0];
          target.parentNode.insertBefore(content, target);
          return this;
      }
      return originalBefore.call(this, par);
  };
})(jQuery);

/**
 * .after()
 *
 * $(target).after(contentToBeInserted)
 */
(function ($) {
    var originalAfter = $.fn.after;
    $.fn.after = function(par) {
      if (arguments.length >= 1 && typeof par == 'object') {
        var target = this instanceof Node ? this : this[0];
        var content = par instanceof Node ? par : par[0];
        target.parentNode.insertBefore(content, target.nextSibling);
        return this;
      }
      return originalAfter.call(this, par);
    };
})(jQuery);

/**
 * .replaceWith()
 *
 * $(target).replaceWith(contentToBeInserted)
 */
(function ($) {
    var originalReplaceWith = $.fn.replaceWith;
    $.fn.replaceWith = function(par) {
       if (arguments.length >= 1) {
            //write mode
            if( typeof par == 'object') {
                $(this).empty();
                var content = this instanceof Node ? par : par[0];
                this[0].parentNode.replaceChild(content, this[0]);
                return this;
            }
            return originalReplaceWith.call(this,par);
        }
      return originalReplaceWith.call(this, par);
    };
})(jQuery);