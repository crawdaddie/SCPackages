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
var subStoreId;
	
StoreHistory.enable;

Dispatcher.addListener('moveObjects', { arg payloads;

	// var updates = MultiLevelIdentityDictionary();

	// payloads.do { |payload|
	// 	var id = payload.id;
	// 	var timingContext = Store.getTimingContext(id);
	// 	var path = Store.getPath(id);
	// 	var newState = (
	// 		id: payload.id,
	// 		timestamp: payload.x * 60 / timingContext.bpm,
	// 		channel: payload.y,
	// 		length: payload.length * 60 / timingContext.bpm
	// 	);

	// 	updates.put(*(path ++ [newState]));
	// };

	// Store.patch(updates);

	var updates = Dictionary();
	payloads.do { |payload|
		var id = payload.id;
		var timingContext = Store.getTimingContext(id);
		var path = Store.getPath(id);
		var newState = (
			id: payload.id,
			timestamp: payload.x * 60 / timingContext.bpm,
			channel: payload.y,
			length: payload.length * 60 / timingContext.bpm
		);

		updates.put(id, newState);
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

	var newItems = payload.items.collect({ arg item;
		item.timestamp = payload.x + item.timestamp;
		item.channel = payload.y + item.channel;
		item;
	});
	newItems.do { arg item;
		Store.addObject(item, payload.parentId)
	}
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


subStoreId = Store.addObject((type: 'store', timestamp: 2, channel: 5).parent_(sequencerEvent));
Store.addObject(
	(soundfile: soundfile.path, timestamp: 6.5, startPos: 0.2, length: 2)
		.parent_(sampleEvent),
	subStoreId
);



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

// Store.getBase.postln;

a = SequencerCanvas.fromStore();
)
