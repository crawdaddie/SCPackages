ItemRow {
  var <view;
  var <menuActions;
  *new { arg item ... columns;
    ^super.new.init(item, *columns)
  }

  init { arg item ... columns;
    view = View().layout_(
      HLayout(
        *columns.collect({ arg val; StaticText().string_(val) })
      )
    )
    .beginDragAction_({ arg v; item.value() });
    ^this
  }
  menuActions_ { arg actions; 
    view.setContextMenuActions(*actions.collect({ arg action; MenuAction(action[0], action[1])}));
    ^this
  }
}

ExpandableList {
  var <view;
  *new { arg parent, header, children;
    ^super.new.init(parent, header, children);
  }
  init { arg parent, headerText, children;
    var open, layout, header;
    open = false;

    layout = VLayout(*children);

    view = View().layout_(layout);
    header = StaticText().string_(headerText)
      .mouseDownAction_({
        open = open.not;
        view.visible_(open);
        view.refresh;
      });
    header.font = Font(bold: true); 
    
    parent.add(header);
    parent.add(view);
  }
}

AssetView {
	var <view;
	var <srcView;
	var <sfView;
	var <synthdefView;

	*new {
		^super.new.init();
	}

	getSrcViews { arg parent;
		var srcDir = Project.srcDir;
		var srcModule, itemCallback, traverse;
    var views = [];

		srcModule = Mod.new(srcDir);
		itemCallback = { arg item;
      try {
				item.md !? { arg md;
          views = views.add(
            ItemRow(item, item.md.memberKey).menuActions_([
              ["edit",  {Document.open(md.path)}]
            ]).view
          )
        }
			} { arg e;
        //
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
    ExpandableList(parent, "src", views);
		^views;
	}

	getSfViews { arg parent;
		var dataDir = Project.dataDir;
		var views = (dataDir ++ "/*").pathMatch.collect { arg path;
      ItemRow((soundfile: path), path.basename)
        .menuActions_([
          ["edit", { SoundFile.use(path, { arg sf; SoundfileEditor(sf) })}]
        ])
        .view
		};
    ExpandableList(parent, "soundfiles", views);
	}

	getSynthdefViews { arg parent;
    var views;
		views = SynthDescLib.global.synthDescs.values.select(_.find("system_").notNil).collect { arg synthDesc;
			ItemRow((synthDef: synthDesc.name), synthDesc.name).view;
		};
    ExpandableList(parent, "synthdefs", views);
	}


	init {
		var srcDir, dataDir;
		var srcView;
		var sfView;
		var synthDefView;
    var layout = VLayout();
    var scroll = ScrollView(bounds:Rect(0,0,300,400).left_(Window.availableBounds.left));
    var canvas = View();
    var actionManager = ProjectKeyActionManager();

		srcDir = Project.srcDir;
		dataDir = Project.dataDir;
    

    this.getSrcViews(layout);
    this.getSfViews(layout);
    this.getSynthdefViews(layout);
    scroll.keyDownAction = { arg ... args;
      if (scroll.hasFocus) {
        actionManager.keyDownAction(*args);
      }
    };
    canvas.layout = layout;
    scroll.canvas = canvas;
    scroll.name = format("% (%)", Project.projectDir.basename, Project.projectFile.basename);
    scroll.front;
	}
}
