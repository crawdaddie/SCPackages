Store : RxEvent {
	classvar global;
	classvar <pathManager;
	
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
    pathManager = PathManager();
	}

  *readFromArchive { arg path;
		global = path.load;
		pathManager.resetPaths(global);

  } 
  *at { arg id;
		var fullPath;
    if (id == PathManager.initialId) {
      ^this.global;
    };
		id ?? { ^global };
		fullPath = pathManager.getPath(id);

		fullPath ?? { ^nil };
		if (fullPath.size > 1) {
			var parentId = fullPath[fullPath.size - 2];
			^Store.at(parentId).at(id);
		} { 
			^global.at(id);
		}
	}

	*new { arg object;
		^super.new.init(object)
	}

	init { arg object;
		object !? { 
			this.putAll(object);
			super.parent_(object.parent);
		}
    ^this
	}
  embedView {
    ^StoreCanvasObject;
  }
  getView {
    ^SequencerCanvas(this);
  }

	getRxEvent { arg object, id;
		object['id'] = id;
		^RxEvent(object);
	}
  getOffset {
    ^this.beats ?? 0
  }
  
	addObject { arg object;
		var objectId = object.id ?? pathManager.getId();
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
		var it = Items(this).flat;
    ^it;
	}
  play {
    var duration = this.dur;
    ^Prout(StorePlayer(this).getRoutineFunc(0, duration)).play; 
  }
  copy {
    var newStore = Store(());
    this.pairsDo { arg key, value;
			if (key.class == Integer, {
        newStore.addObject(value.copyAsEvent);
      }, {
        newStore.put(key, value);
      });
		}
    ^newStore;
  }
}

Items {
	var <items;
	*new { arg store;
		^super.new.init(store);
	}

	init { arg store;
	  items = [];	
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
    item.beats.postln;
    [options, ( options.end !? (item.beats < options.end) ?? true  )].postln;

		^(
			( options.start !? (item.beats >= options.start) ?? true )
			//&& ( options.end !? (item.beats < options.end) ?? true )
			// && ( options.rows !? (options.rows.includes(item.row)) ?? true )
		)
	}
  
  getNextEventGroup { arg start = 0, end;
    var itemsDict = ();
    
    items.do { arg item;
      var beats = item.beats;
      if (beats > start) {
        itemsDict[beats] = itemsDict[beats] ++ [item];
      }
    };
    ^itemsDict.asSortedArray[0]
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

  itemsFlat {
    var items = [];
    this.values.keysValuesDo({ arg key, value;
      if (key.class == Integer) {
        items = items.add(value)
      }
    });
    ^items
  }

}

S {
	*new { arg id;
		^Store.at(id);
	}
	*push { arg id;
		var env = (
			'items': Store.at(id).itemsFlat,
      's': Store.at(id),
		);
		^env.push;
	}
}
