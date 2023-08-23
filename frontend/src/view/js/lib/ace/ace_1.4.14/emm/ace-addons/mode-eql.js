ace.define("ace/mode/eql_completions", ["require", "exports", "module", "ace/token_iterator", "ace/ext/eql_constructions_basic", "ace/ext/eql_constructions_extended", "ace/ext/eql_constructions_tokens"], function(require, exports, module) {
  "use strict";

  /*
  * Class for searching through the content of ace editor.
  * @link https://ace.c9.io/api/token_iterator.html
  */
  var TokenIterator = require("../token_iterator").TokenIterator;
  var eql_constructions_module = require('ace/ext/eql_constructions_extended');
  if (!eql_constructions_module) {
    eql_constructions_module = require('ace/ext/eql_constructions_basic');
  }
  var eql_constructions = eql_constructions_module.eql_constructions;
  var Keyword = require('ace/ext/eql_constructions_tokens').Keyword;

  var EqlCompletions = function() {
  };


  {
    var result = [];
    for(var i = 0; i < eql_constructions.length; ++i) {
      var suggestionExpression = eql_constructions[i];
      var addToResult = true;
      for(var j = 0; j < suggestionExpression.length; ++j) {
        var suggestionToken = suggestionExpression[j];
        if (!suggestionToken.isRequiredConstruction()) {
          eql_constructions.push(suggestionExpression.slice(0, j).concat(suggestionExpression.slice(j + 1)));
          eql_constructions.push(suggestionExpression.slice(0, j).concat(suggestionToken.getSuggestionTokens(), suggestionExpression.slice(j + 1)));
          addToResult = false;
          break;
        }
      }
      if (addToResult && !containsInMatrOfSuggestions(result, suggestionExpression)) {
        result.push(suggestionExpression);
      }
    }
    eql_constructions = result;
  }

  function containsInMatrOfSuggestions(matr, array) {
    for(var i = 0; i < matr.length; ++i) {
      var contains = true;
      for(var j = 0; j < array.length && j < matr[i].length; ++j) {
        if (!_.isEqual(matr[i][j], array[j])) {
          contains = false;
          break;
        }
      }
      if (contains) {
        return true;
      }
    }
    return false;
  }

  var groupSuggestions = [new Keyword('AND'), new Keyword('OR')];

  (function() {
    this.getCompletions = function(state, session, pos, prefix) {
      var filteredSuggestions = this.getKeywordCompletions(state, session, pos, prefix);
      return filteredSuggestions.map(function(suggestion) {
        return {
          // actually suggestion
          value: suggestion.keyword,

          // score affects on order of suggestions
          // the bigger score the higher it will be displayed
          // in list of suggestions
          score: suggestion.score,

          // description
          // will be displayed in the right side of suggestion
          meta: 'keyword',

          // didn't figure out what is it.
          name: 'keyword'
        }
      }, []);
    };

    this.getKeywordCompletions = function(state, session, pos, prefix) {
      var iterator = new TokenIterator(session, pos.row, pos.column);
      var prevToken = getCorrectPrevToken(iterator);
      if (!prevToken) {
        return getSuggestionsFirstWords();
      }

      var startOfNewExpression = groupSuggestions.some(function(suggestion) {
        return suggestion.isMatched(prevToken);
      });
      if (startOfNewExpression) {
        return getSuggestionsFirstWords();
      }

      return getSuggestions(session, pos);
    };

    function getSuggestions(session, pos) {
      var result = [];

      eql_constructions.forEach(function(suggestion) {
        var score = 0;
        var iterator = new TokenIterator(session, pos.row, pos.column);
        var prevToken = getCorrectPrevToken(iterator);
        for(var i = suggestion.length - 1; i >= 0; i--) {
          if (!prevToken) {
            score = 0;
            break;
          }

          var suggestionToken = suggestion[i];
          if (suggestionToken.isMatched(prevToken)) {
            score++;
            prevToken = getCorrectPrevToken(iterator);
          } else {
            iterator = new TokenIterator(session, pos.row, pos.column);
            prevToken = getCorrectPrevToken(iterator);
            score = 0;
          }
        }

        if (score > 0) {
          addOrUpdateSuggestionToArray(result, suggestion, score);
        }
      });

      result = Object.values(result);

      if (isPrevTokenInOneOfLastSuggestionsTokens(session, pos)) {
        result = result.concat(getGroupSuggestions());
      }
      return result;
    }

    function isPrevTokenInOneOfLastSuggestionsTokens(session, pos) {
      var iterator = new TokenIterator(session, pos.row, pos.column);
      var prevToken = getCorrectPrevToken(iterator);

      return eql_constructions.some(function(currentValue) {
        return currentValue[currentValue.length - 1].isMatched(prevToken);
      });
    }

    function getSuggestionsFirstWords() {
      var result = [];
      eql_constructions.forEach(function(suggestion) {
        var token = suggestion[0];
        if (token && typeof token.isForSuggestion === 'function' && token.isForSuggestion()) {
          token.getKeywords().forEach(function(item) {
            result[item] = {
              keyword: item,
              score: 1
            };
          });
        }
      });
      return Object.values(result);
    }

    function getCorrectPrevToken(iterator) {
      var result = iterator.stepBackward();
      if (!result || result.value.trim()) {//start of line or not blank string
        return result;
      }
      return iterator.stepBackward();
    }

    function getGroupSuggestions() {
      var result = [];
      groupSuggestions.forEach(function(suggestion) {
        suggestion.getKeywords().forEach(function(suggestionWord) {
          result.push({
            keyword: suggestionWord,
            score: 1
          });
        });
      });
      return result;
    }

    function addOrUpdateSuggestionToArray(array, suggestion, tokenIndex) {
      var neededToken = suggestion[tokenIndex];
      if (neededToken && typeof neededToken.isForSuggestion === 'function' && neededToken.isForSuggestion()) {
        neededToken.getKeywords().forEach(function(item) {
          var resultScore = tokenIndex;
          var oldToken = array[item];
          if (oldToken && oldToken['score'] > resultScore) {
            resultScore = oldToken['score'];
          }
          array[item] = {
            keyword: item,
            score: resultScore
          };
        });
      }
    }
  }).call(EqlCompletions.prototype);

  exports.EqlCompletions = EqlCompletions;
});

