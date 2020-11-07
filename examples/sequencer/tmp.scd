a = (
	a: Val((a: 1))
);



a = Value((a: 1).parent_((metadata: (path: "", memberKey: 'mod'))))

a.metadata
a.know

a.metadata

a.a = 2;

a.



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


(
var soundfile = 'data/oxygen'.import;
var sampleEvent = 'sampleEvent'.importFrom('src/baseEvents');
var sequencerEvent = 'sequencerEvent'.importFrom('src/baseEvents');
var events = [
	(soundfile: soundfile.path, beats: 0, startPos: 0, length: 2),
 	(soundfile: soundfile.path, beats: 2, startPos: 0, length: 2),
 	(soundfile: soundfile.path, beats: 4, startPos: 0, length: 2),
 	(soundfile: soundfile.path, beats: 6.5, startPos: 0.2, length: 2 ),
 	].collect(_.parent_(sampleEvent));

events = events.add((beats: 0, row: 2).parent_(sequencerEvent));


events.do { |event, index|
	Store.addObject(event);
};


)






Dictionary