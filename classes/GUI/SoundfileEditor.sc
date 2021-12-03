SoundfileEditor { *new { arg soundfile, start, end;
    ^super.new.init(soundfile, start, end);
  }
  init { arg soundfile, start, end;
    var view = View(nil, Rect(0, 0, 600, 300)).layout_(VLayout(
      HLayout(Button(), Button(), Button())
    ));
    var sfview = SoundFileView(view);
    //var actionManager = ProjectKeyActionManager();
    sfview.soundfile = soundfile;
    sfview.read(0, soundfile.numFrames);
    sfview
      .timeCursorOn_(true)
      .gridOn_(false);

    sfview.beginDragAction_({ arg view;
      var selectionIndex = view.currentSelection;
      var selection = view.selections[selectionIndex];
      var startPos = selection[0] / soundfile.numFrames;
      (
        soundfile: soundfile.path,
        selection: selection,
        startPos: startPos,
      );
    });
    /* 
    view.keyDownAction = { arg ... args;
      if (view.hasFocus) {
        actionManager.keyDownAction(*args);
      }
    };
    */

    view.refresh;
    view.front; 
  }
}
