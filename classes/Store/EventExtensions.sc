EventExtensions {
  classvar sample;
  classvar <sequencer;

  *add { arg eventType, func, parent;
    Event.addEventType(eventType, func, parent);
    ^parent;
  }

  *initClass {
    sequencer = (
      beats: 0,
      dur: 1,
      row: 0,
    );
    sample = this.add(
      \sample,
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
  *sample { arg event = ();
    ^(type: 'sample').putAll(event).parent_(sample)
  }
}

CoreEvents : EventExtensions {}


