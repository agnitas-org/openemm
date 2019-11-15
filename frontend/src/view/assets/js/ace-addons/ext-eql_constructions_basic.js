ace.define("ace/ext/eql_constructions_basic", ["require", "exports", "ace/ext/eql_constructions_tokens"], function (require, exports) {
  "use strict";

  var Keyword = require('./eql_constructions_tokens').Keyword;
  var Paren = require('./eql_constructions_tokens').Paren;
  var NumericId = require('./eql_constructions_tokens').NumericId;
  var NotRequiredConstruction = require('./eql_constructions_tokens').NotRequiredConstruction;
  var Identifier = require('./eql_constructions_tokens').Identifier;
  var StringInQuotes = require('./eql_constructions_tokens').StringInQuotes;
  var NotEquals = require('./eql_constructions_tokens').NotEquals;
  var ConstantList = require('./eql_constructions_tokens').ConstantList;

  var eql_suggestions_constructions = [
    [new Keyword('CLICKED'), new Keyword('IN'), new Keyword('MAILING'), new NumericId()],
    [new Keyword('NOT'), new Paren('('), new Keyword('CLICKED'), new Keyword('IN'), new Keyword('MAILING'), new NumericId(), new Paren(')')],
    [new Keyword('CLICKED'), new Keyword('LINK'), new NumericId(), new Keyword('IN'), new Keyword('MAILING'), new NumericId()],
    [new Keyword('NOT'), new Paren('('), new Keyword('CLICKED'), new Keyword('LINK'), new NumericId(), new Keyword('IN'), new Keyword('MAILING'), new NumericId(), new Paren(')')],
    [new Keyword('OPENED'), new Keyword('MAILING'), new NumericId()],
    [new Keyword('NOT'), new Paren('('), new Keyword('OPENED'), new Keyword('MAILING'), new NumericId(), new Paren(')')],
    [new Keyword('RECEIVED'), new Keyword('MAILING'), new NumericId()],
    [new Keyword('NOT'), new Paren('('), new Keyword('RECEIVED'), new Keyword('MAILING'), new NumericId(), new Paren(')')],
    [new Identifier(), new NotRequiredConstruction([new Keyword('NOT')]), new Keyword('LIKE'), new StringInQuotes()],
    [new Identifier(), new Keyword('IS'), new NotRequiredConstruction([new Keyword('NOT')]), new Keyword('EMPTY')],
    [new Identifier(), new NotRequiredConstruction([new Keyword('NOT')]), new Keyword('STARTS'), new Keyword('WITH'), new StringInQuotes()],
    [new Identifier(), new NotRequiredConstruction([new Keyword('NOT')]), new Keyword('IN'), new ConstantList()],
    [new Identifier(), new NotRequiredConstruction([new Keyword('NOT')]), new Keyword('CONTAINS'), new StringInQuotes()],
    [new Identifier(), new Keyword('='), new StringInQuotes(), new NotRequiredConstruction([new Keyword('DATEFORMAT'), new StringInQuotes()])],
    [new Identifier(), new NotEquals(), new StringInQuotes(), new NotRequiredConstruction([new Keyword('DATEFORMAT'), new StringInQuotes()])],
    [new Identifier(), new Keyword('>'), new StringInQuotes(), new NotRequiredConstruction([new Keyword('DATEFORMAT'), new StringInQuotes()])],
    [new Identifier(), new Keyword('>='), new StringInQuotes(), new NotRequiredConstruction([new Keyword('DATEFORMAT'), new StringInQuotes()])],
    [new Identifier(), new Keyword('<'), new StringInQuotes(), new NotRequiredConstruction([new Keyword('DATEFORMAT'), new StringInQuotes()])],
    [new Identifier(), new Keyword('<='), new StringInQuotes(), new NotRequiredConstruction([new Keyword('DATEFORMAT'), new StringInQuotes()])],
    [new Identifier(), new Keyword('='), new NumericId(), new NotRequiredConstruction([new Keyword('DATEFORMAT'), new StringInQuotes()])],
    [new Identifier(), new NotEquals(), new NumericId(), new NotRequiredConstruction([new Keyword('DATEFORMAT'), new StringInQuotes()])],
    [new Identifier(), new Keyword('>'), new NumericId(), new NotRequiredConstruction([new Keyword('DATEFORMAT'), new StringInQuotes()])],
    [new Identifier(), new Keyword('>='), new NumericId(), new NotRequiredConstruction([new Keyword('DATEFORMAT'), new StringInQuotes()])],
    [new Identifier(), new Keyword('<'), new NumericId(), new NotRequiredConstruction([new Keyword('DATEFORMAT'), new StringInQuotes()])],
    [new Identifier(), new Keyword('<='), new NumericId(), new NotRequiredConstruction([new Keyword('DATEFORMAT'), new StringInQuotes()])]
  ];

  exports.eql_constructions = eql_suggestions_constructions;
});
