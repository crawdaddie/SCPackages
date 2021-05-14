SequencerCanvas {
	var <canvas;
	var props; 

	/**
	 * (
	 *   quantX: 100, // number in px
	 *	 origin: 0@0, // point - represents where the 'current' viewport is
	 *	 timingOffset: 0, // at which 'global' offset is the represented collection of events
	 *	 zoom: 1@1, // point
	 * );
	 */

	// child canvas objects
	var grid;
	var views;

	*new { arg store;
		^super.new.init(store);
	}

  front {
    canvas.front;
  }

	init { arg store;
		var parent, bounds;
		var title = format("sequencer - %", store.id);

		if (store.id == 1000) {
			title = title ++ " (top level)"
		};

		parent = Window.new(title, Rect(740, 455, 700, 400)).front;
		bounds = parent.view.bounds;
		canvas = UserView(parent, bounds);
		canvas.resize = 5;
		parent.acceptsMouseOver_(true);

		props = Props((
			quantX: 100,
			origin: 0@0,
			timingOffset: 0,
			zoom: 1@1,
			redraw: { canvas.refresh },
			canvasBounds: canvas.parent.bounds,
		));

		canvas.onResize = { arg c;
			props.canvasBounds = c.parent.bounds;
		};
		
		this.addChildViews(store.itemsFlat);

		this.connectKeyActions;
		this.connectMouseActions;

		canvas.onClose = { arg view;
			views.do(_.onClose);
			grid.onClose;
      this.release;
		};

		canvas.drawFunc = {
			grid.render(props);
			views.do(_.render());
		};
    
	}
  getContextMenuActions {
    ^[]
  }

	connectKeyActions {
		var keyAction;
		canvas.keyDownAction = { arg canvas, char, modifiers, unicode, keycode, key;
			if (canvas.hasFocus) {
				[modifiers, key].postln;
				switch ([modifiers, key]) 
					{ [ 393216, 95 ] } { this.zoomBy(1.05.reciprocal, 1.05.reciprocal) } // cmd-shift-minus
					{ [ 393216, 43 ] } { this.zoomBy(1.05, 1.05) } // cmd-shift-plus
					{ [ 524288, 72 ] } { this.moveOrigin(-10, 0) } // option-h
					{ [ 524288, 76 ] } { this.moveOrigin(10, 0) } // option-right
					{ [ 524288, 75 ] } { this.moveOrigin(0, -10) } // option-up
					{ [ 524288, 74 ] } { this.moveOrigin(0, 10) } // option-down
				;
			}
		};

		canvas.keyUpAction = { arg canvas, char, modifiers, unicode, keycode, key;
			keyAction = nil;
		};
	}

	connectMouseActions {
		var mouseAction;
		
		canvas.mouseDownAction = { arg view, mouseX, mouseY, modifiers, buttonNumber, clickCount;
			var position = this.translateMousePosition(mouseX, mouseY);
			var notSelected, selected;
			#notSelected, selected = views.partition(_.contains(position).not);
			views = notSelected ++ selected;

      if (selected.size > 0, {
        canvas.setContextMenuActions(
          *selected.last.getContextMenuActions()
        );
      }, { canvas.setContextMenuActions(*this.getContextMenuActions()) });
			

      mouseAction = selected
				!? {
					selected.do(_.select);

					(
						initialPosition: position.x@position.y,
						mouseMoveAction: { arg ev; selected.collect(_.onDrag(ev)) },
						mouseUpAction: { arg ev;
							selected.do(_.unselect);
							selected.collect(_.onDragEnd(ev))
						},
					)
				}
				?? ( initialPosition: position );
		};

		canvas.mouseMoveAction = { arg view, mouseX, mouseY, modifiers, buttonNumber, clickCount;
			var position = this.translateMousePosition(mouseX, mouseY);
			mouseAction.position = position;
			mouseAction !? { mouseAction.mouseMoveAction }
		};

		canvas.mouseUpAction = { arg view, mouseX, mouseY, modifiers, buttonNumber, clickCount;
			var position = this.translateMousePosition(mouseX, mouseY);
			mouseAction !? { mouseAction.mouseUpAction };
		};
	}

	translateMousePosition { arg mouseX, mouseY;
		var translatedMouse = Point(mouseX, mouseY) - props.origin;
		^translatedMouse;
	}

	addChildViews { arg items;
		grid = SequencerGrid();
		views = items.collect({ arg item;
			var class = item.embedView ?? CanvasObject;
			class.new(item, props);
		});
	}

	zoomBy { arg x = 1, y = 1;
		var zoomX = props.zoom.x;
		var zoomY = props.zoom.y;
		props.zoom = (zoomX * x)@(zoomY * y);
		canvas.refresh;	
	}

	moveOrigin { arg x, y;
		props.origin = (props.origin.x + x)@(props.origin.y + y);
		canvas.refresh;
	}
}
