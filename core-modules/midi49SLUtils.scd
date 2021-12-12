var midiSpec = [0, 127, 'linear'].asSpec;
MIDIIn.connectAll;
~midiSrc = MIDIClient.sources.detect({ arg a; a.name == "SL MkIII MIDI" });

~knobs = (21..28) ++ (31..38);

~mapKnobs = { arg ev, prefix, macros, knobMap = [];
  macros.do { arg keyFuncKnobIndex, i;
    var key, func, knobIndex, knob, ccNum;
    #key, func, knobIndex = keyFuncKnobIndex;
    knob = knobIndex ?? i;
    ccNum = ev.knobs[knobMap[i] ?? knobIndex ?? i];

    MIDIdef.cc((prefix ++ '_' ++ key ++ '_ctl').asSymbol, { |val, num, chan, src|
      func.value(key, midiSpec.unmap(val))
    }, ccNum: ccNum, srcID: ev.midiSrc.uid);
  };
};

~faders = (41..48);

~mapFaders = { arg ev, macros;
  macros.do { arg channelNameMacro, i;
    var channelName, macro;
    #channelName, macro = channelNameMacro;
    MIDIdef.cc((channelName ++ '_volume_ctl').asSymbol, { |val, num, chan, src|
      macro.value(midiSpec.unmap(val))
    }, ccNum: ev.faders[i], srcID: ev.midiSrc.uid);
  }
};

~monoVoicer = { arg ev, paramGetter = (), node;
  var midiNotes = [];
  var evNode;
  node.quant = 0;
  
  MIDIdef.noteOn(\sl_on, { |vel, num, chan, src|
    midiNotes = midiNotes ++ [num];
    if (midiNotes.size > 1, {
      // resource.set(num, vel);
      (type: 'set', db: (vel/127).ampdb - 10, id: evNode, midinote: midiNotes.last).play;
    }, {
      // resource.start(num, vel);
      var params = paramGetter.value(num, vel);
      node.source = params;
      evNode = params.proxy.nodeID;
    });
  }, srcID: ev.midiSrc.uid);

  MIDIdef.noteOff(\sl_off, { |vel, num, chan, src|
    var event;
    midiNotes = midiNotes.reject({ arg note; note == num });
    if (midiNotes.size == 0, {
      // resource .free
      event = (
        type: 'off',
        id: node.nodeID,
      );
      event.play;
    }, {
      // resource.set(midiNotes.last, vel)
      (type: 'set', id: evNode, midinote: midiNotes.last).play;
    })
   }, srcID: ev.midiSrc.uid);
};

~polyVoicer = {

};





