a = (
	a: Val((a: 1))
);






















S.push;

~items[0]

~items[0].put('row', 1)

S.push

~items[0].put('beats', 2);
















~sequencerEvet

Platform.userConfigDir

a = Value((a: 1).parent_((metadata: (path: "", memberKey: 'mod'))))

a.metadata
a.know

a.metadata

a.a = 2;

a.
Dispatcher.debug(false);

Dispatcher.listeners['*']

Dispatcher('new', (a: 1, b: 2))

Dispatcher.debug()

(


	ObjectGui
)


Event

g = Store.global;
g.addObject((beats: 1.0.rand, param: 2));
g.getItems


g.values.select { arg item;
	var id = item['id'];
	id.notNil && id.class == Integer
};


j = g.pairsDo { arg key, item;
	[key, item].postln;
	true;
};



j = (0: 'a', 1: 'b', 2: ['c', 'd'])





j.asSortedArray

d = (a: 5, b: 7, c: 1, d: 0);
d.asSortedArray;


j.powerset



nil.class
g.values
g

g[1001].param = 3

g


g[1001] = nil
D


Dispatcher.debug()
g = Store.global;
g.addObject((beats: 1, b: 2))

g.put(1001, nil)	

g.values


Dictionary

(
j = g.select({ arg value, key;
	[key, value].postln;
	true;
});
)



'*'


g.pairsDo { arg key, value;
	[key, value].postln;
};


g.select


g.itemsFlat[0]
j = Props((
	a: 1,
	b: 2,
	c: 1@1,
));

k = Props().putAll((a: 1), j);

k.c



k

j.c = 2@2;


k = Props((
	d: 1,
	c: j.c
));

j.addDependant(k);


k





k = [1, 2];

j = [3, 4];


l = k ++ j



l = k.addAll(j)


j = Props(
	a: 1,
	b: 2
);



g

.snap


k.

j.a = 2;


g

[].push(1)
g


[ objectUpdated,
( 'transportContext': (  ), 'timingContext': ( 'bpm': 60 ) ), ( 'timingContext': ( 'bpm': 60 ), 1001: ( 'a': 1, 'b': 2, 'id': 1001 ), 'transportContext': (  ) ) ]

TestStore.run;

g


(a: 1, b: 2) == (b: 2, a: 1)


[['a', (a: 1, b: 2)], ['b', (a: 1, b: 2)]] == [['a', (a: 1, b: 2)], ['b', (a: 1, b: 2)]]

nil ++ [1]

Store.global.addObject((timestamp: 0))
Store.postTree;

Project.new;


Store.global;

function
User

e = (
	a: Value((id: 1001, a: 1))
);

e.push;

~a




j = (a: 1, b: 2);

j.class


~a.a = 2;
	

e.a.a = 3;
	
~a.a = 2;

a.b = 3;
(
Dispatcher.addListener('objectUpdated', 'system', { arg payload;
	"listener".postln;
	payload.postln;
});
a = V((
	id: 1001,
	b: 1,
	row: 0,
	beats: 1,
	length: 1
));
);


(
var identifier;
var selected;
var mouseAction;
a = V((
	id: 1001,
	b: 1,
	row: 0,
	beats: 1,
	length: 1
));

identifier = { arg p;
	p.id == a.id;
};

u = UserView();

v = RxBlock(a);


Dispatcher.addListener('objectUpdated', u, { arg payload, u;
	if (identifier.value(payload)) {
		u.refresh;
	}
});

u.drawFunc = { 
	v.renderView;
};

u.mouseDownAction = { arg view, x, y;
	mouseAction = RxMouseAction([v].select(_.contains(x@y)), x, y);
};

u.mouseMoveAction = { arg view, x, y;
	mouseAction.mouseMoveAction(x, y);
	u.refresh;
};

u.mouseUpAction = { arg view, x, y;
	mouseAction.mouseUpAction(x, y);
	mouseAction = nil;
};

u.front;
)


Point(0, 1) - Point(2, 1)

u.canReceiveDrag

Dispatcher(
	type: 'objectUpdated',
	payload: (
		id: 1001,
		beats: 10.0.rand,
		row: 10.rand,
		length: rrand(2, 5),
	)
);


v

a
v.width


v.x



v.x

u.refresh;

v.listen;

u.drawFunc = {
	v.renderView;
};
u.front;
)



g.itemsFlat[1]
.put('beats', 3)


.beats = 3



a.writeMinifiedTextArchive("/Users/adamjuraszek/PROJECTS/supercollider/packages/examples/sequencer/val")
a



h = { arg a;
	a.postln;
	a + 2;
};




k = (a: 1, b: 2);
l = (b: 2, c: 2);
h.valueWithEnvir(k);

try {
	h.valueWithEnvir(l);
};


a

Dispatcher('objce')



a.putAll((c: 2))

a = ();

a.size


a[1002] = (a: 1)

a.b = 3;


V(()).push;






a.push;



currentEnvironment.put('h', 2)
~b = 2;
~c = 3;
~h = 5;

a.put(1002, V((a: 1)))


a[1002]



currentEnvironment
Project.new;

(
var soundfile = 'data/oxygen'.import;
var sampleEvent = 'sampleEvent'.importFrom('src/baseEvents');
var sequencerEvent = 'sequencerEvent'.importFrom('src/baseEvents');
var events = [
	( row: 0, soundfile: soundfile.path, beats: 0, startPos: 0, length: 2 ),
 	( row: 0, soundfile: soundfile.path, beats: 2, startPos: 0, length: 2 ),
 	( row: 0, soundfile: soundfile.path, beats: 4, startPos: 0, length: 2 ),
 	( row: 0, soundfile: soundfile.path, beats: 6.5, startPos: 0.2, length: 2 ),
].collect(_.parent_(sampleEvent));

events = events.add((beats: 0, row: 2).parent_(sequencerEvent));


events.do { |event, index|
	Store.global.addObject(event);
};

)

Object

h = k => k + 1;


valueWithEnvir


g.itemsFlat[0]['getEmbedView']

h.(1)


t = 1;



j = t => t.postln;


j.()




g.itemsFlat[0].put('beats', 2);

1 >= 2;



-

Platform

g = Store.global


Platform.userAppSupportDir
c = SequencerCanvas(Store.global);


Store.global


Dictionary

Store.global.items

TestStore.run;


Store.postTree;



Dispatcher.debug = true;


4

g











Dictionary
