StoreCanvasObject : SequenceableCanvasObject {

  copyTo { arg position, parentStore;
    var newProps = props.copy;
    var bounds = newProps.renderBounds;
    var newStore = item.copy;

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

  renderView { arg renderBounds, origin, zoom, canvasBounds, color, label, selected, canvasProps;
    this.renderContainedItems(renderBounds, origin, zoom, canvasBounds, color, label, selected);
    super.renderView(renderBounds, origin, zoom, canvasBounds, color, label, selected, canvasProps);
  }

  renderContainedItems { arg renderBounds, origin, zoom, canvasBounds, color, label, selected, canvasProps;
		var itemColor = color.multiply(Color(0.5, 0.5, 0.5));
    item.pairsDo { arg key, value;
      if (key.class == Integer) { 
        [value.beats <= item.dur].postln
      };
    }

  }
}
