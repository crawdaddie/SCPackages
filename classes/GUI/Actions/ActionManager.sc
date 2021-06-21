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


MouseAction {
	var objects;

	*new { | ...objects |
		^super.new.init(objects);
	}

	init { | argobjects |
		objects = argobjects;

	}
}


		// switch ([modifiers, key])
		// 	{ Keys(\cmdMod, \plus) }	{ canvas.zoomBy(1.05, 1) }
		// 	{ Keys(\cmdMod, \minus) }	{ canvas.zoomBy(1.05.reciprocal, 1) }

		// 	{ [ 1179648, 61 ] }	{ canvas.zoomBy(1.05, 1.05) }
		// 	{ [ 1179648, 45 ] }	{ canvas.zoomBy(1.05.reciprocal, 1.05.reciprocal) }

		// 	{ [ 1310720, 61 ] }	{ canvas.zoomBy(1, 1.05) }
		// 	{ [ 1310720, 45 ] }	{ canvas.zoomBy(1, 1.05.reciprocal) }

		// 	{ Keys(\optMod, \left) 	} { canvas.moveOrigin(-10, 0) } //left
		// 	{ Keys(\optMod, \right) } { canvas.moveOrigin(10, 0) } //right
		// 	{ Keys(\optMod, \up) 		} { canvas.moveOrigin(0, -10) } //up
		// 	{ Keys(\optMod, \down) 	} { canvas.moveOrigin(0, 10) } //down

		// 	{ Keys(\cmdMod, \z) } { Dispatcher((type: 'undo')) } //cmd -z
		// 	{ [ 1179648, 90 ] } 	{ Dispatcher((type: 'redo')) } //cmd -shift -z
					
		// 	{ Keys(\cmdMod, \s) } { Dispatcher((type: 'save', payload: (newFile: false))) } // cmd-s
		// 	{ [ 1179648, 83 ] } 	{ Dispatcher((type: 'save', payload: (newFile: true))) } // cmd-shift-s
		// 	{ Keys(\cmdMod, \o) } { Dispatcher((type: 'open')) } // cmd-o	

		// 	{ Keys(\noMod, \q) } { canvas.toggleQuantization } // Q
					
		// 	{ [ 1179648, 91 ] } { canvas.subdivisions_(canvas.subdivisions - 1) } // cmd-shift-[
	 // 		{ [ 1179648, 93 ] } { canvas.subdivisions_(canvas.subdivisions + 1) } // cmd-shift-]

		// 	{ Keys(\noMod, \tab) } { canvas.cycleThroughViews }
	 // 		{ [ 0, 16777216 ] } { canvas.deselectAll } // esc
	 // 		{ [ 1048576, 65 ] } { canvas.selectAll } // cmd - a
				
		// 	{ [ 2097152, 16777234 ] } { keyUpVar = canvas.moveViewsHandler(-1, 0, modifiers) } //left
		// 	{ [ 2097152, 16777236 ] } { keyUpVar = canvas.moveViewsHandler(1, 0, modifiers) } 	//right
		// 	{ [ 2097152, 16777235 ] } { keyUpVar = canvas.moveViewsHandler(0, -1, modifiers) } //up
		// 	{ [ 2097152, 16777237 ] } { keyUpVar = canvas.moveViewsHandler(0, 1, modifiers) }  //down

		// 	{ [ 2228224, 16777234 ] } { keyUpVar = canvas.extendSelectionHandler(-1, 0) } //shift-left
		// 	{ [ 2228224, 16777236 ] } { keyUpVar = canvas.extendSelectionHandler(1,  0) } //shift-right
		// 	{ [ 2228224, 16777235 ] } { keyUpVar = canvas.extendSelectionHandler(0, -1) } //shift-up
		// 	{ [ 2228224, 16777237 ] } { keyUpVar = canvas.extendSelectionHandler(0,  1) } //shift-down
		// 	;


	 // 		// { [ 1048576, 67 ] } {
	 // 		// 	keyUpHandler = {
	 // 		// 		Clipboard.clear;
	 // 		// 		this.selectedViews.do { arg view; Clipboard.add(view.id) };
	 // 		// 		canvas.deselectAll;
	 // 		// 	}
	 // 		// } // cmd - c
	 // 		// { [ 1048576, 86 ] } {
	 // 		// 	keyUpHandler = {
	 // 		// 		this.pasteObjects(cursorView.x, cursorView.y, Clipboard.normalizedItems) 
	 // 		// 	} 
	 // 		// }; // cmd-v

		// // canvas.keyDownAction = { |canvas, char, modifiers, unicode, keycode, key|
		// // 	"deferred k down action".postln;
		// // }
		
		// // ^{ |canvas, char, modifiers, unicode, keycode, key|
		// // 	"deferred k up action".postln;
		// // 	canvas.resetKey
		// // }
