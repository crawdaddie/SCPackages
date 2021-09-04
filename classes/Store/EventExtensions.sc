EventExtensions {
  classvar soundfile;
  classvar <sequencer;

  *add { arg eventType, func, parent;
    // [eventType, func, parent].postln;
    Event.addEventType(eventType, func, parent);
    ^parent;
  }

  *initClass {
    sequencer = (
      beats: 0,
      dur: 1,
      row: 0,
    );
    soundfile = this.add(
      \soundfile,
      { arg server;
        var soundfileMod = ~soundfile.value.asSoundfileMod;
        ~buf = soundfileMod.buffer;
        ~type = 'note';
        currentEnvironment.play;
      },
      (
        instrument: 'stereo_player',
        soundfile: "",
        startPos: 0,
        db: 0
      );
    )
  }
  *soundfile { arg event = ();
    ^(type: 'soundfile').putAll(event).parent_(soundfile)
  }
}

CoreEvents : EventExtensions {}


