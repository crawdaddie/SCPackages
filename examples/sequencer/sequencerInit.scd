var soundfile = 'data/oxygen'.import;
var sampleEvent = 'sampleEvent'.importFrom('src/baseEvents');
var sequencerEvent = 'sequencerEvent'.importFrom('src/baseEvents');
var events = [
	( row: 0, soundfile: soundfile.path, beats: 0, startPos: 0, dur: 2 ),
  ( row: 0, soundfile: soundfile.path, beats: 4, startPos: 0, dur: 2 ),
 	( row: 0, soundfile: soundfile.path, beats: 2, startPos: 0, dur: 2 ),
 	( row: 0, soundfile: soundfile.path, beats: 6.5, startPos: 0.2, dur: 2 ),
].collect(_.parent_(sampleEvent));

var subStoreEvents = [
	( row: 0, soundfile: soundfile.path, beats: 0, startPos: 0, dur: 2 ),
  ( row: 0, soundfile: soundfile.path, beats: 4, startPos: 0, dur: 2 ),
 	( row: 0, soundfile: soundfile.path, beats: 2, startPos: 0, dur: 2 ),
 	( row: 0, soundfile: soundfile.path, beats: 6.5, startPos: 0.2, dur: 2 ),
].collect(_.parent_(sampleEvent));


var codeEvent = (
  beats: 0,
  row: 3,
  dur: 2,
  src: thisProcess.nowExecutingPath.dirname +/+ "src/moduleCode_1.scd"
).parent_(sequencerEvent);

var subStore = Store.new(
  (
    row: 1, 
    beats: 2,
    dur: 2,
    getView: { arg ev;
      SequencerCanvas(ev); 
    },
    src: thisProcess.nowExecutingPath.dirname +/+ "src/storeCode.scd"
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

//(
//p = p({
//  var eventGroup = ~store.items(0)[0];
//  var time, nextEventGroup;
//
//  while ({ eventGroup.notNil }, {
//    time = eventGroup[0];
//
//    nextEventGroup = ~store.getNextEventGroup(time);
//
//    if (nextEventGroup.isNil, {
//      (events: eventGroup[1]).yield;
//      eventGroup = nil;
//    }, {
//      var dur = nextEventGroup[0] - time; 
//      (events: eventGroup[1], delta: dur).yield; 
//      eventGroup = nextEventGroup;
//    }); 
//  })
//});
//

//)
//p.trace.play;

//p = StorePlayer(~store);
//Pbindf(p(StorePlayer(~store).getRoutineFunc)).play;
