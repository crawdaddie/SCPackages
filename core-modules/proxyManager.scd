~p = ProxySpace.new().makeTempoClock(145/60);
~clock = ~p.clock;

~tempo = M { arg tempo;
  ~clock.tempo = tempo ? ~clock.tempo;
};

~initProxies = { arg ev, expression;
  var cb, out;
  cb = expression.value;
  out = ev.p.use {
    cb.value;
  };
  out.vol = -8.dbamp;
  out.play;
  ~clock = ev.clock;
  ~out = out;
};

~mixer = { arg module ...channelNames;
  var proxySpace = currentEnvironment;
  {
    Mix.ar(
      channelNames.collect({ arg name; proxySpace[name] * (name ++ '_mix').asSymbol.kr(1, 0.05) })
    )
  };
};

~withProxySpace = M { arg cb;
  ~p.use {
    cb.value;
  }
};

~getGui = M {
  var m = ProxyMixer.small(~p, 16);
  m.switchSize(0);
  ProxyMeter.addMixer(m);
  m;
};

~editNode = { arg name, node;
  var m = currentEnvironment;
  var path = m.path.asString.replace(Project.projectDir ?? "" +/+ "", "");
  var baseName = format("% % ", path, name);
  var eqGui, eqParams;

  var ndefGui = node.edit;
  // ndefGui.zone.front;
  // ndefGui.zone.parent.findWindow.name_(baseName ++ "params");

  ndefGui.zone.keyDownAction_({ arg v, char, mod, unicode, keycode, key;
    if ([mod, unicode] == [262144, 19], {
      m.save;
    });
  });
  eqParams = node.get('eq_controls');
  if (eqParams.notNil, {
    var eq = 'eq'.asModule;
    eqGui = eq.edit(node);
    eqGui.name = baseName ++ "eq_controls";
    eqGui.keyDownAction_({ arg v, char, mod, unicode, keycode, key;
      if ([mod, unicode] == [262144, 19], {
        m.save;
      });
      // [v, char, mod, unicode, keycode, key].postln;
    });
  });
  ndefGui;
}
