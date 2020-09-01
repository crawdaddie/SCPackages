RxParam {
	var val;
	var callback;
	
	*new { arg initialValue, callback;
		^super.new.init(initialValue, callback);
	}

	init { arg initialValue, callbackFn;
		val = initialValue;
		callback = callbackFn;
	}
	
	update { arg payload;
		try {
			val = callback.valueWithEnvir(payload) ?? val;
		}
	}

	val_ { arg value;
		val = value;
	}

	value {
		^val
	}
}

RxBlock { 
	var <eventId;
	// var x, y, width;
	var params;

	var <selected;

	var mouseStart, mouseDelta;
	
	*new { arg event;
		^super.new.init(event)
	}

	listen { arg type, callback;

		Dispatcher.addListener(
			type,
			this,
			callback
		)
	}

	init { arg event;

		eventId = event.id;
		params = ();

		params.use {
			~x = RxParam(
				event.beats * 40,
				{ arg beats; beats * 40 },
			);
			
			~y = RxParam(
				event.row * 50,
				{ arg row; row * 50 }
			);
			
			~width = RxParam(
				event.length * 50,
				{ arg length; length * 50 }
			);

			~selected = RxParam(
				false,
				{ arg id; id == eventId }
			);
		};
		
		this.listen(
			Topics.objectUpdated,
			{ arg payload;
				if (payload.id == eventId) {
					params.keysValuesDo { arg key, rxVal;
						rxVal.update(payload);
					}
				}
			}
		);
	}

	renderView {
		params.use {
			Pen.addRect(Rect(~x.value, ~y.value, ~width.value, 40));
   		Pen.strokeColor = SequencerTheme.darkGrey;
   		Pen.stroke;
   	}
	}

	contains { arg point;
		var rect = params.use {
			Rect(~x.value, ~y.value, ~width.value, 40);
		};
		^rect.contains(point);
	}

	mouseDownAction { arg x, y;
		var payload = ();
		mouseDelta = Point(x.value() - x, y.value() - y);
	}

	mouseMoveAction { arg x, y, initialMouse;
		var payload = (
			beats: (x + mouseDelta.x) / 50,
			row: (y + mouseDelta.y) / 40,
		);
		params.use {
			~x.update(payload);
			~y.update(payload);
		}
	}

	mouseUpAction {
		"mouse up".postln;
	}
}

RxMouseAction {
	var initialValue;
	var listeners;

	*new { arg listeners, x, y;
		^super.new.init(listeners, x, y);
	}

	init { arg argListeners, x, y;
		listeners = argListeners;
		listeners.postln;
		this.mouseDownAction(x, y);
	}

	mouseDownAction { arg x, y;
		initialValue = x@y;
		listeners.do(_.mouseDownAction(x, y));
	}

	mouseMoveAction { arg x, y;
		listeners.do(_.mouseMoveAction(x, y, initialValue));		
	}

	mouseUpAction { arg x, y;
		listeners.do(_.mouseUpAction(x, y));
	}
}

