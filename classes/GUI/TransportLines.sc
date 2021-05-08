TransportLines {
	classvar loopPointColor;
	classvar playCursorColor;
	var <id;
	var loopPoints;
	var playCursor;
	var canvas;
	var pollCursorRoutine;

	*new { arg id, canvas;
		^super.new.init(id, canvas)
	}

	*initClass {
		loopPointColor = Color.grey(0.8, 0.5);
		playCursorColor = Color.grey(0.5, 1);
	}

	storeUpdated { arg payload;
		var loopPoints = Store.at(id).getLoopPoints;
		this.setLoopPoints(loopPoints);
	}

	playerStarted { arg payload;
		playCursor = payload.startPosition;
		pollCursorRoutine = Routine({
			inf.do {
				playCursor = payload.player.currentPosition;
				canvas.refresh;
				0.1.wait;
			}
		})
		.play(AppClock)
	}

	playerStopped { arg payload;
		playCursor = payload.stopPosition;
		pollCursorRoutine.stop;
	}

	init { arg argid, argcanvas;
		id = argid;
		canvas = argcanvas;

		loopPoints = Store.at(id).getLoopPoints;
		Dispatcher.connectObject(this,
			'storeUpdated',
			'playerStarted',
			'playerStopped'
		);
	}

	setLoopPoints { arg setLoopPoints;
		loopPoints = setLoopPoints;
	}

	drawLoopPoints { arg gap, xOffset, bounds;
		loopPoints !? {
			var loopStart = loopPoints[0] * gap + xOffset;
			var loopEnd = loopPoints[1] * gap + xOffset;
			Pen.fillColor_(loopPointColor);
			Pen.addRect(Rect.newSides(loopStart, 0, loopEnd, bounds.height));
			Pen.fill;
		};
	}

	drawPlayCursor { arg gap, xOffset, bounds;
		playCursor !? {
			var linePosition = playCursor * gap + xOffset;
			Pen.strokeColor_(playCursorColor);
			Pen.line(Point(linePosition, 0), Point(linePosition, bounds.height));
			Pen.stroke;
		}
	}

	renderView { arg quantX, origin, timingOffset, bounds, zoom, subdivisions;
		var gap = quantX * zoom.x;
		var timingOffsetInPixels = timingOffset * gap;
		var xOffset = origin.x - timingOffsetInPixels;
		this.drawLoopPoints(gap, xOffset, bounds);
		this.drawPlayCursor(gap, xOffset, bounds);
	}
}