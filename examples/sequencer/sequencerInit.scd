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


Dispatcher.addListener('deleteObjects', { arg payload;
	Store.removeObjects(*payload.ids)
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

events.do { |event, index|
	Store.addObject(event);
};



load = { arg path;
	var archive = Archive.read(path).global;
	Store.objects.postln;
	Store.replace(archive);
	Store.objects.postln;

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
// aggregate = Store.createAggregate(*events);

a = SequencerCanvas.fromAggregate(Store.base);
)


UniqueId.next()

Store.at(1001)
UniqueID.next
Store.base

Store.objects.do(_.postln)

q = Dictionary.with(1001 -> "a", 1002 -> "b");
StoreHistory.history
q.
q[1002]
q = Dictionary();

q.at(1001)

q.class

Store.objects.do(_.postln);

h = (1001 -> nil)

h = false;

h ?? {"jj".postln}


d = (
	1001: 'a',
	1002: (1003: 'b', 1004: 'c')
);


a = [
	(a: 1, b: 2),
	(a: 1, b: 2),
	(a: 1, b: 2),
	(a: 1, b: 2)
];

d = MultiLevelIdentityDictionary.with(*[
	1001 -> a[0],
	1002 -> a[1],
	1003 -> Dictionary.with(*[
		1004 -> a[2],
		1005 -> a[3]
	])
]);
d = MultiLevelIdentityDictionary()


d.put(1001, 1002, 1003, 1004, (a: 1, b: 2))

g = d[1001, 1002, 1003];


g[1005] = (a: 3, b: 2)


Store.base


g

 = nil




d

a = Store.new(*[(a: 1, b: 2), (a: 2, b: 3)]);

b = 
Store.objects;



b = nil
a.instanceObjects

a
d[1003][1004].b = 4
 = nil

d


or
Tree

h = Store.at(1007);
