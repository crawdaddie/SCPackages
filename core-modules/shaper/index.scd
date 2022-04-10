~generateCheby = M { arg amplitudes = [0, 1, 1, -2, 1];
  var wav = Wavetable.chebyFill(256+1, amplitudes, normalize: true, zeroOffset: false);
  var cwd = thisProcess.nowExecutingPath;

  var savePath = ~path.dirname +/+ "cheby" ++ ".wav";
  b = Buffer.loadCollection(s, wav);
  wav.asSignal.write(savePath);
  b;
};

~chebybuf = Buffer.read(s, path: ~path.dirname +/+ "cheby.wav");


