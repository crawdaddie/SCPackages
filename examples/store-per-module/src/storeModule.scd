var kick = "/Users/adam/projects/sc/lib/Drum_Hits/Kick_01.wav".loadSoundfile;
var snare = "/Users/adam/projects/sc/lib/Drum_Hits/Snare_01.wav".loadSoundfile;
var hihat = "/Users/adam/projects/sc/lib/Drum_Hits/ClosedHat_01.wav".loadSoundfile;
var exportEvent = { arg memberKey, event;
  currentEnvironment.put(memberKey, event.putAll((md: (path: ~path, memberKey: memberKey ))));
};

exportEvent.('kick', CoreEvents.sample((soundfile: kick, extraKey: "extra 2")));
exportEvent.('snare', CoreEvents.sample((soundfile: snare)));
exportEvent.('hihat', CoreEvents.sample((soundfile: hihat)));


