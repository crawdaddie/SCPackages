(
	startPos: 0.2,
	beats: 6.5,
	length: 2,
) 


g = Store.global;

e = g[1002, 1003];
e
e.proto

{

	{"yielded".postln}.yield;
}.fork;


k.play(1.5);




j = Store.at(1006)




j.postTree;

j

k.play;

k.routine.beats;
SimpleNumber


(
c = nil;

{
	var a, b;
	#a, b = nil;
	a.postln;
	b.postln;

}.value();
)
a
b

k.play;

k.stop;
SimpleNumber

g = Store.global;



g.postTree;
.wait
k.routine.clock.beats;

k.lastLogicalTime;


k.currentPosition;


(
g = Store.global;
g.patch((
	transportContext: (loopPoints: [nil]),
));
g.postTree;
)




l = (
	transportContext: (loopPoints: nil),
	timingContext: (bpm: 60)
);



l;

g.postTree;


(
g = Store.global;
g.patch((
	transportContext: (loopPoints: [4, 8]),
));


// "".postln;
// g.patch((
// 	transportContext: (loopPoints: nil),
// 	timingContext: (bpm: 60)
// ));
g.postTree;
)

k.play;


(
g = Store.global;

// k.play;
)
.yield

Object

g.transportContext

g.postTree;
g.patch((
	1004: (
		beats: 4.0
	)
))

thisThread.seconds;
thisThread.seconds;


g.postTree;
g.timingContext 

g.postTree;

g.timingContext
g.loopPoints

g.postTree;
g.patch((loopPoints: [2, 8]));

g.setLoopPoints([2, 8])


j = Store.at(nil)


j.postTree;

Store.at(0)

j




j[2]



g.getItems

j

(


var getFirstEvents = { arg futureEvents;
	var firstTimestamp = futureEvents[0].timestamp;
	var firstEvents = futureEvents.select({ arg item;
		item.timestamp == firstTimestamp;
	});
	(timestamp: firstTimestamp, events: firstEvents);
};

var getNextEvents = { arg array, time;
	var futureEvents = array
		.select({ arg item;
			item.timestamp > time;
		});
	if (futureEvents.size != 0) {
		getFirstEvents.(futureEvents);
	} {
		nil;
	}
};

var playEventGroup = { arg eventGroup;
	eventGroup.events.do { arg ev;
		ev.postln;
	};
	"".postln;
};


g = Store.global;



j = [
	g[1001],
	g[1002],
	g[1002].putAll((channel: 3)),
	g[1003],
	g[1004]
];



r = Routine({ arg inval;
	var prevTime;
	var delta;
	var nextEvents = getFirstEvents.(j);
	playEventGroup.(nextEvents);
	prevTime = nextEvents.timestamp;

	inf.do {
		nextEvents = getNextEvents.(j, prevTime);
		if (nextEvents.notNil) {
			delta = nextEvents.timestamp - prevTime;
			r.beats.postln;
			delta.wait;
			playEventGroup.(nextEvents);
			prevTime = nextEvents.timestamp;
		} {
			nil.yield
		};
	}
});
r.play;
)

r.stop;
r.resume;


Task

k = [1,2,3];
l = k.select({ arg i; i > 1})

if (l.size != 0) { l } { nil };



(
a = (
    pattern: Pbind(\note, Pgeom(1, 1.1, { 20.rand }), \dur, 0.05),
    embedInStream: { |event, inevent| event[\pattern].embedInStream(inevent) }
);
b = (note: [2, 5]);
c = (freq: 402, dur: 0.3);
Prand([a, b, c], inf).trace.play;
)

b = (note: [0, 3, 7]);



j[0].proto

(
r = Routine { arg inval;
    loop {
        // thisThread refers to the routine.
        postf("beats: % seconds: % time: % \n",
            thisThread.beats, thisThread.seconds, Main.elapsedTime
        );
        1.0.yield;

    }
}.play;
)

r.stop;
r.beats;
r.seconds;
r.clock;

j = [
	(note: [0, 3, 7])
]

j = Store.global.getItems[2]


j.timestamp




8/6 * 1.5

b.play;

k = Dictionary[];
Store.global.getTimingContext;
k.size

k.play;
k.routine.clock.beats;



k.routine.state;
k.routine.isPlaying

j[0].timestamp

j[1]


Task

.elapsedTime


.wait;