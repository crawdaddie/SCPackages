+ Array {
	partition { arg predicate;
		^[
			this.select({ arg member; predicate.value(member) }),
			this.select({ arg member; predicate.value(member).not })
		];
	}

	groupBy { arg predicate;
		var partition = IdentityDictionary();

		this.do { | object |
			var identifier = predicate.value(object);


			partition.at(identifier) !? { |set|
				set = set.add(object);
			} ?? {
				partition.put(identifier, [object]);
			};
			
		};
		^partition
	}
}
