Store : Dictionary {
	classvar global;
	classvar lastId = 1000;
	classvar <lookups;


	var <type = 'store';
	var <>id;
	
	*getId {
		lastId = lastId + 1;
		^lastId;
	}

	*global { ^global }
	*global_ { arg obj; global = obj; }

	*initClass {
		lookups = Dictionary();
		global = this.new().addTimingContext;
	}

	addTimingContext { arg ctx = ();
		var defaultTimingCtx = (bpm: 60);
		super.put('timingContext',
			defaultTimingCtx.putAll(ctx)
		);
	}

	getTimingContext {
		this.at('timingContext') !? { arg ctx;
			^ctx
		} ?? {
			var parent = this.getParent;
			^parent.getTimingContext 
		};
	}

	getParent {
		var path = this.getPath;
		if (path.size > 1) {
			^Store.at(path[ path.size - 2 ]);
		} {
			^global
		};
	}

	*getPath { arg id;
		^lookups[id];
	}
	getPath {
		^lookups[id];
	}

	*setPath { arg id, path;
		if (id.class == Integer) {
			lookups[id] = path;
		}
	}

	*archive { arg path;
		global.writeMinifiedTextArchive(path);
	}

	*readFromArchive { arg path;
		global = path.load;
		this.resetPaths;
	}

	*resetPaths {
		var pathTraverse = { arg store, maxArchiveId, path;
			store.keysValuesDo { arg id, value;
				if (id.class == Integer) {
					maxArchiveId = max(maxArchiveId, id);
					this.setPath(id, path ++ [id]);
					if (value.type == 'store') {
						pathTraverse(value, maxArchiveId, path ++ [id])
					}
				}
			};
			maxArchiveId;
		};
		lastId = pathTraverse(global, 0, []);
	}

	*new { arg ...objects;
		^super.new.init(*objects)
	}

	init { arg ...objects;
		objects !? {
			objects.do { arg obj;
				this.addObject(obj);
			}
		};
	}

	put { arg key, value;
		var lookupPath = Store.getPath(id);
		super.at(key) !? {
			// update objectx
		} ?? {
			// new object
			super.put(key, value);
		};

		Store.setPath(key, lookupPath ++ [key]);
	}

	addObject { arg object;
		var id = Store.getId;
		object.id = id;
		this.put(id, object);
	}

	*getStoreOrGlobal { arg storeId;
		^(storeId !? this.at(storeId) ?? global);
	}

	*addObject { arg object, storeId;
		var store = this.getStoreOrGlobal(storeId);
		store.addObject(object);
	}

	*at { arg id;
		var fullPath = lookups[id];
		fullPath ?? { ^nil };
		if (fullPath.size > 1) {
			var parentId = fullPath[fullPath.size - 2];
			^Store.at(parentId).at(id);
		} { 
			^global.at(id);
		}
	}
	
	/*
	patch will look like this
	CURRENT STATE:
	(
		1001: (a: 1, timestamp: 3),
		1002: (a: 1, b: 2, timestamp: 1),
		1003: ( // substore which contains object at 1004
			type: 'store',
			1004: (
				a: 1, b: 2, timestamp: 0
			)
		)
	)

	PATCH:
	(
		1001: (a: 2), // <-- update 'a' key of object 1001
		1002: [nil], // <-- delete object 1002 
		1003: (
			type: 'store',
			1004: [nil] // <-- delete object 1004
		),
		1005: (
			type: 'store',
			new: [
				(a: 1, b: 2, timestamp: 0), // <-- add new object (copy of 1004) to new store 1005
				(a: 2, b: 2, timestamp: 1), // <-- totally new object
			]
			// 'moving' an object around means deleting it and adding a copy of it as a new object somewhere else
			// means a new ID is created for it
		)
	)

	NEW STATE:
	(
		1001: (a: 2, timestamp: 3),
		1003: ( // substore is now empty because 1004 was moved out
			type: 'store',
		)
		1005: (
			type: 'store',
			1006: (a: 1, b: 2, timestamp: 0), // <-- copy of old 1004 but with new ID
			1007: (a: 2, b: 2, timestamp: 1), // <-- new object
		)
	 )
	*/

	patch { arg patch, history;
		patch.keysValuesDo { arg id, subPatch;
			var target = this.at(id);
			case 
				{ id == 'new' } { ^subPatch.do { arg object; this.addObject(object)} }
				{ target.notNil && subPatch == [nil] } { this.deleteObject(id) }
				{ target.notNil && target.type == 'store' } { ^target.patch(subPatch) }
				{ target.notNil } { ^this.updateObject(id, subPatch) }
			;
		};
	}

	*patch { arg patch, storeId, history;
		var store = storeId !? this.at(storeId) ?? global; 
		store.patch(patch, history);
	}

	deleteObject { arg id;
		super.put(id, nil);
		lookups[id] = nil;
	}

	updateObject { arg id, newState, history;
		var object = this.at(id);
		var diff = getDiff(object, newState);
		// var historyMarker = getDiff(newState, object);
		if (diff.size > 0) {
			diff.keysValuesDo { arg key, value;
				object[key] = value
			}
		}
	}

	*postTree {
		global.postTree;
	}

	postTree { arg obj, tabs = 0;
		if(obj.isNil, { obj = this });
		
		if (obj.type == 'store') {
			"".postln;

			obj.keysValuesDo({ arg key, value;
				tabs.do({ Char.tab.post });
				
				key.post;
				":".post;
				
				this.postTree(value, tabs + 1)
			});

		} {
			Char.tab.post;
			obj.asString.postln;
		};
	}

	*getItems { arg storeId;
		var store = this.getStoreOrGlobal(storeId);
		^store.getItems;
	}

	getItems {
		var returnDict = Dictionary();
		this.keysValuesDo { arg key, value;
			if (key.class == Integer) {
				returnDict[key] = value;
			}
		}
		^returnDict;
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
			history.postln;
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

	*saveHistory { arg historyMarker;
		if (enabled) {
			history.add(historyMarker);
			future = List();
		}
	}

	*restoreFromHistory {
		"restore from past".postln;
		this.restore(history) !? { |marker|
			marker.postln;
			future.add(marker);
		}
	}

	*restoreFromFuture {
		"restore from future".postln;
		// future.postln;
		this.restore(future) !? { |marker|
			marker.postln;
			history.add(marker)
		}
	}

	*restore { arg list;
		if (enabled) {
			if (list.size > 0) {
				var patch = list[list.size - 1];
				Store.patch(patch, false);
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