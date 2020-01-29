BaseMouseAction {

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