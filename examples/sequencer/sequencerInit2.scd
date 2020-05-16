~createSequencerGui = {
	var saveDir = thisProcess.nowExecutingPath !? _.dirname ?? "/Users/adamjuraszek/PROJECTS/supercollider/saved_projects";
	var projectFile = "/Users/adamjuraszek/PROJECTS/supercollider/test_piece/saves/opentest";
	var gui = SequencerCanvas();
	var load;
	
	load = { arg path;
		var archive = Archive.read(path).global;
		Store.replace(archive);
		gui.clear;
		gui.addObjects(Store.objects.values);
		projectFile = path;
		gui.parent.name = "sequencer - %".format(path.basename);
	};
	
	StoreHistory.enable;

	Dispatcher.addListener('objectMove', { arg payload;
		var id = payload.id;
		var newState = (
			timestamp: payload.x * 60 / t.bpm,
			channel: payload.y,
			length: payload.length * 60 / t.bpm
		);

		Store.updateObject(id, newState)
	});

	Dispatcher.addListener('save', { arg payload;
		"save".postln;
		payload.postln;

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
		"open".postln;

		Dialog.openPanel(load,
	    path: saveDir
	  );
	});
}

(
var soundfile = 'data/oxygen'.import;
var sampleEvent = 'sampleEvent'.importFrom('events/baseEvents');
var bpm = 60;
var events;

a = ~createSequencerGu

t = (
	type: 'transport',
	bpm: 60
);
Store.addObject(t);


events = [
	(type: 'soundfile', soundfile: soundfile.path, timestamp: 0, startPos: 0, length: 2),
 	(type: 'soundfile', soundfile: soundfile.path, timestamp: 2, startPos: 0, length: 2),
 	(type: 'soundfile', soundfile: soundfile.path, timestamp: 4, startPos: 0, length: 2),
 	(type: 'soundfile', soundfile: soundfile.path, timestamp: 6.5, startPos: 0.2, length: 2 )
 	].collect(_.parent_(sampleEvent));


events.do { |event, index|
	Store.addObject(event);

};


o = events[0];


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

a = SequencerCanvas.fromObjects(events);
)



Store.objects.values.do(_.postln)
a.views
SCNvim


