// Dispatcher.debug = true;

// 'storeModule'.import;
// ~store = Store((), ~storeModule);
// 
// ~store.addObject(~storeModule.kick, 0);
// ~store = load("/Users/adam/projects/sc/ecosystem/examples/store-per-module/src/001.storeModule.scproj");
// ~canvas = SequencerCanvas(~store);
// ~store.addSequenceableItem(~storeModule.kick, 0);
// ~store[1002].rate = 120/140
// 
// ~store.writeMinifiedTextArchive("/Users/adam/projects/sc/ecosystem/examples/store-per-module/src/001.storeModule.scproj");
// ~store.play;
// ~store.play(TempoClock(140/60));
// Store.global.timingContext.bpm = 140;
// 
// 
// 
// ~store.addItem(~storeModule.kick ptimeli, 0);
// ~store.deleteItem(1001)
// // ProxySpace.push;
// (
// var b = "/Users/adam/projects/sc/lib/Beats/Beat01_Full(140BPM).wav".asSoundfileMod;
// ~p = Pbind(
//   \instrument, 'stereo_player',
//   \startPos, Prand([0, 0.5, 0.75, 0.25, 0.25, 0.25], inf),
//   \dur, Pseq([1, Prand([Pseq([0.25, 0.5, 0.25]), 0.5], inf)]),
//   \sustain, {~dur},
//   \buf, b.buffer,
//   \db, Pwhite(-5, -1),
// );
// )
// ~p.edit;
// ~p.stop;
//   ~p.quant = 1;
// ~store.play();
// ~p.play;
//   ~p.trace
//   TempoClock.default.tempo = 140/60;
// thisProcess.stop;
// currentEnvironment.gui;
// 
// "/Users/adam/projects/sc/ecosystem/examples/store-per-module/src/synths.scd".load;
// ~store = Store.global;
// Store.postTree;
// 
// 
// ~b = ~store[1001].soundfile.asSoundfileMod;
// 
// ~p = Pbind(\instrument, 'stereo_player', \dur, 4, \sustain, 4, \buf, ~b.buffer ).trace.play(TempoClock(150/60));
// 
// ~p.stop;
// thisProcess.stop;
