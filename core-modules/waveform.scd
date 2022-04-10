~getSection = M { arg array, startPos, endPos;
  var arrayLength = array.size;
  var arrayChunk = array[(arrayLength * startPos).asInteger .. (arrayLength * endPos).asInteger];
  arrayChunk;
};

~getSectionExtrema = M { arg chunk, resolution;
  var frames = chunk.size;
  var framesPerPixel = (frames / resolution).asInteger;
  var offset = 0;
  var chunkExtrema = (resolution.asInteger).collect { arg i;
    var subChunk = chunk[offset .. offset + framesPerPixel];
    offset = offset + framesPerPixel;
    [subChunk.maxItem, subChunk.minItem];
  };
  chunkExtrema;
};

~getWaveform = M { arg 
  arr,
  resolution      /* integer */,
  startFrame = 0  /* integer */,
  endFrame        /* integer */
  ;
  var isMono = arr[0].isArray.not;
  var array = if (isMono, {[arr]}, {arr});
  var size = array[0].size;
  var start = startFrame;
  var end = endFrame ?? size;
  var waveformExtrema = array.collect({ arg arr;
    var chunk = arr[start .. end];
    var extrema = mod.getSectionExtrema(chunk, resolution);
    extrema;
  }); 
  waveformExtrema;
}
