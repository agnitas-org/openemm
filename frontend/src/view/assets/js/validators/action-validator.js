AGN.Lib.Validator.new('action', {
  valid: function ($e, options, isFieldsValid) {
    return isFieldsValid && !this.errors($e, options).length;
  },
  errors: function ($e, options) {
    var errors = [];
    if ($('#serviceMailOperation').length) this.validateSendServiceOperation(errors);

    return errors;
  },
  validateSendServiceOperation: function (errors) {
    validateEmailList($('input[id$=toAddress]'), errors, 'triggerManager.operation.serviceMail.error.recipientAddress');
    validateEmail($('input[id$=fromAddress]'), errors, 'triggerManager.operation.serviceMail.error.senderAddress');
    validateEmail($('input[id$=replyAddress]'), errors, 'triggerManager.operation.serviceMail.error.replyAddress');
    validateSubject($('input[id$=subjectLine]'), errors)
  }
});

function validateEmailList($fields, errors, errorCode) {
  $fields.each(function (i, field) {
    var $field = $(field);
    var emails = getEmailsFromField($field);
    if (!emails || !isValidEmailList(emails)) {
      errors.push({field: $field, msg: t(errorCode)});
    } else {
      $field.val(emails.join(","));
    }
  });
}

function validateEmail($fields, errors, errorCode) {
  $fields.each(function (i, field) {
    var $field = $(field);
    var value = $field.val().trim();
    if (!isValidEmail(value)) {
      errors.push({field: $field, msg: t(errorCode)});
    } else {
      $field.val(value);
    }
  });
}

function validateSubject($fields, errors) {
  $fields.each(function (i, field) {
    var $field = $(field);
    var value = $field.val().trim();
    if (!value || value.length < 3) {
      errors.push({field: $field, msg: t('triggerManager.operation.serviceMail.error.subjectToShort')});
    } else if (value && value.length > 200) {
      errors.push({field: $field, msg: t('triggerManager.operation.serviceMail.error.subjectToLong')});
    } else {
      $field.val(value);
    }
  });
}

function getEmailsFromField($field) {
  var value = $field.val();
  var emails = value.trim() && value.trim().split(/[ ,|]+/);
  _.map(emails, _.trim);
  return emails;
}

function isValidEmailList(list) {
  for (var i = 0; i < list.length; i++) {
    if (!isValidEmail(list[i])) {
      return false;
    }
  }
  return true;
}
//TODO replace with AGN.Lib.Helpers.isValidEmail(email)
function isValidEmail(email) {
  var re = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
  return re.test(String(email).toLowerCase());
}
