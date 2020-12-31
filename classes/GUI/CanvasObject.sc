CanvasObject {
	var props;
	*new {
		^super.new.init()
	}

	init {
		props = ();
	}

	renderView {
	}

	render {
		^this.performWithEnvir('renderView', props)
	}

	onClose {
		Dispatcher.removeListenersForObject(this)
	}
}

SequenceableCanvasObject : CanvasObject {
	var item;
	var props;
	var canvasProps;

	*new { arg item, canvasProps;
		^super.new.init(item, canvasProps);
	}

	init { arg anRxEvent, aCanvasProps;
		item = anRxEvent;
		canvasProps = aCanvasProps;

		props = (
			color: Color.rand,
			label: item.id.asString,
			bounds: this.getBounds(item)
		);

		this.listen(
			Topics.objectUpdated,
			{ arg payload;
				props.bounds = this.getBounds(item);
				canvasProps.redraw();
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

	getBounds { arg item;
		var xFactor = Theme.horizontalUnit;
		var yFactor = Theme.verticalUnit;	

		^Rect(
			item.beats * xFactor,
			item.row * yFactor,
			item.length * xFactor,
			yFactor
		)
	}

	contains { arg aPoint;
		var bounds = this.performWithEnvir('renderBounds', ().putAll(canvasProps, props));
		^bounds.contains(aPoint);
	}

	renderBounds { arg bounds, origin, zoom;
		^bounds
			.scaleBy(zoom.x, zoom.y)
			.moveBy(origin.x, origin.y);
	}

	renderView { arg origin, zoom, bounds, canvasBounds, color, label;
		var renderBounds = this.renderBounds(bounds, origin, zoom);

		if (renderBounds.intersects(canvasBounds).not) { ^false };

		Pen.smoothing = true;
		Pen.addRect(renderBounds);
		Pen.color = color;
	  Pen.draw;

	  Pen.stringInRect(label, renderBounds, font: Theme.font, color: Theme.grey);
	}

	render {
		^this.performWithEnvir('renderView', ().putAll(canvasProps, props))
	}
}


SoundfileCanvasObject : SequenceableCanvasObject {

}