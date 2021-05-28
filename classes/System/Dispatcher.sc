Dispatcher {

	classvar <listeners;
	classvar matchAnyListeners;
	classvar <dispatching;
	
	*initClass {
		listeners = Dictionary();
		dispatching = false;
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
		listeners.at('*') !? { arg anyMatchers;
			typeListeners = typeListeners.putAll(anyMatchers);	
		};

		// dispatching = true;
		{
			eventSource !? {
				typeListeners = typeListeners.select { arg item;
					item != eventSource; // do not want to recursively trigger oneself
				}
			};

			typeListeners.keysValuesDo { arg listeningObject, listener;
				listener.value(payload, listeningObject, type);
			};
			// dispatching = false;
		}.fork(AppClock)
	}

	*debug { arg debugValue = true;
		if (debugValue, {
			this.addListener('*', this, { arg payload, listeningObject, type;
				[ type, payload, listeningObject ].postln;
			});

		}, {
			this.removeListenersForObject(this);
		});

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
