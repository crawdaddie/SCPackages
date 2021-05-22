var soundfile = 'data/oxygen'.import;
var sampleEvent = 'sampleEvent'.importFrom('src/baseEvents');
var sequencerEvent = 'sequencerEvent'.importFrom('src/baseEvents');
var events = [
	( row: 0, soundfile: soundfile.path, beats: 0, startPos: 0, length: 2 ),
  ( row: 0, soundfile: soundfile.path, beats: 4, startPos: 0, length: 2 ),
 	( row: 0, soundfile: soundfile.path, beats: 2, startPos: 0, length: 2 ),
 	( row: 0, soundfile: soundfile.path, beats: 6.5, startPos: 0.2, length: 2 ),
].collect(_.parent_(sampleEvent));
var subStoreEvents = [
	( row: 0, soundfile: soundfile.path, beats: 0, startPos: 0, length: 2 ),
  ( row: 0, soundfile: soundfile.path, beats: 4, startPos: 0, length: 2 ),
 	( row: 0, soundfile: soundfile.path, beats: 2, startPos: 0, length: 2 ),
 	( row: 0, soundfile: soundfile.path, beats: 6.5, startPos: 0.2, length: 2 ),
].collect(_.parent_(sampleEvent));


var codeEvent = (
  beats: 0,
  row: 3,
  length: 2,
  src: thisProcess.nowExecutingPath.dirname +/+ "src/moduleCode_1.scd"
).parent_(sequencerEvent);

var subStore = Store.new(
  (
    row: 1, 
    beats: 2,
    length: 2,
    getView: { arg ev;
      SequencerCanvas(ev); 
    }
  ).parent_(sequencerEvent)
);

events.do { |event, index|
	Store.global.addObject(event);
};
subStoreEvents.do { |event|
  subStore.addObject(event);
};

Store.global.addObject(subStore);
Store.global.addObject(codeEvent);
~store = Store.global;

~canvas = SequencerCanvas(Store.global);






//~stream.(~store);

(
p = p({
  var eventGroup = ~store.items(0)[0];


  while ({eventGroup.notNil}, {
    var time = eventGroup[0];
    var nextEventGroup = ~store.getNextEventGroup(time);
    postf("beats: % seconds: % time: % \n",
      thisThread.beats, thisThread.seconds, Main.elapsedTime
    );
    if (nextEventGroup.isNil, {
      (events: eventGroup[1]).yield;
      eventGroup = Nil;
    }, {
      var dur = nextEventGroup[0] - time; 
      (events: eventGroup[1], delta: dur).yield; 
      eventGroup = nextEventGroup;
    }); 
  })
});


)
//p.play;


