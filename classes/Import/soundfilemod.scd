var selectedWaveform;
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

~createWaveform = { arg ev, zoom = 1, completionCallback;
	var duration = ev.soundfile.duration;
	var chunks = (duration * Theme.horizontalUnit * zoom).asInt;
	var waveformObject = (waveform: Array.fill(chunks, [0, 0]));
	ev.waveformCache.put(zoom, waveformObject);
	ev.withRawArray { |rawArray|
		ev.computeWaveform(rawArray, waveformObject, completionCallback);
	};
	waveformObject.complete = false;
	waveformObject.task = Task {};

	waveformObject;
};


~computeWaveform = { arg ev, rawArray, waveformObject, completionCallback;

	var chunks = waveformObject.waveform.size;
	var chunkSize = (rawArray.size / chunks).asInt;

	waveformObject.task = Task({
		chunks.do { |index|
			var maxVal, minVal;
			var rowData;
			var startFrame = index * chunkSize;
			rowData = rawArray[startFrame .. (startFrame + chunkSize - 1)];
			minVal = maxVal = rowData[0];
			rowData.do { |data, index|
				maxVal = max(maxVal, data);
				minVal = min(minVal, data);
			};
			waveformObject.waveform[index] = [maxVal, minVal];
		};
		waveformObject.complete = true;
    "finished waveform computation".postln;
    completionCallback.();
	});
	waveformObject.task.start(AppClock);
};

~getWaveform = { arg ev, zoom = 1, completionCallback;
	var prevWaveform = selectedWaveform;

	selectedWaveform = ev.waveformCache.at(zoom) !? { arg object;
		object.task.resume;
		object;
		} ?? { ev.createWaveform(zoom, completionCallback) };

	prevWaveform !? { |wf| wf.task.pause };

	(selectedWaveformObject: selectedWaveform, previousWaveformObject: prevWaveform);
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
};

Server.local.doWhenBooted({
	currentEnvironment.load;
});

