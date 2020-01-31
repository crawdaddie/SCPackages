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

MoveTopView {
	var view;
	*new { arg view, x, y, modifiers, buttonNumber, clickCount;
		^super.new.init(view, x, y, modifiers, buttonNumber, clickCount);
	}

	init { arg view, x, y, modifiers, buttonNumber, clickCount;
		view = view;
		this.mouseDownAction(x, y, modifiers, buttonNumber, clickCount);
	}

	mouseDownAction { arg x, y, modifiers, buttonNumber, clickCount;
		view.mouseDownAction(x, y, modifiers, buttonNumber, clickCount);

	}

	mouseMoveAction {
		
	}

	mouseUpAction {

	}
}

MoveViews {
	
}
