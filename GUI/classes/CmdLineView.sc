CmdLineView {
	var searchField;
	var listView;
	var container;
	
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

	*new { arg bounds = Rect(0, 0, 200, 50);
		^super.new.init(bounds);
	}
	
	init { arg bounds;
		// var window = Window(border: false);
		container = Window(
			// parent: window,
			// bounds: bounds,
			border: false
		)
		.layout_(VLayout());

		// container.parent.asView.border_(false);
		
		container.layout.spacing = 0;
		container.layout.margins = 0!4;

		// create search field
		searchField = TextField(container);
		searchField.background_(Color.clear);
		searchField.string = "";
		// searchField pixel height = 24;


		// create list view
		listView = EZListView(container, bounds: container.bounds)
			.globalAction_({ arg view;
				var value = view.value;
				view.items[value].postln;
			})
		;

		listView.listView.background = Color.clear;
		listView.items_(list);


		searchField.action = { arg field; var search = field.value; this.find(search) };
		searchField.keyUpAction = { arg view; view.doAction };

		searchField.asView.keyDownAction = { arg view, char, mod, unicode, keycode, key;
			[char, mod, unicode, keycode, key].postln;
			if ([mod, key] == [0, 16777216]) {
				container.close;
			}

		};

		container.front;
	}

	find { arg query;
		if (query == "") {
			this.displayItems(list);
		} { 
			var filtered = list.select(_.contains(query.asString));
			filtered.postln;
			this.displayItems(filtered);
		}
	}

	displayItems { arg items;
		// var bounds = container.bounds;
		// bounds.postln;
		// container.bounds = bounds.set(
		// 	argLeft: bounds.left,
		// 	argTop: bounds.top,
		// 	argWidth: bounds.width,
		// 	argHeight: 24 + (items.size * 17)
		// );

		listView.items_(items);

	}

}