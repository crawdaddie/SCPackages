SequencerTheme {
	classvar <>grey;
	classvar <>black;
	classvar <>darkGrey;
	*initClass {
		grey = [Color.grey(0.3, 1), Color.grey(0.5, 1), Color.grey(0.7, 1)];
		darkGrey = Color.grey(0.1, 1);
		black = Color.black;
	}
}

Keys {
	classvar dict;
	*initClass {
		dict = Dictionary.with(*[
			\noMod 		-> 0,
			\cmdMod 	-> 1048576,
			\shiftMod -> 1048576,
			\optMod 	-> 2621440,
			\ctrlMod 	-> 262144,

			\tab 			-> 16777217,

			\plus 		-> 61,
			\minus 		-> 45,

			\left 		-> 16777234,
			\right 		-> 16777236,
			\up 			-> 16777235,
			\down 		-> 16777237,

			\s 				-> 83,
			\z 				-> 90,
			\o 				-> 79,
			\q 				-> 81,
		]);
  }

	*new { arg ... keys;
		^dict.atAll(keys)
	}
}

SequencerWindowKeyActions {
	*canvasKeyDown { |canvas, char, modifiers, unicode, keycode, key|
		
		if (canvas.hasFocus.not, {^nil});

		switch ([modifiers, key])
			{ Keys(\cmdMod, \plus) }	{ canvas.zoomBy(1.05, 1) }
			{ Keys(\cmdMod, \minus) }	{ canvas.zoomBy(1.05.reciprocal, 1) }

			{ [ 1179648, 61 ] }	{ canvas.zoomBy(1.05, 1.05) }
			{ [ 1179648, 45 ] }	{ canvas.zoomBy(1.05.reciprocal, 1.05.reciprocal) }

			{ [ 1310720, 61 ] }	{ canvas.zoomBy(1, 1.05) }
			{ [ 1310720, 45 ] }	{ canvas.zoomBy(1, 1.05.reciprocal) }

			{ [ 2097152, 16777234 ] } { canvas.moveViews(-1, 0) } //left
			{ [ 2097152, 16777236 ] } { canvas.moveViews(1, 0) } 	//right
			{ [ 2097152, 16777235 ] } { canvas.moveViews(0, -1) } //up
			{ [ 2097152, 16777237 ] } { canvas.moveViews(0, 1) }  //down

			{ Keys(\optMod, \left) 	} { canvas.moveOrigin(-10, 0) } //left
			{ Keys(\optMod, \right) } { canvas.moveOrigin(10, 0) } //right
			{ Keys(\optMod, \up) 		} { canvas.moveOrigin(0, -10) } //up
			{ Keys(\optMod, \down) 	} { canvas.moveOrigin(0, 10) } //down

			{ Keys(\noMod, \tab) } { canvas.cycleThroughViews }
			{ Keys(\cmdMod, \z) } { Dispatcher((type: 'undo')) } //cmd -z
			{ [ 1179648, 90 ] } 	{ Dispatcher((type: 'redo')) } //cmd -shift -z
				
			{ Keys(\cmdMod, \s) } { Dispatcher((type: 'save', payload: (newFile: false))) } // cmd-s
			{ [ 1179648, 83 ] } 	{ Dispatcher((type: 'save', payload: (newFile: true))) } // cmd-shift-s
			{ Keys(\cmdMod, \o) } { Dispatcher((type: 'open')) } // cmd-o

			{ Keys(\noMod, \q) } { canvas.toggleQuantization } // Q
				
			{ [ 1179648, 91 ] } { canvas.subdivisions_(canvas.subdivisions - 1) } // cmd-shift-[
 			{ [ 1179648, 93 ] } { canvas.subdivisions_(canvas.subdivisions + 1) } // cmd-shift-]

 			{ [ 0, 16777216 ] } { canvas.deselectAll } // esc
 			{ [ 1048576, 65 ] } { canvas.selectAll }; // cmd - a 
		canvas.refresh;
	}
}


