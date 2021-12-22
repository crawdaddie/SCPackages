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
~makeGrid = { arg module, gridItems, callingEnvironment = currentEnvironment;
  // ~midiOut.control(0, 0, colors['NULL']); // clear display
  gridItems.do { arg item;
    var x, y, object, callingEnv;
    #x, y, object, callingEnv = item;
    module.addToGrid(object, x, y, callingEnv ? callingEnvironment);
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

~addToGrid = M { arg object, x = 0, y = 0, callingEnvironment;
  var oldObject = ~grid[y][x];
  ~grid[y][x] = (object: object, env: callingEnvironment);
  if (oldObject.isNil, {~midiOut.noteOn(0, y * 16 + x, colors['AMBER_FULL'])});
};

~gridAt = M { arg x = 0, y = 0;
  ~grid[y][x];
};

~gridColumn = M { arg x = 0;
  ~grid.collect(_[x])
};


~flashPad = M { arg x, y, color, offcolor = (colors['OFF']);
  Routine {
    inf.do {
      ~midiOut.noteOn(0, y * 16 + x, color);
      0.15.wait;
      ~midiOut.noteOn(0, y * 16 + x, offcolor);
      0.85.wait; }
  };
};
~setPad = M { arg x, y, color = (colors['OFF']);
  ~midiOut.noteOn(0, y * 16 + x, color);
};

~renderGrid = M { arg offset;
  var gridSubset = module.gridSubset(~grid, offset); 
  ~midiOut.control(0, 0, colors['NULL']); // clear display

  gridSubset.do { arg row, y;
    row.do { arg col, x;
      if (col.notNil, {
        var color;
        color = if (col['output'].notNil, {
          colors['GREEN_FULL']
        }, {colors['AMBER_FULL']});
        
        ~midiOut.noteOn(0, y * 16 + x, color)
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

~handleItem = M { arg item;
  var object, output;
  object = item[0];
  output = object.value;
  item = [object, output];
};


~stopGridColumn = M { arg item;
  item.postln;
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
  var gridItems;
  // var grid = module.gridSubset(module.grid, module.offset);
  var grid = module.grid;
  var x = num % 16;
  var y = ((num - x) / 16).asInteger;
  var gridColumn = module.offset[0] + x;
  var gridRow = module.offset[1] + y;

  gridItems = if (x == 8, grid[gridRow], [grid[gridRow][gridColumn]]);

  gridItems.do { arg item, i;
    if (item.notNil, {
      var env = item['env'];
      var output = env.use { item['object'].value };
      var flasher = module.flashPad(x, y, colors['GREEN_FULL']);

      var clock = env['clock'] ?? TempoClock.default;

      { flasher.play(clock, quant: 1) }.fork(clock, quant: 1);

      {
        flasher.stop;
        module.setPad(x, y, colors['GREEN_FULL']);
        module.gridColumn(gridColumn).do { arg columnItem, columnY;
          if ((columnItem.notNil && (columnY != gridRow)), {
            columnItem.put('output', nil);
            module.setPad(x, columnY, colors['AMBER_FULL']);
          })
        };
        item.put('output', output);
      }.fork(clock, quant: output.quant);
    }, {
      
      module.gridColumn(gridColumn).do { arg columnItem, row;
        var output = columnItem !? _['output'];
        if (output.notNil, {
          var env = columnItem['env'];
          var clock = env['clock'] ?? TempoClock.default;

          var flasher = module.flashPad(x, row, colors['AMBER_FULL'], colors['GREEN_FULL']);

          { flasher.play(clock, quant: 1) }.fork(clock, quant: 1);

          {
            flasher.stop;
            columnItem.put('output', nil);
            output.source = nil;
            module.setPad(x, row, colors['AMBER_FULL'])
          }.fork(clock, quant: output.quant)
        })
      };
    });
  };
}.inEnvir, chan: 0, srcID: ~midiSrc.uid);


  
