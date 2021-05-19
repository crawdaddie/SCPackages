Waveform {
  classvar <waveforms;
  var <cache;
  var soundfile;
  var rawArray;
  var afterRawArray;

  *new { arg path;
    ^waveforms[path] ?? super.new.init(path)
  }

  init { arg aPath;
    soundfile = Soundfile.openRead(aPath); 
  }
  getWaveform { arg zoom = 1, cb;
    
  }
}

SoundfileMod {
  var <buffer;
  var <path;
  
  *new { arg path;
    ^super.new.init(path);
  }
  
  init { arg aPath;
    path = aPath;
    Server.local.doWhenBooted({
	    this.load;
    });
  }
  
  load {
    buffer = Buffer.read(Server.local, path);
  }

}
