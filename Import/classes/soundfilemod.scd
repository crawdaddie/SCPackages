~soundfile = SoundFile.openRead(~path);

~rawArray = nil;
~afterRawArray = Set();

Routine({
	var rawArray = FloatArray.newClear(~soundfile.numFrames * ~soundfile.numChannels);
	
	~soundfile.readData(rawArray);
	~rawArray = rawArray;

	~afterRawArray.do { |object|
		object.value(~rawArray)
	};
	
	~afterRawArray = Set();
}).play(AppClock);

~withRawArray = { arg ev, object;
	ev.rawArray !? { |rawArray|
		object.value(rawArray)
	} ?? {
		ev.afterRawArray.add(object)
	}
};


~waveformCache = Dictionary();

~createWaveform = { arg ev, zoom = 1;
	var duration = ev.soundfile.duration;
	var chunks = (duration * SequenceableBlock.xFactor * zoom).asInt;
	var waveformObject = (waveform: Array.fill(chunks, [0, 0]));
	ev.waveformCache.put(zoom, waveformObject);
	ev.withRawArray { |rawArray|
		ev.computeWaveform(rawArray, waveformObject);
	};

	waveformObject;
};


~computeWaveform = { arg ev, rawArray, waveformObject;

	var chunks = waveformObject.waveform.size;
	var chunkSize = (rawArray.size / chunks).asInt;

	waveformObject.complete = false;
	waveformObject.task = Task({
		chunks.do { |index|
			var maxVal, minVal;
			var channelData;
			var startFrame = index * chunkSize;
			channelData = rawArray[startFrame .. (startFrame + chunkSize - 1)];
			maxVal = channelData[0];
			minVal = channelData[0];
			channelData.do { |data, index|
				maxVal = max(maxVal, data);
				minVal = min(minVal, data);
			};
			waveformObject.waveform[index] = [maxVal, minVal];
		};
		waveformObject.complete = true;
	});
	waveformObject.task.start(AppClock);
};

~getWaveform = { arg ev, zoom = 1;
	ev.waveformCache.at(zoom) !? { arg object; object } ?? { ev.createWaveform(zoom) };
};



~buffer = nil;
~open = #{ arg ev;
};
~load = { arg ev;
	ev.use {
		"loading buffer %".format(~path).postln;
		// ~soundfile.openRead;
		~buffer = Buffer.read(Server.local, ~path);
	}
}