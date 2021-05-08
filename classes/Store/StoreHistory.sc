StoreHistory {
	classvar <history, <future;
	classvar <enabled;
	/**
	 * StoreHistory is made up of 2 stacks
	 * history - list of patches
 	 * future  - list of patches
 	 *
 	 * when some change is made to objects in the store
 	 * (and we want to treat these changes as one 'action') 
 	 * eg objects with ids 1001, 1002 we create a HistoryMarker
 	 * out of the state of the objects before the change
 	 *
 	 * a HistoryMarker is an object (dictionary) with shape eg.
 	 * 1001 -> (
 	 *	changedKey1: previousValue1,
 	 *  changedKey2: previousValue2,
 	 * 	changedKey3: previousValue3,
 	 * ),
 	 * 1002 -> (
 	 *	changedKey1: previousValue1,
 	 *  changedKey2: previousValue2,
 	 * 	changedKey3: previousValue3,
 	 * )
	 *
 	 * And then this HistoryMarker is pushed to the history stack 
	 * when we want to restore the store's state to that historymarker
	 * we pop that HistoryMarker from the history stack, prepend it to the future stack
	 * and go through each id in the history marker, and for that object set the changedKey back
	 * to the previousValue
	 *
	 **/

	*initClass {
		history = List();
		future = List();
		enabled = false;
	}

	*enable {
		enabled = true;
	}

	*disable {
		enabled = false;
	}

	*saveHistory { arg historyMarker;
		if (enabled) {
			history.add(historyMarker);
			future = List();
		}
	}

	*undo {
		var patch = history;
		this.restore(history) !? { |marker|
			future.add(marker);
		}
	}

	*redo {
		this.restore(future) !? { |marker|
			marker.postln;
			history.add(marker)
		}
	}

	*restore { arg list;
		if (enabled) {
			if (list.size > 0) {
				var patch = list[list.size - 1];
				list.do(_.postln);
				Store.patch(patch, nil, false);
				^patch
			}
			^nil
		} {
			^nil
		}
	}

	*clearHistory {
		history = List();
		future = List();
	}
}