SequenceableSoundfileBlock : SequenceableBlock {
	var soundfileMod, <waveformObject, <startPos;
	*new { arg event;
		^super.new(event).initSoundfileView(event);
	}

	initSoundfileView { arg event;
		soundfileMod = Mod(event.soundfile);
		startPos = event.startPos;
		label = "% - %".format(event.soundfile.basename, event.id);
		this.getWaveform;
		^this;
	}

	renderView { arg origin, parentBounds;
		super.renderView(origin, parentBounds);
		
		this.renderWaveform(origin);
	}

	objectUpdated { arg payload;
		super.objectUpdated(payload);
		payload.startPos !? { |startPos|
			startPos = startPos;
		};
	}

	zoomBy { arg zoomX = 1, zoomY = 1;
		super.zoomBy(zoomX, zoomY);
		this.getWaveform;
	}

	getWaveform {
		waveformObject = soundfileMod.getWaveform(zoom.x);
	}

	renderWaveform { arg origin;

		var waveform = waveformObject.waveform;
		var renderBounds = bounds.moveBy(origin.x, origin.y);
		var height = renderBounds.height;
		var waveformColor = color.multiply(Color(0.5, 0.5, 0.5));

		Pen.smoothing = true;
		Pen.strokeColor = waveformColor;
		
		if (waveform.size > 0) {
			var middle = (renderBounds.leftTop + renderBounds.leftBottom) / 2;
			min(renderBounds.width, waveform.size).do { arg index;
				var data = waveform[index];
				var max = middle + Point(0, data[0] * height / 2);
				var min = middle + Point(0, data[1] * height / 2);

				Pen.line(max, min);
				Pen.fillStroke;
				middle.x = middle.x + 1;
			}
		}
	}

	// edit {
	// 	// no-op
	// 	super.edit();
	// }

	setLeftTo { arg left;
		super.setLeftTo(left);
		// startPos = 
	}
}