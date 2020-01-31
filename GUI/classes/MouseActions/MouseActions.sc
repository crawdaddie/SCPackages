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
	var cursor
	*new { arg views, cursor, x, y, modifiers, buttonNumber, clickCount;
		^super.new.init(views, cursor, x, y, modifiers, buttonNumber, clickCount);
	}

	init { arg views, cursor, x, y, modifiers, buttonNumber, clickCount;
		views = views;
		cursor = cursor;
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
	var views;
	var cursor
	*new { arg views, cursor, x, y, modifiers, buttonNumber, clickCount;
		^super.new.init(views, cursor, x, y, modifiers, buttonNumber, clickCount);
	}

	init { arg views, cursor, x, y, modifiers, buttonNumber, clickCount;
		views = views;
		cursor = cursor;
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


