SequencerGrid {
	classvar mainGridColor;
	classvar subdivisionColor;
	var <tick;

	*new {
		^super.new.init()
	}

	*initClass {
		mainGridColor = Color.grey(0.7, 1);
		subdivisionColor = Color.grey(0.7, 0.5);
	}

	init {}


	drawYGrid { arg origin, canvasBounds, zoom;
		var gap = Theme.verticalUnit * zoom.y;
		var yOffset = origin.y + (0 - origin.y).roundUp(gap);

		Pen.strokeColor_(mainGridColor);
		while ({ yOffset < canvasBounds.height }) {
			Pen.line(Point(0, yOffset), Point(canvasBounds.width, yOffset));
			Pen.stroke;
			yOffset = yOffset + gap;
		}
	}

	drawXGrid { arg quantX, origin, timingOffset, canvasBounds, zoom, quantSubdivisions;
		var gap = quantX * zoom.x;
		var timingOffsetPixels = timingOffset * gap;
		var initXOffset = origin.x + (0 - origin.x).roundUp(gap) - timingOffsetPixels;
		var xOffset = initXOffset;
		
		var minorGap = gap / quantSubdivisions;
		var initSubOffset = origin.x + (0 - origin.x).roundUp(minorGap) - timingOffsetPixels; 
		var subOffset = initSubOffset;
		// var tickNum = 0;
		tick = minorGap;

		
		Pen.strokeColor_(mainGridColor);
		while ({ xOffset < canvasBounds.width }) {
			Pen.line(Point(xOffset, 0), Point(xOffset, canvasBounds.height));
			// Pen.stroke;
			xOffset = xOffset + gap;
		};


		Pen.strokeColor_(subdivisionColor);
		while ({ subOffset < canvasBounds.width }) {
			Pen.line(Point(subOffset, 0), Point(subOffset, canvasBounds.height));
			
			// [tickNum, subOffset, minorGap].postln;
			subOffset = subOffset + minorGap;
			// tickNum = tickNum + 1;
		}; 
		Pen.stroke;
	}

	renderView { arg quantX, origin, timingOffset, canvasBounds, zoom, quantSubdivisions = 1;
		this.drawYGrid(origin, canvasBounds, zoom);
		this.drawXGrid(quantX, origin, timingOffset, canvasBounds, zoom, quantSubdivisions);
	}

	render { arg ctx;
		^this.performWithEnvir('renderView', ctx)
	}
  onClose {
    // no-op
  }
}
