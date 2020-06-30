Store : Event {
	classvar global;
	classvar lastId = 1000;
	classvar <lookups;

	var <>id;
	var <orderedItems;

	var <player;
	
	*getId {
		lastId = lastId + 1;
		^lastId;
	}

	*global { ^global }
	*global_ { arg obj; global = obj; }

	*initClass {
		lookups = Dictionary();
		global = this
			.new()
			.addTimingContext
			.addTransportContext;
	}

	addTimingContext { arg ctx = ();
		var defaultTimingCtx = (bpm: 60);
		super.put('timingContext',
			defaultTimingCtx.putAll(ctx)
		);
	}

	addTransportContext { arg ctx = ();
		var defaultTransportCtx = ();
		super.put('transportContext',
			defaultTransportCtx.putAll(ctx)
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

	setLoopPoints { arg loopPoints, storeUpdates;
		if (loopPoints == [nil] || loopPoints.isNil || (loopPoints[0] == loopPoints[1])) {
			this.transportContext.loopPoints = nil;
		} {
			this.transportContext.loopPoints = loopPoints.sort;
		};
		storeUpdates.transportContext = this.transportContext;
	}

	getOffset {
		^0;
	}

	getLoopPoints {
		^this.transportContext.loopPoints;
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
					(path ++ [id]).postln;
					this.setPath(id, path ++ [id]);
					if (value.class == Store) {
						pathTraverse(value, maxArchiveId, path ++ [id])
					}
				}
			};
			maxArchiveId;
		};
		lastId = pathTraverse.value(global, 0, []);
	}

	*new { arg object;
		^super.new.init(object)
	}

	init { arg object;
		object !? {
			this.putAll(object);
			super.parent_(object.parent);
		};

		orderedItems = SortedList(8, { arg a, b;
			a.timestamp < b.timestamp;
		});
	}

	put { arg key, value;
		var lookupPath = Store.getPath(id);
		super.put(key, value);
		if (key.class == Integer) {
			value.proto_(this); // allow object to use data from its parent - eg timingContext
		};
		Store.setPath(key, lookupPath ++ [key]);
	}

	*getStoreOrGlobal { arg storeId;
		^(storeId !? this.at(storeId) ?? global);
	}

	*at { arg id;
		var fullPath;
		id ?? { ^global };
		fullPath = lookups[id];
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
		1001: (a: 1, beats: 3),
		1002: (a: 1, b: 2, beats: 1),
		1003: ( // substore which contains object at 1004
			type: 'store',
			1004: (
				a: 1, b: 2, beats: 0
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
				(a: 1, b: 2, beats: 0), // <-- add new object (copy of 1004) to new store 1005
				(a: 2, b: 2, beats: 1), // <-- totally new object
			]
			// 'moving' an object around means deleting it and adding a copy of it as a new object somewhere else
			// means a new ID is created for it
		)
	)

	NEW STATE:
	(
		1001: (a: 2, beats: 3),
		1003: ( // substore is now empty because 1004 was moved out
			type: 'store',
		)
		1005: (
			type: 'store',
			1006: (a: 1, b: 2, beats: 0), // <-- copy of old 1004 but with new ID
			1007: (a: 2, b: 2, beats: 1), // <-- new object
		)
	)
	*/

	patch { arg patch;
		var storeUpdates = ( storeId: id, timestampUpdates: [] );
		patch.keysValuesDo { arg objectId, subPatch;
			var target = this.at(objectId);
			case 
				{ objectId == 'new' } {
					subPatch.do { arg object;
						this.addObject(object, storeUpdates);
					}
				}
				{ target.notNil && subPatch == [nil] } { this.deleteObject(objectId, storeUpdates) }
				{ target.notNil && target.class == Store } { target.updateObject(nil, subPatch, storeUpdates) }
				{ target.notNil } {
					var update = this.updateObject(objectId, subPatch, storeUpdates);
					if (objectId.class == Symbol && update.notNil) {
						storeUpdates.put(objectId, subPatch)
					}
				}
			;
		};

		// storeUpdates.postln;
		Dispatcher((
			type: 'storeUpdated',
			payload: storeUpdates,
			)
		)
	}

	*patch { arg patch, storeId, shouldSave = true;
		var store = storeId !? this.at(storeId) ?? global;
		store.patch(patch);
		// var history = store.patch(patch);
		// if (shouldSave) {
		// 	StoreHistory.saveHistory(history);
		// }
	}

	removeFromOrderedList { arg id;
		var index = 0;
		while { orderedItems[index].id != id } { index = index + 1 };

		orderedItems.removeAt(index);
	} 

	deleteObject { arg objectId, storeUpdates;
		var historyMarker = this.at(objectId);
		var oldObject = lookups[objectId];
		super.put(objectId, nil);
		lookups[objectId] = nil;
		this.removeFromOrderedList(objectId);
		storeUpdates.timestampUpdates = storeUpdates.timestampUpdates.add(oldObject.timestamp); 

		Dispatcher((
			type: 'objectDeleted',
			payload: (
				storeId: id,
				objectId: objectId
				)
			)
		);
	}

	updateObject { arg objectId, newState, storeUpdates;
		var object = objectId !? this.at(objectId) ?? this;
		var diff = getDiff(object, newState);
		var historyMarker = Dictionary();

		if (diff.size == 0) {
			^nil
		} {
			diff.keysValuesDo { arg key, newValue;
				historyMarker[key] = object[key]; // get old state and push to history
				if (key == 'loopPoints') {
					this.setLoopPoints(newValue, storeUpdates);
				} {
					object[key] = newValue;
					if ((key == 'beats') || (key == 'absolute')) {
						orderedItems.sort;
						storeUpdates.timestampUpdates = storeUpdates.timestampUpdates.add(object.timestamp); 
					}
				}				

			};
			Dispatcher((
				type: 'objectUpdated',
				payload: object.putAll((storeId: id))
			));
			^object;
		}
	}

	*addObject { arg object, storeId;
		var store = this.getStoreOrGlobal(storeId);
		^store.addObject(object);
	}

	addObject { arg object, history, storeUpdates;
		var objectId = Store.getId;
		var historyMarker = [nil];
		object.id = objectId;
		this.put(objectId, object);
		object.src !? { arg path;
			Mod.new(path);
		};
		orderedItems.add(object);
		storeUpdates !? { arg updates;
			updates.timestampUpdates = storeUpdates.timestampUpdates.add(object.timestamp); 
		};

		Dispatcher((
			type: 'objectAdded',
			payload: (
				storeId: id,
				object: object
				)
			)
		);
		^objectId;
	}

	*postTree {
		"global:".post;
		global.postTree(nil, 1);
	}

	postTree { arg obj, tabs = 0;
		if(obj.isNil, { obj = this });
		
		if (obj.class == Store) {
			"".postln;

			obj.keysValuesDo({ arg key, value;
				if (key.class == Symbol) {
					tabs.do({ Char.tab.post });
					
					key.post;
					": ".post;
					value.postln;
				}
			});

			obj.keysValuesDo({ arg key, value;
				if (key.class == Integer) {
					tabs.do({ Char.tab.post });
					
					key.asString.post;
					":".post;
					
					this.postTree(value, tabs + 1)
				}
			});

		} {
			obj.postTree(tabs);
		};
	}

	getEmbedView { arg zoom;
		^StoreBlock(this, zoom).select();
	}
	getView {
		^SequencerCanvas.fromStore(this);
	}

	*getItems { arg storeId;
		var store = this.getStoreOrGlobal(storeId);
		^store.orderedItems;
	}

	getItems {
		^orderedItems;
	}

	play { arg start = 0, quant;
		// ^StorePlayer(this).play(start, quant);
	}

	*play {
		^global.play;
	}
	
}