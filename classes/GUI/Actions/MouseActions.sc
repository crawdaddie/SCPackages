BaseMouseAction {
	var initialCursor;

	mouseDownAction { arg x, y, modifiers, buttonNumber, clickCount, quant;
		// abstract method
		this.subclassResponsibility(thisMethod);
	}

	mouseMoveAction { arg x, y, modifiers, quant;
		// abstract method
		this.subclassResponsibility(thisMethod);
	}

	mouseUpAction { arg x, y, modifiers, quant;
		// abstract method
		this.subclassResponsibility(thisMethod);
	}
}

MoveViewsAction {
	var views;
	var cursor;
	var canvas;
	*new { arg canvas, x, y, modifiers, buttonNumber, clickCount;
		^super.new.init(canvas, x, y, modifiers, buttonNumber, clickCount);
	}

	init { arg argCanvas, x, y, modifiers, buttonNumber, clickCount;
		canvas = argCanvas;
		this.mouseDownAction(x, y, modifiers, buttonNumber, clickCount);
	}

	mouseDownAction { arg x, y, modifiers, buttonNumber, clickCount;
		// view.mouseDownAction(x, y, modifiers, buttonNumber, clickCount);

	}

	mouseMoveAction {
		
	}

	mouseUpAction {

	}
}

SelectionAction {
	var canvas;
	var views;
	var cursorView;
	var initialMouse;
	var selectionBounds;

	var initialMouse;
	*new { arg canvas, x, y, modifiers, buttonNumber, clickCount;
		^super.new.init(canvas, x, y, modifiers, buttonNumber, clickCount);
	}

	init { arg argCanvas, x, y, modifiers, buttonNumber, clickCount;
		canvas = argCanvas;
		views = canvas.views;
		this.mouseDownAction(x, y, modifiers, buttonNumber, clickCount);
	}

	mouseDownAction { arg x, y, modifiers, buttonNumber, clickCount;
		initialMouse = x@y;
		canvas.selectionBounds = Rect.fromPoints(initialMouse, initialMouse);
	}

	mouseMoveAction { arg x, y;
		var minX = min(initialMouse.x, x);
		var minY = min(initialMouse.y, y);
		var maxX = max(initialMouse.x, x);
		var maxY = max(initialMouse.y, y);

		canvas.selectionBounds = Rect.fromPoints(Point(minX, minY), Point(maxX, maxY));
		canvas.selectionBounds.postln;
		views.do { | view |
			if (view.bounds.intersects(canvas.selectionBounds), { view.select });
		};
	}

	mouseUpAction {
		canvas.selectionBounds = nil;
	}
}


