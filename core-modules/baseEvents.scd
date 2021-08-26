var sampleEvent;
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
      Import(ev.src, expose: false);
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
	decay = 0.01,
  rate = 1
	;

	var sig = PlayBuf.ar(2, buf,
    rate: rate,
		startPos: startPos * BufFrames.kr(buf),
		loop: 0
	);

	var env = EnvGen.kr(
		Env.asr(atk, amp, decay),
		gate: gate,
		doneAction: 2
	);
  Out.ar(out, (sig * env));
}).add;

sampleEvent = ~inheritEvents.value(
	(
		type: 'sampleEvent',
		soundfile: nil,
		startPos: 0,
		atk: 0.05,
		decay: 0.05,
		instrument: 'stereo_player',
    rate: 1,
    amp: 1,
		play: {
			var soundfileMod = ~soundfile.value.asSoundfileMod;
			(
				instrument: ~instrument.value,
				atk: ~atk.value,
				decay: ~decay.value,
				sustain: ~sustain.value,
				startPos: ~startPos.value,
				buf: soundfileMod.buffer,
        dur: ~dur.value,
        rate: ~rate.value,
			).play;
		},
	),
	~sequencerEvent
);
~sampleEvent = sampleEvent;
~percEvent = sampleEvent.create((atk: 0));

~getSoundfileEvent = { arg ev, soundfile, base = sampleEvent;
  Import(soundfile);
  base.create((soundfile: soundfile));
};

~automationEvent = ~inheritEvents.value(
	(
		type: 'automationEvent',
		automations: Dictionary()
	),
	~timingEvent
);

~storeEvent = ~inheritEvents.value((
  type: 'storeEvent',
  create: { arg ev, concreteParams;
    var newEvent = ev.parent['create'].value(ev, concreteParams);
    Store.new(newEvent);
  }
),
  ~sequencerEvent
);






