TransportLines {
	classvar loopPointColor;
	classvar playCursorColor;
	var id;
	var loopPoints;
	var playCursor;
	var pollCursorRoutine;

	*new { arg id, canvas;
		^super.new.init(id, canvas)
	}

	*initClass {
		loopPointColor = Color.grey(0.8, 0.5);
		playCursorColor = Color.grey(0.5, 1);
	}

	init { arg id, canvas;
		id = id;
		loopPoints = Store.at(id).transportContext.loopPoints;

		Dispatcher.addListener(
			'storeUpdated',
			this,
			{ arg payload, view;
				var loopPoints = payload.transportContext !? _.loopPoints ?? nil;

				if (payload.storeId == id && loopPoints.notNil) {
					view.setLoopPoints(loopPoints);
				}
			}
		);

		Dispatcher.addListener(
			'storePlaying',
			this,
			{ arg payload, view;
				if (payload.storeId == id) {
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
			}
		);

		Dispatcher.addListener(
			'storeNotPlaying',
			this,
			{ arg payload, view;
				if (payload.storeId == id) {
					playCursor = payload.stopPosition;
					pollCursorRoutine.stop;
				}
			}
		)
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

	renderView { arg quantX, origin, bounds, zoom, subdivisions;
		var gap = quantX * zoom.x;
		var xOffset = origin.x + (0 - origin.x).roundUp(gap);
		this.drawLoopPoints(gap, xOffset, bounds);
		this.drawPlayCursor(gap, xOffset, bounds);
	}
}