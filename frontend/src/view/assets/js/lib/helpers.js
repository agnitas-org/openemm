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
var url = 'http://localhost:8080/css/mailingbase.do?action=1&isTemplate=false&page=1';
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

(function () {

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

                if (typeof(value) === "undefined") {
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
            $('body').append('<style class="disable-animation">* { -webkit-transition: none !important; -o-transition: none !important; transition: none !important; }</style>')
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

        pad: function(value, count, char) {
            char = char || '0';

            var s = value + '';
            if (s.length < count) {
                return new Array(count - s.length + 1).join(char) + s;
            } else {
                return s;
            }
        },

        caching: function(func, thisArg) {
            var calculated = false;
            var value;

            if (arguments.length > 1) {
                func = _.bind.apply(null, Array.prototype.slice.call(arguments));
            }

            return function() {
                if (!calculated) {
                    calculated = true;
                    value = func();
                }
                return value;
            };
        }
    }

})();
