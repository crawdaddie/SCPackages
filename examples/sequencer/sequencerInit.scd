(
var system = (
	projectDir: "/Users/adamjuraszek/PROJECTS/supercollider/packages/examples/sequencer/",
	srcDir: "/Users/adamjuraszek/PROJECTS/supercollider/packages/examples/sequencer/src/",
	projectFile: "/Users/adamjuraszek/PROJECTS/supercollider/packages/examples/sequencer/saves/opentest.scproj",
	saveDir: thisProcess.nowExecutingPath !? _.dirname +/+ "saves" ?? "/Users/adamjuraszek/PROJECTS/supercollider/packages/examples/sequencer/saves/",
	
	moveObjects: { arg ev, payload;
		var updates = Dictionary();
		var store = Store.at(payload.storeId);
		var timingContext = store.getTimingContext;
	
		payload.updates.do { |update|
			var id = update.id;
			var newState = (
				beats: update.x,
				row: update.y,
				length: update.length
			);
			updates.put(id, newState);
		};
		if (updates.size > 0) {
			Store.patch(updates, store.id);
		}
	},
	
	deleteObjects: { arg ev, payload;
		Store.patch(
			Dictionary.with(
				*payload.toDelete.collect({ arg id; id -> [nil] })
			),
			payload.storeId
		);
	},
	
	pasteObjects: { arg ev, payload;
		var newItems = payload.items.collect({ arg item;
			item.timestamp = payload.x + item.timestamp;
			item.row = payload.y + item.row;
			item;
		});

		newItems.do { arg item;
			Store.addObject(item, payload.storeId)
		}
	},
	
	save: { arg ev, payload;
		ev.use {
			if ((payload.newFile || ~projectFile.isNil), {
				Dialog.savePanel({ arg path;
					ev.projectFile = path;
					"saving to %".format(~projectFile).postln;
					Store.archive(~projectFile);
					~sequencerCanvas.parent.name = "sequencer - %".format(~projectFile.basename);
					~saveDir = ~projectFile.dirname;
					},
					path: ~saveDir
				);
			}, {
				"saving to %".format(~projectFile).postln;
				Store.archive(~projectFile);
				~saveDir = ~projectFile.dirname;
			});
		}
	},
	
	open: { arg ev, payload;
		Dialog.openPanel(
			{ arg path;
				ev.use {
					Store.readFromArchive(path);
					~sequencerCanvas.fromStore(Store.global);
					~projectFile = path;
					~sequencerCanvas.parent.name = "sequencer - %".format(path.basename);
				}
			},
			path: ~saveDir
		);
	}
);



Dispatcher.connectObject(system);
StoreHistory.enable;

system.use {
	Store.readFromArchive(~projectFile);
	~sequencerCanvas = SequencerCanvas.fromStore(Store.global);
	~sequencerCanvas.parent.name = "sequencer - %".format(system.projectFile.basename);
	Store.postTree;
};
)



