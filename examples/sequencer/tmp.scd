'sequencerEvent'.importFrom('events/baseEvents');


(
p = (
	// 1005: (
	// 	channel: 3,
	// 	beats: 0,
	// ),
	// // 1002: [nil],
	// // 1001: [nil],
	// 1006: (
	// 	beats: 2,
	// 	channel: 5,
	// 	1007: (
	// 		startPos: 0.2,
	// 		beats: 6.5,
	// 		length: 2,
	// 	) 
	// ),
	// 1003: (
	// 	startPos: 0,
	// 	beats: 4,
	// 	length: 1.1,
	// ),
	1003: [nil],
	// 1004: (
	// 	startPos: 0.2,
	// 	beats: 6.5,
	// 	length: 0.5,
	// ),
	// new: 100.collect({ arg i;
	// 	(
	// 		beats: 4.rand / 8,
	// 		length: rrand(0.5, 1),
	// 		channel: 5.rand,
	// 	).parent_(~sequencerEvent)
	// })
);
Store.patch(p);
);



a.refresh;
)
Store.lookups

Store.postTree;
Store.global.at(1005)
Store.at(1006).class
Store.lookups;
4.9.rand

Store.resetPaths;

a.views
4.collect({ arg i; i})
Store.lookups;



Store.postTree;


e.timestamp = { arg ev;
	var absolute;
	if (ev.unit == 'beats') {

	}
	absolute;
};

e.timestamp;
p = Store.at(1006)
Store.lookups

p.addTimingContext;

p.postTree;

p.patch((
	timingContext: (bpm: 80)
));

p = Store.at(1006)
p
p.addTimingContext((bpm: 80))

q = p[1007]

p


q.proto_(p)

q.use {
	~timingContext.postln;
};

Store.
q.proto

l = "/Users/adamjuraszek/PROJECTS/supercollider/packages/examples/sequencer/saves/opentest".load;


l.postTree;

q.use {
	~timingContext.postln
};

q.proto


qq



Store.postTree;


(startPos: 0.2, timestamp: 6.5, channel: 6).parent_(~sequencerEvent).postcs


Store.global[1005]

a.id

f.value(a: 1, c: 2);


f = { arg a, b;
	[a, b].postln;
}

f = [ \a -> 1, \b -> 2 ];
Env

f[0].class
EnvelopeEdit
e
e = Env.new(
	[0, 		1, 		 0.3, 	 0.8,	 0],
	[2, 		3, 		 1, 		 4], 
	['exp', 'exp', 'exp', 'lin']);

e.plot;

e.plot;

w = Window("envelope", Rect(150 , Window.screenBounds.height - 250, 400, 150)).front;
w.view.decorator = FlowLayout(w.view.bounds);

b = EnvelopeView(w, Rect(0, 0, 350, 100))
  .thumbSize_(5)
  .drawLines_(true)
  .fillColor_(Color.green)
  .selectionColor_(Color.red)
  .drawRects_(true)
  .value_([(0.0, 0.1 .. 1.0), (0.0, 0.1 .. 1.0)])
  .setEditable(0,false);


c = (allow: 1, d: 2);

e.getModule
e['getView']
e

valueEnvir
'baseEvents'.import;

~sequencerEvent = 'sequencerEvent'.importFrom('events/baseEvents');


e.getView(1)

e.getModule['play']
Event
e.play
e.use {
	~startPos.postln;
};

e.play

e[\play].value

k = Store.at(1006)


Mod.all

e.use {
	e.getModule.play.valueEnvir;
};

e.playEvent;


e.getModule

e.playEvent

q = 'sampleEvent'.importFrom('events/baseEvents');

q
'events/baseEvents'.import;

currentEnvironment.baseEvents.postln;

e.parent


c = (a: 1, b: 2, viewClass: StoreBlock);

e.getView(e, 1)
e.postln;
e

e['getView']

e.getView(1)
e.postcs

.postcs;

4.postln;

e.postTree;



e.getModule.codeForSoundfileMod

j = e.getModule;
j.codeForSoundfileMod;

j.proto
4.postln;
j.know_(true)
e.postTree;

j.codeForSoundfileMod

.value(j)


.codeForSoundfileMod;

g = { arg a, b;

}
j = 'code event';
j.class


q = (
	BEATS: 'beats',

	ABSOLUTE: 'absolute'
);


a.ABSOLUTE

g.parents
Store.



z.type

c = (c: 1);

j = (a: 1, b: 2);

j.parent_(c);



h = j.make { 
	[~a, ~b, ~c].postln;
	~q = ~a + ~c;
}

h

j.value();





q = Environment.make {
	j.value();
	~c = 3;
};
EnvirGui(Store.at(1001))


j.class
j.play;

j.src


Store.global.play;

m = Store.at(1006);


m.play;


m.getModule

Store.global.play;


j.getModule.open

j = .parents



q
Store.postTree;

m = Mod.all['/Users/adamjuraszek/PROJECTS/supercollider/packages/examples/sequencer/src/module_1.scd']

a.views[0];



Store.postTree;
q = a.views[0];

i = { arg view, extension;
	view.bounds.width = view.bounds.width + extension;
	a.refresh;
};

l = { arg view, extension;
	view.bounds.width = view.bounds.width - extension;
	view.bounds.left = view.bounds.left + extension;
	a.refresh;
};


q.bounds.left

l.(a.views[0], -10)
q.bounds.width = q.bounds.width + 10;
a.refresh;


 = q.bounds.set(
	Rect(0, 0, 120, 40);
a.refresh;

a = (
	a: 1, 
	b: 2
);


p = (
	timingContext: (bpm: 70),
);

p[1001] = a;

a.parent_(p);

Store.lookups

a = SortedList(8, { arg a, b;
	var itemA = Store.at(a);
	var itemB = Store.at(b);
	itemA.beats < itemB.beats;
});


Store.at(1007).timestamp


Store.global.orderedItems;


l = Store.getItems;

l.powersetDo

l.pairsDo { arg a, b;
	[a, b].postln
}

(
l = Store.getItems;
r = Routine { arg inval;
	l.do { arg item;
		item.embedInStream(inval);
	}
};
)
r.start
r.reset();


Store.global.orderedItems;


l[0]

l[0]


(
r = Store.global.getRoutine;
r.play;
);




l.remove

Store.postTree;

Store.global.values;
a

a.addAll([1001, 1002, 1003, 1004, 1005, 1006])



a

a = p[1001]
SortedList



j = Store.global[1001]

j.use { 
	~timingContext.postln;
}

j.parent_(Store.global);

j.parent

j.use {
	~timingContext.postln;
};

j
Store.patch((1003: [nil]));

4.postln;

a.use { 
	~timingContext.bpm.postln;
};



m.codeForSoundfileMod