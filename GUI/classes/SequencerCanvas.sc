Keys {
	classvar dict;
	*initClass {
		dict = Dictionary.with(*[
			\noMod -> 0,
			\cmdMod -> 1048576,
			\shiftMod -> 1048576,
			\optMod -> 2621440,

			\tab -> 16777217,

			\plus -> 61,
			\minus -> 45,

			\left -> 16777234,
			\right -> 16777236,
			\up -> 16777235,
			\down -> 16777237,

			\s -> 83,
			\z -> 90,
			\o -> 79,
			\q -> 81,
		]);
  }

	*new { arg ... keys;
		^dict.atAll(keys)
	}
}


SequencerCanvas : UserView {
	var <views;
	var zoom;
	var origin;

	var <bpm;
	
	var <>quantX, <>quantY;
	var <quantize;
	var <subdivisions;
	
	var <timingContextView;
	var <>grid;
	var <cursorView;

	*new { arg argParent, argBounds, subviews, quantX, quantY/*, shouldQuantizeX = true, shouldQuantizeY = true*/;
		var parent = argParent ?? Window.new('sequencer', Rect(1040, 455, 400, 400))
			.front;
		var bounds = argBounds ?? parent.view.bounds;

		^super.new(parent, bounds).init(subviews ? [], quantX, quantY);
	}

	*fromObjects { arg objects;
		var canvas = this.new();
		canvas.addObjects(objects);
		^canvas
	}

	clear {
		this.init([], quantX, quantY);
		this.refresh;
	}

	renderView {
		var bounds = this.parent.bounds;
		grid.draw(quantX, origin, bounds, zoom, subdivisions);
		
		timingContextView !? { |view|
			view.draw(bounds, subdivisions);
		};

		views.do(_.renderView(origin));
		cursorView.renderView(origin);
	}


	getTopView { arg x, y;
		^views.reverse.detect(_.contains(x@y))
	}

	deselectAll {
		views.do(_.unselect)
	}

	moveViews { arg x, y;
		var moveY = y * quantY * zoom.y;
		var moveX = x;
		// { [ 2097152, 16777234 ] } { this.snapMoveViews(-1 * quantX * zoom.x / subdivisions, 0) }
		// { [ 2097152, 16777236 ] } { this.snapMoveViews(quantX * zoom.x / subdivisions, 0) }
		// { [ 2097152, 16777235 ] } { this.snapMoveViews(0, -1 * moveY) }
		// { [ 2097152, 16777237 ] } { this.snapMoveViews(0, moveY) }
		if (quantize) {
			moveX = moveX * quantX * zoom.x / subdivisions;
		};
		
		views.do(_.moveAction(moveX, moveY, snap: quantize));
		cursorView.moveAction(moveX, moveY, snap: quantize);
	}

	snapMoveViews { arg x, y;
		views.do(_.snapMoveAction(x, y));
		cursorView.snapMoveAction(x, y);
	}

	moveOrigin { arg x, y;
		origin.x = origin.x + x;
		origin.y = origin.y + y;

		this.refresh;
	}


	setZoom { arg zoomX, zoomY;
		zoom.x = zoomX;
		zoom.y = zoomY;

		this.refresh;
	}

	zoomBy { arg zoomX, zoomY;
		views.do(_.zoomBy(zoomX, zoomY));
		cursorView.zoomBy(zoomX, zoomY);
		this.setZoom(zoom.x * zoomX, zoom.y * zoomY);	
	}

	cycleThroughViews {
		var selectedIndex = views.indexOf(_.selected) !? { |selectedIndex|
			selectedIndex = (selectedIndex + 1) % views.size;
		} ?? 0;
		views.do(_.unselect);
		views[selectedIndex].select;
		^views;
	}

	addObjects { arg newObjects;
		newObjects.do { |newObject|
			this.addObject(newObject);
		};

		this.deselectAll();
		this.refresh;
	}

	addObject { arg object;
		case
			{ object.type == 'sampleEvent' } {
				views = views.add(SequenceableSoundfileBlock(object))
			}
			{ object.type == 'sequencerEvent' } {
				views = views.add(SequenceableBlock(object))
			}
			{ object.type == 'timingContext' } {
				timingContextView = (
					bpm: object.bpm,
					draw: { arg ev, bounds, subdivisions;
						var color = Color.grey(0.7, 1);
						var point = bounds.leftBottom;
						var string = if (quantize, { "%bpm - Q".format(ev.bpm) }, { "%bpm".format(ev.bpm) });
						Pen.stringAtPoint(string, point + Point(10, -20), color: color);
					},
					setBPM: { arg ev, newBPM;
						Dispatcher((type: 'setBPM', payload: (id: object.id, bpm: newBPM)));
						ev.bpm = newBPM;
					}
				) 
			};
	}

	save { arg newFile = false;
		if (this.hasFocus) {
			Dispatcher((type: 'save', payload: (newFile: newFile)))
		}
	}

	open {
		if (this.hasFocus) {
			Dispatcher((type: 'open'))
		}
	}

	historyAction { arg action;
		if (this.hasFocus) {
			Dispatcher((type: action))
		}
	}

	bpm_ { arg newBpm;
		bpm = newBpm;
		quantX = SequenceableBlock.xFactor * 60 / bpm;
		this.refresh;
	}

	init { arg argviews, argQuantX, argQuantY;
		var xGrid, yGrid;

		quantize = true;
		views = argviews;
		zoom = 1@1;
		origin = 0@0;
		bpm = 60;


		quantX = argQuantX ?? SequenceableBlock.xFactor * 60 / bpm;
		quantY = argQuantY ?? SequenceableBlock.yFactor;
		subdivisions = 1;

		grid = SequencerGrid();
		cursorView = Cursor((timestamp: 0, channel: 0));
		
		this.resize = 5;
		this.parent.acceptsMouseOver_(true);

		this.drawFunc = {
			views = partition(views, { |m| m.selected.not }).flatten;
			this.renderView;
		};

		this.mouseDownAction = { |canvas, mouseX, mouseY, modifiers, buttonNumber, clickCount|
			var translatedMouse = Point(mouseX, mouseY) - origin;
			var x = translatedMouse.x;
			var y = translatedMouse.y;
			var topView = this.getTopView(x, y);

			this.deselectAll;

			topView !? { |topView|
				topView.mouseDownAction(x, y, modifiers, buttonNumber, clickCount);
			} ?? {
				// cursorView.position = Point(x, y.trunc(quantY * zoom.y));
				// cursorView.move
				if (buttonNumber == 1) { this.showMenu }
			};
			
			cursorView.mouseDownAction(x, y, modifiers, buttonNumber, clickCount);

			this.refresh;
		};

		this.mouseMoveAction = { |v, mouseX, mouseY, modifiers|
			var translatedMouse = Point(mouseX, mouseY) - origin;
			var x = translatedMouse.x;
			var y = translatedMouse.y;
			var passedInQuant;


			if (modifiers != 524288) {
				passedInQuant = quantX * zoom.x / subdivisions
			};

			views.do(_.mouseMoveAction(x, y, modifiers, passedInQuant));

			this.refresh;
		};

		this.mouseUpAction = { |canvas, mouseX, mouseY, modifiers, buttonNumber, clickCount|

			views.do(_.mouseUpAction);

			this.refresh;
		};

		this.keyDownAction = { |canvas, char, modifiers, unicode, keycode, key|
			var moveY = quantY * zoom.y;
			[modifiers, key].postln;
			switch ([modifiers, key])
				{ Keys(\cmdMod, \plus) }	{ this.zoomBy(1.05, 1) }
				{ Keys(\cmdMod, \minus) }	{ this.zoomBy(1.05.reciprocal, 1) }

				{ [ 1179648, 61 ] }	{ this.zoomBy(1.05, 1.05) }
				{ [ 1179648, 45 ] }	{ this.zoomBy(1.05.reciprocal, 1.05.reciprocal) }

				{ [ 1310720, 61 ] }	{ this.zoomBy(1, 1.05) }
				{ [ 1310720, 45 ] }	{ this.zoomBy(1, 1.05.reciprocal) }

				{ [ 2097152, 16777234 ] } { this.moveViews(-1, 0) } //left
				{ [ 2097152, 16777236 ] } { this.moveViews(1, 0) } 	//right
				{ [ 2097152, 16777235 ] } { this.moveViews(0, -1) } //up
				{ [ 2097152, 16777237 ] } { this.moveViews(0, 1) }  //down

				// { Keys(\optMod, \left) 	} { this.moveOrigin(-10, 0) } //left
				// { Keys(\optMod, \right) } { this.moveOrigin(10, 0) } //right
				// { Keys(\optMod, \up) 		} { this.moveOrigin(0, -10) } //up
				// { Keys(\optMod, \down) 	} { this.moveOrigin(0, 10) } //down
				// { Keys(\optMod, \left) 	} { this.moveViews(-1, 0)}
				// { Keys(\optMod, \right) } { this.moveViews(1, 0)}
				// { Keys(\optMod, \up) 		} { this.moveViews(0, -1 * moveY)}
				// { Keys(\optMod, \down) 	} { this.moveViews(0, moveY)}

				{ Keys(\noMod, \tab) } { this.cycleThroughViews }
				{ Keys(\cmdMod, \z) } { this.historyAction('undo') } //cmd -z
				{ [ 1179648, 90 ] } { this.historyAction('redo') } //cmd -shift -z
				
				{ Keys(\cmdMod, \s) } { this.save }
				{ [ 1179648, 83 ] } { this.save(true) }
				{ Keys(\cmdMod, \o) } { this.open }

				{ Keys(\noMod, \q) } { this.toggleQuantization }
				
				{ [ 1179648, 91 ] } { this.subdivisions_(subdivisions - 1) }
 				{ [ 1179648, 93 ] } { this.subdivisions_(subdivisions + 1) };
			this.refresh;
		};

		this.onResize = { |canvas|
			// var newBounds = canvas.parent.bounds;	
			// this.updateGrid(origin, newBounds, zoom)
		}


		^this
	}

	showMenu {
		^Menu(
			MenuAction("A", { "A selected".postln }),
   		MenuAction("B", { "B selected".postln }),
   		MenuAction("C", { "C selected".postln }),
		).front;
	}

	subdivisions_ { |newDivisions|
		subdivisions = max(1, newDivisions);
		this.refresh;
	}

	toggleQuantization {
		quantize = quantize.not;
	}
}

