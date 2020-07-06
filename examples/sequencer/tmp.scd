(
	startPos: 0.2,
	beats: 6.5,
	length: 2,
) 

4.postln;

g = Store.global;
g.getPath.size
g.getOffset

Store.at(1007).getOffset;
Store.at(1007)
Store.lookups.put(1007, [1006, 1007]);

Store.at(1006).getOffset
q.keysValuesDo { arg key, value;
	if (value.class == Function) {
		value.value(q, 1).postln;		
	}
};

1.dumpFullInterface

q.respondsTo('keysValuesDo')

q = { arg ignoreLoop;

}


q.value(ignoreLoop2: false);


Store.patch


g = Store.global
Object.dumpInterface



q.perform('open', 1)

a.keysValuesDo { arg a, b;
	[a, b.class].postln;
};

a.proto_(b)


a.keys.includes('c')

g = Store.global;

g.patch((transportContext: (loopPoints: [2, 8])));

4.postln;
j = Store.at(1007);

j.postTree;

[0, 1] - 1;

j.transportContext

(
g = Store.global;
r = StoreRoutine(g, 1, ignoreLoop: true);
r.play;
)

l = Store.at(1007)
l.src
l.play;

l.postTree;
l.getModule;

l.getModule.play;

m = Mod.new(l.src)

roto.postTree
l.src


l.play = { arg ev;
	ev.postln;
}
Store.postTree;


Store.at(1006)

Store.keysValuesDo
l.play
TextView().front;
(
w = Window.new.front;
a = TextField(w, Rect(10, 10, 150, 20));
a.string = "hi there";
a.action = {arg field; field.value.postln; };
)
Dialog.openPanel();
File
l.parent.parent.parent.parent

Store.methods.collect(_.name)
u = 'system'.import;
u.postln;
Class
Object
k = Store.dumpInterface

k = File.readAllString(

File.use("/Users/adamjuraszek/PROJECTS/supercollider/packages/classes/Project/recentProjects", "w", { |f|
	f.write("write value");
});

Project();
Project.recentProjects;
k.split($\n)

File
Set
String
k =[1,2,3,1,4];

Dialog.savePanel({ arg path;
	path.basename.postln;
});

k = k.as(OrderedIdentitySet)

k.asArray.join("\n")


k = [0] ++ k;


k


k.prepend(0)

j = Set(*[1,2,3,4])
k = [0] ++ k;
.add

k = "/Users/adamjuraszek/PROJECTS/supercollider/_modules/system/index.scd"
k.dirname.dirname

Project.recentProjects;

k.basename


Store.postTree;

k.currentPosition;
Store.global.getLoopPoints

Store.at().getLoopPoints

k = "/Users/adamjuraszek/PROJECTS/supercollider/_modules/system/index.scd";

p = "/Users/adamjuraszek/PROJECTS/supercollider/_modules/system"; 


(p +/+ "indexx.scd").pathMatch
.includes((p +/+ "index.scd"));


p +/+ "index.scd"

Store.at(1001).inspect;


system/index.scd


(
g = Store.global;
g.patch((
	transportContext: (loopPoints: [4, 8]),
));

)

~sys.player.currentPosition;


Store.postTree;
g[1007].playInStore(1, 2);
g[1007].getModule

g[1007]..playInStore(1, 2)

g = Store.global;
g.play;

k.play;

g = Store.at()

g.play;





g.postTree;

k.currentPosition;

g.postTree;
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


Project("/Users/adamjuraszek/PROJECTS/supercollider/packages/examples/sequencer/saves/opentest.scproj");
Project.recentProjects
asArray.join("\n")

k = [1,2];
Set[*([1] ++ k)]


Set[1,2,3]

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
	g[1002].putAll((row: 3)),
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

Project();

Project.recentProjects;

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


Project();
S
Task

.elapsedTime


.wait;