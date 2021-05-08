~timingTypes = (
	beats: 'beats',
	absolute: 'absolute'
);

~inheritEvents = #{ arg event ... protoEvents;
	protoEvents.do { arg protoEvent;
		event.insertParent(protoEvent);
	};
	event;
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

~rowEvent = (
	type: 'rowEvent',
	row: 0
);

~sequencerEvent = ~inheritEvents.value(
	(
		type: 'sequencerEvent',
		getEmbedView: { arg ev, zoom;
			SequenceableBlock(ev, zoom);
		}
	),
	~timingEvent,
	~rowEvent
);

~midiNoteEvent = ~inheritEvents.value(
	(
		midinote: { arg ev;
			-1 * ev.row + 64 
		}
	),
	~sequencerEvent
);


~sampleEvent = ~inheritEvents.value(
	(
		type: 'sampleEvent',
		soundfileData: nil,
		startPos: 0,
		getEmbedView: { arg ev, zoom;
			SequenceableSoundfileBlock(ev, zoom);
		},
		getView: { arg ev;
			// open a soundfile editor from here
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











