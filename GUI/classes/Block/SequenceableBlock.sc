SequenceableBlock : CanvasBlockBase {

	classvar <moveWidgetPixelsWidth = 5;
	// reference to state
	var <id;
	var label;

	*new { arg event, zoom = Point(1, 1);
		^super.new(event, zoom).initView(event);
	}

	initView { arg event;
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

	
		Pen.stringInRect(label, renderBounds, font: Font("Helvetica", 10), color: Color.grey(0.1, 0.5));
	}

	getUpdate {
		// call these variables 'absolute<Name>' because the store takes care of updating the events themselves with
		// bpm values
		var absoluteTime = bounds.origin.x / (xFactor * zoom.x);
		var absoluteExtension = bounds.origin.y / (yFactor * zoom.y);
		var absoluteLength = bounds.width / (xFactor * zoom.x);

		^(id: id, x: absoluteTime, y: absoluteExtension, length: absoluteLength)
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
