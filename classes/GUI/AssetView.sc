AssetView {
	var <view;
	var <srcView;
	var <sfView;
	var <synthdefView;

	*new {
		^super.new.init();
	}

	getTextView { arg text;
		var v = View();
		v.addFLowLayout;

		StaticText(parent: v).string_(text);
		v.mouseDownAction = { arg v;
			text.postln;
		};
		^v;
	}

	getSrcView {
		var srcDir = Project.srcDir;
		var srcModule, itemCallback, traverse;

		srcView = view.addItem(["src"]);
		srcModule = Mod.new(srcDir);
		
		itemCallback = { arg item;
			try {
				item.md !? { arg md;
					srcView.addChild([md.memberKey, md.path])
						.setView(0,
							View()
								.children_([StaticText().string_(md.memberKey)])
								.mouseDownAction_({ md.memberKey.postln })
						)
						.setView(1,
							View()
								.children_([StaticText().string_(md.path)])
								.mouseDownAction_({ md.path.postln })
						)
				}
			} { arg e;
				e.postln;
			}
		};

		traverse = { arg mod;
			mod.keysValuesDo { arg key, item;
				if (item.class == Mod) {
					traverse.value(item);
				} {
					itemCallback.value(item);
				}
			}
		};

		traverse.value(srcModule);
		^srcView;
	}

	getSfView {
		var dataDir = Project.dataDir;
		sfView = view.addItem(["soundfiles"]);

		(dataDir ++ "/*").pathMatch.do { arg path;
			sfView.addChild([path.basename, path]);
		};
		^sfView;
	}

	getSynthdefView {
		synthdefView = view.addItem(["synthdefs"]);

		SynthDescLib.global.synthDescs.values.select(_.find("system_").notNil).do { arg synthDesc;
			synthdefView.addChild([synthDesc.name]);
		};
	}


	init {
		var srcDir, dataDir;

		var srcView;
		var sfView;
		var synthDefView;

		srcDir = Project.srcDir;
		dataDir = Project.dataDir;

		view = TreeView();
		view.canSort_(true);
		view.columns_(["name", "path"]);

		this.getSrcView;
		this.getSfView;
		this.getSynthdefView;
		view.front;

	}
}