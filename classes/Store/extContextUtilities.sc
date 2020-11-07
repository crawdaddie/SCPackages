+ Store {
	getUtility { arg utilityName;
		this.at(utilityName) !? { arg ctx;
			^ctx;
		} ?? {
			var parent = this.getParentStore;
			^parent.getUtility(utilityName);
		}
	}

	setUtility { arg utilityName, value = ();
		var defaultUtility = defaultContexts[utilityName];
		super.put(utilityName, defaultUtility.putAll(value));
	}

	timingContext_ { arg ctx = ();
		this.setUtility('timingContext');
	}

	timingContext {
		^this.getUtility('timingContext');
	}

	transportContext {
		^this.getUtility('transportContext');
	}

	transportContext_ {
		^this.setUtility('transportContext');
	}

	setLoopPoints { arg loopPoints, storeUpdates;
		if (loopPoints == [nil] || loopPoints.isNil || (loopPoints[0] == loopPoints[1])) {
			this.transportContext.loopPoints = nil;
		} {
			this.transportContext.loopPoints = loopPoints.sort;
		};
		storeUpdates.transportContext = this.transportContext;
	}

	getOffset {
		var path = this.getPath;
		if (path.isNil) {
			^0
		} {
			^this.getParentStore.getOffset + this.timestamp ? 0;
		}
	}

	getLoopPoints {
		^this.transportContext.loopPoints;
	}
}