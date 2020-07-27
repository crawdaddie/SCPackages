u = 'updateTestEvent'.importFrom('src/baseEvents');

u

Mod.all.at('/Users/adamjuraszek/PROJECTS/supercollider/packages/examples/sequencer/src/baseEvents.SCD')

Mod.all.at('/Users/adamjuraszek/PROJECTS/supercollider/packages/examples/sequencer/src/baseEvents.scd').updateTestEvent


Mod.all.keysValuesDo { arg key, val;
	key.postln
};

S(1005)

S(1002).row




compileString
o = StoreEvent((a: 1), l.md)


o = 

k = S(1002).getView


k
o.


o.dummyKey

o.parent


storeOn
o = S(1002)

k = o.copy


asCompileStri

a = { 10.do { 10.postln } };
a.asCompileString.postcs;
a.postcs;


l = 'sampleEvent'.importFrom('src/baseEvents');

k = StoreEvent((a: 1), l)

k.asCompileString

k.parent

k.asCompileString


ng

o.parent

k.archiveAsCompileString
k.checkCanArchive;
Object

k = { arg a;
a.postln
};

o.asCompileString
4

k.archiveAsCompileString
k.asCompileString;

o.parent
o.parent
o.dummyKey

o.parent

Array

l = Mod.all.at(o.parentMetadata.path)[o.parentMetadata.memberKey]

S(1002).parent

S(1002)['dummyKey']


Store.postTree;


l
o.class


Store.postTree;

Event


EnvirGui(S(1002))

o = 
S(1002).getView

S
o





o.parentMetadata


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



~sf = 'sampleEvent'.importFrom('src/baseEvents');
~seqE = 'sequencerEvent'.importFrom('src/baseEvents');

k = Store.readFromrchive("/Users/adamjuraszek/PROJECTS/supercollider/packages/examples/sequencer/saves/opentest2.scproj");


k = "/Users/adamjuraszek/PROJECTS/supercollider/packages/examples/sequencer/saves/opentest2.scproj".load;

Mod.new(S(1002).parentMetadata.path)[S(1002).parentMetadata.memberKey]['getEmbedView'].value(S(1002), 1)
.getEmbedView.postcs;

Dispatcher.listeners['moduleReload']



Mod.all['/Users/adamjuraszek/PROJECTS/supercollider/packages/examples/sequencer/src/baseEvents.scd'].sampleEvent.dummyKey;


(
t = { arg value, events;
	if (value.class == Mod) {
		value.keysValuesDo { arg key, value;
			t.(value);
		}
	} {
		try {
			if (value.md) {
				value.postln;	
			}
		}
	};
	// if (value.tryPerform('md').notNil) {
	// 	value.postln;
	// }
};

Mod.all.keysValuesDo { arg key, value;
	t.(value);
}
)
x

Store.global.orderedItems[0]


S().push;


S().push;
4
Store.push;

Store()
S.push


~items[0].type;


~items[0].beats = 1;
~items[0].row = 3;


'src/baseEvents'.asModule.open;
~items[0].timestamp
put('beats', 1)

~items[0].getEmbedView;


~items[0].beats = 1;

~items[0].put('beats', 1)

~items[2]
~items[2].beats = 1;

Store.global.use {
	// ~transportContext.loopPoints = [2, 8];
	// currentEnvironment.class.postln;
	currentEnvironment.getItems()
};

Store.global.push;
Environment

~items = Store.global.getItems()


Store.global.patch((
	1005: (
		beats: 2
	))
);


S.push;

~i = ~items[0];=

a  (a: 1)



Store.at(1001).beats = 2;

S().pop;



~i.put('beats', 0)

~i.beats = 2;


~i.getPath

~i.beats = 1;





Store.postTree;

~items


~i = ~items[0]


~i.beats = 1;

~i.put('beats', 1)

~i.put('length', 2)


~i.beats = 1;
Store.postTree;
~i = ~items[0]


~i.put('beats', 1)
~i.beats = 2;

Dictionary



k = ~items[0];
currentEnvironment[1005].row = 3;
.beats = 2;


k.beats = 2;

Store.at(1005).length



k

Envor

Environment

Store.patch((timingContext: (bpm: 165)))


S(1002).dummyKey

k.parent.name = "1001"
k = S(1002).getView
k.viewForParam('id').visible_(false)
k.editKeys

Store.lookups.keysValuesDo { arg key, value;
	key.postln
};


Dispatcher.listeners

Dispatcher.addListener('moduleReload', 'system', { arg payload;
	"module reload".postln;
	payload.postln;
});





S.push;


~i = Store.at(1005)


~i.beats

~i.beats = 1;



4


~i = ~items[0]



~i.beats = 1;

~i.put('beats', 1)





~i = ~items[0];


~i.beats = 1;

~i.put('beats', 1)






k = (a: 1).putAll()

k = ().putAll()

Store.global.orderedItems[2

Dispatcher.listeners

S(1002).dummyKey

k.at(1002)

S(1002).parent
S(1002).getEmbedView

Store.pathTraverse { arg key, value, store, path;		


Store.pathTraverse({ arg key, value, store, path;

	store.put(key, StoreEvent(value, value.parent.md));
});

S(1002).postln;


Dispatcher.listeners['objectUpdated']

S(1007).parent_(~sf);
S(1006).parent_(~seqE)

S(1005).parent_(~seqE)

S(1005)

S(1002).parent

Store.archive("/Users/adamjuraszek/PROJECTS/supercollider/packages/examples/sequencer/saves/opentest2.scproj")


Object

m = 'src/baseEvents'.import;

m.sampleEvent.md.path


m.updateTestEvent.md.notNil

Store.patch((
	transportContext: (loopPoints: [4, 8])
	))

j = (a: 1, b: 2);






~i = ~items[0]


~i.know

S.push;
~i.beats = 4;


~i.beats

~i.use {
	~beats = 1;
}

Store.at(1001).beats_(3)

j.put('b', 1)

Event

j.b = 2;
Store.at(1001).know_(true)



Store.at(1001).beats = 2;
j
j.parent = u


a 

j.buffer