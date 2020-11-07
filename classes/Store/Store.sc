// PathManager {
// 	*getId {
// 		lastId = lastId + 1;
// 		^lastId;
// 	}
// }

Store : RxEvent {
	classvar global;
	classvar lastId;
	classvar <lookups;

	classvar defaultContexts;

	var <>id;
	var <>orderedItems;

	var <player;
	
	*getId {
		lastId = lastId + 1;
		^lastId;
	}

	*global { ^global }
	*global_ { arg obj; global = obj; }

	*initClass {
		Class.initClassTree(Dispatcher);
		Class.initClassTree(Topics);

		defaultContexts = (
			timingContext: (bpm: 60),
			transportContext: (),
		);

		lookups = Dictionary();
		lastId = 1000;
		global = this
			.new((
				timingContext: defaultContexts.timingContext,
				transportContext: defaultContexts.transportContext,
			))

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

		this.dispatch(
			Topics.storeUpdated,
			storeUpdates,
		)
	}

	*patch { arg patch, storeId, shouldSave = true;
		var store = storeId !? this.at(storeId) ?? global;
		store.patch(patch);
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

		this.dispatch(
			Topics.objectDeleted,
			(
				storeId: id,
				objectId: objectId
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
					object.setAt(key, newValue); // use StoreEvent::setAt so we can defer sending objectUpdated with all updates together
					if ((key == 'beats') || (key == 'absolute')) {
						orderedItems.sort;
						storeUpdates.timestampUpdates = storeUpdates.timestampUpdates.add(object.timestamp); 
					}
				}
			};
			Dispatcher((
				type: 'objectUpdated',
				payload: object.copy.put('storeId', id, false)
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
		var module;

		object.id = objectId;

		this.put(
			objectId,
			RxEvent(object)
		);

		module = object.src !? { arg path;
			Mod.new(path);
		};

		orderedItems.add(object);


		this.dispatch(
			Topics.objectAdded,
			(
				storeId: id,
				object: object
			)
		)

		^objectId;
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

	apply { arg fn;
		this.orderedItems.do { arg item;
			item.putAll(fn.value(item))
		}
	}


	play { arg startPos;
		^StorePlayer(
			this,
			startPos ? 0
		).play;
	}

	*play {
		^global.play;
	}

	push {
		this['items'] = this.orderedItems;
		super.push();
	}

	pop {
		this['items'] = nil;
		super.pop();
	}

	/*
	 * path utilities
	 * ./extPathUtilities.sc
	 * 
	 * printing + formatting functionality: 
	 * ./extPrintStore.sc
	 * 
	 * context utilities (transport, timing, loop points etc.)
	 * ./extContextUtilities.sc
	 *
	 */
}

S {
	*new { arg id;
		^Store.at(id);
	}
	*push { arg id;
		var env = (
			'items': Store.at(id).orderedItems,
			's': Store.at(id),
		);
		^env.push;
	}
}
