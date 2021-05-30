CanvasObject {
	var props;

	onClose {
		Dispatcher.removeListenersForObject(this)
	}
  
  onDragStart { arg aMouseAction;
  }

	onDrag { arg aMouseAction;
	
	}

	onDragEnd { arg aMouseAction;
	
	}

	renderView {
	}

	render {
		^this.performWithEnvir('renderView', props)
	}

	select {
		props.selected = true
	}

	unselect {
		props.selected = false
	}

	selected {
		^props.selected;
	}
  getContextMenuActions {
    ^[
      MenuAction("cut", { props.postln }),
      MenuAction("copy", { props.postln }),
    ]
  }
}

SequenceableCanvasObject : CanvasObject {
  classvar widgetSize;
	var item;
	var props;
	var canvasProps;

  *initClass {
    widgetSize = 5;
  }

	*new { arg item, canvasProps;
		^super.new.init(item, canvasProps);
	}

  getProps { arg item, canvasProps; /*: Props */
    // translation item -> props
    ^(
      canvasProps: canvasProps,
			renderBounds: this.renderBounds(item, canvasProps.origin, canvasProps.zoom),
			redraw: canvasProps['redraw'],
		)
  }
  getItemParams { arg props; /*: Item */
    // translation props -> item
		var origin = props.canvasProps.origin;
		var zoom = props.canvasProps.zoom;
		var xFactor = Theme.horizontalUnit;
		var yFactor = Theme.verticalUnit;

		
		var bounds = props.renderBounds
			.moveBy(-1 * origin.x, -1 * origin.y)
			.scaleBy(zoom.x.reciprocal, zoom.y.reciprocal);

		var itemParams = (
			beats: bounds.left / xFactor,
			row: bounds.top / yFactor,
			dur: bounds.width / xFactor
		);
    ^itemParams;
  }


	init { arg anRxEvent, aCanvasProps;
		item = anRxEvent;
		props = Props((
      color: Color.rand,
      selected: false,
      label: item.id.asString,
    ).putAll(this.getProps(item, aCanvasProps)));

		aCanvasProps.addDependant(props);
		props.onUpdate_({ arg aCanvasProps;
			this.getProps(item, aCanvasProps); 
		});

		this.listen(
			Topics.objectUpdated,
			{ arg payload;
        props.putAll(this.getProps(item, props.canvasProps));
				props.redraw();
			}
		);
	}

	listen { arg type, fn;
		Dispatcher.addListener(type, this, { arg payload;
			if (payload.id == item.id) {
				fn.value(payload)
			}
		})
	}

	contains { arg aPoint;
		^this.bounds(item, props.canvasProps.origin, props.canvasProps.zoom).contains(aPoint);
	}

	bounds { arg item, origin, zoom;
		var xFactor = Theme.horizontalUnit;
		var yFactor = Theme.verticalUnit;	

		var bounds = Rect(
			item.beats * xFactor,
			item.row * yFactor,
			item.dur * xFactor,
			yFactor
		);

		^bounds
			.scaleBy(zoom.x, zoom.y)
	}


	renderBounds { arg item, origin, zoom;
		^this.bounds(item, origin, zoom).moveBy(origin.x, origin.y);
	}

	renderView { arg renderBounds, origin, zoom, canvasBounds, color, label, selected, canvasProps;
		if (renderBounds.intersects(canvasProps.canvasBounds).not) { ^false };

		Pen.smoothing = true;
		Pen.addRect(renderBounds);
		Pen.color = color;
	  Pen.draw;

	  if (selected) {
			Pen.addRect(renderBounds);
   		Pen.strokeColor = Theme.darkGrey;
   		Pen.stroke;
		};

	  Pen.stringInRect(label, renderBounds, font: Theme.font, color: Theme.grey);
	}
  onDragStart {
    props.initialBounds = props.renderBounds;
  }
  getMouseAction { arg aMouseAction/*: (
    modifiers: Number, 
    initialPosition: Point,
    mouseDelta: Point,
    position: Point,
    mouseMoveAction: Function,
    mouseUpAction: Function
  ) */;
    var initialPosition = aMouseAction.initialCanvasPosition;
    var modifiers = aMouseAction.modifiers;
    var renderBounds = props.renderBounds;
    props.initialBounds = renderBounds;

    if (renderBounds.width < (widgetSize * 2)) {
      ^();
    };

    if ((modifiers == 262144) && (this.pointInLeftWidget(initialPosition, renderBounds))) {
      ^(
        mouseMoveAction: { arg ev; ev.selected.collect(_.resizeLeft(ev))}
      )
    };

    if ((modifiers == 262144) && (this.pointInRightWidget(initialPosition, renderBounds))) {
      ^(
        mouseMoveAction: { arg ev; ev.selected.collect(_.resizeRight(ev))}
      )
    };


    ^()
  }
  pointInRightWidget { arg aPoint, aRect;
    ^Rect(
      aRect.right - widgetSize,
      aRect.top,
      widgetSize,
      aRect.height,
    ).contains(aPoint)
  }
  
  pointInLeftWidget { arg aPoint, aRect;
    ^Rect(
      aRect.left,
      aRect.top,
      widgetSize,
      aRect.height,
    ).contains(aPoint)
  }
  
  dragProps { arg aMouseAction /*: (
    modifiers: Number, 
    initialPosition: Point,
    mouseDelta: Point,
    position: Point,
    mouseMoveAction: Function,
    mouseUpAction: Function
  ) */;
    var renderBounds = this.renderBounds(item, props.canvasProps.origin, props.canvasProps.zoom);
		var delta = aMouseAction.mouseDelta;
		
		var newBounds = Rect(
			renderBounds.left + delta.x,
			renderBounds.top + delta.y,
			renderBounds.width,
			renderBounds.height,
		);
    ^(
      renderBounds: newBounds
        .snapToRow(props)
        .snapToBeat(props)
    );
  }

	onDrag { arg aMouseAction;
    props.putAll(this.dragProps(aMouseAction));
		props.redraw();
	}

  resizeRight { arg aMouseAction;
    var dragProps;
    var delta = aMouseAction.mouseDelta;
    var initialBounds = props.initialBounds;
  
    dragProps = (
      renderBounds: Rect(
        initialBounds.left,
        initialBounds.top,
        max(initialBounds.width + (delta.x), 10),
        initialBounds.height
      ) 
    );
    props.putAll(dragProps);
    props.redraw();
  }

  resizeLeft { arg aMouseAction;
    var dragProps;
    var delta = aMouseAction.mouseDelta;
    var newBounds = props.renderBounds;
    var initialBounds = props.initialBounds;
  
    dragProps = (
      renderBounds: Rect(
        initialBounds.left + (delta.x),
        initialBounds.top,
        max(initialBounds.width - (delta.x), 10),
        initialBounds.height
      ) 
    ); 
    props.putAll(dragProps);
    props.redraw();
  }

  copyTo { arg position, store;
    var newProps = props.copy;
    var bounds = newProps.renderBounds;
    var newItem = item.copyAsEvent;

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

    newItem.putAll(this.getItemParams(newProps));
    store.addObject(newItem);
  }

	onDragEnd { arg aMouseAction;
    item.putAll(this.getItemParams(props));
	}

  getItemEditView {
    var view = EnvirGui(item, options: [\proto])
      .putSpec(\row, ControlSpec(0, 128, \lin, 1, 0));

		view.viewForParam('id').visible_(false);
		view.parent.name = item.id;
		^view;
  }

  getContextMenuActions {
    var actions = [
      MenuAction("edit", { this.getItemEditView }),
    ];
    if (item.src.notNil) {
      actions = actions.add(
        MenuAction("edit source", { item.getModule.open}),
      );
    }
    ^actions;
  }
}


