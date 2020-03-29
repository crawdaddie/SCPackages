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
		^canvas;
	}

	*fromStore { arg id;
		var store = id !? Store.at(id) ?? Store.getBase;
		var canvas = this.new(id);
		var items = Store.getItems(id);
		canvas.addObjects(items.values);
		^canvas;
	}

	fromStore { arg storeDict;
		var items;
		this.clear;
		id = storeDict['id'] !? { arg id; id } ?? nil;
		items = Store.getItems(id);
		this.addObjects(items);
		^this;
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
			{ object.type == 'store' } {
				var newView = StoreBlock(object).select();
				views = views.add(newView)
			}
			{ object.type == 'sampleEvent' } {
				var newView = SequenceableSoundfileBlock(object).select();
				views = views.add(newView)
			}
			{ object.type == 'sequencerEvent' } {
				var newView = SequenceableBlock(object).select();
				views = views.add(newView)
			}
			{ object.type == 'timingContext' } {
				timingContextView = TimingContextView(object);
			};
		

	}

	clear {
		this.init(id, [], quantX, quantY);
		this.refresh;
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

			action = case
				{ topView.isNil && Keys(\cmdMod).includes(modifiers) } {
					this.selectMouseDownAction(x, y, modifiers, buttonNumber, clickCount);
				}

				{ topView.isNil } {
					var action;
					if (buttonNumber == 1, {
						action = (mouseMoveAction: {}, mouseUpAction: { this.showMenuForBackgroundClick(x, y) });
					}, {
						this.deselectAll;
						action = this.selectMouseDownAction(x, y, modifiers, buttonNumber, clickCount);
					});
					action;
				}

				{ topView.notNil && clickCount == 2 } { (mouseMoveAction: {}, mouseUpAction: { topView.edit }) }
				
				{ topView.notNil && buttonNumber == 1 } {
					if (modifiers == 524288, {
							topView.select 
						}, {
							this.deselectAll;
							topView.select;
					});
					(mouseMoveAction: {}, mouseUpAction: { this.showMenuForItemClick(x, y) });
				}
				
				{ topView.notNil && Keys(\cmdMod).includes(modifiers) } {
					topView.selected = topView.selected.not;
					(mouseMoveAction: {}, mouseUpAction: {});
				}
				
				{ topView.notNil && modifiers == 524288 } {
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
			var updates; 
			
			action.mouseUpAction(x, y);

			action.updates !? { |updates|
				Dispatcher((type: 'moveObjects', payload: updates));
			};
			
			this.refresh;
		};

		this.keyDownAction = { |canvas, char, modifiers, unicode, keycode, key|
			if (this.hasFocus.not, {^nil});
			
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
	 			{ [ 1048576, 65 ] } { canvas.selectAll } // cmd - a
	 			{ [ 1048576, 67 ] } {
	 				Clipboard.clear;
	 				this.selectedViews.do { arg view; Clipboard.add(view.id);
	 				canvas.deselectAll;
	 			} } // cmd - c
	 			{ [ 1048576, 86 ] } { this.pasteObjects(cursorView.x, cursorView.y, Clipboard.normalizedItems) }; // cmd-v    


			this.refresh;
		};

		this.onResize = { |canvas|
		
		};

		Dispatcher.addListener('objectUpdated', { arg payload;
			// if ((payload.id == id) || (views.collect(_.id).includes(payload.id)), {
			// });
			this.refresh;
		});

		Dispatcher.addListener('objectAdded', { arg payload;
			var parentId = id ? 0;
			if (payload.parentId == parentId, {
				this.addObject(payload.object)	
			});

			this.refresh;
		});

		^this
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
		var updates;
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

		updates = selectedViews.collect { |view|
			view.moveBy(moveX, moveY);
			view.getUpdate;
		};
		Dispatcher((type: 'moveObjects', payload: updates));

		

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
		selectedBounds = views[selectedIndex].select.bounds;
		cursorView.moveTo(selectedBounds.left, selectedBounds.top);
		^views;
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
		y = max(0, y.trunc		(this.getTickY));
		
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
		var selectedViews, unselectedViews;
		var mostLeftView, mostTopView, verticalDifference, initialOrigin, initialRight, offsets, newCursorPosition, cursorOffset;
 
		// #selectedViews, unselectedViews = this.views.partition({ _.selected });
		
		selectedViews = this.selectedViews;
		unselectedViews = this.views.select({ arg view; view.selected.not });

		mostLeftView = (
			selectedViews.sort { arg viewA, viewB;
				viewA.bounds.left < viewB.bounds.left;
			}
		)[0];

		mostTopView = (
			selectedViews.sort { arg viewA, viewB;
				viewA.bounds.top < viewB.bounds.top;
			}
		)[0];

		verticalDifference = mostLeftView.bounds.top - mostTopView.bounds.top;

		initialOrigin = mostLeftView.bounds.origin;
		initialRight = mostLeftView.bounds.rightTop;


		offsets = selectedViews.collect({ arg view;
			view.bounds.origin - initialOrigin;
		});
		
		newCursorPosition = this.quantizePoint(Point(initialCursor.x, topView.bounds.top));
		cursorOffset = newCursorPosition - initialOrigin;

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
						var selectedViewsByChannel = selectedViews
							.groupBy({ | view | view.bounds.top.asInteger })
							.collect(_.sort({ |a, b| a.bounds.left < b.bounds.left }));

						var unselectedViewsByChannel =  unselectedViews
							.groupBy({ | view | view.bounds.top.asInteger })
							.collect(_.sort({ |a, b| a.bounds.left < b.bounds.left }));

						var overlapUpdates = [];

						object.updates = selectedViews.collect { |view|
							// var overlappedUpdates = view.findOverlapUpdates(unselectedViews);
							// overlappedUpdates.postln;
							view.setOpaque;
							view.getUpdate;
						};

						unselectedViewsByChannel.keysValuesDo { | channel, group |
							var selectedViewsGroup = selectedViewsByChannel[channel];

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
						object.updates = selectedViews.collect(_.getUpdate)
					}
				)
			},
			{ 'resizeRight' }, {
				(
					mouseMoveAction: { arg object, x, y;
						var newRight = x@y - (initialCursor - initialRight);
						var difference;					
						newRight = this.quantizePoint(newRight);
						difference = topView.bounds.rightTop - newRight;

						selectedViews.do(_.resizeRightBy(difference.x));
						// selectedViews.do(_.resizeRightToXValue(x))
					},
					mouseUpAction: { arg object, x, y;
						object.updates = selectedViews.collect(_.getUpdate)
					}
				)
			}
		)
	}

	showMenuForItemClick { arg x, y;
		var selection = this.selectedViews;

		^Menu(
			MenuAction("copy (cmd-c)", 	{
				// selection.postln;
				Clipboard.clear;
				selection.do { arg view; Clipboard.add(view.id) }
			}),
			MenuAction("paste (cmd-v)", {
				this.pasteObjects(x, y, Clipboard.normalizedItems);

			}),
   		MenuAction("cut (cmd-x)", 	{ "cut item".postln; }),
   		MenuAction("slice (cmd-d)", { "slice".postln 	}),
		).front;
	}

	showMenuForBackgroundClick { arg x, y;
		var selection = this.selectedViews;
		if (Clipboard.items.size > 0) {
			^Menu(
				MenuAction("paste (cmd-v)", {
					this.pasteObjects(x, y, Clipboard.normalizedItems);
					}),
			).front;
		}

	}
	pasteObjects { arg x, y, items;
		var newCursorPosition = this.quantizePoint(x@y);
		var absoluteTime = newCursorPosition.x / (SequenceableBlock.xFactor * zoom.x);
		var absoluteExtension = newCursorPosition.y / (SequenceableBlock.yFactor * zoom.y);
		items !? {
			Dispatcher((
				type: 'pasteObjects',
				payload: (
					x: absoluteTime,
					y: absoluteExtension,
					items: items,
					parentId: id
				))
			);
		};
		// Clipboard.clear;
	}

	subdivisions_ { |newDivisions|
		subdivisions = max(1, newDivisions);
		this.refresh;
	}

	toggleQuantization {
		quantize = quantize.not;
	}
}

