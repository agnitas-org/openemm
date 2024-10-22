AGN.Lib.Controller.new('salutation-view', function() {
  const Select = AGN.Lib.Select;
  let $recipient;
  let AGN_TAG_TITLE;
  let AGN_TAG_TITLE_FIRST;
  let AGN_TAG_TITLE_FULL;
  let id;

  this.addDomInitializer('salutation-view', function () {
    $recipient = $('#recipient');
    id = this.config.id;
    AGN_TAG_TITLE = this.config.AGN_TAG_TITLE;
    AGN_TAG_TITLE_FIRST = this.config.AGN_TAG_TITLE_FIRST;
    AGN_TAG_TITLE_FULL = this.config.AGN_TAG_TITLE_FULL;
    switchSalutation();
  });

  this.addAction({
    change: 'switch-salutation',
    input: 'switch-salutation'}, () => switchSalutation());

  function switchSalutation() {
    $('#salutation-preview').text(getSalutationText() + ',');
    $('#copy-salutation').data('copyable-value', `[${getCurrentAgnTag()} type='${id}']`);
  }

  function getCurrentAgnTag() {
    return $('#agnTag').val();
  }

  function getSalutationText() {
    const { title, firstname, lastname, gender } = Select.get($recipient).$findOption($recipient.val()).data();
    const salutation = $(`[name="genderMapping[${gender}]"]`).val();

    switch (getCurrentAgnTag()) {
      case(AGN_TAG_TITLE):
        return `${salutation} ${title ? `${title} ` : ''}${lastname}`;
      case(AGN_TAG_TITLE_FIRST):
        return `${salutation} ${firstname}`;
      case(AGN_TAG_TITLE_FULL):
        return `${salutation} ${title ? `${title} ` : ''}${firstname} ${lastname}`;
      default:
        return '';
    }
  }
});
