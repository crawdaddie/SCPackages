// + Store {
// 	getParentStore {
// 		var path = this.getPath;
// 		if (path.size > 1) {
// 			^Store.at(path[ path.size - 2 ]);
// 		} {
// 			^global
// 		};
// 	}

// 	*getPath { arg id;
// 		^lookups[id];
// 	}

// 	getPath {
// 		^lookups[id];
// 	}

// 	*setPath { arg id, path;
// 		if (id.class == Integer) {
// 			lookups[id] = path;
// 		}
// 	}

// 	*archive { arg path;
// 		global.writeMinifiedTextArchive(path);
// 	}

// 	*readFromArchive { arg path;
// 		global = path.load;
// 		global.init();
// 		this.resetPaths;
// 	}

// 	*resetPaths {
// 		var pathTraverse = { arg store, maxArchiveId, path;
// 			store.keysValuesDo { arg id, value;
// 				if (id.class == Integer) {
// 					var newPath = path ++ [id];
// 					maxArchiveId = max(maxArchiveId, id);
// 					this.setPath(id, newPath);
				
// 					if (value.class == Store) {
// 						pathTraverse.value(value, maxArchiveId, newPath)
// 					};

// 					if (value.class == RxEvent) {
// 						store.orderedItems.add(value);
// 					}
// 				}
// 			};
// 			maxArchiveId;
// 		};
// 		lastId = pathTraverse.value(global, 0, []);
// 	}	
// }