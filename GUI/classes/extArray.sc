+ Array {
	partition { arg predicate;
		^[
			this.select({ arg member; predicate.value(member) }),
			this.select({ arg member; predicate.value(member).not })
		];
	}
}
