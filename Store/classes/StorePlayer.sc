StorePlayer {
	var <store;
	var loopPoints;
	var <routine;
	var position;
	var nextPosition;
	var bpm;
	var <lastLoopOffset;
	
	*new { arg store;
		^super.newCopyArgs(store).init();
	}

	init {
		lastLoopOffset = 0;
		Dispatcher.addListener(
			'storeUpdated',
			this,
			{ arg payload, player;
			if (
				payload.storeId == player.store.id && this.shouldInterruptRoutine(payload)
			) {
				this.stop;
				this.play(position);
			}
		})
	}

	currentPosition {
		^routine.clock.beats - lastLoopOffset;
	}

	shouldInterruptRoutine { arg payload;
		var timestampUpdatesBeforeNextEvents;

		routine ?? {
			^false
		};
		
		timestampUpdatesBeforeNextEvents = payload
			.timestampUpdates
			.select({ arg ts; (ts > this.currentPosition) && (ts <= nextPosition)});

		^(
			(payload.timingContext.notNil) || /* bpm has changed */
			(timestampUpdatesBeforeNextEvents.size > 0) || /* something has moved 
				or been deleted in between current position and next position */
			(payload.transportContext.notNil) /* loop points have changed */
		)
	}

	stop {
		var currentBeats, stopPosition;
		position = this.currentPosition;
		routine.stop;
		routine = nil;

		Dispatcher((
			type: 'storeNotPlaying',
			payload: (
				storeId: store.id,
				player: this,
				stopPosition: this.currentPosition
			)
		));

	}

	play { arg start = 0, quant;
		var timingContext, transportContext, loopPoints;
		routine !? {
			^routine;
		};

		timingContext = store.getTimingContext;
		transportContext = store.transportContext;
		loopPoints = store.transportContext.loopPoints;
		
		routine = this.getRoutine(
			start,
			loopPoints,
		);

		store.getModule !? { arg mod;
			mod.play(this);
		};
		
		lastLoopOffset = start;
		routine.play(
			TempoClock(timingContext.bpm / 60, start),
			quant,
		);

		Dispatcher((
			type: 'storePlaying',
			payload: (
				storeId: store.id,
				player: this,
				startPosition: start
			)
		));
		^routine;
	}

	getNextEventGroup { arg events, start = 0, strictly = true, loopPoints;
		var timestamp, futureEvents;

		futureEvents = events.select({ arg event;
			(event.timestamp > start) || (strictly.not && event.timestamp == start)
		});

		if (futureEvents.size == 0) {
			^nil
		};

		timestamp = futureEvents[0].timestamp;
		

		^(timestamp: timestamp, events: futureEvents.select({ arg ev; ev.timestamp == timestamp }));
	}

	postBeats {
		postf("pos: %\n", position);
	}

	playEvents { arg events;
		store.getModule !? { arg module;
			^module.playInStore(store, events);
		};
		
		^events.collect({ arg ev;
			ev.play;
		});
	}

	waitAndPlay { arg pos, nextPos, events;
		var delta;
		position = pos;
		nextPosition = nextPos;
		delta = nextPosition - position;
		if (delta > 0) {
			delta.wait;
			position = nextPosition;
		};
		events !? {
			this.playEvents(events);
		}
	}

	tick { arg inval, eventGroup, loopPoints;
		var nextEvents = eventGroup ?? {
			this.getNextEventGroup(
				store.getItems,
				inval
			);
		};

		if (nextEvents.notNil) {
			if (loopPoints.notNil && nextEvents.timestamp >= loopPoints[1]) {
				^this.handleLoopPoints(inval, *loopPoints)
			};

			this.waitAndPlay(
				inval,
				nextEvents.timestamp,
				nextEvents.events
			);
			
			^nextEvents.timestamp;
		} {
			loopPoints !? {
				^this.handleLoopPoints(inval, *loopPoints)
			};
			nil.yield;
		}
	}

	handleLoopPoints { arg inval, loopStart, loopEnd;
		var nextEvents;
		this.waitAndPlay(inval, loopEnd);
				
		
		nextEvents = this.getNextEventGroup(
			store.getItems,
			loopStart,
			strictly: false
		);
			
		lastLoopOffset = thisThread.clock.beats - loopStart;
		this.waitAndPlay(
			loopStart,
			nextEvents.timestamp,
			nextEvents.events
		);

		^nextEvents.timestamp;
	}

	getRoutine { arg start = 0, loopPoints;
		^Routine({ arg inval;
			var firstEvents = this.getNextEventGroup(
				store.getItems,
				start,
				strictly: false,
			);

			inval = this.tick(
				start,
				firstEvents,
				loopPoints,
			);

			loop {
				inval = this.tick(
					inval,
					loopPoints: loopPoints,
				);
			}
		})
	}
}