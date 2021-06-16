~inheritEvents = #{ arg event ... parentEvents;
	parentEvents.do { arg parentEvent;
		event.insertParent(parentEvent);
	};
	event.putAll((md: (path: ~path, memberKey: event.type)));
};

~codeEvent = (
	(

		type: 'codeEvent',
		getModule: { arg ev;
      Mod(ev.src);
		},
		play: {
			var ev = currentEnvironment;
			var mod = ev.getModule;
			mod.play(ev);
		},
    create: { arg ev, concreteParams;
      var newEvent = ();
      newEvent.parent_(ev);
      newEvent.putAll(concreteParams);
      newEvent;
    }
	)
);

~timingEvent = ~inheritEvents.value(
	(
		type: 'timingEvent',
		beats: 0,
		dur: 1
	),
	~codeEvent
);


~sequencerEvent = ~inheritEvents.value(
	(
		type: 'sequencerEvent',
		row: 0,
	),
	~timingEvent,
);
~midiNoteEvent = ~inheritEvents.value(
	(
		midinote: { arg ev;
			-1 * ev.row + 64 
		}
	),
	~sequencerEvent
);



// one loop segment
SynthDef(\stereo_player, { arg
	out = 0,
	buf = 0,
	gate = 1,
	startPos = 0,
	sustain = 1,
	amp = 1,
	atk = 0.01,
	decay = 0.01
	;

	var sig = PlayBuf.ar(2, buf,
		startPos: startPos * BufFrames.kr(buf),
		loop: 0
	);

	var env = EnvGen.kr(
		Env.asr(atk, amp, decay),
		gate: gate,
		doneAction: Done.freeSelf
	);
  Out.ar(out, (sig * env));
}).add;

~sampleEvent = ~inheritEvents.value(
	(
		type: 'sampleEvent',
		soundfile: nil,
		startPos: 0,
		atk: 0.05,
		decay: 0.05,
		instrument: 'stereo_player',
		play: {
			var soundfileMod = ~soundfile.asSoundfileMod;
			(
				instrument: ~instrument,
				atk: ~atk,
				decay: ~decay,
				sustain: ~sustain,
				startPos: ~startPos,
				buffer: soundfileMod.buffer
			).play;
		},
    auxiliaryActions: [
      { MenuAction("edit soundfile", {"action pressed".postln}); }
    ]
	),
	~sequencerEvent
);

~automationEvent = ~inheritEvents.value(
	(
		type: 'automationEvent',
		automations: Dictionary()
	),
	~timingEvent
);

~storeEvent = ~inheritEvents.value((type: 'storeEvent'), ~sequencerEvent)






