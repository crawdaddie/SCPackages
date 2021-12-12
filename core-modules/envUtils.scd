var pCURVESPEC = [0, 20].asSpec;
var pALT_MOD = 524288;


var calculateCurveValue = { arg curveValue = 0, y, initialY, initialEnvValue, slope;
  var scaler = initialY; 
  var direction = y - scaler;
  var scaledValue = direction;
  scaledValue = slope.neg * scaledValue;
  scaledValue = pCURVESPEC.map(scaledValue.magnitude) * scaledValue.sign;

  curveValue = curveValue + scaledValue;
  curveValue;
};


var editCurveAction = { arg b, curves, x, y;
  var curveIndex = b.value[0].detectIndex({ arg v; v > x }) - 1;
  var slope = if (b.value[1][curveIndex] < b.value[1][curveIndex + 1], { 1 }, { -1 });  
  var curveValue = curves[curveIndex];
  (
    curveIndex: curveIndex,
    initialY: y,
    initialEnvValue: b.asEnv.at(x),
    slope: slope,
    initialCurveValue: curveValue,
    curves: curves,
    mouseMoveAction: { arg ev, b, x, y, yRelative;
      ev.use {
        var newCurveValue = calculateCurveValue.value(
          ~initialCurveValue,
          y,
          ~initialY,
          ~initialEnvValue,
          ~slope
        ); 
        ~curves.put(~curveIndex, newCurveValue);
        b.curves_(~curves);
      } 
    }
  ).know_(true)
};

var addPointAction = {arg b, curves, x, y;
  var curveIndex = b.value[0].detectIndex({ arg v; v > x }) - 1;
  
  // (mouseMoveAction: { arg ev, b, x, y, yRelative; 
  // })
};


~editSynthEnv = { arg ev, env, onEdit;
  var b, curves = env.curves;
  var totalDuration = env.totalDuration;
  var releaseNode = env.releaseNode;
  var mouseAction;
  var strings = (releaseNode ?? 2).collect({|i|
    switch (i,
      { i == 1 }, { "attack" },
      { releaseNode.notNil && i == releaseNode }, { "release" },
      { nil });
  });

  b = EnvelopeView(w, Rect(0, 0, 230, 80))
    .drawLines_(true)
    .selectionColor_(Color.red)
    .resize_(5)
    .thumbSize_(5)
    .setEnv(env)
    .palette_(QPalette.dark)
    .grid_(Point(0.2, 0.2))
    .gridOn_(true)
    .keepHorizontalOrder_(true)
    // .elasticSelection_(true)
    .strings_(strings)
    .setEditable(0,false)
  ;

  b.setEditable(b.lastIndex, false);

  b.mouseDownAction_({ arg b, x, y, modifiers, buttonNumber, clickCount;
    var xRelativeToView = x / b.bounds.width; 
    var yRelativeToView = (b.bounds.height - y) / b.bounds.height;
    var curveIndex = b.value[0].detectIndex({ arg v; v > xRelativeToView }) - 1;

    if (modifiers == pALT_MOD) {
      mouseAction = editCurveAction.value(b, curves, xRelativeToView, yRelativeToView);
    }
  });

  b.mouseMoveAction_({ arg b, x, y, modifiers,buttonNumber, clickCount;
    var xRelativeToView = x / b.bounds.width;
    var yRelativeToView = (b.bounds.height - y) / b.bounds.height;
    mouseAction !? {
      mouseAction.mouseMoveAction(b, xRelativeToView, yRelativeToView)
    };
  });

  b.mouseUpAction_({ arg b, x, y, modifiers,buttonNumber, clickCount;
    var newEnv;
    newEnv = b.asEnv(curves, timeScale: totalDuration, releaseNode: releaseNode, loopNode: nil);
    onEdit.value(newEnv);
    b.setEnv(newEnv);
    mouseAction = nil;
  });

  b.bounds_(Rect(0.0, 53.0, 783.0, 331.0));

  b.front;
};

~editAutomationEnv = { arg ev, env, onEdit;
  var b, curves = env.curves.collect({ arg c; if( c == \lin, { 0 }, { c }) });
  var totalDuration = env.totalDuration;
  var times = env.times;
  var levels = env.levels;
  
  var mouseAction;

  b = EnvelopeView(w, Rect(0, 0, 230, 80))
    .drawLines_(true)
    .selectionColor_(Color.red)
    // .resize_(5)
    .thumbSize_(5)
    .setEnv(env)
    .palette_(QPalette.dark)
    // .grid_(Point(0.2, 0.2))
    // .gridOn_(true)
    .keepHorizontalOrder_(true)
    // .drawRects_(true)
  ;
  b.setEditable(b.lastIndex, false);


  b.mouseDownAction_({ arg b, x, y, modifiers, buttonNumber, clickCount;
    var xRelativeToView = x / b.bounds.width; 
    var yRelativeToView = (b.bounds.height - y) / b.bounds.height;
    var curveIndex = b.value[0].detectIndex({ arg v; v > xRelativeToView }) - 1;
    if (modifiers == pALT_MOD) {
      mouseAction = editCurveAction.value(b, curves, xRelativeToView, yRelativeToView);
    };

    if (clickCount == 2) {
      mouseAction = addPointAction.value(b, curves, xRelativeToView, yRelativeToView);  
    }
  });

  b.mouseMoveAction_({ arg b, x, y, modifiers,buttonNumber, clickCount;
    var yRelativeToView = (b.bounds.height - y) / b.bounds.height;
    var xRelativeToView = x / b.bounds.width;
    mouseAction !? {
      mouseAction.mouseMoveAction(b, xRelativeToView, yRelativeToView)
    };
  });

  b.mouseUpAction_({ arg b, x, y, modifiers,buttonNumber, clickCount;
    var newEnv;
    mouseAction !? {
      mouseAction.mouseUpAction(b)
    };

    newEnv = b.asEnv(curves, timeScale: totalDuration);
    onEdit.value(newEnv);
    b.setEnv(newEnv);
    mouseAction = nil;
  });

  b.bounds_(Rect(0.0, 53.0, 783.0, 331.0));

  b.front;
};



