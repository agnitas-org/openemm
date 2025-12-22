(() => {
  AGN.Lib.Validator.new('reject-not-allowed-chars', {
    valid: function ($e, options) {
      return !this.errors($e, options).length;
    },
    errors: function ($e, options) {
      const chars = typeof options.chars === 'string'
        ? $(options.chars).json().chars
        : options.chars;

      const disallowedChars = findDisallowedChars($e.val(), chars);
      if (!disallowedChars.length) {
        return [];
      }

      return [{
        field: $e, msg: t(
          options.msgKey,
          disallowedChars.map(char => `'${char}'`).join(', '))
      }];
    }
  });

  function findDisallowedChars(content, allowedChars) {
    const disallowedChars = [];
    const chars = new Set(allowedChars);

    for (let char of content) {
      if (!chars.has(char) && !disallowedChars.includes(char)) {
        disallowedChars.push(char);
      }
    }

    return disallowedChars;
  }
})();