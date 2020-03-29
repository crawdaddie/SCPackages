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

	*new { arg event;
		^super.new.init(event)
	}

	init { arg event;
		"base class init".postln;
		color = Color.rand;
		zoom = 1@1;
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

	resetBounds { arg event;
		var rect = this.getRectFromEvent(event);
		bounds.set(
			rect.left,
			rect.top,
			rect.width,
			rect.height
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

	getRectFromEvent { arg event;

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
	}

	unselect {
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
}

SequenceableBlock : CanvasBlockBase {

	classvar <moveWidgetPixelsWidth = 5;
	// reference to state
	var <id;
	var label;

	*new { arg event;
		^super.new.init(event)
	}

	init { arg event;
		"subclass init".postln;
		super.init(event);
		id = event.id;
		label = id.asString;

		Dispatcher.addListener('objectUpdated', { arg payload;
			if (payload.id == id) {
				this.objectUpdated(payload)
			}
		});
	}

	getAction { arg x, y;
		var innerArea = bounds.insetBy(moveWidgetPixelsWidth, 0);
		^case
			{innerArea.contains(x@y)} {'move'}
			{x < innerArea.left} {'resizeLeft'}
			{x > innerArea.right} {'resizeRight'};
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


	objectUpdated { arg payload;
		if (id == payload.id) {
			this.resetBoundsFromEvent(payload);
		}
	}

	renderView { arg origin, parentBounds;
		var renderBounds = super.renderView(origin, parentBounds);

		
		Pen.smoothing = true;
		Pen.addRect(renderBounds);
		Pen.color = color;
	  Pen.draw;
		
		if (selected) {
			Pen.addRect(renderBounds);
   		Pen.strokeColor = SequencerTheme.darkGrey;
   		Pen.stroke;
		};

	
		Pen.stringInRect(label, renderBounds, font: Font("Helvetica", 10), color: Color.grey(0.1, 0.5));
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

	setRightTo { arg right;
		bounds.width = right - bounds.left;
	}

	setLeftTo { arg left;
		bounds.left = left;
		bounds.width = left - bounds.right;
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
		bounds.setExtent(max(moveWidgetPixelsWidth, initialBounds.width - difference), bounds.height);
			
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

	findOverlapUpdates { arg otherViews;
		// var intersections = otherViews
		// 	.select(_.bounds.intersects(bounds))
		// 	.collect({ arg view;
		// 	if (view.bounds.right > bounds.left) {
		// 		// view.setRightTo(bounds.left);
		// 	};
		// 	if (view.bounds.left < bounds.right) {
		// 		// view.setLeftTo(bounds.right);
		// 	};
		// 	if ((view.bounds.left < bounds.left) && (view.bounds.right > bounds.right)) {
		// 		// view.splitAt(bounds.left, bounds.right);
		// 	};
		// 	if ((view.bounds.left >= bounds.left) && (view.bounds.right <= bounds.right)) {
		// 		// view.delete;
		// 	};
		// 	view;
		// });

		// ^intersections;
	}

	*partitionByChannel { arg views;
		var partition = ();
		views.do { | view |
			var chan = view.bounds.top.asInteger;
			partition[chan] !? { | set |
				set.add(view)
				} ?? {
					partition[chan] = Set[view]
				}
		};
		^partition;
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

	setLeftTo { arg left;
		super.setLeftTo(left);
		// startPos = 
	}
}


StoreBlock : SequenceableBlock {
	
	init { arg event;
		super.init(event);
		label = "store - %".format(event.id);
	}

	edit {
		var path = Store.getPath(id);
		var canvas = SequencerCanvas.fromStore(id);
		canvas.parent.name = "store - %".format(path);
		^canvas;
	}
}