StoreRoutine {
	var store;
	var ignoreLoop;
	var start;
	var <position, <nextPosition, <lastLoopOffset;
	var <routine;

	*new { arg store, start, ignoreLoop;
		^super.newCopyArgs(store, ignoreLoop ? false, start ? 0).init();
	}
	
	init {
		lastLoopOffset = 0;
		routine = this.getRoutine(start);
	}

	currentPosition {
		^routine.clock.beats - lastLoopOffset;
	}

	play {
		Dispatcher((
			type: 'playerStarted',
			payload: (
				player: this,
				startPosition: start
			)
		));
		^routine.play;
	}

	stop {
		Dispatcher((
			type: 'playerStopped',
			payload: (
				player: this,
				stopPosition: this.currentPosition
			)
		));
		routine.stop;
		routine = nil;
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
			^module.play(store, events);
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

	tick { arg inval, eventGroup;
		var loopPoints = store.getLoopPoints;
		var nextEvents = eventGroup ?? {
			this.getNextEventGroup(
				store.getItems,
				inval
			);
		};

		if (nextEvents.notNil) {
			if (loopPoints.notNil && ignoreLoop.not && (nextEvents.timestamp >= loopPoints[1])) {
				^this.handleLoopPoints(inval, *loopPoints)
			};

			this.waitAndPlay(
				inval,
				nextEvents.timestamp,
				nextEvents.events
			);
			
			^nextEvents.timestamp;
		} {
			if (loopPoints.notNil && ignoreLoop.not) {
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

	getRoutine { arg start;

		^Routine({ arg inval;
			var firstEvents = this.getNextEventGroup(
				store.getItems,
				start,
				strictly: false,
			);

			this.postBeats;
			inval = this.tick(
				start,
				firstEvents,
			);
			this.postBeats;

			loop {
				this.postBeats;
				inval = this.tick(inval);
			}
		})
	}
}

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

	*play { arg store, startPos;
		^super.newCopyArgs(store).init().play(startPos);

	}

	setStore { arg argstore;
		store = argstore;
	}

	init {
		lastLoopOffset = 0;
		Dispatcher.addListener(
			'storeUpdated',
			this,
			{ arg payload, player;
			if (
				payload.storeId == player.store.id && player.shouldInterruptRoutine(payload)
			) {
				player.stop;
				player.play(position);
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

	play { arg start, quant;
		var timingContext, transportContext, loopPoints;
		routine !? {
			routine.postln;
			^routine;
		};

		timingContext = store.getTimingContext;
		transportContext = store.transportContext;
		loopPoints = store.transportContext.loopPoints;
		
		routine = StoreRoutine(store, start);

		// store.getModule !? { arg mod;
		// 	mod.play(this);
		// };
		
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
	}
}