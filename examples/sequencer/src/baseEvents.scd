~timingTypes = (
	beats: 'beats',
	absolute: 'absolute'
);

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
			Mod.all[ev.src.asSymbol];
		},
		play: {
			var ev = currentEnvironment;
			var mod = ev.getModule;
			mod.play(ev);
		}
	)
);

~timingEvent = ~inheritEvents.value(
	(
		type: 'timingEvent',
		timestamp: { arg ev;
			var timestamp = if (ev.unit == 'beats') {
				ev[ev.unit]; // events will be played on a tempo clock so beats is the more universal measure
			} {
				ev[ev.unit] * ev.timingContext.bpm / 60; // a time in seconds is converted to a beat value
			};
			timestamp;
		},
		absolute: 0,
		beats: 0,
		unit: 'beats',
		length: 1
	),
	~codeEvent
);


~sequencerEvent = ~inheritEvents.value(
	(
		type: 'sequencerEvent',
		getEmbedView: { arg ev, zoom;
			SequenceableBlock(ev, zoom);
		},
		getView: { arg ev;
			var view = EnvirGui(ev)
				.putSpec(\row, ControlSpec(0, 128, \lin, 1, 0));

			view.viewForParam('id').visible_(false);
			view.parent.name = ev.id;
			view;
		},
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
		getEmbedView: { arg ev, zoom;
			SequenceableSoundfileBlock(ev, zoom);
		},
		getView: { arg ev;
			var view = EnvirGui(ev)
				.putSpec('row', ControlSpec(0, 128, 'lin', 1, 0))
				.putSpec('startPos', ControlSpec(0, 1, 'lin'));

			view.viewForParam('id').visible_(false);
			view.parent.name = ev.id;
			view;
		},
		atk: 0.05,
		decay: 0.05,
		instrument: 'stereo_player',
		play: {
			var soundfileMod = ~soundfile.asSoundfileMod;
			(
				instrument: ~instrument,
				atk: ~atk,
				decay: ~decay,
				sustain: ~length,
				startPos: ~startPos,
				buffer: soundfileMod.buffer
			).play;
		}
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








