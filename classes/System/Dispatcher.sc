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

	*new { arg type, payload, eventSource;
		var typeListeners = listeners.at(type) ?? Dictionary();
		
		{
			eventSource !? {
				typeListeners = typeListeners.select { arg item;
					item != eventSource; // do not want to recursively trigger oneself
				}
			};

			if (debug) {
				payload.postln;
			};

			typeListeners.keysValuesDo { arg listeningObject, listener;
				listener.value(payload, listeningObject);
			};
		}.fork(AppClock)
	}

	*connectObject { arg object ...methods;
		methods.do { arg method;
			this.addListener(
				method,
				object, 
				{ arg payload;
					object.perform(method, payload);
				}
			)
		}
	}
}