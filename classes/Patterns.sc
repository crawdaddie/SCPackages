R : Rest {}
Pval { 
  *new { arg nextFunc, resetFunc;
    ^Pfunc({ arg inval;
      inval.use {
        nextFunc.value;
      }
    }, resetFunc)
  }
}

+ Array {
  prand { arg repeats = inf;
    ^Prand(this, repeats);
  }
  pseq { arg repeats = inf;
    ^Pseq(this, repeats);
  }
}

