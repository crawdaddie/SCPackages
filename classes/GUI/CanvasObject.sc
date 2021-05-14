CanvasObject {
	var props;

	onClose {
		Dispatcher.removeListenersForObject(this)
	}

	onDrag { arg aMouseAction;
	
	}

	onDragEnd { arg aMouseAction;
	
	}

	getMouseAction { arg x, y;
		^(
			initialPosition: x@y,
			mouseMoveAction: { arg ev; this.onDrag(ev) },
			mouseUpAction: { arg ev; this.onDragEnd(ev); this.select(false); },
		)
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
	var <item;
	var props;
	var canvasProps;

	*new { arg item, canvasProps;
		^super.new.init(item, canvasProps);
	}

  getProps { arg item, canvasProps;
    ^(
			label: item.id.asString,
      canvasProps: canvasProps,
			renderBounds: this.renderBounds(item, canvasProps.origin, canvasProps.zoom),
			redraw: canvasProps['redraw'],
		)
  }


	init { arg anRxEvent, aCanvasProps;
		item = anRxEvent;
		props = Props((color: Color.rand, selected: false).putAll(this.getProps(item, aCanvasProps)));

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
			item.length * xFactor,
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

	onDrag { arg aMouseAction;
		var renderBounds = this.renderBounds(item, props.canvasProps.origin, props.canvasProps.zoom);
		var delta = aMouseAction.position - aMouseAction.initialPosition;
		
		var newBounds = Rect(
			renderBounds.left + delta.x,
			renderBounds.top + delta.y,
			renderBounds.width,
			renderBounds.height,
		);

    props.put(
      'renderBounds',
      newBounds
        .snapToRow(props)
        .snapToBeat(props)
    );
		
		props.redraw();
	}

	onDragEnd { arg aMouseAction;
		var origin = props.canvasProps.origin;
		var zoom = props.canvasProps.zoom;
		var xFactor = Theme.horizontalUnit;
		var yFactor = Theme.verticalUnit;
		var bounds;
		var itemParams;

		
		bounds = props.renderBounds
			.moveBy(-1 * origin.x, -1 * origin.y)
			.scaleBy(zoom.x.reciprocal, zoom.y.reciprocal);

		itemParams = (
			beats: bounds.left / xFactor,
			row: bounds.top / yFactor,
			length: bounds.width / xFactor
		);

		item.putAll(itemParams);
	}

  getContextMenuActions {
    var actions = [
      MenuAction("edit", { item.getView }),
    ];
    if (item.src.notNil) {
      actions = actions.add(
        MenuAction("edit source", { var mod = item.getModule; mod.open}),

      );
    }
    ^actions;
  }
}


