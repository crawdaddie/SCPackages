+ SimpleNumber {
	asBeats { arg bpm = 60;
		^this * bpm / 60; 
	}
	asSeconds { arg bpm = 60;
		^this * 60 / bpm; 
	}
}