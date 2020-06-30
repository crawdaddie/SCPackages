Dispatcher {

	classvar <listeners;
	classvar <>debug;

	*initClass {
		listeners = Dictionary();
		debug = false;
	}

	*addListener { arg type, object, listener;
		listeners.at(type) !? _.put(object, listener) ?? {
			listeners.put(type, Dictionary.with(object -> listener));
		}
	}

	*removeListener { arg type, object;
		listeners.at(type) !? _.removeAt(object);
	}

	*removeListenersForObject { arg object;
		listeners.keysValuesDo { arg key, typeListener;
			typeListener.removeAt(object);
		}
	}

	*new { arg event;
		var type = event.type;
		var typeListeners = listeners.at(type) ?? Dictionary();

		if (debug) {
			event.postln;
		};

		typeListeners.keysValuesDo { arg listeningObject, listener;
			listener.value(event.payload, listeningObject);
		};
	}

	*connectObject { arg object;
		object.keysValuesDo({ arg key, listener;
			if (listener.class == Function) {
				Dispatcher.addListener(
					key,
					object,
					{ arg payload;
						listener.value(object, payload);
					},
				)
			}
		});
	}
}