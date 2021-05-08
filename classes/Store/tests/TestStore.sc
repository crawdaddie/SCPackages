TestStore : UnitTest {
	var store;
	var collectedEvents;
  setUp {
		// this will be called before each test
    this.collectEvents();
    this.createBasicStore();
	}
	
	tearDown {
		// this will be called after each test
    Dispatcher.removeListenersForObject(this);
  }

  collectEvents {
    collectedEvents = [];
    Dispatcher.addListener('*', this, { arg payload, listeningObject, type;
      collectedEvents = collectedEvents.add([type, payload]);
    });
  }

  createBasicStore {
    Store.global_(nil);
    store = Store.global;
  	store.addObject(
  		(
  			beats: 0,
  			param: 1
  		)
  	);
  }


  test_storeInit {
    this.assert(store[1001] == RxEvent((beats: 0, param: 1, id: 1001)));
  }

  test_storeAddChild {
    store.addObject((beats: 1, param: 2));
    this.assert(store[1002] == RxEvent((beats: 1, param: 2, id: 1002)));
  }

  test_storeEditChild {
    store[1001].param = 2;
    this.assert(store[1001] == RxEvent((beats: 0, param: 2, id: 1001)));
  }

  test_storeDeleteChild {
    store[1001] = nil;
    this.assert(store[1001].isNil);
  }

  test_storeGetItems {
    var events = [
      ( soundfile: "path/s01.wav", beats: 0, startPos: 0, length: 2 ),
      ( soundfile: "path/s02.wav", beats: 2, startPos: 0, length: 2 ),
      ( soundfile: "path/s02.wav", beats: 2, startPos: 0.2, length: 2 ),
      ( soundfile: "path/s04.wav", beats: 4, startPos: 0, length: 2 ),
    ];

    events.do { |event, index|
      Store.global.addObject(event);
    };

    this.assert(store.items == [
      [ 0, [
        RxEvent(( startPos: 0, beats: 0, length: 2, soundfile: "path/s01.wav", id: 1002 )),
        RxEvent(( beats: 0, param: 1, id: 1001 ))
        ]
      ],

      [ 2, [
        RxEvent(( startPos: 0, beats: 2, length: 2, soundfile: "path/s02.wav", id: 1003 )),
        RxEvent(( startPos: 0.2, beats: 2, length: 2, soundfile: "path/s02.wav", id: 1004 )),
        ]
      ],

      [ 4, [ RxEvent(( startPos: 0, beats: 4, length: 2, soundfile: "path/s04.wav", id: 1005 )) ] ]
    ]);
  }

  test_storeGetRowItems {
    var events = [
      ( row: 0, soundfile: "path/s01.wav", beats: 0, startPos: 0, length: 2 ),
      ( row: 0, soundfile: "path/s02.wav", beats: 2, startPos: 0, length: 2 ),
      ( row: 1, soundfile: "path/s02.wav", beats: 2, startPos: 0, length: 2 ),
      ( row: 0, soundfile: "path/s02.wav", beats: 2, startPos: 0.2, length: 2 ),
      ( row: 2, soundfile: "path/s04.wav", beats: 4, startPos: 0, length: 2 ),
    ];

    events.do { |event, index|
      Store.global.addObject(event);
    };

    this.assert(store.rowItems(0) == [
      [ 0, [RxEvent(( startPos: 0, beats: 0, length: 2, soundfile: "path/s01.wav", row: 0, id: 1002 )) ] ],

      [ 2, [
        RxEvent(( startPos: 0.2, beats: 2, length: 2, soundfile: "path/s02.wav", row: 0, id: 1005 )),
        RxEvent(( startPos: 0, beats: 2, length: 2, soundfile: "path/s02.wav", row: 0, id: 1003 )) 
        ]
      ] 
    ]); // should only return the 3 events with row 0
  }


  test_storeAddOverlappingObject {
    var event = ( row: 0, beats: 0, length: 3 );
    var overlappingEvent = (row: 0, beats: 1, length: 1);
    store.addObject(event);
    store.addObject(overlappingEvent);

    store.rowItems(0).postln;
    this.assert(store.rowItems(0) == [
      [ 0, [RxEvent(( row: 0, beats: 0, length: 1, id: 1002 ))] ],
      [ 1, [RxEvent(( row: 0, beats: 1, length: 1, id: 1003 ))] ],
      [ 2, [RxEvent(( row: 0, beats: 2, length: 1, id: 1004 ))] ]
    ]);

  }
}