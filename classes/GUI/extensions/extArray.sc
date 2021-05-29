+ Array {
	partition { arg predicate;
		var selected = [];
		var notSelected = [];

		this.do { arg member;
			if (predicate.value(member), {
				selected = selected.add(member);
			}, {
				notSelected = notSelected.add(member);
			});
		}

		^[
			selected,
			notSelected
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
