(() => {

  const Field = AGN.Lib.Field;

  AGN.Lib.Validator.new('calendar-comment', {
    valid: function ($e, options) {
      return !this.errors($e, options).length;
    },
    errors: function () {
      const errors = validateCommentText();

      if ($('[name="isCustomRecipients"]').val() === 'true') {
        errors.push(...validateCustomRecipients());
      }

      if ($('[name="deadline"]').is(':checked')) {
        errors.push(...validateReminder());
      }
      return errors;
    }
  });

  function validateCommentText() {
    const errors = [];
    const $comment = $('#comment-text');
    const commentText = $comment.val();

    if (_.isEmpty(_.trim(commentText))) {
      errors.push({field: $comment, msg: t("calendar.error.empty_comment")});
    }
    if (byteCount(commentText) > 1000) {
      errors.push({field: $comment, msg: t("calendar.error.long_comment")});
    }
    return errors;
  }

  function validateCustomRecipients() {
    const errors = [];
    const $recipients = $('[name="recipients"]');
    const emails = $recipients.val().filter(email => email?.trim() !== "");

    if (!emails?.length) {
      errors.push({field: $recipients, msg: t("calendar.error.empty_recipient_list")});
    }
    const recipients = emails.join(',');
    if (byteCount(recipients) > 2000) {
      errors.push({field: $recipients, msg: t("calendar.error.long_recipient_list")});
    }
    _.each(emails, email => {
      if (!AGN.Lib.Helpers.isValidEmail(email)) {
        errors.push({field: $recipients, msg: t("calendar.error.invalid_email") + " " + email});
      }
    });
    return errors;
  }

  function validateReminder() {
    const $reminderDate = $('#remind-date');
    return Field.create($reminderDate).dateValue < new Date()
      ? [{field: $reminderDate, msg: t('calendar.error.reminderInPast')}]
      : [];
  }

  function byteCount(s) {
    var escapedStr = encodeURI(s);
    if (escapedStr.indexOf("%") != -1) {
      var count = escapedStr.split("%").length - 1;
      if (count == 0) count++;
      var tmp = escapedStr.length - (count * 3);
      count = count + tmp;
    } else {
      count = escapedStr.length;
    }
    return count;
  }
})();
