Project {
	classvar <recentProjects;
	classvar recentProjectsFilePath;
	classvar emptyProjectDir;
	classvar <>defaultProjectsDir = "/home/adam/projects/supercollider/projects";

	classvar <projectFile, <projectDir, <srcDir, <saveDir, <dataDir;
	classvar <canvas;
	classvar <player;
	classvar <assetView;

	*initClass {
		recentProjectsFilePath = Project.filenameSymbol.asString.dirname +/+ "recentProjects";
		recentProjects = File
			.readAllString(recentProjectsFilePath)
			.split($\n)
			.select({ arg p; p != ""});
		

		emptyProjectDir = Project.filenameSymbol.asString.dirname +/+ "emptyProject";
	}

	*setRecents { arg path;
		recentProjects = [path] ++ recentProjects.select({ arg p; p != path });		

		File.use(
			recentProjectsFilePath,
			"w",
			{ |f|
				f.write(recentProjects.asArray.join("\n"));
			}
		);
	}
		
	*new { arg projectFile;
		^this.initProject(projectFile)
	}

	*openLatest {
		this.new(recentProjects[0]);
	}

	*initProject { arg projectFile;
		
		projectFile !? {
			this.initFromProjectFile(projectFile);
		} ?? {
			this.initNewProject()
		};
		StoreHistory.enable;	
		Dispatcher.connectObject(
			this,
			'moveObjects',
			'deleteObjects',
			'pasteObjects',
			'save',
			'open',
			'playStore',
		);
	}

	*load { arg path;
    this.setPaths(path);
		(srcDir +/+ "synths.scd").load;
		path !? {
			Store.readFromArchive(path);
		};
		canvas = SequencerCanvas(Store.global);
		assetView = AssetView();
    EnvirGui(Store.global.timingContext);

		Store.postTree;
	}

	*setPaths { arg path;
		projectFile = path;
		saveDir = projectFile.dirname;
		projectDir = saveDir.dirname;
		srcDir = projectDir +/+ "src";
		dataDir = projectDir +/+ "data";
	}

	*initFromProjectFile { arg path;
		
		if (path.pathMatch.size != 1) {
			Error("project file % does not exist".format(path)).throw;
		};
		this.setPaths(path);
		this.load(path);
		this.setRecents(path);
	}

	*initNewProject {
		Dialog.savePanel({ arg path;
			var name = path.basename; 
			"cp -R '%' '%'".format(emptyProjectDir, path).systemCmd;
			this.setPaths((path +/+ "saves/%.scproj".format(name)));
			this.setRecents(projectFile);
			this.save();
      this.load(projectFile);
		},
		path: defaultProjectsDir
		);
	}

	*moveObjects { arg payload;
		var updates = Dictionary();
		var store = Store.at(payload.storeId);
		var timingContext = store.timingContext;
	
		updates = payload.updates.do { |update|
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
	}

	*deleteObjects { arg payload;
		Store.patch(
			Dictionary.with(
				*payload.toDelete.collect({ arg id; id -> [nil] })
			),
			payload.storeId
		);
	}

	*pasteObjects { arg payload;
		var newItems = payload.items.collect({ arg item;
			item.timestamp = payload.x + item.timestamp;
			item.row = payload.y + item.row;
			item;
		});

		newItems.do { arg item;
			Store.addObject(item, payload.storeId)
		}
	}
	
	*save { arg payload;
		if (projectFile.isNil) {
			Dialog.savePanel(
				{ |path|
					"saving to %".format(projectFile).postln;
					this.setPaths(path);
					Store.global.writeMinifiedTextArchive(projectFile);
					canvas.parent.name = "sequencer - %".format(projectFile.basename);
				},
				path: saveDir
			);
		} {
			"saving to %".format(projectFile).postln;
			Store.global.writeMinifiedTextArchive(projectFile);
			saveDir = projectFile.dirname;
		};
	}
	
	*open { arg payload;
		Dialog.openPanel(
			{ |path|
				this.setPaths(path);
				this.load(path);
			},
			path: saveDir
		);
	}

	*playStore { arg payload;
		var offset = Store.at(payload.storeId).getOffset;
		var startPos = payload.startPos + offset;
		player !? {
			player.stop;
			Dispatcher((
				type: 'playerStopped',
				payload: (
					player: player,
					stopPosition: player.currentPosition
				)
			));
			player = nil;
		} ?? {
			player = StorePlayer(
				Store.global,
				startPos
			);
			player.play;
			Dispatcher((
				type: 'playerStarted',
				payload: (
					player: player,
					startPosition: startPos
				)
			));
		}
	}

	*initAssetView {
    ^AssetView();
  }
}

ProjectKeyActionManager {
  *new {
    ^super.new();
  }
  keyDownAction { arg view, char, modifiers, unicode, keycode, key;
		switch ([modifiers, key]) 
      { [ 262144, 83 ] } { Project.save } // ctrl-s
    ;
  }

}
