TestStore : UnitTest {
	var store;
	setUp {
		// this will be called before each test
		// Store.initClass();
	}
	
	tearDown {
		// this will be called after each test
  }

  createBasicStore {
  	Store.addObject(
  		(
  			beats: 0,
  			param: 1
  		)
  	);
  }



  test_storeInit {
  	this.createBasicStore;
  }
}