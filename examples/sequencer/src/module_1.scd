~thingInModule = 1;




~play = M { arg ev; // <-- passed in event - can contain stuff like soundfile, startPos, channel number etc
	ev.postln;
}; 
