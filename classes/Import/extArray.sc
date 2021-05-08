+ Array {
	importFrom { arg module;
		try {
			var mod = module.asModule;
			this.do({ arg key;
				var member = mod.at(key);
				if (member.class == Function) {
					member = { arg ... args;
						member.value(mod, *args);
					};
				};
				currentEnvironment.put(key, member);
			});
		} { |error|
			"module % not found".format(module).postln;
		}
	}
}