AGN.Lib.Validator.new('phone-number', {
  valid: function($e, options) {
    return !this.errors($e, options).length;
  },

  errors: function($e, options) {
    var content = $.trim($e.val());
    var errors = [];

    var TOKEN_BEGIN = 0;
    var TOKEN_PLUS = 1;
    var TOKEN_DIGIT = 3;
    var TOKEN_SEPARATOR = 4;
    var TOKEN_LPAREN = 5;
    var TOKEN_RPAREN = 6;

    if (content) {
      var previousToken = TOKEN_BEGIN;
      var hasViolations = false;
      var insideParentheses = false;
      var digitsCount = 0;

      for (var i = 0; i < content.length; i++) {
        var c = content.charAt(i);

        if (c >= '0' && c <= '9') {
          digitsCount++;
          previousToken = TOKEN_DIGIT;
        } else if (c == '.' || c == '-' || c == '/') {
          // Separators can only be placed next to digits or outer side of parentheses.
          // Multiple separators next to each other are not allowed.
          if (previousToken == TOKEN_DIGIT || previousToken == TOKEN_RPAREN) {
            previousToken = TOKEN_SEPARATOR;
          } else {
            hasViolations = true;
            break;
          }
        } else if (c == '(') {
          // Nested parentheses are not allowed.
          if (insideParentheses) {
            hasViolations = true;
            break;
          }

          previousToken = TOKEN_LPAREN;
          insideParentheses = true;
        } else if (c == ')') {
          // Every closing parenthesis requires an opening one.
          // Empty parentheses are not allowed.
          if (previousToken == TOKEN_DIGIT && insideParentheses) {
            previousToken = TOKEN_RPAREN;
            insideParentheses = false;
          } else {
            hasViolations = true;
            break;
          }
        } else if (c == '+') {
          // Plus sign is optional and can only be a prefix.
          if (previousToken == TOKEN_BEGIN) {
            previousToken = TOKEN_PLUS;
          } else {
            hasViolations = true;
            break;
          }
        } else if (!/\s/.test(c)) {
          // Whitespaces are simply ignored, the rest of characters are not allowed.
          hasViolations = true;
          break;
        }
      }

      if (hasViolations || insideParentheses || digitsCount < 3 || digitsCount > 20) {
        errors.push({field: $e, msg: t('fields.errors.phone_number_invalid')});
      }
    } else if (options.required === true) {
      errors.push({field: $e, msg: t('fields.required.errors.missing')});
    }

    return errors;
  }
});
