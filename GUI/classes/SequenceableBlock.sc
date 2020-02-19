SequenceableBlock {
	/**
	 * xFactor = 50
	 * default value for a 1-second long block, or equivalently:
	 * 1 beat at 60 bpm
	 * so ordinarily, let's say a soundfile block lasts 2 seconds, and zoom = 1@1 
	 * this block will render with a width of 100 pixels
	 **/
	classvar <xFactor = 50; 
	
	classvar <yFactor = 40;
	classvar <moveWidgetPixelsWidth = 5;
	// reference to state
	var <id;

	// view variables
	var color, <bounds, initialBounds;
	var initialCursor;

	// state-action variables
	var action = nil;
	var <>selected = false, toRefresh = true;
	var zoom;
	var label;
	var parent;


	getAction { arg x, y;
		var innerArea = bounds.insetBy(moveWidgetPixelsWidth, 0);
		^case
			{innerArea.contains(x@y)} {'move'}
			{x < innerArea.left} {'resizeLeft'}
			{x > innerArea.right} {'resizeRight'};
	}

	*new { arg event;
		^super.new.init(event)
	}

	showMenu { arg x, y;
		^Menu(
			MenuAction("copy (cmd-c)", 	{ Clipboard.add(id) }),
			MenuAction("paste (cmd-v)", {
				var absoluteTime = x / (xFactor * zoom.x);
				var absoluteExtension = y / (yFactor * zoom.y);
				Dispatcher((
					type: 'pasteObjects',
					payload: (
						x: absoluteTime,
						y: absoluteExtension,
						items: Clipboard.normalizedItems
					))
				);
				Clipboard.clear;
			}),
   		MenuAction("cut (cmd-x)", 	{ "cut item".postln; }),
   		MenuAction("slice (cmd-d)", { "slice".postln 	}),
		).front;
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

		// "get rect".postln;
		// event.postcs;
		length = event.length;
		timestamp = event.timestamp;


		^Rect(
			timestamp * xFactor * zoom.x,
			event.channel * yFactor * zoom.y,
			length * xFactor * zoom.x,
			yFactor * zoom.y
		);
	}

	select {
		selected = true;
		^this.bounds;
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
			if (payload.id == id) {
				this.objectUpdated(payload)
			}
		});
		label = id.asString;

	}

	renderView { arg origin, parentBounds;
		var renderBounds = bounds.moveBy(origin.x, origin.y);
		if (renderBounds.intersects(parentBounds).not) { ^false };
		
		Pen.smoothing = true;
		Pen.addRect(renderBounds);
		Pen.color = color;
	  Pen.draw;
		
		if (selected) {
			Pen.addRect(renderBounds);
   		Pen.strokeColor = SequencerTheme.darkGrey;
   		Pen.stroke;
		};

		label !? {
			var point = renderBounds.leftTop;
			Pen.stringInRect(label, renderBounds, font: Font("Helvetica", 10), color: Color.grey(0.1, 0.5));
		};
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

	moveTo { arg x, y;
		bounds = bounds.moveTo(x, y);
	}

	setTransparent {
		color.alpha = 0.8;
	}

	setOpaque {
		color.alpha = 1;
	}

	resizeLeftBy { arg difference;
		bounds.set(
			bounds.left - difference,
			bounds.top,
			max(moveWidgetPixelsWidth, bounds.width + difference),
			bounds.height
		);
	}

	resizeRightBy { arg difference;
		bounds = Rect(
			bounds.left,
			bounds.top,
			max(moveWidgetPixelsWidth, initialBounds.width - difference),
			bounds.height
		);
	}

	getUpdate {
		// call these variables 'absolute<Name>' because the store takes care of updating the events themselves with
		// bpm values
		var absoluteTime = bounds.origin.x / (xFactor * zoom.x);
		var absoluteExtension = bounds.origin.y / (yFactor * zoom.y);
		var absoluteLength = bounds.width / (xFactor * zoom.x);

		^(id: id, x: absoluteTime, y: absoluteExtension, length: absoluteLength)
	}

	contains { arg x_y;
		^bounds.contains(x_y)
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

	edit {
		// no-op
	}
}


SequenceableSoundfileBlock : SequenceableBlock {
	var soundfileMod, <waveformObject, <startPos;
	*new { arg event;
		^super.new(event).initSoundfile(event);
	}

	initSoundfile { arg event;
		soundfileMod = Mod(event.soundfile);
		startPos = event.startPos;
		label = "% - %".format(event.soundfile.basename, event.id);
		this.getWaveform;
		^this;
	}

	renderView { arg origin, parentBounds;
		super.renderView(origin, parentBounds);
		this.renderWaveform(origin);
	}

	objectUpdated { arg payload;
		super.objectUpdated(payload);
		payload.startPos !? { |startPos|
			startPos = startPos;
		};
	}

	zoomBy { arg zoomX = 1, zoomY = 1;
		super.zoomBy(zoomX, zoomY);
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

		Pen.smoothing = true;
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

	edit {
		// no-op
	}
}


StoreBlock : SequenceableBlock {
	
	init { arg event;
		super.init(event);
		label = "store - %".format(event.id);
	}

	edit {
		var path = Store.getPath(id);
		var store = Store.getBase(id);
		var canvas = SequencerCanvas.fromStore(store);
		canvas.parent.name = "store - %".format(path); 
		^canvas;
	}
}