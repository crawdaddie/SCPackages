StoreCanvasObject : SequenceableCanvasObject {

  copyTo { arg position, parentStore;
    var newProps = props.copy;
    var bounds = newProps.renderBounds;
    var newStore = item.copy.put('id', nil);

    newProps.putAll(
      (
        renderBounds: Rect(
          position.x,
          position.y,
          bounds.width,
          bounds.height
        )
        .snapToRow(props)
        .snapToBeat(props)
      )
    );

    newStore.putAll(this.getItemParams(newProps));
    parentStore.addObject(newStore);
  }
}
