(
var soundfile = 'data/oxygen'.import;
var sampleEvent = 'sampleEvent'.importFrom('events/baseEvents');
var sequencerEvent = 'sequencerEvent'.importFrom('events/baseEvents');
var bpm = 60;
var events;
var saveDir = thisProcess.nowExecutingPath !? _.dirname +/+ "saves" ?? "/Users/adamjuraszek/PROJECTS/supercollider/packages/examples/sequencer/saves/";
// var saveDir = "/Users/adamjuraszek/PROJECTS/supercollider/packages/examples/sequencer/";
var projectFile = "/Users/adamjuraszek/PROJECTS/supercollider/packages/examples/sequencer/saves/opentest";
var load;
var clipboard;
var aggregate;
	
	
StoreHistory.enable;

// Dispatcher.addListener('moveObject', { arg payload;
// 	var id = payload.id;
// 	var timingContext = Store.getTimingContext(id);
// 	var newState = (
// 		timestamp: payload.x * 60 / timingContext.bpm,
// 		channel: payload.y,
// 		length: payload.length * 60 / timingContext.bpm
// 	);

// 	Store.updateObject(id, newState)
// });

Dispatcher.addListener('moveObjects', { arg payloads;

	var updates = MultiLevelIdentityDictionary();

	payloads.do { |payload|
		var id = payload.id;
		var timingContext = Store.getTimingContext(id);
		var path = Store.getPath(id);
		var newState = (
			timestamp: payload.x * 60 / timingContext.bpm,
			channel: payload.y,
			length: payload.length * 60 / timingContext.bpm
		);
		updates.put(*(path.add(newState)));
	};
	Store.patch(updates);
});


Dispatcher.addListener('save', { arg payload;

	if ((payload.newFile || projectFile.isNil), {		
		Dialog.savePanel({ arg path;

			projectFile = path;
			"saving to %".format(projectFile).postln;
			Store.archive(projectFile);
			a.parent.name = "sequencer - %".format(projectFile.basename);
			saveDir = projectFile.dirName;
			},
			path: saveDir
		);
	}, {
		"saving to %".format(projectFile).postln;
		Store.archive(projectFile);
		saveDir = projectFile.dirName;
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

Store.addTimingContext((
	type: 'timingContext',
	bpm: 60
));


events = [
	(soundfile: soundfile.path, timestamp: 0, startPos: 0, length: 2),
 	(soundfile: soundfile.path, timestamp: 2, startPos: 0, length: 2),
 	(soundfile: soundfile.path, timestamp: 4, startPos: 0, length: 2),
 	(soundfile: soundfile.path, timestamp: 6.5, startPos: 0.2, length: 2 )
 	].collect(_.parent_(sampleEvent));

events = events.add((timestamp: 0, channel: 2).parent_(sequencerEvent));

events.do { |event, index|
	Store.addObject(event);
};


load = { arg path;
	Store.readFromArchive(path);
	a.fromStore(Store.getBase);
	projectFile = path;
	a.parent.name = "sequencer - %".format(path.basename);
};

// s.waitForBoot {
// 	"booting".postln;
// 	Dispatcher((type: 'serverBooted'));
// 	s.makeGui;
// };

a = SequencerCanvas.fromStore(Store.getBase);
)


Store.global.put(1004, nil);

Store.global.postTree

a.clear;
a.refresh;
a

Store.postTree;


b = Store.getBase

b.select({ arg value, key; [key, value].postln; true})

j

Store.archive("/Users/adamjuraszek/PROJECTS/supercollider/packages/examples/sequencer/saves/opentest")

Store.readFromArchive("/Users/adamjuraszek/PROJECTS/supercollider/packages/examples/sequencer/saves/opentest");

Store.addObject((a: 'something', b: 6, timestamp: 0), 1002)

Store.addObject((type: 'store', timestamp: 1))




MultiLevelIdentityDictionary