KeyActionManager_ {
	classvar <>keyUpVar;
	classvar <>keyDownVar;

	*keyDownHandler { arg canvas, char, modifiers, unicode, keycode, key;
		keyDownVar !? {
			^keyDownVar.value(canvas, char, modifiers, unicode, keycode, key);
		};

		^switch ([modifiers, key])
			{ [ 131072, 16777248 ] } {
				this.shiftActionManager(canvas, char, modifiers, unicode, keycode, key);
			} // shift

			{ [ 1048576, 16777249 ] } {
			 this.cmdActionManager(canvas, char, modifiers, unicode, keycode, key);
			} // cmd

			
			{ Keys(\cmdMod, \plus) }	{ canvas.zoomBy(1.05, 1) }
			{ Keys(\cmdMod, \minus) }	{ canvas.zoomBy(1.05.reciprocal, 1) }

			{ [ 1179648, 61 ] }	{ canvas.zoomBy(1.05, 1.05) }
			{ [ 1179648, 45 ] }	{ canvas.zoomBy(1.05.reciprocal, 1.05.reciprocal) }

			{ [ 1310720, 61 ] }	{ canvas.zoomBy(1, 1.05) }
			{ [ 1310720, 45 ] }	{ canvas.zoomBy(1, 1.05.reciprocal) }

			{ Keys(\optMod, \left) 	} { canvas.moveOrigin(-10, 0) } //left
			{ Keys(\optMod, \right) } { canvas.moveOrigin(10, 0) } //right
			{ Keys(\optMod, \up) 		} { canvas.moveOrigin(0, -10) } //up
			{ Keys(\optMod, \down) 	} { canvas.moveOrigin(0, 10) } //down

			{ Keys(\noMod, \q) } { canvas.toggleQuantization } // Q
					

			{ Keys(\noMod, \tab) } { canvas.cycleThroughViews }
	 		{ [ 0, 16777216 ] } { this.reset; } // esc

	 		{ [ 2097152, 16777234 ] } { canvas.moveCursorHandler(-1, 0); } //left
			{ [ 2097152, 16777236 ] } { canvas.moveCursorHandler(1,  0); } //right
			{ [ 2097152, 16777235 ] } { canvas.moveCursorHandler(0, -1); } //up
			{ [ 2097152, 16777237 ] } { canvas.moveCursorHandler(0,  1); } //down
			{ [ 0, 32 ]} { canvas.playStore; }
		;
	}

	*keyUpHandler { arg ...keyArgs;
		keyUpVar !? {
			keyUpVar.value(*keyArgs);
		}
	}

	*reset {
		keyDownVar = nil;
		keyUpVar = nil;
	}

	*shiftActionManager { arg canvas, char, modifiers, unicode, keycode, key;
		var initModKey = [modifiers, key];
		var result;

		keyDownVar = { |canvas, char, modifiers, unicode, keycode, key|
			result = switch ([modifiers, key])
				{ [ 2228224, 16777234 ] } { canvas.extendSelectionHandler(-1, 0); } //left
				{ [ 2228224, 16777236 ] } { canvas.extendSelectionHandler(1,  0); } //right
				{ [ 2228224, 16777235 ] } { canvas.extendSelectionHandler(0, -1); } //up
				{ [ 2228224, 16777237 ] } { canvas.extendSelectionHandler(0,  1); } //down
			;
			
		};

		keyUpVar = { |canvas, char, modifiers, unicode, keycode, key|
			if (this.shouldRelease([modifiers, key], initModKey)) {
				result.tryEval;
				this.reset;
			}
		}
	}

	*cmdActionManager { arg canvas, char, modifiers, unicode, keycode, key;
		var initModKey = [modifiers, key];
		var result;
		
		if (canvas.selectedViews.size > 1) {
			canvas.deselectAll;
		} {
			canvas.toggleSelectViewsUnderCursor();
		};


		keyDownVar = { |canvas, char, modifiers, unicode, keycode, key|
			switch ([modifiers, key])
				{ [ 3145728, 16777234 ] } { result = canvas.moveViewsHandler(-1, 0); } //left
				{ [ 3145728, 16777236 ] } { result = canvas.moveViewsHandler(1,  0); } //right
				{ [ 3145728, 16777235 ] } { result = canvas.moveViewsHandler(0, -1); } //up
				{ [ 3145728, 16777237 ] } { result = canvas.moveViewsHandler(0,  1); } //down
				{ [ 1048576, 65 ] } { canvas.selectAll } 			// cmd-a
				{ [ 1048576, 75 ] } { canvas.openTextView; } 	// cmd-k
				{ [ 1048576, 90 ] } { StoreHistory.undo } 		//cmd-z
				{ [ 1179648, 90 ] } { StoreHistory.redo } 		//cmd-shift -z
				{ [ 1179648, 91 ] } { canvas.subdivisions_(canvas.subdivisions - 1) } // cmd-shift-[
	 			{ [ 1179648, 93 ] } { canvas.subdivisions_(canvas.subdivisions + 1) } // cmd-shift-]
				
				{ Keys(\cmdMod, \s) } { Dispatcher((type: 'save', payload: (newFile: false))) } // cmd-s
				{ [ 1179648, 83 ] } 	{ Dispatcher((type: 'save', payload: (newFile: true))) } 	// cmd-shift-s
				
				{ Keys(\cmdMod, \o) } { Dispatcher((type: 'open')) } // cmd-o
				{ [ 1048576, 69 ] } { canvas.editSelection } // cmd-e - edit
				{ [ 1048576, 16777219 ] } { canvas.deleteSelection } // cmd-backspace

				{ [ 1179648, 61 ] }	{ canvas.zoomBy(1.05, 1.05) }
				{ [ 1179648, 45 ] }	{ canvas.zoomBy(1.05.reciprocal, 1.05.reciprocal) }
				{ [ 1048576, 66 ] } { Server.local.boot }
			;
		};

		keyUpVar = { |canvas, char, modifiers, unicode, keycode, key|
			if (this.shouldRelease([modifiers, key], initModKey)) {
				result !? {
					Dispatcher((
						type: 'moveObjects',
						payload: (
							updates: result.(),
							storeId: canvas.id
						)
					));
				};
				this.reset;
			}
		}
	}

	*shouldRelease { arg modKey, initModKey;
		^modKey == [ 0, initModKey[1] ]
	}

}


