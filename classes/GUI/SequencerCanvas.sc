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


SequencerCanvas : UserView {
	var <views;
	var zoom;
	var origin;
	
	var <>quantX, <>quantY;
	var <quantize;
	var <subdivisions;
	
	var <timingContextView;
	var <>grid;
	var <>transportLines;
	var <cursorView;
	var <>selectionBounds;
	var mouseAction;
	var <id;

	var <>timingOffset = 0;


	*new { arg id, argParent, argBounds, subviews, quantX, quantY/*, shouldQuantizeX = true, shouldQuantizeY = true*/;
		var parent = argParent ?? Window.new('sequencer', Rect(740, 455, 700, 400))
			.front;
		var bounds = argBounds ?? parent.view.bounds;
	
		^super.new(parent, bounds).init(id, subviews ? [], quantX, quantY);
	}

	*fromObjects { arg objects;
		var aggregate = Store.createAggregate(*objects);
		var canvas = this.new(aggregate.id);
		canvas.addObjects(objects);
		^canvas;
	}

	*fromStore { arg store;
		var canvas = this.new(store.id);
		var items = store.itemsFlat;
	  	
    ["from store", store, items].postln;
		canvas.addObjects(items);
		^canvas;
	}

	fromStore { arg store;
		var items;
		
		this.clear;
		id = store.id;
		items = store.itemsFlat;
		this.addObjects(items);
		^this;
	}

	addObjects { arg newObjects;
    ["add objects", newObjects].postln;
		newObjects.do { |newObject|
			this.addObject(newObject);
		};

		this.deselectAll();
		this.refresh;
	}

	addObject { arg object;
    ["add object", object].postln;
		views = views.add(object.getEmbedView(zoom););
	}

	clear {
		this.init(id, [], quantX, quantY);
		this.refresh;
	}

	init { arg argId, argviews, argQuantX, argQuantY;
		var xGrid, yGrid;
		var mouseAction;

		id = argId;
		timingOffset = Store.at(id).getOffset;
		
		quantize = true;
		views = argviews;
		zoom = 1@1;
		origin = 0@0;


		quantX = argQuantX ?? CanvasBlockBase.xFactor;
		quantY = argQuantY ?? CanvasBlockBase.yFactor;
		subdivisions = 1;

		grid = SequencerGrid();
		transportLines = TransportLines(
			id,
			this
		);
		cursorView = Cursor((x: 0, y: 0), zoom);
		
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

			var actions = [];

			topView !? {
				actions = actions.add(topView.getMouseAction(x, y, modifiers, buttonNumber, clickCount)) 
			};
			actions.add(cursorView.getMouseAction(x, y, modifiers, buttonNumber, clickCount));
			actions.addAll(this.selectedViews.collect(_.getMouseAction(x, y, modifiers, buttonNumber, clickCount)));


			mouseAction = (
				mouseMoveAction: { arg x, y;
					actions.do { arg action;
						action.mouseMoveAction(x, y);
					}
				},
				mouseUpAction: { arg x, y;
					var updates = actions.collect { arg action;
						action.mouseUpAction(x, y)
					};
					updates.select(_.notNil);
				});

			this.refresh;
		};

		this.mouseMoveAction = { |v, mouseX, mouseY, modifiers|
			var translatedMouse = Point(mouseX, mouseY) - origin;
			var x = translatedMouse.x;
			var y = translatedMouse.y;

			mouseAction.mouseMoveAction(x, y);
			this.refresh;
		};

		this.mouseUpAction = { |canvas, mouseX, mouseY, modifiers, buttonNumber, clickCount|
			var translatedMouse = Point(mouseX, mouseY) - origin;
			var x = translatedMouse.x;
			var y = translatedMouse.y;
			var updates; 
			
			updates = mouseAction.mouseUpAction(x, y);

			updates !? { |updates|
				Dispatcher((
					type: 'moveObjects',
					payload: (
						updates: updates,
						storeId: id
					)
				));
			};
			
			this.refresh;
		};

		this.keyDownAction = { arg ...keyArgs;
			if (this.hasFocus) {
				KeyActionManager.keyDownHandler(*keyArgs);
				this.refresh;
			}
		};


		this.keyUpAction = { arg ...keyArgs;
			KeyActionManager.keyUpHandler(*keyArgs)
		};

		this.onResize = { |canvas|
		
		};

		Dispatcher.connectObject(
			this,
			'objectUpdated',
			'objectDeleted',
			'objectAdded',
		);

		this.onClose = { arg view;
			views.do { arg view;
				Dispatcher.removeListenersForObject(view);
			};
			Dispatcher.removeListenersForObject(grid);
			Dispatcher.removeListenersForObject(transportLines);
		};

		^this
	}

	// connected to Dispatcher signals
	objectAdded { arg payload, canvas;
		if (payload.storeId == id) {
			canvas.addObject(payload.object)	
		};
		canvas.refresh;
	}

	objectDeleted { arg payload;
		if (payload.storeId == id) {
			views = views.select({ arg view;
				var shouldDeleteView = view.id == payload.objectId;
				if (shouldDeleteView) {
					Dispatcher.removeListenersForObject(view);
				};
				shouldDeleteView.not;
			});
		}
	}

	objectUpdated { arg payload;
		if (payload.storeId == id) {
			var store = Store.at(id);
			timingOffset = store.getOffset;
		};
		this.refresh;
	}

	renderView {
		var parentBounds = this.parent.bounds;

		
		grid.renderView(
			quantX,
			origin,
			timingOffset,
			parentBounds,
			zoom,
			subdivisions
		);
		
		transportLines.renderView(
			quantX,
			origin,
			timingOffset,
			parentBounds,
			zoom,
			subdivisions
		);
		
		timingContextView !? { |view|
			view.draw(parentBounds, subdivisions, quantize);
		};

		views.do({ |view|
			view.renderView(origin, parentBounds) 
		});

		cursorView.renderView(origin, parentBounds);

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

	moveViewsHandler { arg x, y;
		var moveY = y * quantY * zoom.y;
		var moveX = x;
		var selectedViews = this.selectedViews;
		var mostLeft;
		var mostTop;
		var tick = this.getTick;
		var newLeft;
		var keyUpActions;
		// if (selectedViews.size == 0, {^cursorView.move}, {});

		if (selectedViews.size > 0, {
			mostLeft = selectedViews[0].bounds.left;
			mostTop = selectedViews[0].bounds.top;

			// don't bother sorting array, this is a little quicker 
			selectedViews.do { arg view;
				mostLeft = min(mostLeft, view.bounds.left);
				mostTop = min(mostTop, view.bounds.top);
			};

			cursorView.moveTo(mostLeft, mostTop);
		
		}, {
			mostLeft = cursorView.bounds.left;
			mostTop = cursorView.bounds.top;
		});

		if (quantize) {
			moveX = moveX * tick;
			newLeft = mostLeft.round(tick) + moveX;
			moveX = newLeft - mostLeft
		};

		selectedViews.do(_.moveBy(moveX, moveY));
		cursorView.moveBy(moveX, moveY);
		^{
			selectedViews.collect(_.getUpdate())
		}
	}

	moveCursorHandler { arg x, y;
		var moveY = y * quantY * zoom.y;
		var moveX = x;
		var mostLeft = cursorView.bounds.left;
		var mostTop = cursorView.bounds.top;
		var newLeft;
		var tick = this.getTick;

		if (quantize) {
			moveX = moveX * tick;
			newLeft = mostLeft.round(tick) + moveX;
			moveX = newLeft - mostLeft
		};
		cursorView.moveBy(moveX, moveY);
	}

	selectViewsUnderCursor {
		var cursorBounds = cursorView.bounds;
		views.do { |view|
			if (view.intersects(cursorBounds)) { view.select } { view.unselect };
		}
	}

	toggleSelectViewsUnderCursor {
		var cursorBounds = cursorView.bounds;
		views.do { |view|
			if (view.intersects(cursorBounds)) { view.selected = view.selected.not };
		}
	}

	extendSelectionHandler { arg x, y;
		var moveY = y * quantY * zoom.y;
		var moveX = x;
		var mostLeft = cursorView.bounds.left;
		var mostTop = cursorView.bounds.top;
		var tick = this.getTick;
		var newLeft;
		var newBounds;
		if (quantize) {
			moveX = moveX * tick;
			newLeft = mostLeft.round(tick) + moveX;
			moveX = newLeft - mostLeft
		};

		cursorView.extendSelectionHandler(moveX, moveY);
		this.selectViewsUnderCursor;

		// callback to call when the KeyActionManager gives up its action
		^{
			cursorView.resetSelection;
			this.refresh;
		}

	}


	getMoveActionOptions { arg x, y;
		var moveY = y * quantY * zoom.y;
		var moveX = x;
		var selectedViews = this.selectedViews;
		var mostLeft;
		var mostTop;
		var tick;
		var newLeft;
		var keyUpActions;
		// if (selectedViews.size == 0, {^cursorView.move}, {});

		if (selectedViews.size > 0, {
			mostLeft = selectedViews[0].bounds.left;
			mostTop = selectedViews[0].bounds.top;

			// don't bother sorting array, this is a little quicker 
			selectedViews.do { arg view;
				mostLeft = min(mostLeft, view.bounds.left);
				mostTop = min(mostTop, view.bounds.top);
			};

			cursorView.moveTo(mostLeft, mostTop);
		
		}, {
			mostLeft = cursorView.bounds.left;
			mostTop = cursorView.bounds.top;
		});

		tick = this.getTick;

		if (quantize) {
			moveX = moveX * tick;
			newLeft = mostLeft.round(tick) + moveX;
			moveX = newLeft - mostLeft
		};

		^(moveX: moveX, moveY: moveY)
	}

	moveOrigin { arg x, y;
		origin.x = origin.x + x;
		origin.y = origin.y + y;

		this.refresh;
	}

	zoomBy { arg x, y;
		
		views.do(_.zoomBy(x, y));
		cursorView.zoomBy(x, y);
		
		zoom.x = zoom.x * x;
		zoom.y = zoom.y * y;
		
		if (zoom.x < 1) {
			subdivisions = zoom.x.floorBase2;
		};
		
		this.refresh;
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
		y = max(0, y.trunc(this.getTickY));
		
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
						var selectedViewsByrow = selectedViews
							.groupBy({ | view | view.bounds.top.asInteger })
							.collect(_.sort({ |a, b| a.bounds.left < b.bounds.left }));

						var unselectedViewsByrow =  unselectedViews
							.groupBy({ | view | view.bounds.top.asInteger })
							.collect(_.sort({ |a, b| a.bounds.left < b.bounds.left }));

						var overlapUpdates = [];

						object.updates = selectedViews.collect { |view|
							// var overlappedUpdates = view.findOverlapUpdates(unselectedViews);
							// overlappedUpdates.postln;
							view.setOpaque;
							view.getUpdate;
						};

						unselectedViewsByrow.keysValuesDo { | row, group |
							var selectedViewsGroup = selectedViewsByrow[row];

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
	pasteObjects { arg items;
		var x = cursorView.x;
		var y = cursorView.y;
		var newCursorPosition = this.quantizePoint(x@y);
		var absoluteTime = newCursorPosition.x / (Theme.horizontalUnit * zoom.x);
		var absoluteExtension = newCursorPosition.y / (Theme.verticalUnit * zoom.y);
		items !? {
			Dispatcher((
				type: 'pasteObjects',
				payload: (
					x: absoluteTime,
					y: absoluteExtension,
					items: items,
					storeId: id
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
		if (quantize, {
			"quantization on".postln;
		}, {
			"quantization off".postln;
		});
	}

	renderText { arg text;
		cursorView.renderText(text);
	}

	openTextView {
		^CmdLineView();
	}

	editSelection {
		this.selectedViews.do { arg view;
			view.tryPerform('edit');
		}
	}

	deleteSelection {
		Dispatcher((
			type: 'deleteObjects',
			payload: (
				storeId: id,
				toDelete: this.selectedViews.collect(_.id);
				)
			)
		);
	}

	edit {
		var object = Store.at(id);
		object.getModule !? { arg mod;
			// mod.postln;
			mod.open;
		};

		object.tryPerform('getView');
	}

	playStore {
		var startPos = cursorView.x / (Theme.horizontalUnit * zoom.x);
		Dispatcher((type: 'playStore', payload: (storeId: id, startPos: startPos)))
	}

}