SequencerCanvas : UserView {
	var <views;
	var zoom;
	var origin;
	
	var <>quantX, <>quantY;
	var <quantize;
	var <subdivisions;
	
	var <timingContextView;
	var <>grid;
	var <cursorView;
	var <>selectionBounds;
	var mouseAction;
	var id;

	*new { arg argId, argParent, argBounds, subviews, quantX, quantY/*, shouldQuantizeX = true, shouldQuantizeY = true*/;
		var parent = argParent ?? Window.new('sequencer', Rect(1040, 455, 400, 400))
			.front;
		var bounds = argBounds ?? parent.view.bounds; 	

		^super.new(parent, bounds).init(argId, subviews ? [], quantX, quantY);
	}

	*fromObjects { arg objects;
		var aggregate = Store.createAggregate(*objects);
		var canvas = this.new(aggregate.id);
		canvas.addObjects(objects);
		^canvas
	}

	*fromAggregate { arg object;
		var canvas = this.new(object.id);
		var objects = Store.atAll(object.ids);
		canvas.addObjects(objects);
		canvas;
	}

	init { arg argId, argviews, argQuantX, argQuantY;
		var xGrid, yGrid;

		id = argId;

		quantize = true;
		views = argviews;
		zoom = 1@1;
		origin = 0@0;


		quantX = argQuantX ?? SequenceableBlock.xFactor;
		quantY = argQuantY ?? SequenceableBlock.yFactor;
		subdivisions = 1;

		grid = SequencerGrid();
		cursorView = Cursor((x: 0, y: 0));
		
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
			var cursorX = x;

			action = case
				{ topView.isNil && Keys(\cmdMod).includes(modifiers) } {
					this.selectMouseDownAction(x, y, modifiers, buttonNumber, clickCount);
				}
				{ topView.isNil } {
					this.deselectAll;
					this.selectMouseDownAction(x, y, modifiers, buttonNumber, clickCount);
				}
				{ topView.notNil && Keys(\cmdMod).includes(modifiers) } {
					topView.selected = topView.selected.not;
					(mouseMoveAction: {}, mouseUpAction: {});
				}
				{ topView.notNil && modifiers == 524288 } {
					buttonNumber.postln;
					topView.select;
					this.selectionMoveMouseDownAction(topView, x, y, modifiers, buttonNumber, clickCount);
				}
				{ topView.notNil } {
					this.deselectAll;
					topView.select;
					this.selectionMoveMouseDownAction(topView, x, y, modifiers, buttonNumber, clickCount);
				}
				// default action:
				{ (mouseMoveAction: {}, mouseUpAction: {}) };

			this.refresh;
		};

		this.mouseMoveAction = { |v, mouseX, mouseY, modifiers|
			var translatedMouse = Point(mouseX, mouseY) - origin;
			var x = translatedMouse.x;
			var y = translatedMouse.y;

			action.mouseMoveAction(x, y);
			this.refresh;
		};

		this.mouseUpAction = { |canvas, mouseX, mouseY, modifiers, buttonNumber, clickCount|
			var translatedMouse = Point(mouseX, mouseY) - origin;
			var x = translatedMouse.x;
			var y = translatedMouse.y;
			
			action.mouseUpAction(x, y);
			this.refresh;
		};

		this.keyDownAction = { |canvas, char, modifiers, unicode, keycode, key|
			SequencerWindowKeyActions.canvasKeyDown(canvas, char, modifiers, unicode, keycode, key);
		};

		this.onResize = { |canvas|
		
		};

		^this
	}


	clear {
		this.init([], quantX, quantY);
		this.refresh;
	}

	renderView {
		var parentBounds = this.parent.bounds;
		grid.draw(quantX, origin, parentBounds, zoom, subdivisions);
		
		timingContextView !? { |view|
			view.draw(parentBounds, subdivisions, quantize);
		};

		views.do({ |view|
			view.renderView(origin, parentBounds) 
		});

		cursorView.renderView(origin);

		if (selectionBounds.notNil) {
			var renderableSelectionBounds = selectionBounds.moveBy(origin.x, origin.y);
			Pen.addRect(renderableSelectionBounds);
   		Pen.strokeColor = SequencerTheme.darkGrey;
   		Pen.stroke;
		};
	}


	getTopView { arg x, y;
		^views.reverse.detect(_.contains(x@y))
	}

	selectAll {
		views.do(_.select)
	}

	deselectAll {
		views.do(_.unselect)
	}

	selectedViews {
		^views.select(_.selected);
	}

	moveViews { arg x, y;
		var moveY = y * quantY * zoom.y;
		var moveX = x;
		var selectedViews = this.selectedViews;
		var mostLeft;
		var tick;
		var newLeft;
		if (selectedViews.size > 0, {
			mostLeft = selectedViews[0].bounds.left;
			
			// don't bother sorting array, this is a little quicker 
			selectedViews.do { arg view;
				mostLeft = min(mostLeft, view.bounds.left);
			};
		
		}, {
			mostLeft = cursorView.bounds.left;
		});

		tick = this.getTick;


		if (quantize) {
			moveX = moveX * tick;
			newLeft = mostLeft.round(tick) + moveX;
			moveX = newLeft - mostLeft
		};

		selectedViews.do { |view|
			view.moveBy(moveX, moveY);
		};

		cursorView.moveBy(moveX, moveY);
	}

	moveOrigin { arg x, y;
		origin.x = origin.x + x;
		origin.y = origin.y + y;

		this.refresh;
	}

	setZoom { arg zoomX, zoomY;
		zoom.x = zoomX;
		zoom.y = zoomY;

		if (zoomX < 1) {
			subdivisions = zoomX.floorBase2;
		};

		this.refresh;
	}

	zoomBy { arg zoomX, zoomY;
		views.do(_.zoomBy(zoomX, zoomY));
		cursorView.zoomBy(zoomX, zoomY);
		this.setZoom(zoom.x * zoomX, zoom.y * zoomY);	
	}

	cycleThroughViews {
		var selectedBounds;
		var selectedIndex = views.indexOf(_.selected) !? { |selectedIndex|
			selectedIndex = (selectedIndex + 1) % views.size;
		} ?? 0;
		views.do(_.unselect);
		selectedBounds = views[selectedIndex].select;
		cursorView.moveTo(selectedBounds.left, selectedBounds.top);
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
				timingContextView = TimingContextView(object);
			};

		Dispatcher((type: 'addToAggregate', payload: (id: id, newObjectId: object.id)))
	}

	getTick { 
		^quantX * zoom.x / subdivisions
	}

	getTickY { 
		^quantY * zoom.y
	}


	quantizePoint { arg position;
		var x = position.x;
		var y = position.y;
		if (quantize) {
			x = max(0, x.round(this.getTick));
		};
		y = max(0, y.round(this.getTickY));
		
		^Point(x, y);
	}

	selectMouseDownAction { arg x, y;
		var actionObject;
		var initialMouse = x@y;
		
		selectionBounds = Rect.fromPoints(initialMouse, initialMouse);

		actionObject = (
			mouseMoveAction: { arg object, x, y;
				var minX = min(initialMouse.x, x);
				var minY = min(initialMouse.y, y);
				var maxX = max(initialMouse.x, x);
				var maxY = max(initialMouse.y, y);

				selectionBounds = Rect.fromPoints(Point(minX, minY), Point(maxX, maxY));
				views.do { | view |
					if (view.bounds.intersects(selectionBounds), {
						view.select;
					});
				};
			},
			mouseUpAction: { arg object, x, y;
				var newCursorPosition = this.quantizePoint(x@y);
				
				cursorView.moveTo(newCursorPosition.x, newCursorPosition.y);
				selectionBounds = nil;
			}
		);


		^actionObject;
	}


	selectionMoveMouseDownAction { |topView, x, y, modifiers, buttonNumber, clickCount|
		var actionType = topView.getAction(x, y);		
		var initialCursor = x@y;
		var selectedViews = this.selectedViews;
		var mostLeftView = (
			selectedViews.sort { arg viewA, viewB;
				viewA.bounds.left < viewB.bounds.left;
			}
		)[0];

		var mostTopView = (
			selectedViews.sort { arg viewA, viewB;
				viewA.bounds.top < viewB.bounds.top;
			}
		)[0];

		var verticalDifference = mostLeftView.bounds.top - mostTopView.bounds.top;

		var initialOrigin = mostLeftView.bounds.origin;
		var offsets = selectedViews.collect({ arg view;
			view.bounds.origin - initialOrigin;
		});
		
		var newCursorPosition = this.quantizePoint(Point(initialCursor.x, topView.bounds.top));
		var cursorOffset = newCursorPosition - initialOrigin;

		cursorView.moveTo(newCursorPosition.x, newCursorPosition.y);



		selectedViews.do { |view|
			view.setTransparent;
		};

		^switch(actionType,
			{ 'move' }, {

				(
					mouseMoveAction: { arg object, x, y;
						var newOrigin = x@y - (initialCursor - initialOrigin);
						
						newOrigin = this.quantizePoint(newOrigin);

						selectedViews.do { |view, i|
							var viewOffset = offsets[i]; 
							view.moveTo(newOrigin.x + viewOffset.x, newOrigin.y + viewOffset.y);					
						};

						cursorView.moveTo(newOrigin.x + cursorOffset.x, newOrigin.y + cursorOffset.y);
					},
					mouseUpAction: { arg object, x, y;
						selectedViews.do { |view|
							view.setOpaque;
						}
					}
				);
			},
			{ 'resizeLeft' }, {
				(
					mouseMoveAction: { arg object, x, y;
						var newOrigin = x@y - (initialCursor - initialOrigin);
						var difference;					
						newOrigin = this.quantizePoint(newOrigin);
						difference = topView.bounds.origin - newOrigin;

						selectedViews.do(_.resizeLeftBy(difference.x)); 

					},
					mouseUpAction: { arg object, x, y;
					}
				)
			},
			{ 'resizeRight' }, {
				(
					mouseMoveAction: { arg object, x, y;
						var difference = initialCursor.x - x;
						var quantX = this.getTick;

						selectedViews.do(_.resizeRightBy(difference, quantX));
					},
					mouseUpAction: { arg object, x, y;
					}
				)
			}
		)
	}