CanvasKeyActionManager {
  *new { arg canvas ... mixins;
    ^super.new.init(canvas, *mixins);
  }
  init { arg sequencerCanvas ... mixins;
    var canvas = sequencerCanvas.canvas;
    canvas.keyDownAction = { arg canvas, char, modifiers, unicode, keycode, key;
			if (canvas.hasFocus) {
        //[modifiers, key].postln;
				switch ([modifiers, key]) 
					{ [ 393216, 95 ] } { sequencerCanvas.zoomBy(1.05.reciprocal, 1.05.reciprocal) } // cmd-shift-minus
					{ [ 393216, 43 ] } { sequencerCanvas.zoomBy(1.05, 1.05) } // cmd-shift-plus
					{ [ 524288, 72 ] } { sequencerCanvas.moveOrigin(-10, 0) } // option-h
					{ [ 524288, 76 ] } { sequencerCanvas.moveOrigin(10, 0) } // option-right
					{ [ 524288, 75 ] } { sequencerCanvas.moveOrigin(0, -10) } // option-up
					{ [ 524288, 74 ] } { sequencerCanvas.moveOrigin(0, 10) } // option-down
          { [ 262144, 83 ] } { } // ctrl-s
          { [ 262144, 32 ] } { sequencerCanvas.play; } // ctrl-space
				;
        mixins.do { arg mixin;
          mixin.keyDownAction(canvas, char, modifiers, unicode, keycode, key);
        };
			}
		};

		//canvas.keyUpAction = { arg canvas, char, modifiers, unicode, keycode, key;};
  }
}



MouseActionManager {
  *new { arg canvas;
    ^super.new.init(canvas);
  }
}
