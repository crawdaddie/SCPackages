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

    this.assert(store.getItems() == (
      0: [
        ( 'startPos': 0, 'beats': 0, 'length': 2, 'soundfile': "path/s01.wav", 'id': 1002 ),
        ( 'beats': 0, 'param': 1, 'id': 1001 )
      ],
      2: [
        ( 'startPos': 0, 'beats': 2, 'length': 2, 'soundfile': "path/s02.wav", 'id': 1003 ),
        ( 'startPos': 0.2, 'beats': 2, 'length': 2, 'soundfile': "path/s02.wav", 'id': 1004 ),
      ],
      4: [ ( 'startPos': 0, 'beats': 4, 'length': 2, 'soundfile': "path/s04.wav", 'id': 1005 ) ] )
    );
  }
}