// difference = bounds.origin - newOrigin;
				// 	'resizeLeft', {
				// 	bounds.set(
				// 		bounds.left - difference.x,
				// 		bounds.top,
				// 		max(moveWidgetPixelsWidth, bounds.width + difference.x),
				// 		bounds.height);
				// },
				// 'resizeRight', {
				// 	bounds.set(
				// 		bounds.left,
				// 		bounds.top,
				// 		max(moveWidgetPixelsWidth, initialBounds.width - difference.x),
				// 		bounds.height);
				// }


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
	var <tick;
	
	*new {
		^super.new.init()
	}

	*initClass {
		mainGridColor = Color.grey(0.8, 1);
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
		// var tickNum = 0;
		tick = minorGap;




		
		Pen.strokeColor_(mainGridColor);
		while ({ xOffset < bounds.width }) {
			Pen.line(Point(xOffset, 0), Point(xOffset, bounds.height));
			// Pen.stroke;
			xOffset = xOffset + gap;
		};


		Pen.strokeColor_(subdivisionColor);
		while ({ subOffset < bounds.width }) {
			Pen.line(Point(subOffset, 0), Point(subOffset, bounds.height));
			
			// [tickNum, subOffset, minorGap].postln;
			subOffset = subOffset + minorGap;
			// tickNum = tickNum + 1;
		}; 
		Pen.stroke;
	}

	draw { arg quantX, origin, bounds, zoom, subdivisions;
		this.drawYGrid(origin, bounds, zoom);
		this.drawXGrid(quantX, origin, bounds, zoom, subdivisions);
	}
}
