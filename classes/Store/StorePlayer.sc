StorePlayer {
  var store;
  var items;
  *new { arg store;
    ^super.new.init(store);
  }

  init { arg aStore;
    store = aStore;
  //   Dispatcher.addListener(Topics.objectAdded, this, { arg payload;
  //     if (payload.storeId == store.id) {
  //       var item = store[payload.object.id];
  //       items.add(item);
  //     }
  //   });
  //   Dispatcher.addListener(Topics.objectDeleted, this, { arg payload;
  //     if (this.filterEvent(payload)) {
  //       this.orderItems;
  //     }
  //   });
  //   Dispatcher.addListener(Topics.objectUpdated, this, { arg payload;
  //     if (store[payload.id].notNil && payload['beats'].notNil) {
  //       this.orderItems;
  //     }
  //   });
  }
  getRoutineFunc { arg start = 0, dur;
  
    ^{

    }
  }

// 
//   getFirstEventGroup { arg start, end;
//     ^Items(store).groupByTimestamp((start: start, end: end))[0];
//   }
//   
//   getNextEventGroup { arg start, end;
//     ^Items(store).getNextEventGroup(start, end);
//   }
// 
//   getPlayerEvent { arg eventGroup, delta;
//     ^(
//       events: eventGroup[1],
//       play: #{
//         ~events.do { arg ev;
//           ev.id.postln;
//           ev.play;
//         }
//       },
//       delta: delta
//     );
//   }
//   
//   getRoutineFunc { arg start = 0, dur;
//     var eventGroups = Items(store).groupByTimestamp((start: start));
//     var firstEventGroup = eventGroups[0];
//     var end = if (dur.isNil, {eventGroups.last[0]}, { start + dur });
// 
//     ^{
//       var storeEventGroup = this.getFirstEventGroup(start, end);
//       var time, nextStoreEventGroup;
//       Dispatcher('storePlayerStart', (), this);
//       "start playing store".postln;
// 
//       while ({ storeEventGroup.notNil }, {
//         time = storeEventGroup[0];
// 
//         nextStoreEventGroup = this.getNextEventGroup(time, end);
// 
//         if (nextStoreEventGroup.isNil, {
//           this.getPlayerEvent(storeEventGroup).yield;
//           storeEventGroup = nil;
//         }, {
//           var delta = nextStoreEventGroup[0] - time; 
//           this.getPlayerEvent(storeEventGroup, delta).yield;
//           storeEventGroup = nextStoreEventGroup;
//         }); 
//       })
//     }
//   }
}
