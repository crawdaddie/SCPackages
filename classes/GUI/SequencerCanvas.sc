CanvasObject {
	var item;

	//props
	var props;

	var canvasProps;

	*new { arg item, canvasProps;
		^super.new.init(item, canvasProps);
	}

	listen { arg type, fn;
		Dispatcher.addListener(type, this, { arg payload;
			if (payload.id == item.id) {
				fn.value(payload)
			}
		})
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

	renderView { arg origin, zoom, bounds, canvasBounds, color;
		var renderBounds = bounds
			.scaleBy(zoom.x, zoom.y)
			.moveBy(origin.x, origin.y)
		;

		if (renderBounds.intersects(canvasBounds).not) { ^false };

		Pen.smoothing = true;
		Pen.addRect(renderBounds);
		Pen.color = color;
	  Pen.draw;

	  Pen.stringInRect(props.label, renderBounds, font: Theme.font, color: Theme.grey);
	}

	render {
		^this.performWithEnvir('renderView', ().putAll(canvasProps, props))
	}
}

SequenceableCanvasObject : CanvasObject {

}

SoundfileCanvasObject : SequenceableCanvasObject {

}

// Props : Event {
// 	var onChange;

// 	*new { arg event, onChange;
// 		^super.new.init(event, onChange);
// 	}

// 	init { arg anEvent, anOnChangeCallback;
// 		know = true;
// 		super.putAll(anEvent);

// 		onChange = anOnChangeCallback;
// 	}

// 	put { arg key, value;
// 		var originalValue = this.at(key);
// 		super.put(key, value);

// 		if (originalValue !== value) {
// 			onChange.value();
// 		};
// 	}
// }


SequencerCanvas {
	var canvas;

	// "props"
	var canvasProps; 
	/**
	 * (
	 *   quantX: 100, // number in px
	 *	 origin: 0@0, // point - represents where the 'current' viewport is
	 *	 timingOffset: 0, // at which 'global' offset is the represented collection of events
	 *	 zoom: 1@1, // point
	 * );
	 */

	// child canvas objects
	var grid;
	var views;

	*new { arg store;
		^super.new.init(store);
	}

	init { arg store;
		var parent, bounds;
		var title = format("sequencer - %", store.id);
		if (store.id == 1000) {
			title = title ++ " (top level)"
		};

		parent = Window.new(title, Rect(740, 455, 700, 400)).front;
		bounds = parent.view.bounds;
		canvas = UserView(parent, bounds);
		canvas.resize = 5;
		parent.acceptsMouseOver_(true);

		canvasProps = (
			quantX: 100,
			origin: 0@0,
			timingOffset: 0,
			zoom: 1@1,
			redraw: { canvas.refresh },
			canvasBounds: canvas.parent.bounds,
		);

		canvas.onResize = { arg c;
			canvasProps.canvasBounds = c.parent.bounds;
		};
		
		this.makeChildren(store.itemsFlat);

		canvas.drawFunc = {
			this.renderView();
		};
	}

	makeChildren { arg items;
		grid = SequencerGrid();
		views = items.collect({ arg item;
			var class = item.embedView ?? CanvasObject;
			class.new(item, canvasProps);
		});
	}

	renderView {
		grid.render(canvasProps);
		views.do(_.render(canvasProps));
	}

	zoomBy { arg x = 1, y = 1;
		var zoomX = canvasProps.zoom.x;
		var zoomY = canvasProps.zoom.y;
		canvasProps.zoom.x = zoomX * x;
		canvasProps.zoom.y = zoomY * y;
		canvas.refresh;	
	}
}
