Cursor {

	classvar <xFactor = 50; 
	classvar <yFactor = 40;

	// view variables
	var color, <>bounds, initialBounds;

	var zoom;
	var <selected = false;

	*new { arg event;
		^super.new.init(event)
	}

	getRect { arg event;
		
		^Rect(
			event.x,
			event.y,
			1,
			yFactor
		);
	}

	init { arg event;
		color = SequencerTheme.darkGrey;
		zoom = 1@1;

		bounds = this.getRect(event);
	}

	renderView { arg origin;
		var renderBounds = bounds.moveBy(origin.x, origin.y);
		Pen.smoothing = true;
		Pen.color = color;
		
		if (bounds.width == 1, {
			Pen.line(renderBounds.origin, renderBounds.leftBottom);
	  	Pen.stroke;
	  	}, {
	  	Pen.addRect(renderBounds);
	  	Pen.draw;
	  });
	}

	setBounds { arg rect;
		bounds.set(
			rect.left,
			rect.top,
			rect.width,
			rect.height
		);
	}

	shouldMove {
		^true
	}

	moveAction { arg x, y, snap;
		if (this.shouldMove) {
			if ((snap && (bounds.origin.x % x != 0)), {
				var snappedValue = bounds.origin.x.roundUp(x);
				bounds = bounds.moveTo(snappedValue, bounds.origin.y);
				}, {
					bounds = bounds.moveBy(x, y)
			});
		}
	}

	mouseDownAction { arg x, y, modifiers, buttonNumber, clickCount;
		bounds.left = x;
		bounds.width = 1;
		bounds.top = y.trunc(yFactor * zoom.y);
	}

	moveTo { arg x, y;
		bounds.left = x;
		bounds.top = y;
	}

	moveBy { arg x, y;
		bounds = bounds.moveBy(x, y);
	}


	zoomBy { arg zoomX = 1, zoomY = 1;
		zoom.x = zoom.x * zoomX;
		zoom.y = zoom.y * zoomY;
		bounds.set(
			bounds.left * zoomX,
			bounds.top * zoomY,
			bounds.width * zoomX,
			bounds.height * zoomY
		);
	}
}