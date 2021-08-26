+ Rect {
	scaleBy { arg h, v;
		^Rect(
			left * h,
			top * v,
			width * h,
			height * v
		)
	}

  snapToRow { arg canvasCtx;
    var y = this.top;
    var unit = canvasCtx.zoom.y * Theme.verticalUnit;
    var offset = canvasCtx.origin.y ; 
    var constrainedValue = (y - offset).round(unit) + offset;

		this.top = constrainedValue;
		^this; 
  }

  snapToBeat { arg canvasCtx;
    var x = this.left;
    var unit = canvasCtx.zoom.x * Theme.horizontalUnit / canvasCtx.quantSubdivisions;
    var offset = canvasCtx.origin.x;
    var constrainedValue = (x - offset).round(unit) + offset;

		this.left = constrainedValue;
		^this;
  } 
}

+ Point {
  snapToRow { arg canvasCtx;
    var y = this.y;
    var unit = canvasCtx.zoom.y * Theme.verticalUnit;
    var offset = canvasCtx.origin.y ; 
    var constrainedValue = (y - offset).round(unit) + offset;

		this.y = constrainedValue;
		^this; 
  }
  snapToBeat { arg canvasCtx;
    var x = this.x;
    var unit = canvasCtx.zoom.x * Theme.horizontalUnit;
    var offset = canvasCtx.origin.x;
    var constrainedValue = (x - offset).round(unit) + offset;

		this.x = constrainedValue;
		^this;
  }
}
