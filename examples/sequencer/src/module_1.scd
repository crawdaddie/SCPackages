~thingInModule = 1;

"init module_1".postln;



~play = M { arg ev; // <-- passed in event - can contain stuff like soundfile, startPos, channel number etc
	~thingInModule.postln;
}; 
