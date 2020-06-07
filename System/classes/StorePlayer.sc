StorePlayer {
	var store;
	*new { arg store;
		^super.newCopyArgs(store)
	}

	play {
		var routine = this.getRoutine;
		store.getModule !? { arg mod;
			mod.play(this);
		};

		routine.play;
	}

	getRoutine {
		var orderedItems = store.getItems;
		var itemsWithDelta = orderedItems.collect { arg item, idx;
			if (idx != (orderedItems.size - 1)) {
				item[\delta] = orderedItems[idx + 1].timestamp - item.timestamp;
			}
		};
		// itemsWithDelta.postln;

		^Routine { arg inval;
			itemsWithDelta.do { arg item;
				item.postln;
				item.embedInStream(inval);
				item.play;
			}
		}
	}
}