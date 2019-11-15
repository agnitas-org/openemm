ace.define("ace/ext/eql_constructions_tokens", ["require", "exports"], function(require, exports) {
  "use strict";

  var DefaultSuggestionToken = function() {
    this.isMatched = function() {
      return false;
    };

    this.isForSuggestion = function() {
      return false;
    };

    this.getKeywords = function() {
      return [];
    };

    this.isRequiredConstruction = function() {
      return true;
    };
  };

  var Keyword = function(keywords) {
    DefaultSuggestionToken.apply(this, arguments);

    if (Array.isArray(keywords)) {
      this._keywords = keywords.map(function(keyword) {
        return keyword.toUpperCase();
      });
    } else {
      this._keywords = [keywords.toUpperCase()];
    }

    this.isMatched = function(token) {
      return token && token.value && this._keywords.includes(token.value.toUpperCase());
    };

    this.isForSuggestion = function() {
      return true;
    };

    this.getKeywords = function() {
      return this._keywords;
    };
  };

  var NumericId = function() {
    DefaultSuggestionToken.apply(this, arguments);

    this.isMatched = function(token) {
      return token && /^0$|^(?:[1-9][0-9]*)$/.test(token.value);
    };

  };

  var Identifier = function() {
    DefaultSuggestionToken.apply(this, arguments);

    this.isMatched = function(token) {
      return token && token.type === 'identifier';
    };

  };

  var StringInQuotes = function() {
    DefaultSuggestionToken.apply(this, arguments);

    this.isMatched = function(token) {
      return token && token.type === 'string';
    };

    this.isForSuggestion = function() {
      return true;
    };

  };

  var Paren = function(paren) {
    DefaultSuggestionToken.apply(this, arguments);

    this._paren = paren;

    this.getKeywords = function() {
      return [this._paren + ' '];
    };

    this.isMatched = function(token) {
      return token && (token.type === 'paren.lparen' || token.type === 'paren.rparen');
    };

    this.isForSuggestion = function() {
      return true;
    };

  };

  var ConstantList = function() {
    DefaultSuggestionToken.apply(this, arguments);

    this.isMatched = function(token) {
      return token && token.type === 'constant.list';
    };

  };

  var ExpressionInParentheses = function() {
    DefaultSuggestionToken.apply(this, arguments);

    this.isMatched = function(token) {
      return token && token.type === 'expression_in_parentheses';
    };
  };

  var NotRequiredConstruction = function(arrayOfSuggestionTokens) {
    DefaultSuggestionToken.apply(this, arguments);

    this._suggestionTokens = arrayOfSuggestionTokens || [];

    this.getSuggestionTokens = function() {
      return this._suggestionTokens;
    };

    this.isRequiredConstruction = function() {
      return false;
    };
  };

  var MediaType = function() {
    Keyword.apply(this, [['EMAIL', 'SMS']]);
  };

  var BindingType = function() {
    Keyword.apply(this, [['ADMIN', 'TEST', 'REGULAR']]);
  };

  var BindingStatus = function() {
    Keyword.apply(this, [['ACTIVE', 'OPTOUT', 'BLACKLISTED', 'WAIT_FOR_DOI_CONFIRM']]);
  };

  var NotEquals = function() {
    Keyword.apply(this, [['<>', '!=']]);

    this.getKeywords = function() {
      return ['<>'];
    };

  };

  exports.DefaultSuggestionToken = DefaultSuggestionToken;
  exports.Keyword = Keyword;
  exports.Paren = Paren;
  exports.NumericId = NumericId;
  exports.NotRequiredConstruction = NotRequiredConstruction;
  exports.Identifier = Identifier;
  exports.ExpressionInParentheses = ExpressionInParentheses;
  exports.StringInQuotes = StringInQuotes;
  exports.MediaType = MediaType;
  exports.BindingType = BindingType;
  exports.BindingStatus = BindingStatus;
  exports.NotEquals = NotEquals;
  exports.ConstantList = ConstantList;
});
