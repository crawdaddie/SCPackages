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
}

SequenceableCanvasObject : CanvasObject {
	var <item;
	var props;
	var canvasProps;

	*new { arg item, canvasProps;
		^super.new.init(item, canvasProps);
	}

	init { arg anRxEvent, aCanvasProps;
		item = anRxEvent;
		props = Props((
			color: Color.rand,
			label: item.id.asString,
			zoom: aCanvasProps.zoom,
			canvasBounds: aCanvasProps.canvasBounds,
			renderBounds: this.renderBounds(item, aCanvasProps.origin, aCanvasProps.zoom),
			origin: aCanvasProps.origin,
			redraw: aCanvasProps['redraw'],
			selected: false,
		));

		aCanvasProps.addDependant(props);
		
		props.onUpdate_({ arg aCanvasProps;
			(
				zoom: aCanvasProps.zoom,
				canvasBounds: aCanvasProps.canvasBounds,
				renderBounds: this.renderBounds(item, aCanvasProps.origin, aCanvasProps.zoom);,
				origin: aCanvasProps.origin,
				redraw: aCanvasProps['redraw']
			)
		});

		this.listen(
			Topics.objectUpdated,
			{ arg payload;
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
		^this.bounds(item, props.origin, props.zoom).contains(aPoint);
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

	snapBoundsToRow { arg renderBounds;
		var y = renderBounds.top;

		[y, props.origin.y * props.zoom.y, props.zoom.y * Theme.verticalUnit].postln;

		renderBounds.top = y;
		^renderBounds;
	}

	snapBoundsToBeat { arg renderBounds;
		var x = renderBounds.left;

		renderBounds.left = x;
		^renderBounds;
	}

	renderBounds { arg item, origin, zoom;
		^this.bounds(item, origin, zoom).moveBy(origin.x, origin.y);
	}

	renderView { arg renderBounds, origin, zoom, canvasBounds, color, label, selected;
		if (renderBounds.intersects(canvasBounds).not) { ^false };

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
		var renderBounds = this.renderBounds(item, props.origin, props.zoom);
		var delta = aMouseAction.position - aMouseAction.initialPosition;
		
		var newBounds = Rect(
			renderBounds.left + delta.x,
			renderBounds.top + delta.y,
			renderBounds.width,
			renderBounds.height,
		);

		props.renderBounds = this.snapBoundsToRow(newBounds);
		
		props.redraw();
	}

	onDragEnd { arg aMouseAction;
		var origin = props.origin;
		var zoom = props.zoom;
		var xFactor = Theme.horizontalUnit;
		var yFactor = Theme.verticalUnit;
		var bounds;
		var itemParams;

		props.renderBounds = this.snapBoundsToBeat(props.renderBounds);
		
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
}


SoundfileCanvasObject : SequenceableCanvasObject {

}