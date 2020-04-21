StoreBlock : SequenceableBlock {
	
	*new { arg event, zoom = Point(1, 1);
		^super.new(event, zoom).initStoreView(event);
	}

	initStoreView { arg event;
		label = "store - %".format(event.id);
	}

	edit {
		var path = Store.getPath(id);
		var canvas = SequencerCanvas.fromStore(id);
		canvas.parent.name = "store - %".format(path);
		^canvas;
	}
}