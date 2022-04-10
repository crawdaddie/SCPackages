var fromControl, toControl;
var flat, flatCtl;

// control parameters:
// [freq, db, reciprocal slope]
flat = [
 [ 100, 0, 1 ],
 [ 250, 0, 1 ],
 [ 1000, 0, 1 ],
 [ 3500, 0, 1 ],
 [ 6000, 0, 1 ]
]; 
~data = Data(
  ( eq_controls: flat ),
  presets: (
    flat: [
      [ 100, 0, 1 ],
      [ 250, 0, 1 ],
      [ 1000, 0, 1 ],
      [ 3500, 0, 1 ], 
      [ 6000, 0, 1 ]
    ], 
    loudness: [
      [ 78.0, 7.5, 0.65 ],
      [ 250, 0, 1 ],
      [ 890.0, -9.5, 3.55 ], 
      [ 2800.0, 3.5, 1.54 ],
      [ 7400.0, 7.0, 1.0 ]
    ], 
    telephone: [
      [ 600.0, -22.0, 0.7 ],
      [ 250, 0, 1 ],
      [ 1200.0, -2.0, 0.5 ],
      [ 1800.0, 1.0, 0.5 ],
      [ 4000.0, -22.0, 0.7 ]
    ]
  )
);



~presets = M {
  ~data.presets;
};

toControl = { arg ctl;
  ctl.collect({ |item,i|
    [ item[0], item[1], item[2] ]
  }).flat;
};

flatCtl = toControl.(flat);

~toControl = M { arg c; toControl.(c) };

fromControl = { arg ctl; 
  ctl.clump(3).collect({ |item|
    [ item[0], item[1], item[2] ]
  });
};

~fromControl = M { arg c; fromControl.(c) };

~setEq = { arg ev, node, params;
  node.setn('eq_controls', toControl.(params));
};

~ar3 = {
  { arg input;
    var frdb = fromControl.( Control.names([\eq_controls]).kr( flatCtl ) );

    input = BLowShelf.ar( input, *frdb[0][[0,2,1]].lag(0.1) );
    input = BPeakEQ.ar( input, *frdb[1][[0,2,1]].lag(0.1));
    // input = BPeakEQ.ar( input, *frdb[2][[0,2,1]].lag(0.1));
    // input = BPeakEQ.ar( input, *frdb[3][[0,2,1]].lag(0.1));
    input = BHiShelf.ar( input, *frdb[4][[0,2,1]].lag(0.1));
    input;
  }
};

~ar4 = {
  { arg input;
    var frdb = fromControl.( Control.names([\eq_controls]).kr( flatCtl ) );

    input = BLowShelf.ar( input, *frdb[0][[0,2,1]].lag(0.1) );
    input = BPeakEQ.ar( input, *frdb[1][[0,2,1]].lag(0.1));
    input = BPeakEQ.ar( input, *frdb[2][[0,2,1]].lag(0.1));
    // input = BPeakEQ.ar( input, *frdb[3][[0,2,1]].lag(0.1));
    input = BHiShelf.ar( input, *frdb[4][[0,2,1]].lag(0.1));
    input;
  }
};

~ar5 = {
  { arg input;
    var frdb = fromControl.( Control.names([\eq_controls]).kr( flatCtl ) );

    input = BLowShelf.ar( input, *frdb[0][[0,2,1]].lag(0.1) );
    input = BPeakEQ.ar( input, *frdb[1][[0,2,1]].lag(0.1));
    input = BPeakEQ.ar( input, *frdb[2][[0,2,1]].lag(0.1));
    input = BPeakEQ.ar( input, *frdb[3][[0,2,1]].lag(0.1));
    input = BHiShelf.ar( input, *frdb[4][[0,2,1]].lag(0.1));
    input;
  }
};


~edit = M { arg node, parent;
  var m = currentEnvironment;
  var params = node.get('eq_controls');
  var gui;
  if (params.notNil, {
    var eqGUIControls = EQuiParams(*params);
    gui = EQui(parent, nil, nil, eqGUIControls)
      .action_({ arg v, params, prefix;
        node.setn('eq_controls', params.asControlArray);
      })
      .front;

  });
  gui;
};
