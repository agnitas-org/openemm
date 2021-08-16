(function() {

  var Def = AGN.Lib.WM.Definitions,
    Node = AGN.Lib.WM.Node;

  function UndoStack(maxLength) {
    this.maxLength = maxLength;

    this._undoStack = [];
    this._transactionStack = [];
    this._transaction = false;
  }

  UndoStack.prototype.add = function(stack) {
    while (this._undoStack.length >= this.maxLength) {
      this._undoStack.shift();
    }

    this._undoStack.push(stack);
  };

  UndoStack.prototype.pop = function() {
    return this._undoStack.pop();
  };

  UndoStack.prototype.addOperation = function(operation) {
    this._transactionStack.push(operation);
  };

  UndoStack.prototype.isEmpty = function() {
    return _.isEmpty(this._undoStack);
  };

  UndoStack.prototype.startTransaction = function() {
    this._transaction = true;
  };

  UndoStack.prototype.endTransaction = function() {
    this.flush();
    this._transaction = false;
  };

  UndoStack.prototype.flush = function() {
    if (!_.isEmpty(this._transactionStack)) {
      this.add(this._transactionStack);
    }
    this._transactionStack = [];
  };

  UndoStack.prototype.isTransactionStarted = function() {
    return this._transaction === true;
  };

  function UndoManager(editor, options) {
    this.editor = editor;

    this.undoOperations = {
      'nodeAdded': this.undoAddNode,
      'nodeDeleted': this.undoDeleteNode,
      'nodeMoved': this.undoMoveNode,
      'nodeDataUpdated': this.undoSaveNodeData,
      'connectionCreated': this.undoCreateConnection,
      'connectionDeleted': this.undoDeleteConnection
    };
    this.manualStart = false;
    this.undoStack = new UndoStack(Def.MAX_UNDO_STEP);
    this.onChange = _.isFunction(options.onChange) ? options.onChange : _.noop;
  }

  UndoManager.prototype.undoAddNode = function(node) {
    this.editor.deleteNode(node);
  };

  UndoManager.prototype.undoDeleteNode = function(node) {
    if (node instanceof Node) {
      this.editor.add(Node.restore(node));
    } else {
      this.editor.add(Node.get(node));
    }
  };

  UndoManager.prototype.undoCreateConnection = function(connection) {
      this.editor.deleteConnectionBetween(connection.source, connection.target);
  };

  UndoManager.prototype.undoDeleteConnection = function(connection) {
    this.editor.connect(Node.get(connection.source), Node.get(connection.target));
  };

  UndoManager.prototype.undoMoveNode = function(movingData) {
    var scale = this.editor.getZoom();
    var self = this;
    movingData.forEach(function(data) {
      data.node.setTitleEnabled(false);
    });

    this.editor.batch(function() {
      movingData.forEach(function(data) {
        var coordinates = data.coordinates;
        var node = data.node;

        node.setCoordinates(coordinates.x, coordinates.y);
        node.updateCoordinates(scale);
      });

      self.editor.updateOverlays();
      self.editor.positionTitles();
      self.editor.updateMinimap();
    });

    movingData.forEach(function(data) {
      data.node.setTitleEnabled(true);
    });
  };

  UndoManager.prototype.undoSaveNodeData = function(node, undoNode) {
    undoNode = undoNode instanceof Node ? undoNode : Node.get(undoNode);
    node = node instanceof Node ? node : Node.get(node);

    node.setTitle(undoNode.getTitle());
    node.setOverlayTitle(undoNode.getOverlayTitle());
    node.setOverlayImage(undoNode.getOverlayImage());
    node.setComment(undoNode.getComment());
    node.setFilled(undoNode.isFilled());
    node.setDependent(undoNode.isDependent());
    node.setInRecipientsChain(undoNode.isInRecipientsChain());

    node.setData(undoNode.getData());

    this.editor.positionTitles();
  };

  UndoManager.prototype.canUndo = function() {
    return !this.undoStack.isEmpty();
  };

  UndoManager.prototype.startTransaction = function() {
    this.manualStart = true;
    this.undoStack.startTransaction();
  };

   UndoManager.prototype.endTransaction = function() {
     this.undoStack.endTransaction();
     this.onChange();
     this.manualStart = false;
   };

  UndoManager.prototype.transaction = function(targetFunc) {
    if (_.isFunction(targetFunc)) {
      if (!this.undoStack.isTransactionStarted()) {
        this.manualStart = false;
        this.undoStack.startTransaction();
      }

      targetFunc.call();

      if (!this.manualStart) {
        this.undoStack.endTransaction();
      }
      this.onChange();
    }
  };

  UndoManager.prototype.operation = function(operation) {
    if (this.undoStack.isTransactionStarted()) {
      var args = [].slice.call(arguments);
      args.shift(); //first param is operation name and it is not necessary to store in undo stack

      this.undoStack.addOperation({operation: operation, args: args});
    }
  };

  UndoManager.prototype.undo = function() {
    if (this.canUndo()) {
      var operations = this.undoStack.pop();

      var compareFn = function(op1, op2) {
        var o1 = op1.operation;
        var o2 = op2.operation;
        var nodeChanges = ['nodeAdded', 'nodeDeleted', 'nodeDataUpdated'];
        var connectionChanges = ['connectionCreated', 'connectionDeleted'];
        if (connectionChanges.includes(o1) && nodeChanges.includes(o2)) {
          return 1;
        }
        if (nodeChanges.includes(o1) && connectionChanges.includes(o2)) {
          return -1;
        }

        return 0;
      };

      var self = this;
      this.editor.batch(function() {
        operations.sort(compareFn)
          .forEach(function(operation) {
            var undoFunc = self.undoOperations[operation.operation];
            if (undoFunc) {
              undoFunc.apply(self, operation.args);
            }
          });
      });

      this.onChange();
    }
  };

  AGN.Lib.WM.UndoManager = UndoManager;
})();
