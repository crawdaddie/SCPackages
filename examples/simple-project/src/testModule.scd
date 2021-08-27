var createEvent = { arg name, event;
  currentEnvironment.postln;
  event.parent_(currentEnvironment);
  event.putAll(
    (md: (path: ~path, memberKey: name))
  );
  currentEnvironment.put(name, event);
};

var hat = "/Users/adam/projects/sc/lib/Drum_Hits/ClosedHat_01.wav";
// .asSoundfileMod;

createEvent.('defaultEvent', (
  row: 0,
  beats: 0,
  dur: 1,
  soundfile: hat, 
));

~play = { arg ev ... params;
  [ev, params].postln;
}
