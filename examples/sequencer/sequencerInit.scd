(
var soundfile = 'data/oxygen'.import;
var sampleEvent = 'sampleEvent'.importFrom('events/baseEvents');
var sequencerEvent = 'sequencerEvent'.importFrom('events/baseEvents');
var bpm = 60;
var events;
var saveDir = thisProcess.nowExecutingPath !? _.dirname ?? "/Users/adamjuraszek/PROJECTS/supercollider/saved_projects";
var projectFile = "/Users/adamjuraszek/PROJECTS/supercollider/test_piece/saves/opentest";
var load;
var clipboard;
var aggregate;
	
t = (
	type: 'timingContext',
	bpm: 60
);
	
StoreHistory.enable;

Dispatcher.addListener('addToAggregate', { arg payload;
	var id = payload.id;
	var aggregate = Store.at(id);
	var ids = aggregate.ids;
	var newState = (ids: ids.add(payload.newObjectId));
	Store.updateObject(id, newState);
	// Store.objects.postln;
});

Dispatcher.addListener('moveObject', { arg payload;
	var id = payload.id;
	var newState = (
		timestamp: payload.x * 60 / t.bpm,
		channel: payload.y,
		length: payload.length * 60 / t.bpm
	);

	Store.updateObject(id, newState)
});

Dispatcher.addListener('save', { arg payload;

	if ((payload.newFile || projectFile.isNil), {		
		Dialog.savePanel({ arg path;

			projectFile = path;
			"saving to %".format(projectFile).postln;
			Store.objects.writeMinifiedTextArchive(projectFile);
			a.parent.name = "sequencer - %".format(projectFile.basename);

			},
			path: saveDir
		);
	}, {
		"saving to %".format(projectFile).postln;
		Store.objects.writeMinifiedTextArchive(projectFile)
	});
});

Dispatcher.addListener('open', {
	Dialog.openPanel(load,
    path: saveDir
  );
});

Dispatcher.addListener('pasteObjects', { arg payload;
	// payload.postln;
});

Store.addObject(t);


events = [
	(soundfile: soundfile.path, timestamp: 0, startPos: 0, length: 2),
 	(soundfile: soundfile.path, timestamp: 2, startPos: 0, length: 2),
 	(soundfile: soundfile.path, timestamp: 4, startPos: 0, length: 2),
 	(soundfile: soundfile.path, timestamp: 6.5, startPos: 0.2, length: 2 )
 	].collect(_.parent_(sampleEvent));

events = events.add((timestamp: 0, channel: 2).parent_(sequencerEvent));

// events.do { |event, index|
// 	Store.addObject(event);
// };



load = { arg path;
	var archive = Archive.read(path).global;
	Store.replace(archive);
	a.clear;
	a.addObjects(Store.objects.values);

	projectFile = path;
	a.parent.name = "sequencer - %".format(path.basename);
};




// s.waitForBoot {
// 	"booting".postln;
// 	Dispatcher((type: 'serverBooted'));
// 	s.makeGui;
// };
aggregate = Store.createAggregate(*events);

a = SequencerCanvas.fromAggregate(aggregate);
)

h = Store.at(1007);
