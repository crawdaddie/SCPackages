var midiSpec = [0, 127, 'linear'].asSpec;
MIDIIn.connectAll;
~midiSrc = MIDIClient.sources.detect({ arg a; a.name == "Midi Fighter Twister" });

~knobs = (0..63);

~mapKnobs = { arg ev, prefix, macros, knobMap = [];
  macros.do { arg keyFuncKnobIndex, i;
    var key, func, knobIndex, knob, ccNum;
    #key, func, knobIndex = keyFuncKnobIndex;
    knob = knobIndex ?? i;
    ccNum = ev.knobs[knobMap[i] ?? knobIndex ?? i];

    MIDIdef.cc(
      (prefix ++ '_' ++ key ++ '_ctl').asSymbol,
      { |val, num, chan, src|
        func.value(key, midiSpec.unmap(val))
      },
      ccNum: ccNum,
      // chan: 3,
      srcID: ev.midiSrc.uid
    );
  };
};




