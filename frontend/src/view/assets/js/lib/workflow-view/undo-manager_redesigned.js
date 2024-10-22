(() => {
  const Def = AGN.Lib.WM.Definitions;
  const Node = AGN.Lib.WM.Node;

  class UndoStack {
    constructor(maxLength) {
      this.maxLength = maxLength;
      this._undoStack = [];
      this._transactionStack = [];
      this._transaction = false;
    }
  
    add(stack) {
      while (this._undoStack.length >= this.maxLength) {
        this._undoStack.shift();
      }
  
      this._undoStack.push(stack);
    }
  
    pop() {
      return this._undoStack.pop().reverse();
    }
  
    addOperation(operation) {
      this._transactionStack.push(operation);
    }
  
    isEmpty() {
      return _.isEmpty(this._undoStack);
    }
  
    startTransaction() {
      this._transaction = true;
    };
  
    endTransaction() {
      this.flush();
      this._transaction = false;
    }
  
    flush() {
      if (!_.isEmpty(this._transactionStack)) {
        this.add(this._transactionStack);
      }
      this._transactionStack = [];
    }
  
    isTransactionStarted() {
      return this._transaction === true;
    }
  }

  class UndoManager {
    constructor(editor, options) {
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

    undoAddNode(node) {
      this.editor.deleteNode(node);
    }

    undoDeleteNode(node) {
      if (node instanceof Node) {
        this.editor.add(Node.restore(node));
      } else {
        this.editor.add(Node.get(node));
      }
    }

    undoCreateConnection(connection) {
      this.editor.deleteConnectionBetween(connection.source, connection.target);
    }

    undoDeleteConnection(connection) {
      this.editor.connect(Node.get(connection.source), Node.get(connection.target));
    }

    undoMoveNode(movingData) {
      const scale = this.editor.getZoom();
      const self = this;
      movingData.forEach(function (data) {
        data.node.setTitleEnabled(false);
      });

      this.editor.batch(function () {
        movingData.forEach(function (data) {
          var coordinates = data.coordinates;
          var node = data.node;

          if (self.editor.getCurrentDragMode() !== data.dragMode) {
            self.editor.convertCoordinates(coordinates);
          }

          node.setCoordinates(coordinates.x, coordinates.y);
          node.updateCoordinates(scale);
        });

        self.editor.updateOverlays();
        self.editor.positionTitles();
        self.editor.updateMinimap();
      });

      movingData.forEach(function (data) {
        data.node.setTitleEnabled(true);
      });
    }

    undoSaveNodeData(node, undoNode) {
      undoNode = undoNode instanceof Node ? undoNode : Node.get(undoNode);
      node = node instanceof Node ? node : Node.get(node);

      node.setTitle(undoNode.title);
      node.setOverlayTitle(undoNode.getOverlayTitle());
      node.setComment(undoNode.getComment());
      node.setDependent(undoNode.isDependent());
      node.setInRecipientsChain(undoNode.isInRecipientsChain());
      node.setData(undoNode.getData());
      node.setFilled(undoNode.isFilled());

      this.editor.positionTitles();
      node.nodePopover.update();
    }

    canUndo() {
      return !this.undoStack.isEmpty();
    }

    startTransaction() {
      this.manualStart = true;
      this.undoStack.startTransaction();
    }

    endTransaction() {
      this.undoStack.endTransaction();
      this.onChange();
      this.manualStart = false;
    }

    transaction(targetFunc) {
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
    }

    operation(operation) {
      if (this.undoStack.isTransactionStarted()) {
        var args = [].slice.call(arguments);
        args.shift(); //first param is operation name and it is not necessary to store in undo stack

        this.undoStack.addOperation({operation: operation, args: args});
      }
    }

    undo() {
      if (this.canUndo()) {
        const operations = this.undoStack.pop().sort((op1, op2) => {
          const o1 = op1.operation;
          const o2 = op2.operation;

          if (o1 === 'nodeDeleted' && o2 !== 'nodeDeleted') {
            return -1;
          }

          if (o1 !== 'nodeDeleted' && o2 === 'nodeDeleted') {
            return 1;
          }

          return 0;
        });

        this.editor.batch(() => {
          operations.forEach(operation => {
            const undoFunc = this.undoOperations[operation.operation];
            undoFunc?.apply(this, operation.args);
          });
        });

        this.onChange();
      }
    }
  }

  AGN.Lib.WM.UndoManager = UndoManager;
})();
