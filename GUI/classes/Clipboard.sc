Clipboard {
	classvar items;

	*initClass {
		items = [];
	}

	*add { arg id;
		var item = Store.at(id);
		items = items.add(item); 
	}

	*clear {
		items = [];
	}

	*normalizedItems {
		// this returns the items array but with their timestamps offset against
		// the earliest item, so let's say items would be a set of timestamps
		// [0.2, 0.4, 0.5, 0.7] this would return [0, 0.2, 0.3, 0.5]
		var earliestTimestamp = 0;
		items.do { |item|
			earliestTimestamp = min(earliestTimestamp, item.timestamp);
		};
		
		^items.collect { |item|
			var newItem = item.copy;
			newItem.timestamp = item.timestamp - earliestTimestamp;
			newItem;
		}
	}

}