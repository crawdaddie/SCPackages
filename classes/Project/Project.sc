Project {
	classvar <recentProjects;
	classvar recentProjectsFilePath;
	classvar emptyProjectDir;
	classvar <>defaultProjectsDir = "/home/adam/projects/supercollider/projects";

	classvar <projectFile, <projectDir, <srcDir, <saveDir, <dataDir;
  classvar <>canvas;
  classvar <timingContextGui;
	classvar <player;
	classvar <assetView;

	*initClass {
		recentProjectsFilePath = Project.filenameSymbol.asString.dirname +/+ "recentProjects";
		recentProjects = File
			.readAllString(recentProjectsFilePath)
			.split($\n)
			.select({ arg p; p != ""});
		

		emptyProjectDir = Project.filenameSymbol.asString.dirname +/+ "emptyProject";
    this.registerMainMenuItems;
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
	}

	*load { arg path;
    timingContextGui !? _.close;
    canvas !? _.close;
    this.setPaths(path);
		(srcDir +/+ "synths.scd").load;
		path !? {
			Store.readFromArchive(path);
		};
		canvas = SequencerCanvas(Store.global);
		// assetView = AssetView();
    timingContextGui = EnvirGui(Store.global.timingContext);
		Store.postTree;
    this.registerMainMenuItems;
    this.setRecents(path);
	}

  *registerMainMenuItems {
    MainMenu.register(MenuAction("New", {this.initProject}), "File");
    MainMenu.register(MenuAction("Save", {this.save}), "File");
    MainMenu.register(
      Menu(
        *recentProjects.collect({ arg recentProject;
          MenuAction(recentProject, {this.openPath(recentProject)})
        })
      ).title_("open recent"),
      "File"
    );

    MainMenu.register(MenuAction(), "Edit");
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
  *openPath { arg path;
    this.setPaths(path);
		this.load(path);
  }
	
	*open { arg payload;
		Dialog.openPanel(
			{ |path|
        this.openPath(path)
			},
			path: saveDir
		);
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
