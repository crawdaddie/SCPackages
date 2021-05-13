//Project.openLatest;
// 
//Project.new;
// Project.save;


/*
var soundfile = 'data/oxygen'.import;
var sampleEvent = 'sampleEvent'.importFrom('src/baseEvents');
var sequencerEvent = 'sequencerEvent'.importFrom('src/baseEvents');
var events = [
	( row: 0, soundfile: soundfile.path, beats: 0, startPos: 0, length: 2 ),
  ( row: 0, soundfile: soundfile.path, beats: 4, startPos: 0, length: 2 ),
 	( row: 0, soundfile: soundfile.path, beats: 2, startPos: 0, length: 2 ),
 	( row: 0, soundfile: soundfile.path, beats: 6.5, startPos: 0.2, length: 2 ),
].collect(_.parent_(sampleEvent));



events.do { |event, index|
	Store.global.addObject(event);
};

~store = Store.global;

~canvas = SequencerCanvas(Store.global);

*/
//Store.global.beats ?? 0
Project("/home/adam/projects/supercollider/packages/examples/sequencer/saves/opentest.scproj");
Store.postTree;
4.postln;


Store.at(1007)
