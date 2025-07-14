AGN.Lib.Controller.new('salutation-view', function() {
  const Select = AGN.Lib.Select;
  let $recipient;
  let $tagType;
  let AGN_TAG_TITLE;
  let AGN_TAG_TITLE_FIRST;
  let AGN_TAG_TITLE_FULL;
  let id;

  this.addDomInitializer('salutation-view', function () {
    $recipient = $('#recipient');
    $tagType = $('#tagType');
    id = this.config.id;
    AGN_TAG_TITLE = this.config.AGN_TAG_TITLE;
    AGN_TAG_TITLE_FIRST = this.config.AGN_TAG_TITLE_FIRST;
    AGN_TAG_TITLE_FULL = this.config.AGN_TAG_TITLE_FULL;
    switchSalutation();
  });

  this.addAction({
    change: 'switch-salutation',
    input: 'switch-salutation'}, () => switchSalutation());

  function getCurrentAgnTag() {
    return `[${getCurrentAgnTagName()} type='${id}']`;
  }

  function switchSalutation() {
    getSalutationText().then(text => $('#salutation-preview').text(text + ','));
    $('#copy-salutation').data('copyable-value', getCurrentAgnTag());
  }

  function getCurrentAgnTagName() {
    return $tagType.val();
  }

  async function resolveWithServer() {
    const agnTag = getCurrentAgnTag();
    try {
      return await $.get(AGN.url(`/salutation/${id}/resolve.action`), {
        recipientId: $recipient.val(),
        type: Select.get($tagType).$selectedOption.data('id')});
    } catch (error) {
      console.error("Error resolving salutation agnTag:", error);
      return agnTag;
    }
  }

  async function getSalutationText() {
    const { title, firstname, lastname, gender } = Select.get($recipient).$selectedOption.data();
    const salutation = $(`[name="genderMapping[${gender}]"]`).val();

    if (salutation.includes('#<>#')) {
      return await resolveWithServer();
    }

    switch (getCurrentAgnTagName()) {
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
