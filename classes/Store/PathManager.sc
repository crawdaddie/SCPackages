PathManager {
	var lastId;
	var lookups;
	classvar <initialId = 1000;

	*new {
		^super.new.init()
	}

	init {
		lookups = Dictionary();
		lastId = initialId;		
	}

	getId {
		lastId = lastId + 1;
		^lastId;
	}

	getPath { arg id;
		^lookups[id]
	}

	setPath { arg id, path;
		if (id.class == Integer) {
			lookups[id] = path;
		}
	}
	setChildPath { arg childId, parentId;
		var parentPath = parentId !? { this.getPath(parentId) } ?? [];
		this.setPath(childId, parentPath ++ [childId])
	}

	printLookups {
		lookups.postln;
	}
  
  traverseStore { arg store, cb, currentPath = [];
    store.keysValuesDo { arg key, value;
      if (key.class == Integer) {
        var path = currentPath ++ [key];
        cb.value(path, value);
        if (value.class == Store) {
          this.traverseStore(value, cb, path);
        }
      }
    }
  }
  
  resetPaths { arg store;
		var maxArchiveId = initialId;
    this.traverseStore(store: store, cb: { arg path, value; 
			var id = path[ path.size -1 ];
      value.updateAfterLoadFromArchive;
			if (id.class == Integer) {
				maxArchiveId = max(maxArchiveId, id);
				this.setPath(id, path);
			};
		});	
		lastId = maxArchiveId;
  }
}


