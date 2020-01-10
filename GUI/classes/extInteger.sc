+ Integer {
	floorBase2 {
		// returns the largest power of 2 smaller than the integer
		// var v = this;
		// var pos = 0;
		// while ( { v > 1 } ) {
		// 	v = v >> 1;
		// 	pos = pos + 1;
		// };
		// ^(2 ** pos).asInt;

		^(this.log / 2.log + 1e-10).asInteger
	}
}