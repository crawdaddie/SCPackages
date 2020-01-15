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
	
t = (
	type: 'timingContext',
	bpm: 60
);
	
StoreHistory.enable;

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
	payload.postln;
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
)

a = SequencerCanvas.fromObjects(Store.objects.values);

rightClick
Rect


Clipboard
x = Point(0,0)

j = x.translate(Point(1,1))



j = (a: 1, b: 2);

k = j.copy;


k.a = 2;
k

j





(
~view = View(bounds:300@100).layout_(VLayout(
    Button()
        .states_([["Options..."]])
        .action_({ ~menu.front }),
    ~text = TextView()
));

~menu = Menu (
    MenuAction("Option A").toolTip_("I like option A..."),
    MenuAction("Option B").toolTip_("But option B is better..."),
    MenuAction("Option C").toolTip_("No one really likes C."),
);

~actionFunc = {
    |obj, what, action|
    if (what == \hovered) {
        ~text.string = action.toolTip
    };
    if (what == \aboutToHide) {
        ~text.string = ""
    }
};
~menu.addDependant(~actionFunc);
~view.onClose_({ ~menu.removeDependant(~actionFunc) }).front;

)

(
s.waitForBoot {
    ~startIcon = Image(40).draw({
        Pen.fillColor = Color.green;
        Pen.moveTo(5@5);
        Pen.lineTo(35@20);
        Pen.lineTo(5@35);
        Pen.lineTo(5@5);
        Pen.fill;
    });
    ~stopIcon = Image(40).draw({
        Pen.fillColor = Color.red;
        Pen.fillRect(Rect(10, 10, 20, 20));
    });

    ~startAction = MenuAction("start")
                    .action_({ ~playSynth.() })
                    .icon_(~startIcon)
                    .toolTip_("Start playing synth.")
                    .shortcut_("space")
                    .font_(Font(bold:true, size:18));
    ~stopAction = MenuAction("stop")
                    .action_({ ~stopSynth.() })
                    .icon_(~stopIcon)
                    .toolTip_("Stop the synth.")
                    .enabled_(false)
                    .shortcut_(".")
                    .font_(Font(bold:true, size:18));
    ~playing = MenuAction("playing")
                    .enabled_(false)
                    .checkable_(true)
                    .checked_(false);

    ~view = View(bounds:300@200).layout_(VLayout(
        ToolBar(~startAction, ~stopAction, MenuAction.separator, ~playing).focus,
        nil,
        Button().states_([["Options..."]]).action_({
            Menu(~startAction, ~stopAction, MenuAction.separator, ~playing).front;
        })
    ));
    ~view.onClose_({ ~stopSynth.() });

    ~synth = nil;
    ~playSynth = {
        if (~synth.isNil) {
            ~synth = { SinOsc.ar(440) * 0.1 }.play;

            ~startAction.enabled = false;
            ~stopAction.enabled = true;
            ~playing.checked = true;

            ~synth.onFree({
                ~synth = nil;
                {
                    ~playing.checked = false;
                    ~startAction.enabled = true;
                    ~stopAction.enabled = false;
                }.defer
            })
        }
    };

    ~stopSynth = {
        ~synth !? { ~synth.free };
    };

    ~view.front;

}
)


z.asCompileString == "nil"

Store.objects.values()

(
	var o, p, n = nil;
	[o,p,n].postln;
)
a.cursorView

o
(
~show = MenuAction("Show Window", {
    ~window ?? {
        ~window = TextView().string_("Here it goes!").minSize_(300@200);
        ~window.onClose = { ~window = nil };
        ~window.front;
    }
});
~hide = MenuAction("Hide Window", {
    ~window !? {
        ~window.close();
        ~window = nil;
    }
});

MainMenu.register(Menu(~show, ~hide).title_("My Quark"), "Quarks", "My Quark")
)

(
View().fixedWidth_(300).layout_(HLayout(
    ToolBar(
        MenuAction("Option A", { "Option A".postln }),
        MenuAction("Option B", { "Option B".postln }),
        MenuAction("Option C", { "Option C".postln })
    )
)).front
)
t
(
View().layout_(HLayout(
    ~text = StaticText().string_("Right-click me").minWidth_(100)
)).front;

~text.setContextMenuActions(
    MenuAction("Red", { ~text.background = Color.red }),
    MenuAction("Green", { ~text.background = Color.green }),
    MenuAction("Blue", { ~text.background = Color.blue })
)
)

h = {
	~menu = Menu(
    MenuAction("A", { "A selected".postln }),
    MenuAction("B", { "B selected".postln }),
    MenuAction("C", { "C selected".postln }),
).front;
};











	h.();



o

(
~view = View().layout_(HLayout(
    ToolBar(
        MenuAction("Min", { ~slider.value = 0 }),
        MenuAction("Max", { ~slider.value = 1 }),
        CustomViewAction(
            ~slider = Slider().orientation_(\horizontal)
        ),
        Menu(
            MenuAction("Randomize", { ~multi.value = 4.collect({ 1.0.rand }) }),
            CustomViewAction(
                ~multi = MultiSliderView().drawLines_(true).elasticMode_(true).value_([0, 1, 0, 1])
            )
        ).title_("Submenu")
    ).minWidth_(400)
)).front;
)