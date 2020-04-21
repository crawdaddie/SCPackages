MouseAction {
	var objects;

	*new { | ...objects |
		^super.new.init(objects);
	}

	init { | argobjects |
		objects = argobjects;

	}
}