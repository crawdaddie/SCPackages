~ampEnv = { arg ev, gate, defaultEnv, timeScale = 1;
  var envctl = \ampEnv.kr(defaultEnv !? defaultEnv.asArray ?? Env.adsr(0.1, 0.1, 0.9, 1).asArray);
  EnvGen.kr(envctl, gate, doneAction: Done.freeSelf, timeScale: timeScale);
};

~namedEnv = { arg ev, name, gate, defaultEnv, doneAction = 0, timeScale = 1;
  var envctl = name.asSymbol.kr(defaultEnv !? defaultEnv.asArray ?? ~data !? ~data[name] ?? Env.newClear(8).asArray);
  EnvGen.kr(envctl, gate, doneAction: doneAction, timeScale: timeScale);
};


