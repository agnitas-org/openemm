AGN.Lib.MailingContent.Def = {

  get $dynTagsList() {
    return $('#dyn-tags-container');
  },

  get $dynTagsMobileSelect() {
    return $('#mobile-dyn-tags-list');
  },

  get $dynTagSettings() {
    return $('#dyn-tag-settings');
  },

  get htmlDyntagNames() {
    return this._htmlDyntagNames || [];
  },

  set htmlDyntagNames(htmlDyntagNames) {
    this._htmlDyntagNames = htmlDyntagNames;
  },

  get smsDyntagNames() {
    return this._smsDyntagNames || [];
  },

  set smsDyntagNames(smsDyntagNames) {
    this._smsDyntagNames = smsDyntagNames;
  },

  get availableTargetGroups() {
    return this._availableTargetGroups || [];
  },

  set availableTargetGroups(availableTargetGroups) {
    this._availableTargetGroups = availableTargetGroups;
  },

  get isEditableMailing() {
    return this._isEditableMailing;
  },

  set isEditableMailing(isEditableMailing) {
    this._isEditableMailing = isEditableMailing;
  },

  get isContentGenerationAllowed() {
    return this._isContentGenerationAllowed;
  },

  set isContentGenerationAllowed(isContentGenerationAllowed) {
    this._isContentGenerationAllowed = isContentGenerationAllowed;
  },
};
