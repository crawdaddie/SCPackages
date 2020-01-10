Keys {
	classvar dict;
	*initClass {
		dict = Dictionary.with(*[
			\noMod -> 0,
			\cmdMod -> 1048576,
			\shiftMod -> 1048576,
			\optMod -> 2621440,

			\tab -> 16777217,

			\plus -> 61,
			\minus -> 45,

			\left -> 16777234,
			\right -> 16777236,
			\up -> 16777235,
			\down -> 16777237,

			\s -> 83,
			\z -> 90,
			\o -> 79
		]);
  }

	*new { arg ... keys;
		^dict.atAll(keys)
	}
}


SequencerCanvas : UserView {
	var <views;
	var zoom;
	var origin;

	*new { arg argParent, argBounds, subviews;
		var parent = argParent ?? Window.new('sequencer', Rect(1040, 455, 400, 400))
			.front;
		var bounds = argBounds ?? parent.view.bounds;

		^super.new(parent, bounds).init(subviews ? []);
	}

	clear {
		this.init([]);
		this.refresh;
	}

	renderView {
		views.do(_.renderView(origin))
	}

	getTopView { arg x, y;
		^views.reverse.detect(_.contains(x@y))
	}

	deselectAll {
		views.do(_.unselect)
	}

	moveViews { arg x, y;
		views.do(_.moveAction(x, y));
	}

	moveOrigin { arg x, y;
		origin.x = origin.x + x;
		origin.y = origin.y + y;
		this.refresh;
	}

	setZoom { arg zoomX, zoomY;
		zoom = zoomX@zoomY;
		views.do(_.setZoom(zoomX, zoomY));
		this.refresh;
	}

	cycleThroughViews {
		var selectedIndex = views.indexOf(_.selected) !? { |selectedIndex|
			selectedIndex = (selectedIndex + 1) % views.size;
		} ?? 0;
		views.do(_.unselect);
		views[selectedIndex].select;
		^views;
	}

	addViews { arg ...newViews;
		views = views ++ newViews;
		this.deselectAll();
		this.refresh;
	}

	addEvents { arg newEvents;
		var newViews = newEvents.collect { |event|
			event.soundfile !? SequenceableSoundfileBlock(event) ?? SequenceableBlock(event);
		};

		views = views ++ newViews;
		this.deselectAll();
		this.refresh;
	}

	addEvent { arg event;
		views = views ++ [
			event.soundfile !? SequenceableSoundfileBlock(event, this) ?? SequenceableBlock(event)
		];
		this.deselectAll();
		this.refresh;
	}

	save { arg newFile = false;
		if (this.hasFocus) {
			Dispatcher((type: 'save', payload: (newFile: newFile)))
		}
	}

	open {
		if (this.hasFocus) {
			Dispatcher((type: 'open'))
		}
	}

	historyAction { arg action;
		if (this.hasFocus) {
			Dispatcher((type: action))
		}
	}

	init { arg argviews;

		views = argviews;
		zoom = 1@1;
		origin = 0@0;

		this.resize = 5;
		this.parent.acceptsMouseOver_(true);

		this.drawFunc = {
			views = partition(views, { |m| m.selected.not }).flatten;
			this.renderView;
		};

		this.mouseDownAction = { |canvas, mouseX, mouseY|
			var translatedMouse = Point(mouseX, mouseY) - origin;
			var x = translatedMouse.x;
			var y = translatedMouse.y;
			var topView = this.getTopView(x, y);

			this.deselectAll;

			topView !? _.mouseDownAction(x, y);

			this.refresh;
		};

		this.mouseMoveAction = { |v, mouseX, mouseY|
			var translatedMouse = Point(mouseX, mouseY) - origin;
			var x = translatedMouse.x;
			var y = translatedMouse.y;

			views.do(_.mouseMoveAction(x, y));

			this.refresh;
		};

		this.mouseUpAction = {

			views.do(_.mouseUpAction);

			this.refresh;
		};

		this.keyDownAction = { |canvas, char, modifiers, unicode, keycode, key|
			switch ([modifiers, key])
				{ Keys(\cmdMod, \plus) }	{ this.setZoom(1.05, 1.05) }
				{ Keys(\shiftMod, \minus) }	{ this.setZoom(1.05.reciprocal, 1.05.reciprocal) }

				{ Keys(\optMod, \left) } { this.moveViews(-2, 0) }
				{ Keys(\optMod, \right) } { this.moveViews(2, 0) }
				{ Keys(\optMod, \up) } { this.moveViews(0, -2) }
				{ Keys(\optMod, \down) } { this.moveViews(0, 2) }

				{ [ 2097152, 16777234 ] } { this.moveOrigin(-10, 0) } //left
				{ [ 2097152, 16777236 ] } { this.moveOrigin(10, 0) } //right
				{ [ 2097152, 16777235 ] } { this.moveOrigin(0, -10) } //up
				{ [ 2097152, 16777237 ] } { this.moveOrigin(0, 10) } //down

				{ Keys(\noMod, \tab) } { this.cycleThroughViews }
				{ Keys(\cmdMod, \z) } { this.historyAction('undo') } //cmd -z
				{ [ 1179648, 90 ] } { this.historyAction('redo') } //cmd -shift -z
				{ Keys(\cmdMod, \s) } { this.save }
				{ [ 1179648, 83 ] } { this.save(true) }
				{ Keys(\cmdMod, \o) } { this.open };

			this.refresh;
		};

		^this
	}
}
