SoundfileCanvasObject : SequenceableCanvasObject { 
  *new { arg item, canvasProps;
    ^super.new(item, canvasProps).init(item, canvasProps);
  }

  getProps { arg item, canvasProps;
    var baseProps = super.getProps(item, canvasProps);
    ^baseProps.putAll((
      waveformObjects: Mod(item.soundfile).getWaveform(canvasProps.zoom.x, canvasProps['redraw']),
      startPos: item.startPos,
    ))
  }
  

  renderView { arg renderBounds, origin, zoom, canvasBounds, color, label, selected, waveformObjects, startPos, canvasProps; 
    super.renderView(renderBounds, origin, zoom, canvasBounds, color, label, selected, canvasProps);
    this.renderWaveform(renderBounds, origin, zoom, canvasBounds, color, label, selected, waveformObjects, startPos);
  }


  renderWaveform { arg renderBounds, origin, zoom, canvasBounds, color, label, selected, waveformObjects, startPos, canvasProps;
    var currentWF = waveformObjects.selectedWaveformObject;
    var waveform = if ((currentWF.status.notNil && currentWF.status), {
      currentWF.waveform
    }, {
      waveformObjects.previousWaveformObject.waveform
    });

		var height = renderBounds.height;
		var waveformColor = color.multiply(Color(0.5, 0.5, 0.5));

		Pen.smoothing = true;
		Pen.strokeColor = waveformColor;

		if (waveform.size > 0, {
			var middlePoint = (renderBounds.leftTop + renderBounds.leftBottom) / 2;
			var waveformSize = waveform.size;
			var framesToRender = ((1 - startPos) * waveformSize).floor.asInteger;
			var firstFrame = (startPos * waveformSize).floor.asInteger;

			min(renderBounds.width, framesToRender).do { arg index;
				var data = waveform[index + firstFrame];
				var max = middlePoint + Point(0, data[0] * height / 2);
				var min = middlePoint + Point(0, data[1] * height / 2);

				Pen.line(max, min);
				Pen.fillStroke;
				middlePoint.x = middlePoint.x + 1;
			}
    });
	}
}
