SequencerGrid {
	classvar mainGridColor;
	classvar subdivisionColor;
	var <tick;

	*new {
		^super.new.init()
	}

	*initClass {
		mainGridColor = Color.grey(0.8, 1);
		subdivisionColor = Color.grey(0.7, 0.5);
	}

	init {
	}


	drawYGrid { arg origin, bounds, zoom;
		var gap = SequenceableBlock.yFactor * zoom.y;
		var yOffset = origin.y + (0 - origin.y).roundUp(gap);

		Pen.strokeColor_(mainGridColor);
		while ({ yOffset < bounds.height }) {
			Pen.line(Point(0, yOffset), Point(bounds.width, yOffset));
			Pen.stroke;
			yOffset = yOffset + gap;
		}
	}

	drawXGrid { arg quantX, origin, timingOffset, bounds, zoom, subdivisions;
		var gap = quantX * zoom.x;
		var timingOffsetPixels = timingOffset * gap;
		var initXOffset = origin.x + (0 - origin.x).roundUp(gap) - timingOffsetPixels;
		var xOffset = initXOffset;
		
		var minorGap = gap / subdivisions;
		var initSubOffset = origin.x + (0 - origin.x).roundUp(minorGap) - timingOffsetPixels; 
		var subOffset = initSubOffset;
		// var tickNum = 0;
		tick = minorGap;

		
		Pen.strokeColor_(mainGridColor);
		while ({ xOffset < bounds.width }) {
			Pen.line(Point(xOffset, 0), Point(xOffset, bounds.height));
			// Pen.stroke;
			xOffset = xOffset + gap;
		};


		Pen.strokeColor_(subdivisionColor);
		while ({ subOffset < bounds.width }) {
			Pen.line(Point(subOffset, 0), Point(subOffset, bounds.height));
			
			// [tickNum, subOffset, minorGap].postln;
			subOffset = subOffset + minorGap;
			// tickNum = tickNum + 1;
		}; 
		Pen.stroke;
	}

	renderView { arg quantX, origin, timingOffset, bounds, zoom, subdivisions;
		this.drawYGrid(origin, bounds, zoom);
		this.drawXGrid(quantX, origin, timingOffset, bounds, zoom, subdivisions);
	}
}