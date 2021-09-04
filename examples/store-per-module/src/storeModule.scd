~kick = "/Users/adam/projects/sc/lib/Drum_Hits/Kick_01.wav".loadSoundfile;
~snare = "/Users/adam/projects/sc/lib/Drum_Hits/Snare_01.wav".loadSoundfile;
~hihat = "/Users/adam/projects/sc/lib/Drum_Hits/ClosedHat_01.wav".loadSoundfile;
~events = (
  kick: CoreEvents.sample((soundfile: ~kick)),
  snare: CoreEvents.sample((soundfile: ~snare)),
  hihat: CoreEvents.sample((soundfile: ~hihat)),
);



~events.snare


