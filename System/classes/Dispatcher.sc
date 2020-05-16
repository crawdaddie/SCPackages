Dispatcher {

	classvar <listeners;

	*initClass {
		listeners = Dictionary();
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

		typeListeners.keysValuesDo { arg listeningObject, listener;
			listener.value(event.payload, listeningObject);
		};
	}
}