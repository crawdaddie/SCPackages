Data : Environment {
  var <>savePath;
  var <>defaults;
  var <>presets;
  *new { arg defaults;
    var cwd = thisProcess.nowExecutingPath;
    var savePath = cwd.dirname +/+ "." ++ cwd.basename.replace(".scd", ".data.scd");
    try {
      var savedData = savePath.load;
      savedData.defaults = defaults;
      savedData.proto = defaults;
      ^savedData;
    } { |error|
			// "module % not found".format(savePath).postln;
      ^super.new().init(defaults, savePath)
    };
  }

  init { arg argDefaults, argSavePath;
    defaults = argDefaults; 
    savePath = argSavePath;
    presets = ();
    this.know_(true);
    this.putAll(defaults);
  }

  savePreset { arg name, input;
    var copy = ().putAll(this).putAll(input);
    presets.put(name, copy);
    this.save;
  }

  loadPreset { arg name;
    var preset = presets[name];
    this.putAll(preset);
    this.save;
    ^preset;
  }

  atAllPairs { arg ...keys;
    ^keys.collect({ arg key;
      var value = this[key];
      if (value.class == Env, {
        value = [value.asArray];
      });
      [key, value]
    }).flatten(1)
  }

  save {
    this.writeMinifiedTextArchive(savePath);
  }
	// archiving
	writeMinifiedTextArchive { arg pathname, copy;
		var text = this.asMinifiedTextArchive;
		var file = File(pathname, "w");
		if(file.isOpen) {
			protect {
				file.write(text);
			} { file.close };
		} {
			MethodError("Could not open file % for writing".format(pathname.asCompileString), this).throw;
		}
	}

  asMinifiedTextArchive {
		var objects, list, stream, firsttime = true;
  
		objects = IdentityDictionary.new;

		this.getContainedObjects(objects);

		stream = CollStream.new;
		stream << "var o,p,n=nil;\n";

		list = List.newClear(objects.size);
		objects.keysValuesDo {|obj, index| list[index] = obj };

		stream << "o=[";
		list.do {|obj, i|
			var size;
			if (i != 0) { stream << ","; };
			if ((i & 3) == 0) { stream << "" };
			obj.checkCanArchive;
			if (obj.archiveAsCompileString) {
				// objects like functions, strings etc
				// cannot have open functions here
				stream << obj.asCompileString;
			}{
				size = obj.indexedSize;
				stream << obj.class.name << ".prNew";
				if (size > 0) {
					stream << "(" << size << ")"
				};
			};
		};
		stream << "];\np=[";
		// put in slots
		firsttime = true;
		list.do {|obj, i|
			var slots;
			if (obj.archiveAsCompileString.not) {
				slots = obj.getSlots;
				if (slots.size > 0) {
					if (firsttime.not) { stream << ","; };
					firsttime = false;
					// stream << "\n\t// " << obj.class.name;
					// stream << "\n\t";
					stream << i << ",[";
					if (obj.isKindOf(ArrayedCollection)) {
						slots.do {|slot, j|
							var index;
							if (j != 0) { stream << ","; };
							// if ((j != 0) && ((j & 3) == 0)) { stream << "\n\t\t" };
							index = objects[slot];
							if (index.isNil) {
								var slotcs = slot.asCompileString;
								if (slotcs == "nil") { slotcs = "n"};
								stream << slotcs;
							}{
								stream << "o[" << index << "]";
							};
						};
					}{
						slots.pairsDo {|key, slot, j|
							var index;
							if (j != 0) { stream << ","; };
							// if ((j != 0) && ((j & 3) == 0)) { stream << "\n\t\t" };
							stream << key << ":";
							index = objects[slot];
							if (index.isNil) {
								stream << slot.asCompileString;
							}{
								stream << "o[" << index << "]";
							};
						};
					};
					stream << "]";
				};
			};
		};
		stream << "\n];\n";

		stream << "prUnarchive(o,p);\n";
		^stream.contents
	}
}
