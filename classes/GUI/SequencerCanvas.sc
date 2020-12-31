SequencerCanvas {
	var canvas;

	var props; 
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
		var mouseAction;
		
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

		props = (
			quantX: 100,
			origin: 0@0,
			timingOffset: 0,
			zoom: 1@1,
			redraw: { canvas.refresh },
			canvasBounds: canvas.parent.bounds,
		);

		canvas.onResize = { arg c;
			props.canvasBounds = c.parent.bounds;
		};
		
		this.makeChildren(store.itemsFlat);

		canvas.drawFunc = {
			this.renderView();
		};

		canvas.mouseDownAction = { arg view, x, y, modifiers, buttonNumber, clickCount;
			var clickedView = views.detect(_.contains(x@y));
			// [x, y, clickedView].postln;
			mouseAction = ( x: x, y: y, clickedView: clickedView);
		};

		canvas.mouseMoveAction = { arg view, x, y, modifiers, buttonNumber, clickCount;
			mouseAction.putPairs(['x', x, 'y', y]);
			mouseAction.postln;
		};

		canvas.mouseUpAction = { arg view, x, y, modifiers, buttonNumber, clickCount;

			mouseAction.postln;
			mouseAction = nil;		
		};

		canvas.onClose = { arg view;
			views.do(_.onClose);
			grid.onClose;
		};

	}

	makeChildren { arg items;
		grid = SequencerGrid();
		views = items.collect({ arg item;
			var class = item.embedView ?? CanvasObject;
			class.new(item, props);
		});
	}

	renderView {
		grid.render(props);
		views.do(_.render(props));
	}

	zoomBy { arg x = 1, y = 1;
		var zoomX = props.zoom.x;
		var zoomY = props.zoom.y;
		props.zoom.x = zoomX * x;
		props.zoom.y = zoomY * y;
		canvas.refresh;	
	}
}
