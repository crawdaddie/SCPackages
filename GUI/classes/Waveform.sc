Waveform {
	var <waveformData;
	var <task;

	*new { arg chunks;
		^super.new.init(chunks)
	}

	init { arg chunks;
		waveformData = Array.fill(chunks, [0, 0])
	}

	computeWaveform { arg rawArray;
		var chunks = waveformData.size;
		var chunkSize = (rawArray.size / chunks).asInt;

		task = Task {
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

				waveformData[index] = [maxVal, minVal];
			};
		};

		task.play(AppClock);

	}

	pauseTask {
		task.pause;
	}

	resumeTask {
		task.resume;
	}
}

WaveformCache {
	var <cache;
	var <rawArray;
	var <soundfile;

	*new { arg soundfile, zoom = 1;
		^super.new.init(soundfile, zoom);
	}

	init { arg argSoundfile, zoom;
		soundfile = Soundfile;
		cache = Dictionary();
		this.createWaveform(zoom);

		rawArray = FloatArray.newClear(soundfile.numFrames * soundfile.numChannels);
		Routine({
			soundfile.readData(rawArray);
			this.computeWaveforms(rawArray);
		}).play(AppClock);
	}

	createWaveform { arg zoom;
		var duration = soundfile.duration;
		var chunks = (duration * Theme.horizontalUnit * zoom).asInt;
		var waveform = Waveform(chunks);
		waveform.computeWaveform();
		cache.put(zoom, waveform);
		^waveform
	}

	getWaveform { arg zoom;
		^cache.at(zoom) !? { | waveform |
			this.resumeTask(zoom);
			waveform;
		} ?? {
			this.createWaveform(zoom);
		}
	}

	resumeTask { arg zoom;
		var waveform;
		cache.keysValuesDo { | zoomKey, wf |
			if ((zoomKey != zoom), {
				wf.pauseTask;
			}, {
				waveform = wf;
				waveform.resumeTask
			});
		}
		^waveform;
	}

	computeWaveforms { arg rawArray;
		cache.keysValuesDo { |key, val|
			val.computeWaveform(rawArray);
		}
	}
}