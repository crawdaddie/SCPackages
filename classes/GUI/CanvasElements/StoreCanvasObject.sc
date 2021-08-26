StoreCanvasObject : SequenceableCanvasObject {

  copyTo { arg position, parentStore;
    var newProps = props.copy;
    var bounds = newProps.renderBounds;
    var newStore = item.copyAsEvent.put('id', nil);
    props.postln;

    newProps.putAll(
      (
        renderBounds: Rect(
          position.x,
          position.y,
          bounds.width,
          bounds.height
        )
        .snapToRow(props.canvasProps)
        .snapToBeat(props.canvasProps)
      )
    );

    newStore.putAll(this.getItemParams(newProps));
    parentStore.addObject(Store.new(newStore));
  }
  getItemEditView {
    SequencerCanvas(item);
  }
}
