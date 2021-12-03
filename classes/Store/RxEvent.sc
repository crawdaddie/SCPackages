RxEvent : Event {
	var <>metadata;
	
	dispatch { arg type, payload;
		// implement this to act as an event-source
		Dispatcher(type, payload, this)
	}

	listen { arg type, fn;
		Dispatcher.addListener(type, this, { arg payload;
			if (payload.id == this.at('id')) {
				fn.value(payload)
			}
		})
	}

	*new { arg event;
		if (event.isKindOf(RxEvent)) {
			^event;
		};
    if (event.notNil, {
      ^super.new().init(event);
    })
		^super.new().init(event)
	}

	init { arg event;
    this.parent_(event.parent);
    proto = event.proto;
    this.putAll(event, false);
		know = true;
  }

  reloadFromMetadata {
    super.at('md') !? { arg md;
      var parentMod = md.path.asSymbol.asModule;
      var memberKey = md.memberKey;
      parent = parentMod[memberKey];
    }
  }

	put { arg key, value, dispatch = true;
		var originalValue = this.at(key);

		super.put(key, value);

		if (dispatch && (originalValue != value)) {
      this.dispatch(
			  type: Topics.objectUpdated,
				payload: (id: this.id).put(key, value),
			)
		};
		^this;
	}

	putAll { arg dictionary, dispatch = true;
		var updates = ();

		dictionary.keysValuesDo { arg key, value;
			if (this.at(key) != value) {
				updates.put(key, value);
			};
			
			super.put(key, value);
		};


		if ((updates.size > 0) && dispatch) {
			this.dispatch(
				type: Topics.objectUpdated,
				payload: updates.putAll((id: this.id))
			)
		}
	}

	id {
		^this['id']
	}
  
  copyAsEvent {
    var newEvent = ().putAll(this);
    newEvent.id = nil; 
    newEvent.parent_(this.parent);
    ^newEvent;
  }

  play { arg storeCtx = (), clock;
    var playEvent = this.copy;
    var mod = storeCtx.modulePath !? { arg p; Mod(p) };
    playEvent.use {
      ~modCtx = mod;
      ~clock = clock; 
      currentEnvironment[\play].value();
    }
  }
}


