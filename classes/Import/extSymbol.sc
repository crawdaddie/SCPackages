+ Symbol {

	import { arg expose = true;
		^Import(this, expose: expose);
	}
	require {
		^Import(this, false)
	}

	importFrom { arg module, expose = true;
		var importedMember;
		try {
			var mod = module.asModule;
			importedMember = mod.at(this);
			if (importedMember.class == Function) {
				importedMember = { arg ... args;
					mod.at(this).value(mod, *args);
				};
			};
			if (expose) {
				currentEnvironment.put(this, importedMember);
			};
			^importedMember
		} { |error|
			"module % not found".format(module).postln;
		};
	}

	asModule {
		// imports the relevant module but doesn't automatically 'expose'
		// it to the current Environment, ie. doesn't add it in a new env variable,
		// just returns the module
		^Import(this, expose: false);
	}
}
