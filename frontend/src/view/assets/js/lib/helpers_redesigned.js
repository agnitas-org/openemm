/*doc
---
title: Helpers
name: js-helpers
category: Javascripts - Helpers
---

`AGN.Lib.Helpers` provides helper functions
*/

/*doc
---
title: paramsFromUrl
name: js-helpers-01
parent: js-helpers
---

`AGN.Lib.Helpers.paramsFromUrl(url)` extracts parameters from the url and returns an object

```
var url = 'http://localhost:8080/css/mailing/list.action?forTemplates=false&page=1';
AGN.Lib.Helpers.paramsFromUrl(url) 
-> Object {action: "1", isTemplate: "false", page: "1"}
```
*/

/*doc
---
title: objFromString
name: js-helpers-02
parent: js-helpers
---

`AGN.Lib.Helpers.objFromString(string)` parses a string and returns an object

```
var string = "number: 9, float: 2.2, boolean: true, string: someString, forcedString: 'true'";
AGN.Lib.Helpers.objFromString(string) 
-> Object {number: 9, float: 2.2, boolean: true, string: "someString", forcedString: "true"}
```
*/

/*doc
---
title: Animations
name: js-helpers-03
parent: js-helpers
---

`AGN.Lib.Helpers.disableCSSAnimations()` and `AGN.Lib.Helpers.enableCSSAnimations()` allow to globally toggle css animations.
*/

/*doc
---
title: formatBytes
name: js-helpers-04
parent: js-helpers
---
`AGN.Lib.Helpers.formatBytes(bytes, unit ) format a bytes to number with prefix by chosen metrics system
by default using SI
* */

/*doc
---
title: isMobileView
name: js-helpers-05
parent: js-helpers
---

`AGN.Lib.Helpers.isMobileView()` check if the current screen resolution matches the mobile version.
*/

/*doc
---
title: openHelpModal
name: js-helpers-06
parent: js-helpers
---

`AGN.Lib.Helpers.openHelpModal(tab, options)` allows to open help modal with specified tab.
If you want to open manual with specified helpKey, you can use it like this `AGN.Lib.Helpers.openHelpModal('manual', {key: '<helpKey>'})`
*/

