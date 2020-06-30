Promise {
	// var <state;
	// var <result = nil;

	// var <handlers;
	// var <onResolve, <onReject;

	// *new { arg fn, reject, execute = true;
	// 	^super.new.init(fn, reject, execute)
	// }

	// init { arg fn, reject, execute;
		
	// 	onResolve = fn;
		
	// 	reject !? {
	// 		onReject = reject;
	// 	};

	// 	state = 'PENDING';

	// 	if (execute) {
	// 		this.execute();
	// 	}
	// }

	// execute {
	// 	Routine({
	// 		try {
	// 			result = onResolve.value();
	// 			handlers.do { |object|
	// 				object.resolve(result);
	// 			};
	// 			state = 'RESOLVED';
	// 		} { |error|
	// 			result = error;
	// 			handlers.do { |object|
	// 				object.reject(error);
	// 			};
	// 			state = 'REJECTED'
	// 		}
	// 	}).play(AppClock);		
	// }

	// then { arg fn, reject;
	// 	^switch (state)
	// 		{'PENDING'} {
	// 			var newPromise = Promise(fn, reject, false);
	// 			handlers.add(newPromise);
	// 			newPromise;
	// 		}
	// 		{'RESOLVED'} {
	// 			fn !? {
	// 				fn.value(result)
	// 			} ?? result;
	// 		}
	// 		{'REJECTED'} {
	// 			reject !? { |reject|
	// 				reject.value(result)
	// 			} ?? result
	// 		};
	// }

	// resolve { arg result;
	// 	state = 'RESOLVED';
	// 	onResolve.value(result);
	// }

	// reject { arg error;
	// 	state = 'REJECTED';
	// 	onReject.value(error);
	// }

}