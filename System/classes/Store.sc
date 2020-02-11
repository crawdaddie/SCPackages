Store {
	classvar <objects;
	classvar <base;
	classvar lastId;

	*getId {
		lastId = lastId + 1;
		^lastId;
	}


	*initClass {
		var baseId;
		lastId = 1000;
		
		baseId = this.getId;
		objects = Dictionary();
		base = (id: baseId, type: 'aggregate', ids: Set());
		objects.put(baseId, base);
	}

	*dispatchHistoryMarker { arg id, aggregate;
		if (StoreHistory.enabled) {
			var object = this.at(id);
			var history = Dictionary();

			history.put(id, this.at(id));
			aggregate !? {
				history.put(aggregate.id, (ids: aggregate.ids));
			};
			StoreHistory.store(history);
		}
	}

	*addObject { arg object, argAggregate;
		var id = this.getId;
		var aggregate = argAggregate ?? base;

		object.id = id;
		object.aggregateId = aggregate.id;
		
		objects.put(id, object);
		aggregate.ids.add(id);

		object.init !? {
			object.init;
		};

		^object;
	}

	*removeObject { arg id;
		var object = Store.at(id);
		var aggregate = Store.at(object.aggregateId);

		// this.dispatchHistoryMarker(id, aggregate);  

		aggregate.ids.remove(id);
		Dispatcher((type: 'objectUpdated', payload: aggregate));
		objects[id] = nil;
	}

	*removeObjects { arg ... ids;
		var aggregates = Set();
		
		ids.do { |id|
			var object = Store.at(id);
			var aggregate = Store.at(object.aggregateId);
			aggregates.add(aggregate);

			// this.dispatchHistoryMarker(id, aggregate);  

			aggregate.ids.remove(id);
			objects[id] = nil;
		};

		aggregates.do { |aggregate|
			Dispatcher((type: 'objectUpdated', payload: aggregate))
		};
	}

	*updateObject { arg id, newState, history = true;
		var object = objects[id];
		var diff = this.getDiff(object, newState);

		if (diff.size > 0) {
			if (history, { this.dispatchHistoryMarker(id) });

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
		var baseId = this.getId;
		objects = Dictionary();
		base = (id: baseId, type: 'aggregate', ids: Set());
		objects.put(baseId, base);
	}

	*replace { arg dict;
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
			var marker = Dictionary();
			state.keysValuesDo { |id, value|
				var object = Store.at(id);
				var subMarker = value.collect { |val, key|
					object[key]
				};
				Store.updateObject(id, value, false);
				marker.put(id, subMarker);
			};

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