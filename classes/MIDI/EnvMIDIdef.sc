EnvMIDIdef : MIDIdef {
  var env;
  *new { arg env ...args;
    ^super.new(*args).initEnv(env)
	}

  initEnv { arg ev;
    env = ev;
    func = { arg ...args;
      env.use { func.value(*args) }
    };
    func.postcs;
  }

  *cc { arg env, key, func, ccNum, chan, srcID, argTemplate, dispatcher;
		^this.new(env, key, func, ccNum, chan, \control, srcID, argTemplate, dispatcher);
	}

	*noteOn { arg env, key, func, noteNum, chan, srcID, argTemplate, dispatcher;
		^this.new(env, key, func, noteNum, chan, \noteOn, srcID, argTemplate, dispatcher);
	}

	*noteOff { arg env, key, func, noteNum, chan, srcID, argTemplate, dispatcher;
		^this.new(env, key, func, noteNum, chan, \noteOff, srcID, argTemplate, dispatcher);
	}

	*polytouch { arg env, key, func, noteNum, chan, srcID, argTemplate, dispatcher;
		^this.new(env, key, func, noteNum, chan, \polytouch, srcID, argTemplate, dispatcher);
	}

	*touch { arg env, key, func, chan, srcID, argTemplate, dispatcher;
		^this.new(env, key, func, nil, chan, \touch, srcID, argTemplate, dispatcher);
	}

	*bend { arg env, key, func, chan, srcID, argTemplate, dispatcher;
		^this.new(env, key, func, nil, chan, \bend, srcID, argTemplate, dispatcher);
	}

	*program { arg env, key, func, chan, srcID, argTemplate, dispatcher;
		^this.new(env, key, func, nil, chan, \program, srcID, argTemplate, dispatcher);
	}

	///// system messages

	*sysex { arg env, key, func, srcID, argTemplate, dispatcher;
		^this.new(env, key, func, nil, nil, \sysex, srcID, argTemplate, dispatcher);
	}

		// system common

	// does this need to be registered on the SMPTE hook? Yes!
	*mtcQuarterFrame {arg env, key, func, srcID, argTemplate, dispatcher;
		^this.new(env, key, func, nil, nil, \mtcQF, srcID, argTemplate, dispatcher); // actually index 1 sysrt, but on smpte hook
	}

	*smpte {arg env, key, func, srcID, argTemplate, dispatcher;
		^this.new(env, key, func, nil, nil, \smpte, srcID, argTemplate, dispatcher); // actually index 1 sysrt, but on smpte hook
	}

	*songPosition {arg env, key, func, srcID, argTemplate, dispatcher;
		^this.new(env, key, func, 2, nil, \songPosition, srcID, argTemplate, dispatcher);
	}

	*songSelect {arg env, key, func, srcID, argTemplate, dispatcher;
		^this.new(env, key, func, 3, nil, \songSelect, srcID, argTemplate, dispatcher);
	}

	*tuneRequest {arg env, key, func, srcID, dispatcher;
		^this.new(env, key, func, 6, nil, \tuneRequest, srcID, nil, dispatcher);
	}

	*midiClock {arg env, key, func, srcID, dispatcher;
		^this.new(env, key, func, 8, nil, \midiClock, srcID, nil, dispatcher);
	}

	// system realtime

	// generic
	*sysrt { arg env, key, func, index, srcID, argTemplate, dispatcher;
		^this.new(env, key,func, index, nil, \sysrt, srcID, argTemplate, dispatcher);
	}

	*tick {arg env, key, func, srcID, dispatcher;
		^this.new(env, key,func, 9, nil, \tick, srcID, nil, dispatcher);
	}

	*start {arg env, key, func, srcID, dispatcher;
		^this.new(env, key,func, 10, nil, \start, srcID, nil, dispatcher);
	}

	*continue {arg env, key, func, srcID, dispatcher;
		^this.new(env, key,func, 11, nil, \continue, srcID, nil, dispatcher);
	}

	*stop {arg env, key, func, srcID, dispatcher;
		^this.new(env, key,func, 12, nil, \stop, srcID, nil, dispatcher);
	}

	*activeSense {arg env, key, func, srcID, dispatcher;
		^this.new(env, key,func, 14, nil, \activeSense, srcID, nil, dispatcher);
	}

	*reset {arg env, key, func, srcID, dispatcher;
		^this.new(env, key,func, 15, nil, \reset, srcID, nil, dispatcher);
	}

}
