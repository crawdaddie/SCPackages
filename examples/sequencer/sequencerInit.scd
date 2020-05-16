(
var soundfile = 'data/oxygen'.import;
var sampleEvent = 'sampleEvent'.importFrom('events/baseEvents');
var sequencerEvent = 'sequencerEvent'.importFrom('events/baseEvents');
var events;
var saveDir = thisProcess.nowExecutingPath !? _.dirname +/+ "saves" ?? "/Users/adamjuraszek/PROJECTS/supercollider/packages/examples/sequencer/saves/";
// var saveDir = "/Users/adamjuraszek/PROJECTS/supercollider/packages/examples/sequencer/";
var projectFile = "/Users/adamjuraszek/PROJECTS/supercollider/packages/examples/sequencer/saves/opentest.scproj";
var load;
var clipboard;
var aggregate;
var subStoreId;

StoreHistory.enable;

Store.global.addTimingContext((
	bpm: 80
));

Dispatcher.addListener(
	'moveObjects',
	'system',
	{ arg payload;
		var updates = Dictionary();
		var store = payload['storeId'] !? Store.at(payload.storeId) ?? Store.global;
		var timingContext = store.getTimingContext;
	
		payload.updates.do { |update|
			var id = update.id;
			var newState = (
				beats: update.x,
				channel: update.y,
				length: update.length
			);
			updates.put(id, newState);
		};
		Store.patch(updates, store.id);
	}
);


Dispatcher.addListener(
	'pasteObjects',
	'system',
	{ arg payload;
		var newItems = payload.items.collect({ arg item;
			item.timestamp = payload.x + item.timestamp;
			item.channel = payload.y + item.channel;
			item;
		});

		newItems.do { arg item;
			Store.addObject(item, payload.parentId)
		}
	}
);


// initialisation

// e = (soundfile: soundfile.path, startPos: 0, length: 2).parent_(sampleEvent);

// // events = [e];
// events = [
// 	(soundfile: soundfile.path, beats: 0, startPos: 0, length: 2),
//  	(soundfile: soundfile.path, beats: 2, startPos: 0, length: 2),
//  	(soundfile: soundfile.path, beats: 4, startPos: 0, length: 2),
//  	(soundfile: soundfile.path, beats: 6.5, startPos: 0.2, length: 2 ),
//  	].collect(_.parent_(sampleEvent));

// events = events.add((beats: 0, channel: 2).parent_(sequencerEvent));


// events.do { |event, index|
// 	Store.addObject(event);
// };


// subStoreId = Store.addObject(
// 	Store((beats: 2, channel: 5, length: 1).parent_(sequencerEvent))
// );

// Store.addObject(
// 	(soundfile: soundfile.path, beats: 6.5, startPos: 0.2, length: 2).parent_(sampleEvent),
// 	subStoreId
// );

load = { arg path;
	Store.readFromArchive(path);
	Store.postTree;
	a.fromStore(Store.global);
	projectFile = path;
	a.parent.name = "sequencer - %".format(path.basename);
};



Dispatcher.addListener(
	'save',
	'system',
	{ arg payload;
		if ((payload.newFile || projectFile.isNil), {
			Dialog.savePanel({ arg path;
				projectFile = path;
				"saving to %".format(projectFile).postln;
				Store.archive(projectFile);
				a.parent.name = "sequencer - %".format(projectFile.basename);
				saveDir = projectFile.dirname;
				},
				path: saveDir
			);
		}, {
			"saving to %".format(projectFile).postln;
			Store.archive(projectFile);
			saveDir = projectFile.dirname;
		});
	}
);

Dispatcher.addListener('open', 'system', {
	Dialog.openPanel(load,
    path: saveDir
  );
});

// s.waitForBoot {
// 	"booting".postln;
// 	Dispatcher((type: 'serverBooted'));
// 	s.makeGui;
// };


// a = SequencerCanvas.fromStore(Store.global);
a = SequencerCanvas();
// a.views[0].select;
Store.postTree;

load.value(projectFile);
)



