Promise {
  classvar promiseStates;
  var <state;
  var value;
  var then, catch, routine, clock;
  *new { arg fn, clock = AppClock;
    ^super.new.init(fn, clock);
  }
  *initClass {
    promiseStates = (
      'PENDING': "PENDING",
      'FULFILLED': "FULFILLED",
      'REJECTED': "REJECTED"
    );
  }
  init { arg fn, aClock;
    clock = aClock;
    routine = Routine({
      state = promiseStates['PENDING'];
      try {
        var thenFunc = then !? { |a| "default then".postln; a.postln; };
        value = fn.value();
        promiseStates['FULFILLED'];
        thenFunc.value(value);
      } { |e|
        var catchFunc = catch !? {};
        promiseStates['REJECTED'];
        catchFunc.value(e);
      }
    }).play;
  }

  then { arg fn;
    then = fn;
  }
  await {
    
    ^Environment.use {
      this.postln;
      1;
    }
  }

  catch { arg fn;
    catch = fn;
  }
}