(() => {
  let mobileViewThreshold = 0;

  AGN.Lib.Helpers = {

    paramsFromUrl: function (url) {
      var result = {};
      var searchIndex;
      if (url) {
        searchIndex = url.indexOf("?");
      } else {
        return result
      }
      if (searchIndex == -1) return result;
      var sPageURL = url.substring(searchIndex + 1);
      var sURLVariables = sPageURL.split('&');
      for (var i = 0; i < sURLVariables.length; i++) {
        var sParameterName = sURLVariables[i].split('=');
        result[sParameterName[0]] = sParameterName[1];
      }
      return result;
    },

    objFromString: function (optsAsString) {
      var optsArray,
        optsSplitRegex = /,\s*(?=(?:[^'\\]*(?:\\.|'(?:[^'\\]*\\.)*[^'\\]*'))*[^']*$)/m,
        tupleSplitRegex = /:\s*(?=(?:[^'\\]*(?:\\.|'(?:[^'\\]*\\.)*[^'\\]*'))*[^']*$)/m,
        floatRegex = /^\s*-?(\d*\.?\d+|\d+\.?\d*)(e[-+]?\d+)?\s*$/i,
        arrayRegex = /^\s?\[.*?\]$/i
      opts = {};

      if (optsAsString === "" || optsAsString === undefined) {
        return opts;
      }

      optsArray = optsAsString.split(optsSplitRegex);
      _.forEach(optsArray, function (opt) {
        var tup = opt.split(tupleSplitRegex),
          name, value;

        tup = _.filter(tup);

        name = tup[0].replace(/^'([^]*?)'$/m, "$1");
        name = name.replace(/\\/, "");
        value = tup[1];

        if (typeof (value) === "undefined") {
          value = '';
        }

        if (value == "true") {
          value = true;
        } else if (value == "false") {
          value = false;
        } else if (floatRegex.test(value)) {
          value = parseFloat(value);
        } else if (arrayRegex.test(value)) {

          value = value.replace(/\[|\]/g, "")
          value = value.split(/\s+/)

          value = _.map(value, function (v) {

            if (/^\'(.*?)\'$/.test(v)) {
              return v.replace(/^\'(.*?)\'$/g, "$1");
            } else {
              return eval(v);
            }
          });

        } else {
          value = value.replace(/^'(.*?)'$/m, "$1");

          value = value.replace(/\\/, "");
        }

        opts[name] = value;
      });

      return opts;

    },

    disableCSSAnimations: function () {
      var script = $('<style class="disable-animation">* { -webkit-transition: none !important; -o-transition: none !important; transition: none !important; }</style>');
      $('body').append(script);
    },

    enableCSSAnimations: function () {
      window.setTimeout(function () {
        $('.disable-animation').remove()
      }, 10);
    },

    // Intended for inputs having type="file" but also works for other input types
    clearFormField: function ($element) {
      var $form = $('<form></form>');
      var $div = $('<div></div>');

      $div.insertAfter($element);

      $form.append($element);
      $form.get(0).reset();

      $element.insertBefore($div);
      $div.remove();
    },

    renameFile: function (file, name) {
      Object.defineProperty(file, 'name', {
        writable: true,
        value: name
      });
    },

    formatBytes: function (bytes, units) {
      // Handle some special cases
      if (bytes == 0) return '0 Bytes';
      if (bytes == 1) return '1 Byte';
      if (bytes == -1) return '-1 Byte';

      var bytes = Math.abs(bytes);

      if (units && units.toLowerCase() && units.toLowerCase() == 'iec') {
        // IEC units use 2^10 as an order of magnitude
        var orderOfMagnitude = Math.pow(2, 10);
        var abbreviations = ['Bytes', 'KiB', 'MiB', 'GiB', 'TiB', 'PiB', 'EiB', 'ZiB', 'YiB'];
      } else {
        // SI units use the Metric representation based on 10^3 as a order of magnitude
        var orderOfMagnitude = Math.pow(10, 3);
        var abbreviations = ['Bytes', 'kB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
      }

      var i = Math.floor(Math.log(bytes) / Math.log(orderOfMagnitude));
      var result = (bytes / Math.pow(orderOfMagnitude, i));

      // This will get the sign right
      if (bytes < 0) {
        result *= -1;
      }

      // This bit here is purely for show. it drops the percision on numbers greater than 100 before the units.
      // it also always shows the full number of bytes if bytes is the unit.
      if (result >= 99.995 || i == 0) {
        return result.toFixed(0) + ' ' + abbreviations[i];
      } else {
        return result.toFixed(2) + ' ' + abbreviations[i];
      }
    },

    pad: function (value, count, char) {
      char = char || '0';

      var s = value + '';
      if (s.length < count) {
        return new Array(count - s.length + 1).join(char) + s;
      } else {
        return s;
      }
    },

    caching: function (func, thisArg) {
      var calculated = false;
      var value;

      if (arguments.length > 1) {
        func = _.bind.apply(null, Array.prototype.slice.call(arguments));
      }

      return function () {
        if (!calculated) {
          calculated = true;
          value = func();
        }
        return value;
      };
    },

    replaceAgnTags: function (html, replacementFn) {
      try {
        return html.replace(
          /\[(agn[A-Z0-9_]+)(?:[^'"\]]|'.*?'|".*?")+]/g,
          function (whole, tagName) {
            var tag = $('<' + whole.substring(1, whole.length - 1) + '>')[0];

            if (tag instanceof HTMLElement) {
              var tagAttributes = tag.attributes;
              var attributes = {};

              for (var i = 0; i < tagAttributes.length; i++) {
                var tagAttribute = tagAttributes[i];
                attributes[tagAttribute.nodeName] = tagAttribute.nodeValue;
              }

              return replacementFn({name: tagName, attributes: attributes}, whole);
            }

            return whole;
          }
        );
      } catch (e) {
        // Do nothing.
      }

      return html;
    },

    escapeAgnTags: function (html) {
      try {
        if (html) {
          return html.replace(
            /\[agn[A-Z0-9]+(?:[^'"\]]|'.*?'|".*?")+]/g,
            function (whole) {
              // If no attributes defined then there's nothing to escape.
              // If no quotation marks found it's probably already escaped.
              if (whole.includes('\'') || whole.includes('"')) {
                return _.escape(whole);
              } else {
                return whole;
              }
            }
          );
        }
      } catch (e) {
        // Do nothing.
      }

      return html;
    },

    unescapeAgnTags: function (html) {
      try {
        if (html) {
          return html.replace(
            // Make sure it's required to unescape.
            /\[agn[A-Z0-9]+(?:(?!&#39;|&quot;)[^'"\]]|=\s*&#39;.*?&#39;|=\s*&quot;.*?&quot;)*]/g,
            function (whole) {
              return _.unescape(whole);
            }
          );
        }
      } catch (e) {
        // Do nothing.
      }

      return html;
    },

    isDarkTheme: function ($body = $('body')) {
      const theme = $body.data('bs-theme');
      return theme === 'dark' || theme === 'dark-contrast';
    },

    isValidEmails: function (emails) {
      return emails.every(email => this.isValidEmail(email));
    },

    isValidEmail: function (email) {
      const re = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
      return re.test(String(email).toLowerCase());
    },

    isUrl: function (url) {
      // starts with HTTP/HTTPS
      const re = /^https?:\/\/(www\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_+.~#?&/=!$]*)$/;
      return re.test(url);
    },

    isNumber: val => {
      return typeof val === 'number';
    },

    deepFreeze: function (obj) {
      Object.freeze(obj);

      const propNames = Object.getOwnPropertyNames(obj);
      for (const propName of propNames) {
        const prop = obj[propName];
        if (typeof prop === 'object' && prop !== null && !Object.isFrozen(prop)) {
          this.deepFreeze(prop);
        }
      }
      return obj;
    },

    isMobileView: function () {
      if (mobileViewThreshold === 0) {
        mobileViewThreshold = parseInt($('html').css('--mobile-view-threshold'));
      }
      return $(window).width() <= mobileViewThreshold;
    },

    openHelpModal: function (tab = '', options = {}) {
      const helpKey = tab === 'manual'
        ? options.key
        : $('[data-help-key]').data('help-key');

      $.get(AGN.url(`/support/help-center.action?helpKey=${helpKey}`)).done(resp => {
        AGN.Lib.Modal.create(resp)
        if (tab) {
          $(`[data-help-tab="${tab}"]`).trigger('help-tab:open', options)
        }
      });
    },
    getColorLuminance: function (hex) {
      if (hex === undefined || hex === null) {
        return hex;
      }

      hex = hex.replace('#', '');

      const r = parseInt(hex.substring(0, 2), 16);
      const g = parseInt(hex.substring(2, 4), 16);
      const b = parseInt(hex.substring(4, 6), 16);

      return (0.299 * r + 0.587 * g + 0.114 * b) / 255;
    },

    isFilledTextNode: function (node) {
      return node.nodeType === 3  // text node
        && node.nodeValue?.trim()?.length > 0;
    },

    containsTruncatedElements: function (node) {
      if ($(node).is('select, .select2')) {
        return false;
      }
      if ($(node.parentElement).is(':truncated')) {
        return true;
      }
      if (node.childNodes && node.childNodes.length > 0) {
        for (let i = 0; i < node.childNodes.length; i++) {
          const result = this.containsTruncatedElements(node.childNodes[i]);
          if (result) {
            return result;
          }
        }
      }
      return false;
    },

    retrieveTextElement: function (node) {
      if (this.isFilledTextNode(node)) {
        return node.parentElement;
      }
      if (node.childNodes && node.childNodes.length > 0) {
        for (let i = 0; i < node.childNodes.length; i++) {
          const result = this.retrieveTextElement(node.childNodes[i]);
          if (result) {
            return result; // Return the first element found
          }
        }
      }
      return null;
    },

    getStatColors: function() {
      const pairs = getComputedStyle(document.documentElement)
        .getPropertyValue('--stat-colors')
        .trim()
        .split(', ');
      return _.fromPairs(pairs.map(pair => pair.split(' ')));
    },

    getStatColorsNames: function () {
      return Object.keys(this.getStatColors());
    },

    getStatColorsHexValues: function () {
      return Object.values(this.getStatColors());
    },

    imagesLoaded: function ($images) {
      const deferred = $.Deferred();
      const images = $images.filter(function() {
        return this.complete ? null : this;
      });

      const loadedImages = [];

      const checkLoadingComplete = function () {
        let complete = true;
        for (let i = 0; i < images.length; i++) {
          const image = images[i];
          if (image) {
            if (image.complete) {
              $(image).off('load error');
              images[i] = null;
              loadedImages.push(image);
            } else {
              complete = false;
            }
          }
        }
        return complete;
      };

      if (!images.length) {
        deferred.resolve(loadedImages);
      } else {
        for (let i = 0; i < images.length; i++) {
          $(images[i]).on('load error', function() {
            if (checkLoadingComplete()) {
              deferred.resolve(loadedImages);
            }
          });
        }
      }

      return deferred;
    }
  };

    /*
    // Run these tests manually in browser's console in order to verify your changes to escapeAgnTags/unescapeAgnTags.
    try {
        function testEscapeAgnTags(expectedResult, html) {
            var actualResult = AGN.Lib.Helpers.escapeAgnTags(html);
            if (expectedResult != actualResult) {
                console.error("Test failed!\nExpected: " + expectedResult + "\nActual: " + actualResult);
            }
        }

        function testUnescapeAgnTags(expectedResult, html) {
            var actualResult = AGN.Lib.Helpers.unescapeAgnTags(html);
            if (expectedResult != actualResult) {
                console.error("Test failed!\nExpected: " + expectedResult + "\nActual: " + actualResult);
            }
        }

        // Nothing to escape.
        testEscapeAgnTags(
            '<span>[agnCUSTOMERID]</span>',
            '<span>[agnCUSTOMERID]</span>'
        );

        // Normal escape.
        testEscapeAgnTags(
            '<span>[agnDB column=&quot;creation_date&quot;]</span>',
            '<span>[agnDB column="creation_date"]</span>'
        );

        // Multiple attributes escape.
        testEscapeAgnTags(
            '<span>[agnDB column=&quot;creation_date&quot; foo=&quot;bar&quot;]</span>',
            '<span>[agnDB column="creation_date" foo="bar"]</span>'
        );

        // Multiple tags escape.
        testEscapeAgnTags(
            '<span>[agnDB column=&quot;creation_date&quot;]</span><span>[agnDB column=&quot;foo&quot;]</span>',
            '<span>[agnDB column="creation_date"]</span><span>[agnDB column="foo"]</span>'
        );

        // Prevent double escape.
        testEscapeAgnTags(
            '<span>[agnDB column=&quot;creation_date&quot;]</span>',
            '<span>[agnDB column=&quot;creation_date&quot;]</span>'
        );

        // Prevent invalid agn-tag escape.
        testEscapeAgnTags(
            '<span>[ agnDB column="creation_date"]</span>',
            '<span>[ agnDB column="creation_date"]</span>'
        );

        // Normal escape inside attribute.
        testEscapeAgnTags(
            '<span title="[agnDB column=&quot;creation_date&quot;]">Creation date</span>',
            '<span title="[agnDB column="creation_date"]">Creation date</span>'
        );

        // Prevent double escape inside attribute.
        testEscapeAgnTags(
            '<span title="[agnDB column=&quot;creation_date&quot;]">Creation date</span>',
            '<span title="[agnDB column=&quot;creation_date&quot;]">Creation date</span>'
        );

        // Nothing to unescape.
        testUnescapeAgnTags(
            '<span>[agnCUSTOMERID]</span>',
            '<span>[agnCUSTOMERID]</span>'
        );

        // Normal unescape.
        testUnescapeAgnTags(
            '<span>[agnDB column="creation_date"]</span>',
            '<span>[agnDB column=&quot;creation_date&quot;]</span>'
        );

        // Multiple attributes unescape.
        testUnescapeAgnTags(
            '<span>[agnDB column="creation_date" foo="bar"]</span>',
            '<span>[agnDB column=&quot;creation_date&quot; foo=&quot;bar&quot;]</span>'
        );

        // Multiple tags unescape.
        testUnescapeAgnTags(
            '<span>[agnDB column="creation_date"]</span><span>[agnDB column="foo"]</span>',
            '<span>[agnDB column=&quot;creation_date&quot;]</span><span>[agnDB column=&quot;foo&quot;]</span>'
        );

        // Prevent unescaping of what's not escaped.
        testUnescapeAgnTags(
            '<span>[agnFOO foo="aaa&quot;bbb" bar="xxx&quot;yyy"]</span>',
            '<span>[agnFOO foo="aaa&quot;bbb" bar="xxx&quot;yyy"]</span>'
        );

        // Prevent invalid agn-tag unescape.
        testUnescapeAgnTags(
            '<span>[ agnDB column=&quot;creation_date&quot;]</span>',
            '<span>[ agnDB column=&quot;creation_date&quot;]</span>'
        );

        // Normal unescape inside attribute.
        testUnescapeAgnTags(
            '<span title="[agnDB column="creation_date"]">Creation date</span>',
            '<span title="[agnDB column=&quot;creation_date&quot;]">Creation date</span>'
        );

        // Prevent unescaping of what's not escaped inside attribute.
        testUnescapeAgnTags(
            '<span title="[agnFOO foo="aaa&quot;bbb" bar="xxx&quot;yyy"]">Creation date</span>',
            '<span title="[agnFOO foo="aaa&quot;bbb" bar="xxx&quot;yyy"]">Creation date</span>'
        );
    } catch (e) {
        console.error(e);
    }
    */

})();
