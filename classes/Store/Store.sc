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
  
  traverseStore { arg store, cb, currentPath = [];
    store.keysValuesDo { arg key, value;
      if (key.class == Integer) {
        var path = currentPath ++ [key];
        cb.value(path, value);
        if (value.class == Store) {
          this.traverseStore(value, cb, path);
        }
      }
    }
  }
  
  resetPaths { arg store;
		var maxArchiveId = 0;
    this.traverseStore(store: store, cb: { arg path, value; 
			var id = path[ path.size -1 ];
			if (id.class == Integer) {
				maxArchiveId = max(maxArchiveId, id);
				this.setPath(id, path);
			};
		});	
		lastId = maxArchiveId;
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
		super.new.init(object)
	}

	init { arg object;
		object !? { 
			this.putAll(object);
			super.parent_(object.parent);
		}
    ^this
	}

	getRxEvent { arg object, id;
		object['id'] = id;
		^RxEvent(object);
	}
  getOffset {
    ^this.beats ?? 0
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
		var items = ();
		this.pairsDo { arg key, value;
			if (key.class == Integer) {
				var beats = value.beats;
				if (beats.notNil && beats >= timestamp) {
					items[beats] = items[beats] ++ [value]; 
				}
			}
		}

		^items;
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
			'items': Store.at(id).orderedItems,
      's': Store.at(id),
		);
		^env.push;
	}
}
