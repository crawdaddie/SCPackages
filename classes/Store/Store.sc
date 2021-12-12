Store : RxEvent {
	classvar global;
	// classvar <pathManager;
	classvar <defaultContexts;

  var <timelineItems;
	var <player;
  var <modulePath;

  var <pathManager;

	*new { arg object, items, module;
		^super.new.init(object, items, module)
	}

	init { arg object, items = [], module;
    pathManager = PathManager();
    modulePath = module !? _.path;

		object !? { 
			this.putAll(object);
			super.parent_(object.parent);
		};

    timelineItems = TimelineItems(this.items);
    items.do { |item| this.addItem(item) };

    Dispatcher.addListener(Topics.objectUpdated, this, { arg payload;
      timelineItems = TimelineItems(this.items);
    });
  
    Dispatcher.addListener(Topics.moduleReload, this, { arg payload;
      if (payload.path == modulePath, {
        this.keysValuesDo { arg key, item;
          if (key.class == Integer, {
            item.reloadFromMetadata;
          });
        };
      })
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
    if (object.class == Store, {
      object['id'] = id;
      ^RxEvent(object)
    }, {
      var rxEvent = RxEvent(object);
		  rxEvent.put('id', id, false);
		  ^rxEvent;
    });
	}
  getOffset {
    ^this.beats ?? 0
  }

  copyItem { arg id, newParams;
    var oldItem = this.at(id);
    ^this.addItem(oldItem.copyAsEvent.putAll(newParams)); 
  }

  addSequenceableItem { arg object, beats = 0, row = 0, sustain = 1;
    ^this.addItem(object, (beats: object.beats ?? beats, row: object.row ?? row, sustain: object.sustain ?? sustain));
  }
  
	addItem { arg object, inject = ();
		var objectId = object.id ?? pathManager.getId();
    var objectCopy = inject.putAll((id: objectId), object);
    var rxObject = RxEvent(objectCopy);
    this.put(objectId, rxObject, false);

    this.dispatch(
      Topics.objectAdded,
      (
        storeId: this.id,
        object: rxObject
      )
    );
    if (rxObject['beats'].notNil) {
      timelineItems.addItem(rxObject);
    };
    ^rxObject;
	}

  deleteItem { arg id;
    var oldItem = this[id];
		super.put(id, nil, false);
    if (oldItem['beats'].notNil) {
      timelineItems = TimelineItems(this.items);
		};
		^this.dispatch(
      Topics.objectDeleted,
			(
			  storeId: this.id,
				objectId: id 
			)
		);
  }

	put { arg key, value, dispatch = true;
		if (value == nil) {
      ^this.deleteItem(key);
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

  play { arg storeCtx, clock;
    var thisclock = clock ?? TempoClock(global.timingContext.bpm / 60);
    player !? _.stop;
    
    player = Prout(timelineItems.getRoutineFunc(0)).trace.play(
      thisclock,
      protoEvent: (storeCtx: this, clock: thisclock),
    ); 
    ^player
    // ^player;
  }

  timelinePlayer {
    ^Prout(timelineItems.getRoutineFunc(0));
  }
  

  copy {
    var newStore = Store(());
    this.pairsDo { arg key, value;
			if (key.class == Integer, {
        newStore.addItem(value.copyAsEvent);
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

