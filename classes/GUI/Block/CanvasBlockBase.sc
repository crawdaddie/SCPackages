CanvasBlockBase {

	/*
	 * xFactor = 50
	 * default value for a 1-second long block, or equivalently:
	 * 1 beat at 60 bpm
	 * so ordinarily, let's say a soundfile block lasts 2 seconds, and zoom = 1@1 
	 * this block will render with a width of 100 pixels
	 */

	classvar <xFactor = 50; 
	classvar <yFactor = 40;

	
	// view variables
	var color, <>bounds;
	var initialBounds;

	var <>selected = false;
	var zoom;

	*new { arg event, zoom = Point(1, 1);
		^super.new.init(event, zoom)
	}

	init { arg event, argzoom = Point(1, 1);
		color = Color.rand;
		zoom = argzoom.copy;
		bounds = this.getRectFromEvent(event);
		initialBounds = bounds.copy;
	}

	getMouseAction { arg x, y;
		^(
			mouseMoveAction: { arg x, y;
			
			},
			mouseUpAction: { arg x, y;
				// clean up and return update
			}
		);
	}

	resetBoundsFromEvent { arg event;
		var rect = this.getRectFromEvent(event);
		bounds.set(
			rect.left,
			rect.top,
			rect.width,
			rect.height
		);
	}

	getRectFromEvent { arg event;

		var timestamp, length, row;
		length = event.length;
		timestamp = event.beats;
		row = event.row;

		^Rect(
			timestamp * xFactor * zoom.x,
			row * yFactor * zoom.y,
			length * xFactor * zoom.x,
			yFactor * zoom.y
		);
	}

	setBounds { arg rect;
		bounds.set(
			rect.left,
			rect.top,
			rect.width,
			rect.height
		);
	}

	select {
		selected = true;
	}

	unselect {
		// "unselect".postln;
		selected = false;
	}

	renderView { arg origin, parentBounds;
		/*
		 * subclass responsibility - implementation tip:
		 * { arg origin, parentBounds;
		 *   var renderBounds = super.renderView(origin, parentBounds) // <- will return early if shouldn't render
		 *   // subclass-specific rendering code
		 * }
		 */		
		var renderBounds = bounds.moveBy(origin.x, origin.y);
		if (renderBounds.intersects(parentBounds).not) { ^false };
		^renderBounds;

	}

	shouldMove {
		^selected
	}

	moveAction { arg x, y, snap;
		if (this.shouldMove) {
			if ((snap && ((bounds.origin.x % x.asInteger).magnitude != 0.0)), {
				var snappedValue = bounds.origin.x.roundUp(x);
				bounds = bounds.moveTo(snappedValue, bounds.origin.y);
				}, {
					bounds = bounds.moveBy(x, y)
				});
		};

	}

	moveBy { arg x, y;
		bounds = bounds.moveBy(x, y);
	}

	// moveByAction { arg x, y;
	// 	bounds = bounds.moveBy(x, y);
	// 	^{
	// 		this.getUpdate()
	// 	}
	// }

	moveTo { arg x, y;
		bounds = bounds.moveTo(x, y);
	}

	setTransparent {
		color.alpha = 0.8;
	}

	setOpaque {
		color.alpha = 1;
	}

	setRightTo { arg right;
		bounds.width = right - bounds.left;
	}

	setLeftTo { arg left;
		bounds.left = left;
		bounds.width = left - bounds.right;
	}

	resizeLeftBy { arg difference;
		bounds.width = bounds.width + difference;
		bounds.left = bounds.left - difference;
	}

	resizeRightBy { arg difference;
		bounds.width = bounds.width - difference;			
	}

	contains { arg x_y;
		^bounds.contains(x_y)
	}
	
	intersects { arg rect;
		^bounds.intersects(rect)
	}

	zoomBy { arg x = 1, y = 1;
		zoom.x = zoom.x * x;
		zoom.y = zoom.y * y;
		
		bounds.set(
			bounds.left * x,
			bounds.top * y,
			bounds.width * x,
			bounds.height * y
		);
	}

	keyDownAction { |modifiers, key| 
		if (this.selected.not, {
			^nil;
		});
		^nil
	}
}