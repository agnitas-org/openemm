(() => {

  class DynContent { // content for the specific target group

    constructor(data) {
      if (typeof DynContent.uniqueIdCounter === 'undefined') {
        DynContent.uniqueIdCounter = 0;
      }
      this.uniqueId = ++DynContent.uniqueIdCounter; // id for frontend side
      this.id = data.id; // id for backend side
      this.index = data.dynOrder === undefined ? data.index : data.dynOrder;
      this.content = data.dynContent === undefined ? data.content : data.dynContent;
      this.targetId = data.targetID === undefined ? data.targetId : data.targetID;
    }

    get lightWeight() {
      const { id, content, targetId } = this;
      return { id, content, targetId };
    }
  }

  AGN.Lib.MailingContent.DynContent = DynContent;
})();
