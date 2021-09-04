// default event values - can and probably will be overriden
// by concrete events
~soundfile = "/Users/adam/projects/sc/lib/Drum_Hits/ClosedHat_01.wav".loadSoundfile;
~beats = 1;
~row = 2;
~dur = 3;
~play = #{
  (
    soundfile: ~soundfile,
    rate: ~row * 2,
    dur: ~dur,
    type: 'soundfile'
  ).play;
}


