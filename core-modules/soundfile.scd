~soundfile = SoundFile.openRead(~path);
~bufRateScale = ~soundfile.sampleRate / Server.default.sampleRate;
~numChannels = ~soundfile.numChannels;
// "bufRateScale for sample % is %".format(~path, ~bufRateScale).postln;

~buffer = Buffer.read(
  Server.local,
  ~path,
);

