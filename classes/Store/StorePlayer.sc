StorePlayer {
	var <store;
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
			(timestampUpdatesBeforeNextEvents.size > 0) ||  /*something has moved 
				or been deleted in between current position and next position */
			(payload.transportContext.notNil) /* loop points have changed */
		)
	}

	play {
		Dispatcher.addListener(
			'storeUpdated',
			this,
			{ arg payload, player;
			if (
				payload.storeId == player.store.id && player.shouldInterruptRoutine(payload)
			) {
				player.resetRoutine;
			}
		});
		Dispatcher((
			type: 'playerStarted',
			payload: (
				player: this,
				startPosition: start
			)
		));
		^routine.play;
	}

	stopRoutine {
		routine.stop;
	}

	resetRoutine {
		position = this.currentPosition;
		routine.stop;
		routine = this.getRoutine(position);
	}

	stop {
		this.stopRoutine;
		Dispatcher.removeListener(
			'storeUpdated',
			this,
		);
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
		
		events.postln;
		"".postln;
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
		// jump back to beginning of loop
				
		
		nextEvents = this.getNextEventGroup(
			store.getItems,
			loopStart,
			strictly: false
		);


		if (nextEvents.isNil) {
			// if next events is nil it means this loop is empty - might as well stop the routine now
			^nil.yield;
		};
			
		lastLoopOffset = thisThread.clock.beats - loopStart;

		nextEvents.postln;
		store.id.postln;

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

			inval = this.tick(
				start,
				firstEvents,
			);

			loop {
				inval = this.tick(inval);
			}
		})
	}
}
