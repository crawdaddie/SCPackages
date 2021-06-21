~setupStore = {
var sampleEvent, sequencerEvent, storeEvent, events, subStoreEvents, codeEvent, subStore;
var soundfile = 'data/oxygen'.import;
# sampleEvent, sequencerEvent, storeEvent = ['sampleEvent', 'sequencerEvent', 'storeEvent'].importFrom('src/baseEvents');

events = [
  ( soundfile: soundfile.path ),
  ( soundfile: soundfile.path, beats: 2 ),
  ( soundfile: soundfile.path, beats: 4, dur: 2 ),
 	( soundfile: soundfile.path, beats: 6.5, startPos: 0.2, ),
].collect(sampleEvent.create(_));

subStoreEvents = [
	( soundfile: soundfile.path, beats: 0 ),
  ( soundfile: soundfile.path, beats: 4 ),
 	( soundfile: soundfile.path, beats: 2 ),
 	( soundfile: soundfile.path, beats: 6.5, startPos: 0.2, dur: 2 ),
].collect(sampleEvent.create(_));


codeEvent = sequencerEvent.create((
  beats: 0,
  row: 3,
  dur: 2,
  src: thisProcess.nowExecutingPath.dirname +/+ "src/moduleCode_1.scd"
));

subStore = Store.new(
  storeEvent.create((
    row: 1, 
    beats: 2,
    dur: 2,
    src: thisProcess.nowExecutingPath.dirname +/+ "src/storeCode.scd"
  ))
);

events.do { |event, index|
	Store.global.addObject(event);
};
Store.global.addObject(subStore);
Store.global.addObject(codeEvent);
subStoreEvents.do { |event|
  subStore.addObject(event);
};
};













//~setupStore.();
//~store = Store.global;
//~canvas = SequencerCanvas(Store.global);
//~store.writeMinifiedTextArchive("/home/adam/projects/supercollider/packages/examples/sequencer2/archive.scproj");

//Store.readFromArchive("/home/adam/projects/supercollider/packages/examples/sequencer/archive.scproj");
//~store = Store.global;
//~canvas = SequencerCanvas(~store);
//~store[1005]
//~stream.(~store);
Project.openLatest;
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
