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

  var store;

	*new { arg store;
		^super.new.init(store);
	}

  front {
    canvas.front;
  }

	init { arg aStore;
		var parent, bounds;
		var title = format("sequencer - %", aStore.id);
    store = aStore;

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
    var keyActionManager = KeyActionManager(this);
	}

	connectMouseActions {
		var mouseAction;
		
		canvas.mouseDownAction = { arg view, mouseX, mouseY, modifiers, buttonNumber, clickCount;
      var initialCanvasPosition = Point(mouseX, mouseY); /* this is a position relative to the window or canvas bounds (eg canvas objects can compare it to their own renderBounds props) */
			var position = this.translateMousePosition(mouseX, mouseY); /* this is a position relative to the origin*/ 
			var notSelected, selected;
			#notSelected, selected = views.partition(_.contains(position).not);

      views = notSelected ++ selected;
      mouseAction = if (selected.size > 0, 
				{
          var baseAction;
          canvas.setContextMenuActions(
            *selected.last.getContextMenuActions()
          );

          selected.do({ arg view;
            view.select;
            view.onDragStart;
          });
          baseAction = ( modifiers: modifiers,
						initialPosition: position,
            initialCanvasPosition: initialCanvasPosition,
            selected: selected,
						mouseMoveAction: { arg ev; ev.selected.collect(_.onDrag(ev)) },
						mouseUpAction: { arg ev;
							ev.selected.do(_.unselect);
							ev.selected.collect(_.onDragEnd(ev))
						},
					);
          selected[0] !? baseAction.putAll(selected[0].getMouseAction(baseAction)) ?? baseAction;
				}, {
          canvas.setContextMenuActions(*this.getContextMenuActions());
          (
            initialPosition: position,
            //mouseMoveAction: { arg ev; selectionRectangle.onDrag(ev)},
            //mouseUpAction: { arg ev;
            //  selectionRectangle.onDragEnd(ev)
            //}
          )
        });
		};

		canvas.mouseMoveAction = { arg view, mouseX, mouseY, modifiers, buttonNumber, clickCount;
			var position = this.translateMousePosition(mouseX, mouseY);
			mouseAction.position = position;
    
      mouseAction.mouseDelta = position - mouseAction.initialPosition;
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

  play {
    store.play;
  }
}
