Import {
	classvar <> defaultModulePath; classvar <> projectModulePath;
	*initClass {
		defaultModulePath = "/Users/adam/projects/sc/ecosystem/core-modules";
	}
	// this class is supposed to contain utilities for resolving a symbol into a path
	// used for loading an instance of Mod (module)
	*new { arg module, expose = true;
		var mod;
		var modPath = this.resolvePath(module);

		mod = Mod.new(modPath);

		if (expose, { currentEnvironment.put(this.getModuleVarname(module), mod) });

		^mod;
	}

	*getModuleVarname { arg module;
		var modString = module.asString;
		^modString.split($/).last.asSymbol
	}

	*resolvePath { arg module;
    var moduleString = module.asString;
    var pathMatch = (moduleString ++ "*" ).pathMatch;
    if (pathMatch.isEmpty) {
      var cwd = thisProcess.nowExecutingPath !? (_.dirname) ?? "./";
      // matches paths relative to the current file 
      pathMatch = (cwd +/+ moduleString ++ "*").pathMatch;
    };

		if (pathMatch.isEmpty && Project.srcDir.notNil) { pathMatch = (Project.srcDir +/+ moduleString ++ "*").pathMatch };

		if (pathMatch.isEmpty) { pathMatch = (defaultModulePath +/+ moduleString ++ "*").pathMatch };

		^pathMatch[0]
	}
}


Mod : Environment {
	classvar <>all;

	*initClass {
		all = IdentityDictionary.new();
	}

	*new { arg path;
		// check if module already exists and return that, or make a new one
 
		var mod = all.at(path.asSymbol) !? { arg existingModule;
			//format("module % already exists (passing previously loaded module)", path.split($/).last).postln;
			existingModule;
			} ?? {
				super.new.init(path);
			};

		^mod;
	}

	*newVirtual { arg path, extra;
		var mod = all.at(path.asSymbol) !? { arg module;
			format("module % already exists (passing previously loaded module)", path.split($/).last).postln;
			module;
			} ?? {
				super.new.init(path).loadVirtual(extra);
			};
		^mod;
	}

	*newWithinEnv { arg path, env; 
		^super.new.init(path).loadWithinEnv(env);
	}


	init { arg path;

		this.proto_((
			path: path,
			open: #{ arg ev; Document.open(ev[\path]) };
		));

		know = true;

		this.registerModule;

		if (PathName.new(path).isFolder) {
			this.loadFromFolder(path)
		} {
			var ext = path.splitext.last;
			switch (ext,
				"wav", { this.loadFromSoundfile },
				"mp3", { this.loadFromSoundfile },
				{ this.loadFromPath }
			);
		};

		^this
	}

	loadFromFolder { arg path;
		^this.make {
			var indexPath = ~path +/+ "index.scd";
			if (indexPath.pathMatch.size == 1) {
				var mod = super.new(indexPath);
				currentEnvironment.put(~path.basename.splitext[0].asSymbol, mod);
			} {
				(~path +/+ "*.scd").pathMatch.do({ arg fullPath;
					var mod = Mod(fullPath);
					currentEnvironment.put(fullPath.basename.splitext[0].asSymbol, mod);
				});
			}
		};
	}

	loadFromPath {
		^this.make { ~path.load };
	}

	loadFromSoundfile {
		var modpath = Mod.filenameSymbol.asString.dirname +/+ "soundfilemod.scd";
		this.make {
			modpath.load;
		};
	}

	loadVirtual { arg extra;
		^this.make { extra.value() };
	}

	loadWithinEnv { arg env;
	 	this.parent_(env);
	 	this.use { ~path.load };
	 	^this
	}

	registerModule {
		var symbol = this.at(\path).asSymbol;
		all.put(symbol, this);
	}

	reload {
		^this.loadFromPath
	}

	*reloadOnSave { arg path;
    path.postln;
		all.at(path.asSymbol) !? { arg module;
			"reloading on save: %".format(module).postln;
			fork {
				module.reload;
				Dispatcher(
					type: Topics.moduleReload, 
					payload: (
						path: path
					)
				);
			}	
		}
	}

	printModule {
		this.asString.postln;
		this.order.do({ arg key;
			if ((key != 'open') && (key != 'path')) {
				var keyString = key.asString;
				format("%: %", keyString, this.at(key).cs).postln;
			}
		});
	}

	asString {
		^format("Module %", this.at(\path).basename);
	}

	asModule {
		^this
	}
}

ModFunc {
	*new { arg function;
		^{ arg ev ... args;
			ev.use {
				function.valueArray(args);
			}
		}
	}
}

M : ModFunc {}

ModObject {
	*new { arg function;
		^{ arg ev ... args;
			ev.use {
				Environment.make({
					function.valueArray(args);
				})
				.know_(true);
			}
		}
	}
}

O : ModObject {}

+ String {

  loadSoundfile {
    // ensures soundfile is loaded
    // but returns string
    this.asSoundfileMod;
    ^this;
  }

	asSoundfileMod {
		var mod = Mod.all.at(this.asSymbol);
    if (mod.isNil) {
      mod = Import(this, false);
    };
    ^mod;
	}
}

+ Object {
  export {
    // var oldKeys, newKeys;
    // oldKeys = currentEnvironment.keys;
    // currentEnvironment.use { this.value };
    // newKeys = currentEnvironment.keys.select({ arg k; oldKeys.includes(k).not });
    currentEnvironment.exports !? {
      currentEnvironment.exports
    };
  }
}
