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
KeyActionManager {
	classvar <>keyUpVar;
	classvar <>keyDownVar;

	*keyDownHandler { |canvas, char, modifiers, unicode, keycode, key|
		keyDownVar !? {
			^keyDownVar.value(canvas, char, modifiers, unicode, keycode, key);
		};

		^switch ([modifiers, key])
			{ [ 131072, 16777248 ] } {
				this.shiftActionManager();
			} // shift
			
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

			{ Keys(\cmdMod, \z) } { Dispatcher((type: 'undo')) } //cmd -z
			{ [ 1179648, 90 ] } 	{ Dispatcher((type: 'redo')) } //cmd -shift -z
					
			{ Keys(\cmdMod, \s) } { Dispatcher((type: 'save', payload: (newFile: false))) } // cmd-s
			{ [ 1179648, 83 ] } 	{ Dispatcher((type: 'save', payload: (newFile: true))) } // cmd-shift-s
			{ Keys(\cmdMod, \o) } { Dispatcher((type: 'open')) } // cmd-o	

			{ Keys(\noMod, \q) } { canvas.toggleQuantization } // Q
					
			{ [ 1179648, 91 ] } { canvas.subdivisions_(canvas.subdivisions - 1) } // cmd-shift-[
	 		{ [ 1179648, 93 ] } { canvas.subdivisions_(canvas.subdivisions + 1) } // cmd-shift-]

			{ Keys(\noMod, \tab) } { canvas.cycleThroughViews }
	 		{ [ 0, 16777216 ] } { canvas.deselectAll } // esc
	 		{ [ 1048576, 65 ] } { canvas.selectAll } // cmd - a

	 		{ [ 2097152, 16777234 ] } { canvas.moveViewsHandler(-1, 0); } //left
			{ [ 2097152, 16777236 ] } { canvas.moveViewsHandler(1, 	0);	} //right
			{ [ 2097152, 16777235 ] } { canvas.moveViewsHandler(0, -1);	} //up
			{ [ 2097152, 16777237 ] } { canvas.moveViewsHandler(0, 	1);	} //down
		;
	}

	*keyUpHandler { |canvas, char, modifiers, unicode, keycode, key|
		keyUpVar !? {
			keyUpVar.value(canvas, char, modifiers, unicode, keycode, key);
		}
	}

	*reset {
		keyDownVar = nil;
		keyUpVar = nil;
	}

	*shiftActionManager {
		var result;
		keyDownVar = { |canvas, char, modifiers, unicode, keycode, key|
			[modifiers, key].postln;
			result = switch ([modifiers, key])
				{ [ 2228224, 16777234 ] } { canvas.extendSelectionHandler(-1, 0); } //left
				{ [ 2228224, 16777236 ] } { canvas.extendSelectionHandler(1,  0); } //right
				{ [ 2228224, 16777235 ] } { canvas.extendSelectionHandler(0, -1); } //up
				{ [ 2228224, 16777237 ] } { canvas.extendSelectionHandler(0,  1); } //down
			;
			
		};

		keyUpVar = { |canvas, char, modifiers, unicode, keycode, key|
			if ([modifiers, key] == [ 0, 16777248 ]) {
				"cleanup".postln;
				result !? _.();
				this.reset;
			}
		}
	}

}

