Store {
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