Clipboard {
	classvar <items;

	*initClass {
		items = [];
	}

	*add { arg id;
		var item = Store.at(id);
		items = items.add(item);
		// items.postln;
	}

	*clear {
		items = [];
	}

	*normalizedItems {
		// this returns the items array but with their timestamps offset against
		// the earliest item, so let's say items would be a set of timestamps
		// [0.2, 0.4, 0.5, 0.7] this would return [0, 0.2, 0.3, 0.5]
		var earliestTimestamp = items[0].timestamp;
		var lowestRow = items[0].row;
		items.do { |item|
			earliestTimestamp = min(earliestTimestamp, item.timestamp);
			lowestRow = min(lowestRow, item.row);
		};
		
		^items.collect { |item|
			var newItem = item.deepCopy;
			newItem.timestamp = item.timestamp - earliestTimestamp;
			newItem.row = item.row - lowestRow;
			newItem;
		}
	}

}