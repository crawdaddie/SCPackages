ComboRect {
	var <rects;
	*new { arg ...rects;
		^super.newCopyArgs(rects);
	}

	intersects { arg bounds;
		rects.do { arg comboRect;
			if (comboRect.intersects(bounds)) {
				^true;
			}
		};
		^false
	}

}