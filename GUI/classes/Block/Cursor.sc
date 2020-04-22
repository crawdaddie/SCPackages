Cursor : CanvasBlockBase {

	*new { arg event;
		^super.new(event).initCursor(event);
	}

	initCursor { arg event;
		color = SequencerTheme.darkGrey;
	}

	getRectFromEvent { arg event;	
		^Rect(
			event.x,
			event.y,
			1,
			yFactor
		);
	}

	x {
		^bounds.left
	}

	y { 
		^bounds.top
	}

	asPoint {
		^Point(this.x, this.y)
	}

	zoomBy { arg zoomX = 1, zoomY = 1;
		zoom.x = zoom.x * zoomX;
		zoom.y = zoom.y * zoomY;
		bounds.set(
			bounds.left * zoomX,
			bounds.top * zoomY,
			1,
			bounds.height * zoomY
		);
	}

	renderView { arg origin, parentBounds;
		var renderBounds = bounds.moveBy(origin.x, origin.y);
		if (renderBounds.intersects(parentBounds).not) { ^false };
		Pen.smoothing = true;
		Pen.color = color;
		
		if (bounds.width == 1, {
			Pen.line(renderBounds.origin, renderBounds.leftBottom);
	  	Pen.stroke;
	  	}, {
	  	Pen.addRect(renderBounds);
	  	Pen.stroke;
	  });
	}

	extendSelectionHandler { |moveX, moveY|
		var newWidth;
		var newHeight;
		if (bounds.width == 1) {
			newWidth = moveX;
		} {
			newWidth = bounds.width + moveX;
		};

		newHeight = bounds.height + moveY;

		bounds.set(
			bounds.left,
			bounds.top,
			newWidth,
			newHeight
		);
		^bounds;
	}

	resetSelection {
		bounds.set(
			bounds.left,
			bounds.top,
			1,
			yFactor
		)
	}

}