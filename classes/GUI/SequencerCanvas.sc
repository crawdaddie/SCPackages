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
      this.release;
			grid.onClose;
		};

		canvas.drawFunc = {
			grid.render(props);
			views.do(_.render());
		};

    this.listen(Topics.objectAdded, { arg payload;
      var item = payload.object;
      var viewClass = this.getItemEmbedView(item);
			var view = viewClass.new(item, props);
      views = views.add(view);
      canvas.refresh();
    });

    canvas.canReceiveDragHandler_({ arg view; true });
    canvas.receiveDragHandler_({ arg view, x, y;
      var params = this.getNewItemParams(x, y);
      var item = View.currentDrag;
      var newItem = this.getDragItemEvent(View.currentDrag, params);
      store.addObject(newItem);
    });
	}
  
  getDragItemEvent { arg currentDrag, params;
    if (currentDrag.class == String) {
      var mod = Mod(currentDrag);
      if (mod.soundfile.notNil, {^mod.getSFEvent.create(
        params.putAll(
          (soundfile: currentDrag)
        )
      )})
    };

    if (currentDrag.soundfile.notNil, { ^Mod(currentDrag.soundfile).getSFEvent.create(params.putAll((soundfile: currentDrag.soundfile))) }, { ^currentDrag });
  }

  getNewItemParams { arg x, y;
    var point = Point(x, y);
    var origin = props.origin;
		var zoom = props.zoom;
		var xFactor = Theme.horizontalUnit;
		var yFactor = Theme.verticalUnit;
    var itemParams;
		point = (point + Point(-1 * origin.x, -1 * origin.y)) * Point(zoom.x.reciprocal, zoom.y.reciprocal);

		itemParams = (
			beats: (point.x / xFactor).round(1),
			row: (point.y / yFactor).round(1),
			//dur: bounds.width / xFactor
		);
    ^itemParams;
  }

  getItemEmbedView { arg item;
    if (item.type == 'sampleEvent') {
      if (item.soundfile.notNil, { Mod(item.soundfile).setSFEvent(item) });
      ^SoundfileCanvasObject;
    };
    if (item.class == Store) {
      ^StoreCanvasObject;
    };
    if (item.type == 'sequencerEvent') {
      ^SequenceableCanvasObject;
    };
    ^CanvasObject;
  }

  listen { arg type, fn;
		Dispatcher.addListener(type, this, { arg payload;
			if (payload.storeId == store.id) {
				fn.value(payload)
			}
		})
	}

  getContextMenuActions { arg mouseAction, clipboard;
    ^[MenuAction("paste", {
      clipboard.do { arg view;
        var newView = view.copyTo(mouseAction.initialCanvasPosition, store);
      }
    })]
  }

	connectKeyActions {
    var keyActionManager = CanvasKeyActionManager(this, ProjectKeyActionManager());
	}

	connectMouseActions {
		var mouseAction;
    var clipboard;
		
		canvas.mouseDownAction = { arg view, mouseX, mouseY, modifiers, buttonNumber, clickCount;
      var initialCanvasPosition = Point(mouseX, mouseY); /* this is a position relative to the
      window or canvas bounds (eg canvas objects can compare it to their own renderBounds props) */
			var position = this.translateMousePosition(mouseX, mouseY); /* this is a position relative to the origin*/ 
			var notSelected, selected;
			#notSelected, selected = views.partition(_.contains(position).not);

      views = notSelected ++ selected;
      mouseAction = if (selected.size > 0, 
				{
          var baseAction;
          canvas.setContextMenuActions(
            MenuAction(
              "copy",
              { clipboard = selected }
            ),
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
          var baseAction = (
            initialPosition: position,
            initialCanvasPosition: initialCanvasPosition,
            //mouseMoveAction: { arg ev; selectionRectangle.onDrag(ev)},
            //mouseUpAction: { arg ev;
            //  selectionRectangle.onDragEnd(ev)
            //}
          );
          canvas.setContextMenuActions(
            *this.getContextMenuActions(baseAction, clipboard)
          );
          baseAction;
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
			var class = this.getItemEmbedView(item);
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
