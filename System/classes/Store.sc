Store {
	classvar <base;
	classvar <lookups;
	classvar <lastId;
	var <objects;
	var id;
	var <>timestamp = 0; 

	*getId {
		lastId = lastId + 1;
		^lastId;
	}
	
	*initClass {
		lastId = 1000;
		base = Store();
		lookups = Dictionary();
	}

	*new { arg id, timestamp;
		^super.new.init(id, timestamp);
	}

	init { arg argId, argTimestamp = 0;
		objects = IdentityDictionary();
		id = argId ?? this.class.getId();
		timestamp = argTimestamp;
	}

	*addStore { arg timestamp;
		^base.addStore(timestamp);
	}

	addLookupPath { arg newId;
		lookups.put(newId, lookups[id] ++ [newId]);
	}

	addStore { arg timestamp = 0;
		var newId = Store.getId();
		var newStore = Store(newId, timestamp);
		objects.put(newId, newStore);
		this.addLookupPath(newId);
		^newStore;
	}

	addObject { arg object;
		var newId = Store.getId();
		object.id = newId;
		objects.put(newId, object);
		this.addLookupPath(newId);
		^object;
	}

	*updateObject { arg id, newState, history = true;
		var lookupPath = lookups[id];
		var storeId = lookupPath[lookupPath.size - 2];
		var store = Store.at(storeId);
		^store.updateObject(id, newState, history);
	}

	updateObject { arg id, newState, history = true;
		var object = objects[id];
		var diff = getDiff(object, newState);

		if (diff.size > 0) {
			if (history) {
				var historyMarker = diff.collect { |val, key|
					object[key]
				};
				historyMarker.id = id;
				StoreHistory.store(historyMarker)
			};

			object.putAll(diff);
			Dispatcher((type: 'objectUpdated', payload: object));
		};
	}

	at { arg id;
		^objects[id];
	}

	*at { arg id;
		var path = lookups[id];
		var obj = base;
		path.do { |id|
			obj = obj.at(id);
		};
		^obj;
	}

	*archive { arg path;
		(lookups: lookups, base: base, lastId: lastId).writeMinifiedTextArchive(path);
	}
	
	*readFromArchive { arg path;
		var archive = path.load;
		lookups = archive.lookups;
		base = archive.base;
		lastId = archive.lastId;
	}
}


StoreB {
	classvar <objects;

	*initClass {
		objects = Dictionary();
	}

	*addObject { arg object;
		var id = UniqueID.next;
		object.id = id;
		objects.put(id, object);
		object.init !? {
			object.init;
		}
		^object;
	}

	*createAggregate { arg ... objects;
		var ids = Set();
		
		objects.do { |object|
			object.id !? { |id|
				ids = ids.add(id) 
			} ?? {
				var storedObject = this.addObject(object);
				ids = ids.add(storedObject.id);
			}
		};
		^this.addObject((type: 'aggregate', ids: ids));
	}

	*updateObject { arg id, newState, history = true;
		var object = objects[id];
		var diff = this.getDiff(object, newState);

		if (diff.size > 0) {
			if (history) {
				var historyMarker = diff.collect { |val, key|
					object[key]
				};
				historyMarker.id = id;
				StoreHistory.store(historyMarker)
			};

			object.putAll(diff);
			Dispatcher((type: 'objectUpdated', payload: object));
		};
	}

	*getDiff { arg object, newState;
		^newState.select { |val, key|
			object[key] != val;
		}
	}

	*at { arg key;
		^objects.at(key)
	}

	*atAll { arg keys;
		^objects.atAll(keys)
	}

	*clear {
		objects = Dictionary();
	}

	*replace { arg dict;
		this.clear;
		dict.keysValuesDo { |key, val|
			this.addObject(val);
		}
	}
}

StoreHistory {
	classvar <history, <future;
	classvar <enabled;
	/**
	 * StoreHistory is made up of 2 stacks
	 * history - list of HistoryMarkers
 	 * future  - list of HistoryMarkers
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

		Dispatcher.addListener('undo', {
			this.restoreFromHistory;
		});

		Dispatcher.addListener('redo', {
			this.restoreFromFuture;
		});
	}

	*disable {
		enabled = false;
		// Dispatcher.removeListener('')
	}

	*store { arg marker;
		if (enabled) {
			history.add(marker);
			future = List();
		}
	}

	*restoreFromHistory {
		this.restore(history) !? { |marker|
			future.add(marker);
		}
	}

	*restoreFromFuture {
		this.restore(future) !? { |marker|
			history.add(marker)
		}
	}

	*restore { arg list;
		if (list.size > 0 && enabled) {
			var state = list.removeAt(list.size - 1);
			var id = state.id;
			var object = Store.at(id);
			var marker = state.collect { |val, key|
				object[key]
			};

			Store.updateObject(id, state, false);
			^marker
		} {
			^nil
		}
	}

	*clearHistory {
		history = List();
		future = List();
	}
}

Dispatcher {

	classvar listeners;

	*initClass {
		listeners = Dictionary();
	}

	*addListener { arg type, listener;
		listeners.at(type) !? _.add(listener) ?? {
			listeners.put(type, Set[listener])
		}
	}

	*removeListener { arg type, listener;
		listeners.at(type) !? _.remove(listener);
	}

	*new { arg editEvent;
		var type = editEvent.type;
		var typeListeners = listeners.at(type) ?? [];

		typeListeners.do { arg listener;
			listener.value(editEvent.payload)
		};
	}
}