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
		Dispatcher.connectObject(this, 'objectUpdated');
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
				// clean up and return updat
			}
		);
	}

	showMenu { arg x, y;
		^Menu(
			MenuAction("copy (cmd-c)", 	{ Clipboard.add(id) }),
			MenuAction("paste (cmd-v)", {
				var absoluteTime = x / (Theme.horizontalUnit * zoom.x);
				var absoluteExtension = y / (Theme.verticalUnit * zoom.y);
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
		if (payload.id == id) {
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

	
		Pen.stringInRect(label, renderBounds, font: Theme.font, color: Theme.grey);
	}

	getUpdate {
		var beats = bounds.origin.x / (Theme.horizontalUnit * zoom.x);
		var extension = bounds.origin.y / (Theme.verticalUnit * zoom.y);
		var lengthInBeats = bounds.width / (Theme.horizontalUnit * zoom.x);

		^(id: id, x: beats, y: extension, length: lengthInBeats)
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


	*partitionByrow { arg views;
		var partition = ();
		views.do { | view |
			var row = view.bounds.top.asInteger;
			partition[row] !? { | set |
				set.add(view)
				} ?? {
					partition[row] = Set[view]
				}
		};
		^partition;
	}
	
	edit {
		var object = Store.at(id);
		object.getModule !? { arg mod;
			mod.open;			
		};
		object.getView;
	}
}
