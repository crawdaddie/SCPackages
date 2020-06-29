~thingInModule = 1;

~playInStore = M { arg ev, events; // <-- passed in event - can contain stuff like soundfile, startPos, channel number etc
	// ~thingInModule.postln;
	// ev.class.postln;
	// ev.play;
	// ev.postTree;
	[ev, events].postln;
}; 
