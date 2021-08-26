var sf = 'sampleEvent'.importFrom('baseEvents');
~getSoundfileEvent = { arg ev, soundfile, base = sf;
  base.create((soundfile: soundfile))
};
~playFromSequencer = { arg ev, cb;
  { arg ev, seqEvent;
    cb.value(ev, seqEvent)
  }
}
