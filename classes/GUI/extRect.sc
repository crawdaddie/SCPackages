+ Rect {
	scaleBy { arg h, v;
		^Rect(
			left * h,
			top * v,
			width * h,
			height * v
		)
	}
}