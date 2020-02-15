+ Dictionary {
	getDiff { arg thatObject;
		^thatObject.select { |val, key|
			this[key] != val;
		}
	}
}