SequencerGrid {
	classvar mainGridColor;
	classvar subdivisionColor;
	
	*new {
		^super.new.init()
	}

	*initClass {
		mainGridColor = Color.grey(0.7, 1);
		subdivisionColor = Color.grey(0.7, 0.5);
	}

	init {}

	drawYGrid { arg origin, bounds, zoom;
		var gap = SequenceableBlock.yFactor * zoom.y;
		var yOffset = origin.y + (0 - origin.y).roundUp(gap);

		Pen.strokeColor_(mainGridColor);
		while ({ yOffset < bounds.height }) {
			Pen.line(Point(0, yOffset), Point(bounds.width, yOffset));
			Pen.stroke;
			yOffset = yOffset + gap;
		}
	}

	drawXGrid { arg quantX, origin, bounds, zoom, subdivisions;
		var gap = quantX * zoom.x;
		var initXOffset = origin.x + (0 - origin.x).roundUp(gap);
		var xOffset = initXOffset;
		
		var minorGap = gap / subdivisions;
		var initSubOffset = origin.x + (0 - origin.x).roundUp(minorGap); 
		var subOffset = initSubOffset;


		
		Pen.strokeColor_(mainGridColor);
		while ({ xOffset < bounds.width }) {
			Pen.line(Point(xOffset, 0), Point(xOffset, bounds.height));
			// Pen.stroke;
			xOffset = xOffset + gap;
		};


		Pen.strokeColor_(subdivisionColor);
		while ({ subOffset < bounds.width }) {
			Pen.line(Point(subOffset, 0), Point(subOffset, bounds.height));
			subOffset = subOffset + minorGap;
		}; 
		Pen.stroke;
	}

	draw { arg quantX, origin, bounds, zoom, subdivisions;
		this.drawYGrid(origin, bounds, zoom);
		this.drawXGrid(quantX, origin, bounds, zoom, subdivisions);
	}
}
