ace.define("ace/mode/eql_highlight_rules",["require","exports","module","ace/lib/oop","ace/mode/text_highlight_rules"], function(require, exports, module) {
"use strict";

var oop = require("../lib/oop");
var TextHighlightRules = require("./text_highlight_rules").TextHighlightRules;

var EqlHighlightRules = function() {

    var keywords = (
        "is|empty|in|like|received|mailing|opened|clicked|link|revenue|by|references|having|and|or|not|dateformat"
    );

    var keywordMapper = this.createKeywordMapper({
        "keyword": keywords
    }, "identifier", true);

    this.$rules = {
        "start" : [ {
            token : "string",           // " string
            regex : '".*?"'
        }, {
            token : "string",           // ' string
            regex : "'.*?'"
        }, {
            token : "identifier",           // ` identifier
            regex : "`.*?`"
        }, {
            token : "constant.numeric", // float
            regex : "[+-]?\\d+(?:(?:\\.\\d*)?(?:[eE][+-]?\\d+)?)?\\b"
        }, {
            token : keywordMapper,
            regex : "[a-zA-Z_$][a-zA-Z0-9_$]*\\b"
        }, {
            token : "keyword.operator",
            regex : "\\+|\\-|\\*|\\/|%|<|>|<=|=>|!=|<>|="
        }, {
            token : "paren.lparen",
            regex : "[\\(]"
        }, {
            token : "paren.rparen",
            regex : "[\\)]"
        }, {
            token : "text",
            regex : "\\s+"
        } ]
    };
    this.normalizeRules();
};

oop.inherits(EqlHighlightRules, TextHighlightRules);

exports.EqlHighlightRules = EqlHighlightRules;
});

ace.define("ace/mode/eql",["require","exports","module","ace/lib/oop","ace/mode/text","ace/mode/eql_highlight_rules","ace/range"], function(require, exports, module) {
"use strict";

var oop = require("../lib/oop");
var TextMode = require("./text").Mode;
var EqlHighlightRules = require("./eql_highlight_rules").EqlHighlightRules;
var Range = require("../range").Range;

var Mode = function() {
    this.HighlightRules = EqlHighlightRules;
};
oop.inherits(Mode, TextMode);

(function() {

    this.lineCommentStart = "--";

    this.$id = "ace/mode/eql";
}).call(Mode.prototype);

exports.Mode = Mode;

});
