var selectedWaveform;
var lastUsedSFEvent = 'sampleEvent'.importFrom('baseEvents').create((soundfile: ~path));
~soundfile = SoundFile.openRead(~path);
~bufRateScale = ~soundfile.sampleRate / Server.default.sampleRate;
~getIndex = { arg ev, zoom;
  ev.bufRateScale * zoom;
};
"bufRateScale for sample % is %".format(~path, ~bufRateScale).postln;

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
// ~waveformCache = WaveformCache(~soundfile);

~createWaveform = { arg ev, zoom = 1, completionCallback;
	var duration = ev.soundfile.duration;
  var index = ev.getIndex(zoom);
	var chunks = (duration * Theme.horizontalUnit * index).asInt;
	var waveformObject = (waveform: Array.fill(chunks, [0, 0]));
	ev.waveformCache.put(index, waveformObject);
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
    completionCallback.();
	});
	waveformObject.task.start(AppClock);
};

~getWaveform = { arg ev, zoom = 1, completionCallback;
	var prevWaveform = selectedWaveform;
  var index = ev.getIndex(zoom);

	selectedWaveform = ev.waveformCache.at(index) !? { arg object;
		object.task.resume;
		object;
		} ?? { ev.createWaveform(zoom, completionCallback) };

	prevWaveform !? { |wf| wf.task.pause };

	(selectedWaveformObject: selectedWaveform, previousWaveformObject: prevWaveform);
};


~setSFEvent = M { arg event;
  lastUsedSFEvent = event; 
};

~getSFEvent = M { lastUsedSFEvent };

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

