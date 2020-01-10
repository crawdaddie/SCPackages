SequenceableBlock {
	classvar <xFactor = 50, <yFactor = 40;
	// reference to state
	var id;

	// view variables
	var color, bounds, initialBounds;
	var initialCursor;

	// state-action variables
	var action = nil;
	var <selected = false, toRefresh = true;
	var zoom;

	getAction { arg x, y;
		var innerArea = bounds.insetBy(5, 0);
		^case
			{innerArea.contains(x@y)} {'move'}
			{x < innerArea.left} {'resizeLeft'}
			{x > innerArea.right} {'resizeRight'};
	}

	*new { arg event;
		^super.new.init(event)
	}

	resetBounds { arg event;
		var rect = this.getRect(event);
		bounds.set(
			rect.left,
			rect.top,
			rect.width,
			rect.height
		);
	}

	getRect { arg event;
		var timestamp, length;

		length = event.resolveLength;
		timestamp = event.resolveTimestamp;


		^Rect(
			timestamp * xFactor * zoom.x,
			event.channel * yFactor * zoom.y,
			length * xFactor * zoom.x,
			yFactor * zoom.y
		);
	}

	select {
		selected = true;
	}

	unselect {
		selected = false;
	}

	objectUpdated { arg payload;
		if (id == payload.id) {
			this.resetBounds(payload);
		}
	}

	init { arg event;
		id = event.id;
		color = Color.rand;
		initialCursor = 0@0;
		zoom = 1@1;
		bounds = this.getRect(event);
		initialBounds = bounds.copy;

		Dispatcher.addListener('objectUpdated', { arg payload;
			this.objectUpdated(payload)
		})
	}

	renderView { arg origin;
		var renderBounds = bounds.moveBy(origin.x, origin.y);
		Pen.addRect(renderBounds); // wie addRect
		Pen.color = color;
	  Pen.draw;
		if (selected) {
			Pen.addRect(renderBounds);
   		Pen.strokeColor = Color.black;
   		Pen.stroke;
		};
	}

	mouseDownAction { arg x, y;
		action = this.getAction(x, y);
		selected = true;
		initialBounds = bounds.copy;
		initialCursor = x@y;
	}

	moveAction { arg x, y;
		if (selected) {
			bounds = bounds.moveBy(x, y);
		};
		this.updateState;
	}

	mouseMoveAction { arg x, y;
		var newOrigin = x@y - (initialCursor - initialBounds.origin);
		var difference = bounds.origin - newOrigin;

		toRefresh = bounds.origin != newOrigin;

		if (selected) {
			switch (action,
				'move', {
					bounds.origin = newOrigin;
				},
				'resizeLeft', {
					bounds.set(
						bounds.left - difference.x,
						bounds.top,
						bounds.width + difference.x,
						bounds.height);
				},
				'resizeRight', {
					bounds.set(
						bounds.left,
						bounds.top,
						initialBounds.width - difference.x,
						bounds.height);
				}
			);
		}
	}

	mouseUpAction {
		if (selected && bounds != initialBounds) {
			this.updateState;
		}
	}

	updateState {
		// call these variables 'absolute<Name>' because the store takes care of updating the events themselves with
		// bpm values
		var absoluteTime = bounds.origin.x / (xFactor * zoom.x);
		var absoluteExtension = bounds.origin.y / (yFactor * zoom.y);
		var absoluteLength = bounds.width / (xFactor * zoom.x);
		Dispatcher((
			type: 'timingChange',
			payload: (
				id: id,
				x: absoluteTime,
				y: absoluteExtension,
				length: absoluteLength
				)
		));
	}

	contains { arg x_y;
		^bounds.contains(x_y)
	}

	setZoom { arg zoomX = 1, zoomY = 1;
		zoom = (zoom.x * zoomX)@(zoom.y * zoomY);
		bounds.set(
			bounds.left * zoomX,
			bounds.top * zoomY,
			bounds.width * zoomX,
			bounds.height * zoomY
		);
	}
}


SequenceableSoundfileBlock : SequenceableBlock {
	var soundfileMod, <waveformObject, <startPos;
	*new { arg event;
		^super.new(event).initSoundfile(event);
	}

	initSoundfile { arg event;
		soundfileMod = Mod.all.at(event.soundfile);
		startPos = event.startPos;
		this.getWaveform;
		^this;
	}

	renderView { arg origin;
		super.renderView(origin);
		this.renderWaveform(origin);
	}

	objectUpdated { arg payload;
		super.objectUpdated(payload);
		payload.startPos !? { |startPos|
			startPos = startPos;
		};
	}

	setZoom { arg zoomX = 1, zoomY = 1;
		super.setZoom(zoomX, zoomY);
		this.getWaveform;
	}

	getWaveform {
		waveformObject = soundfileMod.getWaveform(zoom.x);
	}

	renderWaveform { arg origin;

		var waveform = waveformObject.waveform;
		var renderBounds = bounds.moveBy(origin.x, origin.y);
		var height = renderBounds.height;
		var waveformColor = color.multiply(Color(0.5, 0.5, 0.5));

		Pen.strokeColor = waveformColor;
		
		if (waveform.size > 0) {
			var middle = (renderBounds.leftTop + renderBounds.leftBottom) / 2;
			min(renderBounds.width, waveform.size).do { arg index;
				var data = waveform[index];
				var max = middle + Point(0, data[0] * height / 2);
				var min = middle + Point(0, data[1] * height / 2);

				Pen.line(max, min);
				Pen.fillStroke;
				middle.x = middle.x + 1;
			}
		}
	}
}