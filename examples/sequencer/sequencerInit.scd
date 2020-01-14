(
var soundfile = 'data/oxygen'.import;
var sampleEvent = 'sampleEvent'.importFrom('events/baseEvents');
var bpm = 60;
var events;
var applyNewState;

// var transport = (
// 	type: 'transport',
// 	bpm: 60
// );

var saveDir = thisProcess.nowExecutingPath !? _.dirname ?? "/Users/adamjuraszek/PROJECTS/supercollider/saved_projects";
var projectFile = "/Users/adamjuraszek/PROJECTS/supercollider/test_piece/saves/opentest";
var load;

t = (
	type: 'transport',
	bpm: 60
);


StoreHistory.enable;


events = [
	(type: 'soundfile', soundfile: soundfile.path, timestamp: 0, startPos: 0, length: 2),
 	(type: 'soundfile', soundfile: soundfile.path, timestamp: 2, startPos: 0, length: 2),
 	(type: 'soundfile', soundfile: soundfile.path, timestamp: 4, startPos: 0, length: 2),
 	(type: 'soundfile', soundfile: soundfile.path, timestamp: 6.5, startPos: 0.2, length: 2 )
 	].collect(_.parent_(sampleEvent));


events.do { |event, index|
	Store.addObject(event);
};

Store.addObject(t);

o = events[0];


load = { arg path;
	var archive = Archive.read(path).global;
	Store.replace(archive);
	a.clear;
	a.addEvents(Store.objects.values);

	projectFile = path;
	a.parent.name = "sequencer - %".format(path.basename);
};


Dispatcher.addListener('timingChange', { arg payload;
	var id = payload.id;
	var newState = (
		timestamp: payload.x * 60 / bpm,
		channel: payload.y,
		length: payload.length * 60 / bpm
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



// s.waitForBoot {
// 	"booting".postln;
// 	Dispatcher((type: 'serverBooted'));
// 	s.makeGui;
// };

a = SequencerCanvas.fromEvents(events);
)
