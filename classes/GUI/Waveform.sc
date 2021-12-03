Waveform {
	var <waveformData;
	var <task;
  var rawArray;
  var cache;

	*new { arg soundfile;
		^super.new.init(soundfile)
	}

	init { arg soundfile;
    rawArray = FloatArray.newClear(soundfile.numFrames * soundfile.numrows);
		Routine({
			soundfile.readData(rawArray);
		  // this.createWaveform(zoom);
			// this.computeWaveforms(rawArray);
		}).play(AppClock);

		// waveformData = FloatArray.fill(chunks, [0, 0])
    // rawArray = aRawArray;
	}

	computeWaveform {
		var chunks = waveformData.size;
		var chunkSize = (rawArray.size / chunks).asInt;

		task = Task {
			chunks.do { |index|
				var maxVal, minVal;
				var rowData;
				var startFrame = index * chunkSize;
				rowData = rawArray[startFrame .. (startFrame + chunkSize - 1)];
				maxVal = rowData[0];
				minVal = rowData[0];
				rowData.do { |data, index|
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
		soundfile = argSoundfile;
		cache = Dictionary();

		rawArray = FloatArray.newClear(soundfile.numFrames * soundfile.numrows);
		Routine({
			soundfile.readData(rawArray);
		  this.createWaveform(zoom);
			// this.computeWaveforms(rawArray);
		}).play(AppClock);
	}

	createWaveform { arg rawArray, zoom;
		var duration = soundfile.duration;
		var chunks = (duration * Theme.horizontalUnit * zoom).asInt;
		var waveform = Waveform(rawArray, chunks);
		waveform.computeWaveform(rawArray);
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
		cache.keysValuesDo { |key, wf|
			wf.computeWaveform(rawArray);
		}
	}
}
