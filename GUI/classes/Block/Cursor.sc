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
		var renderBounds = super.renderView(origin, parentBounds);
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
}