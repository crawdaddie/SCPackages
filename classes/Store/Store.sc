PathManager {
	var lastId;
	var lookups;
	classvar <initialId = 1000;

	*new {
		^super.new.init()
	}

	init {
		lookups = Dictionary();
		lastId = initialId;		
	}

	getId {
		lastId = lastId + 1;
		^lastId;
	}

	getPath { arg id;
		^lookups[id]
	}

	setPath { arg id, path;
		if (id.class == Integer) {
			lookups[id] = path;
		}
	}

	setChildPath { arg childId, parentId;
		var parentPath = parentId !? { this.getPath(parentId) } ?? [];
		this.setPath(childId, parentPath ++ [childId])
	}

	printLookups {
		lookups.postln;
	}
}

Store : RxEvent {
	classvar global;
	classvar pathManager;
	
	classvar defaultContexts;

	var <>orderedItems;
	var <player;

	*global {
		global = global ?? {
			pathManager = PathManager();
			super.new.init(defaultContexts.put('id', PathManager.initialId));
		};

		^global;
	}

	*global_ { arg obj; global = obj; }


	*initClass {
		defaultContexts = (
			timingContext: (bpm: 60),
			transportContext: (),
		);
	}

	*new { arg object;
		super.new.init(object)
	}

	init { arg object;
		object !? { 
			this.putAll(object);
			super.parent_(object.parent);
		}
	}

	getRxEvent { arg object, id;
		object['id'] = id;
		^RxEvent(object);
	}

	addObject { arg object;
		var objectId = pathManager.getId();
		var rxObject = this.getRxEvent(object, objectId);


		this.put(objectId, rxObject, false);

		this.dispatch(
			Topics.objectAdded,
			(
				storeId: this.id,
				object: rxObject
			)
		);

		pathManager.setChildPath(objectId, this.id);

		if (rxObject['row'].notNil) {
			this.resolveOverlaps(rxObject);
		};
	}

	resolveOverlaps { arg object;
		var rowItems = this.rowItems(object['row']);
		rowItems.do { arg timestampWithItem;
			var timestamp, items;
			#timestamp, items = timestampWithItem;

		}
	}

	put { arg key, value, dispatch = true;
		if (value == nil) {
			super.put(key, value, false);
			^this.dispatch(
				Topics.objectDeleted,
				(
					storeId: this.id,
					objectId: key
				)
			);
		};

		^super.put(key, value, dispatch);
	}

	items { arg timestamp = 0;
		^Items(this).groupByTimestamp((start: timestamp));
	}


	itemsFlat {
		^Items(this).flat;
	}
}

Items {
	var <items;
	*new { arg store;
		^super.new.init(store);
	}

	init { arg store;
		
		store.pairsDo { arg key, value;
			if (key.class == Integer) {
				var beats = value.beats;
				if (beats.notNil) {
					items = items.add(value);
				}
			}
		}
	}

	flat {
		^items
	}

	filterByOptions { arg item, options;
		if (item.beats.isNil || item.row.isNil) {
			^false;
		};

		^(
			( options.start !? (item.beats >= options.start) ?? true )
			// && ( options.end !? (item.beats <= options.end) ?? true )
			// && ( options.rows !? (options.rows.includes(item.row)) ?? true )
		)
	}

	groupByTimestamp { arg options;
		var itemsDict = ();

		items.do { arg item;
			var beats = item.beats;
			if (this.filterByOptions(item, options)) {
				itemsDict[beats] = itemsDict[beats] ++ [item];
			};
		};

		^itemsDict.asSortedArray;
	}

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
