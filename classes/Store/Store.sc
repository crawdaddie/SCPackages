Store : RxEvent {
	classvar global;
	classvar <pathManager;
	
	classvar defaultContexts;

  var <timelineItems;
	var <player;

	*global {
		global = global ?? {
			pathManager = PathManager();
			super.new.init(defaultContexts.put('id', PathManager.initialId));
		};
    thisThread.randSeed = global.randSeed;

		^global;
	}

	*global_ { arg obj;
    global = obj;
  }
	*initClass {
		defaultContexts = (
			timingContext: (bpm: 60),
			transportContext: (),
      randSeed: 1000 
		);
    pathManager = PathManager();
	}

  *readFromArchive { arg path;
    var archivedObject = path.load;
    ["read from archive", archivedObject].postln;
		global = this.new(archivedObject);
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
		};
    timelineItems = TimelineItems(this.items);

    Dispatcher.addListener(Topics.objectUpdated, this, { arg payload;
      timelineItems = TimelineItems(this.items);
    });

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
    if (rxObject['beats'].notNil) {
      timelineItems.addItem(rxObject);
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
      var oldItem = this[key];
			super.put(key, value, false);
      if (oldItem['beats'].notNil) {
        [key, value, oldItem].postln;
      };
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

	items {
    var items = [];	
		this.pairsDo { arg key, value;
			if (key.class == Integer) {
				var beats = value.beats;
				if (beats.notNil) {
					items = items.add(value);
				}
			}
		};

		^items
  }


	itemsFlat {
    ^this.items;
	}

  play {
    var duration = this.dur;
    ^Prout(timelineItems.getRoutineFunc(0, duration)).play(
      clock: TempoClock(global.timingContext.bpm / 60),
      protoEvent: (storeCtx: this),
    ).trace; 
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
  updateAfterLoadFromArchive {
    super.updateAfterLoadFromArchive;
    timelineItems = TimelineItems(this.items)
  }
}

