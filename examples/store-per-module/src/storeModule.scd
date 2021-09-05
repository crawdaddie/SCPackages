var exportEvent = { arg memberKey, event;
  currentEnvironment.put(
    memberKey,
    event.putAll((md: (path: ~path, memberKey: memberKey )))
  );
};

var kick = "/Users/adam/projects/sc/lib/Drum_Hits/Kick_01.wav".loadSoundfile;
var snare = "/Users/adam/projects/sc/lib/Drum_Hits/Snare_01.wav".loadSoundfile;
var hihat = "/Users/adam/projects/sc/lib/Drum_Hits/ClosedHat_01.wav".loadSoundfile;

var ev = (
  instrument: 'stereo_player',
  soundfile: "",
  startPos: 0,
  db: 0,
  type: 'sample',
  play: {
    var soundfileMod = ~soundfile.value.asSoundfileMod;
    (
      instrument: ~instrument.value,
      atk: ~atk.value,
      decay: ~decay.value,
      sustain: ~sustain.value,
      startPos: ~startPos.value,
      buf: soundfileMod.buffer,
      dur: ~dur.value,
      rate: ~rate.value,
    ).play;
  },
);



exportEvent.('kick', (soundfile: kick).parent_(ev));
exportEvent.('snare', (soundfile: snare).parent_(ev));
exportEvent.('hihat', (soundfile: hihat).parent_(ev));