ace.define("ace/mode/eql_highlight_rules", ["require", "exports", "module", "ace/lib/oop", "ace/mode/text_highlight_rules"], function(require, exports, module) {
  "use strict";

  var oop = require("../lib/oop");
  var TextHighlightRules = require("./text_highlight_rules").TextHighlightRules;

  var EqlHighlightRules = function() {

    var keywords = (
      "is|empty|in|like|received|mailing|opened|clicked|link|revenue|by|references|having|and|or|not|" +
      "dateformat|finished|autoimport|contains|starts|with|today|has|binding|for|mediatype|on|mailinglist|" +
      "of|type|status|references|having|email|sms|admin|test|regular|active|optout|blacklisted|wait_for_doi_confirm"
    );

    var keywordMapper = this.createKeywordMapper({
      "keyword": keywords
    }, "identifier", true);

    this.$rules = {
      "start": [{
        token: "constant.list",
        regex: "\\(\\s*((?:\".*?\")|(?:'.*?')|\\d+)\\s*(?:,\\s*((?:\".*?\")|(?:'.*?')|\\d+)\\s*)*\\)"
      }, {
        token: "string",           // " string
        regex: '".*?"'
      }, {
        token: "string",           // ' string
        regex: "'.*?'"
      }, {
        token: "identifier",       // ` identifier
        regex: "`.*?`"
      }, {
        token: "constant.numeric", // float
        regex: "[+-]?\\d+(?:(?:\\.\\d*)?(?:[eE][+-]?\\d+)?)?\\b"
      }, {
        token: keywordMapper,
        regex: "[a-zA-Z_$][a-zA-Z0-9_$]*\\b"
      }, {
        token: "keyword.operator",
        regex: "\\+|\\-|\\*|\\/|%|<|>|<=|=>|!=|<>|="
      }, {
        token: "expression_in_parentheses",
        regex: "\\(\\)"
      }, {
        token: "paren.lparen",
        regex: "[\\(]"
      }, {
        token: "paren.rparen",
        regex: "[\\)]"
      }, {
        token: "text",
        regex: "\\s+"
      }]
    };
    this.normalizeRules();
  };

  oop.inherits(EqlHighlightRules, TextHighlightRules);

  exports.EqlHighlightRules = EqlHighlightRules;
});

ace.define("ace/mode/eql", ["require", "exports", "module", "ace/lib/oop", "ace/mode/text", "ace/mode/eql_highlight_rules", "ace/range", "ace/ext/language_tools", "ace/mode/eql_completions", "ace/ext/eql_constructions"], function(require, exports, module) {
  "use strict";

  var oop = require("../lib/oop");
  var TextMode = require("./text").Mode;
  var EqlHighlightRules = require("./eql_highlight_rules").EqlHighlightRules;
  var EqlCompletions = require("./eql_completions").EqlCompletions;

  var Mode = function() {
    this.HighlightRules = EqlHighlightRules;
    this.$completer = new EqlCompletions();
  };

  oop.inherits(Mode, TextMode);

  (function() {
    this.lineCommentStart = "--";

    this.$id = "ace/mode/eql";

    this.getCompletions = function(state, session, pos, prefix) {
      return this.$completer.getCompletions(state, session, pos, prefix);
    };
  }).call(Mode.prototype);

  exports.Mode = Mode;

});
