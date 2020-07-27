// one loop segment
SynthDef(\stereo_player, { arg
	out = 0,
	buf = 0,
	gate = 1,
	startPos = 0,
	sustain = 1,
	amp = 1,
	atk = 0.01,
	decay = 0.01
	;

	var sig = PlayBuf.ar(2, buf,
		startPos: startPos * BufFrames.kr(buf),
		loop: 0
	);

	var env = EnvGen.kr(
		Env.asr(atk, amp, decay),
		gate: gate,
		doneAction: Done.freeSelf
	);
  Out.ar(out, (sig * env));
}).store;