CmdLineView {
	var textField;
	var listView;
	var query;
	var selectedValue;

	classvar list;
	*initClass {
		list = [
			"rand",
			"rand2",
			"binomial",
			"gauss",
			"rando",
			"random",
			"qran"	
		];
	}

	*new { arg bounds = Rect(0, 0, 200, 40);
		^super.new.init(bounds);
	}
	
	init { arg bounds;
		var container;
		query = "";
		textField = TextField();
		listView = ListView().canFocus_(false);

		container = View(
			bounds: bounds
		)
		.layout_(VLayout(textField, listView));
		
		container.layout.spacing = 0;
		container.layout.margins = 0!4;

		textField.keyUpAction = { | textField ...keyArgs |
			this.keyUpHandler(*keyArgs);
		};

		textField.keyDownAction = { | textField ...keyArgs |
			this.keyDownHandler(*keyArgs);
		};

		selectedValue = nil;


		// listView.action = { }

		container.front;
	}

	keyDownHandler { | char, modifiers, unicode, keycode, key |
		var originalVal = listView.value;
		query = textField.string;
		if (char.isPrint) {
			query = query ++ char;
			listView.items_(this.filterList(query));
			selectedValue = 0;
			listView.value = selectedValue;
		}; // character press

		if ([modifiers, key] == [ 0, 16777219 ] && query != "") {
			query = query[0 .. (query.size - 1)];
			listView.items_(this.filterList(query));
			selectedValue = 0;
			listView.value = selectedValue;
		}; // backspace


		if ([modifiers, key] == [0, 16777217]) {
			this.tabThroughList()
		}; // tab

		if ([modifiers, key] == [ 0, 16777220 ]) {
			// query.postln;
		}; // enter
	}

	keyUpHandler { | char, modifiers, unicode, keycode, key |
		// if (query.size != textField.string.size) {
		// 	var filteredList;
		// 	query = textField.string;
		// 	listView.items_(this.filterList);
		// 	listView.value = 0;
		// };
	}

	filterList { arg queryString;
		^list.select(_.contains(queryString.asString));
	}

	tabThroughList {
		selectedValue = (selectedValue + 1) % listView.items.size;
		listView.value = selectedValue;
		// var size = listView.items.size;
		// var val = listView.value;
		// "orig".postln;
		// listView.value.postln;
		// listView.items.postln;
		// val !? { arg val;
		// 	listView.value = (val + 1) % size; 
		// } ?? {
		// 	listView.value = 0;
		// };
		// "after".postln;
		// listView.value.postln;
		// listView.items.postln;

	}
}