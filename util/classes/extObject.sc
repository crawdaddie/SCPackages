+ Object {
	debugln {
		thisProcess.nowExecutingPath.postln;
		this.postln;
	}

	postTree { arg tabs = 0;
		tabs.do({ Char.tab.post });
		this.asString.postln;
	}
}

+ String {
	postTree { arg tabs = 0;
		tabs.do({ Char.tab.post });
		this.postln;
	}
}

+ Dictionary {
	postTree { arg tabs = 0;
		"".postln;
		this.keysValuesDo({ arg key, value;
			if (key.class == Symbol) {
				tabs.do({ Char.tab.post });
				
				key.post;
				": ".post;
				value.postTree;	
			}
		});
			
		this.keysValuesDo({ arg key, value;
			if (key.class == Integer) {
				tabs.do({ Char.tab.post });
				
				key.asString.post;
				": ".post;
				value.postTree;
			}
		});
	}
}
