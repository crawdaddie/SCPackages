StoreBlock : SequenceableBlock {
	
	*new { arg event, zoom = Point(1, 1);
		^super.new(event, zoom).initStoreView(event);
	}

	initStoreView { arg event;
		label = "store - %".format(event.id);
	}

	edit {
		var canvas;
		super.edit();
		canvas = SequencerCanvas.fromStore(Store.at(id));
		canvas.parent.name = "store - %".format(id);
		^canvas;
	}
}