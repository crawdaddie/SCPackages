var module = currentEnvironment;
var constants = Dictionary.with(*[
  'PAGE_UP' ->    [0, -8], 
  'PAGE_DOWN' ->  [0,  8], 
  'PAGE_LEFT' ->  [-8, 0], 
  'PAGE_RIGHT' -> [8,  0], 
]);

var colors = Dictionary.with(*[
  'NULL' -> 0x00,
	'OFF' -> 0x0C,
	'RED_THIRD' -> 0x0D,
	'RED_HALF' -> 0x0E,
	'RED_FULL' -> 0x0F,
	'RED_BLINK' -> 0x0B,
	'GREEN_THIRD' -> 0x1C,
	'GREEN_HALF' -> 0x2C,
	'GREEN_FULL' -> 0x3C,
	'GREEN_BLINK' -> 0x38,	
	'AMBER_THIRD' -> 0x1D,
	'AMBER_HALF' -> 0x2E,
	'AMBER_FULL' -> 0x3F,
	'AMBER_BLINK' -> 0x3B
]);

MIDIIn.connectAll;

~midiSrc = MIDIClient.sources.detect({ arg a; a.name == "Launchpad Mini" });
~midiDest = MIDIClient.destinations.detect({ arg a; a.name == "Launchpad Mini" });
~midiOut = MIDIOut.newByName(~midiDest.name, ~midiDest.name);

~midiOut.control(0, 0, colors['NULL']); // clear display
~midiOut.control(0, 0, 0x28);

~grid = Array.newClear(128)!128;
~makeGrid = M { arg ...gridItems;
  ~grid = Array.newClear(128)!128;
  ~midiOut.control(0, 0, colors['NULL']); // clear display
  gridItems.do { arg item;
    var x, y, object;
    #x, y, object = item;
    module.addToGrid(object, x, y);
  };
};

~setGrid = M { arg grid;
  ~grid = grid;
  module.renderGrid(~offset);
};

~offset = [0, 0];

~gridSubset = M { arg grid, offset;
  var x, y;
  #x, y = offset;
  grid[(y..y + 7)].collect({ arg col; col[(x..x + 7)]})
};

~addToGrid = M { arg object, x = 0, y = 0;
  ~grid[y][x] = object;
  ~midiOut.noteOn(0, y * 16 + x, colors['AMBER_FULL'])
};

~gridAt = M { arg x = 0, y = 0;
  ~grid[y][x];
};

~renderGrid = M { arg offset;
  var gridSubset = module.gridSubset(~grid, offset); 
  ~midiOut.control(0, 0, colors['NULL']); // clear display

  gridSubset.do { arg row, y;
    row.do { arg col, x;
      if (col.notNil, {
        ~midiOut.noteOn(0, y * 16 + x, colors['AMBER_FULL'])
      })
    }
  }
};

~updateOffset = M { arg delta;
  var newOffset = (~offset + delta).clip(0, 120);
  if (newOffset != ~offset, {
    ~offset = newOffset;
    ~renderGrid.value(module, ~offset);
  })
};

MIDIdef.cc('launchpadPosition', { |vel, num|
  if (vel == 127, {
    switch (num,
      104, {module.updateOffset(constants['PAGE_UP'])},
      105, {module.updateOffset(constants['PAGE_DOWN'])},
      106, {module.updateOffset(constants['PAGE_LEFT'])},
      107, {module.updateOffset(constants['PAGE_RIGHT'])},
    ) 
  });
}, chan: 0, srcID: ~midiSrc.uid);


MIDIdef.noteOn('launchpad', { |vel, num|
  var gridObjects;
  var grid = module.gridSubset(module.grid, module.offset);
  var x = num % 16;
  var y = ((num - x) / 16).asInt;
  gridObjects = if (x == 8, grid[y], [grid[y][x]]);
  // [x, y, gridObjects.select(_.notNil)].postln;
  gridObjects.do { arg object;
    if (object.notNil, {
      object.value;
    })
  };
}, chan: 0, srcID: ~midiSrc.uid);



