TimingContextView {
	var <bpm, <id;

	var color;

	*new { arg object;
		^super.newCopyArgs(object.bpm, object.id).init()
	}

	init {
		color = Color.grey(0.7, 1);
	}

	draw { arg bounds, subdivisions, quantize;
		var point = bounds.leftBottom;
		var string = if (quantize, { "%bpm - Q".format(bpm) }, { "%bpm".format(bpm) });
		Pen.stringAtPoint(string, point + Point(10, -20), color: color);
	}

	setBPM { arg newBPM;
		Dispatcher((type: 'setBPM', payload: (id: id, bpm: newBPM)));
		bpm = newBPM;
	}
}
 