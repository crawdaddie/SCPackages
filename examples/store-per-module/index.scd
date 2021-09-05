Dispatcher.debug = true;

'storeModule'.import;
~store = Store((), ~storeModule);
// 
// ~store.addObject(~storeModule.kick, 0);
~canvas = SequencerCanvas(~store);
~store.addSequenceableItem(~storeModule.kick, 0);
// 
// ~store.writeMinifiedTextArchive("/Users/adam/projects/sc/ecosystem/examples/store-per-module/src/001.storeModule.scproj");
// ~store.play;
// 
// 
// 
// ~store.addItem(~storeModule.kick, 0);
// ~store.deleteItem(1001)
