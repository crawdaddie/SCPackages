StorePlayer {
  var store;
  *new { arg store;
    ^super.new.init(store);
  }
  init { arg aStore;
    store = aStore;
  }
  *initClass {

  }

  getFirstEventGroup { arg start;
    ^Items(store).groupByTimestamp((start: start))[0];
  }
  
  getNextEventGroup { arg start;
    ^Items(store).getNextEventGroup(start);
  }

  getPlayerEvent { arg eventGroup, delta;
    ^(
      events: eventGroup[1],
      play: #{
        ~events.do { arg ev;
          ev.id.postln;
          ev.play;
        }
      },
      delta: delta
    );
  }
  
  getRoutineFunc { arg start = 0;
    ^{
      var storeEventGroup = this.getFirstEventGroup(start);
      var time, nextStoreEventGroup;
      "start playing store".postln;

      while ({ storeEventGroup.notNil }, {
        time = storeEventGroup[0];

        nextStoreEventGroup = this.getNextEventGroup(time);

        if (nextStoreEventGroup.isNil, {
          this.getPlayerEvent(storeEventGroup).yield;
          storeEventGroup = nil;
        }, {
          var delta = nextStoreEventGroup[0] - time; 
          this.getPlayerEvent(storeEventGroup, delta).yield;
          storeEventGroup = nextStoreEventGroup;
        }); 
      })
    }
  }
}
